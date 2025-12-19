package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.ReadingArticle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SmartRecommendationEngine - AI-powered article recommendations
 * Uses collaborative filtering and content-based filtering
 */
public class SmartRecommendationEngine {
    private static final String TAG = "RecommendationEngine";
    private static SmartRecommendationEngine instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private Map<String, Integer> categoryPreferences;
    private Map<String, Integer> difficultyHistory;
    private List<String> readArticleIds;

    private SmartRecommendationEngine() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        categoryPreferences = new HashMap<>();
        difficultyHistory = new HashMap<>();
        readArticleIds = new ArrayList<>();
    }

    public static synchronized SmartRecommendationEngine getInstance() {
        if (instance == null) {
            instance = new SmartRecommendationEngine();
        }
        return instance;
    }

    // Callback interface
    public interface RecommendationCallback {
        void onSuccess(List<ReadingArticle> recommendations);
        void onFailure(Exception e);
    }

    /**
     * Get personalized recommendations
     */
    public void getRecommendations(int count, RecommendationCallback callback) {
        if (auth.getCurrentUser() == null) {
            // For non-logged in users, return popular articles
            getPopularArticles(count, callback);
            return;
        }

        // Load user reading history first
        loadUserHistory(() -> {
            // Then get recommendations based on history
            generateRecommendations(count, callback);
        });
    }

    /**
     * Get "Read Next" suggestion after finishing an article
     */
    public void getNextArticle(String currentArticleId, RecommendationCallback callback) {
        db.collection("reading_articles").document(currentArticleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ReadingArticle currentArticle = documentSnapshot.toObject(ReadingArticle.class);
                    if (currentArticle != null) {
                        findSimilarArticles(currentArticle, 1, callback);
                    } else {
                        callback.onFailure(new Exception("Article not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get articles similar to a given article
     */
    public void getSimilarArticles(String articleId, int count, RecommendationCallback callback) {
        db.collection("reading_articles").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    ReadingArticle article = documentSnapshot.toObject(ReadingArticle.class);
                    if (article != null) {
                        findSimilarArticles(article, count, callback);
                    } else {
                        callback.onFailure(new Exception("Article not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get trending articles (most read this week)
     */
    public void getTrendingArticles(int count, RecommendationCallback callback) {
        db.collection("reading_articles")
                .orderBy("readCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(count)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingArticle> articles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        articles.add(article);
                    }
                    callback.onSuccess(articles);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get articles by difficulty level
     */
    public void getArticlesByDifficulty(String difficulty, int count, RecommendationCallback callback) {
        db.collection("reading_articles")
                .whereEqualTo("difficulty", difficulty)
                .limit(count)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingArticle> articles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        if (!readArticleIds.contains(article.getId())) {
                            articles.add(article);
                        }
                    }
                    callback.onSuccess(articles);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Get articles by category
     */
    public void getArticlesByCategory(String category, int count, RecommendationCallback callback) {
        db.collection("reading_articles")
                .whereEqualTo("category", category)
                .limit(count)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingArticle> articles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        if (!readArticleIds.contains(article.getId())) {
                            articles.add(article);
                        }
                    }
                    callback.onSuccess(articles);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Load user reading history
     */
    private void loadUserHistory(Runnable onComplete) {
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("reading_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryPreferences.clear();
                    difficultyHistory.clear();
                    readArticleIds.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String articleId = document.getId();
                        String category = document.getString("category");
                        String difficulty = document.getString("difficulty");
                        
                        readArticleIds.add(articleId);
                        
                        if (category != null) {
                            categoryPreferences.put(category, 
                                categoryPreferences.getOrDefault(category, 0) + 1);
                        }
                        
                        if (difficulty != null) {
                            difficultyHistory.put(difficulty, 
                                difficultyHistory.getOrDefault(difficulty, 0) + 1);
                        }
                    }
                    
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user history", e);
                    onComplete.run();
                });
    }

    /**
     * Generate personalized recommendations
     */
    private void generateRecommendations(int count, RecommendationCallback callback) {
        // Get all articles
        db.collection("reading_articles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ArticleScore> scoredArticles = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        
                        // Skip already read articles
                        if (readArticleIds.contains(article.getId())) {
                            continue;
                        }
                        
                        // Calculate recommendation score
                        double score = calculateRecommendationScore(article);
                        scoredArticles.add(new ArticleScore(article, score));
                    }
                    
                    // Sort by score
                    Collections.sort(scoredArticles, (a, b) -> 
                        Double.compare(b.score, a.score));
                    
                    // Return top N
                    List<ReadingArticle> recommendations = new ArrayList<>();
                    for (int i = 0; i < Math.min(count, scoredArticles.size()); i++) {
                        recommendations.add(scoredArticles.get(i).article);
                    }
                    
                    callback.onSuccess(recommendations);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Calculate recommendation score for an article
     */
    private double calculateRecommendationScore(ReadingArticle article) {
        double score = 0.0;
        
        // Category preference (40% weight)
        String category = article.getCategory();
        if (category != null && categoryPreferences.containsKey(category)) {
            score += categoryPreferences.get(category) * 0.4;
        }
        
        // Difficulty match (30% weight)
        String difficulty = article.getDifficulty();
        if (difficulty != null && difficultyHistory.containsKey(difficulty)) {
            score += difficultyHistory.get(difficulty) * 0.3;
        }
        
        // Popularity (20% weight)
        score += article.getReadCount() * 0.02;
        
        // Recency (10% weight) - newer articles get slight boost
        if (article.getCreatedAt() != null) {
            long daysSinceCreation = (System.currentTimeMillis() - article.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24);
            if (daysSinceCreation < 7) {
                score += (7 - daysSinceCreation) * 0.1;
            }
        }
        
        return score;
    }

    /**
     * Find articles similar to a given article
     */
    private void findSimilarArticles(ReadingArticle referenceArticle, int count, RecommendationCallback callback) {
        db.collection("reading_articles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ArticleScore> scoredArticles = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        
                        // Skip the reference article itself and already read articles
                        if (article.getId().equals(referenceArticle.getId()) || 
                            readArticleIds.contains(article.getId())) {
                            continue;
                        }
                        
                        // Calculate similarity score
                        double similarity = calculateSimilarity(referenceArticle, article);
                        scoredArticles.add(new ArticleScore(article, similarity));
                    }
                    
                    // Sort by similarity
                    Collections.sort(scoredArticles, (a, b) -> 
                        Double.compare(b.score, a.score));
                    
                    // Return top N
                    List<ReadingArticle> similar = new ArrayList<>();
                    for (int i = 0; i < Math.min(count, scoredArticles.size()); i++) {
                        similar.add(scoredArticles.get(i).article);
                    }
                    
                    callback.onSuccess(similar);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Calculate similarity between two articles
     */
    private double calculateSimilarity(ReadingArticle article1, ReadingArticle article2) {
        double similarity = 0.0;
        
        // Same category (50% weight)
        if (article1.getCategory() != null && 
            article1.getCategory().equals(article2.getCategory())) {
            similarity += 0.5;
        }
        
        // Same difficulty (30% weight)
        if (article1.getDifficulty() != null && 
            article1.getDifficulty().equals(article2.getDifficulty())) {
            similarity += 0.3;
        }
        
        // Similar word count (20% weight)
        int wordCountDiff = Math.abs(article1.getWordCount() - article2.getWordCount());
        if (wordCountDiff < 100) {
            similarity += 0.2;
        } else if (wordCountDiff < 300) {
            similarity += 0.1;
        }
        
        return similarity;
    }

    /**
     * Get popular articles (for non-logged in users)
     */
    private void getPopularArticles(int count, RecommendationCallback callback) {
        db.collection("reading_articles")
                .orderBy("readCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(count)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReadingArticle> articles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        articles.add(article);
                    }
                    callback.onSuccess(articles);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Helper class to store article with its score
     */
    private static class ArticleScore {
        ReadingArticle article;
        double score;

        ArticleScore(ReadingArticle article, double score) {
            this.article = article;
            this.score = score;
        }
    }
}
