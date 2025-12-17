package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;
import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionRepository {
    private final Context context;

    public TransactionRepository(Context context) {
        this.context = context;
    }

    // Interface callback chung
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    // Interface callback riêng cho báo cáo
    public interface CashFlowCallback {
        void onSuccess(CashFlowResponse data);
        void onError(String message);
    }

    // --- 1. LẤY DANH SÁCH GIAO DỊCH ---
    public void getTransactions(ApiCallback<List<Transaction>> callback) {
        // 🔥 CẬP NHẬT: Truyền đủ 4 tham số (search, wallet_id, start_date, end_date)
        // Vì hàm này chỉ lấy danh sách mặc định, ta truyền null cho các bộ lọc
        RetrofitClient.getTransactionService(context)
                .getTransactions(null, null, null, null)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi tải giao dịch: " + response.message());
                        }
                    }
                    @Override public void onFailure(Call<List<Transaction>> call, Throwable t) { callback.onError(t.getMessage()); }
                });
    }

    // --- 2. CÁC HÀM CRUD (Thêm/Sửa/Xóa) - Giữ nguyên ---
    public void createTransaction(Transaction t, ApiCallback<Transaction> callback) {
        RetrofitClient.getTransactionService(context).createTransaction(t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Lỗi thêm: " + response.message());
            }
            @Override public void onFailure(Call<Transaction> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void updateTransaction(int id, Transaction t, ApiCallback<Transaction> callback) {
        RetrofitClient.getTransactionService(context).updateTransaction(id, t).enqueue(new Callback<Transaction>() {
            @Override
            public void onResponse(Call<Transaction> call, Response<Transaction> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Lỗi cập nhật: " + response.message());
            }
            @Override public void onFailure(Call<Transaction> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    public void deleteTransaction(int id, ApiCallback<Void> callback) {
        RetrofitClient.getTransactionService(context).deleteTransaction(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Lỗi xóa: " + response.message());
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
        });
    }

    // --- 3. LẤY BÁO CÁO DÒNG TIỀN ---
    public void getCashFlowReport(String startDate, String endDate, CashFlowCallback callback) {
        // 🔥 CẬP NHẬT: Truyền thêm tham số wallet_id là null (vì HomeFragment lấy tổng tất cả ví)
        RetrofitClient.getTransactionService(context)
                .getCashFlow(startDate, endDate, null)
                .enqueue(new Callback<CashFlowResponse>() {
                    @Override
                    public void onResponse(Call<CashFlowResponse> call, Response<CashFlowResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi tải dữ liệu: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<CashFlowResponse> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}