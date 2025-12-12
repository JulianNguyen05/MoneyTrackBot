package ht.nguyenhuutrong.fe_moneytrackbot.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Category implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type; // "income" hoặc "expense"

    // --- 1. Constructor Rỗng (Bắt buộc để Gson map dữ liệu không bị lỗi) ---
    public Category() {
    }

    // --- 2. Constructor dùng để Gửi lên Server (Tạo mới - Không cần ID) ---
    public Category(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // --- 3. Constructor đầy đủ (Khi nhận từ Server về - Có ID) ---
    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }

    // --- Setters (Nên có) ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }

    // 🔥 QUAN TRỌNG: Để hiển thị tên lên Dropdown Menu
    @Override
    public String toString() {
        return name;
    }
}