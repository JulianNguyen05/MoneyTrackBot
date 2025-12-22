package ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments;

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
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.data.renderers.HomeUIManager;
import ht.nguyenhuutrong.fe_moneytrackbot.data.renderers.WalletRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.CategoryDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private HomeUIManager uiManager;

    // UI
    private TextView tvNetChange;
    private TextView tvExpenseValue;
    private TextView tvIncomeValue;
    private TextView tvSelectedDate;
    private View cardDateRangePicker;

    // Định dạng tiền tệ VNĐ
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
        bindViewModel();

        // Load dữ liệu ban đầu
        viewModel.loadWallets();
        viewModel.loadCategories();
        loadCurrentMonthData();

        return view;
    }

    /**
     * Ánh xạ View & xử lý sự kiện UI cơ bản
     */
    private void initViews(View view) {
        tvNetChange = view.findViewById(R.id.tv_net_change);
        tvExpenseValue = view.findViewById(R.id.tv_expense_value);
        tvIncomeValue = view.findViewById(R.id.tv_income_value);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);

        cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());
    }

    /**
     * Kết nối ViewModel → UI
     */
    private void bindViewModel() {

        // Wallet: Thêm / Sửa / Xóa
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets ->
                uiManager.updateWallets(wallets, new WalletRenderer.WalletActionListener() {
                    @Override
                    public void onCreate(String name) {
                        viewModel.createWallet(name, 0);
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

        // Category: Thêm / Sửa / Xóa
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories ->
                uiManager.updateCategories(categories, new CategoryDialog.OnCategoryActionListener() {
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
                })
        );

        // Hiển thị lỗi từ ViewModel
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Dữ liệu Thu – Chi
        if (viewModel.cashFlowData != null) {
            viewModel.cashFlowData.observe(
                    getViewLifecycleOwner(),
                    this::updateCashFlowUI
            );
        }
    }

    /**
     * Cập nhật UI Thu – Chi – Số dư
     */
    private void updateCashFlowUI(CashFlowResponse data) {
        if (data == null) return;

        double income = Math.abs(data.getTotalIncome());
        double expense = Math.abs(data.getTotalExpense());
        double net = income - expense;

        // Thu nhập
        tvIncomeValue.setText("+" + currencyFormat.format(income));
        tvIncomeValue.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.normal_weight)
        );

        // Chi tiêu
        tvExpenseValue.setText("-" + currencyFormat.format(expense));
        tvExpenseValue.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.obese)
        );

        // Số dư (Net Change)
        String netText = currencyFormat.format(Math.abs(net));
        tvNetChange.setText((net < 0 ? "-" : "+") + netText);
        tvNetChange.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        net < 0 ? R.color.obese : R.color.normal_weight
                )
        );
    }

    /**
     * Load dữ liệu của tháng hiện tại
     */
    private void loadCurrentMonthData() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat apiFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startApi = apiFormat.format(calendar.getTime());
        String startDisplay = displayFormat.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endApi = apiFormat.format(calendar.getTime());
        String endDisplay = displayFormat.format(calendar.getTime());

        tvSelectedDate.setText(startDisplay + " - " + endDisplay);
        viewModel.loadCashFlow(startApi, endApi);
    }

    /**
     * Dialog chọn khoảng thời gian
     */
    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn khoảng thời gian")
                        .setTheme(R.style.CustomDatePickerTheme)
                        .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first == null || selection.second == null) return;

            SimpleDateFormat apiFormat =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat displayFormat =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date startDate = new Date(selection.first);
            Date endDate = new Date(selection.second);

            tvSelectedDate.setText(
                    displayFormat.format(startDate) + " - " +
                            displayFormat.format(endDate)
            );

            viewModel.loadCashFlow(
                    apiFormat.format(startDate),
                    apiFormat.format(endDate)
            );
        });

        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}