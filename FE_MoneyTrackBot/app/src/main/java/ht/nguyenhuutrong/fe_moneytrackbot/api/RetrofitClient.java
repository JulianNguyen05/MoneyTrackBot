package ht.nguyenhuutrong.fe_moneytrackbot.api;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.132.229.81/";

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {

        if (retrofit == null) {

            // 🔥 Lấy singleton đúng cách
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

    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }
}
