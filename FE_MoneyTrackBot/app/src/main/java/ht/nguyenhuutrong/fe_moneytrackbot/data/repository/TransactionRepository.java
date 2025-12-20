package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository chịu trách nhiệm:
 * - CRUD giao dịch
 * - Lấy danh sách giao dịch
 * - Lấy báo cáo dòng tiền
 */
public class TransactionRepository {

    private final Context context;

    public TransactionRepository(Context context) {
        this.context = context;
    }

    /* ===================== CALLBACK ===================== */

    /**
     * Callback dùng chung cho các API CRUD
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Callback riêng cho báo cáo dòng tiền
     */
    public interface CashFlowCallback {
        void onSuccess(CashFlowResponse data);
        void onError(String message);
    }

    /* ===================== GET ===================== */

    /**
     * Lấy danh sách giao dịch (không filter)
     */
    public void getTransactions(ApiCallback<List<Transaction>> callback) {
        RetrofitClient.getTransactionService(context)
                .getTransactions(null, null, null, null)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(
                            Call<List<Transaction>> call,
                            Response<List<Transaction>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi tải giao dịch: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== CREATE ===================== */

    public void createTransaction(
            Transaction transaction,
            ApiCallback<Transaction> callback
    ) {
        RetrofitClient.getTransactionService(context)
                .createTransaction(transaction)
                .enqueue(new Callback<Transaction>() {
                    @Override
                    public void onResponse(
                            Call<Transaction> call,
                            Response<Transaction> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi thêm giao dịch: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Transaction> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== UPDATE ===================== */

    public void updateTransaction(
            int id,
            Transaction transaction,
            ApiCallback<Transaction> callback
    ) {
        RetrofitClient.getTransactionService(context)
                .updateTransaction(id, transaction)
                .enqueue(new Callback<Transaction>() {
                    @Override
                    public void onResponse(
                            Call<Transaction> call,
                            Response<Transaction> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi cập nhật giao dịch: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Transaction> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== DELETE ===================== */

    public void deleteTransaction(int id, ApiCallback<Void> callback) {
        RetrofitClient.getTransactionService(context)
                .deleteTransaction(id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError("Lỗi xóa giao dịch: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== REPORT ===================== */

    /**
     * Lấy báo cáo dòng tiền theo khoảng thời gian
     * walletId = null → lấy tổng tất cả ví
     */
    public void getCashFlowReport(
            String startDate,
            String endDate,
            CashFlowCallback callback
    ) {
        RetrofitClient.getTransactionService(context)
                .getCashFlow(startDate, endDate, null)
                .enqueue(new Callback<CashFlowResponse>() {
                    @Override
                    public void onResponse(
                            Call<CashFlowResponse> call,
                            Response<CashFlowResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Lỗi tải báo cáo: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<CashFlowResponse> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}