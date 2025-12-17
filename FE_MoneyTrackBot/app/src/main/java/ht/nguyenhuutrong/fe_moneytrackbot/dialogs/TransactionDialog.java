package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

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
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;

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

        // 3. Phân loại danh mục
        List<Category> expenseList = new ArrayList<>();
        List<Category> incomeList = new ArrayList<>();
        for (Category c : categories) {
            if ("income".equals(c.getType())) incomeList.add(c);
            else expenseList.add(c);
        }

        // 4. Logic đổi Giao diện (Màu sắc & Icon) khi chọn loại giao dịch
        Runnable updateUiByType = () -> {
            boolean isIncome = rgType.getCheckedRadioButtonId() == R.id.rb_income;

            // Chọn màu và icon tương ứng
            int colorRes = isIncome ? R.color.normal_weight : R.color.obese;
            int iconRes = isIncome ? R.drawable.ic_triangle_up : R.drawable.ic_triangle_down;
            int color = ContextCompat.getColor(context, colorRes);

            // Cập nhật TextInputLayout (Số tiền)
            tilAmount.setStartIconDrawable(iconRes);
            tilAmount.setStartIconTintList(ColorStateList.valueOf(color));
            tilAmount.setBoxStrokeColor(color);
            tilAmount.setHintTextColor(ColorStateList.valueOf(color));

            // Cập nhật EditText bên trong
            etAmount.setTextColor(color);

            // Cập nhật danh sách Category
            List<Category> list = isIncome ? incomeList : expenseList;

            // 🔥 CẬP NHẬT QUAN TRỌNG: Sử dụng R.layout.item_dropdown để chữ màu đen
            autoCat.setAdapter(new ArrayAdapter<>(context, R.layout.item_dropdown, list));

            autoCat.setText(""); // Clear text khi đổi loại
            selectedIds[1] = -1;
            if (!list.isEmpty()) {
                autoCat.setText(list.get(0).getName(), false);
                selectedIds[1] = list.get(0).getId();
            }
        };

        rgType.setOnCheckedChangeListener((g, id) -> updateUiByType.run());
        autoCat.setOnItemClickListener((p, v, pos, id) -> selectedIds[1] = ((Category) p.getItemAtPosition(pos)).getId());

        // 5. Logic Ví (Wallet)
        // 🔥 CẬP NHẬT QUAN TRỌNG: Sử dụng R.layout.item_dropdown cho Ví luôn
        autoWallet.setAdapter(new ArrayAdapter<>(context, R.layout.item_dropdown, wallets));
        autoWallet.setOnItemClickListener((p, v, pos, id) -> selectedIds[0] = ((Wallet) p.getItemAtPosition(pos)).getId());

        // 6. Điền dữ liệu (Fill Data)
        if (existingTransaction != null) {
            // --- CHẾ ĐỘ SỬA ---
            etAmount.setText(String.valueOf((long) Math.abs(existingTransaction.getAmount())));
            etNote.setText(existingTransaction.getNote());

            // Chọn ví cũ
            for (Wallet w : wallets) {
                if (w.getId() == existingTransaction.getWalletId()) {
                    autoWallet.setText(w.getName(), false);
                    selectedIds[0] = w.getId();
                    break;
                }
            }
            // Chọn danh mục cũ
            for (Category c : categories) {
                if (c.getId() == existingTransaction.getCategoryId()) {
                    if ("income".equals(c.getType())) rgType.check(R.id.rb_income);
                    else rgType.check(R.id.rb_expense);

                    updateUiByType.run(); // Cập nhật màu sắc trước khi set text
                    autoCat.setText(c.getName(), false);
                    selectedIds[1] = c.getId();
                    break;
                }
            }
            btnSave.setText("Cập nhật");
        } else {
            // --- CHẾ ĐỘ THÊM MỚI ---
            updateUiByType.run(); // Chạy lần đầu để set màu mặc định (Expense)

            // Tự động chọn Ví đang hiển thị ở Fragment
            if (currentWallet != null) {
                // Nếu đang chọn ví cụ thể
                autoWallet.setText(currentWallet.getName(), false);
                selectedIds[0] = currentWallet.getId();
            } else if (!wallets.isEmpty()) {
                // Nếu đang chọn "Tất cả ví", mặc định lấy ví đầu tiên
                autoWallet.setText(wallets.get(0).getName(), false);
                selectedIds[0] = wallets.get(0).getId();
            }
        }

        // 7. Tạo Dialog và Xử lý Button
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Làm nền trong suốt để thấy bo góc của layout custom
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            Transaction t = buildTransaction(etAmount, etNote, selectedIds[1], selectedIds[0]);
            if (t != null) {
                if (existingTransaction == null) listener.onSave(t, null);
                else listener.onSave(t, existingTransaction.getId());
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Vui lòng nhập số tiền và chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private static Transaction buildTransaction(EditText etAmt, EditText etNote, int catId, int walletId) {
        try {
            String amtStr = etAmt.getText().toString().trim();
            if (amtStr.isEmpty()) return null;

            double amt = Double.parseDouble(amtStr);
            if (catId == -1 || walletId == -1) return null;

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            return new Transaction(amt, catId, etNote.getText().toString().trim(), date, walletId);
        } catch (Exception e) {
            return null;
        }
    }
}