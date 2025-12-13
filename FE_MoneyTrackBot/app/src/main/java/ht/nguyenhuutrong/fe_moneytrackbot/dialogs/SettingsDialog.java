package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

import android.app.AlertDialog;
import android.content.Context;

public class SettingsDialog {

    public interface OnLogoutConfirmListener {
        void onConfirm();
    }

    public static void showLogoutConfirmation(Context context, OnLogoutConfirmListener listener) {
        if (context == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> listener.onConfirm())
                .setNegativeButton("Hủy", null)
                .show();
    }
}