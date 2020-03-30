package com.example.assignment1;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Word... words);

    @Delete
    void delete(Word word);

    @Update
    void updateAll(Word... words);

    @Query("SELECT * FROM word")
    List<Word> getAll();

    @Query("SELECT * FROM word WHERE name LIKE :searchWord")
    Word GetWord(String searchWord);
}
