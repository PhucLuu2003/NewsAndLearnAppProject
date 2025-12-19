package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.ReadingGamification;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AdvancedReadingAnalytics;
import com.example.newsandlearn.Utils.GamificationManager;
import com.example.newsandlearn.Utils.GeminiReadingAssistant;
import com.example.newsandlearn.Utils.ImmersiveReadingManager;
import com.example.newsandlearn.Utils.SmartRecommendationEngine;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * SuperReadingActivity - Ultimate reading experience with AI, gamification, and immersive modes
 * Features: Gemini AI Assistant, Speed Reading, Bionic Reading, Focus Mode, Smart Recommendations
 */
public class SuperReadingActivity extends AppCompatActivity {

    private ImageView backButton, bookmarkButton;
    private TextView articleTitle, articleContent, levelBadge, xpEarned;
    private FloatingActionButton aiAssistantFab, speedReadFab, focusModeFab;
    private MaterialButton generateQuizButton, getSummaryButton, recommendButton;
    private LinearLayout aiActionsLayout, recommendationsLayout;
    
    private ReadingArticle article;
    private String articleId;
    
    // Managers
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GeminiReadingAssistant aiAssistant;
    private GamificationManager gamificationManager;
    private SmartRecommendationEngine recommendationEngine;
    private AdvancedReadingAnalytics analytics;
    private ImmersiveReadingManager immersiveManager;
    
