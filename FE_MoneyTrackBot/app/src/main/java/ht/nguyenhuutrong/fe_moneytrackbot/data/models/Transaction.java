package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Transaction
 * --------------------------------------------------
 * Model giao dịch (Thu / Chi) dùng cho:
 * - Parse dữ liệu từ API
 * - Gửi dữ liệu tạo giao dịch mới
 */
public class Transaction implements Serializable {

    // ===== Basic fields =====

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    // ===== Category fields =====

    /** ID category dùng khi tạo mới giao dịch */
    @SerializedName("category")
    private int categoryId;

    /** Tên category để hiển thị UI */
    @SerializedName("category_name")
    private String categoryName;

    /** Loại category: "income" | "expense" (dùng cho Adapter) */
    @SerializedName("category_type")
    private String categoryType;

    // ===== Transaction info =====

    @SerializedName("description")
    private String description;

    @SerializedName("date")
    private String date;

    // ===== Wallet fields =====

    @SerializedName("wallet")
    private int walletId;

    @SerializedName("wallet_name")
    private String walletName;

    // ===== Constructors =====

    /** Constructor rỗng bắt buộc cho Gson */
    public Transaction() {
    }

    /**
     * Constructor dùng khi tạo giao dịch mới (POST).
     */
    public Transaction(double amount, int categoryId, String description, String date, int walletId) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
        this.date = date;
        this.walletId = walletId;
    }

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Dùng để xác định Thu / Chi trong Adapter.
     * Mặc định là "income" nếu backend không trả về.
     */
    public String getType() {
        return categoryType != null ? categoryType : "income";
    }

    public String getDescription() {
        return description;
    }

    /** Tương thích ngược với code cũ */
    public String getNote() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public int getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }
}