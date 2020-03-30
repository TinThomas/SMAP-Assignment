package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity {

    Float score = 5f;
    String note = "";
    Word word;

    final static String LOG = "EditActivity";

    private ServiceConnection wordServiceConnection;
    private WordService wordService;

    private String wordName;

    //Names of the intent extras
    public static final String NAME_EXTRA = "name_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setupConnectionToBindingService();

        bindService(new Intent(EditActivity.this, WordService.class),
                wordServiceConnection, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();

        wordName = intent.getStringExtra(NAME_EXTRA);

        Button btnCancel = findViewById(R.id.btnCancel);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });


        TextView nameView = findViewById(R.id.txtName);
        final TextView scoreView = findViewById(R.id.txtScore);
        final TextView notesView = findViewById(R.id.txtNotes);

        nameView.setText(wordName);
        scoreView.setText(Float.toString(score));

        note = getString(R.string.no_notes);
        notesView.setText(note);

        SeekBar scoreSeek = findViewById(R.id.skbScore);

        //The score are floats, but the seekbar only takes int
        int scoreInt = Math.round(score*10);
        scoreSeek.setProgress(scoreInt);

        scoreSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                score = (float) progress/10;
                scoreView.setText(Float.toString(score));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Do nothing
            }
        });

        Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Because everything now goes through the service,
                //Nothing is needed in the intent
                Intent intent = new Intent();

                String newNote = notesView.getText().toString();

                //Update the word in the database, then return from this activity
                if(word != null){
                    wordService.UpdateWord(word, newNote, score);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });
    }

    private void setupConnectionToBindingService(){
        wordServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                wordService = ((WordService.WordServiceBinder)service).getService();
                Log.d(LOG, "Word service bound");

                word = wordService.GetWord(wordName);
                score = word.getScore();
                note = word.getNotes();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                wordService = null;
                Log.d(LOG,"Word service unbound");
            }
        };
    }
}
