package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters.TransactionsAdapter;

/**
 * TransactionRenderer
 * ------------------------------------------------
 * Chịu trách nhiệm hiển thị danh sách giao dịch
 * và xử lý sự kiện click trên từng item.
 */
public class TransactionRenderer {

    private final TransactionsAdapter adapter;
    private final List<Transaction> transactions = new ArrayList<>();

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
    }

    public TransactionRenderer(
            Context context,
            RecyclerView recyclerView,
            OnItemClickListener listener
    ) {
        setupRecyclerView(context, recyclerView);
        this.adapter = new TransactionsAdapter(transactions, listener::onClick);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Render lại danh sách giao dịch
     */
    public void render(List<Transaction> newList) {
        transactions.clear();

        if (newList != null) {
            transactions.addAll(newList);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Cấu hình RecyclerView cho danh sách giao dịch
     */
    private void setupRecyclerView(Context context, RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}