package com.example.newsandlearn.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PhonicsLesson - Model for Phonics/Pronunciation lessons
 * Includes alphabet, sounds, and pronunciation practice
 */
public class PhonicsLesson {

    private String id;
    private String title;
    private String titleVi;
    private String symbol; // IPA symbol e.g., /æ/, /ʌ/
    private String emoji;
    private String description;
    private String descriptionVi;
    private String category; // alphabet, vowels, consonants, stress
    private int orderIndex;
    private int order; // Alias for orderIndex
    private String audioUrl;
    private String videoUrl;
    private List<String> exampleWords;
    private List<String> exampleSentences;
    private String mouthPosition; // Description of mouth position
    private String mouthImageUrl;
    private boolean isCompleted;
    private int xpReward;
    private String level; // A0, A1, A2
    private long createdAt;

    // Empty constructor for Firebase
    public PhonicsLesson() {
    }

    public PhonicsLesson(String id, String title, String symbol, String category, int orderIndex) {
        this.id = id;
        this.title = title;
        this.symbol = symbol;
        this.category = category;
        this.orderIndex = orderIndex;
        this.xpReward = 10;
        this.level = "A0";
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

    public String getTitleVi() {
        return titleVi;
    }

    public void setTitleVi(String titleVi) {
        this.titleVi = titleVi;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionVi() {
        return descriptionVi;
    }

    public void setDescriptionVi(String descriptionVi) {
        this.descriptionVi = descriptionVi;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public List<String> getExampleWords() {
        return exampleWords;
    }

    public void setExampleWords(List<String> exampleWords) {
        this.exampleWords = exampleWords;
    }

    public List<String> getExampleSentences() {
        return exampleSentences;
    }

    public void setExampleSentences(List<String> exampleSentences) {
        this.exampleSentences = exampleSentences;
    }

    public String getMouthPosition() {
        return mouthPosition;
    }

    public void setMouthPosition(String mouthPosition) {
        this.mouthPosition = mouthPosition;
    }

    public String getMouthImageUrl() {
        return mouthImageUrl;
    }

    public void setMouthImageUrl(String mouthImageUrl) {
        this.mouthImageUrl = mouthImageUrl;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getOrder() {
        return order > 0 ? order : orderIndex;
    }

    public void setOrder(int order) {
        this.order = order;
        this.orderIndex = order;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("titleVi", titleVi);
        map.put("symbol", symbol);
        map.put("emoji", emoji);
        map.put("description", description);
        map.put("descriptionVi", descriptionVi);
        map.put("category", category);
        map.put("order", order > 0 ? order : orderIndex);
        map.put("audioUrl", audioUrl);
        map.put("videoUrl", videoUrl);
        map.put("exampleWords", exampleWords);
        map.put("exampleSentences", exampleSentences);
        map.put("mouthPosition", mouthPosition);
        map.put("mouthImageUrl", mouthImageUrl);
        map.put("xpReward", xpReward);
        map.put("level", level);
        map.put("createdAt", System.currentTimeMillis());
        return map;
    }
}
