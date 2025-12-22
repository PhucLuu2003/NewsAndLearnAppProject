package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.MemoryPalace;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.CrazyImageGenerator;
import com.google.android.material.button.MaterialButton;

/**
 * Word Placement Activity - Place a word in a room with crazy story
 */
public class WordPlacementActivity extends AppCompatActivity {

    private TextView roomTitleText, roomEmojiText;
    private EditText wordInput, meaningInput;
    private LinearLayout storySuggestionsLayout;
    private CardView story1Card, story2Card, story3Card;
    private TextView story1Text, story2Text, story3Text;
    private TextView visualImageText;
    private MaterialButton generateButton, saveButton;
    
    private String palaceId, roomId, roomName, roomEmoji;
    private int roomPosition;
    private String selectedStory = "";
    private com.example.newsandlearn.Utils.MemoryPalaceFirebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_placement);

        palaceId = getIntent().getStringExtra("PALACE_ID");
        roomId = getIntent().getStringExtra("ROOM_ID");
        roomName = getIntent().getStringExtra("ROOM_NAME");
        roomEmoji = getIntent().getStringExtra("ROOM_EMOJI");
        roomPosition = getIntent().getIntExtra("ROOM_POSITION", 0);

        firebase = new com.example.newsandlearn.Utils.MemoryPalaceFirebase();

        initializeViews();
        setupListeners();
        loadExistingWord(); // Load if editing
        animateEntrance();
    }

    private void initializeViews() {
        roomTitleText = findViewById(R.id.room_title);
        roomEmojiText = findViewById(R.id.room_emoji_display);
        wordInput = findViewById(R.id.word_input);
        meaningInput = findViewById(R.id.meaning_input);
        storySuggestionsLayout = findViewById(R.id.story_suggestions_layout);
        story1Card = findViewById(R.id.story1_card);
        story2Card = findViewById(R.id.story2_card);
        story3Card = findViewById(R.id.story3_card);
        story1Text = findViewById(R.id.story1_text);
        story2Text = findViewById(R.id.story2_text);
        story3Text = findViewById(R.id.story3_text);
        visualImageText = findViewById(R.id.visual_image);
        generateButton = findViewById(R.id.generate_button);
        saveButton = findViewById(R.id.save_button);

        roomTitleText.setText(roomName);
        roomEmojiText.setText(roomEmoji);
        storySuggestionsLayout.setVisibility(View.GONE);
    }

    private void setupListeners() {
        generateButton.setOnClickListener(v -> generateStories());
        saveButton.setOnClickListener(v -> saveWord());
        
        setupStoryCardClick(story1Card, story1Text);
        setupStoryCardClick(story2Card, story2Text);
        setupStoryCardClick(story3Card, story3Text);
    }

    private void setupStoryCardClick(CardView card, TextView textView) {
        card.setOnClickListener(v -> {
            // Deselect all
            story1Card.setCardBackgroundColor(getColor(R.color.surface_primary));
            story2Card.setCardBackgroundColor(getColor(R.color.surface_primary));
            story3Card.setCardBackgroundColor(getColor(R.color.surface_primary));
            
            // Select this one
            card.setCardBackgroundColor(getColor(R.color.primary_light));
            selectedStory = textView.getText().toString();
            
            // Animate
            card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    card.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
            
            // Update visual
            updateVisualImage();
        });
    }

    private void generateStories() {
        String word = wordInput.getText().toString().trim();
        
        if (word.isEmpty()) {
            Toast.makeText(this, "Please enter a word first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate 3 crazy stories
        String[] stories = CrazyImageGenerator.generateStoryOptions(word, roomId, roomName);
        
        story1Text.setText(stories[0]);
        story2Text.setText(stories[1]);
        story3Text.setText(stories[2]);
        
        // Show suggestions with animation
        storySuggestionsLayout.setVisibility(View.VISIBLE);
        storySuggestionsLayout.setAlpha(0f);
        storySuggestionsLayout.setTranslationY(50f);
        storySuggestionsLayout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .start();
        
        // Auto-select first story
        story1Card.performClick();
    }

    private void updateVisualImage() {
        String word = wordInput.getText().toString().trim();
        String visual = CrazyImageGenerator.createVisualImage(word, selectedStory);
        visualImageText.setText(visual);
        
        // Animate visual
        visualImageText.setScaleX(0.5f);
        visualImageText.setScaleY(0.5f);
        visualImageText.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction(() -> {
                visualImageText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }

    private void saveWord() {
        String word = wordInput.getText().toString().trim();
        String meaning = meaningInput.getText().toString().trim();
        
        if (word.isEmpty() || meaning.isEmpty()) {
            Toast.makeText(this, "Please fill in word and meaning!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedStory.isEmpty()) {
            Toast.makeText(this, "Please generate and select a story!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create word memory
        String visual = com.example.newsandlearn.Utils.CrazyImageGenerator.createVisualImage(word, selectedStory);
        MemoryPalace.WordMemory wordMemory = new MemoryPalace.WordMemory(
            word, meaning, selectedStory, visual
        );

        // Save to Firebase
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        
        // Timeout handler (10 seconds)
        Handler timeoutHandler = new Handler();
        Runnable timeoutRunnable = () -> {
            runOnUiThread(() -> {
                saveButton.setEnabled(true);
                saveButton.setText("💾 Save to Room");
                Toast.makeText(this, "Save timeout! Check internet connection.", Toast.LENGTH_LONG).show();
            });
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000); // 10 second timeout
        
        firebase.saveWordToRoom(palaceId, roomPosition, wordMemory, 
            new com.example.newsandlearn.Utils.MemoryPalaceFirebase.OnSaveListener() {
                @Override
                public void onSuccess() {
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    runOnUiThread(() -> {
                        saveButton.setText("✓ Saved!");
                        saveButton.setBackgroundColor(getColor(R.color.success));
                        
                        new Handler().postDelayed(() -> {
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }, 1000);
                    });
                }

                @Override
                public void onFailure(String error) {
                    timeoutHandler.removeCallbacks(timeoutRunnable); // Cancel timeout
                    runOnUiThread(() -> {
                        saveButton.setEnabled(true);
                        saveButton.setText("💾 Save to Room");
                        
                        // Show detailed error
                        String errorMsg = "Failed to save: " + error;
                        if (error.contains("PERMISSION_DENIED")) {
                            errorMsg = "Permission denied! Please update Firebase rules.";
                        } else if (error.contains("UNAVAILABLE")) {
                            errorMsg = "No internet connection!";
                        }
                        
                        Toast.makeText(WordPlacementActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            });
    }

    private void loadExistingWord() {
        firebase.loadWordFromRoom(palaceId, roomPosition, 
            new com.example.newsandlearn.Utils.MemoryPalaceFirebase.OnLoadListener() {
                @Override
                public void onLoaded(MemoryPalace.WordMemory wordMemory) {
                    runOnUiThread(() -> {
                        // Pre-fill fields
                        wordInput.setText(wordMemory.getWord());
                        meaningInput.setText(wordMemory.getMeaning());
                        
                        // Show story
                        selectedStory = wordMemory.getCrazyStory();
                        story1Text.setText(selectedStory);
                        story1Card.setCardBackgroundColor(getColor(R.color.primary_light));
                        
                        // Show visual
                        visualImageText.setText(wordMemory.getImageUrl());
                        
                        // Show suggestions layout
                        storySuggestionsLayout.setVisibility(View.VISIBLE);
                        
                        // Change button text
                        saveButton.setText("✏️ Update Word");
                    });
                }

                @Override
                public void onEmpty() {
                    // New word - do nothing
                }

                @Override
                public void onFailure(String error) {
                    // Ignore - treat as new word
                }
            });
    }

    private void animateEntrance() {
        // Room emoji bounce
        roomEmojiText.setScaleX(0f);
        roomEmojiText.setScaleY(0f);
        roomEmojiText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start();

        // Title fade
        roomTitleText.setAlpha(0f);
        new Handler().postDelayed(() -> {
            roomTitleText.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 200);

        // Inputs slide up
        wordInput.setTranslationY(50f);
        wordInput.setAlpha(0f);
        new Handler().postDelayed(() -> {
            wordInput.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 400);

        meaningInput.setTranslationY(50f);
        meaningInput.setAlpha(0f);
        new Handler().postDelayed(() -> {
            meaningInput.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
