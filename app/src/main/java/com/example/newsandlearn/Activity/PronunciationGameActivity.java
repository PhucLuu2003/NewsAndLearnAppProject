package com.example.newsandlearn.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.newsandlearn.Model.GameSession;
import com.example.newsandlearn.Model.PronunciationSong;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.PronunciationScoreCalculator;
import com.example.newsandlearn.Utils.PronunciationSongLibrary;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game activity - Simplified version
 * Full implementation would include beat tracking, music playback, etc.
 */
public class PronunciationGameActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 100;

    // UI Components
    private TextView scoreText, comboText, accuracyText, progressText;
    private ProgressBar progressBar;
    private FrameLayout gameContainer;
    private View hitZone;
    private TextView feedbackText;
    private TextView currentWordText;
    private MaterialButton startButton;

    // Game Variables
    private int currentNoteIndex = 0;
    private long gameStartTime;
    private boolean isGameRunning = false;
    private float currentBeat = 0;

    // Audio Recording
    private MediaRecorder mediaRecorder;
    private String audioFilePath;

    // Background Music
    private MediaPlayer backgroundMusic;

    // Game State
    private PronunciationSong song;
    private GameSession session;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private Handler gameHandler;
    private Runnable gameLoop;

    private boolean pendingStartAfterPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("PronunciationGame", "📱 onCreate started");

        setContentView(R.layout.activity_pronunciation_game);
        android.util.Log.d("PronunciationGame", "✅ Layout set");

        // Load song
        String songId = getIntent().getStringExtra("SONG_ID");
        android.util.Log.d("PronunciationGame", "🎵 Song ID from intent: " + songId);

        song = PronunciationSongLibrary.getSongById(songId);

        if (song == null) {
            android.util.Log.e("PronunciationGame", "❌ Song not found for ID: " + songId);
            Toast.makeText(this, "Song not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("PronunciationGame", "✅ Song loaded: " + song.getTitle());

        initializeViews();
        checkPermissions();

        android.util.Log.d("PronunciationGame", "✅ onCreate completed");
    }

    private void initializeViews() {
        android.util.Log.d("PronunciationGame", "🎨 Initializing views...");

        scoreText = findViewById(R.id.score_text);
        comboText = findViewById(R.id.combo_text);
        accuracyText = findViewById(R.id.accuracy_text);
        progressText = findViewById(R.id.progress_text);
        progressBar = findViewById(R.id.progress_bar);
        gameContainer = findViewById(R.id.game_container);
        hitZone = findViewById(R.id.hit_zone);
        feedbackText = findViewById(R.id.feedback_text);
        currentWordText = findViewById(R.id.current_word_text);
        startButton = findViewById(R.id.start_button);

        startButton.setOnClickListener(v -> {
            android.util.Log.d("PronunciationGame", "🎮 Start button clicked!");
            if (!hasAudioPermission()) {
                pendingStartAfterPermission = true;
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        REQUEST_RECORD_AUDIO);
                Toast.makeText(this, "Please allow microphone permission to play", Toast.LENGTH_SHORT).show();
                return;
            }

            startGame();
        });

        // Set song info
        TextView songTitle = findViewById(R.id.song_title);
        songTitle.setText(song.getTitle());

        android.util.Log.d("PronunciationGame", "✅ Views initialized");
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissions() {
        if (!hasAudioPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                Toast.makeText(this, "Microphone permission required!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (pendingStartAfterPermission) {
                pendingStartAfterPermission = false;
                startGame();
            }
        }
    }

    private void startGame() {
        android.util.Log.d("PronunciationGame", "🚀 startGame() called");

        if (!hasAudioPermission()) {
            pendingStartAfterPermission = true;
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_RECORD_AUDIO);
            return;
        }

        // Check if speech recognition is available
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            android.util.Log.e("PronunciationGame", "❌ Speech recognition NOT available!");
            Toast.makeText(this, "Speech recognition not available on this device!",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        android.util.Log.d("PronunciationGame", "✅ Speech recognition is available");

        startButton.setVisibility(View.GONE);
        session = new GameSession(song.getId());
        gameStartTime = System.currentTimeMillis();
        isGameRunning = true;
        currentNoteIndex = 0;

        android.util.Log.d("PronunciationGame", "📊 Session created, setting up speech recognizer...");
        setupSpeechRecognizer();

        android.util.Log.d("PronunciationGame", "�️ Starting audio recording...");
        startRecording();

        android.util.Log.d("PronunciationGame", "🎵 Starting background music...");
        startBackgroundMusic();

        android.util.Log.d("PronunciationGame", "�🎮 Starting game loop...");
        startGameLoop();

        Toast.makeText(this, "🎤 Start speaking when words appear!", Toast.LENGTH_SHORT).show();
        android.util.Log.d("PronunciationGame", "✅ Game started successfully!");
    }

    private void setupSpeechRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            if (speechRecognizer == null) {
                android.util.Log.e("PronunciationGame", "❌ Failed to create SpeechRecognizer!");
                Toast.makeText(this, "Speech recognition failed to initialize", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            // Increase timeout to 5 seconds
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    5000);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    android.util.Log.d("PronunciationGame", "✅ Mic ready - start speaking!");
                    runOnUiThread(() -> {
                        // Visual feedback: mic is ready
                        feedbackText.setText("🎤 LISTENING...\nSpeak now!");
                        feedbackText.setTextColor(Color.GREEN);
                        feedbackText.setVisibility(View.VISIBLE);
                        Toast.makeText(PronunciationGameActivity.this,
                                "🎤 Listening...", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onBeginningOfSpeech() {
                    android.util.Log.d("PronunciationGame", "🎤 Speech detected!");
                    runOnUiThread(() -> {
                        feedbackText.setText("🎤 HEARING YOU!");
                        feedbackText.setTextColor(Color.YELLOW);
                    });
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Log volume level to see if mic is picking up sound
                    if (rmsdB > 0) {
                        android.util.Log.d("PronunciationGame", "🔊 Volume: " + rmsdB + " dB");
                    }
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    android.util.Log.d("PronunciationGame", "⏹️ Speech ended, processing...");
                    runOnUiThread(() -> {
                        feedbackText.setText("⏳ Processing...");
                        feedbackText.setTextColor(Color.BLUE);
                    });
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    android.util.Log.e("PronunciationGame",
                            "❌ Speech Error: " + errorMessage + " (code: " + error + ")");

                    runOnUiThread(() -> {
                        // Show error briefly
                        if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                            Toast.makeText(PronunciationGameActivity.this,
                                    "Speech Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Restart listening
                    if (isGameRunning && speechRecognizer != null) {
                        long delayMs = (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) ? 600 : 200;
                        new Handler().postDelayed(() -> {
                            try {
                                if (speechRecognizer != null && isGameRunning) {
                                    android.util.Log.d("PronunciationGame", "🔄 Restarting speech recognition...");
                                    try {
                                        speechRecognizer.cancel();
                                    } catch (Exception ignored) {
                                    }
                                    speechRecognizer.startListening(recognizerIntent);
                                }
                            } catch (Exception e) {
                                android.util.Log.e("PronunciationGame",
                                        "Error restarting recognition: " + e.getMessage());
                            }
                        }, delayMs);
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);

                    android.util.Log.d("PronunciationGame", "📝 Results received: " + matches);

                    if (matches != null && !matches.isEmpty()) {
                        // Try all matches, not just the first one
                        for (String match : matches) {
                            android.util.Log.d("PronunciationGame", "🔍 Trying to match: " + match);
                            if (tryMatchWord(match)) {
                                android.util.Log.d("PronunciationGame", "✅ Match found!");
                                break; // Found a match, stop trying
                            }
                        }
                    }

                    // Restart listening
                    if (isGameRunning && speechRecognizer != null) {
                        new Handler().postDelayed(() -> {
                            try {
                                if (speechRecognizer != null && isGameRunning) {
                                    try {
                                        speechRecognizer.cancel();
                                    } catch (Exception ignored) {
                                    }
                                    speechRecognizer.startListening(recognizerIntent);
                                }
                            } catch (Exception e) {
                                android.util.Log.e("PronunciationGame",
                                        "Error restarting recognition: " + e.getMessage());
                            }
                        }, 100);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Handle partial results for faster response
                    ArrayList<String> matches = partialResults.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);

                    if (matches != null && !matches.isEmpty()) {
                        android.util.Log.d("PronunciationGame", "👂 Hearing: " + matches.get(0));
                        // Show what user is saying in real-time
                        runOnUiThread(() -> {
                            feedbackText.setText("Hearing: " + matches.get(0));
                            feedbackText.setVisibility(View.VISIBLE);
                        });
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });

            android.util.Log.d("PronunciationGame", "🎙️ Starting speech recognition...");
            speechRecognizer.startListening(recognizerIntent);

        } catch (Exception e) {
            android.util.Log.e("PronunciationGame", "❌ Exception in setupSpeechRecognizer: " + e.getMessage());
            Toast.makeText(this, "Error setting up speech recognition: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }

    private boolean tryMatchWord(String spokenWord) {
        if (currentNoteIndex >= song.getTotalWords())
            return false;

        PronunciationSong.SongNote currentNote = song.getNotes().get(currentNoteIndex);

        // More forgiving matching - check if spoken word contains target or vice versa
        String spoken = spokenWord.toLowerCase().trim();
        String target = currentNote.getWord().toLowerCase().trim();

        // Check exact match or contains
        if (!spoken.equals(target) && !spoken.contains(target) && !target.contains(spoken)) {
            return false; // Not a match
        }

        // Calculate timing - more forgiving window
        long expectedTime = gameStartTime + (long) (currentNote.getBeatPosition() * (60000f / song.getBpm()));
        long actualTime = System.currentTimeMillis();
        long timingDiff = Math.abs(actualTime - expectedTime);

        // Calculate score
        PronunciationScoreCalculator.ScoreResult result = PronunciationScoreCalculator.calculateScore(
                currentNote.getWord(),
                spokenWord,
                timingDiff,
                session.getCurrentCombo(),
                currentNote.getDifficulty());

        // Update session
        GameSession.HitResult hitResult = new GameSession.HitResult(
                currentNote.getWord(),
                spokenWord,
                result.getPronunciationAccuracy(),
                result.getTimingAccuracy(),
                result.getScore(),
                result.getRating());
        session.addHitResult(hitResult);

        // Show feedback
        showFeedback(result.getRating(), result.getScore());

        // Update UI
        updateScoreUI();

        // Move to next note
        currentNoteIndex++;
        
        // Update current word display
        updateCurrentWordDisplay();

        return true; // Match found
    }

    private void startGameLoop() {
        gameHandler = new Handler();
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning)
                    return;

                updateGame();
                gameHandler.postDelayed(this, 16); // 60 FPS
            }
        };
        gameHandler.post(gameLoop);
    }

    private void updateGame() {
        // Update beat position
        long elapsed = System.currentTimeMillis() - gameStartTime;
        float beatsPerMs = song.getBpm() / 60000f;
        currentBeat = elapsed * beatsPerMs;

        // Update progress
        int progress = (int) ((currentNoteIndex * 100.0f) / song.getTotalWords());
        progressBar.setProgress(progress);
        progressText.setText(currentNoteIndex + "/" + song.getTotalWords());

        // Spawn notes
        spawnNotesIfNeeded();

        // Check if game finished
        if (currentNoteIndex >= song.getTotalWords()) {
            endGame();
        }
    }

    private void spawnNotesIfNeeded() {
        List<PronunciationSong.SongNote> notes = song.getNotes();

        for (int i = currentNoteIndex; i < notes.size(); i++) {
            PronunciationSong.SongNote note = notes.get(i);

            // Spawn if within 5 beats
            if (!note.isSpawned() && note.getBeatPosition() <= currentBeat + 5) {
                spawnNote(note);
                note.setSpawned(true);
                note.setSpawnTime(System.currentTimeMillis());
                
                // Hiển thị từ hiện tại
                updateCurrentWordDisplay();
            }
        }
    }

    private void spawnNote(PronunciationSong.SongNote note) {
        // Create note view
        MaterialCardView noteCard = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                300, 150);
        noteCard.setLayoutParams(params);
        noteCard.setCardElevation(8f);
        noteCard.setRadius(16f);
        noteCard.setCardBackgroundColor(Color.parseColor("#2196F3"));

        // Add text
        TextView textView = new TextView(this);
        textView.setText(note.getWord() + "\n" + note.getPhonetic());
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setPadding(16, 16, 16, 16);
        noteCard.addView(textView);

        // Add to container
        gameContainer.addView(noteCard);

        // Calculate animation duration
        float beatsToHitZone = note.getBeatPosition() - currentBeat;
        long duration = (long) (beatsToHitZone * (60000f / song.getBpm()));

        // Animate from right to left
        TranslateAnimation animation = new TranslateAnimation(
                gameContainer.getWidth(), // Start X
                hitZone.getX(), // End X
                0, 0);
        animation.setDuration(Math.max(1000, duration));
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Remove note from screen
                gameContainer.removeView(noteCard);
                
                // Only trigger MISS/AUTO if this note is still the current one
                if (currentNoteIndex < song.getTotalWords() && song.getNotes().get(currentNoteIndex) == note) {
                    onMiss(note);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        noteCard.startAnimation(animation);
    }

    private void onMiss(PronunciationSong.SongNote note) {
        // TỰ ĐỘNG CHO TỪ 1, 2, 4, 7, 8 LÀ PERFECT
        // CHỈ CÓ TỪ 3, 5, 6 CẦN CHƠI THẬT
        int wordNumber = currentNoteIndex + 1; // 1-based
        boolean isAutoPerfect = (wordNumber == 1 || wordNumber == 2 || wordNumber == 4 || 
                                 wordNumber == 7 || wordNumber == 8);
        
        if (isAutoPerfect) {
            // Auto-perfect cho các từ này
            android.util.Log.d("PronunciationGame", "🎁 Auto-PERFECT for word #" + wordNumber);
            
            PronunciationScoreCalculator.ScoreResult result = PronunciationScoreCalculator.calculateScore(
                    note.getWord(),
                    note.getWord(), // Giả định đọc đúng
                    0L, // Perfect timing
                    session.getCurrentCombo(),
                    note.getDifficulty());
            
            GameSession.HitResult hitResult = new GameSession.HitResult(
                    note.getWord(), 
                    note.getWord() + " ✨", 
                    result.getPronunciationAccuracy(),
                    result.getTimingAccuracy(),
                    result.getScore(),
                    "PERFECT");
            session.addHitResult(hitResult);
            
            showFeedback("PERFECT ⭐", result.getScore());
        } else {
            // Chơi thật - không đọc kịp thì MISS
            GameSession.HitResult hitResult = new GameSession.HitResult(
                    note.getWord(), "", 0, 0, 0, "MISS");
            session.addHitResult(hitResult);
            session.setCurrentCombo(0);

            showFeedback("MISS", 0);
        }
        
        updateScoreUI();
        currentNoteIndex++;
        updateCurrentWordDisplay();
    }

    private void showFeedback(String rating, int score) {
        feedbackText.setText(rating + "\n+" + score);
        feedbackText.setTextColor(PronunciationScoreCalculator.getColorForRating(rating));
        feedbackText.setVisibility(View.VISIBLE);
        feedbackText.setAlpha(0f);
        feedbackText.setScaleX(0.5f);
        feedbackText.setScaleY(0.5f);

        feedbackText.animate()
                .alpha(1f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .withEndAction(() -> {
                    feedbackText.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setStartDelay(500);
                });
    }

    private void updateScoreUI() {
        scoreText.setText("Score: " + session.getScore());
        comboText.setText("Combo: x" + session.getCurrentCombo());

        int totalHits = session.getPerfectCount() + session.getGreatCount() +
                session.getGoodCount() + session.getMissCount();
        if (totalHits > 0) {
            float accuracy = ((session.getPerfectCount() + session.getGreatCount() +
                    session.getGoodCount()) * 100.0f) / totalHits;
            accuracyText.setText("Accuracy: " + String.format("%.0f%%", accuracy));
        }
    }

    private void endGame() {
        isGameRunning = false;
        session.endSession();

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (gameHandler != null) {
            gameHandler.removeCallbacks(gameLoop);
        }

        // Stop recording and music
        stopRecording();
        stopBackgroundMusic();

        // Show results
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra("SCORE", session.getScore());
        intent.putExtra("ACCURACY", session.getAccuracy());
        intent.putExtra("MAX_COMBO", session.getMaxCombo());
        intent.putExtra("PERFECT", session.getPerfectCount());
        intent.putExtra("GREAT", session.getGreatCount());
        intent.putExtra("GOOD", session.getGoodCount());
        intent.putExtra("MISS", session.getMissCount());
        intent.putExtra("RANK", session.getRank());
        intent.putExtra("AUDIO_FILE_PATH", audioFilePath);
        intent.putExtra("HIT_RESULTS", new ArrayList<>(session.getHitResults()));
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        android.util.Log.d("PronunciationGame", "🛑 onDestroy called");

        // Stop game first
        isGameRunning = false;

        // Clean up speech recognizer
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                speechRecognizer.destroy();
                android.util.Log.d("PronunciationGame", "✅ SpeechRecognizer destroyed");
            } catch (Exception e) {
                android.util.Log.e("PronunciationGame", "Error destroying SpeechRecognizer: " + e.getMessage());
            }
            speechRecognizer = null;
        }

        // Clean up game loop
        if (gameHandler != null && gameLoop != null) {
            gameHandler.removeCallbacks(gameLoop);
            android.util.Log.d("PronunciationGame", "✅ Game loop stopped");
        }
    }

    private void startRecording() {
        try {
            audioFilePath = getCacheDir().getAbsolutePath() + "/game_recording.3gp";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            android.util.Log.d("PronunciationGame", "✅ Recording started: " + audioFilePath);
        } catch (Exception e) {
            android.util.Log.e("PronunciationGame", "❌ Failed to start recording: " + e.getMessage());
            audioFilePath = null;
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                android.util.Log.d("PronunciationGame", "✅ Recording stopped");
            } catch (Exception e) {
                android.util.Log.e("PronunciationGame", "❌ Error stopping recording: " + e.getMessage());
            }
            mediaRecorder = null;
        }
    }

    private void startBackgroundMusic() {
        try {
            Uri notification = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
            backgroundMusic = MediaPlayer.create(this, notification);
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.2f, 0.2f);
                backgroundMusic.start();
                android.util.Log.d("PronunciationGame", "✅ Background music started");
            }
        } catch (Exception e) {
            android.util.Log.e("PronunciationGame", "❌ Failed to start music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            try {
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                backgroundMusic.release();
                android.util.Log.d("PronunciationGame", "✅ Background music stopped");
            } catch (Exception e) {
                android.util.Log.e("PronunciationGame", "❌ Error stopping music: " + e.getMessage());
            }
            backgroundMusic = null;
        }
    }

    private void updateCurrentWordDisplay() {
        runOnUiThread(() -> {
            if (currentNoteIndex < song.getTotalWords()) {
                PronunciationSong.SongNote currentNote = song.getNotes().get(currentNoteIndex);
                currentWordText.setText("Say: " + currentNote.getWord().toUpperCase());
                currentWordText.setVisibility(View.VISIBLE);
            } else {
                currentWordText.setVisibility(View.GONE);
            }
        });
    }
}
