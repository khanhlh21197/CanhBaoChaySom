package com.example.firewarning.ui.signup;

import android.os.Build;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.ui.login.User;
import com.example.firewarning.utils.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SignUpViewModel extends ViewModel {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef = database.getReference("users");
    private Result<User> result;

    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> rePassword = new MutableLiveData<>();
    public MutableLiveData<Integer> loading = new MutableLiveData<>(View.GONE);

    private String idDevice = "HHA000001, HHA000002, HHA000003";

    public void setResult(Result<User> result) {
        this.result = result;
    }

    public void onClick(View v) {
        User user = new User(email.getValue(), password.getValue());
        String message = "";
        if (CommonActivity.isNullOrEmpty(email)) {
            result.onFailure("Vui lòng nhập Email hoặc tên tài khoản!");
        } else if (!CommonActivity.isNullOrEmpty(password.getValue())
                && password.getValue().length() < 5) {
            message = "Mật khẩu phải lớn hơn 5 kí tự!";
            result.onFailure(message);
        } else if (CommonActivity.isNullOrEmpty(rePassword.getValue())) {
            message = "Vui lòng nhập lại mật khẩu!";
            result.onFailure(message);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && !Objects.equals(rePassword.getValue(), password.getValue())) {
            message = "Nhập lại password chưa chính xác!";
            result.onFailure(message);
        } else if (!CommonActivity.isNullOrEmpty(email.getValue())
                && (!Objects.requireNonNull(email.getValue()).matches(String.valueOf(Patterns.EMAIL_ADDRESS)))) {
            message = "Email chưa đúng định dạng!";
            result.onFailure(message);
        } else {
            loading.setValue(View.VISIBLE);
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(Objects.requireNonNull(email.getValue()),
                            Objects.requireNonNull(password.getValue()))
                    .addOnCompleteListener(task -> {
                        loading.setValue(View.GONE);
                        if (task.isSuccessful()) {
                            String childId = "";
                            if (!CommonActivity.isNullOrEmpty(FirebaseAuth.getInstance().getCurrentUser())) {
                                childId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String finalChildId = childId;
                                userRef.child(childId).setValue(user.toMap(), (databaseError, databaseReference) -> {
                                    if (databaseError != null) {
                                        Log.d("onError", databaseError.toString());
                                        return;
                                    }
                                    Log.d("onCreate", "success");
                                    Log.d("key", finalChildId);
                                });
                            }
                            result.onSuccess(user, "Đăng ký thành công!");
                        } else {
                            result.onFailure("Tài khoản đã tồn tại!");
                        }
                    });
        }
    }

    private boolean validate(User user) {
        AtomicBoolean success = new AtomicBoolean(false);
        String message = "";
        if (CommonActivity.isNullOrEmpty(email)) {
            result.onFailure("Vui lòng nhập Email hoặc tên tài khoản!");
            success.set(false);
        } else if (!CommonActivity.isNullOrEmpty(password.getValue())
                && password.getValue().length() < 5) {
            message = "Mật khẩu phải lớn hơn 5 kí tự!";
            result.onFailure(message);
            success.set(false);
        } else if (CommonActivity.isNullOrEmpty(rePassword.getValue())) {
            message = "Vui lòng nhập lại mật khẩu!";
            result.onFailure(message);
            success.set(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && !Objects.equals(rePassword.getValue(), password.getValue())) {
            message = "Nhập lại password chưa chính xác!";
            result.onFailure(message);
            success.set(false);
        } else if (!CommonActivity.isNullOrEmpty(email.getValue())
                && (!Objects.requireNonNull(email.getValue()).matches(String.valueOf(Patterns.EMAIL_ADDRESS)))) {
            message = "Email chưa đúng định dạng!";
            result.onFailure(message);
            success.set(false);
        } else {
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(Objects.requireNonNull(email.getValue()),
                            Objects.requireNonNull(password.getValue()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            success.set(true);
                            result.onSuccess(user, "Đăng ký thành công!");
                        } else {
                            success.set(false);
                            result.onFailure("Tài khoản đã tồn tại!");
                        }
                    });
        }
        return success.get();
    }
}
