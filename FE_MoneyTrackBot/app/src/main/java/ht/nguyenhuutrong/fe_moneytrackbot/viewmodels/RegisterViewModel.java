package ht.nguyenhuutrong.fe_moneytrackbot.viewmodels;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ht.nguyenhuutrong.fe_moneytrackbot.repository.AuthRepository;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    // Getters
    public LiveData<Boolean> getRegisterSuccess() { return registerSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void register(String username, String email, String password) {
        // 1. Validate dữ liệu
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Vui lòng nhập đủ thông tin");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Email không hợp lệ");
            return;
        }

        // 2. Gọi Repository
        isLoading.setValue(true);
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