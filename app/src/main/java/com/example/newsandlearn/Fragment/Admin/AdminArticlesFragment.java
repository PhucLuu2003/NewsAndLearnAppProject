package com.example.newsandlearn.Fragment.Admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Adapter.AdminArticleAdapter;
import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AdminArticlesFragment - Manage articles (CRUD operations)
 */
public class AdminArticlesFragment extends Fragment implements AdminArticleAdapter.OnArticleActionListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnAddArticle, btnSeedArticles;
    private List<Article> articles;
    private AdminArticleAdapter adapter;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_articles, container, false);
        
        db = FirebaseFirestore.getInstance();
        initializeViews(view);
        loadArticles();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.articles_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        btnAddArticle = view.findViewById(R.id.btn_add_article);
        btnSeedArticles = view.findViewById(R.id.btn_seed_articles);
        
        articles = new ArrayList<>();
        adapter = new AdminArticleAdapter(getContext(), articles, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        
        btnAddArticle.setOnClickListener(v -> showAddArticleDialog());
        btnSeedArticles.setOnClickListener(v -> seedArticles());
        swipeRefresh.setOnRefreshListener(this::loadArticles);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadArticles() {
        swipeRefresh.setRefreshing(true);
        
        db.collection("articles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    articles.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Article article = document.toObject(Article.class);
                        articles.add(article);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void showAddArticleDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_article, null);
        
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        EditText etCategory = dialogView.findViewById(R.id.et_category);
        EditText etImageUrl = dialogView.findViewById(R.id.et_image_url);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Article")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    String imageUrl = etImageUrl.getText().toString().trim();
                    
                    if (title.isEmpty() || content.isEmpty()) {
                        Toast.makeText(getContext(), "Title and content are required", 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    addArticle(title, content, category, imageUrl);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addArticle(String title, String content, String category, String imageUrl) {
        String articleId = db.collection("articles").document().getId();
        
        Article article = new Article();
        article.setId(articleId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategory(category.isEmpty() ? "General" : category);
        article.setImageUrl(imageUrl.isEmpty() ? "https://via.placeholder.com/400x200" : imageUrl);
        article.setAuthor("Admin");
        article.setPublishedDate(new Date());
        article.setReadTime(String.valueOf(calculateReadTime(content)));
        
        db.collection("articles")
                .document(articleId)
                .set(article)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Article added successfully", 
                        Toast.LENGTH_SHORT).show();
                    loadArticles();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    private int calculateReadTime(String content) {
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200); // Assuming 200 words per minute
    }

    @Override
    public void onEditArticle(Article article) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_article, null);
        
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        EditText etCategory = dialogView.findViewById(R.id.et_category);
        EditText etImageUrl = dialogView.findViewById(R.id.et_image_url);
        
        // Pre-fill with existing data
        etTitle.setText(article.getTitle());
        etContent.setText(article.getContent());
        etCategory.setText(article.getCategory());
        etImageUrl.setText(article.getImageUrl());
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Article")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    article.setTitle(etTitle.getText().toString().trim());
                    article.setContent(etContent.getText().toString().trim());
                    article.setCategory(etCategory.getText().toString().trim());
                    article.setImageUrl(etImageUrl.getText().toString().trim());
                    article.setReadTime(String.valueOf(calculateReadTime(article.getContent())));
                    
                    updateArticle(article);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateArticle(Article article) {
        db.collection("articles")
                .document(article.getId())
                .set(article)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Article updated", Toast.LENGTH_SHORT).show();
                    loadArticles();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteArticle(Article article) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Article")
                .setMessage("Are you sure you want to delete \"" + article.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("articles")
                            .document(article.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "✅ Article deleted", 
                                    Toast.LENGTH_SHORT).show();
                                loadArticles();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void seedArticles() {
        progressDialog.setTitle("Seeding Articles");
        progressDialog.setMessage("Creating sample articles...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedArticles(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_LONG).show();
                        loadArticles();
                    });
                }
            }

            @Override
            public void onProgress(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> progressDialog.setMessage(message));
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "❌ Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
