package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Vocabulary Model - Represents a single vocabulary word
 * Implements Spaced Repetition System (SRS) for effective learning
 */
public class Vocabulary implements Parcelable {
    
    // Basic Information
    private String id;
    private String word;              // English word
    private String translation;       // Vietnamese translation
    private String pronunciation;     // IPA or phonetic spelling
    private String partOfSpeech;      // noun, verb, adjective, etc.
    private String example;           // Example sentence
    private String exampleTranslation; // Vietnamese translation of example
    
    // Categorization
    private String level;             // A1, A2, B1, B2, C1, C2
    private String category;          // topic/theme (business, travel, etc.)
    private String sourceArticleId;   // ID of article where word was found (optional)
    
    // Learning Progress (SRS - Spaced Repetition System)
    private int mastery;              // 0-5 (0=new, 1-4=learning, 5=mastered)
    private int reviewCount;          // Number of times reviewed
    private Date lastReviewed;        // Last review timestamp
    private Date nextReview;          // Next scheduled review
    private Date createdAt;           // When word was added
    
    // Additional
    private boolean isFavorite;       // User marked as favorite
    private String imageUrl;          // Optional image for visual learning
    private String audioUrl;          // Optional audio pronunciation URL

    // Constructors
    public Vocabulary() {
        // Required empty constructor for Firebase
        this.createdAt = new Date();
        this.mastery = 0;
        this.reviewCount = 0;
        this.isFavorite = false;
    }

    public Vocabulary(String id, String word, String translation, String pronunciation,
                     String partOfSpeech, String example, String exampleTranslation,
                     String level, String category) {
        this.id = id;
        this.word = word;
        this.translation = translation;
        this.pronunciation = pronunciation;
        this.partOfSpeech = partOfSpeech;
        this.example = example;
        this.exampleTranslation = exampleTranslation;
        this.level = level;
        this.category = category;
        this.createdAt = new Date();
        this.mastery = 0;
        this.reviewCount = 0;
        this.isFavorite = false;
    }

    // Parcelable implementation
    protected Vocabulary(Parcel in) {
        id = in.readString();
        word = in.readString();
        translation = in.readString();
        pronunciation = in.readString();
        partOfSpeech = in.readString();
        example = in.readString();
        exampleTranslation = in.readString();
        level = in.readString();
        category = in.readString();
        sourceArticleId = in.readString();
        mastery = in.readInt();
        reviewCount = in.readInt();
        long lastReviewedTime = in.readLong();
        lastReviewed = lastReviewedTime != -1 ? new Date(lastReviewedTime) : null;
        long nextReviewTime = in.readLong();
        nextReview = nextReviewTime != -1 ? new Date(nextReviewTime) : null;
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Date(createdAtTime) : new Date();
        isFavorite = in.readByte() != 0;
        imageUrl = in.readString();
        audioUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(word);
        dest.writeString(translation);
        dest.writeString(pronunciation);
        dest.writeString(partOfSpeech);
        dest.writeString(example);
        dest.writeString(exampleTranslation);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeString(sourceArticleId);
        dest.writeInt(mastery);
        dest.writeInt(reviewCount);
        dest.writeLong(lastReviewed != null ? lastReviewed.getTime() : -1);
        dest.writeLong(nextReview != null ? nextReview.getTime() : -1);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeString(imageUrl);
        dest.writeString(audioUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Vocabulary> CREATOR = new Creator<Vocabulary>() {
        @Override
        public Vocabulary createFromParcel(Parcel in) {
            return new Vocabulary(in);
        }

        @Override
        public Vocabulary[] newArray(int size) {
            return new Vocabulary[size];
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
    
    /**
     * Update mastery when user answers correctly
     */
    public void markCorrect() {
        if (mastery < 5) {
            mastery++;
        }
        reviewCount++;
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

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }

    public String getExampleTranslation() { return exampleTranslation; }
    public void setExampleTranslation(String exampleTranslation) { this.exampleTranslation = exampleTranslation; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceArticleId() { return sourceArticleId; }
    public void setSourceArticleId(String sourceArticleId) { this.sourceArticleId = sourceArticleId; }

    public int getMastery() { return mastery; }
    public void setMastery(int mastery) { this.mastery = mastery; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public Date getLastReviewed() { return lastReviewed; }
    public void setLastReviewed(Date lastReviewed) { this.lastReviewed = lastReviewed; }

    public Date getNextReview() { return nextReview; }
    public void setNextReview(Date nextReview) { this.nextReview = nextReview; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
