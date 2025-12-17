package com.example.newsandlearn.Activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

/**
 * ListeningActivity - Audio player with comprehension questions
 * Audio loaded from Firebase Storage URL, NO hard-coded audio
 */
public class ListeningActivity extends AppCompatActivity {

    private ImageView backButton, playPauseButton, speedButton, transcriptButton;
    private SeekBar audioSeekbar;
    private TextView currentTime, totalTime, lessonTitle;
    private RecyclerView questionsRecyclerView;
    private MaterialButton submitButton;

    private ListeningLesson lesson;
    private String lessonId;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private float playbackSpeed = 1.0f;
    private boolean isPlaying = false;

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
        handler = new Handler();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        playPauseButton = findViewById(R.id.play_pause_button);
        speedButton = findViewById(R.id.speed_button);
        transcriptButton = findViewById(R.id.transcript_button);
        audioSeekbar = findViewById(R.id.audio_seekbar);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        lessonTitle = findViewById(R.id.lesson_title);
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        submitButton = findViewById(R.id.submit_button);

        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        playPauseButton.setOnClickListener(v -> togglePlayPause());

        speedButton.setOnClickListener(v -> changeSpeed());

        transcriptButton.setOnClickListener(v -> showTranscript());

        audioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        submitButton.setOnClickListener(v -> submitAnswers());
    }

    /**
     * Load lesson from Firebase - DYNAMIC
     */
    private void loadLessonFromFirebase() {
        db.collection("listening_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    lesson = documentSnapshot.toObject(ListeningLesson.class);
                    if (lesson != null) {
                        displayLesson();
                        initializeAudioPlayer();
                    } else {
                        Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
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
        totalTime.setText(lesson.getFormattedDuration());

        // TODO: Setup questions adapter
        // questionsAdapter = new ListeningQuestionAdapter(lesson.getQuestions());
        // questionsRecyclerView.setAdapter(questionsAdapter);
    }

    /**
     * Initialize audio player with Firebase Storage URL
     */
    private void initializeAudioPlayer() {
        if (lesson.getAudioUrl() == null) {
            Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            // Load audio from Firebase Storage URL
            mediaPlayer.setDataSource(this, Uri.parse(lesson.getAudioUrl()));
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                audioSeekbar.setMax(mp.getDuration());
                updateSeekbar();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseButton.setImageResource(R.drawable.ic_play);
                lesson.incrementListenCount();
                saveProgress();
            });

        } catch (IOException e) {
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
        isPlaying = !isPlaying;
    }

    private void changeSpeed() {
        if (mediaPlayer == null) return;

        playbackSpeed = playbackSpeed == 1.0f ? 0.5f : (playbackSpeed == 0.5f ? 1.5f : 1.0f);
        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
        Toast.makeText(this, "Speed: " + playbackSpeed + "x", Toast.LENGTH_SHORT).show();
    }

    private void showTranscript() {
        // TODO: Show transcript dialog
        Toast.makeText(this, "Transcript feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void submitAnswers() {
        // TODO: Calculate score from questions
        int score = 85; // Placeholder

        lesson.setCompleted(true);
        lesson.setUserScore(score);
        saveProgress();

        // Award XP
        progressManager.addXP(50, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                Toast.makeText(ListeningActivity.this, "Great! Score: " + score + "% (+50 XP)", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void updateSeekbar() {
        if (mediaPlayer != null && isPlaying) {
            audioSeekbar.setProgress(mediaPlayer.getCurrentPosition());
            currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(this::updateSeekbar, 100);
        }
    }

    private String formatTime(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Save progress to Firebase
     */
    private void saveProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("listening_progress").document(lessonId)
                .set(lesson);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
