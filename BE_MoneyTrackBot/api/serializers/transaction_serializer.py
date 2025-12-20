from rest_framework import serializers
from ..models import Transaction


class TransactionSerializer(serializers.ModelSerializer):
    """
    Serializer cho giao dịch thu/chi.
    """
    # 1. Lấy tên danh mục và tên ví để hiển thị trên UI
    category_name = serializers.ReadOnlyField(source='category.name')
    wallet_name = serializers.ReadOnlyField(source='wallet.name')

    # 🔥 SỬA QUAN TRỌNG: Lấy 'category.type' (expense/income) thay vì 'get_type_display'
    # Code Android cần chuỗi "expense" để đổi màu đỏ, nếu gửi "Chi tiêu" nó sẽ không hiểu.
    category_type = serializers.ReadOnlyField(source='category.type')

    class Meta:
        model = Transaction
        fields = [
            'id', 'amount', 'date', 'description',
            'category', 'category_name', 'category_type',
            'wallet', 'wallet_name', 'user'
        ]
        read_only_fields = ('user',)


class TransferSerializer(serializers.Serializer):
    """
    Serializer xác thực dữ liệu chuyển tiền.
    """
    amount = serializers.DecimalField(max_digits=15, decimal_places=2)
    from_wallet_id = serializers.IntegerField()
    to_wallet_id = serializers.IntegerField()
    date = serializers.DateField()
    description = serializers.CharField(required=False, allow_blank=True, max_length=200)

    # 🔥 Thêm validate để chặn lỗi logic cơ bản
    def validate(self, data):
        # 1. Không cho phép chuyển tiền cho chính ví đó
        if data['from_wallet_id'] == data['to_wallet_id']:
            raise serializers.ValidationError("Không thể chuyển tiền vào chính ví nguồn.")

        # 2. Số tiền chuyển phải lớn hơn 0
        if data['amount'] <= 0:
            raise serializers.ValidationError("Số tiền chuyển phải lớn hơn 0.")

        return data