package ht.nguyenhuutrong.fe_moneytrackbot.adapters;

import android.content.Context;
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
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> list;
    private Context context;

    // 1. Khai báo Listener
    private OnItemClickListener listener;

    // 2. Interface để Fragment implements
    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    // 3. Cập nhật Constructor nhận Listener
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

        // ... (Giữ nguyên code hiển thị Text, Color, Icon cũ của bạn) ...
        // START: Code hiển thị cũ
        String catName = t.getCategoryName();
        if (catName != null && !catName.isEmpty()) holder.tvCategoryTitle.setText(catName);
        else holder.tvCategoryTitle.setText("Giao dịch");
        holder.tvNote.setText(t.getNote());

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(t.getDate());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d 'thg' M, yyyy", new Locale("vi", "VN"));
            if (date != null) holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) { holder.tvDate.setText(t.getDate()); }

        double amount = t.getAmount();
        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(Math.abs(amount));
        holder.tvAmount.setText(formattedAmount);
        String signedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        holder.tvAmountSmall.setText(signedAmount);

        if (amount < 0) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.obese));
            holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_triangle_down, 0, 0, 0);
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.normal_weight));
            holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_triangle_up, 0, 0, 0);
        }

        String categoryLower = (catName != null) ? catName.toLowerCase() : "";
        if (categoryLower.contains("ăn") || categoryLower.contains("uống") || categoryLower.contains("food")) {
            holder.imgCategory.setImageResource(R.mipmap.ic_food);
        } else {
            holder.imgCategory.setImageResource(R.mipmap.ic_launcher);
        }
        // END: Code hiển thị cũ

        // 4. Bắt sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(t);
            }
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