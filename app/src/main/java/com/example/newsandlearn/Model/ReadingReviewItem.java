package com.example.newsandlearn.Model;

public class ReadingReviewItem {
    private final int index;
    private final String questionText;
    private final String userAnswer;
    private final String correctAnswer;
    private final boolean correct;
    private final String explanation;
    private final String evidence;

    public ReadingReviewItem(
            int index,
            String questionText,
            String userAnswer,
            String correctAnswer,
            boolean correct,
            String explanation,
            String evidence) {
        this.index = index;
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.explanation = explanation;
        this.evidence = evidence;
    }

    public int getIndex() {
        return index;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getEvidence() {
        return evidence;
    }
}
