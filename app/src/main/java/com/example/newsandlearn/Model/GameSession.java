package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model cho session chơi game
 */
public class GameSession {
    private String songId;
    private long startTime;
    private long endTime;
    private int score;
    private int maxCombo;
    private int currentCombo;
    private float accuracy;
    private List<HitResult> hitResults;
    private int perfectCount;
    private int greatCount;
    private int goodCount;
    private int missCount;

    public GameSession(String songId) {
        this.songId = songId;
        this.startTime = System.currentTimeMillis();
        this.hitResults = new ArrayList<>();
        this.currentCombo = 0;
        this.maxCombo = 0;
    }

    public static class HitResult {
        private String word;
        private String spokenWord;
        private float pronunciationAccuracy;
        private float timingAccuracy;
        private int scoreEarned;
        private String rating; // "PERFECT", "GREAT", "GOOD", "MISS"
        private long timestamp;

        public HitResult(String word, String spokenWord, float pronAccuracy, 
                        float timingAccuracy, int score, String rating) {
            this.word = word;
            this.spokenWord = spokenWord;
            this.pronunciationAccuracy = pronAccuracy;
            this.timingAccuracy = timingAccuracy;
            this.scoreEarned = score;
            this.rating = rating;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public String getWord() { return word; }
        public String getSpokenWord() { return spokenWord; }
        public float getPronunciationAccuracy() { return pronunciationAccuracy; }
        public float getTimingAccuracy() { return timingAccuracy; }
        public int getScoreEarned() { return scoreEarned; }
        public String getRating() { return rating; }
        public long getTimestamp() { return timestamp; }
    }

    public void addHitResult(HitResult result) {
        hitResults.add(result);
        
        // Update counters
        switch (result.getRating()) {
            case "PERFECT":
                perfectCount++;
                currentCombo++;
                break;
            case "GREAT":
                greatCount++;
                currentCombo++;
                break;
            case "GOOD":
                goodCount++;
                currentCombo = 0;
                break;
            case "MISS":
                missCount++;
                currentCombo = 0;
                break;
        }
        
        // Update max combo
        if (currentCombo > maxCombo) {
            maxCombo = currentCombo;
        }
        
        // Update score
        score += result.getScoreEarned();
    }

    public void endSession() {
        this.endTime = System.currentTimeMillis();
        calculateAccuracy();
    }

    private void calculateAccuracy() {
        int totalHits = perfectCount + greatCount + goodCount + missCount;
        if (totalHits > 0) {
            int successfulHits = perfectCount + greatCount + goodCount;
            this.accuracy = (successfulHits * 100.0f) / totalHits;
        }
    }

    public String getRank() {
        if (accuracy >= 95) return "S";
        if (accuracy >= 90) return "A";
        if (accuracy >= 80) return "B";
        if (accuracy >= 70) return "C";
        return "D";
    }

    public long getDurationMs() {
        return endTime - startTime;
    }

    // Getters and Setters
    public String getSongId() { return songId; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getScore() { return score; }
    public int getMaxCombo() { return maxCombo; }
    public int getCurrentCombo() { return currentCombo; }
    public float getAccuracy() { return accuracy; }
    public List<HitResult> getHitResults() { return hitResults; }
    public int getPerfectCount() { return perfectCount; }
    public int getGreatCount() { return greatCount; }
    public int getGoodCount() { return goodCount; }
    public int getMissCount() { return missCount; }
    
    public void setCurrentCombo(int combo) { 
        this.currentCombo = combo;
        if (combo > maxCombo) maxCombo = combo;
    }
}
