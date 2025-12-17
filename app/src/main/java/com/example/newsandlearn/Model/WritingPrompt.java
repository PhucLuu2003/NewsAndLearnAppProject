package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * WritingPrompt - Model for writing practice prompts
 * All prompts loaded from Firebase, NO hard-coded content
 */
public class WritingPrompt implements Parcelable {

    public enum PromptType {
        ESSAY,              // Long-form essay
        PARAGRAPH,          // Single paragraph
        EMAIL,              // Email writing
        LETTER,             // Formal/informal letter
        STORY,              // Creative writing
        DESCRIPTION,        // Descriptive writing
        ARGUMENT            // Argumentative writing
    }

    private String id;
    private String title;
    private String promptText;           // The writing prompt from Firebase
    private PromptType type;
    private String level;                // A1, A2, B1, B2, C1, C2
    private String category;
    
    // Guidelines
    private int minWords;
    private int maxWords;
    private int timeMinutes;
    private List<String> keyPoints;      // Points to cover
    private List<String> suggestedVocab; // Vocabulary suggestions
    private String sampleAnswer;         // Example answer from Firebase
    
    // Metadata
    private Date createdAt;
    private Date updatedAt;
    
    // User submission
    private String userText;             // User's writing
    private int wordCount;
    private Date submittedAt;
    private boolean completed;
    
    // Scoring
    private int grammarScore;
    private int vocabularyScore;
    private int coherenceScore;
    private int overallScore;
    private String feedback;             // AI or manual feedback

    public WritingPrompt() {
        this.keyPoints = new ArrayList<>();
        this.suggestedVocab = new ArrayList<>();
    }

    public WritingPrompt(String id, String title, String promptText, PromptType type) {
        this.id = id;
        this.title = title;
        this.promptText = promptText;
        this.type = type;
        this.keyPoints = new ArrayList<>();
        this.suggestedVocab = new ArrayList<>();
        this.completed = false;
        this.createdAt = new Date();
    }

    protected WritingPrompt(Parcel in) {
        id = in.readString();
        title = in.readString();
        promptText = in.readString();
        type = PromptType.valueOf(in.readString());
        level = in.readString();
        category = in.readString();
        minWords = in.readInt();
        maxWords = in.readInt();
        timeMinutes = in.readInt();
        keyPoints = in.createStringArrayList();
        suggestedVocab = in.createStringArrayList();
        sampleAnswer = in.readString();
        long createdLong = in.readLong();
        createdAt = createdLong != -1 ? new Date(createdLong) : null;
        long updatedLong = in.readLong();
        updatedAt = updatedLong != -1 ? new Date(updatedLong) : null;
        userText = in.readString();
        wordCount = in.readInt();
        long submittedLong = in.readLong();
        submittedAt = submittedLong != -1 ? new Date(submittedLong) : null;
        completed = in.readByte() != 0;
        grammarScore = in.readInt();
        vocabularyScore = in.readInt();
        coherenceScore = in.readInt();
        overallScore = in.readInt();
        feedback = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(promptText);
        dest.writeString(type.name());
        dest.writeString(level);
        dest.writeString(category);
        dest.writeInt(minWords);
        dest.writeInt(maxWords);
        dest.writeInt(timeMinutes);
        dest.writeStringList(keyPoints);
        dest.writeStringList(suggestedVocab);
        dest.writeString(sampleAnswer);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeString(userText);
        dest.writeInt(wordCount);
        dest.writeLong(submittedAt != null ? submittedAt.getTime() : -1);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(grammarScore);
        dest.writeInt(vocabularyScore);
        dest.writeInt(coherenceScore);
        dest.writeInt(overallScore);
        dest.writeString(feedback);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WritingPrompt> CREATOR = new Creator<WritingPrompt>() {
        @Override
        public WritingPrompt createFromParcel(Parcel in) {
            return new WritingPrompt(in);
        }

        @Override
        public WritingPrompt[] newArray(int size) {
            return new WritingPrompt[size];
        }
    };

    // Business logic
    public void submitWriting(String text) {
        this.userText = text;
        this.wordCount = text.split("\\s+").length;
        this.submittedAt = new Date();
        this.completed = true;
    }

    public void scoreWriting(int grammar, int vocabulary, int coherence) {
        this.grammarScore = grammar;
        this.vocabularyScore = vocabulary;
        this.coherenceScore = coherence;
        this.overallScore = (grammar + vocabulary + coherence) / 3;
    }

    public boolean meetsWordCount() {
        return wordCount >= minWords && wordCount <= maxWords;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public PromptType getType() { return type; }
    public void setType(PromptType type) { this.type = type; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getMinWords() { return minWords; }
    public void setMinWords(int minWords) { this.minWords = minWords; }

    public int getMaxWords() { return maxWords; }
    public void setMaxWords(int maxWords) { this.maxWords = maxWords; }

    public int getTimeMinutes() { return timeMinutes; }
    public void setTimeMinutes(int timeMinutes) { this.timeMinutes = timeMinutes; }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public List<String> getSuggestedVocab() { return suggestedVocab; }
    public void setSuggestedVocab(List<String> suggestedVocab) { this.suggestedVocab = suggestedVocab; }

    public String getSampleAnswer() { return sampleAnswer; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getUserText() { return userText; }
    public void setUserText(String userText) { this.userText = userText; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getGrammarScore() { return grammarScore; }
    public void setGrammarScore(int grammarScore) { this.grammarScore = grammarScore; }

    public int getVocabularyScore() { return vocabularyScore; }
    public void setVocabularyScore(int vocabularyScore) { this.vocabularyScore = vocabularyScore; }

    public int getCoherenceScore() { return coherenceScore; }
    public void setCoherenceScore(int coherenceScore) { this.coherenceScore = coherenceScore; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
