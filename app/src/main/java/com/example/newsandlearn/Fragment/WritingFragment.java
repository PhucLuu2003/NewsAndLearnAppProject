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

import com.example.newsandlearn.Adapter.WritingAdapter;
import com.example.newsandlearn.Model.WritingPrompt;
import com.example.newsandlearn.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * WritingFragment - Displays writing prompts from Firebase
 * All prompts loaded dynamically, NO hard-coded content
 */
public class WritingFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView writingRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView essaysWritten, avgScore;
    private ChipGroup typeChipGroup;

    private List<WritingPrompt> allPrompts;
    private List<WritingPrompt> filteredPrompts;
    private WritingAdapter adapter;
    private String currentType = "all";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public WritingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_writing, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadWritingPrompts();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        allPrompts = new ArrayList<>();
        filteredPrompts = new ArrayList<>();
    }

    private void initializeViews(View view) {
        essaysWritten = view.findViewById(R.id.essays_written);
        avgScore = view.findViewById(R.id.avg_score);
        typeChipGroup = view.findViewById(R.id.type_chip_group);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        writingRecyclerView = view.findViewById(R.id.writing_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupRecyclerView() {
        writingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WritingAdapter(getContext(), filteredPrompts, prompt -> {
            Intent intent = new Intent(getActivity(), com.example.newsandlearn.Activity.WritingActivity.class);
            intent.putExtra("prompt_id", prompt.getId());
            intent.putExtra("title", prompt.getTitle());
            startActivity(intent);
        });
        writingRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadWritingPrompts);

        typeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentType = "all";
            } else if (checkedId == R.id.chip_essay) {
                currentType = "ESSAY";
            } else if (checkedId == R.id.chip_email) {
                currentType = "EMAIL";
            }
            filterPrompts();
        });
    }

    /**
     * Load writing prompts from Firebase - DYNAMIC
     */
    private void loadWritingPrompts() {
        showLoading(true);

        db.collection("writing_prompts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allPrompts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WritingPrompt prompt = document.toObject(WritingPrompt.class);
                        if (prompt != null) {
                            allPrompts.add(prompt);
                        }
                    }

                    if (auth.getCurrentUser() != null) {
                        loadUserProgress();
                    } else {
                        updateStats();
                        filterPrompts();
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void loadUserProgress() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("writing_submissions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String promptId = document.getString("promptId");
                        Boolean completed = document.getBoolean("completed");
                        Long score = document.getLong("overallScore");

                        for (WritingPrompt prompt : allPrompts) {
                            if (prompt.getId().equals(promptId)) {
                                if (completed != null) prompt.setCompleted(completed);
                                if (score != null) prompt.setOverallScore(score.intValue());
                                break;
                            }
                        }
                    }

                    updateStats();
                    filterPrompts();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterPrompts() {
        filteredPrompts.clear();

        for (WritingPrompt prompt : allPrompts) {
            if (currentType.equals("all") || 
                (prompt.getType() != null && prompt.getType().name().equals(currentType))) {
                filteredPrompts.add(prompt);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredPrompts.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateStats() {
        int completed = 0;
        int totalScore = 0;
        int scoredCount = 0;

        for (WritingPrompt prompt : allPrompts) {
            if (prompt.isCompleted()) completed++;
            if (prompt.getOverallScore() > 0) {
                totalScore += prompt.getOverallScore();
                scoredCount++;
            }
        }

        essaysWritten.setText(String.valueOf(completed));
        avgScore.setText(scoredCount > 0 ? (totalScore / scoredCount) + "%" : "0%");
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            writingRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        writingRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        writingRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWritingPrompts();
    }
}
