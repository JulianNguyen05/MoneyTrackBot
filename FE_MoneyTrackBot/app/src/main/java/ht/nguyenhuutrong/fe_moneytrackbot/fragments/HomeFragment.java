package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private LinearLayout layoutWalletContainer;
    private LinearLayout layoutCategoryContainer;

    private MaterialCardView selectedCard = null;
    private MaterialCardView cardDateRangePicker;
    private TextView tvSelectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ View
        layoutWalletContainer = view.findViewById(R.id.layoutWalletContainer);
        layoutCategoryContainer = view.findViewById(R.id.layoutCategoryContainer);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        setupDateRangePicker();

        // Gọi API tải dữ liệu
        loadWalletsFromApi();
        loadCategoriesFromApi();

        return view;
    }

    // ================== PHẦN VÍ (WALLET) ==================
    private void loadWalletsFromApi() {
        if (getContext() == null) return;
        RetrofitClient.getApiService(getContext()).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    layoutWalletContainer.removeAllViews();
                    for (Wallet wallet : response.body()) {
                        addWalletView(wallet);
                    }
                    addAddWalletButton();
                }
            }

            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                addAddWalletButton();
            }
        });
    }

    private void addWalletView(Wallet wallet) {
        if (getContext() == null) return;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View itemView = inflater.inflate(R.layout.item_wallet, layoutWalletContainer, false);

        TextView tvName = itemView.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = itemView.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = itemView.findViewById(R.id.card_wallet);

        String formattedBalance = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(wallet.getBalance());
        tvName.setText(wallet.getName());
        tvAmount.setText(formattedBalance);

        card.setOnClickListener(v -> {
            selectWallet(card);
            showUpdateDeleteDialog(wallet);
        });

        layoutWalletContainer.addView(itemView);
    }

    private void addAddWalletButton() {
        if (getContext() == null) return;
        View itemAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_add_wallet, layoutWalletContainer, false);
        itemAdd.findViewById(R.id.card_add_wallet).setOnClickListener(v -> showCreateWalletDialog());
        layoutWalletContainer.addView(itemAdd);
    }

    // ================== PHẦN DANH MỤC (CATEGORY) ==================

    private void loadCategoriesFromApi() {
        if (getContext() == null) return;

        RetrofitClient.getApiService(getContext()).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    layoutCategoryContainer.removeAllViews();
                    for (Category category : response.body()) {
                        addCategoryView(category);
                    }
                    addAddCategoryButton();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                addAddCategoryButton();
            }
        });
    }

    private void addCategoryView(Category category) {
        if (getContext() == null) return;
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, layoutCategoryContainer, false);
        TextView tvName = itemView.findViewById(R.id.tv_category_name);
        tvName.setText(category.getName());
        layoutCategoryContainer.addView(itemView);
    }

    private void addAddCategoryButton() {
        if (getContext() == null) return;
        View itemAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_add_category, layoutCategoryContainer, false);
        itemAdd.findViewById(R.id.card_add_wallet).setOnClickListener(v -> showCreateCategoryDialog());
        layoutCategoryContainer.addView(itemAdd);
    }

    // 🔥 ĐÃ SỬA: Thêm chọn loại Thu/Chi để tránh lỗi 400
    private void showCreateCategoryDialog() {
        if (getContext() == null) return;

        // Tạo layout chứa EditText và RadioGroup
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        // 1. Ô nhập tên
        final EditText etName = new EditText(getContext());
        etName.setHint("Tên danh mục (vd: Xăng xe)");
        layout.addView(etName);

        // 2. Tiêu đề chọn loại
        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Loại danh mục:");
        tvLabel.setPadding(0, 30, 0, 10);
        layout.addView(tvLabel);

        // 3. Chọn loại (Chi tiêu / Thu nhập)
        final RadioGroup rgType = new RadioGroup(getContext());
        rgType.setOrientation(LinearLayout.HORIZONTAL);

        RadioButton rbExpense = new RadioButton(getContext());
        rbExpense.setText("Chi tiêu");
        rbExpense.setChecked(true); // Mặc định là chi tiêu
        rgType.addView(rbExpense);

        RadioButton rbIncome = new RadioButton(getContext());
        rbIncome.setText("Thu nhập");
        rgType.addView(rbIncome);

        layout.addView(rgType);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm Danh Mục Mới")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Lấy loại được chọn
                    String type = rbExpense.isChecked() ? "expense" : "income";

                    createCategoryApi(name, type);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 🔥 ĐÃ SỬA: Nhận thêm tham số type
    private void createCategoryApi(String name, String type) {
        Category newCat = new Category(name, type);
        RetrofitClient.getApiService(getContext()).createCategory(newCat).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm danh mục!", Toast.LENGTH_SHORT).show();
                    loadCategoriesFromApi();
                } else {
                    // Log lỗi nếu server từ chối
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Lỗi server: " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi tạo danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== CÁC HÀM TIỆN ÍCH KHÁC (VÍ, DATE PICKER) ==================

    private void showCreateWalletDialog() {
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
                    loadWalletsFromApi();
                }
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    private void showUpdateDeleteDialog(Wallet wallet) {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);
        etName.setText(wallet.getName());
        etBalance.setText(String.valueOf((long) wallet.getBalance()));

        new AlertDialog.Builder(getContext())
                .setTitle("Chi tiết ví")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();
                    if (!name.isEmpty()) {
                        wallet.setName(name);
                        wallet.setBalance(Double.parseDouble(balanceStr));
                        updateWalletApi(wallet);
                    }
                })
                .setNeutralButton("Xóa", (dialog, which) -> deleteWalletApi(wallet.getId()))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void updateWalletApi(Wallet wallet) {
        RetrofitClient.getApiService(getContext()).updateWallet(wallet.getId(), wallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) loadWalletsFromApi();
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    private void deleteWalletApi(int id) {
        RetrofitClient.getApiService(getContext()).deleteWallet(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) loadWalletsFromApi();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void selectWallet(MaterialCardView card) {
        if (selectedCard != null) selectedCard.setStrokeWidth(0);
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