from django.contrib.auth.models import User
from rest_framework import generics, permissions
from ..serializers.user_serializer import UserSerializer
from django.http import HttpResponse

class UserCreateView(generics.CreateAPIView):
    """
    API View công khai (public) để tạo tài khoản User mới.
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer

    permission_classes = [permissions.AllowAny]

def create_superuser_view(request):
    try:
        # Kiểm tra xem tài khoản 'admin' đã có chưa
        if not User.objects.filter(username='admin').exists():
            # Tạo user mới: Tên=admin, Pass=123456
            User.objects.create_superuser('admin', 'admin@example.com', '123456')
            return HttpResponse("ĐÃ TẠO ADMIN THÀNH CÔNG! <br>User: <b>admin</b> <br>Pass: <b>123456</b>")
        else:
            return HttpResponse("Admin đã tồn tại rồi! Không cần tạo lại.")
    except Exception as e:
        return HttpResponse(f"Lỗi: {str(e)}")