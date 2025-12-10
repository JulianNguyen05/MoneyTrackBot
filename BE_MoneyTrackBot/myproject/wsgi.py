import os
from django.core.wsgi import get_wsgi_application

# Set the default Django settings module for the WSGI application
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "myproject.settings")

# Create the WSGI application
application = get_wsgi_application()
