package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.app.AlertDialog;
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

import com.google.android.material.card.MaterialCardView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.activities.LoginActivity;
import ht.nguyenhuutrong.fe_moneytrackbot.api.TokenManager;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Constructor rỗng
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý nút Theme
        setupThemeToggleButton(view);

        // --- CÁC MỤC CÀI ĐẶT ---
        setupItem(view, R.id.itemWallet, "Cài đặt ví và danh mục", "Thể loại, Tiền tệ, Số dư...", R.drawable.ic_wallet);
        setupItem(view, R.id.itemAccount, "Cài đặt tài khoản", "Ngôn ngữ, Xuất CSV...", R.drawable.ic_settings);

        // --- 🔥 MỤC ĐĂNG XUẤT (SỬA Ở ĐÂY) ---
        // 1. Setup giao diện cơ bản
        setupItem(view, R.id.itemPremium, "Đăng xuất", null, R.drawable.ic_add);

        // 2. Ghi đè sự kiện click riêng cho nút Đăng xuất
        View logoutItem = view.findViewById(R.id.itemPremium);
        if (logoutItem != null) {
            // Đổi màu icon thành vàng (như code cũ của bạn)
            ImageView icon = logoutItem.findViewById(R.id.icon);
            if (icon != null) icon.setColorFilter(Color.parseColor("#FFC107"));

            // Gán sự kiện click -> Hiện dialog hỏi
            logoutItem.setOnClickListener(v -> showLogoutConfirmation());
        }

        // Các mục thông tin chung
        setupItem(view, R.id.itemFeature, "Yêu cầu tính năng", null, R.drawable.ic_add);
        setupItem(view, R.id.itemContact, "Liên hệ với chúng tôi", null, R.drawable.ic_add);
        setupItem(view, R.id.itemTerms, "Điều khoản dịch vụ", null, R.drawable.ic_add);
        setupItem(view, R.id.itemPrivacy, "Chính sách bảo mật", null, R.drawable.ic_add);
    }

    // --- HÀM HIỂN THỊ HỘP THOẠI XÁC NHẬN ---
    private void showLogoutConfirmation() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout()) // Bấm Đồng ý -> Gọi hàm logout
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- HÀM THỰC HIỆN ĐĂNG XUẤT ---
    private void performLogout() {
        if (getContext() == null) return;

        // 1. Xóa Token trong SharedPreferences
        TokenManager.getInstance(getContext()).clearToken();

        // 2. Chuyển về màn hình Đăng nhập
        Intent intent = new Intent(getContext(), LoginActivity.class);

        // 🔥 QUAN TRỌNG: Xóa sạch lịch sử Activity để không Back lại được
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
    }

    // --- HÀM SETUP ITEM CHUNG (Giữ nguyên) ---
    private void setupItem(View parentView, int itemId, String title, String subtitle, int iconRes) {
        View item = parentView.findViewById(itemId);
        if (item == null) return;

        TextView tvTitle = item.findViewById(R.id.title);
        TextView tvSubtitle = item.findViewById(R.id.subtitle);
        ImageView imgIcon = item.findViewById(R.id.icon);

        if (tvTitle != null) tvTitle.setText(title);
        if (imgIcon != null) imgIcon.setImageResource(iconRes);

        if (tvSubtitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                tvSubtitle.setText(subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }
        }

        // Mặc định hiện Toast tên item (Nút Đăng xuất sẽ ghi đè sự kiện này sau)
        item.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Bạn chọn: " + title, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupThemeToggleButton(View view) {
        MaterialCardView btnTheme = view.findViewById(R.id.btnThemeToggle);
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đổi Theme sáng/tối", Toast.LENGTH_SHORT).show();
            });
        }
    }
}