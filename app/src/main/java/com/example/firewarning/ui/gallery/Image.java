package com.example.firewarning.ui.gallery;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

@Entity(tableName = "image")
public class Image implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @PropertyName("deviceId")
    private String deviceId;
    @PropertyName("image")
    private byte[] image;

    public Image(String deviceId, byte[] image) {
        this.deviceId = deviceId;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
