package com.example.firewarning.ui.device;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.utils.FireBaseCallBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.reactivex.Observable;

public class DetailDeviceViewModel extends ViewModel {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference deviceRef = database.getReference("devices");
    ValueEventListener valueEventListener;

//    MutableLiveData<DataSnapshot> getAllDevice() {
//        MutableLiveData<DataSnapshot> dataSnapshotMutableLiveData = new MutableLiveData<>();
//        getDevice(value -> dataSnapshotMutableLiveData.setValue(value));
//        deviceRef.addValueEventListener(valueEventListener);
//        return dataSnapshotMutableLiveData;
//    }

    Observable<ArrayList<Device>> getAllDevice() {
        return Observable.create(emitter -> {
            deviceRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<Device> devices;
                    GenericTypeIndicator<ArrayList<Device>> t =
                            new GenericTypeIndicator<ArrayList<Device>>() {
                            };
                    devices = dataSnapshot.getValue(t);
                    emitter.onNext(devices);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });
    }

    private void getDevice(FireBaseCallBack<DataSnapshot> fireBaseCallBack) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fireBaseCallBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("onCancelled", databaseError.toString());
            }
        };
    }

    public MutableLiveData<DataSnapshot> onTemperatureChange(int indexOfDevice) {
        MutableLiveData<DataSnapshot> temp = new MutableLiveData<>();
        deviceRef.child(String.valueOf(indexOfDevice)).child("NO").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                temp.setValue(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return temp;
    }

    public Observable<Device> observerDevice(int index) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(index)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    emitter.onNext(dataSnapshot.getValue(Device.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });
    }

    io.reactivex.Observable<String> setNG(int indexOfDevice, String ng) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(indexOfDevice)).child("NG").setValue(ng)
                    .addOnSuccessListener(aVoid -> emitter.onNext("Success"));
        });
    }

    io.reactivex.Observable<String> setPicture(int indexOfDevice, String ng) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(indexOfDevice)).child("picture").setValue(ng)
                    .addOnSuccessListener(aVoid -> emitter.onNext("Success"));
        });
    }

    io.reactivex.Observable<String> reset(int indexOfDevice, String rST) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(indexOfDevice)).child("RST").setValue(rST)
                    .addOnSuccessListener(aVoid -> emitter.onNext("Success"));
        });
    }

    io.reactivex.Observable<String> setOffset(int indexOfDevice, String offSet) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(indexOfDevice)).child("NDU").setValue(offSet)
                    .addOnSuccessListener(aVoid -> emitter.onNext("Success"));
        });
    }

    io.reactivex.Observable<String> setLoopingTime(int indexOfDevice, String looping) {
        return Observable.create(emitter -> {
            deviceRef.child(String.valueOf(indexOfDevice)).child("NCL").setValue(looping)
                    .addOnSuccessListener(aVoid -> emitter.onNext("Success"));
        });
    }

    public void setName(int indexOfDevice, String name) {
        deviceRef.child(String.valueOf(indexOfDevice)).child("name").setValue(name);
    }

    void setTotal(int indexOfDevice, String total) {
        deviceRef.child(String.valueOf(indexOfDevice)).child("total").setValue(total);
    }

    void setNumberOfHighTemp(int indexOfDevice, String number) {
        deviceRef.child(String.valueOf(indexOfDevice)).child("highTemp").setValue(number);
    }

    void removeAllListeners() {
        deviceRef.removeEventListener(valueEventListener);
    }
}
