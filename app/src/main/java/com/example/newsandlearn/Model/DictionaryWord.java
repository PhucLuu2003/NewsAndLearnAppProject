package com.example.newsandlearn.Model;

import java.util.List;

public class DictionaryWord {
    private String word;
    private String phonetic;
    private List<Meaning> meanings;
    private String audioUrl;

    public static class Meaning {
        private String partOfSpeech;
        private List<Definition> definitions;

        public String getPartOfSpeech() { return partOfSpeech; }
        public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

        public List<Definition> getDefinitions() { return definitions; }
        public void setDefinitions(List<Definition> definitions) { this.definitions = definitions; }
    }

    public static class Definition {
        private String definition;
        private String example;
        private List<String> synonyms;

        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }

        public String getExample() { return example; }
        public void setExample(String example) { this.example = example; }

        public List<String> getSynonyms() { return synonyms; }
        public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }
    }

    // Getters and Setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public List<Meaning> getMeanings() { return meanings; }
    public void setMeanings(List<Meaning> meanings) { this.meanings = meanings; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
