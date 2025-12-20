package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * CashFlowResponse
 * --------------------------------------------------
 * Tổng hợp dữ liệu dòng tiền trong một khoảng thời gian.
 * Được sử dụng trong báo cáo Cash Flow.
 */
public class CashFlowResponse {

    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    /**
     * Chênh lệch thu - chi (income - expense)
     */
    @SerializedName("net_change")
    private double netChange;

    // ===== Getters =====

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetChange() {
        return netChange;
    }
}