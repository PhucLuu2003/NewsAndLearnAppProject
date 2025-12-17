package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.Achievement;
import com.example.newsandlearn.Model.DailyTask;
import com.example.newsandlearn.Model.GrammarExercise;
import com.example.newsandlearn.Model.GrammarLesson;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularySet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * SampleDataHelper - Generates sample data for testing
 * Use this to populate Firebase with test data
 */
public class SampleDataHelper {

    private static final String TAG = "SampleDataHelper";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public SampleDataHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Generate all sample data
     */
    public void generateAllSampleData(OnCompleteListener listener) {
        if (auth.getCurrentUser() == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        Log.d(TAG, "Generating sample data...");
        
        generateSampleVocabulary(() -> {
            Log.d(TAG, "Vocabulary generated");
            generateSampleGrammar(() -> {
                Log.d(TAG, "Grammar generated");
                generateSampleAchievements(() -> {
                    Log.d(TAG, "Achievements generated");
                    listener.onSuccess();
                }, listener::onFailure);
            }, listener::onFailure);
        }, listener::onFailure);
    }

    /**
     * Generate sample vocabulary (50 words)
     */
    public void generateSampleVocabulary(OnSuccessListener onSuccess, OnFailureListener onFailure) {
        String userId = auth.getCurrentUser().getUid();
        List<Vocabulary> vocabularyList = createSampleVocabularyList();

        int[] counter = {0};
        for (Vocabulary vocab : vocabularyList) {
            db.collection("users").document(userId)
                    .collection("vocabulary").document(vocab.getId())
                    .set(vocab)
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        if (counter[0] == vocabularyList.size()) {
                            onSuccess.onSuccess();
                        }
                    })
                    .addOnFailureListener(onFailure::onFailure);
        }
    }

    /**
     * Generate sample grammar lessons
     */
    public void generateSampleGrammar(OnSuccessListener onSuccess, OnFailureListener onFailure) {
        List<GrammarLesson> lessons = createSampleGrammarLessons();

        int[] counter = {0};
        for (GrammarLesson lesson : lessons) {
            db.collection("grammar_lessons").document(lesson.getId())
                    .set(lesson)
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        if (counter[0] == lessons.size()) {
                            onSuccess.onSuccess();
                        }
                    })
                    .addOnFailureListener(onFailure::onFailure);
        }
    }

    /**
     * Generate sample achievements
     */
    public void generateSampleAchievements(OnSuccessListener onSuccess, OnFailureListener onFailure) {
        List<Achievement> achievements = createSampleAchievements();

        int[] counter = {0};
        for (Achievement achievement : achievements) {
            db.collection("achievements").document(achievement.getId())
                    .set(achievement)
                    .addOnSuccessListener(aVoid -> {
                        counter[0]++;
                        if (counter[0] == achievements.size()) {
                            onSuccess.onSuccess();
                        }
                    })
                    .addOnFailureListener(onFailure::onFailure);
        }
    }

    // ==================== SAMPLE DATA CREATION ====================

    private List<Vocabulary> createSampleVocabularyList() {
        List<Vocabulary> list = new ArrayList<>();

        // Common words for beginners
        String[][] words = {
                {"vocab_1", "ambitious", "đầy tham vọng", "/æmˈbɪʃəs/", "adjective", "She is very ambitious and wants to become a CEO.", "Cô ấy rất tham vọng và muốn trở thành CEO."},
                {"vocab_2", "achieve", "đạt được", "/əˈtʃiːv/", "verb", "He worked hard to achieve his goals.", "Anh ấy làm việc chăm chỉ để đạt được mục tiêu."},
                {"vocab_3", "benefit", "lợi ích", "/ˈbenɪfɪt/", "noun", "Exercise has many health benefits.", "Tập thể dục có nhiều lợi ích cho sức khỏe."},
                {"vocab_4", "challenge", "thách thức", "/ˈtʃælɪndʒ/", "noun", "Learning a new language is a challenge.", "Học một ngôn ngữ mới là một thách thức."},
                {"vocab_5", "develop", "phát triển", "/dɪˈveləp/", "verb", "We need to develop new skills.", "Chúng ta cần phát triển các kỹ năng mới."},
                {"vocab_6", "efficient", "hiệu quả", "/ɪˈfɪʃnt/", "adjective", "This is a very efficient method.", "Đây là một phương pháp rất hiệu quả."},
                {"vocab_7", "flexible", "linh hoạt", "/ˈfleksəbl/", "adjective", "You need to be flexible in your approach.", "Bạn cần linh hoạt trong cách tiếp cận."},
                {"vocab_8", "generate", "tạo ra", "/ˈdʒenəreɪt/", "verb", "Solar panels generate electricity.", "Tấm pin mặt trời tạo ra điện."},
                {"vocab_9", "implement", "thực hiện", "/ˈɪmplɪment/", "verb", "We will implement the new policy next month.", "Chúng tôi sẽ thực hiện chính sách mới vào tháng sau."},
                {"vocab_10", "maintain", "duy trì", "/meɪnˈteɪn/", "verb", "It's important to maintain a healthy lifestyle.", "Quan trọng là duy trì lối sống lành mạnh."},
                {"vocab_11", "opportunity", "cơ hội", "/ˌɒpəˈtjuːnəti/", "noun", "This is a great opportunity for you.", "Đây là một cơ hội tuyệt vời cho bạn."},
                {"vocab_12", "perspective", "quan điểm", "/pəˈspektɪv/", "noun", "Everyone has a different perspective.", "Mọi người đều có quan điểm khác nhau."},
                {"vocab_13", "significant", "quan trọng", "/sɪɡˈnɪfɪkənt/", "adjective", "This is a significant achievement.", "Đây là một thành tựu quan trọng."},
                {"vocab_14", "strategy", "chiến lược", "/ˈstrætədʒi/", "noun", "We need a better marketing strategy.", "Chúng ta cần một chiến lược marketing tốt hơn."},
                {"vocab_15", "traditional", "truyền thống", "/trəˈdɪʃənl/", "adjective", "This is a traditional Vietnamese dish.", "Đây là một món ăn truyền thống Việt Nam."},
                {"vocab_16", "unique", "độc đáo", "/juˈniːk/", "adjective", "Everyone has a unique personality.", "Mọi người đều có tính cách độc đáo."},
                {"vocab_17", "valuable", "có giá trị", "/ˈvæljuəbl/", "adjective", "Time is the most valuable resource.", "Thời gian là tài nguyên quý giá nhất."},
                {"vocab_18", "analyze", "phân tích", "/ˈænəlaɪz/", "verb", "We need to analyze the data carefully.", "Chúng ta cần phân tích dữ liệu cẩn thận."},
                {"vocab_19", "collaborate", "hợp tác", "/kəˈlæbəreɪt/", "verb", "Let's collaborate on this project.", "Hãy hợp tác trong dự án này."},
                {"vocab_20", "demonstrate", "chứng minh", "/ˈdemənstreɪt/", "verb", "Can you demonstrate how it works?", "Bạn có thể chứng minh nó hoạt động như thế nào không?"}
        };

        for (int i = 0; i < words.length; i++) {
            String[] word = words[i];
            Vocabulary vocab = new Vocabulary(
                String.valueOf(i),  // id
                word[0],            // word
                word[1],            // translation
                "",                 // pronunciation
                "noun",             // partOfSpeech
                word[2],            // example
                "",                 // exampleTranslation
                "B1",               // level
                "general"           // category
            );
            vocab.setPronunciation(word[3]);
            vocab.setPartOfSpeech(word[4]);
            vocab.setExample(word[5]);
            vocab.setExampleTranslation(word[6]);
            vocab.setLevel("B1");
            vocab.setCategory("General");
            
            // Vary mastery levels for testing
            if (i < 5) {
                vocab.setMastery(0); // New
            } else if (i < 10) {
                vocab.setMastery(2); // Learning
                vocab.setReviewCount(3);
            } else if (i < 15) {
                vocab.setMastery(4); // Known
                vocab.setReviewCount(8);
            } else {
                vocab.setMastery(5); // Mastered
                vocab.setReviewCount(15);
            }
            
            vocab.calculateNextReview();
            list.add(vocab);
        }

        return list;
    }

    private List<GrammarLesson> createSampleGrammarLessons() {
        List<GrammarLesson> lessons = new ArrayList<>();

        // Lesson 1: Present Perfect
        GrammarLesson lesson1 = new GrammarLesson("grammar_1", "Present Perfect Tense", 
                "Learn how to use the present perfect tense", "B1", "tenses");
        lesson1.setStructure("Subject + have/has + past participle");
        lesson1.addRule("Use 'have' with I, you, we, they");
        lesson1.addRule("Use 'has' with he, she, it");
        lesson1.addRule("Form: have/has + past participle (V3)");
        lesson1.addExample("I have lived in Hanoi for 5 years.", "Tôi đã sống ở Hà Nội được 5 năm.");
        lesson1.addExample("She has finished her homework.", "Cô ấy đã hoàn thành bài tập về nhà.");
        lesson1.addKeyPoint("Used for actions that started in the past and continue to the present");
        lesson1.addKeyPoint("Often used with 'for' and 'since'");
        lessons.add(lesson1);

        // Lesson 2: Conditional Sentences
        GrammarLesson lesson2 = new GrammarLesson("grammar_2", "Conditional Sentences Type 1",
                "Learn about real conditions in the present or future", "B1", "conditionals");
        lesson2.setStructure("If + present simple, will + base verb");
        lesson2.addRule("Use present simple in the if-clause");
        lesson2.addRule("Use will + base verb in the main clause");
        lesson2.addExample("If it rains, I will stay home.", "Nếu trời mưa, tôi sẽ ở nhà.");
        lesson2.addExample("If you study hard, you will pass the exam.", "Nếu bạn học chăm chỉ, bạn sẽ đỗ kỳ thi.");
        lesson2.addKeyPoint("Used for real or possible situations in the future");
        lessons.add(lesson2);

        return lessons;
    }

    private List<Achievement> createSampleAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // Reading achievements
        achievements.add(new Achievement("ach_1", "First Steps", "Read your first article",
                "📰", Achievement.AchievementCategory.READING, Achievement.AchievementTier.BRONZE,
                "articles_read", 1, 50));
        
        achievements.add(new Achievement("ach_2", "Bookworm", "Read 10 articles",
                "📚", Achievement.AchievementCategory.READING, Achievement.AchievementTier.SILVER,
                "articles_read", 10, 100));
        
        achievements.add(new Achievement("ach_3", "Reading Master", "Read 50 articles",
                "🎓", Achievement.AchievementCategory.READING, Achievement.AchievementTier.GOLD,
                "articles_read", 50, 500));

        // Vocabulary achievements
        achievements.add(new Achievement("ach_4", "Word Collector", "Learn 10 words",
                "🧠", Achievement.AchievementCategory.VOCABULARY, Achievement.AchievementTier.BRONZE,
                "words_learned", 10, 100));
        
        achievements.add(new Achievement("ach_5", "Vocabulary Builder", "Learn 50 words",
                "📝", Achievement.AchievementCategory.VOCABULARY, Achievement.AchievementTier.SILVER,
                "words_learned", 50, 200));
        
        achievements.add(new Achievement("ach_6", "Word Master", "Learn 100 words",
                "🏆", Achievement.AchievementCategory.VOCABULARY, Achievement.AchievementTier.GOLD,
                "words_learned", 100, 500));

        // Streak achievements
        achievements.add(new Achievement("ach_7", "Getting Started", "3-day streak",
                "🔥", Achievement.AchievementCategory.STREAK, Achievement.AchievementTier.BRONZE,
                "streak", 3, 75));
        
        achievements.add(new Achievement("ach_8", "Week Warrior", "7-day streak",
                "⚡", Achievement.AchievementCategory.STREAK, Achievement.AchievementTier.SILVER,
                "streak", 7, 150));
        
        achievements.add(new Achievement("ach_9", "Dedication", "30-day streak",
                "💪", Achievement.AchievementCategory.STREAK, Achievement.AchievementTier.GOLD,
                "streak", 30, 1000));

        return achievements;
    }

    // ==================== INTERFACES ====================

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnSuccessListener {
        void onSuccess();
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }
}
