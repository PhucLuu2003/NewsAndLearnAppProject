package com.example.newsandlearn.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UserProgress Model - Tracks user's learning progress and statistics
 * Used for gamification and analytics
 */
public class UserProgress {
    
    // User identification
    private String userId;
    
    // XP and Level System
    private int totalXP;
    private int currentLevel;
    private int xpForNextLevel;
    private int xpInCurrentLevel;      // XP earned in current level
    
    // Streak System
    private int currentStreak;         // Current consecutive days
    private int longestStreak;         // Best streak ever
    private Date lastActiveDate;       // Last day user was active
    private Date streakStartDate;      // When current streak started
    
    // Learning Statistics
    private int totalArticlesRead;
    private int totalWordsLearned;
    private int totalLessonsCompleted;
    private int totalVideoWatched;
    private int totalExercisesCompleted;
    private int totalTimeSpentMinutes;  // Total learning time
    
    // Daily Statistics
    private int articlesReadToday;
    private int wordsLearnedToday;
    private int lessonsCompletedToday;
    private int timeSpentTodayMinutes;
    
    // Weekly Statistics
    private int articlesReadThisWeek;
    private int wordsLearnedThisWeek;
    private int lessonsCompletedThisWeek;
    
    // Monthly Statistics
    private int articlesReadThisMonth;
    private int wordsLearnedThisMonth;
    private int lessonsCompletedThisMonth;
    
    // Achievement counts
    private int totalAchievements;
    private int unlockedAchievements;
    
    // Activity tracking (last 7 days)
    private Map<String, DailyActivity> weeklyActivity; // date -> activity
    
    // Timestamps
    private Date createdAt;
    private Date updatedAt;

    /**
     * DailyActivity - Tracks activity for a specific day
     */
    public static class DailyActivity {
        private String date;           // Format: yyyy-MM-dd
        private int articlesRead;
        private int wordsLearned;
        private int lessonsCompleted;
        private int timeSpentMinutes;
        private int xpEarned;
        private boolean goalCompleted;
        
        public DailyActivity() {}
        
        public DailyActivity(String date) {
            this.date = date;
            this.articlesRead = 0;
            this.wordsLearned = 0;
            this.lessonsCompleted = 0;
            this.timeSpentMinutes = 0;
            this.xpEarned = 0;
            this.goalCompleted = false;
        }
        
        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public int getArticlesRead() { return articlesRead; }
        public void setArticlesRead(int articlesRead) { this.articlesRead = articlesRead; }
        
        public int getWordsLearned() { return wordsLearned; }
        public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }
        
        public int getLessonsCompleted() { return lessonsCompleted; }
        public void setLessonsCompleted(int lessonsCompleted) { this.lessonsCompleted = lessonsCompleted; }
        
        public int getTimeSpentMinutes() { return timeSpentMinutes; }
        public void setTimeSpentMinutes(int timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }
        
        public int getXpEarned() { return xpEarned; }
        public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
        
