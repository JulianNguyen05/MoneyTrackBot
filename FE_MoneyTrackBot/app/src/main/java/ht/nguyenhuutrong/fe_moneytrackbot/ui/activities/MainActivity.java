package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments.HomeFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments.SettingsFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.fragments.TransactionsFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Init ViewModel & check login
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if (!viewModel.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // 2. Init Views
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 3. Handle bottom navigation trực tiếp
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.nav_chatbot) {
                // Mở Activity ChatBot - Không dùng Fragment
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // 4. Load Home mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // Hàm load Fragment thay thế cho logic trong Helper
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}