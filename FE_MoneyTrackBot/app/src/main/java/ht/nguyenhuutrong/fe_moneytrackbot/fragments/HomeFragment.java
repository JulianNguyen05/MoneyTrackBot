package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
import android.content.Context;
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

    // Đã xóa biến pieChart

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ View
        layoutWalletContainer = view.findViewById(R.id.layoutWalletContainer);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        // Setup chức năng chọn ngày
        setupDateRangePicker();

        loadWalletsFromApi();

        return view;
    }

    private void loadWalletsFromApi() {
        if (getContext() == null) return;

        RetrofitClient.getApiService(getContext()).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Wallet> wallets = response.body();

                    // 1. Xóa sạch các ví cũ/dummy trên giao diện để tránh bị trùng
                    layoutWalletContainer.removeAllViews();

                    // 2. Duyệt qua danh sách server trả về và vẽ lên màn hình
                    for (Wallet wallet : wallets) {
                        String formattedBalance = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                                .format(wallet.getBalance());

                        // Add ví vào (false nghĩa là không auto select, hoặc tùy bạn)
                        addWallet(wallet.getName(), formattedBalance, false);
                    }

                    // 3. Cuối cùng mới thêm nút "Thêm ví" vào dưới cùng
                    addAddWalletButton();

                } else {
                    Toast.makeText(getContext(), "Không tải được ví: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Dù lỗi cũng nên hiện nút Add Wallet để người dùng còn thao tác
                layoutWalletContainer.removeAllViews();
                addAddWalletButton();
            }
        });
    }

    // --- LOGIC MỚI: Nút thêm ví ---
    private void addAddWalletButton() {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemAdd = inflater.inflate(R.layout.item_add_wallet, layoutWalletContainer, false);

        MaterialCardView cardAdd = itemAdd.findViewById(R.id.card_add_wallet);

        // Gọi hàm mở Dialog khi bấm nút
        cardAdd.setOnClickListener(v -> showAddWalletDialog());

        layoutWalletContainer.addView(itemAdd);
    }

    // --- LOGIC MỚI: Hiển thị Dialog và Gọi API ---
    private void showAddWalletDialog() {
        if (getContext() == null) return;

        // Inflate layout dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm ví mới") // Thêm title cho rõ ràng
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);

                    // Gọi API tạo ví
                    createNewWalletOnServer(name, balance);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createNewWalletOnServer(String name, double balance) {
        Wallet newWallet = new Wallet(name, balance);

        RetrofitClient.getApiService(getContext())
                .createWallet(newWallet)
                .enqueue(new Callback<Wallet>() {
                    @Override
                    public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Thêm ví thành công!", Toast.LENGTH_SHORT).show();

                            // ✅ Gọi lại hàm tải danh sách để làm mới giao diện
                            loadWalletsFromApi();
                        } else {
                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Wallet> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addWallet(String name, String amount, boolean select) {
        if (getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_wallet, layoutWalletContainer, false);

        TextView tvName = itemView.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = itemView.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = itemView.findViewById(R.id.card_wallet);

        tvName.setText(name);
        tvAmount.setText(amount);
        card.setOnClickListener(v -> selectWallet(card));

        // Vì ta đã xóa hết view và add lại theo thứ tự, cứ add thẳng vào là được
        layoutWalletContainer.addView(itemView);

        if (select) {
            selectWallet(card);
        }
    }

    private void selectWallet(MaterialCardView card) {
        if (selectedCard != null) {
            selectedCard.setStrokeWidth(0);
        }
        card.setStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
        card.setStrokeWidth(6);
        selectedCard = card;
    }

    private void setupDateRangePicker() {
        if (cardDateRangePicker != null) {
            cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();

        builder.setTitleText("Chọn phạm vi thời gian");
        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;
            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
            String startString = sdf.format(new Date(startDate));
            String endString = sdf.format(new Date(endDate));

            if (tvSelectedDate != null) {
                tvSelectedDate.setText(startString + "  -  " + endString);
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}