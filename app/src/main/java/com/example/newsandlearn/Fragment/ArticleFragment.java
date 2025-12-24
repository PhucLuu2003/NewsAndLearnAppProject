package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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

        // Load articles with animation
        new Handler().postDelayed(this::loadArticles, 300);

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_articles);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
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
        // Scroll listener removed since search is no longer present
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

    // Search and filter methods removed - showing all articles

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
                    
                    adapter.setArticles(allArticles);
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
            adapter.setArticles(allArticles);
        }
    }
}

