package com.example.newsandlearn.Model;

import java.util.HashMap;
import java.util.Map;

public class SavedContent {
    private int vocabularyCount;
    private int articleCount;
    private int lessonCount;

    public SavedContent() {
        this.vocabularyCount = 0;
        this.articleCount = 0;
        this.lessonCount = 0;
    }

    public SavedContent(int vocabularyCount, int articleCount, int lessonCount) {
        this.vocabularyCount = vocabularyCount;
        this.articleCount = articleCount;
        this.lessonCount = lessonCount;
    }

    // Getters and Setters
    public int getVocabularyCount() {
        return vocabularyCount;
    }

    public void setVocabularyCount(int vocabularyCount) {
        this.vocabularyCount = vocabularyCount;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("vocabularyCount", vocabularyCount);
        map.put("articleCount", articleCount);
        map.put("lessonCount", lessonCount);
        return map;
    }
}
