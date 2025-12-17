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
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;

public class CategoryDialog {

    // Interface lắng nghe đủ 3 sự kiện: Thêm, Sửa, Xóa
    public interface OnCategoryActionListener {
        void onCreate(String name, String type);
        void onUpdate(Category category);
        void onDelete(int id);
    }

    // 1. Dialog THÊM MỚI
    public static void showAdd(Context context, String defaultType, OnCategoryActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        // Ánh xạ View
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextInputEditText etName = dialogView.findViewById(R.id.et_category_name);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_category_type);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_expense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rb_income);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);
        TextView btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        // --- Setup giao diện THÊM ---
        tvTitle.setText("Thêm danh mục mới");
        btnConfirm.setText("THÊM");

        // Mặc định chọn loại theo tham số truyền vào
        if ("income".equals(defaultType)) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Làm nền trong suốt để hiện bo góc
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Nút HỦY -> Đóng dialog
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Nút THÊM -> Gọi hàm onCreate
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên danh mục!", Toast.LENGTH_SHORT).show();
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

    // 2. Dialog SỬA / XÓA (Tái sử dụng layout nhưng đổi nút)
    public static void showUpdateDelete(Context context, Category category, OnCategoryActionListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        // Ánh xạ View
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextInputEditText etName = dialogView.findViewById(R.id.et_category_name);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_category_type);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_expense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rb_income);
        TextView btnCancel = dialogView.findViewById(R.id.btn_cancel);   // Sẽ biến thành nút XÓA
        TextView btnConfirm = dialogView.findViewById(R.id.btn_confirm); // Sẽ biến thành nút CẬP NHẬT

        // --- Setup giao diện SỬA ---
        tvTitle.setText("Chi tiết danh mục");

        // Điền dữ liệu cũ
        etName.setText(category.getName());
        if ("income".equals(category.getType())) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        // 🔥 BIẾN HÌNH NÚT BẤM
        btnConfirm.setText("CẬP NHẬT");

        btnCancel.setText("XÓA"); // Đổi text thành XÓA
        btnCancel.setTextColor(Color.parseColor("#F44336")); // Đổi màu chữ thành Đỏ

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Nút Trái (Lúc này là XÓA) -> Gọi onDelete
        btnCancel.setOnClickListener(v -> {
            // Có thể thêm Dialog xác nhận "Bạn có chắc chắn xóa?" ở đây nếu muốn
            if (listener != null) {
                listener.onDelete(category.getId());
            }
            dialog.dismiss();
        });

        // Nút Phải (Lúc này là CẬP NHẬT) -> Gọi onUpdate
        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(context, "Tên không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật vào object category
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