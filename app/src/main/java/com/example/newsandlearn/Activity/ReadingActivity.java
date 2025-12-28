package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.ReadingQuestionAdapter;
import com.example.newsandlearn.Model.ReadingArticle;
import com.example.newsandlearn.Model.ReadingQuestion;
import com.example.newsandlearn.Model.UserProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.BionicReadingManager;
import com.example.newsandlearn.Utils.ProgressManager;
import com.example.newsandlearn.Utils.VocabularyPreTeachManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ReadingActivity - Display full article with comprehension questions
 * All content loaded from Firebase, NO hard-coded articles
 */
public class ReadingActivity extends AppCompatActivity {

    private static final String TAG = "ReadingActivity";

    private MaterialToolbar toolbar;
    private LinearProgressIndicator readingProgress;
    private NestedScrollView scrollView;

    private TextView articleTitle, authorText, readTime, articleContent, questionsSubtitle;
    private TextView keyVocabTitle;
    private ChipGroup keyVocabGroup;
    private RecyclerView questionsRecyclerView;
    private MaterialButton submitButton;

    private ReadingArticle article;
    private String articleId;
    private ReadingQuestionAdapter questionsAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressManager progressManager;

    private boolean bionicEnabled = false;
    private boolean bookmarked = false;
    private float contentTextSizeSp = 16f;
    private Integer restoreScrollY = null;
    private long sessionStartUptimeMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        articleId = getIntent().getStringExtra("article_id");
        if (articleId == null) {
            Toast.makeText(this, "Error: No article ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupListeners();
        loadArticleFromFirebase();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        progressManager = ProgressManager.getInstance();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        readingProgress = findViewById(R.id.reading_progress);
        scrollView = findViewById(R.id.reading_scroll);

        articleTitle = findViewById(R.id.article_title);
        authorText = findViewById(R.id.author_text);
        readTime = findViewById(R.id.read_time);
        articleContent = findViewById(R.id.article_content);
        questionsSubtitle = findViewById(R.id.questions_subtitle);
        keyVocabTitle = findViewById(R.id.key_vocab_title);
        keyVocabGroup = findViewById(R.id.key_vocab_group);
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        submitButton = findViewById(R.id.submit_button);

        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            toolbar.setTitle("Reading");
        }

        loadLocalReaderPrefs();
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> submitAnswers());

