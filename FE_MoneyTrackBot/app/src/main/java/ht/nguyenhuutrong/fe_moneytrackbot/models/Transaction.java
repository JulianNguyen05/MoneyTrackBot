package ht.nguyenhuutrong.fe_moneytrackbot.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Transaction implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("category")
    private String category; // Ví dụ: "Ăn uống"

    @SerializedName("note")
    private String note;     // Ví dụ: "Bún bò"

    @SerializedName("date")
    private String date;     // Format: YYYY-MM-DD

    @SerializedName("wallet")
    private int walletId;    // ID của ví

    // Constructor dùng để gửi lên Server (Tạo mới)
    public Transaction(double amount, String category, String note, String date, int walletId) {
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.walletId = walletId;
    }

    // Constructor đầy đủ
    public Transaction(int id, double amount, String category, String note, String date, int walletId) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.walletId = walletId;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public String getDate() { return date; }
    public int getWalletId() { return walletId; }
}