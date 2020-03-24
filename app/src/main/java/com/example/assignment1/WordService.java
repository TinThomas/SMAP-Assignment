package com.example.assignment1;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.room.Room;

public class WordService extends Service {

    private WordDatabase db;
    public WordService() {
    }

    //Extends binder class, we will return instance of this in onBind()
    public class WordServiceBinder extends Binder {
        WordService getService(){
            //Return a reference to the service that the activity can call public methods on
            return WordService.this;
        }
    }

    //The instance to return (from above)
    private final IBinder binder = new WordServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //This method will only run once in the service's lifetime

        //Create a database if one does not already exist
        db = Room.databaseBuilder(getApplicationContext(),
                WordDatabase.class, "word-database").build();
    }
}
