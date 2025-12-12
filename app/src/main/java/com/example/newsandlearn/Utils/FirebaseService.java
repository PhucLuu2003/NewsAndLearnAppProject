package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.VideoLesson;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase service utility class for managing Firestore operations
 */
public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static final String COLLECTION_VIDEO_LESSONS = "video_lessons";
    
    private final FirebaseFirestore db;
    
    public FirebaseService() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Add a video lesson to Firestore
     * @param lesson The VideoLesson object to add
     * @param callback Callback for success/failure
     */
    public void addVideoLesson(VideoLesson lesson, FirestoreCallback callback) {
        if (lesson == null || lesson.getId() == null) {
            callback.onFailure(new Exception("Invalid lesson data"));
            return;
        }
        
        db.collection(COLLECTION_VIDEO_LESSONS)
            .document(lesson.getId())
            .set(lesson)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Video lesson added successfully: " + lesson.getId());
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error adding video lesson", e);
                callback.onFailure(e);
            });
    }
    
    /**
     * Add a video lesson from raw data (Map format)
     * Useful for adding data directly from JSON
     */
    public void addVideoLessonFromMap(Map<String, Object> lessonData, FirestoreCallback callback) {
        try {
            String id = (String) lessonData.get("id");
            if (id == null) {
                callback.onFailure(new Exception("Lesson ID is required"));
                return;
            }
            
            db.collection(COLLECTION_VIDEO_LESSONS)
                .document(id)
                .set(lessonData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Video lesson added successfully from map: " + id);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding video lesson from map", e);
                    callback.onFailure(e);
                });
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }
    
    /**
     * Convert JSON-style question data to Question objects
     * Handles type conversion from string to enum
     */
    public static List<Question> convertQuestionsFromMap(List<Map<String, Object>> questionsData) {
        List<Question> questions = new ArrayList<>();
        
        for (Map<String, Object> qData : questionsData) {
            Question question = new Question();
            
            question.setId((String) qData.get("id"));
            
            // Convert type string to enum
            String typeStr = (String) qData.get("type");
            if (typeStr != null) {
                question.setType(Question.QuestionType.valueOf(typeStr));
            }
            
            // Handle numeric fields
            Object appearAtObj = qData.get("appearAtSecond");
            if (appearAtObj instanceof Number) {
                question.setAppearAtSecond(((Number) appearAtObj).intValue());
            }
            
            question.setQuestionText((String) qData.get("questionText"));
            question.setCorrectAnswer((String) qData.get("correctAnswer"));
            question.setExplanation((String) qData.get("explanation"));
            
            // Handle options list
            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) qData.get("options");
            question.setOptions(options);
            
            // Handle blank position if present
            Object blankPosObj = qData.get("blankPosition");
            if (blankPosObj instanceof Number) {
                question.setBlankPosition(((Number) blankPosObj).intValue());
            } else {
                question.setBlankPosition(-1);
            }
            
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * Create a VideoLesson object from Map data
     */
    public static VideoLesson createVideoLessonFromMap(Map<String, Object> data) {
        VideoLesson lesson = new VideoLesson();
        
        lesson.setId((String) data.get("id"));
        lesson.setTitle((String) data.get("title"));
        lesson.setDescription((String) data.get("description"));
        lesson.setVideoUrl((String) data.get("videoUrl"));
        lesson.setThumbnailUrl((String) data.get("thumbnailUrl"));
        lesson.setLevel((String) data.get("level"));
        lesson.setCategory((String) data.get("category"));
        
        // Handle numeric fields
        Object durationObj = data.get("duration");
        if (durationObj instanceof Number) {
            lesson.setDuration(((Number) durationObj).intValue());
        }
        
        Object viewsObj = data.get("views");
        if (viewsObj instanceof Number) {
            lesson.setViews(((Number) viewsObj).intValue());
        }
        
        Object createdAtObj = data.get("createdAt");
        if (createdAtObj instanceof Number) {
            lesson.setCreatedAt(((Number) createdAtObj).longValue());
        }
        
        // Handle questions
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questionsData = (List<Map<String, Object>>) data.get("questions");
        if (questionsData != null) {
            lesson.setQuestions(convertQuestionsFromMap(questionsData));
        }
        
        return lesson;
    }
    
    /**
     * Callback interface for Firestore operations
     */
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
