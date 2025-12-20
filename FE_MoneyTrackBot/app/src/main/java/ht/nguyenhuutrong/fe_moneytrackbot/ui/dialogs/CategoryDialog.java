package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

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
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;

/**
 * Dialog dùng chung cho:
 * - Thêm danh mục
 * - Cập nhật danh mục
 * - Xóa danh mục
 */
public class CategoryDialog {

    /**
     * Listener xử lý hành động từ Dialog
     */
    public interface OnCategoryActionListener {
        void onCreate(String name, String type);
        void onUpdate(Category category);
        void onDelete(int id);
    }

    /**
     * Hiển thị dialog THÊM danh mục
     */
    public static void showAdd(Context context, String defaultType,
                               OnCategoryActionListener listener) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_category, null);

        // Views
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextInputEditText etName = view.findViewById(R.id.et_category_name);
        RadioButton rbExpense = view.findViewById(R.id.rb_expense);
        RadioButton rbIncome = view.findViewById(R.id.rb_income);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        // UI setup
        tvTitle.setText("Thêm danh mục mới");
        btnConfirm.setText("THÊM");

        // Chọn loại mặc định
        if ("income".equals(defaultType)) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        // Bo góc dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() == null
                    ? ""
                    : etName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context,
                        "Vui lòng nhập tên danh mục!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String type = rbIncome.isChecked() ? "income" : "expense";

            if (listener != null) {
                listener.onCreate(name, type);
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Hiển thị dialog CHI TIẾT – cho phép CẬP NHẬT hoặc XÓA
     */
    public static void showUpdateDelete(Context context, Category category,
                                        OnCategoryActionListener listener) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_category, null);

        // Views
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextInputEditText etName = view.findViewById(R.id.et_category_name);
        RadioButton rbExpense = view.findViewById(R.id.rb_expense);
        RadioButton rbIncome = view.findViewById(R.id.rb_income);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        TextView btnConfirm = view.findViewById(R.id.btn_confirm);

        // UI setup
        tvTitle.setText("Chi tiết danh mục");
        btnConfirm.setText("CẬP NHẬT");

        btnCancel.setText("XÓA");
        btnCancel.setTextColor(Color.parseColor("#F44336"));

        // Fill dữ liệu cũ
        etName.setText(category.getName());
        if ("income".equals(category.getType())) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        // XÓA danh mục
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(category.getId());
            }
            dialog.dismiss();
        });

        // CẬP NHẬT danh mục
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() == null
                    ? ""
                    : etName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(context,
                        "Tên không được để trống!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            category.setName(name);
            category.setType(rbIncome.isChecked() ? "income" : "expense");

            if (listener != null) {
                listener.onUpdate(category);
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}