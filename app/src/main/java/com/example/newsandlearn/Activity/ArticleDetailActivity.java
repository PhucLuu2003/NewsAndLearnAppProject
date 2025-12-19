package com.example.newsandlearn.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.example.newsandlearn.Utils.VocabularyHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArticleDetailActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ImageView articleImage;
    private CircleImageView authorAvatar;
    private TextView categoryBadge, levelBadge, articleTitle;
    private TextView authorName, readingTime, publishDate, articleContent;
    private ImageView favoriteButton;
    private NestedScrollView scrollView;
    private ProgressBar readingProgress;
    private TextView progressText;
    private FloatingActionButton fabAddVocab;

    private FirebaseFirestore db;
    private String articleId;
    private String articleLevel;
    private int currentProgress = 0;
    private long startTime;
    private String selectedText = "";
    private int selectionStart = 0;
    private int selectionEnd = 0;

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
        setupTextSelection();
        loadArticleData();

        // Track start time
        startTime = System.currentTimeMillis();
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        articleImage = findViewById(R.id.article_image);
        authorAvatar = findViewById(R.id.author_avatar);
        categoryBadge = findViewById(R.id.category_badge);
        levelBadge = findViewById(R.id.level_badge);
        articleTitle = findViewById(R.id.article_title);
        authorName = findViewById(R.id.author_name);
        readingTime = findViewById(R.id.reading_time);
        publishDate = findViewById(R.id.publish_date);
        articleContent = findViewById(R.id.article_content);
        favoriteButton = findViewById(R.id.favorite_button);
        scrollView = findViewById(R.id.scroll_view);
        readingProgress = findViewById(R.id.reading_progress);
        progressText = findViewById(R.id.progress_text);
        fabAddVocab = findViewById(R.id.fab_add_vocab);

        // Setup FAB click
        fabAddVocab.setOnClickListener(v -> showAddVocabularyDialog());
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

    private void setupTextSelection() {
        articleContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Add custom menu items
                menu.add(0, 1, 0, "Highlight")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(0, 2, 0, "Add to Vocabulary")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove default menu items
                menu.removeItem(android.R.id.selectAll);
                menu.removeItem(android.R.id.cut);
                menu.removeItem(android.R.id.paste);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int start = articleContent.getSelectionStart();
                int end = articleContent.getSelectionEnd();
                
                if (start >= 0 && end > start) {
                    selectedText = articleContent.getText().toString().substring(start, end);
                    selectionStart = start;
                    selectionEnd = end;
                    
                    switch (item.getItemId()) {
                        case 1: // Highlight
                            highlightText(start, end);
                            mode.finish();
                            return true;
                        case 2: // Add to Vocabulary
                            showAddVocabularyDialog();
                            mode.finish();
                            return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Clean up if needed
            }
        });
    }

    private void highlightText(int start, int end) {
        SpannableString spannableString = new SpannableString(articleContent.getText());
        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(Color.YELLOW);
        spannableString.setSpan(highlightSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        articleContent.setText(spannableString);
        
        Toast.makeText(this, "Text highlighted!", Toast.LENGTH_SHORT).show();
        
        // TODO: Save highlight to Firebase
    }

    private void showAddVocabularyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_vocabulary);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        EditText editWord = dialog.findViewById(R.id.edit_word);
        EditText editMeaning = dialog.findViewById(R.id.edit_meaning);
        EditText editContext = dialog.findViewById(R.id.edit_context);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialog.findViewById(R.id.btn_save);

        // Pre-fill with selected text
        if (!selectedText.isEmpty()) {
            editWord.setText(selectedText);
            // Extract sentence containing the word as context
            String fullText = articleContent.getText().toString();
            String context = extractSentence(fullText, selectionStart);
            editContext.setText(context);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String word = editWord.getText().toString().trim();
            String meaning = editMeaning.getText().toString().trim();
            String context = editContext.getText().toString().trim();

            if (word.isEmpty()) {
                editWord.setError("Please enter a word");
                return;
            }

            if (meaning.isEmpty()) {
                editMeaning.setError("Please enter the meaning");
                return;
            }

            // Add to vocabulary using VocabularyHelper
            VocabularyHelper.getInstance()
                    .addVocabularyFromArticle(word, meaning, articleId, articleLevel)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, "✅ " + result, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        selectedText = "";
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private String extractSentence(String text, int position) {
        // Find sentence boundaries
        int start = text.lastIndexOf('.', position);
        if (start == -1) start = 0;
        else start++;
        
        int end = text.indexOf('.', position);
        if (end == -1) end = text.length();
        else end++;
        
        return text.substring(start, end).trim();
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
                        String source = document.getString("source");
                        String authorNameStr = document.getString("authorName");
                        String authorAvatarUrl = document.getString("authorAvatar");
                        Long readTime = document.getLong("readingTime");
                        com.google.firebase.Timestamp timestamp = document.getTimestamp("publishedDate");

                        // Store level for vocabulary
                        articleLevel = level;

                        // Set data
                        if (title != null) {
                            articleTitle.setText(title);
                            collapsingToolbar.setTitle(title);
                        }
                        if (content != null) articleContent.setText(content);
                        if (category != null) categoryBadge.setText(category);
                        if (level != null) levelBadge.setText(level);
                        if (readTime != null) readingTime.setText(readTime + " min read");
                        if (timestamp != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                            publishDate.setText(sdf.format(timestamp.toDate()));
                        }

                        // Set author info
                        if (authorNameStr != null) {
                            authorName.setText(authorNameStr);
                        } else if (source != null) {
                            authorName.setText(source);
                        }

                        // Load images with Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.video_placeholder)
                                    .into(articleImage);
                        }

                        if (authorAvatarUrl != null && !authorAvatarUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(authorAvatarUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(authorAvatar);
                        }

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
