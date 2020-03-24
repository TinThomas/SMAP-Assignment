package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.InputStream;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private WordAdapter wordAdapter;
    private ListView wordListView;
    private ArrayList<Word> wordList;

    final private int REQUEST_VIEW = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        InputStream inputStream = getResources().openRawResource(R.raw.animal_list);

        //Read the csv file using the csvfilereader class
        CSVFileReader csvFileReader = new CSVFileReader(inputStream);
        ArrayList<String[]> csvOutput = csvFileReader.read();
        wordList = new ArrayList<>();

        //Convert the output from the csv reader to Word objects
        for (String[] var: csvOutput) {
            wordList.add(new Word(var));
        }

        wordAdapter = new WordAdapter(this, wordList);
        wordListView = findViewById(R.id.listAnimals);
        wordListView.setAdapter(wordAdapter);

        //Get preferences
        /*Note: In hindsight, the assignment implies that the intent is to use
        a saved instance state instead of preferences. This way allows the app
        to save notes and scores even when it's shut down, though, so I'm going
        to leave it like this
         */
        getData();

        wordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Word word = wordList.get(position);
                Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.NAME_EXTRA, word.getName());
                intent.putExtra(DetailsActivity.PRONUNCIATION_EXTRA, word.getPronunciation());
                intent.putExtra(DetailsActivity.DESCRIPTION_EXTRA, word.getDescription());
                intent.putExtra(DetailsActivity.SCORE_EXTRA, word.getScore());
                intent.putExtra(DetailsActivity.NOTES_EXTRA, word.getNotes());

                //The position makes it easier to handle the returned values in
                //onActivityResult
                intent.putExtra(DetailsActivity.POSITION_EXTRA, position);

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
    }

    //Intent extras for returning from editActivity
    public static final String POSITION_EXTRA = "position_extra";
    public static final String NOTE_EXTRA = "note_extra";
    public static final String SCORE_EXTRA = "score_extra";


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);

        switch (requestCode){
            case REQUEST_VIEW:
                if(resultCode == RESULT_OK){
                    int position = data.getIntExtra(POSITION_EXTRA, 0);

                    Word editWord = wordAdapter.getItem(position);
                    editWord.setNotes(data.getStringExtra(NOTE_EXTRA));
                    editWord.setScore(data.getFloatExtra(SCORE_EXTRA, 5));
                    wordAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        saveData();
    }

    private void saveData(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //Go through all of the words, saving notes and scores
        for (int i = 0; i < wordAdapter.getCount(); i++){
            String noteKey = "note_" + i;
            String notes = wordAdapter.getItem(i).getNotes();
            String scoreKey = "score_" + i;
            float score = wordAdapter.getItem(i).getScore();

            editor.putString(noteKey, notes);
            editor.putFloat(scoreKey, score);
        }
        editor.apply();
    }

    private void getData(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        for (int i = 0; i < wordAdapter.getCount(); i++){
            String noteKey = "note_" + i;
            String scoreKey = "score_" + i;

            //Get the note by key, otherwise use a default value if not found
            String note = sharedPref.getString(noteKey,
                    getResources().getString(R.string.no_notes));

            float score = sharedPref.getFloat(scoreKey, 5);

            //Fetch the word at the current position
            Word word = wordAdapter.getItem(i);

            word.setScore(score);
            word.setNotes(note);
        }
        wordAdapter.notifyDataSetChanged();
    }
}
