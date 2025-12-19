package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.EnhancedArticleDetailActivity;
import com.example.newsandlearn.Adapter.ArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ArticleFragment - Hiển thị danh sách bài báo để đọc
 */
public class ArticleFragment extends Fragment implements ArticleAdapter.OnArticleClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ArticleAdapter adapter;
    private List<Article> articles;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_articles);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Setup RecyclerView
        articles = new ArrayList<>();
        adapter = new ArticleAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup SwipeRefresh
        swipeRefresh.setOnRefreshListener(this::loadArticles);
        swipeRefresh.setColorSchemeResources(R.color.primary);

        // Load articles
        loadArticles();

        return view;
    }

    private void loadArticles() {
        swipeRefresh.setRefreshing(true);

        db.collection("articles")
                .orderBy("publishedDate", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    articles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        articles.add(article);
                    }
                    adapter.setArticles(articles);
                    swipeRefresh.setRefreshing(false);

                    if (articles.isEmpty()) {
                        Toast.makeText(getContext(), "No articles found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading articles: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(getContext(), EnhancedArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Article article) {
        // TODO: Implement favorite functionality
        Toast.makeText(getContext(), "Favorite: " + article.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh articles when returning to fragment
        if (adapter != null && !articles.isEmpty()) {
            adapter.notifyDataSetChanged();
        }
    }
}
