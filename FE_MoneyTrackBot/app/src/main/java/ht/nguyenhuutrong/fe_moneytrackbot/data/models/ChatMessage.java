package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

public class ChatMessage {
    private String message;
    private boolean isSentByMe; // true = Người dùng gửi, false = Bot gửi
    private String timestamp;   // (Tùy chọn) Lưu thời gian: "10:30"

    // Constructor đơn giản
    public ChatMessage(String message, boolean isSentByMe) {
        this.message = message;
        this.isSentByMe = isSentByMe;
        this.timestamp = getCurrentTime(); // Bạn có thể viết hàm lấy giờ hiện tại
    }

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

    // Hàm lấy giờ hiện tại đơn giản (Ví dụ)
    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}