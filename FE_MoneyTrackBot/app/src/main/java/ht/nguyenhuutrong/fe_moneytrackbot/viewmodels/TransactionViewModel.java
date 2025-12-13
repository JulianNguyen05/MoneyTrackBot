package ht.nguyenhuutrong.fe_moneytrackbot.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import ht.nguyenhuutrong.fe_moneytrackbot.repository.CategoryRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.repository.TransactionRepository;
import ht.nguyenhuutrong.fe_moneytrackbot.repository.WalletRepository;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;
    private final WalletRepository walletRepo;
    private final CategoryRepository categoryRepo;

    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<Wallet>> wallets = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
        walletRepo = new WalletRepository(application);
        categoryRepo = new CategoryRepository(application);
    }

    public LiveData<List<Transaction>> getTransactions() { return transactions; }
    public LiveData<List<Wallet>> getWallets() { return wallets; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<String> getMessage() { return message; }

    public void loadData() {
        // Load Transactions
        transactionRepo.getTransactions(new TransactionRepository.ApiCallback<List<Transaction>>() {
            @Override public void onSuccess(List<Transaction> result) { transactions.setValue(result); }
            @Override public void onError(String msg) { message.setValue(msg); }
        });

        // Load Wallets (cho dialog)
        walletRepo.getWallets(new WalletRepository.WalletCallback() {
            @Override public void onSuccess(List<Wallet> data) { wallets.setValue(data); }
            @Override public void onError(String msg) {}
        });

        // Load Categories (cho dialog)
        categoryRepo.getCategories(new CategoryRepository.CategoryCallback() {
            @Override public void onSuccess(List<Category> data) { categories.setValue(data); }
            @Override public void onError(String msg) {}
        });
    }

    public void createTransaction(Transaction t) {
        transactionRepo.createTransaction(t, new TransactionRepository.ApiCallback<Transaction>() {
            @Override public void onSuccess(Transaction result) {
                message.setValue("Thêm thành công");
                loadData(); // Refresh list
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