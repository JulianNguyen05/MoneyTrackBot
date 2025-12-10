import os
from django.core.asgi import get_asgi_application

# --- Thiết lập môi trường Django ---
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "myproject.settings")

# --- Tạo ứng dụng ASGI ---
application = get_asgi_application()
