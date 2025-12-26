package com.example.newsandlearn.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String avatarUrl;
    private String role; // "admin" or "user"
    private String level; // Beginner, Intermediate, Advanced
    private int xp;
    private int dailyGoal; // in minutes
    private int weeklyGoal; // in minutes
    private int currentStreak;
    private int longestStreak;
    private Date lastActive;
    private UserStats stats;
    private SavedContent saved;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = "user"; // Default role
        this.level = "Beginner";
        this.xp = 0;
        this.dailyGoal = 30;
        this.weeklyGoal = 210;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastActive = new Date();
        this.stats = new UserStats();
        this.saved = new SavedContent();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getDailyGoal() {
        return dailyGoal;
    }

    public void setDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    public int getWeeklyGoal() {
        return weeklyGoal;
    }

    public void setWeeklyGoal(int weeklyGoal) {
        this.weeklyGoal = weeklyGoal;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public SavedContent getSaved() {
        return saved;
    }

    public void setSaved(SavedContent saved) {
        this.saved = saved;
    }

    // Calculate XP needed for next level
    public int getXpForNextLevel() {
        return getXpRequiredForLevel(getCurrentLevel() + 1);
    }

    // Get current level based on XP
    public int getCurrentLevel() {
        return xp / 100; // Every 100 XP = 1 level
    }

    // Get XP required for a specific level
    private int getXpRequiredForLevel(int level) {
        return level * 100;
    }

    // Get progress percentage to next level
    public int getProgressPercentage() {
        int currentLevel = getCurrentLevel();
        int xpForCurrentLevel = getXpRequiredForLevel(currentLevel);
        int xpForNextLevel = getXpForNextLevel();
        int xpInCurrentLevel = xp - xpForCurrentLevel;
        int xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;

        return xpNeededForNextLevel > 0 ? (xpInCurrentLevel * 100) / xpNeededForNextLevel : 0;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("avatarUrl", avatarUrl);
        map.put("role", role);
        map.put("level", level);
        map.put("xp", xp);
        map.put("dailyGoal", dailyGoal);
        map.put("weeklyGoal", weeklyGoal);
        map.put("currentStreak", currentStreak);
        map.put("longestStreak", longestStreak);
        map.put("lastActive", lastActive);
        if (stats != null)
            map.put("stats", stats.toMap());
        if (saved != null)
            map.put("saved", saved.toMap());
        return map;
    }
}
