package ht.nguyenhuutrong.fe_moneytrackbot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatList;

    // Định nghĩa 2 loại view
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    // 🔥 QUAN TRỌNG: Hàm này quyết định dùng layout nào
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatList.get(position);
        if (message.isSentByMe()) {
            return VIEW_TYPE_SENT; // Người dùng gửi -> Bên phải
        } else {
            return VIEW_TYPE_RECEIVED; // Bot gửi -> Bên trái
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            // Nạp layout Gửi (Bên phải)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            // Nạp layout Nhận (Bên trái)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    // ================== VIEW HOLDERS ==================

    // 1. ViewHolder cho tin nhắn Gửi đi
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.text_message_body);
            tvTime = itemView.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            if (message.getTimestamp() != null) {
                tvTime.setText(message.getTimestamp());
                tvTime.setVisibility(View.VISIBLE);
            }
        }
    }

    // 2. ViewHolder cho tin nhắn Nhận về (Bot)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        // ImageView imgProfile; // Nếu muốn set avatar động thì ánh xạ thêm

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.text_message_body);
            tvTime = itemView.findViewById(R.id.text_message_time);
            // imgProfile = itemView.findViewById(R.id.image_message_profile);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            if (message.getTimestamp() != null) {
                tvTime.setText(message.getTimestamp());
                tvTime.setVisibility(View.VISIBLE);
            }
        }
    }
}