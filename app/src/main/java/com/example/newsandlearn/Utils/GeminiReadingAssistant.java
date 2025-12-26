package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * GeminiReadingAssistant - AI-powered reading companion using Google Gemini
 * Provides intelligent assistance for reading comprehension
 */
public class GeminiReadingAssistant {
    private static final String TAG = "GeminiAssistant";

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private static GeminiReadingAssistant instance;
    private GenerativeModelFutures model;
    private Executor executor;

    private String currentArticleContent = "";
    private String currentArticleTitle = "";

    private GeminiReadingAssistant() {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "Missing GEMINI_API_KEY. Set it in local.properties (GEMINI_API_KEY=...) or env var GEMINI_API_KEY.");
        }
        // Initialize Gemini model
        GenerativeModel gm = new GenerativeModel("gemini-pro", API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized GeminiReadingAssistant getInstance() {
        if (instance == null) {
            instance = new GeminiReadingAssistant();
        }
        return instance;
    }

    // Callback interfaces
    public interface AICallback {
        void onSuccess(String response);

        void onFailure(Exception e);
    }

    public interface QuestionCallback {
        void onSuccess(java.util.List<Question> questions);

        void onFailure(Exception e);
    }

    public static class Question {
        public String question;
        public String[] options;
        public int correctAnswer;
        public String explanation;

        public Question(String question, String[] options, int correctAnswer, String explanation) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }
    }

    /**
     * Set the current article context
     */
    public void setArticleContext(String title, String content) {
        this.currentArticleTitle = title;
        this.currentArticleContent = content;
    }

    /**
     * Ask AI a question about the article
     */
    public void askAboutArticle(String userQuestion, AICallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = buildChatPrompt(userQuestion);
        generateResponse(prompt, callback);
    }

    /**
     * Explain a specific word or phrase from the article
     */
    public void explainText(String selectedText, AICallback callback) {
        String prompt = String.format(
                "In the context of this article titled '%s', explain the meaning of: \"%s\"\n\n" +
                        "Article excerpt: %s\n\n" +
                        "Provide a clear, concise explanation in Vietnamese suitable for English learners.",
                currentArticleTitle,
                selectedText,
                getRelevantExcerpt(selectedText));

        generateResponse(prompt, callback);
    }

    /**
     * Generate a summary of the article
     */
    public void generateSummary(AICallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = String.format(
                "Summarize this English article in Vietnamese. Make it concise but comprehensive:\n\n" +
                        "Title: %s\n\n" +
                        "Content: %s\n\n" +
                        "Provide a 3-4 sentence summary in Vietnamese.",
                currentArticleTitle,
                currentArticleContent);

        generateResponse(prompt, callback);
    }

    /**
     * Generate comprehension questions
     */
    public void generateQuestions(int numQuestions, QuestionCallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = String.format(
                "Generate %d multiple-choice comprehension questions for this article:\n\n" +
                        "Title: %s\n\n" +
                        "Content: %s\n\n" +
                        "For each question, provide:\n" +
                        "1. The question\n" +
                        "2. Four options (A, B, C, D)\n" +
                        "3. The correct answer (A, B, C, or D)\n" +
                        "4. A brief explanation\n\n" +
                        "Format each question as:\n" +
                        "Q: [question]\n" +
                        "A) [option A]\n" +
                        "B) [option B]\n" +
                        "C) [option C]\n" +
                        "D) [option D]\n" +
                        "Answer: [correct letter]\n" +
                        "Explanation: [explanation]\n" +
                        "---",
                numQuestions,
                currentArticleTitle,
                currentArticleContent);

        generateResponse(prompt, new AICallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    java.util.List<Question> questions = parseQuestions(response);
                    callback.onSuccess(questions);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Analyze article difficulty
     */
    public void analyzeDifficulty(AICallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = String.format(
                "Analyze the difficulty level of this English article:\n\n" +
                        "Title: %s\n\n" +
                        "Content: %s\n\n" +
                        "Provide:\n" +
                        "1. CEFR level (A1, A2, B1, B2, C1, C2)\n" +
                        "2. Estimated reading time for intermediate learner\n" +
                        "3. Key vocabulary complexity\n" +
                        "4. Grammar complexity\n" +
                        "5. Brief recommendation for learners\n\n" +
                        "Respond in Vietnamese.",
                currentArticleTitle,
                currentArticleContent);

        generateResponse(prompt, callback);
    }

    /**
     * Get vocabulary from article
     */
    public void extractVocabulary(String level, AICallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = String.format(
                "Extract 10 important vocabulary words from this article that are suitable for %s level learners:\n\n" +
                        "Content: %s\n\n" +
                        "For each word, provide:\n" +
                        "- The word\n" +
                        "- Vietnamese translation\n" +
                        "- Example sentence from the article\n\n" +
                        "Format as a numbered list.",
                level,
                currentArticleContent);

        generateResponse(prompt, callback);
    }

    /**
     * Suggest similar articles (based on content)
     */
    public void suggestSimilarTopics(AICallback callback) {
        if (currentArticleContent.isEmpty()) {
            callback.onFailure(new Exception("No article context set"));
            return;
        }

        String prompt = String.format(
                "Based on this article titled '%s', suggest 5 related topics or article ideas " +
                        "that would help learners expand their knowledge in this area.\n\n" +
                        "Brief article summary: %s\n\n" +
                        "Provide topic suggestions in Vietnamese with brief explanations.",
                currentArticleTitle,
                currentArticleContent.substring(0, Math.min(500, currentArticleContent.length())));

        generateResponse(prompt, callback);
    }

    /**
     * Build chat prompt with context
     */
    private String buildChatPrompt(String userQuestion) {
        return String.format(
                "You are an English learning assistant. A student is reading this article:\n\n" +
                        "Title: %s\n\n" +
                        "Content: %s\n\n" +
                        "Student's question: %s\n\n" +
                        "Provide a helpful, clear answer in Vietnamese. Be encouraging and educational.",
                currentArticleTitle,
                currentArticleContent,
                userQuestion);
    }

    /**
     * Get relevant excerpt containing the selected text
     */
    private String getRelevantExcerpt(String selectedText) {
        int index = currentArticleContent.indexOf(selectedText);
        if (index == -1) {
            return currentArticleContent.substring(0, Math.min(200, currentArticleContent.length()));
        }

        int start = Math.max(0, index - 100);
        int end = Math.min(currentArticleContent.length(), index + selectedText.length() + 100);

        return "..." + currentArticleContent.substring(start, end) + "...";
    }

    /**
     * Generate response from Gemini
     */
    private void generateResponse(String prompt, AICallback callback) {
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result.getText();
                callback.onSuccess(text);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error generating response", t);
                callback.onFailure(new Exception(t));
            }
        }, executor);
    }

    /**
     * Parse questions from AI response
     */
    private java.util.List<Question> parseQuestions(String response) {
        java.util.List<Question> questions = new java.util.ArrayList<>();

        // Split by question separator
        String[] questionBlocks = response.split("---");

        for (String block : questionBlocks) {
            block = block.trim();
            if (block.isEmpty())
                continue;

            try {
                // Parse question
                String questionText = extractBetween(block, "Q:", "\nA)");

                // Parse options
                String[] options = new String[4];
                options[0] = extractBetween(block, "A)", "\nB)").trim();
                options[1] = extractBetween(block, "B)", "\nC)").trim();
                options[2] = extractBetween(block, "C)", "\nD)").trim();
                options[3] = extractBetween(block, "D)", "\nAnswer:").trim();

                // Parse answer
                String answerLine = extractBetween(block, "Answer:", "\nExplanation:");
                int correctAnswer = parseAnswerLetter(answerLine.trim());

                // Parse explanation
                String explanation = extractAfter(block, "Explanation:");

                questions.add(new Question(questionText.trim(), options, correctAnswer, explanation.trim()));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing question block", e);
            }
        }

        return questions;
    }

    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex);
        if (startIndex == -1 || endIndex == -1)
            return "";
        return text.substring(startIndex + start.length(), endIndex);
    }

    private String extractAfter(String text, String marker) {
        int index = text.indexOf(marker);
        if (index == -1)
            return "";
        return text.substring(index + marker.length());
    }

    private int parseAnswerLetter(String answer) {
        answer = answer.toUpperCase().trim();
        if (answer.startsWith("A"))
            return 0;
        if (answer.startsWith("B"))
            return 1;
        if (answer.startsWith("C"))
            return 2;
        if (answer.startsWith("D"))
            return 3;
        return 0; // Default to A
    }
}
