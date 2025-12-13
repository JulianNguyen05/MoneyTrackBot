package ht.nguyenhuutrong.fe_moneytrackbot.helpers;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.adapters.ChatAdapter;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatMessage;

public class ChatRenderer {

    private final RecyclerView recyclerView;
    private final ChatAdapter adapter;
    private final List<ChatMessage> displayList = new ArrayList<>();

    public ChatRenderer(Context context, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        // Setup Adapter
        this.adapter = new ChatAdapter(displayList);
        this.recyclerView.setAdapter(adapter);

        // Setup LayoutManager (StackFromEnd = true để tin nhắn luôn ở dưới)
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        this.recyclerView.setLayoutManager(layoutManager);
    }

    public void render(List<ChatMessage> newMessages) {
        if (newMessages == null) return;

        // Cập nhật dữ liệu
        displayList.clear();
        displayList.addAll(newMessages);
        adapter.notifyDataSetChanged();

        // Tự động cuộn xuống tin nhắn mới nhất
        if (!displayList.isEmpty()) {
            recyclerView.smoothScrollToPosition(displayList.size() - 1);
        }
    }
}