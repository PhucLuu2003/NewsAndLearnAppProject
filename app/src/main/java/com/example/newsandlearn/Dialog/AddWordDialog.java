package com.example.newsandlearn.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddWordDialog extends DialogFragment {

    private TextInputEditText wordInput, definitionInput, exampleInput;
    private ChipGroup levelChipGroup;
    private MaterialButton saveButton, cancelButton;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private OnWordAddedListener listener;

    public interface OnWordAddedListener {
        void onWordAdded();
    }

    public static AddWordDialog newInstance() {
        return new AddWordDialog();
    }

    public void setOnWordAddedListener(OnWordAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_word, null);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews(view);
        setupListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initializeViews(View view) {
        wordInput = view.findViewById(R.id.word_input);
        definitionInput = view.findViewById(R.id.definition_input);
        exampleInput = view.findViewById(R.id.example_input);
        levelChipGroup = view.findViewById(R.id.level_chip_group);
        saveButton = view.findViewById(R.id.save_button);
        cancelButton = view.findViewById(R.id.cancel_button);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveWord());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void saveWord() {
        String word = wordInput.getText().toString().trim();
        String definition = definitionInput.getText().toString().trim();
        String example = exampleInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(word)) {
            wordInput.setError("Please enter a word");
            wordInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(definition)) {
            definitionInput.setError("Please enter a definition");
            definitionInput.requestFocus();
            return;
        }

        // Get selected level
        String level = "beginner";
        int selectedId = levelChipGroup.getCheckedChipId();
        if (selectedId == R.id.chip_intermediate) {
            level = "intermediate";
        } else if (selectedId == R.id.chip_advanced) {
            level = "advanced";
        }

        // Save to Firebase
        saveToFirebase(word, definition, example, level);
    }

    private void saveToFirebase(String word, String definition, String example, String level) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> vocabularyData = new HashMap<>();
        vocabularyData.put("word", word);
        vocabularyData.put("definition", definition);
        vocabularyData.put("example", example);
        vocabularyData.put("level", level);
        vocabularyData.put("status", "new");
        vocabularyData.put("mastery", 0);
        vocabularyData.put("favorite", false);
        vocabularyData.put("addedAt", timestamp);
        vocabularyData.put("reviewCount", 0);
        vocabularyData.put("lastReviewed", null);
        vocabularyData.put("nextReview", null);

        // Show loading
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        db.collection("users").document(userId)
                .collection("vocabulary")
                .add(vocabularyData)
                .addOnSuccessListener(documentReference -> {
                    // Update progress
                    ProgressHelper.updateModuleProgress("vocabulary", 1);
                    
                    Toast.makeText(getContext(), "✅ Word added successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Notify listener
                    if (listener != null) {
                        listener.onWordAdded();
                    }
                    
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Save");
                });
    }
}
