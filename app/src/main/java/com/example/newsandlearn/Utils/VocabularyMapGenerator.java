package com.example.newsandlearn.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vocabulary Map Generator - Creates visual vocabulary maps from articles
 * Features:
 * - Word frequency analysis
 * - Word cloud data generation
 * - Vocabulary relationships
 * - Category clustering
 * - Difficulty scoring
 */
public class VocabularyMapGenerator {
    private static final String TAG = "VocabMapGenerator";
    private static VocabularyMapGenerator instance;

    // Common English stop words to filter out
    private static final String[] STOP_WORDS = {
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their",
        "what", "so", "up", "out", "if", "about", "who", "get", "which", "go",
        "me", "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could", "them",
        "see", "other", "than", "then", "now", "look", "only", "come", "its", "over",
        "think", "also", "back", "after", "use", "two", "how", "our", "work",
        "first", "well", "way", "even", "new", "want", "because", "any", "these",
        "give", "day", "most", "us", "is", "was", "are", "been", "has", "had",
        "were", "said", "did", "having", "may", "should", "am"
    };

    private VocabularyMapGenerator() {}

    public static synchronized VocabularyMapGenerator getInstance() {
        if (instance == null) {
            instance = new VocabularyMapGenerator();
        }
        return instance;
    }

    /**
     * Generate word cloud data from article
     */
    public List<WordCloudItem> generateWordCloud(String text, int maxWords) {
        Map<String, Integer> wordFrequency = analyzeWordFrequency(text);
        List<WordCloudItem> cloudItems = new ArrayList<>();

        // Convert to WordCloudItem list
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            WordCloudItem item = new WordCloudItem();
            item.word = entry.getKey();
            item.frequency = entry.getValue();
            item.size = calculateWordSize(entry.getValue(), wordFrequency);
            item.importance = calculateImportance(entry.getKey(), entry.getValue());
            item.color = getColorForImportance(item.importance);
            cloudItems.add(item);
        }

        // Sort by frequency and limit
        cloudItems.sort((a, b) -> Integer.compare(b.frequency, a.frequency));
        if (cloudItems.size() > maxWords) {
            cloudItems = cloudItems.subList(0, maxWords);
        }

