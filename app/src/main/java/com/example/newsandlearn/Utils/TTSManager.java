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

    public void initialize(Context context, Runnable onReady) {
        if (tts != null) {
            tts.shutdown();
        }

        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                    isInitialized = false;
                } else {
                    isInitialized = true;
                    tts.setSpeechRate(speechRate);
                    
                    // Set utterance progress listener
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if (currentCallback != null) {
                                android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper()
                                );
                                mainHandler.post(() -> currentCallback.onStart());
                            }
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (currentCallback != null) {
                                android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper()
                                );
                                mainHandler.post(() -> currentCallback.onDone());
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            if (currentCallback != null) {
                                android.os.Handler mainHandler = new android.os.Handler(
                                    android.os.Looper.getMainLooper()
                                );
                                mainHandler.post(() -> currentCallback.onError());
                            }
                        }
                    });
                    
                    if (onReady != null) {
                        onReady.run();
                    }
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
                isInitialized = false;
            }
        });
    }

    public void speak(String text, TTSCallback callback) {
        if (!isInitialized || tts == null) {
            Log.e(TAG, "TTS not initialized");
            if (callback != null) {
                callback.onError();
            }
            return;
        }

        currentCallback = callback;
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    public void speakWord(String word) {
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
