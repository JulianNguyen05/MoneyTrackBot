from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from .wallet import Wallet
from .category import Category

class Transaction(models.Model):
    TRANSACTION_TYPES = [
        ("income", "Thu"),
        ("expense", "Chi"),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE)
    wallet = models.ForeignKey(Wallet, on_delete=models.CASCADE)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)

    amount = models.DecimalField(max_digits=18, decimal_places=2)
    type = models.CharField(max_length=10, choices=TRANSACTION_TYPES)
    note = models.TextField(blank=True)

    transaction_date = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.type} - {self.amount} - {self.user.username}"
