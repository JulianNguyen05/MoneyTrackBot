package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

/**
 * RegisterRequest
 * --------------------------------------------------
 * Request model dùng để đăng ký tài khoản người dùng.
 */
public class RegisterRequest {

    private String username;
    private String email;
    private String password;

    /**
     * Khởi tạo request đăng ký tài khoản.
     */
    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ===== Getters =====

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}