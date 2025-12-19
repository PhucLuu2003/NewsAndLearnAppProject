package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AdvancedReadingAnalytics - Comprehensive reading analytics
 * Tracks reading patterns, speed, comprehension, and generates visualizations
 */
public class AdvancedReadingAnalytics {
    private static final String TAG = "AdvancedAnalytics";
    private static AdvancedReadingAnalytics instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private AdvancedReadingAnalytics() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized AdvancedReadingAnalytics getInstance() {
        if (instance == null) {
            instance = new AdvancedReadingAnalytics();
        }
        return instance;
    }

    // Data classes
    public static class ReadingSession {
        public String articleId;
        public String articleTitle;
        public int wordCount;
        public long startTime;
        public long endTime;
        public int readingSpeed; // WPM
        public int comprehensionScore;
        public String category;
        public String difficulty;

        public ReadingSession() {}

        public long getDurationMinutes() {
            return (endTime - startTime) / (1000 * 60);
        }
    }

    public static class HeatmapData {
        public Map<String, Integer> dateToCount; // Date -> number of articles read
        public int maxCount;
        public int currentStreak;
        public int longestStreak;

        public HeatmapData() {
            dateToCount = new HashMap<>();
            maxCount = 0;
            currentStreak = 0;
            longestStreak = 0;
        }
    }

    public static class CategoryStats {
        public String category;
        public int count;
        public int totalTime; // minutes
        public double avgScore;

        public CategoryStats(String category) {
            this.category = category;
            this.count = 0;
            this.totalTime = 0;
            this.avgScore = 0.0;
        }
    }

    public static class TrendData {
        public List<DataPoint> points;
        
        public TrendData() {
            points = new ArrayList<>();
        }
    }

    public static class DataPoint {
        public String label; // Date or week
        public float value;

        public DataPoint(String label, float value) {
            this.label = label;
            this.value = value;
        }
    }

    // Callback interfaces
    public interface HeatmapCallback {
        void onSuccess(HeatmapData data);
        void onFailure(Exception e);
    }

    public interface StatsCallback {
        void onSuccess(List<CategoryStats> stats);
        void onFailure(Exception e);
    }

    public interface TrendCallback {
        void onSuccess(TrendData data);
        void onFailure(Exception e);
    }

    public interface SessionCallback {
        void onSuccess(List<ReadingSession> sessions);
        void onFailure(Exception e);
    }

