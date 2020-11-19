package com.example.firewarning.ui.main;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.utils.FireBaseCallBack;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;

public class MainViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<Device> deviceMutableLiveData = new MutableLiveData<>();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference deviceRef = database.getReference("devices");
    private ArrayList<Device> devices = new ArrayList<>();

    public MutableLiveData<DataSnapshot> getAllDevices() {
        MutableLiveData<DataSnapshot> liveDataSnapShot = new MutableLiveData<>();
        getFBData(liveDataSnapShot::setValue);
        return liveDataSnapShot;
    }

    public MutableLiveData<DataSnapshot> getDevicesOfUser(String idDevice) {
        MutableLiveData<DataSnapshot> liveDataSnapShot = new MutableLiveData<>();
        getFBDevicesOfUser(liveDataSnapShot::setValue, idDevice);
        return liveDataSnapShot;
    }


    public MutableLiveData<DataSnapshot> monitoringJustTemp(String idDevice) {
        MutableLiveData<DataSnapshot> temp = new MutableLiveData<>();
        getFBJustTemp(item -> {
            for (DataSnapshot dataSnapshot : item.getChildren()) {
                temp.setValue(dataSnapshot);
            }
        }, idDevice);
        return temp;
    }

    private void getFBJustTemp(FireBaseCallBack<DataSnapshot> callBack, String idDevice) {
        deviceRef.child(idDevice).child("no").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFBData(FireBaseCallBack<DataSnapshot> fireBaseCallBack) {
        deviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fireBaseCallBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFBDevicesOfUser(FireBaseCallBack<DataSnapshot> fireBaseCallBack, String idDevice) {
        deviceRef.child(idDevice).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fireBaseCallBack.afterDataChanged(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("CheckResult")
    public Observable<Device> test(String idDevice) {
        String[] ids = idDevice.split(",");
        return Observable.create(emitter -> {
            for (String id : ids) {
                if (!id.equals("")) {
                    database.getReference(id).addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Device device = dataSnapshot.getValue(Device.class);
                            if (device != null) {
                                emitter.onNext(device);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void add(Device device) {
        Set<String> idSet = new HashSet<>();
        if (idSet.add(device.getId())) {
            devices.add(device);
        } else {
            devices.set((getIndex(idSet, device.getId())), device);
        }
    }

    public static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry : set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
    }
}
