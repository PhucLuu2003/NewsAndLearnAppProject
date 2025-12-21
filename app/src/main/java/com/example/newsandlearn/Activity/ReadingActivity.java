package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.ReadingQuestionAdapter;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.ReadingQuestion;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ReadingActivity - Display full article with comprehension questions
 * All content loaded from Firebase, NO hard-coded articles
 */
public class ReadingActivity extends AppCompatActivity {

    private static final String TAG = "ReadingActivity";
    
    private ImageView backButton, bookmarkButton, articleImage;
    private TextView articleTitle, authorText, readTime, articleContent, questionsSubtitle;
    private RecyclerView questionsRecyclerView;
    private MaterialButton submitButton;

    private ReadingArticle article;
    private String articleId;
    private ReadingQuestionAdapter questionsAdapter;

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
        questionsSubtitle = findViewById(R.id.questions_subtitle);
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
        db.collection("reading_lessons").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        article = new ReadingArticle();
                        article.setId(documentSnapshot.getString("id"));
                        article.setTitle(documentSnapshot.getString("title"));
                        article.setPassage(documentSnapshot.getString("passage"));
                        article.setContent(documentSnapshot.getString("content"));
                        article.setLevel(documentSnapshot.getString("level"));
                        article.setCategory(documentSnapshot.getString("category"));
                        
                        Long wordCount = documentSnapshot.getLong("wordCount");
                        if (wordCount != null) {
                            article.setWordCount(wordCount.intValue());
                        }
                        
                        // Parse exercises from Firebase Map format to ReadingQuestion objects
                        List<?> exercisesList = (List<?>) documentSnapshot.get("exercises");
                        Log.d(TAG, "Exercises list from Firebase: " + (exercisesList != null ? exercisesList.size() + " items" : "null"));
                        
                        if (exercisesList != null) {
                            List<ReadingQuestion> questions = new ArrayList<>();
                            int exerciseIndex = 0;
                            for (Object exerciseObj : exercisesList) {
                                if (exerciseObj instanceof Map) {
                                    Map<String, Object> exerciseMap = (Map<String, Object>) exerciseObj;
                                    ReadingQuestion question = new ReadingQuestion();
                                    question.setQuestionText((String) exerciseMap.get("question"));
                                    question.setCorrectAnswer((String) exerciseMap.get("correctAnswer"));
                                    
                                    List<String> options = (List<String>) exerciseMap.get("options");
                                    question.setOptions(options);
                                    
                                    questions.add(question);
                                    Log.d(TAG, "Exercise " + (exerciseIndex + 1) + ": " + question.getQuestionText());
                                    exerciseIndex++;
                                }
                            }
                            article.setQuestions(questions);
                            Log.d(TAG, "Total questions parsed: " + questions.size());
                        } else {
                            Log.w(TAG, "No exercises found in Firebase document");
                        }
                        
                        displayArticle();
                        article.incrementReadCount();
                        saveProgress();
                        
                        // Track reading for daily tasks
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
                    } else {
                        Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading article", e);
                    finish();
                });
    }

    private void displayArticle() {
        articleTitle.setText(article.getTitle());
        
        // Prefer passage over content (reading_lessons use 'passage')
        String textToDisplay = article.getPassage() != null ? article.getPassage() : article.getContent();
        if (textToDisplay != null) {
            articleContent.setText(textToDisplay);
        }
        
        if (article.getAuthor() != null) {
            authorText.setText("By " + article.getAuthor());
        } else {
            authorText.setText("By Author");
        }
        
        // Calculate read time from word count if not set
        int readTimeMinutes = article.getEstimatedMinutes();
        if (readTimeMinutes == 0 && article.getWordCount() > 0) {
            readTimeMinutes = article.getWordCount() / 200; // Average reading speed
        }
        if (readTimeMinutes == 0) {
            readTimeMinutes = 5; // Default
        }
        readTime.setText(readTimeMinutes + " min read");

        // Setup questions adapter
        if (article.getQuestions() != null && !article.getQuestions().isEmpty()) {
            questionsAdapter = new ReadingQuestionAdapter(article.getQuestions());
            questionsRecyclerView.setAdapter(questionsAdapter);
            
            int questionCount = article.getQuestions().size();
            Log.d(TAG, "Loaded " + questionCount + " questions");
            
            // Update subtitle with question count
            questionsSubtitle.setText("Answer all " + questionCount + " questions below");
            
            // Show toast to inform user
            Toast.makeText(this, questionCount + " questions loaded", Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "No questions found for this article");
            questionsSubtitle.setText("No questions available");
            submitButton.setEnabled(false);
            submitButton.setText("No Questions Available");
        }
    }

    private void submitAnswers() {
        if (questionsAdapter == null) {
            Toast.makeText(this, "No questions to submit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if all questions are answered
        Map<Integer, String> userAnswers = questionsAdapter.getUserAnswers();
        int totalQuestions = article.getQuestions().size();
        
        if (userAnswers.size() < totalQuestions) {
            Toast.makeText(this, "Please answer all questions (" + userAnswers.size() + "/" + totalQuestions + ")", 
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate score
        int score = questionsAdapter.calculateScore();

        article.setCompleted(true);
        article.setUserScore(score);
        saveProgress();

        // Award XP based on score
        int xpEarned = score >= 80 ? 30 : (score >= 60 ? 20 : 10);
        
        progressManager.addXP(xpEarned, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                String message = score >= 80 ? "Excellent!" : (score >= 60 ? "Good job!" : "Keep practicing!");
                Toast.makeText(ReadingActivity.this, 
                        message + " Score: " + score + "% (+" + xpEarned + " XP)", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
            
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ReadingActivity.this, "Score: " + score + "%", Toast.LENGTH_SHORT).show();
                finish();
            }
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
                .set(article)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progress saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save progress", e));
    }
}

