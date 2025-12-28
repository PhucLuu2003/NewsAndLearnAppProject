package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.newsandlearn.Adapter.SurvivalWordAdapter;
import com.example.newsandlearn.Model.SurvivalWord;
import com.example.newsandlearn.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📚 CategoryWordsActivity - View and learn words in a specific category
 * Displays words as flashcards with swipe navigation
 */
public class CategoryWordsActivity extends AppCompatActivity implements SurvivalWordAdapter.OnWordActionListener {

    // Intent extras
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    // UI Components
    private Toolbar toolbar;
    private TextView tvTitle, tvProgress, tvEmpty;
    private RecyclerView recyclerWords;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;
    private ExtendedFloatingActionButton fabShuffle;

    // Adapter & Data
    private SurvivalWordAdapter adapter;
    private List<SurvivalWord> words = new ArrayList<>();
    private String categoryId, categoryName;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_words);

        // Get intent data
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        initFirebase();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadWords();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvProgress = findViewById(R.id.tv_progress);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerWords = findViewById(R.id.recycler_words);
        progressLoading = findViewById(R.id.progress_loading);
        emptyState = findViewById(R.id.empty_state);
        fabShuffle = findViewById(R.id.fab_shuffle);

        tvTitle.setText(categoryName != null ? categoryName : "Vocabulary");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void setupRecyclerView() {
        adapter = new SurvivalWordAdapter(this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerWords.setLayoutManager(layoutManager);
        recyclerWords.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabShuffle.setOnClickListener(v -> {
            animateFab(v);
            shuffleWords();
        });
    }

    private void animateFab(View v) {
        v.animate()
                .rotation(v.getRotation() + 360)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void loadWords() {
        if (categoryName == null) {
            showEmptyState(true);
            return;
        }

        showLoading(true);

        db.collection("survival_words")
                .whereEqualTo("category", categoryName)
                .orderBy("word", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    words.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        SurvivalWord word = doc.toObject(SurvivalWord.class);
                        if (word != null) {
                            word.setId(doc.getId());
                            words.add(word);
                        }
                    }

                    if (words.isEmpty()) {
                        showLoading(false);
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        loadUserProgress();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProgress() {
        if (userId == null) {
            showLoading(false);
            adapter.setWords(words);
            updateProgress();
            return;
        }

        db.collection("users").document(userId)
                .collection("vocab_progress")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);

                    Map<String, Map<String, Object>> progressMap = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        progressMap.put(doc.getId(), doc.getData());
                    }

                    for (SurvivalWord word : words) {
                        Map<String, Object> progress = progressMap.get(word.getId());
                        if (progress != null) {
                            Number srsLevel = (Number) progress.get("srsLevel");
                            Number nextReview = (Number) progress.get("nextReviewTime");
                            Boolean learned = (Boolean) progress.get("learned");

                            if (srsLevel != null)
                                word.setSrsLevel(srsLevel.intValue());
                            if (nextReview != null)
                                word.setNextReviewTime(nextReview.longValue());
                            if (learned != null)
                                word.setLearned(learned);
                        }
                    }

                    adapter.setWords(words);
                    updateProgress();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    adapter.setWords(words);
                    updateProgress();
                });
    }

    private void updateProgress() {
        int learned = 0;
        for (SurvivalWord word : words) {
            if (word.isLearned())
                learned++;
        }
        tvProgress.setText(learned + "/" + words.size() + " learned");
    }

    private void shuffleWords() {
        if (words.isEmpty())
            return;

        java.util.Collections.shuffle(words);
        adapter.setWords(words);
        recyclerWords.scrollToPosition(0);
        Toast.makeText(this, "🔀 Words shuffled!", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerWords.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerWords.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ========== Adapter Callbacks ==========

    @Override
    public void onWordClick(SurvivalWord word, int position) {
        // Could open detail view
    }

    @Override
    public void onKnowClick(SurvivalWord word, int position) {
        updateWordProgress(word, position, true);
    }

    @Override
    public void onLearnClick(SurvivalWord word, int position) {
        updateWordProgress(word, position, false);
    }

    @Override
    public void onPlaySound(SurvivalWord word, int position) {
        Toast.makeText(this, "🔊 " + word.getWord(), Toast.LENGTH_SHORT).show();
    }

    private void updateWordProgress(SurvivalWord word, int position, boolean known) {
        if (userId == null)
            return;

        int newSrsLevel = known ? Math.min(word.getSrsLevel() + 1, 3) : Math.max(word.getSrsLevel() - 1, 0);
        long now = System.currentTimeMillis();

        long[] intervals = {
                60 * 60 * 1000L,
                24 * 60 * 60 * 1000L,
                3 * 24 * 60 * 60 * 1000L,
                7 * 24 * 60 * 60 * 1000L
        };
        long nextReview = now + intervals[newSrsLevel];

        Map<String, Object> progress = new HashMap<>();
        progress.put("srsLevel", newSrsLevel);
        progress.put("nextReviewTime", nextReview);
        progress.put("reviewCount", word.getReviewCount() + 1);
        progress.put("lastReviewTime", now);
        progress.put("learned", true);

        db.collection("users").document(userId)
                .collection("vocab_progress")
                .document(word.getId())
                .set(progress)
                .addOnSuccessListener(aVoid -> {
                    word.setSrsLevel(newSrsLevel);
                    word.setNextReviewTime(nextReview);
                    word.setReviewCount(word.getReviewCount() + 1);
                    word.setLearned(true);

                    adapter.notifyItemChanged(position);
                    updateProgress();

                    String msg = known ? "✅ Great!" : "📖 Keep practicing!";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null)
            adapter.release();
    }
}
