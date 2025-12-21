package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
import com.example.newsandlearn.Activity.QuizActivity;
import com.example.newsandlearn.Adapter.VocabularyAdapter;
import com.example.newsandlearn.Dialog.AddWordDialog;
import com.example.newsandlearn.Fragment.VocabularySetsFragment;
import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularyWithProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.ProgressManager;
import com.example.newsandlearn.Utils.SampleDataHelper;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * VocabularyFragment - Displays user's vocabulary with filtering and stats
 * Uses new two-collection structure: vocabularies (public) + user_vocabulary
 * (progress)
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
    private ExtendedFloatingActionButton practiceButton;
    private FloatingActionButton addWordButton;
    private MaterialButton quizButton, btnAddSampleData;
    private MaterialCardView setsButton;

    // Data
    private List<VocabularyWithProgress> allVocabulary;
    private List<VocabularyWithProgress> filteredVocabulary;
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
        View view = inflater.inflate(R.layout.fragment_vocabulary_enhanced, container, false);

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
        vocabularyRecyclerView = view.findViewById(R.id.vocabulary_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);
        btnAddSampleData = view.findViewById(R.id.btn_add_sample_data);

        // Buttons - Updated for enhanced layout
        practiceButton = view.findViewById(R.id.practice_fab); // ExtendedFAB
        addWordButton = view.findViewById(R.id.add_word_fab); // Regular FAB
        
        // Quick action cards
        setsButton = view.findViewById(R.id.browse_sets_card); // MaterialCardView
        
        // Note: swipeRefresh removed in enhanced layout (uses NestedScrollView)
        swipeRefresh = null;
    }

    private void setupRecyclerView() {
        vocabularyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VocabularyAdapter(getContext(), filteredVocabulary, new VocabularyAdapter.VocabularyListener() {
            @Override
            public void onVocabularyClick(VocabularyWithProgress vocabulary) {
                // Open vocabulary detail activity
                Intent intent = new Intent(getActivity(), com.example.newsandlearn.Activity.VocabularyDetailActivity.class);
                intent.putExtra("vocabulary_id", vocabulary.getId());
                startActivity(intent);
            }

            @Override
            public void onSpeakerClick(VocabularyWithProgress vocabulary) {
                speakWord(vocabulary.getWord());
            }

            @Override
            public void onFavoriteClick(VocabularyWithProgress vocabulary) {
                toggleFavorite(vocabulary);
            }
        });
        vocabularyRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Pull to refresh (only if available in layout)
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadVocabulary);
        }

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

        // Sets button
        if (setsButton != null) {
            setsButton.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(getId(), new VocabularySetsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Add Sample Data Button
        if (btnAddSampleData != null) {
            btnAddSampleData.setOnClickListener(v -> {
                showLoading(true);
                new FirebaseDataSeeder().seedAllData(new FirebaseDataSeeder.SeedCallback() {
                    @Override
                    public void onSuccess(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                                loadVocabulary();
                            });
                        }
                    }

                    @Override
                    public void onProgress(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                String message = "Failed: " + error;
                                if (error.contains("PERMISSION_DENIED")) {
                                    message = "Permission Denied: Please update Firestore Rules (see FIRESTORE_SETUP.md)";
                                }
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            });
        }
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    /**
     * Load vocabulary using new two-collection structure
     * Step 1: Load user's vocabulary progress from user_vocabulary
     * Step 2: Load actual vocabulary data from vocabularies collection
     */
    private void loadVocabulary() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading(true);
        String userId = auth.getCurrentUser().getUid();

        // Step 1: Load user's vocabulary progress
        db.collection("users").document(userId)
                .collection("user_vocabulary")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> vocabIds = new ArrayList<>();
                    Map<String, UserVocabulary> progressMap = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserVocabulary userVocab = document.toObject(UserVocabulary.class);
                        if (userVocab != null && userVocab.getVocabularyId() != null) {
                            vocabIds.add(userVocab.getVocabularyId());
                            progressMap.put(userVocab.getVocabularyId(), userVocab);
                        }
                    }

                    if (vocabIds.isEmpty()) {
                        allVocabulary.clear();
                        updateStats();
                        filterVocabulary();
                        showLoading(false);
                        if (swipeRefresh != null) {
                            swipeRefresh.setRefreshing(false);
                        }
                        return;
                    }

                    // Step 2: Load actual vocabulary data in batches (Firestore limit is 10 for
                    // 'in' query)
                    loadVocabularyDetails(vocabIds, progressMap);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user vocabulary", e);
                    Toast.makeText(getContext(), "Error loading vocabulary", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    /**
     * Load vocabulary details from public vocabularies collection
     * Handles batching for Firestore's 10-item limit on 'in' queries
     */
    private void loadVocabularyDetails(List<String> vocabIds, Map<String, UserVocabulary> progressMap) {
        allVocabulary.clear();

        // Split into batches of 10 (Firestore limit)
        List<List<String>> batches = partition(vocabIds, 10);
        final int[] completedBatches = { 0 };

        for (List<String> batch : batches) {
            db.collection("vocabularies")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Vocabulary vocab = doc.toObject(Vocabulary.class);
                            if (vocab != null) {
                                vocab.setId(doc.getId());
                                UserVocabulary progress = progressMap.get(vocab.getId());

                                VocabularyWithProgress item = new VocabularyWithProgress();
                                item.setVocabulary(vocab);
                                item.setUserProgress(progress);
                                allVocabulary.add(item);
                            }
                        }

                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            // All batches completed
                            updateStats();
                            filterVocabulary();
                            showLoading(false);
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading vocabulary batch", e);
                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            Toast.makeText(getContext(), "Some vocabulary failed to load", Toast.LENGTH_SHORT).show();
                            updateStats();
                            filterVocabulary();
                            showLoading(false);
                            if (swipeRefresh != null) {
                                swipeRefresh.setRefreshing(false);
                            }
                        }
                    });
        }
    }

    /**
     * Helper method to partition list into chunks
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    private void filterVocabulary() {
        filteredVocabulary.clear();

        for (VocabularyWithProgress vocab : allVocabulary) {
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

        for (VocabularyWithProgress vocab : allVocabulary) {
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

    private void toggleFavorite(VocabularyWithProgress vocabulary) {
        UserVocabulary userProgress = vocabulary.getUserProgress();
        if (userProgress == null)
            return;

        userProgress.setFavorite(!userProgress.isFavorite());
        adapter.notifyDataSetChanged();

        // Save to Firebase
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("user_vocabulary").document(vocabulary.getId())
                    .update("favorite", userProgress.isFavorite())
                    .addOnSuccessListener(aVoid -> {
                        String message = userProgress.isFavorite() ? "Added to favorites" : "Removed from favorites";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating favorite", e);
                        // Revert on failure
                        userProgress.setFavorite(!userProgress.isFavorite());
                        adapter.notifyDataSetChanged();
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
