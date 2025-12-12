package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private LinearLayout layoutWalletContainer;
    private MaterialCardView selectedCard = null;
    private MaterialCardView cardDateRangePicker;
    private TextView tvSelectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        layoutWalletContainer = view.findViewById(R.id.layoutWalletContainer);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        setupDateRangePicker();
        loadWalletsFromApi();

        return view;
    }

    // --- 1. TẢI DANH SÁCH VÍ ---
    private void loadWalletsFromApi() {
        if (getContext() == null) return;

        RetrofitClient.getApiService(getContext()).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Wallet> wallets = response.body();
                    layoutWalletContainer.removeAllViews(); // Xóa cũ

                    for (Wallet wallet : wallets) {
                        // Gọi hàm addWallet phiên bản mới nhận Object
                        addWalletView(wallet);
                    }
                    addAddWalletButton(); // Thêm nút Add vào cuối
                } else {
                    Toast.makeText(getContext(), "Lỗi tải ví: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                layoutWalletContainer.removeAllViews();
                addAddWalletButton();
            }
        });
    }

    // --- 2. THÊM VÍ MỚI (CREATE) ---
    private void addAddWalletButton() {
        if (getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemAdd = inflater.inflate(R.layout.item_add_wallet, layoutWalletContainer, false);

        // Bấm nút dấu cộng -> Mở Dialog Thêm
        itemAdd.findViewById(R.id.card_add_wallet).setOnClickListener(v -> showCreateDialog());

        layoutWalletContainer.addView(itemAdd);
    }

    private void showCreateDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm ví mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();
                    if (name.isEmpty()) return;
                    double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);

                    createWalletApi(name, balance);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createWalletApi(String name, double balance) {
        Wallet newWallet = new Wallet(name, balance);
        RetrofitClient.getApiService(getContext()).createWallet(newWallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm ví!", Toast.LENGTH_SHORT).show();
                    loadWalletsFromApi(); // Load lại
                }
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    // --- 3. SỬA VÀ XÓA (UPDATE & DELETE) ---
    // Hàm này được gọi khi bấm nhẹ vào ví
    private void showUpdateDeleteDialog(Wallet wallet) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);

        // Đổ dữ liệu cũ vào
        etName.setText(wallet.getName());
        etBalance.setText(String.valueOf((long)wallet.getBalance()));

        new AlertDialog.Builder(getContext())
                .setTitle("Chi tiết ví")
                .setView(dialogView)
                // Nút bên Phải: Lưu
                .setPositiveButton("Lưu thay đổi", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();
                    if (!name.isEmpty()) {
                        double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);
                        wallet.setName(name);
                        wallet.setBalance(balance);
                        updateWalletApi(wallet);
                    }
                })
                // Nút bên Trái: Xóa
                .setNeutralButton("Xóa ví này", (dialog, which) -> {
                    // Hỏi lại cho chắc
                    new AlertDialog.Builder(getContext())
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa ví '" + wallet.getName() + "' không?")
                            .setPositiveButton("Xóa luôn", (d, w) -> deleteWalletApi(wallet.getId()))
                            .setNegativeButton("Hủy", null)
                            .show();
                })
                // Nút ở Giữa: Đóng
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateWalletApi(Wallet wallet) {
        RetrofitClient.getApiService(getContext()).updateWallet(wallet.getId(), wallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                    loadWalletsFromApi();
                } else {
                    Toast.makeText(getContext(), "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    private void deleteWalletApi(int id) {
        RetrofitClient.getApiService(getContext()).deleteWallet(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa ví!", Toast.LENGTH_SHORT).show();
                    loadWalletsFromApi();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // --- 4. VẼ GIAO DIỆN VÍ ---
    private void addWalletView(Wallet wallet) {
        if (getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_wallet, layoutWalletContainer, false);

        TextView tvName = itemView.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = itemView.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = itemView.findViewById(R.id.card_wallet);

        String formattedBalance = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                .format(wallet.getBalance());
        tvName.setText(wallet.getName());
        tvAmount.setText(formattedBalance);

        // 🔥 LOGIC QUAN TRỌNG: Bấm vào là Sửa/Xóa luôn
        card.setOnClickListener(v -> {
            // 1. Vẫn đổi màu viền cho đẹp (hiệu ứng chọn)
            selectWallet(card);

            // 2. Mở dialog Sửa/Xóa ngay lập tức
            showUpdateDeleteDialog(wallet);
        });

        layoutWalletContainer.addView(itemView);
    }

    private void selectWallet(MaterialCardView card) {
        if (selectedCard != null) selectedCard.setStrokeWidth(0);
        card.setStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
        card.setStrokeWidth(6);
        selectedCard = card;
    }

    // --- DATE PICKER (GIỮ NGUYÊN) ---
    private void setupDateRangePicker() {
        if (cardDateRangePicker != null) {
            cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Chọn phạm vi thời gian");
        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
            if (tvSelectedDate != null) {
                tvSelectedDate.setText(sdf.format(new Date(selection.first)) + "  -  " + sdf.format(new Date(selection.second)));
            }
        });
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}