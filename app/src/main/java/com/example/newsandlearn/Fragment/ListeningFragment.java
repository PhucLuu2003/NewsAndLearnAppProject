package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.ListeningActivity;
import com.example.newsandlearn.Adapter.ListeningAdapter;
import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListeningFragment extends Fragment {

    private RecyclerView recyclerView;
    private ListeningAdapter adapter;
    private List<ListeningLesson> allLessons;
    private List<ListeningLesson> filteredLessons;

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private ChipGroup categoryChipGroup;

    // Stats TextViews
    private TextView completedCountText;
    private TextView hoursListenedText;
    private TextView avgScoreText;
    private ProgressBar scoreProgressRing;

    private FirebaseFirestore db;
    private String currentUserId;

    private String selectedFilter = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listening, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        initializeViews(view);
        setupRecyclerView();
        setupChipFilters();
        loadLessons();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.listening_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
        categoryChipGroup = view.findViewById(R.id.category_chip_group);

        completedCountText = view.findViewById(R.id.completed_count);
        hoursListenedText = view.findViewById(R.id.hours_listened);
        avgScoreText = view.findViewById(R.id.avg_score);
        scoreProgressRing = view.findViewById(R.id.score_progress_ring);

        swipeRefresh.setOnRefreshListener(this::loadLessons);

        allLessons = new ArrayList<>();
        filteredLessons = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ListeningAdapter(getContext(), filteredLessons, lesson -> {
            Intent intent = new Intent(getContext(), ListeningActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupChipFilters() {
        categoryChipGroup.removeAllViews();

        // Add "All" chip
        addFilterChip("All", true);

        // Add level chips
        String[] levels = {"A1", "A2", "B1", "B2"};
        for (String level : levels) {
            addFilterChip(level, false);
        }

        // Add category chips
        String[] categories = {"Social", "Greetings", "Food", "Daily Life",
                "Travel", "Transport", "Routine", "Habits",
                "Shopping", "Fashion"};
        for (String category : categories) {
            addFilterChip(category, false);
        }
    }

    private void addFilterChip(String text, boolean isChecked) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chip.setChipBackgroundColorResource(R.color.chip_background_selector);
        chip.setChipStrokeColorResource(R.color.primary);
        chip.setChipStrokeWidth(1);

        chip.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                selectedFilter = text;
                filterLessons();
            }
        });

        categoryChipGroup.addView(chip);
    }

    private void loadLessons() {
        showLoading(true);

        db.collection("listening_lessons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allLessons.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ListeningLesson lesson = doc.toObject(ListeningLesson.class);

                        // Load user-specific progress if available
                        if (currentUserId != null) {
                            loadUserProgress(lesson);
                        } else {
                            allLessons.add(lesson);
                        }
                    }

                    // Wait a bit for user progress to load
                    if (currentUserId != null) {
                        recyclerView.postDelayed(() -> {
                            filterLessons();
                            updateStats();
                            showLoading(false);
                        }, 500);
                    } else {
                        filterLessons();
                        updateStats();
                        showLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                });
    }

    private void loadUserProgress(ListeningLesson lesson) {
        if (currentUserId == null) {
            allLessons.add(lesson);
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("listening_progress")
                .document(lesson.getId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean completed = doc.getBoolean("completed");
                        Long userScore = doc.getLong("userScore");
                        Long timesListened = doc.getLong("timesListened");

                        if (completed != null) lesson.setCompleted(completed);
                        if (userScore != null) lesson.setUserScore(userScore.intValue());
                        if (timesListened != null) lesson.setTimesListened(timesListened.intValue());
                    }
                    allLessons.add(lesson);
                })
                .addOnFailureListener(e -> allLessons.add(lesson));
    }

    private void filterLessons() {
        filteredLessons.clear();

        if ("All".equals(selectedFilter)) {
            filteredLessons.addAll(allLessons);
        } else {
            for (ListeningLesson lesson : allLessons) {
                // Check if filter matches level
                if (selectedFilter.equals(lesson.getLevel())) {
                    filteredLessons.add(lesson);
                    continue;
                }

                // Check if filter matches category
                if (lesson.getCategory() != null &&
                        lesson.getCategory().toLowerCase().contains(selectedFilter.toLowerCase())) {
                    filteredLessons.add(lesson);
                }
            }
        }

        if (adapter != null) {
            adapter.updateData(filteredLessons);
        }
        showEmptyState(filteredLessons.isEmpty());
    }

    private void updateStats() {
        int completedCount = 0;
        int totalScore = 0;
        int lessonsWithScores = 0;
        int totalMinutes = 0;

        for (ListeningLesson lesson : allLessons) {
            if (lesson.isCompleted()) {
                completedCount++;
            }

            if (lesson.getUserScore() > 0) {
                totalScore += lesson.getUserScore();
                lessonsWithScores++;
            }

            // Estimate time: assume 30 seconds per question on average
            if (lesson.getTimesListened() > 0) {
                int estimatedSeconds = lesson.getQuestionCount() * 30 * lesson.getTimesListened();
                totalMinutes += estimatedSeconds / 60;
            }
        }

        // Update completed count
        completedCountText.setText(String.valueOf(completedCount));

        // Update hours listened
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours > 0) {
            hoursListenedText.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
        } else {
            hoursListenedText.setText(String.format(Locale.getDefault(), "%dm", minutes));
        }

        // Update average score
        int avgScore = lessonsWithScores > 0 ? totalScore / lessonsWithScores : 0;
        avgScoreText.setText(String.format(Locale.getDefault(), "%d%%", avgScore));
        scoreProgressRing.setProgress(avgScore);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload lessons when returning to fragment to update stats
        loadLessons();
    }
}