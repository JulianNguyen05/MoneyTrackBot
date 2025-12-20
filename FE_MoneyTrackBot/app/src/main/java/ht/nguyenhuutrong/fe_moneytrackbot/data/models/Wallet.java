package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Wallet
 * ----------------------------------------
 * Model ví tiền dùng để hiển thị và thao tác
 * (nhận từ server hoặc tạo mới).
 */
public class Wallet implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private double balance;

    // Constructor rỗng (bắt buộc cho Gson)
    public Wallet() {
    }

    // Dùng khi tạo mới ví
    public Wallet(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    // Dùng khi nhận dữ liệu đầy đủ từ server
    public Wallet(int id, String name, double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    // ===== Setters =====

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Quan trọng:
     * Giúp Spinner / Dropdown hiển thị tên ví.
     */
    @Override
    public String toString() {
        return name;
    }
}