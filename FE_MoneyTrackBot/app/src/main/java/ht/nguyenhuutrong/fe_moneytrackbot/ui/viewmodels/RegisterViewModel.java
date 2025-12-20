package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.AuthRepository;

/**
 * ViewModel xử lý logic đăng ký tài khoản
 * - Kiểm tra dữ liệu hợp lệ
 * - Gọi AuthRepository để đăng ký
 * - Cập nhật trạng thái cho UI qua LiveData
 */
public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    // --- Getters cho Activity/Fragment observe ---
    public LiveData<Boolean> getRegisterSuccess() { return registerSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    /**
     * Thực hiện đăng ký người dùng
     * @param username tên đăng nhập
     * @param email email hợp lệ
     * @param password mật khẩu
     */
    public void register(String username, String email, String password) {
        // 1. Validate dữ liệu cơ bản
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập đủ thông tin");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Email không hợp lệ");
            return;
        }

        // 2. Gọi Repository đăng ký
        isLoading.setValue(true); // Bật loading
        authRepository.register(username, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                registerSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}