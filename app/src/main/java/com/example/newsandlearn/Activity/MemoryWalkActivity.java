package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.MemoryPalace;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;

/**
 * Memory Walk Activity - Review words by walking through palace
 */
public class MemoryWalkActivity extends AppCompatActivity {

    private CardView roomDisplayCard;
    private TextView roomEmojiText, roomNameText;
    private TextView crazyStoryText, visualImageText;
    private EditText answerInput;
    private MaterialButton checkButton, nextButton;
    private TextView scoreText, progressText;

    private int currentRoomIndex = 0;
    private int correctAnswers = 0;
    private int totalRooms = 0;
    
    private String palaceId;
    private com.example.newsandlearn.Utils.MemoryPalaceFirebase firebase;
    private java.util.List<RoomData> roomsData = new java.util.ArrayList<>();
    
    // Room data class
    private static class RoomData {
        String emoji;
        String name;
        String word;
        String story;
        String visual;
        
        RoomData(String emoji, String name, String word, String story, String visual) {
            this.emoji = emoji;
            this.name = name;
            this.word = word;
            this.story = story;
            this.visual = visual;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_walk);

        palaceId = getIntent().getStringExtra("PALACE_ID");
        totalRooms = getIntent().getIntExtra("TOTAL_ROOMS", 5);
        
        firebase = new com.example.newsandlearn.Utils.MemoryPalaceFirebase();

        initializeViews();
        setupListeners();
        loadRoomsFromFirebase();
        animateEntrance();
    }

    private void initializeViews() {
        roomDisplayCard = findViewById(R.id.room_display_card);
        roomEmojiText = findViewById(R.id.room_emoji_large);
        roomNameText = findViewById(R.id.room_name_large);
        crazyStoryText = findViewById(R.id.crazy_story_text);
        visualImageText = findViewById(R.id.visual_image_large);
        answerInput = findViewById(R.id.answer_input);
        checkButton = findViewById(R.id.check_button);
        nextButton = findViewById(R.id.next_button);
        scoreText = findViewById(R.id.score_text);
        progressText = findViewById(R.id.progress_text);

        // Next button starts disabled
        nextButton.setEnabled(false);
    }

    private void setupListeners() {
        checkButton.setOnClickListener(v -> checkAnswer());
        nextButton.setOnClickListener(v -> nextRoom());
    }

