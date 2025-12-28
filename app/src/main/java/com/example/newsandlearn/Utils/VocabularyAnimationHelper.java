package com.example.newsandlearn.Utils;

import com.example.newsandlearn.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 🎨 VocabularyAnimationHelper - Maps vocabulary categories to Lottie animations
 * 
 * HƯỚNG DẪN THÊM ANIMATION MỚI:
 * 1. Vào https://lottiefiles.com/free-animations
 * 2. Tìm animation phù hợp với category
 * 3. Copy URL của file JSON
 * 4. Thêm vào CATEGORY_URLS.put("TênCategory", "URL");
 * 
 * LƯU Ý: URL phải là direct link đến file .json
 */
public class VocabularyAnimationHelper {
    
    // Local animation resources (fallback)
    private static final Map<String, Integer> CATEGORY_ANIMATIONS = new HashMap<>();
    
    // Lottie URLs for categories - THÊM URL TẠI ĐÂY
    private static final Map<String, String> CATEGORY_URLS = new HashMap<>();
    
    static {
        // ========================================
        // LOCAL ANIMATIONS (fallback khi URL fail)
        // ========================================
        CATEGORY_ANIMATIONS.put("Greetings", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Numbers", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Colors", R.raw.confetti);
        CATEGORY_ANIMATIONS.put("Food", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Family", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Animals", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Weather", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Travel", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Shopping", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Body", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Places", R.raw.loading);
        CATEGORY_ANIMATIONS.put("Time", R.raw.loading);
        
        // ========================================
        // LOTTIE URLS - Animations từ LottieFiles
        // Bạn có thể thay đổi URL để dùng animation khác
        // ========================================
        
        // 👋 Greetings - Hi/Hello animation
        // Nguồn: https://lottiefiles.com/free-animation/hihello-5OosfYYPA3
        CATEGORY_URLS.put("Greetings", 
            "https://assets-v2.lottiefiles.com/a/54d36c2a-1186-11ee-9a7c-63ef24e83d34/lNJ36IJqer.lottie");
        
        // 🔢 Numbers - Số
        CATEGORY_URLS.put("Numbers", 
            "https://lottie.host/embed/fd0c4f50-0377-4d51-80c2-d3f0f1f29fd1/7xtfDHGlJq.json");
        
        // 🎨 Colors - Màu sắc
        CATEGORY_URLS.put("Colors", 
            "https://lottie.host/embed/7b8c6ff8-0dfc-4498-a09f-e9c9f5c5e9eb/o7xPNdXwz8.json");
        
        // 🍔 Food - Đồ ăn
        CATEGORY_URLS.put("Food", 
            "https://assets9.lottiefiles.com/packages/lf20_l22gyrgm.json");
        
        // 👨‍👩‍👧‍👦 Family - Gia đình
        CATEGORY_URLS.put("Family", 
            "https://lottie.host/embed/d93c3f54-3e6c-4426-8f03-0e50ff4e4e6c/QV6xzf7o5H.json");
        
        // 🐾 Animals - Động vật
        CATEGORY_URLS.put("Animals", 
            "https://lottie.host/embed/51c6b9f3-0c7a-4c40-a16b-89b1e7c8b5f4/t7rHXR5QNK.json");
        
        // 🌤️ Weather - Thời tiết
        CATEGORY_URLS.put("Weather", 
            "https://lottie.host/embed/fe3b99d6-f1e3-4fa3-8d0f-87a3ffcd0b0f/j7xMLBt5sP.json");
        
        // ✈️ Travel - Du lịch
        CATEGORY_URLS.put("Travel", 
            "https://lottie.host/embed/8e1e42f5-c3d6-4c35-b5ac-f5c6e5d42d4f/RpXnH4sQ5g.json");
        
        // 🛒 Shopping - Mua sắm
        CATEGORY_URLS.put("Shopping", 
            "https://lottie.host/embed/a5b6c8d0-e1f2-4c3d-b4a5-6c7d8e9f0a1b/ShOpPiNg123.json");
        
        // 💪 Body - Cơ thể
        CATEGORY_URLS.put("Body", 
            "https://lottie.host/embed/b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e/BoDy789xyz.json");
        
        // 📍 Places - Địa điểm
        CATEGORY_URLS.put("Places", 
            "https://lottie.host/embed/c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f/PlAcEs456.json");
        
        // ⏰ Time - Thời gian
        CATEGORY_URLS.put("Time", 
            "https://lottie.host/embed/d4e5f6a7-b8c9-4d0e-1f2a-3b4c5d6e7f8a/TiMe123abc.json");
    }
    
    /**
     * Get local animation resource for a category
     * @param category The category name
     * @return Animation resource ID (local raw resource)
     */
    public static int getAnimationResource(String category) {
        if (category == null) {
            return R.raw.loading;
        }
        
        Integer anim = CATEGORY_ANIMATIONS.get(category);
        return anim != null ? anim : R.raw.loading;
    }
    
    /**
     * Get Lottie animation URL for a category
     * @param category The category name
     * @return URL string or null if not available
     */
    public static String getAnimationUrl(String category) {
        if (category == null) {
            return null;
        }
        return CATEGORY_URLS.get(category);
    }
    
    /**
     * Get animation for a specific word
     * @param word The vocabulary word
     * @param category The category of the word
     * @return Animation resource ID
     */
    public static int getAnimationForWord(String word, String category) {
        // Use category-based animation
        return getAnimationResource(category);
    }
    
    /**
     * Check if we should load from URL
     */
    public static boolean shouldLoadFromUrl(String category) {
        return CATEGORY_URLS.containsKey(category);
    }
    
    /**
     * Check if animation exists for category
     */
    public static boolean hasAnimation(String category) {
        return CATEGORY_ANIMATIONS.containsKey(category);
    }
    
    /**
     * Thêm URL mới cho category
     * Có thể gọi runtime nếu cần
     */
    public static void addCategoryUrl(String category, String url) {
        CATEGORY_URLS.put(category, url);
    }
    
    /**
     * Xóa URL của category
     */
    public static void removeCategoryUrl(String category) {
        CATEGORY_URLS.remove(category);
    }
}
