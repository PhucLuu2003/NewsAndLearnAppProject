package com.example.newsandlearn.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.Model.ListeningQuestion;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListeningActivity extends AppCompatActivity {

    private FlexboxLayout answerArea, wordPoolArea;
    private MaterialButton checkButton;
    private TextView instructionText, lessonTitle;
    private ImageView playAudioButton;

    private ListeningLesson lesson;
    private ListeningQuestion currentQuestion;
    private String lessonId;
    private ExoPlayer exoPlayer;

    private List<String> answerWords = new ArrayList<>();
    private List<String> poolWords = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

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
        loadLessonFromFirebase();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
        exoPlayer = new ExoPlayer.Builder(this).build();
    }

    private void initializeViews() {
        answerArea = findViewById(R.id.answer_area);
        wordPoolArea = findViewById(R.id.word_pool_area);
        checkButton = findViewById(R.id.check_button);
        instructionText = findViewById(R.id.instruction_text);
        lessonTitle = findViewById(R.id.lesson_title);
        playAudioButton = findViewById(R.id.play_audio_button);
    }

    private void setupListeners() {
        checkButton.setOnClickListener(v -> checkAnswer());
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());
        playAudioButton.setOnClickListener(v -> playAudio());
    }

    private void loadLessonFromFirebase() {
        db.collection("listening_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    lesson = documentSnapshot.toObject(ListeningLesson.class);
                    if (lesson != null && lesson.getQuestions() != null && !lesson.getQuestions().isEmpty()) {
                        for (ListeningQuestion question : lesson.getQuestions()) {
                            if (question.getType() == ListeningQuestion.QuestionType.SENTENCE_BUILDING) {
                                currentQuestion = question;
                                break;
                            }
                        }
                        if (currentQuestion != null) {
                            displayLesson();
                            setupNewQuestion();
                            playAudio(); // Autoplay when loaded
                        } else {
                            Toast.makeText(this, "No sentence building questions found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Lesson or questions not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayLesson() {
        lessonTitle.setText(lesson.getTitle());
        instructionText.setText(currentQuestion.getQuestionText());
    }

    private void setupNewQuestion() {
        answerWords.clear();
        poolWords.clear();

        String[] words = currentQuestion.getCorrectAnswer().split("\\s+");
        poolWords.addAll(Arrays.asList(words));
        poolWords.addAll(currentQuestion.getOptions());
        Collections.shuffle(poolWords);

        updateWordViews();
    }

    private void updateWordViews() {
        answerArea.removeAllViews();
        wordPoolArea.removeAllViews();

        for (String word : answerWords) {
            Chip chip = createWordChip(word, true);
            answerArea.addView(chip);
        }

        for (String word : poolWords) {
            Chip chip = createWordChip(word, false);
            wordPoolArea.addView(chip);
        }
    }

    private Chip createWordChip(String word, boolean isAnswerChip) {
        LayoutInflater inflater = LayoutInflater.from(this);
        Chip chip = (Chip) inflater.inflate(R.layout.chip_word, null, false);
        chip.setText(word);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            if (isAnswerChip) {
                answerWords.remove(word);
                poolWords.add(word);
            } else {
                poolWords.remove(word);
                answerWords.add(word);
            }
            Collections.shuffle(poolWords);
            updateWordViews();
        });

        return chip;
    }

    private void playAudio() {
        if (lesson != null && lesson.getAudioUrl() != null && !lesson.getAudioUrl().isEmpty()) {
            try {
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(lesson.getAudioUrl()));
                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();
                exoPlayer.play();
            } catch (Exception e) {
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAnswer() {
        String userAnswer = String.join(" ", answerWords);
        if (currentQuestion.checkAnswer(userAnswer)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            progressManager.addXP(20, new ProgressManager.ProgressCallback() {
                @Override
                public void onSuccess(UserProgress progress) {
                    Toast.makeText(ListeningActivity.this, "+20 XP", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Incorrect. Try again!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}
