package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * QuizQuestion Model - Represents a single quiz question
 */
public class QuizQuestion {
    
    private String vocabularyId;
    private String questionType; // "multiple_choice", "fill_blank", "true_false", "spelling"
    private String question;
    private String correctAnswer;
    private List<String> options; // For multiple choice
    private String userAnswer;
    private boolean isCorrect;
    private long timeTaken;

    // For display
    private transient VocabularyWithProgress vocabulary;

    public QuizQuestion() {
        this.options = new ArrayList<>();
    }

    public QuizQuestion(VocabularyWithProgress vocabulary, String questionType) {
        this.vocabulary = vocabulary;
        this.vocabularyId = vocabulary.getId();
        this.questionType = questionType;
        this.options = new ArrayList<>();
        generateQuestion();
    }

    /**
     * Generate question based on type
     */
    private void generateQuestion() {
        switch (questionType) {
            case "multiple_choice_translation":
                question = "What is the meaning of \"" + vocabulary.getWord() + "\"?";
                correctAnswer = vocabulary.getTranslation();
                break;
                
            case "multiple_choice_word":
                question = "Which word means \"" + vocabulary.getTranslation() + "\"?";
                correctAnswer = vocabulary.getWord();
                break;
                
            case "fill_blank":
                String example = vocabulary.getExample();
                if (example != null && example.contains(vocabulary.getWord())) {
                    question = example.replace(vocabulary.getWord(), "_____");
                    correctAnswer = vocabulary.getWord();
                } else {
                    question = "Complete: The word that means \"" + vocabulary.getTranslation() + "\" is _____";
                    correctAnswer = vocabulary.getWord();
                }
                break;
                
            case "spelling":
                question = "Spell the word that means: " + vocabulary.getTranslation();
                correctAnswer = vocabulary.getWord().toLowerCase();
                break;
                
            case "true_false":
                // Random true/false question
                if (Math.random() > 0.5) {
                    question = "\"" + vocabulary.getWord() + "\" means \"" + vocabulary.getTranslation() + "\"";
                    correctAnswer = "true";
                } else {
                    question = "\"" + vocabulary.getWord() + "\" means \"wrong translation\"";
                    correctAnswer = "false";
                }
                options.add("True");
                options.add("False");
                break;
        }
    }

    /**
     * Add options for multiple choice (call this after creating question)
     */
    public void addOptions(List<String> wrongOptions) {
        if (questionType.startsWith("multiple_choice")) {
            options.clear();
            options.add(correctAnswer);
            options.addAll(wrongOptions);
            Collections.shuffle(options);
        }
    }

    /**
     * Check if user's answer is correct
     */
    public boolean checkAnswer(String answer) {
        if (answer == null || correctAnswer == null) {
            isCorrect = false;
            return false;
        }

        userAnswer = answer;
        
        if (questionType.equals("spelling")) {
            isCorrect = answer.trim().toLowerCase().equals(correctAnswer.toLowerCase());
        } else {
            isCorrect = answer.trim().equals(correctAnswer.trim());
        }
        
        return isCorrect;
    }

    // Getters and Setters
    public String getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public long getTimeTaken() { return timeTaken; }
    public void setTimeTaken(long timeTaken) { this.timeTaken = timeTaken; }

    public VocabularyWithProgress getVocabulary() { return vocabulary; }
    public void setVocabulary(VocabularyWithProgress vocabulary) { this.vocabulary = vocabulary; }
}
