package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

public class LearningStory {
    private String id;
    private String title;
    private String topic;
    private String fullStory;
    private String storyWithBlanks; // Story with ___ for practice
    private List<VocabularyWord> vocabularyWords;
    private long createdAt;

    public LearningStory() {
        this.vocabularyWords = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public LearningStory(String id, String title, String topic, String fullStory, 
                        String storyWithBlanks, List<VocabularyWord> vocabularyWords) {
        this.id = id;
        this.title = title;
        this.topic = topic;
        this.fullStory = fullStory;
        this.storyWithBlanks = storyWithBlanks;
        this.vocabularyWords = vocabularyWords != null ? vocabularyWords : new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getFullStory() { return fullStory; }
    public void setFullStory(String fullStory) { this.fullStory = fullStory; }

    public String getStoryWithBlanks() { return storyWithBlanks; }
    public void setStoryWithBlanks(String storyWithBlanks) { this.storyWithBlanks = storyWithBlanks; }

    public List<VocabularyWord> getVocabularyWords() { return vocabularyWords; }
    public void setVocabularyWords(List<VocabularyWord> vocabularyWords) { 
        this.vocabularyWords = vocabularyWords; 
    }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper method to get word count
    public int getWordCount() {
        return vocabularyWords != null ? vocabularyWords.size() : 0;
    }
}
