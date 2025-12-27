package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

public class StoryGameResult {
    private String storyId;
    private String topic;
    private int totalWords;
    private int correctAnswers;
    private int wrongAnswers;
    private long timeSpentSeconds;
    private int xpEarned;
    private List<String> learnedWords;
    private long completedAt;

    public StoryGameResult() {
        this.learnedWords = new ArrayList<>();
        this.completedAt = System.currentTimeMillis();
    }

    public StoryGameResult(String storyId, String topic, int totalWords, int correctAnswers, 
                          int wrongAnswers, long timeSpentSeconds) {
        this.storyId = storyId;
        this.topic = topic;
        this.totalWords = totalWords;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.timeSpentSeconds = timeSpentSeconds;
        this.learnedWords = new ArrayList<>();
        this.completedAt = System.currentTimeMillis();
        calculateXP();
    }

    // Calculate XP based on performance
    private void calculateXP() {
        int baseXP = 100;
        int correctBonus = correctAnswers * 50;
        int perfectBonus = (correctAnswers == totalWords) ? 100 : 0;
        int speedBonus = (timeSpentSeconds < 120) ? 50 : 0; // Bonus if completed under 2 minutes
        
        this.xpEarned = baseXP + correctBonus + perfectBonus + speedBonus;
    }

    // Getters and Setters
    public String getStoryId() { return storyId; }
    public void setStoryId(String storyId) { this.storyId = storyId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getTotalWords() { return totalWords; }
    public void setTotalWords(int totalWords) { this.totalWords = totalWords; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { 
        this.correctAnswers = correctAnswers;
        calculateXP();
    }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public long getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(long timeSpentSeconds) { 
        this.timeSpentSeconds = timeSpentSeconds;
        calculateXP();
    }

    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }

    public List<String> getLearnedWords() { return learnedWords; }
    public void setLearnedWords(List<String> learnedWords) { this.learnedWords = learnedWords; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    // Helper methods
    public int getAccuracyPercentage() {
        if (totalWords == 0) return 0;
        return (int) ((correctAnswers * 100.0) / totalWords);
    }

    public String getFormattedTime() {
        long minutes = timeSpentSeconds / 60;
        long seconds = timeSpentSeconds % 60;
        return String.format("%dm %ds", minutes, seconds);
    }

    public boolean isPerfectScore() {
        return correctAnswers == totalWords;
    }
}
