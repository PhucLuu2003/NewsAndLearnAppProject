package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.newsandlearn.Adapter.VocabularySetAdapter;
import com.example.newsandlearn.Model.VocabularySet;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.VocabularyHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * VocabularySetsFragment - Browse and manage vocabulary sets
 */
public class VocabularySetsFragment extends Fragment {

    private static final String TAG = "VocabularySetsFragment";

    // UI Components
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView setsRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipBeginner, chipIntermediate, chipAdvanced;

    // Data
    private List<VocabularySet> allSets;
    private List<VocabularySet> filteredSets;
    private VocabularySetAdapter adapter;
    private String currentTab = "public"; // "public" or "my_sets"
    private String currentFilter = "all";

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private VocabularyHelper vocabularyHelper;

    public VocabularySetsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocabulary_sets, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        loadSets();

        return view;
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vocabularyHelper = VocabularyHelper.getInstance();
        allSets = new ArrayList<>();
        filteredSets = new ArrayList<>();
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        setsRecyclerView = view.findViewById(R.id.sets_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        emptyState = view.findViewById(R.id.empty_state);

        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        chipAll = view.findViewById(R.id.chip_all);
        chipBeginner = view.findViewById(R.id.chip_beginner);
        chipIntermediate = view.findViewById(R.id.chip_intermediate);
        chipAdvanced = view.findViewById(R.id.chip_advanced);
    }

    private void setupRecyclerView() {
        setsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VocabularySetAdapter(getContext(), filteredSets, new VocabularySetAdapter.SetListener() {
            @Override
            public void onSetClick(VocabularySet set) {
                // TODO: Open set detail
                Toast.makeText(getContext(), "Set: " + set.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadClick(VocabularySet set) {
                downloadSet(set);
            }

            @Override
            public void onDeleteClick(VocabularySet set) {
                deleteSet(set);
            }
        });
        setsRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "public" : "my_sets";
                loadSets();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Pull to refresh
        swipeRefresh.setOnRefreshListener(this::loadSets);

        // Filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                currentFilter = "all";
            } else if (checkedId == R.id.chip_beginner) {
                currentFilter = "A1-A2";
            } else if (checkedId == R.id.chip_intermediate) {
                currentFilter = "B1-B2";
            } else if (checkedId == R.id.chip_advanced) {
                currentFilter = "C1-C2";
            }
            filterSets();
        });
    }

    private void loadSets() {
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        showLoading(true);

        if (currentTab.equals("public")) {
            loadPublicSets();
        } else {
            loadMySets();
        }
    }

    private void loadPublicSets() {
        db.collection("vocabulary_sets")
                .whereEqualTo("isPublic", true)
                .orderBy("downloadCount", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allSets.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        VocabularySet set = doc.toObject(VocabularySet.class);
                        if (set != null) {
                            set.setId(doc.getId());
                            allSets.add(set);
                        }
                    }
                    filterSets();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading public sets", e);
                    Toast.makeText(getContext(), "Error loading sets", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void loadMySets() {
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .collection("user_sets")
                .get()
                .addOnSuccessListener(userSetsSnapshot -> {
                    List<String> setIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : userSetsSnapshot) {
                        setIds.add(doc.getId());
                    }

                    if (setIds.isEmpty()) {
                        allSets.clear();
                        filterSets();
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        return;
                    }

                    // Load set details in batches
                    loadSetDetails(setIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading my sets", e);
                    Toast.makeText(getContext(), "Error loading sets", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void loadSetDetails(List<String> setIds) {
        allSets.clear();
        
        // Batch loading (10 items per batch)
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < setIds.size(); i += 10) {
            batches.add(setIds.subList(i, Math.min(i + 10, setIds.size())));
        }

        final int[] completedBatches = {0};

        for (List<String> batch : batches) {
            db.collection("vocabulary_sets")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            VocabularySet set = doc.toObject(VocabularySet.class);
                            if (set != null) {
                                set.setId(doc.getId());
                                allSets.add(set);
                            }
                        }

                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            filterSets();
                            showLoading(false);
                            swipeRefresh.setRefreshing(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading set details", e);
                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            filterSets();
                            showLoading(false);
                            swipeRefresh.setRefreshing(false);
                        }
                    });
        }
    }

    private void filterSets() {
        filteredSets.clear();

        for (VocabularySet set : allSets) {
            boolean shouldInclude = false;

            if (currentFilter.equals("all")) {
                shouldInclude = true;
            } else {
                String level = set.getLevel();
                if (level != null) {
                    if (currentFilter.equals("A1-A2") && (level.equals("A1") || level.equals("A2"))) {
                        shouldInclude = true;
                    } else if (currentFilter.equals("B1-B2") && (level.equals("B1") || level.equals("B2"))) {
                        shouldInclude = true;
                    } else if (currentFilter.equals("C1-C2") && (level.equals("C1") || level.equals("C2"))) {
                        shouldInclude = true;
                    }
                }
            }

            if (shouldInclude) {
                filteredSets.add(set);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredSets.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void downloadSet(VocabularySet set) {
        vocabularyHelper.downloadVocabularySet(set.getId())
                .addOnSuccessListener(message -> {
                    Toast.makeText(getContext(), "Set downloaded successfully!", Toast.LENGTH_SHORT).show();
                    // Reload if on my sets tab
                    if (currentTab.equals("my_sets")) {
                        loadSets();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error downloading set", e);
                    Toast.makeText(getContext(), "Error downloading set: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSet(VocabularySet set) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        
        // Delete from user_sets
        db.collection("users").document(userId)
                .collection("user_sets").document(set.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Set removed", Toast.LENGTH_SHORT).show();
                    loadSets();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting set", e);
                    Toast.makeText(getContext(), "Error removing set", Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingIndicator.setVisibility(View.VISIBLE);
            setsRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        setsRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        setsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSets();
    }
}
