package ht.nguyenhuutrong.fe_moneytrackbot.adapters;

import android.content.Context;
import android.graphics.Color;
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

    public TransactionsAdapter(List<Transaction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = list.get(position);

        // 1. Set text cơ bản
        holder.tvCategoryTitle.setText(t.getCategory());
        holder.tvNote.setText(t.getNote());

        // 2. Format Ngày tháng
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(t.getDate());
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, d 'thg' M, yyyy", new Locale("vi", "VN"));
            if (date != null) {
                holder.tvDate.setText(outputFormat.format(date));
            }
        } catch (Exception e) {
            holder.tvDate.setText(t.getDate());
        }

        // 3. Xử lý logic Tiền tệ (Màu sắc & Icon)
        double amount = t.getAmount();

        // Format số tiền (vd: 15.000 ₫)
        String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(Math.abs(amount));

        // Set text cho cả 2 vị trí (lớn và nhỏ)
        holder.tvAmount.setText(formattedAmount);

        // Dòng nhỏ hiển thị dấu trừ nếu là âm
        String signedAmount = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        holder.tvAmountSmall.setText(signedAmount);

        if (amount < 0) {
            // --- CHI TIÊU ---
            // Màu đỏ (Lấy từ colors.xml hoặc hardcode nếu chưa có)
            int redColor = ContextCompat.getColor(context, R.color.obese); // Đảm bảo bạn có màu này trong colors.xml
            holder.tvAmount.setTextColor(redColor);

            // Icon mũi tên xuống (ic_triangle_down)
            holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_triangle_down, 0, 0, 0);
        } else {
            // --- THU NHẬP ---
            // Màu xanh (Ví dụ: normal_weight hoặc màu xanh lá)
            int greenColor = ContextCompat.getColor(context, R.color.normal_weight); // Hoặc Color.parseColor("#4CAF50")
            holder.tvAmount.setTextColor(greenColor);

            // Icon mũi tên lên (ic_triangle_up)
            holder.tvAmount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_triangle_up, 0, 0, 0);
        }

        // 4. Xử lý Icon danh mục (Ví dụ đơn giản)
        // Bạn nên tạo một hàm riêng hoặc Map để quản lý cái này cho gọn
        String category = t.getCategory().toLowerCase();
        if (category.contains("ăn") || category.contains("uống") || category.contains("food")) {
            holder.imgCategory.setImageResource(R.mipmap.ic_food); // Đảm bảo có ảnh này
        } else if (category.contains("xe") || category.contains("xăng") || category.contains("di chuyển")) {
            // holder.imgCategory.setImageResource(R.drawable.ic_transport); // Ví dụ
            holder.imgCategory.setImageResource(R.mipmap.ic_launcher); // Tạm thời để default
        } else {
            holder.imgCategory.setImageResource(R.mipmap.ic_launcher); // Ảnh mặc định
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvCategoryTitle, tvNote, tvAmount, tvAmountSmall;
        ImageView imgCategory;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ đúng với ID trong XML mới của bạn
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmountSmall = itemView.findViewById(R.id.tvAmountSmall); // Mới

            imgCategory = itemView.findViewById(R.id.imgCategory);     // Mới
            tvCategoryTitle = itemView.findViewById(R.id.tvCategory);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}