package ht.nguyenhuutrong.fe_moneytrackbot.api.services;

import java.util.List;
import ht.nguyenhuutrong.fe_moneytrackbot.models.Category;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface CategoryService {
    @GET("api/categories/")
    Call<List<Category>> getCategories();

    @POST("api/categories/")
    Call<Category> createCategory(@Body Category category);
}