package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.CategoryDialog; // 🔥 QUAN TRỌNG: Import interface mới
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.DateRangeDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;

public class HomeUIManager {

    private final Context context;
    private final FragmentManager fragmentManager;

    private final WalletRenderer walletRenderer;
    private final CategoryRenderer categoryRenderer;

    private final TextView tvSelectedDate;

    private String currentType = "expense";
    private List<Category> cachedCategories = new ArrayList<>();

    // 🔥 CẬP NHẬT: Đổi kiểu Listener sang CategoryDialog.OnCategoryActionListener
    // Để hỗ trợ đầy đủ 3 sự kiện: onCreate, onUpdate, onDelete
    private CategoryDialog.OnCategoryActionListener categoryListener;

    public HomeUIManager(Context context, View rootView, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;

        MaterialCardView cardDate = rootView.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = rootView.findViewById(R.id.tvSelectedDate);
        cardDate.setOnClickListener(v -> showDatePicker());

        LinearLayout walletContainer = rootView.findViewById(R.id.layoutWalletContainer);
        walletRenderer = new WalletRenderer(context, walletContainer);

        LinearLayout categoryContainer = rootView.findViewById(R.id.layoutCategoryContainer);
        TextView btnExpense = rootView.findViewById(R.id.btn_filter_expense);
        TextView btnIncome = rootView.findViewById(R.id.btn_filter_income);

        categoryRenderer = new CategoryRenderer(context, categoryContainer, btnExpense, btnIncome);

        btnExpense.setOnClickListener(v -> changeCategoryFilter("expense"));
        btnIncome.setOnClickListener(v -> changeCategoryFilter("income"));
    }

    public void updateWallets(List<Wallet> wallets, WalletRenderer.WalletActionListener listener) {
        walletRenderer.render(wallets, listener);
    }

    // 🔥 CẬP NHẬT: Thay đổi tham số truyền vào
    public void updateCategories(List<Category> categories, CategoryDialog.OnCategoryActionListener listener) {
        this.cachedCategories = categories;
        this.categoryListener = listener;
        renderCategoryList();
    }

    private void showDatePicker() {
        DateRangeDialog.show(fragmentManager, (text, start, end) -> tvSelectedDate.setText(text));
    }

    private void changeCategoryFilter(String type) {
        this.currentType = type;
        renderCategoryList();
    }

    private void renderCategoryList() {
        categoryRenderer.updateFilterUI(currentType);
        if (categoryListener != null) {
            // Lúc này hàm render đã nhận đúng kiểu Listener mới
            categoryRenderer.render(cachedCategories, currentType, categoryListener);
        }
    }
}