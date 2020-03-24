package com.example.assignment1;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class Word {
    @PrimaryKey
    @NonNull
    private String name;
    private String pronunciation;
    private String description;
    private String notes;
    private Float score;

    public Word(String[] arg){
        this.name = arg[0];
        this.pronunciation = arg[1];
        this.description = arg[2];
        this.notes = "";
        this.score = 5f;
    }

    public Word(String name, String pronunciation, String description){
        this.name = name;
        this.pronunciation = pronunciation;
        this.description = description;
        this.notes = "";
        this.score = 5f;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
