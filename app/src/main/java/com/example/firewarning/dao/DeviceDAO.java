package com.example.firewarning.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.firewarning.ui.device.model.Device;

import java.util.List;

@Dao
public interface DeviceDAO {
    @Query("SELECT * FROM device")
    List<Device> getAllDevice();

    @Query("SELECT * FROM device WHERE id = :id")
    List<Device> getAllDevice(String id);

    @Insert
    void insertDevice(Device device);

    @Query("DELETE FROM device WHERE id = :id")
    int deleteHistory(String id);

    @Delete
    void deleteDevice(Device device);
}
