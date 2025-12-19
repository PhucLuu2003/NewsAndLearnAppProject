package com.example.newsandlearn.Model;

import java.io.Serializable;
import java.util.List;

/**
 * Model class for interactive questions in video lessons
 * Supports two types: DRAG_AND_DROP (Fill-in-the-blank) and MULTIPLE_CHOICE
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum QuestionType {
        DRAG_AND_DROP, // Fill-in-the-blank with drag and drop
        MULTIPLE_CHOICE, // Multiple choice with 3-4 options
        FILL_IN_THE_BLANK, // Fill-in-the-blank question
        TRUE_OR_FALSE, // True or False question
        OPEN_ENDED // Open ended question
    }

    private String id;
    private QuestionType type;
    private int appearAtSecond; // When to pause video and show question
    private String questionText; // The question or sentence with blank
    private String correctAnswer;
    private List<String> options; // For multiple choice or draggable words
    private String explanation; // Shown when answer is incorrect
    private int blankPosition; // Position of blank in drag-and-drop (0-based index)

    // Empty constructor for Firestore
    public Question() {
    }

    // Compatibility Setters for Seeder
    public void setTimestamp(int seconds) {
        this.appearAtSecond = seconds;
    }

    public void setQuestionType(String typeStr) {
        if (typeStr == null) return;
        switch (typeStr.toLowerCase()) {
            case "multiple_choice":
                this.type = QuestionType.MULTIPLE_CHOICE;
                break;
            case "true_false":
            case "true_or_false":
                this.type = QuestionType.TRUE_OR_FALSE;
                break;
            case "drag_and_drop":
                this.type = QuestionType.DRAG_AND_DROP;
                break;
            case "fill_in_the_blank":
                this.type = QuestionType.FILL_IN_THE_BLANK;
                break;
            case "open_ended":
                this.type = QuestionType.OPEN_ENDED;
                break;
            default:
                this.type = QuestionType.MULTIPLE_CHOICE;
        }
    }

    /**
     * Constructor for MULTIPLE_CHOICE questions
     */
    public Question(String id, int appearAtSecond, String questionText,
            String correctAnswer, List<String> options, String explanation) {
        this.id = id;
        this.type = QuestionType.MULTIPLE_CHOICE;
        this.appearAtSecond = appearAtSecond;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.explanation = explanation;
        this.blankPosition = -1;
    }

    /**
     * Constructor for DRAG_AND_DROP questions
     */
    public Question(String id, int appearAtSecond, String questionText,
            String correctAnswer, List<String> options, String explanation,
            int blankPosition) {
        this.id = id;
        this.type = QuestionType.DRAG_AND_DROP;
        this.appearAtSecond = appearAtSecond;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.explanation = explanation;
        this.blankPosition = blankPosition;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public int getAppearAtSecond() {
        return appearAtSecond;
    }

    public void setAppearAtSecond(int appearAtSecond) {
        this.appearAtSecond = appearAtSecond;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getBlankPosition() {
        return blankPosition;
    }

    public void setBlankPosition(int blankPosition) {
        this.blankPosition = blankPosition;
    }

    /**
     * Check if the given answer is correct
     */
    public boolean isCorrect(String answer) {
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer.trim());
    }

    /**
     * Get question text with blank marker for drag-and-drop
     * Returns array of [before_blank, after_blank]
     * Renamed to avoid being serialized by Firestore object mapper.
     */
    public String[] calculateSplitQuestionText() {
        if (type != QuestionType.DRAG_AND_DROP || questionText == null) {
            return new String[] { questionText, "" };
        }

        String[] words = questionText.split("\\s+");
        if (blankPosition < 0 || blankPosition >= words.length) {
            return new String[] { questionText, "" };
        }

        StringBuilder before = new StringBuilder();
        StringBuilder after = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i < blankPosition) {
                before.append(words[i]).append(" ");
            } else if (i > blankPosition) {
                after.append(words[i]).append(" ");
            }
        }

        return new String[] { before.toString().trim(), after.toString().trim() };
    }
}