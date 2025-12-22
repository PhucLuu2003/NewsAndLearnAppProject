package com.example.newsandlearn.Utils;

import com.example.newsandlearn.Model.MemoryPalace;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary storage for Memory Palace data
 * In production, this should be replaced with Firebase/Database
 */
public class MemoryPalaceStorage {
    
    private static MemoryPalaceStorage instance;
    private Map<String, MemoryPalace> palaces;
    private String currentPalaceId;
    
    private MemoryPalaceStorage() {
        palaces = new HashMap<>();
    }
    
    public static MemoryPalaceStorage getInstance() {
        if (instance == null) {
            instance = new MemoryPalaceStorage();
        }
        return instance;
    }
    
    public void setCurrentPalace(MemoryPalace palace) {
        this.currentPalaceId = palace.getId();
        palaces.put(palace.getId(), palace);
    }
    
    public MemoryPalace getCurrentPalace() {
        if (currentPalaceId != null && palaces.containsKey(currentPalaceId)) {
            return palaces.get(currentPalaceId);
        }
        return null;
    }
    
    public void saveWordToRoom(int roomPosition, MemoryPalace.WordMemory wordMemory) {
        MemoryPalace palace = getCurrentPalace();
        if (palace != null && roomPosition >= 0 && roomPosition < palace.getRooms().size()) {
            palace.addWordToRoom(roomPosition, wordMemory);
        }
    }
    
    public MemoryPalace.Room getRoom(int roomPosition) {
        MemoryPalace palace = getCurrentPalace();
        if (palace != null && roomPosition >= 0 && roomPosition < palace.getRooms().size()) {
            return palace.getRooms().get(roomPosition);
        }
        return null;
    }
}
