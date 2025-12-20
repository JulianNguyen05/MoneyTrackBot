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

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> list;
    private Context context;
    private OnItemClickListener listener;

    // Định dạng ngày và tiền tệ (Khai báo static để tối ưu hiệu năng, tránh tạo lại nhiều lần)
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d 'thg' M, yyyy", new Locale("vi", "VN"));
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

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

        // --- 1. Hiển thị Category và Note (Description) ---
        // Lưu ý: Đảm bảo Model Transaction của bạn có hàm getCategoryName() và getDescription()
        String catName = t.getCategoryName();
        holder.tvCategoryTitle.setText((catName != null && !catName.isEmpty()) ? catName : "Giao dịch");

        // Backend trả về 'description', FE nên map vào đây
        String note = t.getDescription() != null ? t.getDescription() : t.getNote();
        holder.tvNote.setText(note);

        // --- 2. Hiển thị Ngày ---
        try {
            Date date = inputFormat.parse(t.getDate());
            if (date != null) holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDate.setText(t.getDate());
        }

        // --- 3. XỬ LÝ LOGIC TIỀN TỆ MỚI (Dựa vào Type thay vì dấu) ---
        double rawAmount = Math.abs(t.getAmount()); // Luôn lấy số dương để format
        String moneyString = currencyFormat.format(rawAmount);

        // 🔥 LOGIC QUAN TRỌNG: Kiểm tra loại giao dịch
        // Giả sử Model Transaction có hàm getType() trả về "expense" hoặc "income"
        // Hoặc t.getCategory().getType()
        boolean isExpense = "expense".equalsIgnoreCase(t.getType());

        if (isExpense) {
            // === CHI TIÊU (MÀU ĐỎ) ===
            holder.tvAmount.setText("-" + moneyString); // Thêm dấu trừ hiển thị

            int colorRed = ContextCompat.getColor(context, R.color.obese);
            holder.tvAmount.setTextColor(colorRed);
            setupArrow(holder.tvAmount, R.drawable.ic_triangle_down, colorRed);

        } else {
            // === THU NHẬP (MÀU XANH) ===
            holder.tvAmount.setText("+" + moneyString); // Thêm dấu cộng hiển thị

            int colorGreen = ContextCompat.getColor(context, R.color.normal_weight);
            holder.tvAmount.setTextColor(colorGreen);
            setupArrow(holder.tvAmount, R.drawable.ic_triangle_up, colorGreen);
        }

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

    // Hàm phụ trợ để set icon mũi tên cho gọn code
    private void setupArrow(TextView textView, int iconResId, int color) {
        Drawable arrow = ContextCompat.getDrawable(context, iconResId);
        if (arrow != null) {
            arrow = arrow.mutate();
            arrow.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            textView.setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCategoryTitle, tvNote, tvAmount; // Bỏ tvAmountSmall nếu không dùng
        ImageView imgCategory;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategoryTitle = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}