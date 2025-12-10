package ht.nguyenhuutrong.fe_moneytrackbot.models;

public class TransactionModel {
    private String date;
    private String category;
    private String note;
    private String amount;

    public TransactionModel(String date, String category, String note, String amount) {
        this.date = date;
        this.category = category;
        this.note = note;
        this.amount = amount;
    }

    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getNote() { return note; }
    public String getAmount() { return amount; }
}
