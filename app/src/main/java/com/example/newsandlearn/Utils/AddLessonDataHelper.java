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
     * Add the "Asking for Directions - Finding the Nearest Station" lesson to Firebase
     */
    public static void addDirectionsLesson() {
        FirebaseService firebaseService = new FirebaseService();
        
        // Create the lesson object
        VideoLesson lesson = createDirectionsLesson();
        
        // Add to Firebase
        firebaseService.addVideoLesson(lesson, new FirebaseService.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added 'Asking for Directions' lesson to Firebase!");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add lesson to Firebase", e);
            }
        });
    }
    
    /**
     * Create the VideoLesson object for the Directions lesson
     */
    private static VideoLesson createDirectionsLesson() {
        // Create questions
        List<Question> questions = new ArrayList<>();
        
        // Question 1: Multiple Choice
        Question q1 = new Question();
        q1.setId("q3");
        q1.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q1.setAppearAtSecond(45);
        q1.setQuestionText("What is the polite way to start a question for directions?");
        q1.setCorrectAnswer("Excuse me, could you tell me...");
        q1.setOptions(Arrays.asList(
            "Hey you, where is the station?",
            "Excuse me, could you tell me...",
            "Tell me where to go.",
            "I need directions now."
        ));
        q1.setExplanation("'Excuse me, could you tell me...' is the most common and polite way to approach someone for help.");
        questions.add(q1);
        
        // Question 2: Fill in the Blank
        Question q2 = new Question();
        q2.setId("q4");
        q2.setType(Question.QuestionType.FILL_IN_THE_BLANK);
        q2.setAppearAtSecond(90);
        q2.setQuestionText("The post office is _____ the corner, opposite the library.");
        q2.setCorrectAnswer("on");
        q2.setOptions(Arrays.asList("at", "in", "on", "next"));
        q2.setExplanation("We use 'on the corner' to describe a location at the intersection of two streets.");
        questions.add(q2);
        
        // Question 3: True or False
        Question q3 = new Question();
        q3.setId("q5");
        q3.setType(Question.QuestionType.TRUE_OR_FALSE);
        q3.setAppearAtSecond(150);
        q3.setQuestionText("Turn right at the first intersection is the same as Go straight ahead.");
        q3.setCorrectAnswer("False");
        q3.setOptions(Arrays.asList("True", "False"));
        q3.setExplanation("'Turn right' means changing direction, while 'Go straight ahead' means continuing in the same direction.");
        questions.add(q3);
        
        // Create the lesson
        VideoLesson lesson = new VideoLesson();
        lesson.setId("lesson_002");
        lesson.setTitle("Asking for Directions - Finding the Nearest Station");
        lesson.setDescription("Essential phrases and vocabulary for asking and giving directions in a city.");
        // Dùng video mẫu để test (thay bằng URL video thật của bạn)
        lesson.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        lesson.setThumbnailUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg");
        lesson.setLevel("A2");
        lesson.setCategory("Conversation");
        lesson.setDuration(245);
        lesson.setViews(890);
        lesson.setCreatedAt(1702456789012L);
        lesson.setQuestions(questions);
        
        return lesson;
    }
    
    /**
     * Add the "Ordering Food at a Restaurant" lesson to Firebase
     * Video by Bob the Canadian - YouTube
     */
    public static void addRestaurantLesson() {
        FirebaseService firebaseService = new FirebaseService();
        
        // Create the lesson object
        VideoLesson lesson = createRestaurantLesson();
        
        // Add to Firebase
        firebaseService.addVideoLesson(lesson, new FirebaseService.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully added 'Ordering Food at a Restaurant' lesson to Firebase!");
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to add restaurant lesson to Firebase", e);
            }
        });
    }
    
    /**
     * Create the VideoLesson object for the Restaurant lesson
     */
    private static VideoLesson createRestaurantLesson() {
        // Create questions
        List<Question> questions = new ArrayList<>();
        
        // Question 1: What phrase does Bob use to order pizza?
        Question q1 = new Question();
        q1.setId("q1");
        q1.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q1.setAppearAtSecond(60);
        q1.setQuestionText("What phrase does Bob use to order pizza?");
        q1.setCorrectAnswer("I would like to order");
        q1.setOptions(Arrays.asList(
            "I would like to order",
            "Give me",
            "I want",
            "Can I get"
        ));
        q1.setExplanation("Bob uses the polite phrase 'I would like to order' when ordering pizza at the first restaurant.");
        questions.add(q1);
        
        // Question 2: What size pizza did Bob order?
        Question q2 = new Question();
        q2.setId("q2");
        q2.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q2.setAppearAtSecond(180);
        q2.setQuestionText("What size pizza did Bob order?");
        q2.setCorrectAnswer("Small");
        q2.setOptions(Arrays.asList("Large", "Medium", "Small", "Extra large"));
        q2.setExplanation("Bob ordered a small pepperoni pizza at the pizza restaurant.");
        questions.add(q2);
        
        // Question 3: What phrase does Bob use at the fast food restaurant?
        Question q3 = new Question();
        q3.setId("q3");
        q3.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q3.setAppearAtSecond(300);
        q3.setQuestionText("What phrase does Bob use at the fast food restaurant?");
        q3.setCorrectAnswer("I'll have");
        q3.setOptions(Arrays.asList("I would like", "I'll have", "Give me", "I need"));
        q3.setExplanation("At the fast food restaurant, Bob uses the casual phrase 'I'll have' to order a hamburger.");
        questions.add(q3);
        
        // Question 4: What did Bob order to drink with his hamburger?
        Question q4 = new Question();
        q4.setId("q4");
        q4.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q4.setAppearAtSecond(450);
        q4.setQuestionText("What did Bob order to drink with his hamburger?");
        q4.setCorrectAnswer("Root beer");
        q4.setOptions(Arrays.asList("Coca Cola", "Root beer", "Orange juice", "Water"));
        q4.setExplanation("Bob ordered fries and a root beer with his Teen Burger.");
        questions.add(q4);
        
        // Question 5: How many different restaurants did Bob visit in the video?
        Question q5 = new Question();
        q5.setId("q5");
        q5.setType(Question.QuestionType.MULTIPLE_CHOICE);
        q5.setAppearAtSecond(600);
        q5.setQuestionText("How many different restaurants did Bob visit in the video?");
        q5.setCorrectAnswer("4");
        q5.setOptions(Arrays.asList("2", "3", "4", "5"));
        q5.setExplanation("Bob visited 4 different restaurants: a pizza place, fast food restaurant, sandwich shop, and donut shop.");
        questions.add(q5);
        
        // Create the lesson
        VideoLesson lesson = new VideoLesson();
        lesson.setId("lesson_003");
        lesson.setTitle("Ordering Food at a Restaurant");
        lesson.setDescription("Learn how to order food politely with Bob the Canadian");
        // NOTE: ExoPlayer không thể phát YouTube URL trực tiếp
        // Bạn cần:
        // 1. Upload video lên Firebase Storage hoặc server riêng
        // 2. Hoặc dùng YouTube Data API để lấy stream URL
        // 3. Tạm thời dùng video mẫu để test
        lesson.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        lesson.setThumbnailUrl("https://img.youtube.com/vi/uUMPULuwdLI/maxresdefault.jpg");
        lesson.setLevel("A2");
        lesson.setCategory("Conversation");
        lesson.setDuration(840);
        lesson.setViews(500);
        lesson.setCreatedAt(1702456789012L);
        lesson.setQuestions(questions);
        
        return lesson;
    }
    
    
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
        addDirectionsLesson();
        addRestaurantLesson();
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
