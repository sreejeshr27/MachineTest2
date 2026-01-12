package com.personal.machinetest.data.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Device {
    @PrimaryKey
    @NonNull
    public String ipAddress;
    public String name;
    public Boolean isOnline;

    public Device(@NonNull String ipAddress, String name, Boolean isOnline) {
        this.ipAddress = ipAddress;
        this.name = name;
        this.isOnline = isOnline;
    }
}
