package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SpeakingPrompt - Individual speaking prompt/question
 * Loaded from Firebase, NO hard-coded prompts
 */
public class SpeakingPrompt implements Parcelable {

    private String id;
    private String promptText;         // What to say
    private String context;            // Background/situation
    private String sampleAnswer;       // Example response
    private String sampleAudioUrl;     // Example audio from Firebase Storage
    private int preparationSeconds;    // Time to prepare
    private int responseSeconds;       // Time to respond
    private String[] keywords;         // Key vocabulary to use

    public SpeakingPrompt() {
        // Required for Firebase
    }

    public SpeakingPrompt(String id, String promptText) {
        this.id = id;
        this.promptText = promptText;
    }

    protected SpeakingPrompt(Parcel in) {
        id = in.readString();
        promptText = in.readString();
        context = in.readString();
        sampleAnswer = in.readString();
        sampleAudioUrl = in.readString();
        preparationSeconds = in.readInt();
        responseSeconds = in.readInt();
        keywords = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(promptText);
        dest.writeString(context);
        dest.writeString(sampleAnswer);
        dest.writeString(sampleAudioUrl);
        dest.writeInt(preparationSeconds);
        dest.writeInt(responseSeconds);
        dest.writeStringArray(keywords);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpeakingPrompt> CREATOR = new Creator<SpeakingPrompt>() {
        @Override
        public SpeakingPrompt createFromParcel(Parcel in) {
            return new SpeakingPrompt(in);
        }

        @Override
        public SpeakingPrompt[] newArray(int size) {
            return new SpeakingPrompt[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getSampleAnswer() { return sampleAnswer; }
    public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }

    public String getSampleAudioUrl() { return sampleAudioUrl; }
    public void setSampleAudioUrl(String sampleAudioUrl) { this.sampleAudioUrl = sampleAudioUrl; }

    public int getPreparationSeconds() { return preparationSeconds; }
    public void setPreparationSeconds(int preparationSeconds) { this.preparationSeconds = preparationSeconds; }

    public int getResponseSeconds() { return responseSeconds; }
    public void setResponseSeconds(int responseSeconds) { this.responseSeconds = responseSeconds; }

    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }
}
