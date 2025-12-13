package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;

import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.RegisterRequest; // 🔥 MỚI
import ht.nguyenhuutrong.fe_moneytrackbot.models.User;            // 🔥 MỚI

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

    // 🔥 CẬP NHẬT: Đổi tên thành AuthCallback để dùng chung cho cả Login và Register
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    // --- 1. XỬ LÝ ĐĂNG NHẬP ---
    public void login(String username, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getAuthService(context).loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Tự động lưu Token
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

    // --- 2. XỬ LÝ ĐĂNG KÝ (🔥 MỚI THÊM VÀO) ---
    public void register(String username, String email, String password, AuthCallback callback) {
        RegisterRequest request = new RegisterRequest(username, email, password);

        RetrofitClient.getAuthService(context).registerUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Đăng ký thành công (Server trả về User object)
                    callback.onSuccess();
                } else if (response.code() == 400) {
                    // Lỗi validation từ server (thường là trùng username/email)
                    callback.onError("Tên đăng nhập hoặc Email đã tồn tại.");
                } else {
                    callback.onError("Đăng ký thất bại. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // --- 3. KIỂM TRA TRẠNG THÁI ---
    public boolean isLoggedIn() {
        return tokenManager.getToken() != null;
    }

    public void logout() {
        tokenManager.clearToken();
    }
}