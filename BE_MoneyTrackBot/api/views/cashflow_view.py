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

        # 1. Xử lý ngày tháng
        try:
            start_date_str = request.query_params.get('start_date')
            end_date_str = request.query_params.get('end_date')

            if start_date_str:
                start_date = datetime.datetime.strptime(start_date_str, '%Y-%m-%d').date()
            else:
                start_date = datetime.date(today.year, today.month, 1)

            if end_date_str:
                end_date = datetime.datetime.strptime(end_date_str, '%Y-%m-%d').date()
            else:
                end_date = today
        except (ValueError, TypeError):
            start_date = today - timedelta(days=30)
            end_date = today

        # 2. Tạo QuerySet ban đầu (Lọc User + Ngày)
        transactions = Transaction.objects.filter(
            user=user,
            date__range=[start_date, end_date]
        )

        # 🔥 SỬA LỖI Ở ĐÂY: Lọc theo Wallet ID TRƯỚC khi tính toán
        wallet_id = request.query_params.get('wallet_id')
        if wallet_id:
            transactions = transactions.filter(wallet_id=wallet_id)

        # 3. Sau khi lọc xong xuôi mới tính tổng (Aggregate)
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

        total_income = totals['sum_income']
        total_expense = totals['sum_expense']
        net_change = total_income - total_expense

        # 4. Trả về kết quả
        data = {
            'start_date': start_date,
            'end_date': end_date,
            'total_income': total_income,
            'total_expense': total_expense,
            'net_change': net_change
        }

        return Response(data, status=status.HTTP_200_OK)