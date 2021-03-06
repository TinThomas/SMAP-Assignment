package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    final int REQUEST_EDIT = 100;

    private String wordName;

    final static String LOG = "DetailsActivity";

    private ServiceConnection wordServiceConnection;
    private WordService wordService;

    //Names of the intent extras
    public static final String NAME_EXTRA = "name_extra";

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        context = this;


        setupConnectionToBindingService();
        bindService(new Intent(DetailsActivity.this, WordService.class),
                wordServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();

        wordName = intent.getStringExtra(NAME_EXTRA);

        TextView nameView = findViewById(R.id.txtName);
        nameView.setText(wordName);


        //-------------------------------------------------------------------------
        //Set the buttons
        //-------------------------------------------------------------------------

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //No data needs to be returned, so the intent is empty
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.NAME_EXTRA, wordName);
                startActivityForResult(intent, REQUEST_EDIT);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordService.DeleteWord(wordName);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        //There's only 1 possible request code atm, but I made a switch case for consistency
        switch (requestCode){
            case REQUEST_EDIT:
                if(resultCode == RESULT_OK){
                    //Send the returned intent back to the main activity,
                    //then close this activity
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void setupConnectionToBindingService(){
        wordServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                wordService = ((WordService.WordServiceBinder)service).getService();
                Log.d(LOG, "Word service bound");

                //Based on name of word, find it in database
                Word word = wordService.GetWord(wordName);

                //Use the word to fill out information
                TextView pronunciationView = findViewById(R.id.txtPronunciation);
                pronunciationView.setText(word.getPronunciation());
                TextView descriptionView = findViewById(R.id.txtDescription);
                descriptionView.setText(word.getDescription());
                TextView notesView = findViewById(R.id.txtNotes);
                notesView.setText(word.getNotes());
                TextView scoreView = findViewById(R.id.txtScore);
                scoreView.setText(Float.toString(word.getScore()));

                ImageView imageView = findViewById(R.id.imgAnimal);

                //If the api doesn't have an image, it just returns the string "null"
                //Use of Picasso based on tutorial at
                //https://abhiandroid.com/programming/picasso#Callbacks_and_Targets_In_Picasso_In_Android
                //on 09/04-2020
                if(word.getImage_url().equals("null") || word.getImage_url() == null)
                    imageView.setImageResource(R.drawable.no_image);
                else
                    Picasso.with(context).load(word.getImage_url()).into(imageView);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                wordService = null;
                Log.d(LOG,"Word service unbound");
            }
        };
    }
}
