package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;
    private ProgressBar loadingIndicator;
    private LinearLayout leaderboardContainer;
    private TextView emptyState;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        // Initialize views
        initializeViews();
        setupListeners();
        
        // Load leaderboard
        loadLeaderboard();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        loadingIndicator = findViewById(R.id.loading_indicator);
        leaderboardContainer = findViewById(R.id.leaderboard_container);
        emptyState = findViewById(R.id.empty_state);
    }

    private void setupListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
    }

    private void loadLeaderboard() {
        showLoading(true);
        
        Log.d("LeaderboardActivity", "Starting to load leaderboard...");
        
        db.collection("users")
                .orderBy("totalXP", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("LeaderboardActivity", "Query successful. Documents: " + queryDocumentSnapshots.size());
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("LeaderboardActivity", "No users found");
                        showEmptyState();
                        return;
                    }

                    leaderboardContainer.removeAllViews();
                    int position = 1;
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String username = document.getString("username");
                        Long xp = document.getLong("totalXP");
                        
                        // Fallback if totalXP doesn't exist
                        if (xp == null) {
                            xp = 0L;
                        }
                        
                        // Fallback if username doesn't exist
                        if (username == null || username.isEmpty()) {
                            username = "User " + userId.substring(0, Math.min(8, userId.length()));
                        }
                        
                        Log.d("LeaderboardActivity", "Adding user: " + username + " with " + xp + " XP");
                        addLeaderboardItem(position, userId, username, xp.intValue());
                        position++;
                    }
                    
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("LeaderboardActivity", "Error loading leaderboard", e);
                    showLoading(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    
                    // Show error details in empty state
                    if (emptyState != null) {
                        emptyState.setText("Error loading leaderboard:\n" + e.getMessage());
                        emptyState.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void addLeaderboardItem(int position, String userId, String username, int xp) {
        View itemView = getLayoutInflater().inflate(R.layout.item_leaderboard_full, 
                leaderboardContainer, false);

        TextView rankText = itemView.findViewById(R.id.rank_text);
        TextView usernameText = itemView.findViewById(R.id.username_text);
        TextView xpText = itemView.findViewById(R.id.xp_text);
        View highlightIndicator = itemView.findViewById(R.id.highlight_indicator);

        // Set rank with medal for top 3
        String rankDisplay;
        if (position == 1) {
            rankDisplay = "🥇";
        } else if (position == 2) {
            rankDisplay = "🥈";
        } else if (position == 3) {
            rankDisplay = "🥉";
        } else {
            rankDisplay = String.valueOf(position);
        }
        
        rankText.setText(rankDisplay);
        usernameText.setText(username);
        xpText.setText(xp + " XP");

        // Highlight current user
        if (userId.equals(currentUserId)) {
            itemView.setBackgroundResource(R.drawable.leaderboard_item_highlight_bg);
            if (highlightIndicator != null) {
                highlightIndicator.setVisibility(View.VISIBLE);
            }
        }

        leaderboardContainer.addView(itemView);

        // Animate entrance
        itemView.setAlpha(0f);
        itemView.setTranslationY(20f);
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(position * 50L)
                .start();
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (leaderboardContainer != null) {
            leaderboardContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState() {
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
        if (leaderboardContainer != null) {
            leaderboardContainer.setVisibility(View.GONE);
        }
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
