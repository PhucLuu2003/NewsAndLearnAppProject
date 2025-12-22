package com.example.newsandlearn.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generates crazy, memorable stories and images for vocabulary
 */
public class CrazyImageGenerator {
    
    private static final Random random = new Random();
    
    // Crazy actions
    private static final String[] ACTIONS = {
        "is dancing wildly",
        "is singing opera",
        "is doing backflips",
        "is wearing a tutu",
        "is riding a unicycle",
        "is juggling fire",
        "is breakdancing",
        "is doing yoga",
        "is playing guitar",
        "is cooking pancakes",
        "is painting rainbows",
        "is flying around",
        "is swimming in chocolate",
        "is sleeping and snoring loudly",
        "is having a party"
    };
    
    // Crazy modifiers
    private static final String[] MODIFIERS = {
        "GIANT",
        "TINY",
        "RAINBOW-COLORED",
        "GLOWING",
        "FLOATING",
        "INVISIBLE",
        "TRANSPARENT",
        "GOLDEN",
        "SPARKLING",
        "BOUNCING"
    };
    
    // Room-specific scenarios
    private static final Map<String, String[]> ROOM_SCENARIOS = new HashMap<>();
    
    static {
        ROOM_SCENARIOS.put("entrance", new String[]{
            "blocking the door",
            "welcoming guests",
            "guarding the entrance",
            "stuck in the doorway"
        });
        
        ROOM_SCENARIOS.put("living_room", new String[]{
            "sitting on the couch",
            "watching TV",
            "playing video games",
            "reading newspaper"
        });
        
        ROOM_SCENARIOS.put("kitchen", new String[]{
            "cooking dinner",
            "washing dishes",
            "eating from the fridge",
            "making coffee"
        });
        
        ROOM_SCENARIOS.put("bedroom", new String[]{
            "sleeping on the bed",
            "jumping on the mattress",
            "hiding under blankets",
            "snoring loudly"
        });
        
        ROOM_SCENARIOS.put("bathroom", new String[]{
            "taking a bubble bath",
            "brushing teeth",
            "singing in the shower",
            "looking in the mirror"
        });
    }
    
    /**
     * Generate a crazy story for a word in a specific room
     */
    public static String generateCrazyStory(String word, String roomId, String roomName) {
        String modifier = MODIFIERS[random.nextInt(MODIFIERS.length)];
        String action = ACTIONS[random.nextInt(ACTIONS.length)];
        
        String scenario = "";
        if (ROOM_SCENARIOS.containsKey(roomId)) {
            String[] scenarios = ROOM_SCENARIOS.get(roomId);
            scenario = scenarios[random.nextInt(scenarios.length)];
        } else {
            scenario = "in the " + roomName;
        }
        
        return String.format("A %s %s %s %s!", 
            modifier, word.toUpperCase(), action, scenario);
    }
    
    /**
     * Generate multiple story options
     */
    public static String[] generateStoryOptions(String word, String roomId, String roomName) {
        String[] stories = new String[3];
        for (int i = 0; i < 3; i++) {
            stories[i] = generateCrazyStory(word, roomId, roomName);
        }
        return stories;
    }
    
    /**
     * Get emoji representation for word (simplified)
     */
    public static String getWordEmoji(String word) {
        // Common word to emoji mapping
        Map<String, String> emojiMap = new HashMap<>();
        
        // Animals
        emojiMap.put("cat", "🐱");
        emojiMap.put("dog", "🐕");
        emojiMap.put("elephant", "🐘");
        emojiMap.put("bird", "🐦");
        emojiMap.put("fish", "🐟");
        emojiMap.put("rabbit", "🐰");
        emojiMap.put("bear", "🐻");
        emojiMap.put("lion", "🦁");
        emojiMap.put("tiger", "🐯");
        emojiMap.put("monkey", "🐵");
        emojiMap.put("cow", "🐮");
        emojiMap.put("pig", "🐷");
        emojiMap.put("mouse", "🐭");
        emojiMap.put("zebra", "🦓");
        
        // Food
        emojiMap.put("apple", "🍎");
        emojiMap.put("orange", "🍊");
        emojiMap.put("banana", "🍌");
        emojiMap.put("pizza", "🍕");
        emojiMap.put("burger", "🍔");
        emojiMap.put("cake", "🎂");
        emojiMap.put("ice cream", "🍦");
        emojiMap.put("coffee", "☕");
        emojiMap.put("juice", "🧃");
        
        // Objects
        emojiMap.put("book", "📚");
        emojiMap.put("car", "🚗");
        emojiMap.put("house", "🏠");
        emojiMap.put("key", "🔑");
        emojiMap.put("lamp", "💡");
        emojiMap.put("notebook", "📓");
        emojiMap.put("pencil", "✏️");
        emojiMap.put("phone", "📱");
        emojiMap.put("computer", "💻");
        emojiMap.put("watch", "⌚");
        emojiMap.put("camera", "📷");
        emojiMap.put("umbrella", "☂️");
        
        // Nature
        emojiMap.put("flower", "🌸");
        emojiMap.put("tree", "🌳");
        emojiMap.put("sun", "☀️");
        emojiMap.put("moon", "🌙");
        emojiMap.put("star", "⭐");
        emojiMap.put("rainbow", "🌈");
        emojiMap.put("water", "💧");
        
        // Music
        emojiMap.put("guitar", "🎸");
        emojiMap.put("violin", "🎻");
        emojiMap.put("xylophone", "🎹");
        
        // Others
        emojiMap.put("queen", "👸");
        emojiMap.put("yacht", "⛵");
        emojiMap.put("ball", "⚽");
        emojiMap.put("gift", "🎁");
        
        return emojiMap.getOrDefault(word.toLowerCase(), "📝");
    }
    
    /**
     * Create a visual representation combining emoji and modifiers
     */
    public static String createVisualImage(String word, String story) {
        String emoji = getWordEmoji(word);
        
        // Add visual effects based on story
        if (story.contains("GIANT")) {
            return emoji + emoji + emoji; // Triple size
        } else if (story.contains("TINY")) {
            return "🔍" + emoji; // Magnifying glass
        } else if (story.contains("RAINBOW")) {
            return "🌈" + emoji;
        } else if (story.contains("GLOWING")) {
            return "✨" + emoji + "✨";
        } else if (story.contains("FLOATING")) {
            return "☁️" + emoji + "☁️";
        } else if (story.contains("GOLDEN")) {
            return "⭐" + emoji + "⭐";
        } else if (story.contains("SPARKLING")) {
            return "💫" + emoji + "💫";
        } else if (story.contains("BOUNCING")) {
            return "⬆️" + emoji + "⬇️";
        }
        
        return emoji;
    }
}
