package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowResponse; // Nhớ import model này
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
    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(@Query("search") String searchTerm);

    @POST("api/transactions/")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(@Path("id") int transactionId);

    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(@Path("id") int transactionId, @Body Transaction transaction);

    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(@Path("id") int transactionId);

    @GET("api/reports/cashflow/")
    Call<CashFlowResponse> getCashFlow(
            @Query("start_date") String startDate,  // Định dạng: yyyy-MM-dd
            @Query("end_date") String endDate       // Định dạng: yyyy-MM-dd
    );
}