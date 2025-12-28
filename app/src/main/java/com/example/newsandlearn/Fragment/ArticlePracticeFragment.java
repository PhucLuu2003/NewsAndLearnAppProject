package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.newsandlearn.Activity.FlashcardActivity;
import com.example.newsandlearn.Adapter.PracticeVocabularySelectAdapter;
import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularyWithProgress;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArticlePracticeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MaterialButton practiceButton;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    private ProgressBar loadingProgress;

    private PracticeVocabularySelectAdapter adapter;
    private final List<VocabularyWithProgress> items = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_practice, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recycler_practice_vocab);
        practiceButton = view.findViewById(R.id.btn_practice_selected);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        emptyStateText = view.findViewById(R.id.empty_state_text);
        loadingProgress = view.findViewById(R.id.loading_progress);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PracticeVocabularySelectAdapter(items, selectedCount -> {
            practiceButton.setEnabled(selectedCount > 0);
        });
        recyclerView.setAdapter(adapter);

        practiceButton.setOnClickListener(v -> startPracticeSelected());

        loadUserVocabulary();

        return view;
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show, String message) {
        emptyStateLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null)
            emptyStateText.setText(message);
    }

    private void loadUserVocabulary() {
        if (auth.getCurrentUser() == null) {
            showEmptyState(true, "Please login first");
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        showLoading(true);
        showEmptyState(false, null);

        db.collection("users").document(userId)
                .collection("user_vocabulary")
                .get()
                .addOnSuccessListener(userVocabSnapshot -> {
                    items.clear();
                    adapter.clearSelection();
                    practiceButton.setEnabled(false);

                    List<String> vocabIds = new ArrayList<>();
                    Map<String, UserVocabulary> progressMap = new HashMap<>();

                    userVocabSnapshot.forEach(doc -> {
                        UserVocabulary progress = doc.toObject(UserVocabulary.class);
                        if (progress != null && progress.getVocabularyId() != null) {
                            vocabIds.add(progress.getVocabularyId());
                            progressMap.put(progress.getVocabularyId(), progress);
                        }
                    });

                    if (vocabIds.isEmpty()) {
                        showLoading(false);
                        showEmptyState(true, getString(R.string.no_saved_words_yet));
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    loadVocabularyDetails(vocabIds, progressMap);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true, "Failed to load saved words");
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadVocabularyDetails(List<String> vocabIds, Map<String, UserVocabulary> progressMap) {
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < vocabIds.size(); i += 10) {
            batches.add(vocabIds.subList(i, Math.min(i + 10, vocabIds.size())));
        }

        final int[] completed = { 0 };

        for (List<String> batch : batches) {
            db.collection("vocabularies")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        querySnapshot.forEach(doc -> {
                            Vocabulary vocab = doc.toObject(Vocabulary.class);
                            if (vocab != null) {
                                vocab.setId(doc.getId());
                                VocabularyWithProgress combined = new VocabularyWithProgress();
                                combined.setVocabulary(vocab);
                                combined.setUserProgress(progressMap.get(vocab.getId()));
                                items.add(combined);
                            }
                        });

                        completed[0]++;
                        if (completed[0] >= batches.size()) {
                            showLoading(false);
                            showEmptyState(items.isEmpty(),
                                    items.isEmpty() ? getString(R.string.no_saved_words_yet) : null);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        if (completed[0] >= batches.size()) {
                            showLoading(false);
                            showEmptyState(items.isEmpty(),
                                    items.isEmpty() ? getString(R.string.no_saved_words_yet) : null);
                            adapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void startPracticeSelected() {
        Set<String> selected = adapter.getSelectedIds();
        if (selected.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.select_words_to_practice), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), FlashcardActivity.class);
        intent.putStringArrayListExtra(FlashcardActivity.EXTRA_VOCAB_IDS, new ArrayList<>(selected));
        startActivity(intent);
    }
}
