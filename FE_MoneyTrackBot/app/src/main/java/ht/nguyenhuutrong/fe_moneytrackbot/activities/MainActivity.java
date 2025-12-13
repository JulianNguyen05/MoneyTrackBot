package ht.nguyenhuutrong.fe_moneytrackbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.helpers.MainNavigationHelper;
import ht.nguyenhuutrong.fe_moneytrackbot.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private MainNavigationHelper navigationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Init ViewModel & Kiểm tra đăng nhập
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if (!viewModel.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // 2. Setup Navigation Helper
        navigationHelper = new MainNavigationHelper(getSupportFragmentManager(), R.id.fragment_container);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 3. Setup Logic chuyển tab
        bottomNavigationView.setOnItemSelectedListener(item ->
                navigationHelper.onItemSelected(item.getItemId())
        );

        // 4. Load mặc định Home nếu chưa có savedState (tránh load lại khi xoay màn hình)
        if (savedInstanceState == null) {
            navigationHelper.loadDefaultFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}