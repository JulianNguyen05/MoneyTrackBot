package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.TransactionDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.renderers.TransactionRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.TransactionViewModel;

public class TransactionsFragment extends Fragment {

    private TransactionViewModel viewModel;
    private TransactionRenderer renderer;

    // Cache data for Dialog
    private List<Wallet> cachedWallets = new ArrayList<>();
    private List<Category> cachedCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // 2. Init Renderer (RecyclerView)
        RecyclerView rcv = view.findViewById(R.id.rcvTransactions);
        renderer = new TransactionRenderer(getContext(), rcv, this::showDialog);

        // 3. Observers
        setupObservers();

        // 4. Load Data
        viewModel.loadData();

        // 5. Add Button
        MaterialCardView btnAdd = view.findViewById(R.id.btnAddTransaction);
        btnAdd.setOnClickListener(v -> showDialog(null));

        return view;
    }

    private void setupObservers() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> renderer.render(list));
        viewModel.getWallets().observe(getViewLifecycleOwner(), list -> cachedWallets = list);
        viewModel.getCategories().observe(getViewLifecycleOwner(), list -> cachedCategories = list);

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (getContext() != null && msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void showDialog(Transaction existingTransaction) {
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
}