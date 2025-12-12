package ht.nguyenhuutrong.fe_moneytrackbot.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Transaction implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    // 🔥 SỬA 1: Dùng int để gửi ID lên server (tránh lỗi 400 Bad Request)
    @SerializedName("category")
    private int categoryId;

    // 🔥 SỬA 2: Thêm field này để hứng tên danh mục từ Server (để hiển thị lên Adapter)
    @SerializedName("category_name")
    private String categoryName;

    // 🔥 SỬA 3: Backend dùng key "description", không phải "note"
    @SerializedName("description")
    private String note;

    @SerializedName("date")
    private String date;

    @SerializedName("wallet")
    private int walletId;

    // (Tùy chọn) Thêm tên ví nếu muốn hiển thị
    @SerializedName("wallet_name")
    private String walletName;

    // --- Constructor 1: Dùng khi TẠO MỚI (Gửi lên Server) ---
    // Lúc tạo chỉ cần gửi ID (categoryId), không cần gửi tên
    public Transaction(double amount, int categoryId, String note, String date, int walletId) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.note = note;
        this.date = date;
        this.walletId = walletId;
    }

    // --- Constructor 2: Dùng khi NHẬN VỀ (Đầy đủ thông tin) ---
    public Transaction(int id, double amount, int categoryId, String categoryName, String note, String date, int walletId, String walletName) {
        this.id = id;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.note = note;
        this.date = date;
        this.walletId = walletId;
        this.walletName = walletName;
    }

    // --- Getters ---
    public int getId() { return id; }
    public double getAmount() { return amount; }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; } // Dùng hàm này để setText trong Adapter

    public String getNote() { return note; }
    public String getDate() { return date; }
    public int getWalletId() { return walletId; }
    public String getWalletName() { return walletName; }
}