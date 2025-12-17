import datetime
from datetime import timedelta
from django.db.models import Sum, Q, DecimalField, Value
from django.db.models.functions import Coalesce
from rest_framework import permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from ..models import Transaction


class CashFlowReportView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request, *args, **kwargs):
        user = request.user
        today = datetime.date.today()

        # 1. Xử lý ngày tháng (Giữ nguyên logic tốt của bạn)
        try:
            start_date_str = request.query_params.get('start_date')
            end_date_str = request.query_params.get('end_date')

            if start_date_str:
                start_date = datetime.datetime.strptime(start_date_str, '%Y-%m-%d').date()
            else:
                start_date = datetime.date(today.year, today.month, 1)  # Mặc định ngày 1 tháng này

            if end_date_str:
                end_date = datetime.datetime.strptime(end_date_str, '%Y-%m-%d').date()
            else:
                end_date = today  # Mặc định hôm nay
        except (ValueError, TypeError):
            start_date = today - timedelta(days=30)
            end_date = today

        # 2. Lọc giao dịch trong khoảng thời gian
        transactions = Transaction.objects.filter(
            user=user,
            date__range=[start_date, end_date]
        )

        # 3. Tính tổng (Dùng aggregate thay vì annotate để ra 1 con số tổng)
        totals = transactions.aggregate(
            sum_income=Coalesce(
                Sum('amount', filter=Q(category__type='income'), output_field=DecimalField()),
                Value(0, output_field=DecimalField())
            ),
            sum_expense=Coalesce(
                Sum('amount', filter=Q(category__type='expense'), output_field=DecimalField()),
                Value(0, output_field=DecimalField())
            )
        )

        # Lấy giá trị ra (nếu None thì là 0)
        total_income = totals['sum_income']
        total_expense = totals['sum_expense']

        # 4. Tính Chênh lệch (Net Change)
        net_change = total_income - total_expense

        # 5. Trả về JSON object phẳng (Không phải List)
        data = {
            'start_date': start_date,
            'end_date': end_date,
            'total_income': total_income,
            'total_expense': total_expense,
            'net_change': net_change
        }

        wallet_id = request.query_params.get('wallet_id')

        transactions = Transaction.objects.filter(
            user=request.user,
            date__range=[start_date, end_date]
        )

        # 🔥 QUAN TRỌNG: Nếu có wallet_id thì lọc thêm
        if wallet_id:
            transactions = transactions.filter(wallet_id=wallet_id)

        return Response(data, status=status.HTTP_200_OK)