package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GrammarLessonActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView levelBadge, lessonTitle, lessonDescription;
    private TextView explanationText, examplesText, rulesText;
    private MaterialCardView rulesCard;
    private MaterialButton completeButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String lessonId;
    private String title;
    private boolean isCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_lesson);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get lesson data from intent
        lessonId = getIntent().getStringExtra("lesson_id");
        title = getIntent().getStringExtra("title");

        // Initialize views
        initializeViews();
        setupToolbar();
        loadLessonData();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        levelBadge = findViewById(R.id.level_badge);
        lessonTitle = findViewById(R.id.lesson_title);
        lessonDescription = findViewById(R.id.lesson_description);
        explanationText = findViewById(R.id.explanation_text);
        examplesText = findViewById(R.id.examples_text);
        rulesText = findViewById(R.id.rules_text);
        rulesCard = findViewById(R.id.rules_card);
        completeButton = findViewById(R.id.complete_button);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Grammar Lesson");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadLessonData() {
        if (lessonId == null) {
            Toast.makeText(this, "Error: Lesson not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load lesson from Firebase
        db.collection("grammar_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Set data
                        String level = document.getString("level");
                        String titleStr = document.getString("title");
                        String description = document.getString("description");
                        String content = document.getString("content");

                        if (level != null) levelBadge.setText(level);
                        if (titleStr != null) lessonTitle.setText(titleStr);
                        if (description != null) lessonDescription.setText(description);
                        
                        // Display content in explanation text (main content area)
                        if (content != null && !content.isEmpty()) {
                            explanationText.setText(content);
                            // Hide examples and rules cards since we're using single content field
                            if (findViewById(R.id.examples_card) != null) {
                                findViewById(R.id.examples_card).setVisibility(View.GONE);
                            }
                            rulesCard.setVisibility(View.GONE);
                        } else {
                            // Fallback to old format if content is not available
                            String explanation = document.getString("explanation");
                            String examples = document.getString("examples");
                            String rules = document.getString("rules");
                            
                            if (explanation != null) explanationText.setText(explanation);
                            if (examples != null) examplesText.setText(examples);
                            
                            if (rules != null && !rules.isEmpty()) {
                                rulesText.setText(rules);
                                rulesCard.setVisibility(View.VISIBLE);
                            } else {
                                rulesCard.setVisibility(View.GONE);
                            }
                        }

                        // Check if user completed this lesson
                        checkUserProgress();
                    } else {
                        Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading lesson: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void checkUserProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("grammar_progress")
                .whereEqualTo("lessonId", lessonId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Boolean completed = queryDocumentSnapshots.getDocuments().get(0)
                                .getBoolean("completed");
                        if (completed != null && completed) {
                            isCompleted = true;
                            updateCompleteButton();
                        }
                    }
                });
    }

    private void setupListeners() {
        completeButton.setOnClickListener(v -> {
            if (isCompleted) {
                Toast.makeText(this, "✅ Already completed!", Toast.LENGTH_SHORT).show();
            } else {
                markAsComplete();
            }
        });
    }

    private void markAsComplete() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Save progress to Firebase
        Map<String, Object> progress = new HashMap<>();
        progress.put("lessonId", lessonId);
        progress.put("completed", true);
        progress.put("score", 100); // Default score for reading
        progress.put("completedAt", System.currentTimeMillis());

        completeButton.setEnabled(false);
        completeButton.setText("Saving...");

        db.collection("users").document(userId)
                .collection("grammar_progress")
                .add(progress)
                .addOnSuccessListener(documentReference -> {
                    isCompleted = true;
                    updateCompleteButton();

                    // Update module progress
                    ProgressHelper.completeLesson("grammar", 5);

                    Toast.makeText(this, "🎉 Lesson completed! +5% progress", 
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    completeButton.setEnabled(true);
                    completeButton.setText("Mark as Complete");
                });
    }

    private void updateCompleteButton() {
        completeButton.setText("✅ Completed");
        completeButton.setEnabled(false);
        completeButton.setBackgroundColor(getColor(R.color.success));
    }
}
