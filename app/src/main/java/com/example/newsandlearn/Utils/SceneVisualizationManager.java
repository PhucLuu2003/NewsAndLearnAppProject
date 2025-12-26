package com.example.newsandlearn.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.newsandlearn.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * SceneVisualizationManager - Tạo hình ảnh 3D/minh họa cho các mô tả trong bài
 * viết
 * Sử dụng AI để phân tích văn bản và tạo prompt cho image generation
 */
public class SceneVisualizationManager {

    private static SceneVisualizationManager instance;
    private GenerativeModelFutures model;
    private Executor executor;

    private SceneVisualizationManager() {
        if (BuildConfig.GEMINI_API_KEY == null || BuildConfig.GEMINI_API_KEY.isEmpty()) {
            throw new IllegalStateException(
                    "Missing GEMINI_API_KEY. Set it in local.properties (GEMINI_API_KEY=...) or env var GEMINI_API_KEY.");
        }
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.GEMINI_API_KEY);
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized SceneVisualizationManager getInstance() {
        if (instance == null) {
            instance = new SceneVisualizationManager();
        }
        return instance;
    }

    /**
     * Phân tích đoạn văn và tạo scene visualization
     */
    public void visualizeScene(Context context, String textPassage, ImageView targetImageView,
            VisualizationCallback callback) {

        // Bước 1: Phân tích văn bản để tạo image prompt
        String analysisPrompt = "Analyze this text passage and create a detailed image generation prompt:\n\n" +
                "Text: " + textPassage + "\n\n" +
                "Create a JSON response:\n" +
                "{\n" +
                "  \"scene_description\": \"Detailed visual description of the scene\",\n" +
                "  \"key_elements\": [\"element1\", \"element2\", \"element3\"],\n" +
                "  \"mood\": \"The emotional atmosphere\",\n" +
                "  \"style\": \"Art style (realistic, cartoon, 3D, etc.)\",\n" +
                "  \"image_prompt\": \"Complete prompt for image generation\"\n" +
                "}\n\n" +
                "Focus on visual elements that help understand the text. Return ONLY valid JSON.";

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

                    SceneVisualization visualization = new SceneVisualization();
                    visualization.sceneDescription = json.getString("scene_description");
                    visualization.mood = json.getString("mood");
                    visualization.style = json.getString("style");
                    visualization.imagePrompt = json.getString("image_prompt");

                    // Bước 2: Sử dụng prompt để tạo ảnh (giả lập - trong thực tế dùng API như
                    // DALL-E, Stable Diffusion)
                    // Ở đây chúng ta sẽ tạo placeholder với thông tin
                    generatePlaceholderVisualization(context, visualization, targetImageView, callback);

                } catch (Exception e) {
                    callback.onError("Failed to analyze scene: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    /**
     * Tạo visualization placeholder (trong production sẽ gọi API tạo ảnh thật)
     */
    private void generatePlaceholderVisualization(Context context, SceneVisualization visualization,
            ImageView targetImageView, VisualizationCallback callback) {

        // Trong thực tế, đây là nơi gọi API như:
        // - OpenAI DALL-E
        // - Stability AI
        // - Midjourney API
        // - Pollinations.ai (free alternative)

        // Tạm thời sử dụng Pollinations.ai (free API)
        String imageUrl = generatePollinationsUrl(visualization.imagePrompt);

        // Load image từ URL
        executor.execute(() -> {
            try {
                Bitmap bitmap = loadImageFromUrl(imageUrl);

                if (bitmap != null) {
                    // Update UI on main thread
                    if (targetImageView != null) {
                        targetImageView.post(() -> {
                            targetImageView.setImageBitmap(bitmap);
                            callback.onSuccess(visualization);
                        });
                    } else {
                        callback.onSuccess(visualization);
                    }
                } else {
                    callback.onError("Failed to load generated image");
                }

            } catch (Exception e) {
                callback.onError("Image generation error: " + e.getMessage());
            }
        });
    }

    /**
     * Tạo URL cho Pollinations.ai (free AI image generation)
     */
    private String generatePollinationsUrl(String prompt) {
        // Pollinations.ai API - free và không cần API key
        String encodedPrompt = prompt.replace(" ", "%20");
        return "https://image.pollinations.ai/prompt/" + encodedPrompt +
                "?width=800&height=600&nologo=true&enhance=true";
    }

    /**
     * Load image từ URL
     */
    private Bitmap loadImageFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo quick visualization cho từ khóa
     */
    public void visualizeKeyword(Context context, String keyword, ImageView targetImageView,
            VisualizationCallback callback) {
        String simplePrompt = "A clear, educational illustration of: " + keyword +
                ", simple style, bright colors, white background";

        SceneVisualization viz = new SceneVisualization();
        viz.imagePrompt = simplePrompt;
        viz.sceneDescription = keyword;
        viz.style = "Educational illustration";
        viz.mood = "Clear and informative";

        generatePlaceholderVisualization(context, viz, targetImageView, callback);
    }

    /**
     * Phân tích toàn bộ bài viết và tìm các đoạn có thể visualize
     */
    public void findVisualizableScenes(String articleContent, SceneAnalysisCallback callback) {
        String prompt = "Analyze this article and identify 3-5 passages that would benefit most from visual illustration:\n\n"
                +
                "Article: " + articleContent + "\n\n" +
                "Return JSON:\n" +
                "{\n" +
                "  \"scenes\": [\n" +
                "    {\n" +
                "      \"passage\": \"The exact text passage\",\n" +
                "      \"reason\": \"Why this needs visualization\",\n" +
                "      \"priority\": 1-10\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Focus on descriptive passages, complex concepts, or important scenes. Return ONLY valid JSON.";

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

                    callback.onSuccess(jsonText);

                } catch (Exception e) {
                    callback.onError("Analysis error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("AI Error: " + t.getMessage());
            }
        }, executor);
    }

    // Data classes
    public static class SceneVisualization {
        public String sceneDescription;
        public String mood;
        public String style;
        public String imagePrompt;
        public Bitmap generatedImage;
    }

    // Callbacks
    public interface VisualizationCallback {
        void onSuccess(SceneVisualization visualization);

        void onError(String error);
    }

    public interface SceneAnalysisCallback {
        void onSuccess(String analysisJson);

        void onError(String error);
    }
}
