package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.Achievement;
import com.example.newsandlearn.Model.DailyTask;
import com.example.newsandlearn.Model.GrammarExercise;
import com.example.newsandlearn.Model.GrammarLesson;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularySet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ProgressManager - Manages user progress, XP, achievements, and daily tasks
 * Centralized manager for all gamification features
 */
public class ProgressManager {
    
    private static final String TAG = "ProgressManager";
    
    private static ProgressManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    
    // Callbacks
    public interface ProgressCallback {
        void onSuccess(UserProgress progress);
        void onFailure(Exception e);
    }
    
    public interface AchievementCallback {
        void onAchievementUnlocked(Achievement achievement);
        void onFailure(Exception e);
    }
    
    public interface TaskCallback {
        void onSuccess(List<DailyTask> tasks);
        void onFailure(Exception e);
    }
    
    private ProgressManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
    }
    
    public static synchronized ProgressManager getInstance() {
        if (instance == null) {
            instance = new ProgressManager();
        }
        return instance;
    }
    
    // ==================== USER PROGRESS ====================
    
    /**
     * Get user's progress data
     */
    public void getUserProgress(ProgressCallback callback) {
        if (userId == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection("users").document(userId)
                .collection("progress").document("current")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProgress progress;
                    if (documentSnapshot.exists()) {
                        progress = documentSnapshot.toObject(UserProgress.class);
                    } else {
                        // Create new progress
                        progress = new UserProgress(userId);
                        saveUserProgress(progress, null);
                    }
                    callback.onSuccess(progress);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Save user progress
     */
    public void saveUserProgress(UserProgress progress, ProgressCallback callback) {
        if (userId == null) return;
        
        progress.setUpdatedAt(new Date());
        
        db.collection("users").document(userId)
                .collection("progress").document("current")
                .set(progress, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(progress);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving progress", e);
                    if (callback != null) callback.onFailure(e);
                });
    }
    
    /**
     * Add XP to user
     */
    public void addXP(int xp, ProgressCallback callback) {
        getUserProgress(new ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                boolean leveledUp = progress.addXP(xp);
                progress.updateStreak();
                saveUserProgress(progress, callback);
                
                if (leveledUp) {
                    Log.d(TAG, "User leveled up to level " + progress.getCurrentLevel());
                    // TODO: Show level up celebration
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    /**
     * Track article read
     */
    public void trackArticleRead(ProgressCallback callback) {
        getUserProgress(new ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                progress.incrementArticlesRead();
                progress.addXP(50); // 50 XP for reading an article
                progress.updateStreak();
                saveUserProgress(progress, callback);
                
                // Update daily task
                updateDailyTaskProgress(DailyTask.TaskType.READ_ARTICLE, 1);
                
                // Check achievements
                checkAchievements("articles_read", progress.getTotalArticlesRead());
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    /**
     * Track words learned
     */
    public void trackWordsLearned(int count, ProgressCallback callback) {
        getUserProgress(new ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                progress.incrementWordsLearned(count);
                progress.addXP(count * 10); // 10 XP per word
                progress.updateStreak();
                saveUserProgress(progress, callback);
                
                // Update daily task
                updateDailyTaskProgress(DailyTask.TaskType.LEARN_WORDS, count);
                
                // Check achievements
                checkAchievements("words_learned", progress.getTotalWordsLearned());
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    /**
     * Track lesson completed
     */
    public void trackLessonCompleted(ProgressCallback callback) {
        getUserProgress(new ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                progress.incrementLessonsCompleted();
                progress.addXP(100); // 100 XP for completing a lesson
                progress.updateStreak();
                saveUserProgress(progress, callback);
                
                // Update daily task
                updateDailyTaskProgress(DailyTask.TaskType.COMPLETE_LESSON, 1);
                
                // Check achievements
                checkAchievements("lessons_completed", progress.getTotalLessonsCompleted());
            }
            
            @Override
            public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }
    
    // ==================== ACHIEVEMENTS ====================
    
    /**
     * Check and unlock achievements based on progress
     */
    private void checkAchievements(String conditionType, int currentValue) {
        if (userId == null) return;
        
        db.collection("achievements")
                .whereEqualTo("conditionType", conditionType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Achievement achievement = doc.toObject(Achievement.class);
                        if (achievement != null && !achievement.isUnlocked()) {
                            if (achievement.checkUnlock(currentValue)) {
                                unlockAchievement(achievement);
                            }
                        }
                    }
                });
    }
    
    /**
     * Unlock an achievement
     */
    private void unlockAchievement(Achievement achievement) {
        if (userId == null) return;
        
        achievement.unlock();
        
        // Save to user's achievements
        db.collection("users").document(userId)
                .collection("achievements").document(achievement.getId())
                .set(achievement)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Achievement unlocked: " + achievement.getTitle());
                    
                    // Award XP
                    addXP(achievement.getXpReward(), null);
                    
                    // TODO: Show achievement unlock animation
                });
    }
    
    /**
     * Get user's achievements
     */
    public void getUserAchievements(AchievementCallback callback) {
        if (userId == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        db.collection("users").document(userId)
                .collection("achievements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Achievement> achievements = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Achievement achievement = doc.toObject(Achievement.class);
                        if (achievement != null) {
                            achievements.add(achievement);
                        }
                    }
                    // TODO: Return achievements via callback
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    // ==================== DAILY TASKS ====================
    
    /**
     * Get today's tasks
     */
    public void getTodaysTasks(TaskCallback callback) {
        if (userId == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        db.collection("users").document(userId)
                .collection("daily_tasks").document(today)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<DailyTask> tasks;
                    if (documentSnapshot.exists()) {
                        // Load existing tasks
                        tasks = (List<DailyTask>) documentSnapshot.get("tasks");
                        if (tasks == null) tasks = new ArrayList<>();
                    } else {
                        // Generate new tasks for today
                        tasks = generateDailyTasks();
                        saveDailyTasks(today, tasks);
                    }
                    callback.onSuccess(tasks);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    /**
     * Generate daily tasks
     */
    private List<DailyTask> generateDailyTasks() {
        List<DailyTask> tasks = new ArrayList<>();
        
        // Task 1: Read an article
        tasks.add(new DailyTask(
                "task_read_article",
                "Đọc 1 bài báo",
                "Đọc một bài báo tiếng Anh",
                DailyTask.TaskType.READ_ARTICLE,
                1,
                "bài",
                50
        ));
        
        // Task 2: Learn 10 words
        tasks.add(new DailyTask(
                "task_learn_words",
                "Học 10 từ vựng",
                "Học 10 từ vựng mới",
                DailyTask.TaskType.LEARN_WORDS,
                10,
                "từ",
                100
        ));
        
        // Task 3: Complete a lesson
        tasks.add(new DailyTask(
                "task_complete_lesson",
                "Hoàn thành 1 bài học",
                "Hoàn thành một bài học ngữ pháp",
                DailyTask.TaskType.COMPLETE_LESSON,
                1,
                "bài",
                100
        ));
        
        // Task 4: Spend 15 minutes
        tasks.add(new DailyTask(
                "task_spend_time",
                "Học 15 phút",
                "Dành 15 phút học tiếng Anh",
                DailyTask.TaskType.SPEND_TIME,
                15,
                "phút",
                75
        ));
        
        return tasks;
    }
    
    /**
     * Save daily tasks
     */
    private void saveDailyTasks(String date, List<DailyTask> tasks) {
        if (userId == null) return;
        
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("tasks", tasks);
        
        db.collection("users").document(userId)
                .collection("daily_tasks").document(date)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Daily tasks saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving daily tasks", e));
    }
    
    /**
     * Update daily task progress
     */
    public void updateDailyTaskProgress(DailyTask.TaskType taskType, int increment) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        getTodaysTasks(new TaskCallback() {
            @Override
            public void onSuccess(List<DailyTask> tasks) {
                boolean updated = false;
                for (DailyTask task : tasks) {
                    if (task.getType() == taskType) {
                        task.updateProgress(task.getCurrentValue() + increment);
                        updated = true;
                        
                        if (task.isCompleted()) {
                            // Award XP
                            addXP(task.getXpReward(), null);
                        }
                    }
                }
                
                if (updated) {
                    saveDailyTasks(today, tasks);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error updating task progress", e);
            }
        });
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Initialize user progress (call on first login)
     */
    public void initializeUserProgress() {
        if (userId == null) return;
        
        getUserProgress(new ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                // Progress already exists or created
                Log.d(TAG, "User progress initialized");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error initializing progress", e);
            }
        });
    }
}
