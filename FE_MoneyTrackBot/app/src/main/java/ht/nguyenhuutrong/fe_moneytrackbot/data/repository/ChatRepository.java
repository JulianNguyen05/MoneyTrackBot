package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;

import org.json.JSONObject;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository chịu trách nhiệm giao tiếp với Chatbot API
 * - Gửi tin nhắn
 * - Nhận phản hồi từ AI
 * - Parse lỗi từ server
 */
public class ChatRepository {

    private final Context context;

    public ChatRepository(Context context) {
        this.context = context;
    }

    /* ===================== CALLBACK ===================== */

    public interface ChatCallback {
        void onSuccess(String botReply);
        void onError(String errorMessage);
    }

    /* ===================== API ===================== */

    /**
     * Gửi tin nhắn tới Chatbot và nhận phản hồi
     */
    public void sendMessage(String message, ChatCallback callback) {
        ChatRequest request = new ChatRequest(message);

        RetrofitClient.getChatbotService(context)
                .chatWithBot(request)
                .enqueue(new Callback<ChatResponse>() {
                    @Override
                    public void onResponse(
                            Call<ChatResponse> call,
                            Response<ChatResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body().getReply());
                        } else {
                            callback.onError(parseError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatResponse> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== ERROR HANDLING ===================== */

    /**
     * Parse lỗi trả về từ server (JSON)
     * Server có thể trả về:
     * - reply
     * - detail
     */
    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return "Lỗi không xác định từ Server";
            }

            String errorJson = response.errorBody().string();
            JSONObject json = new JSONObject(errorJson);

            if (json.has("reply")) {
                return json.getString("reply");
            }

            if (json.has("detail")) {
                return json.getString("detail");
            }

            return "AI không thể xử lý yêu cầu này.";

        } catch (Exception e) {
            return "Lỗi Server: " + response.code();
        }
    }
}