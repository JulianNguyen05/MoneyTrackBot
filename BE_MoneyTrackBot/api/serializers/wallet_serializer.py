from rest_framework import serializers
from ..models import Wallet


class WalletSerializer(serializers.ModelSerializer):
    """
    Serializer cho ví tiền của người dùng.
    - Tự động ánh xạ các trường trong model Wallet
    - Hỗ trợ upload ảnh (ImageField)
    - Thêm trường formatted_balance để hiển thị số tiền đẹp hơn
    """

    # Trường chỉ để hiển thị (không lưu vào database)
    # SerializerMethodField → gọi hàm get_formatted_balance()
    formatted_balance = serializers.SerializerMethodField()

    class Meta:
        model = Wallet

        # __all__ → lấy toàn bộ các trường của model Wallet:
        # id, user, name, balance, image, ...
        fields = '__all__'

        # user được set tự động trong view → client không được phép sửa
        read_only_fields = ('user',)

    def get_formatted_balance(self, obj):
        """
        Trả về số dư theo định dạng VNĐ:
        1200000 → "1,200,000đ"
        """
        return f"{obj.balance:,.0f}đ"
