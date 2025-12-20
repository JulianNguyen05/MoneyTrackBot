package ht.nguyenhuutrong.fe_moneytrackbot.data.api.services;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * AuthService
 * --------------------------------------------------
 * Định nghĩa các API liên quan đến xác thực người dùng
 * (Đăng ký, Đăng nhập)
 * Sử dụng Retrofit để giao tiếp với Backend
 */
public interface AuthService {

    /**
     * Đăng ký tài khoản mới
     *
     * @param registerRequest Thông tin đăng ký người dùng
     * @return Thông tin người dùng sau khi đăng ký thành công
     */
    @POST("api/register/")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    /**
     * Đăng nhập hệ thống
     *
     * @param loginRequest Thông tin đăng nhập (username, password)
     * @return Access token và thông tin xác thực
     */
    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
}
