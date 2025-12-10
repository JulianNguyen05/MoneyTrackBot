from django.contrib import admin
from .models import User, Wallet, Category, Transaction, Budget

admin.site.register(User)
admin.site.register(Wallet)
admin.site.register(Category)
admin.site.register(Transaction)
admin.site.register(Budget)