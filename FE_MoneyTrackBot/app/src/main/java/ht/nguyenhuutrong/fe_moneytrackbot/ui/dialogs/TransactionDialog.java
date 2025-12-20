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

/**
 * Dialog thêm / sửa / xóa giao dịch
 */
public class TransactionDialog {

    /**
     * Callback xử lý hành động của dialog
     */
    public interface DialogListener {
        void onSave(Transaction transaction, Integer updateId);
        void onDelete(int id);
    }

    public static void show(
            Context context,
            Transaction existingTransaction,
            List<Wallet> wallets,
            List<Category> categories,
            Wallet currentWallet,
            DialogListener listener
    ) {
        if (wallets == null || wallets.isEmpty()
                || categories == null || categories.isEmpty()) {
            Toast.makeText(context, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        /* ---------------- Inflate & Bind View ---------------- */

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_transaction, null);

        TextInputLayout tilAmount = view.findViewById(R.id.til_amount);
        EditText etAmount = view.findViewById(R.id.et_amount);
        EditText etNote = view.findViewById(R.id.et_note);
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        AutoCompleteTextView autoCategory = view.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = view.findViewById(R.id.auto_complete_wallet);

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        // [0] = WalletId, [1] = CategoryId
        final int[] selectedIds = {-1, -1};

        /* ---------------- Create Dialog ---------------- */

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        /* ---------------- Phân loại Category ---------------- */

        List<Category> expenseCategories = new ArrayList<>();
        List<Category> incomeCategories = new ArrayList<>();

        for (Category c : categories) {
            if ("income".equals(c.getType())) incomeCategories.add(c);
            else expenseCategories.add(c);
        }

        /* ---------------- Update UI theo loại Thu / Chi ---------------- */

        Runnable updateUiByType = () -> {
            boolean isIncome = rgType.getCheckedRadioButtonId() == R.id.rb_income;

            int color = ContextCompat.getColor(
                    context,
                    isIncome ? R.color.normal_weight : R.color.obese
            );

            tilAmount.setStartIconDrawable(
                    isIncome ? R.drawable.ic_triangle_up : R.drawable.ic_triangle_down
            );
            tilAmount.setStartIconTintList(ColorStateList.valueOf(color));
            tilAmount.setBoxStrokeColor(color);
            tilAmount.setHintTextColor(ColorStateList.valueOf(color));
            etAmount.setTextColor(color);

            List<Category> currentList =
                    isIncome ? incomeCategories : expenseCategories;

            autoCategory.setAdapter(
                    new ArrayAdapter<>(context, R.layout.item_dropdown, currentList)
            );

            // Nếu category hiện tại không thuộc loại mới → reset
            boolean validCategory = false;
            for (Category c : currentList) {
                if (c.getId() == selectedIds[1]) {
                    validCategory = true;
                    break;
                }
            }

            if (!validCategory && !currentList.isEmpty()) {
                autoCategory.setText(currentList.get(0).getName(), false);
                selectedIds[1] = currentList.get(0).getId();
            }
        };

        rgType.setOnCheckedChangeListener((g, id) -> updateUiByType.run());

        autoCategory.setOnItemClickListener(
                (p, v, pos, id) ->
                        selectedIds[1] =
                                ((Category) p.getItemAtPosition(pos)).getId()
        );

        autoWallet.setAdapter(
                new ArrayAdapter<>(context, R.layout.item_dropdown, wallets)
        );
        autoWallet.setOnItemClickListener(
                (p, v, pos, id) ->
                        selectedIds[0] =
                                ((Wallet) p.getItemAtPosition(pos)).getId()
        );

        /* ---------------- Chế độ SỬA ---------------- */

        if (existingTransaction != null) {

            etAmount.setText(
                    String.valueOf((long) Math.abs(existingTransaction.getAmount()))
            );
            etNote.setText(existingTransaction.getNote());

            for (Wallet w : wallets) {
                if (w.getId() == existingTransaction.getWalletId()) {
                    autoWallet.setText(w.getName(), false);
                    selectedIds[0] = w.getId();
                    break;
                }
            }

            for (Category c : categories) {
                if (c.getId() == existingTransaction.getCategoryId()) {
                    rgType.check(
                            "income".equals(c.getType())
                                    ? R.id.rb_income
                                    : R.id.rb_expense
                    );
                    selectedIds[1] = c.getId();
                    autoCategory.setText(c.getName(), false);
                    break;
                }
            }

            updateUiByType.run();

            btnSave.setText("Cập nhật");
            btnCancel.setText("Xóa");
            btnCancel.setTextColor(Color.RED);

            btnCancel.setOnClickListener(v ->
                    new AlertDialog.Builder(context)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                listener.onDelete(existingTransaction.getId());
                                dialog.dismiss();
                            })
                            .setNegativeButton("Hủy", null)
                            .show()
            );

        }
        /* ---------------- Chế độ THÊM ---------------- */
        else {

            updateUiByType.run();

            Wallet defaultWallet =
                    currentWallet != null ? currentWallet : wallets.get(0);

            autoWallet.setText(defaultWallet.getName(), false);
            selectedIds[0] = defaultWallet.getId();

            btnSave.setText("Lưu");
            btnCancel.setText("Hủy");
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        /* ---------------- Save Transaction ---------------- */

        btnSave.setOnClickListener(v -> {
            boolean isExpense =
                    rgType.getCheckedRadioButtonId() == R.id.rb_expense;

            Transaction t = buildTransaction(
                    etAmount,
                    etNote,
                    selectedIds[1],
                    selectedIds[0],
                    isExpense
            );

            if (t == null) {
                Toast.makeText(
                        context,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            listener.onSave(
                    t,
                    existingTransaction == null
                            ? null
                            : existingTransaction.getId()
            );

            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Tạo Transaction từ dữ liệu nhập
     */
    private static Transaction buildTransaction(
            EditText etAmount,
            EditText etNote,
            int categoryId,
            int walletId,
            boolean isExpense
    ) {
        try {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty() || categoryId == -1 || walletId == -1)
                return null;

            double amount = Math.abs(Double.parseDouble(amountStr));
            amount = isExpense ? -amount : amount;

            String date = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            ).format(new Date());

            return new Transaction(
                    amount,
                    categoryId,
                    etNote.getText().toString().trim(),
                    date,
                    walletId
            );
        } catch (Exception e) {
            return null;
        }
    }
}