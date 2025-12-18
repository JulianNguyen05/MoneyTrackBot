package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.renderers.ChatRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.ChatViewModel;

public class ChatBotActivity extends AppCompatActivity {

    private ChatViewModel viewModel;
    private ChatRenderer renderer;
    private EditText etMessage;
    private RecyclerView recyclerChat;
    private ConstraintLayout layoutInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot); // dùng lại layout cũ OK

        // 1. Ánh xạ view
        recyclerChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        ImageButton btnSend = findViewById(R.id.btn_send);
        layoutInput = findViewById(R.id.layout_input);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // 2. Toolbar back → thoát Activity (về Home)
        toolbar.setNavigationOnClickListener(v -> finish());

        // 3. ViewModel & Renderer
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        renderer = new ChatRenderer(this, recyclerChat);

        viewModel.getMessageList().observe(this, messages -> {
            if (messages != null) {
                renderer.render(messages);
                scrollChatToBottom();
            }
        });

        // 4. Fix keyboard che input
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            layoutInput.setPadding(0, 0, 0, imeHeight);
            return insets;
        });

        // 5. Scroll khi focus input
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                recyclerChat.postDelayed(this::scrollChatToBottom, 200);
            }
        });

        // 6. Gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendUserMessage(text);
                etMessage.setText("");
                scrollChatToBottom();
            }
        });
    }

    private void scrollChatToBottom() {
        if (recyclerChat.getAdapter() != null) {
            int count = recyclerChat.getAdapter().getItemCount();
            if (count > 0) {
                recyclerChat.smoothScrollToPosition(count - 1);
            }
        }
    }
}
