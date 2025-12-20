package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.CategoryDialog;

/**
 * CategoryRenderer
 * ----------------------------------------
 * Chịu trách nhiệm render danh sách Category
 * theo loại (income / expense) và xử lý UI tương tác.
 */
public class CategoryRenderer {

    private final Context context;
    private final LinearLayout container;

    private final TextView btnExpense;
    private final TextView btnIncome;

    private CategoryDialog.OnCategoryActionListener actionListener;

    public CategoryRenderer(
            Context context,
            LinearLayout container,
            TextView btnExpense,
            TextView btnIncome
    ) {
        this.context = context;
        this.container = container;
        this.btnExpense = btnExpense;
        this.btnIncome = btnIncome;
    }

    /**
     * Cập nhật trạng thái UI cho nút lọc Thu / Chi
     */
    public void updateFilterUI(String currentType) {
        boolean isExpense = "expense".equals(currentType);

        btnExpense.setBackgroundResource(
                isExpense ? R.drawable.bg_button_gradient : R.drawable.bg_gray_rounded
        );
        btnExpense.setTextColor(isExpense ? Color.WHITE : Color.BLACK);

        btnIncome.setBackgroundResource(
                !isExpense ? R.drawable.bg_button_gradient : R.drawable.bg_gray_rounded
        );
        btnIncome.setTextColor(!isExpense ? Color.WHITE : Color.BLACK);
    }

    /**
     * Render danh sách Category theo loại hiện tại
     */
    public void render(
            List<Category> categories,
            String currentType,
            CategoryDialog.OnCategoryActionListener listener
    ) {
        if (context == null) return;

        this.actionListener = listener;
        container.removeAllViews();

        for (Category category : categories) {
            if (currentType.equals(category.getType())) {
                addCategoryItem(category);
            }
        }

        addAddButton(currentType);
    }

    /**
     * Render 1 Category item
     */
    private void addCategoryItem(Category category) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_category, container, false);

        TextView tvName = itemView.findViewById(R.id.tv_category_name);
        tvName.setText(category.getName());

        itemView.setOnClickListener(v ->
                CategoryDialog.showUpdateDelete(context, category, actionListener)
        );

        container.addView(itemView);
    }

    /**
     * Render nút thêm Category
     */
    private void addAddButton(String currentType) {
        View addView = LayoutInflater.from(context)
                .inflate(R.layout.item_add_category, container, false);

        View btnAdd = addView.findViewById(R.id.card_add_wallet);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v ->
                    CategoryDialog.showAdd(context, currentType, actionListener)
            );
        }

        container.addView(addView);
    }
}