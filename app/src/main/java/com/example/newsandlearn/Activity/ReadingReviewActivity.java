package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Adapter.ReadingReviewAdapter;
import com.example.newsandlearn.Model.ReadingQuestion;
import com.example.newsandlearn.Model.ReadingReviewItem;
import com.example.newsandlearn.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ReadingReviewActivity extends AppCompatActivity {

    private static final String TAG = "ReadingReviewActivity";

    public static final String EXTRA_ARTICLE_ID = "article_id";
    public static final String EXTRA_SCORE = "score";
    public static final String EXTRA_XP_EARNED = "xp_earned";
    public static final String EXTRA_USER_ANSWERS = "user_answers";

    private MaterialToolbar toolbar;
    private ImageView image;
    private TextView title;
    private TextView summary;
    private RecyclerView recycler;
    private MaterialButton done;

    private FirebaseFirestore db;

    private String articleId;
    private int score;
    private int xpEarned;
    private ArrayList<String> userAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_review);

        db = FirebaseFirestore.getInstance();

        toolbar = findViewById(R.id.review_toolbar);
        image = findViewById(R.id.review_image);
        title = findViewById(R.id.review_title);
        summary = findViewById(R.id.review_summary);
        recycler = findViewById(R.id.review_recycler);
        done = findViewById(R.id.review_done);

        Intent intent = getIntent();
        articleId = intent.getStringExtra(EXTRA_ARTICLE_ID);
        score = intent.getIntExtra(EXTRA_SCORE, 0);
        xpEarned = intent.getIntExtra(EXTRA_XP_EARNED, 0);
        userAnswers = intent.getStringArrayListExtra(EXTRA_USER_ANSWERS);
        if (userAnswers == null)
            userAnswers = new ArrayList<>();

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.setTitle("Review");
        }

        recycler.setLayoutManager(new LinearLayoutManager(this));

        done.setOnClickListener(v -> finish());

        if (articleId == null || articleId.trim().isEmpty()) {
            Toast.makeText(this, "Missing article id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadFromFirestore();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFromFirestore() {
        db.collection("reading_lessons").document(articleId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String t = doc.getString("title");
                    title.setText(t != null ? t : "Reading review");

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        image.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_article)
                                .error(R.drawable.placeholder_article)
                                .centerCrop()
                                .into(image);
                    } else {
                        image.setVisibility(View.GONE);
                    }

                    String passage = doc.getString("passage");
                    if (passage == null)
                        passage = doc.getString("content");
                    if (passage == null)
                        passage = "";

                    List<?> exercisesList = (List<?>) doc.get("exercises");
                    List<ReadingReviewItem> items = new ArrayList<>();
                    int total = 0;
                    int correctCount = 0;

                    if (exercisesList != null) {
                        total = exercisesList.size();
                        for (int i = 0; i < exercisesList.size(); i++) {
                            Object exerciseObj = exercisesList.get(i);
                            if (!(exerciseObj instanceof Map)) {
                                continue;
                            }

                            // noinspection unchecked
                            Map<String, Object> map = (Map<String, Object>) exerciseObj;

                            ReadingQuestion q = new ReadingQuestion();
                            q.setQuestionText((String) map.get("question"));
                            q.setCorrectAnswer((String) map.get("correctAnswer"));
                            q.setExplanation((String) map.get("explanation"));

                            // noinspection unchecked
                            q.setOptions((List<String>) map.get("options"));

                            String userAnswer = i < userAnswers.size() ? userAnswers.get(i) : null;
                            boolean isCorrect = userAnswer != null && q.checkAnswer(userAnswer);
                            if (isCorrect)
                                correctCount++;

                            String evidence = findBestEvidenceSentence(passage, q.getQuestionText(),
                                    q.getCorrectAnswer());

                            String explanation = q.getExplanation();
                            if (explanation == null || explanation.trim().isEmpty()) {
                                explanation = buildFallbackExplanation(q.getCorrectAnswer(), evidence);
                            }

                            items.add(new ReadingReviewItem(
                                    i,
                                    q.getQuestionText(),
                                    userAnswer,
                                    q.getCorrectAnswer(),
                                    isCorrect,
                                    explanation,
                                    evidence));
                        }
                    }

                    String summaryText;
                    if (total > 0) {
                        summaryText = String.format(Locale.getDefault(), "Score: %d%% • %d/%d correct", score,
                                correctCount, total);
                    } else {
                        summaryText = String.format(Locale.getDefault(), "Score: %d%%", score);
                    }
                    if (xpEarned > 0) {
                        summaryText += String.format(Locale.getDefault(), " • +%d XP", xpEarned);
                    }
                    summary.setText(summaryText);

                    recycler.setAdapter(new ReadingReviewAdapter(items));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load lesson", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private static String buildFallbackExplanation(String correctAnswer, String evidence) {
        if (evidence != null && !evidence.trim().isEmpty()) {
            return "The passage supports this answer: \"" + evidence.trim() + "\"";
        }
        if (correctAnswer != null && !correctAnswer.trim().isEmpty()) {
            return "The correct answer is \"" + correctAnswer.trim() + "\" based on the passage.";
        }
        return "This answer is supported by the passage.";
    }

    private static String findBestEvidenceSentence(String passage, String question, String correctAnswer) {
        if (passage == null)
            return "";
        String text = passage.trim();
        if (text.isEmpty())
            return "";

        String[] sentences = text.split("(?<=[.!?])\\s+|\\n+");
        if (sentences.length == 0)
            return "";

        Set<String> queryTokens = tokenize(question);
        queryTokens.addAll(tokenize(correctAnswer));

        int bestScore = -1;
        String best = sentences[0].trim();

        for (String s : sentences) {
            String candidate = s.trim();
            if (candidate.isEmpty())
                continue;
            int score = overlapScore(queryTokens, tokenize(candidate));
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private static int overlapScore(Set<String> a, Set<String> b) {
        int score = 0;
        for (String t : a) {
            if (b.contains(t))
                score++;
        }
        return score;
    }

    private static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null)
            return tokens;

        String cleaned = text.toLowerCase(Locale.getDefault())
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty())
            return tokens;

        String[] parts = cleaned.split(" ");
        for (String p : parts) {
            if (p.length() >= 3) {
                tokens.add(p);
            }
        }

        return tokens;
    }
}
