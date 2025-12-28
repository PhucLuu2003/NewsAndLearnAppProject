package com.example.newsandlearn.Activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.RoleManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class AdminDocumentEditorActivity extends AppCompatActivity {

    private String collectionName;
    private String docId;

    private TextView tvTitle;
    private EditText etDocId;
    private EditText etJson;
    private Button btnSaveMerge;
    private Button btnSaveOverwrite;
    private Button btnDelete;
    private ProgressBar progressBar;

    private final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_document_editor);

        collectionName = getIntent().getStringExtra(AdminFirestoreActivity.EXTRA_COLLECTION);
        docId = getIntent().getStringExtra(AdminCollectionActivity.EXTRA_DOC_ID);

        if (collectionName == null || collectionName.trim().isEmpty()) {
            Toast.makeText(this, "Missing collection", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle = findViewById(R.id.tv_title);
        etDocId = findViewById(R.id.et_doc_id);
        etJson = findViewById(R.id.et_json);
        btnSaveMerge = findViewById(R.id.btn_save_merge);
        btnSaveOverwrite = findViewById(R.id.btn_save_overwrite);
        btnDelete = findViewById(R.id.btn_delete);
        progressBar = findViewById(R.id.progress_bar);

        boolean isEditingExisting = !TextUtils.isEmpty(docId);

        tvTitle.setText(isEditingExisting
                ? ("Edit: " + collectionName + "/" + docId)
                : ("Create: " + collectionName));
        setTitle("Admin • Editor");

        etDocId.setText(isEditingExisting ? docId : "");
        etDocId.setEnabled(!isEditingExisting);
        btnDelete.setVisibility(isEditingExisting ? View.VISIBLE : View.GONE);

        gateAdminThenLoad(isEditingExisting);

        btnSaveMerge.setOnClickListener(v -> save(true));
        btnSaveOverwrite.setOnClickListener(v -> save(false));
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void gateAdminThenLoad(boolean isEditingExisting) {
        progressBar.setVisibility(View.VISIBLE);
        RoleManager.isCurrentUserAdmin(new RoleManager.RoleCheckCallback() {
            @Override
            public void onResult(boolean isAdmin) {
                if (!isAdmin) {
                    Toast.makeText(AdminDocumentEditorActivity.this, "Bạn không có quyền Admin", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                    return;
                }
                if (isEditingExisting) {
                    loadDocument();
                } else {
                    etJson.setText("{}\n");
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AdminDocumentEditorActivity.this, "Không kiểm tra được quyền: " + error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadDocument() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(collectionName)
                .document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data == null) {
                        etJson.setText("{}\n");
                    } else {
                        etJson.setText(gsonPretty.toJson(data));
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminDocumentEditorActivity.this, "Load failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void save(boolean merge) {
        String inputDocId = etDocId.getText() != null ? etDocId.getText().toString().trim() : "";
        String json = etJson.getText() != null ? etJson.getText().toString().trim() : "";

        if (TextUtils.isEmpty(json)) {
            Toast.makeText(this, "JSON is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map;
        try {
            map = new Gson().fromJson(json, mapType);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (map == null) {
            Toast.makeText(this, "JSON must be an object", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref;

        if (TextUtils.isEmpty(docId)) {
            // Creating new doc
            if (TextUtils.isEmpty(inputDocId)) {
                ref = db.collection(collectionName).document();
                docId = ref.getId();
                etDocId.setText(docId);
                etDocId.setEnabled(false);
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                ref = db.collection(collectionName).document(inputDocId);
                docId = inputDocId;
                etDocId.setEnabled(false);
                btnDelete.setVisibility(View.VISIBLE);
            }
        } else {
            ref = db.collection(collectionName).document(docId);
        }

        progressBar.setVisibility(View.VISIBLE);
        if (merge) {
            ref.set(map, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Saved (merge)", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    });
        } else {
            ref.set(map)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Saved (overwrite)", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }

    private void confirmDelete() {
        if (TextUtils.isEmpty(docId)) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete document")
                .setMessage("Delete " + collectionName + "/" + docId + " ?")
                .setPositiveButton("Delete", (d, which) -> delete())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void delete() {
        if (TextUtils.isEmpty(docId)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection(collectionName)
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}
