import datetime
import json
from decimal import Decimal

# Google Gemini AI
import google.generativeai as genai

# Django
from django.conf import settings
from django.db import transaction
from django.db.models import Sum
from django.utils import timezone
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import permissions, status

# Models
from ..models.wallet import Wallet
from ..models.category import Category
from ..models.transaction import Transaction


# ==========================================================
# 🔑 CẤU HÌNH GEMINI API
# ==========================================================
try:
    genai.configure(api_key=settings.GEMINI_API_KEY)
    model = genai.GenerativeModel("models/gemini-2.5-flash")
    print("✅ [Chatbot] Kết nối Google Gemini API thành công")
except Exception as e:
    print("❌ [Chatbot] Không thể kết nối Gemini API")
    print(str(e))
    model = None


# ==========================================================
# 💬 CHATBOT API
# ==========================================================
class ChatbotView(APIView):
    """
    API Chatbot xử lý ngôn ngữ tự nhiên:
    - Tạo giao dịch
    - Truy vấn chi tiêu
    - Quản lý ví, danh mục
    """
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, *args, **kwargs):
        user = request.user
        message = request.data.get("message", "").strip()

        # =============================
        # LOG: USER INPUT
        # =============================
        print("\n" + "=" * 70)
        print(f"👤 USER ({user.username}): {message}")
        print("=" * 70)

        if not message:
            return Response({"reply": "Tin nhắn rỗng"}, status=status.HTTP_400_BAD_REQUEST)

        if model is None:
            return Response(
                {"reply": "Bot AI chưa sẵn sàng. Vui lòng kiểm tra API Key."},
                status=status.HTTP_503_SERVICE_UNAVAILABLE
            )

        try:
            # =============================
            # (1) LOAD CONTEXT USER
            # =============================
            wallets = list(
                Wallet.objects.filter(user=user).values("id", "name")
            )
            categories = list(
                Category.objects.filter(user=user).values("id", "name", "type")
            )

            # =============================
            # (2) BUILD PROMPT
            # =============================
            prompt = self.build_prompt(message, wallets, categories)

            # =============================
            # (3) CALL GEMINI
            # =============================
            generation_config = genai.types.GenerationConfig(
                response_mime_type="application/json"
            )
            response = model.generate_content(
                prompt, generation_config=generation_config
            )

            # =============================
            # LOG: AI RAW RESPONSE
            # =============================
            print("🤖 AI RAW RESPONSE (JSON)")
            print("-" * 70)
            print(response.text)
            print("-" * 70)

            # =============================
            # (4) PARSE JSON
            # =============================
            ai_data = json.loads(response.text)
            action = ai_data.get("action")
            reply_message = ai_data.get("reply", "Đã xử lý xong.")

            print(f"⚙️ AI ACTION: {action}")

            # ==================================================
            # (A) CREATE TRANSACTION
            # ==================================================
            if action == "create_transaction":
                self.create_transaction_from_ai(user, ai_data.get("data"))
                print(f"💬 BOT REPLY: {reply_message}")
                return Response({"reply": reply_message})

            # ==================================================
            # (B) VALIDATION ERROR
            # ==================================================
            if action == "error_validation":
                print(f"⚠️ BOT REPLY: {reply_message}")
                return Response({"reply": reply_message}, status=status.HTTP_400_BAD_REQUEST)

            # ==================================================
            # (C) ANSWER QUESTION
            # ==================================================
            if action == "answer_question":
                query_type = ai_data.get("query_type")
                data = ai_data.get("data", {})
                final_reply = ""

                if query_type == "total_expense_current_month":
                    now = timezone.now()
                    total = (
                        Transaction.objects.filter(
                            user=user,
                            category__type="expense",
                            date__year=now.year,
                            date__month=now.month
                        )
                        .aggregate(total=Sum("amount"))["total"]
                        or Decimal(0)
                    )
                    final_reply = f"Tổng chi tháng này của bạn là {abs(total):,.0f}đ."

                elif query_type == "get_wallet_balance":
                    wallet_id = data.get("wallet_id")
                    if wallet_id:
                        wallet = Wallet.objects.get(id=wallet_id, user=user)
                        final_reply = f"Số dư ví '{wallet.name}' là {wallet.balance:,.0f}đ."
                    else:
                        final_reply = "Bạn muốn hỏi số dư của ví nào?"

                elif query_type == "total_expense_specific_month":
                    month = data.get("month")
                    now = timezone.now()
                    total = (
                        Transaction.objects.filter(
                            user=user,
                            category__type="expense",
                            date__year=now.year,
                            date__month=month
                        )
                        .aggregate(total=Sum("amount"))["total"]
                        or Decimal(0)
                    )
                    final_reply = f"Tổng chi tháng {month} là {abs(total):,.0f}đ."

                else:
                    final_reply = "Tôi chưa hỗ trợ truy vấn này."

                print(f"💬 BOT REPLY: {final_reply}")
                return Response({"reply": final_reply})

            # ==================================================
            # (D) MANAGE WALLET
            # ==================================================
            if action == "manage_wallet":
                data = ai_data.get("data", {})
                sub_action = data.get("sub_action")

                if sub_action == "create":
                    Wallet.objects.create(
                        user=user,
                        name=data["name"],
                        balance=Decimal(str(data.get("balance", 0)))
                    )

                elif sub_action == "update":
                    wallet = Wallet.objects.get(id=data["wallet_id"], user=user)
                    if "new_name" in data:
                        wallet.name = data["new_name"]
                    if "new_balance" in data:
                        wallet.balance = Decimal(str(data["new_balance"]))
                    wallet.save()

                elif sub_action == "delete":
                    Wallet.objects.filter(id=data["wallet_id"], user=user).delete()

                print(f"💬 BOT REPLY: {reply_message}")
                return Response({"reply": reply_message})

            # ==================================================
            # (E) MANAGE CATEGORY
            # ==================================================
            if action == "manage_category":
                data = ai_data.get("data", {})
                sub_action = data.get("sub_action")

                if sub_action == "create":
                    Category.objects.create(
                        user=user,
                        name=data["name"],
                        type=data.get("type", "expense")
                    )
                elif sub_action == "delete":
                    Category.objects.filter(
                        id=data["category_id"], user=user
                    ).delete()

                print(f"💬 BOT REPLY: {reply_message}")
                return Response({"reply": reply_message})

            # ==================================================
            # (F) NORMAL CHAT
            # ==================================================
            print(f"💬 BOT REPLY: {reply_message}")
            return Response({"reply": reply_message})

        except Exception as e:

            error_message = str(e)

            print("❌ CHATBOT ERROR")

            print("-" * 70)

            print(error_message)

            print("-" * 70)

            # ===== HANDLE QUOTA ERROR =====

            if "429" in error_message or "Quota exceeded" in error_message:
                return Response(

                    {

                        "reply": (

                            "🤖 Bot đang tạm nghỉ do vượt giới hạn sử dụng AI miễn phí.\n"

                            "⏳ Bạn vui lòng thử lại sau khoảng 1 phút nhé!"

                        )

                    },

                    status=status.HTTP_429_TOO_MANY_REQUESTS

                )

            # ===== OTHER ERROR =====

            return Response(

                {"reply": "Xin lỗi, hệ thống chatbot đang gặp sự cố."},

                status=status.HTTP_500_INTERNAL_SERVER_ERROR

            )

    # ==================================================
    # PROMPT BUILDER
    # ==================================================
    def build_prompt(self, message, wallets, categories):
        today = datetime.date.today().strftime("%Y-%m-%d")
        return f"""
            Bạn là MoneyTrack Bot – trợ lý tài chính thông minh.
            Ngày: {today}. Ngôn ngữ: Tiếng Việt.
            
            Danh sách ví: {json.dumps(wallets)}
            Danh sách danh mục: {json.dumps(categories)}
            
            Chỉ trả về JSON duy nhất.
            
            Tin nhắn người dùng: "{message}"
            """

    # ==================================================
    # CREATE TRANSACTION
    # ==================================================
    def create_transaction_from_ai(self, user, data):
        with transaction.atomic():
            wallet = Wallet.objects.get(id=data["wallet_id"], user=user)
            category = Category.objects.get(id=data["category_id"], user=user)

            raw_amount = Decimal(str(data["amount"]))
            amount = -abs(raw_amount) if category.type == "expense" else abs(raw_amount)

            Transaction.objects.create(
                user=user,
                wallet=wallet,
                category=category,
                amount=amount,
                date=data.get("date", datetime.date.today()),
                description=data.get("description", category.name).capitalize()
            )

            wallet.balance += amount
            wallet.save(update_fields=["balance"])
