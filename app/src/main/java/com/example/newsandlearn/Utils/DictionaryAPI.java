package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.DictionaryWord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DictionaryAPI {
    private static final String TAG = "DictionaryAPI";
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static DictionaryAPI instance;
    private final ExecutorService executorService;
    private final Gson gson;

    private DictionaryAPI() {
        executorService = Executors.newSingleThreadExecutor();
        gson = new Gson();
    }

    public static synchronized DictionaryAPI getInstance() {
        if (instance == null) {
            instance = new DictionaryAPI();
        }
        return instance;
    }

    public interface DictionaryCallback {
        void onSuccess(DictionaryWord word);
        void onError(String error);
    }

    public void lookupWord(String word, DictionaryCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(API_URL + word.toLowerCase().trim());
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
                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        DictionaryWord dictionaryWord = parseDictionaryWord(jsonObject);
                        
                        // Post result on main thread
                        android.os.Handler mainHandler = new android.os.Handler(
                            android.os.Looper.getMainLooper()
                        );
                        mainHandler.post(() -> callback.onSuccess(dictionaryWord));
                    } else {
                        postError(callback, "Word not found");
                    }
                } else if (responseCode == 404) {
                    postError(callback, "Word not found in dictionary");
                } else {
                    postError(callback, "Error: " + responseCode);
                }
                
                connection.disconnect();
                
            } catch (Exception e) {
                Log.e(TAG, "Error looking up word: " + word, e);
                postError(callback, "Network error: " + e.getMessage());
            }
        });
    }

    private DictionaryWord parseDictionaryWord(JSONObject json) throws Exception {
        DictionaryWord word = new DictionaryWord();
        
        if (json.has("word")) {
            word.setWord(json.getString("word"));
        }
        
        if (json.has("phonetic")) {
            word.setPhonetic(json.getString("phonetic"));
        }
        
        // Get audio URL from phonetics array
        if (json.has("phonetics")) {
            JSONArray phonetics = json.getJSONArray("phonetics");
            for (int i = 0; i < phonetics.length(); i++) {
                JSONObject phonetic = phonetics.getJSONObject(i);
                if (phonetic.has("audio") && !phonetic.getString("audio").isEmpty()) {
                    word.setAudioUrl(phonetic.getString("audio"));
                    break;
                }
            }
        }
        
        // Parse meanings
        if (json.has("meanings")) {
            String meaningsJson = json.getJSONArray("meanings").toString();
            List<DictionaryWord.Meaning> meanings = gson.fromJson(
                meaningsJson,
                new TypeToken<List<DictionaryWord.Meaning>>(){}.getType()
            );
            word.setMeanings(meanings);
        }
        
        return word;
    }

    private void postError(DictionaryCallback callback, String error) {
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
