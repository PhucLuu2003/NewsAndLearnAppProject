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

import com.example.newsandlearn.Adapter.SpeakingAdapter;
import com.example.newsandlearn.Model.SpeakingLesson;
import com.example.newsandlearn.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * SpeakingFragment - Displays speaking lessons from Firebase
 * All data loaded dynamically, NO hard-coded content
 */
public class SpeakingFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView speakingRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView completedCount, pronunciationScore, fluencyScore;
    private ChipGroup typeChipGroup;

    private List<SpeakingLesson> allLessons;
    private List<SpeakingLesson> filteredLessons;
    private SpeakingAdapter adapter;
    private String currentType = "all";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public SpeakingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speaking, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadSpeakingLessons();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        allLessons = new ArrayList<>();
        filteredLessons = new ArrayList<>();
    }

    private void initializeViews(View view) {
        completedCount = view.findViewById(R.id.completed_count);
        pronunciationScore = view.findViewById(R.id.pronunciation_score);
        fluencyScore = view.findViewById(R.id.fluency_score);
        typeChipGroup = view.findViewById(R.id.type_chip_group);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        speakingRecyclerView = view.findViewById(R.id.speaking_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        speakingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SpeakingAdapter(getContext(), filteredLessons, lesson -> {
            // Open speaking activity
            Intent intent = new Intent(getActivity(), com.example.newsandlearn.Activity.SpeakingActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            intent.putExtra("title", lesson.getTitle());
            startActivity(intent);
        });
        speakingRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadSpeakingLessons);

        typeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentType = "all";
            } else if (checkedId == R.id.chip_pronunciation) {
                currentType = "PRONUNCIATION";
            } else if (checkedId == R.id.chip_conversation) {
                currentType = "CONVERSATION";
            }
            filterLessons();
        });
    }

    /**
     * Load speaking lessons from Firebase - DYNAMIC
     */
    private void loadSpeakingLessons() {
        showLoading(true);
        android.util.Log.d("SpeakingFragment", "Starting to load speaking lessons...");

        db.collection("speaking_lessons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("SpeakingFragment", "Firebase query successful");
                    allLessons.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        android.util.Log.w("SpeakingFragment", "No documents found in speaking_lessons");
                        Toast.makeText(getContext(), "No speaking lessons found. Please seed data from Settings.", Toast.LENGTH_LONG).show();
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        showEmptyState();
                        return;
                    }

                    android.util.Log.d("SpeakingFragment", "Found " + queryDocumentSnapshots.size() + " documents");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            android.util.Log.d("SpeakingFragment", "Processing document: " + document.getId());
                            
                            // Try to parse as SpeakingLesson
                            SpeakingLesson lesson = document.toObject(SpeakingLesson.class);
                            
                            if (lesson != null) {
                                // Ensure ID is set
                                if (lesson.getId() == null || lesson.getId().isEmpty()) {
                                    lesson.setId(document.getId());
                                }
                                allLessons.add(lesson);
                                android.util.Log.d("SpeakingFragment", "Successfully added lesson: " + lesson.getTitle());
                            } else {
                                android.util.Log.w("SpeakingFragment", "Lesson is null for document: " + document.getId());
                            }
                        } catch (Exception e) {
                            android.util.Log.e("SpeakingFragment", "Error parsing document " + document.getId(), e);
                            // Continue with next document
                        }
                    }

                    android.util.Log.d("SpeakingFragment", "Total lessons loaded: " + allLessons.size());

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
                    android.util.Log.e("SpeakingFragment", "Firebase query failed", e);
                    String errorMsg = "Error loading lessons: " + e.getMessage();
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                    showEmptyState();
                });
    }

    private void loadUserProgress() {
        String userId = auth.getCurrentUser().getUid();
        android.util.Log.d("SpeakingFragment", "Loading user progress for: " + userId);

        db.collection("users").document(userId)
                .collection("speaking_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("SpeakingFragment", "User progress loaded: " + queryDocumentSnapshots.size() + " records");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lessonId = document.getString("lessonId");
                        Boolean completed = document.getBoolean("completed");
                        Long pronScore = document.getLong("pronunciationScore");
                        Long fluScore = document.getLong("fluencyScore");
                        Long attempts = document.getLong("attemptCount");

                        for (SpeakingLesson lesson : allLessons) {
                            if (lesson.getId().equals(lessonId)) {
                                if (completed != null) lesson.setCompleted(completed);
                                if (pronScore != null) lesson.setPronunciationScore(pronScore.intValue());
                                if (fluScore != null) lesson.setFluencyScore(fluScore.intValue());
                                if (attempts != null) lesson.setAttemptCount(attempts.intValue());
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
                    android.util.Log.w("SpeakingFragment", "Failed to load user progress (this is OK): " + e.getMessage());
                    // Continue without progress data - just show lessons
                    updateStats();
                    filterLessons();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterLessons() {
        filteredLessons.clear();

        for (SpeakingLesson lesson : allLessons) {
            if (currentType.equals("all") || 
                (lesson.getType() != null && lesson.getType().name().equals(currentType))) {
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
        int completed = 0;
        int totalPron = 0;
        int totalFlu = 0;
        int scoredCount = 0;

        for (SpeakingLesson lesson : allLessons) {
            if (lesson.isCompleted()) completed++;
            if (lesson.getPronunciationScore() > 0) {
                totalPron += lesson.getPronunciationScore();
                totalFlu += lesson.getFluencyScore();
                scoredCount++;
            }
        }

        completedCount.setText(String.valueOf(completed));
        pronunciationScore.setText(scoredCount > 0 ? (totalPron / scoredCount) + "%" : "0%");
        fluencyScore.setText(scoredCount > 0 ? (totalFlu / scoredCount) + "%" : "0%");
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            speakingRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        speakingRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        speakingRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSpeakingLessons();
    }
}
