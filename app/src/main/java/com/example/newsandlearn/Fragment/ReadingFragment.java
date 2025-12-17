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

import com.example.newsandlearn.Adapter.ReadingAdapter;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.R;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ReadingFragment - Displays reading articles from Firebase
 * All articles loaded dynamically, NO hard-coded content
 */
public class ReadingFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView readingRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView articlesRead, avgScore;
    private ChipGroup categoryChipGroup;

    private List<ReadingArticle> allArticles;
    private List<ReadingArticle> filteredArticles;
    private ReadingAdapter adapter;
    private String currentCategory = "all";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ReadingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadReadingArticles();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        allArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>();
    }

    private void initializeViews(View view) {
        articlesRead = view.findViewById(R.id.articles_read);
        avgScore = view.findViewById(R.id.avg_score);
        categoryChipGroup = view.findViewById(R.id.category_chip_group);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        readingRecyclerView = view.findViewById(R.id.reading_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
        View articlesCard = view.findViewById(R.id.articles_card);
        View scoreCard = view.findViewById(R.id.score_card);
        
        // Staggered Entrance Animation
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(view.getContext(), articlesCard, 0);
        com.example.newsandlearn.Utils.AnimationHelper.itemFallDown(view.getContext(), scoreCard, 1);
    }

    private void setupRecyclerView() {
        readingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReadingAdapter(getContext(), filteredArticles, article -> {
            Intent intent = new Intent(getContext(), com.example.newsandlearn.Activity.ArticleDetailActivity.class);
            // Pass the article object (Must be Serializable/Parcelable)
            intent.putExtra("article_data", article); 
            startActivity(intent);
        });
        readingRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadReadingArticles);

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
    }

    /**
     * Load reading articles from Firebase - DYNAMIC
     */
    private void loadReadingArticles() {
        showLoading(true);

        db.collection("reading_articles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ReadingArticle article = document.toObject(ReadingArticle.class);
                        if (article != null) {
                            allArticles.add(article);
                        }
                    }

                    if (auth.getCurrentUser() != null) {
                        loadUserProgress();
                    } else {
                        updateStats();
                        filterArticles();
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
                .collection("reading_progress")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String articleId = document.getString("articleId");
                        Boolean completed = document.getBoolean("completed");
                        Long score = document.getLong("userScore");
                        Long readCount = document.getLong("readCount");

                        for (ReadingArticle article : allArticles) {
                            if (article.getId().equals(articleId)) {
                                if (completed != null) article.setCompleted(completed);
                                if (score != null) article.setUserScore(score.intValue());
                                if (readCount != null) article.setReadCount(readCount.intValue());
                                break;
                            }
                        }
                    }

                    updateStats();
                    filterArticles();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterArticles() {
        filteredArticles.clear();

        for (ReadingArticle article : allArticles) {
            if (currentCategory.equals("all") || 
                (article.getCategory() != null && article.getCategory().equals(currentCategory))) {
                filteredArticles.add(article);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredArticles.isEmpty()) {
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
            if (article.isCompleted()) readCount++;
            if (article.getUserScore() > 0) {
                totalScore += article.getUserScore();
                scoredCount++;
            }
        }

        articlesRead.setText(String.valueOf(readCount));
        avgScore.setText(scoredCount > 0 ? (totalScore / scoredCount) + "%" : "0%");
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            readingRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
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
