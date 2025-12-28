package com.example.newsandlearn.Activity;

import android.content.Context;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.Model.ListeningQuestion;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListeningActivity extends AppCompatActivity {

    private static final String TAG = "ListeningActivity";

    // UI Components
    private FlexboxLayout answerArea, wordPoolArea;
    private MaterialButton checkButton;
    private TextView instructionText;
    private ImageView playAudioButton;
    private ShapeableImageView lessonIllustration;
    private ProgressBar lessonProgressBar;
    private FrameLayout bottomSheetContainer;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Lesson Data
    private String lessonId;
    private List<ListeningQuestion> allQuestions;
    private List<ListeningQuestion> reviewQueue;
    private int currentQuestionIndex = 0;
    private boolean isReviewing = false;

    // State
    private List<String> answerWords;
    private List<String> poolWords;

    // Services
    private ExoPlayer exoPlayer, prefetchPlayer;
    private SoundPool soundPool;
    private int correctSoundId, incorrectSoundId;
    private Vibrator vibrator;
    private ProgressManager progressManager;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);

        lessonId = getIntent().getStringExtra("lesson_id");
        if (lessonId == null) {
            Toast.makeText(this, "Error: No lesson ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        loadLessonFromFirebase(lessonId);
        incrementTimesListened();
    }

    private void initializeServices() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        prefetchPlayer = new ExoPlayer.Builder(this).build();
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        correctSoundId = soundPool.load(this, R.raw.correct_sound, 1);
        incorrectSoundId = soundPool.load(this, R.raw.incorrect_sound, 1);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        progressManager = ProgressManager.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        allQuestions = new ArrayList<>();
        reviewQueue = new ArrayList<>();
        answerWords = new ArrayList<>();
        poolWords = new ArrayList<>();
    }

    private void initializeViews() {
        answerArea = findViewById(R.id.answer_area);
        wordPoolArea = findViewById(R.id.word_pool_area);
        checkButton = findViewById(R.id.check_button);
        instructionText = findViewById(R.id.instruction_text);
        playAudioButton = findViewById(R.id.play_audio_button);
        lessonIllustration = findViewById(R.id.lesson_illustration);
        lessonProgressBar = findViewById(R.id.lesson_progress_bar);
        bottomSheetContainer = findViewById(R.id.bottom_sheet_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setupListeners() {
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());
        playAudioButton.setOnClickListener(v -> playAudio());
        checkButton.setOnClickListener(v -> checkAnswer());
    }

    private void loadLessonFromFirebase(String lessonId) {
        db.collection("listening_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Lesson not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ListeningLesson lesson = documentSnapshot.toObject(ListeningLesson.class);
                    if (lesson != null && lesson.getQuestions() != null && !lesson.getQuestions().isEmpty()) {
                        allQuestions = lesson.getQuestions().stream()
                                .filter(q -> q.getType() == ListeningQuestion.QuestionType.SENTENCE_BUILDING)
                                .collect(Collectors.toList());

                        if (!allQuestions.isEmpty()) {
                            lessonProgressBar.setMax(allQuestions.size());
                            loadQuestion(0);
                        } else {
                            Toast.makeText(this, "No sentence games in this lesson.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Lesson is empty or invalid.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void incrementTimesListened() {
        if (currentUserId == null || lessonId == null) return;

        db.collection("users")
                .document(currentUserId)
                .collection("listening_progress")
                .document(lessonId)
                .get()
                .addOnSuccessListener(doc -> {
                    long currentCount = doc.exists() && doc.contains("timesListened") ?
                            doc.getLong("timesListened") : 0;

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("timesListened", currentCount + 1);
                    updates.put("lastAccessed", System.currentTimeMillis());

                    db.collection("users")
                            .document(currentUserId)
                            .collection("listening_progress")
                            .document(lessonId)
                            .set(updates, com.google.firebase.firestore.SetOptions.merge());
                });
    }

    private void loadQuestion(int index) {
        // 1. Reset result UI
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetContainer.removeAllViews();

        // 2. Enable Check Button and set Index
        checkButton.setEnabled(true);
        currentQuestionIndex = index;

        // 3. Load question data
        ListeningQuestion question = isReviewing ? reviewQueue.get(index) : allQuestions.get(index);

        instructionText.setText(question.getQuestionText());
        lessonProgressBar.setProgress(isReviewing ? allQuestions.size() - reviewQueue.size() : index);

        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            Glide.with(this).load(question.getImageUrl()).into(lessonIllustration);
        }

        answerWords.clear();
        poolWords.clear();
        poolWords.addAll(Arrays.asList(question.getCorrectAnswer().split("\\s+")));
        if (question.getOptions() != null) {
            poolWords.addAll(question.getOptions());
        }
        Collections.shuffle(poolWords);

        updateWordViews();
        playAudio();
        prefetchNextAudio();
    }

    private void updateWordViews() {
        answerArea.removeAllViews();
        wordPoolArea.removeAllViews();

        for (String word : answerWords) {
            answerArea.addView(createWordChip(word, true));
        }
        for (String word : poolWords) {
            wordPoolArea.addView(createWordChip(word, false));
        }
    }

    private Chip createWordChip(String word, boolean isAnswerChip) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.chip_word, null, false);
        chip.setText(word);
        chip.setOnClickListener(v -> {
            if (isAnswerChip) {
                answerWords.remove(word);
                poolWords.add(word);
            } else {
                poolWords.remove(word);
                answerWords.add(word);
            }
            updateWordViews();
        });
        return chip;
    }

    private void playAudio() {
        ListeningQuestion question = isReviewing ? reviewQueue.get(currentQuestionIndex) : allQuestions.get(currentQuestionIndex);
        if (question.getAudioUrl() != null && !question.getAudioUrl().isEmpty()) {
            try {
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(question.getAudioUrl())));
                exoPlayer.prepare();
                exoPlayer.play();
            } catch (Exception e) {
                Toast.makeText(this, "Audio Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prefetchNextAudio() {
        int nextIndex = currentQuestionIndex + 1;
        List<ListeningQuestion> currentList = isReviewing ? reviewQueue : allQuestions;

        if (nextIndex < currentList.size()) {
            ListeningQuestion nextQuestion = currentList.get(nextIndex);
            if (nextQuestion.getAudioUrl() != null && !nextQuestion.getAudioUrl().isEmpty()) {
                try {
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(nextQuestion.getAudioUrl()));
                    prefetchPlayer.setMediaItem(mediaItem);
                    prefetchPlayer.prepare();
                } catch (Exception e) { /* Prefetch failed, not critical */ }
            }
        }
    }

    private void checkAnswer() {
        String userAnswer = String.join(" ", answerWords);
        ListeningQuestion currentQuestion = isReviewing ? reviewQueue.get(currentQuestionIndex) : allQuestions.get(currentQuestionIndex);

        checkButton.setEnabled(false);

        if (currentQuestion.checkAnswer(userAnswer)) {
            handleCorrectAnswer();
        } else {
            handleIncorrectAnswer();
        }
    }

    private void handleCorrectAnswer() {
        soundPool.play(correctSoundId, 1, 1, 1, 0, 1);
        vibrator.vibrate(100);
        if (isReviewing) {
            reviewQueue.remove(currentQuestionIndex);
            currentQuestionIndex--; // Adjust index after removal
        }
        showResultDrawer(true);
    }

    private void handleIncorrectAnswer() {
        soundPool.play(incorrectSoundId, 1, 1, 1, 0, 1);
        vibrator.vibrate(new long[]{0, 200, 100, 200}, -1);
        if (!isReviewing) {
            ListeningQuestion currentQuestion = allQuestions.get(currentQuestionIndex);
            if (!reviewQueue.contains(currentQuestion)) {
                reviewQueue.add(currentQuestion);
            }
        }
        showResultDrawer(false);
    }

    private void showResultDrawer(boolean isCorrect) {
        View sheetView = isCorrect ?
                getLayoutInflater().inflate(R.layout.bottom_sheet_correct, bottomSheetContainer, false) :
                getLayoutInflater().inflate(R.layout.bottom_sheet_incorrect, bottomSheetContainer, false);

        bottomSheetContainer.removeAllViews();
        bottomSheetContainer.addView(sheetView);

        Button continueBtn = sheetView.findViewById(R.id.continue_button);
        continueBtn.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            new Handler(Looper.getMainLooper()).postDelayed(this::proceedToNextStep, 300);
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void proceedToNextStep() {
        int nextIndex = currentQuestionIndex + 1;
        if (isReviewing) {
            if (reviewQueue.isEmpty()) {
                finishLesson(); // All mistakes corrected
            } else if (nextIndex >= reviewQueue.size()) {
                loadQuestion(0); // Loop back to the start of the remaining mistakes
            } else {
                loadQuestion(nextIndex);
            }
        } else {
            if (nextIndex < allQuestions.size()) {
                loadQuestion(nextIndex);
            } else {
                if (reviewQueue.isEmpty()) {
                    finishLesson(); // No mistakes made
                } else {
                    showScoreboard();
                }
            }
        }
    }

    private void showScoreboard() {
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_scoreboard, bottomSheetContainer, false);
        bottomSheetContainer.removeAllViews();
        bottomSheetContainer.addView(sheetView);

        ((TextView) sheetView.findViewById(R.id.correct_count_text)).setText(String.valueOf(allQuestions.size() - reviewQueue.size()));
        ((TextView) sheetView.findViewById(R.id.incorrect_count_text)).setText(String.valueOf(reviewQueue.size()));

        sheetView.findViewById(R.id.review_mistakes_button).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isReviewing = true;
                loadQuestion(0);
            }, 300);
        });

        sheetView.findViewById(R.id.finish_lesson_button).setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            new Handler(Looper.getMainLooper()).postDelayed(this::finishLesson, 300);
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void finishLesson() {
        int correctAnswersOnFirstTry = allQuestions.size() - reviewQueue.size();
        int totalQuestions = allQuestions.size();

        // Calculate score as percentage
        int scorePercentage = (int) ((correctAnswersOnFirstTry / (double) totalQuestions) * 100);

        // Calculate XP
        int totalXP = correctAnswersOnFirstTry * 20;

        // Save progress to Firestore
        saveProgressToFirestore(scorePercentage, () -> {
            progressManager.addXP(totalXP, new ProgressManager.ProgressCallback() {
                @Override
                public void onSuccess(UserProgress progress) {
                    String message = String.format("Lesson Complete! You earned %d XP. Score: %d%%", totalXP, scorePercentage);
                    Toast.makeText(ListeningActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                }
                @Override
                public void onFailure(Exception e) {
                    finish();
                }
            });
        });
    }

    private void saveProgressToFirestore(int score, Runnable onComplete) {
        if (currentUserId == null || lessonId == null) {
            onComplete.run();
            return;
        }

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("completed", true);
        progressData.put("userScore", score);
        progressData.put("completedAt", System.currentTimeMillis());

        db.collection("users")
                .document(currentUserId)
                .collection("listening_progress")
                .document(lessonId)
                .set(progressData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Progress saved successfully");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving progress", e);
                    onComplete.run();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) exoPlayer.release();
        if (prefetchPlayer != null) prefetchPlayer.release();
        if (soundPool != null) soundPool.release();
    }
}