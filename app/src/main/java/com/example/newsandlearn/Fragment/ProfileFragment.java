package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Activity.LoginActivity;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Main containers
    // private SwipeRefreshLayout swipeRefresh; // Removed in new design
    private View shimmerLayout;
    private View contentLayout;

    // Views
    private ImageView profileAvatar;
    private View editAvatarButton;
    private View editProfileButton;
    private TextView profileName, profileEmail, profileLevel;
    private TextView xpText;
    private TextView currentStreak, todayActive;
    private TextView statVocabulary, statArticles;
    private ProgressBar circularProgress;
    
    // Cards for animation
    private CardView xpCard, actionsCard, settingsCard;

    // Action buttons
    private View actionVocabulary, actionQuiz, actionShare, actionHelp;
    private View settingsButton, logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload user data whenever returning to this fragment to show updated profile
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadUserData();
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initializeViews(view);
        
        // Setup click listeners
        setupClickListeners();
        
        // Show shimmer and load data
        showShimmer();
        loadUserData();

        return view;
    }

    private void initializeViews(View view) {
        // Containers
        // swipeRefresh = view.findViewById(R.id.swipe_refresh); // Not in new layout
        contentLayout = view.findViewById(R.id.scroll_view);
        
        // Header views
        profileAvatar = view.findViewById(R.id.profile_avatar);
        editAvatarButton = view.findViewById(R.id.edit_avatar_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileLevel = view.findViewById(R.id.profile_level);
        
        // XP views
        xpText = view.findViewById(R.id.xp_text);
        circularProgress = view.findViewById(R.id.circular_progress);
        
        // Streak views
        currentStreak = view.findViewById(R.id.current_streak);
        todayActive = view.findViewById(R.id.today_active);
        
        // Stats views
        statVocabulary = view.findViewById(R.id.stat_vocabulary);
        statArticles = view.findViewById(R.id.stat_articles);
        
        // Cards
        xpCard = view.findViewById(R.id.xp_card);
        view.findViewById(R.id.stats_card); // stats_card is a LinearLayout, not used for animation
        actionsCard = view.findViewById(R.id.actions_card);
        settingsCard = view.findViewById(R.id.settings_card);
        
        // Action buttons
        actionVocabulary = view.findViewById(R.id.action_vocabulary);
        actionQuiz = view.findViewById(R.id.action_quiz);
        actionShare = view.findViewById(R.id.action_share);
        actionHelp = view.findViewById(R.id.action_help);
        
        // Settings
        settingsButton = view.findViewById(R.id.settings_button);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void setupClickListeners() {
        // Edit Avatar and Profile
        View.OnClickListener editProfileClickListener = v -> {
            animateClick(v);
            if (currentUser != null) {
                EditProfileDialog dialog = EditProfileDialog.newInstance(
                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "",
                        currentUser.getEmail() != null ? currentUser.getEmail() : ""
                );
                dialog.show(getParentFragmentManager(), "EditProfileDialog");
            } else {
                Toast.makeText(getContext(), "❌ User not logged in", Toast.LENGTH_SHORT).show();
            }
        };

        if (editAvatarButton != null) {
            editAvatarButton.setOnClickListener(editProfileClickListener);
        }
        
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(editProfileClickListener);
        }
        
        // Learn Vocabulary
        if (actionVocabulary != null) {
            actionVocabulary.setOnClickListener(v -> {
                animateCardClick(v);
                Toast.makeText(getContext(), "📖 Learn New Vocabulary", Toast.LENGTH_SHORT).show();
                // TODO: Open vocabulary learning activity
            });
        }
        
        // Take Quiz
        if (actionQuiz != null) {
            actionQuiz.setOnClickListener(v -> {
                animateCardClick(v);
                Toast.makeText(getContext(), "✍️ Take Quiz", Toast.LENGTH_SHORT).show();
                // TODO: Open quiz activity
            });
        }
        
        // Share App
        if (actionShare != null) {
            actionShare.setOnClickListener(v -> {
                animateCardClick(v);
                shareApp();
            });
        }
        
        // Help & Support
        if (actionHelp != null) {
            actionHelp.setOnClickListener(v -> {
                animateCardClick(v);
                Toast.makeText(getContext(), "💬 Help & Support", Toast.LENGTH_SHORT).show();
                // TODO: Open help activity
            });
        }
        
        // Settings
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                animateClick(v);
                Toast.makeText(getContext(), "⚙️ App Settings", Toast.LENGTH_SHORT).show();
                // TODO: Open settings activity
            });
        }
        
        // Logout
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                animateClick(v);
                logout();
            });
        }
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this cool app!");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showShimmer() {
        // TODO: Implement shimmer effect
    }

    private void hideShimmer() {
        // Hide shimmer and show content with animations
        startEntranceAnimations();
    }

    private void loadUserData() {
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        // Set email from auth
        profileEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        // Load user data from Firestore
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    boolean avatarLoaded = false;
                    // Load from Firestore if document exists
                    if (document.exists()) {
                        profileName.setText(document.getString("username"));
                        profileLevel.setText(String.format("%s XP", document.getString("level")));
                        
                        String avatarUrl = document.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).circleCrop().into(profileAvatar);
                            avatarLoaded = true;
                        }
                    }

                    // Fallback to Firebase Auth data if not loaded from Firestore
                    if (!avatarLoaded && currentUser.getPhotoUrl() != null) {
                        Glide.with(this).load(currentUser.getPhotoUrl()).placeholder(R.drawable.ic_profile_placeholder).error(R.drawable.ic_profile_placeholder).circleCrop().into(profileAvatar);
                        avatarLoaded = true;
                    }
                    
                    // If still no avatar, use placeholder
                    if (!avatarLoaded) {
                        Glide.with(this).load(R.drawable.ic_profile_placeholder).circleCrop().into(profileAvatar);
                    }

                    // If document doesn't exist, use auth data for name
                    if (!document.exists()) {
                        profileName.setText(currentUser.getDisplayName());
                    }

                    hideShimmer();
                })
                .addOnFailureListener(e -> {
                    // On failure, still try to load from auth
                    profileName.setText(currentUser.getDisplayName());
                    profileEmail.setText(currentUser.getEmail());
                    if (currentUser.getPhotoUrl() != null) {
                        Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(profileAvatar);
                    } else {
                        Glide.with(this).load(R.drawable.ic_profile_placeholder).circleCrop().into(profileAvatar);
                    }
                    Toast.makeText(getContext(), "❌ Couldn't refresh profile", Toast.LENGTH_SHORT).show();
                    hideShimmer();
                });

        // Load other data modules...
        loadProgressData(userId);
        loadStreakData(userId);
        loadStatistics(userId);
    }

    private void loadProgressData(String userId) {
        // TODO: Implement
    }

    private void loadStreakData(String userId) {
        // TODO: Implement
    }

    private void loadStatistics(String userId) {
        // TODO: Implement
    }

    private void redirectToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void startEntranceAnimations() {
        // TODO: Implement
    }

    private void animateTextChange(TextView textView) {
        // TODO: Implement
    }

    private void animateClick(View view) {
        // TODO: Implement
    }

    private void animateCardClick(View view) {
        // TODO: Implement
    }
}
