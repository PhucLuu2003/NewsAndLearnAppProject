package com.example.newsandlearn.Activity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VideoCompletionActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;
    private TextView congratsText;
    private TextView messageText;
    private CardView statsCard;
    private TextView watchTimeText, questionsAnsweredText, xpEarnedText, streakText;
    private ProgressBar xpProgress;
    private MaterialButton continueButton, replayButton;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private String lessonId;
    private String lessonTitle;
    private long watchDuration; // in seconds
    private int xpEarned;
    private int questionsAnswered;
    private int totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_completion);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get data from intent
        lessonId = getIntent().getStringExtra("lessonId");
        lessonTitle = getIntent().getStringExtra("lessonTitle");
        watchDuration = getIntent().getLongExtra("watchDuration", 0);
        questionsAnswered = getIntent().getIntExtra("questionsAnswered", 0);
        totalQuestions = getIntent().getIntExtra("totalQuestions", 0);
        
        // Calculate XP based on watch duration
        xpEarned = calculateXP(watchDuration);

        // Initialize views
        initializeViews();
        setupListeners();
        
        // Save progress and animate
        saveProgress();
        animateEntrance();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        congratsText = findViewById(R.id.congrats_text);
        messageText = findViewById(R.id.message_text);
        statsCard = findViewById(R.id.stats_card);
        watchTimeText = findViewById(R.id.watch_time_text);
        questionsAnsweredText = findViewById(R.id.questions_answered_text);
        xpEarnedText = findViewById(R.id.xp_earned_text);
        streakText = findViewById(R.id.streak_text);
        xpProgress = findViewById(R.id.xp_progress);
        continueButton = findViewById(R.id.continue_button);
        replayButton = findViewById(R.id.replay_button);

        // Set initial values
        titleText.setText("Video Completed! 🎉");
        congratsText.setText("Great job!");
        messageText.setText("You've completed: " + (lessonTitle != null ? lessonTitle : "this lesson"));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        continueButton.setOnClickListener(v -> {
            animateButtonClick(v);
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 200);
        });
        
        replayButton.setOnClickListener(v -> {
            animateButtonClick(v);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
            }, 200);
        });
    }

    private int calculateXP(long durationSeconds) {
        // Base XP: 10 per minute watched
        int baseXP = (int) (durationSeconds / 60) * 10;
        // Bonus for completing
        int completionBonus = 50;
        return baseXP + completionBonus;
    }

    private void saveProgress() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        
        // Save video progress
        Map<String, Object> progress = new HashMap<>();
        progress.put("lessonId", lessonId);
        progress.put("completed", true);
        progress.put("watchDuration", watchDuration);
        progress.put("xpEarned", xpEarned);
        progress.put("completedAt", System.currentTimeMillis());
        
        db.collection("users").document(userId)
                .collection("video_progress").document(lessonId)
                .set(progress);
        
        // Update user XP
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        long currentXP = document.getLong("totalXP") != null ? 
                                document.getLong("totalXP") : 0;
                        long currentStreak = document.getLong("currentStreak") != null ?
                                document.getLong("currentStreak") : 0;
                        
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("totalXP", currentXP + xpEarned);
                        
                        db.collection("users").document(userId).update(updates);
                        
                        // Update UI with actual streak
                        streakText.setText(String.valueOf(currentStreak));
                    }
                });
    }

    private void animateEntrance() {
        // Hide all views initially
        congratsText.setAlpha(0f);
        messageText.setAlpha(0f);
        statsCard.setAlpha(0f);
        continueButton.setAlpha(0f);
        replayButton.setAlpha(0f);

        // Animate congrats text
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            congratsText.animate()
                    .alpha(1f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        congratsText.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        }, 200);

        // Animate message
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            messageText.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .start();
        }, 400);

        // Animate stats card
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            statsCard.setTranslationY(50f);
            statsCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(this::animateStats)
                    .start();
        }, 600);

        // Animate buttons
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            continueButton.setTranslationY(30f);
            continueButton.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .start();
                    
            replayButton.setTranslationY(30f);
            replayButton.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .setStartDelay(100)
                    .start();
        }, 1000);
    }

    private void animateStats() {
        // Animate watch time
        animateNumber(watchTimeText, 0, (int) watchDuration, " seconds");
        
        // Animate questions answered
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateQuestions(questionsAnsweredText, 0, questionsAnswered, totalQuestions);
        }, 200);
        
        // Animate XP earned
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            animateNumber(xpEarnedText, 0, xpEarned, " XP");
            animateProgressBar(xpProgress, xpEarned);
        }, 400);
    }
    
    private void animateQuestions(TextView textView, int start, int end, int total) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(value + "/" + total);
        });
        animator.start();
    }

    private void animateNumber(TextView textView, int start, int end, String suffix) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(value + suffix);
        });
        animator.start();
    }

    private void animateProgressBar(ProgressBar progressBar, int targetProgress) {
        ValueAnimator animator = ValueAnimator.ofInt(0, Math.min(targetProgress, 100));
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });
        animator.start();
    }

    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
