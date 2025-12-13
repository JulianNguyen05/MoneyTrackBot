#!/usr/bin/.env bash
# exit on error
set -o errexit

pip install -r requirements.txt

# Lệnh tạo database và static files
python manage.py collectstatic --no-input
python manage.py migrate