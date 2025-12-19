package com.example.newsandlearn.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interactive Quiz Manager
 * Shows quiz popups while reading with instant feedback
 */
public class InteractiveQuizManager {
    private static InteractiveQuizManager instance;
    
    private Dialog currentQuizDialog;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    
    private InteractiveQuizManager() {}
    
    public static synchronized InteractiveQuizManager getInstance() {
        if (instance == null) {
            instance = new InteractiveQuizManager();
        }
        return instance;
    }
    
    /**
     * Show quiz based on AI analysis
     */
    public void showQuizFromAnalysis(Activity activity, 
                                    AIReadingCoach.SentenceAnalysis analysis,
                                    String sentence,
                                    QuizCallback callback) {
        
        // Generate quiz question from analysis
        QuizQuestion question = generateQuestionFromAnalysis(analysis, sentence);
        
        if (question != null) {
            showQuizDialog(activity, question, callback);
        }
    }
    
    /**
     * Generate quiz question from sentence analysis
     */
    private QuizQuestion generateQuestionFromAnalysis(
            AIReadingCoach.SentenceAnalysis analysis, String sentence) {
        
        QuizQuestion question = new QuizQuestion();
        
        // If has vocabulary, create vocabulary quiz
        if (!analysis.vocabulary.isEmpty()) {
            AIReadingCoach.VocabWord word = analysis.vocabulary.get(0);
            
            question.question = "What does '" + word.word + "' mean in this context?";
            question.correctAnswer = word.definition;
            
            // Generate wrong answers
            question.options = new ArrayList<>();
            question.options.add(word.definition); // Correct answer
            question.options.add("A type of food");
            question.options.add("A place or location");
            question.options.add("An action or movement");
            
            Collections.shuffle(question.options);
            question.type = QuizType.VOCABULARY;
            
        } else if (!analysis.grammar.isEmpty()) {
            // Grammar quiz
            question.question = "What grammar structure is used in this sentence?";
            question.correctAnswer = analysis.grammar.get(0);
            
            question.options = new ArrayList<>();
            question.options.add(analysis.grammar.get(0));
            question.options.add("Simple present tense");
            question.options.add("Past continuous");
            question.options.add("Future perfect");
            
            Collections.shuffle(question.options);
            question.type = QuizType.GRAMMAR;
            
        } else {
            // Comprehension quiz
            question.question = "What is the main idea of this sentence?";
            question.correctAnswer = analysis.tip;
            question.type = QuizType.COMPREHENSION;
        }
        
        return question;
    }
    
    /**
     * Show quiz dialog with beautiful UI
     */
    private void showQuizDialog(Activity activity, QuizQuestion question, 
                               QuizCallback callback) {
        
        currentQuizDialog = new Dialog(activity);
        currentQuizDialog.setContentView(R.layout.dialog_interactive_quiz);
        currentQuizDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        currentQuizDialog.getWindow().setLayout(
            (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.9),
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        // Setup views
        TextView tvQuestion = currentQuizDialog.findViewById(R.id.tv_quiz_question);
        LinearLayout optionsContainer = currentQuizDialog.findViewById(R.id.options_container);
        TextView tvFeedback = currentQuizDialog.findViewById(R.id.tv_feedback);
        MaterialButton btnNext = currentQuizDialog.findViewById(R.id.btn_next);
        
        tvQuestion.setText(question.question);
        tvFeedback.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        
        // Add option buttons
        final boolean[] answered = {false};
        
        for (String option : question.options) {
            MaterialButton optionBtn = new MaterialButton(activity);
            optionBtn.setText(option);
            optionBtn.setCornerRadius(12);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            optionBtn.setLayoutParams(params);
            
            optionBtn.setOnClickListener(v -> {
                if (answered[0]) return;
                
                answered[0] = true;
                totalQuestions++;
                
                boolean isCorrect = option.equals(question.correctAnswer);
                
                if (isCorrect) {
                    correctAnswers++;
                    optionBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
                    tvFeedback.setText("✅ Correct! " + question.correctAnswer);
                    tvFeedback.setTextColor(Color.parseColor("#4CAF50"));
                    
                    // Play success sound
                    VoiceFeedbackManager.getInstance().playSuccessSound();
                    
                } else {
                    optionBtn.setBackgroundColor(Color.parseColor("#F44336"));
                    tvFeedback.setText("❌ Incorrect. The answer is: " + question.correctAnswer);
                    tvFeedback.setTextColor(Color.parseColor("#F44336"));
                    
                    // Highlight correct answer
                    for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                        View child = optionsContainer.getChildAt(i);
                        if (child instanceof MaterialButton) {
                            MaterialButton btn = (MaterialButton) child;
                            if (btn.getText().toString().equals(question.correctAnswer)) {
                                btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                            }
                        }
                    }
                    
                    // Play error sound
                    VoiceFeedbackManager.getInstance().playErrorSound();
                }
                
                tvFeedback.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
                
                // Disable all buttons
                for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                    optionsContainer.getChildAt(i).setEnabled(false);
                }
                
                if (callback != null) {
                    callback.onAnswered(isCorrect);
                }
            });
            
            optionsContainer.addView(optionBtn);
        }
        
