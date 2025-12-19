package com.example.newsandlearn.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ReadingAnalyticsManager;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class ReadingAnalyticsActivity extends AppCompatActivity {

    private TextView tvCurrentStreak, tvLongestStreak;
    private TextView tvTotalArticles, tvTotalTime;
    private TextView tvVocabularyLearned, tvQuizScore;
    private LinearLayout categoriesContainer, levelsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_analytics);

        initializeViews();
        setupToolbar();
        loadAnalytics();
    }

    private void initializeViews() {
        tvCurrentStreak = findViewById(R.id.tv_current_streak);
        tvLongestStreak = findViewById(R.id.tv_longest_streak);
        tvTotalArticles = findViewById(R.id.tv_total_articles);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvVocabularyLearned = findViewById(R.id.tv_vocabulary_learned);
        tvQuizScore = findViewById(R.id.tv_quiz_score);
        categoriesContainer = findViewById(R.id.categories_container);
        levelsContainer = findViewById(R.id.levels_container);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAnalytics() {
        ReadingAnalyticsManager.getInstance().getAnalytics(new ReadingAnalyticsManager.AnalyticsCallback() {
            @Override
            public void onSuccess(Map<String, Object> analytics) {
                displayAnalytics(analytics);
            }

            @Override
            public void onError(String error) {
                // Show default values
                displayAnalytics(null);
            }
        });
    }

    private void displayAnalytics(Map<String, Object> analytics) {
        if (analytics == null || analytics.isEmpty()) {
            // Show zeros
            tvCurrentStreak.setText("0");
            tvLongestStreak.setText("0");
            tvTotalArticles.setText("0");
            tvTotalTime.setText("0");
            tvVocabularyLearned.setText("0");
            tvQuizScore.setText("0%");
            return;
        }

        // Display streak
        Long currentStreak = (Long) analytics.get("currentStreak");
        Long longestStreak = (Long) analytics.get("longestStreak");
        tvCurrentStreak.setText(String.valueOf(currentStreak != null ? currentStreak : 0));
        tvLongestStreak.setText(String.valueOf(longestStreak != null ? longestStreak : 0));

        // Display totals
        Long totalArticles = (Long) analytics.get("totalArticlesRead");
        Long totalTime = (Long) analytics.get("totalReadingTimeMinutes");
        tvTotalArticles.setText(String.valueOf(totalArticles != null ? totalArticles : 0));
        tvTotalTime.setText(String.valueOf(totalTime != null ? totalTime : 0));

        // Display vocabulary and quiz
        Long vocabLearned = (Long) analytics.get("vocabularyLearned");
        Double avgQuizScore = (Double) analytics.get("averageQuizScore");
        tvVocabularyLearned.setText(String.valueOf(vocabLearned != null ? vocabLearned : 0));
        tvQuizScore.setText(String.format("%.0f%%", avgQuizScore != null ? avgQuizScore : 0.0));

        // Display categories
        Map<String, Object> categories = (Map<String, Object>) analytics.get("categoriesRead");
        if (categories != null && !categories.isEmpty()) {
            displayCategoriesChart(categories);
        }

        // Display levels
        Map<String, Object> levels = (Map<String, Object>) analytics.get("levelsRead");
        if (levels != null && !levels.isEmpty()) {
            displayLevelsChart(levels);
        }
    }

    private void displayCategoriesChart(Map<String, Object> categories) {
        categoriesContainer.removeAllViews();
        
        int total = 0;
        for (Object value : categories.values()) {
            if (value instanceof Long) {
                total += ((Long) value).intValue();
            }
        }

        final int totalCount = total;
        
        for (Map.Entry<String, Object> entry : categories.entrySet()) {
            String category = entry.getKey();
            int count = entry.getValue() instanceof Long ? ((Long) entry.getValue()).intValue() : 0;
            
            addChartItem(categoriesContainer, category, count, totalCount, getRandomColor());
        }
    }

    private void displayLevelsChart(Map<String, Object> levels) {
        levelsContainer.removeAllViews();
        
        int total = 0;
        for (Object value : levels.values()) {
            if (value instanceof Long) {
                total += ((Long) value).intValue();
            }
        }

        final int totalCount = total;
        
        String[] levelColors = {"#4CAF50", "#8BC34A", "#FFC107", "#FF9800", "#F44336"};
        int colorIndex = 0;
        
        for (Map.Entry<String, Object> entry : levels.entrySet()) {
            String level = entry.getKey();
            int count = entry.getValue() instanceof Long ? ((Long) entry.getValue()).intValue() : 0;
            String color = levelColors[colorIndex % levelColors.length];
            
            addChartItem(levelsContainer, level, count, totalCount, color);
            colorIndex++;
        }
    }

    private void addChartItem(LinearLayout container, String label, int count, int total, String color) {
        View itemView = getLayoutInflater().inflate(R.layout.item_chart_bar, container, false);
        
        TextView tvLabel = itemView.findViewById(R.id.tv_label);
        TextView tvCount = itemView.findViewById(R.id.tv_count);
        ProgressBar progressBar = itemView.findViewById(R.id.progress_bar);
        
        tvLabel.setText(label);
        tvCount.setText(String.valueOf(count));
        
        int percentage = total > 0 ? (count * 100) / total : 0;
        progressBar.setProgress(percentage);
        
        try {
            progressBar.getProgressDrawable().setColorFilter(
                Color.parseColor(color), 
                android.graphics.PorterDuff.Mode.SRC_IN
            );
        } catch (Exception e) {
            // Use default color
        }
        
        container.addView(itemView);
    }

    private String getRandomColor() {
        String[] colors = {"#2196F3", "#9C27B0", "#00BCD4", "#009688", "#FF5722"};
        return colors[(int) (Math.random() * colors.length)];
    }
}
