package com.example.newsandlearn.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ComprehensionCheckpointManager - Tạo câu hỏi kiểm tra hiểu biết tự động cho
 * mỗi đoạn văn
 * Sử dụng Gemini AI để phân tích và tạo câu hỏi contextual
 */
public class ComprehensionCheckpointManager {

    private static ComprehensionCheckpointManager instance;
    private GenerativeModelFutures model;
    private Executor executor;
    private List<Checkpoint> checkpoints;
    private int currentCheckpointIndex = 0;

    private ComprehensionCheckpointManager() {
        // Initialize Gemini AI
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "Missing GEMINI_API_KEY. Set it in local.properties (GEMINI_API_KEY=...) or env var GEMINI_API_KEY.");
        }
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
        checkpoints = new ArrayList<>();
    }

    public static synchronized ComprehensionCheckpointManager getInstance() {
        if (instance == null) {
            instance = new ComprehensionCheckpointManager();
        }
        return instance;
    }

    /**
     * Phân tích bài viết và tạo checkpoints tự động
     */
    public void generateCheckpoints(String articleContent, CheckpointCallback callback) {
        checkpoints.clear();
        currentCheckpointIndex = 0;

        // Chia bài viết thành các đoạn
        String[] paragraphs = articleContent.split("\\n\\n+");

        if (paragraphs.length == 0) {
            callback.onError("No paragraphs found");
            return;
        }

        // Tạo checkpoint cho mỗi 2-3 đoạn
        List<String> sections = new ArrayList<>();
        StringBuilder currentSection = new StringBuilder();
        int paragraphCount = 0;

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty())
                continue;

            currentSection.append(paragraph).append("\n\n");
            paragraphCount++;

            // Mỗi 2-3 đoạn tạo 1 checkpoint
            if (paragraphCount >= 2) {
                sections.add(currentSection.toString().trim());
                currentSection = new StringBuilder();
                paragraphCount = 0;
            }
        }

        // Thêm phần còn lại
        if (currentSection.length() > 0) {
            sections.add(currentSection.toString().trim());
        }

        // Tạo câu hỏi cho mỗi section
        generateQuestionsForSections(sections, 0, callback);
    }

    private void generateQuestionsForSections(List<String> sections, int index, CheckpointCallback callback) {
        if (index >= sections.size()) {
            callback.onSuccess(checkpoints);
            return;
        }

        String section = sections.get(index);

        String prompt = "Analyze this text passage and create ONE comprehension question:\n\n" +
                "Text: " + section + "\n\n" +
                "Create a JSON response with this format:\n" +
                "{\n" +
                "  \"question\": \"The main question\",\n" +
                "  \"options\": [\"Option A\", \"Option B\", \"Option C\", \"Option D\"],\n" +
                "  \"correctAnswer\": 0,\n" +
                "  \"explanation\": \"Why this is the correct answer\"\n" +
                "}\n\n" +
                "Make the question test understanding, not just memory. Return ONLY valid JSON.";

        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String jsonText = result.getText().trim();

                    // Clean JSON response
                    if (jsonText.startsWith("```json")) {
                        jsonText = jsonText.substring(7);
                    }
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 3);
                    }
                    jsonText = jsonText.trim();

                    JSONObject json = new JSONObject(jsonText);

                    Checkpoint checkpoint = new Checkpoint();
                    checkpoint.sectionText = section;
                    checkpoint.question = json.getString("question");
                    checkpoint.explanation = json.getString("explanation");
                    checkpoint.correctAnswer = json.getInt("correctAnswer");

                    JSONArray optionsArray = json.getJSONArray("options");
                    checkpoint.options = new ArrayList<>();
                    for (int i = 0; i < optionsArray.length(); i++) {
                        checkpoint.options.add(optionsArray.getString(i));
                    }

                    checkpoints.add(checkpoint);

                    // Generate next checkpoint
                    generateQuestionsForSections(sections, index + 1, callback);

                } catch (Exception e) {
                    callback.onError("Failed to parse question: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Hiển thị checkpoint dialog khi người dùng đọc đến
     */
    public void showCheckpoint(Context context, int checkpointIndex, CheckpointResultCallback resultCallback) {
        if (checkpointIndex >= checkpoints.size()) {
            Toast.makeText(context, "🎉 All checkpoints completed!", Toast.LENGTH_SHORT).show();
            return;
        }

        Checkpoint checkpoint = checkpoints.get(checkpointIndex);

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_comprehension_checkpoint);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        TextView tvTitle = dialog.findViewById(R.id.tv_checkpoint_title);
        TextView tvQuestion = dialog.findViewById(R.id.tv_checkpoint_question);
        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group_options);
        MaterialButton btnSubmit = dialog.findViewById(R.id.btn_submit_answer);
        TextView tvExplanation = dialog.findViewById(R.id.tv_explanation);
        LinearLayout explanationContainer = dialog.findViewById(R.id.explanation_container);

        tvTitle.setText("📍 Checkpoint " + (checkpointIndex + 1) + "/" + checkpoints.size());
        tvQuestion.setText(checkpoint.question);

        // Add radio buttons for options
        radioGroup.removeAllViews();
        for (int i = 0; i < checkpoint.options.size(); i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(checkpoint.options.get(i));
            radioButton.setId(i);
            radioButton.setPadding(16, 16, 16, 16);
            radioGroup.addView(radioButton);
        }

        btnSubmit.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(context, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isCorrect = (selectedId == checkpoint.correctAnswer);

            // Show explanation
            explanationContainer.setVisibility(View.VISIBLE);
            if (isCorrect) {
                tvExplanation.setText("✅ Correct! " + checkpoint.explanation);
                tvExplanation.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvExplanation.setText("❌ Incorrect. " + checkpoint.explanation);
                tvExplanation.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            }

            // Change button to continue
            btnSubmit.setText("Continue Reading");
            btnSubmit.setOnClickListener(v2 -> {
                dialog.dismiss();
                if (resultCallback != null) {
                    resultCallback.onCheckpointCompleted(isCorrect);
                }
            });
        });

        dialog.show();
    }

    /**
     * Kiểm tra xem có nên hiển thị checkpoint không dựa trên vị trí đọc
     */
    public boolean shouldShowCheckpoint(String fullText, int currentScrollPosition, int totalHeight) {
        if (checkpoints.isEmpty())
            return false;
        if (currentCheckpointIndex >= checkpoints.size())
            return false;

        // Tính phần trăm đã đọc
        float percentageRead = (float) currentScrollPosition / totalHeight * 100;

        // Hiển thị checkpoint mỗi 25% bài viết
        int expectedCheckpoint = (int) (percentageRead / 25);

        return expectedCheckpoint > currentCheckpointIndex;
    }

    public int getCurrentCheckpointIndex() {
        return currentCheckpointIndex;
    }

    public void incrementCheckpointIndex() {
        currentCheckpointIndex++;
    }

    public void reset() {
        checkpoints.clear();
        currentCheckpointIndex = 0;
    }

    // Data classes
    public static class Checkpoint {
        public String sectionText;
        public String question;
        public List<String> options;
        public int correctAnswer;
        public String explanation;
    }

    // Callbacks
    public interface CheckpointCallback {
        void onSuccess(List<Checkpoint> checkpoints);

        void onError(String error);
    }

    public interface CheckpointResultCallback {
        void onCheckpointCompleted(boolean isCorrect);
    }
}
