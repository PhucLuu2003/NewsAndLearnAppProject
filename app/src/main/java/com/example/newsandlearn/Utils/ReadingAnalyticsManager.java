package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReadingAnalyticsManager {
    private static final String TAG = "ReadingAnalytics";
    private static ReadingAnalyticsManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private ReadingAnalyticsManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized ReadingAnalyticsManager getInstance() {
        if (instance == null) {
            instance = new ReadingAnalyticsManager();
        }
        return instance;
    }

    public void trackArticleRead(String articleId, String category, String level, int readingTimeMinutes) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("totalArticlesRead", FieldValue.increment(1));
        updates.put("totalReadingTimeMinutes", FieldValue.increment(readingTimeMinutes));
        updates.put("lastReadDate", today);
        
        // Update category count
        if (category != null) {
            updates.put("categoriesRead." + category, FieldValue.increment(1));
        }
        
        // Update level count
        if (level != null) {
            updates.put("levelsRead." + level, FieldValue.increment(1));
        }

        db.collection("users").document(userId)
            .collection("reading_analytics").document("stats")
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Analytics updated successfully");
                updateStreak(userId, today);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error updating analytics", e));
    }

    private void updateStreak(String userId, String today) {
        db.collection("users").document(userId)
            .collection("reading_analytics").document("stats")
            .get()
            .addOnSuccessListener(document -> {
                int currentStreak = 0;
                int longestStreak = 0;
                String lastReadDate = null;

                if (document.exists()) {
                    Long streak = document.getLong("currentStreak");
                    currentStreak = streak != null ? streak.intValue() : 0;
                    
                    Long longest = document.getLong("longestStreak");
                    longestStreak = longest != null ? longest.intValue() : 0;
                    
                    lastReadDate = document.getString("lastReadDate");
                }

                // Calculate new streak
                if (lastReadDate == null || !lastReadDate.equals(today)) {
                    if (isConsecutiveDay(lastReadDate, today)) {
                        currentStreak++;
                    } else if (lastReadDate != null && !lastReadDate.equals(today)) {
                        currentStreak = 1;
                    } else {
                        currentStreak = 1;
                    }

                    if (currentStreak > longestStreak) {
                        longestStreak = currentStreak;
                    }

                    Map<String, Object> streakUpdate = new HashMap<>();
                    streakUpdate.put("currentStreak", currentStreak);
                    streakUpdate.put("longestStreak", longestStreak);

                    db.collection("users").document(userId)
                        .collection("reading_analytics").document("stats")
                        .update(streakUpdate);
                }
            });
    }

    private boolean isConsecutiveDay(String lastDate, String today) {
        if (lastDate == null) return false;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date last = sdf.parse(lastDate);
            Date current = sdf.parse(today);
            
            if (last != null && current != null) {
                long diff = current.getTime() - last.getTime();
                long daysDiff = diff / (1000 * 60 * 60 * 24);
                return daysDiff == 1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dates", e);
        }
        
        return false;
    }

    public void trackVocabularyLearned() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users").document(userId)
            .collection("reading_analytics").document("stats")
            .update("vocabularyLearned", FieldValue.increment(1));
    }

    public void trackQuizCompleted(double score) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        db.collection("users").document(userId)
            .collection("reading_analytics").document("stats")
            .get()
            .addOnSuccessListener(document -> {
                int quizzesTaken = 0;
                double currentAverage = 0.0;

                if (document.exists()) {
                    Long quizzes = document.getLong("quizzesTaken");
                    quizzesTaken = quizzes != null ? quizzes.intValue() : 0;
                    
                    Double avg = document.getDouble("averageQuizScore");
                    currentAverage = avg != null ? avg : 0.0;
                }

                // Calculate new average
                double newAverage = ((currentAverage * quizzesTaken) + score) / (quizzesTaken + 1);

                Map<String, Object> updates = new HashMap<>();
                updates.put("quizzesTaken", quizzesTaken + 1);
                updates.put("averageQuizScore", newAverage);

                db.collection("users").document(userId)
                    .collection("reading_analytics").document("stats")
                    .set(updates, com.google.firebase.firestore.SetOptions.merge());
            });
    }

    public interface AnalyticsCallback {
        void onSuccess(Map<String, Object> analytics);
        void onError(String error);
    }

    public void getAnalytics(AnalyticsCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId)
            .collection("reading_analytics").document("stats")
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    callback.onSuccess(document.getData());
                } else {
                    callback.onSuccess(new HashMap<>());
                }
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