    private void loadRoomsFromFirebase() {
        // Get palace info to know room names/emojis
        MemoryPalace palace;
        if (palaceId.contains("home")) {
            palace = MemoryPalace.createHomePalace();
        } else if (palaceId.contains("school")) {
            palace = MemoryPalace.createSchoolPalace();
        } else {
            palace = MemoryPalace.createMallPalace();
        }
        
        firebase.loadAllRooms(palaceId, totalRooms, roomsDataMap -> {
            runOnUiThread(() -> {
                // Build rooms data list
                roomsData.clear();
                for (int i = 0; i < totalRooms; i++) {
                    MemoryPalace.Room room = palace.getRooms().get(i);
                    
                    if (roomsDataMap.containsKey(i)) {
                        MemoryPalace.WordMemory wordMemory = roomsDataMap.get(i);
                        roomsData.add(new RoomData(
                            room.getEmoji(),
                            room.getName(),
                            wordMemory.getWord(),
                            wordMemory.getCrazyStory(),
                            wordMemory.getImageUrl()
                        ));
                    }
                }
                
                // Start with first room
                if (!roomsData.isEmpty()) {
                    loadRoom(0);
                } else {
                    Toast.makeText(this, "No words found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void loadRoom(int index) {
        if (index >= roomsData.size()) {
            showResults();
            return;
        }

        currentRoomIndex = index;
        RoomData room = roomsData.get(index);
        
        // Update UI
        roomEmojiText.setText(room.emoji);
        roomNameText.setText(room.name);
        crazyStoryText.setText(room.story);
        visualImageText.setText(room.visual);
        progressText.setText((index + 1) + "/" + roomsData.size());
        
        // Reset input
        answerInput.setText("");
        answerInput.setEnabled(true);
        
        // Reset buttons
        checkButton.setEnabled(true);
        checkButton.setText("✓ Check Answer");
        checkButton.setBackgroundTintList(getColorStateList(R.color.primary));
        nextButton.setEnabled(false);

        // Animate room entrance
        animateRoomEntrance();
    }

    private void checkAnswer() {
        String answer = answerInput.getText().toString().trim().toUpperCase();
        String correctWord = roomsData.get(currentRoomIndex).word.toUpperCase();

        if (answer.isEmpty()) {
            Toast.makeText(this, "Please enter your answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (answer.equals(correctWord)) {
            // Correct!
            correctAnswers++;
            showFeedback(true);
            checkButton.setText("✓ Correct!");
            checkButton.setBackgroundColor(getColor(R.color.success));
        } else {
            // Wrong
            showFeedback(false);
            checkButton.setText("✗ Wrong!");
            checkButton.setBackgroundColor(getColor(R.color.error));
        }

        answerInput.setEnabled(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(true); // Enable Next button
        
        scoreText.setText("Score: " + correctAnswers + "/" + (currentRoomIndex + 1));
    }

    private void showFeedback(boolean correct) {
        if (correct) {
            // Green flash
            roomDisplayCard.setCardBackgroundColor(getColor(R.color.success_light));
            Toast.makeText(this, "✓ Correct! " + roomsData.get(currentRoomIndex).word, Toast.LENGTH_SHORT).show();
            
            // Confetti animation
            visualImageText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(300)
                .withEndAction(() -> {
                    visualImageText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start();
                })
                .start();
        } else {
            // Red flash
            roomDisplayCard.setCardBackgroundColor(getColor(R.color.error_light));
            Toast.makeText(this, "✗ Wrong! Correct: " + roomsData.get(currentRoomIndex).word, Toast.LENGTH_LONG).show();
            
            // Shake animation
            roomDisplayCard.animate()
                .translationX(-25f)
                .setDuration(100)
                .withEndAction(() -> {
                    roomDisplayCard.animate()
                        .translationX(25f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            roomDisplayCard.animate()
                                .translationX(0f)
                                .setDuration(100)
                                .start();
                        })
                        .start();
                })
                .start();
        }

        // Reset color after delay
        new Handler().postDelayed(() -> {
            roomDisplayCard.setCardBackgroundColor(getColor(R.color.primary));
        }, 1000);
    }

    private void nextRoom() {
        // Slide out current room
        roomDisplayCard.animate()
            .alpha(0f)
            .translationX(-300f)
            .setDuration(300)
            .withEndAction(() -> {
                loadRoom(currentRoomIndex + 1);
                roomDisplayCard.setTranslationX(300f);
                roomDisplayCard.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .start();
            })
            .start();
    }

    private void showResults() {
        int totalRooms = roomsData.size();
        float accuracy = totalRooms > 0 ? (correctAnswers * 100f) / totalRooms : 0;
        String grade;
        
        if (accuracy >= 90) grade = "🏆 PERFECT!";
        else if (accuracy >= 70) grade = "⭐ GREAT!";
        else if (accuracy >= 50) grade = "👍 GOOD!";
        else grade = "📚 Keep Practicing!";

        Toast.makeText(this, 
            grade + "\nScore: " + correctAnswers + "/" + totalRooms + " (" + (int)accuracy + "%)", 
            Toast.LENGTH_LONG).show();

        new Handler().postDelayed(() -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }, 2000);
    }

    private void animateEntrance() {
        scoreText.setAlpha(0f);
        scoreText.animate().alpha(1f).setDuration(500).start();

        progressText.setAlpha(0f);
        new Handler().postDelayed(() -> {
            progressText.animate().alpha(1f).setDuration(500).start();
        }, 200);
    }

    private void animateRoomEntrance() {
        // Emoji bounce
        roomEmojiText.setScaleX(0f);
        roomEmojiText.setScaleY(0f);
        roomEmojiText.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .start();

        // Story fade in
        crazyStoryText.setAlpha(0f);
        new Handler().postDelayed(() -> {
            crazyStoryText.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 300);

        // Visual pop
        visualImageText.setAlpha(0f);
        visualImageText.setScaleX(0.5f);
        visualImageText.setScaleY(0.5f);
        new Handler().postDelayed(() -> {
            visualImageText.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start();
        }, 500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
