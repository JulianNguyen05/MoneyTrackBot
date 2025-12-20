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

/**
 * CategoryService
 * --------------------------------------------------
 * Cung cấp các API CRUD cho Category
 * Sử dụng Retrofit để giao tiếp với Backend
 */
public interface CategoryService {

    /**
     * Lấy danh sách tất cả danh mục
     */
    @GET("api/categories/")
    Call<List<Category>> getCategories();

    /**
     * Tạo danh mục mới
     *
     * @param category Dữ liệu danh mục cần tạo
     */
    @POST("api/categories/")
    Call<Category> createCategory(@Body Category category);

    /**
     * Cập nhật danh mục theo ID
     *
     * @param id       ID của danh mục
     * @param category Dữ liệu cập nhật
     */
    @PUT("api/categories/{id}/")
    Call<Category> updateCategory(
            @Path("id") int id,
            @Body Category category
    );

    /**
     * Xóa danh mục theo ID
     *
     * @param id ID của danh mục cần xóa
     */
    @DELETE("api/categories/{id}/")
    Call<Void> deleteCategory(@Path("id") int id);
}