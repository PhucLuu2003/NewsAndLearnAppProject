package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LevelSelectionActivity extends AppCompatActivity {

    private TextView titleText;
    private Button levelA1Button, levelA2Button, levelB1Button, levelB2Button,
            levelC1Button, levelC2Button;
    private String selectedLevel = "A1";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        titleText = findViewById(R.id.title_level_selection);
        levelA1Button = findViewById(R.id.level_a1_button);
        levelA2Button = findViewById(R.id.level_a2_button);
        levelB1Button = findViewById(R.id.level_b1_button);
        levelB2Button = findViewById(R.id.level_b2_button);
        levelC1Button = findViewById(R.id.level_c1_button);
        levelC2Button = findViewById(R.id.level_c2_button);

        // Set click listeners
        levelA1Button.setOnClickListener(v -> selectLevel("A1"));
        levelA2Button.setOnClickListener(v -> selectLevel("A2"));
        levelB1Button.setOnClickListener(v -> selectLevel("B1"));
        levelB2Button.setOnClickListener(v -> selectLevel("B2"));
        levelC1Button.setOnClickListener(v -> selectLevel("C1"));
        levelC2Button.setOnClickListener(v -> selectLevel("C2"));
    }

    private void selectLevel(String level) {
        selectedLevel = level;

        // Update user level in Firestore
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("level", level)
                    .addOnSuccessListener(aVoid -> {
                        // Navigate to topic selection
                        Intent intent = new Intent(LevelSelectionActivity.this, TopicSelectionActivity.class);
                        startActivity(intent);
                        finish();
                    });
        }
    }
}
