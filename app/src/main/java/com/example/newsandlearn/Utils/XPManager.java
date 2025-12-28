package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * XPManager - Centralized XP and Streak management with Firebase sync
 * All XP and Streak operations MUST go through this manager
 */
public class XPManager {
    private static final String TAG = "XPManager";
    private static XPManager instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // XP Rewards Configuration
    public static final int XP_READ_ARTICLE = 50;
    public static final int XP_COMPLETE_LESSON = 100;
    public static final int XP_LEARN_WORD = 10;
    public static final int XP_COMPLETE_QUIZ = 75;
    public static final int XP_FLASHCARD_SESSION = 30;
    public static final int XP_VIDEO_COMPLETE = 60;
    public static final int XP_DAILY_GOAL = 150;
    public static final int XP_STREAK_BONUS = 25; // Per day of streak
    
    // Level System
    public static final int XP_PER_LEVEL = 500; // 500 XP = 1 level
    
    private XPManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static synchronized XPManager getInstance() {
        if (instance == null) {
            instance = new XPManager();
        }
        return instance;
    }
    
    /**
     * Add XP to user and update Firebase
     */
    public void addXP(int xp, String source, XPCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onError("User not authenticated");
            return;
        }
        
        DocumentReference userRef = db.collection("users").document(user.getUid());
        
