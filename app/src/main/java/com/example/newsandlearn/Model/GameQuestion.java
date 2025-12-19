package com.example.newsandlearn.Model;

import java.io.Serializable;
import java.util.List;

/**
 * GameQuestion - Câu hỏi trong game RPG
 */
public class GameQuestion implements Serializable {
    private String id;
    private String question;
    private List<String> options; // 4 đáp án
    private int correctAnswerIndex; // Index của đáp án đúng (0-3)
    private String explanation; // Giải thích đáp án
    private String difficulty; // "easy", "medium", "hard"
    private String category; // "vocabulary", "grammar", "listening"
    private String audioUrl; // Cho câu hỏi listening
    private int timeLimit; // Giây

    public GameQuestion() {
        // Default constructor for Firebase
    }

    public GameQuestion(String id, String question, List<String> options, 
                       int correctAnswerIndex, String difficulty, String category) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.difficulty = difficulty;
        this.category = category;
        this.timeLimit = 30; // Default 30 seconds
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctAnswerIndex;
    }

    public String getCorrectAnswer() {
        if (options != null && correctAnswerIndex >= 0 && correctAnswerIndex < options.size()) {
            return options.get(correctAnswerIndex);
        }
        return "";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { 
        this.correctAnswerIndex = correctAnswerIndex; 
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
}
