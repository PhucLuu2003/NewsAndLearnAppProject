package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.VideoLesson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to add the "Asking for Directions" lesson to Firebase
 * This is a one-time data upload utility
 */
public class AddLessonDataHelper {
    private static final String TAG = "AddLessonDataHelper";
    

    
    
    /**
     * TEMPLATE: Copy method này để tạo bài học mới
     * Thay đổi các giá trị theo dữ liệu JSON của bạn
     */
    public static void addNewLessonTemplate() {
        FirebaseService firebaseService = new FirebaseService();
        
        // Tạo danh sách câu hỏi
        List<Question> questions = new ArrayList<>();
        
        // VÍ DỤ: Câu hỏi Multiple Choice
        Question q1 = new Question();
        q1.setId("q1");  // ID câu hỏi
        q1.setType(Question.QuestionType.MULTIPLE_CHOICE);  // Loại: MULTIPLE_CHOICE, FILL_IN_THE_BLANK, TRUE_OR_FALSE, DRAG_AND_DROP
        q1.setAppearAtSecond(30);  // Xuất hiện ở giây thứ 30
        q1.setQuestionText("Câu hỏi của bạn?");
        q1.setCorrectAnswer("Đáp án đúng");
        q1.setOptions(Arrays.asList("Đáp án 1", "Đáp án 2", "Đáp án 3", "Đáp án 4"));
        q1.setExplanation("Giải thích tại sao đáp án này đúng");
        questions.add(q1);
        
        // Thêm nhiều câu hỏi khác nếu cần...
        
        // Tạo bài học
        VideoLesson lesson = new VideoLesson();
        lesson.setId("lesson_XXX");  // ID bài học (phải unique)
        lesson.setTitle("Tiêu đề bài học");
        lesson.setDescription("Mô tả bài học");
        lesson.setVideoUrl("https://example.com/video.mp4");
        lesson.setThumbnailUrl("https://example.com/thumbnail.jpg");
        lesson.setLevel("A1");  // A1, A2, B1, B2, C1, C2
        lesson.setCategory("Grammar");  // Grammar, Conversation, Vocabulary, etc.
        lesson.setDuration(300);  // Thời lượng tính bằng giây
        lesson.setViews(0);
        lesson.setCreatedAt(System.currentTimeMillis());
        lesson.setQuestions(questions);
        
        // Upload lên Firebase
        firebaseService.addVideoLesson(lesson, new FirebaseService.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added new lesson to Firebase!");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add new lesson to Firebase", e);
            }
        });
    }
    
    /**
     * Upload tất cả bài học cùng một lúc
     * Gọi method này để upload nhiều bài học
     */
    public static void addAllLessons() {
        // Thêm các bài học khác ở đây
        // addNewLessonTemplate();
        // addAnotherLesson();
    }
    
    /**
     * Helper method: Tạo bài học từ dữ liệu thô
     * Sử dụng khi bạn có dữ liệu JSON và muốn convert nhanh
     */
    public static void addLessonFromRawData(
            String id,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            String level,
            String category,
            int duration,
            int views,
            long createdAt,
            List<Question> questions) {
        
        FirebaseService firebaseService = new FirebaseService();
        
        VideoLesson lesson = new VideoLesson();
        lesson.setId(id);
        lesson.setTitle(title);
        lesson.setDescription(description);
        lesson.setVideoUrl(videoUrl);
        lesson.setThumbnailUrl(thumbnailUrl);
        lesson.setLevel(level);
        lesson.setCategory(category);
        lesson.setDuration(duration);
        lesson.setViews(views);
        lesson.setCreatedAt(createdAt);
        lesson.setQuestions(questions);
        
        firebaseService.addVideoLesson(lesson, new FirebaseService.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added lesson: " + id);
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add lesson: " + id, e);
            }
        });
    }
}
