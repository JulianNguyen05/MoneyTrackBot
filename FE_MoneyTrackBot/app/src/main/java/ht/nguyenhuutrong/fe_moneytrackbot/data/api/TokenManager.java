package ht.nguyenhuutrong.fe_moneytrackbot.data.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TokenManager
 * --------------------------------------------------
 * Quản lý Access Token của người dùng thông qua SharedPreferences.
 * Được sử dụng bởi RetrofitClient để gắn Authorization header.
 */
public class TokenManager {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static TokenManager instance;
    private final SharedPreferences sharedPreferences;

    /**
     * Khởi tạo TokenManager theo mô hình Singleton.
     * Sử dụng Application Context để tránh memory leak.
     */
    private TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lấy instance duy nhất của TokenManager (thread-safe).
     */
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    /**
     * Lưu Access Token sau khi đăng nhập thành công.
     */
    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply();
    }

    /**
     * Lấy Access Token hiện tại.
     *
     * @return access token hoặc null nếu chưa đăng nhập
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Xóa Access Token khi người dùng đăng xuất.
     */
    public void clearToken() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .apply();
    }
}