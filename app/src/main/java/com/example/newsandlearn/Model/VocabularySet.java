package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * VocabularySet Model - Represents a collection of vocabulary words
 * Organized by theme, topic, or level
 */
public class VocabularySet implements Parcelable {
    
    private String id;
    private String title;              // e.g., "Business English", "Travel Vocabulary"
    private String description;        // Brief description of the set
    private String level;              // A1, A2, B1, B2, C1, C2
    private String category;           // business, travel, academic, daily, etc.
    private String imageUrl;           // Cover image for the set
    
    private int wordCount;             // Total number of words in set
    private List<String> vocabularyIds; // List of vocabulary IDs in this set
    
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;          // "system" or userId for custom sets
    
    private boolean isPublic;          // Public sets vs user's private sets
    private int downloadCount;         // How many users downloaded this set
    private double rating;             // Average rating (0-5)

    // Constructors
    public VocabularySet() {
        // Required empty constructor for Firebase
        this.vocabularyIds = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublic = true;
        this.downloadCount = 0;
        this.rating = 0.0;
    }

    public VocabularySet(String id, String title, String description, String level, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.category = category;
        this.vocabularyIds = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublic = true;
        this.downloadCount = 0;
        this.rating = 0.0;
    }

    // Parcelable implementation
    protected VocabularySet(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        level = in.readString();
        category = in.readString();
        imageUrl = in.readString();
        wordCount = in.readInt();
        vocabularyIds = in.createStringArrayList();
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Date(createdAtTime) : new Date();
        long updatedAtTime = in.readLong();
        updatedAt = updatedAtTime != -1 ? new Date(updatedAtTime) : new Date();
        createdBy = in.readString();
        isPublic = in.readByte() != 0;
        downloadCount = in.readInt();
        rating = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeString(imageUrl);
        dest.writeInt(wordCount);
        dest.writeStringList(vocabularyIds);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeString(createdBy);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeInt(downloadCount);
        dest.writeDouble(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VocabularySet> CREATOR = new Creator<VocabularySet>() {
        @Override
        public VocabularySet createFromParcel(Parcel in) {
            return new VocabularySet(in);
        }

        @Override
        public VocabularySet[] newArray(int size) {
            return new VocabularySet[size];
        }
    };

    // Helper methods
    public void addVocabulary(String vocabularyId) {
        if (!vocabularyIds.contains(vocabularyId)) {
            vocabularyIds.add(vocabularyId);
            wordCount = vocabularyIds.size();
            updatedAt = new Date();
        }
    }

    public void removeVocabulary(String vocabularyId) {
        if (vocabularyIds.remove(vocabularyId)) {
            wordCount = vocabularyIds.size();
            updatedAt = new Date();
        }
    }

    public boolean containsVocabulary(String vocabularyId) {
        return vocabularyIds.contains(vocabularyId);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public List<String> getVocabularyIds() { return vocabularyIds; }
    public void setVocabularyIds(List<String> vocabularyIds) { 
        this.vocabularyIds = vocabularyIds;
        this.wordCount = vocabularyIds != null ? vocabularyIds.size() : 0;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
