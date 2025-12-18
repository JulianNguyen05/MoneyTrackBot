package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;

public class TransactionDialog {

    public interface DialogListener {
        void onSave(Transaction t, Integer id);
        void onDelete(int id);
    }

    public static void show(Context context, Transaction existingTransaction,
                            List<Wallet> wallets, List<Category> categories,
                            Wallet currentWallet,
                            DialogListener listener) {

        if (wallets == null || wallets.isEmpty() || categories == null || categories.isEmpty()) {
            Toast.makeText(context, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Inflate View
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null);

        // 2. Bind View
        TextInputLayout tilAmount = view.findViewById(R.id.til_amount);
        EditText etAmount = view.findViewById(R.id.et_amount);
        EditText etNote = view.findViewById(R.id.et_note);
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        AutoCompleteTextView autoCat = view.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = view.findViewById(R.id.auto_complete_wallet);

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        final int[] selectedIds = {-1, -1}; // [0]=WalletId, [1]=CatId

        // 3. Tạo Dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 4. Phân loại danh mục
        List<Category> expenseList = new ArrayList<>();
        List<Category> incomeList = new ArrayList<>();
        for (Category c : categories) {
            if ("income".equals(c.getType())) incomeList.add(c);
            else expenseList.add(c);
        }

        // 5. Logic UI update
        Runnable updateUiByType = () -> {
            boolean isIncome = rgType.getCheckedRadioButtonId() == R.id.rb_income;
            int colorRes = isIncome ? R.color.normal_weight : R.color.obese;
            int iconRes = isIncome ? R.drawable.ic_triangle_up : R.drawable.ic_triangle_down;
            int color = ContextCompat.getColor(context, colorRes);

            tilAmount.setStartIconDrawable(iconRes);
            tilAmount.setStartIconTintList(ColorStateList.valueOf(color));
            tilAmount.setBoxStrokeColor(color);
            tilAmount.setHintTextColor(ColorStateList.valueOf(color));
            etAmount.setTextColor(color);

            List<Category> list = isIncome ? incomeList : expenseList;
            autoCat.setAdapter(new ArrayAdapter<>(context, R.layout.item_dropdown, list));

            // 🔥 SỬA LỖI Ở ĐÂY: Kiểm tra thủ công xem ID đã chọn có trong list mới không
            boolean isCurrentCategoryInList = false;
            if (selectedIds[1] != -1) {
                for (Category c : list) {
                    if (c.getId() == selectedIds[1]) {
                        isCurrentCategoryInList = true;
                        break;
                    }
                }
            }

            // Nếu chưa chọn gì hoặc danh mục cũ không nằm trong loại mới (Thu/Chi) -> Reset về cái đầu tiên
            if (autoCat.getText().toString().isEmpty() || !isCurrentCategoryInList) {
                autoCat.setText("");
                selectedIds[1] = -1;
                if (!list.isEmpty()) {
                    autoCat.setText(list.get(0).getName(), false);
                    selectedIds[1] = list.get(0).getId();
                }
            }
        };

        rgType.setOnCheckedChangeListener((g, id) -> updateUiByType.run());
        autoCat.setOnItemClickListener((p, v, pos, id) -> selectedIds[1] = ((Category) p.getItemAtPosition(pos)).getId());
        autoWallet.setAdapter(new ArrayAdapter<>(context, R.layout.item_dropdown, wallets));
        autoWallet.setOnItemClickListener((p, v, pos, id) -> selectedIds[0] = ((Wallet) p.getItemAtPosition(pos)).getId());

        // 6. XỬ LÝ LOGIC FILL DATA
        if (existingTransaction != null) {
            // === CHẾ ĐỘ SỬA ===
            etAmount.setText(String.valueOf((long) Math.abs(existingTransaction.getAmount())));
            etNote.setText(existingTransaction.getNote());

            for (Wallet w : wallets) {
                if (w.getId() == existingTransaction.getWalletId()) {
                    autoWallet.setText(w.getName(), false);
                    selectedIds[0] = w.getId();
                    break;
                }
            }
            // Tìm Category cũ để hiển thị đúng tab Thu/Chi
            for (Category c : categories) {
                if (c.getId() == existingTransaction.getCategoryId()) {
                    // Set tab trước
                    if ("income".equals(c.getType())) rgType.check(R.id.rb_income);
                    else rgType.check(R.id.rb_expense);

                    // Set ID trước khi chạy updateUi để nó không bị reset
                    selectedIds[1] = c.getId();

                    // Cập nhật list dropdown theo loại
                    List<Category> list = "income".equals(c.getType()) ? incomeList : expenseList;
                    autoCat.setAdapter(new ArrayAdapter<>(context, R.layout.item_dropdown, list));

                    // Set Text hiển thị
                    autoCat.setText(c.getName(), false);
                    break;
                }
            }
            updateUiByType.run(); // Cập nhật màu sắc UI

            // --- BIẾN ĐỔI NÚT ---
            btnSave.setText("Cập nhật");
            btnCancel.setText("Xóa");
            btnCancel.setTextColor(Color.RED);

            btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                        .setPositiveButton("Xóa", (confirmDialog, which) -> {
                            listener.onDelete(existingTransaction.getId());
                            dialog.dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

        } else {
            // === CHẾ ĐỘ THÊM MỚI ===
            updateUiByType.run();

            if (currentWallet != null) {
                autoWallet.setText(currentWallet.getName(), false);
                selectedIds[0] = currentWallet.getId();
            } else if (!wallets.isEmpty()) {
                autoWallet.setText(wallets.get(0).getName(), false);
                selectedIds[0] = wallets.get(0).getId();
            }

            btnSave.setText("Lưu");
            btnCancel.setText("Hủy");
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        // 7. Xử lý logic nút SAVE
        btnSave.setOnClickListener(v -> {
            boolean isExpense = rgType.getCheckedRadioButtonId() == R.id.rb_expense;
            Transaction t = buildTransaction(etAmount, etNote, selectedIds[1], selectedIds[0], isExpense);

            if (t != null) {
                if (existingTransaction == null) {
                    listener.onSave(t, null);
                } else {
                    listener.onSave(t, existingTransaction.getId());
                }
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Vui lòng nhập số tiền và chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private static Transaction buildTransaction(EditText etAmt, EditText etNote, int catId, int walletId, boolean isExpense) {
        try {
            String amtStr = etAmt.getText().toString().trim();
            if (amtStr.isEmpty()) return null;

            double amt = Double.parseDouble(amtStr);
            if (catId == -1 || walletId == -1) return null;

            if (isExpense) amt = -Math.abs(amt);
            else amt = Math.abs(amt);

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            return new Transaction(amt, catId, etNote.getText().toString().trim(), date, walletId);
        } catch (Exception e) {
            return null;
        }
    }
}