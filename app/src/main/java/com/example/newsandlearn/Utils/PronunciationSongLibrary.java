package com.example.newsandlearn.Utils;

import com.example.newsandlearn.Model.PronunciationSong;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class để tạo sample songs cho Pronunciation Beat game
 */
public class PronunciationSongLibrary {

    public static List<PronunciationSong> getSampleSongs() {
        List<PronunciationSong> songs = new ArrayList<>();
        
        songs.add(createHappyVibes());
        songs.add(createDailyRoutine());
        songs.add(createTongueTwister());
        songs.add(createBusinessEnglish());
        
        return songs;
    }

    private static PronunciationSong createHappyVibes() {
        PronunciationSong song = new PronunciationSong(
            "happy_vibes",
            "Happy Vibes",
            "Emotions",
            1, // Easy
            120 // BPM
        );
        
        song.setDurationSeconds(150); // 2:30
        
        // Add notes (words with timing)
        song.addNote(new PronunciationSong.SongNote("happy", "/ˈhæpi/", 8.0f, 1));
        song.addNote(new PronunciationSong.SongNote("smile", "/smaɪl/", 12.0f, 1));
        song.addNote(new PronunciationSong.SongNote("joy", "/dʒɔɪ/", 16.0f, 1));
        song.addNote(new PronunciationSong.SongNote("laugh", "/læf/", 20.0f, 1));
        song.addNote(new PronunciationSong.SongNote("friend", "/frend/", 24.0f, 1));
        song.addNote(new PronunciationSong.SongNote("love", "/lʌv/", 28.0f, 1));
        song.addNote(new PronunciationSong.SongNote("peace", "/piːs/", 32.0f, 1));
        song.addNote(new PronunciationSong.SongNote("kind", "/kaɪnd/", 36.0f, 1));
        song.addNote(new PronunciationSong.SongNote("bright", "/braɪt/", 40.0f, 1));
        song.addNote(new PronunciationSong.SongNote("cheerful", "/ˈtʃɪrfəl/", 44.0f, 2));
        song.addNote(new PronunciationSong.SongNote("wonderful", "/ˈwʌndərfəl/", 48.0f, 2));
        song.addNote(new PronunciationSong.SongNote("amazing", "/əˈmeɪzɪŋ/", 52.0f, 2));
        song.addNote(new PronunciationSong.SongNote("fantastic", "/fænˈtæstɪk/", 56.0f, 2));
        song.addNote(new PronunciationSong.SongNote("delightful", "/dɪˈlaɪtfəl/", 60.0f, 2));
        song.addNote(new PronunciationSong.SongNote("joyful", "/ˈdʒɔɪfəl/", 64.0f, 2));
        
        return song;
    }

    private static PronunciationSong createDailyRoutine() {
        PronunciationSong song = new PronunciationSong(
            "daily_routine",
            "Daily Routine",
            "Daily Life",
            2, // Medium
            100 // BPM
        );
        
        song.setDurationSeconds(180); // 3:00
        
        song.addNote(new PronunciationSong.SongNote("wake", "/weɪk/", 8.0f, 1));
        song.addNote(new PronunciationSong.SongNote("breakfast", "/ˈbrekfəst/", 12.0f, 2));
        song.addNote(new PronunciationSong.SongNote("shower", "/ˈʃaʊər/", 16.0f, 1));
        song.addNote(new PronunciationSong.SongNote("dress", "/dres/", 20.0f, 1));
        song.addNote(new PronunciationSong.SongNote("commute", "/kəˈmjuːt/", 24.0f, 2));
        song.addNote(new PronunciationSong.SongNote("work", "/wɜːrk/", 28.0f, 1));
        song.addNote(new PronunciationSong.SongNote("lunch", "/lʌntʃ/", 32.0f, 1));
        song.addNote(new PronunciationSong.SongNote("meeting", "/ˈmiːtɪŋ/", 36.0f, 1));
        song.addNote(new PronunciationSong.SongNote("exercise", "/ˈeksərsaɪz/", 40.0f, 2));
        song.addNote(new PronunciationSong.SongNote("dinner", "/ˈdɪnər/", 44.0f, 1));
        song.addNote(new PronunciationSong.SongNote("relax", "/rɪˈlæks/", 48.0f, 1));
        song.addNote(new PronunciationSong.SongNote("sleep", "/sliːp/", 52.0f, 1));
        
        return song;
    }

