package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Budget;
import ht.nguyenhuutrong.fe_moneytrackbot.models.CashFlowEntry;
import ht.nguyenhuutrong.fe_moneytrackbot.models.ReportEntry;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReportService {
    @GET("api/reports/summary/")
    Call<List<ReportEntry>> getReportSummary(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @GET("api/reports/cashflow/")
    Call<List<CashFlowEntry>> getCashFlowReport(
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
}