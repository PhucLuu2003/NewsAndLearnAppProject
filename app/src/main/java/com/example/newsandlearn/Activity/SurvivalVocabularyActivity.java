package com.example.newsandlearn.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.newsandlearn.Adapter.SurvivalWordAdapter;
import com.example.newsandlearn.Adapter.VocabularyCategoryAdapter;
import com.example.newsandlearn.Model.SurvivalWord;
import com.example.newsandlearn.Model.VocabularyCategory;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 📚 VocabularyActivity (Merged from Survival Vocabulary)
 * Main vocabulary learning system with SRS
 * Features:
 * - 10 vocabulary categories (Greetings, Numbers, Colors, etc.)
 * - Flashcard-style learning with images & audio  
 * - SRS (Spaced Repetition System) for long-term retention
 * - Progress tracking per category
 * - Daily review reminders
 * - Quiz feature
 */
public class SurvivalVocabularyActivity extends AppCompatActivity
        implements VocabularyCategoryAdapter.OnCategoryClickListener, SurvivalWordAdapter.OnWordActionListener {

    // UI Components
    private Toolbar toolbar;
    private LottieAnimationView headerAnimation;
    private TextView tvLearnedCount, tvStreak, tvDueReview, tvNewWords, tvReviewCount, tvRecentTitle;
    private MaterialCardView cardStartLearning, cardReview;
    private RecyclerView recyclerCategories, recyclerRecent;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;
    private MaterialButton btnSeedData;
    private ExtendedFloatingActionButton fabQuiz;

    // Adapters & Data
    private VocabularyCategoryAdapter categoryAdapter;
    private SurvivalWordAdapter recentAdapter;
    private List<VocabularyCategory> categories = new ArrayList<>();
    private List<SurvivalWord> allWords = new ArrayList<>();
    private List<SurvivalWord> recentWords = new ArrayList<>();
    private List<SurvivalWord> dueForReview = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survival_vocabulary);

        initFirebase();
        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupClickListeners();
        loadCategories();
        loadUserProgress();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        headerAnimation = findViewById(R.id.header_animation);
        tvLearnedCount = findViewById(R.id.tv_learned_count);
        tvStreak = findViewById(R.id.tv_streak);
        tvDueReview = findViewById(R.id.tv_due_review);
        tvNewWords = findViewById(R.id.tv_new_words);
        tvReviewCount = findViewById(R.id.tv_review_count);
        tvRecentTitle = findViewById(R.id.tv_recent_title);
        cardStartLearning = findViewById(R.id.card_start_learning);
        cardReview = findViewById(R.id.card_review);
        recyclerCategories = findViewById(R.id.recycler_categories);
        recyclerRecent = findViewById(R.id.recycler_recent);
        progressLoading = findViewById(R.id.progress_loading);
        emptyState = findViewById(R.id.empty_state);
        btnSeedData = findViewById(R.id.btn_seed_data);
        fabQuiz = findViewById(R.id.fab_quiz);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerViews() {
        // Categories grid (2 columns)
        categoryAdapter = new VocabularyCategoryAdapter(this, this);
        GridLayoutManager gridManager = new GridLayoutManager(this, 2);
        recyclerCategories.setLayoutManager(gridManager);
        recyclerCategories.setAdapter(categoryAdapter);
        recyclerCategories.setNestedScrollingEnabled(false);

        // Recent words horizontal scroll
        recentAdapter = new SurvivalWordAdapter(this, this);
        LinearLayoutManager horizontalManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecent.setLayoutManager(horizontalManager);
        recyclerRecent.setAdapter(recentAdapter);
    }

    private void setupClickListeners() {
        btnSeedData.setOnClickListener(v -> seedVocabularyData());

        cardStartLearning.setOnClickListener(v -> {
            animateCard(v);
            startLearningSession();
        });

        cardReview.setOnClickListener(v -> {
            animateCard(v);
            startReviewSession();
        });

        fabQuiz.setOnClickListener(v -> {
            animateFab(fabQuiz);
            Toast.makeText(this, "🎯 Quiz feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void animateCard(View card) {
        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(
                ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1.02f, 1f),
                ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1.02f, 1f));
        scaleAnim.setDuration(250);
        scaleAnim.setInterpolator(new OvershootInterpolator(1.5f));
        scaleAnim.start();
    }

    private void animateFab(View fab) {
        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0.9f, 1.15f, 1f),
                ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0.9f, 1.15f, 1f));
        scaleAnim.setDuration(300);
        scaleAnim.setInterpolator(new OvershootInterpolator(2f));
        scaleAnim.start();
    }

    // ========== Data Loading ==========

    private void loadCategories() {
        showLoading(true);

        db.collection("vocabulary_categories")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    categories.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        VocabularyCategory category = doc.toObject(VocabularyCategory.class);
                        if (category != null) {
                            category.setId(doc.getId());
                            categories.add(category);
                        }
                    }

                    if (categories.isEmpty()) {
                        showLoading(false);
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        categoryAdapter.setCategories(categories);
                        loadWordsForCategories();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWordsForCategories() {
        db.collection("survival_words")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    allWords.clear();

                    Map<String, Integer> categoryCounts = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot) {
                        SurvivalWord word = doc.toObject(SurvivalWord.class);
                        if (word != null) {
                            word.setId(doc.getId());
                            allWords.add(word);

                            // Count words per category
                            String cat = word.getCategory();
                            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
                        }
                    }

                    // Update category word counts
                    for (VocabularyCategory category : categories) {
                        Integer count = categoryCounts.get(category.getName());
                        if (count != null) {
                            category.setWordCount(count);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();

                    // Get recent words (last 10)
                    updateRecentWords();
                    updateStats();
                });
    }

    private void loadUserProgress() {
        if (userId == null)
            return;

        db.collection("users").document(userId)
                .collection("vocab_progress")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Map<String, Object>> progressMap = new HashMap<>();
                    int totalLearned = 0;

                    for (DocumentSnapshot doc : querySnapshot) {
                        progressMap.put(doc.getId(), doc.getData());
                        Boolean learned = doc.getBoolean("learned");
                        if (learned != null && learned)
                            totalLearned++;
                    }

                    // Update words with progress
                    long now = System.currentTimeMillis();
                    dueForReview.clear();

                    for (SurvivalWord word : allWords) {
                        Map<String, Object> progress = progressMap.get(word.getId());
                        if (progress != null) {
                            Number srsLevel = (Number) progress.get("srsLevel");
                            Number nextReview = (Number) progress.get("nextReviewTime");
                            Number reviewCount = (Number) progress.get("reviewCount");
                            Boolean learned = (Boolean) progress.get("learned");

                            if (srsLevel != null)
                                word.setSrsLevel(srsLevel.intValue());
                            if (nextReview != null)
                                word.setNextReviewTime(nextReview.longValue());
                            if (reviewCount != null)
                                word.setReviewCount(reviewCount.intValue());
                            if (learned != null)
                                word.setLearned(learned);

                            // Check if due for review
                            if (nextReview != null && nextReview.longValue() <= now) {
                                dueForReview.add(word);
                            }
                        }
                    }

                    // Update category progress
                    Map<String, Integer> learnedByCategory = new HashMap<>();
                    for (SurvivalWord word : allWords) {
                        if (word.isLearned()) {
                            String cat = word.getCategory();
                            learnedByCategory.put(cat, learnedByCategory.getOrDefault(cat, 0) + 1);
                        }
                    }

                    for (VocabularyCategory category : categories) {
                        Integer learned = learnedByCategory.get(category.getName());
                        if (learned != null) {
                            category.setLearnedCount(learned);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();

                    updateStats();
                    tvReviewCount.setText(dueForReview.size() + " words due");
                });
    }

    private void updateRecentWords() {
        recentWords.clear();

        // Sort by lastLearnedAt and take top 10
        List<SurvivalWord> sorted = new ArrayList<>(allWords);
        sorted.sort((a, b) -> Long.compare(b.getLastLearnedAt(), a.getLastLearnedAt()));

        for (int i = 0; i < Math.min(10, sorted.size()); i++) {
            if (sorted.get(i).isLearned()) {
                recentWords.add(sorted.get(i));
            }
        }

        if (recentWords.isEmpty()) {
            tvRecentTitle.setVisibility(View.GONE);
            recyclerRecent.setVisibility(View.GONE);
        } else {
            tvRecentTitle.setVisibility(View.VISIBLE);
            recyclerRecent.setVisibility(View.VISIBLE);
            recentAdapter.setWords(recentWords);
        }
    }

    private void updateStats() {
        int totalWords = allWords.size();
        int learnedWords = 0;
        for (SurvivalWord word : allWords) {
            if (word.isLearned())
                learnedWords++;
        }

        tvLearnedCount.setText(learnedWords + "/" + totalWords);
        tvDueReview.setText("📖 " + dueForReview.size());

        int newWordsToLearn = Math.min(10, totalWords - learnedWords);
        tvNewWords.setText(newWordsToLearn + " new words");
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerCategories.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerCategories.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ========== Learning Sessions ==========

    private void startLearningSession() {
        // Get unlearned words
        List<SurvivalWord> newWords = new ArrayList<>();
        for (SurvivalWord word : allWords) {
            if (!word.isLearned()) {
                newWords.add(word);
                if (newWords.size() >= 10)
                    break;
            }
        }

        if (newWords.isEmpty()) {
            Toast.makeText(this, "🎉 You've learned all words! Try reviewing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Launch FlashcardActivity with newWords
        Toast.makeText(this, "📚 Starting with " + newWords.size() + " new words!", Toast.LENGTH_SHORT).show();
    }

    private void startReviewSession() {
        if (dueForReview.isEmpty()) {
            Toast.makeText(this, "✨ No words due for review right now!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Launch FlashcardActivity with dueForReview
        Toast.makeText(this, "📝 Reviewing " + dueForReview.size() + " words!", Toast.LENGTH_SHORT).show();
    }

    // ========== Adapter Callbacks ==========

    @Override
    public void onCategoryClick(VocabularyCategory category, int position) {
        // Launch category detail with words
        Intent intent = new Intent(this, CategoryWordsActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onWordClick(SurvivalWord word, int position) {
        Toast.makeText(this, "📖 " + word.getWord() + " - " + word.getMeaningVi(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKnowClick(SurvivalWord word, int position) {
        updateWordProgress(word, position, true);
    }

    @Override
    public void onLearnClick(SurvivalWord word, int position) {
        updateWordProgress(word, position, false);
    }

    @Override
    public void onPlaySound(SurvivalWord word, int position) {
        Toast.makeText(this, "🔊 " + word.getWord(), Toast.LENGTH_SHORT).show();
    }

    private void updateWordProgress(SurvivalWord word, int position, boolean known) {
        if (userId == null)
            return;

        // Calculate SRS values
        int newSrsLevel = known ? Math.min(word.getSrsLevel() + 1, 3) : Math.max(word.getSrsLevel() - 1, 0);
        long now = System.currentTimeMillis();

        // Calculate next review time based on SRS level
        long[] intervals = {
                60 * 60 * 1000L, // Level 0: 1 hour
                24 * 60 * 60 * 1000L, // Level 1: 1 day
                3 * 24 * 60 * 60 * 1000L, // Level 2: 3 days
                7 * 24 * 60 * 60 * 1000L // Level 3: 7 days
        };
        long nextReview = now + intervals[newSrsLevel];

        Map<String, Object> progress = new HashMap<>();
        progress.put("srsLevel", newSrsLevel);
        progress.put("nextReviewTime", nextReview);
        progress.put("reviewCount", word.getReviewCount() + 1);
        progress.put("lastReviewTime", now);
        progress.put("learned", true);
        if (known)
            progress.put("correctCount", word.getCorrectCount() + 1);

        db.collection("users").document(userId)
                .collection("vocab_progress")
                .document(word.getId())
                .set(progress)
                .addOnSuccessListener(aVoid -> {
                    word.setSrsLevel(newSrsLevel);
                    word.setNextReviewTime(nextReview);
                    word.setReviewCount(word.getReviewCount() + 1);
                    word.setLearned(true);
                    word.setLastLearnedAt(now);

                    if (known) {
                        word.setCorrectCount(word.getCorrectCount() + 1);
                        Toast.makeText(this, "✅ Great! Next review in " + getTimeString(intervals[newSrsLevel]),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "📖 Keep practicing! Review again soon.", Toast.LENGTH_SHORT).show();
                    }

                    recentAdapter.notifyItemChanged(position);
                    updateStats();

                    // Award XP for first time learning
                    if (word.getReviewCount() == 1) {
                        awardXP(5);
                    }
                });
    }

    private String getTimeString(long millis) {
        if (millis < 60 * 60 * 1000)
            return (millis / (60 * 1000)) + " min";
        if (millis < 24 * 60 * 60 * 1000)
            return (millis / (60 * 60 * 1000)) + " hours";
        return (millis / (24 * 60 * 60 * 1000)) + " days";
    }

    private void awardXP(int xp) {
        if (userId == null)
            return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    long currentXP = doc.getLong("totalXP") != null ? doc.getLong("totalXP") : 0;
                    db.collection("users").document(userId)
                            .update("totalXP", currentXP + xp);
                });
    }

    // ========== Seed Data ==========

    private void seedVocabularyData() {
        showLoading(true);

        WriteBatch batch = db.batch();

        // Create categories
        List<VocabularyCategory> cats = createSampleCategories();
        for (VocabularyCategory cat : cats) {
            batch.set(db.collection("vocabulary_categories").document(), cat.toMap());
        }

        // Create words
        List<SurvivalWord> words = createSampleWords();
        for (SurvivalWord word : words) {
            batch.set(db.collection("survival_words").document(), word.toMap());
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Loaded " + cats.size() + " categories & " + words.size() + " words!",
                            Toast.LENGTH_SHORT).show();
                    loadCategories();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<VocabularyCategory> createSampleCategories() {
        List<VocabularyCategory> cats = new ArrayList<>();

        String[][] catData = {
                { "Greetings", "Chào hỏi", "👋", "#10B981", "#14B8A6" },
                { "Numbers", "Số đếm", "🔢", "#3B82F6", "#06B6D4" },
                { "Colors", "Màu sắc", "🎨", "#F59E0B", "#F97316" },
                { "Food & Drinks", "Đồ ăn & Thức uống", "🍔", "#8B5CF6", "#A855F7" },
                { "Family", "Gia đình", "👨‍👩‍👧‍👦", "#EC4899", "#F43F5E" },
                { "Weather", "Thời tiết", "☀️", "#6366F1", "#8B5CF6" },
                { "Body Parts", "Cơ thể", "💪", "#14B8A6", "#06B6D4" },
                { "Emotions", "Cảm xúc", "😊", "#EF4444", "#F97316" },
                { "Places", "Địa điểm", "🏠", "#84CC16", "#22C55E" },
                { "Transport", "Phương tiện", "🚗", "#0EA5E9", "#3B82F6" },
        };

        for (int i = 0; i < catData.length; i++) {
            VocabularyCategory cat = new VocabularyCategory();
            cat.setName(catData[i][0]);
            cat.setNameVi(catData[i][1]);
            cat.setEmoji(catData[i][2]);
            cat.setGradientColors(Arrays.asList(catData[i][3], catData[i][4]));
            cat.setOrder(i);
            cat.setWordCount(20);
            cats.add(cat);
        }

        return cats;
    }

    private List<SurvivalWord> createSampleWords() {
        List<SurvivalWord> words = new ArrayList<>();

        // Greetings
        addWord(words, "Hello", "həˈloʊ", "Xin chào", "Greetings", "👋", "Hello, how are you?");
        addWord(words, "Goodbye", "ɡʊdˈbaɪ", "Tạm biệt", "Greetings", "👋", "Goodbye, see you later!");
        addWord(words, "Thank you", "θæŋk juː", "Cảm ơn", "Greetings", "🙏", "Thank you very much!");
        addWord(words, "Please", "pliːz", "Làm ơn", "Greetings", "🙏", "Please help me.");
        addWord(words, "Sorry", "ˈsɔːri", "Xin lỗi", "Greetings", "😔", "I'm sorry for being late.");
        addWord(words, "Yes", "jes", "Có/Vâng", "Greetings", "✅", "Yes, I understand.");
        addWord(words, "No", "noʊ", "Không", "Greetings", "❌", "No, thank you.");
        addWord(words, "Excuse me", "ɪkˈskjuːz miː", "Xin lỗi (để hỏi)", "Greetings", "🙋",
                "Excuse me, where is the station?");
        addWord(words, "Good morning", "ɡʊd ˈmɔːrnɪŋ", "Chào buổi sáng", "Greetings", "🌅", "Good morning, everyone!");
        addWord(words, "Good night", "ɡʊd naɪt", "Chúc ngủ ngon", "Greetings", "🌙", "Good night, sweet dreams!");

        // Numbers
        addWord(words, "One", "wʌn", "Một", "Numbers", "1️⃣", "I have one apple.");
        addWord(words, "Two", "tuː", "Hai", "Numbers", "2️⃣", "Two plus two equals four.");
        addWord(words, "Three", "θriː", "Ba", "Numbers", "3️⃣", "There are three cats.");
        addWord(words, "Four", "fɔːr", "Bốn", "Numbers", "4️⃣", "I need four eggs.");
        addWord(words, "Five", "faɪv", "Năm", "Numbers", "5️⃣", "Give me five!");
        addWord(words, "Ten", "ten", "Mười", "Numbers", "🔟", "I have ten dollars.");
        addWord(words, "Hundred", "ˈhʌndrəd", "Một trăm", "Numbers", "💯", "It costs one hundred dollars.");
        addWord(words, "Thousand", "ˈθaʊzənd", "Một nghìn", "Numbers", "🔢", "A thousand stars.");
        addWord(words, "First", "fɜːrst", "Thứ nhất", "Numbers", "🥇", "I came first!");
        addWord(words, "Second", "ˈsekənd", "Thứ hai", "Numbers", "🥈", "She finished second.");

        // Colors
        addWord(words, "Red", "red", "Đỏ", "Colors", "🔴", "The apple is red.");
        addWord(words, "Blue", "bluː", "Xanh dương", "Colors", "🔵", "The sky is blue.");
        addWord(words, "Green", "ɡriːn", "Xanh lá", "Colors", "🟢", "Grass is green.");
        addWord(words, "Yellow", "ˈjeloʊ", "Vàng", "Colors", "🟡", "The sun is yellow.");
        addWord(words, "Black", "blæk", "Đen", "Colors", "⚫", "My cat is black.");
        addWord(words, "White", "waɪt", "Trắng", "Colors", "⚪", "Snow is white.");
        addWord(words, "Orange", "ˈɔːrɪndʒ", "Cam", "Colors", "🟠", "I like orange juice.");
        addWord(words, "Pink", "pɪŋk", "Hồng", "Colors", "🩷", "She wears pink.");
        addWord(words, "Purple", "ˈpɜːrpl", "Tím", "Colors", "🟣", "Purple is my favorite.");
        addWord(words, "Brown", "braʊn", "Nâu", "Colors", "🤎", "The dog is brown.");

        // Food & Drinks
        addWord(words, "Water", "ˈwɔːtər", "Nước", "Food & Drinks", "💧", "I need some water.");
        addWord(words, "Rice", "raɪs", "Cơm", "Food & Drinks", "🍚", "Rice is delicious.");
        addWord(words, "Bread", "bred", "Bánh mì", "Food & Drinks", "🍞", "I eat bread for breakfast.");
        addWord(words, "Meat", "miːt", "Thịt", "Food & Drinks", "🥩", "This meat is tender.");
        addWord(words, "Fish", "fɪʃ", "Cá", "Food & Drinks", "🐟", "Fish is healthy.");
        addWord(words, "Chicken", "ˈtʃɪkɪn", "Gà", "Food & Drinks", "🍗", "I love fried chicken.");
        addWord(words, "Egg", "eɡ", "Trứng", "Food & Drinks", "🥚", "Two eggs please.");
        addWord(words, "Milk", "mɪlk", "Sữa", "Food & Drinks", "🥛", "Drink your milk.");
        addWord(words, "Coffee", "ˈkɔːfi", "Cà phê", "Food & Drinks", "☕", "Coffee keeps me awake.");
        addWord(words, "Tea", "tiː", "Trà", "Food & Drinks", "🍵", "Would you like some tea?");

        // Family
        addWord(words, "Mother", "ˈmʌðər", "Mẹ", "Family", "👩", "My mother is kind.");
        addWord(words, "Father", "ˈfɑːðər", "Bố", "Family", "👨", "My father is tall.");
        addWord(words, "Sister", "ˈsɪstər", "Chị/Em gái", "Family", "👧", "I have a sister.");
        addWord(words, "Brother", "ˈbrʌðər", "Anh/Em trai", "Family", "👦", "My brother plays football.");
        addWord(words, "Grandmother", "ˈɡrænmʌðər", "Bà", "Family", "👵", "I visit my grandmother.");
        addWord(words, "Grandfather", "ˈɡrænfɑːðər", "Ông", "Family", "👴", "My grandfather tells stories.");
        addWord(words, "Son", "sʌn", "Con trai", "Family", "👦", "This is my son.");
        addWord(words, "Daughter", "ˈdɔːtər", "Con gái", "Family", "👧", "My daughter is smart.");
        addWord(words, "Wife", "waɪf", "Vợ", "Family", "👰", "She is my wife.");
        addWord(words, "Husband", "ˈhʌzbənd", "Chồng", "Family", "🤵", "He is my husband.");

        return words;
    }

    private void addWord(List<SurvivalWord> list, String word, String pronunciation,
            String meaningVi, String category, String emoji, String example) {
        SurvivalWord w = new SurvivalWord();
        w.setWord(word);
        w.setPronunciation(pronunciation);
        w.setMeaningVi(meaningVi);
        w.setCategory(category);
        w.setEmoji(emoji);
        w.setExampleSentence(example);
        w.setSrsLevel(0);
        list.add(w);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProgress(); // Refresh progress when returning
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recentAdapter != null)
            recentAdapter.release();
    }
}
