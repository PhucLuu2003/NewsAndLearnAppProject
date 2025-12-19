package com.example.newsandlearn.Utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.widget.TextView;

/**
 * Real-time Highlighting Manager
 * Highlights text with smooth animations while AI is analyzing
 */
public class RealtimeHighlightManager {
    private static RealtimeHighlightManager instance;
    
    private ValueAnimator currentAnimator;
    private int highlightColor = Color.parseColor("#FFEB3B"); // Yellow
    private int analysingColor = Color.parseColor("#4CAF50"); // Green
    private int completedColor = Color.parseColor("#2196F3"); // Blue
    
    private RealtimeHighlightManager() {}
    
    public static synchronized RealtimeHighlightManager getInstance() {
        if (instance == null) {
            instance = new RealtimeHighlightManager();
        }
        return instance;
    }
    
    /**
     * Highlight text with pulsing animation while analyzing
     */
    public void highlightWithAnimation(TextView textView, String fullText, 
                                      int startIndex, int endIndex, 
                                      HighlightCallback callback) {
        
        // Stop any existing animation
        stopAnimation();
        
        SpannableString spannable = new SpannableString(fullText);
        
        // Create pulsing animation
        currentAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), 
            Color.parseColor("#80FFEB3B"), // Semi-transparent yellow
            Color.parseColor("#FFFFEB3B"), // Full yellow
            Color.parseColor("#80FFEB3B")  // Back to semi-transparent
        );
        
        currentAnimator.setDuration(1500);
        currentAnimator.setRepeatCount(ValueAnimator.INFINITE);
        currentAnimator.setRepeatMode(ValueAnimator.REVERSE);
        
        currentAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            
            // Remove old spans
            BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), 
                BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                spannable.removeSpan(span);
            }
            
            // Add new span with animated color
            spannable.setSpan(
                new BackgroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            
            textView.setText(spannable);
        });
        
        currentAnimator.start();
        
        if (callback != null) {
            callback.onHighlightStarted();
        }
    }
    
    /**
     * Show completion highlight (no animation)
     */
    public void showCompletionHighlight(TextView textView, String fullText,
                                       int startIndex, int endIndex) {
        stopAnimation();
        
        SpannableString spannable = new SpannableString(fullText);
        
        // Fade from analyzing color to completed color
        ValueAnimator fadeAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
            analysingColor, completedColor);
        
        fadeAnimator.setDuration(500);
        fadeAnimator.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            
            // Remove old spans
            BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(),
                BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                spannable.removeSpan(span);
            }
            
            // Add new span
            spannable.setSpan(
                new BackgroundColorSpan(Color.argb(100, 
                    Color.red(color), 
                    Color.green(color), 
                    Color.blue(color))),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            
            textView.setText(spannable);
        });
        
        fadeAnimator.start();
    }
    
    /**
     * Clear all highlights
     */
    public void clearHighlights(TextView textView, String originalText) {
        stopAnimation();
        textView.setText(originalText);
    }
    
    /**
     * Stop current animation
     */
    public void stopAnimation() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
        }
    }
    
    /**
     * Highlight multiple sentences with cascade effect
     */
    public void cascadeHighlight(TextView textView, String fullText,
                                 String[] sentences, int currentIndex,
                                 CascadeCallback callback) {
        
        if (currentIndex >= sentences.length) {
            if (callback != null) {
                callback.onAllComplete();
            }
            return;
        }
        
        // Find sentence position in full text
        String sentence = sentences[currentIndex];
        int startIndex = fullText.indexOf(sentence);
        
        if (startIndex == -1) {
            // Sentence not found, skip
            cascadeHighlight(textView, fullText, sentences, currentIndex + 1, callback);
            return;
        }
        
        int endIndex = startIndex + sentence.length();
        
        // Highlight current sentence
        highlightWithAnimation(textView, fullText, startIndex, endIndex, 
            new HighlightCallback() {
                @Override
                public void onHighlightStarted() {
                    if (callback != null) {
                        callback.onSentenceHighlighted(currentIndex, sentence);
                    }
                }
            });
        
        // After 2 seconds, move to next sentence
        textView.postDelayed(() -> {
            showCompletionHighlight(textView, fullText, startIndex, endIndex);
            
            textView.postDelayed(() -> {
                cascadeHighlight(textView, fullText, sentences, currentIndex + 1, callback);
            }, 500);
        }, 2000);
    }
    
    /**
     * Highlight word by word (for reading assistance)
     */
    public void highlightWordByWord(TextView textView, String fullText,
                                    int wordsPerMinute, WordCallback callback) {
        
        String[] words = fullText.split("\\s+");
        int delayPerWord = 60000 / wordsPerMinute; // milliseconds
        
        highlightWordsSequentially(textView, fullText, words, 0, delayPerWord, callback);
    }
    
    private void highlightWordsSequentially(TextView textView, String fullText,
                                           String[] words, int currentIndex,
                                           int delay, WordCallback callback) {
        
        if (currentIndex >= words.length) {
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }
        
        String word = words[currentIndex];
        int startIndex = fullText.indexOf(word);
        
        if (startIndex != -1) {
            SpannableString spannable = new SpannableString(fullText);
            
            // Clear previous highlights
            BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(),
                BackgroundColorSpan.class);
            for (BackgroundColorSpan span : spans) {
                spannable.removeSpan(span);
            }
            
            // Highlight current word
            spannable.setSpan(
                new BackgroundColorSpan(Color.parseColor("#80FFEB3B")),
                startIndex,
                startIndex + word.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            
            textView.setText(spannable);
            
            if (callback != null) {
                callback.onWordHighlighted(currentIndex, word);
            }
        }
        
        // Move to next word
        textView.postDelayed(() -> {
            highlightWordsSequentially(textView, fullText, words, 
                currentIndex + 1, delay, callback);
        }, delay);
    }
    
    // ==================== CALLBACKS ====================
    
    public interface HighlightCallback {
        void onHighlightStarted();
    }
    
    public interface CascadeCallback {
        void onSentenceHighlighted(int index, String sentence);
        void onAllComplete();
    }
    
    public interface WordCallback {
        void onWordHighlighted(int index, String word);
        void onComplete();
    }
}
