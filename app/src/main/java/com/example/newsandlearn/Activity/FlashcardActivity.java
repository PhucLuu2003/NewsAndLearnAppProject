package com.example.newsandlearn.Activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularyWithProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.example.newsandlearn.Utils.VocabularyHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * FlashcardActivity - Interactive flashcard learning with 3D flip animation
 * Features:
 * - 3D card flip animation
 * - Swipe gestures (left/right)
 * - Text-to-Speech pronunciation
 * - SRS (Spaced Repetition System) integration
 * - Progress tracking
 */
public class FlashcardActivity extends AppCompatActivity {

    private static final String TAG = "FlashcardActivity";
    private static final int MIN_SWIPE_DISTANCE = 120;
    private static final int MAX_SWIPE_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    // UI Components
    private ImageView backButton, speakerIcon;
    private TextView progressText, knownCount, learningCount, remainingCount;
    private ProgressBar flashcardProgress;
    private LinearLayout cardFront, cardBack;
    private MaterialButton learningButton, knownButton;
    private FrameLayout flashcardContainer, completionOverlay;
    private CardView flashcard;
    private TextView wordText, pronunciationText;
    private TextView definitionText, exampleText;
    private TextView completionStats;
    private TextView swipeLeftIndicator, swipeRightIndicator;
    private MaterialButton doneButton;

