package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * VocabularyDetailActivity - Hiển thị chi tiết từ vựng
 * Bao gồm: nghĩa, phiên âm, ví dụ, loại từ, level, và các tính năng tương tác
 */
public class VocabularyDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView favoriteButton, speakerButton;
    private TextView wordText, phoneticText, partOfSpeechText, levelBadge;
    private TextView vietnameseMeaningText, exampleText, synonymsText, antonymsText;
    private MaterialCardView exampleCard, synonymsCard, antonymsCard;
    private MaterialButton markAsLearnedButton;

    private Vocabulary vocabulary;
    private String vocabularyId;
    private boolean isFavorite = false;
    private boolean isLearned = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_detail);

        // Get vocabulary ID from intent
        vocabularyId = getIntent().getStringExtra("vocabulary_id");
        if (vocabularyId == null) {
            Toast.makeText(this, "Error: No vocabulary ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeServices();
        initializeViews();
        setupToolbar();
        setupListeners();
        setupTextToSpeech();
        loadVocabularyDetail();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        favoriteButton = findViewById(R.id.favorite_button);
        speakerButton = findViewById(R.id.speaker_button);
        
        wordText = findViewById(R.id.word_text);
        phoneticText = findViewById(R.id.phonetic_text);
        partOfSpeechText = findViewById(R.id.part_of_speech_text);
        levelBadge = findViewById(R.id.level_badge);
        
        vietnameseMeaningText = findViewById(R.id.vietnamese_meaning_text);
        exampleText = findViewById(R.id.example_text);
        synonymsText = findViewById(R.id.synonyms_text);
        antonymsText = findViewById(R.id.antonyms_text);
        
        exampleCard = findViewById(R.id.example_card);
        synonymsCard = findViewById(R.id.synonyms_card);
        antonymsCard = findViewById(R.id.antonyms_card);
        
        markAsLearnedButton = findViewById(R.id.mark_as_learned_button);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vocabulary Detail");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        speakerButton.setOnClickListener(v -> {
            if (vocabulary != null && vocabulary.getWord() != null) {
                speakWord(vocabulary.getWord());
            }
        });

        favoriteButton.setOnClickListener(v -> toggleFavorite());

        markAsLearnedButton.setOnClickListener(v -> markAsLearned());
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void loadVocabularyDetail() {
        // Load vocabulary from public collection
        db.collection("vocabularies").document(vocabularyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    vocabulary = documentSnapshot.toObject(Vocabulary.class);
                    if (vocabulary != null) {
                        displayVocabulary();
                        loadUserProgress();
                    } else {
                        Toast.makeText(this, "Vocabulary not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadUserProgress() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("user_vocabulary").document(vocabularyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean favorite = documentSnapshot.getBoolean("isFavorite");
                        Boolean learned = documentSnapshot.getBoolean("isLearned");
                        
                        if (favorite != null) {
                            isFavorite = favorite;
                            updateFavoriteButton();
                        }
                        
                        if (learned != null) {
                            isLearned = learned;
                            updateLearnedButton();
                        }
                    }
                });
    }

    private void displayVocabulary() {
        // Word and phonetic
        wordText.setText(vocabulary.getWord());
        
        if (vocabulary.getPronunciation() != null && !vocabulary.getPronunciation().isEmpty()) {
            phoneticText.setText("/" + vocabulary.getPronunciation() + "/");
            phoneticText.setVisibility(View.VISIBLE);
        } else {
            phoneticText.setVisibility(View.GONE);
        }

        // Part of speech
        if (vocabulary.getPartOfSpeech() != null) {
            partOfSpeechText.setText(vocabulary.getPartOfSpeech());
            partOfSpeechText.setVisibility(View.VISIBLE);
        } else {
            partOfSpeechText.setVisibility(View.GONE);
        }

        // Level badge
        if (vocabulary.getLevel() != null) {
            levelBadge.setText(vocabulary.getLevel());
            levelBadge.setVisibility(View.VISIBLE);
        } else {
            levelBadge.setVisibility(View.GONE);
        }

        // Vietnamese meaning (translation)
        if (vocabulary.getTranslation() != null) {
            vietnameseMeaningText.setText(vocabulary.getTranslation());
        }

        // Example
        if (vocabulary.getExample() != null && !vocabulary.getExample().isEmpty()) {
            exampleText.setText(vocabulary.getExample());
            exampleCard.setVisibility(View.VISIBLE);
        } else {
            exampleCard.setVisibility(View.GONE);
        }

        // Synonyms
        if (vocabulary.getSynonyms() != null && !vocabulary.getSynonyms().isEmpty()) {
            synonymsText.setText(String.join(", ", vocabulary.getSynonyms()));
            synonymsCard.setVisibility(View.VISIBLE);
        } else {
            synonymsCard.setVisibility(View.GONE);
        }

        // Antonyms
        if (vocabulary.getAntonyms() != null && !vocabulary.getAntonyms().isEmpty()) {
            antonymsText.setText(String.join(", ", vocabulary.getAntonyms()));
            antonymsCard.setVisibility(View.VISIBLE);
        } else {
            antonymsCard.setVisibility(View.GONE);
        }
    }

    private void speakWord(String word) {
        if (tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void toggleFavorite() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavorite = !isFavorite;
        updateFavoriteButton();

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("isFavorite", isFavorite);
        updates.put("vocabularyId", vocabularyId);

        db.collection("users").document(userId)
                .collection("user_vocabulary").document(vocabularyId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    String message = isFavorite ? "Added to favorites ❤️" : "Removed from favorites";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isFavorite = !isFavorite;
                    updateFavoriteButton();
                });
    }

    private void markAsLearned() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        isLearned = !isLearned;
        updateLearnedButton();

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("isLearned", isLearned);
        updates.put("vocabularyId", vocabularyId);
        updates.put("masteryLevel", isLearned ? "Mastered" : "Learning");

        db.collection("users").document(userId)
                .collection("user_vocabulary").document(vocabularyId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    String message = isLearned ? "✅ Marked as learned!" : "Marked as learning";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLearned = !isLearned;
                    updateLearnedButton();
                });
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void updateLearnedButton() {
        if (isLearned) {
            markAsLearnedButton.setText("✅ Learned");
            markAsLearnedButton.setBackgroundColor(getColor(R.color.success));
        } else {
            markAsLearnedButton.setText("Mark as Learned");
            markAsLearnedButton.setBackgroundColor(getColor(R.color.primary));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
