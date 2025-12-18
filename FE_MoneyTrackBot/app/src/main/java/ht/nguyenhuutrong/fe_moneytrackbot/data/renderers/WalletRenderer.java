package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.WalletDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;

public class WalletRenderer {

    private final Context context;
    private final LinearLayout container;
    private MaterialCardView selectedCard = null;

    // Interface để gọi ngược về Fragment/ViewModel khi người dùng thao tác
    public interface WalletActionListener {
        void onCreate(String name, double balance);
        void onUpdate(Wallet wallet);
        void onDelete(int id);
    }

    public WalletRenderer(Context context, LinearLayout container) {
        this.context = context;
        this.container = container;
    }

    public void render(List<Wallet> wallets, WalletActionListener listener) {
        if (context == null) return;
        container.removeAllViews();

        for (Wallet wallet : wallets) {
            addWalletView(wallet, listener);
        }
        addAddButton(listener);
    }

    private void addWalletView(Wallet wallet, WalletActionListener listener) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_wallet, container, false);
        TextView tvName = itemView.findViewById(R.id.tv_wallet_name);
        TextView tvAmount = itemView.findViewById(R.id.tv_wallet_amount);
        MaterialCardView card = itemView.findViewById(R.id.card_wallet);

        tvName.setText(wallet.getName());
        tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(wallet.getBalance()));

        card.setOnClickListener(v -> {
            selectWallet(card);
            WalletDialog.showUpdateDelete(context, wallet, new WalletDialog.OnWalletActionListener() {
                @Override public void onCreate(String n, double b) {}
                @Override public void onUpdate(Wallet w) { listener.onUpdate(w); }
                @Override public void onDelete(int id) { listener.onDelete(id); }
            });
        });
        container.addView(itemView);
    }

    private void addAddButton(WalletActionListener listener) {
        View itemAdd = LayoutInflater.from(context).inflate(R.layout.item_add_wallet, container, false);
        itemAdd.findViewById(R.id.card_add_wallet).setOnClickListener(v ->
                WalletDialog.showAddWallet(context, new WalletDialog.OnWalletActionListener() {
                    @Override public void onCreate(String name, double balance) { listener.onCreate(name, balance); }
                    @Override public void onUpdate(Wallet w) {}
                    @Override public void onDelete(int id) {}
                })
        );
        container.addView(itemAdd);
    }

    private void selectWallet(MaterialCardView card) {
        if (selectedCard != null) selectedCard.setStrokeWidth(0);
        card.setStrokeColor(context.getResources().getColor(android.R.color.holo_blue_light));
        card.setStrokeWidth(6);
        selectedCard = card;
    }
}