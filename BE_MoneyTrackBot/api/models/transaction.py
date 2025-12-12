from django.db import models
from django.contrib.auth.models import User
from .wallet import Wallet
from .category import Category


class Transaction(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="transactions")
    wallet = models.ForeignKey(Wallet, on_delete=models.CASCADE, related_name="transactions")
    category = models.ForeignKey(Category, on_delete=models.CASCADE, related_name="transactions")
    amount = models.DecimalField(max_digits=15, decimal_places=2)
    description = models.TextField(blank=True, null=True)
    date = models.DateField()

    class Meta:
        verbose_name = "Transaction"
        verbose_name_plural = "Transactions"
        ordering = ["-date"]

    def __str__(self):
        return f"{self.category.name}: {self.amount:,.0f}đ"

    # ================= LOGIC TỰ ĐỘNG CẬP NHẬT VÍ =================

    def save(self, *args, **kwargs):
        """
        Khi LƯU giao dịch:
        - Nếu là mới tạo: Cộng/Trừ tiền ví ngay lập tức.
        - Nếu là sửa (update): Logic phức tạp hơn (cần trừ cái cũ, cộng cái mới) - Tạm thời chưa xử lý ở đây để đơn giản.
        """
        # 1. Kiểm tra xem đây là tạo mới (chưa có ID) hay sửa
        is_new = self.pk is None

        # 2. Lưu giao dịch vào database trước
        super().save(*args, **kwargs)

        # 3. Chỉ cập nhật ví nếu là giao dịch mới
        if is_new:
            self.process_balance_change(is_add=True)

    def delete(self, *args, **kwargs):
        """
        Khi XÓA giao dịch:
        - Phải hoàn tác lại số dư (Revert balance).
        - Ví dụ: Xóa một khoản chi tiêu -> Tiền phải quay về ví.
        """
        # 1. Hoàn tiền trước khi xóa
        self.process_balance_change(is_add=False)

        # 2. Xóa giao dịch
        super().delete(*args, **kwargs)

    def process_balance_change(self, is_add=True):
        """
        Hàm xử lý cộng trừ tiền chung.
        :param is_add: True nếu là Thêm giao dịch, False nếu là Xóa giao dịch (đảo ngược)
        """
        # Lấy loại danh mục: 'income' (thu) hoặc 'expense' (chi)
        transaction_type = self.category.type

        # Xác định dấu: + hay -
        if transaction_type == 'income':
            # Nếu là Thu nhập: Thêm thì Cộng (+), Xóa thì Trừ (-)
            delta = self.amount if is_add else -self.amount
        else:
            # Nếu là Chi tiêu: Thêm thì Trừ (-), Xóa thì Cộng (+)
            delta = -self.amount if is_add else self.amount

        # Cập nhật và lưu ví
        self.wallet.balance += delta
        self.wallet.save()