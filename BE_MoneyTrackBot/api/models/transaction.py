from django.db import models, transaction
from django.db.models import F
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

    def save(self, *args, **kwargs):
        with transaction.atomic():
            if self.pk:
                # TRƯỜNG HỢP UPDATE: Hoàn tác số tiền cũ trước
                old_obj = Transaction.objects.get(pk=self.pk)
                old_obj.process_balance_change(is_add=False)

                # Sau khi hoàn tác, ta lấy lại instance ví từ DB
                # để tránh dùng giá trị cũ đang lưu trong RAM
                self.wallet.refresh_from_db()

            # Lưu giao dịch mới/cập nhật
            super().save(*args, **kwargs)

            # Cập nhật số dư mới
            self.process_balance_change(is_add=True)

    def delete(self, *args, **kwargs):
        with transaction.atomic():
            # Hoàn tác số dư trước khi xóa hẳn giao dịch
            self.process_balance_change(is_add=False)
            super().delete(*args, **kwargs)

    from django.db.models import F

    def process_balance_change(self, is_add=True):
        transaction_type = str(self.category.type).strip().lower()

        # Lấy giá trị tuyệt đối để tránh lỗi âm chồng âm
        amount = abs(self.amount)

        if transaction_type == 'income':
            delta = amount if is_add else -amount
        elif transaction_type == 'expense':
            delta = -amount if is_add else amount
        else:
            delta = 0

        self.wallet.balance = F('balance') + delta
        self.wallet.save(update_fields=['balance'])
        self.wallet.refresh_from_db()