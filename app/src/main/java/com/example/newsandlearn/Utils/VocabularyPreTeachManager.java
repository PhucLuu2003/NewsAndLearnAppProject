package com.example.newsandlearn.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.newsandlearn.BuildConfig;
import com.example.newsandlearn.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * VocabularyPreTeachManager - Hiển thị từ vựng quan trọng TRƯỚC KHI đọc bài
 * Giúp người học chuẩn bị tốt hơn và tăng khả năng hiểu bài
 */
public class VocabularyPreTeachManager {

    private static VocabularyPreTeachManager instance;
    private GenerativeModelFutures model;
    private Executor executor;
    private List<PreTeachWord> preTeachWords;

    private VocabularyPreTeachManager() {
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "Missing GEMINI_API_KEY. Set it in local.properties (GEMINI_API_KEY=...) or env var GEMINI_API_KEY.");
        }
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
        preTeachWords = new ArrayList<>();
    }

    public static synchronized VocabularyPreTeachManager getInstance() {
        if (instance == null) {
            instance = new VocabularyPreTeachManager();
        }
        return instance;
    }

    /**
     * Phân tích bài viết và chọn 5-10 từ quan trọng nhất để dạy trước
     */
    public void analyzeAndSelectWords(String articleContent, String articleTitle, String userLevel,
            PreTeachCallback callback) {
        preTeachWords.clear();

        String prompt = "Analyze this article and select the 5-10 MOST IMPORTANT vocabulary words that a " + userLevel
                + " learner should know BEFORE reading:\n\n" +
                "Title: " + articleTitle + "\n" +
                "Article: " + articleContent + "\n\n" +
                "Selection criteria:\n" +
                "1. Essential for understanding the main ideas\n" +
                "2. Appear multiple times in the article\n" +
                "3. Challenging but learnable for " + userLevel + " level\n" +
                "4. Not basic words everyone knows\n\n" +
                "Return JSON:\n" +
                "{\n" +
                "  \"key_words\": [\n" +
                "    {\n" +
                "      \"word\": \"the word\",\n" +
                "      \"importance\": 1-10,\n" +
                "      \"frequency\": number of times it appears,\n" +
                "      \"simple_definition\": \"easy to understand definition\",\n" +
                "      \"example_sentence\": \"simple example\",\n" +
                "      \"vietnamese\": \"nghĩa tiếng Việt\",\n" +
                "      \"pronunciation\": \"phonetic pronunciation\",\n" +
                "      \"word_type\": \"noun/verb/adjective/etc\",\n" +
                "      \"memory_tip\": \"helpful way to remember this word\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"article_summary\": \"One sentence summary of what the article is about\"\n" +
                "}\n\n" +
                "Limit to 5-10 words maximum. Return ONLY valid JSON.";

        Content content = new Content.Builder()
                .addText(prompt)
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
                    String summary = json.getString("article_summary");

                    JSONArray wordsArray = json.getJSONArray("key_words");

                    for (int i = 0; i < wordsArray.length(); i++) {
                        JSONObject wordObj = wordsArray.getJSONObject(i);

                        PreTeachWord word = new PreTeachWord();
                        word.word = wordObj.getString("word");
                        word.importance = wordObj.getInt("importance");
                        word.frequency = wordObj.getInt("frequency");
                        word.simpleDefinition = wordObj.getString("simple_definition");
                        word.exampleSentence = wordObj.getString("example_sentence");
                        word.vietnamese = wordObj.getString("vietnamese");
                        word.pronunciation = wordObj.getString("pronunciation");
                        word.wordType = wordObj.getString("word_type");
                        word.memoryTip = wordObj.getString("memory_tip");

                        preTeachWords.add(word);
                    }

                    callback.onSuccess(preTeachWords, summary);

                } catch (Exception e) {
                    callback.onError("Failed to analyze vocabulary: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Hiển thị Pre-Teach dialog trước khi đọc bài
     */
    public void showPreTeachDialog(Context context, String articleSummary, PreTeachResultCallback resultCallback) {
        if (preTeachWords.isEmpty()) {
            resultCallback.onSkipped();
            return;
        }

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_vocabulary_preteach);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tv_preteach_title);
        TextView tvSummary = dialog.findViewById(R.id.tv_article_summary);
        TextView tvInstruction = dialog.findViewById(R.id.tv_instruction);
        LinearLayout wordsContainer = dialog.findViewById(R.id.words_container);
        MaterialButton btnStartReading = dialog.findViewById(R.id.btn_start_reading);
        MaterialButton btnSkip = dialog.findViewById(R.id.btn_skip);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_learning);
        TextView tvProgress = dialog.findViewById(R.id.tv_progress);

        tvTitle.setText("📚 Key Vocabulary Preview");
        tvSummary.setText("This article is about: " + articleSummary);
        tvInstruction.setText("Learn these " + preTeachWords.size() + " important words first:");

        // Add word cards
        final int[] currentWordIndex = { 0 };
        final int[] learnedCount = { 0 };

        showWordCard(context, wordsContainer, currentWordIndex[0], new WordCardCallback() {
            @Override
            public void onWordLearned() {
                learnedCount[0]++;
                currentWordIndex[0]++;

                // Update progress
                int progress = (learnedCount[0] * 100) / preTeachWords.size();
                progressBar.setProgress(progress);
                tvProgress.setText(learnedCount[0] + "/" + preTeachWords.size() + " learned");

                if (currentWordIndex[0] < preTeachWords.size()) {
                    // Show next word
                    wordsContainer.removeAllViews();
                    showWordCard(context, wordsContainer, currentWordIndex[0], this);
                } else {
                    // All words learned
                    btnStartReading.setEnabled(true);
                    btnStartReading.setText("🎉 Start Reading Now!");
                }
            }

            @Override
            public void onWordSkipped() {
                currentWordIndex[0]++;

                if (currentWordIndex[0] < preTeachWords.size()) {
                    wordsContainer.removeAllViews();
                    showWordCard(context, wordsContainer, currentWordIndex[0], this);
                } else {
                    btnStartReading.setEnabled(true);
                }
            }
        });

        btnStartReading.setOnClickListener(v -> {
            dialog.dismiss();
            resultCallback.onCompleted(learnedCount[0], preTeachWords.size());
        });

        btnSkip.setOnClickListener(v -> {
            dialog.dismiss();
            resultCallback.onSkipped();
        });

        dialog.show();
    }

    /**
     * Hiển thị card cho một từ
     */
    private void showWordCard(Context context, LinearLayout container, int wordIndex, WordCardCallback callback) {
        if (wordIndex >= preTeachWords.size()) {
            return;
        }

        PreTeachWord word = preTeachWords.get(wordIndex);

        View cardView = View.inflate(context, R.layout.item_preteach_word_card, null);

        TextView tvWord = cardView.findViewById(R.id.tv_word);
        TextView tvPronunciation = cardView.findViewById(R.id.tv_pronunciation);
        TextView tvWordType = cardView.findViewById(R.id.tv_word_type);
        TextView tvDefinition = cardView.findViewById(R.id.tv_definition);
        TextView tvVietnamese = cardView.findViewById(R.id.tv_vietnamese);
        TextView tvExample = cardView.findViewById(R.id.tv_example);
        TextView tvMemoryTip = cardView.findViewById(R.id.tv_memory_tip);
        ChipGroup chipImportance = cardView.findViewById(R.id.chip_importance);
        MaterialButton btnGotIt = cardView.findViewById(R.id.btn_got_it);
        MaterialButton btnSkipWord = cardView.findViewById(R.id.btn_skip_word);

        tvWord.setText(word.word);
        tvPronunciation.setText("/" + word.pronunciation + "/");
        tvWordType.setText(word.wordType);
        tvDefinition.setText(word.simpleDefinition);
        tvVietnamese.setText("🇻🇳 " + word.vietnamese);
        tvExample.setText("\"" + word.exampleSentence + "\"");
        tvMemoryTip.setText("💡 " + word.memoryTip);

        // Show importance stars
        for (int i = 0; i < Math.min(word.importance, 5); i++) {
            Chip chip = new Chip(context);
            chip.setText("⭐");
            chip.setClickable(false);
            chipImportance.addView(chip);
        }

        btnGotIt.setOnClickListener(v -> {
            word.wasLearned = true;
            callback.onWordLearned();
        });

        btnSkipWord.setOnClickListener(v -> {
            callback.onWordSkipped();
        });

        container.addView(cardView);
    }

    /**
     * Lấy danh sách từ đã pre-teach
     */
    public List<PreTeachWord> getPreTeachWords() {
        return new ArrayList<>(preTeachWords);
    }

    /**
     * Kiểm tra xem từ có trong danh sách pre-teach không
     */
    public boolean isPreTeachWord(String word) {
        for (PreTeachWord ptWord : preTeachWords) {
            if (ptWord.word.equalsIgnoreCase(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reset
     */
    public void reset() {
        preTeachWords.clear();
    }

    // Data classes
    public static class PreTeachWord {
        public String word;
        public int importance;
        public int frequency;
        public String simpleDefinition;
        public String exampleSentence;
        public String vietnamese;
        public String pronunciation;
        public String wordType;
        public String memoryTip;
        public boolean wasLearned = false;
    }

    // Callbacks
    public interface PreTeachCallback {
        void onSuccess(List<PreTeachWord> words, String articleSummary);

        void onError(String error);
    }

    public interface PreTeachResultCallback {
        void onCompleted(int learnedCount, int totalCount);

        void onSkipped();
    }

    private interface WordCardCallback {
        void onWordLearned();

        void onWordSkipped();
    }
}
