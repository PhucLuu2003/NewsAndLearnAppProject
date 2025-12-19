package com.example.newsandlearn.Model;

import java.io.Serializable;
import java.util.List;

/**
 * GameLevel - Màn chơi trong RPG
 */
public class GameLevel implements Serializable {
    private String id;
    private String name;
    private String description;
    private String theme; // "greetings", "travel", "food", "work"
    private int levelNumber;
    private boolean isUnlocked;
    private boolean isCompleted;
    private int starsEarned; // 0-3 stars
    private String monsterId;
    private List<String> questionIds; // List of question IDs
    private int requiredLevel; // Player level required to unlock
    private String backgroundImageUrl;

    public GameLevel() {
        // Default constructor for Firebase
    }

    public GameLevel(String id, String name, String theme, int levelNumber) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.levelNumber = levelNumber;
        this.isUnlocked = (levelNumber == 1); // First level is unlocked
        this.isCompleted = false;
        this.starsEarned = 0;
        this.requiredLevel = levelNumber;
    }

    public void complete(int stars) {
        this.isCompleted = true;
        this.starsEarned = Math.max(this.starsEarned, stars);
    }

    public void unlock() {
        this.isUnlocked = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getStarsEarned() { return starsEarned; }
    public void setStarsEarned(int starsEarned) { this.starsEarned = starsEarned; }

    public String getMonsterId() { return monsterId; }
    public void setMonsterId(String monsterId) { this.monsterId = monsterId; }

    public List<String> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<String> questionIds) { this.questionIds = questionIds; }

    public int getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(int requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getBackgroundImageUrl() { return backgroundImageUrl; }
    public void setBackgroundImageUrl(String backgroundImageUrl) { 
        this.backgroundImageUrl = backgroundImageUrl; 
    }
}
