package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Activity.FlashcardActivity;
import com.example.newsandlearn.Adapter.VocabularyAdapter;
import com.example.newsandlearn.Dialog.AddWordDialog;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * VocabularyFragment - Displays user's vocabulary with filtering and stats
 */
public class VocabularyFragment extends Fragment {

    private static final String TAG = "VocabularyFragment";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView vocabularyRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private TextView wordsLearnedCount, toReviewCount, masteryPercentage;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipLearning, chipMastered, chipFavorites;
    private MaterialButton practiceButton, addWordButton;

    // Data
    private List<Vocabulary> allVocabulary;
    private List<Vocabulary> filteredVocabulary;
    private VocabularyAdapter adapter;
    private String currentFilter = "all";

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextToSpeech tts;
    private ProgressManager progressManager;

    public VocabularyFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        setupTextToSpeech();
        loadVocabulary();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
        allVocabulary = new ArrayList<>();
        filteredVocabulary = new ArrayList<>();
    }

    private void initializeViews(View view) {
        // Stats
        wordsLearnedCount = view.findViewById(R.id.words_learned_count);
        toReviewCount = view.findViewById(R.id.to_review_count);
        masteryPercentage = view.findViewById(R.id.mastery_percentage);

        // Filter chips
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        chipAll = view.findViewById(R.id.chip_all);
        chipLearning = view.findViewById(R.id.chip_learning);
        chipMastered = view.findViewById(R.id.chip_mastered);
        chipFavorites = view.findViewById(R.id.chip_favorites);

        // List
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        vocabularyRecyclerView = view.findViewById(R.id.vocabulary_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);

        // Buttons
        practiceButton = view.findViewById(R.id.practice_button);
        addWordButton = view.findViewById(R.id.add_word_button);
    }

    private void setupRecyclerView() {
        vocabularyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VocabularyAdapter(getContext(), filteredVocabulary, new VocabularyAdapter.VocabularyListener() {
            @Override
            public void onVocabularyClick(Vocabulary vocabulary) {
                // TODO: Open detail view
                Toast.makeText(getContext(), "Detail: " + vocabulary.getWord(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSpeakerClick(Vocabulary vocabulary) {
                speakWord(vocabulary.getWord());
            }

            @Override
            public void onFavoriteClick(Vocabulary vocabulary) {
                toggleFavorite(vocabulary);
            }
        });
        vocabularyRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Pull to refresh
        swipeRefresh.setOnRefreshListener(this::loadVocabulary);

        // Filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
            } else if (checkedId == R.id.chip_learning) {
                currentFilter = "learning";
            } else if (checkedId == R.id.chip_mastered) {
                currentFilter = "mastered";
            } else if (checkedId == R.id.chip_favorites) {
                currentFilter = "favorites";
            }
            filterVocabulary();
        });

        // Practice button
        practiceButton.setOnClickListener(v -> {
            if (filteredVocabulary.isEmpty()) {
                Toast.makeText(getContext(), "No vocabulary to practice", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), FlashcardActivity.class);
            intent.putExtra("review_only", currentFilter.equals("learning"));
            startActivity(intent);
        });

        // Add word button
        addWordButton.setOnClickListener(v -> {
            AddWordDialog dialog = AddWordDialog.newInstance();
            dialog.setOnWordAddedListener(() -> {
                // Refresh vocabulary list
                loadVocabulary();
            });
            dialog.show(getParentFragmentManager(), "AddWordDialog");
        });
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void loadVocabulary() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading(true);
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("vocabulary")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allVocabulary.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vocabulary vocab = document.toObject(Vocabulary.class);
                        if (vocab != null) {
                            allVocabulary.add(vocab);
                        }
                    }

                    updateStats();
                    filterVocabulary();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading vocabulary", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void filterVocabulary() {
        filteredVocabulary.clear();

        for (Vocabulary vocab : allVocabulary) {
            boolean shouldInclude = false;

            switch (currentFilter) {
                case "all":
                    shouldInclude = true;
                    break;
                case "learning":
                    shouldInclude = vocab.getMastery() > 0 && vocab.getMastery() < 5;
                    break;
                case "mastered":
                    shouldInclude = vocab.getMastery() == 5;
                    break;
                case "favorites":
                    shouldInclude = vocab.isFavorite();
                    break;
            }

            if (shouldInclude) {
                filteredVocabulary.add(vocab);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredVocabulary.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateStats() {
        int totalWords = allVocabulary.size();
        int toReview = 0;
        int totalMastery = 0;

        for (Vocabulary vocab : allVocabulary) {
            if (vocab.needsReview()) {
                toReview++;
            }
            totalMastery += vocab.getMastery();
        }

        wordsLearnedCount.setText(String.valueOf(totalWords));
        toReviewCount.setText(String.valueOf(toReview));

        if (totalWords > 0) {
            int avgMastery = (totalMastery * 100) / (totalWords * 5); // 5 is max mastery
            masteryPercentage.setText(avgMastery + "%");
        } else {
            masteryPercentage.setText("0%");
        }
    }

    private void speakWord(String word) {
        if (tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void toggleFavorite(Vocabulary vocabulary) {
        vocabulary.setFavorite(!vocabulary.isFavorite());
        adapter.notifyDataSetChanged();

        // Save to Firebase
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("vocabulary").document(vocabulary.getId())
                    .set(vocabulary)
                    .addOnSuccessListener(aVoid -> {
                        String message = vocabulary.isFavorite() ? 
                                "Added to favorites" : "Removed from favorites";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            vocabularyRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        vocabularyRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        vocabularyRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadVocabulary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
