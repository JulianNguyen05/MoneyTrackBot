import datetime
import json
from decimal import Decimal

import google.generativeai as genai
from google.api_core import exceptions as google_exceptions

from django.conf import settings
from django.db import transaction
from django.db.models import Sum
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import permissions, status

from ..models import Wallet, Category, Transaction

model = None
try:
    genai.configure(api_key=settings.GEMINI_API_KEY)

    my_models = [m.name for m in genai.list_models() if 'generateContent' in m.supported_generation_methods]

    priority_list = [
        "gemini-1.5-flash", "gemini-1.5-flash-latest", "gemini-1.5-flash-001",
        "gemini-flash-latest", "gemini-2.0-flash-exp", "gemini-pro"
    ]

    selected_model = None
    for priority in priority_list:
        for m in my_models:
            if priority in m:
                selected_model = m
                break
        if selected_model: break

    if not selected_model and my_models: selected_model = my_models[0]

    if selected_model:
        print(f"✅ [Chatbot] Model: {selected_model}")
        model = genai.GenerativeModel(selected_model)
    else:
        print("❌ [Chatbot] Không tìm thấy model.")

except Exception as e:
    print(f"❌ [Chatbot] Lỗi khởi tạo: {str(e)}")


class ChatbotView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, *args, **kwargs):
        user = request.user
        message = request.data.get("message", "").strip()

        print("\n" + "=" * 70)
        print(f"👤 USER ({user.username}): {message}")
        print("=" * 70)

        if not message: return Response({"reply": "Tin nhắn rỗng"}, status=400)
        if model is None: return Response({"reply": "Lỗi AI Server."}, status=503)

        try:
            wallets_qs = Wallet.objects.filter(user=user).values("id", "name", "balance")
            wallets = [{"id": w["id"], "name": w["name"], "balance": float(w["balance"])} for w in wallets_qs]
            categories = list(Category.objects.filter(user=user).values("id", "name", "type"))

            prompt = self.build_prompt(message, wallets, categories)

            generation_config = genai.types.GenerationConfig(
                response_mime_type="application/json", temperature=0.2
            )
            response = model.generate_content(prompt, generation_config=generation_config)

            print("🤖 AI RESPONSE (JSON)\n" + "-" * 70 + f"\n{response.text}\n" + "-" * 70)

            ai_data = json.loads(response.text)
            action = ai_data.get("action")
            reply_message = ai_data.get("reply", "Đã xử lý.")

            if action == "create_transaction":
                msg = self.create_transaction_from_ai(user, ai_data.get("data"))
                return Response({"reply": msg})

            if action == "create_wallet":
                msg = self.handle_create_wallet(user, ai_data.get("data", {}))
                return Response({"reply": msg})

            if action == "create_category":
                msg = self.handle_create_category(user, ai_data.get("data", {}))
                return Response({"reply": msg})

            if action == "answer_question":
                msg = self.handle_answer_question(user, ai_data)
                return Response({"reply": msg})

            return Response({"reply": reply_message})

        except google_exceptions.ResourceExhausted:
            return Response({"reply": "Bot quá tải (Hết lượt free). Thử lại sau 1 phút! ⏳"}, status=429)
        except Exception as e:
            print(f"❌ ERROR: {str(e)}")
            return Response({"reply": "Lỗi xử lý hệ thống."}, status=500)

    def build_prompt(self, message, wallets, categories):
        today = datetime.date.today().strftime("%Y-%m-%d")
        return f"""
            Bạn là MoneyTrack Bot. Hôm nay: {today}.
            Ví hiện có: {json.dumps(wallets)}
            Danh mục hiện có: {json.dumps(categories)}

            NHIỆM VỤ: Trả về JSON chuẩn.
            Phân tích tin nhắn người dùng để thực hiện:
            1. Tạo giao dịch (thu/chi): action="create_transaction"
            2. Tạo ví mới (VD: "tạo ví Momo"): action="create_wallet", data: {{"name": str}}
            3. Tạo danh mục (VD: "thêm danh mục Ăn uống loại chi"): action="create_category", data: {{"name": str, "type": "income"|"expense"}}
            4. Hỏi đáp: action="answer_question"

            Format JSON:
            {{
                "action": "create_transaction" | "create_wallet" | "create_category" | "answer_question" | "error_validation",
                "query_type": "...",
                "data": {{ 
                    "wallet_id": int, 
                    "category_id": int, 
                    "amount": float, 
                    "description": "string",
                    "date": "YYYY-MM-DD",
                    "name": "string",
                    "balance": float,
                    "type": "income"|"expense"
                }},
                "reply": "Câu trả lời ngắn gọn xác nhận hành động",
                "answer": "Câu trả lời chi tiết"
            }}
            Tin nhắn: "{message}"
        """

    def handle_create_wallet(self, user, data):
        try:
            name = data.get("name")
            balance = data.get("balance", 0)
            if not name: return "❌ Vui lòng cung cấp tên ví."

            wallet = Wallet.objects.create(user=user, name=name, balance=balance)
            return f"✅ Đã tạo ví **{wallet.name}** thành công với số dư {wallet.balance:,.0f}đ."
        except Exception as e:
            return f"❌ Lỗi tạo ví: {str(e)}"

    def handle_create_category(self, user, data):
        try:
            name = data.get("name")
            c_type = data.get("type", "expense")  # Mặc định là chi
            if not name: return "❌ Vui lòng cung cấp tên danh mục."

            category, created = Category.objects.get_or_create(
                user=user, name=name, defaults={'type': c_type}
            )
            if not created: return f"ℹ️ Danh mục '{name}' đã tồn tại."

            type_str = "Thu nhập" if c_type == "income" else "Chi tiêu"
            return f"✅ Đã tạo danh mục **{name}** ({type_str}) thành công."
        except Exception as e:
            return f"❌ Lỗi tạo danh mục: {str(e)}"

    def create_transaction_from_ai(self, user, data):
        try:
            with transaction.atomic():
                wallet = Wallet.objects.get(id=data["wallet_id"], user=user)
                category = Category.objects.get(id=data["category_id"], user=user)

                description = data.get("description") or data.get("note") or category.name
                description = description.strip().capitalize()

                raw_amount = Decimal(str(data["amount"]))
                amount_val = abs(raw_amount)

                new_t = Transaction.objects.create(
                    user=user,
                    wallet=wallet,
                    category=category,
                    amount=amount_val,
                    date=data.get("date", datetime.date.today()),
                    description=description
                )

                display_amount = f"{new_t.amount:,.0f}"
                if category.type == 'expense':
                    display_amount = f"-{display_amount}"
                else:
                    display_amount = f"+{display_amount}"

                return f"✅ Đã ghi: {new_t.description} ({display_amount}đ) vào ví {wallet.name}"
        except Exception as e:
            return f"❌ Lỗi: {str(e)}"

    def handle_answer_question(self, user, ai_data):
        q_type = ai_data.get("query_type")
        if q_type == "list_wallets":
            ws = Wallet.objects.filter(user=user)
            if not ws: return "Bạn chưa có ví nào."
            return "Danh sách ví:\n" + "\n".join([f"- {w.name}: {w.balance:,.0f}đ" for w in ws])

        if q_type == "get_wallet_balance":
            total = sum(w.balance for w in Wallet.objects.filter(user=user))
            return f"Tổng tài sản: {total:,.0f}đ"

        return ai_data.get("answer") or "Chào bạn!"