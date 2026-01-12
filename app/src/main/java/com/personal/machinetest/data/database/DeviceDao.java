package com.personal.machinetest.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM devices")
    LiveData<List<Device>> getAllDevices();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDevice(Device device);

    @Query("UPDATE devices SET isOnline=0")
    void setAllOffline();

}