    /**
     * Track a reading session
     */
    public void trackReadingSession(ReadingSession session) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("reading_sessions")
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Reading session tracked: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error tracking session", e);
                });
    }

    /**
     * Calculate reading speed (WPM)
     */
    public int calculateReadingSpeed(int wordCount, long timeSpentMs) {
        if (timeSpentMs == 0) return 0;
        double minutes = timeSpentMs / (1000.0 * 60.0);
        return (int) (wordCount / minutes);
    }

    /**
     * Get reading heatmap data (last N days)
     */
    public void getReadingHeatmap(int days, HeatmapCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        // Calculate date range
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        long startDate = calendar.getTimeInMillis();

        db.collection("users").document(userId)
                .collection("reading_sessions")
                .whereGreaterThan("startTime", startDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HeatmapData heatmap = new HeatmapData();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    
                    // Count articles per day
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long startTime = document.getLong("startTime");
                        if (startTime != null) {
                            String date = dateFormat.format(new Date(startTime));
                            int count = heatmap.dateToCount.getOrDefault(date, 0) + 1;
                            heatmap.dateToCount.put(date, count);
                            heatmap.maxCount = Math.max(heatmap.maxCount, count);
                        }
                    }
                    
                    // Calculate streaks
                    calculateStreaks(heatmap);
                    
                    callback.onSuccess(heatmap);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Calculate current and longest streaks
     */
    private void calculateStreaks(HeatmapData heatmap) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        
        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 0;
        
        // Check backwards from today
        for (int i = 0; i < 365; i++) {
            String date = dateFormat.format(calendar.getTime());
            
            if (heatmap.dateToCount.containsKey(date)) {
                tempStreak++;
                if (i < 30) { // Only count as current streak if within last 30 days
                    currentStreak = tempStreak;
                }
                longestStreak = Math.max(longestStreak, tempStreak);
            } else {
                if (i == 0) {
                    // Today has no reading, check yesterday
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    continue;
                }
                tempStreak = 0;
            }
            
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        heatmap.currentStreak = currentStreak;
        heatmap.longestStreak = longestStreak;
    }

    /**
     * Get category statistics
     */
    public void getCategoryStats(StatsCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("reading_sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, CategoryStats> statsMap = new HashMap<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String category = document.getString("category");
                        if (category == null) category = "Other";
                        
                        CategoryStats stats = statsMap.getOrDefault(category, 
                            new CategoryStats(category));
                        
                        stats.count++;
                        
                        Long startTime = document.getLong("startTime");
                        Long endTime = document.getLong("endTime");
                        if (startTime != null && endTime != null) {
                            stats.totalTime += (endTime - startTime) / (1000 * 60);
                        }
                        
                        Long score = document.getLong("comprehensionScore");
                        if (score != null) {
                            stats.avgScore = (stats.avgScore * (stats.count - 1) + score) / stats.count;
                        }
                        
                        statsMap.put(category, stats);
                    }
                    
                    callback.onSuccess(new ArrayList<>(statsMap.values()));
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get comprehension score trends (weekly)
     */
    public void getComprehensionTrends(int weeks, TrendCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -weeks);
        long startDate = calendar.getTimeInMillis();

        db.collection("users").document(userId)
                .collection("reading_sessions")
                .whereGreaterThan("startTime", startDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    TrendData trend = new TrendData();
                    Map<String, List<Integer>> weeklyScores = new HashMap<>();
                    SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-'W'ww", Locale.US);
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long startTime = document.getLong("startTime");
                        Long score = document.getLong("comprehensionScore");
                        
                        if (startTime != null && score != null) {
                            String week = weekFormat.format(new Date(startTime));
                            List<Integer> scores = weeklyScores.getOrDefault(week, new ArrayList<>());
                            scores.add(score.intValue());
                            weeklyScores.put(week, scores);
                        }
                    }
                    
                    // Calculate average for each week
                    for (Map.Entry<String, List<Integer>> entry : weeklyScores.entrySet()) {
                        List<Integer> scores = entry.getValue();
                        float avg = 0;
                        for (int score : scores) {
                            avg += score;
                        }
                        avg /= scores.size();
                        
                        trend.points.add(new DataPoint(entry.getKey(), avg));
                    }
                    
                    // Sort by week
                    trend.points.sort((a, b) -> a.label.compareTo(b.label));
                    
                    callback.onSuccess(trend);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get reading speed trends
     */
    public void getReadingSpeedTrends(int weeks, TrendCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -weeks);
        long startDate = calendar.getTimeInMillis();

        db.collection("users").document(userId)
                .collection("reading_sessions")
                .whereGreaterThan("startTime", startDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    TrendData trend = new TrendData();
                    Map<String, List<Integer>> weeklySpeeds = new HashMap<>();
                    SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-'W'ww", Locale.US);
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long startTime = document.getLong("startTime");
                        Long speed = document.getLong("readingSpeed");
                        
                        if (startTime != null && speed != null && speed > 0) {
                            String week = weekFormat.format(new Date(startTime));
                            List<Integer> speeds = weeklySpeeds.getOrDefault(week, new ArrayList<>());
                            speeds.add(speed.intValue());
                            weeklySpeeds.put(week, speeds);
                        }
                    }
                    
                    // Calculate average for each week
                    for (Map.Entry<String, List<Integer>> entry : weeklySpeeds.entrySet()) {
                        List<Integer> speeds = entry.getValue();
                        float avg = 0;
                        for (int speed : speeds) {
                            avg += speed;
                        }
                        avg /= speeds.size();
                        
                        trend.points.add(new DataPoint(entry.getKey(), avg));
                    }
                    
                    // Sort by week
                    trend.points.sort((a, b) -> a.label.compareTo(b.label));
                    
                    callback.onSuccess(trend);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get recent reading sessions
     */
    public void getRecentSessions(int limit, SessionCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("reading_sessions")
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingSession> sessions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingSession session = document.toObject(ReadingSession.class);
                        sessions.add(session);
                    }
                    callback.onSuccess(sessions);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
