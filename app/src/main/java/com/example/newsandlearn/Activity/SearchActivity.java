package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.DynamicArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements DynamicArticleAdapter.OnArticleClickListener {

    private EditText searchInput;
    private ImageButton backButton, clearButton;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    
    private DynamicArticleAdapter adapter;
    private List<Article> allArticles = new ArrayList<>();
    private List<Article> filteredArticles = new ArrayList<>();
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        searchInput = findViewById(R.id.search_input);
        backButton = findViewById(R.id.back_button_search);
        clearButton = findViewById(R.id.clear_button);
        searchResultsRecyclerView = findViewById(R.id.search_results);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        emptyStateText = findViewById(R.id.empty_state_text);

        // Setup RecyclerView
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DynamicArticleAdapter(this, this);
        searchResultsRecyclerView.setAdapter(adapter);

        // Setup listeners
        backButton.setOnClickListener(v -> finish());

        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.requestFocus();
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
                // Show/hide clear button
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Load all articles from Firebase
        loadAllArticles();
        
        // Focus on search input and show keyboard
        searchInput.requestFocus();
    }

    private void loadAllArticles() {
        showLoading(true);
        
        db.collection("articles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allArticles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        article.setId(document.getId());
                        allArticles.add(article);
                    }
                    showLoading(false);
                    
                    // If there's already text in search, perform search
                    String currentQuery = searchInput.getText().toString();
                    if (!currentQuery.isEmpty()) {
                        performSearch(currentQuery);
                    } else {
                        showEmptyState(true, "🔍 Start typing to search articles...");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "❌ Error loading articles: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState(true, "⚠️ Failed to load articles.\nPlease try again.");
                });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            filteredArticles.clear();
            adapter.setArticles(filteredArticles);
            showEmptyState(true, "🔍 Start typing to search articles...");
            return;
        }

        // Filter articles based on query
        filteredArticles.clear();
        String lowerQuery = query.toLowerCase().trim();
        
        for (Article article : allArticles) {
            boolean matchesTitle = article.getTitle() != null && 
                    article.getTitle().toLowerCase().contains(lowerQuery);
            boolean matchesCategory = article.getCategory() != null && 
                    article.getCategory().toLowerCase().contains(lowerQuery);
            boolean matchesSource = article.getSource() != null && 
                    article.getSource().toLowerCase().contains(lowerQuery);
            boolean matchesLevel = article.getLevel() != null && 
                    article.getLevel().toLowerCase().contains(lowerQuery);
            
            if (matchesTitle || matchesCategory || matchesSource || matchesLevel) {
                filteredArticles.add(article);
            }
        }
        
        // Update adapter
        adapter.setArticles(filteredArticles);
        
        // Show/hide empty state
        if (filteredArticles.isEmpty()) {
            showEmptyState(true, "😔 No results found for \"" + query + "\"");
        } else {
            showEmptyState(false, "");
        }
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(boolean show, String message) {
        if (emptyStateLayout != null && emptyStateText != null) {
            emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
            searchResultsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            emptyStateText.setText(message);
        }
    }

    @Override
    public void onArticleClick(Article article) {
        Intent intent = new Intent(this, EnhancedArticleDetailActivity.class);
        intent.putExtra("article_id", article.getId());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onFavoriteClick(Article article) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "⚠️ Please login to save favorites", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
}
