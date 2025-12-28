package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.newsandlearn.Fragment.DragDropQuestionFragment;
import com.example.newsandlearn.Fragment.MultipleChoiceQuestionFragment;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.example.newsandlearn.View.QuestionMarkerProgressBar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activity for video lessons with ExoPlayer and interactive questions
 */
public class VideoLessonActivity extends AppCompatActivity implements
        DragDropQuestionFragment.OnQuestionAnsweredListener,
        MultipleChoiceQuestionFragment.OnQuestionAnsweredListener {

    private static final String TAG = "VideoLessonActivity";

    private PlayerView playerView;
    private ExoPlayer player;
    private WebView youtubeWebView;
    private boolean isYouTubePaused = false;
    private View videoOverlay;
    private View questionContainer;
    private FrameLayout questionFragmentContainer;
    private ImageView correctIcon, incorrectIcon, backButton;
    private ProgressBar loadingIndicator;
    private QuestionMarkerProgressBar lessonProgress;
    private TextView lessonTitle, lessonLevel, questionProgressText;
    private View progressContainer;

    private VideoLesson currentLesson;
    private List<Question> sortedQuestions;
    private int currentQuestionIndex = -1;
    private int questionsAnsweredCorrectly = 0;
    private boolean isQuestionShowing = false;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_lesson);

        // Disable WebRTC and media stream features to prevent camera access attempts
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting WebView debugging: " + e.getMessage());
        }

        db = FirebaseFirestore.getInstance();
        initializeViews();
        initializePlayer();
        loadLessonData();
        setupListeners();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.player_view);
        youtubeWebView = findViewById(R.id.youtube_webview);
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
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Add player listener for progress tracking and question triggering
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    loadingIndicator.setVisibility(View.GONE);
                    startProgressTracking();
                } else if (playbackState == Player.STATE_ENDED) {
                    // Video completed - launch completion activity
                    launchVideoCompletionActivity();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (!isPlaying) {
                    stopProgressTracking();
                }
            }
        });
    }

    private void launchVideoCompletionActivity() {
        if (currentLesson == null) return;
        
        long watchDuration = player != null ? player.getDuration() / 1000 : 0;
        int totalQuestions = sortedQuestions != null ? sortedQuestions.size() : 0;
        
        Intent intent = new Intent(this, VideoCompletionActivity.class);
        intent.putExtra("lessonId", getIntent().getStringExtra("lesson_id"));
        intent.putExtra("lessonTitle", currentLesson.getTitle());
        intent.putExtra("watchDuration", watchDuration);
        intent.putExtra("questionsAnswered", questionsAnsweredCorrectly);
        intent.putExtra("totalQuestions", totalQuestions);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null && player.isPlaying()) {
                long currentPosition = player.getCurrentPosition() / 1000; // Convert to seconds
                checkForQuestions((int) currentPosition);
                updateProgressBar(currentPosition);
                progressHandler.postDelayed(this, 500); // Check every 500ms
            }
        }
    };

    private void startProgressTracking() {
        progressHandler.post(progressRunnable);
    }

    private void stopProgressTracking() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void checkForQuestions(int currentSecond) {
        if (sortedQuestions == null || isQuestionShowing) return;

        for (int i = currentQuestionIndex + 1; i < sortedQuestions.size(); i++) {
            Question question = sortedQuestions.get(i);
            if (question.getAppearAtSecond() <= currentSecond) {
                showQuestion(question);
                break;
            }
        }
    }

    private void updateProgressBar(long currentSeconds) {
        if (player != null && lessonProgress != null) {
            long duration = player.getDuration() / 1000;
            if (duration > 0) {
                int progress = (int) ((currentSeconds * 100) / duration);
                lessonProgress.setProgress(progress);
            }
        }
    }

    private void loadLessonData() {
        loadingIndicator.setVisibility(View.VISIBLE);

        String lessonId = getIntent().getStringExtra("lesson_id");
        Log.d(TAG, "loadLessonData: lessonId=" + lessonId);

        if (lessonId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("video_lessons").document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentLesson = documentSnapshot.toObject(VideoLesson.class);
                    if (currentLesson != null) {
                        setupLesson();
                    } else {
                        Toast.makeText(this, "Lỗi: Không thể tải bài học", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupLesson() {
        lessonTitle.setText(currentLesson.getTitle());
        lessonLevel.setText(currentLesson.getLevel());

        String videoUrl = currentLesson.getVideoUrl();
        Log.d(TAG, "Video URL: " + videoUrl);

        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Toast.makeText(this, "Lỗi: URL video không hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Sort questions
        sortedQuestions = new ArrayList<>(currentLesson.getQuestions());
        Collections.sort(sortedQuestions, Comparator.comparingInt(Question::getAppearAtSecond));

        // Set question markers on progress bar
        if (lessonProgress != null && !sortedQuestions.isEmpty()) {
            List<Integer> timestamps = new ArrayList<>();
            for (Question q : sortedQuestions) {
                timestamps.add(q.getAppearAtSecond());
            }
            // Get video duration (will be set properly after video loads)
            int duration = currentLesson.getDuration() > 0 ? currentLesson.getDuration() : 300;
            lessonProgress.setQuestionMarkers(timestamps, duration);
        }

        // Load media based on URL type
        if (isYouTubeUrl(videoUrl)) {
            // Use WebView for YouTube
            showWebView();
            initializeWebView();
            loadYouTubeVideo(videoUrl);
        } else {
            // Use ExoPlayer for direct streams
            showPlayerView();
            loadVideo(videoUrl);
        }

        progressContainer.setVisibility(View.VISIBLE);
        updateQuestionProgress();
    }

    private void initializeWebView() {
        WebSettings settings = youtubeWebView.getSettings();
        
        // Enable JavaScript and DOM storage for YouTube player
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Enable hardware acceleration for smooth playback
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Additional settings to prevent media device access attempts
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        
        // Enable video playback
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        
        // Disable camera and microphone access to prevent errors
        youtubeWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(android.webkit.PermissionRequest request) {
                // Deny all permission requests (camera, microphone, etc.)
                Log.d(TAG, "Permission request denied: " + request.getResources().length + " resources");
                request.deny();
            }
            
            @Override
            public void onPermissionRequestCanceled(android.webkit.PermissionRequest request) {
                Log.d(TAG, "Permission request canceled");
            }
            
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    loadingIndicator.setVisibility(View.GONE);
                }
            }
        });
        
        youtubeWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Prevent opening external apps - keep everything in WebView
                if (url.contains("youtube.com") || url.contains("youtu.be")) {
                    return false; // Let WebView handle it
                }
                return true; // Block other URLs
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject JavaScript to disable getUserMedia (camera/mic access)
                String js = "navigator.mediaDevices.getUserMedia = undefined;" +
                           "navigator.getUserMedia = undefined;" +
                           "navigator.webkitGetUserMedia = undefined;" +
                           "navigator.mozGetUserMedia = undefined;";
                view.evaluateJavascript(js, null);
                loadingIndicator.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView error: " + description);
                Toast.makeText(VideoLessonActivity.this, 
                    "Lỗi tải video: " + description, Toast.LENGTH_SHORT).show();
            }
        });
        
        youtubeWebView.addJavascriptInterface(new WebAppInterface(), "Android");
    }

    private void loadYouTubeVideo(String url) {
        String videoId = extractYouTubeVideoId(url);
        if (videoId == null) {
            Toast.makeText(this, "Không thể đọc YouTube URL", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enhanced HTML with better YouTube iframe configuration
        String html = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>" +
                "<style>" +
                "* { margin:0; padding:0; box-sizing:border-box; }" +
                "body { margin:0; background:#000; overflow:hidden; }" +
                "#player { width:100%; height:100vh; border:none; }" +
                "iframe { border:0; }" +
                "</style>" +
                "</head><body>" +
                "<div id='player'></div>" +
                "<script src='https://www.youtube.com/iframe_api'></script>" +
                "<script>" +
                "var player;" +
                "function onYouTubeIframeAPIReady() {" +
                "  player = new YT.Player('player', {" +
                "    height: '100%'," +
                "    width: '100%'," +
                "    videoId: '" + videoId + "'," +
                "    playerVars: {" +
                "      'playsinline': 1," +
                "      'autoplay': 1," +
                "      'controls': 1," +
                "      'rel': 0," +
                "      'modestbranding': 1," +
                "      'fs': 1," +
                "      'enablejsapi': 1," +
                "      'origin': window.location.origin" +
                "    }," +
                "    events: {" +
                "      'onReady': function(e) { " +
                "        e.target.playVideo();" +
                "        Android.onVideoReady();" +
                "      }," +
                "      'onStateChange': function(e) {" +
                "        if (e.data == YT.PlayerState.PLAYING) {" +
                "          console.log('Video is playing');" +
                "        }" +
                "      }," +
                "      'onError': function(e) {" +
                "        console.error('YouTube error:', e.data);" +
                "      }" +
                "    }" +
                "  });" +
                "}" +
                "function pauseVideo() { if(player && player.pauseVideo) player.pauseVideo(); }" +
                "function playVideo() { if(player && player.playVideo) player.playVideo(); }" +
                "function getCurrentTime() { return player ? player.getCurrentTime() : 0; }" +
                "</script>" +
                "</body></html>";

        youtubeWebView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null);
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onVideoReady() {
            runOnUiThread(() -> loadingIndicator.setVisibility(View.GONE));
        }
    }

    private void loadVideo(String videoUrl) {
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private boolean isYouTubeUrl(String url) {
        if (url == null)
            return false;
        String lower = url.toLowerCase();
        return lower.contains("youtube.com") || lower.contains("youtu.be");
    }

    private String extractYouTubeVideoId(String url) {
        if (url == null)
            return null;
        try {
            // youtu.be/<id>
            if (url.contains("youtu.be/")) {
                String[] parts = url.split("youtu.be/");
                if (parts.length > 1) {
                    String id = parts[1];
                    int q = id.indexOf('?');
                    return q > -1 ? id.substring(0, q) : id;
                }
            }
            // youtube.com/watch?v=<id>
            int vIndex = url.indexOf("v=");
            if (vIndex > -1) {
                String id = url.substring(vIndex + 2);
                int amp = id.indexOf('&');
                return amp > -1 ? id.substring(0, amp) : id;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void showWebView() {
        playerView.setVisibility(View.GONE);
        youtubeWebView.setVisibility(View.VISIBLE);
    }

    private void showPlayerView() {
        youtubeWebView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
    }

    private void showQuestion(Question question) {
        if (isQuestionShowing)
            return;

        isQuestionShowing = true;
        currentQuestionIndex++;
        updateQuestionProgress();

        // Pause video
        if (playerView.getVisibility() == View.VISIBLE && player != null) {
            player.pause();
        } else if (youtubeWebView.getVisibility() == View.VISIBLE) {
            youtubeWebView.evaluateJavascript("pauseVideo();", null);
            isYouTubePaused = true;
        }

        // Show overlay
        videoOverlay.setVisibility(View.VISIBLE);
        videoOverlay.animate().alpha(1f).setDuration(300).start();

        // Load question fragment
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

        // Animate question container
        questionContainer.setVisibility(View.VISIBLE);
        questionContainer.animate()
                .translationY(0)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }

    private void hideQuestion() {
        questionContainer.animate()
                .translationY(questionContainer.getHeight())
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    questionContainer.setVisibility(View.GONE);
                    isQuestionShowing = false;
                })
                .start();

        videoOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> videoOverlay.setVisibility(View.GONE))
                .start();

        // Resume video
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (playerView.getVisibility() == View.VISIBLE && player != null) {
                player.play();
            } else if (youtubeWebView.getVisibility() == View.VISIBLE && isYouTubePaused) {
                youtubeWebView.evaluateJavascript("playVideo();", null);
                isYouTubePaused = false;
            }
        }, 600);
    }

    @Override
    public void onQuestionAnswered(boolean isCorrect) {
        if (isCorrect) {
            questionsAnsweredCorrectly++;
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
        correctIcon.setVisibility(View.VISIBLE);
        correctIcon.setScaleX(0f);
        correctIcon.setScaleY(0f);

        correctIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .setInterpolator(new BounceInterpolator())
                .withEndAction(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
        incorrectIcon.setVisibility(View.VISIBLE);

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

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            AnimationHelper.scaleUp(VideoLessonActivity.this, backButton);
            onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerView.getVisibility() == View.VISIBLE && player != null) {
            player.pause();
        }
        // YouTube player lifecycle handled automatically
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        if (youtubeWebView != null) {
            youtubeWebView.loadUrl("about:blank");
            youtubeWebView.stopLoading();
            youtubeWebView.clearHistory();
            youtubeWebView.removeAllViews();
            youtubeWebView.destroy();
        }
    }
}