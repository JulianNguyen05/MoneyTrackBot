from django.db import models
from django.contrib.auth.models import User

class Wallet(models.Model):
    WALLET_TYPES = [
        ("cash", "Tiền mặt"),
        ("bank", "Tài khoản ngân hàng"),
        ("ewallet", "Ví điện tử"),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE)
    name = models.CharField(max_length=100)
    balance = models.DecimalField(max_digits=18, decimal_places=2, default=0)
    type = models.CharField(max_length=20, choices=WALLET_TYPES)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.name} ({self.user.username})"