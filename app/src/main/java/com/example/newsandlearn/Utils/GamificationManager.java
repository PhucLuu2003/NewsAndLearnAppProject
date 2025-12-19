package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.ReadingGamification;
import com.example.newsandlearn.Model.ReadingGamification.Badge;
import com.example.newsandlearn.Model.ReadingGamification.BadgeType;
import com.example.newsandlearn.Model.ReadingGamification.ChallengeType;
import com.example.newsandlearn.Model.ReadingGamification.DailyChallenge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * GamificationManager - Manages XP, badges, challenges, and leaderboards
 */
public class GamificationManager {
    private static final String TAG = "GamificationManager";
    private static GamificationManager instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ReadingGamification currentGamification;

    private GamificationManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized GamificationManager getInstance() {
        if (instance == null) {
            instance = new GamificationManager();
        }
        return instance;
    }

    // Callback interfaces
    public interface GamificationCallback {
        void onSuccess(ReadingGamification gamification);
        void onFailure(Exception e);
    }

    public interface BadgeCallback {
        void onBadgeEarned(Badge badge);
    }

    public interface ChallengeCallback {
        void onChallengeCompleted(DailyChallenge challenge);
    }

    /**
     * Load user's gamification data
     */
    public void loadGamification(GamificationCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("gamification").document("stats")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentGamification = documentSnapshot.toObject(ReadingGamification.class);
                    } else {
                        // Create new gamification profile
                        currentGamification = new ReadingGamification(userId);
                        initializeDailyChallenges();
                        saveGamification(null);
                    }
                    callback.onSuccess(currentGamification);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Award XP to user
     */
    public void awardXP(int amount, String reason, GamificationCallback callback, BadgeCallback badgeCallback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    awardXP(amount, reason, callback, badgeCallback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        int oldLevel = currentGamification.getCurrentLevel();
        currentGamification.awardXP(amount, reason);
        int newLevel = currentGamification.getCurrentLevel();

        // Check for level up badge
        if (newLevel > oldLevel && badgeCallback != null) {
            checkLevelBadges(newLevel, badgeCallback);
        }

        saveGamification(callback);
    }

    /**
     * Track article read
     */
    public void trackArticleRead(GamificationCallback callback, ChallengeCallback challengeCallback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    trackArticleRead(callback, challengeCallback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        // Update READ_ARTICLES challenge
        updateChallenge(ChallengeType.READ_ARTICLES, 1, challengeCallback);
        
        // Award XP
        awardXP(50, "Read an article", callback, null);
    }

    /**
     * Track quiz completion
     */
    public void trackQuizCompletion(int score, GamificationCallback callback, 
                                   BadgeCallback badgeCallback, ChallengeCallback challengeCallback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    trackQuizCompletion(score, callback, badgeCallback, challengeCallback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        // Update QUIZ_SCORE challenge
        if (score >= 80) {
            updateChallenge(ChallengeType.QUIZ_SCORE, 1, challengeCallback);
        }

        // Award XP based on score
        int xp = score; // 1 XP per percentage point
        awardXP(xp, "Quiz score: " + score + "%", callback, badgeCallback);

        // Check for perfectionist badge
        if (score == 100 && badgeCallback != null) {
            checkPerfectionistBadge(badgeCallback);
        }
    }

    /**
     * Track vocabulary learned
     */
    public void trackVocabularyLearned(int count, GamificationCallback callback, 
                                      BadgeCallback badgeCallback, ChallengeCallback challengeCallback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    trackVocabularyLearned(count, callback, badgeCallback, challengeCallback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        // Update LEARN_WORDS challenge
        updateChallenge(ChallengeType.LEARN_WORDS, count, challengeCallback);
        
        // Award XP
        awardXP(count * 5, "Learned " + count + " words", callback, badgeCallback);
    }

    /**
     * Track reading time
     */
    public void trackReadingTime(int minutes, GamificationCallback callback, ChallengeCallback challengeCallback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    trackReadingTime(minutes, callback, challengeCallback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        // Update READING_TIME challenge
        updateChallenge(ChallengeType.READING_TIME, minutes, challengeCallback);
        
        // Award XP
        awardXP(minutes * 2, "Read for " + minutes + " minutes", callback, null);
    }

    /**
     * Update challenge progress
     */
    private void updateChallenge(ChallengeType type, int amount, ChallengeCallback callback) {
        List<DailyChallenge> completedBefore = currentGamification.getCompletedChallenges();
        currentGamification.updateChallenge(type, amount);
        List<DailyChallenge> completedAfter = currentGamification.getCompletedChallenges();

        // Check if any new challenges were completed
        if (callback != null && completedAfter.size() > completedBefore.size()) {
            for (DailyChallenge challenge : completedAfter) {
                if (!completedBefore.contains(challenge)) {
                    callback.onChallengeCompleted(challenge);
                }
            }
        }
    }

    /**
     * Initialize daily challenges
     */
    private void initializeDailyChallenges() {
        List<DailyChallenge> challenges = new ArrayList<>();
        
        // Get tomorrow's midnight as expiry
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long expiresAt = calendar.getTimeInMillis();

        // Challenge 1: Read 3 articles
        challenges.add(new DailyChallenge(
            "daily_read_3",
            "Daily Reader",
            "Read 3 articles today",
            ChallengeType.READ_ARTICLES,
            3,
            100,
            expiresAt
        ));

        // Challenge 2: Score 80%+ on a quiz
        challenges.add(new DailyChallenge(
            "daily_quiz_80",
            "Quiz Master",
            "Score 80% or higher on a quiz",
            ChallengeType.QUIZ_SCORE,
            1,
            150,
            expiresAt
        ));

        // Challenge 3: Learn 20 new words
        challenges.add(new DailyChallenge(
            "daily_vocab_20",
            "Word Collector",
            "Learn 20 new words today",
            ChallengeType.LEARN_WORDS,
            20,
            120,
            expiresAt
        ));

        currentGamification.setActiveChallenges(challenges);
    }

    /**
     * Refresh daily challenges
     */
    public void refreshDailyChallenges(GamificationCallback callback) {
        if (currentGamification == null) {
            loadGamification(new GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    refreshDailyChallenges(callback);
                }
                @Override
                public void onFailure(Exception e) {
                    if (callback != null) callback.onFailure(e);
                }
            });
            return;
        }

        currentGamification.removeExpiredChallenges();
        
        // If no active challenges, create new ones
        if (currentGamification.getActiveChallenges().isEmpty()) {
            initializeDailyChallenges();
        }

        saveGamification(callback);
    }

    /**
     * Check and award level badges
     */
    private void checkLevelBadges(int level, BadgeCallback callback) {
        if (level == 10 && !currentGamification.hasBadge("level_10")) {
            Badge badge = new Badge(
                "level_10",
                "Rising Star",
                "Reached Level 10",
                "ic_badge_level_10",
                BadgeType.SCHOLAR
            );
            currentGamification.earnBadge(badge);
            callback.onBadgeEarned(badge);
        } else if (level == 25 && !currentGamification.hasBadge("level_25")) {
            Badge badge = new Badge(
                "level_25",
                "Expert Reader",
                "Reached Level 25",
                "ic_badge_level_25",
                BadgeType.SCHOLAR
            );
            currentGamification.earnBadge(badge);
            callback.onBadgeEarned(badge);
        } else if (level == 50 && !currentGamification.hasBadge("level_50")) {
            Badge badge = new Badge(
                "level_50",
                "Master Scholar",
                "Reached Level 50",
                "ic_badge_level_50",
                BadgeType.SCHOLAR
            );
            currentGamification.earnBadge(badge);
            callback.onBadgeEarned(badge);
        }
    }

    /**
     * Check perfectionist badge (5 perfect scores in a row)
     */
    private void checkPerfectionistBadge(BadgeCallback callback) {
        // TODO: Track consecutive perfect scores
        // For now, just award the badge
        if (!currentGamification.hasBadge("perfectionist")) {
            Badge badge = new Badge(
                "perfectionist",
                "Perfectionist",
                "Scored 100% on 5 quizzes in a row",
                "ic_badge_perfectionist",
                BadgeType.PERFECTIONIST
            );
            currentGamification.earnBadge(badge);
            callback.onBadgeEarned(badge);
        }
    }

    /**
     * Save gamification data to Firebase
     */
    private void saveGamification(GamificationCallback callback) {
        if (auth.getCurrentUser() == null || currentGamification == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("gamification").document("stats")
                .set(currentGamification)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess(currentGamification);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving gamification", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Get current gamification data (cached)
     */
    public ReadingGamification getCurrentGamification() {
        return currentGamification;
    }
}
