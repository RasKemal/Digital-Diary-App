package com.example.gunlukapp.data_access;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gunlukapp.entities.Memory;

import java.util.List;

@Dao

public interface MemoryDao {

    @Query("SELECT * FROM memories ORDER BY id DESC")
    List<Memory> getMemories();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMemory(Memory memory);

    @Delete
    void deleteMemory(Memory memory);
}
