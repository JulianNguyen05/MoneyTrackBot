package ht.nguyenhuutrong.fe_moneytrackbot.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;

public class SettingsUIManager {

    private final View rootView;

    public SettingsUIManager(View rootView) {
        this.rootView = rootView;
    }

    public void setupUI(View.OnClickListener logoutListener) {
        // Xử lý nút Theme
        setupThemeButton();

        // --- CÁC MỤC CÀI ĐẶT ---
        setupItem(R.id.itemWallet, "Cài đặt ví và danh mục", "Thể loại, Tiền tệ, Số dư...", R.drawable.ic_wallet, null);
        setupItem(R.id.itemAccount, "Cài đặt tài khoản", "Ngôn ngữ, Xuất CSV...", R.drawable.ic_settings, null);

        // --- 🔥 MỤC ĐĂNG XUẤT ---
        setupItem(R.id.itemPremium, "Đăng xuất", null, R.drawable.ic_add, v -> {
            // Đổi màu icon đặc biệt cho nút này
            ImageView icon = v.findViewById(R.id.icon);
            if (icon != null) icon.setColorFilter(Color.parseColor("#FFC107"));

            // Gọi listener từ Fragment truyền vào
            if (logoutListener != null) logoutListener.onClick(v);
        });

        // Các mục thông tin chung
        setupItem(R.id.itemFeature, "Yêu cầu tính năng", null, R.drawable.ic_add, null);
        setupItem(R.id.itemContact, "Liên hệ với chúng tôi", null, R.drawable.ic_add, null);
        setupItem(R.id.itemTerms, "Điều khoản dịch vụ", null, R.drawable.ic_add, null);
        setupItem(R.id.itemPrivacy, "Chính sách bảo mật", null, R.drawable.ic_add, null);
    }

    private void setupThemeButton() {
        MaterialCardView btnTheme = rootView.findViewById(R.id.btnThemeToggle);
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v ->
                    Toast.makeText(rootView.getContext(), "Đổi Theme sáng/tối", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupItem(int itemId, String title, String subtitle, int iconRes, View.OnClickListener customListener) {
        View item = rootView.findViewById(itemId);
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

        // Nếu có listener riêng (như nút Logout) thì dùng, không thì dùng mặc định
        if (customListener != null) {
            item.setOnClickListener(customListener);
            // Hack nhỏ để set màu icon logout ngay khi setup
            if (title.equals("Đăng xuất")) {
                if (imgIcon != null) imgIcon.setColorFilter(Color.parseColor("#FFC107"));
            }
        } else {
            item.setOnClickListener(v ->
                    Toast.makeText(rootView.getContext(), "Bạn chọn: " + title, Toast.LENGTH_SHORT).show()
            );
        }
    }
}