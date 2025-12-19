package com.example.newsandlearn.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Voice Feedback Manager
 * Provides voice feedback for AI analysis results
 */
public class VoiceFeedbackManager {
    private static VoiceFeedbackManager instance;
    
    private TextToSpeech tts;
    private Context context;
    private boolean isInitialized = false;
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    
    // Sound effects
    private MediaPlayer successSound;
    private MediaPlayer errorSound;
    private MediaPlayer notificationSound;
    
    private VoiceFeedbackManager() {}
    
    public static synchronized VoiceFeedbackManager getInstance() {
        if (instance == null) {
            instance = new VoiceFeedbackManager();
        }
        return instance;
    }
    
    /**
     * Initialize TTS and sound effects
     */
    public void initialize(Context context, InitCallback callback) {
        this.context = context.getApplicationContext();
        
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = false;
                    if (callback != null) {
                        callback.onError("Language not supported");
                    }
                } else {
                    isInitialized = true;
                    tts.setSpeechRate(speechRate);
                    tts.setPitch(pitch);
                    
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
            } else {
                isInitialized = false;
                if (callback != null) {
                    callback.onError("TTS initialization failed");
                }
            }
        });
        
        // Initialize sound effects (optional)
        // successSound = MediaPlayer.create(context, R.raw.success);
        // errorSound = MediaPlayer.create(context, R.raw.error);
        // notificationSound = MediaPlayer.create(context, R.raw.notification);
    }
    
    /**
     * Speak analysis result with emotion
     */
    public void speakAnalysisResult(AIReadingCoach.SentenceAnalysis analysis, 
                                    SpeechCallback callback) {
        
        if (!isInitialized) {
            if (callback != null) {
                callback.onError("TTS not initialized");
            }
            return;
        }
        
        // Build feedback message
        StringBuilder message = new StringBuilder();
        
        // Difficulty feedback
        if (analysis.difficulty <= 3) {
            message.append("This sentence is easy. ");
        } else if (analysis.difficulty <= 6) {
            message.append("This sentence has medium difficulty. ");
        } else {
            message.append("This is a challenging sentence. ");
        }
        
        // Vocabulary feedback
        if (!analysis.vocabulary.isEmpty()) {
            message.append("Key words: ");
            for (int i = 0; i < Math.min(3, analysis.vocabulary.size()); i++) {
                message.append(analysis.vocabulary.get(i).word);
                if (i < Math.min(3, analysis.vocabulary.size()) - 1) {
                    message.append(", ");
                }
            }
            message.append(". ");
        }
        
        // Grammar feedback
        if (!analysis.grammar.isEmpty()) {
            message.append("Grammar point: ");
            message.append(analysis.grammar.get(0));
            message.append(". ");
        }
        
        // Learning tip
        if (analysis.tip != null && !analysis.tip.isEmpty()) {
            message.append("Tip: ");
            message.append(analysis.tip);
        }
        
        // Speak with callbacks
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "analysis_feedback");
        
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (callback != null) {
                    callback.onStart();
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (callback != null) {
                    callback.onComplete();
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (callback != null) {
                    callback.onError("Speech error");
                }
            }
        });
        
        tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, params);
    }
    
    /**
     * Speak custom message
     */
    public void speak(String message, SpeechCallback callback) {
        if (!isInitialized) {
            if (callback != null) {
                callback.onError("TTS not initialized");
            }
            return;
        }
        
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "custom_message");
        
        if (callback != null) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    callback.onStart();
                }

                @Override
                public void onDone(String utteranceId) {
                    callback.onComplete();
                }

                @Override
                public void onError(String utteranceId) {
                    callback.onError("Speech error");
                }
            });
        }
        
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, params);
    }
    
    /**
     * Speak with different emotions/tones
     */
    public void speakWithEmotion(String message, Emotion emotion, SpeechCallback callback) {
        if (!isInitialized) {
            if (callback != null) {
                callback.onError("TTS not initialized");
            }
            return;
        }
        
        // Adjust speech parameters based on emotion
        switch (emotion) {
            case EXCITED:
                tts.setSpeechRate(1.2f);
                tts.setPitch(1.2f);
                break;
            case CALM:
                tts.setSpeechRate(0.8f);
                tts.setPitch(0.9f);
                break;
            case ENCOURAGING:
                tts.setSpeechRate(1.0f);
                tts.setPitch(1.1f);
                break;
            case SERIOUS:
                tts.setSpeechRate(0.9f);
                tts.setPitch(0.8f);
                break;
            default:
                tts.setSpeechRate(1.0f);
                tts.setPitch(1.0f);
        }
        
        speak(message, callback);
        
        // Reset to default
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
    }
    
    /**
     * Play success sound
     */
    public void playSuccessSound() {
        if (successSound != null) {
            successSound.start();
        }
    }
    
    /**
     * Play error sound
     */
    public void playErrorSound() {
        if (errorSound != null) {
            errorSound.start();
        }
    }
    
    /**
     * Play notification sound
     */
    public void playNotificationSound() {
        if (notificationSound != null) {
            notificationSound.start();
        }
    }
    
    /**
     * Stop speaking
     */
    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    public void setSpeechRate(float rate) {
        this.speechRate = rate;
        if (tts != null) {
            tts.setSpeechRate(rate);
        }
    }
    
    /**
     * Set pitch (0.5 - 2.0)
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
        if (tts != null) {
            tts.setPitch(pitch);
        }
    }
    
    /**
     * Shutdown TTS
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        
        if (successSound != null) {
            successSound.release();
        }
        if (errorSound != null) {
            errorSound.release();
        }
        if (notificationSound != null) {
            notificationSound.release();
        }
    }
    
    // ==================== ENUMS & CALLBACKS ====================
    
    public enum Emotion {
        NEUTRAL,
        EXCITED,
        CALM,
        ENCOURAGING,
        SERIOUS
    }
    
    public interface InitCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface SpeechCallback {
        void onStart();
        void onComplete();
        void onError(String error);
    }
}
