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

    // LiveData: Fragment sẽ lắng nghe 2 biến này
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

    // --- Các hàm xử lý dữ liệu ---

    // 1. Tải Ví
    public void loadWallets() {
        walletRepo.getWallets(new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(List<Wallet> data) {
                wallets.setValue(data); // Bắn dữ liệu mới về cho Fragment
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
                // Có thể log lỗi
            }
        });
    }

    // 3. Các thao tác Thêm/Sửa/Xóa (Sau khi xong thì tự reload lại list)
    public void createWallet(String name, double balance) {
        walletRepo.createWallet(name, balance, this::loadWallets);
    }

    public void updateWallet(Wallet wallet) {
        walletRepo.updateWallet(wallet, this::loadWallets);
    }

    public void deleteWallet(int id) {
        walletRepo.deleteWallet(id, this::loadWallets);
    }

    public void createCategory(String name, String type) {
        categoryRepo.createCategory(name, type, this::loadCategories);
    }
}