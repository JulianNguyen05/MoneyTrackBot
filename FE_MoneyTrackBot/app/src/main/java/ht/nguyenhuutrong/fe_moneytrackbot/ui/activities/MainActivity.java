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

        // Khởi tạo ViewModel và kiểm tra trạng thái đăng nhập
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        if (!viewModel.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupBottomNavigation();

        // Load Home Fragment mặc định lần đầu tiên mở app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Xử lý điều hướng nếu có Intent gửi đến (Ví dụ: từ Chatbot quay về)
        handleNavigationIntent(getIntent());
    }

    /**
     * Hỗ trợ chế độ SingleTop (launchMode):
     * Khi Activity đã tồn tại trong stack và được gọi lại, onCreate sẽ không chạy,
     * thay vào đó hệ thống gọi onNewIntent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật Intent mới nhất cho Activity
        handleNavigationIntent(intent);
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Đặc biệt: Mở Chatbot Activity (Không phải Fragment)
            if (itemId == R.id.nav_chatbot) {
                Intent intent = new Intent(this, ChatBotActivity.class);
                startActivity(intent);
                return false; // Trả về false để icon Chatbot không bị highlight (giữ trạng thái tab cũ)
            }

            Fragment selectedFragment = null;
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_transactions) {
                selectedFragment = new TransactionsFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    /**
     * Kiểm tra Intent để tự động chuyển Tab.
     * Được dùng khi ChatBotActivity finish() và yêu cầu quay về màn hình Giao dịch.
     */
    private void handleNavigationIntent(Intent intent) {
        if (intent != null && "TRANSACTIONS".equals(intent.getStringExtra("NAVIGATE_TO"))) {
            // Tự động chọn tab Giao dịch -> Kích hoạt listener -> Load TransactionsFragment
            bottomNavigationView.setSelectedItemId(R.id.nav_transactions);
        }
    }

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