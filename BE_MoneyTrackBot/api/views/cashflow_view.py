import datetime
from datetime import timedelta
from django.db.models import Sum, Q, DecimalField, Value
from django.db.models.functions import Coalesce, Abs  # Thêm Abs để lấy giá trị tuyệt đối
from rest_framework import permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from ..models import Transaction


class CashFlowReportView(APIView):
    """
    API báo cáo dòng tiền (Thu/Chi/Biến động).
    Đã fix lỗi âm chồng âm bằng cách sử dụng Abs().
    """
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request, *args, **kwargs):
        user = request.user
        today = datetime.date.today()

        # 1. Xử lý khoảng thời gian lọc
        try:
            start_date_str = request.query_params.get('start_date')
            end_date_str = request.query_params.get('end_date')

            if start_date_str:
                start_date = datetime.datetime.strptime(start_date_str, '%Y-%m-%d').date()
            else:
                # Mặc định là từ đầu tháng hiện tại
                start_date = datetime.date(today.year, today.month, 1)

            if end_date_str:
                end_date = datetime.datetime.strptime(end_date_str, '%Y-%m-%d').date()
            else:
                end_date = today
        except (ValueError, TypeError):
            start_date = today - timedelta(days=30)
            end_date = today

        # 2. Tạo QuerySet cơ bản (Lọc theo user và ngày)
        transactions = Transaction.objects.filter(
            user=user,
            date__range=[start_date, end_date]
        )

        # 3. Lọc theo ví nếu có tham số wallet_id
        wallet_id = request.query_params.get('wallet_id')
        if wallet_id:
            transactions = transactions.filter(wallet_id=wallet_id)

        # 4. Tính toán tổng Income và Expense sử dụng Abs()
        # Mục đích: Biến -10.000 thành 10.000 trước khi SUM để phép trừ (income - expense) luôn đúng
        totals = transactions.aggregate(
            sum_income=Coalesce(
                Sum(Abs('amount'), filter=Q(category__type='income'), output_field=DecimalField()),
                Value(0, output_field=DecimalField())
            ),
            sum_expense=Coalesce(
                Sum(Abs('amount'), filter=Q(category__type='expense'), output_field=DecimalField()),
                Value(0, output_field=DecimalField())
            )
        )

        total_income = totals['sum_income']
        total_expense = totals['sum_expense']

        # Công thức chuẩn: Tổng Thu - Tổng Chi
        # Ví dụ: 20.000 (Thu) - 10.000 (Chi) = 10.000 (Biến động)
        net_change = total_income - total_expense

        # 5. Trả về kết quả cho Frontend
        data = {
            'start_date': start_date,
            'end_date': end_date,
            'total_income': total_income,
            'total_expense': total_expense,
            'net_change': net_change
        }

        return Response(data, status=status.HTTP_200_OK)