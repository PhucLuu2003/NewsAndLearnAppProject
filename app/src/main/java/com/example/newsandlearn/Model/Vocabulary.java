package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Vocabulary Model - Represents a public vocabulary word
 * Stored in 'vocabularies' collection (root level)
 * This is the master vocabulary data shared across all users
 */
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class Vocabulary implements Parcelable {
    
    // Basic Information
    private String id;
    private String word;              // English word
    private String translation;       // Vietnamese translation
    private String pronunciation;     // IPA or phonetic spelling
    private String partOfSpeech;      // noun, verb, adjective, etc.
    private String definition;        // English definition
    private String example;           // Example sentence (Legacy, PRIMARY)
    private String exampleTranslation; // Vietnamese translation of example (Legacy, PRIMARY)
    
    // Categorization
    private String level;             // A1, A2, B1, B2, C1, C2
    private String category;          // topic/theme (business, travel, etc.)
    
    // Additional Information
    private List<String> synonyms;    // List of synonyms
    private List<String> antonyms;    // List of antonyms
    private List<String> examples;    // List of examples
    private List<String> exampleTranslations; // List of example translations
    private String imageUrl;          // Optional image for visual learning
    private String audioUrl;          // Optional audio pronunciation URL
    
    // Metadata
    private Date createdAt;           // When word was added to system
    private String createdBy;         // "system" or userId who created it
    private boolean isPublic;         // Public words vs user-created private words

    // Constructors
    public Vocabulary() {
        // Required empty constructor for Firebase
        this.createdAt = new Date();
        this.isPublic = true;
        this.synonyms = new ArrayList<>();
        this.antonyms = new ArrayList<>();
        this.examples = new ArrayList<>();
        this.exampleTranslations = new ArrayList<>();
        this.createdBy = "system";
    }

    public Vocabulary(String id, String word, String translation, String pronunciation,
                     String partOfSpeech, String example, String exampleTranslation,
                     String level, String category) {
        this.id = id;
        this.word = word;
        this.translation = translation;
        this.pronunciation = pronunciation;
        this.partOfSpeech = partOfSpeech;
        this.example = example;
        this.exampleTranslation = exampleTranslation;
        this.level = level;
        this.category = category;
        this.createdAt = new Date();
        this.isPublic = true;
        this.synonyms = new ArrayList<>();
        this.antonyms = new ArrayList<>();
        this.examples = new ArrayList<>();
        if (example != null) this.examples.add(example);
        this.exampleTranslations = new ArrayList<>();
        if (exampleTranslation != null) this.exampleTranslations.add(exampleTranslation);
        this.createdBy = "system";
    }

    // Parcelable implementation
    protected Vocabulary(Parcel in) {
        id = in.readString();
        word = in.readString();
        translation = in.readString();
        pronunciation = in.readString();
        partOfSpeech = in.readString();
        definition = in.readString();
        example = in.readString();
        exampleTranslation = in.readString();
        level = in.readString();
        category = in.readString();
        synonyms = in.createStringArrayList();
        antonyms = in.createStringArrayList();
        examples = in.createStringArrayList();
        exampleTranslations = in.createStringArrayList();
        imageUrl = in.readString();
        audioUrl = in.readString();
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Date(createdAtTime) : new Date();
        createdBy = in.readString();
        isPublic = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(word);
        dest.writeString(translation);
        dest.writeString(pronunciation);
        dest.writeString(partOfSpeech);
        dest.writeString(definition);
        dest.writeString(example);
        dest.writeString(exampleTranslation);
        dest.writeString(level);
        dest.writeString(category);
        dest.writeStringList(synonyms);
        dest.writeStringList(antonyms);
        dest.writeStringList(examples);
        dest.writeStringList(exampleTranslations);
        dest.writeString(imageUrl);
        dest.writeString(audioUrl);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeString(createdBy);
        dest.writeByte((byte) (isPublic ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Vocabulary> CREATOR = new Creator<Vocabulary>() {
        @Override
        public Vocabulary createFromParcel(Parcel in) {
            return new Vocabulary(in);
        }

        @Override
        public Vocabulary[] newArray(int size) {
            return new Vocabulary[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }
    
    // Alias for seeder compatibility
    public void setPhonetic(String phonetic) { setPronunciation(phonetic); }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }



    @Exclude
    public String getExample() { return example; }

    @Exclude
    public void setExample(String example) { 
        this.example = example; 
        // Keep lists in sync
        if (this.examples == null) this.examples = new ArrayList<>();
        if (example != null && !this.examples.contains(example)) this.examples.add(0, example);
    }

    @PropertyName("example")
    public void setExampleField(Object exampleObj) {
        if (exampleObj instanceof String) {
            setExample((String) exampleObj);
        } else if (exampleObj instanceof List) {
            List<?> list = (List<?>) exampleObj;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof String) {
                    setExample((String) first);
                    // Also populate the full list if needed
                    if (this.examples == null) this.examples = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof String && !this.examples.contains(item)) {
                            this.examples.add((String) item);
                        }
                    }
                }
            }
        }
    }

    @PropertyName("example")
    public Object getExampleField() {
        return example;
    }

    @Exclude
    public String getExampleTranslation() { return exampleTranslation; }

    @Exclude
    public void setExampleTranslation(String exampleTranslation) { 
        this.exampleTranslation = exampleTranslation;
        // Keep lists in sync
        if (this.exampleTranslations == null) this.exampleTranslations = new ArrayList<>();
        if (exampleTranslation != null && !this.exampleTranslations.contains(exampleTranslation)) this.exampleTranslations.add(0, exampleTranslation);
    }

    @PropertyName("exampleTranslation")
    public void setExampleTranslationField(Object exampleObj) {
        if (exampleObj instanceof String) {
            setExampleTranslation((String) exampleObj);
        } else if (exampleObj instanceof List) {
            List<?> list = (List<?>) exampleObj;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof String) {
                    setExampleTranslation((String) first);
                    if (this.exampleTranslations == null) this.exampleTranslations = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof String && !this.exampleTranslations.contains(item)) {
                            this.exampleTranslations.add((String) item);
                        }
                    }
                }
            }
        }
    }

    @PropertyName("exampleTranslation")
    public Object getExampleTranslationField() {
        return exampleTranslation;
    }
    
    public List<String> getExamples() { return examples; }
    public void setExamples(List<String> examples) { 
        this.examples = examples;
        if (examples != null && !examples.isEmpty()) {
            this.example = examples.get(0);
        }
    }
    
    public List<String> getExampleTranslations() { return exampleTranslations; }
    public void setExampleTranslations(List<String> exampleTranslations) { 
        this.exampleTranslations = exampleTranslations;
        if (exampleTranslations != null && !exampleTranslations.isEmpty()) {
            this.exampleTranslation = exampleTranslations.get(0);
        }
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getSynonyms() { return synonyms; }
    public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }

    public List<String> getAntonyms() { return antonyms; }
    public void setAntonyms(List<String> antonyms) { this.antonyms = antonyms; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }
}
