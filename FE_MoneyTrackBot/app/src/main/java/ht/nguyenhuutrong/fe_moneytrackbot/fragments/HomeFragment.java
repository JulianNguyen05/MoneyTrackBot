package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Cần thư viện lifecycle

import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.AddCategoryDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.WalletDialogs;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.utils.DateRangeHelper;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.HomeViewModel; // Import ViewModel

public class HomeFragment extends Fragment {

    private LinearLayout layoutWalletContainer;
    private LinearLayout layoutCategoryContainer;
    private TextView btnFilterExpense, btnFilterIncome;
    private MaterialCardView selectedCard = null;
    private MaterialCardView cardDateRangePicker;
    private TextView tvSelectedDate;

    // 🔥 Thay thế Repository bằng ViewModel
    private HomeViewModel viewModel;

    private String currentType = "expense";
    private List<Category> allCategories = new ArrayList<>(); // Vẫn giữ tạm để filter hiển thị

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🔥 Khởi tạo ViewModel (Nó sẽ tự giữ kết nối với Repository)
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        initViews(view);
        setupEvents();

        // 🔥 LẮNG NGHE DỮ LIỆU TỪ VIEWMODEL (Quan trọng nhất)
        setupObservers();

        // Yêu cầu ViewModel tải dữ liệu lần đầu
        viewModel.loadWallets();
        viewModel.loadCategories();

        return view;
    }

    // --- HÀM MỚI: LẮNG NGHE SỰ THAY ĐỔI DỮ LIỆU ---
    private void setupObservers() {
        // 1. Khi danh sách Ví thay đổi -> Tự động vẽ lại
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> {
            layoutWalletContainer.removeAllViews();
            for (Wallet wallet : wallets) addWalletView(wallet);
            addAddWalletButton();
        });

        // 2. Khi danh sách Danh mục thay đổi
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            allCategories.clear();
            allCategories.addAll(categories);
            renderCategories(); // Vẽ lại theo filter hiện tại
        });

        // 3. Khi có lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        layoutWalletContainer = view.findViewById(R.id.layoutWalletContainer);
        layoutCategoryContainer = view.findViewById(R.id.layoutCategoryContainer);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        btnFilterExpense = view.findViewById(R.id.btn_filter_expense);
        btnFilterIncome = view.findViewById(R.id.btn_filter_income);
    }

    private void setupEvents() {
        cardDateRangePicker.setOnClickListener(v -> {
            DateRangeHelper.show(getParentFragmentManager(), (displayText, startDate, endDate) -> {
                tvSelectedDate.setText(displayText);
            });
        });

        btnFilterExpense.setOnClickListener(v -> changeFilter("expense"));
        btnFilterIncome.setOnClickListener(v -> changeFilter("income"));
    }

    // --- LOGIC DANH MỤC ---
    private void changeFilter(String type) {
        currentType = type;
        updateFilterUI();
        renderCategories();
    }

    private void updateFilterUI() {
        if (getContext() == null) return;
        boolean isExpense = "expense".equals(currentType);
        btnFilterExpense.setBackgroundResource(isExpense ? R.drawable.bg_button_gradient_teal : R.drawable.bg_gray_rounded);
        btnFilterExpense.setTextColor(isExpense ? Color.WHITE : Color.BLACK);
        btnFilterIncome.setBackgroundResource(!isExpense ? R.drawable.bg_button_gradient_teal : R.drawable.bg_gray_rounded);
        btnFilterIncome.setTextColor(!isExpense ? Color.WHITE : Color.BLACK);
    }

    private void renderCategories() {
        if (getContext() == null) return;
        layoutCategoryContainer.removeAllViews();
        for (Category category : allCategories) {
            if (category.getType() != null && category.getType().equals(currentType)) {
                addCategoryView(category);
            }
        }
        addAddCategoryButton();
    }

    private void addCategoryView(Category category) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_category, layoutCategoryContainer, false);
        ((TextView) itemView.findViewById(R.id.tv_category_name)).setText(category.getName());
        layoutCategoryContainer.addView(itemView);
    }

    private void addAddCategoryButton() {
        View itemAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_add_category, layoutCategoryContainer, false);
        View btnAdd = itemAdd.findViewById(R.id.card_add_wallet);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v ->
                    AddCategoryDialog.show(getContext(), currentType, (name, type) -> {
                        // 🔥 Gọi ViewModel thay vì Repository
                        viewModel.createCategory(name, type);
                    })
            );
        }
        layoutCategoryContainer.addView(itemAdd);
    }

    // --- LOGIC VÍ ---
    private void addWalletView(Wallet wallet) {
        if (getContext() == null) return;
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet, layoutWalletContainer, false);
        TextView tvName = itemView.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = itemView.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = itemView.findViewById(R.id.card_wallet);

        tvName.setText(wallet.getName());
        tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(wallet.getBalance()));

        card.setOnClickListener(v -> {
            selectWallet(card);
            WalletDialogs.showUpdateDelete(getContext(), wallet, new WalletDialogs.OnWalletActionListener() {
                @Override public void onCreate(String n, double b) {}
                @Override public void onUpdate(Wallet w) {
                    // 🔥 Gọi ViewModel
                    viewModel.updateWallet(w);
                }
                @Override public void onDelete(int id) {
                    // 🔥 Gọi ViewModel
                    viewModel.deleteWallet(id);
                }
            });
        });
        layoutWalletContainer.addView(itemView);
    }

    private void addAddWalletButton() {
        View itemAdd = LayoutInflater.from(getContext()).inflate(R.layout.item_add_wallet, layoutWalletContainer, false);
        itemAdd.findViewById(R.id.card_add_wallet).setOnClickListener(v ->
                WalletDialogs.showAddWallet(getContext(), new WalletDialogs.OnWalletActionListener() {
                    @Override public void onCreate(String name, double balance) {
                        // 🔥 Gọi ViewModel
                        viewModel.createWallet(name, balance);
                    }
                    @Override public void onUpdate(Wallet w) {}
                    @Override public void onDelete(int id) {}
                })
        );
        layoutWalletContainer.addView(itemAdd);
    }

    private void selectWallet(MaterialCardView card) {
        if (selectedCard != null) selectedCard.setStrokeWidth(0);
        card.setStrokeColor(getResources().getColor(android.R.color.holo_blue_light));
        card.setStrokeWidth(6);
        selectedCard = card;
    }
}