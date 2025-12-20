package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;

import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Wallet;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository chịu trách nhiệm:
 * - Lấy danh sách ví
 * - Thêm / Sửa / Xóa ví
 */
public class WalletRepository {

    private final Context context;

    public WalletRepository(Context context) {
        this.context = context;
    }

    /* ===================== CALLBACK ===================== */

    /**
     * Callback dùng cho việc lấy danh sách ví
     */
    public interface WalletCallback {
        void onSuccess(List<Wallet> wallets);
        void onError(String message);
    }

    /**
     * Callback dùng cho các hành động CRUD (Create / Update / Delete)
     */
    public interface WalletActionCallback {
        void onSuccess();
        void onError(String message);
    }

    /* ===================== GET ===================== */

    /**
     * Lấy danh sách tất cả ví
     */
    public void getWallets(WalletCallback callback) {
        RetrofitClient.getWalletService(context)
                .getWallets()
                .enqueue(new Callback<List<Wallet>>() {
                    @Override
                    public void onResponse(
                            Call<List<Wallet>> call,
                            Response<List<Wallet>> response
                    ) {
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

    /* ===================== CREATE ===================== */

    public void createWallet(
            String name,
            double balance,
            WalletActionCallback callback
    ) {
        Wallet wallet = new Wallet(name, balance);

        RetrofitClient.getWalletService(context)
                .createWallet(wallet)
                .enqueue(new Callback<Wallet>() {
                    @Override
                    public void onResponse(
                            Call<Wallet> call,
                            Response<Wallet> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Lỗi tạo ví: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Wallet> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== UPDATE ===================== */

    public void updateWallet(
            Wallet wallet,
            WalletActionCallback callback
    ) {
        RetrofitClient.getWalletService(context)
                .updateWallet(wallet.getId(), wallet)
                .enqueue(new Callback<Wallet>() {
                    @Override
                    public void onResponse(
                            Call<Wallet> call,
                            Response<Wallet> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Lỗi cập nhật ví: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Wallet> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /* ===================== DELETE ===================== */

    public void deleteWallet(
            int walletId,
            WalletActionCallback callback
    ) {
        RetrofitClient.getWalletService(context)
                .deleteWallet(walletId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Lỗi xóa ví: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}