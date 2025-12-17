package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * ReadingActivity - Display full article with comprehension questions
 * All content loaded from Firebase, NO hard-coded articles
 */
public class ReadingActivity extends AppCompatActivity {

    private ImageView backButton, bookmarkButton, articleImage;
    private TextView articleTitle, authorText, readTime, articleContent;
    private RecyclerView questionsRecyclerView;
    private MaterialButton submitButton;

    private ReadingArticle article;
    private String articleId;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        articleId = getIntent().getStringExtra("article_id");
        if (articleId == null) {
            Toast.makeText(this, "Error: No article ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        loadArticleFromFirebase();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        bookmarkButton = findViewById(R.id.bookmark_button);
        articleImage = findViewById(R.id.article_image);
        articleTitle = findViewById(R.id.article_title);
        authorText = findViewById(R.id.author_text);
        readTime = findViewById(R.id.read_time);
        articleContent = findViewById(R.id.article_content);
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        submitButton = findViewById(R.id.submit_button);

        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        bookmarkButton.setOnClickListener(v -> {
            // TODO: Toggle bookmark
            Toast.makeText(this, "Bookmark feature coming soon", Toast.LENGTH_SHORT).show();
        });

        submitButton.setOnClickListener(v -> submitAnswers());
    }

    /**
     * Load article from Firebase - DYNAMIC
     */
    private void loadArticleFromFirebase() {
        db.collection("reading_articles").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    article = documentSnapshot.toObject(ReadingArticle.class);
                    if (article != null) {
                        displayArticle();
                        article.incrementReadCount();
                        saveProgress();
                        
                        // Track reading for daily tasks
                        progressManager.trackArticleRead(new ProgressManager.ProgressCallback() {
                            @Override
                            public void onSuccess(UserProgress progress) {}
                            
                            @Override
                            public void onFailure(Exception e) {}
                        });
                    } else {
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayArticle() {
        articleTitle.setText(article.getTitle());
        articleContent.setText(article.getContent());
        
        if (article.getAuthor() != null) {
            authorText.setText("By " + article.getAuthor());
        }
        
        readTime.setText(article.getEstimatedMinutes() + " min read");

        // TODO: Load image from Firebase Storage
        // Glide.with(this).load(article.getImageUrl()).into(articleImage);

        // TODO: Setup questions adapter
        // questionsAdapter = new ReadingQuestionAdapter(article.getQuestions());
        // questionsRecyclerView.setAdapter(questionsAdapter);
    }

    private void submitAnswers() {
        // TODO: Calculate score from questions
        int score = 85; // Placeholder

        article.setCompleted(true);
        article.setUserScore(score);
        saveProgress();

        // Award XP
        progressManager.addXP(30, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                Toast.makeText(ReadingActivity.this, "Great! Score: " + score + "% (+30 XP)", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onFailure(Exception e) {}
        });
    }

    /**
     * Save progress to Firebase
     */
    private void saveProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("reading_progress").document(articleId)
                .set(article);
    }
}
