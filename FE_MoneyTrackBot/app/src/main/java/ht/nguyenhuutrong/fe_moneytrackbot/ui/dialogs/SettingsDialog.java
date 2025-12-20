package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Dialog cài đặt – xử lý các hành động xác nhận
 */
public class SettingsDialog {

    /**
     * Callback xác nhận đăng xuất
     */
    public interface OnLogoutConfirmListener {
        void onConfirm();
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    public static void showLogoutConfirmation(
            Context context,
            OnLogoutConfirmListener listener
    ) {
        if (context == null || listener == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất",
                        (dialog, which) -> listener.onConfirm())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}