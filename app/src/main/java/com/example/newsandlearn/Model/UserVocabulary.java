package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UserVocabulary Model - Represents user's learning progress for a vocabulary word
 * Stored in 'users/{userId}/user_vocabulary' collection
 * Contains SRS (Spaced Repetition System) data and user-specific information
 */
public class UserVocabulary implements Parcelable {
    
    private String id;                // Document ID (same as vocabularyId for easy reference)
    private String vocabularyId;      // Reference to vocabularies/{vocabId}
    
    // Learning Progress (SRS - Spaced Repetition System)
    private int mastery;              // 0-5 (0=new, 1-4=learning, 5=mastered)
    private int reviewCount;          // Total number of times reviewed
    private int correctCount;         // Number of correct answers
    private int incorrectCount;       // Number of incorrect answers
    private Date lastReviewed;        // Last review timestamp
    private Date nextReview;          // Next scheduled review
    
    // User-specific data
    private boolean isFavorite;       // User marked as favorite
    private Date addedAt;             // When user added this word
    private String sourceArticleId;   // ID of article where word was found (optional)
    private String sourceType;        // "article", "manual", "set"
    private String notes;             // User's personal notes
    private List<String> tags;        // User's custom tags

    // Constructors
    public UserVocabulary() {
        // Required empty constructor for Firebase
        this.addedAt = new Date();
        this.mastery = 0;
        this.reviewCount = 0;
        this.correctCount = 0;
        this.incorrectCount = 0;
        this.isFavorite = false;
        this.tags = new ArrayList<>();
        this.sourceType = "manual";
    }

    public UserVocabulary(String vocabularyId) {
        this.vocabularyId = vocabularyId;
        this.id = vocabularyId; // Use same ID for easy reference
        this.addedAt = new Date();
        this.mastery = 0;
        this.reviewCount = 0;
        this.correctCount = 0;
        this.incorrectCount = 0;
        this.isFavorite = false;
        this.tags = new ArrayList<>();
        this.sourceType = "manual";
        calculateNextReview();
    }

