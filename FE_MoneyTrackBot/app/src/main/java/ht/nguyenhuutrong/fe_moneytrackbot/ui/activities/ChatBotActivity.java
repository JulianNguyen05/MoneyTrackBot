package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.data.renderers.ChatRenderer;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.ChatViewModel;

public class ChatBotActivity extends AppCompatActivity {

    private ChatViewModel viewModel;
    private ChatRenderer renderer;

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ConstraintLayout layoutInput;

    // 1. Khai báo biến launcher để hứng kết quả giọng nói
    private ActivityResultLauncher<Intent> voiceLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // 2. Cài đặt launcher trước khi initViews
        setupVoiceLauncher();

        initViews();
        setupNavigation();
        setupViewModel();
        setupKeyboardHandling();
    }

    /* ===================== VOICE SETUP ===================== */

    private void setupVoiceLauncher() {
        voiceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Lấy kết quả giọng nói trả về
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                        if (matches != null && !matches.isEmpty()) {
                            String spokenText = matches.get(0);

                            // Nối thêm vào nội dung cũ (nếu muốn) hoặc ghi đè
                            // Ở đây mình chọn ghi đè hoặc thêm vào nếu đang nhập dở
                            String currentText = etMessage.getText().toString();
                            if (!currentText.isEmpty()) {
                                etMessage.setText(currentText + " " + spokenText);
                            } else {
                                etMessage.setText(spokenText);
                            }

                            // Đưa con trỏ về cuối câu
                            etMessage.setSelection(etMessage.getText().length());
                        }
                    }
                }
        );
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // Tự động theo ngôn ngữ máy
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Đang nghe... Nói lệnh của bạn");

        try {
            voiceLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Thiết bị không hỗ trợ nhận diện giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    /* ===================== INIT ===================== */

    private void initViews() {
        recyclerChat = findViewById(R.id.recycler_chat);
        etMessage = findViewById(R.id.et_message);
        layoutInput = findViewById(R.id.layout_input);

        ImageButton btnSend = findViewById(R.id.btn_send);
        // 3. Ánh xạ nút Voice (Đảm bảo ID này khớp với file XML layout của bạn)
        ImageButton btnVoice = findViewById(R.id.btn_voice);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(v -> navigateToTransactions());

        btnSend.setOnClickListener(v -> sendMessage());

        // 4. Gán sự kiện click cho nút Voice
        if (btnVoice != null) {
            btnVoice.setOnClickListener(v -> startVoiceRecognition());
        }
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