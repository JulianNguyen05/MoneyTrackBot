package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;
import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionRepository {
    private final Context context;

    public TransactionRepository(Context context) {
        this.context = context;
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void getTransactions(ApiCallback<List<Transaction>> callback) {
        RetrofitClient.getApiService(context).getTransactions(null).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (response.isSuccessful() && response.body() != null) callback.onSuccess(response.body());
                else callback.onError("Lỗi tải giao dịch");
            }
            @Override public void onFailure(Call<List<Transaction>> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void createTransaction(Transaction t, ApiCallback<Transaction> callback) {
        RetrofitClient.getApiService(context).createTransaction(t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Lỗi thêm: " + response.message());
            }
            @Override public void onFailure(Call<Transaction> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void updateTransaction(int id, Transaction t, ApiCallback<Transaction> callback) {
        RetrofitClient.getApiService(context).updateTransaction(id, t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Lỗi cập nhật: " + response.message());
            }
            @Override public void onFailure(Call<Transaction> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void deleteTransaction(int id, ApiCallback<Void> callback) {
        RetrofitClient.getApiService(context).deleteTransaction(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Lỗi xóa: " + response.message());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }
}