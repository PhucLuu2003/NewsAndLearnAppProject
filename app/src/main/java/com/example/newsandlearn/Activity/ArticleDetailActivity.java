package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class ArticleDetailActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ImageView articleImage;
    private TextView categoryBadge, levelBadge, articleTitle;
    private TextView readingTime, publishDate, articleContent;
    private NestedScrollView scrollView;
    private ProgressBar readingProgress;
    private TextView progressText;

    private FirebaseFirestore db;
    private String articleId;
    private int currentProgress = 0;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get article ID from intent
        articleId = getIntent().getStringExtra("article_id");

        // Initialize views
        initializeViews();
        setupToolbar();
        setupScrollListener();
        loadArticleData();

        // Track start time
        startTime = System.currentTimeMillis();
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        articleImage = findViewById(R.id.article_image);
        categoryBadge = findViewById(R.id.category_badge);
        levelBadge = findViewById(R.id.level_badge);
        articleTitle = findViewById(R.id.article_title);
        readingTime = findViewById(R.id.reading_time);
        publishDate = findViewById(R.id.publish_date);
        articleContent = findViewById(R.id.article_content);
        scrollView = findViewById(R.id.scroll_view);
        readingProgress = findViewById(R.id.reading_progress);
        progressText = findViewById(R.id.progress_text);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupScrollListener() {
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Calculate reading progress
            int contentHeight = v.getChildAt(0).getHeight();
            int scrollViewHeight = v.getHeight();
            int maxScroll = contentHeight - scrollViewHeight;

            if (maxScroll > 0) {
                int progress = (int) ((scrollY / (float) maxScroll) * 100);
                updateProgress(progress);
            }
        });
    }

    private void updateProgress(int progress) {
        if (progress > currentProgress) {
            currentProgress = progress;
            readingProgress.setProgress(progress);
            progressText.setText(progress + "%");

            // Update Firebase every 25%
            if (progress % 25 == 0 && progress > 0) {
                ProgressHelper.updateReadingProgress(articleId, progress);
            }

            // Mark as complete at 100%
            if (progress >= 100) {
                onArticleCompleted();
            }
        }
    }

    private void loadArticleData() {
        if (articleId == null) {
            Toast.makeText(this, "Error: Article not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load article from Firebase
        db.collection("articles").document(articleId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String title = document.getString("title");
                        String content = document.getString("content");
                        String category = document.getString("category");
                        String level = document.getString("level");
                        String imageUrl = document.getString("imageUrl");
                        Long readTime = document.getLong("readingTime");
                        String date = document.getString("publishDate");

                        // Set data
                        if (title != null) {
                            articleTitle.setText(title);
                            collapsingToolbar.setTitle(title);
                        }
                        if (content != null) articleContent.setText(content);
                        if (category != null) categoryBadge.setText(category);
                        if (level != null) levelBadge.setText(level);
                        if (readTime != null) readingTime.setText("⏱️ " + readTime + " min read");
                        if (date != null) publishDate.setText("📅 " + date);

                        // Load image if URL provided
                        // TODO: Use Glide or Picasso to load image from URL
                        // For now, using placeholder

                    } else {
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading article: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onArticleCompleted() {
        // Only trigger once
        if (currentProgress == 100) {
            Toast.makeText(this, "🎉 Article completed! +5% progress", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Track reading time
        long endTime = System.currentTimeMillis();
        int minutes = (int) ((endTime - startTime) / 60000);
        if (minutes > 0) {
            ProgressHelper.updateStudyTime(minutes);
        }

        // Save final progress
        if (currentProgress > 0) {
            ProgressHelper.updateReadingProgress(articleId, currentProgress);
        }
    }
}
