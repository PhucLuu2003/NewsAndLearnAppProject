package com.example.newsandlearn.Utils;

import android.content.Context;
import android.widget.Toast;

import com.example.newsandlearn.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * PreloadingDefinitionsManager - Pre-load định nghĩa từ khó trước khi người
 * dùng đọc đến
 * Sử dụng AI để dự đoán từ nào người dùng sẽ cần tra và load sẵn
 */
public class PreloadingDefinitionsManager {

    private static PreloadingDefinitionsManager instance;
    private GenerativeModelFutures model;
    private Executor executor;
    private Map<String, WordDefinition> preloadedDefinitions;
    private List<String> difficultWords;
    private boolean isPreloading = false;

    private PreloadingDefinitionsManager() {
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "Missing GEMINI_API_KEY. Set it in local.properties (GEMINI_API_KEY=...) or env var GEMINI_API_KEY.");
        }
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
        preloadedDefinitions = new HashMap<>();
        difficultWords = new ArrayList<>();
    }

    public static synchronized PreloadingDefinitionsManager getInstance() {
        if (instance == null) {
            instance = new PreloadingDefinitionsManager();
        }
        return instance;
    }

    /**
     * Phân tích bài viết và pre-load định nghĩa cho từ khó
     */
    public void preloadArticleDefinitions(Context context, String articleContent, String userLevel,
            PreloadCallback callback) {
        if (isPreloading) {
            callback.onError("Already preloading...");
            return;
        }

        isPreloading = true;
        preloadedDefinitions.clear();
        difficultWords.clear();

        // Bước 1: Phân tích và tìm từ khó
        String analysisPrompt = "Analyze this article and identify difficult vocabulary words for a " + userLevel
                + " English learner:\n\n" +
                "Article: " + articleContent + "\n\n" +
                "Return JSON:\n" +
                "{\n" +
                "  \"difficult_words\": [\n" +
                "    {\n" +
                "      \"word\": \"the word\",\n" +
                "      \"difficulty\": 1-10,\n" +
                "      \"context\": \"sentence where it appears\",\n" +
                "      \"definition\": \"simple definition\",\n" +
                "      \"example\": \"example sentence\",\n" +
                "      \"synonyms\": [\"synonym1\", \"synonym2\"],\n" +
                "      \"vietnamese\": \"nghĩa tiếng Việt\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Focus on words that are challenging but important for understanding. Limit to 15-20 words. Return ONLY valid JSON.";

        Content content = new Content.Builder()
                .addText(analysisPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String jsonText = result.getText().trim();

                    // Clean JSON
                    if (jsonText.startsWith("```json")) {
                        jsonText = jsonText.substring(7);
                    }
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 3);
                    }
                    jsonText = jsonText.trim();

                    JSONObject json = new JSONObject(jsonText);
                    JSONArray wordsArray = json.getJSONArray("difficult_words");

                    for (int i = 0; i < wordsArray.length(); i++) {
                        JSONObject wordObj = wordsArray.getJSONObject(i);

                        WordDefinition def = new WordDefinition();
                        def.word = wordObj.getString("word");
                        def.difficulty = wordObj.getInt("difficulty");
                        def.context = wordObj.getString("context");
                        def.definition = wordObj.getString("definition");
                        def.example = wordObj.getString("example");
                        def.vietnamese = wordObj.getString("vietnamese");

                        // Parse synonyms
                        JSONArray synonymsArray = wordObj.getJSONArray("synonyms");
                        def.synonyms = new ArrayList<>();
                        for (int j = 0; j < synonymsArray.length(); j++) {
                            def.synonyms.add(synonymsArray.getString(j));
                        }

                        preloadedDefinitions.put(def.word.toLowerCase(), def);
                        difficultWords.add(def.word);
                    }

                    isPreloading = false;
                    callback.onSuccess(difficultWords.size());

                } catch (Exception e) {
                    isPreloading = false;
                    callback.onError("Failed to parse definitions: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                isPreloading = false;
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Lấy định nghĩa đã được pre-load
     */
    public WordDefinition getPreloadedDefinition(String word) {
        return preloadedDefinitions.get(word.toLowerCase());
    }

    /**
     * Kiểm tra xem từ có được pre-load chưa
     */
    public boolean hasPreloadedDefinition(String word) {
        return preloadedDefinitions.containsKey(word.toLowerCase());
    }

    /**
     * Lấy danh sách tất cả từ khó
     */
    public List<String> getDifficultWords() {
        return new ArrayList<>(difficultWords);
    }

    /**
     * Dự đoán từ tiếp theo người dùng sẽ cần dựa trên vị trí đọc
     */
    public List<String> predictNextWords(String currentText, int currentPosition) {
        List<String> upcomingWords = new ArrayList<>();

        // Lấy 200 ký tự tiếp theo
        int endPosition = Math.min(currentPosition + 200, currentText.length());
        String upcomingText = currentText.substring(currentPosition, endPosition).toLowerCase();

        // Tìm từ khó trong đoạn sắp đọc
        for (String word : difficultWords) {
            if (upcomingText.contains(word.toLowerCase())) {
                upcomingWords.add(word);
            }
        }

        return upcomingWords;
    }

    /**
     * Tạo hint nhỏ cho từ khó sắp tới
     */
    public String generateUpcomingHint(List<String> upcomingWords) {
        if (upcomingWords.isEmpty()) {
            return null;
        }

        StringBuilder hint = new StringBuilder("💡 Upcoming: ");
        for (int i = 0; i < Math.min(3, upcomingWords.size()); i++) {
            if (i > 0)
                hint.append(", ");
            hint.append(upcomingWords.get(i));
        }

        return hint.toString();
    }

    /**
     * Lấy thống kê pre-loading
     */
    public PreloadStats getStats() {
        PreloadStats stats = new PreloadStats();
        stats.totalPreloaded = preloadedDefinitions.size();
        stats.isLoading = isPreloading;

        // Tính độ khó trung bình
        int totalDifficulty = 0;
        for (WordDefinition def : preloadedDefinitions.values()) {
            totalDifficulty += def.difficulty;
        }
        stats.averageDifficulty = preloadedDefinitions.isEmpty() ? 0
                : (float) totalDifficulty / preloadedDefinitions.size();

        return stats;
    }

    /**
     * Clear cache
     */
    public void clearCache() {
        preloadedDefinitions.clear();
        difficultWords.clear();
        isPreloading = false;
    }

    // Data classes
    public static class WordDefinition {
        public String word;
        public int difficulty;
        public String context;
        public String definition;
        public String example;
        public List<String> synonyms;
        public String vietnamese;
        public boolean wasViewed = false;
    }

    public static class PreloadStats {
        public int totalPreloaded;
        public boolean isLoading;
        public float averageDifficulty;
    }

    // Callbacks
    public interface PreloadCallback {
        void onSuccess(int wordsPreloaded);

        void onError(String error);
    }
}
