package com.example.firewarning.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.ui.gallery.Image;

@Database(entities = {Device.class, Image.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract DeviceDAO deviceDAO();
    public abstract ImageDAO imageDAO();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void deleteInstance() {
        INSTANCE = null;
    }
}