        btnNext.setOnClickListener(v -> {
            currentQuizDialog.dismiss();
            if (callback != null) {
                callback.onQuizComplete();
            }
        });
        
        currentQuizDialog.show();
    }
    
    /**
     * Show quick quiz (True/False)
     */
    public void showQuickQuiz(Activity activity, String statement, 
                             boolean correctAnswer, QuizCallback callback) {
        
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_quick_quiz);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView tvStatement = dialog.findViewById(R.id.tv_statement);
        MaterialButton btnTrue = dialog.findViewById(R.id.btn_true);
        MaterialButton btnFalse = dialog.findViewById(R.id.btn_false);
        
        tvStatement.setText(statement);
        
        View.OnClickListener answerListener = v -> {
            boolean userAnswer = v.getId() == R.id.btn_true;
            boolean isCorrect = userAnswer == correctAnswer;
            
            totalQuestions++;
            if (isCorrect) {
                correctAnswers++;
                Toast.makeText(activity, "✅ Correct!", Toast.LENGTH_SHORT).show();
                VoiceFeedbackManager.getInstance().playSuccessSound();
            } else {
                Toast.makeText(activity, "❌ Incorrect!", Toast.LENGTH_SHORT).show();
                VoiceFeedbackManager.getInstance().playErrorSound();
            }
            
            dialog.dismiss();
            
            if (callback != null) {
                callback.onAnswered(isCorrect);
                callback.onQuizComplete();
            }
        };
        
        btnTrue.setOnClickListener(answerListener);
        btnFalse.setOnClickListener(answerListener);
        
        dialog.show();
    }
    
    /**
     * Get quiz statistics
     */
    public QuizStats getStats() {
        QuizStats stats = new QuizStats();
        stats.totalQuestions = totalQuestions;
        stats.correctAnswers = correctAnswers;
        stats.accuracy = totalQuestions > 0 ? 
            (correctAnswers * 100.0 / totalQuestions) : 0;
        return stats;
    }
    
    /**
     * Reset statistics
     */
    public void resetStats() {
        correctAnswers = 0;
        totalQuestions = 0;
    }
    
    /**
     * Dismiss current quiz
     */
    public void dismissCurrentQuiz() {
        if (currentQuizDialog != null && currentQuizDialog.isShowing()) {
            currentQuizDialog.dismiss();
        }
    }
    
    // ==================== DATA CLASSES ====================
    
    public static class QuizQuestion {
        public String question;
        public List<String> options;
        public String correctAnswer;
        public QuizType type;
    }
    
    public enum QuizType {
        VOCABULARY,
        GRAMMAR,
        COMPREHENSION
    }
    
    public static class QuizStats {
        public int totalQuestions;
        public int correctAnswers;
        public double accuracy;
    }
    
    public interface QuizCallback {
        void onAnswered(boolean isCorrect);
        void onQuizComplete();
    }
}
