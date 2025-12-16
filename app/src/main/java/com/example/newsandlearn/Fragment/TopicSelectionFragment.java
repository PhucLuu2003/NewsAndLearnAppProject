package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.Activity.MainActivity;
import com.example.newsandlearn.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicSelectionFragment extends Fragment {

    private ChipGroup topicChipGroup;
    private Button continueButton;
    private ProgressBar progressBar; // Recommended to show loading

    public TopicSelectionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_topic_selection, container, false);

        topicChipGroup = view.findViewById(R.id.topic_chip_group);
        continueButton = view.findViewById(R.id.continue_button_topic);
        progressBar = view.findViewById(R.id.progress_bar_topic);

        continueButton.setOnClickListener(v -> handleContinue());

        return view;
    }

    private void handleContinue() {
        List<String> selectedTopics = new ArrayList<>();

        // 1. Extract selected topics from Chips
        for (int i = 0; i < topicChipGroup.getChildCount(); i++) {
            View child = topicChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selectedTopics.add(chip.getText().toString());
                }
            }
        }

        // 2. Pro-Tip: Skip Logic
        // If nothing is selected, we treat it as a "Skip".
        // You can leave the list empty or add a default "General" category.
        saveTopicsToFirestore(selectedTopics);
    }

    private void saveTopicsToFirestore(List<String> topics) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, Object> data = new HashMap<>();
        data.put("interestedTopics", topics);
        // We use boolean flag to know the user finished onboarding
        data.put("hasCompletedOnboarding", true);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(data, SetOptions.merge()) // Use merge to avoid deleting name/email
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    navigateToAppActivity();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Error saving preferences: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void navigateToAppActivity() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            // Crucial: Clear the stack so user can't "back" into onboarding
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        continueButton.setEnabled(!isLoading);
    }
}