package ht.nguyenhuutrong.fe_moneytrackbot.data.api.services;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * WalletService
 * --------------------------------------------------
 * Cung cấp các API quản lý ví và chuyển tiền giữa các ví
 */
public interface WalletService {

    /**
     * Lấy danh sách tất cả ví của người dùng
     */
    @GET("api/wallets/")
    Call<List<Wallet>> getWallets();

    /**
     * Tạo ví mới
     */
    @POST("api/wallets/")
    Call<Wallet> createWallet(@Body Wallet wallet);

    /**
     * Cập nhật thông tin ví theo ID
     */
    @PUT("api/wallets/{id}/")
    Call<Wallet> updateWallet(
            @Path("id") int id,
            @Body Wallet wallet
    );

    /**
     * Xóa ví theo ID
     */
    @DELETE("api/wallets/{id}/")
    Call<Void> deleteWallet(@Path("id") int id);

    /**
     * Chuyển tiền giữa hai ví
     *
     * @param fromWalletId ID ví nguồn
     * @param toWalletId   ID ví đích
     * @param amount       Số tiền chuyển
     * @param date         Ngày giao dịch (yyyy-MM-dd)
     * @param description Mô tả giao dịch
     */
    @FormUrlEncoded
    @POST("api/transfer/")
    Call<Void> transferFunds(
            @Field("from_wallet_id") int fromWalletId,
            @Field("to_wallet_id") int toWalletId,
            @Field("amount") double amount,
            @Field("date") String date,
            @Field("description") String description
    );
}