package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.newsandlearn.Adapter.ReadingAdapter;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.ReadingGamification;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.GamificationManager;
import com.example.newsandlearn.Utils.SmartRecommendationEngine;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReadingFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView readingRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView articlesRead, avgScore;

    private List<ReadingArticle> allArticles;
    private List<ReadingArticle> filteredArticles;
    private ReadingAdapter adapter;
    private String currentCategory = "all";
    private String currentLevel = "all";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GamificationManager gamificationManager;
    private SmartRecommendationEngine recommendationEngine;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView(view);
        setupListeners(view);
        loadReadingArticles();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        gamificationManager = GamificationManager.getInstance();
        recommendationEngine = SmartRecommendationEngine.getInstance();
        allArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>(); // Initialized once
    }

    private void initializeViews(View view) {
        articlesRead = view.findViewById(R.id.articles_read);
        avgScore = view.findViewById(R.id.avg_score);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        readingRecyclerView = view.findViewById(R.id.reading_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
    }

    private void setupRecyclerView(View view) {
        readingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReadingAdapter(getContext(), filteredArticles, article -> {
            Intent intent = new Intent(getContext(), com.example.newsandlearn.Activity.ReadingActivity.class);
            intent.putExtra("article_id", article.getId());
            startActivity(intent);
        });
        readingRecyclerView.setAdapter(adapter);

        readingRecyclerView.post(() -> Log.d("RV_DEBUG", "childCount=" + readingRecyclerView.getChildCount()));
    }

    private void setupListeners(View view) {
        swipeRefresh.setOnRefreshListener(this::loadReadingArticles);

        ChipGroup categoryChipGroup = view.findViewById(R.id.category_chip_group);
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentCategory = "all";
            } else if (checkedId == R.id.chip_news) {
                currentCategory = "news";
            } else if (checkedId == R.id.chip_story) {
                currentCategory = "story";
            }
            filterArticles();
        });

        ChipGroup levelChipGroup = view.findViewById(R.id.level_chip_group);
        levelChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_level_all) {
                currentLevel = "all";
            } else if (checkedId == R.id.chip_a1) {
                currentLevel = "A1";
            } else if (checkedId == R.id.chip_a2) {
                currentLevel = "A2";
            } else if (checkedId == R.id.chip_b1) {
                currentLevel = "B1";
            } else if (checkedId == R.id.chip_b2) {
                currentLevel = "B2";
            }
            filterArticles();
        });
    }

    private void loadReadingArticles() {
        showLoading(true);
        db.collection("reading_lessons").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        if (article != null) {
                            article.setId(document.getId());
                            allArticles.add(article);
                        }
                    }
                    if (auth.getCurrentUser() != null) {
                        loadUserProgress();
                    } else {
                        updateStats();
                        filterArticles();
                        showLoading(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading articles", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void loadUserProgress() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("reading_progress").get()
                .addOnSuccessListener(progressSnapshots -> {
                    for (QueryDocumentSnapshot doc : progressSnapshots) {
                        for (ReadingArticle article : allArticles) {
                            if (article.getId().equals(doc.getId())) {
                                if (doc.getBoolean("completed") != null) {
                                    article.setCompleted(doc.getBoolean("completed"));
                                }
                                if (doc.getLong("userScore") != null) {
                                    article.setUserScore(doc.getLong("userScore").intValue());
                                }
                                break;
                            }
                        }
                    }
                    updateStats();
                    filterArticles();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    filterArticles();
                });
    }

    private void filterArticles() {
        List<ReadingArticle> temp = new ArrayList<>();
        for (ReadingArticle article : allArticles) {
            boolean categoryMatches = currentCategory.equals("all") ||
                    (article.getCategory() != null && article.getCategory().equalsIgnoreCase(currentCategory));

            boolean levelMatches = currentLevel.equals("all") ||
                    (article.getLevel() != null && article.getLevel().equalsIgnoreCase(currentLevel));

            if (categoryMatches && levelMatches) {
                temp.add(article);
            }
        }

        adapter.updateData(temp);

        if (temp.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateStats() {
        int readCount = 0;
        int totalScore = 0;
        int scoredCount = 0;
        for (ReadingArticle article : allArticles) {
            if (article.isCompleted()) {
                readCount++;
            }
            if (article.getUserScore() > 0) {
                totalScore += article.getUserScore();
                scoredCount++;
            }
        }
        articlesRead.setText(String.valueOf(readCount));
        avgScore.setText(scoredCount > 0 ? (totalScore / scoredCount) + "%" : "0%");
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            readingRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            readingRecyclerView.setVisibility(View.VISIBLE);
        }
        swipeRefresh.setRefreshing(isLoading);
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        readingRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        readingRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReadingArticles();
    }
}
