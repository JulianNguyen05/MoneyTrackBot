package ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.activities.LoginActivity;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.SettingsDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupUI();
        bindViewModel();

        return rootView;
    }

    /**
     * Lắng nghe các sự kiện từ ViewModel
     */
    private void bindViewModel() {
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (Boolean.TRUE.equals(isLoggedOut)) {
                navigateToLogin();
            }
        });
    }

    /**
     * Khởi tạo & gán sự kiện cho UI
     */
    private void setupUI() {

        // Toggle Theme (đang phát triển)
        MaterialCardView btnThemeToggle = rootView.findViewById(R.id.btnThemeToggle);
        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v ->
                    Toast.makeText(
                            getContext(),
                            "Chế độ sáng/tối đang được phát triển",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }

        // Các item dạng Double (Title + Subtitle)
        setupDoubleItem(
                R.id.itemWallet,
                "Ví và Danh mục",
                "Quản lý nguồn tiền và phân loại",
                R.drawable.ic_wallet
        );

        setupDoubleItem(
                R.id.itemAccount,
                "Tài khoản",
                "Thông tin cá nhân và bảo mật",
                R.drawable.ic_settings
        );

        // Các item dạng Single
        setupSingleItem(
                R.id.itemPremium,
                "Đăng xuất",
                R.drawable.ic_add,
                v -> showLogoutDialog()
        );

        setupSingleItem(R.id.itemFeature, "Tính năng mới", R.drawable.ic_add, null);
        setupSingleItem(R.id.itemContact, "Liên hệ hỗ trợ", R.drawable.ic_add, null);
        setupSingleItem(R.id.itemTerms, "Điều khoản sử dụng", R.drawable.ic_settings, null);
        setupSingleItem(R.id.itemPrivacy, "Chính sách bảo mật", R.drawable.ic_settings, null);
    }

    /**
     * Thiết lập item cài đặt dạng Double (Title + Subtitle)
     */
    private void setupDoubleItem(
            int id,
            String title,
            String subtitle,
            int iconRes
    ) {
        View item = rootView.findViewById(id);
        if (item == null) return;

        TextView tvTitle = item.findViewById(R.id.title);
        TextView tvSubtitle = item.findViewById(R.id.subtitle);
        ImageView imgIcon = item.findViewById(R.id.icon);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) tvSubtitle.setText(subtitle);
        if (imgIcon != null) imgIcon.setImageResource(iconRes);

        item.setOnClickListener(v ->
                Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Thiết lập item cài đặt dạng Single (chỉ Title)
     */
    private void setupSingleItem(
            int id,
            String title,
            int iconRes,
            View.OnClickListener customListener
    ) {
        View item = rootView.findViewById(id);
        if (item == null) return;

        TextView tvTitle = item.findViewById(R.id.title);
        ImageView imgIcon = item.findViewById(R.id.icon);

        if (tvTitle != null) tvTitle.setText(title);

        if (imgIcon != null) {
            imgIcon.setImageResource(iconRes);

            // Highlight riêng cho nút Đăng xuất
            if ("Đăng xuất".equals(title)) {
                imgIcon.setColorFilter(Color.parseColor("#FFB74D"));
            }
        }

        item.setOnClickListener(
                customListener != null
                        ? customListener
                        : v -> Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Hiển thị dialog xác nhận đăng xuất
     */
    private void showLogoutDialog() {
        if (getContext() == null) return;
        SettingsDialog.showLogoutConfirmation(
                getContext(),
                () -> viewModel.logout()
        );
    }

    /**
     * Điều hướng về màn hình Login và clear back stack
     */
    private void navigateToLogin() {
        if (getContext() == null) return;

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
    }
}