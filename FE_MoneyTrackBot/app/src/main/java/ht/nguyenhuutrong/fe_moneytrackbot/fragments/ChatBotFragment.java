package ht.nguyenhuutrong.fe_moneytrackbot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.adapters.ChatAdapter;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatMessage;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotFragment extends Fragment {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageButton btnSend;

    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerChat = view.findViewById(R.id.recycler_chat);
        etMessage = view.findViewById(R.id.et_message);
        btnSend = view.findViewById(R.id.btn_send);

        // Khởi tạo danh sách tin nhắn
        messageList = new ArrayList<>();
        messageList.add(new ChatMessage("Xin chào! Tôi là trợ lý tài chính AI. Bạn cần giúp gì? (Ví dụ: 'Ăn sáng 30k')", false));

        adapter = new ChatAdapter(messageList);
        recyclerChat.setAdapter(adapter);

        // Cấu hình LayoutManager để luôn cuộn xuống dưới cùng
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // 1. Hiển thị tin nhắn của User lên màn hình ngay lập tức
        addMessageToChat(messageText, true);
        etMessage.setText(""); // Xóa ô nhập liệu

        // 2. Gọi API gửi tin nhắn lên Server
        callChatbotApi(messageText);
    }

    private void callChatbotApi(String message) {
        if (getContext() == null) return;

        // Tạo request
        ChatRequest request = new ChatRequest(message);

        // Gọi Retrofit
        RetrofitClient.getApiService(getContext()).chatWithBot(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!isAdded()) return; // Tránh crash nếu thoát màn hình

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Thành công: Hiển thị câu trả lời từ AI
                    String botReply = response.body().getReply();
                    addMessageToChat(botReply, false);
                } else {
                    // ❌ Lỗi từ Server (VD: 400 Bad Request do AI không hiểu)
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                if (!isAdded()) return;
                addMessageToChat("Lỗi kết nối: " + t.getMessage(), false);
            }
        });
    }

    // Hàm xử lý hiển thị tin nhắn lên giao diện
    private void addMessageToChat(String message, boolean isSentByMe) {
        messageList.add(new ChatMessage(message, isSentByMe));
        int position = messageList.size() - 1;
        adapter.notifyItemInserted(position);
        recyclerChat.smoothScrollToPosition(position); // Cuộn xuống tin nhắn mới nhất
    }

    // Hàm xử lý lỗi chi tiết từ Server (để đọc được thông báo JSON lỗi)
    private void handleErrorResponse(Response<?> response) {
        try {
            // Cố gắng đọc nội dung lỗi từ response.errorBody()
            String errorJson = response.errorBody().string();

            // Server của bạn trả về: {"reply": "Nội dung lỗi..."}
            // Chúng ta cần parse JSON này để lấy thông báo thân thiện
            JSONObject jsonObject = new JSONObject(errorJson);

            String errorMessage = "Đã xảy ra lỗi.";
            if (jsonObject.has("reply")) {
                errorMessage = jsonObject.getString("reply");
            } else if (jsonObject.has("detail")) {
                errorMessage = jsonObject.getString("detail");
            } else {
                // Trường hợp lỗi validation mặc định của Django (như hình bạn gửi)
                // Nó trả về dạng {"category": ["Invalid pk..."]} -> Rất khó đọc
                // Ta sẽ hiển thị thông báo chung
                errorMessage = "AI không thể xử lý yêu cầu này. Vui lòng kiểm tra lại thông tin ví hoặc danh mục.";
            }

            addMessageToChat(errorMessage, false);

        } catch (Exception e) {
            addMessageToChat("Lỗi Server: " + response.code(), false);
        }
    }
}