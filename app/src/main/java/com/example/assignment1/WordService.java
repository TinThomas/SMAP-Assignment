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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Insert;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.PRIORITY_LOW;

public class WordService extends Service {

    final private String CHANNEL_ID = "wordServiceChannel";
    final private int NOTIFICATION_ID = 1;

    final private static String LOG = "WORDSERVICE";
    private WordDatabase db;

    public ArrayList<Word> wordList;

    //in milliseconds
    public Long NOTIFICATION_INTERVAL = 60000L;

    //Volley queue
    RequestQueue queue;

    private String URL = "https://owlbot.info/api/v4/dictionary/";
    private String token = "Token 1dc8ae33937b88c6341f41758939cc7fe4703ef2";

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(){
        super.onCreate();
        //This method will only run once in the service's lifetime
        db = Room.databaseBuilder(getApplicationContext(),
                WordDatabase.class, "word-database")
                .fallbackToDestructiveMigration()
                .build();

        wordList = new ArrayList<>();

        Intent notificationIntent = new Intent(this, WordService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel();

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle(getText(R.string.foreground_notification_title))
                        .setContentText(getText(R.string.foreground_notification_text))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setPriority(PRIORITY_LOW)
                        .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        startNotifications(NOTIFICATION_INTERVAL);
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

    public void DeleteWord(String word){
        Word deleteWord = GetWord(word);
        new DeleteWordTask().execute(deleteWord);
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
        //This method reworked to get data from the API instead of files
        //now it just looks silly instead...
        searchApi("lion");
        searchApi("leopard");
        searchApi("cheetah");
        searchApi("elephant");
        searchApi("giraffe");
        searchApi("kudu");
        searchApi("gnu");
        searchApi("oryx");
        searchApi("camel");
        searchApi("shark");
        searchApi("crocodile");
        searchApi("snake");
        searchApi("buffalo");
        searchApi("ostrich");
    }


    private class AddWordTask extends AsyncTask<Word, Void, Void>{

        @Override
        protected Void doInBackground(Word... words) {
            db.wordDao().insertAll(words);
            updateList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            broadcastDatabaseUpdated();
        }
    }

    private class DeleteWordTask extends AsyncTask<Word, Void, Void>{

        @Override
        protected Void doInBackground(Word... words) {
            //This method will only ever act on one word
            Word word = words[0];
            db.wordDao().delete(word);
            updateList();
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
            updateList();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            broadcastDatabaseUpdated();
        }
    }

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

    /*This method from
    https://developer.android.com/training/notify-user/build-notification
    on 26/03-2020*/
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);

            //Low importance so it doesn't make noise
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //Register notification with system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void findWord(String word){

        //Check if the word is already in the list (and thus the database)
        boolean exists = false;
        for (Word var : wordList)
        {
            if(var.getName().toLowerCase().equals(word)){
                exists = true;
                break;
            }

        }

        if(exists)
            Toast.makeText(this, "That word already exists", Toast.LENGTH_SHORT).show();
        else
            searchApi(word);
    }

    public void searchApi(String word){
        //Create a new Volley request queue if one does not already exist
        if(queue == null)
            queue = Volley.newRequestQueue(getApplicationContext());


        //-----------------------------------------------------------------------
        //Creation of JsonObjectRequest with response and error listeners
        final JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, URL + word, null,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //The structure of this part may lean heavily on Rikke Fanoe's assignment,
                //which I peer reviewed before getting Volley set up for myself
                try{
                    //There can be multiple definitions, so get all of them
                    Object definitions = response.get("definitions");
                    //Cast the object to an array, and take only the first one
                    Object firstDefinition = ((JSONArray) definitions).get(0);
                    //Get the definition text from the definition object
                    String definition = (((JSONObject)firstDefinition).getString("definition"));
                    //Get the image url from the definition object
                    String imageUrl = String.valueOf(((JSONObject) firstDefinition).getString("image_url"));

                    String name = response.getString("word");
                    String pronunciation = response.getString("pronunciation");

                    //These two can be null, in that case just make empty strings
                    if(pronunciation == null)
                        pronunciation = "";
                    if(definition == null)
                        definition = "";

                    //Create a new word object and add it to the database
                    Word newWord = new Word(name, pronunciation, definition);
                    newWord.setImage_url(imageUrl);

                    AddWord(newWord);
                }
                catch (JSONException ex){
                    Log.d(LOG, "Error getting JSON: " + ex.getMessage());
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG, "Error getting word from web: " + error);
                        Toast.makeText(getApplicationContext(), "Word not found", Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            public Map<String, String>getHeaders(){
                Map<String, String> header = new HashMap<>();
                header.put("Authorization",token);
                return header;
            }
        };
        //-----------------------------------------------------------------------

        queue.add(request);

    }

    private void updateList(){
        wordList = (ArrayList<Word>)db.wordDao().getAll();
    }

    private void startNotifications(final Long interval){
        ExecutorService es = Executors.newSingleThreadExecutor();

        es.submit(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(interval);

                        Word notificationWord = getRandomWord();

                        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                                .setContentTitle("Remember to practice!")
                                .setContentText("How about this word: " + notificationWord.getName() +"?")
                                .setSmallIcon(R.mipmap.ic_launcher_round)
                                .build();

                        startForeground(2, notification);
                    }
                    catch (InterruptedException ex){
                        Log.d(LOG, "Notification service interrupted: " + ex.getMessage());
                    }

                }

            }
        });
    }

    private Word getRandomWord(){
        if(wordList != null && wordList.size() != 0){
            int num = (int)Math.floor(Math.random()*wordList.size());
            return wordList.get(num);
        }
        //If wordlist is null or empty
        return new Word("Error", "Error", "Error");
    }
}


