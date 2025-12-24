package com.example.newsandlearn.Model;

public class VocabularyWord {
    private String word;
    private String pronunciation;
    private String meaning;
    private String example;
    private int position; // Position in story where word appears

    public VocabularyWord() {
        // Required empty constructor
    }

    public VocabularyWord(String word, String pronunciation, String meaning, String example, int position) {
        this.word = word;
        this.pronunciation = pronunciation;
        this.meaning = meaning;
        this.example = example;
        this.position = position;
    }

    // Getters and Setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