        return cloudItems;
    }

    /**
     * Generate vocabulary network (relationships between words)
     */
    public VocabularyNetwork generateNetwork(String text) {
        VocabularyNetwork network = new VocabularyNetwork();
        
        // Split into sentences
        String[] sentences = text.split("[.!?]+");
        
        // Analyze each sentence for word relationships
        for (String sentence : sentences) {
            List<String> words = extractWords(sentence);
            
            // Create nodes for each word
            for (String word : words) {
                if (!isStopWord(word) && word.length() > 3) {
                    NetworkNode node = network.getOrCreateNode(word);
                    node.frequency++;
                }
            }
            
            // Create edges between words in same sentence
            for (int i = 0; i < words.size(); i++) {
                for (int j = i + 1; j < words.size(); j++) {
                    String word1 = words.get(i);
                    String word2 = words.get(j);
                    
                    if (!isStopWord(word1) && !isStopWord(word2) &&
                        word1.length() > 3 && word2.length() > 3) {
                        network.addConnection(word1, word2);
                    }
                }
            }
        }

        return network;
    }

    /**
     * Categorize vocabulary by topic
     */
    public Map<String, List<String>> categorizeVocabulary(String text) {
        Map<String, List<String>> categories = new HashMap<>();
        
        // Initialize categories
        categories.put("Academic", new ArrayList<>());
        categories.put("Common", new ArrayList<>());
        categories.put("Technical", new ArrayList<>());
        categories.put("Advanced", new ArrayList<>());
        
        Map<String, Integer> wordFrequency = analyzeWordFrequency(text);
        
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            String word = entry.getKey();
            int freq = entry.getValue();
            
            // Simple categorization based on word characteristics
            if (word.length() > 10) {
                categories.get("Advanced").add(word);
            } else if (word.length() > 7) {
                categories.get("Academic").add(word);
            } else if (freq > 3) {
                categories.get("Common").add(word);
            } else {
                categories.get("Technical").add(word);
            }
        }
        
        return categories;
    }

    /**
     * Get vocabulary statistics
     */
    public VocabularyStats getStatistics(String text) {
        VocabularyStats stats = new VocabularyStats();
        
        List<String> allWords = extractWords(text);
        Map<String, Integer> wordFrequency = analyzeWordFrequency(text);
        
        stats.totalWords = allWords.size();
        stats.uniqueWords = wordFrequency.size();
        stats.averageWordLength = calculateAverageWordLength(allWords);
        stats.vocabularyDiversity = (double) stats.uniqueWords / stats.totalWords;
        
        // Count by difficulty
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            String word = entry.getKey();
            if (word.length() <= 4) {
                stats.easyWords++;
            } else if (word.length() <= 7) {
                stats.mediumWords++;
            } else {
                stats.hardWords++;
            }
        }
        
        return stats;
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Integer> analyzeWordFrequency(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        List<String> words = extractWords(text);
        
        for (String word : words) {
            if (!isStopWord(word) && word.length() > 2) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }
        
        return frequency;
    }

    private List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(text.toLowerCase());
        
        while (matcher.find()) {
            words.add(matcher.group());
        }
        
        return words;
    }

    private boolean isStopWord(String word) {
        for (String stopWord : STOP_WORDS) {
            if (stopWord.equals(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int calculateWordSize(int frequency, Map<String, Integer> allFrequencies) {
        int maxFreq = 0;
        for (int freq : allFrequencies.values()) {
            maxFreq = Math.max(maxFreq, freq);
        }
        
        // Scale from 12sp to 48sp
        return 12 + (int) ((frequency / (float) maxFreq) * 36);
    }

    private int calculateImportance(String word, int frequency) {
        // Importance based on frequency and word length
        int lengthScore = Math.min(word.length(), 15);
        int freqScore = Math.min(frequency * 2, 20);
        return (lengthScore + freqScore) / 2;
    }

    private String getColorForImportance(int importance) {
        if (importance > 15) return "#E91E63"; // Pink - Very important
        if (importance > 10) return "#9C27B0"; // Purple - Important
        if (importance > 5) return "#3F51B5";  // Blue - Medium
        return "#4CAF50"; // Green - Less important
    }

    private double calculateAverageWordLength(List<String> words) {
        if (words.isEmpty()) return 0;
        int totalLength = 0;
        for (String word : words) {
            totalLength += word.length();
        }
        return (double) totalLength / words.size();
    }

    // ==================== DATA CLASSES ====================

    public static class WordCloudItem {
        public String word;
        public int frequency;
        public int size; // Font size in sp
        public int importance; // 0-20
        public String color; // Hex color
        public float x; // Position for rendering
        public float y;
    }

    public static class VocabularyNetwork {
        public Map<String, NetworkNode> nodes = new HashMap<>();
        public List<NetworkEdge> edges = new ArrayList<>();

        public NetworkNode getOrCreateNode(String word) {
            if (!nodes.containsKey(word)) {
                NetworkNode node = new NetworkNode();
                node.word = word;
                node.id = nodes.size();
                nodes.put(word, node);
            }
            return nodes.get(word);
        }

        public void addConnection(String word1, String word2) {
            NetworkNode node1 = getOrCreateNode(word1);
            NetworkNode node2 = getOrCreateNode(word2);
            
            // Check if edge already exists
            for (NetworkEdge edge : edges) {
                if ((edge.from == node1.id && edge.to == node2.id) ||
                    (edge.from == node2.id && edge.to == node1.id)) {
                    edge.weight++;
                    return;
                }
            }
            
            // Create new edge
            NetworkEdge edge = new NetworkEdge();
            edge.from = node1.id;
            edge.to = node2.id;
            edge.weight = 1;
            edges.add(edge);
        }
    }

    public static class NetworkNode {
        public int id;
        public String word;
        public int frequency;
        public float x; // Position for graph rendering
        public float y;
        public int size; // Node size
    }

    public static class NetworkEdge {
        public int from; // Node ID
        public int to;   // Node ID
        public int weight; // Connection strength
    }

    public static class VocabularyStats {
        public int totalWords;
        public int uniqueWords;
        public double averageWordLength;
        public double vocabularyDiversity; // 0-1
        public int easyWords;
        public int mediumWords;
        public int hardWords;
    }
}
