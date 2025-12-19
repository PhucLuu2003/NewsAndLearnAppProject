package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.GameLevel;
import com.example.newsandlearn.Model.GameQuestion;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GameDataSeeder - Seed sample game data to Firebase
 */
public class GameDataSeeder {

    private static final String TAG = "GameDataSeeder";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void seedAllGameData(OnCompleteListener listener) {
        seedGameQuestions(() -> {
            seedGameLevels(() -> {
                Log.d(TAG, "✅ All game data seeded successfully!");
                listener.onSuccess();
            }, listener::onFailure);
        }, listener::onFailure);
    }

    private static void seedGameQuestions(Runnable onSuccess, OnFailureListener onFailure) {
        Log.d(TAG, "Seeding game questions...");

        GameQuestion[] questions = {
            // Greetings & Introductions
            createQuestion("q1", "What does 'Hello' mean?",
                Arrays.asList("Xin chào", "Tạm biệt", "Cảm ơn", "Xin lỗi"),
                0, "easy", "vocabulary"),
            
            createQuestion("q2", "How do you say 'Goodbye' in English?",
                Arrays.asList("Hello", "Goodbye", "Thank you", "Sorry"),
                1, "easy", "vocabulary"),
            
            createQuestion("q3", "What is the correct response to 'How are you?'",
                Arrays.asList("I'm fine, thank you", "Goodbye", "Hello", "My name is"),
                0, "easy", "grammar"),
            
            createQuestion("q4", "Choose the correct sentence:",
                Arrays.asList("My name is John", "My name John is", "Is John my name", "Name my is John"),
                0, "easy", "grammar"),
            
            createQuestion("q5", "'Nice to meet you' means:",
                Arrays.asList("Rất vui được gặp bạn", "Tạm biệt", "Xin lỗi", "Cảm ơn"),
                0, "easy", "vocabulary"),

            // Travel
            createQuestion("q6", "What does 'Airport' mean?",
                Arrays.asList("Sân bay", "Bến xe", "Nhà ga", "Bến tàu"),
                0, "medium", "vocabulary"),
            
            createQuestion("q7", "How do you ask for directions?",
                Arrays.asList("Where is the station?", "What is your name?", "How old are you?", "I like coffee"),
                0, "medium", "grammar"),
            
            createQuestion("q8", "'Excuse me' is used to:",
                Arrays.asList("Get someone's attention", "Say goodbye", "Say thank you", "Introduce yourself"),
                0, "medium", "vocabulary"),

            // Food
            createQuestion("q9", "What does 'Menu' mean?",
                Arrays.asList("Thực đơn", "Hóa đơn", "Bàn ăn", "Nhà hàng"),
                0, "medium", "vocabulary"),
            
            createQuestion("q10", "How do you order food?",
                Arrays.asList("I would like a pizza", "I am pizza", "Pizza is me", "Like I pizza"),
                0, "medium", "grammar"),

            // Work
            createQuestion("q11", "What does 'Meeting' mean?",
                Arrays.asList("Cuộc họp", "Văn phòng", "Công việc", "Đồng nghiệp"),
                0, "hard", "vocabulary"),
            
            createQuestion("q12", "Choose the correct sentence:",
                Arrays.asList("I work at a company", "I at work company", "Work I at company", "Company at I work"),
                0, "hard", "grammar"),

            // Advanced
            createQuestion("q13", "What is the past tense of 'go'?",
                Arrays.asList("Went", "Goed", "Gone", "Going"),
                0, "hard", "grammar"),
            
            createQuestion("q14", "Choose the correct sentence:",
                Arrays.asList("She has been working here for 5 years", "She have been working here for 5 years", 
                             "She has be working here for 5 years", "She has been work here for 5 years"),
                0, "hard", "grammar"),
            
            createQuestion("q15", "'Accomplish' means:",
                Arrays.asList("Hoàn thành", "Bắt đầu", "Tiếp tục", "Dừng lại"),
                0, "hard", "vocabulary")
        };

        int[] count = {0};
        for (GameQuestion question : questions) {
            db.collection("game_questions").document(question.getId())
                    .set(question)
                    .addOnSuccessListener(aVoid -> {
                        count[0]++;
                        Log.d(TAG, "Question added: " + question.getId());
                        if (count[0] == questions.length) {
                            onSuccess.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding question", e);
                        onFailure.onFailure(e);
                    });
        }
    }

    private static void seedGameLevels(Runnable onSuccess, OnFailureListener onFailure) {
        Log.d(TAG, "Seeding game levels...");

        GameLevel[] levels = {
            createLevel("level1", "Greetings & Introductions", "greetings", 1,
                "Learn basic greetings and how to introduce yourself",
                Arrays.asList("q1", "q2", "q3", "q4", "q5")),
            
            createLevel("level2", "Travel Essentials", "travel", 2,
                "Essential phrases for traveling",
                Arrays.asList("q6", "q7", "q8")),
            
            createLevel("level3", "Food & Dining", "food", 3,
                "Order food and drinks like a pro",
                Arrays.asList("q9", "q10")),
            
            createLevel("level4", "Work & Business", "work", 4,
                "Professional English for the workplace",
                Arrays.asList("q11", "q12")),
            
            createLevel("level5", "Advanced Grammar", "grammar", 5,
                "Master complex grammar structures",
                Arrays.asList("q13", "q14", "q15"))
        };

        int[] count = {0};
        for (GameLevel level : levels) {
            db.collection("game_levels").document(level.getId())
                    .set(level)
                    .addOnSuccessListener(aVoid -> {
                        count[0]++;
                        Log.d(TAG, "Level added: " + level.getName());
                        if (count[0] == levels.length) {
                            onSuccess.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding level", e);
                        onFailure.onFailure(e);
                    });
        }
    }

    private static GameQuestion createQuestion(String id, String question, 
                                              java.util.List<String> options,
                                              int correctIndex, String difficulty, String category) {
        GameQuestion q = new GameQuestion(id, question, options, correctIndex, difficulty, category);
        q.setTimeLimit(30);
        return q;
    }

    private static GameLevel createLevel(String id, String name, String theme, int levelNumber,
                                        String description, java.util.List<String> questionIds) {
        GameLevel level = new GameLevel(id, name, theme, levelNumber);
        level.setDescription(description);
        level.setQuestionIds(questionIds);
        level.setRequiredLevel(levelNumber);
        level.setMonsterId("monster_" + id);
        return level;
    }

    private interface OnFailureListener {
        void onFailure(Exception e);
    }
}
