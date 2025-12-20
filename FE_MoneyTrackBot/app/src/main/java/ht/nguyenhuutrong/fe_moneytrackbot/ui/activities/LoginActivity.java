package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo ViewModel (MVVM)
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Nếu đã đăng nhập (có token) → đi thẳng vào Main
        if (viewModel.isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        observeViewModel();
        setupActions();
    }

    private void initViews() {
        etUsername = findViewById(R.id.editTextUsername);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tvRegister = findViewById(R.id.textViewRegister);
    }

    private void setupActions() {
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            viewModel.login(username, password);
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void observeViewModel() {
        // Đăng nhập thành công → chuyển màn hình
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });

        // Hiển thị lỗi từ ViewModel
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Trạng thái loading → tránh spam click
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading);
            btnLogin.setText(isLoading ? "Đang xử lý..." : "Đăng nhập");
        });
    }

    /**
     * Điều hướng sang MainActivity và kết thúc LoginActivity
     */
    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}