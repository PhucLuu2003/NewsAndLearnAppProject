package com.example.newsandlearn.Model;

public class StoryTopic {
    private String id;
    private String name;
    private String emoji;
    private String description;
    private boolean isCustom;

    public StoryTopic() {
        // Required empty constructor for Firestore
    }

    public StoryTopic(String id, String name, String emoji, String description) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.isCustom = false;
    }

    public StoryTopic(String id, String name, String emoji, String description, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.description = description;
        this.isCustom = isCustom;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    // Predefined topics
    public static StoryTopic[] getDefaultTopics() {
        return new StoryTopic[]{
                new StoryTopic("travel", "Travel", "✈️", "Explore the world"),
                new StoryTopic("food", "Food", "🍔", "Delicious cuisine"),
                new StoryTopic("business", "Business", "💼", "Professional life"),
                new StoryTopic("health", "Health", "💪", "Stay healthy"),
                new StoryTopic("technology", "Technology", "💻", "Digital world"),
                new StoryTopic("custom", "Custom Topic", "✏️", "Your own topic", true)
        };
    }
}
