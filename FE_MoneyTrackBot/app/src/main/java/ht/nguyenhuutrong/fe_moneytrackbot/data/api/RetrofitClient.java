package ht.nguyenhuutrong.fe_moneytrackbot.data.api;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.AuthService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.CategoryService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.ChatbotService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.TransactionService;
import ht.nguyenhuutrong.fe_moneytrackbot.data.api.services.WalletService;

/**
 * RetrofitClient
 * --------------------------------------------------
 * Khởi tạo Retrofit và cung cấp các Service dùng chung cho toàn bộ ứng dụng.
 * Tự động đính kèm Bearer Token vào header nếu người dùng đã đăng nhập.
 */
public class RetrofitClient {

    // Base URL của Backend
    private static final String BASE_URL = "https://moneytrackbot.onrender.com/";
//    private static final String BASE_URL = "http://10.0.2.2:8000/";

    private static Retrofit retrofit;

    /**
     * Khởi tạo Retrofit với OkHttp Interceptor để gắn Authorization token.
     * Sử dụng Singleton để tái sử dụng kết nối.
     */
    private static Retrofit getClient(Context context) {
        if (retrofit == null) {
            TokenManager tokenManager = TokenManager.getInstance(context);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String token = tokenManager.getToken();

                        if (token != null && !token.isEmpty()) {
                            Request authorizedRequest = originalRequest.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(authorizedRequest);
                        }

                        return chain.proceed(originalRequest);
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

    /**
     * Auth APIs (Login, Register)
     */
    public static AuthService getAuthService(Context context) {
        return getClient(context).create(AuthService.class);
    }

    /**
     * Transaction APIs (CRUD, Cash Flow)
     */
    public static TransactionService getTransactionService(Context context) {
        return getClient(context).create(TransactionService.class);
    }

    /**
     * Wallet APIs (CRUD, Transfer)
     */
    public static WalletService getWalletService(Context context) {
        return getClient(context).create(WalletService.class);
    }

    /**
     * Category APIs (CRUD)
     */
    public static CategoryService getCategoryService(Context context) {
        return getClient(context).create(CategoryService.class);
    }

    /**
     * Chatbot APIs
     */
    public static ChatbotService getChatbotService(Context context) {
        return getClient(context).create(ChatbotService.class);
    }
}