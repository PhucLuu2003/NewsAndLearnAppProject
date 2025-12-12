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

import com.example.newsandlearn.Activity.ArticleDetailActivity;
import com.example.newsandlearn.Adapter.ArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        // TODO: Load from Firebase
        // For now, show empty state
        if (favoriteArticles.isEmpty()) {
            showEmptyState();
        } else {
            showFavorites();
        }
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
        Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    private void removeFavorite(Article article) {
        favoriteArticles.remove(article);
        adapter.setArticles(favoriteArticles);

        if (favoriteArticles.isEmpty()) {
            showEmptyState();
        }

        Toast.makeText(getContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
    }
}
