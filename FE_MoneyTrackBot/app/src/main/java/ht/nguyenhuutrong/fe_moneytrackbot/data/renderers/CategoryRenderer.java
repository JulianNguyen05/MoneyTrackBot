package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs.CategoryDialog;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;

public class CategoryRenderer {

    private final Context context;
    private final LinearLayout container;

    // UI Filter Buttons
    private final TextView btnExpense;
    private final TextView btnIncome;

    // Lưu listener để dùng cho cả Thêm và Sửa/Xóa
    private CategoryDialog.OnCategoryActionListener actionListener;

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

    // 🔥 CẬP NHẬT: Nhận vào Listener của Dialog để xử lý đủ 3 thao tác
    public void render(List<Category> allCategories, String currentType, CategoryDialog.OnCategoryActionListener listener) {
        if (context == null) return;
        this.actionListener = listener; // Lưu lại để dùng ở các hàm con
        container.removeAllViews();

        for (Category category : allCategories) {
            // Lọc danh mục theo loại (chi tiêu/thu nhập)
            if (category.getType() != null && category.getType().equals(currentType)) {
                addCategoryView(category);
            }
        }
        addAddButton(currentType);
    }

    private void addCategoryView(Category category) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_category, container, false);
        ((TextView) itemView.findViewById(R.id.tv_category_name)).setText(category.getName());

        // 🔥 MỚI: Click vào item thì mở Dialog Sửa/Xóa
        itemView.setOnClickListener(v ->
                CategoryDialog.showUpdateDelete(context, category, actionListener)
        );

        container.addView(itemView);
    }

    private void addAddButton(String currentType) {
        View itemAdd = LayoutInflater.from(context).inflate(R.layout.item_add_category, container, false);

        // Lưu ý: Đảm bảo ID này đúng với file item_add_category.xml của bạn
        View btnAdd = itemAdd.findViewById(R.id.card_add_wallet);

        if (btnAdd != null) {
            // 🔥 MỚI: Click nút cộng thì mở Dialog Thêm
            btnAdd.setOnClickListener(v ->
                    CategoryDialog.showAdd(context, currentType, actionListener)
            );
        }
        container.addView(itemAdd);
    }
}