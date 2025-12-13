package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

// Interface để gửi kết quả về HomeFragment
public class AddCategoryDialog {

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(String name, String type);
    }

    public static void show(Context context, String currentType, OnCategoryCreatedListener listener) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etName = new EditText(context);
        etName.setHint("Tên danh mục (vd: Ăn sáng)");
        layout.addView(etName);

        TextView tvLabel = new TextView(context);
        tvLabel.setText("Loại danh mục:");
        tvLabel.setPadding(0, 30, 0, 10);
        layout.addView(tvLabel);

        final RadioGroup rgType = new RadioGroup(context);
        rgType.setOrientation(LinearLayout.HORIZONTAL);

        RadioButton rbExpense = new RadioButton(context);
        rbExpense.setId(View.generateViewId());
        rbExpense.setText("Chi phí");

        RadioButton rbIncome = new RadioButton(context);
        rbIncome.setId(View.generateViewId());
        rbIncome.setText("Thu nhập");

        rgType.addView(rbExpense);
        rgType.addView(rbIncome);
        layout.addView(rgType);

        // Auto check
        if ("income".equals(currentType)) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        new AlertDialog.Builder(context)
                .setTitle("Thêm Danh Mục Mới")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String type = rbIncome.isChecked() ? "income" : "expense";

                    // Gửi dữ liệu về HomeFragment xử lý
                    if (listener != null) {
                        listener.onCategoryCreated(name, type);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}