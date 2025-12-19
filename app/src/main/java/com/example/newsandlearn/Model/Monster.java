package com.example.newsandlearn.Model;

import java.io.Serializable;

/**
 * Monster - Quái vật trong game
 */
public class Monster implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private int level;
    private int currentHp;
    private int maxHp;
    private int attack;
    private int expReward;
    private String type; // "normal", "boss", "elite"

    public Monster() {
        // Default constructor for Firebase
    }

    public Monster(String id, String name, int level, String type) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.type = type;
        
        // Calculate stats based on level and type
        this.maxHp = 50 + (level * 30);
        this.attack = 5 + (level * 3);
        this.expReward = 20 + (level * 10);
        
        // Boss monsters are stronger
        if ("boss".equals(type)) {
            this.maxHp *= 2;
            this.attack = (int) (attack * 1.5);
            this.expReward *= 3;
        } else if ("elite".equals(type)) {
            this.maxHp = (int) (maxHp * 1.5);
            this.attack = (int) (attack * 1.2);
            this.expReward *= 2;
        }
        
        this.currentHp = this.maxHp;
    }

    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public int getHpPercentage() {
        return (int) ((currentHp / (float) maxHp) * 100);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getExpReward() { return expReward; }
    public void setExpReward(int expReward) { this.expReward = expReward; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
