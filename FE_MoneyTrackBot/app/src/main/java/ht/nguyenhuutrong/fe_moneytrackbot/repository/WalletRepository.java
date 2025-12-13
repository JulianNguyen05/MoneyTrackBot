package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;
import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRepository {

    private final Context context;

    public WalletRepository(Context context) {
        this.context = context;
    }

    public interface WalletCallback {
        void onSuccess(List<Wallet> wallets);
        void onError(String message);
    }

    public void getWallets(WalletCallback callback) {
        RetrofitClient.getApiService(context).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tải ví: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void createWallet(String name, double balance, Runnable onSuccess) {
        RetrofitClient.getApiService(context).createWallet(new Wallet(name, balance)).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    public void updateWallet(Wallet wallet, Runnable onSuccess) {
        RetrofitClient.getApiService(context).updateWallet(wallet.getId(), wallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {}
        });
    }

    public void deleteWallet(int id, Runnable onSuccess) {
        RetrofitClient.getApiService(context).deleteWallet(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}