package ht.nguyenhuutrong.fe_moneytrackbot.api;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static TokenManager instance;

    private final SharedPreferences sharedPreferences;

    // 🔒 Constructor private để ép buộc singleton
    private TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 🔥 Singleton thread-safe
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    // =======================
    //    TOKEN FUNCTIONS
    // =======================

    // Lưu token
    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply();
    }

    // Lấy token
    public String getToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    // Xóa token khi logout
    public void clearToken() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .apply();
    }
}
