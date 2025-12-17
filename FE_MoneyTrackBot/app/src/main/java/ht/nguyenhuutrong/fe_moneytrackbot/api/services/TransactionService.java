package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TransactionService {

    // --- 1. LẤY DANH SÁCH GIAO DỊCH (Có lọc) ---
    // Gộp tất cả vào 1 hàm. Truyền null cho tham số không dùng.
    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(
            @Query("search") String searchTerm,
            @Query("wallet_id") Integer walletId,   // Có thể null
            @Query("start_date") String startDate,  // Có thể null
            @Query("end_date") String endDate       // Có thể null
    );

    // --- 2. CÁC THAO TÁC CRUD CƠ BẢN ---
    @POST("api/transactions/")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(@Path("id") int transactionId);

    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(@Path("id") int transactionId, @Body Transaction transaction);

    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(@Path("id") int transactionId);

    // --- 3. BÁO CÁO TỔNG KẾT (CASHFLOW) ---
    // Gộp thành 1 hàm có hỗ trợ lọc theo ví
    @GET("api/reports/cashflow/")
    Call<CashFlowResponse> getCashFlow(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("wallet_id") Integer walletId    // Có thể null
    );
}