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

    // Callback cho việc lấy danh sách
    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }

    // Callback cho việc tạo mới (Thêm onError để biết nếu lỗi)
    public interface CreateCallback {
        void onSuccess();
        void onError(String message);
    }

    // 1. Lấy danh sách danh mục
    public void getCategories(CategoryCallback callback) {
        // 🔥 CẬP NHẬT: Gọi qua getCategoryService()
        RetrofitClient.getCategoryService(context).getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Lỗi tải danh mục: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // 2. Tạo danh mục mới
    public void createCategory(String name, String type, CreateCallback callback) {
        Category category = new Category(name, type);

        // 🔥 CẬP NHẬT: Gọi qua getCategoryService()
        RetrofitClient.getCategoryService(context).createCategory(category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Lỗi tạo danh mục: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}