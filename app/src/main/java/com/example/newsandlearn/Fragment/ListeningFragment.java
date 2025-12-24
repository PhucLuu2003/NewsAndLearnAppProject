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

import com.example.newsandlearn.Adapter.ListeningAdapter;
import com.example.newsandlearn.Model.ListeningLesson;
import com.example.newsandlearn.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ListeningFragment - Displays listening lessons from Firebase
 * All data loaded dynamically, NO hard-coded content
 */
public class ListeningFragment extends Fragment {

    private static final String TAG = "ListeningFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView listeningRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView completedCount, hoursListened, avgScore;
    private ChipGroup categoryChipGroup;

    // Data
    private List<ListeningLesson> allLessons;
    private List<ListeningLesson> filteredLessons;
    private ListeningAdapter adapter;
    private String currentCategory = "all";

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ListeningFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listening, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadListeningLessons();

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
        completedCount = view.findViewById(R.id.completed_count);
        hoursListened = view.findViewById(R.id.hours_listened);
        avgScore = view.findViewById(R.id.avg_score);

        // Filter
        categoryChipGroup = view.findViewById(R.id.category_chip_group);

        // List
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        listeningRecyclerView = view.findViewById(R.id.listening_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
        
        emptyState = view.findViewById(R.id.empty_state);
        View completedCard = view.findViewById(R.id.completed_card);
        View hoursCard = view.findViewById(R.id.hours_card);
        View scoreCard = view.findViewById(R.id.score_card);
        
        // Staggered Entrance Animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(view.getContext(), completedCard, 0);
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(view.getContext(), hoursCard, 1);
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(view.getContext(), scoreCard, 2);
    }

    private void setupRecyclerView() {
        listeningRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListeningAdapter(getContext(), filteredLessons, lesson -> {
            Intent intent = new Intent(getContext(), com.example.newsandlearn.Activity.ListeningActivity.class);
            intent.putExtra("lesson_id", lesson.getId());
            startActivity(intent);
        });
        listeningRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadListeningLessons);

        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentCategory = "all";
            } else if (checkedId == R.id.chip_conversation) {
                currentCategory = "conversation";
            } else if (checkedId == R.id.chip_news) {
                currentCategory = "news";
            } else if (checkedId == R.id.chip_story) {
                currentCategory = "story";
            }
            filterLessons();
        });
    }

    /**
     * Load listening lessons from Firebase - DYNAMIC
     */
    private void loadListeningLessons() {
        showLoading(true);

        // Load from public listening_lessons collection
        db.collection("listening_lessons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allLessons.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ListeningLesson lesson = document.toObject(ListeningLesson.class);
                        if (lesson != null) {
                            lesson.setId(document.getId()); // <-- THE FIX: Set the actual document ID
                            allLessons.add(lesson);
                        }
                    }

                    // Load user progress
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
     * Load user's progress from Firebase
     */
    private void loadUserProgress() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("listening_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String lessonId = document.getString("lessonId");
                        Boolean completed = document.getBoolean("completed");
                        Long score = document.getLong("userScore");
                        Long listened = document.getLong("timesListened");

                        for (ListeningLesson lesson : allLessons) {
                            if (lesson.getId().equals(lessonId)) {
                                if (completed != null) lesson.setCompleted(completed);
                                if (score != null) lesson.setUserScore(score.intValue());
                                if (listened != null) lesson.setTimesListened(listened.intValue());
                                break;
                            }
                        }
                    }

                    updateStats();
                    filterLessons();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterLessons() {
        filteredLessons.clear();

        for (ListeningLesson lesson : allLessons) {
            if (currentCategory.equals("all") || 
                (lesson.getCategory() != null && lesson.getCategory().equals(currentCategory))) {
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
        int totalScore = 0;
        int scoredCount = 0;
        int totalSeconds = 0;

        for (ListeningLesson lesson : allLessons) {
            if (lesson.isCompleted()) {
                completed++;
            }
            if (lesson.getUserScore() > 0) {
                totalScore += lesson.getUserScore();
                scoredCount++;
            }
            totalSeconds += lesson.getDurationSeconds() * lesson.getTimesListened();
        }

        completedCount.setText(String.valueOf(completed));

        int hours = totalSeconds / 3600;
        hoursListened.setText(hours + "h");

        if (scoredCount > 0) {
            avgScore.setText((totalScore / scoredCount) + "%");
        } else {
            avgScore.setText("0%");
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            listeningRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        listeningRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        listeningRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadListeningLessons();
    }
}
