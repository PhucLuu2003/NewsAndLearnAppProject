package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * ListeningQuestion - Question for listening comprehension
 * Loaded from Firebase, NO hard-coded questions
 */
public class ListeningQuestion implements Parcelable {

    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        FILL_IN_BLANK,
        ORDERING
    }

    private String id;
    private String questionText;
    private QuestionType type;
    private List<String> options;      // For multiple choice
    private String correctAnswer;
    private String explanation;        // Why this is the correct answer
    private int timestampSeconds;      // When in audio this question relates to

    public ListeningQuestion() {
        // Required for Firebase
        this.options = new ArrayList<>();
    }

    public ListeningQuestion(String id, String questionText, QuestionType type) {
        this.id = id;
        this.questionText = questionText;
        this.type = type;
        this.options = new ArrayList<>();
    }

    protected ListeningQuestion(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        type = QuestionType.valueOf(in.readString());
        options = in.createStringArrayList();
        correctAnswer = in.readString();
        explanation = in.readString();
        timestampSeconds = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeString(type.name());
        dest.writeStringList(options);
        dest.writeString(correctAnswer);
        dest.writeString(explanation);
        dest.writeInt(timestampSeconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListeningQuestion> CREATOR = new Creator<ListeningQuestion>() {
        @Override
        public ListeningQuestion createFromParcel(Parcel in) {
            return new ListeningQuestion(in);
        }

        @Override
        public ListeningQuestion[] newArray(int size) {
            return new ListeningQuestion[size];
        }
    };

    public void addOption(String option) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
    }

    public boolean checkAnswer(String userAnswer) {
        if (correctAnswer == null || userAnswer == null) {
            return false;
        }
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getTimestampSeconds() { return timestampSeconds; }
    public void setTimestampSeconds(int timestampSeconds) { this.timestampSeconds = timestampSeconds; }
}
