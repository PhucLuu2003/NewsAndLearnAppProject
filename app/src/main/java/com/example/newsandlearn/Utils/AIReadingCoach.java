package com.example.newsandlearn.Utils;

import android.util.Log;

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
 * AI Reading Coach - Real-time reading assistance with Gemini AI
 * Features:
 * - Sentence-by-sentence analysis
 * - Vocabulary suggestions
 * - Grammar explanations
 * - Difficulty assessment
 * - Personalized tips
 */
public class AIReadingCoach {
    private static final String TAG = "AIReadingCoach";
    private static AIReadingCoach instance;
    private GenerativeModelFutures model;
    private Executor executor;
    
    private String currentArticle = "";
    private String userLevel = "intermediate"; // beginner, intermediate, advanced
    private Map<String, SentenceAnalysis> analysisCache;

    private AIReadingCoach() {
        executor = Executors.newSingleThreadExecutor();
        analysisCache = new HashMap<>();
        initializeModel();
    }

    public static synchronized AIReadingCoach getInstance() {
        if (instance == null) {
            instance = new AIReadingCoach();
        }
        return instance;
    }

    private void initializeModel() {
        try {
            GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                "AIzaSyAXGYeWoZ9y3aerzHUatkdcAXhXWd5EzA8"
            );
            model = GenerativeModelFutures.from(gm);
            Log.d(TAG, "AI Reading Coach initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing model: " + e.getMessage());
        }
    }

    public void setArticleContext(String article) {
        this.currentArticle = article;
        analysisCache.clear();
    }

    public void setUserLevel(String level) {
        this.userLevel = level;
    }

    /**
     * Analyze a sentence in real-time
     */
    public void analyzeSentence(String sentence, AnalysisCallback callback) {
        // Check cache first
        if (analysisCache.containsKey(sentence)) {
            callback.onSuccess(analysisCache.get(sentence));
            return;
        }

        String prompt = buildAnalysisPrompt(sentence);
        
        Content content = new Content.Builder()
            .addText(prompt)
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String text = result.getText();
                    SentenceAnalysis analysis = parseAnalysis(text, sentence);
                    analysisCache.put(sentence, analysis);
                    callback.onSuccess(analysis);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

    /**
     * Get vocabulary insights from entire article
     */
    public void getVocabularyInsights(VocabularyCallback callback) {
        String prompt = "Analyze this article and extract:\n" +
            "1. Top 10 most important vocabulary words\n" +
            "2. Their difficulty level (easy/medium/hard)\n" +
            "3. Brief definition\n" +
            "4. Example usage\n\n" +
            "Article: " + currentArticle + "\n\n" +
            "Return as JSON array: [{\"word\": \"...\", \"level\": \"...\", \"definition\": \"...\", \"example\": \"...\"}]";

        Content content = new Content.Builder()
            .addText(prompt)
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String text = result.getText();
                    List<VocabularyItem> items = parseVocabulary(text);
                    callback.onSuccess(items);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

    /**
     * Assess article difficulty for user
     */
    public void assessDifficulty(DifficultyCallback callback) {
        String prompt = "Analyze this article for a " + userLevel + " English learner.\n" +
            "Provide:\n" +
            "1. Overall difficulty (1-10)\n" +
            "2. Vocabulary difficulty (1-10)\n" +
            "3. Grammar complexity (1-10)\n" +
            "4. Recommended reading time (minutes)\n" +
            "5. Key challenges\n" +
            "6. Learning tips\n\n" +
            "Article: " + currentArticle + "\n\n" +
            "Return as JSON: {\"overall\": 7, \"vocabulary\": 8, \"grammar\": 6, \"readingTime\": 15, \"challenges\": [...], \"tips\": [...]}";

        Content content = new Content.Builder()
            .addText(prompt)
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String text = result.getText();
                    DifficultyAssessment assessment = parseDifficulty(text);
                    callback.onSuccess(assessment);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

    /**
     * Get personalized reading tips
     */
    public void getReadingTips(TipsCallback callback) {
        String prompt = "As an English reading coach, provide 5 personalized tips for a " + 
            userLevel + " learner reading this article:\n\n" +
            currentArticle.substring(0, Math.min(500, currentArticle.length())) + "...\n\n" +
            "Focus on:\n" +
            "- Reading strategies\n" +
            "- Vocabulary building\n" +
            "- Comprehension techniques\n" +
            "- Time management\n\n" +
            "Return as JSON array: [{\"title\": \"...\", \"description\": \"...\", \"icon\": \"💡\"}]";

        Content content = new Content.Builder()
            .addText(prompt)
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String text = result.getText();
                    List<ReadingTip> tips = parseTips(text);
                    callback.onSuccess(tips);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

    // ==================== HELPER METHODS ====================

    private String buildAnalysisPrompt(String sentence) {
        return "Analyze this sentence for an English learner (" + userLevel + " level):\n\n" +
            "\"" + sentence + "\"\n\n" +
            "Provide:\n" +
            "1. Key vocabulary words (with definitions)\n" +
            "2. Grammar structures used\n" +
            "3. Difficulty level (1-10)\n" +
            "4. Learning tip\n\n" +
            "Return as JSON: {\"vocabulary\": [{\"word\": \"...\", \"definition\": \"...\"}], " +
            "\"grammar\": [...], \"difficulty\": 7, \"tip\": \"...\"}";
    }

    private SentenceAnalysis parseAnalysis(String jsonText, String sentence) {
        try {
            // Extract JSON from markdown code blocks if present
            String cleanJson = jsonText.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JSONObject json = new JSONObject(cleanJson);
            
            SentenceAnalysis analysis = new SentenceAnalysis();
            analysis.sentence = sentence;
            analysis.difficulty = json.optInt("difficulty", 5);
            analysis.tip = json.optString("tip", "");
            
            // Parse vocabulary
            JSONArray vocabArray = json.optJSONArray("vocabulary");
            if (vocabArray != null) {
                for (int i = 0; i < vocabArray.length(); i++) {
                    JSONObject vocabObj = vocabArray.getJSONObject(i);
                    VocabWord word = new VocabWord();
                    word.word = vocabObj.optString("word", "");
                    word.definition = vocabObj.optString("definition", "");
                    analysis.vocabulary.add(word);
                }
            }
            
            // Parse grammar
            JSONArray grammarArray = json.optJSONArray("grammar");
            if (grammarArray != null) {
                for (int i = 0; i < grammarArray.length(); i++) {
                    analysis.grammar.add(grammarArray.getString(i));
                }
            }
            
            return analysis;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing analysis: " + e.getMessage());
            return createDefaultAnalysis(sentence);
        }
    }

    private List<VocabularyItem> parseVocabulary(String jsonText) {
        List<VocabularyItem> items = new ArrayList<>();
        try {
            String cleanJson = cleanJsonText(jsonText);
            JSONArray array = new JSONArray(cleanJson);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                VocabularyItem item = new VocabularyItem();
                item.word = obj.optString("word", "");
                item.level = obj.optString("level", "medium");
                item.definition = obj.optString("definition", "");
                item.example = obj.optString("example", "");
                items.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing vocabulary: " + e.getMessage());
        }
        return items;
    }

    private DifficultyAssessment parseDifficulty(String jsonText) {
        try {
            String cleanJson = cleanJsonText(jsonText);
            JSONObject json = new JSONObject(cleanJson);
            
            DifficultyAssessment assessment = new DifficultyAssessment();
            assessment.overall = json.optInt("overall", 5);
            assessment.vocabulary = json.optInt("vocabulary", 5);
            assessment.grammar = json.optInt("grammar", 5);
            assessment.readingTime = json.optInt("readingTime", 10);
            
            JSONArray challenges = json.optJSONArray("challenges");
            if (challenges != null) {
                for (int i = 0; i < challenges.length(); i++) {
                    assessment.challenges.add(challenges.getString(i));
                }
            }
            
            JSONArray tips = json.optJSONArray("tips");
            if (tips != null) {
                for (int i = 0; i < tips.length(); i++) {
                    assessment.tips.add(tips.getString(i));
                }
            }
            
            return assessment;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing difficulty: " + e.getMessage());
            return new DifficultyAssessment();
        }
    }

    private List<ReadingTip> parseTips(String jsonText) {
        List<ReadingTip> tips = new ArrayList<>();
        try {
            String cleanJson = cleanJsonText(jsonText);
            JSONArray array = new JSONArray(cleanJson);
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ReadingTip tip = new ReadingTip();
                tip.title = obj.optString("title", "");
                tip.description = obj.optString("description", "");
                tip.icon = obj.optString("icon", "💡");
                tips.add(tip);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing tips: " + e.getMessage());
        }
        return tips;
    }

    private String cleanJsonText(String text) {
        String clean = text.trim();
        if (clean.startsWith("```json")) clean = clean.substring(7);
        if (clean.startsWith("```")) clean = clean.substring(3);
        if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
        return clean.trim();
    }

    private SentenceAnalysis createDefaultAnalysis(String sentence) {
        SentenceAnalysis analysis = new SentenceAnalysis();
        analysis.sentence = sentence;
        analysis.difficulty = 5;
        analysis.tip = "Read this sentence carefully and look up unfamiliar words.";
        return analysis;
    }

    // ==================== DATA CLASSES ====================

    public static class SentenceAnalysis {
        public String sentence;
        public List<VocabWord> vocabulary = new ArrayList<>();
        public List<String> grammar = new ArrayList<>();
        public int difficulty; // 1-10
        public String tip;
    }

    public static class VocabWord {
        public String word;
        public String definition;
    }

    public static class VocabularyItem {
        public String word;
        public String level; // easy, medium, hard
        public String definition;
        public String example;
    }

    public static class DifficultyAssessment {
        public int overall = 5;
        public int vocabulary = 5;
        public int grammar = 5;
        public int readingTime = 10;
        public List<String> challenges = new ArrayList<>();
        public List<String> tips = new ArrayList<>();
    }

    public static class ReadingTip {
        public String title;
        public String description;
        public String icon;
    }

    // ==================== CALLBACKS ====================

    public interface AnalysisCallback {
        void onSuccess(SentenceAnalysis analysis);
        void onFailure(Exception e);
    }

    public interface VocabularyCallback {
        void onSuccess(List<VocabularyItem> items);
        void onFailure(Exception e);
    }

    public interface DifficultyCallback {
        void onSuccess(DifficultyAssessment assessment);
        void onFailure(Exception e);
    }

    public interface TipsCallback {
        void onSuccess(List<ReadingTip> tips);
        void onFailure(Exception e);
    }
}
