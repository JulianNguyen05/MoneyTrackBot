package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.TokenManager;

/**
 * ViewModel chính (MainViewModel)
 * - Quản lý trạng thái đăng nhập toàn cục
 * - Sử dụng TokenManager để kiểm tra token hợp lệ
 */
public class MainViewModel extends AndroidViewModel {

    private final TokenManager tokenManager;

    public MainViewModel(@NonNull Application application) {
        super(application);
        tokenManager = TokenManager.getInstance(application);
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa
     * @return true nếu token tồn tại và không rỗng
     */
    public boolean isUserLoggedIn() {
        String token = tokenManager.getToken();
        return token != null && !token.isEmpty();
    }
}