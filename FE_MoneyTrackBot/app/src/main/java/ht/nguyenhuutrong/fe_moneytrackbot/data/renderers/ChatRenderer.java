package ht.nguyenhuutrong.fe_moneytrackbot.data.renderers;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatMessage;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters.ChatAdapter;

/**
 * ChatRenderer
 * ----------------------------------------
 * Quản lý hiển thị danh sách tin nhắn chat
 * và tự động cuộn về tin nhắn mới nhất.
 */
public class ChatRenderer {

    private final RecyclerView recyclerView;
    private final ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    public ChatRenderer(Context context, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        setupRecyclerView(context);
        this.adapter = new ChatAdapter(messages);
        this.recyclerView.setAdapter(adapter);
    }

    /**
     * Cập nhật danh sách tin nhắn và render lại UI
     */
    public void render(List<ChatMessage> newMessages) {
        if (newMessages == null) return;

        messages.clear();
        messages.addAll(newMessages);
        adapter.notifyDataSetChanged();

        scrollToBottom();
    }

    /**
     * Cấu hình RecyclerView cho màn hình chat
     */
    private void setupRecyclerView(Context context) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true); // Tin nhắn mới luôn ở dưới
        recyclerView.setLayoutManager(layoutManager);
    }

    /**
     * Tự động cuộn về tin nhắn mới nhất
     */
    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            recyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
}