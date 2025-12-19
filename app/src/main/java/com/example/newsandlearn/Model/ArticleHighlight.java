package com.example.newsandlearn.Model;

import com.google.firebase.Timestamp;

public class ArticleHighlight {
    private String id;
    private String articleId;
    private String userId;
    private String text;
    private int startIndex;
    private int endIndex;
    private String color; // "yellow", "green", "blue", "red"
    private String note;
    private Timestamp createdAt;

    public ArticleHighlight() {
    }

    public ArticleHighlight(String id, String articleId, String userId, String text, 
                           int startIndex, int endIndex, String color, String note) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.text = text;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.color = color;
        this.note = note;
        this.createdAt = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getStartIndex() { return startIndex; }
    public void setStartIndex(int startIndex) { this.startIndex = startIndex; }

    public int getEndIndex() { return endIndex; }
    public void setEndIndex(int endIndex) { this.endIndex = endIndex; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
