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
    private CardView xpCard, statsCard, actionsCard, settingsCard;
    
    // Action buttons
    private View actionVocabulary, actionQuiz, actionShare, actionHelp;
    private View settingsButton, logoutButton;

    public ProfileFragment() {
        // Required empty public constructor
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
        statsCard = view.findViewById(R.id.stats_card);
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

    /* Removed - SwipeRefreshLayout not in new design
    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            // Set color scheme
            swipeRefresh.setColorSchemeResources(
                R.color.purple_500,
                R.color.purple_700,
                R.color.blue_500
            );
            
            // Set refresh listener
            swipeRefresh.setOnRefreshListener(() -> {
                // Reload all data
                loadUserData();
                
                // Stop refreshing after 2 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), "✅ Profile refreshed!", Toast.LENGTH_SHORT).show();
                }, 2000);
            });
        }
    }
    */

    private void setupClickListeners() {
        // Edit Avatar
        if (editAvatarButton != null) {
            editAvatarButton.setOnClickListener(v -> {
                animateClick(v);
                Toast.makeText(getContext(), "📸 Change Avatar", Toast.LENGTH_SHORT).show();
                // TODO: Implement avatar picker
            });
        }
        
        // Edit Profile
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                animateClick(v);
                Toast.makeText(getContext(), "✏️ Edit Profile", Toast.LENGTH_SHORT).show();
                // TODO: Open edit profile activity
            });
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

    private void showShimmer() {
        // TODO: Implement shimmer effect
        // For now, just show a loading state
    }

    private void hideShimmer() {
        // Hide shimmer and show content with animations
        startEntranceAnimations();
    }

    private void loadUserData() {
        if (currentUser == null) {
            // User not logged in, redirect to login
            redirectToLogin();
            return;
        }

        // Set email
        String email = currentUser.getEmail();
        if (email != null && profileEmail != null) {
            profileEmail.setText(email);
        }

        // Load user data from Firestore
        String userId = currentUser.getUid();
        
        // Load basic user info
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("username");
                        String level = document.getString("level");
                        
                        if (username != null && profileName != null) {
                            profileName.setText(username);
                            animateTextChange(profileName);
                        }
                        
                        if (level != null && profileLevel != null) {
                            profileLevel.setText(level + " XP");
                        } else if (profileLevel != null) {
                            profileLevel.setText("0 XP");
                        }
                    } else {
                        // Set default values
                        if (profileName != null) profileName.setText("User");
                        if (profileLevel != null) profileLevel.setText("0 XP");
                    }
                    
                    // Hide shimmer after data loaded
                    hideShimmer();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "❌ Error loading profile", Toast.LENGTH_SHORT).show();
                    }
                    hideShimmer();
                });

        // Load progress data
        loadProgressData(userId);
        
        // Load streak data
        loadStreakData(userId);
        
        // Load statistics
        loadStatistics(userId);
    }

    private void loadProgressData(String userId) {
        db.collection("users").document(userId)
                .collection("progress").document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long currentXP = document.getLong("currentLevelXP");
                        Long targetXP = document.getLong("xpForNextLevel");
                        
                        if (currentXP != null && targetXP != null) {
                            int progress = (int) ((currentXP * 100) / targetXP);
                            
                            if (xpText != null) {
                                xpText.setText(currentXP + " / " + targetXP + " XP");
                                animateTextChange(xpText);
                            }
                            
                            // Animate progress bar
                            if (circularProgress != null) {
                                animateProgressBar(circularProgress, progress);
                            }
                        }
                    } else {
                        // Set default progress
                        if (xpText != null) xpText.setText("0 / 100 XP");
                        if (circularProgress != null) circularProgress.setProgress(0);
                    }
                })
                .addOnFailureListener(e -> {
                    if (xpText != null) xpText.setText("0 / 100 XP");
                });
    }

    private void loadStreakData(String userId) {
        db.collection("users").document(userId)
                .collection("progress").document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long current = document.getLong("currentStreak");
                        
                        if (current != null && currentStreak != null) {
                            currentStreak.setText(String.valueOf(current));
                            animateTextChange(currentStreak);
                        }
                    } else {
                        // Set default values
                        if (currentStreak != null) currentStreak.setText("0");
                    }
                })
                .addOnFailureListener(e -> {
                    if (currentStreak != null) currentStreak.setText("0");
                });
    }

    private void loadStatistics(String userId) {
        // Load vocabulary count
        db.collection("users").document(userId)
                .collection("vocabulary")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    if (statVocabulary != null) {
                        statVocabulary.setText(String.valueOf(count));
                        animateTextChange(statVocabulary);
                    }
                })
                .addOnFailureListener(e -> {
                    if (statVocabulary != null) statVocabulary.setText("0");
                });

        // Load articles read count (lessons completed)
        db.collection("users").document(userId)
                .collection("reading_progress")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    if (statArticles != null) {
                        statArticles.setText(String.valueOf(count));
                        animateTextChange(statArticles);
                    }
                })
                .addOnFailureListener(e -> {
                    if (statArticles != null) statArticles.setText("0");
                });
    }

    private void startEntranceAnimations() {
        if (getContext() == null) return;

        // Animate XP card first
        if (xpCard != null) {
            xpCard.setAlpha(0f);
            Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            xpCard.startAnimation(slideUp);
            xpCard.setAlpha(1f);
        }

        // Animate stats card
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (statsCard != null && getContext() != null) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                statsCard.startAnimation(slideUp);
                statsCard.setAlpha(1f);
            }
        }, 150);

        // Animate actions card
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (actionsCard != null && getContext() != null) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                actionsCard.startAnimation(slideUp);
                actionsCard.setAlpha(1f);
            }
        }, 300);

        // Animate settings card
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (settingsCard != null && getContext() != null) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                settingsCard.startAnimation(slideUp);
                settingsCard.setAlpha(1f);
            }
        }, 450);

        // Animate avatar with scale
        if (profileAvatar != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getContext() != null) {
                    Animation scaleIn = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
                    profileAvatar.startAnimation(scaleIn);
                }
            }, 100);
        }
    }

    private void animateClick(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateCardClick(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void animateTextChange(TextView textView) {
        if (textView == null) return;
        
        // Pulse animation
        AlphaAnimation pulse = new AlphaAnimation(0.4f, 1.0f);
        pulse.setDuration(400);
        textView.startAnimation(pulse);
    }

    private void animateProgressBar(ProgressBar progressBar, int targetProgress) {
        if (progressBar == null) return;
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            int progress = 0;

            @Override
            public void run() {
                if (progress <= targetProgress) {
                    progressBar.setProgress(progress);
                    progress += 2;
                    handler.postDelayed(this, 20);
                }
            }
        };
        handler.post(runnable);
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Learn English App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "Check out this amazing English learning app! 📚✨\n" +
                "Download now and start your learning journey!");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void logout() {
        mAuth.signOut();
        if (getContext() != null) {
            Toast.makeText(getContext(), "👋 Logged out successfully", Toast.LENGTH_SHORT).show();
        }
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
