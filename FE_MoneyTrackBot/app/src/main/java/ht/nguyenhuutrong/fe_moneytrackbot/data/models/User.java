package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

/**
 * User
 * ----------------------------------------
 * Model người dùng trả về từ server
 * (sau khi đăng ký hoặc lấy thông tin tài khoản).
 */
public class User {

    private int id;
    private String username;
    private String email;

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // ===== Setters =====

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}