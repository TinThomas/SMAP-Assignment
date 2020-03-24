package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity {

    Float score;

    //Names of the intent extras
    public static final String NAME_EXTRA = "name_extra";
    public static final String SCORE_EXTRA = "score_extra";
    public static final String NOTES_EXTRA = "notes_extra";
    public static final String POSITION_EXTRA = "position_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();

        score = intent.getFloatExtra(SCORE_EXTRA, 5f);

        TextView nameView = findViewById(R.id.txtName);
        final TextView scoreView = findViewById(R.id.txtScore);
        final TextView notesView = findViewById(R.id.txtNotes);

        SeekBar scoreSeek = findViewById(R.id.skbScore);

        nameView.setText(intent.getStringExtra(NAME_EXTRA));
        scoreView.setText(Float.toString(score));
        notesView.setText(intent.getStringExtra(NOTES_EXTRA));

        final int position = intent.getIntExtra(POSITION_EXTRA, 1);

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

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnOk = findViewById(R.id.btnOk);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String newNote = notesView.getText().toString();
                intent.putExtra(ListActivity.NOTE_EXTRA, newNote);

                intent.putExtra(ListActivity.SCORE_EXTRA, score);
                intent.putExtra(ListActivity.POSITION_EXTRA, position);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
