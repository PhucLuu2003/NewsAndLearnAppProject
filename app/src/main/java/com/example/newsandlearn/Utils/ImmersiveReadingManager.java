package com.example.newsandlearn.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

/**
 * ImmersiveReadingManager - Advanced reading modes
 * Includes speed reading, focus mode, and bionic reading
 */
public class ImmersiveReadingManager {
    private static ImmersiveReadingManager instance;
    
    private Context context;
    private SharedPreferences prefs;
    private Handler handler;
    
    // Reading modes
    public enum ReadingMode {
        NORMAL,
        SPEED_READING,    // RSVP - one word at a time
        BIONIC_READING,   // Highlight first half of words
        FOCUS_MODE        // Highlight current paragraph
    }
    
    // Reading themes
    public enum ReadingTheme {
        LIGHT(Color.WHITE, Color.parseColor("#212121"), Color.parseColor("#F5F5F5")),
        DARK(Color.parseColor("#121212"), Color.parseColor("#E0E0E0"), Color.parseColor("#1E1E1E")),
        SEPIA(Color.parseColor("#F4ECD8"), Color.parseColor("#5B4636"), Color.parseColor("#E8D9C3")),
        FOREST(Color.parseColor("#1B3A2F"), Color.parseColor("#C8E6C9"), Color.parseColor("#2E5F4E")),
        OCEAN(Color.parseColor("#0D1B2A"), Color.parseColor("#A8DADC"), Color.parseColor("#1B263B"));
        
        public final int backgroundColor;
        public final int textColor;
        public final int cardColor;
        
        ReadingTheme(int bg, int text, int card) {
            this.backgroundColor = bg;
            this.textColor = text;
            this.cardColor = card;
        }
    }
    
    private ReadingMode currentMode = ReadingMode.NORMAL;
    private ReadingTheme currentTheme = ReadingTheme.LIGHT;
    private int fontSize = 16; // sp
    private float lineSpacing = 1.5f;
    private boolean isSpeedReadingActive = false;
    private int speedReadingWPM = 250;

