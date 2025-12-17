package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ListeningLesson - Model for listening practice lessons
 * All data loaded from Firebase, NO hard-coded content
 */
public class ListeningLesson implements Parcelable {

    private String id;
    private String title;
    private String description;
    private String audioUrl;           // Firebase Storage URL
    private String transcriptUrl;      // Optional transcript file URL
    private String transcript;         // Full transcript text
    private int durationSeconds;
    private String level;              // A1, A2, B1, B2, C1, C2
    private String category;           // conversation, news, story, podcast, etc.
    private String thumbnailUrl;       // Cover image URL
    
    // Questions
    private List<ListeningQuestion> questions;
    
    // Metadata
    private String speaker;            // Native speaker name
    private String accent;             // British, American, Australian, etc.
    private Date createdAt;
    private Date updatedAt;
    
    // User progress
    private boolean completed;
    private int userScore;             // 0-100
    private int timesListened;
    private Date lastListenedAt;

    public ListeningLesson() {
        // Required empty constructor for Firebase
        this.questions = new ArrayList<>();
    }

    public ListeningLesson(String id, String title, String description, String audioUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.audioUrl = audioUrl;
        this.questions = new ArrayList<>();
        this.completed = false;
        this.userScore = 0;
        this.timesListened = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Parcelable implementation
    protected ListeningLesson(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        audioUrl = in.readString();
        transcriptUrl = in.readString();
        transcript = in.readString();
        durationSeconds = in.readInt();
        level = in.readString();
        category = in.readString();
        thumbnailUrl = in.readString();
        questions = in.createTypedArrayList(ListeningQuestion.CREATOR);
        speaker = in.readString();
        accent = in.readString();
        long createdAtLong = in.readLong();
        createdAt = createdAtLong != -1 ? new Date(createdAtLong) : null;
        long updatedAtLong = in.readLong();
        updatedAt = updatedAtLong != -1 ? new Date(updatedAtLong) : null;
        completed = in.readByte() != 0;
        userScore = in.readInt();
        timesListened = in.readInt();
        long lastListenedLong = in.readLong();
        lastListenedAt = lastListenedLong != -1 ? new Date(lastListenedLong) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(audioUrl);
        dest.writeString(transcriptUrl);
        dest.writeString(transcript);
        dest.writeInt(durationSeconds);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeString(thumbnailUrl);
        dest.writeTypedList(questions);
        dest.writeString(speaker);
        dest.writeString(accent);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(userScore);
        dest.writeInt(timesListened);
        dest.writeLong(lastListenedAt != null ? lastListenedAt.getTime() : -1);
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

    // Business logic
    public void addQuestion(ListeningQuestion question) {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        questions.add(question);
    }

    public void incrementListenCount() {
        this.timesListened++;
        this.lastListenedAt = new Date();
    }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
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

    public String getTranscriptUrl() { return transcriptUrl; }
    public void setTranscriptUrl(String transcriptUrl) { this.transcriptUrl = transcriptUrl; }

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

    public List<ListeningQuestion> getQuestions() { return questions; }
    public void setQuestions(List<ListeningQuestion> questions) { this.questions = questions; }

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }

    public String getAccent() { return accent; }
    public void setAccent(String accent) { this.accent = accent; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }

    public int getTimesListened() { return timesListened; }
    public void setTimesListened(int timesListened) { this.timesListened = timesListened; }

    public Date getLastListenedAt() { return lastListenedAt; }
    public void setLastListenedAt(Date lastListenedAt) { this.lastListenedAt = lastListenedAt; }
}
