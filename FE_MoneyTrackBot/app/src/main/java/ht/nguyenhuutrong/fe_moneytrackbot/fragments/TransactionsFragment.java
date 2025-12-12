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
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category; // Import mới
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;    // Import mới
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {
    RecyclerView rcv;
    TransactionsAdapter adapter;
    List<Transaction> transactionList = new ArrayList<>();
    MaterialCardView btnAddTransaction;

    // 🔥 1. Biến lưu trữ danh sách lấy từ Server
    private List<Wallet> serverWallets = new ArrayList<>();
    private List<Category> serverCategories = new ArrayList<>();

    // Biến lưu ID đang chọn
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

        // 🔥 2. Gọi API để tải dữ liệu cần thiết ngay khi vào màn hình
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
                    serverWallets = response.body(); // Lưu lại để dùng cho Dialog
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
                    serverCategories = response.body(); // Lưu lại để dùng cho Dialog
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

        // Kiểm tra xem dữ liệu đã tải xong chưa
        if (serverWallets.isEmpty() || serverCategories.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải dữ liệu Ví & Danh mục, vui lòng thử lại sau giây lát!", Toast.LENGTH_SHORT).show();
            // Gọi tải lại phòng trường hợp mạng lag
            loadWalletsFromServer();
            loadCategoriesFromServer();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etNote = dialogView.findViewById(R.id.et_note);
        AutoCompleteTextView autoCategory = dialogView.findViewById(R.id.auto_complete_category);
        AutoCompleteTextView autoWallet = dialogView.findViewById(R.id.auto_complete_wallet);

        // 🔥 3. Đổ dữ liệu thật vào Dropdown CATEGORY
        // ArrayAdapter mặc định dùng phương thức toString() của object để hiển thị tên
        ArrayAdapter<Category> adapterCat = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, serverCategories);
        autoCategory.setAdapter(adapterCat);

        // Mặc định chọn cái đầu tiên
        autoCategory.setText(serverCategories.get(0).getName(), false);
        selectedCategoryId = serverCategories.get(0).getId();

        // Bắt sự kiện chọn
        autoCategory.setOnItemClickListener((parent, view, position, id) -> {
            // Lấy object Category tại vị trí click -> Lấy ID thật
            Category selectedCat = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCat.getId();
        });

        // 🔥 4. Đổ dữ liệu thật vào Dropdown WALLET
        ArrayAdapter<Wallet> adapterWallet = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, serverWallets);
        autoWallet.setAdapter(adapterWallet);

        autoWallet.setText(serverWallets.get(0).getName(), false);
        selectedWalletId = serverWallets.get(0).getId();

        autoWallet.setOnItemClickListener((parent, view, position, id) -> {
            Wallet selectedWallet = (Wallet) parent.getItemAtPosition(position);
            selectedWalletId = selectedWallet.getId();
        });

        // Tạo Dialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton("Lưu", (d, w) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(getContext(), "Nhập số tiền!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        // Gọi API với ID thật đã chọn
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
        // Lưu ý: categoryId ở đây đang là int, nếu Model Transaction của bạn biến category là String
        // thì hãy đổi thành String.valueOf(categoryId)
        Transaction newTrans = new Transaction(amount, String.valueOf(categoryId), note, date, walletId);

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