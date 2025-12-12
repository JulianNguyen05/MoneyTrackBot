package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
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
import java.util.ArrayList;
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

    // 🔥 BIẾN MỚI CHO BỘ LỌC
    private TextView btnFilterExpense, btnFilterIncome;
    private List<Category> allCategories = new ArrayList<>(); // Lưu tất cả danh mục
    private String currentType = "expense"; // Mặc định là chi phí

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

        // 🔥 Ánh xạ 2 nút lọc
        btnFilterExpense = view.findViewById(R.id.btn_filter_expense);
        btnFilterIncome = view.findViewById(R.id.btn_filter_income);

        setupDateRangePicker();
        setupCategoryFilterEvents(); // Cài đặt sự kiện bấm nút lọc

        loadWalletsFromApi();
        loadCategoriesFromApi();

        return view;
    }

    // --- LOGIC BỘ LỌC DANH MỤC ---
    private void setupCategoryFilterEvents() {
        btnFilterExpense.setOnClickListener(v -> {
            currentType = "expense";
            updateFilterUI();
            renderCategories(); // Vẽ lại
        });

        btnFilterIncome.setOnClickListener(v -> {
            currentType = "income";
            updateFilterUI();
            renderCategories(); // Vẽ lại
        });
    }

    private void updateFilterUI() {
        if (getContext() == null) return;

        if (currentType.equals("expense")) {
            // Nút Chi phí sáng, nút Thu nhập tối
            btnFilterExpense.setBackgroundResource(R.drawable.bg_button_gradient_teal);
            btnFilterExpense.setTextColor(Color.WHITE);

            btnFilterIncome.setBackgroundResource(R.drawable.bg_gray_rounded); // Tạo file này trong drawable nếu chưa có
            btnFilterIncome.setTextColor(Color.BLACK);
        } else {
            // Nút Thu nhập sáng, nút Chi phí tối
            btnFilterIncome.setBackgroundResource(R.drawable.bg_button_gradient_teal);
            btnFilterIncome.setTextColor(Color.WHITE);

            btnFilterExpense.setBackgroundResource(R.drawable.bg_gray_rounded);
            btnFilterExpense.setTextColor(Color.BLACK);
        }
    }

    // ================== API DANH MỤC (SỬA LẠI) ==================

    private void loadCategoriesFromApi() {
        if (getContext() == null) return;

        RetrofitClient.getApiService(getContext()).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Lưu vào list gốc
                    allCategories.clear();
                    allCategories.addAll(response.body());

                    // 2. Lọc và hiển thị theo loại đang chọn
                    renderCategories();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                // Lỗi mạng thì vẫn render (có thể list rỗng) để hiện nút Add
                renderCategories();
            }
        });
    }

    // Hàm này chỉ có nhiệm vụ vẽ lại giao diện dựa trên list gốc và loại đang chọn
    private void renderCategories() {
        if (getContext() == null) return;

        layoutCategoryContainer.removeAllViews(); // Xóa sạch cũ

        for (Category category : allCategories) {
            // Kiểm tra: Nếu loại của category trùng với loại đang chọn thì mới hiện
            if (category.getType() != null && category.getType().equals(currentType)) {
                addCategoryView(category);
            }
        }

        // Luôn hiện nút thêm ở cuối
        addAddCategoryButton();
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
        View itemAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_add_category, layoutCategoryContainer, false); // Đảm bảo bạn có file item_add_category hoặc dùng chung item_add_wallet

        // Lưu ý: Nếu dùng chung item_add_wallet thì ID là card_add_wallet
        View btnAdd = itemAdd.findViewById(R.id.card_add_wallet);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showCreateCategoryDialog());
        }

        layoutCategoryContainer.addView(itemAdd);
    }

    // ================== DIALOG TẠO DANH MỤC (CẬP NHẬT TỰ CHỌN LOẠI) ==================
    private void showCreateCategoryDialog() {
        if (getContext() == null) return;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText etName = new EditText(getContext());
        etName.setHint("Tên danh mục (vd: Ăn sáng)");
        layout.addView(etName);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText("Loại danh mục:");
        tvLabel.setPadding(0, 30, 0, 10);
        layout.addView(tvLabel);

        final RadioGroup rgType = new RadioGroup(getContext());
        rgType.setOrientation(LinearLayout.HORIZONTAL);

        RadioButton rbExpense = new RadioButton(getContext());
        rbExpense.setId(View.generateViewId());
        rbExpense.setText("Chi phí");

        RadioButton rbIncome = new RadioButton(getContext());
        rbIncome.setId(View.generateViewId());
        rbIncome.setText("Thu nhập");

        rgType.addView(rbExpense);
        rgType.addView(rbIncome);
        layout.addView(rgType);

        // 🔥 TỰ ĐỘNG CHỌN LOẠI DỰA TRÊN TAB ĐANG XEM
        if (currentType.equals("income")) {
            rbIncome.setChecked(true);
        } else {
            rbExpense.setChecked(true);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm Danh Mục Mới")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;

                    // Lấy loại từ RadioButton
                    String type = rbIncome.isChecked() ? "income" : "expense";
                    createCategoryApi(name, type);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createCategoryApi(String name, String type) {
        Category newCat = new Category(name, type);
        RetrofitClient.getApiService(getContext()).createCategory(newCat).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã thêm!", Toast.LENGTH_SHORT).show();
                    loadCategoriesFromApi(); // Load lại
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================== PHẦN VÍ (GIỮ NGUYÊN) ==================
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

    // ... CÁC HÀM XỬ LÝ VÍ (createWalletApi, showUpdateDeleteDialog, v.v.) GIỮ NGUYÊN ...

    // Giữ nguyên các hàm helper cho Ví để file không quá dài
    // showCreateWalletDialog, createWalletApi, showUpdateDeleteDialog, updateWalletApi, deleteWalletApi, selectWallet

    private void showCreateWalletDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_wallet, null);
        EditText etName = dialogView.findViewById(R.id.et_wallet_name);
        EditText etBalance = dialogView.findViewById(R.id.et_wallet_balance);
        new AlertDialog.Builder(getContext()).setTitle("Thêm ví mới").setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balanceStr = etBalance.getText().toString().trim();
                    if (!name.isEmpty()) createWalletApi(name, balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr));
                }).setNegativeButton("Hủy", null).show();
    }

    private void createWalletApi(String name, double balance) {
        Wallet newWallet = new Wallet(name, balance);
        RetrofitClient.getApiService(getContext()).createWallet(newWallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) { if(response.isSuccessful()) loadWalletsFromApi(); }
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
        etBalance.setText(String.valueOf((long)wallet.getBalance()));
        new AlertDialog.Builder(getContext()).setTitle("Chi tiết ví").setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    wallet.setName(etName.getText().toString());
                    wallet.setBalance(Double.parseDouble(etBalance.getText().toString()));
                    updateWalletApi(wallet);
                })
                .setNeutralButton("Xóa", (d, w) -> deleteWalletApi(wallet.getId()))
                .setNegativeButton("Hủy", null).show();
    }

    private void updateWalletApi(Wallet wallet) {
        RetrofitClient.getApiService(getContext()).updateWallet(wallet.getId(), wallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) { if(response.isSuccessful()) loadWalletsFromApi(); }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    private void deleteWalletApi(int id) {
        RetrofitClient.getApiService(getContext()).deleteWallet(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { if(response.isSuccessful()) loadWalletsFromApi(); }
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