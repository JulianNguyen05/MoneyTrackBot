from django.db import transaction
from rest_framework import filters
from django_filters import rest_framework as django_filters
from ..models import Transaction
from ..serializers import TransactionSerializer
from .base_viewset import BaseViewSet


class TransactionViewSet(BaseViewSet):
    """CRUD cho giao dịch VÀ HỖ TRỢ LỌC/TÌM KIẾM."""
    queryset = Transaction.objects.all().order_by('-date')
    serializer_class = TransactionSerializer

    filter_backends = (
        django_filters.DjangoFilterBackend,
        filters.SearchFilter,
        filters.OrderingFilter
    )
    filterset_fields = ['category', 'wallet']
    search_fields = ['description']
    ordering_fields = ['date', 'amount']

    def perform_create(self, serializer):
        """Tạo giao dịch mới và cập nhật số dư ví."""
        transaction_obj = serializer.save(user=self.request.user)
        wallet = transaction_obj.wallet
        if transaction_obj.category.type == 'income':
            wallet.balance += transaction_obj.amount
        else:
            wallet.balance -= transaction_obj.amount
        wallet.save(update_fields=['balance'])

    def perform_update(self, serializer):
        """Cập nhật giao dịch và điều chỉnh số dư ví."""
        old_transaction = self.get_object()
        old_wallet = old_transaction.wallet
        old_amount = old_transaction.amount
        old_type = old_transaction.category.type

        if old_type == 'income':
            old_wallet.balance -= old_amount
        else:
            old_wallet.balance += old_amount
        old_wallet.save(update_fields=['balance'])

        new_transaction = serializer.save()
        new_wallet = new_transaction.wallet

        if old_wallet.id == new_wallet.id:
            new_wallet.refresh_from_db()

        if new_transaction.category.type == 'income':
            new_wallet.balance += new_transaction.amount
        else:
            new_wallet.balance -= new_transaction.amount
        new_wallet.save(update_fields=['balance'])

    def perform_destroy(self, instance):
        """Xóa giao dịch và hoàn tác số dư ví."""
        wallet = instance.wallet
        if instance.category.type == 'income':
            wallet.balance -= instance.amount
        else:
            wallet.balance += instance.amount
        wallet.save(update_fields=['balance'])
        instance.delete()

    def get_queryset(self):
        queryset = Transaction.objects.filter(user=self.request.user)

        # 1. Lọc theo Ví (nếu có tham số wallet_id)
        wallet_id = self.request.query_params.get('wallet_id')
        if wallet_id:
            queryset = queryset.filter(wallet_id=wallet_id)

        # 2. Lọc theo khoảng ngày (start_date, end_date)
        start_date = self.request.query_params.get('start_date')
        end_date = self.request.query_params.get('end_date')
        if start_date and end_date:
            queryset = queryset.filter(date__range=[start_date, end_date])

        # 3. Tìm kiếm từ khóa (search)
        search_term = self.request.query_params.get('search')
        if search_term:
            queryset = queryset.filter(note__icontains=search_term)

        return queryset.order_by('-date')  # Sắp xếp mới nhất trước
