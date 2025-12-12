package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.adapters.TransactionsAdapter;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {
    RecyclerView rcv;
    TransactionsAdapter adapter;
    List<Transaction> transactionList = new ArrayList<>();
    MaterialCardView btnAddTransaction;

    private List<Wallet> serverWallets = new ArrayList<>();
    private List<Category> serverCategories = new ArrayList<>();

    // Biến lưu ID đang chọn trong Dialog
    private int selectedWalletId = -1;
    private int selectedCategoryId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        rcv = view.findViewById(R.id.rcvTransactions);
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Cập nhật Adapter với Listener
        adapter = new TransactionsAdapter(transactionList, transaction -> {
            // Khi click vào item -> Mở dialog Sửa/Xóa
            showTransactionDialog(transaction);
        });
        rcv.setAdapter(adapter);

        loadTransactions();
        loadWalletsFromServer();
        loadCategoriesFromServer();

        // Khi bấm nút thêm -> Mở dialog Thêm mới (truyền null)
        btnAddTransaction.setOnClickListener(v -> showTransactionDialog(null));

        return view;
    }

    // --- CÁC HÀM API LOAD DỮ LIỆU ---
    private void loadWalletsFromServer() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) serverWallets = response.body();
            }
            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {}
        });
    }

    private void loadCategoriesFromServer() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) serverCategories = response.body();
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void loadTransactions() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getTransactions(null).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionList.clear();
                    transactionList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (!transactionList.isEmpty()) rcv.smoothScrollToPosition(0);
                }
            }
            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {}
        });
    }

    // --- 🔥 HÀM HIỂN THỊ DIALOG CHUNG (THÊM & SỬA) ---
    private void showTransactionDialog(@Nullable Transaction existingTransaction) {
        if (getContext() == null) return;

        // Check dữ liệu trước
        if (serverWallets.isEmpty() || serverCategories.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            loadWalletsFromServer();
            loadCategoriesFromServer();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);

        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etNote = dialogView.findViewById(R.id.et_note);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_type);
        AutoCompleteTextView autoCategory = dialogView.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = dialogView.findViewById(R.id.auto_complete_wallet);

        // 1. Phân loại danh sách Category
        List<Category> expenseList = new ArrayList<>();
        List<Category> incomeList = new ArrayList<>();
        for (Category c : serverCategories) {
            if ("income".equals(c.getType())) incomeList.add(c);
            else expenseList.add(c);
        }

        // 2. Logic cập nhật Dropdown Category
        final Runnable updateCategoryDropdown = () -> {
            List<Category> filteredList = (rgType.getCheckedRadioButtonId() == R.id.rb_income) ? incomeList : expenseList;
            ArrayAdapter<Category> adapterCat = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, filteredList);
            autoCategory.setAdapter(adapterCat);

            // Nếu đang sửa và loại trùng khớp -> giữ nguyên, ngược lại reset
            // (Đơn giản hóa: reset text nếu người dùng tự đổi loại)
            if (existingTransaction == null) {
                autoCategory.setText("", false);
                selectedCategoryId = -1;
            }
        };

        rgType.setOnCheckedChangeListener((group, checkedId) -> updateCategoryDropdown.run());
        autoCategory.setOnItemClickListener((p, v, pos, id) -> selectedCategoryId = ((Category)p.getItemAtPosition(pos)).getId());

        // 3. Setup Dropdown Wallet
        ArrayAdapter<Wallet> adapterWallet = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, serverWallets);
        autoWallet.setAdapter(adapterWallet);
        autoWallet.setOnItemClickListener((p, v, pos, id) -> selectedWalletId = ((Wallet)p.getItemAtPosition(pos)).getId());

        // 4. --- ĐIỀN DỮ LIỆU CŨ (CHẾ ĐỘ SỬA) ---
        if (existingTransaction != null) {
            etAmount.setText(String.valueOf((long)Math.abs(existingTransaction.getAmount()))); // Lấy trị tuyệt đối
            etNote.setText(existingTransaction.getNote());

            // Tìm và điền Wallet
            for (Wallet w : serverWallets) {
                if (w.getId() == existingTransaction.getWalletId()) {
                    autoWallet.setText(w.getName(), false);
                    selectedWalletId = w.getId();
                    break;
                }
            }

            // Tìm và điền Category + Loại
            for (Category c : serverCategories) {
                if (c.getId() == existingTransaction.getCategoryId()) {
                    // Set đúng RadioButton
                    if ("income".equals(c.getType())) {
                        rgType.check(R.id.rb_income);
                    } else {
                        rgType.check(R.id.rb_expense);
                    }

                    // Cập nhật adapter cho dropdown trước khi set text
                    updateCategoryDropdown.run();

                    // Set text category
                    autoCategory.setText(c.getName(), false);
                    selectedCategoryId = c.getId();
                    break;
                }
            }
        } else {
            // Chế độ THÊM: Mặc định
            updateCategoryDropdown.run(); // Chạy để init list
            // Có thể set default wallet ở đây nếu muốn
        }

        // 5. Tạo Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(dialogView);

        if (existingTransaction == null) {
            // --- NÚT CHO CHẾ ĐỘ THÊM ---
            builder.setTitle("Thêm Giao Dịch")
                    .setPositiveButton("Lưu", (d, w) -> saveTransaction(etAmount, etNote, null))
                    .setNegativeButton("Hủy", null);
        } else {
            // --- NÚT CHO CHẾ ĐỘ SỬA ---
            builder.setTitle("Chi Tiết Giao Dịch")
                    .setPositiveButton("Cập nhật", (d, w) -> saveTransaction(etAmount, etNote, existingTransaction.getId()))
                    .setNeutralButton("Xóa", (d, w) -> confirmDelete(existingTransaction.getId()))
                    .setNegativeButton("Đóng", null);
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    // --- XỬ LÝ LƯU (CHUNG CHO TẠO VÀ SỬA) ---
    private void saveTransaction(EditText etAmount, EditText etNote, Integer transactionId) {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (amountStr.isEmpty() || selectedCategoryId == -1 || selectedWalletId == -1) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (transactionId == null) {
                createTransactionOnServer(amount, selectedCategoryId, note, today, selectedWalletId);
            } else {
                updateTransactionOnServer(transactionId, amount, selectedCategoryId, note, today, selectedWalletId);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số tiền lỗi", Toast.LENGTH_SHORT).show();
        }
    }

    // --- API CALLS ---
    private void createTransactionOnServer(double amount, int catId, String note, String date, int walletId) {
        Transaction t = new Transaction(amount, catId, note, date, walletId);
        RetrofitClient.getApiService(getContext()).createTransaction(t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    loadTransactions();
                } else handleError(response);
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {}
        });
    }

    private void updateTransactionOnServer(int id, double amount, int catId, String note, String date, int walletId) {
        Transaction t = new Transaction(amount, catId, note, date, walletId);
        RetrofitClient.getApiService(getContext()).updateTransaction(id, t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    loadTransactions();
                } else handleError(response);
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {}
        });
    }

    private void confirmDelete(int id) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (d, w) -> {
                    RetrofitClient.getApiService(getContext()).deleteTransaction(id).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                                loadTransactions();
                            } else handleError(response);
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleError(Response<?> response) {
        try {
            String err = response.errorBody().string();
            Log.e("API_ERR", err);
            Toast.makeText(getContext(), "Lỗi: " + err, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {}
    }
}