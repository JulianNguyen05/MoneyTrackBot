package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViewModel();
        initViews();
        setupObservers();
        setupActions();
    }

    /**
     * Khởi tạo ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
    }

    /**
     * Ánh xạ View
     */
    private void initViews() {
        etUsername = findViewById(R.id.editTextUsernameRegister);
        etEmail = findViewById(R.id.editTextEmailRegister);
        etPassword = findViewById(R.id.editTextPasswordRegister);
        btnRegister = findViewById(R.id.buttonRegister);
        tvBackToLogin = findViewById(R.id.textViewBackToLogin);
    }

    /**
     * Gán sự kiện click
     */
    private void setupActions() {
        btnRegister.setOnClickListener(v -> register());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    /**
     * Lắng nghe trạng thái từ ViewModel
     */
    private void setupObservers() {
        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(
                        this,
                        "Đăng ký thành công! Vui lòng đăng nhập.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            btnRegister.setEnabled(!isLoading);
            btnRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký");
        });
    }

    /**
     * Thực hiện đăng ký
     */
    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        viewModel.register(username, email, password);
    }
}