    // Reading tracking
    private long readingStartTime;
    private int currentReadingMode = 0; // 0=Normal, 1=Bionic, 2=Focus, 3=Speed
    private int currentParagraph = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_reading);

        articleId = getIntent().getStringExtra("article_id");
        if (articleId == null) {
            Toast.makeText(this, "Error: No article ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        loadArticle();
        
        readingStartTime = System.currentTimeMillis();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        aiAssistant = GeminiReadingAssistant.getInstance();
        gamificationManager = GamificationManager.getInstance();
        recommendationEngine = SmartRecommendationEngine.getInstance();
        analytics = AdvancedReadingAnalytics.getInstance();
        immersiveManager = ImmersiveReadingManager.getInstance(this);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        bookmarkButton = findViewById(R.id.bookmark_button);
        articleTitle = findViewById(R.id.article_title);
        articleContent = findViewById(R.id.article_content);
        levelBadge = findViewById(R.id.level_badge);
        xpEarned = findViewById(R.id.xp_earned);
        
        aiAssistantFab = findViewById(R.id.ai_assistant_fab);
        speedReadFab = findViewById(R.id.speed_read_fab);
        focusModeFab = findViewById(R.id.focus_mode_fab);
        
        generateQuizButton = findViewById(R.id.generate_quiz_button);
        getSummaryButton = findViewById(R.id.get_summary_button);
        recommendButton = findViewById(R.id.recommend_button);
        
        aiActionsLayout = findViewById(R.id.ai_actions_layout);
        recommendationsLayout = findViewById(R.id.recommendations_layout);
        
        // Apply immersive reading settings
        immersiveManager.applyAllSettings(articleContent);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finishReading());
        
        // AI Assistant FAB
        aiAssistantFab.setOnClickListener(v -> {
            if (aiActionsLayout.getVisibility() == View.VISIBLE) {
                aiActionsLayout.setVisibility(View.GONE);
            } else {
                aiActionsLayout.setVisibility(View.VISIBLE);
                recommendationsLayout.setVisibility(View.GONE);
            }
        });
        
        // Speed Reading FAB
        speedReadFab.setOnClickListener(v -> toggleSpeedReading());
        
        // Focus Mode FAB
        focusModeFab.setOnClickListener(v -> toggleFocusMode());
        
        // AI Actions
        generateQuizButton.setOnClickListener(v -> generateQuiz());
        getSummaryButton.setOnClickListener(v -> getSummary());
        recommendButton.setOnClickListener(v -> showRecommendations());
        
        // Text selection for AI explain
        articleContent.setOnLongClickListener(v -> {
            showAIExplainDialog();
            return true;
        });
    }

    private void loadArticle() {
        db.collection("reading_articles").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    article = documentSnapshot.toObject(ReadingArticle.class);
                    if (article != null) {
                        displayArticle();
                        trackArticleOpened();
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
        
        String content = article.getPassage() != null ? article.getPassage() : article.getContent();
        if (content != null) {
            articleContent.setText(content);
            
            // Set AI context
            aiAssistant.setArticleContext(article.getTitle(), content);
        }
        
        // Load gamification data
        loadGamificationData();
    }

    private void loadGamificationData() {
        gamificationManager.loadGamification(new GamificationManager.GamificationCallback() {
            @Override
            public void onSuccess(ReadingGamification gamification) {
                levelBadge.setText("Level " + gamification.getCurrentLevel());
                levelBadge.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail
            }
        });
    }

    /**
     * Toggle Speed Reading Mode (RSVP)
     */
    private void toggleSpeedReading() {
        if (immersiveManager.isSpeedReadingActive()) {
            immersiveManager.stopSpeedReading();
            speedReadFab.setImageResource(android.R.drawable.ic_media_play);
            articleContent.setText(article.getPassage() != null ? article.getPassage() : article.getContent());
        } else {
            String content = article.getPassage() != null ? article.getPassage() : article.getContent();
            speedReadFab.setImageResource(android.R.drawable.ic_media_pause);
            
            immersiveManager.startSpeedReading(content, articleContent, 
                new ImmersiveReadingManager.SpeedReadingCallback() {
                    @Override
                    public void onWordDisplayed(String word, int index, int total) {
                        // Progress is automatically displayed
                    }

                    @Override
                    public void onComplete() {
                        speedReadFab.setImageResource(android.R.drawable.ic_media_play);
                        articleContent.setText(content);
                        Toast.makeText(SuperReadingActivity.this, "Speed reading complete!", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    /**
     * Toggle Focus Mode
     */
    private void toggleFocusMode() {
        currentReadingMode = (currentReadingMode == 2) ? 0 : 2;
        
        String content = article.getPassage() != null ? article.getPassage() : article.getContent();
        
        if (currentReadingMode == 2) {
            // Focus mode ON
            SpannableString spannable = immersiveManager.applyFocusMode(content, currentParagraph);
            articleContent.setText(spannable);
            focusModeFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            
            // Auto-advance paragraphs
            new Handler().postDelayed(() -> {
                if (currentReadingMode == 2) {
                    currentParagraph++;
                    toggleFocusMode();
                }
            }, 10000); // 10 seconds per paragraph
        } else {
            // Focus mode OFF
            articleContent.setText(content);
            focusModeFab.setImageResource(android.R.drawable.ic_menu_view);
            currentParagraph = 0;
        }
    }

    /**
     * Generate quiz using AI
     */
    private void generateQuiz() {
        Toast.makeText(this, "Generating quiz with AI...", Toast.LENGTH_SHORT).show();
        
        aiAssistant.generateQuestions(5, new GeminiReadingAssistant.QuestionCallback() {
            @Override
            public void onSuccess(List<GeminiReadingAssistant.Question> questions) {
                showQuizDialog(questions);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SuperReadingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get AI summary
     */
    private void getSummary() {
        Toast.makeText(this, "Generating summary...", Toast.LENGTH_SHORT).show();
        
        aiAssistant.generateSummary(new GeminiReadingAssistant.AICallback() {
            @Override
            public void onSuccess(String summary) {
                new AlertDialog.Builder(SuperReadingActivity.this)
                    .setTitle("📝 AI Summary")
                    .setMessage(summary)
                    .setPositiveButton("OK", null)
                    .show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SuperReadingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show AI explain dialog
     */
    private void showAIExplainDialog() {
        // TODO: Get selected text
        String selectedText = "example"; // Placeholder
        
        aiAssistant.explainText(selectedText, new GeminiReadingAssistant.AICallback() {
            @Override
            public void onSuccess(String explanation) {
                new AlertDialog.Builder(SuperReadingActivity.this)
                    .setTitle("💡 AI Explanation")
                    .setMessage(explanation)
                    .setPositiveButton("OK", null)
                    .show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SuperReadingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show quiz dialog
     */
    private void showQuizDialog(List<GeminiReadingAssistant.Question> questions) {
        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions generated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // TODO: Create proper quiz UI
        StringBuilder quizText = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            GeminiReadingAssistant.Question q = questions.get(i);
            quizText.append((i + 1)).append(". ").append(q.question).append("\n\n");
            for (int j = 0; j < q.options.length; j++) {
                quizText.append((char)('A' + j)).append(") ").append(q.options[j]).append("\n");
            }
            quizText.append("\n");
        }
        
        new AlertDialog.Builder(this)
            .setTitle("🧠 AI-Generated Quiz")
            .setMessage(quizText.toString())
            .setPositiveButton("OK", null)
            .show();
    }

    /**
     * Show smart recommendations
     */
    private void showRecommendations() {
        if (recommendationsLayout.getVisibility() == View.VISIBLE) {
            recommendationsLayout.setVisibility(View.GONE);
            return;
        }
        
        aiActionsLayout.setVisibility(View.GONE);
        recommendationsLayout.setVisibility(View.VISIBLE);
        
        recommendationEngine.getNextArticle(articleId, new SmartRecommendationEngine.RecommendationCallback() {
            @Override
            public void onSuccess(List<ReadingArticle> recommendations) {
                // TODO: Display recommendations in RecyclerView
                Toast.makeText(SuperReadingActivity.this, 
                    "Found " + recommendations.size() + " similar articles", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SuperReadingActivity.this, "Error loading recommendations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Track article opened
     */
    private void trackArticleOpened() {
        // Track with gamification
        gamificationManager.trackArticleRead(
            new GamificationManager.GamificationCallback() {
                @Override
                public void onSuccess(ReadingGamification gamification) {
                    // Show XP earned
                    xpEarned.setText("+50 XP");
                    xpEarned.setVisibility(View.VISIBLE);
                    
                    // Animate and hide
                    new Handler().postDelayed(() -> {
                        xpEarned.setVisibility(View.GONE);
                    }, 2000);
                }

                @Override
                public void onFailure(Exception e) {
                    // Silently fail
                }
            },
            challenge -> {
                // Challenge completed
                Toast.makeText(SuperReadingActivity.this, 
                    "🎉 Challenge completed: " + challenge.getTitle(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    /**
     * Finish reading and track analytics
     */
    private void finishReading() {
        long readingTime = System.currentTimeMillis() - readingStartTime;
        int readingMinutes = (int) (readingTime / (1000 * 60));
        
        // Track reading session
        AdvancedReadingAnalytics.ReadingSession session = new AdvancedReadingAnalytics.ReadingSession();
        session.articleId = articleId;
        session.articleTitle = article.getTitle();
        session.wordCount = article.getWordCount();
        session.startTime = readingStartTime;
        session.endTime = System.currentTimeMillis();
        session.readingSpeed = analytics.calculateReadingSpeed(article.getWordCount(), readingTime);
        session.category = article.getCategory();
        session.difficulty = article.getDifficulty();
        
        analytics.trackReadingSession(session);
        
        // Track reading time for gamification
        gamificationManager.trackReadingTime(readingMinutes, null, null);
        
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (immersiveManager.isSpeedReadingActive()) {
            immersiveManager.stopSpeedReading();
        }
    }
}
