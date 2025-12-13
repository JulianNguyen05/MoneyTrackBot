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

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.helpers.HomeUIManager;
import ht.nguyenhuutrong.fe_moneytrackbot.helpers.WalletRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.HomeViewModel;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private HomeUIManager uiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        uiManager = new HomeUIManager(getContext(), view, getParentFragmentManager());

        setupBindings();

        viewModel.loadWallets();
        viewModel.loadCategories();

        return view;
    }

    private void setupBindings() {
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
    }
}