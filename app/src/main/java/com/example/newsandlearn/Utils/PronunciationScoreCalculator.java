package com.example.newsandlearn.Utils;

/**
 * Tính toán điểm số cho Pronunciation Beat game
 */
public class PronunciationScoreCalculator {

    // Timing windows (in milliseconds)
    private static final long PERFECT_WINDOW = 50;  // ±50ms
    private static final long GREAT_WINDOW = 150;   // ±150ms
    private static final long GOOD_WINDOW = 300;    // ±300ms

    // Base scores
    private static final int PERFECT_BASE_SCORE = 100;
    private static final int GREAT_BASE_SCORE = 80;
    private static final int GOOD_BASE_SCORE = 60;

    public static class ScoreResult {
        private int score;
        private String rating; // "PERFECT", "GREAT", "GOOD", "MISS"
        private float pronunciationAccuracy;
        private float timingAccuracy;

        public ScoreResult(int score, String rating, float pronAccuracy, float timingAccuracy) {
            this.score = score;
            this.rating = rating;
            this.pronunciationAccuracy = pronAccuracy;
            this.timingAccuracy = timingAccuracy;
        }

        public boolean isPerfect() { return "PERFECT".equals(rating); }
        public boolean isGreat() { return "GREAT".equals(rating); }
        public boolean isGood() { return "GOOD".equals(rating); }
        public boolean isMiss() { return "MISS".equals(rating); }

        // Getters
        public int getScore() { return score; }
        public String getRating() { return rating; }
        public float getPronunciationAccuracy() { return pronunciationAccuracy; }
        public float getTimingAccuracy() { return timingAccuracy; }
    }

    /**
     * Tính điểm dựa trên pronunciation accuracy, timing, combo và difficulty
     */
    public static ScoreResult calculateScore(
            String targetWord,
            String spokenWord,
            long timingDiffMs,
            int combo,
            int wordDifficulty
    ) {
        // 1. Calculate pronunciation accuracy
        float pronAccuracy = calculatePronunciationAccuracy(targetWord, spokenWord);
        
        // 2. Calculate timing accuracy
        float timingAccuracy = calculateTimingAccuracy(timingDiffMs);
        
        // 3. Determine rating based on timing
        String rating = determineRating(timingDiffMs, pronAccuracy);
        
        // 4. Calculate base score
        int baseScore = getBaseScore(rating);
        
        // 5. Apply pronunciation bonus
        int pronBonus = (int) (pronAccuracy * 0.3f);
        
        // 6. Apply difficulty multiplier
        float diffMultiplier = 1.0f + (wordDifficulty * 0.2f);
        
        // 7. Apply combo multiplier
        float comboMultiplier = Math.min(combo, 10);
        if (comboMultiplier == 0) comboMultiplier = 1;
        
        // 8. Calculate final score
        int finalScore = (int) ((baseScore + pronBonus) * diffMultiplier * comboMultiplier);
        
        return new ScoreResult(finalScore, rating, pronAccuracy, timingAccuracy);
    }

    /**
     * Tính pronunciation accuracy bằng Levenshtein distance
     */
    private static float calculatePronunciationAccuracy(String target, String spoken) {
        if (target == null || spoken == null) return 0f;
        
        target = target.toLowerCase().trim();
        spoken = spoken.toLowerCase().trim();
        
        // Exact match
        if (target.equals(spoken)) return 100f;
        
        // Calculate Levenshtein distance
        int distance = levenshteinDistance(target, spoken);
        int maxLen = Math.max(target.length(), spoken.length());
        
        float similarity = 1.0f - ((float) distance / maxLen);
        return Math.max(0, similarity * 100);
    }

    /**
     * Levenshtein distance algorithm
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Tính timing accuracy (0-100%)
     */
    private static float calculateTimingAccuracy(long timingDiffMs) {
        if (timingDiffMs <= PERFECT_WINDOW) return 100f;
        if (timingDiffMs <= GREAT_WINDOW) return 80f;
        if (timingDiffMs <= GOOD_WINDOW) return 60f;
        return 0f;
    }

    /**
     * Xác định rating dựa trên timing và pronunciation
     */
    private static String determineRating(long timingDiffMs, float pronAccuracy) {
        // Phải phát âm đúng ít nhất 70% mới được điểm
        if (pronAccuracy < 70) return "MISS";
        
        if (timingDiffMs <= PERFECT_WINDOW) return "PERFECT";
        if (timingDiffMs <= GREAT_WINDOW) return "GREAT";
        if (timingDiffMs <= GOOD_WINDOW) return "GOOD";
        return "MISS";
    }

    /**
     * Lấy base score theo rating
     */
    private static int getBaseScore(String rating) {
        switch (rating) {
            case "PERFECT": return PERFECT_BASE_SCORE;
            case "GREAT": return GREAT_BASE_SCORE;
            case "GOOD": return GOOD_BASE_SCORE;
            default: return 0;
        }
    }

    /**
     * Tính rank dựa trên accuracy
     */
    public static String getRank(float accuracy) {
        if (accuracy >= 95) return "S";
        if (accuracy >= 90) return "A";
        if (accuracy >= 80) return "B";
        if (accuracy >= 70) return "C";
        return "D";
    }

    /**
     * Lấy màu theo rating
     */
    public static int getColorForRating(String rating) {
        switch (rating) {
            case "PERFECT": return 0xFFFFD700; // Gold
            case "GREAT": return 0xFF4CAF50; // Green
            case "GOOD": return 0xFFFFC107; // Yellow
            default: return 0xFFF44336; // Red
        }
    }
}
