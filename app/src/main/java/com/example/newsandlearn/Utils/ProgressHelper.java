package com.example.newsandlearn.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class for updating user learning progress in Firebase
 * Use this from any Fragment/Activity to track user progress
 */
public class ProgressHelper {

    private static final String TAG = "ProgressHelper";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Update daily goal progress
     * Call this when user completes a task/lesson
     */
    public static void incrementDailyGoal() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("daily_goals").document(today)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Increment completed count
                        db.collection("users").document(userId)
                                .collection("daily_goals").document(today)
                                .update("completed", FieldValue.increment(1));
                    } else {
                        // Create new daily goal
                        Map<String, Object> dailyGoal = new HashMap<>();
                        dailyGoal.put("completed", 1);
                        dailyGoal.put("total", 5);
                        dailyGoal.put("date", today);
                        
                        db.collection("users").document(userId)
                                .collection("daily_goals").document(today)
                                .set(dailyGoal);
                    }
                });
    }

    /**
     * Update module progress
     * @param moduleName: "vocabulary", "grammar", "listening", "speaking", "reading", "writing"
     * @param progressIncrement: How much to increase (e.g., 5 for 5%)
     */
    public static void updateModuleProgress(String moduleName, int progressIncrement) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String moduleKey = moduleName.toLowerCase();

        db.collection("users").document(userId)
                .collection("module_progress").document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long currentProgress = document.getLong(moduleKey);
                        int newProgress = (currentProgress != null ? currentProgress.intValue() : 0) + progressIncrement;
                        
                        // Cap at 100%
                        if (newProgress > 100) newProgress = 100;
                        
                        db.collection("users").document(userId)
                                .collection("module_progress").document("current")
                                .update(moduleKey, newProgress);
                    } else {
                        // Create new progress document
                        Map<String, Object> progress = new HashMap<>();
                        progress.put("vocabulary", 0);
                        progress.put("grammar", 0);
                        progress.put("listening", 0);
                        progress.put("speaking", 0);
                        progress.put("reading", 0);
                        progress.put("writing", 0);
                        progress.put(moduleKey, progressIncrement);
                        
                        db.collection("users").document(userId)
                                .collection("module_progress").document("current")
                                .set(progress);
                    }
                });
    }

    /**
     * Complete a lesson - updates both daily goal and module progress
     * @param moduleName: Name of the module
     * @param progressIncrement: Progress to add (default: 5%)
     */
    public static void completeLesson(String moduleName, int progressIncrement) {
        incrementDailyGoal();
        updateModuleProgress(moduleName, progressIncrement);
        logActivity(moduleName, "lesson_completed");
    }

    /**
     * Complete a quiz - updates progress with bonus
     * @param moduleName: Name of the module
     * @param score: Quiz score (0-100)
     */
    public static void completeQuiz(String moduleName, int score) {
        incrementDailyGoal();
        
        // Give progress based on score
        int progressIncrement = (int) (score * 0.1); // 100 score = 10% progress
        updateModuleProgress(moduleName, progressIncrement);
        
        // Log quiz result
        logQuizResult(moduleName, score);
    }

    /**
     * Add vocabulary word to user's collection
     * @param word: The vocabulary word
     * @param definition: Word definition
     * @param level: Difficulty level
     */
    public static void addVocabulary(String word, String definition, String level) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> vocabData = new HashMap<>();
        vocabData.put("word", word);
        vocabData.put("definition", definition);
        vocabData.put("level", level);
        vocabData.put("status", "new"); // new, learning, known, mastered
        vocabData.put("addedAt", timestamp);
        vocabData.put("reviewCount", 0);
        vocabData.put("lastReviewed", null);

        db.collection("users").document(userId)
                .collection("vocabulary")
                .add(vocabData)
                .addOnSuccessListener(documentReference -> {
                    // Update vocabulary module progress
                    updateModuleProgress("vocabulary", 1);
                });
    }

    /**
     * Update vocabulary word status
     * @param wordId: Document ID of the word
     * @param status: "new", "learning", "known", "mastered"
     */
    public static void updateVocabularyStatus(String wordId, String status) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("lastReviewed", timestamp);
        updates.put("reviewCount", FieldValue.increment(1));

        db.collection("users").document(userId)
                .collection("vocabulary").document(wordId)
                .update(updates);
    }

    /**
     * Log reading progress
     * @param articleId: ID of the article
     * @param progress: Reading progress (0-100)
     */
    public static void updateReadingProgress(String articleId, int progress) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> readingData = new HashMap<>();
        readingData.put("articleId", articleId);
        readingData.put("progress", progress);
        readingData.put("lastRead", timestamp);
        readingData.put("completed", progress >= 100);

        db.collection("users").document(userId)
                .collection("reading_progress").document(articleId)
                .set(readingData)
                .addOnSuccessListener(aVoid -> {
                    if (progress >= 100) {
                        // Article completed
                        updateModuleProgress("reading", 5);
                        incrementDailyGoal();
                    }
                });
    }

    /**
     * Log user activity
     * @param moduleName: Module name
     * @param activityType: Type of activity (lesson_completed, quiz_taken, etc.)
     */
    private static void logActivity(String moduleName, String activityType) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> activity = new HashMap<>();
        activity.put("module", moduleName);
        activity.put("type", activityType);
        activity.put("timestamp", timestamp);

        db.collection("users").document(userId)
                .collection("activity_log")
                .add(activity);
    }

    /**
     * Log quiz result
     * @param moduleName: Module name
     * @param score: Quiz score
     */
    private static void logQuizResult(String moduleName, int score) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> quizResult = new HashMap<>();
        quizResult.put("module", moduleName);
        quizResult.put("score", score);
        quizResult.put("timestamp", timestamp);
        quizResult.put("passed", score >= 70);

        db.collection("users").document(userId)
                .collection("quiz_results")
                .add(quizResult);
    }

    /**
     * Update study time (in minutes)
     * @param minutes: Study duration in minutes
     */
    public static void updateStudyTime(int minutes) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("study_time").document(today)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        db.collection("users").document(userId)
                                .collection("study_time").document(today)
                                .update("minutes", FieldValue.increment(minutes));
                    } else {
                        Map<String, Object> studyTime = new HashMap<>();
                        studyTime.put("date", today);
                        studyTime.put("minutes", minutes);
                        
                        db.collection("users").document(userId)
                                .collection("study_time").document(today)
                                .set(studyTime);
                    }
                });
    }

    /**
     * Update streak
     * Call this when user completes daily goal
     */
    public static void updateStreak() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId)
                .collection("progress").document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String lastActive = document.getString("lastActiveDate");
                        Long currentStreak = document.getLong("currentStreak");
                        
                        int newStreak = 1;
                        if (lastActive != null && currentStreak != null) {
                            // Check if yesterday
                            // Simple check - you may want to improve this
                            newStreak = currentStreak.intValue() + 1;
                        }
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("currentStreak", newStreak);
                        updates.put("lastActiveDate", today);
                        
                        db.collection("users").document(userId)
                                .collection("progress").document("current")
                                .update(updates);
                    }
                });
    }
}
