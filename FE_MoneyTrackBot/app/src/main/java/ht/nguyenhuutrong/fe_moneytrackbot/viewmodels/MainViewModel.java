package ht.nguyenhuutrong.fe_moneytrackbot.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import ht.nguyenhuutrong.fe_moneytrackbot.api.TokenManager;

public class MainViewModel extends AndroidViewModel {

    private final TokenManager tokenManager;

    public MainViewModel(@NonNull Application application) {
        super(application);
        tokenManager = TokenManager.getInstance(application);
    }

    public boolean isUserLoggedIn() {
        String token = tokenManager.getToken();
        return token != null && !token.isEmpty();
    }
}