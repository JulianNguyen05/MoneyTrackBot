package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> listener.onConfirm());
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }

        dialog.show();

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setTextColor(Color.BLACK);
        }

        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#F44336")); // Màu đỏ
        }

        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#757575")); // Màu xám
        }
    }
}