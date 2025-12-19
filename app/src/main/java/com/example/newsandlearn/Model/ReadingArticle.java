package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ReadingArticle - Model for reading comprehension articles
 * All content loaded from Firebase, NO hard-coded articles
 */
public class ReadingArticle implements Parcelable {

    private String id;
    private String title;
    private String content;              // Full article text from Firebase
    private String passage;              // Reading passage (alternative to content)
    private String summary;
    private String imageUrl;             // Cover image from Firebase Storage
    private String level;                // A1, A2, B1, B2, C1, C2
    private String category;             // news, story, science, culture, etc.
    private int wordCount;
    private int estimatedMinutes;
    
    // Comprehension questions
    private List<ReadingQuestion> questions;
    
    // Vocabulary highlights
    private List<String> keyVocabulary;  // Important words to learn
    
    // Metadata
    private String author;
    private String source;
    private Date publishedAt;
    private Date createdAt;
    
    // User progress
    private boolean completed;
    private int userScore;
    private int readCount;
    private Date lastReadAt;
    private List<String> highlightedWords;  // Words user highlighted
    private String userNotes;               // User's notes

    public ReadingArticle() {
        this.questions = new ArrayList<>();
        this.keyVocabulary = new ArrayList<>();
        this.highlightedWords = new ArrayList<>();
    }

    public ReadingArticle(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.questions = new ArrayList<>();
        this.keyVocabulary = new ArrayList<>();
        this.highlightedWords = new ArrayList<>();
        this.completed = false;
        this.readCount = 0;
        this.createdAt = new Date();
    }

    protected ReadingArticle(Parcel in) {
        id = in.readString();
        title = in.readString();
        content = in.readString();
        summary = in.readString();
        imageUrl = in.readString();
        level = in.readString();
        category = in.readString();
        wordCount = in.readInt();
        estimatedMinutes = in.readInt();
        questions = in.createTypedArrayList(ReadingQuestion.CREATOR);
        keyVocabulary = in.createStringArrayList();
        author = in.readString();
        source = in.readString();
        long publishedLong = in.readLong();
        publishedAt = publishedLong != -1 ? new Date(publishedLong) : null;
        long createdLong = in.readLong();
        createdAt = createdLong != -1 ? new Date(createdLong) : null;
        completed = in.readByte() != 0;
        userScore = in.readInt();
        readCount = in.readInt();
        long lastReadLong = in.readLong();
        lastReadAt = lastReadLong != -1 ? new Date(lastReadLong) : null;
        highlightedWords = in.createStringArrayList();
        userNotes = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(summary);
        dest.writeString(imageUrl);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeInt(wordCount);
        dest.writeInt(estimatedMinutes);
        dest.writeTypedList(questions);
        dest.writeStringList(keyVocabulary);
        dest.writeString(author);
        dest.writeString(source);
        dest.writeLong(publishedAt != null ? publishedAt.getTime() : -1);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(userScore);
        dest.writeInt(readCount);
        dest.writeLong(lastReadAt != null ? lastReadAt.getTime() : -1);
        dest.writeStringList(highlightedWords);
        dest.writeString(userNotes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReadingArticle> CREATOR = new Creator<ReadingArticle>() {
        @Override
        public ReadingArticle createFromParcel(Parcel in) {
            return new ReadingArticle(in);
        }

        @Override
        public ReadingArticle[] newArray(int size) {
            return new ReadingArticle[size];
        }
    };

    // Business logic
    public void addQuestion(ReadingQuestion question) {
        if (questions == null) questions = new ArrayList<>();
        questions.add(question);
    }

    public void addKeyVocabulary(String word) {
        if (keyVocabulary == null) keyVocabulary = new ArrayList<>();
        if (!keyVocabulary.contains(word)) {
            keyVocabulary.add(word);
        }
    }

    public void highlightWord(String word) {
        if (highlightedWords == null) highlightedWords = new ArrayList<>();
        if (!highlightedWords.contains(word)) {
            highlightedWords.add(word);
        }
    }

    public void incrementReadCount() {
        this.readCount++;
        this.lastReadAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPassage() { return passage; }
    public void setPassage(String passage) { this.passage = passage; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    // Alias for compatibility with new code
    public String getDifficulty() { return level; }
    public void setDifficulty(String difficulty) { this.level = difficulty; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public List<ReadingQuestion> getQuestions() { return questions; }
    public void setQuestions(List<ReadingQuestion> questions) { this.questions = questions; }

    public List<String> getKeyVocabulary() { return keyVocabulary; }
    public void setKeyVocabulary(List<String> keyVocabulary) { this.keyVocabulary = keyVocabulary; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date publishedAt) { this.publishedAt = publishedAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }

    public int getReadCount() { return readCount; }
    public void setReadCount(int readCount) { this.readCount = readCount; }

    public Date getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Date lastReadAt) { this.lastReadAt = lastReadAt; }

    public List<String> getHighlightedWords() { return highlightedWords; }
    public void setHighlightedWords(List<String> highlightedWords) { this.highlightedWords = highlightedWords; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }
}
