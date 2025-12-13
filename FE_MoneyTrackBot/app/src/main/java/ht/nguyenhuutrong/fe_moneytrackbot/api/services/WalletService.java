package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import java.util.List;
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

public interface WalletService {
    @GET("api/wallets/")
    Call<List<Wallet>> getWallets();

    @POST("api/wallets/")
    Call<Wallet> createWallet(@Body Wallet wallet);

    @PUT("api/wallets/{id}/")
    Call<Wallet> updateWallet(@Path("id") int id, @Body Wallet wallet);

    @DELETE("api/wallets/{id}/")
    Call<Void> deleteWallet(@Path("id") int id);

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