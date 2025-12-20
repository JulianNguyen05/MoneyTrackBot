package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Budget
 * --------------------------------------------------
 * Model ngân sách theo danh mục và theo tháng/năm.
 * Dùng cho cả request (tạo/cập nhật) và response từ API.
 */
public class Budget {

    // ===== Response fields =====

    @SerializedName("id")
    private int id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("month")
    private int month;

    @SerializedName("year")
    private int year;

    /**
     * Thông tin chi tiết danh mục (object lồng).
     * Chỉ xuất hiện trong response.
     */
    @SerializedName("category_details")
    private Category categoryDetails;

    // ===== Request fields =====

    /**
     * ID danh mục dùng khi tạo/cập nhật ngân sách.
     * Không xuất hiện trong response.
     */
    @SerializedName("category")
    private int category;

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public Category getCategoryDetails() {
        return categoryDetails;
    }

    // ===== Setters =====

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Gán ID danh mục khi tạo hoặc cập nhật Budget.
     */
    public void setCategory(int categoryId) {
        this.category = categoryId;
    }
}