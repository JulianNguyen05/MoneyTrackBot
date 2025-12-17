package ht.nguyenhuutrong.fe_moneytrackbot.renderers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.dialogs.AddCategoryDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;

public class CategoryRenderer {

    private final Context context;
    private final LinearLayout container;

    // UI Filter Buttons
    private final TextView btnExpense;
    private final TextView btnIncome;

    public interface CategoryActionListener {
        void onCreate(String name, String type);
    }

    public CategoryRenderer(Context context, LinearLayout container, TextView btnExpense, TextView btnIncome) {
        this.context = context;
        this.container = container;
        this.btnExpense = btnExpense;
        this.btnIncome = btnIncome;
    }

    public void updateFilterUI(String currentType) {
        boolean isExpense = "expense".equals(currentType);
        btnExpense.setBackgroundResource(isExpense ? R.drawable.bg_button_gradient : R.drawable.bg_gray_rounded);
        btnExpense.setTextColor(isExpense ? Color.WHITE : Color.BLACK);

        btnIncome.setBackgroundResource(!isExpense ? R.drawable.bg_button_gradient : R.drawable.bg_gray_rounded);
        btnIncome.setTextColor(!isExpense ? Color.WHITE : Color.BLACK);
    }

    public void render(List<Category> allCategories, String currentType, CategoryActionListener listener) {
        if (context == null) return;
        container.removeAllViews();

        for (Category category : allCategories) {
            if (category.getType() != null && category.getType().equals(currentType)) {
                addCategoryView(category);
            }
        }
        addAddButton(currentType, listener);
    }

    private void addCategoryView(Category category) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_category, container, false);
        ((TextView) itemView.findViewById(R.id.tv_category_name)).setText(category.getName());
        container.addView(itemView);
    }

    private void addAddButton(String currentType, CategoryActionListener listener) {
        View itemAdd = LayoutInflater.from(context).inflate(R.layout.item_add_category, container, false);
        View btnAdd = itemAdd.findViewById(R.id.card_add_wallet);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v ->
                    AddCategoryDialog.show(context, currentType, listener::onCreate)
            );
        }
        container.addView(itemAdd);
    }
}