package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.VideoLessonsAdapter;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AnimationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display all video lessons in a grid layout with filtering options
 */
public class AllVideosActivity extends AppCompatActivity {

    private RecyclerView videosRecyclerView;
    private VideoLessonsAdapter adapter;
    private ProgressBar loadingIndicator;
    private TextView emptyState;
    private ImageView backButton;
    private ChipGroup levelFilterChips;
    private Chip chipAllLevels, chipA1, chipA2, chipB1, chipB2, chipC1;

    private FirebaseFirestore db;
    private List<VideoLesson> allVideos = new ArrayList<>();
    private List<VideoLesson> filteredVideos = new ArrayList<>();
    private String currentLevel = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_videos);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadAllVideos();
    }

    private void initializeViews() {
        videosRecyclerView = findViewById(R.id.videos_recycler_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyState = findViewById(R.id.empty_state);
        backButton = findViewById(R.id.back_button);
        levelFilterChips = findViewById(R.id.level_filter_chips);
        chipAllLevels = findViewById(R.id.chip_all_levels);
        chipA1 = findViewById(R.id.chip_a1);
        chipA2 = findViewById(R.id.chip_a2);
        chipB1 = findViewById(R.id.chip_b1);
        chipB2 = findViewById(R.id.chip_b2);
        chipC1 = findViewById(R.id.chip_c1);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        videosRecyclerView.setLayoutManager(layoutManager);
        
        adapter = new VideoLessonsAdapter(this, filteredVideos);
        videosRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            AnimationHelper.scaleUp(this, backButton);
            onBackPressed();
        });

        // Level filter chips
        chipAllLevels.setOnClickListener(v -> filterByLevel("all"));
        chipA1.setOnClickListener(v -> filterByLevel("A1"));
        chipA2.setOnClickListener(v -> filterByLevel("A2"));
        chipB1.setOnClickListener(v -> filterByLevel("B1"));
        chipB2.setOnClickListener(v -> filterByLevel("B2"));
        chipC1.setOnClickListener(v -> filterByLevel("C1"));
    }

    private void loadAllVideos() {
        loadingIndicator.setVisibility(View.VISIBLE);
        videosRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("video_lessons")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allVideos.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        VideoLesson lesson = document.toObject(VideoLesson.class);
                        if (lesson != null) {
                            allVideos.add(lesson);
                        }
                    }
                    filterByLevel(currentLevel);
                    loadingIndicator.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingIndicator.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                });
    }

    private void filterByLevel(String level) {
        currentLevel = level;
        filteredVideos.clear();

        if (level.equals("all")) {
            filteredVideos.addAll(allVideos);
        } else {
            for (VideoLesson video : allVideos) {
                if (video.getLevel() != null && video.getLevel().equalsIgnoreCase(level)) {
                    filteredVideos.add(video);
                }
            }
        }

        adapter.updateData(filteredVideos);

        // Update UI
        if (filteredVideos.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            videosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            videosRecyclerView.setVisibility(View.VISIBLE);
        }

        // Update chip selection
        updateChipSelection(level);
    }

    private void updateChipSelection(String level) {
        chipAllLevels.setChecked(level.equals("all"));
        chipA1.setChecked(level.equals("A1"));
        chipA2.setChecked(level.equals("A2"));
        chipB1.setChecked(level.equals("B1"));
        chipB2.setChecked(level.equals("B2"));
        chipC1.setChecked(level.equals("C1"));
    }
}