    private ImmersiveReadingManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("reading_preferences", Context.MODE_PRIVATE);
        this.handler = new Handler(Looper.getMainLooper());
        loadPreferences();
    }

    public static synchronized ImmersiveReadingManager getInstance(Context context) {
        if (instance == null) {
            instance = new ImmersiveReadingManager(context);
        }
        return instance;
    }

    /**
     * Load saved preferences
     */
    private void loadPreferences() {
        String themeName = prefs.getString("theme", "LIGHT");
        try {
            currentTheme = ReadingTheme.valueOf(themeName);
        } catch (Exception e) {
            currentTheme = ReadingTheme.LIGHT;
        }
        
        fontSize = prefs.getInt("font_size", 16);
        lineSpacing = prefs.getFloat("line_spacing", 1.5f);
        speedReadingWPM = prefs.getInt("speed_reading_wpm", 250);
    }

    /**
     * Save preferences
     */
    private void savePreferences() {
        prefs.edit()
            .putString("theme", currentTheme.name())
            .putInt("font_size", fontSize)
            .putFloat("line_spacing", lineSpacing)
            .putInt("speed_reading_wpm", speedReadingWPM)
            .apply();
    }

    /**
     * Apply theme to TextView
     */
    public void applyTheme(TextView textView) {
        textView.setBackgroundColor(currentTheme.backgroundColor);
        textView.setTextColor(currentTheme.textColor);
    }

    /**
     * Apply font settings to TextView
     */
    public void applyFontSettings(TextView textView) {
        textView.setTextSize(fontSize);
        textView.setLineSpacing(0, lineSpacing);
    }

    /**
     * Apply all settings to TextView
     */
    public void applyAllSettings(TextView textView) {
        applyTheme(textView);
        applyFontSettings(textView);
    }

    /**
     * Enable bionic reading (highlight first half of each word)
     */
    public SpannableString applyBionicReading(String text) {
        SpannableString spannable = new SpannableString(text);
        String[] words = text.split("\\s+");
        int currentIndex = 0;
        
        for (String word : words) {
            int wordStart = text.indexOf(word, currentIndex);
            if (wordStart == -1) continue;
            
            int highlightEnd = wordStart + (word.length() / 2);
            
            // Make first half bold
            spannable.setSpan(
                new StyleSpan(android.graphics.Typeface.BOLD),
                wordStart,
                highlightEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            
            currentIndex = wordStart + word.length();
        }
        
        return spannable;
    }

    /**
     * Enable focus mode (highlight current paragraph)
     */
    public SpannableString applyFocusMode(String text, int focusParagraphIndex) {
        SpannableString spannable = new SpannableString(text);
        String[] paragraphs = text.split("\n\n");
        
        int currentIndex = 0;
        for (int i = 0; i < paragraphs.length; i++) {
            int paraStart = text.indexOf(paragraphs[i], currentIndex);
            if (paraStart == -1) continue;
            
            int paraEnd = paraStart + paragraphs[i].length();
            
            if (i == focusParagraphIndex) {
                // Highlight focused paragraph
                spannable.setSpan(
                    new BackgroundColorSpan(Color.parseColor("#FFEB3B33")),
                    paraStart,
                    paraEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else {
                // Dim other paragraphs
                spannable.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#80808080")),
                    paraStart,
                    paraEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            currentIndex = paraEnd;
        }
        
        return spannable;
    }

    /**
     * Start speed reading (RSVP - Rapid Serial Visual Presentation)
     */
    public void startSpeedReading(String text, TextView displayView, SpeedReadingCallback callback) {
        if (isSpeedReadingActive) {
            stopSpeedReading();
        }
        
        isSpeedReadingActive = true;
        String[] words = text.split("\\s+");
        int delayMs = 60000 / speedReadingWPM; // Convert WPM to ms per word
        
        new Thread(() -> {
            for (int i = 0; i < words.length && isSpeedReadingActive; i++) {
                final String word = words[i];
                final int index = i;
                final int total = words.length;
                
                handler.post(() -> {
                    if (callback != null) {
                        callback.onWordDisplayed(word, index, total);
                    }
                    
                    // Display word with focus on optimal recognition point (ORP)
                    SpannableString spannable = new SpannableString(word);
                    int orpIndex = calculateORP(word);
                    
                    if (orpIndex > 0 && orpIndex < word.length()) {
                        spannable.setSpan(
                            new ForegroundColorSpan(Color.RED),
                            orpIndex,
                            orpIndex + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                    }
                    
                    displayView.setText(spannable);
                });
                
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            handler.post(() -> {
                isSpeedReadingActive = false;
                if (callback != null) {
                    callback.onComplete();
                }
            });
        }).start();
    }

    /**
     * Stop speed reading
     */
    public void stopSpeedReading() {
        isSpeedReadingActive = false;
    }

    /**
     * Calculate Optimal Recognition Point for a word
     */
    private int calculateORP(String word) {
        int length = word.length();
        if (length <= 1) return 0;
        if (length <= 5) return 1;
        if (length <= 9) return 2;
        if (length <= 13) return 3;
        return 4;
    }

    /**
     * Enable blue light filter
     */
    public void applyBlueLightFilter(TextView textView, int intensity) {
        // intensity: 0-100
        int alpha = (int) (intensity * 2.55f);
        int orangeOverlay = Color.argb(alpha, 255, 200, 100);
        textView.setBackgroundColor(blendColors(currentTheme.backgroundColor, orangeOverlay));
    }

    /**
     * Blend two colors
     */
    private int blendColors(int color1, int color2) {
        float ratio = 0.5f;
        int a = (int) ((Color.alpha(color1) * (1 - ratio)) + (Color.alpha(color2) * ratio));
        int r = (int) ((Color.red(color1) * (1 - ratio)) + (Color.red(color2) * ratio));
        int g = (int) ((Color.green(color1) * (1 - ratio)) + (Color.green(color2) * ratio));
        int b = (int) ((Color.blue(color1) * (1 - ratio)) + (Color.blue(color2) * ratio));
        return Color.argb(a, r, g, b);
    }

    // Getters and setters
    public ReadingMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(ReadingMode mode) {
        this.currentMode = mode;
    }

    public ReadingTheme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(ReadingTheme theme) {
        this.currentTheme = theme;
        savePreferences();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = Math.max(12, Math.min(32, fontSize));
        savePreferences();
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = Math.max(1.0f, Math.min(3.0f, lineSpacing));
        savePreferences();
    }

    public int getSpeedReadingWPM() {
        return speedReadingWPM;
    }

    public void setSpeedReadingWPM(int wpm) {
        this.speedReadingWPM = Math.max(100, Math.min(1000, wpm));
        savePreferences();
    }

    public boolean isSpeedReadingActive() {
        return isSpeedReadingActive;
    }

    // Callback interface
    public interface SpeedReadingCallback {
        void onWordDisplayed(String word, int index, int total);
        void onComplete();
    }
}
