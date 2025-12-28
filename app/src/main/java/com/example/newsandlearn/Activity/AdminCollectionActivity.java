package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.AdminDocumentAdapter;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.RoleManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminCollectionActivity extends AppCompatActivity {

    public static final String EXTRA_DOC_ID = "extra_doc_id";

    private String collectionName;

    private TextView tvTitle;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private AdminDocumentAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_collection);

        collectionName = getIntent().getStringExtra(AdminFirestoreActivity.EXTRA_COLLECTION);
        if (collectionName == null || collectionName.trim().isEmpty()) {
            Toast.makeText(this, "Missing collection", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tv_title);
        progressBar = findViewById(R.id.progress_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        fabAdd = findViewById(R.id.fab_add);

        tvTitle.setText("Collection: " + collectionName);
        setTitle("Admin • " + collectionName);

        adapter = new AdminDocumentAdapter(doc -> {
            Intent intent = new Intent(AdminCollectionActivity.this, AdminDocumentEditorActivity.class);
            intent.putExtra(AdminFirestoreActivity.EXTRA_COLLECTION, collectionName);
            intent.putExtra(EXTRA_DOC_ID, doc.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadDocuments);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(AdminCollectionActivity.this, AdminDocumentEditorActivity.class);
            intent.putExtra(AdminFirestoreActivity.EXTRA_COLLECTION, collectionName);
            startActivity(intent);
        });

        gateAdminThenLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload after returning from editor.
        if (collectionName != null) {
            loadDocuments();
        }
    }

    private void gateAdminThenLoad() {
        progressBar.setVisibility(View.VISIBLE);
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(AdminCollectionActivity.this, "Bạn không có quyền Admin", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                loadDocuments();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminCollectionActivity.this, "Không kiểm tra được quyền: " + error, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }

    private void loadDocuments() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(collectionName)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    adapter.setDocuments(queryDocumentSnapshots.getDocuments());
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminCollectionActivity.this, "Load failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                });
    }
}
