package com.example.newsandlearn.Utils;

import com.example.newsandlearn.Model.MemoryPalace;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase manager for Memory Palace data
 */
public class MemoryPalaceFirebase {
    
    private static final String COLLECTION_PALACES = "memory_palaces";
    private static final String COLLECTION_ROOMS = "rooms";
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    public MemoryPalaceFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    /**
     * Save word to room in Firebase
     */
    public void saveWordToRoom(String palaceId, int roomPosition, MemoryPalace.WordMemory wordMemory,
                               OnSaveListener listener) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        
        Map<String, Object> data = new HashMap<>();
        data.put("word", wordMemory.getWord());
        data.put("meaning", wordMemory.getMeaning());
        data.put("crazyStory", wordMemory.getCrazyStory());
        data.put("imageUrl", wordMemory.getImageUrl());
        data.put("learnedAt", wordMemory.getLearnedAt());
        data.put("reviewCount", wordMemory.getReviewCount());
        data.put("nextReviewAt", wordMemory.getNextReviewAt());
        
        db.collection("users").document(userId)
            .collection(COLLECTION_PALACES).document(palaceId)
            .collection(COLLECTION_ROOMS).document("room_" + roomPosition)
            .set(data)
            .addOnSuccessListener(aVoid -> {
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }
    
    /**
     * Load word from room in Firebase
     */
    public void loadWordFromRoom(String palaceId, int roomPosition, OnLoadListener listener) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        
        db.collection("users").document(userId)
            .collection(COLLECTION_PALACES).document(palaceId)
            .collection(COLLECTION_ROOMS).document("room_" + roomPosition)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    String word = document.getString("word");
                    String meaning = document.getString("meaning");
                    String crazyStory = document.getString("crazyStory");
                    String imageUrl = document.getString("imageUrl");
                    
                    if (word != null && meaning != null && crazyStory != null && imageUrl != null) {
                        MemoryPalace.WordMemory wordMemory = new MemoryPalace.WordMemory(
                            word, meaning, crazyStory, imageUrl
                        );
                        
                        if (listener != null) listener.onLoaded(wordMemory);
                    } else {
                        if (listener != null) listener.onEmpty();
                    }
                } else {
                    if (listener != null) listener.onEmpty();
                }
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }
    
    /**
     * Load all rooms for a palace
     */
    public void loadAllRooms(String palaceId, int totalRooms, OnLoadAllListener listener) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest";
        
        Map<Integer, MemoryPalace.WordMemory> roomsData = new HashMap<>();
        
        for (int i = 0; i < totalRooms; i++) {
            final int roomIndex = i;
            
            db.collection("users").document(userId)
                .collection(COLLECTION_PALACES).document(palaceId)
                .collection(COLLECTION_ROOMS).document("room_" + i)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String word = document.getString("word");
                        String meaning = document.getString("meaning");
                        String crazyStory = document.getString("crazyStory");
                        String imageUrl = document.getString("imageUrl");
                        
                        if (word != null && meaning != null && crazyStory != null && imageUrl != null) {
                            MemoryPalace.WordMemory wordMemory = new MemoryPalace.WordMemory(
                                word, meaning, crazyStory, imageUrl
                            );
                            roomsData.put(roomIndex, wordMemory);
                        }
                    }
                    
                    // Check if all rooms loaded
                    if (roomsData.size() + (roomIndex + 1 - roomsData.size()) >= totalRooms) {
                        if (listener != null) listener.onAllLoaded(roomsData);
                    }
                });
        }
    }
    
    public interface OnSaveListener {
        void onSuccess();
        void onFailure(String error);
    }
    
    public interface OnLoadListener {
        void onLoaded(MemoryPalace.WordMemory wordMemory);
        void onEmpty();
        void onFailure(String error);
    }
    
    public interface OnLoadAllListener {
        void onAllLoaded(Map<Integer, MemoryPalace.WordMemory> roomsData);
    }
}
