package com.example.newsandlearn.Model;

import java.io.Serializable;
import java.util.List;

/**
 * MillionaireQuestion - Question model for "English Millionaire" game.
 * Stored in Firestore collection: millionaire_questions
 */
public class MillionaireQuestion implements Serializable {
    private String id;
    private int tier; // 1..15
    private String question;
    private List<String> options; // 4 options
    private int correctAnswerIndex; // 0..3

    private String explanation;
    private String hint; // used for Phone-a-friend
    private String funFact; // short humorous / educational note

    private String difficulty; // easy/medium/hard
    private String category; // vocabulary/grammar/reading/listening
    private int timeLimit; // seconds

    public MillionaireQuestion() {
        // Required for Firestore
    }

    public MillionaireQuestion(
            String id,
            int tier,
            String question,
            List<String> options,
            int correctAnswerIndex,
            String explanation,
            String hint,
            String funFact,
            String difficulty,
            String category,
            int timeLimit) {
        this.id = id;
        this.tier = tier;
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
        this.hint = hint;
        this.funFact = funFact;
        this.difficulty = difficulty;
        this.category = category;
        this.timeLimit = timeLimit;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctAnswerIndex;
    }

    public String getCorrectAnswer() {
        if (options == null)
            return "";
        if (correctAnswerIndex < 0 || correctAnswerIndex >= options.size())
            return "";
        return options.get(correctAnswerIndex);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getFunFact() {
        return funFact;
    }

    public void setFunFact(String funFact) {
        this.funFact = funFact;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
}
