package ht.nguyenhuutrong.fe_moneytrackbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.api.TokenManager;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.HomeFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.TransactionsFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.ChatBotFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Kiểm tra Login ---
        tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // --- Mặc định mở HomeFragment ---
        replaceFragment(new HomeFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // --- Bắt chọn menu bottom navigation ---
        // FIX: Replaced switch statement with if-else if
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.nav_chatbot) {
                selectedFragment = new ChatBotFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
