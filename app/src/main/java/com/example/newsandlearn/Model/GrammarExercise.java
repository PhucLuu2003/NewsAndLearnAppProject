package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * GrammarExercise Model - Represents grammar practice exercises
 * Supports multiple question types
 */
public class GrammarExercise implements Parcelable {
    
    public enum ExerciseType {
        MULTIPLE_CHOICE,
        FILL_IN_BLANK,
        ERROR_CORRECTION,
        SENTENCE_REORDER,
        MATCHING
    }
    
    public static class ExerciseQuestion implements Parcelable {
        private String question;
        private List<String> options;      // For multiple choice
        private String correctAnswer;
        private String explanation;
        private String userAnswer;
        private boolean isCorrect;
        
        public ExerciseQuestion() {
            this.options = new ArrayList<>();
        }
        
        public ExerciseQuestion(String question, List<String> options, String correctAnswer, String explanation) {
            this.question = question;
            this.options = options != null ? options : new ArrayList<>();
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
        }
        
        protected ExerciseQuestion(Parcel in) {
            question = in.readString();
            options = in.createStringArrayList();
            correctAnswer = in.readString();
            explanation = in.readString();
            userAnswer = in.readString();
            isCorrect = in.readByte() != 0;
        }
        
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(question);
            dest.writeStringList(options);
            dest.writeString(correctAnswer);
            dest.writeString(explanation);
            dest.writeString(userAnswer);
            dest.writeByte((byte) (isCorrect ? 1 : 0));
        }
        
        public int describeContents() {
            return 0;
        }
        
        public static final Creator<ExerciseQuestion> CREATOR = new Creator<ExerciseQuestion>() {
            @Override
            public ExerciseQuestion createFromParcel(Parcel in) {
                return new ExerciseQuestion(in);
            }
            
            @Override
            public ExerciseQuestion[] newArray(int size) {
                return new ExerciseQuestion[size];
            }
        };
        
        // Getters and Setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        
        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
        
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }
    
    private String id;
    private String lessonId;           // Related grammar lesson
    private String title;
    private ExerciseType type;
    private String typeString;         // For Firebase (enum as string)
    
    private List<ExerciseQuestion> questions;
    private int totalQuestions;
    private int timeLimit;             // Time limit in seconds (0 = no limit)
    
    // User progress
    private int currentQuestionIndex;
    private int correctAnswers;
    private int incorrectAnswers;
    private int score;                 // Percentage (0-100)
    private boolean isCompleted;

    // Constructors
    public GrammarExercise() {
        this.questions = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.correctAnswers = 0;
        this.incorrectAnswers = 0;
        this.score = 0;
        this.isCompleted = false;
        this.type = ExerciseType.MULTIPLE_CHOICE;
        this.typeString = "MULTIPLE_CHOICE";
    }

    public GrammarExercise(String id, String lessonId, String title, ExerciseType type) {
        this();
        this.id = id;
        this.lessonId = lessonId;
        this.title = title;
        this.type = type;
        this.typeString = type.name();
    }

    // Parcelable implementation
    protected GrammarExercise(Parcel in) {
        id = in.readString();
        lessonId = in.readString();
        title = in.readString();
        typeString = in.readString();
        type = typeString != null ? ExerciseType.valueOf(typeString) : ExerciseType.MULTIPLE_CHOICE;
        questions = in.createTypedArrayList(ExerciseQuestion.CREATOR);
        totalQuestions = in.readInt();
        timeLimit = in.readInt();
        currentQuestionIndex = in.readInt();
        correctAnswers = in.readInt();
        incorrectAnswers = in.readInt();
        score = in.readInt();
        isCompleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(lessonId);
        dest.writeString(title);
        dest.writeString(typeString);
        dest.writeTypedList(questions);
        dest.writeInt(totalQuestions);
        dest.writeInt(timeLimit);
        dest.writeInt(currentQuestionIndex);
        dest.writeInt(correctAnswers);
        dest.writeInt(incorrectAnswers);
        dest.writeInt(score);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GrammarExercise> CREATOR = new Creator<GrammarExercise>() {
        @Override
        public GrammarExercise createFromParcel(Parcel in) {
            return new GrammarExercise(in);
        }

        @Override
        public GrammarExercise[] newArray(int size) {
            return new GrammarExercise[size];
        }
    };

    // Helper methods
    public void addQuestion(ExerciseQuestion question) {
        if (questions == null) questions = new ArrayList<>();
        questions.add(question);
        totalQuestions = questions.size();
    }

    public ExerciseQuestion getCurrentQuestion() {
        if (questions != null && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean hasNextQuestion() {
        return currentQuestionIndex < totalQuestions - 1;
    }

    public void nextQuestion() {
        if (hasNextQuestion()) {
            currentQuestionIndex++;
        }
    }

    public void submitAnswer(String answer) {
        ExerciseQuestion current = getCurrentQuestion();
        if (current != null) {
            current.setUserAnswer(answer);
            boolean correct = answer != null && answer.equals(current.getCorrectAnswer());
            current.setCorrect(correct);
            
            if (correct) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
            
            calculateScore();
        }
    }

    public void calculateScore() {
        if (totalQuestions > 0) {
            score = (correctAnswers * 100) / totalQuestions;
        }
    }

    public void complete() {
        isCompleted = true;
        calculateScore();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ExerciseType getType() { return type; }
    public void setType(ExerciseType type) { 
        this.type = type;
        this.typeString = type.name();
    }

    public String getTypeString() { return typeString; }
    public void setTypeString(String typeString) { 
        this.typeString = typeString;
        try {
            this.type = ExerciseType.valueOf(typeString);
        } catch (Exception e) {
            this.type = ExerciseType.MULTIPLE_CHOICE;
        }
    }

    public List<ExerciseQuestion> getQuestions() { return questions; }
    public void setQuestions(List<ExerciseQuestion> questions) { 
        this.questions = questions;
        this.totalQuestions = questions != null ? questions.size() : 0;
    }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { 
        this.currentQuestionIndex = currentQuestionIndex; 
    }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getIncorrectAnswers() { return incorrectAnswers; }
    public void setIncorrectAnswers(int incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
