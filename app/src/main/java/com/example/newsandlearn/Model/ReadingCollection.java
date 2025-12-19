package com.example.newsandlearn.Model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReadingCollection {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String iconName;
    private List<String> articleIds;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ReadingCollection() {
        this.articleIds = new ArrayList<>();
    }

    public ReadingCollection(String id, String userId, String name, String description, String iconName) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.articleIds = new ArrayList<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public List<String> getArticleIds() { return articleIds; }
    public void setArticleIds(List<String> articleIds) { this.articleIds = articleIds; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
