package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.EnhancedArticleDetailActivity;
import com.example.newsandlearn.Adapter.DynamicArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic ArticleFragment - Modern UI with animations and filtering
 */
public class ArticleFragment extends Fragment implements DynamicArticleAdapter.OnArticleClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private DynamicArticleAdapter adapter;
    private List<Article> allArticles;
    private List<Article> filteredArticles;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    // UI Components
    private EditText searchEditText;
    private ImageView searchIcon;
    private ChipGroup filterChipGroup;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    
    private String currentFilter = "All";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFilters();

        // Load articles with animation
        new Handler().postDelayed(this::loadArticles, 300);

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_articles);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchIcon = view.findViewById(R.id.search_icon);
        filterChipGroup = view.findViewById(R.id.chip_group_filter);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        
        allArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new DynamicArticleAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // Add scroll listener for dynamic effects
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Hide search when scrolling down
                if (dy > 0 && searchEditText != null) {
                    searchEditText.clearFocus();
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadArticles);
        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.purple_500,
                R.color.secondary
        );
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);
    }

    private void setupSearch() {
        if (searchEditText == null) return;
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                filterArticles();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        if (filterChipGroup == null) return;
        
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "All";
            } else {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    currentFilter = chip.getText().toString();
                }
            }
            filterArticles();
        });
    }

    private void loadArticles() {
        swipeRefresh.setRefreshing(true);
        showEmptyState(false);

        db.collection("articles")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        article.setId(document.getId());
                        allArticles.add(article);
                    }
                    
                    filterArticles();
                    swipeRefresh.setRefreshing(false);

                    if (allArticles.isEmpty()) {
                        showEmptyState(true);
                        emptyStateText.setText("📰 No articles available yet.\nCheck back soon!");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                    showEmptyState(true);
                    emptyStateText.setText("⚠️ Failed to load articles.\nPull to refresh.");
                });
    }

    private void filterArticles() {
        filteredArticles.clear();

        for (Article article : allArticles) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    article.getTitle().toLowerCase().contains(currentSearchQuery) ||
                    (article.getCategory() != null && article.getCategory().toLowerCase().contains(currentSearchQuery)) ||
                    (article.getSource() != null && article.getSource().toLowerCase().contains(currentSearchQuery));

            boolean matchesFilter = currentFilter.equals("All") ||
                    (article.getLevel() != null && article.getLevel().equalsIgnoreCase(currentFilter));

            if (matchesSearch && matchesFilter) {
                filteredArticles.add(article);
            }
        }

        adapter.setArticles(filteredArticles);
        
        if (filteredArticles.isEmpty() && !allArticles.isEmpty()) {
            showEmptyState(true);
            emptyStateText.setText("🔍 No articles match your search.\nTry different keywords.");
        } else {
            showEmptyState(false);
        }
    }

    private void showEmptyState(boolean show) {
        if (emptyStateLayout != null) {
            if (show) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                emptyStateLayout.startAnimation(
                        AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_scale)
                );
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(getContext(), EnhancedArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
        
        // Add transition animation
        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        }
    }

    @Override
    public void onFavoriteClick(Article article) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "⚠️ Please login to save favorites", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        boolean newFavoriteState = !article.isFavorite();
        
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(article.getId())
                .set(article)
                .addOnSuccessListener(aVoid -> {
                    article.setFavorite(newFavoriteState);
                    adapter.notifyDataSetChanged();
                    
                    String message = newFavoriteState ? 
                            "❤️ Added to favorites!" : 
                            "💔 Removed from favorites";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh articles when returning to fragment
        if (adapter != null && !allArticles.isEmpty()) {
            filterArticles();
        }
    }
}

