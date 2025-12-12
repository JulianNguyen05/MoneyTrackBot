package ht.nguyenhuutrong.fe_moneytrackbot.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Wallet implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    // --- Constructor 1: Mặc định ---
    public Wallet() {
    }

    // --- Constructor 2: Tạo mới ---
    public Wallet(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    // --- Constructor 3: Đầy đủ ---
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

    // 🔥 QUAN TRỌNG: Hàm này giúp Dropdown hiển thị Tên Ví thay vì mã Hash
    @Override
    public String toString() {
        return name; // Trả về tên để hiển thị lên menu
    }
}