        if (scrollView != null && readingProgress != null) {
            readingProgress.setVisibility(android.view.View.VISIBLE);
            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY,
                    oldScrollX, oldScrollY) -> updateReadingProgress());
        }
    }

    /**
     * Load article from Firebase - DYNAMIC
     */
    private void loadArticleFromFirebase() {
        db.collection("reading_lessons").document(articleId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        article = new ReadingArticle();
                        String resolvedId = documentSnapshot.getString("id");
                        if (resolvedId == null || resolvedId.isEmpty()) {
                            resolvedId = documentSnapshot.getId();
                        }
                        if (resolvedId == null || resolvedId.isEmpty()) {
                            resolvedId = articleId;
                        }
                        article.setId(resolvedId);
                        article.setTitle(documentSnapshot.getString("title"));
                        article.setPassage(documentSnapshot.getString("passage"));
                        article.setContent(documentSnapshot.getString("content"));
                        article.setImageUrl(documentSnapshot.getString("imageUrl"));
                        article.setLevel(documentSnapshot.getString("level"));
                        article.setCategory(documentSnapshot.getString("category"));

                        Long wordCount = documentSnapshot.getLong("wordCount");
                        if (wordCount != null) {
                            article.setWordCount(wordCount.intValue());
                        }

                        // Optional: key vocabulary list (if present in Firestore)
                        Object kv = documentSnapshot.get("keyVocabulary");
                        if (kv instanceof List) {
                            try {
                                // noinspection unchecked
                                article.setKeyVocabulary((List<String>) kv);
                            } catch (Exception ignored) {
                                // Keep empty
                            }
                        }

                        // Parse exercises from Firebase Map format to ReadingQuestion objects
                        List<?> exercisesList = (List<?>) documentSnapshot.get("exercises");
                        Log.d(TAG, "Exercises list from Firebase: "
                                + (exercisesList != null ? exercisesList.size() + " items" : "null"));

                        if (exercisesList != null) {
                            List<ReadingQuestion> questions = new ArrayList<>();
                            int exerciseIndex = 0;
                            for (Object exerciseObj : exercisesList) {
                                if (exerciseObj instanceof Map) {
                                    Map<String, Object> exerciseMap = (Map<String, Object>) exerciseObj;
                                    ReadingQuestion question = new ReadingQuestion();
                                    question.setQuestionText((String) exerciseMap.get("question"));
                                    question.setCorrectAnswer((String) exerciseMap.get("correctAnswer"));
                                    question.setExplanation((String) exerciseMap.get("explanation"));

                                    List<String> options = (List<String>) exerciseMap.get("options");
                                    question.setOptions(options);

                                    questions.add(question);
                                    Log.d(TAG, "Exercise " + (exerciseIndex + 1) + ": " + question.getQuestionText());
                                    exerciseIndex++;
                                }
                            }
                            article.setQuestions(questions);
                            Log.d(TAG, "Total questions parsed: " + questions.size());
                        } else {
                            Log.w(TAG, "No exercises found in Firebase document");
                        }

                        displayArticle();
                        article.incrementReadCount();
                        loadUserReadingStateThenApply();
                        saveProgress();

                        // Track reading for daily tasks
                        progressManager.trackArticleRead(new ProgressManager.ProgressCallback() {
                            @Override
                            public void onSuccess(UserProgress progress) {
                                Log.d(TAG, "Reading tracked successfully");
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to track reading", e);
                            }
                        });
                    } else {
                        Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading article", e);
                    finish();
                });
    }

    private void displayArticle() {
        articleTitle.setText(article.getTitle());
        if (toolbar != null && article.getTitle() != null) {
            toolbar.setTitle(article.getTitle());
        }

        // Prefer passage over content (reading_lessons use 'passage')
        String textToDisplay = article.getPassage() != null ? article.getPassage() : article.getContent();
        if (textToDisplay != null) {
            applyReaderText(textToDisplay);
        }

        renderKeyVocabulary(article.getKeyVocabulary());

        if (article.getAuthor() != null) {
            authorText.setText("By " + article.getAuthor());
        } else {
            authorText.setText("By Author");
        }

        // Calculate read time from word count if not set
        int readTimeMinutes = article.getEstimatedMinutes();
        if (readTimeMinutes == 0 && article.getWordCount() > 0) {
            readTimeMinutes = Math.max(1, article.getWordCount() / 200); // Average reading speed
        }
        if (readTimeMinutes == 0) {
            readTimeMinutes = 5; // Default
        }
        readTime.setText(readTimeMinutes + " min read");

        // Setup questions adapter
        if (article.getQuestions() != null && !article.getQuestions().isEmpty()) {
            questionsAdapter = new ReadingQuestionAdapter(article.getQuestions());
            questionsRecyclerView.setAdapter(questionsAdapter);

            int questionCount = article.getQuestions().size();
            Log.d(TAG, "Loaded " + questionCount + " questions");

            // Update subtitle with question count
            questionsSubtitle.setText("Answer all " + questionCount + " questions below");
        } else {
            Log.w(TAG, "No questions found for this article");
            questionsSubtitle.setText("No questions available");
            submitButton.setEnabled(false);
            submitButton.setText("No Questions Available");
        }

        updateReadingProgress();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Menu is declared in XML on the toolbar, but keep this to ensure it shows in
        // all configs.
        getMenuInflater().inflate(R.menu.reading_activity_menu, menu);
        syncMenuState(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_text_larger) {
            adjustTextSize(+1f);
            return true;
        } else if (id == R.id.action_text_smaller) {
            adjustTextSize(-1f);
            return true;
        } else if (id == R.id.action_bionic) {
            bionicEnabled = !bionicEnabled;
            item.setChecked(bionicEnabled);
            persistLocalReaderPrefs();
            if (article != null) {
                String textToDisplay = article.getPassage() != null ? article.getPassage() : article.getContent();
                if (textToDisplay != null)
                    applyReaderText(textToDisplay);
            }
            saveProgress();
            return true;
        } else if (id == R.id.action_vocab_preview) {
            startVocabularyPreview();
            return true;
        } else if (id == R.id.action_bookmark) {
            bookmarked = !bookmarked;
            saveProgress();
            syncMenuState(item.getMenuInfo() == null ? null : null);
            invalidateOptionsMenu();
            Toast.makeText(this, bookmarked ? "Saved" : "Removed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderKeyVocabulary(List<String> keyVocabulary) {
        if (keyVocabTitle == null || keyVocabGroup == null)
            return;

        keyVocabGroup.removeAllViews();

        if (keyVocabulary == null || keyVocabulary.isEmpty()) {
            keyVocabTitle.setVisibility(android.view.View.GONE);
            keyVocabGroup.setVisibility(android.view.View.GONE);
            return;
        }

        keyVocabTitle.setVisibility(android.view.View.VISIBLE);
        keyVocabGroup.setVisibility(android.view.View.VISIBLE);

        int limit = Math.min(10, keyVocabulary.size());
        for (int i = 0; i < limit; i++) {
            String word = keyVocabulary.get(i);
            if (word == null)
                continue;
            String trimmed = word.trim();
            if (trimmed.isEmpty())
                continue;

            Chip chip = new Chip(this);
            chip.setText(trimmed);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("word", trimmed));
                    Toast.makeText(this, "Copied: " + trimmed, Toast.LENGTH_SHORT).show();
                }
            });
            keyVocabGroup.addView(chip);
        }
    }

    private void startVocabularyPreview() {
        if (article == null) {
            Toast.makeText(this, "Please wait…", Toast.LENGTH_SHORT).show();
            return;
        }

        String textToAnalyze = article.getPassage() != null ? article.getPassage() : article.getContent();
        if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
            Toast.makeText(this, "No content to analyze", Toast.LENGTH_SHORT).show();
            return;
        }

        String level = article.getLevel() != null ? article.getLevel() : "B1";

        VocabularyPreTeachManager manager;
        try {
            manager = VocabularyPreTeachManager.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Vocabulary preview requires GEMINI_API_KEY", Toast.LENGTH_LONG).show();
            Log.w(TAG, "VocabularyPreTeachManager unavailable", e);
            return;
        }

        Toast.makeText(this, "Preparing vocabulary…", Toast.LENGTH_SHORT).show();
        manager.analyzeAndSelectWords(textToAnalyze, article.getTitle() != null ? article.getTitle() : "Reading", level,
                new VocabularyPreTeachManager.PreTeachCallback() {
                    @Override
                    public void onSuccess(List<VocabularyPreTeachManager.PreTeachWord> words, String summary) {
                        runOnUiThread(() -> manager.showPreTeachDialog(ReadingActivity.this, summary,
                                new VocabularyPreTeachManager.PreTeachResultCallback() {
                                    @Override
                                    public void onCompleted(int learnedCount, int totalWords) {
                                        Toast.makeText(ReadingActivity.this,
                                                "Learned " + learnedCount + "/" + totalWords + " words",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onSkipped() {
                                        // no-op
                                    }
                                }));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> Toast.makeText(ReadingActivity.this, errorMessage, Toast.LENGTH_LONG)
                                .show());
                    }
                });
    }

    private void syncMenuState(android.view.Menu menu) {
        if (menu == null)
            return;
        android.view.MenuItem bionic = menu.findItem(R.id.action_bionic);
        if (bionic != null)
            bionic.setChecked(bionicEnabled);
        // Bookmark icon isn't defined in this menu (text-only). Keep state for future.
    }

    private void adjustTextSize(float deltaSp) {
        contentTextSizeSp = Math.max(12f, Math.min(22f, contentTextSizeSp + deltaSp));
        persistLocalReaderPrefs();
        if (article != null) {
            String textToDisplay = article.getPassage() != null ? article.getPassage() : article.getContent();
            if (textToDisplay != null)
                applyReaderText(textToDisplay);
        }
        saveProgress();
    }

    private void applyReaderText(String rawText) {
        if (articleContent == null)
            return;
        articleContent.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, contentTextSizeSp);

        if (!bionicEnabled) {
            articleContent.setText(rawText);
            return;
        }

        BionicReadingManager manager = BionicReadingManager.getInstance();
        manager.setHighlightColor(articleContent.getCurrentTextColor());
        articleContent.setText(manager.applyBionicReading(rawText, 2));
    }

    private void updateReadingProgress() {
        if (scrollView == null || readingProgress == null)
            return;
        int contentHeight = scrollView.getChildCount() > 0 ? scrollView.getChildAt(0).getHeight() : 0;
        int viewport = scrollView.getHeight();
        int maxScroll = Math.max(1, contentHeight - viewport);
        int progress = Math.max(0, Math.min(100, (scrollView.getScrollY() * 100) / maxScroll));
        readingProgress.setProgress(progress);
    }

    private void loadLocalReaderPrefs() {
        android.content.SharedPreferences prefs = getSharedPreferences("reading_prefs", MODE_PRIVATE);
        contentTextSizeSp = prefs.getFloat("contentTextSizeSp", 16f);
        bionicEnabled = prefs.getBoolean("bionicEnabled", false);
    }

    private void persistLocalReaderPrefs() {
        android.content.SharedPreferences prefs = getSharedPreferences("reading_prefs", MODE_PRIVATE);
        prefs.edit()
                .putFloat("contentTextSizeSp", contentTextSizeSp)
                .putBoolean("bionicEnabled", bionicEnabled)
                .apply();
    }

    private void loadUserReadingStateThenApply() {
        if (auth.getCurrentUser() == null)
            return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("reading_progress").document(articleId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean bm = doc.getBoolean("bookmarked");
                    bookmarked = bm != null && bm;
                    Long scrollY = doc.getLong("lastScrollY");
                    restoreScrollY = scrollY != null ? scrollY.intValue() : null;

                    // Apply cloud-stored reader prefs if present (fallback to local)
                    Double cloudSize = doc.getDouble("contentTextSizeSp");
                    if (cloudSize != null)
                        contentTextSizeSp = cloudSize.floatValue();
                    Boolean cloudBionic = doc.getBoolean("bionicEnabled");
                    if (cloudBionic != null)
                        bionicEnabled = cloudBionic;

                    persistLocalReaderPrefs();

                    if (article != null) {
                        String textToDisplay = article.getPassage() != null ? article.getPassage()
                                : article.getContent();
                        if (textToDisplay != null)
                            applyReaderText(textToDisplay);
                    }

                    if (restoreScrollY != null && scrollView != null) {
                        scrollView.post(() -> {
                            scrollView.scrollTo(0, restoreScrollY);
                            updateReadingProgress();
                        });
                    }
                    invalidateOptionsMenu();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to load reading state", e));
    }

    private void submitAnswers() {
        if (questionsAdapter == null) {
            Toast.makeText(this, "No questions to submit", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if all questions are answered
        Map<Integer, String> userAnswers = questionsAdapter.getUserAnswers();
        int totalQuestions = article.getQuestions().size();

        if (userAnswers.size() < totalQuestions) {
            Toast.makeText(this, "Please answer all questions (" + userAnswers.size() + "/" + totalQuestions + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate score
        int score = questionsAdapter.calculateScore();

        // Build ordered user answers list for the review screen
        ArrayList<String> orderedAnswers = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++) {
            orderedAnswers.add(userAnswers.get(i));
        }

        article.setCompleted(true);
        article.setUserScore(score);
        saveProgress();

        // Award XP based on score
        int xpEarned = score >= 80 ? 30 : (score >= 60 ? 20 : 10);

        progressManager.addXP(xpEarned, new ProgressManager.ProgressCallback() {
            @Override
            public void onSuccess(UserProgress progress) {
                openReviewScreen(score, xpEarned, orderedAnswers);
            }

            @Override
            public void onFailure(Exception e) {
                openReviewScreen(score, 0, orderedAnswers);
            }
        });
    }

    private void openReviewScreen(int score, int xpEarned, ArrayList<String> orderedAnswers) {
        Intent intent = new Intent(this, ReadingReviewActivity.class);
        intent.putExtra(ReadingReviewActivity.EXTRA_ARTICLE_ID, articleId);
        intent.putExtra(ReadingReviewActivity.EXTRA_SCORE, score);
        intent.putExtra(ReadingReviewActivity.EXTRA_XP_EARNED, xpEarned);
        intent.putStringArrayListExtra(ReadingReviewActivity.EXTRA_USER_ANSWERS, orderedAnswers);
        startActivity(intent);
        finish();
    }

    /**
     * Save progress to Firebase
     */
    private void saveProgress() {
        if (auth.getCurrentUser() == null)
            return;

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> progress = new HashMap<>();
        progress.put("articleId", articleId);
        if (article != null) {
            progress.put("completed", article.isCompleted());
            progress.put("userScore", article.getUserScore());
            progress.put("readCount", article.getReadCount());
            progress.put("lastReadAt", new Date());
        }
        progress.put("bookmarked", bookmarked);
        progress.put("bionicEnabled", bionicEnabled);
        progress.put("contentTextSizeSp", (double) contentTextSizeSp);
        if (scrollView != null) {
            progress.put("lastScrollY", scrollView.getScrollY());
        }

        db.collection("users").document(userId)
                .collection("reading_progress").document(articleId)
                .set(progress, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Progress saved"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save progress", e));
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionStartUptimeMs = SystemClock.uptimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Persist scroll/settings frequently; time tracking can be added later.
        saveProgress();
    }
}
