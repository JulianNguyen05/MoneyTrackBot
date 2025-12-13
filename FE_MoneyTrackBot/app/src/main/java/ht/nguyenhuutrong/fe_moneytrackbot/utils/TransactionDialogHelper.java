package ht.nguyenhuutrong.fe_moneytrackbot.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;

public class TransactionDialogHelper {

    public interface DialogListener {
        void onSave(Transaction t, Integer id);
        void onDelete(int id);
    }

    public static void show(Context context, Transaction existingTransaction,
                            List<Wallet> wallets, List<Category> categories,
                            DialogListener listener) {

        if (wallets == null || wallets.isEmpty() || categories == null || categories.isEmpty()) {
            Toast.makeText(context, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null);

        EditText etAmount = view.findViewById(R.id.et_amount);
        EditText etNote = view.findViewById(R.id.et_note);
        RadioGroup rgType = view.findViewById(R.id.rg_type);
        AutoCompleteTextView autoCat = view.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = view.findViewById(R.id.auto_complete_wallet);

        final int[] selectedIds = {-1, -1}; // [0]=WalletId, [1]=CatId

        // Logic Filter Category
        List<Category> expenseList = new ArrayList<>();
        List<Category> incomeList = new ArrayList<>();
        for (Category c : categories) {
            if ("income".equals(c.getType())) incomeList.add(c); else expenseList.add(c);
        }

        Runnable updateCatList = () -> {
            List<Category> list = (rgType.getCheckedRadioButtonId() == R.id.rb_income) ? incomeList : expenseList;
            autoCat.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, list));
        };

        rgType.setOnCheckedChangeListener((g, id) -> updateCatList.run());
        autoCat.setOnItemClickListener((p, v, pos, id) -> selectedIds[1] = ((Category)p.getItemAtPosition(pos)).getId());

        // Wallet Logic
        autoWallet.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, wallets));
        autoWallet.setOnItemClickListener((p, v, pos, id) -> selectedIds[0] = ((Wallet)p.getItemAtPosition(pos)).getId());

        // Fill Data (Edit Mode)
        if (existingTransaction != null) {
            etAmount.setText(String.valueOf((long)Math.abs(existingTransaction.getAmount())));
            etNote.setText(existingTransaction.getNote());
            // Find Wallet
            for (Wallet w : wallets) if (w.getId() == existingTransaction.getWalletId()) {
                autoWallet.setText(w.getName(), false);
                selectedIds[0] = w.getId();
                break;
            }
            // Find Category
            for (Category c : categories) if (c.getId() == existingTransaction.getCategoryId()) {
                if ("income".equals(c.getType())) rgType.check(R.id.rb_income); else rgType.check(R.id.rb_expense);
                updateCatList.run();
                autoCat.setText(c.getName(), false);
                selectedIds[1] = c.getId();
                break;
            }
        } else {
            updateCatList.run(); // Default
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);

        // Buttons
        if (existingTransaction == null) {
            builder.setTitle("Thêm Giao Dịch")
                    .setPositiveButton("Lưu", (d, w) -> {
                        Transaction t = buildTransaction(etAmount, etNote, selectedIds[1], selectedIds[0]);
                        if (t != null) listener.onSave(t, null);
                    });
        } else {
            builder.setTitle("Chi Tiết")
                    .setPositiveButton("Cập nhật", (d, w) -> {
                        Transaction t = buildTransaction(etAmount, etNote, selectedIds[1], selectedIds[0]);
                        if (t != null) listener.onSave(t, existingTransaction.getId());
                    })
                    .setNeutralButton("Xóa", (d, w) -> showDeleteConfirm(context, existingTransaction.getId(), listener));
        }
        builder.setNegativeButton("Hủy", null).show();
    }

    private static Transaction buildTransaction(EditText etAmt, EditText etNote, int catId, int walletId) {
        try {
            double amt = Double.parseDouble(etAmt.getText().toString().trim());
            if (catId == -1 || walletId == -1) return null;
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            return new Transaction(amt, catId, etNote.getText().toString().trim(), date, walletId);
        } catch (Exception e) { return null; }
    }

    private static void showDeleteConfirm(Context ctx, int id, DialogListener listener) {
        new AlertDialog.Builder(ctx).setTitle("Xóa?").setPositiveButton("Xóa", (d, w) -> listener.onDelete(id)).setNegativeButton("Hủy", null).show();
    }
}