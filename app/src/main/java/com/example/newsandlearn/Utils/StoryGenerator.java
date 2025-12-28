package com.example.newsandlearn.Utils;

import com.example.newsandlearn.Model.LearningStory;
import com.example.newsandlearn.Model.VocabularyWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Story Generator - Creates learning stories with vocabulary words
 * Mock AI implementation with predefined templates
 */
public class StoryGenerator {

    private static final Map<String, StoryTemplate[]> STORY_TEMPLATES = new HashMap<>();
    private static final Random random = new Random();

    static {
        initializeTemplates();
    }

    public static LearningStory generateStory(String topic) {
        StoryTemplate[] templates = STORY_TEMPLATES.get(topic.toLowerCase());
        if (templates == null || templates.length == 0) {
            templates = STORY_TEMPLATES.get("travel"); // Default fallback
        }

        StoryTemplate template = templates[random.nextInt(templates.length)];
        
        List<VocabularyWord> words = new ArrayList<>();
        for (int i = 0; i < template.words.length; i++) {
            words.add(new VocabularyWord(
                template.words[i],
                template.pronunciations[i],
                template.meanings[i],
                "", // Example will be from the story
                i
            ));
        }

        String storyId = topic + "_" + System.currentTimeMillis();
        
        return new LearningStory(
            storyId,
            template.title,
            topic,
            template.fullStory,
            template.storyWithBlanks,
            words
        );
    }

    private static void initializeTemplates() {
        // TRAVEL STORIES
        STORY_TEMPLATES.put("travel", new StoryTemplate[]{
            new StoryTemplate(
                "A Weekend Adventure",
                "Last summer, I planned an amazing trip to Japan. My destination was Tokyo, a vibrant city full of culture and technology. Before leaving, I carefully booked my accommodation in a cozy hotel near Shibuya. I also prepared a detailed itinerary with all the places I wanted to visit, including temples, museums, and local restaurants. It was the best trip ever!",
                "Last summer, I planned an amazing trip to Japan. My ___ was Tokyo, a vibrant city full of culture and technology. Before leaving, I carefully booked my ___ in a cozy hotel near Shibuya. I also prepared a detailed ___ with all the places I wanted to visit, including temples, museums, and local restaurants. It was the best trip ever!",
                new String[]{"destination", "accommodation", "itinerary"},
                new String[]{"/ˌdes.tɪˈneɪ.ʃən/", "/əˌkɒm.əˈdeɪ.ʃən/", "/aɪˈtɪn.ər.ər.i/"},
                new String[]{"điểm đến", "chỗ ở", "lịch trình"}
            ),
            new StoryTemplate(
                "Exploring Europe",
                "My journey through Europe was unforgettable. I visited many landmarks including the Eiffel Tower and Big Ben. The local cuisine was delicious, and I tried many traditional dishes. Meeting fellow travelers from different countries enriched my experience. This adventure taught me so much about different cultures!",
                "My journey through Europe was unforgettable. I visited many ___ including the Eiffel Tower and Big Ben. The local ___ was delicious, and I tried many traditional dishes. Meeting fellow ___ from different countries enriched my experience. This adventure taught me so much about different cultures!",
                new String[]{"landmarks", "cuisine", "travelers"},
                new String[]{"/ˈlænd.mɑːrk/", "/kwɪˈziːn/", "/ˈtræv.əl.ər/"},
                new String[]{"địa danh", "ẩm thực", "du khách"}
            )
        });

        // FOOD STORIES
        STORY_TEMPLATES.put("food", new StoryTemplate[]{
            new StoryTemplate(
                "Cooking Adventure",
                "Yesterday, I tried a new recipe for pasta. The ingredients were fresh from the market. I followed the instructions carefully and added my favorite spices. The aroma filled the kitchen as it cooked. My family loved the delicious meal I prepared!",
                "Yesterday, I tried a new ___ for pasta. The ___ were fresh from the market. I followed the instructions carefully and added my favorite ___. The aroma filled the kitchen as it cooked. My family loved the delicious meal I prepared!",
                new String[]{"recipe", "ingredients", "spices"},
                new String[]{"/ˈres.ə.pi/", "/ɪnˈɡriː.di.ənt/", "/spaɪs/"},
                new String[]{"công thức nấu ăn", "nguyên liệu", "gia vị"}
            )
        });

        // BUSINESS STORIES
        STORY_TEMPLATES.put("business", new StoryTemplate[]{
            new StoryTemplate(
                "First Day at Work",
                "Today was my first day at the new company. I attended an important meeting with the team. My supervisor explained my responsibilities and introduced me to colleagues. I learned about the company's strategy for the next quarter. Everyone was very welcoming and helpful!",
                "Today was my first day at the new company. I attended an important ___ with the team. My ___ explained my responsibilities and introduced me to colleagues. I learned about the company's ___ for the next quarter. Everyone was very welcoming and helpful!",
                new String[]{"meeting", "supervisor", "strategy"},
                new String[]{"/ˈmiː.tɪŋ/", "/ˈsuː.pə.vaɪ.zər/", "/ˈstræt.ə.dʒi/"},
                new String[]{"cuộc họp", "người giám sát", "chiến lược"}
            )
        });

        // HEALTH STORIES
        STORY_TEMPLATES.put("health", new StoryTemplate[]{
            new StoryTemplate(
                "Healthy Lifestyle",
                "I decided to improve my fitness this year. Every morning, I do exercise for 30 minutes. I also changed my diet to include more vegetables and fruits. Drinking enough water and getting proper nutrition is important. I feel much better and have more energy now!",
                "I decided to improve my ___ this year. Every morning, I do ___ for 30 minutes. I also changed my diet to include more vegetables and fruits. Drinking enough water and getting proper ___ is important. I feel much better and have more energy now!",
                new String[]{"fitness", "exercise", "nutrition"},
                new String[]{"/ˈfɪt.nəs/", "/ˈek.sə.saɪz/", "/njuːˈtrɪʃ.ən/"},
                new String[]{"thể lực", "tập thể dục", "dinh dưỡng"}
            )
        });

        // TECHNOLOGY STORIES
        STORY_TEMPLATES.put("technology", new StoryTemplate[]{
            new StoryTemplate(
                "Digital World",
                "Technology has changed our lives dramatically. We use various software for work and entertainment. Cloud storage helps us save important data safely. Learning to code and understanding algorithms is becoming essential. The future of innovation looks very exciting!",
                "Technology has changed our lives dramatically. We use various ___ for work and entertainment. Cloud storage helps us save important ___ safely. Learning to code and understanding ___ is becoming essential. The future of innovation looks very exciting!",
                new String[]{"software", "data", "algorithms"},
                new String[]{"/ˈsɒft.weər/", "/ˈdeɪ.tə/", "/ˈæl.ɡə.rɪ.ðəm/"},
                new String[]{"phần mềm", "dữ liệu", "thuật toán"}
            )
        });
    }

    // Story Template class
    private static class StoryTemplate {
        String title;
        String fullStory;
        String storyWithBlanks;
        String[] words;
        String[] pronunciations;
        String[] meanings;

        StoryTemplate(String title, String fullStory, String storyWithBlanks,
                     String[] words, String[] pronunciations, String[] meanings) {
            this.title = title;
            this.fullStory = fullStory;
            this.storyWithBlanks = storyWithBlanks;
            this.words = words;
            this.pronunciations = pronunciations;
            this.meanings = meanings;
        }
    }
}
