package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final Context context;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.context = context;
        this.tokenManager = TokenManager.getInstance(context);
    }

    public interface LoginCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(String username, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getAuthService(context).loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 🔥 Logic lưu token chuyển vào đây, Activity không cần lo nữa
                    String token = response.body().getAccess();
                    tokenManager.saveToken(token);
                    callback.onSuccess();
                } else {
                    callback.onError("Sai tên đăng nhập hoặc mật khẩu");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return tokenManager.getToken() != null;
    }
}