package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

/**
 * ChatResponse
 * --------------------------------------------------
 * Response model nhận phản hồi từ Chatbot API.
 */
public class ChatResponse {

    /**
     * Nội dung phản hồi từ chatbot.
     */
    private String reply;

    // ===== Getter =====

    public String getReply() {
        return reply;
    }
}