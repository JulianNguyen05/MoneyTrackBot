package ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;

public class TransactionsAdapter
        extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private final List<Transaction> transactions;
    private final OnItemClickListener listener;
    private Context context;

    /* Format dùng chung – static để tránh tạo lại nhiều lần */
    private static final SimpleDateFormat INPUT_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static final SimpleDateFormat OUTPUT_DATE_FORMAT =
            new SimpleDateFormat("EEE, d 'thg' M, yyyy", new Locale("vi", "VN"));

    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionsAdapter(
            List<Transaction> transactions,
            OnItemClickListener listener
    ) {
        this.transactions = transactions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TransactionViewHolder holder,
            int position
    ) {
        Transaction transaction = transactions.get(position);

        bindCategory(holder, transaction);
        bindDate(holder, transaction);
        bindAmount(holder, transaction);
        bindCategoryIcon(holder, transaction);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(transaction);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /* ======================= BIND HELPERS ======================= */

    /**
     * Hiển thị tên danh mục & ghi chú
     */
    private void bindCategory(
            TransactionViewHolder holder,
            Transaction transaction
    ) {
        String categoryName = transaction.getCategoryName();
        holder.tvCategory.setText(
                categoryName == null || categoryName.isEmpty()
                        ? "Giao dịch"
                        : categoryName
        );

        String note = transaction.getDescription() != null
                ? transaction.getDescription()
                : transaction.getNote();

        holder.tvNote.setText(note);
    }

    /**
     * Format và hiển thị ngày giao dịch
     */
    private void bindDate(
            TransactionViewHolder holder,
            Transaction transaction
    ) {
        try {
            Date date = INPUT_DATE_FORMAT.parse(transaction.getDate());
            holder.tvDate.setText(
                    date != null
                            ? OUTPUT_DATE_FORMAT.format(date)
                            : transaction.getDate()
            );
        } catch (Exception e) {
            holder.tvDate.setText(transaction.getDate());
        }
    }

    /**
     * LOGIC NGHIỆP VỤ QUAN TRỌNG:
     * - Dựa vào type (income / expense)
     * - Không dựa vào dấu của amount
     */
    private void bindAmount(
            TransactionViewHolder holder,
            Transaction transaction
    ) {
        double amount = Math.abs(transaction.getAmount());
        String money = CURRENCY_FORMAT.format(amount);

        boolean isExpense =
                "expense".equalsIgnoreCase(transaction.getType());

        if (isExpense) {
            int red = ContextCompat.getColor(context, R.color.obese);
            holder.tvAmount.setText("-" + money);
            holder.tvAmount.setTextColor(red);
            setArrowIcon(holder.tvAmount, R.drawable.ic_triangle_down, red);
        } else {
            int green = ContextCompat.getColor(context, R.color.normal_weight);
            holder.tvAmount.setText("+" + money);
            holder.tvAmount.setTextColor(green);
            setArrowIcon(holder.tvAmount, R.drawable.ic_triangle_up, green);
        }
    }

    /**
     * Icon danh mục (tạm thời map theo tên)
     */
    private void bindCategoryIcon(
            TransactionViewHolder holder,
            Transaction transaction
    ) {
        String category =
                transaction.getCategoryName() != null
                        ? transaction.getCategoryName().toLowerCase()
                        : "";

        if (category.contains("ăn")
                || category.contains("uống")
                || category.contains("food")) {
            holder.imgCategory.setImageResource(R.mipmap.ic_food);
        } else {
            holder.imgCategory.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * Set icon mũi tên + màu cho TextView amount
     */
    private void setArrowIcon(
            TextView textView,
            int iconRes,
            int color
    ) {
        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(
                    new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            );
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    icon, null, null, null
            );
        }
    }

    /* ======================= VIEW HOLDER ======================= */

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate;
        TextView tvCategory;
        TextView tvNote;
        TextView tvAmount;
        ImageView imgCategory;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}