package ht.nguyenhuutrong.fe_moneytrackbot.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.repository.CategoryRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.repository.WalletRepository;

public class HomeViewModel extends AndroidViewModel {

    private final WalletRepository walletRepo;
    private final CategoryRepository categoryRepo;

    // LiveData: Fragment sẽ lắng nghe các biến này
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        walletRepo = new WalletRepository(application);
        categoryRepo = new CategoryRepository(application);
    }

    // --- Getters cho Fragment quan sát (Observe) ---
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- CÁC HÀM XỬ LÝ DỮ LIỆU ---

    // 1. Tải Ví
    public void loadWallets() {
        walletRepo.getWallets(new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(List<Wallet> data) {
                wallets.setValue(data);
            }
            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // 2. Tải Danh mục
    public void loadCategories() {
        categoryRepo.getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(data);
            }
            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // 3. Các thao tác Thêm/Sửa/Xóa Ví (🔥 ĐÃ SỬA: Dùng Callback mới để bắt lỗi)

    public void createWallet(String name, double balance) {
        walletRepo.createWallet(name, balance, new WalletRepository.WalletActionCallback() {
            @Override
            public void onSuccess() {
                loadWallets(); // Reload lại list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message); // Báo lỗi
            }
        });
    }

    public void updateWallet(Wallet wallet) {
        walletRepo.updateWallet(wallet, new WalletRepository.WalletActionCallback() {
            @Override
            public void onSuccess() {
                loadWallets();
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void deleteWallet(int id) {
        walletRepo.deleteWallet(id, new WalletRepository.WalletActionCallback() {
            @Override
            public void onSuccess() {
                loadWallets();
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // 4. Thêm Danh mục
    public void createCategory(String name, String type) {
        categoryRepo.createCategory(name, type, new CategoryRepository.CreateCallback() {
            @Override
            public void onSuccess() {
                loadCategories();
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }
}