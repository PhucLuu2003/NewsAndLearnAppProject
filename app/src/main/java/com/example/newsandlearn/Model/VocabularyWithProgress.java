package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * VocabularyWithProgress - Combined model for UI display
 * Combines public Vocabulary data with user's learning progress
 * This is used in adapters and fragments to display vocabulary with progress
 */
public class VocabularyWithProgress implements Parcelable {
    
    private Vocabulary vocabulary;          // Public vocabulary data
    private UserVocabulary userProgress;    // User's learning progress

    // Constructors
    public VocabularyWithProgress() {
        // Required empty constructor
    }

    public VocabularyWithProgress(Vocabulary vocabulary, UserVocabulary userProgress) {
        this.vocabulary = vocabulary;
        this.userProgress = userProgress;
    }

    // Parcelable implementation
    protected VocabularyWithProgress(Parcel in) {
        vocabulary = in.readParcelable(Vocabulary.class.getClassLoader());
        userProgress = in.readParcelable(UserVocabulary.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(vocabulary, flags);
        dest.writeParcelable(userProgress, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VocabularyWithProgress> CREATOR = new Creator<VocabularyWithProgress>() {
        @Override
        public VocabularyWithProgress createFromParcel(Parcel in) {
            return new VocabularyWithProgress(in);
        }

        @Override
        public VocabularyWithProgress[] newArray(int size) {
            return new VocabularyWithProgress[size];
        }
    };

    // Convenience methods for accessing vocabulary data
    public String getId() {
        return vocabulary != null ? vocabulary.getId() : null;
    }

    public String getWord() {
        return vocabulary != null ? vocabulary.getWord() : "";
    }

    public String getTranslation() {
        return vocabulary != null ? vocabulary.getTranslation() : "";
    }

    public String getPronunciation() {
        return vocabulary != null ? vocabulary.getPronunciation() : "";
    }

    public String getPartOfSpeech() {
        return vocabulary != null ? vocabulary.getPartOfSpeech() : "";
    }

    public String getDefinition() {
        return vocabulary != null ? vocabulary.getDefinition() : "";
    }

    public String getExample() {
        return vocabulary != null ? vocabulary.getExample() : "";
    }

    public String getExampleTranslation() {
        return vocabulary != null ? vocabulary.getExampleTranslation() : "";
    }

    public String getLevel() {
        return vocabulary != null ? vocabulary.getLevel() : "";
    }

    public String getCategory() {
        return vocabulary != null ? vocabulary.getCategory() : "";
    }

    public List<String> getSynonyms() {
        return vocabulary != null ? vocabulary.getSynonyms() : null;
    }

    public List<String> getAntonyms() {
        return vocabulary != null ? vocabulary.getAntonyms() : null;
    }

    public String getImageUrl() {
        return vocabulary != null ? vocabulary.getImageUrl() : null;
    }

    public String getAudioUrl() {
        return vocabulary != null ? vocabulary.getAudioUrl() : null;
    }

    // Convenience methods for accessing user progress data
    public int getMastery() {
        return userProgress != null ? userProgress.getMastery() : 0;
    }

    public String getMasteryLevel() {
        return userProgress != null ? userProgress.getMasteryLevel() : "New";
    }

    public int getReviewCount() {
        return userProgress != null ? userProgress.getReviewCount() : 0;
    }

    public int getCorrectCount() {
        return userProgress != null ? userProgress.getCorrectCount() : 0;
    }

    public int getIncorrectCount() {
        return userProgress != null ? userProgress.getIncorrectCount() : 0;
    }

    public int getAccuracy() {
        return userProgress != null ? userProgress.getAccuracy() : 0;
    }

    public Date getLastReviewed() {
        return userProgress != null ? userProgress.getLastReviewed() : null;
    }

    public Date getNextReview() {
        return userProgress != null ? userProgress.getNextReview() : null;
    }

    public boolean isFavorite() {
        return userProgress != null && userProgress.isFavorite();
    }

    public Date getAddedAt() {
        return userProgress != null ? userProgress.getAddedAt() : null;
    }

    public String getSourceArticleId() {
        return userProgress != null ? userProgress.getSourceArticleId() : null;
    }

    public String getSourceType() {
        return userProgress != null ? userProgress.getSourceType() : "manual";
    }

    public String getNotes() {
        return userProgress != null ? userProgress.getNotes() : "";
    }

    public List<String> getTags() {
        return userProgress != null ? userProgress.getTags() : null;
    }

    public boolean needsReview() {
        return userProgress != null && userProgress.needsReview();
    }

    // Getters and Setters
    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public UserVocabulary getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(UserVocabulary userProgress) {
        this.userProgress = userProgress;
    }
}
