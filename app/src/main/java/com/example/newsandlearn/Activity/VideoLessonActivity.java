package com.example.newsandlearn.Activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.newsandlearn.Utils.AnimationHelper;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.Fragment.DragDropQuestionFragment;
import com.example.newsandlearn.Fragment.MultipleChoiceQuestionFragment;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.ui.PlayerView;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activity for video lessons with interactive questions
 * Features:
 * - ExoPlayer integration for video playback
 * - Automatic pause at specific timestamps for questions
 * - Beautiful animations for question appearance/disappearance
 * - Feedback animations (correct/incorrect)
 * - Sound effects for user interactions
 */
public class VideoLessonActivity extends AppCompatActivity implements
        DragDropQuestionFragment.OnQuestionAnsweredListener,
        MultipleChoiceQuestionFragment.OnQuestionAnsweredListener {

    private static final String TAG = "VideoLessonActivity";

    private PlayerView playerView;
    private ExoPlayer player;
    private View videoOverlay;
    private android.view.View questionContainer;
    private FrameLayout questionFragmentContainer;
    private ImageView correctIcon, incorrectIcon, backButton;
    private ProgressBar loadingIndicator, lessonProgress;
    private TextView lessonTitle, lessonLevel, questionProgressText;
    private View progressContainer;

    private VideoLesson currentLesson;
    private List<Question> sortedQuestions;
    private int currentQuestionIndex = -1;
    private boolean isQuestionShowing = false;
    private Handler playbackHandler;
    private Runnable playbackRunnable;

    private MediaPlayer correctSound, incorrectSound;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_lesson);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        initializeSoundEffects();
        initializePlayer();
        loadLessonData();
        setupListeners();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.player_view);
        videoOverlay = findViewById(R.id.video_overlay);
        questionContainer = findViewById(R.id.question_container);
        questionFragmentContainer = findViewById(R.id.question_fragment_container);
        correctIcon = findViewById(R.id.correct_icon);
        incorrectIcon = findViewById(R.id.incorrect_icon);
        loadingIndicator = findViewById(R.id.loading_indicator);
        lessonTitle = findViewById(R.id.lesson_title);
        lessonLevel = findViewById(R.id.lesson_level);
        backButton = findViewById(R.id.back_button);
        progressContainer = findViewById(R.id.progress_container);
        questionProgressText = findViewById(R.id.question_progress);
        lessonProgress = findViewById(R.id.lesson_progress);

        // Force PlayerView visible
        playerView.setVisibility(View.VISIBLE);
        videoOverlay.setVisibility(View.GONE);
        videoOverlay.setAlpha(0f);
        Log.d(TAG, "initializeViews: PlayerView visibility=" + playerView.getVisibility());
    }

    private void initializeSoundEffects() {
        correctSound = MediaPlayer.create(this, R.raw.correct_sound);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect_sound);
    }

    private void initializePlayer() {
        Log.d(TAG, "initializePlayer: Creating ExoPlayer");
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        // ensure on-screen controls are enabled
        playerView.setUseController(true);
        playerView.setKeepScreenOn(true);
        player.setPlayWhenReady(true);
        Log.d(TAG, "initializePlayer: Player setup complete");

        // Add playback listener to check for question timestamps
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d(TAG, "onPlaybackStateChanged: state=" + playbackState);
                if (playbackState == Player.STATE_READY) {
                    Log.d(TAG, "Player is READY - starting playback");
                    loadingIndicator.setVisibility(View.GONE);
                    videoOverlay.setVisibility(View.GONE);

                    // Force PlayerView visible and request layout
                    playerView.setVisibility(View.VISIBLE);
                    playerView.requestLayout();
                    playerView.invalidate();
                    Log.d(TAG, "PlayerView forced visible, width=" + playerView.getWidth() + ", height="
                            + playerView.getHeight());

                    startPlaybackMonitoring();
                } else if (playbackState == Player.STATE_ENDED) {
                    onLessonCompleted();
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Log.d(TAG, "Player is BUFFERING");
                    loadingIndicator.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_IDLE) {
                    Log.d(TAG, "Player is IDLE");
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying && !isQuestionShowing) {
                    // Video paused but not by question - hide progress
                    progressContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                // Show a helpful message when ExoPlayer fails
                String msg = error == null || error.getMessage() == null ? "Lỗi phát video"
                        : ("Lỗi phát video: " + error.getMessage());
                Log.e(TAG, "onPlayerError: " + msg, error);

                // reveal overlay so user sees error state
                if (videoOverlay != null) {
                    videoOverlay.setVisibility(View.VISIBLE);
                    videoOverlay.setAlpha(0.6f);
                }
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(VideoLessonActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadLessonData() {
        loadingIndicator.setVisibility(View.VISIBLE);

        // Get lesson ID from intent
        String lessonId = getIntent().getStringExtra("lesson_id");
        Log.d(TAG, "loadLessonData: lessonId=" + lessonId);
        if (lessonId == null) {
            Log.e(TAG, "lessonId is null!");
            Toast.makeText(this, "Lỗi: Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load from Firestore
        db.collection("video_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Firestore success: exists=" + documentSnapshot.exists());
                    currentLesson = documentSnapshot.toObject(VideoLesson.class);
                    if (currentLesson != null) {
                        Log.d(TAG, "Lesson loaded: " + currentLesson.getTitle());
                        Log.d(TAG, "Video URL: " + currentLesson.getVideoUrl());
                        setupLesson();
                    } else {
                        Log.e(TAG, "currentLesson is null after toObject");
                        Toast.makeText(this, "Lỗi: Không thể tải bài học", Toast.LENGTH_SHORT).show();
                        loadingIndicator.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore failure: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingIndicator.setVisibility(View.GONE);
                    finish();
                });
    }

    private void setupLesson() {
        Log.d(TAG, "setupLesson: Starting");
        // Set lesson info
        lessonTitle.setText(currentLesson.getTitle());
        lessonLevel.setText(currentLesson.getLevel());

        // Validate video URL
        String videoUrl = currentLesson.getVideoUrl();
        Log.d(TAG, "setupLesson: videoUrl=" + videoUrl);

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Log.e(TAG, "Video URL is null or empty!");
            Toast.makeText(this, "Lỗi: URL video không hợp lệ hoặc trống", Toast.LENGTH_LONG).show();
            loadingIndicator.setVisibility(View.GONE);
            finish();
            return;
        }

        // Sort questions by appearance time
        sortedQuestions = new ArrayList<>(currentLesson.getQuestions());
        Collections.sort(sortedQuestions, Comparator.comparingInt(Question::getAppearAtSecond));

        // If the URL is a YouTube link, open externally because ExoPlayer cannot play
        // YouTube pages directly
        String lower = videoUrl.toLowerCase();
        if (lower.contains("youtube.com") || lower.contains("youtu.be")) {
            Log.d(TAG, "Detected YouTube URL - opening externally");
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open YouTube", e);
                Toast.makeText(this, "Không thể mở YouTube. Hãy kiểm tra URL.", Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }

        // Check if URL is valid HTTP/HTTPS
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            Log.e(TAG, "Invalid URL scheme: " + videoUrl);
            Toast.makeText(this, "Lỗi: URL phải bắt đầu bằng http:// hoặc https://", Toast.LENGTH_LONG).show();
            loadingIndicator.setVisibility(View.GONE);
            finish();
            return;
        }

        // Setup video
        Log.d(TAG, "Setting up MediaItem with URL: " + videoUrl);
        try {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            player.setMediaItem(mediaItem);
            Log.d(TAG, "Preparing player...");
            player.prepare();
            Log.d(TAG, "Starting playback...");
            // ensure playback starts
            player.play();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up player", e);
            Toast.makeText(this, "Lỗi khi load video: " + e.getMessage(), Toast.LENGTH_LONG).show();
            loadingIndicator.setVisibility(View.GONE);
        }

        // Update progress
        progressContainer.setVisibility(View.VISIBLE);
        updateQuestionProgress();
    }

    private void startPlaybackMonitoring() {
        playbackHandler = new Handler(Looper.getMainLooper());
        playbackRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isQuestionShowing) {
                    long currentPosition = player.getCurrentPosition() / 1000; // Convert to seconds
                    checkForQuestions((int) currentPosition);
                    updateLessonProgress();
                }
                playbackHandler.postDelayed(this, 500); // Check every 500ms
            }
        };
        playbackHandler.post(playbackRunnable);
    }

    private void checkForQuestions(int currentSecond) {
        if (currentQuestionIndex + 1 < sortedQuestions.size()) {
            Question nextQuestion = sortedQuestions.get(currentQuestionIndex + 1);
            if (currentSecond >= nextQuestion.getAppearAtSecond()) {
                showQuestion(nextQuestion);
            }
        }
    }

    private void showQuestion(Question question) {
        if (isQuestionShowing)
            return;

        isQuestionShowing = true;
        currentQuestionIndex++;
        updateQuestionProgress();

        // Pause video
        player.pause();

        // Show video overlay with fade-in animation
        videoOverlay.setVisibility(View.VISIBLE);
        videoOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // Load appropriate question fragment
        Fragment questionFragment;
        if (question.getType() == Question.QuestionType.DRAG_AND_DROP) {
            questionFragment = DragDropQuestionFragment.newInstance(question);
        } else {
            questionFragment = MultipleChoiceQuestionFragment.newInstance(question);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.question_fragment_container, questionFragment)
                .commit();

        // Slide up question container with smooth animation
        questionContainer.setVisibility(View.VISIBLE);
        questionContainer.animate()
                .translationY(0)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    private void hideQuestion() {
        // Slide down question container
        questionContainer.animate()
                .translationY(questionContainer.getHeight())
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    questionContainer.setVisibility(View.GONE);
                    isQuestionShowing = false;
                })
                .start();

        // Fade out video overlay
        videoOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> videoOverlay.setVisibility(View.GONE))
                .start();

        // Resume video after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (player != null) {
                player.play();
            }
        }, 600);
    }

    @Override
    public void onQuestionAnswered(boolean isCorrect) {
        if (isCorrect) {
            showCorrectFeedback();
        } else {
            showIncorrectFeedback();
        }
    }

    @Override
    public void onContinueClicked() {
        hideQuestion();
    }

    private void showCorrectFeedback() {
        // Play sound
        if (correctSound != null) {
            correctSound.start();
        }

        // Show checkmark with bounce animation
        correctIcon.setVisibility(View.VISIBLE);
        correctIcon.setScaleX(0f);
        correctIcon.setScaleY(0f);

        correctIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .setInterpolator(new BounceInterpolator())
                .withEndAction(() -> {
                    // Hold for a moment
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Fade out and scale down
                        correctIcon.animate()
                                .scaleX(0f)
                                .scaleY(0f)
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    correctIcon.setVisibility(View.GONE);
                                    correctIcon.setAlpha(1f);
                                })
                                .start();
                    }, 800);
                })
                .start();
    }

    private void showIncorrectFeedback() {
        // Play sound
        if (incorrectSound != null) {
            incorrectSound.start();
        }

        // Show X with shake animation
        incorrectIcon.setVisibility(View.VISIBLE);

        // Shake animation
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        incorrectIcon.startAnimation(shake);

        // Hide after animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            incorrectIcon.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        incorrectIcon.setVisibility(View.GONE);
                        incorrectIcon.setAlpha(1f);
                    })
                    .start();
        }, 1000);
    }

    private void updateQuestionProgress() {
        int totalQuestions = sortedQuestions != null ? sortedQuestions.size() : 0;
        int current = currentQuestionIndex + 1;
        questionProgressText.setText(String.format("Câu hỏi %d/%d",
                Math.min(current, totalQuestions), totalQuestions));
    }

    private void updateLessonProgress() {
        if (player != null && currentLesson != null) {
            long currentPos = player.getCurrentPosition() / 1000;
            int progress = (int) ((currentPos * 100) / currentLesson.getDuration());
            lessonProgress.setProgress(Math.min(progress, 100));
        }
    }

    private void onLessonCompleted() {
        Toast.makeText(this, "Hoàn thành bài học! 🎉", Toast.LENGTH_LONG).show();
        // TODO: Update user progress in Firebase
        finish();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            AnimationHelper.scaleUp(VideoLessonActivity.this, backButton);
            onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (player != null && player.getPlayWhenReady()) {
            player.play();
            Log.d(TAG, "Player resumed playback");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playbackHandler != null && playbackRunnable != null) {
            playbackHandler.removeCallbacks(playbackRunnable);
        }
        if (player != null) {
            player.release();
            player = null;
        }
        if (correctSound != null) {
            correctSound.release();
        }
        if (incorrectSound != null) {
            incorrectSound.release();
        }
    }
}