package com.example.firewarning.ui.login;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firewarning.utils.FireBaseCallBack;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginViewModel extends ViewModel {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef = database.getReference("users");

    public MutableLiveData<String> Email = new MutableLiveData<>();
    public MutableLiveData<String> Password = new MutableLiveData<>();
    public MutableLiveData<String> idDevice = new MutableLiveData<>();

    public MutableLiveData<Integer> loadingVisibility = new MutableLiveData<>();

    private User user = null;
    private String userID = "";
    private ArrayList<User> users = new ArrayList<>();

    public void setUserID(String userID) {
        this.userID = userID;
    }

    ArrayList<User> getAllUsersLiveData() {
        showLoading();
        getData((DataSnapshot item) -> {
            users.clear();
            for (DataSnapshot d : item.getChildren()) {
                User user = d.getValue(User.class);
                if (user != null) {
                    Log.d("User: ", user.toString());
                }
                if (user != null) {
                    user.setUid(d.getKey());
                }
                users.add(user);
            }
            hideLoading();
        });
        return users;
    }

    void showLoading() {
        loadingVisibility.setValue(View.VISIBLE);
    }

    void hideLoading() {
        loadingVisibility.setValue(View.INVISIBLE);
    }

    private void getData(FireBaseCallBack<DataSnapshot> callBack) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("getData", "onCancelled");
            }
        });
    }

    public void updateDevice(String idDevice, OnCompleteListener<Void> onCompleteListener) {
        if (userID != null) {
            userRef.child(userID).child("idDevice")
                    .setValue(idDevice)
                    .addOnCompleteListener(onCompleteListener);
            Log.d(userID, "insert success");
        } else {
            Log.d("userID", "insert fail");
        }
    }
}
