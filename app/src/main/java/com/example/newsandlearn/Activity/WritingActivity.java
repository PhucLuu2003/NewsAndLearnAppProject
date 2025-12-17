package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.WritingPrompt;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * WritingActivity - Writing editor with scoring
 * Prompts loaded from Firebase, NO hard-coded prompts
 */
public class WritingActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView wordCountText, promptText, wordRequirement, timeLimit;
    private TextView grammarScore, vocabularyScore, coherenceScore;
    private EditText writingEditor;
    private CardView scoreCard;
    private MaterialButton submitButton;

    private WritingPrompt prompt;
    private String promptId;
    private int currentWordCount = 0;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        promptId = getIntent().getStringExtra("prompt_id");
        if (promptId == null) {
            Toast.makeText(this, "Error: No prompt ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        loadPromptFromFirebase();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        wordCountText = findViewById(R.id.word_count_text);
        promptText = findViewById(R.id.prompt_text);
        wordRequirement = findViewById(R.id.word_requirement);
        timeLimit = findViewById(R.id.time_limit);
        writingEditor = findViewById(R.id.writing_editor);
        scoreCard = findViewById(R.id.score_card);
        grammarScore = findViewById(R.id.grammar_score);
        vocabularyScore = findViewById(R.id.vocabulary_score);
        coherenceScore = findViewById(R.id.coherence_score);
        submitButton = findViewById(R.id.submit_button);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        writingEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitButton.setOnClickListener(v -> submitWriting());
    }

    /**
     * Load prompt from Firebase - DYNAMIC
     */
    private void loadPromptFromFirebase() {
        db.collection("writing_prompts").document(promptId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    prompt = documentSnapshot.toObject(WritingPrompt.class);
                    if (prompt != null) {
                        displayPrompt();
                    } else {
                        Toast.makeText(this, "Prompt not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayPrompt() {
        promptText.setText(prompt.getPromptText());
        wordRequirement.setText(prompt.getMinWords() + "-" + prompt.getMaxWords() + " words");
        timeLimit.setText(prompt.getTimeMinutes() + " min");
        wordCountText.setText("0 / " + prompt.getMaxWords() + " words");

        // Load existing writing if any
        if (prompt.getUserText() != null) {
            writingEditor.setText(prompt.getUserText());
        }
    }

    private void updateWordCount(String text) {
        if (text.trim().isEmpty()) {
            currentWordCount = 0;
        } else {
            currentWordCount = text.trim().split("\\s+").length;
        }

        wordCountText.setText(currentWordCount + " / " + prompt.getMaxWords() + " words");

        // Change color based on requirement
        if (currentWordCount >= prompt.getMinWords() && currentWordCount <= prompt.getMaxWords()) {
            wordCountText.setTextColor(getColor(R.color.success));
        } else if (currentWordCount > prompt.getMaxWords()) {
            wordCountText.setTextColor(getColor(R.color.error));
        } else {
            wordCountText.setTextColor(getColor(R.color.text_secondary));
        }
    }

    private void submitWriting() {
        String userText = writingEditor.getText().toString().trim();

        if (userText.isEmpty()) {
            Toast.makeText(this, "Please write something first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentWordCount < prompt.getMinWords()) {
            Toast.makeText(this, "Need at least " + prompt.getMinWords() + " words", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentWordCount > prompt.getMaxWords()) {
            Toast.makeText(this, "Exceeded maximum " + prompt.getMaxWords() + " words", Toast.LENGTH_SHORT).show();
            return;
        }

        // Submit writing
        prompt.submitWriting(userText);

        // Simple scoring algorithm (can be enhanced with AI)
        int gramScore = calculateGrammarScore(userText);
        int vocabScore = calculateVocabularyScore(userText);
        int cohScore = calculateCoherenceScore(userText);

        prompt.scoreWriting(gramScore, vocabScore, cohScore);
        displayScores(gramScore, vocabScore, cohScore);
        saveSubmission();

        // Award XP
        int xp = prompt.getOverallScore();
        progressManager.addXP(xp, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                Toast.makeText(WritingActivity.this, "Submitted! +" + xp + " XP", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private int calculateGrammarScore(String text) {
        // Simple scoring based on sentence structure
        int sentences = text.split("[.!?]").length;
        int words = currentWordCount;
        int avgWordsPerSentence = words / Math.max(sentences, 1);
        
        // Ideal: 15-20 words per sentence
        if (avgWordsPerSentence >= 15 && avgWordsPerSentence <= 20) {
            return 90;
        } else if (avgWordsPerSentence >= 10 && avgWordsPerSentence <= 25) {
            return 75;
        } else {
            return 60;
        }
    }

    private int calculateVocabularyScore(String text) {
        // Simple scoring based on unique words
        String[] words = text.toLowerCase().split("\\s+");
        long uniqueWords = java.util.Arrays.stream(words).distinct().count();
        double ratio = (double) uniqueWords / words.length;
        
        // Higher ratio = better vocabulary variety
        if (ratio > 0.7) return 90;
        else if (ratio > 0.5) return 75;
        else return 60;
    }

    private int calculateCoherenceScore(String text) {
        // Simple scoring based on paragraphs and transitions
        int paragraphs = text.split("\n\n").length;
        boolean hasTransitions = text.toLowerCase().matches(".*(however|therefore|moreover|furthermore|in addition|consequently).*");
        
        if (paragraphs >= 3 && hasTransitions) return 90;
        else if (paragraphs >= 2) return 75;
        else return 60;
    }

    private void displayScores(int gramScore, int vocabScore, int cohScore) {
        scoreCard.setVisibility(android.view.View.VISIBLE);
        grammarScore.setText(gramScore + "%");
        vocabularyScore.setText(vocabScore + "%");
        coherenceScore.setText(cohScore + "%");
        submitButton.setEnabled(false);
    }

    /**
     * Save submission to Firebase
     */
    private void saveSubmission() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("writing_submissions").document(promptId)
                .set(prompt);
    }
}
