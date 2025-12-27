package com.example.newsandlearn.Model;

import java.util.Date;
import java.util.List;

public class Article {
    private String id;
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private String source;
    private String readTime;
    private String level; // A1, A2, B1, B2, C1, C2
    private String category;
    private boolean isFeatured;
    private long timestamp;
    private boolean isBookmarked;
    private String url;
    
    // Additional fields for compatibility
    private Date publishedDate;
    private int readingTime;
    private boolean isFavorite;
    private int progress;
    private List<String> tags;

    public Article() {
        // Required empty constructor for Firestore
    }

    public Article(String id, String title, String description, String imageUrl, 
                   String source, String readTime, String level) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source;
        this.readTime = readTime;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
        this.isFeatured = false;
        this.isBookmarked = false;
        this.isFavorite = false;
        this.progress = 0;
    }
    
    // Constructor for compatibility with FirebaseDataSeeder
    public Article(String id, String title, String content, String imageUrl,
                   String category, String level, String source, Date publishedDate,
                   int readingTime, int progress, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.level = level;
        this.source = source;
        this.publishedDate = publishedDate;
        this.readingTime = readingTime;
        this.progress = progress;
        this.isFavorite = isFavorite;
        this.timestamp = publishedDate != null ? publishedDate.getTime() : System.currentTimeMillis();
        this.readTime = String.valueOf(readingTime);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getReadTime() { return readTime; }
    public void setReadTime(String readTime) { this.readTime = readTime; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    // Additional getters/setters for compatibility
    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }
    
    public int getReadingTime() { return readingTime; }
    public void setReadingTime(int readingTime) { 
        this.readingTime = readingTime;
        this.readTime = String.valueOf(readingTime);
    }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
