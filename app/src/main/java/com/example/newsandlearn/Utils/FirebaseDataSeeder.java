package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.VideoLesson;
import com.example.newsandlearn.Model.Vocabulary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseDataSeeder - Tạo dữ liệu mẫu cho Learn module
 * Sử dụng để test và populate dữ liệu ban đầu
 */
public class FirebaseDataSeeder {

    private static final String TAG = "FirebaseDataSeeder";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface SeedCallback {
        void onSuccess(String message);

        void onProgress(String message);

        void onFailure(String error);
    }

    public FirebaseDataSeeder() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Seed tất cả dữ liệu mẫu
     */
    public void seedAllData(SeedCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        callback.onProgress("Starting data seeding...");

        // Step 1: Seed articles
        seedArticles(new SeedCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onProgress("Articles created: " + message);

                // Step 2: Seed video lessons
                seedVideoLessons(new SeedCallback() {
                    @Override
                    public void onSuccess(String message) {
                        callback.onProgress("Video lessons created: " + message);

                        // Step 3: Seed vocabularies
                        seedVocabularies(new SeedCallback() {
                            @Override
                            public void onSuccess(String message) {
                                callback.onProgress("Vocabularies created: " + message);

                                // Step 4: Seed user vocabulary progress
                                seedUserVocabulary(currentUser.getUid(), new SeedCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        callback.onProgress("User vocabulary created: " + message);
                                        callback.onSuccess("All sample data created successfully!");
                                    }

                                    @Override
                                    public void onProgress(String message) {
                                        callback.onProgress(message);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        callback.onFailure("Error creating user vocabulary: " + error);
                                    }
                                });
                            }

                            @Override
                            public void onProgress(String message) {
                                callback.onProgress(message);
                            }

                            @Override
                            public void onFailure(String error) {
                                callback.onFailure("Error creating vocabularies: " + error);
                            }
                        });
                    }

                    @Override
                    public void onProgress(String message) {
                        callback.onProgress(message);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure("Error creating video lessons: " + error);
                    }
                });
            }

            @Override
            public void onProgress(String message) {
                callback.onProgress(message);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure("Error creating articles: " + error);
            }
        });
    }

    /**
     * Tạo từ vựng mẫu trong collection "vocabularies"
     */
    public void seedVocabularies(SeedCallback callback) {
        callback.onProgress("Creating sample vocabularies...");

        List<Vocabulary> vocabularies = createSampleVocabularies();
        WriteBatch batch = db.batch();

        for (Vocabulary vocab : vocabularies) {
            String vocabId = vocab.getId();
            if (vocabId == null || vocabId.isEmpty()) {
                vocabId = db.collection("vocabularies").document().getId();
                vocab.setId(vocabId);
            }
            batch.set(db.collection("vocabularies").document(vocabId), vocab);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sample vocabularies created successfully");
                    callback.onSuccess(vocabularies.size() + " words created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample vocabularies", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Tạo tiến độ học từ vựng cho user
     */
    public void seedUserVocabulary(String userId, SeedCallback callback) {
        callback.onProgress("Creating user vocabulary progress...");

        // First get all vocabularies
        db.collection("vocabularies")
                .limit(50)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onFailure("No vocabularies found. Please seed vocabularies first.");
                        return;
                    }

                    WriteBatch batch = db.batch();
                    int count = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String vocabId = doc.getId();

                        // Create progress for this vocabulary
                        UserVocabulary userVocab = createUserVocabularyProgress(vocabId, count);

                        batch.set(db.collection("users").document(userId)
                                .collection("user_vocabulary").document(vocabId), userVocab);
                        count++;
                    }

                    final int finalCount = count;
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User vocabulary progress created successfully");
                                callback.onSuccess(finalCount + " words added to user vocabulary");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating user vocabulary", e);
                                callback.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading vocabularies", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Tạo danh sách từ vựng mẫu
     */
    private List<Vocabulary> createSampleVocabularies() {
        List<Vocabulary> vocabularies = new ArrayList<>();

        // Basic Words (A1)
        vocabularies.add(createVocab("vocab_hello", "Hello", "Xin chào", "/həˈləʊ/", "greeting", "A1",
                Arrays.asList("Hi there!", "Hello, how are you?"),
                Arrays.asList("Chào bạn!", "Xin chào, bạn khỏe không?")));

        vocabularies.add(createVocab("vocab_goodbye", "Goodbye", "Tạm biệt", "/ˌɡʊdˈbaɪ/", "greeting", "A1",
                Arrays.asList("Goodbye, see you later!", "It's time to say goodbye."),
                Arrays.asList("Tạm biệt, hẹn gặp lại!", "Đã đến lúc phải nói tạm biệt.")));

        vocabularies.add(createVocab("vocab_thank", "Thank you", "Cảm ơn", "/θæŋk juː/", "expression", "A1",
                Arrays.asList("Thank you very much!", "Thank you for your help."),
                Arrays.asList("Cảm ơn rất nhiều!", "Cảm ơn bạn đã giúp đỡ.")));

        vocabularies.add(createVocab("vocab_please", "Please", "Xin vui lòng", "/pliːz/", "expression", "A1",
                Arrays.asList("Please help me.", "Could you please wait?"),
                Arrays.asList("Xin vui lòng giúp tôi.", "Bạn có thể vui lòng đợi không?")));

        vocabularies.add(createVocab("vocab_sorry", "Sorry", "Xin lỗi", "/ˈsɒri/", "expression", "A1",
                Arrays.asList("I'm sorry for being late.", "Sorry, I didn't mean it."),
                Arrays.asList("Tôi xin lỗi vì đến muộn.", "Xin lỗi, tôi không cố ý.")));

        // Common Verbs (A1-A2)
        vocabularies.add(createVocab("vocab_eat", "Eat", "Ăn", "/iːt/", "verb", "A1",
                Arrays.asList("I eat breakfast at 7 AM.", "Let's eat dinner together."),
                Arrays.asList("Tôi ăn sáng lúc 7 giờ sáng.", "Hãy cùng ăn tối nhé.")));

        vocabularies.add(createVocab("vocab_drink", "Drink", "Uống", "/drɪŋk/", "verb", "A1",
                Arrays.asList("I drink coffee every morning.", "Would you like to drink some water?"),
                Arrays.asList("Tôi uống cà phê mỗi sáng.", "Bạn có muốn uống nước không?")));

        vocabularies.add(createVocab("vocab_sleep", "Sleep", "Ngủ", "/sliːp/", "verb", "A1",
                Arrays.asList("I sleep 8 hours every night.", "The baby is sleeping."),
                Arrays.asList("Tôi ngủ 8 tiếng mỗi đêm.", "Em bé đang ngủ.")));

        vocabularies.add(createVocab("vocab_study", "Study", "Học", "/ˈstʌdi/", "verb", "A1",
                Arrays.asList("I study English every day.", "She studies at the library."),
                Arrays.asList("Tôi học tiếng Anh mỗi ngày.", "Cô ấy học ở thư viện.")));

        vocabularies.add(createVocab("vocab_work", "Work", "Làm việc", "/wɜːk/", "verb", "A1",
                Arrays.asList("I work from 9 to 5.", "He works in an office."),
                Arrays.asList("Tôi làm việc từ 9 đến 5.", "Anh ấy làm việc trong văn phòng.")));

        // Common Nouns (A1-A2)
        vocabularies.add(createVocab("vocab_book", "Book", "Sách", "/bʊk/", "noun", "A1",
                Arrays.asList("I'm reading a book.", "This book is very interesting."),
                Arrays.asList("Tôi đang đọc sách.", "Cuốn sách này rất thú vị.")));

        vocabularies.add(createVocab("vocab_water", "Water", "Nước", "/ˈwɔːtə/", "noun", "A1",
                Arrays.asList("I drink a lot of water.", "The water is cold."),
                Arrays.asList("Tôi uống nhiều nước.", "Nước lạnh quá.")));

        vocabularies.add(createVocab("vocab_food", "Food", "Thức ăn", "/fuːd/", "noun", "A1",
                Arrays.asList("This food is delicious.", "I love Vietnamese food."),
                Arrays.asList("Thức ăn này ngon quá.", "Tôi yêu món ăn Việt Nam.")));

        vocabularies.add(createVocab("vocab_house", "House", "Nhà", "/haʊs/", "noun", "A1",
                Arrays.asList("I live in a big house.", "Their house is beautiful."),
                Arrays.asList("Tôi sống trong một ngôi nhà lớn.", "Nhà của họ đẹp quá.")));

        vocabularies.add(createVocab("vocab_family", "Family", "Gia đình", "/ˈfæməli/", "noun", "A1",
                Arrays.asList("My family is very important to me.", "I love my family."),
                Arrays.asList("Gia đình rất quan trọng với tôi.", "Tôi yêu gia đình mình.")));

        // Intermediate Words (B1-B2)
        vocabularies.add(createVocab("vocab_achieve", "Achieve", "Đạt được", "/əˈtʃiːv/", "verb", "B1",
                Arrays.asList("She achieved her goals.", "You can achieve anything with hard work."),
                Arrays.asList("Cô ấy đã đạt được mục tiêu.", "Bạn có thể đạt được bất cứ điều gì với sự chăm chỉ.")));

        vocabularies.add(createVocab("vocab_challenge", "Challenge", "Thách thức", "/ˈtʃælɪndʒ/", "noun", "B1",
                Arrays.asList("This is a big challenge.", "I love a good challenge."),
                Arrays.asList("Đây là một thách thức lớn.", "Tôi thích thách thức.")));

        vocabularies.add(createVocab("vocab_opportunity", "Opportunity", "Cơ hội", "/ˌɒpəˈtjuːnəti/", "noun", "B1",
                Arrays.asList("This is a great opportunity.", "Don't miss this opportunity."),
                Arrays.asList("Đây là một cơ hội tuyệt vời.", "Đừng bỏ lỡ cơ hội này.")));

        vocabularies.add(createVocab("vocab_experience", "Experience", "Kinh nghiệm", "/ɪkˈspɪəriəns/", "noun", "B1",
                Arrays.asList("I have 5 years of experience.", "It was a wonderful experience."),
                Arrays.asList("Tôi có 5 năm kinh nghiệm.", "Đó là một trải nghiệm tuyệt vời.")));

        vocabularies.add(createVocab("vocab_develop", "Develop", "Phát triển", "/dɪˈveləp/", "verb", "B1",
                Arrays.asList("We need to develop new skills.", "The company is developing rapidly."),
                Arrays.asList("Chúng ta cần phát triển kỹ năng mới.", "Công ty đang phát triển nhanh chóng.")));

        // Advanced Words (C1-C2)
        vocabularies.add(
                createVocab("vocab_comprehensive", "Comprehensive", "Toàn diện", "/ˌkɒmprɪˈhensɪv/", "adjective", "C1",
                        Arrays.asList("This is a comprehensive guide.", "We need a comprehensive solution."),
                        Arrays.asList("Đây là một hướng dẫn toàn diện.", "Chúng ta cần một giải pháp toàn diện.")));

        vocabularies.add(createVocab("vocab_substantial", "Substantial", "Đáng kể", "/səbˈstænʃl/", "adjective", "C1",
                Arrays.asList("There's been substantial progress.", "We need substantial evidence."),
                Arrays.asList("Đã có tiến bộ đáng kể.", "Chúng ta cần bằng chứng đáng kể.")));

        return vocabularies;
    }

    /**
     * Helper method để tạo Vocabulary object
     */
    private Vocabulary createVocab(String id, String word, String translation, String phonetic,
                                   String category, String level, List<String> examples,
                                   List<String> exampleTranslations) {
        Vocabulary vocab = new Vocabulary();
        vocab.setId(id);
        vocab.setWord(word);
        vocab.setTranslation(translation);
        vocab.setPhonetic(phonetic);
        vocab.setCategory(category);
        vocab.setLevel(level);
        vocab.setExamples(examples);
        vocab.setExampleTranslations(exampleTranslations);
        vocab.setCreatedAt(new Date());
        return vocab;
    }

    /**
     * Tạo tiến độ học từ vựng cho user với các level khác nhau
     */
    private UserVocabulary createUserVocabularyProgress(String vocabId, int index) {
        UserVocabulary userVocab = new UserVocabulary();
        userVocab.setVocabularyId(vocabId);

        // Tạo progress đa dạng
        if (index < 5) {
            // Mastered
            userVocab.setLevel("mastered");
            userVocab.setCorrectCount(10);
            userVocab.setIncorrectCount(1);
            userVocab.setReviewCount(12);
        } else if (index < 12) {
            // Learning
            userVocab.setLevel("learning");
            userVocab.setCorrectCount(3);
            userVocab.setIncorrectCount(2);
            userVocab.setReviewCount(6);
        } else {
            // New
            userVocab.setLevel("new");
            userVocab.setCorrectCount(0);
            userVocab.setIncorrectCount(0);
            userVocab.setReviewCount(0);
        }

        userVocab.setIsFavorite(index % 7 == 0); // Mỗi từ thứ 7 là favorite
        userVocab.setAddedAt(new Date());
        userVocab.setLastReviewedAt(new Date());
        userVocab.calculateNextReviewDate();

        return userVocab;
    }

    /**
     * Seed articles vào Firestore
     */
    public void seedArticles(SeedCallback callback) {
        callback.onProgress("Creating sample articles...");

        List<Article> articles = createSampleArticles();
        WriteBatch batch = db.batch();

        for (Article article : articles) {
            String articleId = article.getId();
            if (articleId == null || articleId.isEmpty()) {
                articleId = db.collection("articles").document().getId();
                article.setId(articleId);
            }
            batch.set(db.collection("articles").document(articleId), article);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sample articles created successfully");
                    callback.onSuccess(articles.size() + " articles created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample articles", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Tạo danh sách articles TIẾNG ANH thực tế để học
     */
    private List<Article> createSampleArticles() {
        List<Article> articles = new ArrayList<>();

        // Easy Articles (A1-A2 Level)
        articles.add(new Article(
                "article_01",
                "10 Effective Ways to Learn English in 2024",
                "Learning English doesn't have to be difficult or boring. Here are 10 proven methods that will help you improve your English skills quickly and effectively.\n\n" +
                        "1. Watch Movies and TV Shows\n" +
                        "Watching English movies and TV shows is one of the most enjoyable ways to learn. Start with subtitles in your language, then switch to English subtitles, and finally try watching without any subtitles. This helps you understand natural conversation and improves your listening skills.\n\n" +
                        "2. Listen to Podcasts\n" +
                        "Podcasts are perfect for learning on the go. Choose topics you're interested in and listen during your commute or while exercising. Some great podcasts for learners include BBC Learning English, ESL Pod, and All Ears English.\n\n" +
                        "3. Use Language Learning Apps\n" +
                        "Apps like Duolingo, Memrise, and Babbel make learning fun with games and daily challenges. Spend just 15-20 minutes a day and you'll see real progress.\n\n" +
                        "4. Practice with Native Speakers\n" +
                        "Find language exchange partners online through apps like HelloTalk or Tandem. Speaking with native speakers helps you learn real, natural English and builds your confidence.\n\n" +
                        "5. Read English Books\n" +
                        "Start with simple books or graded readers designed for learners. As you improve, move on to novels, newspapers, and magazines. Reading improves your vocabulary and grammar naturally.\n\n" +
                        "6. Keep a Daily Journal\n" +
                        "Write about your day in English. Don't worry about making mistakes - the important thing is to practice expressing your thoughts in English.\n\n" +
                        "7. Use Flashcards for Vocabulary\n" +
                        "Create flashcards for new words you learn. Review them regularly using the spaced repetition method. Apps like Anki can help with this.\n\n" +
                        "8. Join Online Communities\n" +
                        "Participate in English forums, Facebook groups, or Reddit communities. Reading and writing comments helps you practice informal English.\n\n" +
                        "9. Take Online Courses\n" +
                        "Platforms like Coursera, edX, and Udemy offer free and paid English courses. Choose courses that match your level and interests.\n\n" +
                        "10. Set Realistic Goals\n" +
                        "Don't try to learn everything at once. Set small, achievable goals like learning 10 new words per week or watching one English video per day. Celebrate your progress!\n\n" +
                        "Remember, consistency is key. Even 15 minutes of practice every day is better than studying for hours once a week. Choose methods you enjoy and stick with them. Good luck with your English learning journey!",
                "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800",
                "Learning Tips",
                "easy",
                "BBC Learning English",
                new Date(System.currentTimeMillis() - 86400000),
                1250,
                5,
                false));

        articles.add(new Article(
                "article_02",
                "How to Improve Your English Listening Skills",
                "Listening is one of the most important skills in learning English, but it can also be one of the most challenging. Here's a comprehensive guide to help you improve.\n\n" +
                        "Understanding the Challenge\n" +
                        "Many English learners find listening difficult because native speakers talk fast, use contractions, and have different accents. Don't worry - with the right practice, you can overcome these challenges.\n\n" +
                        "Step 1: Start at Your Level\n" +
                        "Don't jump into advanced content too quickly. Begin with materials designed for your level. Graded listening exercises and slow-speed podcasts are perfect for beginners.\n\n" +
                        "Step 2: Active Listening Practice\n" +
                        "Active listening means focusing completely on what you hear. Try these techniques:\n" +
                        "- Listen to a short audio clip (1-2 minutes)\n" +
                        "- Write down what you understand\n" +
                        "- Listen again and add more details\n" +
                        "- Check the transcript and note what you missed\n" +
                        "- Listen one more time to hear those parts clearly\n\n" +
                        "Step 3: Use Different Resources\n" +
                        "Variety is important. Mix different types of listening materials:\n" +
                        "- Podcasts: BBC 6 Minute English, ESL Pod\n" +
                        "- YouTube channels: TED-Ed, BBC Learning English\n" +
                        "- Audiobooks: Start with children's books, then move to adult fiction\n" +
                        "- News: BBC News, CNN 10\n" +
                        "- Movies and TV shows with subtitles\n\n" +
                        "Step 4: Learn to Predict\n" +
                        "Before listening, think about the topic. What vocabulary might you hear? What's the context? This preparation helps your brain recognize words more easily.\n\n" +
                        "Step 5: Focus on Chunks, Not Words\n" +
                        "Native speakers don't speak word by word. They use phrases and expressions. Learn common chunks like 'I'm going to', 'Would you like', 'It depends on'.\n\n" +
                        "Step 6: Practice Shadowing\n" +
                        "Shadowing means repeating what you hear immediately after hearing it. This improves both listening and speaking. Start with slow, clear audio and gradually increase the speed.\n\n" +
                        "Step 7: Be Patient and Consistent\n" +
                        "Improving listening takes time. Practice for at least 20-30 minutes every day. You might not notice progress immediately, but keep going - your brain is learning!\n\n" +
                        "Common Mistakes to Avoid:\n" +
                        "- Trying to understand every single word\n" +
                        "- Only listening to one type of accent\n" +
                        "- Not reviewing what you've listened to\n" +
                        "- Giving up when it feels difficult\n\n" +
                        "Remember, everyone struggles with listening at first. The key is regular practice with materials you find interesting. Make listening a daily habit, and you'll be amazed at your progress!",
                "https://images.unsplash.com/photo-1542435503-956c469947f6?w=800",
                "Skills",
                "easy",
                "English Central",
                new Date(System.currentTimeMillis() - 172800000),
                890,
                7,
                false));

        articles.add(new Article(
                "article_03",
                "Essential English Phrases for Daily Conversation",
                "Master these common English phrases to communicate confidently in everyday situations.\n\n" +
                        "Greetings and Introductions\n" +
                        "- How's it going? (informal greeting)\n" +
                        "- Nice to meet you. / Pleased to meet you. (first meeting)\n" +
                        "- How have you been? (greeting someone you haven't seen for a while)\n" +
                        "- What do you do? (asking about someone's job)\n\n" +
                        "Making Small Talk\n" +
                        "- How was your weekend?\n" +
                        "- The weather's nice today, isn't it?\n" +
                        "- Have you been busy lately?\n" +
                        "- What are you up to these days?\n\n" +
                        "Asking for Help\n" +
                        "- Could you help me with something?\n" +
                        "- I'm not sure how to...\n" +
                        "- Would you mind explaining that again?\n" +
                        "- I'm having trouble with...\n\n" +
                        "In a Restaurant\n" +
                        "- I'd like to make a reservation for two.\n" +
                        "- Could I see the menu, please?\n" +
                        "- I'll have the chicken, please.\n" +
                        "- Could we get the bill, please?\n\n" +
                        "Shopping\n" +
                        "- How much is this?\n" +
                        "- Do you have this in a different size/color?\n" +
                        "- Can I try this on?\n" +
                        "- I'm just looking, thanks.\n\n" +
                        "Asking for Directions\n" +
                        "- Excuse me, how do I get to...?\n" +
                        "- Is it far from here?\n" +
                        "- Could you show me on the map?\n" +
                        "- Which way should I go?\n\n" +
                        "At Work\n" +
                        "- Let me get back to you on that.\n" +
                        "- I'll look into it.\n" +
                        "- Could we schedule a meeting?\n" +
                        "- I'll send you an email about it.\n\n" +
                        "Expressing Opinions\n" +
                        "- In my opinion...\n" +
                        "- I think that...\n" +
                        "- From my point of view...\n" +
                        "- As far as I'm concerned...\n\n" +
                        "Agreeing and Disagreeing\n" +
                        "- I completely agree.\n" +
                        "- That's a good point.\n" +
                        "- I see what you mean, but...\n" +
                        "- I'm not sure I agree with that.\n\n" +
                        "Practice Tips:\n" +
                        "1. Learn phrases in context, not in isolation\n" +
                        "2. Practice saying them out loud\n" +
                        "3. Use them in real conversations\n" +
                        "4. Pay attention to when native speakers use them\n" +
                        "5. Keep a notebook of new phrases you hear\n\n" +
                        "Remember, using these phrases naturally takes practice. Start with a few phrases each week and use them until they become automatic. Soon, you'll be speaking English more fluently and confidently!",
                "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                "Vocabulary",
                "easy",
                "FluentU",
                new Date(System.currentTimeMillis() - 259200000),
                1420,
                6,
                false));

        // Medium Articles (B1-B2 Level)
        articles.add(new Article(
                "article_04",
                "Mastering Conditional Sentences in English",
                "Conditional sentences are essential for expressing possibilities, hypothetical situations, and cause-and-effect relationships. This comprehensive guide will help you master all types of conditionals.\n\n" +
                        "Zero Conditional (General Truths)\n" +
                        "Structure: If + present simple, present simple\n" +
                        "Use: For facts and general truths that are always true\n" +
                        "Examples:\n" +
                        "- If you heat water to 100°C, it boils.\n" +
                        "- If I don't sleep well, I feel tired.\n" +
                        "- Plants die if they don't get water.\n\n" +
                        "First Conditional (Real Future Possibilities)\n" +
                        "Structure: If + present simple, will + base verb\n" +
                        "Use: For real and possible situations in the future\n" +
                        "Examples:\n" +
                        "- If it rains tomorrow, we'll stay home.\n" +
                        "- If you study hard, you'll pass the exam.\n" +
                        "- I'll call you if I have time.\n\n" +
                        "Second Conditional (Unreal Present)\n" +
                        "Structure: If + past simple, would + base verb\n" +
                        "Use: For imaginary or unlikely situations in the present or future\n" +
                        "Examples:\n" +
                        "- If I had more money, I would travel the world.\n" +
                        "- If I were you, I would accept the job offer.\n" +
                        "- What would you do if you won the lottery?\n\n" +
                        "Third Conditional (Unreal Past)\n" +
                        "Structure: If + past perfect, would have + past participle\n" +
                        "Use: For imaginary situations in the past (things that didn't happen)\n" +
                        "Examples:\n" +
                        "- If I had studied harder, I would have passed the exam.\n" +
                        "- If we had left earlier, we wouldn't have missed the train.\n" +
                        "- She would have come to the party if she had known about it.\n\n" +
                        "Mixed Conditionals\n" +
                        "Sometimes we mix time references:\n" +
                        "- If I had studied medicine (past), I would be a doctor now (present).\n" +
                        "- If I were rich (present), I would have bought that house (past).\n\n" +
                        "Common Mistakes to Avoid:\n" +
                        "1. Don't use 'will' in the if-clause: ❌ If it will rain... ✅ If it rains...\n" +
                        "2. Use 'were' for all persons in second conditional: If I were/If he were\n" +
                        "3. Don't mix conditional types randomly\n\n" +
                        "Practice Exercises:\n" +
                        "Complete these sentences:\n" +
                        "1. If I _____ (have) more time, I _____ (learn) another language.\n" +
                        "2. If she _____ (study) harder last year, she _____ (pass) the exam.\n" +
                        "3. If you _____ (heat) ice, it _____ (melt).\n\n" +
                        "Tips for IELTS and Cambridge Exams:\n" +
                        "- Use a variety of conditional forms in your writing\n" +
                        "- Practice using conditionals in speaking to discuss hypothetical situations\n" +
                        "- Learn conditional phrases like 'provided that', 'unless', 'as long as'\n\n" +
                        "Keep practicing with real examples and soon conditionals will become natural!",
                "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800",
                "Grammar",
                "medium",
                "Cambridge English",
                new Date(System.currentTimeMillis() - 345600000),
                650,
                10,
                false));

        articles.add(new Article(
                "article_05",
                "IELTS Speaking: How to Achieve Band 7.0+",
                "Getting a high score in IELTS Speaking requires more than just good English. You need to understand what examiners are looking for and how to demonstrate your skills effectively.\n\n" +
                        "Understanding the Assessment Criteria\n\n" +
                        "1. Fluency and Coherence (25%)\n" +
                        "This measures how smoothly you speak and how well you organize your ideas.\n" +
                        "To improve:\n" +
                        "- Speak at a natural pace without long pauses\n" +
                        "- Use linking words: however, moreover, on the other hand\n" +
                        "- Develop your answers fully - don't give one-word responses\n" +
                        "- Practice speaking for 2 minutes without stopping\n\n" +
                        "2. Lexical Resource (25%)\n" +
                        "This assesses your vocabulary range and accuracy.\n" +
                        "To improve:\n" +
                        "- Use topic-specific vocabulary naturally\n" +
                        "- Learn collocations (word combinations): make a decision, take responsibility\n" +
                        "- Use idiomatic expressions appropriately\n" +
                        "- Paraphrase instead of repeating the same words\n\n" +
                        "3. Grammatical Range and Accuracy (25%)\n" +
                        "This looks at the variety and correctness of your grammar.\n" +
                        "To improve:\n" +
                        "- Use a mix of simple and complex sentences\n" +
                        "- Include different tenses naturally\n" +
                        "- Use conditional sentences and passive voice when appropriate\n" +
                        "- Minor errors are okay, but avoid basic mistakes\n\n" +
                        "4. Pronunciation (25%)\n" +
                        "This measures how clearly you speak and your intonation.\n" +
                        "To improve:\n" +
                        "- Focus on word stress and sentence stress\n" +
                        "- Work on problematic sounds\n" +
                        "- Use natural intonation patterns\n" +
                        "- Speak clearly but don't try to sound like a native speaker\n\n" +
                        "Part 1 Strategy (4-5 minutes)\n" +
                        "Questions about familiar topics: work, studies, hobbies\n" +
                        "- Give extended answers (2-3 sentences)\n" +
                        "- Add examples or reasons\n" +
                        "Example:\n" +
                        "Q: Do you like reading?\n" +
                        "❌ Yes, I do.\n" +
                        "✅ Yes, I really enjoy reading, especially fiction novels. I find it's a great way to relax after a long day at work, and it also helps me improve my vocabulary.\n\n" +
                        "Part 2 Strategy (3-4 minutes)\n" +
                        "Individual long turn - speak for 2 minutes on a topic\n" +
                        "- Use the 1-minute preparation time wisely\n" +
                        "- Make brief notes, not full sentences\n" +
                        "- Cover all points on the card\n" +
                        "- Speak for the full 2 minutes\n" +
                        "- Use past tenses for past experiences\n\n" +
                        "Part 3 Strategy (4-5 minutes)\n" +
                        "Discussion of abstract ideas related to Part 2\n" +
                        "- Give opinions and justify them\n" +
                        "- Consider different perspectives\n" +
                        "- Use phrases like: 'It depends on...', 'There are several factors...'\n" +
                        "- Don't memorize answers - examiners can tell!\n\n" +
                        "Common Mistakes to Avoid:\n" +
                        "1. Memorizing answers word-for-word\n" +
                        "2. Speaking too fast or too slow\n" +
                        "3. Giving very short answers\n" +
                        "4. Using overly complex vocabulary incorrectly\n" +
                        "5. Asking the examiner to repeat questions too often\n\n" +
                        "Practice Tips:\n" +
                        "- Record yourself and listen back\n" +
                        "- Practice with a partner or tutor\n" +
                        "- Time yourself for Part 2\n" +
                        "- Learn topic vocabulary for common IELTS themes\n" +
                        "- Stay calm and confident during the test\n\n" +
                        "Remember, the IELTS Speaking test is a conversation, not an interrogation. Be natural, be yourself, and show the examiner your best English!",
                "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800",
                "IELTS",
                "medium",
                "IDP IELTS",
                new Date(System.currentTimeMillis() - 432000000),
                2100,
                8,
                false));

        // Add more articles with full English content...
        // (Continuing with remaining articles in similar detailed format)

        return articles;
    }

    /**
     * Clear all existing video lessons from Firestore
     */
    public void clearVideoLessons(SeedCallback callback) {
        callback.onProgress("Clearing existing video lessons...");
        
        db.collection("video_lessons")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess("No video lessons to clear");
                        return;
                    }
                    
                    WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Video lessons cleared successfully");
                                callback.onSuccess(querySnapshot.size() + " video lessons deleted");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error clearing video lessons", e);
                                callback.onFailure(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading video lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }
    
    /**
     * Seed video lessons vào Firestore (with option to clear first)
     */
    public void seedVideoLessons(SeedCallback callback) {
        seedVideoLessons(callback, false);
    }
    
    /**
     * Seed video lessons vào Firestore
     */
    public void seedVideoLessons(SeedCallback callback, boolean clearFirst) {
        if (clearFirst) {
            clearVideoLessons(new SeedCallback() {
                @Override
                public void onSuccess(String message) {
                    callback.onProgress("Cleared: " + message);
                    createAndSeedVideoLessons(callback);
                }

                @Override
                public void onProgress(String message) {
                    callback.onProgress(message);
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure("Error clearing video lessons: " + error);
                }
            });
        } else {
            createAndSeedVideoLessons(callback);
        }
    }
    
    private void createAndSeedVideoLessons(SeedCallback callback) {
        callback.onProgress("Creating sample video lessons...");

        List<VideoLesson> lessons = createSampleVideoLessons();
        WriteBatch batch = db.batch();

        for (VideoLesson lesson : lessons) {
            String lessonId = lesson.getId();
            if (lessonId == null || lessonId.isEmpty()) {
                lessonId = db.collection("video_lessons").document().getId();
                lesson.setId(lessonId);
            }
            batch.set(db.collection("video_lessons").document(lessonId), lesson);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sample video lessons created successfully");
                    callback.onSuccess(lessons.size() + " video lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating sample video lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Tạo danh sách 5 video lessons với URL video thật và câu hỏi liên quan
     */
    private List<VideoLesson> createSampleVideoLessons() {
        List<VideoLesson> lessons = new ArrayList<>();

        // Video 1: BBC Learning English - Greetings & Introductions
        lessons.add(new VideoLesson(
                "video_01",
                "Greetings and Introductions in English",
                "Learn how to greet people and introduce yourself in English with common phrases and expressions.",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "https://images.unsplash.com/photo-1521737711867-e3b97375f902?w=800",
                "A1",
                "Conversation",
                596,
                createGreetingsQuestions()
        ));

        // Video 2: English Grammar - Present Simple Tense
        lessons.add(new VideoLesson(
                "video_02",
                "Present Simple Tense - Complete Guide",
                "Master the present simple tense with clear explanations, examples, and practice exercises.",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800",
                "A2",
                "Grammar",
                654,
                createPresentSimpleQuestions()
        ));

        // Video 3: Essential English Vocabulary
        lessons.add(new VideoLesson(
                "video_03",
                "100 Common English Phrases for Daily Life",
                "Learn the most useful English phrases and expressions for everyday conversations and situations.",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800",
                "A2",
                "Vocabulary",
                15,
                createDailyPhrasesQuestions()
        ));

        // Video 4: English Listening Practice
        lessons.add(new VideoLesson(
                "video_04",
                "Improve Your English Listening Skills",
                "Practice your listening comprehension with real-life conversations and stories at natural speed.",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://images.unsplash.com/photo-1542435503-956c469947f6?w=800",
                "B1",
                "Listening",
                15,
                createListeningPracticeQuestions()
        ));

        // Video 5: English Pronunciation Masterclass
        lessons.add(new VideoLesson(
                "video_05",
                "Perfect Your English Pronunciation",
                "Learn correct pronunciation of difficult English sounds, stress patterns, and intonation.",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800",
                "B1",
                "Pronunciation",
                60,
                createPronunciationMasterclassQuestions()
        ));

        return lessons;
    }

    /**
     * Questions for Greetings and Introductions video
     */
    private List<Question> createGreetingsQuestions() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("Which is the most formal way to greet someone?");
        q1.setQuestionType("multiple_choice");
        q1.setOptions(Arrays.asList(
                "Good morning, Mr. Smith",
                "Hey, what's up?",
                "Hi there!",
                "Yo!"
        ));
        q1.setCorrectAnswer("Good morning, Mr. Smith");
        q1.setTimestamp(60);
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("What is the correct response to 'How are you?'");
        q2.setQuestionType("multiple_choice");
        q2.setOptions(Arrays.asList(
                "I'm fine, thank you. And you?",
                "Yes, please",
                "No, thank you",
                "Goodbye"
        ));
        q2.setCorrectAnswer("I'm fine, thank you. And you?");
        q2.setTimestamp(120);
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("When introducing yourself, which phrase is most appropriate?");
        q3.setQuestionType("multiple_choice");
        q3.setOptions(Arrays.asList(
                "My name is John. Nice to meet you.",
                "I am John. Go away.",
                "John here. What do you want?",
                "This is John speaking on phone only"
        ));
        q3.setCorrectAnswer("My name is John. Nice to meet you.");
        q3.setTimestamp(180);
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("Which greeting is appropriate for any time of day?");
        q4.setQuestionType("multiple_choice");
        q4.setOptions(Arrays.asList(
                "Hello",
                "Good morning",
                "Good afternoon",
                "Good evening"
        ));
        q4.setCorrectAnswer("Hello");
        q4.setTimestamp(240);
        questions.add(q4);

        return questions;
    }

    /**
     * Questions for Present Simple Tense video
     */
    private List<Question> createPresentSimpleQuestions() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("Which sentence correctly uses the present simple tense?");
        q1.setQuestionType("multiple_choice");
        q1.setOptions(Arrays.asList(
                "She works at a hospital",
                "She working at a hospital",
                "She is work at a hospital",
                "She work at a hospital"
        ));
        q1.setCorrectAnswer("She works at a hospital");
        q1.setTimestamp(90);
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("When do we use the present simple tense?");
        q2.setQuestionType("multiple_choice");
        q2.setOptions(Arrays.asList(
                "For habits and routines",
                "For actions happening now",
                "For future plans",
                "For past events"
        ));
        q2.setCorrectAnswer("For habits and routines");
        q2.setTimestamp(150);
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("What is the correct negative form of 'He plays football'?");
        q3.setQuestionType("multiple_choice");
        q3.setOptions(Arrays.asList(
                "He doesn't play football",
                "He don't play football",
                "He not plays football",
                "He isn't play football"
        ));
        q3.setCorrectAnswer("He doesn't play football");
        q3.setTimestamp(300);
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("Which question is formed correctly in present simple?");
        q4.setQuestionType("multiple_choice");
        q4.setOptions(Arrays.asList(
                "Do you like coffee?",
                "Are you like coffee?",
                "You like coffee?",
                "Does you like coffee?"
        ));
        q4.setCorrectAnswer("Do you like coffee?");
        q4.setTimestamp(400);
        questions.add(q4);

        return questions;
    }

    /**
     * Questions for Daily Phrases video
     */
    private List<Question> createDailyPhrasesQuestions() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What does 'Excuse me' mean in daily conversation?");
        q1.setQuestionType("multiple_choice");
        q1.setOptions(Arrays.asList(
                "To politely get someone's attention or apologize",
                "To say goodbye",
                "To express happiness",
                "To ask for food"
        ));
        q1.setCorrectAnswer("To politely get someone's attention or apologize");
        q1.setTimestamp(5);
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("Which phrase means 'I don't understand'?");
        q2.setQuestionType("multiple_choice");
        q2.setOptions(Arrays.asList(
                "I don't get it",
                "I get it",
                "No problem",
                "You're welcome"
        ));
        q2.setCorrectAnswer("I don't get it");
        q2.setTimestamp(8);
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("What is the polite way to ask someone to repeat?");
        q3.setQuestionType("multiple_choice");
        q3.setOptions(Arrays.asList(
                "Could you say that again, please?",
                "What?",
                "Huh?",
                "I don't care"
        ));
        q3.setCorrectAnswer("Could you say that again, please?");
        q3.setTimestamp(10);
        questions.add(q3);

        return questions;
    }

    /**
     * Questions for Listening Practice video
     */
    private List<Question> createListeningPracticeQuestions() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is the best way to improve listening skills?");
        q1.setQuestionType("multiple_choice");
        q1.setOptions(Arrays.asList(
                "Practice regularly with native speakers",
                "Only read books",
                "Avoid listening to English",
                "Study grammar only"
        ));
        q1.setCorrectAnswer("Practice regularly with native speakers");
        q1.setTimestamp(5);
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("Why is it important to listen to different accents?");
        q2.setQuestionType("multiple_choice");
        q2.setOptions(Arrays.asList(
                "To understand English speakers from different countries",
                "It's not important",
                "To confuse yourself",
                "To learn only one accent"
        ));
        q2.setCorrectAnswer("To understand English speakers from different countries");
        q2.setTimestamp(8);
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("What should you do when you don't understand something?");
        q3.setQuestionType("multiple_choice");
        q3.setOptions(Arrays.asList(
                "Ask for clarification politely",
                "Give up immediately",
                "Pretend to understand",
                "Change the topic"
        ));
        q3.setCorrectAnswer("Ask for clarification politely");
        q3.setTimestamp(12);
        questions.add(q3);

        return questions;
    }

    /**
     * Questions for Pronunciation Masterclass video
     */
    private List<Question> createPronunciationMasterclassQuestions() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("Why is correct pronunciation important?");
        q1.setQuestionType("multiple_choice");
        q1.setOptions(Arrays.asList(
                "To communicate clearly and be understood",
                "To sound like a robot",
                "It's not important at all",
                "To confuse people"
        ));
        q1.setCorrectAnswer("To communicate clearly and be understood");
        q1.setTimestamp(15);
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("What is word stress?");
        q2.setQuestionType("multiple_choice");
        q2.setOptions(Arrays.asList(
                "Emphasizing certain syllables in a word",
                "Speaking very loudly",
                "Speaking very quietly",
                "Not pronouncing some letters"
        ));
        q2.setCorrectAnswer("Emphasizing certain syllables in a word");
        q2.setTimestamp(30);
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("How can you improve your pronunciation?");
        q3.setQuestionType("multiple_choice");
        q3.setOptions(Arrays.asList(
                "Listen and repeat after native speakers",
                "Never practice speaking",
                "Only write English",
                "Avoid watching English videos"
        ));
        q3.setCorrectAnswer("Listen and repeat after native speakers");
        q3.setTimestamp(45);
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("What is intonation in English?");
        q4.setQuestionType("multiple_choice");
        q4.setOptions(Arrays.asList(
                "The rise and fall of voice pitch when speaking",
                "Speaking in one flat tone",
                "Shouting all the time",
                "Whispering quietly"
        ));
        q4.setCorrectAnswer("The rise and fall of voice pitch when speaking");
        q4.setTimestamp(55);
        questions.add(q4);

        return questions;
    }

    // ========================================
    // SEED DỮ LIỆU CHO CÁC MODULE LEARN
    // ========================================

    /**
     * Seed tất cả dữ liệu cho các module Learn
     */
    public void seedAllLearnModules(SeedCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        callback.onProgress("Starting Learn modules data seeding...");

        // Step 1: Seed Grammar Lessons
        seedGrammarLessons(new SeedCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onProgress("Grammar lessons: " + message);

                // Step 2: Seed Listening Lessons
                seedListeningLessons(new SeedCallback() {
                    @Override
                    public void onSuccess(String message) {
                        callback.onProgress("Listening lessons: " + message);

                        // Step 3: Seed Speaking Lessons
                        seedSpeakingLessons(new SeedCallback() {
                            @Override
                            public void onSuccess(String message) {
                                callback.onProgress("Speaking lessons: " + message);

                                // Step 4: Seed Reading Lessons
                                seedReadingLessons(new SeedCallback() {
                                    @Override
                                    public void onSuccess(String message) {
                                        callback.onProgress("Reading lessons: " + message);

                                        // Step 5: Seed Writing Lessons
                                        seedWritingLessons(new SeedCallback() {
                                            @Override
                                            public void onSuccess(String message) {
                                                callback.onProgress("Writing lessons: " + message);
                                                callback.onSuccess("All Learn modules data created successfully!");
                                            }

                                            @Override
                                            public void onProgress(String message) {
                                                callback.onProgress(message);
                                            }

                                            @Override
                                            public void onFailure(String error) {
                                                callback.onFailure("Error creating writing lessons: " + error);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onProgress(String message) {
                                        callback.onProgress(message);
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        callback.onFailure("Error creating reading lessons: " + error);
                                    }
                                });
                            }

                            @Override
                            public void onProgress(String message) {
                                callback.onProgress(message);
                            }

                            @Override
                            public void onFailure(String error) {
                                callback.onFailure("Error creating speaking lessons: " + error);
                            }
                        });
                    }

                    @Override
                    public void onProgress(String message) {
                        callback.onProgress(message);
                    }

                    @Override
                    public void onFailure(String error) {
                        callback.onFailure("Error creating listening lessons: " + error);
                    }
                });
            }

            @Override
            public void onProgress(String message) {
                callback.onProgress(message);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure("Error creating grammar lessons: " + error);
            }
        });
    }

    // ========================================
    // GRAMMAR LESSONS
    // ========================================

    private void seedGrammarLessons(SeedCallback callback) {
        callback.onProgress("Creating grammar lessons...");

        WriteBatch batch = db.batch();
        int count = 0;

        // Lesson 1: Present Simple Tense
        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("id", "grammar_01");
        lesson1.put("title", "Present Simple Tense");
        lesson1.put("description", "Learn how to use the present simple tense for habits, facts, and general truths.");
        lesson1.put("level", "A1");
        lesson1.put("category", "Tenses");
        lesson1.put("content", "The Present Simple tense is used for:\n\n" +
                "1. Habits and routines:\n" +
                "- I wake up at 7 AM every day.\n" +
                "- She drinks coffee in the morning.\n\n" +
                "2. Facts and general truths:\n" +
                "- The sun rises in the east.\n" +
                "- Water boils at 100°C.\n\n" +
                "3. Permanent situations:\n" +
                "- I live in Ho Chi Minh City.\n" +
                "- He works as a teacher.\n\n" +
                "Formation:\n" +
                "Positive: Subject + base verb (+ s/es for he/she/it)\n" +
                "Negative: Subject + don't/doesn't + base verb\n" +
                "Question: Do/Does + subject + base verb?\n\n" +
                "Examples:\n" +
                "✓ I study English every day.\n" +
                "✓ She doesn't like coffee.\n" +
                "✓ Do you play sports?");
        lesson1.put("exercises", Arrays.asList(
                createGrammarExercise("Complete: I ___ (go) to school every day.", "go"),
                createGrammarExercise("Complete: She ___ (not/like) vegetables.", "doesn't like"),
                createGrammarExercise("Complete: ___ you ___ (speak) English?", "Do, speak")
        ));
        lesson1.put("createdAt", new Date());
        batch.set(db.collection("grammar_lessons").document("grammar_01"), lesson1);
        count++;

        // Lesson 2: Present Continuous
        Map<String, Object> lesson2 = new HashMap<>();
        lesson2.put("id", "grammar_02");
        lesson2.put("title", "Present Continuous Tense");
        lesson2.put("description", "Learn how to talk about actions happening now or around now.");
        lesson2.put("level", "A1");
        lesson2.put("category", "Tenses");
        lesson2.put("content", "The Present Continuous is used for:\n\n" +
                "1. Actions happening now:\n" +
                "- I am studying English right now.\n" +
                "- They are playing football.\n\n" +
                "2. Temporary situations:\n" +
                "- She is living in Hanoi this month.\n" +
                "- I'm working on a new project.\n\n" +
                "3. Future arrangements:\n" +
                "- We are meeting tomorrow at 3 PM.\n\n" +
                "Formation:\n" +
                "Positive: Subject + am/is/are + verb-ing\n" +
                "Negative: Subject + am/is/are + not + verb-ing\n" +
                "Question: Am/Is/Are + subject + verb-ing?\n\n" +
                "Time expressions: now, right now, at the moment, currently");
        lesson2.put("exercises", Arrays.asList(
                createGrammarExercise("Complete: I ___ (read) a book now.", "am reading"),
                createGrammarExercise("Complete: They ___ (not/watch) TV.", "aren't watching"),
                createGrammarExercise("Complete: ___ she ___ (study)?", "Is, studying")
        ));
        lesson2.put("createdAt", new Date());
        batch.set(db.collection("grammar_lessons").document("grammar_02"), lesson2);
        count++;

        // Lesson 3: Past Simple
        Map<String, Object> lesson3 = new HashMap<>();
        lesson3.put("id", "grammar_03");
        lesson3.put("title", "Past Simple Tense");
        lesson3.put("description", "Learn how to talk about completed actions in the past.");
        lesson3.put("level", "A2");
        lesson3.put("category", "Tenses");
        lesson3.put("content", "The Past Simple is used for:\n\n" +
                "1. Completed actions in the past:\n" +
                "- I visited Paris last year.\n" +
                "- She studied English yesterday.\n\n" +
                "2. Past habits:\n" +
                "- I played tennis when I was young.\n\n" +
                "Formation:\n" +
                "Regular verbs: base verb + ed\n" +
                "Irregular verbs: special forms (go→went, see→saw)\n\n" +
                "Positive: Subject + past form\n" +
                "Negative: Subject + didn't + base verb\n" +
                "Question: Did + subject + base verb?\n\n" +
                "Time expressions: yesterday, last week, ago, in 2020");
        lesson3.put("exercises", Arrays.asList(
                createGrammarExercise("Complete: I ___ (go) to the cinema yesterday.", "went"),
                createGrammarExercise("Complete: She ___ (not/eat) breakfast.", "didn't eat"),
                createGrammarExercise("Complete: ___ you ___ (see) that movie?", "Did, see")
        ));
        lesson3.put("createdAt", new Date());
        batch.set(db.collection("grammar_lessons").document("grammar_03"), lesson3);
        count++;

        // Lesson 4: Articles (A, An, The)
        Map<String, Object> lesson4 = new HashMap<>();
        lesson4.put("id", "grammar_04");
        lesson4.put("title", "Articles: A, An, The");
        lesson4.put("description", "Master the use of indefinite and definite articles in English.");
        lesson4.put("level", "A2");
        lesson4.put("category", "Grammar Rules");
        lesson4.put("content", "Articles in English:\n\n" +
                "A/AN (Indefinite Articles):\n" +
                "- Use 'a' before consonant sounds: a book, a university\n" +
                "- Use 'an' before vowel sounds: an apple, an hour\n" +
                "- For singular, countable nouns (first mention)\n\n" +
                "THE (Definite Article):\n" +
                "- Use when both speaker and listener know what we're talking about\n" +
                "- Second mention: I saw a dog. The dog was big.\n" +
                "- Unique things: the sun, the moon, the president\n" +
                "- Superlatives: the best, the biggest\n\n" +
                "No Article:\n" +
                "- Plural nouns in general: I like dogs.\n" +
                "- Uncountable nouns in general: Water is important.\n" +
                "- Names of countries (usually): Vietnam, France");
        lesson4.put("exercises", Arrays.asList(
                createGrammarExercise("Complete: I saw ___ cat in the garden.", "a"),
                createGrammarExercise("Complete: ___ cat was black.", "The"),
                createGrammarExercise("Complete: She is ___ engineer.", "an")
        ));
        lesson4.put("createdAt", new Date());
        batch.set(db.collection("grammar_lessons").document("grammar_04"), lesson4);
        count++;

        // Lesson 5: Modal Verbs
        Map<String, Object> lesson5 = new HashMap<>();
        lesson5.put("id", "grammar_05");
        lesson5.put("title", "Modal Verbs: Can, Could, Should, Must");
        lesson5.put("description", "Learn how to use modal verbs to express ability, possibility, advice, and obligation.");
        lesson5.put("level", "B1");
        lesson5.put("category", "Modal Verbs");
        lesson5.put("content", "Modal Verbs:\n\n" +
                "CAN/CAN'T:\n" +
                "- Ability: I can swim.\n" +
                "- Permission: Can I use your phone?\n" +
                "- Possibility: It can be cold in winter.\n\n" +
                "COULD/COULDN'T:\n" +
                "- Past ability: I could run fast when I was young.\n" +
                "- Polite requests: Could you help me?\n" +
                "- Possibility: It could rain tomorrow.\n\n" +
                "SHOULD/SHOULDN'T:\n" +
                "- Advice: You should study more.\n" +
                "- Recommendation: You shouldn't eat too much sugar.\n\n" +
                "MUST/MUSTN'T:\n" +
                "- Strong obligation: You must wear a seatbelt.\n" +
                "- Prohibition: You mustn't smoke here.\n" +
                "- Strong certainty: She must be tired.\n\n" +
                "Formation: Subject + modal + base verb");
        lesson5.put("exercises", Arrays.asList(
                createGrammarExercise("Complete: I ___ speak three languages.", "can"),
                createGrammarExercise("Complete: You ___ see a doctor.", "should"),
                createGrammarExercise("Complete: We ___ be late for class.", "mustn't")
        ));
        lesson5.put("createdAt", new Date());
        batch.set(db.collection("grammar_lessons").document("grammar_05"), lesson5);
        count++;

        final int finalCount = count;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Grammar lessons created successfully");
                    callback.onSuccess(finalCount + " lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating grammar lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> createGrammarExercise(String question, String answer) {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("question", question);
        exercise.put("answer", answer);
        exercise.put("type", "fill_in_blank");
        return exercise;
    }

    // ========================================
    // LISTENING LESSONS
    // ========================================

    private void seedListeningLessons(SeedCallback callback) {
        callback.onProgress("Creating listening lessons...");

        WriteBatch batch = db.batch();
        int count = 0;

        // Lesson 1: Basic Conversations - Greetings
        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("id", "listening_01");
        lesson1.put("title", "Basic Conversations - Greetings");
        lesson1.put("description", "Practice listening to basic greetings and introductions.");
        lesson1.put("level", "A1");
        lesson1.put("category", "Conversation");
        // Using sample audio from VOA Learning English
        lesson1.put("audioUrl", "https://av.voanews.com/clips/VLE/2023/11/20/20231120_Let_s_Learn_English_Lesson_1.mp3");
        lesson1.put("transcript", "A: Hello! How are you?\n" +
                "B: I'm fine, thank you. And you?\n" +
                "A: I'm great! Nice to meet you.\n" +
                "B: Nice to meet you too!");
        lesson1.put("duration", 30);
        lesson1.put("exercises", Arrays.asList(
                createListeningExercise("How is person B feeling?", "Fine", Arrays.asList("Fine", "Tired", "Sad", "Angry")),
                createListeningExercise("What do they say at the end?", "Nice to meet you", Arrays.asList("Goodbye", "See you later", "Nice to meet you", "Thank you"))
        ));
        lesson1.put("createdAt", new Date());
        batch.set(db.collection("listening_lessons").document("listening_01"), lesson1);
        count++;

        // Lesson 2: Shopping Dialogue
        Map<String, Object> lesson2 = new HashMap<>();
        lesson2.put("id", "listening_02");
        lesson2.put("title", "Shopping Dialogue");
        lesson2.put("description", "Listen to a conversation in a shop.");
        lesson2.put("level", "A2");
        lesson2.put("category", "Shopping");
        // Using sample audio from ESL Lab
        lesson2.put("audioUrl", "https://www.esl-lab.com/wp-content/uploads/2015/02/shop1.mp3");
        lesson2.put("transcript", "Customer: Excuse me, how much is this shirt?\n" +
                "Shopkeeper: It's $25.\n" +
                "Customer: Do you have it in blue?\n" +
                "Shopkeeper: Yes, we do. What size do you need?\n" +
                "Customer: Medium, please.\n" +
                "Shopkeeper: Here you are. Would you like to try it on?");
        lesson2.put("duration", 45);
        lesson2.put("exercises", Arrays.asList(
                createListeningExercise("How much is the shirt?", "$25", Arrays.asList("$15", "$20", "$25", "$30")),
                createListeningExercise("What color does the customer want?", "Blue", Arrays.asList("Red", "Blue", "Green", "Black")),
                createListeningExercise("What size does the customer need?", "Medium", Arrays.asList("Small", "Medium", "Large", "Extra Large"))
        ));
        lesson2.put("createdAt", new Date());
        batch.set(db.collection("listening_lessons").document("listening_02"), lesson2);
        count++;

        // Lesson 3: Weather Forecast
        Map<String, Object> lesson3 = new HashMap<>();
        lesson3.put("id", "listening_03");
        lesson3.put("title", "Weather Forecast");
        lesson3.put("description", "Listen to a weather forecast and answer questions.");
        lesson3.put("level", "A2");
        lesson3.put("category", "News");
        // Using sample weather forecast audio
        lesson3.put("audioUrl", "https://www.esl-lab.com/wp-content/uploads/2015/02/weather1sc.mp3");
        lesson3.put("transcript", "Good morning! Here's today's weather forecast. " +
                "It will be sunny in the morning with temperatures around 25 degrees Celsius. " +
                "In the afternoon, we expect some clouds and a chance of rain. " +
                "The temperature will drop to 20 degrees in the evening. " +
                "Don't forget your umbrella!");
        lesson3.put("duration", 40);
        lesson3.put("exercises", Arrays.asList(
                createListeningExercise("What will the weather be like in the morning?", "Sunny", Arrays.asList("Rainy", "Sunny", "Cloudy", "Windy")),
                createListeningExercise("What is the morning temperature?", "25 degrees", Arrays.asList("20 degrees", "25 degrees", "30 degrees", "15 degrees")),
                createListeningExercise("What should you bring?", "Umbrella", Arrays.asList("Sunglasses", "Jacket", "Umbrella", "Hat"))
        ));
        lesson3.put("createdAt", new Date());
        batch.set(db.collection("listening_lessons").document("listening_03"), lesson3);
        count++;

        final int finalCount = count;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Listening lessons created successfully");
                    callback.onSuccess(finalCount + " lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating listening lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> createListeningExercise(String question, String correctAnswer, List<String> options) {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("question", question);
        exercise.put("correctAnswer", correctAnswer);
        exercise.put("options", options);
        exercise.put("type", "multiple_choice");
        return exercise;
    }

    // ========================================
    // SPEAKING LESSONS
    // ========================================

    private void seedSpeakingLessons(SeedCallback callback) {
        callback.onProgress("Creating speaking lessons...");

        WriteBatch batch = db.batch();
        int count = 0;

        // Lesson 1: Self Introduction
        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("id", "speaking_01");
        lesson1.put("title", "Introducing Yourself");
        lesson1.put("description", "Learn how to introduce yourself confidently in English.");
        lesson1.put("level", "A1");
        lesson1.put("category", "Introduction");
        lesson1.put("estimatedMinutes", 5);
        lesson1.put("content", "Key phrases for self-introduction:\n\n" +
                "1. Greeting:\n" +
                "- Hello! / Hi!\n" +
                "- Good morning/afternoon/evening!\n\n" +
                "2. Name:\n" +
                "- My name is...\n" +
                "- I'm...\n" +
                "- You can call me...\n\n" +
                "3. Where you're from:\n" +
                "- I'm from Vietnam.\n" +
                "- I come from Ho Chi Minh City.\n\n" +
                "4. What you do:\n" +
                "- I'm a student.\n" +
                "- I work as a...\n" +
                "- I study at...\n\n" +
                "5. Hobbies:\n" +
                "- In my free time, I like to...\n" +
                "- My hobbies are...\n\n" +
                "Example:\n" +
                "Hello! My name is Linh. I'm from Vietnam. I'm a student at HCMUTE. " +
                "In my free time, I like reading books and learning English. Nice to meet you!");
        lesson1.put("exercises", Arrays.asList(
                createSpeakingExercise("Introduce yourself", "Record yourself introducing your name, where you're from, and what you do."),
                createSpeakingExercise("Talk about hobbies", "Speak for 30 seconds about your hobbies and interests.")
        ));
        lesson1.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_01"), lesson1);
        count++;

        // Lesson 2: Describing Daily Routine
        Map<String, Object> lesson2 = new HashMap<>();
        lesson2.put("id", "speaking_02");
        lesson2.put("title", "Describing Your Daily Routine");
        lesson2.put("description", "Practice talking about your daily activities.");
        lesson2.put("level", "A2");
        lesson2.put("category", "Daily Life");
        lesson2.put("estimatedMinutes", 7);
        lesson2.put("content", "Useful phrases for daily routine:\n\n" +
                "Time expressions:\n" +
                "- In the morning / afternoon / evening\n" +
                "- At 7 o'clock\n" +
                "- After breakfast\n" +
                "- Before going to bed\n\n" +
                "Activities:\n" +
                "- I wake up at...\n" +
                "- I have breakfast\n" +
                "- I go to work/school\n" +
                "- I finish work at...\n" +
                "- I go to bed at...\n\n" +
                "Frequency adverbs:\n" +
                "- always, usually, often, sometimes, rarely, never\n\n" +
                "Example:\n" +
                "I usually wake up at 6:30 AM. After that, I have breakfast and get ready for work. " +
                "I leave home at 7:30 and arrive at the office at 8:00. I finish work at 5:00 PM. " +
                "In the evening, I often watch TV or read books. I go to bed at 11:00 PM.");
        lesson2.put("exercises", Arrays.asList(
                createSpeakingExercise("Describe your morning routine", "Talk about what you do from waking up until leaving home."),
                createSpeakingExercise("Talk about your evening", "Describe your activities after work or school.")
        ));
        lesson2.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_02"), lesson2);
        count++;

        // Lesson 3: Giving Opinions
        Map<String, Object> lesson3 = new HashMap<>();
        lesson3.put("id", "speaking_03");
        lesson3.put("title", "Expressing Opinions");
        lesson3.put("description", "Learn how to express your opinions clearly and politely.");
        lesson3.put("level", "B1");
        lesson3.put("category", "Communication");
        lesson3.put("estimatedMinutes", 8);
        lesson3.put("content", "Phrases for expressing opinions:\n\n" +
                "Giving your opinion:\n" +
                "- I think (that)...\n" +
                "- In my opinion...\n" +
                "- From my point of view...\n" +
                "- It seems to me that...\n" +
                "- I believe (that)...\n\n" +
                "Agreeing:\n" +
                "- I agree.\n" +
                "- That's true.\n" +
                "- Exactly!\n" +
                "- I think so too.\n\n" +
                "Disagreeing politely:\n" +
                "- I'm not sure I agree.\n" +
                "- I see what you mean, but...\n" +
                "- That's one way to look at it, however...\n\n" +
                "Example:\n" +
                "Topic: Should students wear uniforms?\n" +
                "I think students should wear uniforms. In my opinion, it helps create equality among students. " +
                "However, I understand that some people believe it limits self-expression.");
        lesson3.put("exercises", Arrays.asList(
                createSpeakingExercise("Give your opinion on social media", "Express your views on the advantages and disadvantages of social media."),
                createSpeakingExercise("Agree or disagree", "Respond to this statement: 'Learning English is essential in today's world.'")
        ));
        lesson3.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_03"), lesson3);
        count++;

        // Lesson 4: Describing People
        Map<String, Object> lesson4 = new HashMap<>();
        lesson4.put("id", "speaking_04");
        lesson4.put("title", "Describing People");
        lesson4.put("description", "Learn how to describe someone's appearance and personality.");
        lesson4.put("level", "A2");
        lesson4.put("category", "Description");
        lesson4.put("estimatedMinutes", 6);
        lesson4.put("content", "Vocabulary for describing people:\n\n" +
                "Physical appearance:\n" +
                "- Height: tall, short, medium height\n" +
                "- Build: slim, thin, athletic, overweight\n" +
                "- Hair: long, short, curly, straight, blonde, brown, black\n" +
                "- Eyes: blue, brown, green, big, small\n" +
                "- Age: young, middle-aged, elderly, in his/her 20s\n\n" +
                "Personality:\n" +
                "- Positive: friendly, kind, funny, intelligent, hardworking, patient\n" +
                "- Negative: lazy, rude, impatient, selfish\n\n" +
                "Example:\n" +
                "My best friend is tall and slim. She has long black hair and brown eyes. " +
                "She's very friendly and funny. She always makes me laugh. She's also very intelligent " +
                "and hardworking. She's studying to become a doctor.");
        lesson4.put("exercises", Arrays.asList(
                createSpeakingExercise("Describe your best friend", "Talk about your best friend's appearance and personality."),
                createSpeakingExercise("Describe a family member", "Describe someone in your family in detail.")
        ));
        lesson4.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_04"), lesson4);
        count++;

        // Lesson 5: Talking About Hobbies
        Map<String, Object> lesson5 = new HashMap<>();
        lesson5.put("id", "speaking_05");
        lesson5.put("title", "Talking About Hobbies and Interests");
        lesson5.put("description", "Practice discussing your hobbies and free time activities.");
        lesson5.put("level", "A2");
        lesson5.put("category", "Hobbies");
        lesson5.put("estimatedMinutes", 6);
        lesson5.put("content", "Common hobbies and activities:\n\n" +
                "Sports:\n" +
                "- playing football/basketball/tennis\n" +
                "- going swimming/jogging/cycling\n" +
                "- doing yoga/martial arts\n\n" +
                "Creative activities:\n" +
                "- painting, drawing, photography\n" +
                "- playing musical instruments\n" +
                "- writing, blogging\n\n" +
                "Indoor activities:\n" +
                "- reading books/magazines\n" +
                "- watching movies/TV shows\n" +
                "- playing video games\n" +
                "- cooking, baking\n\n" +
                "Useful phrases:\n" +
                "- I enjoy...\n" +
                "- I'm interested in...\n" +
                "- I'm passionate about...\n" +
                "- In my spare time, I...\n" +
                "- I've been doing this for... years\n\n" +
                "Example:\n" +
                "I'm really passionate about photography. I've been taking photos for about 3 years now. " +
                "I enjoy capturing beautiful moments and landscapes. On weekends, I usually go to different " +
                "places to take pictures. I also like editing photos on my computer.");
        lesson5.put("exercises", Arrays.asList(
                createSpeakingExercise("Talk about your favorite hobby", "Explain why you enjoy this hobby and how often you do it."),
                createSpeakingExercise("Describe a new hobby you want to try", "Talk about a hobby you'd like to start and why.")
        ));
        lesson5.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_05"), lesson5);
        count++;

        // Lesson 6: Describing Places
        Map<String, Object> lesson6 = new HashMap<>();
        lesson6.put("id", "speaking_06");
        lesson6.put("title", "Describing Places");
        lesson6.put("description", "Learn to describe cities, towns, and tourist attractions.");
        lesson6.put("level", "B1");
        lesson6.put("category", "Description");
        lesson6.put("estimatedMinutes", 7);
        lesson6.put("content", "Vocabulary for describing places:\n\n" +
                "Location:\n" +
                "- in the north/south/east/west\n" +
                "- near the coast/mountains\n" +
                "- in the city center\n" +
                "- on the outskirts\n\n" +
                "Characteristics:\n" +
                "- crowded, busy, peaceful, quiet\n" +
                "- modern, historic, traditional\n" +
                "- beautiful, scenic, picturesque\n" +
                "- polluted, clean, green\n\n" +
                "Features:\n" +
                "- has many restaurants/shops/museums\n" +
                "- is famous for...\n" +
                "- is known for...\n" +
                "- offers great...\n\n" +
                "Example:\n" +
                "Ho Chi Minh City is located in the south of Vietnam. It's a very busy and crowded city " +
                "with over 9 million people. The city is a mix of modern and traditional architecture. " +
                "It's famous for its delicious street food and vibrant nightlife. There are many museums, " +
                "parks, and shopping centers. The city offers great opportunities for work and education.");
        lesson6.put("exercises", Arrays.asList(
                createSpeakingExercise("Describe your hometown", "Talk about the location, size, and main features of your hometown."),
                createSpeakingExercise("Describe a place you visited", "Describe a city or tourist attraction you've been to.")
        ));
        lesson6.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_06"), lesson6);
        count++;

        // Lesson 7: Telling Stories
        Map<String, Object> lesson7 = new HashMap<>();
        lesson7.put("id", "speaking_07");
        lesson7.put("title", "Telling Stories and Past Experiences");
        lesson7.put("description", "Practice narrating events and experiences from the past.");
        lesson7.put("level", "B1");
        lesson7.put("category", "Storytelling");
        lesson7.put("estimatedMinutes", 8);
        lesson7.put("content", "Structure for storytelling:\n\n" +
                "1. Setting the scene:\n" +
                "- Last summer/year/month...\n" +
                "- A few years ago...\n" +
                "- When I was...\n" +
                "- One day...\n\n" +
                "2. Describing what happened:\n" +
                "- First, / Then, / After that, / Finally,\n" +
                "- Suddenly, / All of a sudden,\n" +
                "- While I was..., something happened\n\n" +
                "3. Expressing feelings:\n" +
                "- I was surprised/shocked/excited/nervous\n" +
                "- I felt happy/sad/scared\n" +
                "- It was amazing/terrible/unforgettable\n\n" +
                "4. Conclusion:\n" +
                "- In the end, / Eventually,\n" +
                "- It was a great/terrible experience\n" +
                "- I learned that...\n\n" +
                "Example:\n" +
                "Last summer, I went on a trip to Da Lat with my friends. We left early in the morning " +
                "and arrived at noon. The weather was cool and pleasant. First, we visited some beautiful " +
                "waterfalls. Then, we went to a strawberry farm where we could pick our own strawberries. " +
                "It was so much fun! In the evening, we walked around the night market and tried local food. " +
                "The next day, we visited some French colonial buildings and took lots of photos. " +
                "It was an unforgettable trip. I can't wait to go back!");
        lesson7.put("exercises", Arrays.asList(
                createSpeakingExercise("Tell a memorable story", "Share a memorable experience from your past."),
                createSpeakingExercise("Describe a funny incident", "Tell about something funny that happened to you.")
        ));
        lesson7.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_07"), lesson7);
        count++;

        // Lesson 8: Discussing Technology
        Map<String, Object> lesson8 = new HashMap<>();
        lesson8.put("id", "speaking_08");
        lesson8.put("title", "Discussing Technology");
        lesson8.put("description", "Talk about technology, gadgets, and their impact on daily life.");
        lesson8.put("level", "B2");
        lesson8.put("category", "Technology");
        lesson8.put("estimatedMinutes", 9);
        lesson8.put("content", "Technology vocabulary and phrases:\n\n" +
                "Devices and gadgets:\n" +
                "- smartphone, tablet, laptop, smartwatch\n" +
                "- wireless headphones, smart speaker\n" +
                "- virtual reality headset\n\n" +
                "Actions:\n" +
                "- browse the internet\n" +
                "- download/upload files\n" +
                "- stream videos/music\n" +
                "- video call someone\n" +
                "- charge the battery\n" +
                "- update software\n\n" +
                "Discussing advantages:\n" +
                "- Technology has made it easier to...\n" +
                "- One major benefit is...\n" +
                "- It allows us to...\n" +
                "- Thanks to technology, we can...\n\n" +
                "Discussing disadvantages:\n" +
                "- However, there are some drawbacks...\n" +
                "- One concern is...\n" +
                "- It can lead to...\n" +
                "- We need to be careful about...\n\n" +
                "Example:\n" +
                "Smartphones have completely changed how we communicate. They allow us to stay connected " +
                "with friends and family anywhere in the world through video calls and messaging apps. " +
                "We can also access information instantly, take high-quality photos, and manage our daily " +
                "tasks with various apps. However, there are some concerns. Many people spend too much time " +
                "on their phones, which can affect their sleep and social interactions. Privacy is also " +
                "a major issue. Despite these drawbacks, I believe smartphones are incredibly useful tools " +
                "when used responsibly.");
        lesson8.put("exercises", Arrays.asList(
                createSpeakingExercise("Describe your favorite gadget", "Talk about a device you use daily and explain why it's important to you."),
                createSpeakingExercise("Technology pros and cons", "Discuss both positive and negative impacts of social media.")
        ));
        lesson8.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_08"), lesson8);
        count++;

        // Lesson 9: Making Plans and Suggestions
        Map<String, Object> lesson9 = new HashMap<>();
        lesson9.put("id", "speaking_09");
        lesson9.put("title", "Making Plans and Suggestions");
        lesson9.put("description", "Learn how to make plans, give suggestions, and respond to invitations.");
        lesson9.put("level", "A2");
        lesson9.put("category", "Communication");
        lesson9.put("estimatedMinutes", 6);
        lesson9.put("content", "Useful phrases for making plans:\n\n" +
                "Making suggestions:\n" +
                "- How about...?\n" +
                "- Why don't we...?\n" +
                "- Let's...\n" +
                "- We could...\n" +
                "- Would you like to...?\n\n" +
                "Accepting:\n" +
                "- That sounds great!\n" +
                "- Good idea!\n" +
                "- Sure, I'd love to.\n" +
                "- Yes, let's do that.\n\n" +
                "Declining politely:\n" +
                "- I'm sorry, but I can't.\n" +
                "- I'd love to, but...\n" +
                "- Maybe another time?\n" +
                "- Unfortunately, I'm busy.\n\n" +
                "Arranging details:\n" +
                "- What time shall we meet?\n" +
                "- Where should we go?\n" +
                "- How about meeting at...?\n\n" +
                "Example conversation:\n" +
                "A: Hey, are you free this weekend?\n" +
                "B: Yes, I am. Why?\n" +
                "A: How about going to the cinema? There's a new movie I want to see.\n" +
                "B: That sounds great! What time?\n" +
                "A: How about Saturday evening at 7?\n" +
                "B: Perfect! Let's meet at the cinema at 6:45.\n" +
                "A: Great! See you then!");
        lesson9.put("exercises", Arrays.asList(
                createSpeakingExercise("Make weekend plans", "Suggest activities for the weekend and arrange the details."),
                createSpeakingExercise("Decline an invitation", "Politely refuse an invitation and suggest an alternative.")
        ));
        lesson9.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_09"), lesson9);
        count++;

        // Lesson 10: Job Interviews
        Map<String, Object> lesson10 = new HashMap<>();
        lesson10.put("id", "speaking_10");
        lesson10.put("title", "Job Interview Skills");
        lesson10.put("description", "Prepare for job interviews with common questions and professional responses.");
        lesson10.put("level", "B2");
        lesson10.put("category", "Professional");
        lesson10.put("estimatedMinutes", 10);
        lesson10.put("content", "Common interview questions and how to answer:\n\n" +
                "1. Tell me about yourself:\n" +
                "- Brief background\n" +
                "- Current situation\n" +
                "- Why you're interested in this position\n" +
                "Example: 'I graduated from HCMUTE with a degree in Computer Science. Currently, I'm working " +
                "as a junior developer, but I'm looking for new challenges. I'm particularly interested in " +
                "this position because...'\n\n" +
                "2. What are your strengths?\n" +
                "- Choose 2-3 relevant strengths\n" +
                "- Provide specific examples\n" +
                "Example: 'I'm very detail-oriented. In my previous job, I caught several critical bugs " +
                "before release, which saved the company time and money.'\n\n" +
                "3. What are your weaknesses?\n" +
                "- Be honest but strategic\n" +
                "- Show how you're working to improve\n" +
                "Example: 'I sometimes focus too much on details, which can slow me down. However, I'm " +
                "learning to prioritize and manage my time better.'\n\n" +
                "4. Why do you want to work here?\n" +
                "- Research the company\n" +
                "- Show genuine interest\n" +
                "- Align with company values\n\n" +
                "5. Where do you see yourself in 5 years?\n" +
                "- Show ambition\n" +
                "- Be realistic\n" +
                "- Align with company growth\n\n" +
                "Professional language:\n" +
                "- I believe...\n" +
                "- In my experience...\n" +
                "- I'm confident that...\n" +
                "- I would describe myself as...");
        lesson10.put("exercises", Arrays.asList(
                createSpeakingExercise("Introduce yourself professionally", "Give a 1-minute professional introduction."),
                createSpeakingExercise("Answer: Why should we hire you?", "Explain why you're the best candidate for a position.")
        ));
        lesson10.put("createdAt", new Date());
        batch.set(db.collection("speaking_lessons").document("speaking_10"), lesson10);
        count++;

        final int finalCount = count;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Speaking lessons created successfully");
                    callback.onSuccess(finalCount + " lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating speaking lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> createSpeakingExercise(String title, String instruction) {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("title", title);
        exercise.put("instruction", instruction);
        exercise.put("type", "speaking_practice");
        return exercise;
    }

    // ========================================
    // READING LESSONS
    // ========================================

    private void seedReadingLessons(SeedCallback callback) {
        callback.onProgress("Creating reading lessons...");

        WriteBatch batch = db.batch();
        int count = 0;

        // Lesson 1: Short Story - A Day at the Beach
        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("id", "reading_01");
        lesson1.put("title", "A Day at the Beach");
        lesson1.put("description", "Read a short story about a family's day at the beach.");
        lesson1.put("level", "A1");
        lesson1.put("category", "Short Story");
        lesson1.put("passage", "Last Sunday, my family went to the beach. The weather was beautiful. " +
                "The sun was shining and the sky was blue. We arrived at 9 o'clock in the morning.\n\n" +
                "My brother and I played volleyball on the sand. My parents sat under an umbrella and read books. " +
                "At noon, we had a picnic. We ate sandwiches and fruit. The food was delicious!\n\n" +
                "In the afternoon, we went swimming in the sea. The water was cool and refreshing. " +
                "We stayed at the beach until 5 o'clock. We were tired but very happy. " +
                "It was a wonderful day!");
        lesson1.put("wordCount", 95);
        lesson1.put("exercises", Arrays.asList(
                createReadingExercise("When did the family go to the beach?", "Last Sunday", Arrays.asList("Last Saturday", "Last Sunday", "Yesterday", "Last Friday")),
                createReadingExercise("What was the weather like?", "Beautiful and sunny", Arrays.asList("Rainy", "Cloudy", "Beautiful and sunny", "Windy")),
                createReadingExercise("What did they eat for lunch?", "Sandwiches and fruit", Arrays.asList("Pizza", "Sandwiches and fruit", "Rice", "Noodles")),
                createReadingExercise("What time did they leave the beach?", "5 o'clock", Arrays.asList("3 o'clock", "4 o'clock", "5 o'clock", "6 o'clock"))
        ));
        lesson1.put("createdAt", new Date());
        batch.set(db.collection("reading_lessons").document("reading_01"), lesson1);
        count++;

        // Lesson 2: Article - Benefits of Learning English
        Map<String, Object> lesson2 = new HashMap<>();
        lesson2.put("id", "reading_02");
        lesson2.put("title", "Why Learning English is Important");
        lesson2.put("description", "Read about the benefits of learning English in today's world.");
        lesson2.put("level", "A2");
        lesson2.put("category", "Article");
        lesson2.put("passage", "English is one of the most widely spoken languages in the world. " +
                "More than 1.5 billion people speak English, either as their first or second language. " +
                "Learning English can open many doors for you.\n\n" +
                "First, English is the language of international business. Many companies require employees " +
                "who can speak English. If you speak English well, you will have more job opportunities.\n\n" +
                "Second, English is the language of the internet. Most websites and online content are in English. " +
                "When you understand English, you can access more information and learn new things easily.\n\n" +
                "Third, English helps you travel. In most countries, you can find people who speak English. " +
                "This makes traveling easier and more enjoyable.\n\n" +
                "Finally, learning English improves your brain. Studies show that learning a new language " +
                "makes you smarter and improves your memory. It also helps you understand your own language better.\n\n" +
                "In conclusion, learning English is a valuable skill that can benefit you in many ways. " +
                "Start learning today!");
        lesson2.put("wordCount", 180);
        lesson2.put("exercises", Arrays.asList(
                createReadingExercise("How many people speak English?", "More than 1.5 billion", Arrays.asList("500 million", "1 billion", "More than 1.5 billion", "2 billion")),
                createReadingExercise("Why is English important for business?", "Many companies require English speakers", Arrays.asList("It's easy to learn", "Many companies require English speakers", "It's fun", "It's old")),
                createReadingExercise("What percentage of websites are in English?", "Most websites", Arrays.asList("Few websites", "Some websites", "Most websites", "No websites")),
                createReadingExercise("How does learning English affect your brain?", "Makes you smarter", Arrays.asList("Makes you tired", "Makes you smarter", "Makes you confused", "No effect"))
        ));
        lesson2.put("createdAt", new Date());
        batch.set(db.collection("reading_lessons").document("reading_02"), lesson2);
        count++;

        // Lesson 3: News Article - Technology in Education
        Map<String, Object> lesson3 = new HashMap<>();
        lesson3.put("id", "reading_03");
        lesson3.put("title", "Technology Transforms Education");
        lesson3.put("description", "Read about how technology is changing the way we learn.");
        lesson3.put("level", "B1");
        lesson3.put("category", "News");
        lesson3.put("passage", "Technology has revolutionized education in recent years. " +
                "Students today have access to learning resources that previous generations could only dream of.\n\n" +
                "Online learning platforms have made education more accessible. Students can now take courses " +
                "from top universities around the world without leaving their homes. This is especially beneficial " +
                "for people who live in remote areas or have busy schedules.\n\n" +
                "Mobile apps have also changed how we learn languages. Apps like Duolingo and Memrise use " +
                "gamification to make learning fun and engaging. Students can practice anytime, anywhere, " +
                "turning their commute or lunch break into productive learning time.\n\n" +
                "Virtual reality (VR) is another exciting development. Students can now take virtual field trips " +
                "to historical sites, explore the human body in 3D, or practice speaking English in simulated " +
                "real-world scenarios.\n\n" +
                "However, technology is not without challenges. Not all students have equal access to devices " +
                "and internet connections. Teachers also need training to use new technologies effectively. " +
                "Additionally, excessive screen time can have negative effects on health.\n\n" +
                "Despite these challenges, the benefits of educational technology are clear. As technology " +
                "continues to evolve, it will play an increasingly important role in shaping the future of education.");
        lesson3.put("wordCount", 210);
        lesson3.put("exercises", Arrays.asList(
                createReadingExercise("What has technology done to education?", "Revolutionized it", Arrays.asList("Destroyed it", "Revolutionized it", "Ignored it", "Complicated it")),
                createReadingExercise("What is one benefit of online learning?", "More accessible", Arrays.asList("More expensive", "More accessible", "More difficult", "Less effective")),
                createReadingExercise("What do language learning apps use?", "Gamification", Arrays.asList("Books", "Gamification", "Tests", "Lectures")),
                createReadingExercise("What is mentioned as a challenge?", "Unequal access to technology", Arrays.asList("Too easy", "Unequal access to technology", "Too expensive", "Too boring"))
        ));
        lesson3.put("createdAt", new Date());
        batch.set(db.collection("reading_lessons").document("reading_03"), lesson3);
        count++;

        final int finalCount = count;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reading lessons created successfully");
                    callback.onSuccess(finalCount + " lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating reading lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> createReadingExercise(String question, String correctAnswer, List<String> options) {
        Map<String, Object> exercise = new HashMap<>();
        exercise.put("question", question);
        exercise.put("correctAnswer", correctAnswer);
        exercise.put("options", options);
        exercise.put("type", "multiple_choice");
        return exercise;
    }

    // ========================================
    // WRITING LESSONS
    // ========================================

    private void seedWritingLessons(SeedCallback callback) {
        callback.onProgress("Creating writing lessons...");

        WriteBatch batch = db.batch();
        int count = 0;

        // Lesson 1: Writing Paragraphs
        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("id", "writing_01");
        lesson1.put("title", "How to Write a Good Paragraph");
        lesson1.put("description", "Learn the structure and elements of a well-written paragraph.");
        lesson1.put("level", "A2");
        lesson1.put("category", "Basic Writing");
        lesson1.put("content", "A good paragraph has three main parts:\n\n" +
                "1. Topic Sentence:\n" +
                "- States the main idea of the paragraph\n" +
                "- Usually the first sentence\n" +
                "- Example: 'Learning English has many benefits.'\n\n" +
                "2. Supporting Sentences:\n" +
                "- Provide details, examples, or explanations\n" +
                "- Support the topic sentence\n" +
                "- Usually 3-5 sentences\n" +
                "- Example: 'First, it helps you communicate with people from different countries. " +
                "Second, it opens up job opportunities. Third, it allows you to access more information online.'\n\n" +
                "3. Concluding Sentence:\n" +
                "- Summarizes the main idea\n" +
                "- Restates the topic sentence in different words\n" +
                "- Example: 'For these reasons, English is a valuable skill to have.'\n\n" +
                "Tips:\n" +
                "- Use linking words: First, Second, However, Therefore\n" +
                "- Keep sentences clear and simple\n" +
                "- Stay focused on one main idea");
        lesson1.put("prompts", Arrays.asList(
                createWritingPrompt("My Favorite Hobby", "Write a paragraph about your favorite hobby. Include why you like it and how often you do it."),
                createWritingPrompt("My Daily Routine", "Describe your typical day from morning to evening.")
        ));
        lesson1.put("createdAt", new Date());
        batch.set(db.collection("writing_lessons").document("writing_01"), lesson1);
        count++;

        // Lesson 2: Writing Emails
        Map<String, Object> lesson2 = new HashMap<>();
        lesson2.put("id", "writing_02");
        lesson2.put("title", "Writing Formal and Informal Emails");
        lesson2.put("description", "Learn how to write professional and casual emails in English.");
        lesson2.put("level", "B1");
        lesson2.put("category", "Email Writing");
        lesson2.put("content", "Email Structure:\n\n" +
                "1. Subject Line:\n" +
                "- Clear and specific\n" +
                "- Example: 'Meeting Request for Project Discussion'\n\n" +
                "2. Greeting:\n" +
                "Formal: Dear Mr./Ms. [Last Name],\n" +
                "Informal: Hi [First Name],\n\n" +
                "3. Opening:\n" +
                "Formal: I am writing to...\n" +
                "Informal: I hope you're doing well.\n\n" +
                "4. Body:\n" +
                "- State your purpose clearly\n" +
                "- Provide necessary details\n" +
                "- Use paragraphs for different points\n\n" +
                "5. Closing:\n" +
                "Formal: I look forward to hearing from you.\n" +
                "Informal: Let me know what you think!\n\n" +
                "6. Sign-off:\n" +
                "Formal: Sincerely, / Best regards,\n" +
                "Informal: Best, / Cheers,\n\n" +
                "Example Formal Email:\n" +
                "Subject: Application for Marketing Position\n\n" +
                "Dear Ms. Johnson,\n\n" +
                "I am writing to apply for the Marketing Manager position advertised on your website. " +
                "I have five years of experience in digital marketing and believe I would be a great fit for your team.\n\n" +
                "I have attached my resume for your review. I would appreciate the opportunity to discuss " +
                "how my skills and experience align with your needs.\n\n" +
                "Thank you for your consideration. I look forward to hearing from you.\n\n" +
                "Sincerely,\n" +
                "[Your Name]");
        lesson2.put("prompts", Arrays.asList(
                createWritingPrompt("Job Application Email", "Write a formal email applying for a job as an English teacher."),
                createWritingPrompt("Email to a Friend", "Write an informal email to a friend about your recent vacation.")
        ));
        lesson2.put("createdAt", new Date());
        batch.set(db.collection("writing_lessons").document("writing_02"), lesson2);
        count++;

        // Lesson 3: Essay Writing
        Map<String, Object> lesson3 = new HashMap<>();
        lesson3.put("id", "writing_03");
        lesson3.put("title", "Writing Opinion Essays");
        lesson3.put("description", "Learn how to write a well-structured opinion essay.");
        lesson3.put("level", "B2");
        lesson3.put("category", "Essay Writing");
        lesson3.put("content", "Opinion Essay Structure:\n\n" +
                "1. Introduction (1 paragraph):\n" +
                "- Hook: Interesting opening sentence\n" +
                "- Background: Brief context\n" +
                "- Thesis statement: Your main opinion\n" +
                "- Example: 'Social media has become an integral part of modern life. While some argue it " +
                "brings people together, I believe it has more negative than positive effects on society.'\n\n" +
                "2. Body Paragraphs (2-3 paragraphs):\n" +
                "Each paragraph should:\n" +
                "- Start with a topic sentence\n" +
                "- Provide supporting arguments\n" +
                "- Include examples or evidence\n" +
                "- Use linking words\n\n" +
                "3. Counterargument (optional but recommended):\n" +
                "- Acknowledge opposing views\n" +
                "- Refute them with your arguments\n" +
                "- Example: 'Some people claim that... However, I disagree because...'\n\n" +
                "4. Conclusion (1 paragraph):\n" +
                "- Restate your opinion\n" +
                "- Summarize main points\n" +
                "- Final thought or recommendation\n\n" +
                "Useful Phrases:\n" +
                "- In my opinion...\n" +
                "- I strongly believe that...\n" +
                "- Furthermore, / Moreover, / In addition,\n" +
                "- On the other hand, / However,\n" +
                "- In conclusion, / To sum up,");
        lesson3.put("prompts", Arrays.asList(
                createWritingPrompt("Technology in Education", "Do you think technology has improved education? Write an essay expressing your opinion."),
                createWritingPrompt("Working from Home", "Is working from home better than working in an office? Give your opinion with reasons and examples.")
        ));
        lesson3.put("createdAt", new Date());
        batch.set(db.collection("writing_lessons").document("writing_03"), lesson3);
        count++;

        final int finalCount = count;
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Writing lessons created successfully");
                    callback.onSuccess(finalCount + " lessons created");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating writing lessons", e);
                    callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> createWritingPrompt(String title, String instruction) {
        Map<String, Object> prompt = new HashMap<>();
        prompt.put("title", title);
        prompt.put("instruction", instruction);
        prompt.put("minWords", 100);
        prompt.put("type", "essay");
        return prompt;
    }
}
