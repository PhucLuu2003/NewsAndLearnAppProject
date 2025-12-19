package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.newsandlearn.Model.QuizQuestion;
import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.Model.VocabularyQuiz;
import com.example.newsandlearn.Model.VocabularyWithProgress;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.VocabularyHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuizActivity - Vocabulary quiz with multiple question types
 */
public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    // UI Components
    private ImageView backButton;
    private TextView progressText, questionNumberText, questionText, timerText;
    private ProgressBar quizProgress;
    private CardView questionCard;
    
    // Multiple choice
    private RadioGroup optionsGroup;
    private LinearLayout optionsContainer;
    
    // Fill blank / Spelling
    private EditText answerInput;
    
    // Buttons
    private MaterialButton submitButton, nextButton;
    
    // Results
    private LinearLayout resultsLayout;
    private TextView scoreText, accuracyText, gradeText, timeText;
    private MaterialButton retryButton, finishButton;

    // Data
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private VocabularyQuiz quiz;
    private long questionStartTime;
    private long quizStartTime;

    // Services
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private VocabularyHelper vocabularyHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeServices();
        initializeViews();
        loadQuizData();
        setupListeners();
    }

    private void initializeServices() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vocabularyHelper = VocabularyHelper.getInstance();
        questions = new ArrayList<>();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        progressText = findViewById(R.id.progress_text);
        questionNumberText = findViewById(R.id.question_number_text);
        quizProgress = findViewById(R.id.quiz_progress);
        questionCard = findViewById(R.id.question_card);
        questionText = findViewById(R.id.question_text);
        timerText = findViewById(R.id.timer_text);
        
        optionsGroup = findViewById(R.id.options_group);
        optionsContainer = findViewById(R.id.options_container);
        answerInput = findViewById(R.id.answer_input);
        
        submitButton = findViewById(R.id.submit_button);
        nextButton = findViewById(R.id.next_button);
        
        resultsLayout = findViewById(R.id.results_layout);
        scoreText = findViewById(R.id.score_text);
        accuracyText = findViewById(R.id.accuracy_text);
        gradeText = findViewById(R.id.grade_text);
        timeText = findViewById(R.id.time_text);
        retryButton = findViewById(R.id.retry_button);
        finishButton = findViewById(R.id.finish_button);
    }

    private void loadQuizData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String type = getIntent().getStringExtra("quiz_type");
        if (type == null) type = "multiple_choice_translation";
        final String quizType = type;
        
        int questionCount = getIntent().getIntExtra("question_count", 10);

        // Load user's vocabulary
        db.collection("users").document(userId)
                .collection("user_vocabulary")
                .limit(questionCount * 2) // Load more to have options
                .get()
                .addOnSuccessListener(userVocabSnapshot -> {
                    List<String> vocabIds = new ArrayList<>();
                    Map<String, UserVocabulary> progressMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : userVocabSnapshot) {
                        UserVocabulary userVocab = doc.toObject(UserVocabulary.class);
                        if (userVocab != null && userVocab.getVocabularyId() != null) {
                            vocabIds.add(userVocab.getVocabularyId());
                            progressMap.put(userVocab.getVocabularyId(), userVocab);
                        }
                    }

                    if (vocabIds.isEmpty()) {
                        Toast.makeText(this, "No vocabulary to quiz", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Load vocabulary details
                    loadVocabularyDetails(vocabIds, progressMap, quizType, questionCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading vocabulary", e);
                    Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadVocabularyDetails(List<String> vocabIds, Map<String, UserVocabulary> progressMap, 
                                      String quizType, int questionCount) {
        List<VocabularyWithProgress> allVocabulary = new ArrayList<>();
        
        // Batch loading
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < vocabIds.size(); i += 10) {
            batches.add(vocabIds.subList(i, Math.min(i + 10, vocabIds.size())));
        }

        final int[] completedBatches = {0};

        for (List<String> batch : batches) {
            db.collection("vocabularies")
                    .whereIn(FieldPath.documentId(), batch)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Vocabulary vocab = doc.toObject(Vocabulary.class);
                            if (vocab != null) {
                                vocab.setId(doc.getId());
                                UserVocabulary progress = progressMap.get(vocab.getId());

                                VocabularyWithProgress item = new VocabularyWithProgress();
                                item.setVocabulary(vocab);
                                item.setUserProgress(progress);
                                allVocabulary.add(item);
                            }
                        }

                        completedBatches[0]++;
                        if (completedBatches[0] == batches.size()) {
                            // All batches loaded
                            generateQuiz(allVocabulary, quizType, questionCount);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading vocabulary details", e);
                        Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void generateQuiz(List<VocabularyWithProgress> vocabulary, String quizType, int questionCount) {
        Collections.shuffle(vocabulary);
        
        List<String> quizVocabIds = new ArrayList<>();
        int count = Math.min(questionCount, vocabulary.size());
        
        for (int i = 0; i < count; i++) {
            VocabularyWithProgress vocab = vocabulary.get(i);
            quizVocabIds.add(vocab.getId());
            
            QuizQuestion question = new QuizQuestion(vocab, quizType);
            
            // Add wrong options for multiple choice
            if (quizType.startsWith("multiple_choice")) {
                List<String> wrongOptions = new ArrayList<>();
                for (int j = 0; j < vocabulary.size() && wrongOptions.size() < 3; j++) {
                    if (j != i) {
                        String option = quizType.equals("multiple_choice_translation") ?
                                vocabulary.get(j).getTranslation() : vocabulary.get(j).getWord();
                        if (!wrongOptions.contains(option)) {
                            wrongOptions.add(option);
                        }
                    }
                }
                question.addOptions(wrongOptions);
            }
            
            questions.add(question);
        }

        // Initialize quiz
        quiz = new VocabularyQuiz(quizType, quizVocabIds);
        quiz.setUserId(auth.getCurrentUser().getUid());
        
        quizStartTime = System.currentTimeMillis();
        showQuestion(0);
    }

    private void showQuestion(int index) {
        if (index >= questions.size()) {
            showResults();
            return;
        }

        currentQuestionIndex = index;
        QuizQuestion question = questions.get(currentQuestionIndex);
        questionStartTime = System.currentTimeMillis();

        // Update progress
        progressText.setText((index + 1) + "/" + questions.size());
        questionNumberText.setText("Question " + (index + 1));
        quizProgress.setMax(questions.size());
        quizProgress.setProgress(index + 1);

        // Show question
        questionText.setText(question.getQuestion());

        // Setup answer UI based on question type
        setupAnswerUI(question);

        // Show/hide buttons
        submitButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
    }

    private void setupAnswerUI(QuizQuestion question) {
        String type = question.getQuestionType();
        
        if (type.startsWith("multiple_choice") || type.equals("true_false")) {
            // Show radio buttons
            optionsContainer.setVisibility(View.VISIBLE);
            answerInput.setVisibility(View.GONE);
            optionsGroup.removeAllViews();
            
            for (String option : question.getOptions()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(option);
                radioButton.setTextSize(16);
                radioButton.setPadding(16, 16, 16, 16);
                optionsGroup.addView(radioButton);
            }
        } else {
            // Show text input
            optionsContainer.setVisibility(View.GONE);
            answerInput.setVisibility(View.VISIBLE);
            answerInput.setText("");
            answerInput.setHint("Type your answer here");
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        submitButton.setOnClickListener(v -> checkAnswer());

        nextButton.setOnClickListener(v -> {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        });

        retryButton.setOnClickListener(v -> {
            // Restart quiz
            recreate();
        });

        finishButton.setOnClickListener(v -> finish());
    }

    private void checkAnswer() {
        QuizQuestion question = questions.get(currentQuestionIndex);
        String userAnswer = null;

        if (question.getQuestionType().startsWith("multiple_choice") || 
            question.getQuestionType().equals("true_false")) {
            int selectedId = optionsGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selected = findViewById(selectedId);
            userAnswer = selected.getText().toString();
        } else {
            userAnswer = answerInput.getText().toString().trim();
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        long timeTaken = System.currentTimeMillis() - questionStartTime;
        boolean isCorrect = question.checkAnswer(userAnswer);
        question.setTimeTaken(timeTaken);

        // Add result to quiz
        quiz.addResult(question.getVocabularyId(), isCorrect, timeTaken);

        // Update vocabulary progress
        if (isCorrect) {
            vocabularyHelper.markCorrect(question.getVocabularyId());
        } else {
            vocabularyHelper.markIncorrect(question.getVocabularyId());
        }

        // Show feedback
        showFeedback(isCorrect, question.getCorrectAnswer());

        // Show next button
        submitButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void showFeedback(boolean isCorrect, String correctAnswer) {
        if (isCorrect) {
            Toast.makeText(this, "✓ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "✗ Wrong! Correct answer: " + correctAnswer, Toast.LENGTH_LONG).show();
        }
    }

    private void showResults() {
        quiz.complete();
        
        // Save quiz to Firebase
        saveQuizResults();

        // Hide question UI
        questionCard.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);

        // Show results
        resultsLayout.setVisibility(View.VISIBLE);
        scoreText.setText(quiz.getCorrectAnswers() + "/" + quiz.getTotalQuestions());
        accuracyText.setText(quiz.getScore() + "%");
        gradeText.setText(quiz.getGrade());
        
        long seconds = quiz.getTimeSpent() / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timeText.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void saveQuizResults() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("quiz_history")
                .add(quiz)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "Quiz saved: " + docRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving quiz", e);
                });
    }
}
