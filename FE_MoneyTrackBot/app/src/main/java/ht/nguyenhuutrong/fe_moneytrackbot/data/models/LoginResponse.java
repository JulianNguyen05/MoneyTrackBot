package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * LoginResponse
 * --------------------------------------------------
 * Response model trả về sau khi đăng nhập thành công.
 * Chứa access token và refresh token.
 */
public class LoginResponse {

    @SerializedName("access")
    private String access;

    @SerializedName("refresh")
    private String refresh;

    /**
     * Constructor rỗng.
     * Cần thiết cho Gson khi parse JSON từ server.
     */
    public LoginResponse() {
    }

    // ===== Getters & Setters =====

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }
}