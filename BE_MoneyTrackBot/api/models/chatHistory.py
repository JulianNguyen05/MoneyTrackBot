from django.db import models
from django.contrib.auth.models import User

class ChatHistory(models.Model):
    ROLE_CHOICES = (
        ('user', 'User'),
        ('model', 'AI Bot'),
    )
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='chat_history')
    role = models.CharField(max_length=10, choices=ROLE_CHOICES)
    message = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.username} - {self.role}: {self.message[:20]}..."