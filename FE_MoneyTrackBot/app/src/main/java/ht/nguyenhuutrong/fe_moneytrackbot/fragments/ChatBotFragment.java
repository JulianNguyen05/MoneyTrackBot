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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.helpers.ChatRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.ChatViewModel;

public class ChatBotFragment extends Fragment {

    private ChatViewModel viewModel;
    private ChatRenderer renderer;
    private EditText etMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatbot, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init View Components
        RecyclerView recyclerChat = view.findViewById(R.id.recycler_chat);
        etMessage = view.findViewById(R.id.et_message);
        ImageButton btnSend = view.findViewById(R.id.btn_send);

        // 2. Init ViewModel & Renderer
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        renderer = new ChatRenderer(getContext(), recyclerChat);

        // 3. Setup Observer (Lắng nghe tin nhắn mới)
        viewModel.getMessageList().observe(getViewLifecycleOwner(), messages -> {
            renderer.render(messages);
        });

        // 4. Handle Send Button
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendUserMessage(text);
                etMessage.setText(""); // Xóa ô nhập sau khi gửi
            }
        });
    }
}