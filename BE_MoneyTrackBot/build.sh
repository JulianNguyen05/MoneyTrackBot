#!/usr/bin/env bash
# exit on error
set -o errexit

# 1. Cài đặt các thư viện từ requirements.txt
pip install -r requirements.txt

# 2. Thu thập static files (CSS, JS, Ảnh) cho Whitenoise
python manage.py collectstatic --no-input

# 3. QUAN TRỌNG: Tự động tạo file migration nếu bị thiếu
python manage.py makemigrations

# 4. Đồng bộ cấu trúc bảng vào Database PostgreSQL
python manage.py migrate