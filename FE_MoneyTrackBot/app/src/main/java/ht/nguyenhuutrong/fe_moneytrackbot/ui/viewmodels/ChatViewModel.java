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

/**
 * ViewModel quản lý dữ liệu chat giữa người dùng và AI.
 * - Giữ danh sách tin nhắn.
 * - Gửi tin nhắn user và nhận phản hồi từ repository.
 */
public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messageList = new MutableLiveData<>();
    private final List<ChatMessage> currentMessages = new ArrayList<>(); // Danh sách tạm để thao tác

    public ChatViewModel(@NonNull Application application) {
        super(application);
        repository = new ChatRepository(application);

        // Tin nhắn chào mừng mặc định khi mở chat
        addMessage("Xin chào! Tôi là trợ lý tài chính AI. Bạn cần giúp gì? (Ví dụ: 'Ăn sáng 30k')", false);
    }

    /**
     * Trả về LiveData để UI quan sát và cập nhật tự động.
     */
    public LiveData<List<ChatMessage>> getMessageList() {
        return messageList;
    }

    /**
     * Gửi tin nhắn từ người dùng:
     * - Hiển thị ngay lập tức trong UI.
     * - Gọi repository để nhận phản hồi AI.
     */
    public void sendUserMessage(String text) {
        if (text.isEmpty()) return;

        addMessage(text, true); // Hiển thị tin nhắn user

        repository.sendMessage(text, new ChatRepository.ChatCallback() {
            @Override
            public void onSuccess(String botReply) {
                addMessage(botReply, false); // Hiển thị phản hồi AI
            }

            @Override
            public void onError(String errorMessage) {
                addMessage(errorMessage, false); // Hiển thị lỗi
            }
        });
    }

    /**
     * Thêm tin nhắn vào danh sách và cập nhật LiveData để UI vẽ lại.
     *
     * @param text Nội dung tin nhắn
     * @param isUser true nếu là tin nhắn người dùng, false nếu là AI
     */
    private void addMessage(String text, boolean isUser) {
        currentMessages.add(new ChatMessage(text, isUser));
        messageList.setValue(new ArrayList<>(currentMessages));
    }
}