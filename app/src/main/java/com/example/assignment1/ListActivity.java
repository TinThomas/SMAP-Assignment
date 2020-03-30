package com.example.assignment1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private WordAdapter wordAdapter;
    private ListView wordListView;

    final private int REQUEST_VIEW = 100;

    public ArrayList<Word> wordList;

    private ServiceConnection wordServiceConnection;
    private WordService wordService;

    final String PREFS_NAME = "SharedPrefs";
    final String FIRST_LAUNCH = "firstLaunch";
    private boolean first_launch = true;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEdit;

    final private String CHANNEL_ID = "wordServiceChannel";
    final private int NOTIFICATION_ID = 1;

    private String LOG = "MAIN";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


        //Starting the background service
        setupConnectionToBindingService();

        startService(new Intent(this, WordService.class));

        bindService(new Intent(this, WordService.class),
                wordServiceConnection, Context.BIND_AUTO_CREATE);

//        Intent notificationIntent = new Intent(this, WordService.class);
//        PendingIntent pendingIntent =
//                PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        createNotificationChannel();
//
//        Notification notification =
//                new Notification.Builder(this, CHANNEL_ID)
//                        .setContentTitle(getText(R.string.notification_title))
//                        .setContentText(getText(R.string.placeholder_text))
//                        .setSmallIcon(R.drawable.lion)
//                        .setContentIntent(pendingIntent)
//                        .setTicker(getText(R.string.placeholder_text))
//                        .build();
//
//        WordService.startForeground(NOTIFICATION_ID, notification);

        wordList = new ArrayList<>();

        wordAdapter = new WordAdapter(ListActivity.this, wordList);
        wordListView = findViewById(R.id.listWords);
        wordListView.setAdapter(wordAdapter);


        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Word word = wordAdapter.getItem(position);
                String name = word.getName();
                Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.NAME_EXTRA, name);

                startActivityForResult(intent, REQUEST_VIEW);
            }
        });


        Button btnExit = findViewById(R.id.btnExitMain);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefEdit = sharedPref.edit();

        Log.d(LOG, "onCreate complete");

    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(LOG, "Onstart running");

        IntentFilter updateFilter = new IntentFilter();
        updateFilter.addAction(WordService.BROADCAST_DATABASE_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onDatabaseUpdate, updateFilter);

        IntentFilter gotFilter = new IntentFilter();
        gotFilter.addAction(WordService.BROADCAST_DATABASE_GOT);
        LocalBroadcastManager.getInstance(this).registerReceiver(onDatabaseGot, gotFilter);

//        IntentFilter getWordsFilter = new IntentFilter();
//        getWordsFilter.addAction(WordService.BROADCAST_DATABASE_GOT);
//        LocalBroadcastManager.getInstance(this).registerReceiver(onDatabaseGot, getWordsFilter);
    }

    private void setupConnectionToBindingService(){
        wordServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                wordService = ((WordService.WordServiceBinder)service).getService();
                Log.d(LOG, "Word service bound");

                if(sharedPref.getBoolean(FIRST_LAUNCH, true)){
                    wordService.SeedDatabase();
                    prefEdit.putBoolean(FIRST_LAUNCH, false);
                }

                //This makes the wordservice update the (initially empty) list of words
                //with what's in the database, then broadcast database updated
                wordService.InitializeList();
                Log.d(LOG, "Database seeded");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                wordService = null;
                Log.d(LOG,"Word service unbound");
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);

        switch (requestCode){
            case REQUEST_VIEW:
                if(resultCode == RESULT_OK){
                    Log.d(LOG, "ActivityResult request view, ok");
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onStop(){
        //This override no longer needed
        super.onStop();
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

    //Whenever a local broadcast is received, update the wordList with new data
    private BroadcastReceiver onDatabaseUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG, "Received update broadcast");

            updateWordList();
            wordAdapter.notifyDataSetChanged();

        }
    };
    //Whenever a the wordlist has been updated, notify the view that data has changed
    private BroadcastReceiver onDatabaseGot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG, "Received database got broadcast");
            ArrayList<Word> temp = wordService.GetAllWords();
            for(Word var:temp){
                wordList.add(var);
            }

            wordAdapter.notifyDataSetChanged();
        }
    };

    private void updateWordList(){
        //This seems like a dumb way to do it, but nothing else works
        ArrayList<Word> temp = wordService.GetAllWords();
        wordList.clear();
        for(Word var:temp){
            wordList.add(var);
        }
        wordAdapter.notifyDataSetChanged();

    }
}
