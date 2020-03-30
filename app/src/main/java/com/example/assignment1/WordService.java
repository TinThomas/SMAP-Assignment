package com.example.assignment1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Insert;
import androidx.room.Room;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.app.Notification.PRIORITY_LOW;

public class WordService extends Service {

    final private String CHANNEL_ID = "wordServiceChannel";
    final private int NOTIFICATION_ID = 1;

    final private static String LOG = "WORDSERVICE";
    private WordDatabase db;

    public ArrayList<Word> wordList;

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
        db = Room.databaseBuilder(getApplicationContext(),
                WordDatabase.class, "word-database").build();

        wordList = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, WordService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.foreground_notification_title))
                        .setContentText(getText(R.string.foreground_notification_text))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setPriority(PRIORITY_LOW)
                        .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

    public Word GetWord(String word){
        Word retVal = new Word("Error", "Error", "Error");
        try{
             retVal = new GetWordTask().execute(word).get();
        }
        catch (InterruptedException ex){
            Log.d(LOG, "GetAllWords InterruptedException" + ex.getMessage());
        }
        catch (ExecutionException ex){
            Log.d(LOG, "GetAllWords ExecutionException" + ex.getMessage());
        }
        return retVal;
    }

    public void AddWord(Word newWord){
        new AddWordTask().execute(newWord);
    }

    public void UpdateWord(Word word, String notes, float score){
        //Update the local Word object with score and notes
        word.setScore(score);
        word.setNotes(notes);
        //Update the database with the new word
        new UpdateWordTask().execute(word);
    }

    public ArrayList<Word> GetAllWords(){
        return wordList;
    }

    public void SeedDatabase(){
        InputStream inputStream = getResources().openRawResource(R.raw.animal_list);

        //Read the csv file using the csvfilereader class
        CSVFileReader csvFileReader = new CSVFileReader(inputStream);
        ArrayList<String[]> csvOutput = csvFileReader.read();

        //Convert the output from the csv reader to Word objects
        //and add them to the database
        Word[] words = new Word[csvOutput.size()];
        int i = 0;
        for (String[] var: csvOutput) {
            Word newWord = new Word(var);
            words[i] = newWord;
            i++;
        }
        new AddWordTask().execute(words);
    }

    private class AddWordTask extends AsyncTask<Word, Void, Void>{

        @Override
        protected Void doInBackground(Word... words) {
            db.wordDao().insertAll(words);
            wordList.addAll(Arrays.asList(words));
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            broadcastDatabaseUpdated();
        }
    }

    private class UpdateWordTask extends AsyncTask<Word, Void, Void>{

        @Override
        protected Void doInBackground(Word... words) {
            db.wordDao().updateAll(words);
            wordList = new ArrayList<>(db.wordDao().getAll());
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            broadcastDatabaseUpdated();
        }
    }

//    private class GetAllWordsTask extends AsyncTask<Void, Void, Void>{
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            //getAll returns a List, which is used to populate a new ArrayList,
//            //which can then be returned
//            wordList = new ArrayList<>(db.wordDao().getAll());
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result){
//            broadcastDatabaseGot();
//        }
//    }

    private class GetWordTask extends AsyncTask<String, Void, Word>{

        @Override
        protected Word doInBackground(String... strings) {
            return db.wordDao().GetWord(strings[0]);
        }
    }

    public void InitializeList(){
        new InitializeListTask().execute();
    }

    private class InitializeListTask extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... voids) {
            wordList = (ArrayList<Word>) db.wordDao().getAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            broadcastDatabaseUpdated();
        }
    }

    //Broadcast for saying the database has been updated
    public static final String BROADCAST_DATABASE_UPDATE = "DatabaseUpdatedBroadcast";
    private void broadcastDatabaseUpdated(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_DATABASE_UPDATE);
        Log.d(LOG,"Sending database update broadcast");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    //The broadcast for saying that the database has been "gotten"
    public static final String BROADCAST_DATABASE_GOT = "DatabaseGotBroadcast";
    private void broadcastDatabaseGot(){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BROADCAST_DATABASE_GOT);
        Log.d(LOG,"Sending database got broadcast");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    /*This method from
https://developer.android.com/training/notify-user/build-notification
on 26/03-2020*/
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //Register notification with system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


