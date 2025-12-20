package ht.nguyenhuutrong.fe_moneytrackbot.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ht.nguyenhuutrong.fe_moneytrackbot.data.repository.AuthRepository;

/**
 * ViewModel quản lý đăng nhập người dùng:
 * - Gọi AuthRepository để xác thực
 * - Cập nhật trạng thái login, loading và lỗi
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    // LiveData quan sát từ Activity/Fragment
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    // --- Getters để UI observe ---
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // --- Kiểm tra trạng thái đăng nhập sẵn ---
    public boolean isUserLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * Thực hiện đăng nhập
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     */
    public void login(String username, String password) {
        // 1. Validate cơ bản
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        // 2. Hiển thị loading
        isLoading.setValue(true);

        // 3. Gọi repository xác thực
        authRepository.login(username, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                loginSuccess.setValue(true); // Đăng nhập thành công
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message); // Báo lỗi lên UI
            }
        });
    }
}