package com.example.newsandlearn.Model;

import java.io.Serializable;
import java.util.Date;

public class Article implements Serializable {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String category;
    private String level; // "easy", "medium", "hard"
    private String source;
    private String authorName;
    private String authorAvatar; // URL to author/source avatar
    private Date publishedDate;
    private int views;
    private int readingTime; // in minutes
    private boolean isFavorite;

    // Empty constructor for Firebase
    public Article() {
    }

    public Article(String id, String title, String content, String imageUrl, String category,
            String level, String source, Date publishedDate, int views, int readingTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.level = level;
        this.source = source;
        this.publishedDate = publishedDate;
        this.views = views;
        this.readingTime = readingTime;
        this.isFavorite = false;
    }

    public Article(String id, String title, String content, String imageUrl, String category,
            String level, String source, Date publishedDate, int views, int readingTime, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.level = level;
        this.source = source;
        this.publishedDate = publishedDate;
        this.views = views;
        this.readingTime = readingTime;
        this.isFavorite = isFavorite;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(int readingTime) {
        this.readingTime = readingTime;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }
}
