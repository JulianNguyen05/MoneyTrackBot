package ht.nguyenhuutrong.fe_moneytrackbot.data.repository;

import android.content.Context;
import java.util.List;

import ht.nguyenhuutrong.fe_moneytrackbot.data.api.RetrofitClient;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository xử lý toàn bộ nghiệp vụ liên quan đến Category:
 * - Lấy danh sách
 * - Tạo mới
 * - Cập nhật
 * - Xóa
 */
public class CategoryRepository {

    private final Context context;

    public CategoryRepository(Context context) {
        this.context = context;
    }

    /* ===================== CALLBACKS ===================== */

    /** Callback cho nghiệp vụ GET danh sách Category */
    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }

    /** Callback dùng chung cho Create / Update / Delete */
    public interface CategoryActionCallback {
        void onSuccess();
        void onError(String message);
    }

    /* ===================== API METHODS ===================== */

    /**
     * Lấy danh sách tất cả Category
     */
    public void getCategories(CategoryCallback callback) {
        RetrofitClient.getCategoryService(context)
                .getCategories()
                .enqueue(new Callback<List<Category>>() {
                    @Override
                    public void onResponse(
                            Call<List<Category>> call,
                            Response<List<Category>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Không thể tải danh mục");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Category>> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /**
     * Tạo mới một Category
     */
    public void createCategory(
            String name,
            String type,
            CategoryActionCallback callback
    ) {
        Category category = new Category(name, type);

        RetrofitClient.getCategoryService(context)
                .createCategory(category)
                .enqueue(new Callback<Category>() {
                    @Override
                    public void onResponse(
                            Call<Category> call,
                            Response<Category> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Tạo danh mục thất bại");
                        }
                    }

                    @Override
                    public void onFailure(Call<Category> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /**
     * Cập nhật Category theo ID
     */
    public void updateCategory(
            Category category,
            CategoryActionCallback callback
    ) {
        RetrofitClient.getCategoryService(context)
                .updateCategory(category.getId(), category)
                .enqueue(new Callback<Category>() {
                    @Override
                    public void onResponse(
                            Call<Category> call,
                            Response<Category> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Cập nhật danh mục thất bại");
                        }
                    }

                    @Override
                    public void onFailure(Call<Category> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    /**
     * Xóa Category theo ID
     */
    public void deleteCategory(
            int id,
            CategoryActionCallback callback
    ) {
        RetrofitClient.getCategoryService(context)
                .deleteCategory(id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Xóa danh mục thất bại");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}