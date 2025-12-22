package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.MemoryPalace;
import com.example.newsandlearn.R;

/**
 * Palace Selection Activity - Choose or create a memory palace
 */
public class MemoryPalaceActivity extends AppCompatActivity {

    private CardView homeCard, schoolCard, mallCard;
    private TextView titleText, subtitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_palace);

        initializeViews();
        setupListeners();
        animateEntrance();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.title_text);
        subtitleText = findViewById(R.id.subtitle_text);
        homeCard = findViewById(R.id.home_palace_card);
        schoolCard = findViewById(R.id.school_palace_card);
        mallCard = findViewById(R.id.mall_palace_card);
    }

    private void setupListeners() {
        setupCardClick(homeCard, "HOME");
        setupCardClick(schoolCard, "SCHOOL");
        setupCardClick(mallCard, "MALL");
    }

    private void setupCardClick(CardView card, String palaceType) {
        card.setOnClickListener(v -> {
            // Press animation
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction(() -> openPalace(palaceType))
                        .start();
                })
                .start();
        });

        // Touch effect
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    break;
            }
            return false;
        });
    }

    private void openPalace(String palaceType) {
        Intent intent = new Intent(this, PalaceRoomsActivity.class);
        intent.putExtra("PALACE_TYPE", palaceType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void animateEntrance() {
        // Title fade in
        titleText.setAlpha(0f);
        titleText.setTranslationY(-50f);
        titleText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .start();

        // Subtitle fade in
        subtitleText.setAlpha(0f);
        subtitleText.setTranslationY(-30f);
        new Handler().postDelayed(() -> {
            subtitleText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .start();
        }, 200);

        // Cards staggered entrance
        animateCardEntrance(homeCard, 400);
        animateCardEntrance(schoolCard, 550);
        animateCardEntrance(mallCard, 700);
    }

    private void animateCardEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(100f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);

        new Handler().postDelayed(() -> {
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .start();
        }, delay);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
