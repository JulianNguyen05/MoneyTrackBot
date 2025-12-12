package ht.nguyenhuutrong.fe_moneytrackbot.api;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.models.Budget;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowEntry;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatbotRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ChatbotResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.LoginResponse;
import ht.nguyenhuutrong.fe_moneytrackbot.models.RegisterRequest;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ReportEntry;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Transaction;
import ht.nguyenhuutrong.fe_moneytrackbot.models.User;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * ================================
 *  API SERVICE — Retrofit Endpoints
 * ================================
 */
public interface ApiService {

    // ==========================================================
    // 🧑 USER AUTH (Register / Login)
    // ==========================================================

    @POST("api/register/")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/token/")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);



    // ==========================================================
    // 💸 TRANSACTIONS
    // ==========================================================

    @GET("api/transactions/")
    Call<List<Transaction>> getTransactions(
            @Query("search") String searchTerm
    );

    @POST("api/transactions/")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    @GET("api/transactions/{id}/")
    Call<Transaction> getTransactionDetails(
            @Path("id") int transactionId
    );

    @PUT("api/transactions/{id}/")
    Call<Transaction> updateTransaction(
            @Path("id") int transactionId,
            @Body Transaction transaction
    );

    @DELETE("api/transactions/{id}/")
    Call<Void> deleteTransaction(@Path("id") int transactionId);



    // ==========================================================
    // 🏷️ CATEGORIES
    // ==========================================================

    @GET("api/categories/")
    Call<List<Category>> getCategories();

    @POST("api/categories/")
    Call<Category> createCategory(@Body Category category);



    // ==========================================================
    // 💰 WALLETS
    // ==========================================================

    @GET("api/wallets/")
    Call<List<Wallet>> getWallets();

    @POST("api/wallets/")
    Call<Wallet> createWallet(@Body Wallet wallet);

    @PUT("api/wallets/{id}/")
    Call<Wallet> updateWallet(@Path("id") int id, @Body Wallet wallet);

    @DELETE("api/wallets/{id}/")
    Call<Void> deleteWallet(@Path("id") int id);


    // ==========================================================
    // 🔁 TRANSFER (Wallet → Wallet)
    // ==========================================================

    @FormUrlEncoded
    @POST("api/transfer/")
    Call<Void> transferFunds(
            @Field("from_wallet_id") int fromWalletId,
            @Field("to_wallet_id") int toWalletId,
            @Field("amount") double amount,
            @Field("date") String date,
            @Field("description") String description
    );



    // ==========================================================
    // 📊 REPORTS & BUDGET
    // ==========================================================

    @GET("api/reports/summary/")
    Call<List<ReportEntry>> getReportSummary(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @GET("api/budgets/")
    Call<List<Budget>> getBudgets(
            @Query("month") int month,
            @Query("year") int year
    );

    @POST("api/budgets/")
    Call<Budget> createBudget(@Body Budget budget);

    @GET("api/reports/cashflow/")
    Call<List<CashFlowEntry>> getCashFlowReport(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );



    // ==========================================================
    // 🤖 CHATBOT
    // ==========================================================

    @POST("api/chatbot/")
    Call<ChatbotResponse> postChatbotMessage(
            @Body ChatbotRequest request
    );
}
