package ht.nguyenhuutrong.fe_moneytrackbot.helpers;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ht.nguyenhuutrong.fe_moneytrackbot.R;
import ht.nguyenhuutrong.fe_moneytrackbot.activities.ChatBotActivity;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.HomeFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.SettingsFragment;
import ht.nguyenhuutrong.fe_moneytrackbot.fragments.TransactionsFragment;

public class MainNavigationHelper {

    private final FragmentManager fragmentManager;
    private final int containerId;
    private final Context context;

    public MainNavigationHelper(Context context,
                                FragmentManager fragmentManager,
                                int containerId) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }

    public boolean onItemSelected(int itemId) {

        Fragment selectedFragment = null;

        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();

        } else if (itemId == R.id.nav_transactions) {
            selectedFragment = new TransactionsFragment();

        } else if (itemId == R.id.nav_chatbot) {
            // ✅ MỞ ACTIVITY – KHÔNG DÙNG FRAGMENT
            Intent intent = new Intent(context, ChatBotActivity.class);
            context.startActivity(intent);
            return true;

        } else if (itemId == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
            return true;
        }

        return false;
    }

    public void loadDefaultFragment() {
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit();
    }
}
