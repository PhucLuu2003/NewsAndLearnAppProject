package com.example.newsandlearn.Model;

/**
 * SurvivalWord - Model for Survival Vocabulary (200-300 essential words)
 * Categories: greetings, numbers, time, colors, objects, family, food, etc.
 */
public class SurvivalWord {

    private String id;
    private String word;
    private String pronunciation; // IPA
    private String meaningVi;
    private String category; // greetings, numbers, time, colors, objects, family, school, food
    private String categoryVi;
    private String emoji;
    private String imageUrl;
    private String audioUrl;
    private String exampleSentence;
    private String exampleSentenceVi;
    private int orderIndex;
    private int reviewCount;
    private int correctCount;
    private long nextReviewTime; // SRS next review timestamp
    private int srsLevel; // 0=new, 1=learning, 2=known, 3=mastered
    private boolean isLearned;
    private int xpReward;
    private long createdAt;

    // Empty constructor for Firebase
    public SurvivalWord() {
        this.srsLevel = 0;
        this.reviewCount = 0;
        this.correctCount = 0;
        this.xpReward = 5;
    }

    public SurvivalWord(String id, String word, String meaningVi, String category, int orderIndex) {
        this.id = id;
        this.word = word;
        this.meaningVi = meaningVi;
        this.category = category;
        this.orderIndex = orderIndex;
        this.srsLevel = 0;
        this.reviewCount = 0;
        this.correctCount = 0;
        this.xpReward = 5;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getMeaningVi() {
        return meaningVi;
    }

    public void setMeaningVi(String meaningVi) {
        this.meaningVi = meaningVi;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryVi() {
        return categoryVi;
    }

    public void setCategoryVi(String categoryVi) {
        this.categoryVi = categoryVi;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getExampleSentenceVi() {
        return exampleSentenceVi;
    }

    public void setExampleSentenceVi(String exampleSentenceVi) {
        this.exampleSentenceVi = exampleSentenceVi;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public long getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(long nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public int getSrsLevel() {
        return srsLevel;
    }

    public void setSrsLevel(int srsLevel) {
        this.srsLevel = srsLevel;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // SRS Helper methods
    public String getSrsLevelName() {
        switch (srsLevel) {
            case 0:
                return "New";
            case 1:
                return "Learning";
            case 2:
                return "Known";
            case 3:
                return "Mastered";
            default:
                return "New";
        }
    }

    public int getAccuracyPercent() {
        if (reviewCount == 0)
            return 0;
        return (int) ((correctCount / (float) reviewCount) * 100);
    }

    // Last learned timestamp for sorting recently learned words
    private long lastLearnedAt;

    public long getLastLearnedAt() {
        return lastLearnedAt;
    }

    public void setLastLearnedAt(long lastLearnedAt) {
        this.lastLearnedAt = lastLearnedAt;
    }

    // Convert to Map for Firebase
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("word", word);
        map.put("pronunciation", pronunciation);
        map.put("meaningVi", meaningVi);
        map.put("category", category);
        map.put("categoryVi", categoryVi);
        map.put("emoji", emoji);
        map.put("imageUrl", imageUrl);
        map.put("audioUrl", audioUrl);
        map.put("exampleSentence", exampleSentence);
        map.put("exampleSentenceVi", exampleSentenceVi);
        map.put("orderIndex", orderIndex);
        map.put("reviewCount", reviewCount);
        map.put("correctCount", correctCount);
        map.put("nextReviewTime", nextReviewTime);
        map.put("srsLevel", srsLevel);
        map.put("isLearned", isLearned);
        map.put("xpReward", xpReward);
        map.put("createdAt", createdAt);
        map.put("lastLearnedAt", lastLearnedAt);
        return map;
    }
}
