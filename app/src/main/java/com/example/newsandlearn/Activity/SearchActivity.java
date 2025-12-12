package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.ArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton backButton, clearButton;
    private RecyclerView searchResultsRecyclerView;
    private ArticleAdapter adapter;
    private List<Article> allArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        searchInput = findViewById(R.id.search_input);
        backButton = findViewById(R.id.back_button_search);
        clearButton = findViewById(R.id.clear_button);
        searchResultsRecyclerView = findViewById(R.id.search_results);

        // Setup RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(this, new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(Article article) {
                // TODO: Open article detail
            }

            @Override
            public void onFavoriteClick(Article article) {
                // TODO: Toggle favorite
            }
        });
        searchResultsRecyclerView.setAdapter(adapter);

        // Setup listeners
        backButton.setOnClickListener(v -> finish());

        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Focus on search input
        searchInput.requestFocus();
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            adapter.setArticles(new ArrayList<>());
            return;
        }

        // TODO: Implement actual search from Firebase
        List<Article> results = new ArrayList<>();
        // Filter articles based on query
        adapter.setArticles(results);
    }
}
