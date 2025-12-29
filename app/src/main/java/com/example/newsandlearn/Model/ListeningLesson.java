package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListeningLesson implements Parcelable {

    private String id;
    private String title;
    private String description;
    private String audioUrl; 
    private String transcript;
    private int durationSeconds;
    private String level;
    private String category;
    private String thumbnailUrl;
    private int totalStages;

    private List<ListeningQuestion> questions;

    private Date createdAt;
    private boolean completed;
    private int userScore;
    private int timesListened;

    public ListeningLesson() {
        this.questions = new ArrayList<>();
    }

    protected ListeningLesson(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        audioUrl = in.readString();
        transcript = in.readString();
        durationSeconds = in.readInt();
        level = in.readString();
        category = in.readString();
        thumbnailUrl = in.readString();
        totalStages = in.readInt();
        questions = in.createTypedArrayList(ListeningQuestion.CREATOR);
        long createdAtLong = in.readLong();
        createdAt = createdAtLong != -1 ? new Date(createdAtLong) : null;
        completed = in.readByte() != 0;
        userScore = in.readInt();
        timesListened = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(audioUrl);
        dest.writeString(transcript);
        dest.writeInt(durationSeconds);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeString(thumbnailUrl);
        dest.writeInt(totalStages);
        dest.writeTypedList(questions);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(userScore);
        dest.writeInt(timesListened);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListeningLesson> CREATOR = new Creator<ListeningLesson>() {
        @Override
        public ListeningLesson createFromParcel(Parcel in) {
            return new ListeningLesson(in);
        }

        @Override
        public ListeningLesson[] newArray(int size) {
            return new ListeningLesson[size];
        }
    };

    // Helper Methods
    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public int getQuestionCount() {
        if (questions == null) {
            return 0;
        }
        return questions.size();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public int getTotalStages() { return totalStages; }
    public void setTotalStages(int totalStages) { this.totalStages = totalStages; }

    public List<ListeningQuestion> getQuestions() { return questions; }
    public void setQuestions(List<ListeningQuestion> questions) { this.questions = questions; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }

    public int getTimesListened() { return timesListened; }
    public void setTimesListened(int timesListened) { this.timesListened = timesListened; }
}
