package com.example.newsandlearn.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.newsandlearn.Model.SpeakingLesson;
import com.example.newsandlearn.Model.SpeakingPrompt;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageButton;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SpeakingActivity - Speech recognition and pronunciation practice
 * Prompts loaded from Firebase, uses Google Speech API
 */
public class SpeakingActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private ImageView backButton, playSampleButton;
    private TextView lessonTitle, promptText, statusText;
    private TextView pronunciationScore, fluencyScore;
    private CardView scoresCard;
    private android.view.View statusCard;
    private ProgressBar recordingProgress;
    private ImageButton recordButton;
    private MaterialButton tryAgainButton;
    private View pulseView;
    private ObjectAnimator pulseAnimator;

    private SpeakingLesson lesson;
    private SpeakingPrompt currentPrompt;
    private String lessonId;
    private int currentPromptIndex = 0;

    private SpeechRecognizer speechRecognizer;
    private MediaPlayer samplePlayer;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speaking);

        lessonId = getIntent().getStringExtra("lesson_id");
        if (lessonId == null) {
            Toast.makeText(this, "Error: No lesson ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        checkPermissions();
        loadLessonFromFirebase();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        playSampleButton = findViewById(R.id.play_sample_button);
        lessonTitle = findViewById(R.id.lesson_title);
        promptText = findViewById(R.id.prompt_text);
        statusText = findViewById(R.id.status_text);
        statusCard = findViewById(R.id.status_card);
        scoresCard = findViewById(R.id.scores_card);
        recordingProgress = findViewById(R.id.recording_progress);
        pronunciationScore = findViewById(R.id.pronunciation_score);
        fluencyScore = findViewById(R.id.fluency_score);
        recordButton = findViewById(R.id.record_button);
        pulseView = findViewById(R.id.pulse_view);
        tryAgainButton = findViewById(R.id.try_again_button);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        playSampleButton.setOnClickListener(v -> playSampleAudio());

        recordButton.setOnClickListener(v -> toggleRecording());

        tryAgainButton.setOnClickListener(v -> {
            scoresCard.setVisibility(View.GONE);
            recordButton.setVisibility(View.VISIBLE);
        });

        setupSpeechRecognizer();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Load lesson from Firebase - DYNAMIC
     */
    private void loadLessonFromFirebase() {
        android.util.Log.d("SpeakingActivity", "Loading lesson: " + lessonId);
        
        db.collection("speaking_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Lesson not found in database", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    lesson = documentSnapshot.toObject(SpeakingLesson.class);
                    if (lesson != null) {
                        // Set ID if missing
                        if (lesson.getId() == null || lesson.getId().isEmpty()) {
                            lesson.setId(lessonId);
                        }
                        
                        android.util.Log.d("SpeakingActivity", "Lesson loaded: " + lesson.getTitle());
                        android.util.Log.d("SpeakingActivity", "Content: " + (lesson.getContent() != null ? "Yes" : "No"));
                        android.util.Log.d("SpeakingActivity", "Prompts: " + (lesson.getPrompts() != null ? lesson.getPrompts().size() : 0));
                        
                        displayLesson();
                        
                        // Check if we have prompts or just content
                        if (lesson.getPrompts() != null && !lesson.getPrompts().isEmpty()) {
                            loadPrompt(0);
                        } else if (lesson.getContent() != null) {
                            // Show content as prompt
                            promptText.setText(lesson.getContent());
                            playSampleButton.setVisibility(lesson.getSampleAudioUrl() != null ? View.VISIBLE : View.GONE);
                        } else {
                            Toast.makeText(this, "No content available for this lesson", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error parsing lesson data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SpeakingActivity", "Error loading lesson", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayLesson() {
        lessonTitle.setText(lesson.getTitle());
    }

    private void loadPrompt(int index) {
        if (index >= lesson.getPrompts().size()) {
            completeLesson();
            return;
        }

        currentPromptIndex = index;
        currentPrompt = lesson.getPrompts().get(index);
        promptText.setText(currentPrompt.getPromptText());
    }

    /**
     * Play sample audio from Firebase Storage URL or online URL
     */
    private void playSampleAudio() {
        String audioUrl = null;
        
        // Try to get audio URL from current prompt first, then from lesson
        if (currentPrompt != null && currentPrompt.getSampleAudioUrl() != null) {
            audioUrl = currentPrompt.getSampleAudioUrl();
        } else if (lesson != null && lesson.getSampleAudioUrl() != null) {
            audioUrl = lesson.getSampleAudioUrl();
        }
        
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "No sample audio available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (samplePlayer != null) {
            samplePlayer.release();
        }

        samplePlayer = new MediaPlayer();
        try {
            android.util.Log.d("SpeakingActivity", "Playing audio: " + audioUrl);
            samplePlayer.setDataSource(audioUrl);
            samplePlayer.prepareAsync();
            samplePlayer.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(this, "Playing sample...", Toast.LENGTH_SHORT).show();
            });
            samplePlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                return false;
            });
        } catch (IOException e) {
            android.util.Log.e("SpeakingActivity", "Error setting audio source", e);
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                statusText.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {
                statusText.setText("Speaking...");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    analyzeResponse(spokenText);
                }
            }

            @Override
            public void onError(int error) {
                statusCard.setVisibility(View.GONE);
                recordButton.setVisibility(View.VISIBLE);
                Toast.makeText(SpeakingActivity.this, "Error recognizing speech", Toast.LENGTH_SHORT).show();
            }

            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        statusCard.setVisibility(View.VISIBLE);
        // recordButton.setVisibility(View.GONE); // Keep button for stop
        statusText.setText("Listening...");
        
        // Start Pulse Animation
        pulseView.setVisibility(View.VISIBLE);
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                pulseView,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.5f),
                PropertyValuesHolder.ofFloat("alpha", 0.5f, 0f));
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulseAnimator.start();
        isRecording = true;

        // Start speech recognition
        android.content.Intent intent = new android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        speechRecognizer.startListening(intent);
    }

    private void stopRecording() {
        isRecording = false;
        speechRecognizer.stopListening();
        
        // Stop Pulse
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        pulseView.setVisibility(View.INVISIBLE);
        statusCard.setVisibility(View.GONE);
    }

    /**
     * Analyze user's response and calculate scores
     */
    private void analyzeResponse(String spokenText) {
        String targetText = currentPrompt.getPromptText();

        // Simple scoring algorithm (can be enhanced)
        int pronScore = calculatePronunciationScore(spokenText, targetText);
        int fluScore = calculateFluencyScore(spokenText);

        displayScores(pronScore, fluScore);
        lesson.recordAttempt(pronScore, fluScore);
        saveProgress();
    }

    private int calculatePronunciationScore(String spoken, String target) {
        // Simple word matching (can use Levenshtein distance for better accuracy)
        String[] spokenWords = spoken.toLowerCase().split("\\s+");
        String[] targetWords = target.toLowerCase().split("\\s+");

        int matches = 0;
        for (String word : spokenWords) {
            for (String targetWord : targetWords) {
                if (word.equals(targetWord)) {
                    matches++;
                    break;
                }
            }
        }

        return Math.min(100, (matches * 100) / Math.max(targetWords.length, 1));
    }

    private int calculateFluencyScore(String spoken) {
        // Simple fluency based on word count and length
        int wordCount = spoken.split("\\s+").length;
        return Math.min(100, wordCount * 10);
    }

    private void displayScores(int pronScore, int fluScore) {
        statusCard.setVisibility(View.GONE);
        scoresCard.setVisibility(View.VISIBLE);
        recordButton.setVisibility(View.GONE);

        pronunciationScore.setText(pronScore + "%");
        fluencyScore.setText(fluScore + "%");

        // Award XP
        int xp = (pronScore + fluScore) / 2;
        progressManager.addXP(xp, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {}
            
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void completeLesson() {
        lesson.setCompleted(true);
        saveProgress();
        Toast.makeText(this, "Lesson completed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Save progress to Firebase
     */
    private void saveProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("speaking_progress").document(lessonId)
                .set(lesson);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (samplePlayer != null) {
            samplePlayer.release();
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
