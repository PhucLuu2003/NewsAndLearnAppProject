package com.example.newsandlearn.Activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.newsandlearn.Adapter.PhonicsAdapter;
import com.example.newsandlearn.Model.PhonicsLesson;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
 * 🗣️ PhonicsActivity - Learn pronunciation from A-Z
 * Features:
 * - Alphabet letters (A-Z)
 * - Vowel sounds (long, short, diphthongs)
 * - Consonant sounds
 * - Word stress patterns
 * - Audio playback with mouth position guides
 * - Recording & comparison (future)
 */
public class PhonicsActivity extends AppCompatActivity implements PhonicsAdapter.OnLessonClickListener {

    // UI Components
    private Toolbar toolbar;
    private LottieAnimationView headerAnimation;
    private TextView tvCompletedCount, tvSoundsCount, tvXpEarned, tvSectionTitle;
    private ChipGroup chipGroupCategories;
    private Chip chipAlphabet, chipVowels, chipConsonants, chipStress;
    private RecyclerView recyclerLessons;
    private ProgressBar progressLoading;
    private LinearLayout emptyState;
    private MaterialButton btnSeedData;
    private ExtendedFloatingActionButton fabRecord;

    // Adapter & Data
    private PhonicsAdapter adapter;
    private List<PhonicsLesson> allLessons = new ArrayList<>();
    private String currentCategory = "alphabet";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    // Audio
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonics);

        initFirebase();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupChipListeners();
        setupClickListeners();
        loadPhonicsLessons();
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
        tvCompletedCount = findViewById(R.id.tv_completed_count);
        tvSoundsCount = findViewById(R.id.tv_sounds_count);
        tvXpEarned = findViewById(R.id.tv_xp_earned);
        tvSectionTitle = findViewById(R.id.tv_section_title);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        chipAlphabet = findViewById(R.id.chip_alphabet);
        chipVowels = findViewById(R.id.chip_vowels);
        chipConsonants = findViewById(R.id.chip_consonants);
        chipStress = findViewById(R.id.chip_stress);
        recyclerLessons = findViewById(R.id.recycler_lessons);
        progressLoading = findViewById(R.id.progress_loading);
        emptyState = findViewById(R.id.empty_state);
        btnSeedData = findViewById(R.id.btn_seed_data);
        fabRecord = findViewById(R.id.fab_record);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new PhonicsAdapter(this, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerLessons.setLayoutManager(layoutManager);
        recyclerLessons.setAdapter(adapter);
        recyclerLessons.setItemAnimator(null); // Disable default animations for custom ones
    }

    private void setupChipListeners() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_alphabet) {
                currentCategory = "alphabet";
                tvSectionTitle.setText("🔤 Alphabet (A-Z)");
            } else if (checkedId == R.id.chip_vowels) {
                currentCategory = "vowel";
                tvSectionTitle.setText("🔴 Vowel Sounds");
            } else if (checkedId == R.id.chip_consonants) {
                currentCategory = "consonant";
                tvSectionTitle.setText("🔵 Consonant Sounds");
            } else if (checkedId == R.id.chip_stress) {
                currentCategory = "stress";
                tvSectionTitle.setText("💪 Word Stress");
            }

            filterLessonsByCategory(currentCategory);
        });
    }

    private void setupClickListeners() {
        btnSeedData.setOnClickListener(v -> seedPhonicsData());

        fabRecord.setOnClickListener(v -> {
            animateFab(fabRecord);
            Toast.makeText(this, "🎤 Recording feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void animateFab(View fab) {
        AnimatorSet scaleAnim = new AnimatorSet();
        scaleAnim.playTogether(
                ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0.9f, 1.1f, 1f),
                ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0.9f, 1.1f, 1f));
        scaleAnim.setDuration(300);
        scaleAnim.setInterpolator(new OvershootInterpolator(2f));
        scaleAnim.start();
    }

    private void loadPhonicsLessons() {
        showLoading(true);

        db.collection("phonics_lessons")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showLoading(false);
                    allLessons.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        PhonicsLesson lesson = doc.toObject(PhonicsLesson.class);
                        if (lesson != null) {
                            lesson.setId(doc.getId());
                            allLessons.add(lesson);
                        }
                    }

                    if (allLessons.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                        filterLessonsByCategory(currentCategory);
                        updateStats();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState(true);
                    Toast.makeText(this, "Error loading lessons: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProgress() {
        if (userId == null)
            return;

        db.collection("users").document(userId)
                .collection("phonics_progress")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Boolean> completedLessons = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean completed = doc.getBoolean("completed");
                        if (completed != null && completed) {
                            completedLessons.put(doc.getId(), true);
                        }
                    }

                    // Update lessons with completion status
                    for (PhonicsLesson lesson : allLessons) {
                        if (completedLessons.containsKey(lesson.getId())) {
                            lesson.setCompleted(true);
                        }
                    }

                    updateStats();
                    adapter.notifyDataSetChanged();
                });
    }

    private void filterLessonsByCategory(String category) {
        List<PhonicsLesson> filtered = new ArrayList<>();
        for (PhonicsLesson lesson : allLessons) {
            if (lesson.getCategory() != null && lesson.getCategory().equalsIgnoreCase(category)) {
                filtered.add(lesson);
            }
        }
        adapter.setLessons(filtered);
    }

    private void updateStats() {
        int totalLetters = 0, completedLetters = 0;
        int totalSounds = 0;
        int totalXp = 0;

        for (PhonicsLesson lesson : allLessons) {
            if (lesson.getCategory().equalsIgnoreCase("alphabet")) {
                totalLetters++;
                if (lesson.isCompleted())
                    completedLetters++;
            }
            totalSounds++;
            if (lesson.isCompleted()) {
                totalXp += lesson.getXpReward();
            }
        }

        tvCompletedCount.setText(completedLetters + "/" + totalLetters);
        tvSoundsCount.setText("0/" + totalSounds); // Sounds mastered
        tvXpEarned.setText(totalXp + " XP");
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerLessons.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerLessons.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ========== PhonicsAdapter.OnLessonClickListener ==========

    @Override
    public void onLessonClick(PhonicsLesson lesson, int position) {
        // TODO: Open lesson detail activity with mouth position guide
        Toast.makeText(this, "📖 " + lesson.getTitle() + " - " + lesson.getSymbol(), Toast.LENGTH_SHORT).show();

        // Mark as completed and award XP
        if (!lesson.isCompleted() && userId != null) {
            markLessonCompleted(lesson, position);
        }
    }

    @Override
    public void onPlaySound(PhonicsLesson lesson, int position) {
        // Play sound will be handled by adapter
        Toast.makeText(this, "🔊 Playing: /" + lesson.getSymbol() + "/", Toast.LENGTH_SHORT).show();
    }

    private void markLessonCompleted(PhonicsLesson lesson, int position) {
        if (userId == null)
            return;

        Map<String, Object> progress = new HashMap<>();
        progress.put("completed", true);
        progress.put("completedAt", System.currentTimeMillis());
        progress.put("xpEarned", lesson.getXpReward());

        db.collection("users").document(userId)
                .collection("phonics_progress")
                .document(lesson.getId())
                .set(progress)
                .addOnSuccessListener(aVoid -> {
                    lesson.setCompleted(true);
                    adapter.notifyItemChanged(position);
                    updateStats();

                    // Award XP
                    awardXP(lesson.getXpReward());

                    Toast.makeText(this, "🎉 +" + lesson.getXpReward() + " XP earned!", Toast.LENGTH_SHORT).show();
                });
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

    // ========== Seed Data for Testing ==========

    private void seedPhonicsData() {
        showLoading(true);

        WriteBatch batch = db.batch();
        List<PhonicsLesson> lessons = createSampleLessons();

        for (PhonicsLesson lesson : lessons) {
            batch.set(db.collection("phonics_lessons").document(), lesson.toMap());
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Loaded " + lessons.size() + " phonics lessons!", Toast.LENGTH_SHORT).show();
                    loadPhonicsLessons();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<PhonicsLesson> createSampleLessons() {
        List<PhonicsLesson> lessons = new ArrayList<>();

        // Alphabet (A-Z)
        String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        String[] sounds = { "eɪ", "biː", "siː", "diː", "iː", "ef", "dʒiː", "eɪtʃ", "aɪ", "dʒeɪ",
                "keɪ", "el", "em", "en", "oʊ", "piː", "kjuː", "ɑːr", "es", "tiː",
                "juː", "viː", "ˈdʌbljuː", "eks", "waɪ", "ziː" };
        String[][] examples = {
                { "🍎 Apple", "🐜 Ant" }, { "🍌 Banana", "🐻 Bear" }, { "🐱 Cat", "🚗 Car" },
                { "🐕 Dog", "🦆 Duck" }, { "🐘 Elephant", "🥚 Egg" }, { "🐟 Fish", "🦊 Fox" },
                { "🍇 Grape", "🦒 Giraffe" }, { "🏠 House", "🐴 Horse" }, { "🍦 Ice cream", "🏝️ Island" },
                { "🤹 Juggler", "👖 Jeans" }, { "🪁 Kite", "🔑 Key" }, { "🦁 Lion", "🍋 Lemon" },
                { "🐵 Monkey", "🌙 Moon" }, { "👃 Nose", "🌃 Night" }, { "🐙 Octopus", "🍊 Orange" },
                { "🐷 Pig", "🍕 Pizza" }, { "👸 Queen", "❓ Question" }, { "🐇 Rabbit", "🌹 Rose" },
                { "☀️ Sun", "⭐ Star" }, { "🐯 Tiger", "🌳 Tree" }, { "☂️ Umbrella", "🦄 Unicorn" },
                { "🎻 Violin", "🌋 Volcano" }, { "🐋 Whale", "💧 Water" }, { "🎄 X-mas", "🦴 X-ray" },
                { "💛 Yellow", "🧘 Yoga" }, { "🦓 Zebra", "⚡ Zigzag" }
        };

        for (int i = 0; i < letters.length; i++) {
            PhonicsLesson lesson = new PhonicsLesson();
            lesson.setTitle("Letter " + letters[i]);
            lesson.setSymbol(sounds[i]);
            lesson.setCategory("alphabet");
            lesson.setExampleWords(Arrays.asList(examples[i]));
            lesson.setXpReward(10);
            lesson.setLevel("easy");
            lesson.setOrder(i);
            lessons.add(lesson);
        }

        // Vowel sounds
        String[] vowelNames = { "Short A", "Short E", "Short I", "Short O", "Short U",
                "Long A", "Long E", "Long I", "Long O", "Long U" };
        String[] vowelSymbols = { "æ", "e", "ɪ", "ɒ", "ʌ", "eɪ", "iː", "aɪ", "oʊ", "juː" };
        String[][] vowelExamples = {
                { "🐱 Cat", "🎩 Hat" }, { "🥚 Egg", "🛏️ Bed" }, { "🐷 Pig", "🐟 Fish" },
                { "🐕 Dog", "🪵 Log" }, { "☀️ Sun", "🏃 Run" }, { "🎂 Cake", "🎮 Game" },
                { "🐝 Bee", "🌳 Tree" }, { "🪁 Kite", "🚲 Bike" }, { "🐐 Goat", "🚤 Boat" },
                { "🦄 Unicorn", "🎵 Music" }
        };

        for (int i = 0; i < vowelNames.length; i++) {
            PhonicsLesson lesson = new PhonicsLesson();
            lesson.setTitle(vowelNames[i]);
            lesson.setSymbol(vowelSymbols[i]);
            lesson.setCategory("vowel");
            lesson.setExampleWords(Arrays.asList(vowelExamples[i]));
            lesson.setXpReward(15);
            lesson.setLevel(i < 5 ? "easy" : "medium");
            lesson.setOrder(30 + i);
            lessons.add(lesson);
        }

        // Consonant sounds
        String[] consonantNames = { "Voiced TH", "Voiceless TH", "SH", "CH", "ZH", "NG" };
        String[] consonantSymbols = { "ð", "θ", "ʃ", "tʃ", "ʒ", "ŋ" };
        String[][] consonantExamples = {
                { "👨 Father", "🪶 Feather" }, { "🤔 Think", "🦷 Tooth" },
                { "🐚 Shell", "👟 Shoe" }, { "🪑 Chair", "🧀 Cheese" },
                { "📺 Television", "💰 Treasure" }, { "🎤 Sing", "💍 Ring" }
        };

        for (int i = 0; i < consonantNames.length; i++) {
            PhonicsLesson lesson = new PhonicsLesson();
            lesson.setTitle(consonantNames[i]);
            lesson.setSymbol(consonantSymbols[i]);
            lesson.setCategory("consonant");
            lesson.setExampleWords(Arrays.asList(consonantExamples[i]));
            lesson.setXpReward(20);
            lesson.setLevel("medium");
            lesson.setOrder(50 + i);
            lessons.add(lesson);
        }

        // Word stress patterns
        String[] stressNames = { "1st Syllable Stress", "2nd Syllable Stress", "Compound Words" };
        String[] stressSymbols = { "ˈ___", "_ˈ__", "ˈ__ˌ__" };
        String[][] stressExamples = {
                { "🍎 APple", "🌳 TAble" }, { "🍌 baNAna", "🖥️ comPUter" },
                { "🏀 BASketball", "🍔 HAMburger" }
        };

        for (int i = 0; i < stressNames.length; i++) {
            PhonicsLesson lesson = new PhonicsLesson();
            lesson.setTitle(stressNames[i]);
            lesson.setSymbol(stressSymbols[i]);
            lesson.setCategory("stress");
            lesson.setExampleWords(Arrays.asList(stressExamples[i]));
            lesson.setXpReward(25);
            lesson.setLevel("hard");
            lesson.setOrder(60 + i);
            lessons.add(lesson);
        }

        return lessons;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null)
            adapter.release();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
