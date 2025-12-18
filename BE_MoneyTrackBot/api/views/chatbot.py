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

            # --- (B) LỖI VALIDATION (PHẦN BỔ SUNG HOÀN THIỆN) ---
            # Xử lý KỊCH BẢN 4: Không tạo giao dịch và báo lỗi cho người dùng.
            elif action == "error_validation":
                return Response({"reply": reply_message}, status=status.HTTP_400_BAD_REQUEST)

            # --- (C) TRẢ LỜI CÂU HỎI ---
            elif action == "answer_question":
                query_type = ai_data.get("query_type")
                data = ai_data.get("data", {})
                final_reply = ""

                # --- 1. Tổng chi tháng này ---
                if query_type == "total_expense_current_month":
                    now = timezone.now()
                    total = Transaction.objects.filter(
                        user=user,
                        category__type='expense',
                        date__year=now.year,
                        date__month=now.month
                    ).aggregate(total_sum=Sum('amount'))['total_sum'] or Decimal(0)
                    final_reply = f"Tổng chi tháng này của bạn là {total:,.0f}đ."

                # --- 2. Số dư ví ---
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

                # --- 3. Tổng chi của tháng cụ thể ---
                elif query_type == "total_expense_specific_month":
                    month = data.get("month")
                    if month is None:
                        final_reply = "Bạn vui lòng nói rõ tháng nào nhé (ví dụ: 'tháng 10')."
                    else:
                        now = timezone.now()
                        total = Transaction.objects.filter(
                            user=user,
                            category__type='expense',
                            date__year=now.year,  # (Giả định user hỏi năm hiện tại)
                            date__month=month
                        ).aggregate(total_sum=Sum('amount'))['total_sum'] or Decimal(0)
                        final_reply = f"Tổng chi tháng {month} của bạn là {total:,.0f}đ."

                # --- Không biết xử lý ---
                else:
                    final_reply = "Tôi đã nhận được câu hỏi, nhưng hiện tại tôi chưa được lập trình để tính điều này."

                return Response({"reply": final_reply})

            # --- (D) KHÔNG HIỂU ---
            else:
                return Response({"reply": ai_data.get("reply", "Xin lỗi, tôi chưa hiểu ý bạn.")})

        except Exception as e:
            print(f"--- AI API Error --- \n{e}\n------------------")
            return Response({"reply": f"Xin lỗi, Bot AI đang gặp lỗi: {str(e)}"})

    # ==========================================================
    # 🧠 Hàm "Dạy" AI cách hiểu câu hỏi và yêu cầu JSON
    # ==========================================================
    def build_prompt(self, message, wallets, categories):
        wallets_str = json.dumps(wallets)
        categories_str = json.dumps(categories)
        today_str = datetime.date.today().strftime('%Y-%m-%d')

        prompt = f"""
        Bạn là MoneyTrack Bot - một trợ lý tài chính thông minh, thân thiện và có kiến thức sâu rộng về quản lý chi tiêu tại Việt Nam.
        Ngày hôm nay là: {today_str}. Ngôn ngữ phản hồi: Tiếng Việt.

        Kiến thức hệ thống:
        1. Danh sách Ví của người dùng: {wallets_str}
        2. Danh sách Danh mục (Thu nhập/Chi tiêu): {categories_str}

        Nhiệm vụ: Phân tích tin nhắn người dùng và LUÔN phản hồi bằng một đối tượng JSON duy nhất.

        ---
        KỊCH BẢN 1: TẠO GIAO DỊCH (Hành động: create_transaction)
        Dấu hiệu: Người dùng cung cấp số tiền và hoạt động thu/chi.
        Lưu ý: Nếu người dùng không nói rõ ví, hãy chọn ví đầu tiên trong danh sách. Nếu không rõ danh mục, hãy chọn danh mục phù hợp nhất dựa trên mô tả.
        => JSON:
        {{
          "action": "create_transaction",
          "reply": "✅ Đã ghi nhận: (Mô tả ngắn) (Số tiền) vào ví (Tên ví).",
          "data": {{ "amount": float, "date": "{today_str}", "description": "string", "wallet_id": int, "category_id": int }}
        }}

        ---
        KỊCH BẢN 2: TRUY VẤN DỮ LIỆU (Hành động: answer_question)
        - query_type: "total_expense_current_month", "get_wallet_balance", "total_expense_specific_month".
        - data: Chứa tham số như {{"wallet_id": id}} hoặc {{"month": int}}.

        ---
        KỊCH BẢN 3: GIAO TIẾP TỰ NHIÊN & CHÀO HỎI (Hành động: normal_chat)
        Dấu hiệu: Người dùng chào hỏi, hỏi thăm sức khỏe, hoặc khen ngợi bot.
        Yêu cầu: Trả lời hóm hỉnh, thân thiện, có thể dùng icon (emoji).
        => JSON: {{ "action": "normal_chat", "reply": "Chào chủ nhân! Chúc bạn một ngày quản lý chi tiêu thật thông minh nhé! 😊" }}

        ---
        KỊCH BẢN 4: TƯ VẤN & GIÁO DỤC TÀI CHÍNH (Hành động: normal_chat)
        Dấu hiệu: Hỏi về kiến thức (Lạm phát là gì?, Cách tiết kiệm tiền?, Quy tắc 50/30/20...).
        Yêu cầu: Giải thích ngắn gọn, dễ hiểu dưới 3 câu.
        => JSON: {{ "action": "normal_chat", "reply": "Quy tắc 50/30/20 là chia thu nhập thành 3 phần: 50% nhu cầu thiết yếu, 30% sở thích và 20% để tiết kiệm đó!" }}

        ---
        KỊCH BẢN 5: LỖI VALIDATION & THIẾU THÔNG TIN (Hành động: error_validation)
        Dấu hiệu: Muốn tạo giao dịch nhưng không tìm thấy ví/danh mục khớp, HOẶC thiếu số tiền.
        => Ví dụ: "Ăn trưa bằng ví MoMo" (Nhưng không có số tiền).
        => JSON: {{ "action": "error_validation", "reply": "Món ăn trưa đó hết bao nhiêu tiền thế bạn? Hãy nhập thêm số tiền để mình lưu lại nhé!" }}

        ---
        KỊCH BẢN 6: KHÔNG HIỂU (Hành động: unknown)
        Dấu hiệu: Các yêu cầu không liên quan đến tài chính (Thời tiết hôm nay thế nào?, Hát một bài đi...).
        => JSON: {{ "action": "unknown", "reply": "Xin lỗi, mình chỉ tập trung vào tài chính thôi. Để mình giúp bạn quản lý tiền bạc nhé!" }}

        Bây giờ, hãy phân tích tin nhắn sau: "{message}"
        """
        return prompt

    # ==========================================================
    # 🧾 Hàm tạo giao dịch thực tế
    # ==========================================================
    def create_transaction_from_ai(self, user, data):
        try:
            with transaction.atomic():
                wallet = Wallet.objects.get(id=data['wallet_id'], user=user)
                category = Category.objects.get(id=data['category_id'], user=user)
                amount = Decimal(data['amount'])
                date = data.get('date', datetime.date.today())

                Transaction.objects.create(
                    user=user,
                    wallet=wallet,
                    category=category,
                    amount=amount,
                    date=date,
                    description=data.get('description', category.name).capitalize()
                )

                # Cập nhật số dư ví
                if category.type == 'income':
                    wallet.balance += amount
                else:
                    wallet.balance -= amount
                wallet.save(update_fields=['balance'])
        except Exception as e:
            print(f"Lỗi khi tạo Giao dịch từ AI: {e}")
            # Bạn có thể ném lỗi (raise e) để chatbot báo lỗi ngược lại cho user
            raise Exception(f"Lỗi server khi lưu giao dịch: {e}")