        db.runTransaction(transaction -> {
            // Get current user data
            Map<String, Object> userData = transaction.get(userRef).getData();
            if (userData == null) {
                userData = new HashMap<>();
            }
            
            // Get current XP and level
            long currentXP = userData.containsKey("xp") ? ((Number) userData.get("xp")).longValue() : 0;
            int currentLevel = userData.containsKey("level") ? ((Number) userData.get("level")).intValue() : 1;
            
            // Add new XP
            long newXP = currentXP + xp;
            
            // Calculate new level
            int newLevel = calculateLevel(newXP);
            boolean leveledUp = newLevel > currentLevel;
            
            // Update user document
            Map<String, Object> updates = new HashMap<>();
            updates.put("xp", newXP);
            updates.put("level", newLevel);
            updates.put("lastActive", new Date());
            
            transaction.update(userRef, updates);
            
            // Log XP gain
            logXPGain(user.getUid(), xp, source, newXP, newLevel);
            
            return new XPResult(newXP, newLevel, leveledUp, xp);
        }).addOnSuccessListener(result -> {
            Log.d(TAG, "XP added successfully: +" + xp + " from " + source);
            if (callback != null) {
                callback.onSuccess(result);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding XP", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Update streak and award bonus XP
     */
    public void updateStreak(StreakCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onError("User not authenticated");
            return;
        }
        
        DocumentReference userRef = db.collection("users").document(user.getUid());
        
        db.runTransaction(transaction -> {
            Map<String, Object> userData = transaction.get(userRef).getData();
            if (userData == null) {
                userData = new HashMap<>();
            }
            
            // Get current streak data
            int currentStreak = userData.containsKey("currentStreak") ? 
                ((Number) userData.get("currentStreak")).intValue() : 0;
            int longestStreak = userData.containsKey("longestStreak") ? 
                ((Number) userData.get("longestStreak")).intValue() : 0;
            Date lastActive = userData.containsKey("lastActive") ? 
                ((com.google.firebase.Timestamp) userData.get("lastActive")).toDate() : null;
            
            // Calculate new streak
            StreakResult result = calculateStreak(currentStreak, lastActive);
            
            // Update longest streak if needed
            if (result.newStreak > longestStreak) {
                longestStreak = result.newStreak;
            }
            
            // Update user document
            Map<String, Object> updates = new HashMap<>();
            updates.put("currentStreak", result.newStreak);
            updates.put("longestStreak", longestStreak);
            updates.put("lastActive", new Date());
            
            transaction.update(userRef, updates);
            
            return new StreakUpdateResult(result.newStreak, longestStreak, result.isNewDay, result.streakBroken);
        }).addOnSuccessListener(result -> {
            Log.d(TAG, "Streak updated: " + result.currentStreak + " days");
            
            // Award streak bonus XP if it's a new day
            if (result.isNewDay && result.currentStreak > 0) {
                int bonusXP = XP_STREAK_BONUS * result.currentStreak;
                addXP(bonusXP, "streak_bonus", null);
            }
            
            if (callback != null) {
                callback.onSuccess(result);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating streak", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        });
    }
    
    /**
     * Calculate level from XP
     */
    public static int calculateLevel(long xp) {
        return (int) (xp / XP_PER_LEVEL) + 1;
    }
    
    /**
     * Calculate XP needed for next level
     */
    public static long getXPForNextLevel(int currentLevel) {
        return currentLevel * XP_PER_LEVEL;
    }
    
    /**
     * Calculate progress percentage to next level
     */
    public static int getLevelProgress(long currentXP) {
        int currentLevel = calculateLevel(currentXP);
        long xpForCurrentLevel = (currentLevel - 1) * XP_PER_LEVEL;
        long xpForNextLevel = currentLevel * XP_PER_LEVEL;
        long xpInCurrentLevel = currentXP - xpForCurrentLevel;
        long xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;
        
        return (int) ((xpInCurrentLevel * 100) / xpNeededForNextLevel);
    }
    
    /**
     * Calculate streak based on last active date
     */
    private StreakResult calculateStreak(int currentStreak, Date lastActive) {
        if (lastActive == null) {
            // First time - start streak
            return new StreakResult(1, true, false);
        }
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar lastDay = Calendar.getInstance();
        lastDay.setTime(lastActive);
        lastDay.set(Calendar.HOUR_OF_DAY, 0);
        lastDay.set(Calendar.MINUTE, 0);
        lastDay.set(Calendar.SECOND, 0);
        lastDay.set(Calendar.MILLISECOND, 0);
        
        long daysDiff = (today.getTimeInMillis() - lastDay.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        
        if (daysDiff == 0) {
            // Same day - no change
            return new StreakResult(currentStreak, false, false);
        } else if (daysDiff == 1) {
            // Next day - increment streak
            return new StreakResult(currentStreak + 1, true, false);
        } else {
            // Streak broken - reset to 1
            return new StreakResult(1, true, true);
        }
    }
    
    /**
     * Log XP gain for analytics
     */
    private void logXPGain(String userId, int xp, String source, long newTotal, int newLevel) {
        Map<String, Object> log = new HashMap<>();
        log.put("userId", userId);
        log.put("xpGained", xp);
        log.put("source", source);
        log.put("newTotal", newTotal);
        log.put("newLevel", newLevel);
        log.put("timestamp", FieldValue.serverTimestamp());
        
        db.collection("xp_logs")
                .add(log)
                .addOnFailureListener(e -> Log.e(TAG, "Error logging XP", e));
    }
    
    // Callback interfaces
    public interface XPCallback {
        void onSuccess(XPResult result);
        void onError(String error);
    }
    
    public interface StreakCallback {
        void onSuccess(StreakUpdateResult result);
        void onError(String error);
    }
    
    // Result classes
    public static class XPResult {
        public long totalXP;
        public int level;
        public boolean leveledUp;
        public int xpGained;
        
        public XPResult(long totalXP, int level, boolean leveledUp, int xpGained) {
            this.totalXP = totalXP;
            this.level = level;
            this.leveledUp = leveledUp;
            this.xpGained = xpGained;
        }
    }
    
    public static class StreakUpdateResult {
        public int currentStreak;
        public int longestStreak;
        public boolean isNewDay;
        public boolean streakBroken;
        
        public StreakUpdateResult(int currentStreak, int longestStreak, boolean isNewDay, boolean streakBroken) {
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
            this.isNewDay = isNewDay;
            this.streakBroken = streakBroken;
        }
    }
    
    private static class StreakResult {
        int newStreak;
        boolean isNewDay;
        boolean streakBroken;
        
        StreakResult(int newStreak, boolean isNewDay, boolean streakBroken) {
            this.newStreak = newStreak;
            this.isNewDay = isNewDay;
            this.streakBroken = streakBroken;
        }
    }
}
