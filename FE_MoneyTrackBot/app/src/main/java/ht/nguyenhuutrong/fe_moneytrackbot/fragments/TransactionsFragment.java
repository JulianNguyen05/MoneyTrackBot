package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.TransactionDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.TransactionRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.TransactionViewModel;

public class TransactionsFragment extends Fragment {

    private TransactionViewModel viewModel;
    private TransactionRenderer renderer;

    // --- VIEW UI ---
    private TextView tvWalletSelector, tvSelectedDate;
    private TextView tvIncome, tvExpense, tvBalance;
    private View cardDateRangePicker;
    private MaterialCardView btnAdd;

    // --- DATA CACHE (Cho Dialog thêm/sửa) ---
    private List<Wallet> cachedWallets = new ArrayList<>();
    private List<Category> cachedCategories = new ArrayList<>();

    // --- FORMATTER ---
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // 2. Ánh xạ View & Init RecyclerView
        initViews(view);

        // 3. Setup Sự kiện & Observers
        setupListeners();
        setupObservers();

        // 4. Load Dữ liệu ban đầu
        viewModel.loadWallets();     // Load ví để chọn
        viewModel.loadCategories();  // Load danh mục để thêm
        initDefaultDate();           // Set ngày mặc định -> Tự động loadData()

        return view;
    }

    private void initViews(View view) {
        // RecyclerView
        RecyclerView rcv = view.findViewById(R.id.rcvTransactions);
        renderer = new TransactionRenderer(getContext(), rcv, this::showTransactionDialog);

        // Bộ lọc & Header
        tvWalletSelector = view.findViewById(R.id.tvCategory); // Dùng text này để chọn Ví
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);

        // Thẻ Tổng kết
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);

        // Nút thêm
        btnAdd = view.findViewById(R.id.btnAddTransaction);
    }

    private void setupListeners() {
        // Click vào tên Ví -> Chọn Ví
        tvWalletSelector.setOnClickListener(v -> showWalletSelectionDialog());

        // Click vào ngày -> Chọn ngày
        cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());

        // Click nút thêm -> Mở Dialog thêm mới
        btnAdd.setOnClickListener(v -> showTransactionDialog(null));
    }

    private void setupObservers() {
        // 1. Danh sách Giao dịch -> Hiển thị lên RecyclerView
        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> renderer.render(list));

        // 2. Danh sách Ví -> Lưu cache & Cập nhật Dialog chọn ví
        viewModel.getWallets().observe(getViewLifecycleOwner(), list -> cachedWallets = list);

        // 3. Danh sách Danh mục -> Lưu cache cho Dialog thêm mới
        viewModel.getCategories().observe(getViewLifecycleOwner(), list -> cachedCategories = list);

        // 4. Tổng kết tiền (CashFlow) -> Cập nhật thẻ Summary
        viewModel.getCashFlow().observe(getViewLifecycleOwner(), this::updateSummaryUI);

        // 5. Tên ví đang chọn -> Cập nhật UI Header
        viewModel.selectedWallet.observe(getViewLifecycleOwner(), wallet -> {
            if (wallet == null) {
                tvWalletSelector.setText("Tất cả ví");
            } else {
                tvWalletSelector.setText(wallet.getName());
            }
        });

        // 6. Thông báo lỗi/thành công
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (getContext() != null && msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    // --- CẬP NHẬT GIAO DIỆN TỔNG KẾT ---
    private void updateSummaryUI(CashFlowResponse data) {
        if (data == null) return;
        tvIncome.setText(currencyFormat.format(data.getTotalIncome()));
        tvExpense.setText(currencyFormat.format(data.getTotalExpense()));

        double netChange = data.getNetChange();
        tvBalance.setText(currencyFormat.format(netChange));

        // Đổi màu số dư (Xanh/Đỏ)
        int colorRes = netChange >= 0 ? R.color.normal_weight : R.color.obese;
        // Lưu ý: Đảm bảo bạn có màu normal_weight/obese trong colors.xml, hoặc thay bằng R.color.green/red
        tvBalance.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    // --- DIALOG CHỌN VÍ ---
    private void showWalletSelectionDialog() {
        if (cachedWallets.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải danh sách ví...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[cachedWallets.size() + 1];
        names[0] = "Tất cả ví";
        for (int i = 0; i < cachedWallets.size(); i++) {
            names[i + 1] = cachedWallets.get(i).getName();
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Chọn ví xem giao dịch")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) viewModel.setWallet(null);
                    else viewModel.setWallet(cachedWallets.get(which - 1));
                })
                .show();
    }

    // --- DIALOG CHỌN NGÀY ---
// --- DIALOG CHỌN NGÀY (Giao diện giống HomeFragment) ---
        private void showDateRangePicker() {
        // 1. Tạo Builder & Set Theme
        MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();

        builder.setTitleText("Chọn khoảng thời gian");

        // 🔥 Áp dụng Theme (Nền xám nhạt, chữ đen)
        builder.setTheme(R.style.CustomDatePickerTheme);

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        // 2. Xử lý khi bấm OK
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

                // Cập nhật text hiển thị
                tvSelectedDate.setText(String.format("%s - %s", startDisplay, endDisplay));

                // 🔥 QUAN TRỌNG: Gọi setDateRange để ViewModel tải lại
                // cả danh sách Transaction VÀ báo cáo CashFlow
                viewModel.setDateRange(startApi, endApi);
            }
        });

        picker.show(getParentFragmentManager(), "TRANSACTION_DATE_PICKER");
    }

    // --- DIALOG THÊM/SỬA GIAO DỊCH (Giữ nguyên logic cũ) ---
    private void showTransactionDialog(Transaction existingTransaction) {
        if (getContext() == null) return;
        TransactionDialog.show(getContext(), existingTransaction, cachedWallets, cachedCategories,
                new TransactionDialog.DialogListener() {
                    @Override public void onSave(Transaction t, Integer id) {
                        if (id == null) viewModel.createTransaction(t);
                        else viewModel.updateTransaction(id, t);
                    }
                    @Override public void onDelete(int id) {
                        viewModel.deleteTransaction(id);
                    }
                }
        );
    }

    // --- KHỞI TẠO NGÀY MẶC ĐỊNH (Đầu tháng -> Cuối tháng) ---
    private void initDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startApi = apiFormat.format(calendar.getTime());
        String startDisplay = displayFormat.format(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endApi = apiFormat.format(calendar.getTime());
        String endDisplay = displayFormat.format(calendar.getTime());

        tvSelectedDate.setText(String.format("%s - %s", startDisplay, endDisplay));
        viewModel.setDateRange(startApi, endApi);
    }
}