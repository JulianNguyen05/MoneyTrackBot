package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.CategoryRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.TransactionRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.WalletRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionViewModel extends AndroidViewModel {

    // --- REPOSITORIES (Cho các thao tác cơ bản) ---
    private final TransactionRepository transactionRepo;
    private final WalletRepository walletRepo;
    private final CategoryRepository categoryRepo;

    // --- LIVEDATA (Dữ liệu UI quan sát) ---
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<CashFlowResponse> cashFlow = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    // --- BỘ LỌC (FILTER STATE) ---
    // Ví đang chọn (null = Tất cả)
    public MutableLiveData<Wallet> selectedWallet = new MutableLiveData<>(null);
    // Ngày bắt đầu và kết thúc (yyyy-MM-dd)
    public MutableLiveData<String> currentStartDate = new MutableLiveData<>();
    public MutableLiveData<String> currentEndDate = new MutableLiveData<>();

    // --- CONSTRUCTOR ---
    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
        walletRepo = new WalletRepository(application);
        categoryRepo = new CategoryRepository(application);
    }

    // --- GETTERS ---
    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<CashFlowResponse> getCashFlow() { return cashFlow; }
    public LiveData<String> getMessage() { return message; }

    // --- LOAD DATA CHÍNH (Giao dịch + Tổng kết) ---
    // Hàm này sẽ tự động lấy theo bộ lọc (Ngày & Ví)
    public void loadData() {
        String start = currentStartDate.getValue();
        String end = currentEndDate.getValue();
        Integer walletId = selectedWallet.getValue() != null ? selectedWallet.getValue().getId() : null;

        // 1. Gọi API lấy danh sách giao dịch (Có lọc)
        // Chúng ta gọi trực tiếp Retrofit ở đây vì Repository cũ của bạn chưa hỗ trợ tham số lọc
        RetrofitClient.getTransactionService(getApplication())
                .getTransactions(null, walletId, start, end)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        if (response.isSuccessful()) {
                            transactions.postValue(response.body());
                        } else {
                            message.postValue("Lỗi tải giao dịch: " + response.message());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        message.postValue("Lỗi kết nối: " + t.getMessage());
                    }
                });

        // 2. Gọi API lấy tổng tiền (CashFlow)
        RetrofitClient.getTransactionService(getApplication())
                .getCashFlow(start, end, walletId)
                .enqueue(new Callback<CashFlowResponse>() {
                    @Override
                    public void onResponse(Call<CashFlowResponse> call, Response<CashFlowResponse> response) {
                        if (response.isSuccessful()) {
                            cashFlow.postValue(response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<CashFlowResponse> call, Throwable t) {
                        // Không cần báo lỗi cashflow để tránh spam toast
                    }
                });
    }

    // --- CÁC HÀM SETTER CHO BỘ LỌC ---
    public void setDateRange(String start, String end) {
        currentStartDate.setValue(start);
        currentEndDate.setValue(end);
        loadData(); // Tự động tải lại dữ liệu khi đổi ngày
    }

    public void setWallet(Wallet wallet) {
        selectedWallet.setValue(wallet);
        loadData(); // Tự động tải lại dữ liệu khi đổi ví
    }

    // --- LOAD DANH MỤC KHÁC ---
    public void loadWallets() {
        walletRepo.getWallets(new WalletRepository.WalletCallback() {
            @Override public void onSuccess(List<Wallet> data) { wallets.setValue(data); }
            @Override public void onError(String msg) {}
        });
    }

    public void loadCategories() {
        categoryRepo.getCategories(new CategoryRepository.CategoryCallback() {
            @Override public void onSuccess(List<Category> data) { categories.setValue(data); }
            @Override public void onError(String msg) {}
        });
    }

    // --- CRUD GIAO DỊCH (Giữ nguyên dùng Repository) ---
    public void createTransaction(Transaction t) {
        transactionRepo.createTransaction(t, new TransactionRepository.ApiCallback<Transaction>() {
            @Override public void onSuccess(Transaction result) {
                message.setValue("Thêm thành công");
                loadData(); // Refresh lại danh sách sau khi thêm
            }
            @Override public void onError(String msg) { message.setValue(msg); }
        });
    }

    public void updateTransaction(int id, Transaction t) {
        transactionRepo.updateTransaction(id, t, new TransactionRepository.ApiCallback<Transaction>() {
            @Override public void onSuccess(Transaction result) {
                message.setValue("Cập nhật thành công");
                loadData();
            }
            @Override public void onError(String msg) { message.setValue(msg); }
        });
    }

    public void deleteTransaction(int id) {
        transactionRepo.deleteTransaction(id, new TransactionRepository.ApiCallback<Void>() {
            @Override public void onSuccess(Void result) {
                message.setValue("Đã xóa");
                loadData();
            }
            @Override public void onError(String msg) { message.setValue(msg); }
        });
    }
}