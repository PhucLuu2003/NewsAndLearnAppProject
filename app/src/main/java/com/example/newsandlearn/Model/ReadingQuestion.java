package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * ReadingQuestion - Comprehension question for reading articles
 * Loaded from Firebase, NO hard-coded questions
 */
public class ReadingQuestion implements Parcelable {

    private String id;
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String explanation;

    public ReadingQuestion() {}

    public ReadingQuestion(String id, String questionText) {
        this.id = id;
        this.questionText = questionText;
    }

    protected ReadingQuestion(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        options = in.createStringArrayList();
        correctAnswer = in.readString();
        explanation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeStringList(options);
        dest.writeString(correctAnswer);
        dest.writeString(explanation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReadingQuestion> CREATOR = new Creator<ReadingQuestion>() {
        @Override
        public ReadingQuestion createFromParcel(Parcel in) {
            return new ReadingQuestion(in);
        }

        @Override
        public ReadingQuestion[] newArray(int size) {
            return new ReadingQuestion[size];
        }
    };

    public boolean checkAnswer(String answer) {
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
