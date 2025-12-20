package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * CashFlowEntry
 * --------------------------------------------------
 * Đại diện cho dữ liệu dòng tiền trong một ngày.
 * Được sử dụng trong báo cáo Cash Flow.
 */
public class CashFlowEntry {

    /**
     * Ngày thống kê (yyyy-MM-dd)
     */
    @SerializedName("day")
    private String day;

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    // ===== Getters =====

    public String getDay() {
        return day;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }
}