package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.ChatMessage;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.ChatRepository;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messageList = new MutableLiveData<>();
    private final List<ChatMessage> currentMessages = new ArrayList<>(); // List tạm để thao tác

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);

        // Tin nhắn chào mừng mặc định
        addMessage("Xin chào! Tôi là trợ lý tài chính AI. Bạn cần giúp gì? (Ví dụ: 'Ăn sáng 30k')", false);
    }

    public LiveData<List<ChatMessage>> getMessageList() {
        return messageList;
    }

    public void sendUserMessage(String text) {
        if (text.isEmpty()) return;

        // 1. Hiển thị tin nhắn User ngay lập tức
        addMessage(text, true);

        // 2. Gọi API
        repository.sendMessage(text, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(String botReply) {
                addMessage(botReply, false);
            }

            @Override
            public void onError(String errorMessage) {
                addMessage(errorMessage, false);
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        currentMessages.add(new ChatMessage(text, isUser));
        // Cập nhật LiveData để UI tự vẽ lại
        messageList.setValue(new ArrayList<>(currentMessages));
    }
}