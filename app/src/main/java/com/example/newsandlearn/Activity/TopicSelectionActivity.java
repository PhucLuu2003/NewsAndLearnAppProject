package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TopicSelectionActivity extends AppCompatActivity {

    private ChipGroup topicChipGroup;
    private Button confirmButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> selectedTopics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        selectedTopics = new ArrayList<>();

        // Initialize views
        topicChipGroup = findViewById(R.id.topic_chip_group);
        confirmButton = findViewById(R.id.confirm_button);

        // Setup chip listeners
        for (int i = 0; i < topicChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) topicChipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String topic = buttonView.getText().toString();
                if (isChecked) {
                    if (!selectedTopics.contains(topic)) {
                        selectedTopics.add(topic);
                    }
                } else {
                    selectedTopics.remove(topic);
                }
            });
        }

        // Confirm button
        confirmButton.setOnClickListener(v -> saveTopicsAndContinue());
    }

    private void saveTopicsAndContinue() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("topics", selectedTopics)
                    .addOnSuccessListener(aVoid -> {
                        // Navigate to main activity
                        Intent intent = new Intent(TopicSelectionActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
        }
    }
}
