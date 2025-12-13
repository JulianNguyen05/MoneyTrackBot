package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;

public class WalletDialogs {

    public interface OnWalletActionListener {
        void onCreate(String name, double balance);
        void onUpdate(Wallet wallet);
        void onDelete(int walletId);
    }

    // 1. Dialog Thêm Ví
    public static void showAddWallet(Context context, OnWalletActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);

        new AlertDialog.Builder(context)
                .setTitle("Thêm ví mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();
                    if (!name.isEmpty() && listener != null) {
                        double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);
                        listener.onCreate(name, balance);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 2. Dialog Sửa/Xóa Ví
    public static void showUpdateDelete(Context context, Wallet wallet, OnWalletActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);

        // Fill dữ liệu cũ
        etName.setText(wallet.getName());
        etBalance.setText(String.valueOf((long) wallet.getBalance()));

        new AlertDialog.Builder(context)
                .setTitle("Chi tiết ví")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    if (listener != null) {
                        wallet.setName(etName.getText().toString());
                        try {
                            wallet.setBalance(Double.parseDouble(etBalance.getText().toString()));
                            listener.onUpdate(wallet);
                        } catch (NumberFormatException e) { }
                    }
                })
                .setNeutralButton("Xóa", (dialog, which) -> {
                    if (listener != null) listener.onDelete(wallet.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}