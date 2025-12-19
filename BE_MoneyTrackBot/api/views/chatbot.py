import datetime
import json
from decimal import Decimal

# Import thư viện Google AI
import google.generativeai as genai

# --- Django imports ---
from django.conf import settings
from django.db import transaction
from django.db.models import Sum
from django.utils import timezone
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import permissions, status

# --- Models ---
from ..models.wallet import Wallet
from ..models.category import Category
from ..models.transaction import Transaction

# --- (C) CẤU HÌNH API KEY ---
try:
    genai.configure(api_key=settings.GEMINI_API_KEY)
    model = genai.GenerativeModel('models/gemini-2.5-flash')  # Dùng mô hình Flash cho tốc độ
    print("✅ (Chatbot) Kết nối Google Gemini API thành công!")
except Exception as e:
    print(f"❌ (Chatbot) Lỗi: Không thể kết nối Gemini API. Kiểm tra API Key. Lỗi: {e}")
    model = None


# ==========================================================
# 💬 API: Chatbot (Mới - Dùng Google AI)
# ==========================================================
class ChatbotView(APIView):
    """
    API xử lý ngôn ngữ tự nhiên (dùng Google Gemini API)
    để tạo giao dịch và hỏi đáp.
    """
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, *args, **kwargs):
        user = request.user
        message = request.data.get('message', '').strip()

        if not message:
            return Response({"reply": "Tin nhắn rỗng"}, status=status.HTTP_400_BAD_REQUEST)
        if model is None:
            return Response({"reply": "Lỗi: Bot AI chưa sẵn sàng. Vui lòng kiểm tra API Key phía server."},
                            status=status.HTTP_503_SERVICE_UNAVAILABLE)

        try:
            # --- (1) Lấy "Kiến thức" (Context) của User ---
            wallets = list(Wallet.objects.filter(user=user).values('id', 'name'))
            categories = list(Category.objects.filter(user=user).values('id', 'name', 'type'))

            # --- (2) Xây dựng Câu lệnh (Prompt) cho AI ---
            prompt = self.build_prompt(message, wallets, categories)

            # --- (3) Gọi API Google AI ---
            generation_config = genai.types.GenerationConfig(
                response_mime_type="application/json"
            )
            response = model.generate_content(prompt, generation_config=generation_config)

            print("--- AI Raw Response ---")
            print(response.text)
            print("-----------------------")

            # --- (4) Xử lý JSON trả về ---
            ai_data = json.loads(response.text)
            action = ai_data.get("action")
            reply_message = ai_data.get("reply", "Tôi đã xử lý xong.")

            # --- (A) TẠO GIAO DỊCH ---
            if action == "create_transaction":
                data = ai_data.get("data")
                self.create_transaction_from_ai(user, data)
                return Response({"reply": reply_message})

            # --- (B) LỖI VALIDATION ---
            elif action == "error_validation":
                return Response({"reply": reply_message}, status=status.HTTP_400_BAD_REQUEST)

            # --- (C) TRẢ LỜI CÂU HỎI ---
            elif action == "answer_question":
                query_type = ai_data.get("query_type")
                data = ai_data.get("data", {})
                final_reply = ""

                if query_type == "total_expense_current_month":
                    now = timezone.now()
                    total = Transaction.objects.filter(
                        user=user,
                        category__type='expense',
                        date__year=now.year,
                        date__month=now.month
                    ).aggregate(total_sum=Sum('amount'))['total_sum'] or Decimal(0)
                    final_reply = f"Tổng chi tháng này của bạn là {abs(total):,.0f}đ."

                elif query_type == "get_wallet_balance":
                    wallet_id = data.get("wallet_id")
                    if wallet_id is None:
                        final_reply = "Xin lỗi, tôi không rõ bạn muốn hỏi số dư của ví nào."
                    else:
                        try:
                            wallet = Wallet.objects.get(id=wallet_id, user=user)
                            final_reply = f"Số dư trong ví '{wallet.name}' của bạn là {wallet.balance:,.0f}đ."
                        except Wallet.DoesNotExist:
                            final_reply = "Xin lỗi, tôi không tìm thấy ví đó trong tài khoản của bạn."

                elif query_type == "total_expense_specific_month":
                    month = data.get("month")
                    if month is None:
                        final_reply = "Bạn vui lòng nói rõ tháng nào nhé (ví dụ: 'tháng 10')."
                    else:
                        now = timezone.now()
                        total = Transaction.objects.filter(
                            user=user,
                            category__type='expense',
                            date__year=now.year,
                            date__month=month
                        ).aggregate(total_sum=Sum('amount'))['total_sum'] or Decimal(0)
                        final_reply = f"Tổng chi tháng {month} của bạn là {abs(total):,.0f}đ."
                else:
                    final_reply = "Tôi chưa hỗ trợ kiểu truy vấn này."

                return Response({"reply": final_reply})

            # --- (E) QUẢN LÝ VÍ (MỚI) ---
            elif action == "manage_wallet":
                data = ai_data.get("data", {})
                sub_action = data.get("sub_action")

                if sub_action == "create":
                    Wallet.objects.create(user=user, name=data['name'], balance=Decimal(str(data.get('balance', 0))))
                elif sub_action == "update":
                    wallet = Wallet.objects.get(id=data['wallet_id'], user=user)
                    if 'new_name' in data: wallet.name = data['new_name']
                    if 'new_balance' in data: wallet.balance = Decimal(str(data['new_balance']))
                    wallet.save()
                elif sub_action == "delete":
                    Wallet.objects.filter(id=data['wallet_id'], user=user).delete()

                return Response({"reply": reply_message})

            # --- (F) QUẢN LÝ DANH MỤC (MỚI) ---
            elif action == "manage_category":
                data = ai_data.get("data", {})
                sub_action = data.get("sub_action")

                if sub_action == "create":
                    Category.objects.create(user=user, name=data['name'], type=data.get('type', 'expense'))
                elif sub_action == "delete":
                    Category.objects.filter(id=data['category_id'], user=user).delete()

                return Response({"reply": reply_message})

            # --- (G) GIAO TIẾP TỰ NHIÊN ---
            elif action == "normal_chat":
                return Response({"reply": reply_message})

            # --- (D) KHÔNG HIỂU ---
            else:
                return Response({"reply": ai_data.get("reply", "Xin lỗi, tôi chưa hiểu ý bạn.")})

        except Exception as e:
            print(f"--- AI API Error --- \n{e}\n------------------")
            return Response({"reply": f"Xin lỗi, Bot AI đang gặp lỗi: {str(e)}"})

    def build_prompt(self, message, wallets, categories):
        wallets_str = json.dumps(wallets)
        categories_str = json.dumps(categories)
        today_str = datetime.date.today().strftime('%Y-%m-%d')

        prompt = f"""
        Bạn là MoneyTrack Bot - một trợ lý tài chính thông minh, thân thiện. Ngày: {today_str}. Ngôn ngữ: Tiếng Việt.

        Kiến thức hệ thống:
        1. Danh sách Ví: {wallets_str}
        2. Danh sách Danh mục: {categories_str}

        Nhiệm vụ: Phân tích tin nhắn và trả về JSON duy nhất.

        --- 
        KỊCH BẢN 1: TẠO GIAO DỊCH (create_transaction)
        {{ "action": "create_transaction", "reply": "thông báo", "data": {{ "amount": float, "date": "{today_str}", "description": "string", "wallet_id": int, "category_id": int }} }}

        KỊCH BẢN 2: TRUY VẤN (answer_question)
        - query_type: "total_expense_current_month", "get_wallet_balance", "total_expense_specific_month"

        KỊCH BẢN 3: QUẢN LÝ VÍ (manage_wallet)
        - Tạo ví: "tạo ví mới tên Tiền mặt có 1 triệu" -> sub_action: "create", name: "Tiền mặt", balance: 1000000
        - Sửa ví: "đổi tên ví MoMo thành MoMo Pay" -> sub_action: "update", wallet_id: (id), new_name: "MoMo Pay"
        - Xóa ví: "xóa ví Thẻ đi" -> sub_action: "delete", wallet_id: (id)

        KỊCH BẢN 4: QUẢN LÝ DANH MỤC (manage_category)
        - Tạo: "thêm mục Tiền nhà loại chi tiêu" -> sub_action: "create", name: "Tiền nhà", type: "expense"
        - Xóa: "xóa danh mục Ăn uống" -> sub_action: "delete", category_id: (id)

        KỊCH BẢN 5: GIAO TIẾP (normal_chat) - Chào hỏi, tán gẫu.
        KỊCH BẢN 6: LỖI VALIDATION (error_validation) - Thiếu ví/danh mục khi tạo giao dịch.

        Tin nhắn: "{message}"
        """
        return prompt

    def create_transaction_from_ai(self, user, data):
        try:
            with transaction.atomic():
                wallet = Wallet.objects.get(id=data['wallet_id'], user=user)
                category = Category.objects.get(id=data['category_id'], user=user)
                raw_amount = Decimal(str(data['amount']))
                final_amount = -abs(raw_amount) if category.type == 'expense' else abs(raw_amount)

                Transaction.objects.create(
                    user=user, wallet=wallet, category=category,
                    amount=final_amount, date=data.get('date', datetime.date.today()),
                    description=data.get('description', category.name).capitalize()
                )
                wallet.balance += final_amount
                wallet.save(update_fields=['balance'])
        except Exception as e:
            raise Exception(f"Lỗi lưu giao dịch: {e}")