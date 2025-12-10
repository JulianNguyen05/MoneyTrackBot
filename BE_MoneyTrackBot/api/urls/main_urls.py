from django.urls import path, include
from rest_framework.routers import DefaultRouter
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView
from ..views import (
    user_view,
    category_view,
    wallet_view,
    transaction_view,
    cashflow_view,
    budget_view,
    transfer_view,
    report_view,
    chatbot,
)

# Router tự động build API cho các ViewSet
# bao gồm list, detail, create, update, delete
router = DefaultRouter()
router.register(r'categories', category_view.CategoryViewSet, basename='category')
router.register(r'wallets', wallet_view.WalletViewSet, basename='wallet')
router.register(r'transactions', transaction_view.TransactionViewSet, basename='transaction')
router.register(r'budgets', budget_view.BudgetViewSet, basename='budget')

urlpatterns = [
    # Tự động include toàn bộ URL CRUD từ Router
    # /categories/
    # /wallets/
    # /transactions/
    # /budgets/
    path('', include(router.urls)),

    # Đăng ký user
    path('register/', user_view.UserCreateView.as_view(), name='register'),

    # JWT login
    path('token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),

    # Chuyển tiền giữa ví
    path('transfer/', transfer_view.TransferView.as_view(), name='transfer'),

    # Báo cáo tổng quan
    path('reports/summary/', report_view.ReportView.as_view(), name='report-summary'),

    # Báo cáo dòng tiền (cashflow)
    path('reports/cashflow/', cashflow_view.CashFlowReportView.as_view(), name='report-cashflow'),

    # Chatbot AI
    path('chatbot/', chatbot.ChatbotView.as_view(), name='chatbot'),
]