    // Parcelable implementation
    protected UserVocabulary(Parcel in) {
        id = in.readString();
        vocabularyId = in.readString();
        mastery = in.readInt();
        reviewCount = in.readInt();
        correctCount = in.readInt();
        incorrectCount = in.readInt();
        long lastReviewedTime = in.readLong();
        lastReviewed = lastReviewedTime != -1 ? new Date(lastReviewedTime) : null;
        long nextReviewTime = in.readLong();
        nextReview = nextReviewTime != -1 ? new Date(nextReviewTime) : null;
        isFavorite = in.readByte() != 0;
        long addedAtTime = in.readLong();
        addedAt = addedAtTime != -1 ? new Date(addedAtTime) : new Date();
        sourceArticleId = in.readString();
        sourceType = in.readString();
        notes = in.readString();
        tags = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(vocabularyId);
        dest.writeInt(mastery);
        dest.writeInt(reviewCount);
        dest.writeInt(correctCount);
        dest.writeInt(incorrectCount);
        dest.writeLong(lastReviewed != null ? lastReviewed.getTime() : -1);
        dest.writeLong(nextReview != null ? nextReview.getTime() : -1);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeLong(addedAt != null ? addedAt.getTime() : -1);
        dest.writeString(sourceArticleId);
        dest.writeString(sourceType);
        dest.writeString(notes);
        dest.writeStringList(tags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserVocabulary> CREATOR = new Creator<UserVocabulary>() {
        @Override
        public UserVocabulary createFromParcel(Parcel in) {
            return new UserVocabulary(in);
        }

        @Override
        public UserVocabulary[] newArray(int size) {
            return new UserVocabulary[size];
        }
    };

    // SRS Helper Methods
    
    /**
     * Calculate next review date based on mastery level
     * Uses spaced repetition intervals
     */
    public void calculateNextReview() {
        if (lastReviewed == null) {
            lastReviewed = new Date();
        }
        
        long currentTime = lastReviewed.getTime();
        long intervalMillis;
        
        // Spaced repetition intervals based on mastery
        switch (mastery) {
            case 0: // New word
                intervalMillis = 0; // Review immediately
                break;
            case 1: // Just learned
                intervalMillis = 1000L * 60 * 60 * 24; // 1 day
                break;
            case 2: // Getting familiar
                intervalMillis = 1000L * 60 * 60 * 24 * 3; // 3 days
                break;
            case 3: // Know it
                intervalMillis = 1000L * 60 * 60 * 24 * 7; // 1 week
                break;
            case 4: // Know it well
                intervalMillis = 1000L * 60 * 60 * 24 * 14; // 2 weeks
                break;
            case 5: // Mastered
                intervalMillis = 1000L * 60 * 60 * 24 * 30; // 1 month
                break;
            default:
                intervalMillis = 1000L * 60 * 60 * 24; // Default 1 day
        }
        
        nextReview = new Date(currentTime + intervalMillis);
    }
    
    // Alias for seeder compatibility
    public void calculateNextReviewDate() { calculateNextReview(); }
    
    /**
     * Update mastery when user answers correctly
     */
    public void markCorrect() {
        if (mastery < 5) {
            mastery++;
        }
        reviewCount++;
        correctCount++;
        lastReviewed = new Date();
        calculateNextReview();
    }
    
    /**
     * Update mastery when user answers incorrectly
     */
    public void markIncorrect() {
        if (mastery > 0) {
            mastery = Math.max(0, mastery - 1);
        }
        reviewCount++;
        incorrectCount++;
        lastReviewed = new Date();
        calculateNextReview();
    }
    
    /**
     * Check if word needs review
     */
    public boolean needsReview() {
        if (nextReview == null) {
            return true;
        }
        return new Date().after(nextReview);
    }
    
    /**
     * Get mastery level as string
     */
    public String getMasteryLevel() {
        switch (mastery) {
            case 0: return "New";
            case 1: return "Learning";
            case 2: return "Familiar";
            case 3: return "Known";
            case 4: return "Well Known";
            case 5: return "Mastered";
            default: return "Unknown";
        }
    }

    /**
     * Get accuracy percentage
     */
    public int getAccuracy() {
        if (reviewCount == 0) return 0;
        return (int) ((correctCount * 100.0) / reviewCount);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(String vocabularyId) { 
        this.vocabularyId = vocabularyId;
        if (this.id == null) {
            this.id = vocabularyId;
        }
    }

    public int getMastery() { return mastery; }
    public void setMastery(int mastery) { this.mastery = mastery; }
    
    // Alias for seeder compatibility
    public void setLevel(String level) {
        if (level == null) return;
        switch(level.toLowerCase()) {
            case "mastered":
                this.mastery = 5;
                break;
            case "learning":
                this.mastery = 1;
                break;
            case "new":
                this.mastery = 0;
                break;
            default:
                // No change
                break;
        }
    }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }

    public int getIncorrectCount() { return incorrectCount; }
    public void setIncorrectCount(int incorrectCount) { this.incorrectCount = incorrectCount; }

    public Date getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(Date lastReviewed) { this.lastReviewed = lastReviewed; }
    
    // Alias for seeder compatibility
    public void setLastReviewedAt(Date date) { setLastReviewed(date); }

    public Date getNextReview() { return nextReview; }
    public void setNextReview(Date nextReview) { this.nextReview = nextReview; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    // Alias for seeder compatibility
    public void setIsFavorite(boolean favorite) { setFavorite(favorite); }

    public Date getAddedAt() { return addedAt; }
    public void setAddedAt(Date addedAt) { this.addedAt = addedAt; }

    public String getSourceArticleId() { return sourceArticleId; }
    public void setSourceArticleId(String sourceArticleId) { this.sourceArticleId = sourceArticleId; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
