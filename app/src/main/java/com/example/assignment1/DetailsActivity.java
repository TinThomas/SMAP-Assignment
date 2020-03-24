package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    final int REQUEST_EDIT = 100;

    //Names of the intent extras
    public static final String NAME_EXTRA = "name_extra";
    public static final String PRONUNCIATION_EXTRA = "pronunciation_extra";
    public static final String DESCRIPTION_EXTRA = "description_extra";
    public static final String SCORE_EXTRA = "score_extra";
    public static final String NOTES_EXTRA = "notes_extra";
    public static final String POSITION_EXTRA = "position_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        //-------------------------------------------------------------------------
        //Set the contents of the page
        //-------------------------------------------------------------------------

        Intent intent = getIntent();

        final String name = intent.getStringExtra(NAME_EXTRA);
        final String pronunciation = intent.getStringExtra(PRONUNCIATION_EXTRA);
        final String description = intent.getStringExtra(DESCRIPTION_EXTRA);
        final String notes = intent.getStringExtra(NOTES_EXTRA);
        final float score = intent.getFloatExtra(SCORE_EXTRA, 5);

        final int position = intent.getIntExtra(POSITION_EXTRA, 1);

        TextView nameView = findViewById(R.id.txtName);
        nameView.setText(name);

        TextView pronunciationView = findViewById(R.id.txtPronunciation);
        pronunciationView.setText(pronunciation);

        TextView descriptionView = findViewById(R.id.txtDescription);
        descriptionView.setText(description);

        TextView notesView = findViewById(R.id.txtNotes);
        notesView.setText(notes);

        TextView scoreView = findViewById(R.id.txtScore);
        scoreView.setText(Float.toString(score));

        ImageView image = findViewById(R.id.imgAnimal);
        int imageId = ImageResourceFinder.FindImageResource(name);
        image.setImageResource(imageId);

        //-------------------------------------------------------------------------
        //Set the buttons
        //-------------------------------------------------------------------------

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnEdit = findViewById(R.id.btnEdit);

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
                intent.putExtra(EditActivity.NAME_EXTRA, name);
                intent.putExtra(EditActivity.SCORE_EXTRA, score);
                intent.putExtra(EditActivity.NOTES_EXTRA, notes);
                //Keep sending the position along for when we return to the main activity
                intent.putExtra(EditActivity.POSITION_EXTRA, position);
                startActivityForResult(intent, REQUEST_EDIT);
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
}
