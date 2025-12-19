package com.example.newsandlearn.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsandlearn.Model.DictionaryWord;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AIReadingCoach;
import com.example.newsandlearn.Utils.BionicReadingManager;
import com.example.newsandlearn.Utils.CollectionManager;
import com.example.newsandlearn.Utils.DictionaryAPI;
import com.example.newsandlearn.Utils.HighlightManager;
import com.example.newsandlearn.Utils.InteractiveQuizManager;
import com.example.newsandlearn.Utils.ProgressHelper;
import com.example.newsandlearn.Utils.ReadingAnalyticsManager;
import com.example.newsandlearn.Utils.RealtimeHighlightManager;
import com.example.newsandlearn.Utils.TTSManager;
import com.example.newsandlearn.Utils.TranslationAPI;
import com.example.newsandlearn.Utils.VocabularyHelper;
import com.example.newsandlearn.Utils.VocabularyMapGenerator;
import com.example.newsandlearn.Utils.VoiceFeedbackManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class EnhancedArticleDetailActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ImageView articleImage;
    private CircleImageView authorAvatar;
    private TextView categoryBadge, levelBadge, articleTitle;
    private TextView authorName, readingTime, publishDate, articleContent;
    private ImageView favoriteButton;
    private NestedScrollView scrollView;
    private ProgressBar readingProgress;
    private TextView progressText;
    private FloatingActionButton fabAddVocab;
    
    // New UI elements
    private ImageView btnTTS, btnSettings, btnCollections, btnAnalytics;
    private FloatingActionButton fabAICoach, fabBionicReading, fabVocabMap;

    private FirebaseFirestore db;
    private String articleId;
    private String articleLevel;
    private String articleCategory;
    private int currentProgress = 0;
    private long startTime;
    private String selectedText = "";
    private int selectionStart = 0;
    private int selectionEnd = 0;
    
    // Reading settings
    private float fontSize = 16f;
    private float lineSpacing = 1.5f;
    private String readingTheme = "light"; // light, dark, sepia
    private boolean isTTSPlaying = false;
    private boolean isFavorite = false;
    
    // New managers
    private AIReadingCoach aiCoach;
    private BionicReadingManager bionicManager;
    private VocabularyMapGenerator vocabMapGenerator;
    private RealtimeHighlightManager highlightManager;
    private VoiceFeedbackManager voiceFeedback;
    private InteractiveQuizManager quizManager;
    private boolean isBionicEnabled = false;
    private String fullArticleContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get article ID from intent
        articleId = getIntent().getStringExtra("article_id");

        // Initialize views
        initializeViews();
        setupToolbar();
        setupScrollListener();
        setupTextSelection();
        setupClickListeners();
        loadArticleData();
        
        // Initialize TTS
        TTSManager.getInstance().initialize(this, () -> {
            Toast.makeText(this, "Text-to-Speech ready", Toast.LENGTH_SHORT).show();
        });
        
        // Initialize new managers
        aiCoach = AIReadingCoach.getInstance();
        bionicManager = BionicReadingManager.getInstance();
        vocabMapGenerator = VocabularyMapGenerator.getInstance();
        highlightManager = RealtimeHighlightManager.getInstance();
        quizManager = InteractiveQuizManager.getInstance();
        bionicManager.setHighlightColor(getResources().getColor(R.color.primary));
        
        // Initialize voice feedback
        voiceFeedback = VoiceFeedbackManager.getInstance();
        voiceFeedback.initialize(this, new VoiceFeedbackManager.InitCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EnhancedArticleDetailActivity.this, "Voice feedback ready", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                // Silent fail
            }
        });

        // Track start time
        startTime = System.currentTimeMillis();
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        articleImage = findViewById(R.id.article_image);
        authorAvatar = findViewById(R.id.author_avatar);
        categoryBadge = findViewById(R.id.category_badge);
        levelBadge = findViewById(R.id.level_badge);
        articleTitle = findViewById(R.id.article_title);
        authorName = findViewById(R.id.author_name);
        readingTime = findViewById(R.id.reading_time);
        publishDate = findViewById(R.id.publish_date);
        articleContent = findViewById(R.id.article_content);
        favoriteButton = findViewById(R.id.favorite_button);
        scrollView = findViewById(R.id.scroll_view);
        readingProgress = findViewById(R.id.reading_progress);
        progressText = findViewById(R.id.progress_text);
        fabAddVocab = findViewById(R.id.fab_add_vocab);
        
        // New buttons
        btnTTS = findViewById(R.id.btn_tts);
        btnSettings = findViewById(R.id.btn_settings);
        btnCollections = findViewById(R.id.btn_collections);
        btnAnalytics = findViewById(R.id.btn_analytics);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // FAB for adding vocabulary
        fabAddVocab.setOnClickListener(v -> showAddVocabularyDialog());
        
        // TTS Button
        btnTTS.setOnClickListener(v -> toggleTTS());
        
        // Settings Button
        btnSettings.setOnClickListener(v -> showReadingSettings());
        
        // Collections Button
        btnCollections.setOnClickListener(v -> showCollectionsSheet());
        
        // Analytics Button
        btnAnalytics.setOnClickListener(v -> {
            startActivity(new Intent(this, ReadingAnalyticsActivity.class));
        });
        
        // Favorite Button
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    private void setupScrollListener() {
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Calculate reading progress
            int contentHeight = v.getChildAt(0).getHeight();
            int scrollViewHeight = v.getHeight();
            int maxScroll = contentHeight - scrollViewHeight;

            if (maxScroll > 0) {
                int progress = (int) ((scrollY / (float) maxScroll) * 100);
                updateProgress(progress);
            }
        });
    }

    private void setupTextSelection() {
        articleContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Add custom menu items
                menu.add(0, 1, 0, "Highlight")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(0, 2, 0, "Dictionary")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(0, 3, 0, "Translate")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(0, 4, 0, "Add to Vocab")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove default menu items
                menu.removeItem(android.R.id.selectAll);
                menu.removeItem(android.R.id.cut);
                menu.removeItem(android.R.id.paste);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int start = articleContent.getSelectionStart();
                int end = articleContent.getSelectionEnd();
                
                if (start >= 0 && end > start) {
                    selectedText = articleContent.getText().toString().substring(start, end);
                    selectionStart = start;
                    selectionEnd = end;
                    
                    switch (item.getItemId()) {
                        case 1: // Highlight
                            showHighlightSheet();
                            mode.finish();
                            return true;
                        case 2: // Dictionary
                            showDictionaryDialog(selectedText.trim());
                            mode.finish();
                            return true;
                        case 3: // Translate
                            translateText(selectedText);
                            mode.finish();
                            return true;
                        case 4: // Add to Vocabulary
                            showAddVocabularyDialog();
                            mode.finish();
                            return true;
                    }
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Clean up if needed
            }
        });
    }

    // ==================== TTS FUNCTIONALITY ====================
    
    private void toggleTTS() {
        if (isTTSPlaying) {
            stopTTS();
        } else {
            startTTS();
        }
    }
    
    private void startTTS() {
        String content = articleContent.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "No content to read", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TTSManager.getInstance().speak(content, new TTSManager.TTSCallback() {
            @Override
            public void onStart() {
                isTTSPlaying = true;
                btnTTS.setImageResource(R.drawable.ic_pause);
                Toast.makeText(EnhancedArticleDetailActivity.this, "🔊 Reading article...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone() {
                isTTSPlaying = false;
                btnTTS.setImageResource(R.drawable.ic_volume);
                Toast.makeText(EnhancedArticleDetailActivity.this, "✅ Finished reading", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                isTTSPlaying = false;
                btnTTS.setImageResource(R.drawable.ic_volume);
                Toast.makeText(EnhancedArticleDetailActivity.this, "❌ TTS error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void stopTTS() {
        TTSManager.getInstance().stop();
        isTTSPlaying = false;
        btnTTS.setImageResource(R.drawable.ic_volume);
    }

    // ==================== DICTIONARY FUNCTIONALITY ====================
    
    private void showDictionaryDialog(String word) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_dictionary);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView tvWord = dialog.findViewById(R.id.tv_word);
        TextView tvPhonetic = dialog.findViewById(R.id.tv_phonetic);
        TextView tvTranslation = dialog.findViewById(R.id.tv_translation);
        LinearLayout meaningsContainer = dialog.findViewById(R.id.meanings_container);
        LinearLayout translationContainer = dialog.findViewById(R.id.translation_container);
        ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);
        ImageView btnClose = dialog.findViewById(R.id.btn_close);
        ImageView btnSpeak = dialog.findViewById(R.id.btn_speak);
        MaterialButton btnAddVocab = dialog.findViewById(R.id.btn_add_vocab);

        tvWord.setText(word);
        progressBar.setVisibility(View.VISIBLE);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnSpeak.setOnClickListener(v -> TTSManager.getInstance().speakWord(word));
        btnAddVocab.setOnClickListener(v -> {
            selectedText = word;
            showAddVocabularyDialog();
            dialog.dismiss();
        });

        // Lookup word in dictionary
        DictionaryAPI.getInstance().lookupWord(word, new DictionaryAPI.DictionaryCallback() {
            @Override
            public void onSuccess(DictionaryWord dictionaryWord) {
                progressBar.setVisibility(View.GONE);
                
                if (dictionaryWord.getPhonetic() != null) {
                    tvPhonetic.setText(dictionaryWord.getPhonetic());
                }
                
                // Display meanings
                if (dictionaryWord.getMeanings() != null) {
                    for (DictionaryWord.Meaning meaning : dictionaryWord.getMeanings()) {
                        addMeaningView(meaningsContainer, meaning);
                    }
                }
                
                // Get Vietnamese translation
                translationContainer.setVisibility(View.VISIBLE);
                TranslationAPI.getInstance().translateToVietnamese(word, new TranslationAPI.TranslationCallback() {
                    @Override
                    public void onSuccess(String translatedText) {
                        tvTranslation.setText(translatedText);
                    }

                    @Override
                    public void onError(String error) {
                        tvTranslation.setText("Translation not available");
                    }
                });
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EnhancedArticleDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    
    private void addMeaningView(LinearLayout container, DictionaryWord.Meaning meaning) {
        View meaningView = getLayoutInflater().inflate(R.layout.item_meaning, container, false);
        
        TextView tvPartOfSpeech = meaningView.findViewById(R.id.tv_part_of_speech);
        LinearLayout definitionsContainer = meaningView.findViewById(R.id.definitions_container);
        
        tvPartOfSpeech.setText(meaning.getPartOfSpeech());
        
        if (meaning.getDefinitions() != null) {
            int count = 1;
            for (DictionaryWord.Definition definition : meaning.getDefinitions()) {
                if (count > 3) break; // Show max 3 definitions
                
                TextView tvDefinition = new TextView(this);
                tvDefinition.setText(count + ". " + definition.getDefinition());
                tvDefinition.setTextColor(getResources().getColor(R.color.text_primary));
                tvDefinition.setTextSize(14);
                tvDefinition.setPadding(0, 8, 0, 8);
                definitionsContainer.addView(tvDefinition);
                
                if (definition.getExample() != null && !definition.getExample().isEmpty()) {
                    TextView tvExample = new TextView(this);
                    tvExample.setText("   \"" + definition.getExample() + "\"");
                    tvExample.setTextColor(getResources().getColor(R.color.text_secondary));
                    tvExample.setTextSize(12);
                    tvExample.setTypeface(null, android.graphics.Typeface.ITALIC);
                    tvExample.setPadding(0, 4, 0, 12);
                    definitionsContainer.addView(tvExample);
                }
                
                count++;
            }
        }
        
        container.addView(meaningView);
    }

    // ==================== TRANSLATION FUNCTIONALITY ====================
    
    private void translateText(String text) {
        Toast.makeText(this, "Translating...", Toast.LENGTH_SHORT).show();
        
        TranslationAPI.getInstance().translateToVietnamese(text, new TranslationAPI.TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                showTranslationDialog(text, translatedText);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EnhancedArticleDetailActivity.this, "Translation error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showTranslationDialog(String original, String translation) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_translation);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView tvOriginal = dialog.findViewById(R.id.tv_original);
        TextView tvTranslation = dialog.findViewById(R.id.tv_translation);
        MaterialButton btnClose = dialog.findViewById(R.id.btn_close);
        
        tvOriginal.setText(original);
        tvTranslation.setText(translation);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    // ==================== HIGHLIGHT FUNCTIONALITY ====================
    
    private void showHighlightSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottom_sheet_highlight);
        
        LinearLayout colorYellow = bottomSheet.findViewById(R.id.color_yellow);
        LinearLayout colorGreen = bottomSheet.findViewById(R.id.color_green);
        LinearLayout colorBlue = bottomSheet.findViewById(R.id.color_blue);
        LinearLayout colorRed = bottomSheet.findViewById(R.id.color_red);
        EditText editNote = bottomSheet.findViewById(R.id.edit_note);
        MaterialButton btnCancel = bottomSheet.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = bottomSheet.findViewById(R.id.btn_save);
        
        final String[] selectedColor = {"yellow"};
        
        colorYellow.setOnClickListener(v -> selectedColor[0] = "yellow");
        colorGreen.setOnClickListener(v -> selectedColor[0] = "green");
        colorBlue.setOnClickListener(v -> selectedColor[0] = "blue");
        colorRed.setOnClickListener(v -> selectedColor[0] = "red");
        
        btnCancel.setOnClickListener(v -> bottomSheet.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String note = editNote.getText().toString();
            saveHighlight(selectedColor[0], note);
            bottomSheet.dismiss();
        });
        
        bottomSheet.show();
    }
    
    private void saveHighlight(String color, String note) {
        int colorInt = getColorForHighlight(color);
        
        // Apply highlight to text
        SpannableString spannableString = new SpannableString(articleContent.getText());
        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(colorInt);
        spannableString.setSpan(highlightSpan, selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        articleContent.setText(spannableString);
        
        // Save to Firebase
        HighlightManager.getInstance().saveHighlight(
            articleId, selectedText, selectionStart, selectionEnd, color, note,
            new HighlightManager.HighlightCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(EnhancedArticleDetailActivity.this, "✅ Highlight saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(EnhancedArticleDetailActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private int getColorForHighlight(String color) {
        switch (color) {
            case "yellow": return Color.YELLOW;
            case "green": return Color.parseColor("#8BC34A");
            case "blue": return Color.parseColor("#03A9F4");
            case "red": return Color.parseColor("#F44336");
            default: return Color.YELLOW;
        }
    }

    // ==================== READING SETTINGS ====================
    
    private void showReadingSettings() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottom_sheet_reading_settings);
        
        ChipGroup themeChipGroup = bottomSheet.findViewById(R.id.theme_chip_group);
        Slider fontSizeSlider = bottomSheet.findViewById(R.id.font_size_slider);
        Slider lineSpacingSlider = bottomSheet.findViewById(R.id.line_spacing_slider);
        Slider ttsSpeedSlider = bottomSheet.findViewById(R.id.tts_speed_slider);
        MaterialButton btnApply = bottomSheet.findViewById(R.id.btn_apply);
        
        // Set current values
        fontSizeSlider.setValue(fontSize);
        lineSpacingSlider.setValue(lineSpacing);
        ttsSpeedSlider.setValue(TTSManager.getInstance().getSpeechRate());
        
        btnApply.setOnClickListener(v -> {
            // Apply theme
            int selectedId = themeChipGroup.getCheckedChipId();
            if (selectedId == R.id.chip_light) {
                applyTheme("light");
            } else if (selectedId == R.id.chip_dark) {
                applyTheme("dark");
            } else if (selectedId == R.id.chip_sepia) {
                applyTheme("sepia");
            }
            
            // Apply font size
            fontSize = fontSizeSlider.getValue();
            articleContent.setTextSize(fontSize);
            
            // Apply line spacing
            lineSpacing = lineSpacingSlider.getValue();
            articleContent.setLineSpacing(0, lineSpacing);
            
            // Apply TTS speed
            TTSManager.getInstance().setSpeechRate(ttsSpeedSlider.getValue());
            
            Toast.makeText(this, "✅ Settings applied!", Toast.LENGTH_SHORT).show();
            bottomSheet.dismiss();
        });
        
        bottomSheet.show();
    }
    
    private void applyTheme(String theme) {
        readingTheme = theme;
        
        switch (theme) {
            case "light":
                articleContent.setBackgroundColor(Color.WHITE);
                articleContent.setTextColor(Color.parseColor("#212121"));
                break;
            case "dark":
                articleContent.setBackgroundColor(Color.parseColor("#212121"));
                articleContent.setTextColor(Color.WHITE);
                break;
            case "sepia":
                articleContent.setBackgroundColor(Color.parseColor("#F4ECD8"));
                articleContent.setTextColor(Color.parseColor("#5F4B32"));
                break;
        }
    }

    // ==================== COLLECTIONS FUNCTIONALITY ====================
    
    private void showCollectionsSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottom_sheet_collections);
        
        MaterialButton btnCreateCollection = bottomSheet.findViewById(R.id.btn_create_collection);
        
        btnCreateCollection.setOnClickListener(v -> {
            // TODO: Show create collection dialog
            Toast.makeText(this, "Create collection feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        bottomSheet.show();
    }
    
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
            favoriteButton.setColorFilter(Color.RED);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            favoriteButton.setColorFilter(getResources().getColor(R.color.text_secondary));
        }
        
        CollectionManager.getInstance().toggleFavorite(articleId, isFavorite, new CollectionManager.CollectionCallback() {
            @Override
            public void onSuccess() {
                String message = isFavorite ? "Added to favorites ❤️" : "Removed from favorites";
                Toast.makeText(EnhancedArticleDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(EnhancedArticleDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== VOCABULARY FUNCTIONALITY ====================
    
    private void showAddVocabularyDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_vocabulary);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        EditText editWord = dialog.findViewById(R.id.edit_word);
        EditText editMeaning = dialog.findViewById(R.id.edit_meaning);
        EditText editContext = dialog.findViewById(R.id.edit_context);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialog.findViewById(R.id.btn_save);

        // Pre-fill with selected text
        if (!selectedText.isEmpty()) {
            editWord.setText(selectedText);
            String fullText = articleContent.getText().toString();
            String context = extractSentence(fullText, selectionStart);
            editContext.setText(context);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String word = editWord.getText().toString().trim();
            String meaning = editMeaning.getText().toString().trim();
            String context = editContext.getText().toString().trim();

            if (word.isEmpty()) {
                editWord.setError("Please enter a word");
                return;
            }

            if (meaning.isEmpty()) {
                editMeaning.setError("Please enter the meaning");
                return;
            }

            // Add to vocabulary using VocabularyHelper
            VocabularyHelper.getInstance()
                    .addVocabularyFromArticle(word, meaning, articleId, articleLevel)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, "✅ " + result, Toast.LENGTH_SHORT).show();
                        ReadingAnalyticsManager.getInstance().trackVocabularyLearned();
                        dialog.dismiss();
                        selectedText = "";
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "❌ " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private String extractSentence(String text, int position) {
        int start = text.lastIndexOf('.', position);
        if (start == -1) start = 0;
        else start++;
        
        int end = text.indexOf('.', position);
        if (end == -1) end = text.length();
        else end++;
        
        return text.substring(start, end).trim();
    }

    // ==================== PROGRESS TRACKING ====================
    
    private void updateProgress(int progress) {
        if (progress > currentProgress) {
            currentProgress = progress;
            readingProgress.setProgress(progress);
            progressText.setText(progress + "%");

            // Update Firebase every 25%
            if (progress % 25 == 0 && progress > 0) {
                ProgressHelper.updateReadingProgress(articleId, progress);
            }

            // Mark as complete at 100%
            if (progress >= 100) {
                onArticleCompleted();
            }
        }
    }

    private void loadArticleData() {
        if (articleId == null) {
            Toast.makeText(this, "Error: Article not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("articles").document(articleId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String title = document.getString("title");
                        String content = document.getString("content");
                        String category = document.getString("category");
                        String level = document.getString("level");
                        String imageUrl = document.getString("imageUrl");
                        String source = document.getString("source");
                        String authorNameStr = document.getString("authorName");
                        String authorAvatarUrl = document.getString("authorAvatar");
                        Long readTime = document.getLong("readingTime");
                        com.google.firebase.Timestamp timestamp = document.getTimestamp("publishedDate");

                        // Store for analytics
                        articleLevel = level;
                        articleCategory = category;

                        // Set data
                        if (title != null) {
                            articleTitle.setText(title);
                            collapsingToolbar.setTitle(title);
                        }
                        if (content != null) {
                            articleContent.setText(content);
                            fullArticleContent = content; // Save for advanced features
                        }
                        if (category != null) categoryBadge.setText(category);
                        if (level != null) levelBadge.setText(level);
                        if (readTime != null) readingTime.setText(readTime + " min read");
                        if (timestamp != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                            publishDate.setText(sdf.format(timestamp.toDate()));
                        }

                        if (authorNameStr != null) {
                            authorName.setText(authorNameStr);
                        } else if (source != null) {
                            authorName.setText(source);
                        }

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.video_placeholder)
                                    .into(articleImage);
                        }

                        if (authorAvatarUrl != null && !authorAvatarUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(authorAvatarUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(authorAvatar);
                        }

                    } else {
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading article: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onArticleCompleted() {
        if (currentProgress == 100) {
            Toast.makeText(this, "🎉 Article completed! +5 XP", Toast.LENGTH_SHORT).show();
            
            // Track analytics
            long endTime = System.currentTimeMillis();
            int minutes = (int) ((endTime - startTime) / 60000);
            ReadingAnalyticsManager.getInstance().trackArticleRead(
                articleId, articleCategory, articleLevel, minutes
            );
        }
    }

    // ==================== NEW ADVANCED FEATURES ====================
    
    /**
     * Show AI Reading Coach
     */
    private void showAICoach() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(R.layout.bottom_sheet_ai_coach);
        
        TextView tvSentence = bottomSheet.findViewById(R.id.tv_analyzed_sentence);
        ProgressBar difficultyBar = bottomSheet.findViewById(R.id.difficulty_bar);
        TextView tvDifficultyScore = bottomSheet.findViewById(R.id.tv_difficulty_score);
        RecyclerView rvVocabulary = bottomSheet.findViewById(R.id.rv_vocabulary);
        LinearLayout grammarContainer = bottomSheet.findViewById(R.id.grammar_container);
        TextView tvLearningTip = bottomSheet.findViewById(R.id.tv_learning_tip);
        MaterialButton btnAnalyzeNext = bottomSheet.findViewById(R.id.btn_analyze_next);
        MaterialButton btnGetTips = bottomSheet.findViewById(R.id.btn_get_tips);
        ProgressBar progressAnalyzing = bottomSheet.findViewById(R.id.progress_analyzing);
        ImageView btnClose = bottomSheet.findViewById(R.id.btn_close_coach);
        
        // Set article context
        aiCoach.setArticleContext(fullArticleContent);
        
        // Analyze first sentence
        String[] sentences = fullArticleContent.split("[.!?]+");
        final int[] currentSentenceIndex = {0};
        
        if (sentences.length > 0) {
            analyzeSentenceWithAI(sentences[0].trim(), tvSentence, difficultyBar, 
                tvDifficultyScore, rvVocabulary, grammarContainer, tvLearningTip, progressAnalyzing);
        }
        
        btnAnalyzeNext.setOnClickListener(v -> {
            currentSentenceIndex[0]++;
            if (currentSentenceIndex[0] < sentences.length) {
                analyzeSentenceWithAI(sentences[currentSentenceIndex[0]].trim(), 
                    tvSentence, difficultyBar, tvDifficultyScore, rvVocabulary, 
                    grammarContainer, tvLearningTip, progressAnalyzing);
            } else {
                Toast.makeText(this, "✅ All sentences analyzed!", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnGetTips.setOnClickListener(v -> {
            progressAnalyzing.setVisibility(View.VISIBLE);
            aiCoach.getReadingTips(new AIReadingCoach.TipsCallback() {
                @Override
                public void onSuccess(List<AIReadingCoach.ReadingTip> tips) {
                    runOnUiThread(() -> {
                        progressAnalyzing.setVisibility(View.GONE);
                        showReadingTipsDialog(tips);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> {
                        progressAnalyzing.setVisibility(View.GONE);
                        Toast.makeText(EnhancedArticleDetailActivity.this, 
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        
        btnClose.setOnClickListener(v -> bottomSheet.dismiss());
        
        bottomSheet.show();
    }
    
    private void analyzeSentenceWithAI(String sentence, TextView tvSentence, 
            ProgressBar difficultyBar, TextView tvDifficultyScore, RecyclerView rvVocabulary,
            LinearLayout grammarContainer, TextView tvLearningTip, ProgressBar progressBar) {
        
        tvSentence.setText(sentence);
        progressBar.setVisibility(View.VISIBLE);
        
        // FEATURE 1: Real-time highlighting while analyzing
        int startIndex = fullArticleContent.indexOf(sentence);
        if (startIndex != -1) {
            highlightManager.highlightWithAnimation(articleContent, fullArticleContent,
                startIndex, startIndex + sentence.length(),
                new RealtimeHighlightManager.HighlightCallback() {
                    @Override
                    public void onHighlightStarted() {
                        // Highlight started
                    }
                });
        }
        
        aiCoach.analyzeSentence(sentence, new AIReadingCoach.AnalysisCallback() {
            @Override
            public void onSuccess(AIReadingCoach.SentenceAnalysis analysis) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    // Stop highlighting animation and show completion
                    if (startIndex != -1) {
                        highlightManager.showCompletionHighlight(articleContent, 
                            fullArticleContent, startIndex, startIndex + sentence.length());
                    }
                    
                    // Update difficulty
                    difficultyBar.setProgress(analysis.difficulty);
                    tvDifficultyScore.setText(analysis.difficulty + "/10");
                    
                    // Update learning tip
                    tvLearningTip.setText(analysis.tip);
                    
                    // Update grammar
                    grammarContainer.removeAllViews();
                    for (String grammar : analysis.grammar) {
                        TextView tvGrammar = new TextView(EnhancedArticleDetailActivity.this);
                        tvGrammar.setText("• " + grammar);
                        tvGrammar.setTextSize(14);
                        tvGrammar.setPadding(0, 8, 0, 8);
                        grammarContainer.addView(tvGrammar);
                    }
                    
                    // FEATURE 2: Voice feedback - speak analysis result
                    voiceFeedback.speakAnalysisResult(analysis, new VoiceFeedbackManager.SpeechCallback() {
                        @Override
                        public void onStart() {
                            // Voice started
                        }

                        @Override
                        public void onComplete() {
                            // FEATURE 3: Show interactive quiz after analysis
                            quizManager.showQuizFromAnalysis(
                                EnhancedArticleDetailActivity.this,
                                analysis,
                                sentence,
                                new InteractiveQuizManager.QuizCallback() {
                                    @Override
                                    public void onAnswered(boolean isCorrect) {
                                        if (isCorrect) {
                                            voiceFeedback.speakWithEmotion(
                                                "Great job! You got it right!",
                                                VoiceFeedbackManager.Emotion.EXCITED,
                                                null
                                            );
                                        } else {
                                            voiceFeedback.speakWithEmotion(
                                                "Don't worry, keep learning!",
                                                VoiceFeedbackManager.Emotion.ENCOURAGING,
                                                null
                                            );
                                        }
                                    }

                                    @Override
                                    public void onQuizComplete() {
                                        // Quiz completed
                                    }
                                }
                            );
                        }

                        @Override
                        public void onError(String error) {
                            // Voice error - still show quiz
                            quizManager.showQuizFromAnalysis(
                                EnhancedArticleDetailActivity.this,
                                analysis,
                                sentence,
                                null
                            );
                        }
                    });
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    highlightManager.stopAnimation();
                    Toast.makeText(EnhancedArticleDetailActivity.this, 
                        "Analysis error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showReadingTipsDialog(List<AIReadingCoach.ReadingTip> tips) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("💡 Reading Tips");
        
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < tips.size(); i++) {
            AIReadingCoach.ReadingTip tip = tips.get(i);
            message.append(tip.icon).append(" ").append(tip.title).append("\n");
            message.append(tip.description).append("\n\n");
        }
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("Got it!", null);
        builder.show();
    }
    
    /**
     * Toggle Bionic Reading Mode
     */
    private void toggleBionicReading() {
        isBionicEnabled = !isBionicEnabled;
        
        if (isBionicEnabled) {
            // Apply bionic reading
            SpannableString bionicText = bionicManager.applyBionicReading(fullArticleContent, 2);
            articleContent.setText(bionicText);
            Toast.makeText(this, "⚡ Bionic Reading ON - Read 30% faster!", Toast.LENGTH_SHORT).show();
        } else {
            // Restore normal text
            articleContent.setText(fullArticleContent);
            Toast.makeText(this, "Bionic Reading OFF", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show Vocabulary Map
     */
    private void showVocabularyMap() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.card_word_cloud, null);
        bottomSheet.setContentView(view);
        
        FrameLayout wordCloudContainer = view.findViewById(R.id.word_cloud_container);
        TextView tvTotalWords = view.findViewById(R.id.tv_total_words);
        TextView tvUniqueWords = view.findViewById(R.id.tv_unique_words);
        TextView tvDiversity = view.findViewById(R.id.tv_diversity);
        ImageView btnRefresh = view.findViewById(R.id.btn_refresh_cloud);
        
        // Generate word cloud
        generateWordCloud(wordCloudContainer, tvTotalWords, tvUniqueWords, tvDiversity);
        
        btnRefresh.setOnClickListener(v -> {
            wordCloudContainer.removeAllViews();
            generateWordCloud(wordCloudContainer, tvTotalWords, tvUniqueWords, tvDiversity);
        });
        
        bottomSheet.show();
    }
    
    private void generateWordCloud(FrameLayout container, TextView tvTotal, 
            TextView tvUnique, TextView tvDiversity) {
        
        // Get statistics
        VocabularyMapGenerator.VocabularyStats stats = 
            vocabMapGenerator.getStatistics(fullArticleContent);
        
        tvTotal.setText(String.valueOf(stats.totalWords));
        tvUnique.setText(String.valueOf(stats.uniqueWords));
        tvDiversity.setText(String.format("%.0f%%", stats.vocabularyDiversity * 100));
        
        // Generate word cloud items
        List<VocabularyMapGenerator.WordCloudItem> cloudItems = 
            vocabMapGenerator.generateWordCloud(fullArticleContent, 30);
        
        // Add words to container
        Random random = new Random();
        for (VocabularyMapGenerator.WordCloudItem item : cloudItems) {
            TextView wordView = new TextView(this);
            wordView.setText(item.word);
            wordView.setTextSize(item.size);
            wordView.setTextColor(Color.parseColor(item.color));
            wordView.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Random position
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.leftMargin = random.nextInt(container.getWidth() > 0 ? 
                container.getWidth() - 100 : 300);
            params.topMargin = random.nextInt(250);
            wordView.setLayoutParams(params);
            
            // Click listener
            wordView.setOnClickListener(v -> {
                showDictionaryDialog(item.word);
            });
            
            container.addView(wordView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Stop TTS
        if (isTTSPlaying) {
            stopTTS();
        }
        
        // Track reading time
        long endTime = System.currentTimeMillis();
        int minutes = (int) ((endTime - startTime) / 60000);
        if (minutes > 0) {
            ProgressHelper.updateStudyTime(minutes);
        }

        // Save final progress
        if (currentProgress > 0) {
            ProgressHelper.updateReadingProgress(articleId, currentProgress);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup
        if (isTTSPlaying) {
            stopTTS();
        }
        
        // Cleanup new managers
        if (highlightManager != null) {
            highlightManager.stopAnimation();
        }
        if (voiceFeedback != null) {
            voiceFeedback.shutdown();
        }
        if (quizManager != null) {
            quizManager.dismissCurrentQuiz();
        }
    }
}
