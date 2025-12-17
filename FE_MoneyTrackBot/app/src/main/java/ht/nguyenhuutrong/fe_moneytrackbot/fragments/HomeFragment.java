package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.HomeUIManager;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.WalletRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private HomeUIManager uiManager;

    // --- KHAI BÁO VIEW CHO PHẦN CASH FLOW ---
    private TextView tvNetChange, tvExpenseValue, tvIncomeValue, tvSelectedDate;
    private View cardDateRangePicker;

    // Formatter tiền tệ (VNĐ)
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        uiManager = new HomeUIManager(getContext(), view, getParentFragmentManager());

        // 1. Ánh xạ các View của phần Tổng kết (CashFlow)
        initCashFlowViews(view);

        // 2. Thiết lập lắng nghe dữ liệu
        setupBindings();

        // 3. Load dữ liệu ban đầu
        viewModel.loadWallets();
        viewModel.loadCategories();
        loadCurrentMonthData(); // Load báo cáo tài chính tháng này

        return view;
    }

    private void initCashFlowViews(View view) {
        tvNetChange = view.findViewById(R.id.tv_net_change);
        tvExpenseValue = view.findViewById(R.id.tv_expense_value);
        tvIncomeValue = view.findViewById(R.id.tv_income_value);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);

        // Sự kiện click chọn ngày (Tạm thời hiển thị Toast, bạn có thể thêm MaterialDatePicker sau)
        cardDateRangePicker.setOnClickListener(v ->
                Toast.makeText(getContext(), "Tính năng chọn ngày đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBindings() {
        // --- PHẦN CŨ (Ví & Danh mục) ---
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets ->
                uiManager.updateWallets(wallets, new WalletRenderer.WalletActionListener() {
                    @Override public void onCreate(String name, double balance) { viewModel.createWallet(name, balance); }
                    @Override public void onUpdate(Wallet w) { viewModel.updateWallet(w); }
                    @Override public void onDelete(int id) { viewModel.deleteWallet(id); }
                })
        );

        viewModel.getCategories().observe(getViewLifecycleOwner(), categories ->
                uiManager.updateCategories(categories, (name, type) -> viewModel.createCategory(name, type))
        );

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // --- PHẦN MỚI (Cash Flow / Chênh lệch thu chi) ---
        // Giả sử trong HomeViewModel bạn đã tạo LiveData tên là cashFlowData
        if (viewModel.cashFlowData != null) {
            viewModel.cashFlowData.observe(getViewLifecycleOwner(), this::updateCashFlowUI);
        }
    }

    // Hàm cập nhật giao diện thẻ Tổng kết
    private void updateCashFlowUI(CashFlowResponse data) {
        if (data == null) return;

        // 1. Hiển thị số tiền Thu / Chi
        tvIncomeValue.setText(currencyFormat.format(data.getTotalIncome()));
        tvExpenseValue.setText(currencyFormat.format(data.getTotalExpense()));

        // 2. Hiển thị Chênh lệch (Net Change)
        double netChange = data.getNetChange();
        tvNetChange.setText(currencyFormat.format(netChange));

        // 3. Đổi màu sắc: Lãi (Xanh), Lỗ (Đỏ)
        if (netChange >= 0) {
            tvNetChange.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá
        } else {
            tvNetChange.setTextColor(Color.parseColor("#F44336")); // Đỏ
        }
    }

    // Hàm tính ngày đầu tháng và cuối tháng hiện tại để gọi API
    private void loadCurrentMonthData() {
        Calendar calendar = Calendar.getInstance();

        // Format cho Backend (yyyy-MM-dd)
        SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        // Format hiển thị UI (dd/MM/yyyy)
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Ngày 1 đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDateApi = sdfApi.format(calendar.getTime());
        String startDateDisplay = sdfDisplay.format(calendar.getTime());

        // Ngày cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDateApi = sdfApi.format(calendar.getTime());
        String endDateDisplay = sdfDisplay.format(calendar.getTime());

        // Cập nhật text hiển thị ngày
        tvSelectedDate.setText(startDateDisplay + " - " + endDateDisplay);

        // Gọi ViewModel tải dữ liệu
        viewModel.loadCashFlow(startDateApi, endDateApi);
    }
}