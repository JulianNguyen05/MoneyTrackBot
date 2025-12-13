package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatbotService {
    @POST("api/chatbot/")
    Call<ChatResponse> chatWithBot(@Body ChatRequest request);
}