package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Palace Model - Represents a palace with rooms for storing vocabulary
 */
public class MemoryPalace {
    private String id;
    private String name;
    private String type; // HOME, SCHOOL, MALL, CUSTOM
    private List<Room> rooms;
    private int totalWords;
    private long createdAt;

    public MemoryPalace(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rooms = new ArrayList<>();
        this.totalWords = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Room class
    public static class Room {
        private String id;
        private String name;
        private String emoji;
        private int position; // Position in palace
        private WordMemory wordMemory; // Word stored in this room

        public Room(String id, String name, String emoji, int position) {
            this.id = id;
            this.name = name;
            this.emoji = emoji;
            this.position = position;
            this.wordMemory = null;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public int getPosition() { return position; }
        public WordMemory getWordMemory() { return wordMemory; }
        public void setWordMemory(WordMemory wordMemory) { this.wordMemory = wordMemory; }
        public boolean hasWord() { return wordMemory != null; }
    }

    // Word Memory class - stores word with crazy image
    public static class WordMemory {
        private String word;
        private String meaning;
        private String crazyStory;
        private String imageUrl; // Can be emoji or actual image
        private long learnedAt;
        private int reviewCount;
        private long nextReviewAt;

        public WordMemory(String word, String meaning, String crazyStory, String imageUrl) {
            this.word = word;
            this.meaning = meaning;
            this.crazyStory = crazyStory;
            this.imageUrl = imageUrl;
            this.learnedAt = System.currentTimeMillis();
            this.reviewCount = 0;
            this.nextReviewAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 1 day
        }

        public String getWord() { return word; }
        public String getMeaning() { return meaning; }
        public String getCrazyStory() { return crazyStory; }
        public String getImageUrl() { return imageUrl; }
        public long getLearnedAt() { return learnedAt; }
        public int getReviewCount() { return reviewCount; }
        public long getNextReviewAt() { return nextReviewAt; }
        
        public void incrementReview() {
            reviewCount++;
            // Spaced repetition: 1 day, 3 days, 7 days, 14 days, 30 days
            long[] intervals = {1, 3, 7, 14, 30};
            int index = Math.min(reviewCount - 1, intervals.length - 1);
            nextReviewAt = System.currentTimeMillis() + (intervals[index] * 24 * 60 * 60 * 1000);
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public List<Room> getRooms() { return rooms; }
    public int getTotalWords() { return totalWords; }
    public long getCreatedAt() { return createdAt; }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void addWordToRoom(int roomIndex, WordMemory wordMemory) {
        if (roomIndex >= 0 && roomIndex < rooms.size()) {
            rooms.get(roomIndex).setWordMemory(wordMemory);
            totalWords++;
        }
    }

    public int getProgress() {
        int wordsPlaced = 0;
        for (Room room : rooms) {
            if (room.hasWord()) wordsPlaced++;
        }
        return rooms.size() > 0 ? (wordsPlaced * 100 / rooms.size()) : 0;
    }

    // Palace Templates
    public static MemoryPalace createHomePalace() {
        MemoryPalace palace = new MemoryPalace("home_palace", "My Home", "HOME");
        palace.addRoom(new Room("entrance", "Entrance", "🚪", 0));
        palace.addRoom(new Room("living_room", "Living Room", "🛋️", 1));
        palace.addRoom(new Room("kitchen", "Kitchen", "🍳", 2));
        palace.addRoom(new Room("bedroom", "Bedroom", "🛏️", 3));
        palace.addRoom(new Room("bathroom", "Bathroom", "🚿", 4));
        return palace;
    }

    public static MemoryPalace createSchoolPalace() {
        MemoryPalace palace = new MemoryPalace("school_palace", "My School", "SCHOOL");
        palace.addRoom(new Room("gate", "School Gate", "🏫", 0));
        palace.addRoom(new Room("classroom", "Classroom", "📚", 1));
        palace.addRoom(new Room("library", "Library", "📖", 2));
        palace.addRoom(new Room("cafeteria", "Cafeteria", "🍽️", 3));
        palace.addRoom(new Room("gym", "Gym", "🏀", 4));
        palace.addRoom(new Room("lab", "Science Lab", "🔬", 5));
        palace.addRoom(new Room("art_room", "Art Room", "🎨", 6));
        palace.addRoom(new Room("playground", "Playground", "🎪", 7));
        return palace;
    }

    public static MemoryPalace createMallPalace() {
        MemoryPalace palace = new MemoryPalace("mall_palace", "Shopping Mall", "MALL");
        palace.addRoom(new Room("entrance", "Mall Entrance", "🏬", 0));
        palace.addRoom(new Room("food_court", "Food Court", "🍔", 1));
        palace.addRoom(new Room("clothing", "Clothing Store", "👕", 2));
        palace.addRoom(new Room("electronics", "Electronics", "📱", 3));
        palace.addRoom(new Room("bookstore", "Bookstore", "📚", 4));
        palace.addRoom(new Room("cinema", "Cinema", "🎬", 5));
        palace.addRoom(new Room("arcade", "Arcade", "🎮", 6));
        palace.addRoom(new Room("supermarket", "Supermarket", "🛒", 7));
        palace.addRoom(new Room("pharmacy", "Pharmacy", "💊", 8));
        palace.addRoom(new Room("toy_store", "Toy Store", "🧸", 9));
        palace.addRoom(new Room("jewelry", "Jewelry", "💎", 10));
        palace.addRoom(new Room("parking", "Parking Lot", "🚗", 11));
        return palace;
    }
}
