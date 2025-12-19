package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HighlightManager {
    private static final String TAG = "HighlightManager";
    private static HighlightManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private HighlightManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized HighlightManager getInstance() {
        if (instance == null) {
            instance = new HighlightManager();
        }
        return instance;
    }

    public interface HighlightCallback {
        void onSuccess();
        void onError(String error);
    }

    public void saveHighlight(String articleId, String text, int startIndex, int endIndex, 
                            String color, String note, HighlightCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        String highlightId = db.collection("users").document(userId)
            .collection("article_highlights").document().getId();

        Map<String, Object> highlight = new HashMap<>();
        highlight.put("id", highlightId);
        highlight.put("articleId", articleId);
        highlight.put("userId", userId);
        highlight.put("text", text);
        highlight.put("startIndex", startIndex);
        highlight.put("endIndex", endIndex);
        highlight.put("color", color);
        highlight.put("note", note != null ? note : "");
        highlight.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
            .collection("article_highlights").document(highlightId)
            .set(highlight)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Highlight saved successfully");
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving highlight", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void deleteHighlight(String highlightId, HighlightCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId)
            .collection("article_highlights").document(highlightId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void updateHighlightNote(String highlightId, String note, HighlightCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId)
            .collection("article_highlights").document(highlightId)
            .update("note", note)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }
}
