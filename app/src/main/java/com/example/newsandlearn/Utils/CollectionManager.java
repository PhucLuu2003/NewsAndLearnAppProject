package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionManager {
    private static final String TAG = "CollectionManager";
    private static CollectionManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private CollectionManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized CollectionManager getInstance() {
        if (instance == null) {
            instance = new CollectionManager();
        }
        return instance;
    }

    public interface CollectionCallback {
        void onSuccess();
        void onError(String error);
    }

    public void createDefaultCollections() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        // Create default collections
        createCollection("Favorites", "My favorite articles", "favorite", null);
        createCollection("Read Later", "Articles to read later", "bookmark", null);
        createCollection("Completed", "Articles I've finished", "check", null);
    }

    public void createCollection(String name, String description, String iconName, CollectionCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        String collectionId = db.collection("users").document(userId)
            .collection("reading_collections").document().getId();

        Map<String, Object> collection = new HashMap<>();
        collection.put("id", collectionId);
        collection.put("userId", userId);
        collection.put("name", name);
        collection.put("description", description);
        collection.put("iconName", iconName);
        collection.put("articleIds", new ArrayList<String>());
        collection.put("createdAt", FieldValue.serverTimestamp());
        collection.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
            .collection("reading_collections").document(collectionId)
            .set(collection)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Collection created: " + name);
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating collection", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void addArticleToCollection(String collectionId, String articleId, CollectionCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("articleIds", FieldValue.arrayUnion(articleId));
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
            .collection("reading_collections").document(collectionId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void removeArticleFromCollection(String collectionId, String articleId, CollectionCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("articleIds", FieldValue.arrayRemove(articleId));
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
            .collection("reading_collections").document(collectionId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    public void toggleFavorite(String articleId, boolean isFavorite, CollectionCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        // Find Favorites collection
        db.collection("users").document(userId)
            .collection("reading_collections")
            .whereEqualTo("name", "Favorites")
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String favCollectionId = querySnapshot.getDocuments().get(0).getId();
                    
                    if (isFavorite) {
                        addArticleToCollection(favCollectionId, articleId, callback);
                    } else {
                        removeArticleFromCollection(favCollectionId, articleId, callback);
                    }
                } else {
                    // Create Favorites collection first
                    createCollection("Favorites", "My favorite articles", "favorite", 
                        new CollectionCallback() {
                            @Override
                            public void onSuccess() {
                                toggleFavorite(articleId, isFavorite, callback);
                            }

                            @Override
                            public void onError(String error) {
                                if (callback != null) callback.onError(error);
                            }
                        });
                }
            });
    }
}
