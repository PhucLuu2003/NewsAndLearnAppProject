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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.EnhancedArticleDetailActivity;
import com.example.newsandlearn.Adapter.DynamicArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private NestedScrollView scrollView;
    private EditText searchEditText;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipEasy, chipMedium, chipHard;

    private static final int PAGE_SIZE = 20;
    private boolean isLoadingPage = false;
    private boolean hasMorePages = true;
    private DocumentSnapshot lastVisible = null;

    private String currentQuery = "";
    private String currentLevelFilter = "all";

    private final Set<String> favoriteIds = new HashSet<>();
    private boolean favoriteIdsLoaded = false;

    // UI Components
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

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
        setupFiltering();
        setupPaging();

        // Load articles with animation
        adapter.setLoadingInitial(true);
        new Handler().postDelayed(() -> loadFirstPage(true), 200);

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_articles);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        scrollView = view.findViewById(R.id.article_scroll);
        searchEditText = view.findViewById(R.id.search_edit_text);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        chipAll = view.findViewById(R.id.chip_all);
        chipEasy = view.findViewById(R.id.chip_easy);
        chipMedium = view.findViewById(R.id.chip_medium);
        chipHard = view.findViewById(R.id.chip_hard);

        allArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new DynamicArticleAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Add scroll listener for dynamic effects
        // Scroll listener removed since search is no longer present
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> loadFirstPage(false));
        swipeRefresh.setColorSchemeResources(
                R.color.primary,
                R.color.purple_500,
                R.color.secondary);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);
    }

    private void setupFiltering() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s != null ? s.toString() : "";
                    applyFiltersAndRender();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        if (chipGroupFilter != null) {
            chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                int checkedId = checkedIds.isEmpty() ? View.NO_ID : checkedIds.get(0);
                if (checkedId == R.id.chip_easy) {
                    currentLevelFilter = "easy";
                } else if (checkedId == R.id.chip_medium) {
                    currentLevelFilter = "medium";
                } else if (checkedId == R.id.chip_hard) {
                    currentLevelFilter = "hard";
                } else {
                    currentLevelFilter = "all";
                }
                applyFiltersAndRender();
            });
        }
    }

    private void setupPaging() {
        if (scrollView == null)
            return;

        scrollView.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    View content = v.getChildAt(0);
                    if (content == null)
                        return;

                    int distanceToBottom = content.getBottom() - (v.getHeight() + scrollY);
                    if (distanceToBottom < 300) {
                        loadNextPage();
                    }
                });
    }

    private void loadFirstPage(boolean showSkeleton) {
        if (isLoadingPage)
            return;

        isLoadingPage = true;
        hasMorePages = true;
        lastVisible = null;

        allArticles.clear();
        filteredArticles.clear();
        showEmptyState(false);

        if (showSkeleton) {
            adapter.setLoadingInitial(true);
        }
        adapter.setLoadingMore(false);

        swipeRefresh.setRefreshing(true);

        Query query = db.collection("articles")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Article> page = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        article.setId(document.getId());
                        page.add(article);
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    hasMorePages = page.size() >= PAGE_SIZE;
                    allArticles.addAll(page);

                    adapter.setLoadingInitial(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingPage = false;

                    refreshFavoriteIdsThenRender();
                })
                .addOnFailureListener(e -> {
                    adapter.setLoadingInitial(false);
                    adapter.setLoadingMore(false);
                    swipeRefresh.setRefreshing(false);
                    isLoadingPage = false;
                    hasMorePages = false;

                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                    emptyStateText.setText("⚠️ Failed to load articles.\nPull to refresh.");
                });
    }

    private void loadNextPage() {
        if (isLoadingPage || !hasMorePages || lastVisible == null)
            return;

        isLoadingPage = true;
        adapter.setLoadingMore(true);

        Query query = db.collection("articles")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(PAGE_SIZE);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Article> page = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        article.setId(document.getId());
                        page.add(article);
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }

                    if (page.isEmpty()) {
                        hasMorePages = false;
                    } else {
                        hasMorePages = page.size() >= PAGE_SIZE;
                        allArticles.addAll(page);
                    }

                    adapter.setLoadingMore(false);
                    isLoadingPage = false;
                    refreshFavoriteIdsThenRender();
                })
                .addOnFailureListener(e -> {
                    adapter.setLoadingMore(false);
                    isLoadingPage = false;
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshFavoriteIdsThenRender() {
        if (auth.getCurrentUser() == null) {
            favoriteIds.clear();
            favoriteIdsLoaded = true;
            applyFavoriteFlags();
            applyFiltersAndRender();
            return;
        }

        // Always refresh favorites so the list stays consistent across screens.
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteIds.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        favoriteIds.add(doc.getId());
                    }
                    favoriteIdsLoaded = true;
                    applyFavoriteFlags();
                    applyFiltersAndRender();
                })
                .addOnFailureListener(e -> {
                    favoriteIdsLoaded = true;
                    applyFavoriteFlags();
                    applyFiltersAndRender();
                });
    }

    private void applyFavoriteFlags() {
        if (!favoriteIdsLoaded)
            return;
        for (Article article : allArticles) {
            if (article.getId() != null) {
                article.setFavorite(favoriteIds.contains(article.getId()));
            }
        }
    }

    private void applyFiltersAndRender() {
        filteredArticles.clear();

        String q = currentQuery == null ? "" : currentQuery.toLowerCase().trim();

        for (Article article : allArticles) {
            if (article == null)
                continue;

            boolean matchesQuery;
            if (q.isEmpty()) {
                matchesQuery = true;
            } else {
                String title = article.getTitle() != null ? article.getTitle().toLowerCase() : "";
                String category = article.getCategory() != null ? article.getCategory().toLowerCase() : "";
                String source = article.getSource() != null ? article.getSource().toLowerCase() : "";
                String level = article.getLevel() != null ? article.getLevel().toLowerCase() : "";
                matchesQuery = title.contains(q) || category.contains(q) || source.contains(q) || level.contains(q);
            }

            boolean matchesLevel;
            if ("all".equals(currentLevelFilter)) {
                matchesLevel = true;
            } else {
                String level = article.getLevel() != null ? article.getLevel().toLowerCase().trim() : "";
                if ("easy".equals(currentLevelFilter)) {
                    matchesLevel = level.contains("easy") || level.contains("beginner") || level.contains("a1")
                            || level.contains("a2");
                } else if ("medium".equals(currentLevelFilter)) {
                    matchesLevel = level.contains("medium") || level.contains("intermediate") || level.contains("b1")
                            || level.contains("b2");
                } else if ("hard".equals(currentLevelFilter)) {
                    matchesLevel = level.contains("hard") || level.contains("advanced") || level.contains("c1")
                            || level.contains("c2");
                } else {
                    matchesLevel = true;
                }
            }

            if (matchesQuery && matchesLevel) {
                filteredArticles.add(article);
            }
        }

        adapter.setArticles(filteredArticles);

        if (filteredArticles.isEmpty() && !isLoadingPage) {
            showEmptyState(true);
            emptyStateText.setText("📰 No articles found.\nTry a different search.");
        } else {
            showEmptyState(false);
        }
    }

    private void showEmptyState(boolean show) {
        if (emptyStateLayout != null) {
            if (show) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                emptyStateLayout.startAnimation(
                        AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_scale));
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
                    android.R.anim.fade_out);
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

        if (article.getId() == null || article.getId().isEmpty()) {
            Toast.makeText(getContext(), "❌ Missing article id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newFavoriteState) {
            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(article.getId())
                    .set(article)
                    .addOnSuccessListener(aVoid -> {
                        article.setFavorite(true);
                        favoriteIds.add(article.getId());
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "❤️ Added to favorites!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(article.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        article.setFavorite(false);
                        favoriteIds.remove(article.getId());
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "💔 Removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!allArticles.isEmpty()) {
            refreshFavoriteIdsThenRender();
        }
    }
}
