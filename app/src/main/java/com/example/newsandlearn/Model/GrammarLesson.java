package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GrammarLesson Model - Represents a grammar lesson
 * Contains theory, examples, and exercises
 */
public class GrammarLesson implements Parcelable {
    
    private String id;
    private String title;              // e.g., "Present Perfect Tense"
    private String description;        // Brief overview
    private String level;              // A1, A2, B1, B2, C1, C2
    private String category;           // tenses, articles, prepositions, etc.
    
    // Content sections
    private String structure;          // Grammar structure/formula
    private List<String> rules;        // List of grammar rules
    private List<String> examples;     // Example sentences
    private List<String> exampleTranslations; // Vietnamese translations
    private List<String> keyPoints;    // Important points to remember
    
    // Exercises
    private List<String> exerciseIds;  // IDs of related exercises
    private int exerciseCount;
    
    // Metadata
    private Date createdAt;
    private Date updatedAt;
    private int views;
    private int completions;
    private boolean isBookmarked;
    
    // Progress
    private boolean isCompleted;
    private int userScore;             // User's score on exercises (0-100)

    // Constructors
    public GrammarLesson() {
        // Required empty constructor for Firebase
        this.rules = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.exampleTranslations = new ArrayList<>();
        this.keyPoints = new ArrayList<>();
        this.exerciseIds = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.views = 0;
        this.completions = 0;
        this.isBookmarked = false;
        this.isCompleted = false;
        this.userScore = 0;
    }

    public GrammarLesson(String id, String title, String description, String level, String category) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.category = category;
    }

    // Parcelable implementation
    protected GrammarLesson(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        level = in.readString();
        category = in.readString();
        structure = in.readString();
        rules = in.createStringArrayList();
        examples = in.createStringArrayList();
        exampleTranslations = in.createStringArrayList();
        keyPoints = in.createStringArrayList();
        exerciseIds = in.createStringArrayList();
        exerciseCount = in.readInt();
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Date(createdAtTime) : new Date();
        long updatedAtTime = in.readLong();
        updatedAt = updatedAtTime != -1 ? new Date(updatedAtTime) : new Date();
        views = in.readInt();
        completions = in.readInt();
        isBookmarked = in.readByte() != 0;
        isCompleted = in.readByte() != 0;
        userScore = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeString(structure);
        dest.writeStringList(rules);
        dest.writeStringList(examples);
        dest.writeStringList(exampleTranslations);
        dest.writeStringList(keyPoints);
        dest.writeStringList(exerciseIds);
        dest.writeInt(exerciseCount);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeInt(views);
        dest.writeInt(completions);
        dest.writeByte((byte) (isBookmarked ? 1 : 0));
        dest.writeByte((byte) (isCompleted ? 1 : 0));
        dest.writeInt(userScore);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GrammarLesson> CREATOR = new Creator<GrammarLesson>() {
        @Override
        public GrammarLesson createFromParcel(Parcel in) {
            return new GrammarLesson(in);
        }

        @Override
        public GrammarLesson[] newArray(int size) {
            return new GrammarLesson[size];
        }
    };

    // Helper methods
    public void addRule(String rule) {
        if (rules == null) rules = new ArrayList<>();
        rules.add(rule);
    }

    public void addExample(String example, String translation) {
        if (examples == null) examples = new ArrayList<>();
        if (exampleTranslations == null) exampleTranslations = new ArrayList<>();
        examples.add(example);
        exampleTranslations.add(translation);
    }

    public void addKeyPoint(String keyPoint) {
        if (keyPoints == null) keyPoints = new ArrayList<>();
        keyPoints.add(keyPoint);
    }

    public void addExercise(String exerciseId) {
        if (exerciseIds == null) exerciseIds = new ArrayList<>();
        if (!exerciseIds.contains(exerciseId)) {
            exerciseIds.add(exerciseId);
            exerciseCount = exerciseIds.size();
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStructure() { return structure; }
    public void setStructure(String structure) { this.structure = structure; }

    public List<String> getRules() { return rules; }
    public void setRules(List<String> rules) { this.rules = rules; }

    public List<String> getExamples() { return examples; }
    public void setExamples(List<String> examples) { this.examples = examples; }

    public List<String> getExampleTranslations() { return exampleTranslations; }
    public void setExampleTranslations(List<String> exampleTranslations) { 
        this.exampleTranslations = exampleTranslations; 
    }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public List<String> getExerciseIds() { return exerciseIds; }
    public void setExerciseIds(List<String> exerciseIds) { 
        this.exerciseIds = exerciseIds;
        this.exerciseCount = exerciseIds != null ? exerciseIds.size() : 0;
    }

    public int getExerciseCount() { return exerciseCount; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getCompletions() { return completions; }
    public void setCompletions(int completions) { this.completions = completions; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getUserScore() { return userScore; }
    public void setUserScore(int userScore) { this.userScore = userScore; }
}
