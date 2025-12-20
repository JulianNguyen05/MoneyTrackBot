package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * LoginRequest
 * --------------------------------------------------
 * Request model dùng để đăng nhập người dùng.
 */
public class LoginRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    /**
     * Khởi tạo request đăng nhập.
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ===== Getters & Setters =====

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}