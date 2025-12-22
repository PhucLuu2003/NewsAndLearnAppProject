package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;

/**
 * Activity hiển thị kết quả sau khi chơi xong
 */
public class GameResultActivity extends AppCompatActivity {

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
}
