package com.example.newsandlearn.Activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsandlearn.Model.LearningStory;
import com.example.newsandlearn.Model.StoryGameResult;
import com.example.newsandlearn.Model.StoryTopic;
import com.example.newsandlearn.Model.VocabularyWord;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.StoryGenerator;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoryGameActivity extends AppCompatActivity {

    // UI Components
    private ViewFlipper viewFlipper;
    private FrameLayout loadingOverlay;
    private TextView loadingText;
    
    // Topic Selection
    private GridLayout topicsGrid;
    private MaterialCardView customTopicCard;
    private EditText customTopicInput;
    private Button confirmCustomTopicBtn;
    
    // Learning
    private TextView storyTitle;
    private LinearLayout vocabularyList;
    private TextView storyContent;
    private Button listenStoryBtn;
    private Button generateNewStoryBtn;
    private Button startPracticeBtn;
    
    // Practice
    private LinearLayout storyBlanksContainer;
    private LinearLayout wordBank;
    private Button checkAnswersBtn;
    private Button retryBtn;
    private TextView timerText;
    
    // Results
    private TextView celebrationIcon;
    private TextView congratulationsText;
    private TextView correctCount;
    private TextView timeSpent;
    private TextView accuracyPercent;
    private TextView xpEarned;
    private LinearLayout learnedWordsList;
    private Button playAgainBtn;
    private Button newTopicBtn;
    
    // Game State
    private LearningStory currentStory;
    private StoryGameResult gameResult;
    private Map<Integer, String> userAnswers = new HashMap<>();
    private long startTime;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int elapsedSeconds = 0;
    
    // Progress
    private ProgressBar bottomProgressBar;
    private TextView progressPercentage;
    private TextView progressLabel;
    
    // TTS
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.fragment_story_game);
        
        initializeViews();
        initializeTTS();
        setupTopicSelection();
        updateProgress(0); // Start at 0%
    }

    private void initializeViews() {
        viewFlipper = findViewById(R.id.view_flipper);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);
        
        // Topic Selection
        topicsGrid = findViewById(R.id.topics_grid);
        customTopicCard = findViewById(R.id.custom_topic_card);
        customTopicInput = findViewById(R.id.custom_topic_input);
        confirmCustomTopicBtn = findViewById(R.id.confirm_custom_topic_btn);
        
        // Learning
        storyTitle = findViewById(R.id.story_title);
        vocabularyList = findViewById(R.id.vocabulary_list);
        storyContent = findViewById(R.id.story_content);
        listenStoryBtn = findViewById(R.id.listen_story_btn);
        generateNewStoryBtn = findViewById(R.id.generate_new_story_btn);
        startPracticeBtn = findViewById(R.id.start_practice_btn);
        
        // Practice
        storyBlanksContainer = findViewById(R.id.story_blanks_container);
        wordBank = findViewById(R.id.word_bank);
        checkAnswersBtn = findViewById(R.id.check_answers_btn);
        retryBtn = findViewById(R.id.retry_btn);
        timerText = findViewById(R.id.timer_text);
        
        // Results
        celebrationIcon = findViewById(R.id.celebration_icon);
        congratulationsText = findViewById(R.id.congratulations_text);
        correctCount = findViewById(R.id.correct_count);
        timeSpent = findViewById(R.id.time_spent);
        accuracyPercent = findViewById(R.id.accuracy_percent);
        xpEarned = findViewById(R.id.xp_earned);
        learnedWordsList = findViewById(R.id.learned_words_list);
        playAgainBtn = findViewById(R.id.play_again_btn);
        newTopicBtn = findViewById(R.id.new_topic_btn);
        
        // Progress
        bottomProgressBar = findViewById(R.id.bottom_progress_bar);
        progressPercentage = findViewById(R.id.progress_percentage);
        progressLabel = findViewById(R.id.progress_label);
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.75f); // Slower for clearer pronunciation
                tts.setPitch(1.0f);
            }
        });
    }

    private void setupTopicSelection() {
        // Back button
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        
        StoryTopic[] topics = StoryTopic.getDefaultTopics();
        
        for (int i = 0; i < topics.length; i++) {
            StoryTopic topic = topics[i];
            View topicCard = LayoutInflater.from(this)
                    .inflate(R.layout.item_topic_card, topicsGrid, false);
            
            TextView emojiView = topicCard.findViewById(R.id.topic_emoji);
            TextView nameView = topicCard.findViewById(R.id.topic_name);
            TextView descView = topicCard.findViewById(R.id.topic_description);
            
            emojiView.setText(topic.getEmoji());
            nameView.setText(topic.getName());
            descView.setText(topic.getDescription());
            
            // Set GridLayout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(8, 8, 8, 8);
            topicCard.setLayoutParams(params);
            
            if (topic.isCustom()) {
                topicCard.setOnClickListener(v -> showCustomTopicInput());
            } else {
                topicCard.setOnClickListener(v -> onTopicSelected(topic.getId()));
            }
            
            topicsGrid.addView(topicCard);
        }
        
        confirmCustomTopicBtn.setOnClickListener(v -> {
            String customTopic = customTopicInput.getText().toString().trim();
            if (!customTopic.isEmpty()) {
                onTopicSelected(customTopic.toLowerCase());
            } else {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomTopicInput() {
        customTopicCard.setVisibility(View.VISIBLE);
        customTopicInput.requestFocus();
    }

    private void onTopicSelected(String topic) {
        showLoading(true, "Generating story...");
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentStory = StoryGenerator.generateStory(topic);
            showLoading(false, "");
            showLearningScreen();
        }, 1500);
    }

    private void showLearningScreen() {
        storyTitle.setText(currentStory.getTitle());
        storyContent.setText(highlightVocabulary(currentStory.getFullStory()));
        
        vocabularyList.removeAllViews();
        for (VocabularyWord word : currentStory.getVocabularyWords()) {
            View wordCard = LayoutInflater.from(this)
                    .inflate(R.layout.item_vocabulary_word, vocabularyList, false);
            
            TextView wordView = wordCard.findViewById(R.id.vocab_word);
            TextView pronView = wordCard.findViewById(R.id.vocab_pronunciation);
            TextView meaningView = wordCard.findViewById(R.id.vocab_meaning);
            View speakerIcon = wordCard.findViewById(R.id.speaker_icon);
            
            wordView.setText(word.getWord());
            pronView.setText(word.getPronunciation());
            meaningView.setText("→ " + word.getMeaning());
            
            speakerIcon.setOnClickListener(v -> {
                // Visual feedback
                v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start());
                speakWord(word.getWord());
            });
            
            vocabularyList.addView(wordCard);
        }
        
        listenStoryBtn.setOnClickListener(v -> speakText(currentStory.getFullStory()));
        
        // Generate new story button
        generateNewStoryBtn.setOnClickListener(v -> {
            showLoading(true, "Generating new story...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                currentStory = StoryGenerator.generateStory(currentStory.getTopic());
                showLoading(false, "");
                showLearningScreen(); // Refresh with new story
                Toast.makeText(this, "✨ New story generated!", Toast.LENGTH_SHORT).show();
            }, 1000);
        });
        
        startPracticeBtn.setOnClickListener(v -> showPracticeScreen());
        
        viewFlipper.setDisplayedChild(1);
        updateProgress(33); // Learning step
        animateViewEntry(vocabularyList);
    }

    private SpannableString highlightVocabulary(String text) {
        SpannableString spannableString = new SpannableString(text);
        for (VocabularyWord word : currentStory.getVocabularyWords()) {
            int start = text.toLowerCase().indexOf(word.getWord().toLowerCase());
            if (start != -1) {
                int end = start + word.getWord().length();
                spannableString.setSpan(
                    new ForegroundColorSpan(0xFF6C63FF),
                    start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return spannableString;
    }

    private void showPracticeScreen() {
        startTime = System.currentTimeMillis();
        startTimer();
        
        setupPracticeUI();
        viewFlipper.setDisplayedChild(2);
        updateProgress(66); // Practice step
        animateViewEntry(storyBlanksContainer);
    }

    private void setupPracticeUI() {
        storyBlanksContainer.removeAllViews();
        userAnswers.clear();
        
        String[] parts = currentStory.getStoryWithBlanks().split("___");
        List<VocabularyWord> words = currentStory.getVocabularyWords();
        
        for (int i = 0; i < parts.length; i++) {
            // Add story text
            TextView textView = new TextView(this);
            textView.setText(parts[i]);
            textView.setTextSize(15);
            textView.setTextColor(0xFF666666);
            textView.setLineSpacing(4, 1);
            storyBlanksContainer.addView(textView);
            
            // Add blank slot for drag-drop
            if (i < words.size()) {
                final int index = i;
                VocabularyWord word = words.get(i);
                
                // Create drop zone
                FrameLayout dropZone = new FrameLayout(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 12, 0, 12);
                dropZone.setLayoutParams(params);
                dropZone.setPadding(16, 16, 16, 16);
                dropZone.setBackgroundResource(R.drawable.stat_card_background);
                dropZone.setTag("drop_zone_" + index);
                
                // Placeholder text
                TextView placeholder = new TextView(this);
                placeholder.setText("💡 " + word.getMeaning());
                placeholder.setTextSize(14);
                placeholder.setTextColor(0xFF999999);
                placeholder.setGravity(android.view.Gravity.CENTER);
                placeholder.setTag("placeholder_" + index);
                dropZone.addView(placeholder);
                
                // Set up drop listener
                dropZone.setOnDragListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.DragEvent.ACTION_DRAG_STARTED:
                            return true;
                            
                        case android.view.DragEvent.ACTION_DRAG_ENTERED:
                            v.setBackgroundColor(0xFFE1F5FE);
                            return true;
                            
                        case android.view.DragEvent.ACTION_DRAG_EXITED:
                            v.setBackgroundResource(R.drawable.stat_card_background);
                            return true;
                            
                        case android.view.DragEvent.ACTION_DROP:
                            String droppedWord = event.getClipData().getItemAt(0).getText().toString();
                            
                            // Remove placeholder
                            FrameLayout dropFrame = (FrameLayout) v;
                            for (int j = 0; j < dropFrame.getChildCount(); j++) {
                                View child = dropFrame.getChildAt(j);
                                if (child.getTag() != null && child.getTag().toString().startsWith("placeholder_")) {
                                    dropFrame.removeView(child);
                                    break;
                                }
                            }
                            
                            // Add dropped word
                            TextView droppedText = new TextView(this);
                            droppedText.setText(droppedWord);
                            droppedText.setTextSize(16);
                            droppedText.setTextColor(0xFF6C63FF);
                            droppedText.setTypeface(null, android.graphics.Typeface.BOLD);
                            droppedText.setGravity(android.view.Gravity.CENTER);
                            droppedText.setTag("dropped_word_" + index);
                            dropFrame.addView(droppedText);
                            
                            // Save answer
                            userAnswers.put(index, droppedWord);
                            
                            v.setBackgroundResource(R.drawable.stat_card_background);
                            return true;
                            
                        case android.view.DragEvent.ACTION_DRAG_ENDED:
                            v.setBackgroundResource(R.drawable.stat_card_background);
                            return true;
                    }
                    return false;
                });
                
                storyBlanksContainer.addView(dropZone);
            }
        }
        
        // Setup word bank with drag functionality
        wordBank.removeAllViews();
        for (VocabularyWord word : words) {
            com.google.android.material.button.MaterialButton wordBtn = 
                new com.google.android.material.button.MaterialButton(this);
            wordBtn.setText(word.getWord());
            wordBtn.setBackgroundColor(0xFFE1BEE7);
            wordBtn.setTextColor(0xFF6C63FF);
            wordBtn.setCornerRadius(24);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            wordBtn.setLayoutParams(params);
            
            // Set up drag listener
            wordBtn.setOnLongClickListener(v -> {
                android.content.ClipData.Item item = new android.content.ClipData.Item(word.getWord());
                android.content.ClipData dragData = new android.content.ClipData(
                    word.getWord(),
                    new String[]{android.content.ClipDescription.MIMETYPE_TEXT_PLAIN},
                    item
                );
                
                android.view.View.DragShadowBuilder shadowBuilder = new android.view.View.DragShadowBuilder(v);
                v.startDragAndDrop(dragData, shadowBuilder, v, 0);
                
                // Visual feedback
                v.setAlpha(0.5f);
                v.setScaleX(0.9f);
                v.setScaleY(0.9f);
                
                Toast.makeText(this, "Drag to blank above 👆", Toast.LENGTH_SHORT).show();
                return true;
            });
            
            // Reset when drag ends
            wordBtn.setOnDragListener((v, event) -> {
                if (event.getAction() == android.view.DragEvent.ACTION_DRAG_ENDED) {
                    v.setAlpha(1.0f);
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }
                return true;
            });
            
            wordBank.addView(wordBtn);
        }
        
        checkAnswersBtn.setOnClickListener(v -> checkAnswers());
        retryBtn.setOnClickListener(v -> setupPracticeUI());
    }

    private void startTimer() {
        elapsedSeconds = 0;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                int minutes = elapsedSeconds / 60;
                int seconds = elapsedSeconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void checkAnswers() {
        stopTimer();
        
        int correct = 0;
        int wrong = 0;
        List<VocabularyWord> words = currentStory.getVocabularyWords();
        
        for (int i = 0; i < words.size(); i++) {
            String correctAnswer = words.get(i).getWord();
            String userAnswer = userAnswers.get(i);
            
            if (userAnswer != null && userAnswer.equalsIgnoreCase(correctAnswer)) {
                correct++;
            } else {
                wrong++;
            }
        }
        
        if (correct == words.size()) {
            showResultsScreen(correct, wrong);
        } else {
            Toast.makeText(this, "❌ " + wrong + " wrong. Try again!", Toast.LENGTH_SHORT).show();
            retryBtn.setVisibility(View.VISIBLE);
            animateShake(checkAnswersBtn);
        }
    }

    private void showResultsScreen(int correct, int wrong) {
        gameResult = new StoryGameResult(
            currentStory.getId(),
            currentStory.getTopic(),
            currentStory.getWordCount(),
            correct,
            wrong,
            elapsedSeconds
        );
        
        List<String> learnedWords = new ArrayList<>();
        for (VocabularyWord word : currentStory.getVocabularyWords()) {
            learnedWords.add(word.getWord());
        }
        gameResult.setLearnedWords(learnedWords);
        
        celebrationIcon.setText(gameResult.isPerfectScore() ? "🎉" : "👏");
        congratulationsText.setText(gameResult.isPerfectScore() ? 
                "Perfect Score!" : "Good Job!");
        
        correctCount.setText(correct + "/" + gameResult.getTotalWords());
        timeSpent.setText(gameResult.getFormattedTime());
        accuracyPercent.setText(gameResult.getAccuracyPercentage() + "%");
        xpEarned.setText(gameResult.getXpEarned() + " XP");
        
        learnedWordsList.removeAllViews();
        for (String word : learnedWords) {
            TextView wordView = new TextView(this);
            wordView.setText("• " + word);
            wordView.setTextSize(14);
            wordView.setTextColor(0xFF6C63FF);
            wordView.setPadding(12, 4, 12, 4);
            learnedWordsList.addView(wordView);
        }
        
        playAgainBtn.setOnClickListener(v -> {
            onTopicSelected(currentStory.getTopic());
        });
        
        newTopicBtn.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(0);
            customTopicCard.setVisibility(View.GONE);
            customTopicInput.setText("");
        });
        
        viewFlipper.setDisplayedChild(3);
        updateProgress(100); // Complete!
        animateViewEntry(findViewById(R.id.celebration_icon));
    }

    private void speakWord(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void speakText(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void showLoading(boolean show, String message) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingText.setText(message);
    }

    private void animateViewEntry(View view) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateShake(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 
                0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(500);
        animator.start();
    }
    
    private void updateProgress(int percentage) {
        if (bottomProgressBar != null) {
            bottomProgressBar.setProgress(percentage);
            progressPercentage.setText(percentage + "%");
            
            // Update label based on step
            String label = "Progress";
            if (percentage == 0) label = "Select Topic";
            else if (percentage == 33) label = "Learning";
            else if (percentage == 66) label = "Practice";
            else if (percentage == 100) label = "Complete!";
            
            progressLabel.setText(label);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopTimer();
    }
}
