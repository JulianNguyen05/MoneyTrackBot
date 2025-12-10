package ht.nguyenhuutrong.fe_moneytrackbot;

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

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Constructor rỗng bắt buộc phải có
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Liên kết với file giao diện fragment_settings.xml
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. Xử lý nút đổi giao diện Sáng/Tối ---
        setupThemeToggleButton(view);

        // --- 2. Đổ dữ liệu vào các mục Cài đặt (Icon, Tiêu đề, Mô tả) ---

        // Mục: Ví (Có mô tả phụ)
        setupItem(view, R.id.itemWallet,
                "Cài đặt ví và danh mục",
                "Thể loại, Tiền tệ, Số dư ban đầu",
                R.drawable.ic_wallet);

        // Mục: Tài khoản (Có mô tả phụ)
        setupItem(view, R.id.itemAccount,
                "Cài đặt tài khoản",
                "Ngôn ngữ, Cài đặt Rolly, Xuất/Nhập CSV",
                R.drawable.ic_settings);

        // Mục: Premium (Đặc biệt: Icon màu vàng)
        setupItem(view, R.id.itemPremium,
                "Tận hưởng Rolly Premium",
                null,
                R.drawable.ic_add);
        // Tô màu vàng cho vương miện
        ImageView iconPremium = view.findViewById(R.id.itemPremium).findViewById(R.id.icon);
        if (iconPremium != null) {
            iconPremium.setColorFilter(Color.parseColor("#FFC107"));
        }

        // Các mục thông tin chung (1 dòng)
        setupItem(view, R.id.itemFeature, "Yêu cầu tính năng", null, R.drawable.ic_add);
        setupItem(view, R.id.itemContact, "Liên hệ với chúng tôi", null, R.drawable.ic_add);
        setupItem(view, R.id.itemTerms, "Điều khoản dịch vụ", null, R.drawable.ic_add);
        setupItem(view, R.id.itemPrivacy, "Chính sách bảo mật", null, R.drawable.ic_add);
    }

    /**
     * Hàm dùng chung để cài đặt nội dung cho từng thẻ
     */
    private void setupItem(View parentView, int itemId, String title, String subtitle, int iconRes) {
        View item = parentView.findViewById(itemId);
        if (item == null) return;

        TextView tvTitle = item.findViewById(R.id.title);
        TextView tvSubtitle = item.findViewById(R.id.subtitle);
        ImageView imgIcon = item.findViewById(R.id.icon);

        if (tvTitle != null) tvTitle.setText(title);
        if (imgIcon != null) imgIcon.setImageResource(iconRes);

        // Ẩn hiện mô tả phụ
        if (tvSubtitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                tvSubtitle.setText(subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }
        }

        // Sự kiện click
        item.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Bạn chọn: " + title, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Hàm xử lý nút đổi theme
     */
    private void setupThemeToggleButton(View view) {
        MaterialCardView btnTheme = view.findViewById(R.id.btnThemeToggle);
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đổi Theme sáng/tối", Toast.LENGTH_SHORT).show();
            });
        }
    }
}