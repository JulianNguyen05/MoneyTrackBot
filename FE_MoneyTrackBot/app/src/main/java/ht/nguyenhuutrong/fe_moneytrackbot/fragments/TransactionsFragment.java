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

    // Biến lưu trữ danh sách lấy từ Server
    private List<Wallet> serverWallets = new ArrayList<>();
    private List<Category> serverCategories = new ArrayList<>();

    // Biến lưu ID đang chọn (Mặc định -1 là chưa chọn)
    private int selectedWalletId = -1;
    private int selectedCategoryId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        rcv = view.findViewById(R.id.rcvTransactions);
        btnAddTransaction = view.findViewById(R.id.btnAddTransaction);
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TransactionsAdapter(transactionList);
        rcv.setAdapter(adapter);

        // Gọi API tải dữ liệu ngay khi vào màn hình
        loadTransactions();
        loadWalletsFromServer();
        loadCategoriesFromServer();

        btnAddTransaction.setOnClickListener(v -> showAddTransactionDialog());

        return view;
    }

    // --- CÁC HÀM GỌI API ---

    private void loadWalletsFromServer() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serverWallets = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) { Log.e("API", "Lỗi lấy ví"); }
        });
    }

    private void loadCategoriesFromServer() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serverCategories = response.body();
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) { Log.e("API", "Lỗi lấy danh mục"); }
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

    // --- HIỂN THỊ DIALOG ---

    private void showAddTransactionDialog() {
        if (getContext() == null) return;

        // Kiểm tra dữ liệu đã tải xong chưa
        if (serverWallets.isEmpty() || serverCategories.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải dữ liệu Ví & Danh mục...", Toast.LENGTH_SHORT).show();
            loadWalletsFromServer();
            loadCategoriesFromServer();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etNote = dialogView.findViewById(R.id.et_note);

        // Ánh xạ các View quan trọng
        RadioGroup rgType = dialogView.findViewById(R.id.rg_type);
        AutoCompleteTextView autoCategory = dialogView.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = dialogView.findViewById(R.id.auto_complete_wallet);

        // --- 1. Xử lý Logic lọc Danh mục (Chi tiêu / Thu nhập) ---
        List<Category> expenseList = new ArrayList<>();
        List<Category> incomeList = new ArrayList<>();

        // Tách danh sách gốc thành 2 list riêng
        for (Category c : serverCategories) {
            if ("income".equals(c.getType())) {
                incomeList.add(c);
            } else {
                expenseList.add(c); // Còn lại là expense
            }
        }

        // Hàm cập nhật Dropdown khi bấm RadioButton
        final Runnable updateCategoryDropdown = () -> {
            List<Category> filteredList;
            // Kiểm tra nút nào đang được chọn
            if (rgType.getCheckedRadioButtonId() == R.id.rb_income) {
                filteredList = incomeList;
            } else {
                filteredList = expenseList;
            }

            // Đổ dữ liệu mới vào Adapter
            ArrayAdapter<Category> adapterCat = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, filteredList);
            autoCategory.setAdapter(adapterCat);

            // Reset lựa chọn cũ (để tránh hiển thị sai)
            autoCategory.setText("", false);
            selectedCategoryId = -1;

            // Nếu có dữ liệu, tự chọn cái đầu tiên cho tiện
            if (!filteredList.isEmpty()) {
                autoCategory.setText(filteredList.get(0).getName(), false);
                selectedCategoryId = filteredList.get(0).getId();
            } else {
                autoCategory.setHint("Chưa có danh mục loại này");
            }
        };

        // Bắt sự kiện khi người dùng chuyển đổi Thu / Chi
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            updateCategoryDropdown.run();
        });

        // Chạy lần đầu tiên (Mặc định là Chi tiêu)
        updateCategoryDropdown.run();

        // Bắt sự kiện khi chọn 1 dòng trong Dropdown Danh mục
        autoCategory.setOnItemClickListener((parent, view, position, id) -> {
            Category selectedCat = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCat.getId();
        });


        // --- 2. Cấu hình Dropdown VÍ ---
        ArrayAdapter<Wallet> adapterWallet = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, serverWallets);
        autoWallet.setAdapter(adapterWallet);

        // Mặc định chọn ví đầu tiên
        if (!serverWallets.isEmpty()) {
            autoWallet.setText(serverWallets.get(0).getName(), false);
            selectedWalletId = serverWallets.get(0).getId();
        }

        // Bắt sự kiện chọn Ví
        autoWallet.setOnItemClickListener((parent, view, position, id) -> {
            Wallet selectedWallet = (Wallet) parent.getItemAtPosition(position);
            selectedWalletId = selectedWallet.getId();
        });


        // --- 3. Tạo Dialog ---
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(getContext(), "Nhập số tiền!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selectedCategoryId == -1) {
                        Toast.makeText(getContext(), "Vui lòng chọn danh mục!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // Gọi API tạo mới
                        createTransactionOnServer(amount, selectedCategoryId, note, today, selectedWalletId);

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền lỗi", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void createTransactionOnServer(double amount, int categoryId, String note, String date, int walletId) {
        // Model Transaction mới nhận int cho categoryId
        Transaction newTrans = new Transaction(amount, categoryId, note, date, walletId);

        RetrofitClient.getApiService(getContext()).createTransaction(newTrans).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    loadTransactions(); // Load lại list giao dịch
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("API_ERROR", errorBody);
                        Toast.makeText(getContext(), "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {}
                }
            }
            @Override
            public void onFailure(Call<Transaction> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}