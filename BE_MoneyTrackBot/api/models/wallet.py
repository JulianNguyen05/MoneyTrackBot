from django.db import models
from django.contrib.auth.models import User


class Wallet(models.Model):
    # Liên kết mỗi Wallet với một User
    # - ForeignKey: 1 user có thể có nhiều ví
    # - on_delete=models.CASCADE: xóa user → xóa luôn các ví
    # - related_name="wallets": cho phép truy cập user.wallets để lấy danh sách ví
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="wallets")

    # Tên ví (VD: "Ví tiền mặt", "Ví Momo")
    # max_length=100 → tối đa 100 ký tự
    name = models.CharField(max_length=100)

    # Số dư ví
    # DecimalField giúp tránh lỗi số học khi dùng float
    # max_digits=15: tổng số chữ số tối đa
    # decimal_places=2: 2 số sau dấu phẩy
    balance = models.DecimalField(max_digits=15, decimal_places=2, default=0.00)

    # Ảnh của ví
    # upload_to="wallets/" → ảnh sẽ được lưu trong thư mục MEDIA_ROOT/wallets/
    # null=True, blank=True → người dùng không bắt buộc phải chọn ảnh
    image = models.ImageField(upload_to="wallets/", null=True, blank=True)

    class Meta:
        # Tên hiển thị trong admin Django
        verbose_name = "Wallet"
        verbose_name_plural = "Wallets"

        # Sắp xếp danh sách ví theo tên (alphabet)
        ordering = ["name"]

        # user + name phải là duy nhất
        # → Mỗi user không thể tạo 2 ví trùng tên
        unique_together = ("user", "name")

    # Hiển thị dạng "Ví tiền mặt (100,000đ)" trong admin hoặc debug
    def __str__(self):
        return f"{self.name} ({self.balance:,.0f}đ)"
