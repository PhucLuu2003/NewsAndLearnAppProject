package com.example.newsandlearn.Data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LearnDataManager - Centralized data management for Learn module
 * Handles all Firebase operations for learning features:
 * - Daily Goals
 * - Module Progress (Vocabulary, Grammar, Listening, Speaking, Reading,
 * Writing)
 * - Module Access Tracking
 * - Learning Statistics
 * - Achievements & Streaks
 */
public class LearnDataManager {

    private static final String TAG = "LearnDataManager";

    // Singleton instance
    private static LearnDataManager instance;

    // Firebase
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    // Collections
    private static final String USERS_COLLECTION = "users";
    private static final String DAILY_GOALS_COLLECTION = "daily_goals";
    private static final String MODULE_PROGRESS_COLLECTION = "module_progress";
    private static final String MODULE_ACCESS_COLLECTION = "module_access";
    private static final String LEARNING_STATS_COLLECTION = "learning_stats";
    private static final String ACHIEVEMENTS_COLLECTION = "achievements";

    // Module names
    public static final String MODULE_VOCABULARY = "vocabulary";
    public static final String MODULE_GRAMMAR = "grammar";
    public static final String MODULE_LISTENING = "listening";
    public static final String MODULE_SPEAKING = "speaking";
    public static final String MODULE_READING = "reading";
    public static final String MODULE_WRITING = "writing";

    // Default values
    private static final int DEFAULT_DAILY_GOAL_TOTAL = 5;
    private static final int DEFAULT_DAILY_GOAL_COMPLETED = 0;

    private LearnDataManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized LearnDataManager getInstance() {
        if (instance == null) {
            instance = new LearnDataManager();
        }
        return instance;
    }

    // ========================= DAILY GOAL =========================

