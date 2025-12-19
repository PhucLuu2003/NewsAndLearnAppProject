package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * VocabularyQuiz Model - Represents a vocabulary quiz session
 */
public class VocabularyQuiz {
    
    private String id;
    private String userId;
    private String quizType; // "multiple_choice", "fill_blank", "matching", "listening", "spelling"
    private List<String> vocabularyIds; // IDs of vocabulary in this quiz
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private int score; // Percentage
    private long timeSpent; // milliseconds
    private Date startedAt;
    private Date completedAt;
    private boolean isCompleted;
    
    // Quiz results for each question
    private List<QuizResult> results;

    public VocabularyQuiz() {
        this.vocabularyIds = new ArrayList<>();
        this.results = new ArrayList<>();
        this.startedAt = new Date();
        this.isCompleted = false;
    }

    public VocabularyQuiz(String quizType, List<String> vocabularyIds) {
        this.quizType = quizType;
        this.vocabularyIds = vocabularyIds;
        this.totalQuestions = vocabularyIds.size();
        this.results = new ArrayList<>();
        this.startedAt = new Date();
        this.isCompleted = false;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
    }

    // Helper methods
    public void addResult(String vocabularyId, boolean isCorrect, long timeTaken) {
        QuizResult result = new QuizResult(vocabularyId, isCorrect, timeTaken);
        results.add(result);
        
        if (isCorrect) {
            correctAnswers++;
        } else {
            incorrectAnswers++;
        }
        
        updateScore();
    }

    public void complete() {
        this.completedAt = new Date();
        this.isCompleted = true;
        this.timeSpent = completedAt.getTime() - startedAt.getTime();
        updateScore();
    }

    private void updateScore() {
        if (totalQuestions > 0) {
            this.score = (int) ((correctAnswers * 100.0) / totalQuestions);
        }
    }

    public int getAccuracy() {
        return score;
    }

    public String getGrade() {
        if (score >= 90) return "Excellent";
        if (score >= 80) return "Very Good";
        if (score >= 70) return "Good";
        if (score >= 60) return "Fair";
        return "Need Practice";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getQuizType() { return quizType; }
    public void setQuizType(String quizType) { this.quizType = quizType; }

    public List<String> getVocabularyIds() { return vocabularyIds; }
    public void setVocabularyIds(List<String> vocabularyIds) { 
        this.vocabularyIds = vocabularyIds;
        this.totalQuestions = vocabularyIds != null ? vocabularyIds.size() : 0;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getIncorrectAnswers() { return incorrectAnswers; }
    public void setIncorrectAnswers(int incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public long getTimeSpent() { return timeSpent; }
    public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public List<QuizResult> getResults() { return results; }
    public void setResults(List<QuizResult> results) { this.results = results; }

    // Inner class for individual question results
    public static class QuizResult {
        private String vocabularyId;
        private boolean isCorrect;
        private long timeTaken; // milliseconds

        public QuizResult() {}

        public QuizResult(String vocabularyId, boolean isCorrect, long timeTaken) {
            this.vocabularyId = vocabularyId;
            this.isCorrect = isCorrect;
            this.timeTaken = timeTaken;
        }

        public String getVocabularyId() { return vocabularyId; }
        public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public long getTimeTaken() { return timeTaken; }
        public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }
    }
}
