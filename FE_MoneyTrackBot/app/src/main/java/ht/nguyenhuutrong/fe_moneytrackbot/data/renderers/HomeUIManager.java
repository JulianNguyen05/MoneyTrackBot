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
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.CategoryDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.DateRangeDialog;

/**
 * HomeUIManager
 * -------------------------------------------------
 * Điều phối UI cho Home screen:
 * - Ví (Wallet)
 * - Danh mục (Category)
 * - Bộ lọc ngày & loại thu/chi
 */
public class HomeUIManager {

    private final Context context;
    private final FragmentManager fragmentManager;

    private final WalletRenderer walletRenderer;
    private final CategoryRenderer categoryRenderer;

    private final TextView tvSelectedDate;

    private String currentCategoryType = "expense";
    private List<Category> cachedCategories = new ArrayList<>();

    private CategoryDialog.OnCategoryActionListener categoryListener;

    public HomeUIManager(Context context, View rootView, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;

        // Date range picker
        MaterialCardView cardDate = rootView.findViewById(R.id.cardDateRangePicker);
        tvSelectedDate = rootView.findViewById(R.id.tvSelectedDate);
        cardDate.setOnClickListener(v -> showDatePicker());

        // Wallet section
        LinearLayout walletContainer = rootView.findViewById(R.id.layoutWalletContainer);
        walletRenderer = new WalletRenderer(context, walletContainer);

        // Category section
        LinearLayout categoryContainer = rootView.findViewById(R.id.layoutCategoryContainer);
        TextView btnExpense = rootView.findViewById(R.id.btn_filter_expense);
        TextView btnIncome = rootView.findViewById(R.id.btn_filter_income);

        categoryRenderer = new CategoryRenderer(
                context,
                categoryContainer,
                btnExpense,
                btnIncome
        );

        btnExpense.setOnClickListener(v -> changeCategoryType("expense"));
        btnIncome.setOnClickListener(v -> changeCategoryType("income"));
    }

    /**
     * Render danh sách ví
     */
    public void updateWallets(List<Wallet> wallets,
                              WalletRenderer.WalletActionListener listener) {
        walletRenderer.render(wallets, listener);
    }

    /**
     * Render danh sách danh mục (có cache để filter)
     */
    public void updateCategories(List<Category> categories,
                                 CategoryDialog.OnCategoryActionListener listener) {
        this.cachedCategories = categories;
        this.categoryListener = listener;
        renderCategories();
    }

    /**
     * Hiển thị dialog chọn khoảng thời gian
     */
    private void showDatePicker() {
        DateRangeDialog.show(
                fragmentManager,
                (text, start, end) -> tvSelectedDate.setText(text)
        );
    }

    /**
     * Đổi loại danh mục: expense / income
     */
    private void changeCategoryType(String type) {
        this.currentCategoryType = type;
        renderCategories();
    }

    /**
     * Render lại danh sách danh mục theo filter hiện tại
     */
    private void renderCategories() {
        categoryRenderer.updateFilterUI(currentCategoryType);

        if (categoryListener != null) {
            categoryRenderer.render(
                    cachedCategories,
                    currentCategoryType,
                    categoryListener
            );
        }
    }
}