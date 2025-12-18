package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;
import org.json.JSONObject;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final Context context;

    public ChatRepository(Context context) {
        this.context = context;
    }

    public interface ChatCallback {
        void onSuccess(String botReply);
        void onError(String errorMessage);
    }

    public void sendMessage(String message, ChatCallback callback) {
        ChatRequest request = new ChatRequest(message);

        // 🔥 CẬP NHẬT: Gọi qua getChatbotService() thay vì getApiService()
        RetrofitClient.getChatbotService(context).chatWithBot(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Thành công: Trả về câu trả lời của Bot
                    callback.onSuccess(response.body().getReply());
                } else {
                    // Thất bại: Xử lý lỗi JSON phức tạp
                    callback.onError(parseErrorBody(response));
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Hàm tách lỗi JSON (Giữ nguyên logic tốt này)
    private String parseErrorBody(Response<?> response) {
        try {
            String errorJson = response.errorBody().string();
            JSONObject jsonObject = new JSONObject(errorJson);

            if (jsonObject.has("reply")) {
                return jsonObject.getString("reply");
            } else if (jsonObject.has("detail")) {
                return jsonObject.getString("detail");
            } else {
                return "AI không thể xử lý yêu cầu này. Vui lòng kiểm tra lại.";
            }
        } catch (Exception e) {
            return "Lỗi Server: " + response.code();
        }
    }
}