package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserSet Model - Represents user's progress for a vocabulary set
 * Stored in 'users/{userId}/user_sets' collection
 */
public class UserSet {
    
    private String id;                  // Document ID (same as setId)
    private String setId;               // Reference to vocabulary_sets/{setId}
    private Date downloadedAt;          // When user downloaded/added this set
    private int progress;               // Number of words learned
    private int totalWords;             // Total words in set
    private List<String> completedWords; // List of vocabulary IDs completed
    private Date lastStudied;           // Last time user studied this set
    private boolean isCompleted;        // All words mastered

    // Constructors
    public UserSet() {
        // Required empty constructor for Firebase
        this.downloadedAt = new Date();
        this.progress = 0;
        this.totalWords = 0;
        this.completedWords = new ArrayList<>();
        this.isCompleted = false;
    }

    public UserSet(String setId) {
        this.setId = setId;
        this.id = setId; // Use same ID for easy reference
        this.downloadedAt = new Date();
        this.progress = 0;
        this.totalWords = 0;
        this.completedWords = new ArrayList<>();
        this.isCompleted = false;
    }

    // Helper methods
    public void markWordCompleted(String vocabularyId) {
        if (!completedWords.contains(vocabularyId)) {
            completedWords.add(vocabularyId);
            progress = completedWords.size();
            lastStudied = new Date();
            
            if (progress >= totalWords && totalWords > 0) {
                isCompleted = true;
            }
        }
    }

    public void markWordIncomplete(String vocabularyId) {
        if (completedWords.remove(vocabularyId)) {
            progress = completedWords.size();
            lastStudied = new Date();
            isCompleted = false;
        }
    }

    public int getProgressPercentage() {
        if (totalWords == 0) return 0;
        return (int) ((progress * 100.0) / totalWords);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSetId() { return setId; }
    public void setSetId(String setId) { 
        this.setId = setId;
        if (this.id == null) {
            this.id = setId;
        }
    }

    public Date getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(Date downloadedAt) { this.downloadedAt = downloadedAt; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getTotalWords() { return totalWords; }
    public void setTotalWords(int totalWords) { this.totalWords = totalWords; }

    public List<String> getCompletedWords() { return completedWords; }
    public void setCompletedWords(List<String> completedWords) { 
        this.completedWords = completedWords;
        this.progress = completedWords != null ? completedWords.size() : 0;
    }

    public Date getLastStudied() { return lastStudied; }
    public void setLastStudied(Date lastStudied) { this.lastStudied = lastStudied; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
