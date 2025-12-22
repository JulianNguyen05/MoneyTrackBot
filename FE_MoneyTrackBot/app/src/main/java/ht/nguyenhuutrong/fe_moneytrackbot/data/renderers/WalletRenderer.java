package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat; // 🔥 ADDED: Import để lấy màu an toàn

import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.WalletDialog;

/**
 * WalletRenderer
 * ------------------------------------------------
 * Chịu trách nhiệm hiển thị danh sách ví
 * và xử lý tương tác chọn / thêm / sửa / xóa.
 */
public class WalletRenderer {

    private final Context context;
    private final LinearLayout container;

    private MaterialCardView selectedCard;

    /**
     * Callback để thông báo hành động ví về Fragment / ViewModel
     */
    public interface WalletActionListener {
        void onCreate(String name);
        void onUpdate(Wallet wallet);
        void onDelete(int id);
    }

    public WalletRenderer(Context context, LinearLayout container) {
        this.context = context;
        this.container = container;
    }

    /**
     * Render danh sách ví
     */
    public void render(List<Wallet> wallets, WalletActionListener listener) {
        if (context == null) return;

        container.removeAllViews();

        for (Wallet wallet : wallets) {
            addWalletView(wallet, listener);
        }

        addAddButton(listener);
    }

    /**
     * Render một item ví
     */
    private void addWalletView(Wallet wallet, WalletActionListener listener) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_wallet, container, false);

        TextView tvName = view.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = view.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = view.findViewById(R.id.card_wallet);

        tvName.setText(wallet.getName());

        double balance = wallet.getBalance();
        tvAmount.setText(formatCurrency(balance));

        // 🔥 ADDED: Logic đổi màu dựa trên số dư
        if (balance > 0) {
            // Màu Xanh (Dùng R.color.normal_weight hoặc tên màu xanh bạn đặt trong colors.xml)
            tvAmount.setTextColor(ContextCompat.getColor(context, R.color.normal_weight));
        } else {
            // Màu Đỏ (Dùng R.color.obese hoặc tên màu đỏ bạn đặt trong colors.xml)
            tvAmount.setTextColor(ContextCompat.getColor(context, R.color.obese));
        }

        card.setOnClickListener(v -> {
            selectCard(card);
            showUpdateDeleteDialog(wallet, listener);
        });

        container.addView(view);
    }

    /**
     * Render nút thêm ví
     */
    private void addAddButton(WalletActionListener listener) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_add_wallet, container, false);

        view.findViewById(R.id.card_add_wallet).setOnClickListener(v ->
                WalletDialog.showAddWallet(context, new WalletDialog.OnWalletActionListener() {
                    @Override public void onCreate(String name) {
                        listener.onCreate(name);
                    }
                    @Override public void onUpdate(Wallet wallet) {}
                    @Override public void onDelete(int id) {}
                })
        );

        container.addView(view);
    }

    /**
     * Hiển thị dialog sửa / xóa ví
     */
    private void showUpdateDeleteDialog(Wallet wallet, WalletActionListener listener) {
        WalletDialog.showUpdateDelete(
                context,
                wallet,
                new WalletDialog.OnWalletActionListener() {
                    @Override public void onCreate(String name) {}
                    @Override public void onUpdate(Wallet w) { listener.onUpdate(w); }
                    @Override public void onDelete(int id) { listener.onDelete(id); }
                }
        );
    }

    /**
     * Đánh dấu ví đang được chọn
     */
    private void selectCard(MaterialCardView card) {
        if (selectedCard != null) {
            selectedCard.setStrokeWidth(0);
        }

        // Cập nhật dùng ContextCompat cho an toàn
        card.setStrokeColor(
                ContextCompat.getColor(context, android.R.color.holo_blue_light)
        );
        card.setStrokeWidth(6);
        selectedCard = card;
    }

    /**
     * Format số tiền theo tiền tệ Việt Nam
     */
    private String formatCurrency(double amount) {
        return NumberFormat
                .getCurrencyInstance(new Locale("vi", "VN"))
                .format(amount);
    }
}