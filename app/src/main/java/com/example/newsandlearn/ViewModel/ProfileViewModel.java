package com.example.newsandlearn.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsandlearn.Model.Achievement;
import com.example.newsandlearn.Model.SavedContent;
import com.example.newsandlearn.Model.User;
import com.example.newsandlearn.Model.UserStats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";
    private static final String USERS_COLLECTION = "users";
    private static final String ACHIEVEMENTS_COLLECTION = "achievements";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private ListenerRegistration userListener;
    private ListenerRegistration achievementsListener;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Achievement>> achievementsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // LiveData Getters
    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<List<Achievement>> getAchievementsLiveData() {
        return achievementsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Load user profile with realtime updates
    public void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        String userId = currentUser.getUid();

        // Remove previous listener if exists
        if (userListener != null) {
            userListener.remove();
        }

        // Setup realtime listener for user data
        userListener = db.collection(USERS_COLLECTION)
                .document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    isLoading.setValue(false);

                    if (error != null) {
                        Log.e(TAG, "Error loading user profile", error);
                        errorMessage.setValue("Failed to load profile: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        User user = parseUserFromSnapshot(snapshot);
                        userLiveData.setValue(user);
                    } else {
                        // Create new user profile if doesn't exist
                        createNewUserProfile(userId, currentUser.getEmail());
                    }
                });
    }

    // Load achievements with realtime updates
    public void loadAchievements() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();

        // Remove previous listener if exists
        if (achievementsListener != null) {
            achievementsListener.remove();
        }

        // Setup realtime listener for achievements
        achievementsListener = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading achievements", error);
                        return;
                    }

                    List<Achievement> achievements = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Achievement achievement = doc.toObject(Achievement.class);
                            if (achievement.getId() == null) {
                                achievement.setId(doc.getId());
                            }
                            achievements.add(achievement);
                        }
                    }

                    // If no achievements exist, create default ones
                    if (achievements.isEmpty()) {
                        createDefaultAchievements(userId);
                    } else {
                        achievementsLiveData.setValue(achievements);
                    }
                });
    }

    // Parse User object from Firestore snapshot
    private User parseUserFromSnapshot(DocumentSnapshot snapshot) {
        User user = new User();
        user.setUserId(snapshot.getId());
        user.setName(snapshot.getString("name"));
        user.setEmail(snapshot.getString("email"));
        user.setAvatarUrl(snapshot.getString("avatarUrl"));
        user.setRole(snapshot.getString("role") != null ? snapshot.getString("role") : "user");
        user.setLevel(snapshot.getString("level"));

        Long xp = snapshot.getLong("xp");
        user.setXp(xp != null ? xp.intValue() : 0);

        Long dailyGoal = snapshot.getLong("dailyGoal");
        user.setDailyGoal(dailyGoal != null ? dailyGoal.intValue() : 30);

        Long weeklyGoal = snapshot.getLong("weeklyGoal");
        user.setWeeklyGoal(weeklyGoal != null ? weeklyGoal.intValue() : 210);

        Long currentStreak = snapshot.getLong("currentStreak");
        user.setCurrentStreak(currentStreak != null ? currentStreak.intValue() : 0);

        Long longestStreak = snapshot.getLong("longestStreak");
        user.setLongestStreak(longestStreak != null ? longestStreak.intValue() : 0);

        Date lastActive = snapshot.getDate("lastActive");
        user.setLastActive(lastActive != null ? lastActive : new Date());

        // Parse stats
        Map<String, Object> statsMap = (Map<String, Object>) snapshot.get("stats");
        if (statsMap != null) {
            UserStats stats = new UserStats();
            stats.setWordsLearned(getIntFromMap(statsMap, "wordsLearned", 0));
            stats.setLessonsCompleted(getIntFromMap(statsMap, "lessonsCompleted", 0));
            stats.setQuizAccuracy(getIntFromMap(statsMap, "quizAccuracy", 0));
            stats.setAvgStudyTime(getIntFromMap(statsMap, "avgStudyTime", 0));
            stats.setTotalQuizzesTaken(getIntFromMap(statsMap, "totalQuizzesTaken", 0));
            stats.setTotalCorrectAnswers(getIntFromMap(statsMap, "totalCorrectAnswers", 0));
            stats.setTotalStudyMinutes(getIntFromMap(statsMap, "totalStudyMinutes", 0));
            user.setStats(stats);
        } else {
            user.setStats(new UserStats());
        }

        // Parse saved content
        Map<String, Object> savedMap = (Map<String, Object>) snapshot.get("saved");
        if (savedMap != null) {
            SavedContent saved = new SavedContent();
            saved.setVocabularyCount(getIntFromMap(savedMap, "vocabularyCount", 0));
            saved.setArticleCount(getIntFromMap(savedMap, "articleCount", 0));
            saved.setLessonCount(getIntFromMap(savedMap, "lessonCount", 0));
            user.setSaved(saved);
        } else {
            user.setSaved(new SavedContent());
        }

        return user;
    }

    // Helper to safely get int from map
    private int getIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    // Create new user profile for first-time users
    private void createNewUserProfile(String userId, String email) {
        User newUser = new User(userId, "New User", email);

        db.collection(USERS_COLLECTION)
                .document(userId)
                .set(newUser.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user profile created");
                    userLiveData.setValue(newUser);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user profile", e);
                    errorMessage.setValue("Failed to create profile");
                });
    }

    // Create default achievements for new users
    private void createDefaultAchievements(String userId) {
        List<Achievement> defaultAchievements = new ArrayList<>();

        // Streak achievements
        defaultAchievements.add(new Achievement("streak_7", "Week Warrior",
                "Learn for 7 days in a row", "🔥", Achievement.AchievementCategory.STREAK,
                Achievement.AchievementTier.BRONZE, "streak", 7, 50));
        defaultAchievements.add(new Achievement("streak_30", "Month Master",
                "Learn for 30 days in a row", "🔥", Achievement.AchievementCategory.STREAK,
                Achievement.AchievementTier.GOLD, "streak", 30, 200));

        // Vocabulary achievements
        defaultAchievements.add(new Achievement("vocab_50", "Word Explorer",
                "Learn 50 new words", "📚", Achievement.AchievementCategory.VOCABULARY,
                Achievement.AchievementTier.BRONZE, "words_learned", 50, 50));
        defaultAchievements.add(new Achievement("vocab_200", "Vocabulary Expert",
                "Learn 200 new words", "📚", Achievement.AchievementCategory.VOCABULARY,
                Achievement.AchievementTier.GOLD, "words_learned", 200, 200));

        // Reading achievements (Quiz equivalent)
        defaultAchievements.add(new Achievement("quiz_90", "Quiz Master",
                "Achieve 90% accuracy", "🎯", Achievement.AchievementCategory.GENERAL,
                Achievement.AchievementTier.SILVER, "quiz_accuracy", 90, 100));

        // Lesson achievements
        defaultAchievements.add(new Achievement("lesson_10", "Dedicated Learner",
                "Complete 10 lessons", "🎓", Achievement.AchievementCategory.READING,
                Achievement.AchievementTier.BRONZE, "lessons_completed", 10, 50));
        defaultAchievements.add(new Achievement("lesson_50", "Knowledge Seeker",
                "Complete 50 lessons", "🎓", Achievement.AchievementCategory.READING,
                Achievement.AchievementTier.GOLD, "lessons_completed", 50, 250));

        // Batch write achievements
        for (Achievement achievement : defaultAchievements) {
            db.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(ACHIEVEMENTS_COLLECTION)
                    .document(achievement.getId())
                    .set(achievement)
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating achievement: " + achievement.getTitle(), e));
        }

        achievementsLiveData.setValue(defaultAchievements);
    }

    // Update user profile
    public void updateUserProfile(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(user.getUserId())
                .update(user.toMap())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile updated successfully"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user profile", e);
                    errorMessage.setValue("Failed to update profile");
                });
    }

    // Update daily goal
    public void updateDailyGoal(int minutes) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null)
            return;

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .update("dailyGoal", minutes)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Daily goal updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating daily goal", e));
    }

    // Update weekly goal
    public void updateWeeklyGoal(int minutes) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null)
            return;

        db.collection(USERS_COLLECTION)
                .document(currentUser.getUid())
                .update("weeklyGoal", minutes)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Weekly goal updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating weekly goal", e));
    }

    // Sign out
    public void signOut() {
        removeListeners();
        auth.signOut();
    }

    // Remove Firestore listeners
    private void removeListeners() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        if (achievementsListener != null) {
            achievementsListener.remove();
            achievementsListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        removeListeners();
    }
}
