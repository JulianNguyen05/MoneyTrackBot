package ht.nguyenhuutrong.fe_moneytrackbot.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Wallet implements Serializable {

    // @SerializedName giúp map đúng key JSON từ Server
    // Ví dụ: JSON trả về {"id": 1, "name": "Ví chính", "balance": 100000}

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    // --- Constructor 1: Mặc định (bắt buộc cho Gson) ---
    public Wallet() {
    }

    // --- Constructor 2: Dùng khi tạo ví mới (Gửi lên server không cần ID) ---
    public Wallet(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    // --- Constructor 3: Dùng khi nhận dữ liệu đầy đủ từ Server ---
    public Wallet(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBalance(double balance) { this.balance = balance; }
}