package com.example.assignment1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

@Dao
public interface WordDao {

    @Insert
    void insertAll(Word... words);

    @Delete
    void delete(Word word);
}
