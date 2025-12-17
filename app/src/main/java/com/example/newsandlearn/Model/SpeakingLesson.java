package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SpeakingLesson - Model for speaking practice lessons
 * All data loaded from Firebase, NO hard-coded content
 */
public class SpeakingLesson implements Parcelable {

    public enum LessonType {
        PRONUNCIATION,      // Practice specific sounds
        CONVERSATION,       // Dialog practice
        PRESENTATION,       // Monologue practice
        STORYTELLING,       // Narrative practice
        DEBATE              // Argumentative practice
    }

    private String id;
    private String title;
    private String description;
    private LessonType type;
    private String level;              // A1, A2, B1, B2, C1, C2
    private String category;
    
    // Content
    private List<SpeakingPrompt> prompts;
    private String sampleAudioUrl;     // Example pronunciation from Firebase Storage
    private String instructionsText;
    private String targetPhrase;       // What user should say
    
    // Metadata
    private int estimatedMinutes;
    private String focusArea;          // pronunciation, fluency, vocabulary, etc.
    private Date createdAt;
    private Date updatedAt;
    
    // User progress
    private boolean completed;
    private int pronunciationScore;    // 0-100
    private int fluencyScore;          // 0-100
    private int overallScore;          // 0-100
    private int attemptCount;
    private Date lastAttemptAt;
    private String recordingUrl;       // User's last recording URL

    public SpeakingLesson() {
        // Required for Firebase
        this.prompts = new ArrayList<>();
    }

    public SpeakingLesson(String id, String title, String description, LessonType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.prompts = new ArrayList<>();
        this.completed = false;
        this.attemptCount = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    protected SpeakingLesson(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        type = LessonType.valueOf(in.readString());
        level = in.readString();
        category = in.readString();
        prompts = in.createTypedArrayList(SpeakingPrompt.CREATOR);
        sampleAudioUrl = in.readString();
        instructionsText = in.readString();
        targetPhrase = in.readString();
        estimatedMinutes = in.readInt();
        focusArea = in.readString();
        long createdAtLong = in.readLong();
        createdAt = createdAtLong != -1 ? new Date(createdAtLong) : null;
        long updatedAtLong = in.readLong();
        updatedAt = updatedAtLong != -1 ? new Date(updatedAtLong) : null;
        completed = in.readByte() != 0;
        pronunciationScore = in.readInt();
        fluencyScore = in.readInt();
        overallScore = in.readInt();
        attemptCount = in.readInt();
        long lastAttemptLong = in.readLong();
        lastAttemptAt = lastAttemptLong != -1 ? new Date(lastAttemptLong) : null;
        recordingUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(type.name());
        dest.writeString(level);
        dest.writeString(category);
        dest.writeTypedList(prompts);
        dest.writeString(sampleAudioUrl);
        dest.writeString(instructionsText);
        dest.writeString(targetPhrase);
        dest.writeInt(estimatedMinutes);
        dest.writeString(focusArea);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(pronunciationScore);
        dest.writeInt(fluencyScore);
        dest.writeInt(overallScore);
        dest.writeInt(attemptCount);
        dest.writeLong(lastAttemptAt != null ? lastAttemptAt.getTime() : -1);
        dest.writeString(recordingUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpeakingLesson> CREATOR = new Creator<SpeakingLesson>() {
        @Override
        public SpeakingLesson createFromParcel(Parcel in) {
            return new SpeakingLesson(in);
        }

        @Override
        public SpeakingLesson[] newArray(int size) {
            return new SpeakingLesson[size];
        }
    };

    // Business logic
    public void addPrompt(SpeakingPrompt prompt) {
        if (prompts == null) {
            prompts = new ArrayList<>();
        }
        prompts.add(prompt);
    }

    public void recordAttempt(int pronScore, int fluScore) {
        this.attemptCount++;
        this.pronunciationScore = pronScore;
        this.fluencyScore = fluScore;
        this.overallScore = (pronScore + fluScore) / 2;
        this.lastAttemptAt = new Date();
        
        // Mark completed if score is good enough
        if (overallScore >= 70) {
            this.completed = true;
        }
    }

    public int getPromptCount() {
        return prompts != null ? prompts.size() : 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LessonType getType() { return type; }
    public void setType(LessonType type) { this.type = type; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<SpeakingPrompt> getPrompts() { return prompts; }
    public void setPrompts(List<SpeakingPrompt> prompts) { this.prompts = prompts; }

    public String getSampleAudioUrl() { return sampleAudioUrl; }
    public void setSampleAudioUrl(String sampleAudioUrl) { this.sampleAudioUrl = sampleAudioUrl; }

    public String getInstructionsText() { return instructionsText; }
    public void setInstructionsText(String instructionsText) { this.instructionsText = instructionsText; }

    public String getTargetPhrase() { return targetPhrase; }
    public void setTargetPhrase(String targetPhrase) { this.targetPhrase = targetPhrase; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getFocusArea() { return focusArea; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getPronunciationScore() { return pronunciationScore; }
    public void setPronunciationScore(int pronunciationScore) { this.pronunciationScore = pronunciationScore; }

    public int getFluencyScore() { return fluencyScore; }
    public void setFluencyScore(int fluencyScore) { this.fluencyScore = fluencyScore; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public Date getLastAttemptAt() { return lastAttemptAt; }
    public void setLastAttemptAt(Date lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }

    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }
}
