package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("api/register/")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
}