package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model cho bài hát pronunciation
 */
public class PronunciationSong {
    private String id;
    private String title;
    private String category;
    private int difficulty; // 1-5 stars
    private int bpm; // Beats per minute
    private int durationSeconds;
    private List<SongNote> notes;
    private String backgroundMusicUrl;
    private int highScore;
    private boolean isUnlocked;

    public PronunciationSong() {
        this.notes = new ArrayList<>();
    }

    public PronunciationSong(String id, String title, String category, int difficulty, int bpm) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.bpm = bpm;
        this.notes = new ArrayList<>();
        this.isUnlocked = true;
    }

    // Nested class for song notes
    public static class SongNote {
        private String word;
        private String phonetic;
        private String definition;
        private float beatPosition; // Vị trí trong beat (vd: 8.0, 12.5)
        private int difficulty; // 1-3 (easy/medium/hard word)
        private String hint;
        private boolean isSpawned;
        private long spawnTime;

        public SongNote() {}

        public SongNote(String word, String phonetic, float beatPosition, int difficulty) {
            this.word = word;
            this.phonetic = phonetic;
            this.beatPosition = beatPosition;
            this.difficulty = difficulty;
            this.isSpawned = false;
        }

        // Getters and Setters
        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getPhonetic() { return phonetic; }
        public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

        public String getDefinition() { return definition; }
        public void setDefinition(String definition) { this.definition = definition; }

        public float getBeatPosition() { return beatPosition; }
        public void setBeatPosition(float beatPosition) { this.beatPosition = beatPosition; }

        public int getDifficulty() { return difficulty; }
        public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

        public String getHint() { return hint; }
        public void setHint(String hint) { this.hint = hint; }

        public boolean isSpawned() { return isSpawned; }
        public void setSpawned(boolean spawned) { isSpawned = spawned; }

        public long getSpawnTime() { return spawnTime; }
        public void setSpawnTime(long spawnTime) { this.spawnTime = spawnTime; }
    }

    // Helper methods
    public void addNote(SongNote note) {
        this.notes.add(note);
    }

    public List<SongNote> getNotesInRange(float startBeat, float endBeat) {
        List<SongNote> result = new ArrayList<>();
        for (SongNote note : notes) {
            if (note.getBeatPosition() >= startBeat && note.getBeatPosition() <= endBeat) {
                result.add(note);
            }
        }
        return result;
    }

    public int getTotalWords() {
        return notes.size();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public int getBpm() { return bpm; }
    public void setBpm(int bpm) { this.bpm = bpm; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public List<SongNote> getNotes() { return notes; }
    public void setNotes(List<SongNote> notes) { this.notes = notes; }

    public String getBackgroundMusicUrl() { return backgroundMusicUrl; }
    public void setBackgroundMusicUrl(String backgroundMusicUrl) { this.backgroundMusicUrl = backgroundMusicUrl; }

    public int getHighScore() { return highScore; }
    public void setHighScore(int highScore) { this.highScore = highScore; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}
