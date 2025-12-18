package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;

public class CashFlowResponse {
    @SerializedName("total_income")
    private double totalIncome;

    @SerializedName("total_expense")
    private double totalExpense;

    @SerializedName("net_change")
    private double netChange;

    // Getter
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
    public double getNetChange() { return netChange; }
}