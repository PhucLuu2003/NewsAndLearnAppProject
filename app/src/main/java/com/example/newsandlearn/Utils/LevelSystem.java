package com.example.newsandlearn.Utils;

public class LevelSystem {
    
    private static final int BASE_XP = 100;
    private static final double EXPONENT = 1.5;
    
    /**
     * Calculate level from total XP
     * @param totalXP Total XP earned
     * @return Current level
     */
    public static int getLevelFromXP(int totalXP) {
        int level = 1;
        int xpForNextLevel = getXPForLevel(level + 1);
        
        while (totalXP >= xpForNextLevel) {
            level++;
            xpForNextLevel = getXPForLevel(level + 1);
        }
        
        return level;
    }
    
    /**
     * Calculate total XP needed to reach a specific level
     * @param targetLevel Target level
     * @return Total XP needed
     */
    public static int getXPForLevel(int targetLevel) {
        if (targetLevel <= 1) return 0;
        
        int totalXP = 0;
        for (int level = 1; level < targetLevel; level++) {
            totalXP += getXPRequiredForLevelUp(level);
        }
        return totalXP;
    }
    
    /**
     * Calculate XP required to level up from current level
     * @param currentLevel Current level
     * @return XP needed for next level
     */
    public static int getXPRequiredForLevelUp(int currentLevel) {
        return (int) (BASE_XP * Math.pow(currentLevel, EXPONENT));
    }
    
    /**
     * Get current progress in current level
     * @param totalXP Total XP earned
     * @return XP progress in current level
     */
    public static int getCurrentLevelProgress(int totalXP) {
        int currentLevel = getLevelFromXP(totalXP);
        int xpForCurrentLevel = getXPForLevel(currentLevel);
        return totalXP - xpForCurrentLevel;
    }
    
    /**
     * Get XP needed for next level from current XP
     * @param totalXP Total XP earned
     * @return XP needed for next level
     */
    public static int getXPNeededForNextLevel(int totalXP) {
        int currentLevel = getLevelFromXP(totalXP);
        return getXPRequiredForLevelUp(currentLevel);
    }
    
    /**
     * Get progress percentage in current level
     * @param totalXP Total XP earned
     * @return Progress percentage (0-100)
     */
    public static int getLevelProgressPercentage(int totalXP) {
        int currentProgress = getCurrentLevelProgress(totalXP);
        int xpNeeded = getXPNeededForNextLevel(totalXP);
        
        if (xpNeeded == 0) return 100;
        return (int) ((currentProgress * 100.0) / xpNeeded);
    }
    
    /**
     * Get level info as formatted string
     * @param totalXP Total XP earned
     * @return Formatted string like "Level 5 | 150/500 XP"
     */
    public static String getLevelInfo(int totalXP) {
        int level = getLevelFromXP(totalXP);
        int currentProgress = getCurrentLevelProgress(totalXP);
        int xpNeeded = getXPNeededForNextLevel(totalXP);
        
        return String.format("Level %d | %d/%d XP", level, currentProgress, xpNeeded);
    }
    
    /**
     * Check if user leveled up after gaining XP
     * @param oldXP Previous total XP
     * @param newXP New total XP
     * @return true if leveled up
     */
    public static boolean didLevelUp(int oldXP, int newXP) {
        return getLevelFromXP(newXP) > getLevelFromXP(oldXP);
    }
    
    /**
     * Get level title based on level
     * @param level Current level
     * @return Level title
     */
    public static String getLevelTitle(int level) {
        if (level < 5) return "Beginner";
        if (level < 10) return "Learner";
        if (level < 20) return "Intermediate";
        if (level < 30) return "Advanced";
        if (level < 50) return "Expert";
        if (level < 75) return "Master";
        return "Legend";
    }
}
