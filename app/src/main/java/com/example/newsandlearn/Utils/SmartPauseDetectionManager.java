package com.example.newsandlearn.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.example.newsandlearn.BuildConfig;
import com.example.newsandlearn.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * SmartPauseDetectionManager - Phát hiện khi người dùng dừng đọc và tự động đề
 * xuất giúp đỡ
 * Sử dụng scroll behavior analysis để hiểu khi nào người dùng gặp khó khăn
 */
public class SmartPauseDetectionManager {

    private static final String TAG = "SmartPauseDetectionManager";
    private static SmartPauseDetectionManager instance;
    private GenerativeModelFutures model;
    private Executor executor;
    private Handler handler;
    private boolean isAIAvailable = false;

    private long lastScrollTime = 0;
    private int lastScrollPosition = 0;
    private boolean isPaused = false;
    private Runnable pauseCheckRunnable;
    private Dialog currentHelpDialog;

    // Configuration
    private static final long PAUSE_THRESHOLD_MS = 3000; // 3 seconds without scrolling
    private static final int REREAD_THRESHOLD = 2; // Number of times re-reading same section
    private int rereadCount = 0;
    private int lastRereadPosition = -1;

    private SmartPauseDetectionManager() {
        String apiKey = null;
        try {
            apiKey = BuildConfig.GEMINI_API_KEY;
        } catch (Exception e) {
            // Field might not exist
        }
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("null")) {
            android.util.Log.w(TAG, "GEMINI_API_KEY not set. Smart pause detection will be disabled.");
            model = null;
            isAIAvailable = false;
        } else {
            try {
                GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", apiKey);
                model = GenerativeModelFutures.from(gm);
                isAIAvailable = true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to initialize Gemini AI: " + e.getMessage());
                model = null;
                isAIAvailable = false;
            }
        }
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SmartPauseDetectionManager getInstance() {
        if (instance == null) {
            instance = new SmartPauseDetectionManager();
        }
        return instance;
    }
    
    public boolean isAIAvailable() {
        return isAIAvailable && model != null;
    }

    /**
     * Bắt đầu theo dõi scroll behavior
     */
    public void startTracking(Context context, String fullText, PauseDetectionCallback callback) {
        lastScrollTime = System.currentTimeMillis();
        isPaused = false;

        // Setup pause detection
        pauseCheckRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastScroll = currentTime - lastScrollTime;

                if (timeSinceLastScroll >= PAUSE_THRESHOLD_MS && !isPaused) {
                    isPaused = true;
                    onPauseDetected(context, fullText, lastScrollPosition, callback);
                }

                // Continue checking
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(pauseCheckRunnable, 1000);
    }

    /**
     * Cập nhật vị trí scroll
     */
    public void updateScrollPosition(int scrollY, int totalHeight) {
        long currentTime = System.currentTimeMillis();

        // Check for re-reading (scrolling back)
        if (scrollY < lastScrollPosition - 50) {
            if (Math.abs(scrollY - lastRereadPosition) < 100) {
                rereadCount++;
            } else {
                rereadCount = 1;
                lastRereadPosition = scrollY;
            }
        }

        lastScrollTime = currentTime;
        lastScrollPosition = scrollY;
        isPaused = false;
    }

    /**
     * Xử lý khi phát hiện pause
     */
    private void onPauseDetected(Context context, String fullText, int scrollPosition,
            PauseDetectionCallback callback) {
        // Tính vị trí ước tính trong text
        String currentSection = extractCurrentSection(fullText, scrollPosition);

        // Phân tích tại sao người dùng dừng lại
        analyzePauseReason(context, currentSection, rereadCount, new PauseAnalysisCallback() {
            @Override
            public void onAnalysisComplete(PauseAnalysis analysis) {
                handler.post(() -> {
                    showHelpDialog(context, analysis, callback);
                });
            }

            @Override
            public void onError(String error) {
                // Silent fail hoặc show generic help
                handler.post(() -> {
                    if (callback != null) {
                        callback.onPauseDetected(scrollPosition, null);
                    }
                });
            }
        });
    }

