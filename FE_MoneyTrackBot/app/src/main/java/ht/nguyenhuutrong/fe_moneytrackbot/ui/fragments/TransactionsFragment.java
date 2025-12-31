package ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters.TransactionsAdapter;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.TransactionDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.TransactionViewModel;

public class TransactionsFragment extends Fragment {

    private TransactionViewModel viewModel;

    // RecyclerView
    private RecyclerView rcvTransactions;
    private TransactionsAdapter adapter;

    // Header & Summary
    private TextView tvWalletSelector;
    private TextView tvSelectedDate;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvBalance;

    // Actions
    private View cardDateRangePicker;
    private MaterialCardView btnAdd;

    // Cached data
    private List<Wallet> cachedWallets = new ArrayList<>();
    private List<Category> cachedCategories = new ArrayList<>();

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        initViews(view);
        setupListeners();
        bindViewModel();

        viewModel.loadWallets();
        viewModel.loadCategories();
        initDefaultDate();

        return view;
    }

    private void initViews(View view) {
        rcvTransactions = view.findViewById(R.id.rcvTransactions);
        rcvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        tvWalletSelector = view.findViewById(R.id.tvCategory);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        cardDateRangePicker = view.findViewById(R.id.cardDateRangePicker);

        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvBalance = view.findViewById(R.id.tvBalance);

        btnAdd = view.findViewById(R.id.btnAddTransaction);
    }

    private void setupListeners() {
        tvWalletSelector.setOnClickListener(v -> showWalletSelectionDialog());
        cardDateRangePicker.setOnClickListener(v -> showDateRangePicker());
        btnAdd.setOnClickListener(v -> showTransactionDialog(null));
    }

    private void bindViewModel() {

        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            if (list == null) return;
            adapter = new TransactionsAdapter(list, this::showTransactionDialog);
            rcvTransactions.setAdapter(adapter);
        });

        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> cachedWallets = wallets);
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> cachedCategories = categories);

        viewModel.getCashFlow().observe(getViewLifecycleOwner(), this::updateSummaryUI);

        viewModel.selectedWallet.observe(
                getViewLifecycleOwner(),
                wallet -> tvWalletSelector.setText(wallet == null ? "Tất cả ví" : wallet.getName())
        );

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && getContext() != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummaryUI(CashFlowResponse data) {
        if (data == null) return;

        tvIncome.setText(currencyFormat.format(data.getTotalIncome()));
        tvExpense.setText(currencyFormat.format(data.getTotalExpense()));

        double net = data.getNetChange();
        tvBalance.setText(currencyFormat.format(net));

        int color = net >= 0 ? R.color.normal_weight : R.color.obese;
        tvBalance.setTextColor(ContextCompat.getColor(requireContext(), color));
    }

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.item_dropdown, names);

        builder.setAdapter(adapter, (dialog, which) -> {
            if (which == 0) viewModel.setWallet(null);
            else viewModel.setWallet(cachedWallets.get(which - 1));
        });

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        }
        dialog.show();
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder =
                MaterialDatePicker.Builder.dateRangePicker();

        builder.setTitleText("Chọn khoảng thời gian");
        builder.setTheme(R.style.CustomDatePickerTheme);

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first == null || selection.second == null) return;

            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date start = new Date(selection.first);
            Date end = new Date(selection.second);

            tvSelectedDate.setText(
                    String.format(
                            "%s - %s",
                            displayFormat.format(start),
                            displayFormat.format(end)
                    )
            );

            viewModel.setDateRange(
                    apiFormat.format(start),
                    apiFormat.format(end)
            );
        });

        picker.show(getParentFragmentManager(), "TRANSACTION_DATE_PICKER");
    }

    private void showTransactionDialog(Transaction existingTransaction) {
        if (getContext() == null) return;

        Wallet currentWallet = viewModel.selectedWallet.getValue();

        TransactionDialog.show(
                getContext(),
                existingTransaction,
                cachedWallets,
                cachedCategories,
                currentWallet,
                new TransactionDialog.DialogListener() {
                    @Override
                    public void onSave(Transaction t, Integer id) {
                        if (id == null) viewModel.createTransaction(t);
                        else viewModel.updateTransaction(id, t);
                    }

                    @Override
                    public void onDelete(int id) {
                        viewModel.deleteTransaction(id);
                    }
                }
        );
    }

    private void initDefaultDate() {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date start = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = calendar.getTime();

        tvSelectedDate.setText(
                String.format(
                        "%s - %s",
                        displayFormat.format(start),
                        displayFormat.format(end)
                )
        );

        viewModel.setDateRange(
                apiFormat.format(start),
                apiFormat.format(end)
        );
    }
}
