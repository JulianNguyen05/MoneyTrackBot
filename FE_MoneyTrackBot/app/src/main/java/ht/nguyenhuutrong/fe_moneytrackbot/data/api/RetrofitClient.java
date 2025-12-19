package ht.nguyenhuutrong.fe_moneytrackbot.data.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 🔥 IMPORT CÁC SERVICE MỚI TỪ PACKAGE api.services
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.AuthService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.CategoryService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.ChatbotService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.TransactionService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.WalletService;

public class RetrofitClient {

//     private static final String BASE_URL = "https://moneytrackbot.onrender.com/";
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit retrofit;

    // Hàm khởi tạo Retrofit (Giữ nguyên logic thêm Token)
    private static Retrofit getClient(Context context) {
        if (retrofit == null) {
            final TokenManager tokenManager = TokenManager.getInstance(context);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        String token = tokenManager.getToken();

                        if (token != null && !token.isEmpty()) {
                            Request newRequest = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                        return chain.proceed(original);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // ==========================================================
    // 🚀 CÁC HÀM GET SERVICE (Đã tách nhỏ)
    // ==========================================================

    public static AuthService getAuthService(Context context) {
        return getClient(context).create(AuthService.class);
    }

    public static TransactionService getTransactionService(Context context) {
        return getClient(context).create(TransactionService.class);
    }

    public static WalletService getWalletService(Context context) {
        return getClient(context).create(WalletService.class);
    }

    public static CategoryService getCategoryService(Context context) {
        return getClient(context).create(CategoryService.class);
    }

    public static ChatbotService getChatbotService(Context context) {
        return getClient(context).create(ChatbotService.class);
    }
}