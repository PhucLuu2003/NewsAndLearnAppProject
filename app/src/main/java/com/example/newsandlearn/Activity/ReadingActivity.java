package com.example.newsandlearn.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Adapter.ReadingQuestionAdapter;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.ReadingQuestion;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReadingActivity extends AppCompatActivity {

    private static final String TAG = "ReadingActivity";

    private ImageView backButton, bookmarkButton, articleImage;
    private TextView articleTitle, authorText, readTime, articleContent, questionsSubtitle;
    private RecyclerView questionsRecyclerView;
    private MaterialButton submitButton;
    private ProgressBar loadingIndicator;
    private ScrollView contentLayout;

    private ReadingArticle article;
    private String articleId;
    private ReadingQuestionAdapter questionsAdapter;
    private boolean isBookmarked = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        articleId = getIntent().getStringExtra("article_id");
        if (articleId == null || articleId.isEmpty()) {
            Toast.makeText(this, "Error: No article ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        showLoading(true);
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
        questionsSubtitle = findViewById(R.id.questions_subtitle);
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        submitButton = findViewById(R.id.submit_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        contentLayout = findViewById(R.id.content_layout);

        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> handleBackPress());

        bookmarkButton.setOnClickListener(v -> toggleBookmark());

        submitButton.setOnClickListener(v -> submitAnswers());

        // Handle system back button
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (contentLayout != null) {
            contentLayout.setVisibility(show ? android.view.View.GONE : android.view.View.VISIBLE);
        }
    }

    private void loadArticleFromFirebase() {
        showLoading(true);

        db.collection("reading_lessons").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            article = new ReadingArticle();
                            article.setId(documentSnapshot.getString("id"));
                            article.setTitle(documentSnapshot.getString("title"));
                            article.setPassage(documentSnapshot.getString("passage"));
                            article.setContent(documentSnapshot.getString("content"));
                            article.setLevel(documentSnapshot.getString("level"));
                            article.setCategory(documentSnapshot.getString("category"));
                            article.setImageUrl(documentSnapshot.getString("imageUrl"));
                            article.setAuthor(documentSnapshot.getString("author"));

                            Long wordCount = documentSnapshot.getLong("wordCount");
                            if (wordCount != null) {
                                article.setWordCount(wordCount.intValue());
                            }

                            Long estimatedMinutes = documentSnapshot.getLong("estimatedMinutes");
                            if (estimatedMinutes != null) {
                                article.setEstimatedMinutes(estimatedMinutes.intValue());
                            }

                            // Parse exercises from Firebase
                            List<?> exercisesList = (List<?>) documentSnapshot.get("exercises");
                            Log.d(TAG, "Exercises from Firebase: " + (exercisesList != null ? exercisesList.size() + " items" : "null or missing"));

                            if (exercisesList != null && !exercisesList.isEmpty()) {
                                List<ReadingQuestion> questions = new ArrayList<>();
                                for (Object exerciseObj : exercisesList) {
                                    if (exerciseObj instanceof Map) {
                                        Map<String, Object> exerciseMap = (Map<String, Object>) exerciseObj;
                                        ReadingQuestion question = new ReadingQuestion();
                                        question.setQuestionText((String) exerciseMap.get("question"));
                                        question.setCorrectAnswer((String) exerciseMap.get("correctAnswer"));
                                        question.setExplanation((String) exerciseMap.get("explanation"));

                                        List<String> options = (List<String>) exerciseMap.get("options");
                                        question.setOptions(options);

                                        questions.add(question);
                                    }
                                }
                                article.setQuestions(questions);
                                Log.d(TAG, "Loaded " + questions.size() + " questions successfully");
                            } else {
                                Log.w(TAG, "No exercises field found - article will have no questions");
                                article.setQuestions(new ArrayList<>());
                            }

                            displayArticle();
                            article.incrementReadCount();
                            saveProgress();
                            trackReading();
                            showLoading(false);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing article data", e);
                            Toast.makeText(this, "Error loading article content", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load article: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading article", e);
                    finish();
                });
    }

    private void displayArticle() {
        // Set title
        if (article.getTitle() != null) {
            articleTitle.setText(article.getTitle());
        }

        // Load image with Glide
        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.placeholder_article)
                    .error(R.drawable.placeholder_article)
                    .centerCrop()
                    .into(articleImage);
        } else {
            articleImage.setImageResource(R.drawable.placeholder_article);
        }

        // Set author
        if (article.getAuthor() != null && !article.getAuthor().isEmpty()) {
            authorText.setText("By " + article.getAuthor());
        } else {
            authorText.setText("By NewsAndLearn");
        }

        // Calculate and set read time
        int readTimeMinutes = article.getEstimatedMinutes();
        if (readTimeMinutes == 0 && article.getWordCount() > 0) {
            readTimeMinutes = Math.max(1, article.getWordCount() / 200);
        }
        if (readTimeMinutes == 0) {
            readTimeMinutes = 5;
        }
        readTime.setText(readTimeMinutes + " min read");

        // Set content - prefer passage over content
        String textToDisplay = article.getPassage();
        if (textToDisplay == null || textToDisplay.isEmpty()) {
            textToDisplay = article.getContent();
        }
        if (textToDisplay != null && !textToDisplay.isEmpty()) {
            articleContent.setText(textToDisplay);
        } else {
            articleContent.setText("Content not available");
        }

        // Setup questions - SINGLE ARGUMENT CONSTRUCTOR
        if (article.getQuestions() != null && !article.getQuestions().isEmpty()) {
            questionsAdapter = new ReadingQuestionAdapter(article.getQuestions());
            questionsRecyclerView.setAdapter(questionsAdapter);

            int questionCount = article.getQuestions().size();
            questionsSubtitle.setText("Answer all " + questionCount + " questions to complete");
            submitButton.setEnabled(true);
            submitButton.setText("Submit Answers");
        } else {
            questionsSubtitle.setText("No questions available for this article");
            submitButton.setEnabled(false);
            submitButton.setText("No Questions");
            submitButton.setAlpha(0.5f);
        }

        // Check bookmark status
        checkBookmarkStatus();
    }

    private void checkBookmarkStatus() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("bookmarks").document(articleId)
                .get()
                .addOnSuccessListener(doc -> {
                    isBookmarked = doc.exists();
                    updateBookmarkIcon();
                });
    }

    private void toggleBookmark() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to bookmark", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        if (isBookmarked) {
            db.collection("users").document(userId)
                    .collection("bookmarks").document(articleId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isBookmarked = false;
                        updateBookmarkIcon();
                        Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> bookmark = Map.of(
                    "articleId", articleId,
                    "title", article.getTitle(),
                    "timestamp", System.currentTimeMillis()
            );
            db.collection("users").document(userId)
                    .collection("bookmarks").document(articleId)
                    .set(bookmark)
                    .addOnSuccessListener(aVoid -> {
                        isBookmarked = true;
                        updateBookmarkIcon();
                        Toast.makeText(this, "Article bookmarked", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateBookmarkIcon() {
        if (isBookmarked) {
            bookmarkButton.setImageResource(R.drawable.ic_favorite);
            bookmarkButton.setColorFilter(Color.parseColor("#FF6B6B"));
        } else {
            bookmarkButton.setImageResource(R.drawable.ic_favorite_border);
            bookmarkButton.clearColorFilter();
        }
    }

    private void submitAnswers() {
        if (questionsAdapter == null) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Integer, String> userAnswers = questionsAdapter.getUserAnswers();
        int totalQuestions = article.getQuestions().size();

        if (userAnswers.size() < totalQuestions) {
            Toast.makeText(this,
                    "Please answer all questions (" + userAnswers.size() + "/" + totalQuestions + ")",
                    Toast.LENGTH_LONG).show();
            for (int i = 0; i < totalQuestions; i++) {
                if (!userAnswers.containsKey(i)) {
                    questionsRecyclerView.smoothScrollToPosition(i);
                    break;
                }
            }
            return;
        }

        int score = questionsAdapter.calculateScore();
        article.setCompleted(true);
        article.setUserScore(score);
        saveProgress();

        showResultsDialog(score, totalQuestions);
    }

    private void showResultsDialog(int score, int totalQuestions) {
        String title;
        String message;
        int xpEarned;

        if (score >= 80) {
            title = "🎉 Excellent!";
            message = "You got " + score + "% correct!\nYou have great comprehension skills.";
            xpEarned = 30;
        } else if (score >= 60) {
            title = "👍 Good Job!";
            message = "You got " + score + "% correct!\nKeep up the good work.";
            xpEarned = 20;
        } else {
            title = "📚 Keep Practicing!";
            message = "You got " + score + "% correct.\nTry reading more carefully next time.";
            xpEarned = 10;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message + "\n\n+" + xpEarned + " XP earned!")
                .setPositiveButton("Continue", (dialog, which) -> {
                    awardXP(xpEarned, score);
                })
                .setNegativeButton("Review Answers", (dialog, which) -> {
                    Toast.makeText(this, "Scroll through questions to review your answers", Toast.LENGTH_LONG).show();
                    submitButton.setText("Back to Lessons");
                    submitButton.setOnClickListener(v -> finish());
                })
                .setCancelable(false)
                .show();
    }

    private void awardXP(int xpEarned, int score) {
        progressManager.addXP(xpEarned, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to award XP", e);
                finish();
            }
        });
    }

    private void trackReading() {
        progressManager.trackArticleRead(new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                Log.d(TAG, "Reading tracked successfully");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to track reading", e);
            }
        });
    }

    private void saveProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("reading_progress").document(articleId)
                .set(article)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progress saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save progress", e));
    }

    private void handleBackPress() {
        if (article != null && article.isCompleted()) {
            finish();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Leave Reading?")
                    .setMessage("Your progress will be saved, but you won't earn XP until you complete the questions.")
                    .setPositiveButton("Leave", (dialog, which) -> finish())
                    .setNegativeButton("Stay", null)
                    .show();
        }
    }
}