package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.CategoryRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.TransactionRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.WalletRepository;

public class HomeViewModel extends AndroidViewModel {

    private final WalletRepository walletRepo;
    private final CategoryRepository categoryRepo;

    // LiveData: Fragment sẽ lắng nghe các biến này
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MutableLiveData<CashFlowResponse> cashFlowData = new MutableLiveData<>();
    public MutableLiveData<String> errorData = new MutableLiveData<>();

    private TransactionRepository repository;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        walletRepo = new WalletRepository(application);
        categoryRepo = new CategoryRepository(application);
        repository = new TransactionRepository(application);
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

    // 3. Các thao tác Thêm/Sửa/Xóa Ví

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

    // 4. Các thao tác Thêm/Sửa/Xóa Danh mục (🔥 ĐÃ CẬP NHẬT ĐẦY ĐỦ)

    public void createCategory(String name, String type) {
        // Sử dụng CategoryActionCallback chung
        categoryRepo.createCategory(name, type, new CategoryRepository.CategoryActionCallback() {
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

    // 🔥 Hàm Mới: Cập nhật danh mục
    public void updateCategory(Category category) {
        categoryRepo.updateCategory(category, new CategoryRepository.CategoryActionCallback() {
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

    // 🔥 Hàm Mới: Xóa danh mục
    public void deleteCategory(int id) {
        categoryRepo.deleteCategory(id, new CategoryRepository.CategoryActionCallback() {
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

    // 5. Báo cáo dòng tiền
    public void loadCashFlow(String startDate, String endDate) {
        repository.getCashFlowReport(startDate, endDate, new TransactionRepository.CashFlowCallback() {
            @Override
            public void onSuccess(CashFlowResponse data) {
                cashFlowData.postValue(data);
            }

            @Override
            public void onError(String message) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    public void loadCurrentMonthData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Ngày cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = sdf.format(calendar.getTime());

        // Ngày đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = sdf.format(calendar.getTime());

        loadCashFlow(startDate, endDate);
    }
}