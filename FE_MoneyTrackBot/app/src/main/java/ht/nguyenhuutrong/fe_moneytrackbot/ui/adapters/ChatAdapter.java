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

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * Quyết định layout dựa vào người gửi
     */
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentByMe()
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(
                    R.layout.item_message_sent,
                    parent,
                    false
            );
            return new SentViewHolder(view);
        }

        View view = inflater.inflate(
                R.layout.item_message_received,
                parent,
                false
        );
        return new ReceivedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {
        ChatMessage message = messages.get(position);

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ======================= VIEW HOLDERS =======================

    /**
     * Tin nhắn người dùng gửi
     */
    static class SentViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final TextView tvTime;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.text_message_body);
            tvTime = itemView.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            bindTime(message);
        }

        private void bindTime(ChatMessage message) {
            if (message.getTimestamp() != null) {
                tvTime.setText(message.getTimestamp());
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Tin nhắn bot trả về
     */
    static class ReceivedViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final TextView tvTime;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.text_message_body);
            tvTime = itemView.findViewById(R.id.text_message_time);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            bindTime(message);
        }

        private void bindTime(ChatMessage message) {
            if (message.getTimestamp() != null) {
                tvTime.setText(message.getTimestamp());
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setVisibility(View.GONE);
            }
        }
    }
}