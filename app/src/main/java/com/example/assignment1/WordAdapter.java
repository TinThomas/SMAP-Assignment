package com.example.assignment1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class WordAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Word> words;
    private Word word;

    public WordAdapter(Context c, ArrayList<Word> wordList){
        this.context = c;
        this.words = wordList;
    }

    @Override
    public int getCount(){
        //Returns the size of the array list
        if(words != null)
            return words.size();
        else
            return 0;
    }

    @Override
    public Word getItem(int position) {
        //Returns the item in the words list at the given position
        if(words != null)
            return words.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        //The item's ID is just its position in this case
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Only create the new views if they don't already exist
        if(convertView == null){
            LayoutInflater wordInflater =(LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = wordInflater.inflate(R.layout.word_card, null);
        }

        word = words.get(position);

        if(word != null){
            //Set the views in the card to the appropriate data
            TextView txtName = convertView.findViewById(R.id.txtName);
            TextView txtPronunciation = convertView.findViewById(R.id.txtPronunciation);
            TextView txtDescription = convertView.findViewById(R.id.txtDescription);
            TextView txtScore = convertView.findViewById(R.id.txtScore);

            txtName.setText(word.getName());
            txtPronunciation.setText(word.getPronunciation());
            txtDescription.setText(word.getDescription());
            txtScore.setText(word.getScore().toString());

            ImageView animalView = convertView.findViewById(R.id.imgAnimal);

            int imageId = ImageResourceFinder.FindImageResource(word.getName());
            animalView.setImageResource(imageId);
        }
        return convertView;
    }
}
