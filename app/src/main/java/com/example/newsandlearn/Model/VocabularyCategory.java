package com.example.newsandlearn.Model;

import java.util.List;

/**
 * VocabularyCategory - Model for vocabulary categories in Survival Vocabulary
 */
public class VocabularyCategory {

    private String id;
    private String name;
    private String nameVi;
    private String emoji;
    private List<String> gradientColors; // e.g., ["#FF6B6B", "#4ECDC4"]
    private int wordCount;
    private int learnedCount;
    private int orderIndex;
    private int order; // Firebase field for ordering
    private String imageUrl;
    private boolean isUnlocked;

    // Empty constructor for Firebase
    public VocabularyCategory() {
        this.isUnlocked = true;
    }

    public VocabularyCategory(String id, String name, String nameVi, String emoji, int orderIndex) {
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.emoji = emoji;
        this.orderIndex = orderIndex;
        this.isUnlocked = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public List<String> getGradientColors() {
        return gradientColors;
    }

    public void setGradientColors(List<String> gradientColors) {
        this.gradientColors = gradientColors;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getLearnedCount() {
        return learnedCount;
    }

    public void setLearnedCount(int learnedCount) {
        this.learnedCount = learnedCount;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getProgressPercent() {
        if (wordCount == 0)
            return 0;
        return (int) ((learnedCount / (float) wordCount) * 100);
    }

    // Convert to Map for Firebase
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("name", name);
        map.put("nameVi", nameVi);
        map.put("emoji", emoji);
        map.put("gradientColors", gradientColors);
        map.put("wordCount", wordCount);
        map.put("learnedCount", learnedCount);
        map.put("orderIndex", orderIndex);
        map.put("order", order);
        map.put("imageUrl", imageUrl);
        map.put("isUnlocked", isUnlocked);
        return map;
    }
}