        public boolean isGoalCompleted() { return goalCompleted; }
        public void setGoalCompleted(boolean goalCompleted) { this.goalCompleted = goalCompleted; }
    }

    // Constructors
    public UserProgress() {
        this.totalXP = 0;
        this.currentLevel = 1;
        this.xpForNextLevel = calculateXPForLevel(2);
        this.xpInCurrentLevel = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.weeklyActivity = new HashMap<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public UserProgress(String userId) {
        this();
        this.userId = userId;
    }

    // XP and Level Methods
    
    /**
     * Calculate XP required for a specific level
     * Formula: level * 1000 (can be adjusted for difficulty curve)
     */
    public static int calculateXPForLevel(int level) {
        return level * 1000;
    }
    
    /**
     * Add XP and check for level up
     * @return true if leveled up
     */
    public boolean addXP(int xp) {
        totalXP += xp;
        xpInCurrentLevel += xp;
        
        boolean leveledUp = false;
        while (xpInCurrentLevel >= xpForNextLevel) {
            xpInCurrentLevel -= xpForNextLevel;
            currentLevel++;
            xpForNextLevel = calculateXPForLevel(currentLevel + 1);
            leveledUp = true;
        }
        
        updatedAt = new Date();
        return leveledUp;
    }
    
    /**
     * Get progress to next level as percentage
     */
    public int getLevelProgress() {
        if (xpForNextLevel == 0) return 0;
        return (xpInCurrentLevel * 100) / xpForNextLevel;
    }

    // Streak Methods
    
    /**
     * Update streak based on current date
     * Call this when user performs any learning activity
     */
    public void updateStreak() {
        Date today = new Date();
        
        if (lastActiveDate == null) {
            // First time
            currentStreak = 1;
            streakStartDate = today;
            lastActiveDate = today;
        } else {
            long daysDiff = getDaysDifference(lastActiveDate, today);
            
            if (daysDiff == 0) {
                // Same day, no change
                return;
            } else if (daysDiff == 1) {
                // Consecutive day
                currentStreak++;
                lastActiveDate = today;
                
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak;
                }
            } else {
                // Streak broken
                currentStreak = 1;
                streakStartDate = today;
                lastActiveDate = today;
            }
        }
        
        updatedAt = new Date();
    }
    
    /**
     * Calculate days difference between two dates
     */
    private long getDaysDifference(Date date1, Date date2) {
        long diff = date2.getTime() - date1.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    // Activity Tracking Methods
    
    public void incrementArticlesRead() {
        totalArticlesRead++;
        articlesReadToday++;
        articlesReadThisWeek++;
        articlesReadThisMonth++;
        updatedAt = new Date();
    }
    
    public void incrementWordsLearned(int count) {
        totalWordsLearned += count;
        wordsLearnedToday += count;
        wordsLearnedThisWeek += count;
        wordsLearnedThisMonth += count;
        updatedAt = new Date();
    }
    
    public void incrementLessonsCompleted() {
        totalLessonsCompleted++;
        lessonsCompletedToday++;
        lessonsCompletedThisWeek++;
        lessonsCompletedThisMonth++;
        updatedAt = new Date();
    }
    
    public void addTimeSpent(int minutes) {
        totalTimeSpentMinutes += minutes;
        timeSpentTodayMinutes += minutes;
        updatedAt = new Date();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public int getXpForNextLevel() { return xpForNextLevel; }
    public void setXpForNextLevel(int xpForNextLevel) { this.xpForNextLevel = xpForNextLevel; }

    public int getXpInCurrentLevel() { return xpInCurrentLevel; }
    public void setXpInCurrentLevel(int xpInCurrentLevel) { this.xpInCurrentLevel = xpInCurrentLevel; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public Date getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(Date lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public Date getStreakStartDate() { return streakStartDate; }
    public void setStreakStartDate(Date streakStartDate) { this.streakStartDate = streakStartDate; }

    public int getTotalArticlesRead() { return totalArticlesRead; }
    public void setTotalArticlesRead(int totalArticlesRead) { this.totalArticlesRead = totalArticlesRead; }

    public int getTotalWordsLearned() { return totalWordsLearned; }
    public void setTotalWordsLearned(int totalWordsLearned) { this.totalWordsLearned = totalWordsLearned; }

    public int getTotalLessonsCompleted() { return totalLessonsCompleted; }
    public void setTotalLessonsCompleted(int totalLessonsCompleted) { 
        this.totalLessonsCompleted = totalLessonsCompleted; 
    }

    public int getTotalVideoWatched() { return totalVideoWatched; }
    public void setTotalVideoWatched(int totalVideoWatched) { this.totalVideoWatched = totalVideoWatched; }

    public int getTotalExercisesCompleted() { return totalExercisesCompleted; }
    public void setTotalExercisesCompleted(int totalExercisesCompleted) { 
        this.totalExercisesCompleted = totalExercisesCompleted; 
    }

    public int getTotalTimeSpentMinutes() { return totalTimeSpentMinutes; }
    public void setTotalTimeSpentMinutes(int totalTimeSpentMinutes) { 
        this.totalTimeSpentMinutes = totalTimeSpentMinutes; 
    }

    public int getArticlesReadToday() { return articlesReadToday; }
    public void setArticlesReadToday(int articlesReadToday) { this.articlesReadToday = articlesReadToday; }

    public int getWordsLearnedToday() { return wordsLearnedToday; }
    public void setWordsLearnedToday(int wordsLearnedToday) { this.wordsLearnedToday = wordsLearnedToday; }

    public int getLessonsCompletedToday() { return lessonsCompletedToday; }
    public void setLessonsCompletedToday(int lessonsCompletedToday) { 
        this.lessonsCompletedToday = lessonsCompletedToday; 
    }

    public int getTimeSpentTodayMinutes() { return timeSpentTodayMinutes; }
    public void setTimeSpentTodayMinutes(int timeSpentTodayMinutes) { 
        this.timeSpentTodayMinutes = timeSpentTodayMinutes; 
    }

    public int getArticlesReadThisWeek() { return articlesReadThisWeek; }
    public void setArticlesReadThisWeek(int articlesReadThisWeek) { 
        this.articlesReadThisWeek = articlesReadThisWeek; 
    }

    public int getWordsLearnedThisWeek() { return wordsLearnedThisWeek; }
    public void setWordsLearnedThisWeek(int wordsLearnedThisWeek) { 
        this.wordsLearnedThisWeek = wordsLearnedThisWeek; 
    }

    public int getLessonsCompletedThisWeek() { return lessonsCompletedThisWeek; }
    public void setLessonsCompletedThisWeek(int lessonsCompletedThisWeek) { 
        this.lessonsCompletedThisWeek = lessonsCompletedThisWeek; 
    }

    public int getArticlesReadThisMonth() { return articlesReadThisMonth; }
    public void setArticlesReadThisMonth(int articlesReadThisMonth) { 
        this.articlesReadThisMonth = articlesReadThisMonth; 
    }

    public int getWordsLearnedThisMonth() { return wordsLearnedThisMonth; }
    public void setWordsLearnedThisMonth(int wordsLearnedThisMonth) { 
        this.wordsLearnedThisMonth = wordsLearnedThisMonth; 
    }

    public int getLessonsCompletedThisMonth() { return lessonsCompletedThisMonth; }
    public void setLessonsCompletedThisMonth(int lessonsCompletedThisMonth) { 
        this.lessonsCompletedThisMonth = lessonsCompletedThisMonth; 
    }

    public int getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(int totalAchievements) { this.totalAchievements = totalAchievements; }

    public int getUnlockedAchievements() { return unlockedAchievements; }
    public void setUnlockedAchievements(int unlockedAchievements) { 
        this.unlockedAchievements = unlockedAchievements; 
    }

    public Map<String, DailyActivity> getWeeklyActivity() { return weeklyActivity; }
    public void setWeeklyActivity(Map<String, DailyActivity> weeklyActivity) { 
        this.weeklyActivity = weeklyActivity; 
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