    /**
     * Load daily goal from Firebase
     */
    public void loadDailyGoal(DailyGoalCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String today = getTodayDate();

        db.collection(USERS_COLLECTION).document(userId)
                .collection(DAILY_GOALS_COLLECTION).document(today)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        DailyGoal goal = parseDailyGoal(document);
                        callback.onSuccess(goal);
                    } else {
                        // Create default daily goal
                        createDefaultDailyGoal(userId, today, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading daily goal: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Update daily goal progress
     */
    public void updateDailyGoalProgress(int completed, UpdateCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String today = getTodayDate();

        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", completed);
        updates.put("lastUpdated", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(DAILY_GOALS_COLLECTION).document(today)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating daily goal: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Increment daily goal completion
     */
    public void incrementDailyGoalCompletion(UpdateCallback callback) {
        loadDailyGoal(new DailyGoalCallback() {
            @Override
            public void onSuccess(DailyGoal goal) {
                int newCompleted = goal.completed + 1;
                if (newCompleted <= goal.total) {
                    updateDailyGoalProgress(newCompleted, callback);
                } else {
                    callback.onSuccess();
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    private void createDefaultDailyGoal(String userId, String today, DailyGoalCallback callback) {
        Map<String, Object> dailyGoal = new HashMap<>();
        dailyGoal.put("completed", DEFAULT_DAILY_GOAL_COMPLETED);
        dailyGoal.put("total", DEFAULT_DAILY_GOAL_TOTAL);
        dailyGoal.put("date", today);
        dailyGoal.put("createdAt", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(DAILY_GOALS_COLLECTION).document(today)
                .set(dailyGoal)
                .addOnSuccessListener(aVoid -> {
                    DailyGoal goal = new DailyGoal(DEFAULT_DAILY_GOAL_COMPLETED, DEFAULT_DAILY_GOAL_TOTAL, today);
                    callback.onSuccess(goal);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating default daily goal: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ========================= MODULE PROGRESS =========================

    /**
     * Load all module progress
     */
    public void loadModuleProgress(ModuleProgressCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        db.collection(USERS_COLLECTION).document(userId)
                .collection(MODULE_PROGRESS_COLLECTION).document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        ModuleProgress progress = parseModuleProgress(document);
                        callback.onSuccess(progress);
                    } else {
                        createDefaultModuleProgress(userId, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading module progress: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Update specific module progress
     */
    public void updateModuleProgress(String moduleName, int progress, UpdateCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put(moduleName, progress);
        updates.put("lastUpdated", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(MODULE_PROGRESS_COLLECTION).document("current")
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating module progress: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Increment module progress
     */
    public void incrementModuleProgress(String moduleName, int incrementBy, UpdateCallback callback) {
        loadModuleProgress(new ModuleProgressCallback() {
            @Override
            public void onSuccess(ModuleProgress progress) {
                int currentProgress = progress.getProgressForModule(moduleName);
                int newProgress = currentProgress + incrementBy;
                updateModuleProgress(moduleName, newProgress, callback);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    private void createDefaultModuleProgress(String userId, ModuleProgressCallback callback) {
        Map<String, Object> progress = new HashMap<>();
        progress.put(MODULE_VOCABULARY, 0);
        progress.put(MODULE_GRAMMAR, 0);
        progress.put(MODULE_LISTENING, 0);
        progress.put(MODULE_SPEAKING, 0);
        progress.put(MODULE_READING, 0);
        progress.put(MODULE_WRITING, 0);
        progress.put("createdAt", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(MODULE_PROGRESS_COLLECTION).document("current")
                .set(progress)
                .addOnSuccessListener(aVoid -> {
                    ModuleProgress defaultProgress = new ModuleProgress();
                    callback.onSuccess(defaultProgress);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating default module progress: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ========================= MODULE ACCESS TRACKING =========================

    /**
     * Track module access
     */
    public void trackModuleAccess(String moduleName, UpdateCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null)
                callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String timestamp = getCurrentTimestamp();

        Map<String, Object> accessLog = new HashMap<>();
        accessLog.put("module", moduleName);
        accessLog.put("timestamp", timestamp);
        accessLog.put("date", getTodayDate());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(MODULE_ACCESS_COLLECTION)
                .add(accessLog)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null)
                        callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error tracking module access: " + e.getMessage());
                    if (callback != null)
                        callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get module access history
     */
    public void getModuleAccessHistory(String moduleName, int limit, ModuleAccessHistoryCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        db.collection(USERS_COLLECTION).document(userId)
                .collection(MODULE_ACCESS_COLLECTION)
                .whereEqualTo("module", moduleName)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ModuleAccessLog> logs = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ModuleAccessLog log = new ModuleAccessLog(
                                document.getString("module"),
                                document.getString("timestamp"),
                                document.getString("date"));
                        logs.add(log);
                    }
                    callback.onSuccess(logs);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting module access history: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ========================= LEARNING STATISTICS =========================

    /**
     * Load learning statistics
     */
    public void loadLearningStats(LearningStatsCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        db.collection(USERS_COLLECTION).document(userId)
                .collection(LEARNING_STATS_COLLECTION).document("overall")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        LearningStats stats = parseLearningStats(document);
                        callback.onSuccess(stats);
                    } else {
                        createDefaultLearningStats(userId, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading learning stats: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Update learning statistics
     */
    public void updateLearningStats(LearningStats stats, UpdateCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> statsMap = stats.toMap();
        statsMap.put("lastUpdated", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(LEARNING_STATS_COLLECTION).document("overall")
                .set(statsMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating learning stats: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Increment learning time (in minutes)
     */
    public void incrementLearningTime(int minutes, UpdateCallback callback) {
        loadLearningStats(new LearningStatsCallback() {
            @Override
            public void onSuccess(LearningStats stats) {
                stats.totalLearningTime += minutes;
                stats.todayLearningTime += minutes;
                updateLearningStats(stats, callback);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    private void createDefaultLearningStats(String userId, LearningStatsCallback callback) {
        LearningStats stats = new LearningStats();
        Map<String, Object> statsMap = stats.toMap();
        statsMap.put("createdAt", getCurrentTimestamp());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(LEARNING_STATS_COLLECTION).document("overall")
                .set(statsMap)
                .addOnSuccessListener(aVoid -> callback.onSuccess(stats))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating default learning stats: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ========================= ACHIEVEMENTS =========================

    /**
     * Unlock achievement
     */
    public void unlockAchievement(String achievementId, String achievementName, UpdateCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> achievement = new HashMap<>();
        achievement.put("achievementId", achievementId);
        achievement.put("achievementName", achievementName);
        achievement.put("unlockedAt", getCurrentTimestamp());
        achievement.put("date", getTodayDate());

        db.collection(USERS_COLLECTION).document(userId)
                .collection(ACHIEVEMENTS_COLLECTION).document(achievementId)
                .set(achievement)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error unlocking achievement: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Get all achievements
     */
    public void getAllAchievements(AchievementsCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        db.collection(USERS_COLLECTION).document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Achievement> achievements = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Achievement achievement = new Achievement(
                                document.getString("achievementId"),
                                document.getString("achievementName"),
                                document.getString("unlockedAt"),
                                document.getString("date"));
                        achievements.add(achievement);
                    }
                    callback.onSuccess(achievements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting achievements: " + e.getMessage());
                    callback.onFailure(e.getMessage());
                });
    }

    // ========================= HELPER METHODS =========================

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private DailyGoal parseDailyGoal(DocumentSnapshot document) {
        Long completed = document.getLong("completed");
        Long total = document.getLong("total");
        String date = document.getString("date");

        return new DailyGoal(
                completed != null ? completed.intValue() : DEFAULT_DAILY_GOAL_COMPLETED,
                total != null ? total.intValue() : DEFAULT_DAILY_GOAL_TOTAL,
                date != null ? date : getTodayDate());
    }

    private ModuleProgress parseModuleProgress(DocumentSnapshot document) {
        ModuleProgress progress = new ModuleProgress();
        progress.vocabulary = getIntValue(document, MODULE_VOCABULARY);
        progress.grammar = getIntValue(document, MODULE_GRAMMAR);
        progress.listening = getIntValue(document, MODULE_LISTENING);
        progress.speaking = getIntValue(document, MODULE_SPEAKING);
        progress.reading = getIntValue(document, MODULE_READING);
        progress.writing = getIntValue(document, MODULE_WRITING);
        return progress;
    }

    private LearningStats parseLearningStats(DocumentSnapshot document) {
        LearningStats stats = new LearningStats();
        stats.totalLearningTime = getIntValue(document, "totalLearningTime");
        stats.todayLearningTime = getIntValue(document, "todayLearningTime");
        stats.currentStreak = getIntValue(document, "currentStreak");
        stats.longestStreak = getIntValue(document, "longestStreak");
        stats.totalWordsLearned = getIntValue(document, "totalWordsLearned");
        stats.totalLessonsCompleted = getIntValue(document, "totalLessonsCompleted");
        stats.totalQuizzesTaken = getIntValue(document, "totalQuizzesTaken");
        stats.averageScore = getIntValue(document, "averageScore");
        return stats;
    }

    private int getIntValue(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value != null ? value.intValue() : 0;
    }

    // ========================= DATA MODELS =========================

    public static class DailyGoal {
        public int completed;
        public int total;
        public String date;

        public DailyGoal(int completed, int total, String date) {
            this.completed = completed;
            this.total = total;
            this.date = date;
        }

        public int getProgressPercentage() {
            return total > 0 ? (int) ((completed / (float) total) * 100) : 0;
        }

        public boolean isCompleted() {
            return completed >= total;
        }
    }

    public static class ModuleProgress {
        public int vocabulary = 0;
        public int grammar = 0;
        public int listening = 0;
        public int speaking = 0;
        public int reading = 0;
        public int writing = 0;

        public int getProgressForModule(String moduleName) {
            switch (moduleName) {
                case MODULE_VOCABULARY:
                    return vocabulary;
                case MODULE_GRAMMAR:
                    return grammar;
                case MODULE_LISTENING:
                    return listening;
                case MODULE_SPEAKING:
                    return speaking;
                case MODULE_READING:
                    return reading;
                case MODULE_WRITING:
                    return writing;
                default:
                    return 0;
            }
        }

        public void setProgressForModule(String moduleName, int progress) {
            switch (moduleName) {
                case MODULE_VOCABULARY:
                    vocabulary = progress;
                    break;
                case MODULE_GRAMMAR:
                    grammar = progress;
                    break;
                case MODULE_LISTENING:
                    listening = progress;
                    break;
                case MODULE_SPEAKING:
                    speaking = progress;
                    break;
                case MODULE_READING:
                    reading = progress;
                    break;
                case MODULE_WRITING:
                    writing = progress;
                    break;
            }
        }

        public int getTotalProgress() {
            return vocabulary + grammar + listening + speaking + reading + writing;
        }
    }

    public static class LearningStats {
        public int totalLearningTime = 0; // in minutes
        public int todayLearningTime = 0; // in minutes
        public int currentStreak = 0; // consecutive days
        public int longestStreak = 0; // best streak
        public int totalWordsLearned = 0;
        public int totalLessonsCompleted = 0;
        public int totalQuizzesTaken = 0;
        public int averageScore = 0; // percentage

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("totalLearningTime", totalLearningTime);
            map.put("todayLearningTime", todayLearningTime);
            map.put("currentStreak", currentStreak);
            map.put("longestStreak", longestStreak);
            map.put("totalWordsLearned", totalWordsLearned);
            map.put("totalLessonsCompleted", totalLessonsCompleted);
            map.put("totalQuizzesTaken", totalQuizzesTaken);
            map.put("averageScore", averageScore);
            return map;
        }
    }

    public static class ModuleAccessLog {
        public String module;
        public String timestamp;
        public String date;

        public ModuleAccessLog(String module, String timestamp, String date) {
            this.module = module;
            this.timestamp = timestamp;
            this.date = date;
        }
    }

    public static class Achievement {
        public String achievementId;
        public String achievementName;
        public String unlockedAt;
        public String date;

        public Achievement(String achievementId, String achievementName, String unlockedAt, String date) {
            this.achievementId = achievementId;
            this.achievementName = achievementName;
            this.unlockedAt = unlockedAt;
            this.date = date;
        }
    }

    // ========================= CALLBACKS =========================

    public interface DailyGoalCallback {
        void onSuccess(DailyGoal goal);

        void onFailure(String error);
    }

    public interface ModuleProgressCallback {
        void onSuccess(ModuleProgress progress);

        void onFailure(String error);
    }

    public interface LearningStatsCallback {
        void onSuccess(LearningStats stats);

        void onFailure(String error);
    }

    public interface ModuleAccessHistoryCallback {
        void onSuccess(List<ModuleAccessLog> logs);

        void onFailure(String error);
    }

    public interface AchievementsCallback {
        void onSuccess(List<Achievement> achievements);

        void onFailure(String error);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onFailure(String error);
    }
}
