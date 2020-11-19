package com.example.firewarning.ui.signup;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.firewarning.R;
import com.example.firewarning.common.CommonActivity;
import com.example.firewarning.common.ReplaceFragment;
import com.example.firewarning.databinding.SignUpFragmentBinding;
import com.example.firewarning.ui.login.LoginFragment;
import com.example.firewarning.ui.login.User;
import com.example.firewarning.utils.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignUpFragment extends Fragment implements Result<User> {
    private SignUpFragmentBinding mBinding;
    private SignUpViewModel viewModel;
    private FirebaseAuth mAuth;
    private User currentUser = null;

    public static SignUpFragment newInstance() {

        Bundle args = new Bundle();

        SignUpFragment fragment = new SignUpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.sign_up_fragment, container, false);
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SignUpViewModel.class);
        mBinding.setLifecycleOwner(this);
        mBinding.setSignUpViewModel(viewModel);
        viewModel.setResult(this);

        mAuth = FirebaseAuth.getInstance();
        createAccount();
        mBinding.btnSignup.setOnClickListener(v -> {
            Log.d("email", Objects.requireNonNull(viewModel.email.getValue()));
            Log.d("email", Objects.requireNonNull(viewModel.password.getValue()));
        });
        mBinding.linkLogin.setOnClickListener(v -> goToLogIn());
        return mBinding.getRoot();
    }

    private void createAccount() {
        String email = viewModel.email.getValue();
        String password = viewModel.password.getValue();
        if (!CommonActivity.isNullOrEmpty(email) && !CommonActivity.isNullOrEmpty(password)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("createAccount", "success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.d("createAccount", "failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void goToLogIn() {
        String email = mBinding.txtEmailAddress.getText().toString();
        String password = mBinding.txtPassword.getText().toString();
        String rePassword = mBinding.txtRePassword.getText().toString();
        if (CommonActivity.isNullOrEmpty(email)
                || CommonActivity.isNullOrEmpty(password)
                || CommonActivity.isNullOrEmpty(rePassword)) {
            CommonActivity.showConfirmValidate(getActivity(), R.string.not_enough);
            return;
        }
        if (!rePassword.equals(password)) {
            CommonActivity.showConfirmValidate(getActivity(), R.string.wrong_rePassword);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ReplaceFragment.replaceFragment(getActivity(),
                    LoginFragment.newInstance(currentUser.getEmail(),
                            currentUser.getPassword()), true);
        }
    }

    @Override
    public void onFailure(String message) {
        CommonActivity.showConfirmValidate(getActivity(), message);
    }

    @Override
    public void onSuccess(User user, String message) {
        currentUser = user;
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        goToLogIn();
    }
}

