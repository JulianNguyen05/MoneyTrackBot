package ht.nguyenhuutrong.fe_moneytrackbot.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.utils.MainNavigationHelper;
import ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private MainNavigationHelper navigationHelper;

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

        // 2. Setup Navigation Helper (🔥 FIX QUAN TRỌNG)
        navigationHelper = new MainNavigationHelper(
                this, // context để startActivity
                getSupportFragmentManager(),
                R.id.fragment_container
        );

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 3. Handle bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item ->
                navigationHelper.onItemSelected(item.getItemId())
        );

        // 4. Load Home mặc định
        if (savedInstanceState == null) {
            navigationHelper.loadDefaultFragment();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
