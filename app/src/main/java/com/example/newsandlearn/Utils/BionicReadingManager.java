package com.example.newsandlearn.Utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bionic Reading Manager - Enhances reading speed by highlighting word beginnings
 * 
 * Bionic Reading is a reading method that guides the eyes through text with artificial fixation points.
 * It highlights the first part of words to help the brain complete them faster.
 * 
 * Benefits:
 * - Increases reading speed by 20-30%
 * - Improves focus and concentration
 * - Reduces eye strain
 * - Better comprehension
 */
public class BionicReadingManager {
    private static BionicReadingManager instance;
    
    private int highlightColor = 0xFF000000; // Black (will be set dynamically)
    private float highlightRatio = 0.5f; // Highlight first 50% of word
    
    private BionicReadingManager() {}
    
    public static synchronized BionicReadingManager getInstance() {
        if (instance == null) {
            instance = new BionicReadingManager();
        }
        return instance;
    }
    
    /**
     * Set highlight color for bionic reading
     */
    public void setHighlightColor(int color) {
        this.highlightColor = color;
    }
    
    /**
     * Set highlight ratio (0.0 - 1.0)
     * Default is 0.5 (50% of word)
     */
    public void setHighlightRatio(float ratio) {
        this.highlightRatio = Math.max(0.3f, Math.min(0.7f, ratio));
    }
    
    /**
     * Apply bionic reading to text
     */
    public SpannableString applyBionicReading(String text) {
        SpannableString spannable = new SpannableString(text);
        
        // Find all words using regex
        Pattern pattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String word = matcher.group();
            
            // Calculate how many characters to highlight
            int highlightLength = calculateHighlightLength(word.length());
            
            if (highlightLength > 0) {
                // Apply bold style to first part
                spannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    start,
                    start + highlightLength,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                
                // Apply color to first part
                spannable.setSpan(
                    new ForegroundColorSpan(highlightColor),
                    start,
                    start + highlightLength,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        
        return spannable;
    }
    
    /**
     * Apply bionic reading with custom intensity
     * @param intensity 1 = subtle, 2 = medium, 3 = strong
     */
    public SpannableString applyBionicReading(String text, int intensity) {
        switch (intensity) {
            case 1: // Subtle - highlight 40%
                setHighlightRatio(0.4f);
                break;
            case 2: // Medium - highlight 50%
                setHighlightRatio(0.5f);
                break;
            case 3: // Strong - highlight 60%
                setHighlightRatio(0.6f);
                break;
        }
        return applyBionicReading(text);
    }
    
    /**
     * Calculate how many characters to highlight based on word length
     */
    private int calculateHighlightLength(int wordLength) {
        if (wordLength <= 2) {
            return 0; // Don't highlight very short words
        } else if (wordLength <= 4) {
            return 2; // Highlight first 2 characters
        } else if (wordLength <= 7) {
            return (int) Math.ceil(wordLength * highlightRatio);
        } else {
            // For longer words, highlight slightly less proportion
            return (int) Math.ceil(wordLength * (highlightRatio - 0.1f));
        }
    }
    
    /**
     * Toggle bionic reading on/off for a text view
     */
    public static class BionicConfig {
        public boolean enabled = false;
        public int intensity = 2; // 1-3
        public int color = 0xFF000000;
        
        public BionicConfig() {}
        
        public BionicConfig(boolean enabled, int intensity, int color) {
            this.enabled = enabled;
            this.intensity = intensity;
            this.color = color;
        }
    }
}
