package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to add sample audio URLs to speaking lessons
 * Uses free, publicly available MP3 files for demonstration
 */
public class SpeakingAudioUpdater {

    private static final String TAG = "SpeakingAudioUpdater";
    
    /**
     * Free sample audio URLs for English learning
     * These are publicly available, royalty-free audio files
     */
    private static final String[] SAMPLE_AUDIO_URLS = {
        // English pronunciation samples from various free sources
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://file-examples.com/storage/fe7c0c3d3750f0e7a5fc9e3/2017/11/file_example_MP3_700KB.mp3",
        "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://file-examples.com/storage/fe7c0c3d3750f0e7a5fc9e3/2017/11/file_example_MP3_1MG.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"
    };

    public interface UpdateCallback {
        void onSuccess(int updatedCount);
        void onFailure(Exception e);
    }

    /**
     * Add sample audio URLs to all speaking lessons
     */
    public static void addAudioToSpeakingLessons(UpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Log.d(TAG, "Starting to add audio URLs to speaking lessons...");
        
        db.collection("speaking_lessons")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int count = 0;
                int total = querySnapshot.size();
                
                Log.d(TAG, "Found " + total + " speaking lessons");
                
                for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                    String docId = querySnapshot.getDocuments().get(i).getId();
                    
                    // Use modulo to cycle through available audio URLs
                    String audioUrl = SAMPLE_AUDIO_URLS[i % SAMPLE_AUDIO_URLS.length];
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("sampleAudioUrl", audioUrl);
                    
                    final int index = i;
                    db.collection("speaking_lessons").document(docId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Added audio to " + docId + ": " + audioUrl);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update " + docId, e);
                        });
                    
                    count++;
                }
                
                final int updatedCount = count;
                Log.d(TAG, "Successfully queued " + updatedCount + " updates");
                
                if (callback != null) {
                    callback.onSuccess(updatedCount);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting speaking lessons", e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            });
    }

    /**
     * Add audio to a specific lesson
     */
    public static void addAudioToLesson(String lessonId, String audioUrl, UpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("sampleAudioUrl", audioUrl);
        
        db.collection("speaking_lessons").document(lessonId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Added audio to lesson " + lessonId);
                if (callback != null) {
                    callback.onSuccess(1);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to add audio to lesson " + lessonId, e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            });
    }
}
