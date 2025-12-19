package com.example.newsandlearn.Utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to update video URLs from YouTube to direct MP4 URLs
 */
public class VideoUrlUpdater {
    
    private static final String TAG = "VideoUrlUpdater";
    private final FirebaseFirestore db;
    
    public interface UpdateCallback {
        void onSuccess(String message);
        void onProgress(String message);
        void onFailure(String error);
    }
    
    public VideoUrlUpdater() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Update all video lessons to use MP4 URLs instead of YouTube URLs
     */
    public void updateAllVideosToMp4(UpdateCallback callback) {
        callback.onProgress("Đang tải danh sách video...");
        
        db.collection("video_lessons")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure("Không tìm thấy video nào trong database");
                        return;
                    }
                    
                    int totalVideos = querySnapshot.size();
                    final int[] updatedCount = {0};
                    
                    callback.onProgress("Tìm thấy " + totalVideos + " videos. Đang cập nhật...");
                    
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String videoId = document.getId();
                        String currentUrl = document.getString("videoUrl");
                        
                        // Check if it's a YouTube URL
                        if (currentUrl != null && (currentUrl.contains("youtube.com") || currentUrl.contains("youtu.be"))) {
                            // Get new MP4 URL based on video ID
                            String newMp4Url = getMp4UrlForVideo(videoId);
                            
                            // Update the document
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("videoUrl", newMp4Url);
                            
                            db.collection("video_lessons")
                                    .document(videoId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        updatedCount[0]++;
                                        callback.onProgress("Đã cập nhật " + updatedCount[0] + "/" + totalVideos + " videos");
                                        
                                        if (updatedCount[0] == totalVideos) {
                                            callback.onSuccess("Hoàn thành! Đã cập nhật " + totalVideos + " videos sang MP4");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating video " + videoId, e);
                                        callback.onProgress("Lỗi cập nhật video " + videoId + ": " + e.getMessage());
                                    });
                        } else {
                            updatedCount[0]++;
                            callback.onProgress("Video " + videoId + " đã là MP4, bỏ qua");
                            
                            if (updatedCount[0] == totalVideos) {
                                callback.onSuccess("Hoàn thành! Tất cả videos đã là MP4");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading videos", e);
                    callback.onFailure("Lỗi tải danh sách video: " + e.getMessage());
                });
    }
    
    /**
     * Get MP4 URL for a specific video ID
     * Maps video IDs to sample MP4 URLs from Google Cloud Storage
     */
    private String getMp4UrlForVideo(String videoId) {
        // Sample MP4 URLs from Google Cloud Storage
        // You can replace these with your own video URLs
        Map<String, String> videoUrlMap = new HashMap<>();
        
        videoUrlMap.put("video_01", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        videoUrlMap.put("video_02", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        videoUrlMap.put("video_03", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4");
        videoUrlMap.put("video_04", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4");
        videoUrlMap.put("video_05", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");
        videoUrlMap.put("video_06", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4");
        videoUrlMap.put("video_07", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4");
        videoUrlMap.put("video_08", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4");
        videoUrlMap.put("video_09", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4");
        videoUrlMap.put("video_10", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4");
        
        // Return mapped URL or default to first video
        return videoUrlMap.getOrDefault(videoId, 
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
    }
    
    /**
     * Update a single video URL
     */
    public void updateSingleVideo(String videoId, String newMp4Url, UpdateCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("videoUrl", newMp4Url);
        
        db.collection("video_lessons")
                .document(videoId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Video " + videoId + " updated successfully");
                    callback.onSuccess("Đã cập nhật video " + videoId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating video " + videoId, e);
                    callback.onFailure("Lỗi cập nhật video: " + e.getMessage());
                });
    }
}
