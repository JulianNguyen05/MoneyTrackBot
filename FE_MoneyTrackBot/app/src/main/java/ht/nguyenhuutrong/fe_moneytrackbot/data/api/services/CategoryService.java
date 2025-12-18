package ht.nguyenhuutrong.fe_moneytrackbot.data.api.services;

import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.data.models.Category;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryService {
    // 1. Lấy danh sách
    @GET("api/categories/")
    Call<List<Category>> getCategories();

    // 2. Tạo mới
    @POST("api/categories/")
    Call<Category> createCategory(@Body Category category);

    // 3. 🔥 Cập nhật (Sửa)
    // Cần truyền ID vào đường dẫn (Path)
    @PUT("api/categories/{id}/")
    Call<Category> updateCategory(@Path("id") int id, @Body Category category);

    // 4. 🔥 Xóa
    // Cần truyền ID vào đường dẫn (Path)
    @DELETE("api/categories/{id}/")
    Call<Void> deleteCategory(@Path("id") int id);
}