    private static PronunciationSong createTongueTwister() {
        PronunciationSong song = new PronunciationSong(
            "tongue_twister",
            "Tongue Twister Challenge",
            "Challenge",
            5, // Expert
            180 // BPM - Fast!
        );
        
        song.setDurationSeconds(120); // 2:00
        song.setUnlocked(false); // Locked initially
        
        song.addNote(new PronunciationSong.SongNote("she", "/ʃiː/", 10.0f, 1));
        song.addNote(new PronunciationSong.SongNote("sells", "/selz/", 11.0f, 2));
        song.addNote(new PronunciationSong.SongNote("seashells", "/ˈsiːʃelz/", 12.0f, 3));
        song.addNote(new PronunciationSong.SongNote("seashore", "/ˈsiːʃɔːr/", 14.0f, 3));
        song.addNote(new PronunciationSong.SongNote("peter", "/ˈpiːtər/", 18.0f, 1));
        song.addNote(new PronunciationSong.SongNote("piper", "/ˈpaɪpər/", 19.0f, 2));
        song.addNote(new PronunciationSong.SongNote("picked", "/pɪkt/", 20.0f, 2));
        song.addNote(new PronunciationSong.SongNote("peppers", "/ˈpepərz/", 21.0f, 2));
        song.addNote(new PronunciationSong.SongNote("woodchuck", "/ˈwʊdtʃʌk/", 24.0f, 3));
        song.addNote(new PronunciationSong.SongNote("chuck", "/tʃʌk/", 25.0f, 2));
        song.addNote(new PronunciationSong.SongNote("wood", "/wʊd/", 26.0f, 1));
        
        return song;
    }

    private static PronunciationSong createBusinessEnglish() {
        PronunciationSong song = new PronunciationSong(
            "business_english",
            "Business English",
            "Professional",
            3, // Hard
            110 // BPM
        );
        
        song.setDurationSeconds(200); // 3:20
        
        song.addNote(new PronunciationSong.SongNote("meeting", "/ˈmiːtɪŋ/", 8.0f, 1));
        song.addNote(new PronunciationSong.SongNote("presentation", "/ˌprezənˈteɪʃən/", 12.0f, 3));
        song.addNote(new PronunciationSong.SongNote("deadline", "/ˈdedlaɪn/", 16.0f, 2));
        song.addNote(new PronunciationSong.SongNote("project", "/ˈprɑːdʒekt/", 20.0f, 2));
        song.addNote(new PronunciationSong.SongNote("budget", "/ˈbʌdʒɪt/", 24.0f, 2));
        song.addNote(new PronunciationSong.SongNote("strategy", "/ˈstrætədʒi/", 28.0f, 2));
        song.addNote(new PronunciationSong.SongNote("revenue", "/ˈrevənuː/", 32.0f, 2));
        song.addNote(new PronunciationSong.SongNote("profit", "/ˈprɑːfɪt/", 36.0f, 2));
        song.addNote(new PronunciationSong.SongNote("investment", "/ɪnˈvestmənt/", 40.0f, 3));
        song.addNote(new PronunciationSong.SongNote("stakeholder", "/ˈsteɪkhoʊldər/", 44.0f, 3));
        song.addNote(new PronunciationSong.SongNote("collaboration", "/kəˌlæbəˈreɪʃən/", 48.0f, 3));
        song.addNote(new PronunciationSong.SongNote("productivity", "/ˌproʊdʌkˈtɪvəti/", 52.0f, 3));
        
        return song;
    }

    public static PronunciationSong getSongById(String id) {
        for (PronunciationSong song : getSampleSongs()) {
            if (song.getId().equals(id)) {
                return song;
            }
        }
        return null;
    }
}
