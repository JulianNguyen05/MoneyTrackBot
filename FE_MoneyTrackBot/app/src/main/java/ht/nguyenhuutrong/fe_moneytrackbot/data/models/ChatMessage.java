package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ChatMessage
 * --------------------------------------------------
 * Đại diện cho một tin nhắn trong cuộc trò chuyện
 * giữa người dùng và chatbot.
 */
public class ChatMessage {

    private String message;

    /**
     * true  → tin nhắn do người dùng gửi
     * false → tin nhắn do chatbot gửi
     */
    private boolean isSentByMe;

    /**
     * Thời gian gửi tin nhắn (HH:mm)
     */
    private String timestamp;

    /**
     * Khởi tạo tin nhắn mới với thời gian hiện tại.
     */
    public ChatMessage(String message, boolean isSentByMe) {
        this.message = message;
        this.isSentByMe = isSentByMe;
        this.timestamp = getCurrentTime();
    }

    // ===== Getters & Setters =====

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSentByMe() {
        return isSentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        isSentByMe = sentByMe;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // ===== Helpers =====

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date());
    }
}