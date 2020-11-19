package com.example.firewarning.ui.login;

import androidx.annotation.NonNull;

import com.example.firewarning.common.CommonActivity;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("isAuthenticated")
    @Expose
    private boolean isAuthenticated;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("mobile")
    @Expose
    private String mobile;
    @SerializedName("idDevice")
    @PropertyName("idDevice")
    @Expose
    private String idDevice;

    public User(String email, String password, String idDevice) {
        this.email = email;
        this.password = password;
        this.idDevice = idDevice;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdDevice() {
        return idDevice;
    }

    public void setIdDevice(String idDevice) {
        this.idDevice = idDevice;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        if (!CommonActivity.isNullOrEmpty(this.idDevice)) {
            result.put("idDevice", this.idDevice);
        }
        if (!CommonActivity.isNullOrEmpty(this.name)) {
            result.put("name", this.name);
        }
        if (!CommonActivity.isNullOrEmpty(this.email)) {
            result.put("email", this.email);
        }
        if (!CommonActivity.isNullOrEmpty(this.address)) {
            result.put("address", this.address);
        }
        if (!CommonActivity.isNullOrEmpty(this.mobile)) {
            result.put("mobile", this.mobile);
        }
        if (!CommonActivity.isNullOrEmpty(this.password)) {
            result.put("password", this.password);
        }
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return email + password;
    }
}
