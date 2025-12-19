package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReadingGamification - Model for gamification system
 * Tracks XP, levels, badges, challenges, and leaderboard position
 */
public class ReadingGamification {
    private String userId;
    private int totalXP;
    private int currentLevel;
    private int currentLevelXP;
    private int nextLevelXP;
    private List<Badge> earnedBadges;
    private List<DailyChallenge> activeChallenges;
    private int weeklyRank;
    private int monthlyRank;
    private long lastUpdated;

    // Constructors
    public ReadingGamification() {
        this.earnedBadges = new ArrayList<>();
        this.activeChallenges = new ArrayList<>();
        this.totalXP = 0;
        this.currentLevel = 1;
        this.currentLevelXP = 0;
        this.nextLevelXP = 1000;
        this.lastUpdated = System.currentTimeMillis();
    }

    public ReadingGamification(String userId) {
        this();
        this.userId = userId;
    }

    // Badge inner class
    public static class Badge {
        private String id;
        private String name;
        private String description;
        private String iconName;
        private BadgeType type;
        private long earnedDate;
        private boolean isNew;

        public Badge() {}

        public Badge(String id, String name, String description, String iconName, BadgeType type) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.iconName = iconName;
            this.type = type;
            this.earnedDate = System.currentTimeMillis();
            this.isNew = true;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIconName() { return iconName; }
        public void setIconName(String iconName) { this.iconName = iconName; }
        public BadgeType getType() { return type; }
        public void setType(BadgeType type) { this.type = type; }
        public long getEarnedDate() { return earnedDate; }
        public void setEarnedDate(long earnedDate) { this.earnedDate = earnedDate; }
        public boolean isNew() { return isNew; }
        public void setNew(boolean isNew) { this.isNew = isNew; }
    }

    // DailyChallenge inner class
    public static class DailyChallenge {
        private String id;
        private String title;
        private String description;
        private ChallengeType type;
        private int targetValue;
        private int currentValue;
        private int xpReward;
        private long expiresAt;
        private boolean completed;

        public DailyChallenge() {}

        public DailyChallenge(String id, String title, String description, ChallengeType type, 
                            int targetValue, int xpReward, long expiresAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.type = type;
            this.targetValue = targetValue;
            this.currentValue = 0;
            this.xpReward = xpReward;
            this.expiresAt = expiresAt;
            this.completed = false;
        }

        public void incrementProgress(int amount) {
            this.currentValue += amount;
            if (this.currentValue >= this.targetValue) {
                this.completed = true;
            }
        }

        public int getProgress() {
            return Math.min(100, (currentValue * 100) / targetValue);
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public ChallengeType getType() { return type; }
        public void setType(ChallengeType type) { this.type = type; }
        public int getTargetValue() { return targetValue; }
        public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
        public int getCurrentValue() { return currentValue; }
        public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }
        public int getXpReward() { return xpReward; }
        public void setXpReward(int xpReward) { this.xpReward = xpReward; }
        public long getExpiresAt() { return expiresAt; }
        public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    // Enums
    public enum BadgeType {
        SPEED_READER,      // Read many articles quickly
        PERFECTIONIST,     // High quiz scores
        POLYGLOT,          // Learn many words
        STREAK_MASTER,     // Long reading streaks
        EARLY_BIRD,        // Read in the morning
        NIGHT_OWL,         // Read at night
        EXPLORER,          // Read diverse topics
        SCHOLAR            // Complete learning paths
    }

    public enum ChallengeType {
        READ_ARTICLES,     // Read X articles
        QUIZ_SCORE,        // Score X% on quiz
        LEARN_WORDS,       // Learn X new words
        READING_TIME,      // Read for X minutes
        STREAK_DAYS        // Maintain X day streak
    }

    // XP and Level methods
    public void awardXP(int amount, String reason) {
        this.totalXP += amount;
        this.currentLevelXP += amount;
        
        // Check for level up
        while (this.currentLevelXP >= this.nextLevelXP) {
            levelUp();
        }
        
        this.lastUpdated = System.currentTimeMillis();
    }

    private void levelUp() {
        this.currentLevel++;
        this.currentLevelXP -= this.nextLevelXP;
        this.nextLevelXP = calculateNextLevelXP();
    }

    private int calculateNextLevelXP() {
        // Progressive XP requirement: Level 1->2: 1000, Level 2->3: 1200, etc.
        return 1000 + (currentLevel - 1) * 200;
    }

    public int getLevelProgress() {
        return (currentLevelXP * 100) / nextLevelXP;
    }

    // Badge methods
    public void earnBadge(Badge badge) {
        if (!hasBadge(badge.getId())) {
            earnedBadges.add(badge);
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    public boolean hasBadge(String badgeId) {
        for (Badge badge : earnedBadges) {
            if (badge.getId().equals(badgeId)) {
                return true;
            }
        }
        return false;
    }

    public List<Badge> getNewBadges() {
        List<Badge> newBadges = new ArrayList<>();
        for (Badge badge : earnedBadges) {
            if (badge.isNew()) {
                newBadges.add(badge);
            }
        }
        return newBadges;
    }

    public void markBadgesAsSeen() {
        for (Badge badge : earnedBadges) {
            badge.setNew(false);
        }
    }

    // Challenge methods
    public void updateChallenge(ChallengeType type, int amount) {
        for (DailyChallenge challenge : activeChallenges) {
            if (challenge.getType() == type && !challenge.isCompleted()) {
                challenge.incrementProgress(amount);
                if (challenge.isCompleted()) {
                    awardXP(challenge.getXpReward(), "Completed challenge: " + challenge.getTitle());
                }
            }
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    public List<DailyChallenge> getCompletedChallenges() {
        List<DailyChallenge> completed = new ArrayList<>();
        for (DailyChallenge challenge : activeChallenges) {
            if (challenge.isCompleted()) {
                completed.add(challenge);
            }
        }
        return completed;
    }

    public void removeExpiredChallenges() {
        long now = System.currentTimeMillis();
        activeChallenges.removeIf(challenge -> challenge.getExpiresAt() < now);
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public int getCurrentLevelXP() { return currentLevelXP; }
    public void setCurrentLevelXP(int currentLevelXP) { this.currentLevelXP = currentLevelXP; }
    public int getNextLevelXP() { return nextLevelXP; }
    public void setNextLevelXP(int nextLevelXP) { this.nextLevelXP = nextLevelXP; }
    public List<Badge> getEarnedBadges() { return earnedBadges; }
    public void setEarnedBadges(List<Badge> earnedBadges) { this.earnedBadges = earnedBadges; }
    public List<DailyChallenge> getActiveChallenges() { return activeChallenges; }
    public void setActiveChallenges(List<DailyChallenge> activeChallenges) { this.activeChallenges = activeChallenges; }
    public int getWeeklyRank() { return weeklyRank; }
    public void setWeeklyRank(int weeklyRank) { this.weeklyRank = weeklyRank; }
    public int getMonthlyRank() { return monthlyRank; }
    public void setMonthlyRank(int monthlyRank) { this.monthlyRank = monthlyRank; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
