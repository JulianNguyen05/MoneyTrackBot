package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.TokenManager;

/**
 * ViewModel quản lý các cài đặt của người dùng
 * - Xử lý đăng xuất
 * - Thông báo sự kiện đăng xuất cho UI qua LiveData
 */
public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    // --- LiveData để Fragment observe sự kiện logout ---
    public LiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }

    /**
     * Thực hiện đăng xuất
     * - Xóa token lưu trữ
     * - Cập nhật LiveData để Fragment điều hướng về LoginActivity
     */
    public void logout() {
        // 1. Xóa Token
        TokenManager.getInstance(getApplication()).clearToken();

        // 2. Thông báo sự kiện đăng xuất
        logoutEvent.setValue(true);
    }
}