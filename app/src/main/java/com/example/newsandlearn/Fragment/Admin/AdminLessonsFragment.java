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


import com.example.newsandlearn.Adapter.AdminLessonAdapter;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminLessonsFragment - Manage lessons (Reading, Writing, Listening, Speaking, Grammar)
 */
public class AdminLessonsFragment extends Fragment {

    private ChipGroup lessonTypeChips;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnAddLesson, btnSeedLessons;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private AdminLessonAdapter adapter;
    
    private String currentLessonType = "reading"; // Default
    private List<Map<String, Object>> lessons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_lessons, container, false);
        
        db = FirebaseFirestore.getInstance();
        lessons = new ArrayList<>();
        initializeViews(view);
        loadLessons(currentLessonType);
        
        return view;
    }

    private void initializeViews(View view) {
        lessonTypeChips = view.findViewById(R.id.lesson_type_chips);
        recyclerView = view.findViewById(R.id.lessons_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        btnAddLesson = view.findViewById(R.id.btn_add_lesson);
        btnSeedLessons = view.findViewById(R.id.btn_seed_lessons);
        
        lessons = new ArrayList<>();
        adapter = new AdminLessonAdapter(getContext(), lessons);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        
        // Chip selection listener
        lessonTypeChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_reading) currentLessonType = "reading";
                else if (checkedId == R.id.chip_writing) currentLessonType = "writing";
                else if (checkedId == R.id.chip_listening) currentLessonType = "listening";
                else if (checkedId == R.id.chip_speaking) currentLessonType = "speaking";
                else if (checkedId == R.id.chip_grammar) currentLessonType = "grammar";
                
                loadLessons(currentLessonType);
            }
        });
        
        btnAddLesson.setOnClickListener(v -> showAddLessonDialog());
        btnSeedLessons.setOnClickListener(v -> seedLessons());
        swipeRefresh.setOnRefreshListener(() -> loadLessons(currentLessonType));
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void loadLessons(String type) {
        swipeRefresh.setRefreshing(true);
        String collection = type + "_lessons";
        
        db.collection(collection)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    lessons.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> lesson = document.getData();
                        lesson.put("id", document.getId());
                        lessons.add(lesson);
                    }
                    
                    // Update adapter
                    adapter.notifyDataSetChanged();
                    
                    // Update UI with lesson count
                    Toast.makeText(getContext(), 
                        "Loaded " + lessons.size() + " " + type + " lessons", 
                        Toast.LENGTH_SHORT).show();
                    
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void showAddLessonDialog() {
        // Create dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_lesson, null);
        
        // Get views
        com.google.android.material.textfield.TextInputEditText etTitle = dialogView.findViewById(R.id.et_title);
        com.google.android.material.textfield.TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);
        com.google.android.material.textfield.TextInputEditText etContent = dialogView.findViewById(R.id.et_content);
        com.google.android.material.textfield.TextInputEditText etLevel = dialogView.findViewById(R.id.et_level);
        com.google.android.material.textfield.TextInputEditText etCategory = dialogView.findViewById(R.id.et_category);
        
        // Create dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add " + currentLessonType.substring(0, 1).toUpperCase() + currentLessonType.substring(1) + " Lesson")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    String level = etLevel.getText().toString().trim();
                    String category = etCategory.getText().toString().trim();
                    
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    addLesson(title, description, content, level, category);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void addLesson(String title, String description, String content, String level, String category) {
        progressDialog.setTitle("Adding Lesson");
        progressDialog.setMessage("Creating " + currentLessonType + " lesson...");
        progressDialog.show();
        
        String collection = currentLessonType + "_lessons";
        String lessonId = currentLessonType + "_" + System.currentTimeMillis();
        
        Map<String, Object> lesson = new HashMap<>();
        lesson.put("id", lessonId);
        lesson.put("title", title);
        lesson.put("description", description);
        lesson.put("content", content);
        lesson.put("level", level.isEmpty() ? "A1" : level);
        lesson.put("category", category.isEmpty() ? "General" : category);
        lesson.put("createdAt", new Date());
        
        db.collection(collection)
                .document(lessonId)
                .set(lesson)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "✅ Lesson added successfully!", Toast.LENGTH_SHORT).show();
                    loadLessons(currentLessonType);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void seedLessons() {
        progressDialog.setTitle("Seeding Lessons");
        progressDialog.setMessage("Creating " + currentLessonType + " lessons...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        
        // Seed based on current lesson type
        switch (currentLessonType) {
            case "reading":
                seedReadingLessons(seeder);
                break;
            case "writing":
                seedWritingLessons(seeder);
                break;
            case "listening":
                seedListeningLessons(seeder);
                break;
            case "speaking":
                seedSpeakingLessons(seeder);
                break;
            case "grammar":
                seedGrammarLessons(seeder);
                break;
        }
    }

    private void seedReadingLessons(FirebaseDataSeeder seeder) {
        seeder.seedReadingLessons(createSeedCallback());
    }

    private void seedWritingLessons(FirebaseDataSeeder seeder) {
        seeder.seedWritingLessons(createSeedCallback());
    }

    private void seedListeningLessons(FirebaseDataSeeder seeder) {
        seeder.seedListeningLessons(createSeedCallback());
    }

    private void seedSpeakingLessons(FirebaseDataSeeder seeder) {
        seeder.seedSpeakingLessons(createSeedCallback());
    }

    private void seedGrammarLessons(FirebaseDataSeeder seeder) {
        seeder.seedGrammarLessons(createSeedCallback());
    }

    private FirebaseDataSeeder.SeedCallback createSeedCallback() {
        return new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_LONG).show();
                        loadLessons(currentLessonType);
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
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
