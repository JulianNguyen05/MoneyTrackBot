package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;

/**
 * Dialog xử lý Thêm / Sửa / Xóa Ví
 */
public class WalletDialog {

    /**
     * Callback trả kết quả thao tác Ví
     */
    public interface OnWalletActionListener {
        void onCreate(String name, double balance);
        void onUpdate(Wallet wallet);
        void onDelete(int walletId);
    }

    /* =========================================================
     * 1. Dialog THÊM VÍ
     * ========================================================= */

    public static void showAddWallet(Context context, OnWalletActionListener listener) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_wallet, null);

        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText etName = view.findViewById(R.id.et_wallet_name);
        EditText etBalance = view.findViewById(R.id.et_wallet_balance);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        tvTitle.setText("Thêm ví mới");
        btnConfirm.setText("THÊM");

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        // Nền trong suốt để hiển thị bo góc custom
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String balanceStr = etBalance.getText().toString().trim();

            if (name.isEmpty() || listener == null) return;

            double balance = balanceStr.isEmpty()
                    ? 0
                    : Double.parseDouble(balanceStr);

            listener.onCreate(name, balance);
            dialog.dismiss();
        });

        dialog.show();
    }

    /* =========================================================
     * 2. Dialog SỬA / XÓA VÍ
     * ========================================================= */

    public static void showUpdateDelete(
            Context context,
            Wallet wallet,
            OnWalletActionListener listener
    ) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_wallet, null);

        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText etName = view.findViewById(R.id.et_wallet_name);
        EditText etBalance = view.findViewById(R.id.et_wallet_balance);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        tvTitle.setText("Chi tiết ví");
        btnConfirm.setText("CẬP NHẬT");

        etName.setText(wallet.getName());
        etBalance.setText(String.valueOf((long) wallet.getBalance()));

        // Biến nút Cancel thành XÓA
        btnCancel.setText("XÓA");
        btnCancel.setTextColor(Color.parseColor("#F44336"));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Xóa ví
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(wallet.getId());
            }
            dialog.dismiss();
        });

        // Cập nhật ví
        btnConfirm.setOnClickListener(v -> {
            if (listener == null) return;

            wallet.setName(etName.getText().toString().trim());

            try {
                wallet.setBalance(
                        Double.parseDouble(etBalance.getText().toString())
                );
                listener.onUpdate(wallet);
            } catch (NumberFormatException ignored) {
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}