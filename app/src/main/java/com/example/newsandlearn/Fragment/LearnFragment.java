package com.example.newsandlearn.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * LearnFragment - Hub for all learning modules
 * Enhanced with Firebase integration and premium design
 */
public class LearnFragment extends Fragment {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Views
    private CardView vocabularyCard, grammarCard, listeningCard;
    private CardView speakingCard, readingCard, writingCard;
    private CardView memoryPalaceCard; // NEW
    private CardView dailyGoalCard;
    private ProgressBar dailyGoalProgress;
    private TextView goalProgressText;
    


    public LearnFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn_new, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initializeViews(view);
        setupListeners();
        animateCardsOnLoad();
        
        // Load data from Firebase
        loadDailyGoalFromFirebase();
        loadModuleProgressFromFirebase();

        return view;
    }

    private void initializeViews(View view) {
        // Cards
        vocabularyCard = view.findViewById(R.id.vocabulary_card);
        grammarCard = view.findViewById(R.id.grammar_card);
        listeningCard = view.findViewById(R.id.listening_card);
        speakingCard = view.findViewById(R.id.speaking_card);
        readingCard = view.findViewById(R.id.reading_card);
        writingCard = view.findViewById(R.id.writing_card);
        memoryPalaceCard = view.findViewById(R.id.memory_palace_card); // NEW
        dailyGoalCard = view.findViewById(R.id.daily_goal_card);
        
        // Hide Speaking and Writing modules
        if (speakingCard != null) speakingCard.setVisibility(View.GONE);
        if (writingCard != null) writingCard.setVisibility(View.GONE);
        
        // Progress
        dailyGoalProgress = view.findViewById(R.id.daily_goal_progress);
        goalProgressText = view.findViewById(R.id.goal_progress_text);
        

    }

    private void setupListeners() {
        // Add press/release animations to cards
        setupCardListener(vocabularyCard, new VocabularyFragment(), "Vocabulary");
        setupCardListener(grammarCard, new GrammarFragment(), "Grammar");
        setupCardListener(listeningCard, new ListeningFragment(), "Listening");
        // Speaking and Writing are hidden
        // setupCardListener(speakingCard, new SpeakingFragment(), "Speaking");
        setupCardListener(readingCard, new ReadingFragment(), "Reading");
        // setupCardListener(writingCard, new WritingFragment(), "Writing");
        
        // NEW: Memory Palace - opens Activity instead of Fragment
        if (memoryPalaceCard != null) {
            memoryPalaceCard.setOnClickListener(v -> {
                v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                trackModuleAccess("Memory Palace");
                                openMemoryPalace();
                            })
                            .start();
                    })
                    .start();
            });
        }
    }

    private void setupCardListener(CardView card, Fragment fragment, String moduleName) {
        card.setOnClickListener(v -> {
            // Button press animation
            v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                    
                    // Track module access
                    trackModuleAccess(moduleName);
                    
                    // Navigate to fragment
                    openFragment(fragment);
                })
                .start();
        });
        
        // Add scale effect on touch
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    break;
            }
            return false;
        });
    }

    private void animateCardsOnLoad() {
        if (getContext() == null) return;
        
        // Staggered fade-in animation for cards (Speaking and Writing hidden)
        animateCardEntrance(dailyGoalCard, 0);
        animateCardEntrance(vocabularyCard, 100);
        animateCardEntrance(grammarCard, 150);
        animateCardEntrance(listeningCard, 200);
        // animateCardEntrance(speakingCard, 250);
        animateCardEntrance(readingCard, 250); // Moved up
        // animateCardEntrance(writingCard, 350);
    }
    
    private void animateCardEntrance(View view, long delay) {
        if (view == null || getContext() == null) return;
        
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (view != null) {
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(400)
                    .start();
            }
        }, delay);
    }

    private void loadDailyGoalFromFirebase() {
        if (currentUser == null) {
            setDefaultDailyGoal();
            return;
        }

        String userId = currentUser.getUid();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        db.collection("users").document(userId)
                .collection("daily_goals").document(today)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long completed = document.getLong("completed");
                        Long total = document.getLong("total");
                        
                        if (completed != null && total != null) {
                            updateDailyGoalUI(completed.intValue(), total.intValue());
                        } else {
                            setDefaultDailyGoal();
                        }
                    } else {
                        // Create default daily goal
                        createDefaultDailyGoal(userId, today);
                    }
                })
                .addOnFailureListener(e -> {
                    setDefaultDailyGoal();
                });
    }
    
    private void createDefaultDailyGoal(String userId, String today) {
        Map<String, Object> dailyGoal = new HashMap<>();
        dailyGoal.put("completed", 0);
        dailyGoal.put("total", 5);
        dailyGoal.put("date", today);
        
        db.collection("users").document(userId)
                .collection("daily_goals").document(today)
                .set(dailyGoal)
                .addOnSuccessListener(aVoid -> {
                    updateDailyGoalUI(0, 5);
                });
    }
    
    private void setDefaultDailyGoal() {
        updateDailyGoalUI(2, 5);
    }
    
    private void updateDailyGoalUI(int completed, int total) {
        if (goalProgressText == null || dailyGoalProgress == null) return;
        
        int progress = total > 0 ? (int) ((completed / (float) total) * 100) : 0;
        
        // Animate progress bar
        animateProgressBar(dailyGoalProgress, progress);
        
        // Update text with animation
        goalProgressText.setText(completed + "/" + total + " completed");
        animateTextChange(goalProgressText);
    }

    private void loadModuleProgressFromFirebase() {
        if (currentUser == null) {
            setDefaultModuleProgress();
            return;
        }

        String userId = currentUser.getUid();
        
        db.collection("users").document(userId)
                .collection("module_progress").document("current")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        updateModuleProgressUI(document);
                    } else {
                        createDefaultModuleProgress(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    setDefaultModuleProgress();
                });
    }
    
    private void createDefaultModuleProgress(String userId) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("vocabulary", 0);
        progress.put("grammar", 0);
        progress.put("listening", 0);
        progress.put("speaking", 0);
        progress.put("reading", 0);
        progress.put("writing", 0);
        
        db.collection("users").document(userId)
                .collection("module_progress").document("current")
                .set(progress)
                .addOnSuccessListener(aVoid -> {
                    setDefaultModuleProgress();
                });
    }
    
    private void updateModuleProgressUI(DocumentSnapshot document) {
        // Module progress is now tracked in Firebase but not displayed individually
        // The data is still loaded for tracking purposes
    }
    
    private void setDefaultModuleProgress() {
        // Default progress values are set in Firebase
    }

    private void trackModuleAccess(String moduleName) {
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        
        Map<String, Object> accessLog = new HashMap<>();
        accessLog.put("module", moduleName);
        accessLog.put("timestamp", timestamp);
        
        db.collection("users").document(userId)
                .collection("module_access")
                .add(accessLog);
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
    
    private void animateTextChange(TextView textView) {
        if (textView == null || getContext() == null) return;
        
        textView.setAlpha(0.4f);
        textView.animate()
            .alpha(1f)
            .setDuration(400)
            .start();
    }

    private void openMemoryPalace() {
        if (getActivity() == null) return;
        
        android.content.Intent intent = new android.content.Intent(
            getActivity(), 
            com.example.newsandlearn.Activity.MemoryPalaceActivity.class
        );
        startActivity(intent);
        getActivity().overridePendingTransition(
            R.anim.slide_in_right, 
            R.anim.slide_out_left
        );
    }

    private void openFragment(Fragment fragment) {
        if (getActivity() == null) return;
        
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadDailyGoalFromFirebase();
        loadModuleProgressFromFirebase();
    }
}
