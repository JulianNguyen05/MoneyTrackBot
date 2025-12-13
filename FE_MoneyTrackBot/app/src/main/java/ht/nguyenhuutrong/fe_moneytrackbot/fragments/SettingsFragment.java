package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.content.Intent;
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
import ht.nguyenhuutrong.fe_moneytrackbot.activities.LoginActivity;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.SettingsDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.helpers.SettingsUIManager;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private SettingsUIManager uiManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // 1. Khởi tạo ViewModel & UIManager
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        uiManager = new SettingsUIManager(view);

        // 2. Thiết lập UI & Sự kiện click
        uiManager.setupUI(v -> showLogoutDialog());

        // 3. Lắng nghe sự kiện đăng xuất thành công
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                navigateToLogin();
            }
        });

        return view;
    }

    private void showLogoutDialog() {
        SettingsDialog.showLogoutConfirmation(getContext(), () -> {
            // Khi người dùng bấm "Đồng ý" -> Gọi ViewModel
            viewModel.logout();
        });
    }

    private void navigateToLogin() {
        if (getContext() == null) return;
        Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}