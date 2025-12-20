package ht.nguyenhuutrong.fe_moneytrackbot.data.api.services;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * ChatbotService
 * --------------------------------------------------
 * Cung cấp API giao tiếp với Chatbot AI
 */
public interface ChatbotService {

    /**
     * Gửi tin nhắn đến chatbot và nhận phản hồi
     *
     * @param request Nội dung tin nhắn và metadata gửi lên server
     * @return Phản hồi từ chatbot
     */
    @POST("api/chatbot/")
    Call<ChatResponse> chatWithBot(@Body ChatRequest request);
}