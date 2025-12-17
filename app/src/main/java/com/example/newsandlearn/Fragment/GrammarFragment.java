package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.GrammarLessonActivity;
import com.example.newsandlearn.Adapter.GrammarAdapter;
import com.example.newsandlearn.Model.GrammarLesson;
import com.example.newsandlearn.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * GrammarFragment - Displays grammar lessons from Firebase
 * All data loaded dynamically, no hard-coded content
 */
public class GrammarFragment extends Fragment {

    private static final String TAG = "GrammarFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView grammarRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView lessonsCompletedCount, avgScore;
    private ChipGroup levelChipGroup;

    // Data
    private List<GrammarLesson> allLessons;
    private List<GrammarLesson> filteredLessons;
    private GrammarAdapter adapter;
    private String currentLevel = "all";

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public GrammarFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grammar, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadGrammarLessons();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        allLessons = new ArrayList<>();
        filteredLessons = new ArrayList<>();
    }

    private void initializeViews(View view) {
        // Stats
        lessonsCompletedCount = view.findViewById(R.id.lessons_completed_count);
        avgScore = view.findViewById(R.id.avg_score);

        // Filter
        levelChipGroup = view.findViewById(R.id.level_chip_group);

        // List
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        grammarRecyclerView = view.findViewById(R.id.grammar_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        grammarRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GrammarAdapter(getContext(), filteredLessons, lesson -> {
            // Open lesson detail activity
            Intent intent = new Intent(getActivity(), GrammarLessonActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("title", lesson.getTitle());
            startActivity(intent);
        });
        grammarRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Pull to refresh
        swipeRefresh.setOnRefreshListener(this::loadGrammarLessons);

        // Level filter
        levelChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all_levels) {
                currentLevel = "all";
            } else if (checkedId == R.id.chip_a1) {
                currentLevel = "A";
            } else if (checkedId == R.id.chip_b1) {
                currentLevel = "B";
            } else if (checkedId == R.id.chip_c1) {
                currentLevel = "C";
            }
            filterLessons();
        });
    }

    /**
     * Load grammar lessons from Firebase (DYNAMIC)
     */
    private void loadGrammarLessons() {
        showLoading(true);

        // Load from public grammar_lessons collection
        db.collection("grammar_lessons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allLessons.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GrammarLesson lesson = document.toObject(GrammarLesson.class);
                        if (lesson != null) {
                            allLessons.add(lesson);
                        }
                    }

                    // Load user progress for each lesson
                    if (auth.getCurrentUser() != null) {
                        loadUserProgress();
                    } else {
                        updateStats();
                        filterLessons();
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading lessons: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    /**
     * Load user's progress for grammar lessons from Firebase
     */
    private void loadUserProgress() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("grammar_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Update lessons with user progress
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lessonId = document.getString("lessonId");
                        Boolean completed = document.getBoolean("completed");
                        Long score = document.getLong("score");

                        // Find and update lesson
                        for (GrammarLesson lesson : allLessons) {
                            if (lesson.getId().equals(lessonId)) {
                                if (completed != null) lesson.setCompleted(completed);
                                if (score != null) lesson.setUserScore(score.intValue());
                                break;
                            }
                        }
                    }

                    updateStats();
                    filterLessons();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    // Continue even if progress loading fails
                    updateStats();
                    filterLessons();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterLessons() {
        filteredLessons.clear();

        for (GrammarLesson lesson : allLessons) {
            boolean shouldInclude = false;

            if (currentLevel.equals("all")) {
                shouldInclude = true;
            } else {
                // Filter by level prefix (A1/A2 -> A, B1/B2 -> B, C1/C2 -> C)
                if (lesson.getLevel() != null && lesson.getLevel().startsWith(currentLevel)) {
                    shouldInclude = true;
                }
            }

            if (shouldInclude) {
                filteredLessons.add(lesson);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredLessons.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateStats() {
        int completedCount = 0;
        int totalScore = 0;
        int scoredLessons = 0;

        for (GrammarLesson lesson : allLessons) {
            if (lesson.isCompleted()) {
                completedCount++;
            }
            if (lesson.getUserScore() > 0) {
                totalScore += lesson.getUserScore();
                scoredLessons++;
            }
        }

        lessonsCompletedCount.setText(String.valueOf(completedCount));

        if (scoredLessons > 0) {
            int avgScoreValue = totalScore / scoredLessons;
            avgScore.setText(avgScoreValue + "%");
        } else {
            avgScore.setText("0%");
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            grammarRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        grammarRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        grammarRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh when returning
        loadGrammarLessons();
    }
}
