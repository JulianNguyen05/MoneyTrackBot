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

from ..models import Wallet, Category, Transaction, ChatHistory

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

        # Lưu tin nhắn User
        ChatHistory.objects.create(user=user, role='user', message=message)

        if model is None: return Response({"reply": "Lỗi AI Server."}, status=503)

        try:
            # Lấy data ngữ cảnh
            wallets_qs = Wallet.objects.filter(user=user).values("id", "name", "balance")
            wallets = [{"id": w["id"], "name": w["name"], "balance": float(w["balance"])} for w in wallets_qs]
            categories = list(Category.objects.filter(user=user).values("id", "name", "type"))

            # Lấy 10 tin gần nhất để AI hiểu ngữ cảnh (VD: vừa nhập xong thì muốn sửa)
            recent_chats = ChatHistory.objects.filter(user=user).order_by('-created_at')[:10]
            history_context = "\n".join([f"{chat.role}: {chat.message}" for chat in reversed(recent_chats)])

            prompt = self.build_prompt(message, wallets, categories, history_context)

            generation_config = genai.types.GenerationConfig(
                response_mime_type="application/json", temperature=0.2
            )
            response = model.generate_content(prompt, generation_config=generation_config)

            print("🤖 AI RESPONSE (JSON)\n" + "-" * 70 + f"\n{response.text}\n" + "-" * 70)

            ai_data = json.loads(response.text)
            reply_message = ai_data.get("reply", "Đã xử lý.")
            action = ai_data.get("action")

            # --- XỬ LÝ ACTION ---

            final_reply = reply_message  # Mặc định là lời thoại của AI

            if action == "create_transaction":
                final_reply = self.create_transaction_from_ai(user, ai_data.get("data"))

            elif action == "create_wallet":
                final_reply = self.handle_create_wallet(user, ai_data.get("data", {}))

            elif action == "create_category":
                final_reply = self.handle_create_category(user, ai_data.get("data", {}))

            # [NEW] Action sửa ví cho giao dịch vừa nhập
            elif action == "switch_wallet":
                final_reply = self.handle_switch_wallet(user, ai_data.get("data", {}))

            elif action == "answer_question":
                final_reply = self.handle_answer_question(user, ai_data)

            # Lưu câu trả lời của Bot (hoặc kết quả hành động) vào DB
            ChatHistory.objects.create(user=user, role='model', message=final_reply)

            return Response({"reply": final_reply})

        except google_exceptions.ResourceExhausted:
            return Response({"reply": "Bot quá tải. Thử lại sau 1 phút! ⏳"}, status=429)
        except Exception as e:
            print(f"❌ ERROR: {str(e)}")
            return Response({"reply": f"Lỗi xử lý: {str(e)}"}, status=500)

    def build_prompt(self, message, wallets, categories, history_context):
        today = datetime.date.today().strftime("%Y-%m-%d")
        return f"""
            Bạn là MoneyTrack Bot. Hôm nay: {today}.
            DỮ LIỆU CỦA USER:
            - Ví: {json.dumps(wallets, ensure_ascii=False)}
            - Danh mục: {json.dumps(categories, ensure_ascii=False)}

            LỊCH SỬ HỘI THOẠI:
            {history_context}

            NHIỆM VỤ: Phân tích tin nhắn và trả về JSON hành động.

            LOGIC XỬ LÝ QUAN TRỌNG:
            1. **Tạo giao dịch**: Nếu user nói "ăn 20k", "lương 10tr" -> action="create_transaction".
            2. **Chuyển ví / Sửa ví**: Nếu user vừa nhập giao dịch xong, sau đó nói "ghi vào ví khác", "tạo ví ABC rồi ghi vào đó", "nhầm ví rồi" -> action="switch_wallet". 
               Lúc này bạn trích xuất tên ví mới vào data.
            3. **Tạo ví**: Chỉ tạo ví đơn thuần nếu không có ngữ cảnh sửa giao dịch.
            4. **Hỏi đáp**: action="answer_question".

            FORMAT JSON OUTPUT:
            {{
                "action": "create_transaction" | "create_wallet" | "create_category" | "switch_wallet" | "answer_question",
                "data": {{ 
                    "wallet_id": int (nếu có),
                    "target_wallet_name": "string (dùng cho switch_wallet/create_wallet)",
                    "category_id": int, 
                    "amount": float, 
                    "description": "string",
                    "date": "YYYY-MM-DD",
                    "name": "string",
                    "type": "income"|"expense"
                }},
                "reply": "Câu trả lời tự nhiên cho user (Tiếng Việt)"
            }}
            Tin nhắn user: "{message}"
        """

    def handle_switch_wallet(self, user, data):
        """
        Logic: Tìm giao dịch mới nhất của user -> Tạo ví mới (nếu cần) -> Update ví cho giao dịch đó
        """
        target_wallet_name = data.get("target_wallet_name")
        if not target_wallet_name: return "❌ Không xác định được tên ví muốn chuyển tới."

        try:
            with transaction.atomic():
                # 1. Tìm hoặc tạo ví đích
                target_wallet, created = Wallet.objects.get_or_create(
                    user=user,
                    name=target_wallet_name,
                    defaults={'balance': 0}
                )

                # 2. Tìm giao dịch cuối cùng user vừa nhập (sắp xếp theo ID giảm dần)
                last_trans = Transaction.objects.filter(user=user).order_by('-id').first()

                if not last_trans:
                    return f"❌ Không tìm thấy giao dịch nào gần đây để chuyển sang ví {target_wallet_name}."

                # 3. Update ví
                old_wallet_name = last_trans.wallet.name
                last_trans.wallet = target_wallet
                last_trans.save()

                msg_create = f"Đã tạo ví mới **{target_wallet_name}**. " if created else ""
                return f"✅ {msg_create}Đã chuyển giao dịch '{last_trans.description}' ({last_trans.amount:,.0f}đ) từ ví {old_wallet_name} sang ví **{target_wallet_name}**."

        except Exception as e:
            return f"❌ Lỗi khi chuyển ví: {str(e)}"

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
            wallet_id = data.get("wallet_id")
            # Nếu AI không tìm thấy ví, tự lấy ví đầu tiên hoặc ví có nhiều tiền nhất
            if not wallet_id:
                first_wallet = Wallet.objects.filter(user=user).first()
                if not first_wallet: return "❌ Bạn chưa có ví nào. Hãy tạo ví trước."
                wallet = first_wallet
            else:
                wallet = Wallet.objects.get(id=wallet_id, user=user)

            category = Category.objects.get(id=data["category_id"], user=user)

            description = data.get("description") or category.name
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

            prefix = "-" if category.type == 'expense' else "+"
            return f"✅ Đã ghi: {description} ({prefix}{new_t.amount:,.0f}đ) vào ví **{wallet.name}**."
        except Exception as e:
            return f"❌ Lỗi ghi giao dịch: {str(e)}"

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