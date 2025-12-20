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

/**
 * ViewModel quản lý dữ liệu Home:
 * - Danh sách Wallet
 * - Danh sách Category
 * - Báo cáo CashFlow
 * - Thêm/Sửa/Xóa Wallet & Category
 */
public class HomeViewModel extends AndroidViewModel {

    private final WalletRepository walletRepo;
    private final CategoryRepository categoryRepo;
    private final TransactionRepository transactionRepo;

    // LiveData: Fragment sẽ quan sát và cập nhật UI
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<CashFlowResponse> cashFlowData = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        walletRepo = new WalletRepository(application);
        categoryRepo = new CategoryRepository(application);
        transactionRepo = new TransactionRepository(application);
    }

    // --- Getters để Fragment observe ---
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- LOAD DATA ---
    public void loadWallets() {
        walletRepo.getWallets(new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(List<Wallet> data) { wallets.setValue(data); }
            @Override
            public void onError(String message) { errorMessage.setValue(message); }
        });
    }

    public void loadCategories() {
        categoryRepo.getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<Category> data) { categories.setValue(data); }
            @Override
            public void onError(String message) { errorMessage.setValue(message); }
        });
    }

    // --- WALLET ACTIONS: Thêm / Sửa / Xóa ---
    public void createWallet(String name, double balance) {
        walletRepo.createWallet(name, balance, callbackReloadWallets());
    }

    public void updateWallet(Wallet wallet) {
        walletRepo.updateWallet(wallet, callbackReloadWallets());
    }

    public void deleteWallet(int id) {
        walletRepo.deleteWallet(id, callbackReloadWallets());
    }

    private WalletRepository.WalletActionCallback callbackReloadWallets() {
        return new WalletRepository.WalletActionCallback() {
            @Override public void onSuccess() { loadWallets(); }
            @Override public void onError(String message) { errorMessage.setValue(message); }
        };
    }

    // --- CATEGORY ACTIONS: Thêm / Sửa / Xóa ---
    public void createCategory(String name, String type) {
        categoryRepo.createCategory(name, type, callbackReloadCategories());
    }

    public void updateCategory(Category category) {
        categoryRepo.updateCategory(category, callbackReloadCategories());
    }

    public void deleteCategory(int id) {
        categoryRepo.deleteCategory(id, callbackReloadCategories());
    }

    private CategoryRepository.CategoryActionCallback callbackReloadCategories() {
        return new CategoryRepository.CategoryActionCallback() {
            @Override public void onSuccess() { loadCategories(); }
            @Override public void onError(String message) { errorMessage.setValue(message); }
        };
    }

    // --- CASH FLOW REPORT ---
    public void loadCashFlow(String startDate, String endDate) {
        transactionRepo.getCashFlowReport(startDate, endDate, new TransactionRepository.CashFlowCallback() {
            @Override public void onSuccess(CashFlowResponse data) { cashFlowData.postValue(data); }
            @Override public void onError(String message) { errorMessage.postValue(message); }
        });
    }

    /**
     * Load dữ liệu dòng tiền mặc định cho tháng hiện tại
     */
    public void loadCurrentMonthData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Ngày đầu tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String startDate = sdf.format(calendar.getTime());

        // Ngày cuối tháng
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endDate = sdf.format(calendar.getTime());

        loadCashFlow(startDate, endDate);
    }
}