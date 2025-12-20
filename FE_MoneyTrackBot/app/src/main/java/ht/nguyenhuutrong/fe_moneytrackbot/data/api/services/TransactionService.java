package ht.nguyenhuutrong.fe_moneytrackbot.data.api.services;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.CashFlowResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Transaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * TransactionService
 * --------------------------------------------------
 * Cung cấp các API quản lý giao dịch và báo cáo dòng tiền
 */
public interface TransactionService {

    /**
     * Lấy danh sách giao dịch với các điều kiện lọc tùy chọn.
     * Truyền null cho các tham số không sử dụng.
     *
     * @param searchTerm Từ khóa tìm kiếm
     * @param walletId   ID ví
     * @param startDate  Ngày bắt đầu (yyyy-MM-dd)
     * @param endDate    Ngày kết thúc (yyyy-MM-dd)
     */
    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(
            @Query("search") String searchTerm,
            @Query("wallet_id") Integer walletId,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    /**
     * Tạo giao dịch mới
     */
    @POST("api/transactions/")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    /**
     * Lấy chi tiết giao dịch theo ID
     */
    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(@Path("id") int transactionId);

    /**
     * Cập nhật giao dịch theo ID
     */
    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(
            @Path("id") int transactionId,
            @Body Transaction transaction
    );

    /**
     * Xóa giao dịch theo ID
     */
    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(@Path("id") int transactionId);

    /**
     * Lấy báo cáo dòng tiền (Cash Flow) theo khoảng thời gian.
     * Có thể lọc theo ví.
     *
     * @param startDate Ngày bắt đầu (yyyy-MM-dd)
     * @param endDate   Ngày kết thúc (yyyy-MM-dd)
     * @param walletId  ID ví (có thể null)
     */
    @GET("api/reports/cashflow/")
    Call<CashFlowResponse> getCashFlow(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("wallet_id") Integer walletId
    );
}