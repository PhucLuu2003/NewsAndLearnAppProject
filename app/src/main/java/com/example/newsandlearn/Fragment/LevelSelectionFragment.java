package com.example.newsandlearn.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LevelSelectionFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private AlertDialog loadingDialog;

    public LevelSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_level_selection, container, false);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupLoadingDialog();

        // Buttons
        setupLevelButton(view, R.id.level_a1_button, "A1");
        setupLevelButton(view, R.id.level_a2_button, "A2");
        setupLevelButton(view, R.id.level_b1_button, "B1");
        setupLevelButton(view, R.id.level_b2_button, "B2");
        setupLevelButton(view, R.id.level_c1_button, "C1");
        setupLevelButton(view, R.id.level_c2_button, "C2");

        return view;
    }

    private void setupLevelButton(View root, int buttonId, String level) {
        Button button = root.findViewById(buttonId);
        button.setOnClickListener(v -> saveLevelAndContinue(level));
    }

    private void saveLevelAndContinue(String level) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> update = new HashMap<>();
        update.put("level", level);

        firestore.collection("Users")
                .document(uid)
                .update(update)
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismiss();

                    // Go to TopicSelectionFragment
                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.onboarding_container, new TopicSelectionFragment())
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }
}
