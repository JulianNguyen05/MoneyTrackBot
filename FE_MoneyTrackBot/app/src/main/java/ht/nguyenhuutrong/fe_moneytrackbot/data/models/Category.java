package ht.nguyenhuutrong.fe_moneytrackbot.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Category
 * --------------------------------------------------
 * Đại diện cho danh mục thu hoặc chi trong hệ thống.
 */
public class Category implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    /**
     * Loại danh mục: "income" hoặc "expense"
     */
    @SerializedName("type")
    private String type;

    /**
     * Constructor rỗng.
     * Cần thiết cho Gson khi mapping dữ liệu từ JSON.
     */
    public Category() {
    }

    /**
     * Constructor dùng khi tạo mới danh mục.
     */
    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor đầy đủ (dùng khi nhận dữ liệu từ server).
     */
    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // ===== Getters =====

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    // ===== Setters =====

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Trả về tên danh mục để hiển thị trong Spinner / Dropdown.
     */
    @Override
    public String toString() {
        return name;
    }
}