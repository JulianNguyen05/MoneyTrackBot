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

    // 1. Callback cho việc lấy danh sách (GET) - Trả về List<Category>
    public interface CategoryCallback {
        void onSuccess(List<Category> categories);
        void onError(String message);
    }

    // 2. 🔥 Callback chung cho Thêm / Sửa / Xóa - Chỉ cần báo thành công/thất bại
    public interface CategoryActionCallback {
        void onSuccess();
        void onError(String message);
    }

    // ================== A. LẤY DANH SÁCH ==================
    public void getCategories(CategoryCallback callback) {
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
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ================== B. TẠO MỚI (CREATE) ==================
    public void createCategory(String name, String type, CategoryActionCallback callback) {
        Category category = new Category(name, type);

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
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // ================== C. CẬP NHẬT (UPDATE) - MỚI ==================
    public void updateCategory(Category category, CategoryActionCallback callback) {
        // Gọi API update với ID lấy từ đối tượng category
        RetrofitClient.getCategoryService(context)
                .updateCategory(category.getId(), category)
                .enqueue(new Callback<Category>() {
                    @Override
                    public void onResponse(Call<Category> call, Response<Category> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Lỗi cập nhật: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Category> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    // ================== D. XÓA (DELETE) - MỚI ==================
    public void deleteCategory(int id, CategoryActionCallback callback) {
        // Gọi API delete với ID truyền vào
        RetrofitClient.getCategoryService(context)
                .deleteCategory(id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Lỗi xóa danh mục: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }
}