    /**
     * Phân tích lý do pause bằng AI
     */
    private void analyzePauseReason(Context context, String section, int rereadCount, PauseAnalysisCallback callback) {
        // Check if AI is available first
        if (!isAIAvailable()) {
            callback.onError("AI features not available");
            return;
        }
        
        String prompt = "Analyze why a reader might pause at this section:\n\n" +
                "Section: " + section + "\n" +
                "Re-read count: " + rereadCount + "\n\n" +
                "Return JSON:\n" +
                "{\n" +
                "  \"likely_reason\": \"Why they paused (vocabulary/grammar/complexity/confusion)\",\n" +
                "  \"difficulty_score\": 1-10,\n" +
                "  \"suggested_help\": \"What help to offer\",\n" +
                "  \"key_concept\": \"Main concept they might struggle with\",\n" +
                "  \"simplified_explanation\": \"Simpler way to explain this section\"\n" +
                "}\n\n" +
                "Be concise and helpful. Return ONLY valid JSON.";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String jsonText = result.getText().trim();

                    if (jsonText.startsWith("```json")) {
                        jsonText = jsonText.substring(7);
                    }
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 3);
                    }
                    jsonText = jsonText.trim();

                    JSONObject json = new JSONObject(jsonText);

                    PauseAnalysis analysis = new PauseAnalysis();
                    analysis.section = section;
                    analysis.likelyReason = json.getString("likely_reason");
                    analysis.difficultyScore = json.getInt("difficulty_score");
                    analysis.suggestedHelp = json.getString("suggested_help");
                    analysis.keyConcept = json.getString("key_concept");
                    analysis.simplifiedExplanation = json.getString("simplified_explanation");

                    callback.onAnalysisComplete(analysis);

                } catch (Exception e) {
                    callback.onError("Analysis failed: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Hiển thị dialog giúp đỡ
     */
    private void showHelpDialog(Context context, PauseAnalysis analysis, PauseDetectionCallback callback) {
        if (currentHelpDialog != null && currentHelpDialog.isShowing()) {
            return; // Don't show multiple dialogs
        }

        currentHelpDialog = new Dialog(context);
        currentHelpDialog.setContentView(R.layout.dialog_smart_help);
        currentHelpDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitle = currentHelpDialog.findViewById(R.id.tv_help_title);
        TextView tvReason = currentHelpDialog.findViewById(R.id.tv_pause_reason);
        TextView tvExplanation = currentHelpDialog.findViewById(R.id.tv_simplified_explanation);
        MaterialButton btnGetHelp = currentHelpDialog.findViewById(R.id.btn_get_help);
        MaterialButton btnContinue = currentHelpDialog.findViewById(R.id.btn_continue_reading);

        tvTitle.setText("🤔 Need help with this section?");
        tvReason.setText("It looks like: " + analysis.likelyReason);
        tvExplanation.setText(analysis.simplifiedExplanation);

        btnGetHelp.setOnClickListener(v -> {
            currentHelpDialog.dismiss();
            if (callback != null) {
                callback.onHelpRequested(analysis);
            }
        });

        btnContinue.setOnClickListener(v -> {
            currentHelpDialog.dismiss();
            if (callback != null) {
                callback.onContinueReading();
            }
        });

        currentHelpDialog.show();

        if (callback != null) {
            callback.onPauseDetected(lastScrollPosition, analysis);
        }
    }

    /**
     * Trích xuất section hiện tại từ vị trí scroll
     */
    private String extractCurrentSection(String fullText, int scrollPosition) {
        // Ước tính vị trí trong text (giả sử 1000px = 500 characters)
        int estimatedCharPosition = (scrollPosition / 2);

        int start = Math.max(0, estimatedCharPosition - 150);
        int end = Math.min(fullText.length(), estimatedCharPosition + 150);

        return fullText.substring(start, end);
    }

    /**
     * Dừng tracking
     */
    public void stopTracking() {
        if (pauseCheckRunnable != null) {
            handler.removeCallbacks(pauseCheckRunnable);
        }
        if (currentHelpDialog != null && currentHelpDialog.isShowing()) {
            currentHelpDialog.dismiss();
        }
        isPaused = false;
        rereadCount = 0;
    }

    /**
     * Kiểm tra xem có đang pause không
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Lấy số lần re-read
     */
    public int getRereadCount() {
        return rereadCount;
    }

    // Data classes
    public static class PauseAnalysis {
        public String section;
        public String likelyReason;
        public int difficultyScore;
        public String suggestedHelp;
        public String keyConcept;
        public String simplifiedExplanation;
    }

    // Callbacks
    public interface PauseDetectionCallback {
        void onPauseDetected(int position, PauseAnalysis analysis);

        void onHelpRequested(PauseAnalysis analysis);

        void onContinueReading();
    }

    private interface PauseAnalysisCallback {
        void onAnalysisComplete(PauseAnalysis analysis);

        void onError(String error);
    }
}
