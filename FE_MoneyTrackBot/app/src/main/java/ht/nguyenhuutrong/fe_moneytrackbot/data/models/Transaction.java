package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Transaction implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    // --- CÁC TRƯỜNG LIÊN QUAN ĐẾN CATEGORY ---
    @SerializedName("category")
    private int categoryId; // Dùng để gửi ID khi tạo mới

    @SerializedName("category_name")
    private String categoryName; // Dùng để hiển thị tên

    // 🔥 QUAN TRỌNG: Thêm trường này để Adapter biết là Thu hay Chi
    @SerializedName("category_type")
    private String categoryType;

    // --- CÁC TRƯỜNG KHÁC ---
    @SerializedName("description") // Backend trả về key "description"
    private String description;    // Đổi tên biến từ 'note' sang 'description' cho đồng bộ

    @SerializedName("date")
    private String date;

    @SerializedName("wallet")
    private int walletId;

    @SerializedName("wallet_name")
    private String walletName;

    // --- Constructor rỗng (Cần thiết cho Gson) ---
    public Transaction() {
    }

    // --- Constructor để TẠO MỚI (Gửi lên Server) ---
    public Transaction(double amount, int categoryId, String description, String date, int walletId) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
        this.walletId = walletId;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public double getAmount() { return amount; }

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }

    // Hàm này Adapter đang gọi để kiểm tra Thu/Chi
    public String getType() {
        return categoryType != null ? categoryType : "income";
    }

    public String getDescription() { return description; }
    // Giữ lại getNote() nếu code cũ còn dùng, nhưng trỏ về description
    public String getNote() { return description; }

    public String getDate() { return date; }
    public int getWalletId() { return walletId; }
    public String getWalletName() { return walletName; }
}