package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.CategoryDialog; // 🔥 Import mới
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;      // 🔥 Import mới
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.HomeUIManager;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.WalletRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private HomeUIManager uiManager;

    private TextView tvNetChange;
    private TextView tvExpenseValue;
    private TextView tvIncomeValue;
    private TextView tvSelectedDate;
    private View cardDateRangePicker;

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        uiManager = new HomeUIManager(getContext(), view, getParentFragmentManager());

        initViews(view);
        setupBindings();

        viewModel.loadWallets();
        viewModel.loadCategories();
        loadCurrentMonthData(); // Load dữ liệu mặc định

        return view;
    }

    private void initViews(View view) {
        tvNetChange = view.findViewById(R.id.tv_net_change);
        tvExpenseValue = view.findViewById(R.id.tv_expense_value);
        tvIncomeValue = view.findViewById(R.id.tv_income_value);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);

        // Click vào ngày -> Gọi hàm showDateRangePicker của Fragment
        cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());
    }

    private void setupBindings() {
        // 1. Bind WALLET (Thêm, Sửa, Xóa)
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets ->
                uiManager.updateWallets(wallets, new WalletRenderer.WalletActionListener() {
                    @Override
                    public void onCreate(String name, double balance) {
                        viewModel.createWallet(name, balance);
                    }

                    @Override
                    public void onUpdate(Wallet wallet) {
                        viewModel.updateWallet(wallet);
                    }

                    @Override
                    public void onDelete(int id) {
                        viewModel.deleteWallet(id);
                    }
                })
        );

        // 2. Bind CATEGORY (🔥 CẬP NHẬT: Triển khai đủ 3 phương thức Thêm/Sửa/Xóa)
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories ->
                uiManager.updateCategories(
                        categories,
                        new CategoryDialog.OnCategoryActionListener() {
                            @Override
                            public void onCreate(String name, String type) {
                                viewModel.createCategory(name, type);
                            }

                            @Override
                            public void onUpdate(Category category) {
                                viewModel.updateCategory(category);
                            }

                            @Override
                            public void onDelete(int id) {
                                viewModel.deleteCategory(id);
                            }
                        }
                )
        );

        // 3. Bind Error Message
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Bind CashFlow Data
        if (viewModel.cashFlowData != null) {
            viewModel.cashFlowData.observe(
                    getViewLifecycleOwner(),
                    this::updateCashFlowUI
            );
        }
    }

    private void updateCashFlowUI(CashFlowResponse data) {
        if (data == null) return;

        tvIncomeValue.setText(currencyFormat.format(data.getTotalIncome()));
        tvExpenseValue.setText(currencyFormat.format(data.getTotalExpense()));

        double netChange = data.getNetChange();
        tvNetChange.setText(currencyFormat.format(netChange));

        // Logic màu sắc: Dương -> normal_weight, Âm -> obese
        int colorRes = netChange >= 0
                ? R.color.normal_weight
                : R.color.obese;

        tvNetChange.setTextColor(
                ContextCompat.getColor(requireContext(), colorRes)
        );
    }

    private void loadCurrentMonthData() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Ngày đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startApi = apiFormat.format(calendar.getTime());
        String startDisplay = displayFormat.format(calendar.getTime());

        // Ngày cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endApi = apiFormat.format(calendar.getTime());
        String endDisplay = displayFormat.format(calendar.getTime());

        tvSelectedDate.setText(String.format("%s - %s", startDisplay, endDisplay));
        viewModel.loadCashFlow(startApi, endApi);
    }

    // --- DIALOG CHỌN NGÀY ---
    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();

        builder.setTitleText("Chọn khoảng thời gian");
        builder.setTheme(R.style.CustomDatePickerTheme); // Theme màu nền F5F5F5

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Date startDate = new Date(selection.first);
                Date endDate = new Date(selection.second);

                String startApi = apiFormat.format(startDate);
                String endApi = apiFormat.format(endDate);
                String startDisplay = displayFormat.format(startDate);
                String endDisplay = displayFormat.format(endDate);

                tvSelectedDate.setText(String.format("%s - %s", startDisplay, endDisplay));
                viewModel.loadCashFlow(startApi, endApi);
            }
        });

        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}