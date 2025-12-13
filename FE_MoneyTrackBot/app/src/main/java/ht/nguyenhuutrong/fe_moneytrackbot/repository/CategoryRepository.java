package ht.nguyenhuutrong.fe_moneytrackbot.repository;

import android.content.Context;
import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    private final Context context;

    public CategoryRepository(Context context) {
        this.context = context;
    }

    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }

    public void getCategories(CategoryCallback callback) {
        RetrofitClient.getApiService(context).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tải danh mục");
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createCategory(String name, String type, Runnable onSuccess) {
        RetrofitClient.getApiService(context).createCategory(new Category(name, type)).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) onSuccess.run();
            }
            @Override
            public void onFailure(Call<Category> call, Throwable t) {}
        });
    }
}