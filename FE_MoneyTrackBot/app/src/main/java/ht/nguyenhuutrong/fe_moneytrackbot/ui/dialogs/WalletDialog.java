package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;

public class WalletDialog {

    public interface OnWalletActionListener {
        void onCreate(String name, double balance);
        void onUpdate(Wallet wallet);
        void onDelete(int walletId);
    }

    // 1. Dialog Thêm Ví
    public static void showAddWallet(Context context, OnWalletActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);

        // Ánh xạ View từ XML
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // Thiết lập text cho đúng ngữ cảnh
        tvTitle.setText("Thêm ví mới");
        btnConfirm.setText("THÊM");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView); // Chỉ set View, KHÔNG set Title hay Button ở đây nữa

        AlertDialog dialog = builder.create();

        // --- QUAN TRỌNG: Làm trong suốt nền hệ thống để hiện nền bo tròn của ta ---
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Xử lý sự kiện nút bấm
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String balanceStr = etBalance.getText().toString().trim();
            if (!name.isEmpty() && listener != null) {
                double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);
                listener.onCreate(name, balance);
                dialog.dismiss(); // Đóng dialog sau khi thêm
            }
        });

        dialog.show();
    }

    // 2. Dialog Sửa/Xóa Ví
    public static void showUpdateDelete(Context context, Wallet wallet, OnWalletActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // Setup giao diện cho chế độ Sửa
        tvTitle.setText("Chi tiết ví");
        btnConfirm.setText("CẬP NHẬT");
        etName.setText(wallet.getName());
        etBalance.setText(String.valueOf((long) wallet.getBalance()));

        btnCancel.setText("XÓA");
        btnCancel.setTextColor(Color.parseColor("#F44336"));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> {
            // Xử lý xóa
            if (listener != null) listener.onDelete(wallet.getId());
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                wallet.setName(etName.getText().toString());
                try {
                    wallet.setBalance(Double.parseDouble(etBalance.getText().toString()));
                    listener.onUpdate(wallet);
                } catch (NumberFormatException e) { }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}