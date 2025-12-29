package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.WordResultAdapter;
import com.example.newsandlearn.Model.GameSession;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị kết quả sau khi chơi xong
 */
public class GameResultActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private MaterialButton playRecordingButton;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);

        // Get results from intent
        int score = getIntent().getIntExtra("SCORE", 0);
        float accuracy = getIntent().getFloatExtra("ACCURACY", 0);
        int maxCombo = getIntent().getIntExtra("MAX_COMBO", 0);
        int perfect = getIntent().getIntExtra("PERFECT", 0);
        int great = getIntent().getIntExtra("GREAT", 0);
        int good = getIntent().getIntExtra("GOOD", 0);
        int miss = getIntent().getIntExtra("MISS", 0);
        String rank = getIntent().getStringExtra("RANK");
        String audioFilePath = getIntent().getStringExtra("AUDIO_FILE_PATH");
        
        // Get hit results
        ArrayList<GameSession.HitResult> hitResults = 
            (ArrayList<GameSession.HitResult>) getIntent().getSerializableExtra("HIT_RESULTS");

        // Display results
        TextView rankText = findViewById(R.id.rank_text);
        TextView scoreText = findViewById(R.id.score_text);
        TextView accuracyText = findViewById(R.id.accuracy_text);
        TextView comboText = findViewById(R.id.combo_text);
        TextView perfectText = findViewById(R.id.perfect_text);
        TextView greatText = findViewById(R.id.great_text);
        TextView goodText = findViewById(R.id.good_text);
        TextView missText = findViewById(R.id.miss_text);

        rankText.setText(rank);
        scoreText.setText(String.valueOf(score));
        accuracyText.setText(String.format("%.0f%%", accuracy));
        comboText.setText("x" + maxCombo);
        perfectText.setText(perfect + " Perfect");
        greatText.setText(great + " Great");
        goodText.setText(good + " Good");
        missText.setText(miss + " Miss");

        // Setup word list RecyclerView
        RecyclerView wordsRecyclerView = findViewById(R.id.words_recycler_view);
        if (hitResults != null && !hitResults.isEmpty()) {
            WordResultAdapter adapter = new WordResultAdapter(hitResults);
            wordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            wordsRecyclerView.setAdapter(adapter);
        }

        // Setup audio playback
        MaterialCardView audioPlaybackCard = findViewById(R.id.audio_playback_card);
        playRecordingButton = findViewById(R.id.play_recording_button);
        
        if (audioFilePath != null && new File(audioFilePath).exists()) {
            audioPlaybackCard.setVisibility(View.VISIBLE);
            setupAudioPlayback(audioFilePath);
        } else {
            audioPlaybackCard.setVisibility(View.GONE);
        }

        // Buttons
        MaterialButton playAgainButton = findViewById(R.id.play_again_button);
        MaterialButton backButton = findViewById(R.id.back_button);

        playAgainButton.setOnClickListener(v -> {
            finish();
            // Restart game
            Intent intent = new Intent(this, PronunciationGameActivity.class);
            intent.putExtras(getIntent());
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupAudioPlayback(String audioFilePath) {
        playRecordingButton.setOnClickListener(v -> {
            if (isPlaying) {
                stopPlayback();
            } else {
                startPlayback(audioFilePath);
            }
        });
    }

    private void startPlayback(String audioFilePath) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            isPlaying = true;
            playRecordingButton.setText("⏸ Pause Recording");
            playRecordingButton.setIcon(getDrawable(android.R.drawable.ic_media_pause));
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playRecordingButton.setText("▶ Play Your Recording");
                playRecordingButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
            });
            
            Toast.makeText(this, "Playing your recording...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error playing recording: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
            android.util.Log.e("GameResult", "Error playing audio", e);
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            playRecordingButton.setText("▶ Play Your Recording");
            playRecordingButton.setIcon(getDrawable(android.R.drawable.ic_media_play));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
