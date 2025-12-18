package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRepository {

    private final Context context;

    public WalletRepository(Context context) {
        this.context = context;
    }

    // Callback cho việc lấy danh sách
    public interface WalletCallback {
        void onSuccess(List<Wallet> wallets);
        void onError(String message);
    }

    // 🔥 MỚI: Callback cho các hành động Thêm/Sửa/Xóa (để bắt lỗi)
    public interface WalletActionCallback {
        void onSuccess();
        void onError(String message);
    }

    // 1. Lấy danh sách ví
    public void getWallets(WalletCallback callback) {
        // 🔥 CẬP NHẬT: Gọi qua getWalletService()
        RetrofitClient.getWalletService(context).getWallets().enqueue(new Callback<List<Wallet>>() {
            @Override
            public void onResponse(Call<List<Wallet>> call, Response<List<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tải ví: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<List<Wallet>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // 2. Tạo ví mới
    public void createWallet(String name, double balance, WalletActionCallback callback) {
        // 🔥 CẬP NHẬT: Gọi qua getWalletService()
        RetrofitClient.getWalletService(context).createWallet(new Wallet(name, balance)).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Lỗi tạo ví: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // 3. Cập nhật ví
    public void updateWallet(Wallet wallet, WalletActionCallback callback) {
        // 🔥 CẬP NHẬT: Gọi qua getWalletService()
        RetrofitClient.getWalletService(context).updateWallet(wallet.getId(), wallet).enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Lỗi cập nhật: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // 4. Xóa ví
    public void deleteWallet(int id, WalletActionCallback callback) {
        // 🔥 CẬP NHẬT: Gọi qua getWalletService()
        RetrofitClient.getWalletService(context).deleteWallet(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Lỗi xóa: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}