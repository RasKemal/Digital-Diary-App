package com.example.gunlukapp.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gunlukapp.data_access.MemoryDao;
import com.example.gunlukapp.entities.Memory;

@Database(entities = Memory.class, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getDatabase(Context context) {
        if(appDatabase == null){
            appDatabase = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    "memories_db"
            ).build();
        }
        return appDatabase;
    }

    public abstract MemoryDao memDao();
}
