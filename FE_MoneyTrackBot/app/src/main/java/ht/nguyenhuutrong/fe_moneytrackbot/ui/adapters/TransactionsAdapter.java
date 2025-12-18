package ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff; // Import mới
import android.graphics.PorterDuffColorFilter; // Import mới
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

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> list;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public TransactionsAdapter(List<Transaction> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = list.get(position);

        // --- 1. Hiển thị Category và Note ---
        String catName = t.getCategoryName();
        holder.tvCategoryTitle.setText((catName != null && !catName.isEmpty()) ? catName : "Giao dịch");
        holder.tvNote.setText(t.getNote());

        // --- 2. Hiển thị Ngày ---
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(t.getDate());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d 'thg' M, yyyy", new Locale("vi", "VN"));
            if (date != null) holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) { holder.tvDate.setText(t.getDate()); }

        // --- 3. XỬ LÝ TIỀN TỆ (Dấu, Màu, Mũi tên) ---
        double amount = t.getAmount();
        String moneyString = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(Math.abs(amount));

        if (amount < 0) {
            // === CHI TIÊU (MÀU ĐỎ) ===
            holder.tvAmount.setText("-" + moneyString);

            int colorRed = ContextCompat.getColor(context, R.color.obese);
            holder.tvAmount.setTextColor(colorRed);

            // Xử lý icon Mũi tên xuống
            Drawable arrowDown = ContextCompat.getDrawable(context, R.drawable.ic_triangle_down);
            if (arrowDown != null) {
                // mutate() tạo bản sao để không ảnh hưởng icon gốc
                arrowDown = arrowDown.mutate();
                // 🔥 SỬ DỤNG COLOR FILTER (Mạnh hơn setTint)
                arrowDown.setColorFilter(new PorterDuffColorFilter(colorRed, PorterDuff.Mode.SRC_IN));
                holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(arrowDown, null, null, null);
            }
        } else {
            // === THU NHẬP (MÀU XANH) ===
            holder.tvAmount.setText("+" + moneyString);

            int colorGreen = ContextCompat.getColor(context, R.color.normal_weight);
            holder.tvAmount.setTextColor(colorGreen);

            // Xử lý icon Mũi tên lên
            Drawable arrowUp = ContextCompat.getDrawable(context, R.drawable.ic_triangle_up);
            if (arrowUp != null) {
                arrowUp = arrowUp.mutate();
                // 🔥 SỬ DỤNG COLOR FILTER
                arrowUp.setColorFilter(new PorterDuffColorFilter(colorGreen, PorterDuff.Mode.SRC_IN));
                holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(arrowUp, null, null, null);
            }
        }

        // Set text cho dòng tiền nhỏ
        String signedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        holder.tvAmountSmall.setText(signedAmount);

        // --- 4. Icon Category logic ---
        String categoryLower = (catName != null) ? catName.toLowerCase() : "";
        if (categoryLower.contains("ăn") || categoryLower.contains("uống") || categoryLower.contains("food")) {
            holder.imgCategory.setImageResource(R.mipmap.ic_food);
        } else {
            holder.imgCategory.setImageResource(R.mipmap.ic_launcher);
        }

        // --- 5. Click Event ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCategoryTitle, tvNote, tvAmount, tvAmountSmall;
        ImageView imgCategory;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmountSmall = itemView.findViewById(R.id.tvAmountSmall);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}