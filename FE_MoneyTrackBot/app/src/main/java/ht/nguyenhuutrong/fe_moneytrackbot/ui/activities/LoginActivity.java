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

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Init ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 2. Check đăng nhập (Nếu đã có token thì đi tiếp luôn)
        if (loginViewModel.isUserLoggedIn()) {
            startMainActivity();
            return;
        }

        initViews();
        setupObservers();

        // 3. Xử lý sự kiện Click
        buttonLogin.setOnClickListener(v -> {
            String user = editTextUsername.getText().toString().trim();
            String pass = editTextPassword.getText().toString().trim();
            loginViewModel.login(user, pass);
        });

        textViewRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
    }

    private void setupObservers() {
        // Lắng nghe: Đăng nhập thành công -> Chuyển màn hình
        loginViewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        });

        // Lắng nghe: Có lỗi -> Hiện Toast
        loginViewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // (Optional) Lắng nghe loading để disable nút login tránh spam click
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            buttonLogin.setEnabled(!isLoading);
            buttonLogin.setText(isLoading ? "Đang xử lý..." : "Đăng nhập");
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}