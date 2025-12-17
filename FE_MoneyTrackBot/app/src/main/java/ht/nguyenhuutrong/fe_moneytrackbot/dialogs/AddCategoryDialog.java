package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import ht.nguyenhuutrong.fe_moneytrackbot.R;

public class AddCategoryDialog {

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(String name, String type);
    }

    public static void show(Context context, String currentType, OnCategoryCreatedListener listener) {
        // 1. Inflate layout từ XML
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        // 2. Ánh xạ View
        TextInputEditText etName = dialogView.findViewById(R.id.et_category_name);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_category_type);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_expense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rb_income);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // 3. Xử lý Logic hiển thị ban đầu (Auto check)
        if ("income".equals(currentType)) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        // 4. Tạo Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // --- Làm trong suốt nền mặc định để hiện bo góc ---
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 5. Xử lý sự kiện nút bấm
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên danh mục!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xác định loại dựa trên RadioButton đang được chọn
            String type = rbIncome.isChecked() ? "income" : "expense";

            // Gửi dữ liệu về Fragment
            if (listener != null) {
                listener.onCategoryCreated(name, type);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}