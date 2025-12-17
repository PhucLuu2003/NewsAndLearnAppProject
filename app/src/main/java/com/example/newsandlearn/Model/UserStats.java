package com.example.newsandlearn.Model;

import java.util.HashMap;
import java.util.Map;

public class UserStats {
    private int wordsLearned;
    private int lessonsCompleted;
    private int quizAccuracy; // percentage
    private int avgStudyTime; // in minutes per day
    private int totalQuizzesTaken;
    private int totalCorrectAnswers;
    private int totalStudyMinutes;

    public UserStats() {
        this.wordsLearned = 0;
        this.lessonsCompleted = 0;
        this.quizAccuracy = 0;
        this.avgStudyTime = 0;
        this.totalQuizzesTaken = 0;
        this.totalCorrectAnswers = 0;
        this.totalStudyMinutes = 0;
    }

    public UserStats(int wordsLearned, int lessonsCompleted, int quizAccuracy, int avgStudyTime) {
        this.wordsLearned = wordsLearned;
        this.lessonsCompleted = lessonsCompleted;
        this.quizAccuracy = quizAccuracy;
        this.avgStudyTime = avgStudyTime;
        this.totalQuizzesTaken = 0;
        this.totalCorrectAnswers = 0;
        this.totalStudyMinutes = 0;
    }

    // Getters and Setters
    public int getWordsLearned() {
        return wordsLearned;
    }

    public void setWordsLearned(int wordsLearned) {
        this.wordsLearned = wordsLearned;
    }

    public int getLessonsCompleted() {
        return lessonsCompleted;
    }

    public void setLessonsCompleted(int lessonsCompleted) {
        this.lessonsCompleted = lessonsCompleted;
    }

    public int getQuizAccuracy() {
        return quizAccuracy;
    }

    public void setQuizAccuracy(int quizAccuracy) {
        this.quizAccuracy = quizAccuracy;
    }

    public int getAvgStudyTime() {
        return avgStudyTime;
    }

    public void setAvgStudyTime(int avgStudyTime) {
        this.avgStudyTime = avgStudyTime;
    }

    public int getTotalQuizzesTaken() {
        return totalQuizzesTaken;
    }

    public void setTotalQuizzesTaken(int totalQuizzesTaken) {
        this.totalQuizzesTaken = totalQuizzesTaken;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public int getTotalStudyMinutes() {
        return totalStudyMinutes;
    }

    public void setTotalStudyMinutes(int totalStudyMinutes) {
        this.totalStudyMinutes = totalStudyMinutes;
    }

    // Calculate quiz accuracy from total quizzes and correct answers
    public void calculateQuizAccuracy() {
        if (totalQuizzesTaken > 0) {
            this.quizAccuracy = (totalCorrectAnswers * 100) / totalQuizzesTaken;
        }
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("wordsLearned", wordsLearned);
        map.put("lessonsCompleted", lessonsCompleted);
        map.put("quizAccuracy", quizAccuracy);
        map.put("avgStudyTime", avgStudyTime);
        map.put("totalQuizzesTaken", totalQuizzesTaken);
        map.put("totalCorrectAnswers", totalCorrectAnswers);
        map.put("totalStudyMinutes", totalStudyMinutes);
        return map;
    }
}
