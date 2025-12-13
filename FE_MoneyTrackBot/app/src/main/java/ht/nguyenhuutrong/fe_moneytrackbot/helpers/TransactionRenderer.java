package ht.nguyenhuutrong.fe_moneytrackbot.helpers;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.adapters.TransactionsAdapter;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;

public class TransactionRenderer {
    private final TransactionsAdapter adapter;
    private final List<Transaction> transactionList = new ArrayList<>();

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
    }

    public TransactionRenderer(Context context, RecyclerView recyclerView, OnItemClickListener listener) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new TransactionsAdapter(transactionList, listener::onClick);
        recyclerView.setAdapter(adapter);
    }

    public void render(List<Transaction> newList) {
        transactionList.clear();
        if (newList != null) {
            transactionList.addAll(newList);
        }
        adapter.notifyDataSetChanged();
    }
}