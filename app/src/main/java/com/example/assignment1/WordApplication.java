package com.example.assignment1;

import android.app.Application;

import androidx.room.Room;

public class WordApplication extends Application {

    private WordDatabase db;

    public WordDatabase getWordDatabase(){
            //Create a database if one does not already exist
            db = Room.databaseBuilder(getApplicationContext(),
                    WordDatabase.class, "word-database").build();

        return db;
    }
}
