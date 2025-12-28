package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ListeningQuestion implements Parcelable {

    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        FILL_IN_BLANK,
        ORDERING,
        SENTENCE_BUILDING
    }

    private String id;
    private String questionText;
    private QuestionType type;
    private String audioUrl;
    private String imageUrl; // <-- New field for question-specific image
    private List<String> options;      // For multiple choice, or distractors for SENTENCE_BUILDING
    private String correctAnswer;      // Correct choice for MC, or the full sentence for SENTENCE_BUILDING
    private String explanation;        // Why this is the correct answer
    private int timestampSeconds;      // When in audio this question relates to

    public ListeningQuestion() {
        this.options = new ArrayList<>();
    }

    protected ListeningQuestion(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        try {
            String typeName = in.readString();
            if (typeName != null) {
                type = QuestionType.valueOf(typeName);
            }
        } catch (IllegalArgumentException e) {
            type = QuestionType.MULTIPLE_CHOICE;
        }
        audioUrl = in.readString();
        imageUrl = in.readString();
        options = in.createStringArrayList();
        correctAnswer = in.readString();
        explanation = in.readString();
        timestampSeconds = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeString(type != null ? type.name() : "");
        dest.writeString(audioUrl);
        dest.writeString(imageUrl);
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

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getTimestampSeconds() { return timestampSeconds; }
    public void setTimestampSeconds(int timestampSeconds) { this.timestampSeconds = timestampSeconds; }
}
