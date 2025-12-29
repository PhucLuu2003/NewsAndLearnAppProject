package com.example.newsandlearn.Utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class TTSManager {
    private static final String TAG = "TTSManager";
    private static TTSManager instance;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private float speechRate = 1.0f;
    private TTSCallback currentCallback;

    public interface TTSCallback {
        void onStart();

        void onDone();

        void onError();
    }

    private TTSManager() {
    }

    public static synchronized TTSManager getInstance() {
        if (instance == null) {
            instance = new TTSManager();
        }
        return instance;
    }

    private boolean isInitializing = false;
    private String pendingText;
    private TTSCallback pendingCallback;

    public void initialize(Context context, Runnable onReady) {
        Log.d(TAG, "🎤 initialize() called. isInitialized=" + isInitialized + ", isInitializing=" + isInitializing);

        if (isInitialized) {
            Log.d(TAG, "✅ TTS already initialized, running onReady callback");
            if (onReady != null)
                onReady.run();
            return;
        }

        if (isInitializing) {
            Log.w(TAG, "⏳ TTS initialization already in progress, skipping duplicate init");
            return;
        }
        isInitializing = true;

        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception ignored) {
            }
        }

        // Try to initialize with Google engine if available for better quality
        String googleTtsPackage = "com.google.android.tts";
        Log.d(TAG, "🚀 Creating TextToSpeech with engine: " + googleTtsPackage);

        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            isInitializing = false;
            Log.d(TAG, "📨 TTS init callback: status=" + status + " (SUCCESS=" + TextToSpeech.SUCCESS + ")");

            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                Log.d(TAG,
                        "🇺🇸 setLanguage(US) result: " + result + " (LANG_MISSING_DATA="
                                + TextToSpeech.LANG_MISSING_DATA + ", LANG_NOT_SUPPORTED="
                                + TextToSpeech.LANG_NOT_SUPPORTED + ")");

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "❌ English language not supported, trying default locale");
                    tts.setLanguage(Locale.getDefault());
                }

                isInitialized = true;
                tts.setSpeechRate(speechRate);

                Log.i(TAG, "✅ TTS initialized successfully! isInitialized=true");

                // Set utterance progress listener
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (currentCallback != null) {
                            android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper());
                            mainHandler.post(() -> currentCallback.onStart());
                        }
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (currentCallback != null) {
                            android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper());
                            mainHandler.post(() -> currentCallback.onDone());
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (currentCallback != null) {
                            android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper());
                            mainHandler.post(() -> currentCallback.onError());
                        }
                    }
                });

                if (onReady != null) {
                    onReady.run();
                }

                // Speak pending text if any
                if (pendingText != null) {
                    speak(pendingText, pendingCallback);
                    pendingText = null;
                    pendingCallback = null;
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
                isInitialized = false;
                // Try fallback to default engine
                fallbackInitialize(context, onReady);
            }
        }, googleTtsPackage);
    }

    private void fallbackInitialize(Context context, Runnable onReady) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                isInitialized = true;
                if (onReady != null)
                    onReady.run();
                if (pendingText != null) {
                    speak(pendingText, pendingCallback);
                    pendingText = null;
                    pendingCallback = null;
                }
            }
        });
    }

    public void speak(String text, TTSCallback callback) {
        Log.d(TAG, "🗣️ speak() called: text=" + text + ", isInitialized=" + isInitialized);

        if (!isInitialized) {
            Log.w(TAG, "⚠️ TTS not initialized yet, queuing text: " + text);
            pendingText = text;
            pendingCallback = callback;
            return;
        }

        if (tts == null) {
            Log.e(TAG, "❌ TTS object is null!");
            if (callback != null)
                callback.onError();
            return;
        }

        Log.d(TAG, "▶️ Calling tts.speak() with text: " + text);
        currentCallback = callback;
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId_" + System.currentTimeMillis());
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    public void speakWord(String word) {
        Log.d(TAG, "🔊 speakWord() called: word=" + word + ", isInitialized=" + isInitialized);
        speak(word, null);
    }

    public void setSpeechRate(float rate) {
        this.speechRate = rate;
        if (tts != null && isInitialized) {
            tts.setSpeechRate(rate);
        }
    }

    public float getSpeechRate() {
        return speechRate;
    }

    public void stop() {
        if (tts != null && isInitialized) {
            tts.stop();
        }
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInitialized = false;
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
