package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.AdminStringAdapter;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.RoleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminFirestoreActivity extends AppCompatActivity {

    public static final String EXTRA_COLLECTION = "extra_collection";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private AdminStringAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_firestore);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        adapter = new AdminStringAdapter(value -> {
            Intent intent = new Intent(AdminFirestoreActivity.this, AdminCollectionActivity.class);
            intent.putExtra(EXTRA_COLLECTION, value);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setTitle("Admin • Firestore");

        gateAdminAndLoad();
    }

    private void gateAdminAndLoad() {
        progressBar.setVisibility(View.VISIBLE);
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(AdminFirestoreActivity.this, "Bạn không có quyền Admin", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                List<String> collections = new ArrayList<>(Arrays.asList(
                        "users",
                        "articles",
                        "announcements",
                        "app_config",
                        "leaderboards",

                        "vocabularies",
                        "vocabulary_sets",
                        "vocabulary_categories",
                        "survival_words",

                        "phonics_lessons",
                        "video_lessons",

                        "reading_articles",
                        "reading_lessons",
                        "writing_lessons",
                        "listening_lessons",
                        "speaking_lessons",
                        "grammar_lessons",

                        "flashcard_decks",
                        "quiz_templates",
                        "millionaire_questions",

                        "rpg_characters",
                        "rpg_enemies",
                        "rpg_items",
                        "rpg_battles",

                        "analytics",
                        "activity_logs",
                        "comments"));

                adapter.setItems(collections);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminFirestoreActivity.this, "Không kiểm tra được quyền: " + error, Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        });
    }
}
