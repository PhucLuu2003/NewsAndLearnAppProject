package com.example.newsandlearn.Fragment.Admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsandlearn.Adapter.AdminVocabularyAdapter;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminVocabularyFragment - Manage vocabulary words
 */
public class AdminVocabularyFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnAddWord, btnSeedWords;
    private AdminVocabularyAdapter adapter;
    private List<Vocabulary> vocabularies;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_vocabulary, container, false);
        
        db = FirebaseFirestore.getInstance();
        initializeViews(view);
        loadVocabularies();
        
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.vocabulary_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        btnAddWord = view.findViewById(R.id.btn_add_word);
        btnSeedWords = view.findViewById(R.id.btn_seed_words);
        
        vocabularies = new ArrayList<>();
        adapter = new AdminVocabularyAdapter(getContext(), vocabularies, this::onVocabularyAction);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        
        btnAddWord.setOnClickListener(v -> showAddWordDialog());
        btnSeedWords.setOnClickListener(v -> seedVocabularies());
        swipeRefresh.setOnRefreshListener(this::loadVocabularies);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadVocabularies() {
        swipeRefresh.setRefreshing(true);
        
        db.collection("vocabularies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vocabularies.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vocabulary vocab = document.toObject(Vocabulary.class);
                        if (vocab.getId() == null || vocab.getId().isEmpty()) {
                            vocab.setId(document.getId());
                        }
                        vocabularies.add(vocab);
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), 
                        "Loaded " + vocabularies.size() + " words", 
                        Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void onVocabularyAction(Vocabulary vocab, String action) {
        switch (action) {
            case "edit":
                showEditDialog(vocab);
                break;
            case "delete":
                deleteVocabulary(vocab);
                break;
            case "view":
                showDetailsDialog(vocab);
                break;
        }
    }

    private void showAddWordDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_vocabulary, null);
        
        TextInputEditText etWord = dialogView.findViewById(R.id.edit_word);
        TextInputEditText etTranslation = dialogView.findViewById(R.id.edit_meaning);
        TextInputEditText etPhonetic = dialogView.findViewById(R.id.edit_phonetic);
        TextInputEditText etExample = dialogView.findViewById(R.id.edit_context);
        TextInputEditText etCategory = dialogView.findViewById(R.id.edit_category);
        
        // Hide internal buttons as we use AlertDialog buttons
        View buttonContainer = dialogView.findViewById(R.id.button_container);
        if (buttonContainer != null) buttonContainer.setVisibility(View.GONE);
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add New Word")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String word = etWord.getText().toString().trim();
                    String translation = etTranslation.getText().toString().trim();
                    String phonetic = etPhonetic.getText().toString().trim();
                    String example = etExample.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    
                    if (word.isEmpty()) {
                        Toast.makeText(getContext(), "Word is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    addVocabulary(word, translation, phonetic, example, category);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Vocabulary vocab) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_vocabulary, null);
        
        TextInputEditText etWord = dialogView.findViewById(R.id.edit_word);
        TextInputEditText etTranslation = dialogView.findViewById(R.id.edit_meaning);
        TextInputEditText etPhonetic = dialogView.findViewById(R.id.edit_phonetic);
        TextInputEditText etExample = dialogView.findViewById(R.id.edit_context);
        TextInputEditText etCategory = dialogView.findViewById(R.id.edit_category);

        // Hide internal buttons as we use AlertDialog buttons
        View buttonContainer = dialogView.findViewById(R.id.button_container);
        if (buttonContainer != null) buttonContainer.setVisibility(View.GONE);
        
        // Pre-fill with existing data
        etWord.setText(vocab.getWord());
        etTranslation.setText(vocab.getTranslation());
        etPhonetic.setText(vocab.getPronunciation());
        etExample.setText(vocab.getExample());
        etCategory.setText(vocab.getCategory());
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Edit Word")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    vocab.setWord(etWord.getText().toString().trim());
                    vocab.setTranslation(etTranslation.getText().toString().trim());
                    vocab.setPronunciation(etPhonetic.getText().toString().trim());
                    vocab.setExample(etExample.getText().toString().trim());
                    vocab.setCategory(etCategory.getText().toString().trim());
                    
                    updateVocabulary(vocab);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDetailsDialog(Vocabulary vocab) {
        String details = "📝 Word: " + vocab.getWord() + "\n\n" +
                        "🌍 Translation: " + vocab.getTranslation() + "\n\n" +
                        "🔊 Phonetic: " + vocab.getPronunciation() + "\n\n" +
                        "📖 Example: " + vocab.getExample() + "\n\n" +
                        "🏷️ Category: " + vocab.getCategory() + "\n\n" +
                        "⭐ Level: " + vocab.getLevel();
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Word Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void addVocabulary(String word, String translation, String phonetic, String example, String category) {
        progressDialog.setTitle("Adding Word");
        progressDialog.setMessage("Creating vocabulary...");
        progressDialog.show();
        
        String vocabId = "vocab_" + System.currentTimeMillis();
        
        Map<String, Object> vocab = new HashMap<>();
        vocab.put("id", vocabId);
        vocab.put("word", word);
        vocab.put("translation", translation.isEmpty() ? "No translation" : translation);
        vocab.put("phonetic", phonetic.isEmpty() ? "" : phonetic);
        vocab.put("exampleSentence", example.isEmpty() ? "" : example);
        vocab.put("category", category.isEmpty() ? "General" : category);
        vocab.put("difficulty", "intermediate");
        vocab.put("createdAt", new Date());
        
        db.collection("vocabularies")
                .document(vocabId)
                .set(vocab)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "✅ Word added successfully!", Toast.LENGTH_SHORT).show();
                    loadVocabularies();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateVocabulary(Vocabulary vocab) {
        if (vocab.getId() == null || vocab.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Vocabulary ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressDialog.setTitle("Updating Word");
        progressDialog.setMessage("Saving changes...");
        progressDialog.show();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("word", vocab.getWord());
        updates.put("translation", vocab.getTranslation());
        updates.put("pronunciation", vocab.getPronunciation());
        updates.put("example", vocab.getExample());
        updates.put("category", vocab.getCategory());
        
        db.collection("vocabularies")
                .document(vocab.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "✅ Word updated!", Toast.LENGTH_SHORT).show();
                    loadVocabularies();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteVocabulary(Vocabulary vocab) {
        if (vocab.getId() == null || vocab.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Vocabulary ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Word")
                .setMessage("Are you sure you want to delete \"" + vocab.getWord() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    progressDialog.setTitle("Deleting Word");
                    progressDialog.setMessage("Removing vocabulary...");
                    progressDialog.show();
                    
                    db.collection("vocabularies")
                            .document(vocab.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "✅ Word deleted!", Toast.LENGTH_SHORT).show();
                                loadVocabularies();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void seedVocabularies() {
        progressDialog.setTitle("Seeding Vocabularies");
        progressDialog.setMessage("Creating vocabulary sets...");
        progressDialog.show();
        
        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedVocabularies(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_LONG).show();
                        loadVocabularies();
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
                        Toast.makeText(getContext(), "❌ " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
