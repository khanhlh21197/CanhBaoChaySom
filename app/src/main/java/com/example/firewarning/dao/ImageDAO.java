package com.example.firewarning.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.ui.gallery.Image;

import java.util.List;

@Dao
public interface ImageDAO {
    @Query("SELECT * FROM image")
    List<Image> getAllImages();

    @Insert
    void insertImage(Image image);

    @Query("DELETE FROM device WHERE id = :id")
    int deleteImage(String id);

    @Delete
    void deleteImage(Image image);
}
