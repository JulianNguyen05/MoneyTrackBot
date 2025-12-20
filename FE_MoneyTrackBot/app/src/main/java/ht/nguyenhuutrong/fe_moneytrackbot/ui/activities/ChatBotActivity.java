package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
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

/**
 * Màn hình ChatBot
 * - Hiển thị hội thoại
 * - Gửi tin nhắn tới AI
 * - Điều hướng ngược về tab Giao dịch
 */
public class ChatBotActivity extends AppCompatActivity {

    private ChatViewModel viewModel;
    private ChatRenderer renderer;

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ConstraintLayout layoutInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initViews();
        setupNavigation();
        setupViewModel();
        setupKeyboardHandling();
    }

    /* ===================== INIT ===================== */

    private void initViews() {
        recyclerChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        layoutInput = findViewById(R.id.layout_input);

        ImageButton btnSend = findViewById(R.id.btn_send);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> navigateToTransactions());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    /* ===================== NAVIGATION ===================== */

    private void setupNavigation() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToTransactions();
                    }
                }
        );
    }

    /* ===================== VIEWMODEL ===================== */

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        renderer = new ChatRenderer(this, recyclerChat);

        viewModel.getMessageList().observe(this, messages -> {
            if (messages != null) {
                renderer.render(messages);
                scrollToBottom();
            }
        });
    }

    /* ===================== KEYBOARD ===================== */

    /**
     * Tránh bàn phím che ô nhập tin nhắn
     */
    private void setupKeyboardHandling() {
        View rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            layoutInput.setPadding(0, 0, 0, imeHeight);
            return insets;
        });

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                recyclerChat.postDelayed(this::scrollToBottom, 200);
            }
        });
    }

    /* ===================== ACTION ===================== */

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        viewModel.sendUserMessage(text);
        etMessage.setText("");
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (recyclerChat.getAdapter() == null) return;

        int count = recyclerChat.getAdapter().getItemCount();
        if (count > 0) {
            recyclerChat.smoothScrollToPosition(count - 1);
        }
    }

    /* ===================== NAVIGATE ===================== */

    /**
     * Quay về MainActivity và mở tab Giao dịch
     */
    private void navigateToTransactions() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        intent.putExtra("NAVIGATE_TO", "TRANSACTIONS");

        startActivity(intent);
        finish();
    }
}