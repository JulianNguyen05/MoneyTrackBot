package ht.nguyenhuutrong.fe_moneytrackbot.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private TextView textViewBackToLogin;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // 2. Init Views
        editTextUsername = findViewById(R.id.editTextUsernameRegister);
        editTextEmail = findViewById(R.id.editTextEmailRegister);
        editTextPassword = findViewById(R.id.editTextPasswordRegister);
        buttonRegister = findViewById(R.id.buttonRegister);

        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);

        // 3. Setup Observers (Lắng nghe kết quả)
        setupObservers();

        // 4. Handle Click
        buttonRegister.setOnClickListener(v -> {
            String user = editTextUsername.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String pass = editTextPassword.getText().toString().trim();

            viewModel.register(user, email, pass);
        });

        textViewBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupObservers() {
        // Thành công -> Báo Toast & Quay về
        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // Thất bại -> Báo lỗi
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // Loading -> Disable nút bấm
        viewModel.getIsLoading().observe(this, isLoading -> {
            buttonRegister.setEnabled(!isLoading);
            buttonRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký");
        });
    }
}