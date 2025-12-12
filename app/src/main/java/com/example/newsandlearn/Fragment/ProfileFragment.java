package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.Activity.LoginActivity;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileLanguage, profileLevel;
    private TextView logoutButton;
    private TextView streakCount, totalDays;
    private TextView statNewsCount, statVocabCount, statTimeCount, statFavoriteCount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileName = view.findViewById(R.id.profile_name);
        profileLanguage = view.findViewById(R.id.profile_language);
        profileLevel = view.findViewById(R.id.profile_level);
        logoutButton = view.findViewById(R.id.logout_button);
        streakCount = view.findViewById(R.id.streak_count);
        totalDays = view.findViewById(R.id.total_days);
        statNewsCount = view.findViewById(R.id.stat_news_count);
        statVocabCount = view.findViewById(R.id.stat_vocab_count);
        statTimeCount = view.findViewById(R.id.stat_time_count);
        statFavoriteCount = view.findViewById(R.id.stat_favorite_count);

        // Setup logout button
        logoutButton.setOnClickListener(v -> logout());

        // Load user data
        loadUserData();

        return view;
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Load user info
                            String username = document.getString("username");
                            String level = document.getString("level");
                            Long streak = document.getLong("streak");
                            Long days = document.getLong("totalDays");

                            if (username != null)
                                profileName.setText(username);
                            if (level != null)
                                profileLevel.setText(level);
                            if (streak != null)
                                streakCount.setText(String.valueOf(streak));
                            if (days != null)
                                totalDays.setText(String.valueOf(days));

                            // TODO: Load statistics from Firebase
                            statNewsCount.setText("0");
                            statVocabCount.setText("0");
                            statTimeCount.setText("0h");
                            statFavoriteCount.setText("0");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void logout() {
        mAuth.signOut();

        // Navigate to login
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
