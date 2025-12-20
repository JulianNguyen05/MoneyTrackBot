package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent; // Import mới
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback; // Import mới
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
        setContentView(R.layout.activity_chatbot);

        // 1. Ánh xạ view
        recyclerChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        ImageButton btnSend = findViewById(R.id.btn_send);
        layoutInput = findViewById(R.id.layout_input);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        // --- 🔥 SỬA ĐỔI 1: Xử lý nút Back trên Toolbar ---
        toolbar.setNavigationOnClickListener(v -> navigateToTransactions());

        // --- 🔥 SỬA ĐỔI 2: Xử lý nút Back vật lý của điện thoại ---
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToTransactions();
            }
        });

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

    // --- 🔥 HÀM MỚI: Điều hướng về MainActivity và chọn Tab Giao dịch ---
    private void navigateToTransactions() {
        Intent intent = new Intent(this, MainActivity.class);
        // FLAG_ACTIVITY_CLEAR_TOP: Xóa các activity nằm trên MainActivity (nếu có)
        // FLAG_ACTIVITY_SINGLE_TOP: Nếu MainActivity đang mở, dùng lại nó chứ không tạo mới
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Gửi kèm tín hiệu để MainActivity biết cần mở tab nào
        intent.putExtra("NAVIGATE_TO", "TRANSACTIONS");

        startActivity(intent);
        finish();
    }
}