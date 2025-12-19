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
    search_fields = ['description'] # Đã đồng bộ với filter trong get_queryset bên dưới
    ordering_fields = ['date', 'amount']

    def perform_create(self, serializer):
        """
        Giao diện chỉ việc lưu.
        Mọi logic cộng tiền đã nằm trong Transaction.save() ở models.py
        """
        serializer.save(user=self.request.user)

    def perform_update(self, serializer):
        """
        Giao diện chỉ việc lưu.
        Model.save() sẽ tự nhận biết đây là Update để hoàn tác tiền cũ và cộng tiền mới.
        """
        serializer.save()

    def perform_destroy(self, instance):
        """
        Giao diện chỉ việc xóa.
        Model.delete() sẽ tự thực hiện hoàn tác số dư trước khi mất dữ liệu.
        """
        instance.delete()

    def get_queryset(self):
        # Chỉ lấy giao dịch của chính user đang đăng nhập
        queryset = Transaction.objects.filter(user=self.request.user)

        # 1. Lọc theo Ví (wallet_id)
        wallet_id = self.request.query_params.get('wallet_id')
        if wallet_id:
            queryset = queryset.filter(wallet_id=wallet_id)

        # 2. Lọc theo khoảng ngày (start_date, end_date)
        start_date = self.request.query_params.get('start_date')
        end_date = self.request.query_params.get('end_date')
        if start_date and end_date:
            queryset = queryset.filter(date__range=[start_date, end_date])

        # 3. Tìm kiếm theo description (đã sửa note thành description cho đúng model)
        search_term = self.request.query_params.get('search')
        if search_term:
            queryset = queryset.filter(description__icontains=search_term)

        return queryset.order_by('-date')