    // Data
    private List<VocabularyWithProgress> vocabularyList;
    private int currentIndex = 0;
    private boolean isShowingFront = true;
    private int correctCount = 0;
    private int incorrectCount = 0;

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextToSpeech tts;
    private GestureDetector gestureDetector;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        initializeServices();
        initializeViews();
        loadVocabulary();
        setupListeners();
        setupGestures();
        setupTextToSpeech();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
    }

    private void initializeViews() {
        // Header
        backButton = findViewById(R.id.back_button);
        progressText = findViewById(R.id.progress_text);
        flashcardProgress = findViewById(R.id.flashcard_progress);
        knownCount = findViewById(R.id.known_count);
        learningCount = findViewById(R.id.learning_count);
        remainingCount = findViewById(R.id.remaining_count);

        // Cards
        flashcardContainer = findViewById(R.id.flashcard_container);
        flashcard = findViewById(R.id.flashcard);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);

        // Swipe indicators
        swipeLeftIndicator = findViewById(R.id.swipe_left_indicator);
        swipeRightIndicator = findViewById(R.id.swipe_right_indicator);

        // Front card elements
        wordText = findViewById(R.id.word_text);
        pronunciationText = findViewById(R.id.pronunciation_text);
        speakerIcon = findViewById(R.id.speaker_icon);

        // Back card elements
        definitionText = findViewById(R.id.definition_text);
        exampleText = findViewById(R.id.example_text);

        // Action buttons
        learningButton = findViewById(R.id.learning_button);
        knownButton = findViewById(R.id.known_button);

        // Completion overlay
        completionOverlay = findViewById(R.id.completion_overlay);
        completionStats = findViewById(R.id.completion_stats);
        doneButton = findViewById(R.id.finish_button);
    }

    private void loadVocabulary() {
        // Get vocabulary to review from intent or load from Firebase
        String setId = getIntent().getStringExtra("set_id");
        boolean reviewOnly = getIntent().getBooleanExtra("review_only", false);

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        vocabularyList = new ArrayList<>();

        // Load user's vocabulary using new structure
        db.collection("users").document(userId)
                .collection("user_vocabulary")
                .get()
                .addOnSuccessListener(userVocabSnapshot -> {
                    vocabularyList.clear();
                    
                    // Collect vocabulary IDs and progress
                    List<String> vocabIds = new ArrayList<>();
                    java.util.Map<String, UserVocabulary> progressMap = new java.util.HashMap<>();
                    
                    userVocabSnapshot.forEach(doc -> {
                        UserVocabulary userVocab = doc.toObject(UserVocabulary.class);
                        if (userVocab != null && userVocab.getVocabularyId() != null) {
                            // Filter based on review_only flag
                            if (!reviewOnly || userVocab.needsReview()) {
                                vocabIds.add(userVocab.getVocabularyId());
                                progressMap.put(userVocab.getVocabularyId(), userVocab);
                            }
                        }
                    });
                    
                    if (vocabIds.isEmpty()) {
                        Toast.makeText(this, "No vocabulary to review", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    
                    // Load vocabulary details in batches
                    loadVocabularyDetails(vocabIds, progressMap);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading vocabulary: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
    
    private void loadVocabularyDetails(List<String> vocabIds, java.util.Map<String, UserVocabulary> progressMap) {
        // Split into batches of 10 (Firestore limit)
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < vocabIds.size(); i += 10) {
            batches.add(vocabIds.subList(i, Math.min(i + 10, vocabIds.size())));
        }
        
        final int[] completedBatches = {0};
        
        for (List<String> batch : batches) {
            db.collection("vocabularies")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        querySnapshot.forEach(doc -> {
                            Vocabulary vocab = doc.toObject(Vocabulary.class);
                            if (vocab != null) {
                                vocab.setId(doc.getId());
                                UserVocabulary progress = progressMap.get(vocab.getId());
                                
                                VocabularyWithProgress item = new VocabularyWithProgress();
                                item.setVocabulary(vocab);
                                item.setUserProgress(progress);
                                vocabularyList.add(item);
                            }
                        });
                        
                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            // All batches loaded
                            if (vocabularyList.isEmpty()) {
                                Toast.makeText(this, "No vocabulary to review", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                            
                            // Shuffle for variety
                            Collections.shuffle(vocabularyList);
                            
                            // Show first card
                            showCard(0);
                            updateProgress();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading vocabulary details", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        // Tap to flip
        flashcard.setOnClickListener(v -> flipCard());

        // Speaker icon
        speakerIcon.setOnClickListener(v -> speakWord());

        // Action buttons
        learningButton.setOnClickListener(v -> handleAnswer(false));
        knownButton.setOnClickListener(v -> handleAnswer(true));

        // Done button
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> finish());
        }
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > MAX_SWIPE_OFF_PATH) {
                        return false;
                    }

                    // Swipe right = Know it
                    if (e2.getX() - e1.getX() > MIN_SWIPE_DISTANCE &&
                            Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        handleAnswer(true);
                        return true;
                    }
                    // Swipe left = Don't know
                    else if (e1.getX() - e2.getX() > MIN_SWIPE_DISTANCE &&
                            Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        handleAnswer(false);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        flashcardContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Text-to-Speech not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showCard(int index) {
        if (index >= vocabularyList.size()) {
            showCompletionDialog();
            return;
        }

        currentIndex = index;
        VocabularyWithProgress vocab = vocabularyList.get(currentIndex);

        // Reset to front
        if (!isShowingFront) {
            cardFront.setVisibility(View.VISIBLE);
            cardBack.setVisibility(View.GONE);
            isShowingFront = true;
        }

        // Update front card
        wordText.setText(vocab.getWord());
        pronunciationText.setText(vocab.getPronunciation() != null ? vocab.getPronunciation() : "");

        // Update back card
        definitionText.setText(vocab.getTranslation());
        exampleText.setText(vocab.getExample() != null ? vocab.getExample() : "No example available");

        // Entrance animation
        cardFront.setAlpha(0f);
        cardFront.setScaleX(0.8f);
        cardFront.setScaleY(0.8f);
        cardFront.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }



    private void flipCard() {
        // 3D flip animation
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.card_flip_out);
        AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.card_flip_in);

        if (isShowingFront) {
            flipOut.setTarget(cardFront);
            flipIn.setTarget(cardBack);

            flipOut.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    cardFront.setVisibility(View.GONE);
                    cardBack.setVisibility(View.VISIBLE);
                    flipIn.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });

            flipOut.start();
            isShowingFront = false;
        } else {
            flipOut.setTarget(cardBack);
            flipIn.setTarget(cardFront);

            flipOut.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    cardBack.setVisibility(View.GONE);
                    cardFront.setVisibility(View.VISIBLE);
                    flipIn.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });

            flipOut.start();
            isShowingFront = true;
        }
    }

    private void speakWord() {
        if (tts != null && currentIndex < vocabularyList.size()) {
            String word = vocabularyList.get(currentIndex).getWord();
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void handleAnswer(boolean knowIt) {
        if (currentIndex >= vocabularyList.size()) return;

        VocabularyWithProgress vocab = vocabularyList.get(currentIndex);
        UserVocabulary userProgress = vocab.getUserProgress();
        
        if (userProgress == null) return;

        // Update mastery
        if (knowIt) {
            userProgress.markCorrect();
            correctCount++;
            showFeedback(true);
        } else {
            userProgress.markIncorrect();
            incorrectCount++;
            showFeedback(false);
        }

        // Save to Firebase using VocabularyHelper
        VocabularyHelper.getInstance()
                .updateVocabularyProgress(vocab.getId(), userProgress)
                .addOnSuccessListener(aVoid -> {
                    progressManager.trackWordsLearned(1, null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving progress", Toast.LENGTH_SHORT).show();
                });

        // Move to next card after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentIndex++;
            updateProgress();
            showCard(currentIndex);
        }, 800);
    }

    private void showFeedback(boolean correct) {
        // Visual feedback with animation
        View feedbackView = correct ? knownButton : learningButton;
        feedbackView.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .withEndAction(() -> {
                    feedbackView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();

        // Update counts
        if (correct) {
            knownCount.setText("✓ " + correctCount);
        } else {
            learningCount.setText("⟳ " + incorrectCount);
        }
        remainingCount.setText("⋯ " + (vocabularyList.size() - currentIndex - 1));
    }



    private void updateProgress() {
        int total = vocabularyList.size();
        int current = Math.min(currentIndex + 1, total);

        progressText.setText(current + "/" + total);
        flashcardProgress.setMax(total);
        flashcardProgress.setProgress(current);
        
        // Update counts
        knownCount.setText("✓ " + correctCount);
        learningCount.setText("⟳ " + incorrectCount);
        remainingCount.setText("⋯ " + (total - current));
    }

    private void showCompletionDialog() {
        int totalCards = vocabularyList.size();
        int xp = totalCards * 10; // 10 XP per card

        completionStats.setText("You reviewed " + totalCards + " cards\n" +
                correctCount + " correct, " + incorrectCount + " to review");

        completionOverlay.setVisibility(View.VISIBLE);
        completionOverlay.setAlpha(0f);
        completionOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Award XP
        progressManager.addXP(xp, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // Confirm before exiting if in progress
        if (currentIndex > 0 && currentIndex < vocabularyList.size()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Exit Flashcards?")
                    .setMessage("Your progress will be saved")
                    .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Continue", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
