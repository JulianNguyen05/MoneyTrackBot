from django.db import models
from django.contrib.auth.models import AbstractUser

class User(AbstractUser):
    full_name = models.CharField(max_length=100, blank=True)
    last_login_at = models.DateTimeField(null=True, blank=True)

    def __str__(self):
        return self.username
