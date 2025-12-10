from ..models import Wallet
from ..serializers import WalletSerializer
from .base_viewset import BaseViewSet


class WalletViewSet(BaseViewSet):
    """
    ViewSet quản lý CRUD cho ví tiền (Wallet).

    Kế thừa từ BaseViewSet:
    - Tự động hỗ trợ các API: GET (list), GET (detail), POST, PUT/PATCH, DELETE
    - Tự động bind serializer và queryset
    - Tự động kiểm soát quyền truy cập (nếu BaseViewSet có cấu hình)
    """

    # Tập dữ liệu chính của ViewSet: tất cả ví trong hệ thống
    # DRF sẽ dùng queryset này cho các thao tác list(), retrieve(), update(), delete()
    queryset = Wallet.objects.all()

    # Serializer dùng để validate dữ liệu đầu vào và xuất dữ liệu JSON
    serializer_class = WalletSerializer
