package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Activity.EnhancedArticleDetailActivity;
import com.example.newsandlearn.Adapter.ArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private RecyclerView favoritesRecyclerView;
    private LinearLayout emptyStateLayout;
    private ArticleAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Article> favoriteArticles;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        favoriteArticles = new ArrayList<>();

        // Initialize views
        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        // Setup RecyclerView
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticleAdapter(getContext(), new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(Article article) {
                openArticleDetail(article);
            }

            @Override
            public void onFavoriteClick(Article article) {
                removeFavorite(article);
            }
        });
        favoritesRecyclerView.setAdapter(adapter);

        // Load favorites
        loadFavorites();

        return view;
    }

    private void loadFavorites() {
        if (mAuth.getCurrentUser() == null) {
            favoriteArticles.clear();
            showEmptyState();
            Toast.makeText(getContext(), "⚠️ Please login to view favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteArticles.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Article a = doc.toObject(Article.class);
                        a.setId(doc.getId());
                        a.setFavorite(true);
                        favoriteArticles.add(a);
                    }

                    if (favoriteArticles.isEmpty()) {
                        showEmptyState();
                    } else {
                        showFavorites();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
    }

    private void showFavorites() {
        emptyStateLayout.setVisibility(View.GONE);
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        adapter.setArticles(favoriteArticles);
    }

    private void openArticleDetail(Article article) {
        Intent intent = new Intent(getActivity(), EnhancedArticleDetailActivity.class);
        // Pass article ID to load from Firebase
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    private void removeFavorite(Article article) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "⚠️ Please login", Toast.LENGTH_SHORT).show();
            return;
        }

        if (article.getId() == null || article.getId().isEmpty()) {
            Toast.makeText(getContext(), "❌ Missing article id", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(article.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    favoriteArticles.remove(article);
                    adapter.setArticles(favoriteArticles);
                    if (favoriteArticles.isEmpty()) {
                        showEmptyState();
                    }
                    Toast.makeText(getContext(), "💔 Removed from favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(
                        e -> Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}
