package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

/**
 * ChatRequest
 * --------------------------------------------------
 * Request model gửi nội dung tin nhắn lên Chatbot API.
 */
public class ChatRequest {

    private String message;

    /**
     * Khởi tạo request với nội dung tin nhắn của người dùng.
     */
    public ChatRequest(String message) {
        this.message = message;
    }

    // ===== Getter =====

    public String getMessage() {
        return message;
    }
}