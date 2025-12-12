package com.example.newsandlearn.Model;

import com.google.firebase.firestore.Exclude;
import java.util.List;

/**
 * Model class for Video Lessons with interactive questions
 * Supports Firebase Firestore integration
 */
public class VideoLesson {
    private String id;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private String level; // A1, A2, B1, B2, C1, C2
    private String category;
    private int duration; // in seconds
    private List<Question> questions;
    private long createdAt;
    private int views;
    private boolean isFavorite;

    // Empty constructor required for Firestore
    public VideoLesson() {
    }

    public VideoLesson(String id, String title, String description, String videoUrl,
            String thumbnailUrl, String level, String category, int duration,
            List<Question> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.category = category;
        this.duration = duration;
        this.questions = questions;
        this.createdAt = System.currentTimeMillis();
        this.views = 0;
        this.isFavorite = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * Get formatted duration string
     */
    @Exclude // Prevents Firestore from serializing this computed property
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}