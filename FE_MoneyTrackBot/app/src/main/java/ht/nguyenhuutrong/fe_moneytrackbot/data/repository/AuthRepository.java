package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AuthRepository
 * ------------------------------------------------
 * Xử lý các nghiệp vụ xác thực:
 * - Đăng nhập
 * - Đăng ký
 * - Quản lý trạng thái đăng nhập (token)
 */
public class AuthRepository {

    private final Context context;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.context = context;
        this.tokenManager = TokenManager.getInstance(context);
    }

    /**
     * Callback dùng chung cho Login & Register
     */
    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Đăng nhập người dùng
     */
    public void login(String username, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getAuthService(context)
                .loginUser(request)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call,
                                           Response<LoginResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            tokenManager.saveToken(response.body().getAccess());
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

    /**
     * Đăng ký tài khoản mới
     */
    public void register(String username,
                         String email,
                         String password,
                         AuthCallback callback) {

        RegisterRequest request = new RegisterRequest(username, email, password);

        RetrofitClient.getAuthService(context)
                .registerUser(request)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call,
                                           Response<User> response) {

                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else if (response.code() == 400) {
                            callback.onError("Tên đăng nhập hoặc email đã tồn tại");
                        } else {
                            callback.onError("Đăng ký thất bại ("
                                    + response.code() + ")");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    public boolean isLoggedIn() {
        return tokenManager.getToken() != null;
    }

    /**
     * Đăng xuất người dùng
     */
    public void logout() {
        tokenManager.clearToken();
    }
}