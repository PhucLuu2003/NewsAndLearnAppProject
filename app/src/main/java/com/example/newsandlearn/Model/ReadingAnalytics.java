package com.example.newsandlearn.Model;

import java.util.HashMap;
import java.util.Map;

public class ReadingAnalytics {
    private String userId;
    private int totalArticlesRead;
    private int totalReadingTimeMinutes;
    private int currentStreak;
    private int longestStreak;
    private Map<String, Integer> categoriesRead; // category -> count
    private Map<String, Integer> levelsRead; // level -> count
    private int vocabularyLearned;
    private int quizzesTaken;
    private double averageQuizScore;
    private String lastReadDate;

    public ReadingAnalytics() {
        this.categoriesRead = new HashMap<>();
        this.levelsRead = new HashMap<>();
    }

    public ReadingAnalytics(String userId) {
        this.userId = userId;
        this.totalArticlesRead = 0;
        this.totalReadingTimeMinutes = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.categoriesRead = new HashMap<>();
        this.levelsRead = new HashMap<>();
        this.vocabularyLearned = 0;
        this.quizzesTaken = 0;
        this.averageQuizScore = 0.0;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getTotalArticlesRead() { return totalArticlesRead; }
    public void setTotalArticlesRead(int totalArticlesRead) { this.totalArticlesRead = totalArticlesRead; }

    public int getTotalReadingTimeMinutes() { return totalReadingTimeMinutes; }
    public void setTotalReadingTimeMinutes(int totalReadingTimeMinutes) { 
        this.totalReadingTimeMinutes = totalReadingTimeMinutes; 
    }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public Map<String, Integer> getCategoriesRead() { return categoriesRead; }
    public void setCategoriesRead(Map<String, Integer> categoriesRead) { 
        this.categoriesRead = categoriesRead; 
    }

    public Map<String, Integer> getLevelsRead() { return levelsRead; }
    public void setLevelsRead(Map<String, Integer> levelsRead) { this.levelsRead = levelsRead; }

    public int getVocabularyLearned() { return vocabularyLearned; }
    public void setVocabularyLearned(int vocabularyLearned) { this.vocabularyLearned = vocabularyLearned; }

    public int getQuizzesTaken() { return quizzesTaken; }
    public void setQuizzesTaken(int quizzesTaken) { this.quizzesTaken = quizzesTaken; }

    public double getAverageQuizScore() { return averageQuizScore; }
    public void setAverageQuizScore(double averageQuizScore) { 
        this.averageQuizScore = averageQuizScore; 
    }

    public String getLastReadDate() { return lastReadDate; }
    public void setLastReadDate(String lastReadDate) { this.lastReadDate = lastReadDate; }
}
