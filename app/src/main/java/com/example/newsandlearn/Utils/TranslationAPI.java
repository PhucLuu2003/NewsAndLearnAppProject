package com.example.newsandlearn.Utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslationAPI {
    private static final String TAG = "TranslationAPI";
    // Using MyMemory Translation API (Free, no API key required)
    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static TranslationAPI instance;
    private final ExecutorService executorService;

    private TranslationAPI() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized TranslationAPI getInstance() {
        if (instance == null) {
            instance = new TranslationAPI();
        }
        return instance;
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(String error);
    }

    public void translate(String text, String fromLang, String toLang, TranslationCallback callback) {
        executorService.execute(() -> {
            try {
                String encodedText = URLEncoder.encode(text, "UTF-8");
                String langPair = fromLang + "|" + toLang;
                String urlString = API_URL + "?q=" + encodedText + "&langpair=" + langPair;
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON response
                    JSONObject jsonObject = new JSONObject(response.toString());
                    
                    if (jsonObject.has("responseData")) {
                        JSONObject responseData = jsonObject.getJSONObject("responseData");
                        String translatedText = responseData.getString("translatedText");
                        
                        // Post result on main thread
                        android.os.Handler mainHandler = new android.os.Handler(
                            android.os.Looper.getMainLooper()
                        );
                        mainHandler.post(() -> callback.onSuccess(translatedText));
                    } else {
                        postError(callback, "Translation failed");
                    }
                } else {
                    postError(callback, "Error: " + responseCode);
                }
                
                connection.disconnect();
                
            } catch (Exception e) {
                Log.e(TAG, "Error translating text", e);
                postError(callback, "Network error: " + e.getMessage());
            }
        });
    }

    public void translateToVietnamese(String text, TranslationCallback callback) {
        translate(text, "en", "vi", callback);
    }

    public void translateToEnglish(String text, TranslationCallback callback) {
        translate(text, "vi", "en", callback);
    }

    private void postError(TranslationCallback callback, String error) {
        android.os.Handler mainHandler = new android.os.Handler(
            android.os.Looper.getMainLooper()
        );
        mainHandler.post(() -> callback.onError(error));
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
