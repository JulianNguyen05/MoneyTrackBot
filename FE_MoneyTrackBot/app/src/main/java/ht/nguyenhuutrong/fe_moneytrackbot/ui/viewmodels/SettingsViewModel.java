package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.TokenManager;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }

    public void logout() {
        // 1. Xóa Token
        TokenManager.getInstance(getApplication()).clearToken();

        // 2. Thông báo sự kiện đăng xuất thành công
        logoutEvent.setValue(true);
    }
}