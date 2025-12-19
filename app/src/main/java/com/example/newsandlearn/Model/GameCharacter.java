package com.example.newsandlearn.Model;

import java.io.Serializable;

/**
 * GameCharacter - Nhân vật người chơi trong RPG Game
 */
public class GameCharacter implements Serializable {
    private String id;
    private String name;
    private String avatarUrl;
    private int level;
    private int currentExp;
    private int maxExp;
    private int currentHp;
    private int maxHp;
    private int attack;
    private int defense;
    private String characterClass; // "warrior", "mage", "archer"

    public GameCharacter() {
        // Default constructor for Firebase
    }

    public GameCharacter(String id, String name, String characterClass) {
        this.id = id;
        this.name = name;
        this.characterClass = characterClass;
        this.level = 1;
        this.currentExp = 0;
        this.maxExp = 100;
        this.maxHp = 100;
        this.currentHp = 100;
        this.attack = 10;
        this.defense = 5;
    }

    // Level up logic
    public void gainExp(int exp) {
        currentExp += exp;
        while (currentExp >= maxExp) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        currentExp -= maxExp;
        maxExp = (int) (maxExp * 1.5);
        maxHp += 20;
        currentHp = maxHp; // Full heal on level up
        attack += 5;
        defense += 2;
    }

    public void takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense);
        currentHp = Math.max(0, currentHp - actualDamage);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getCurrentExp() { return currentExp; }
    public void setCurrentExp(int currentExp) { this.currentExp = currentExp; }

    public int getMaxExp() { return maxExp; }
    public void setMaxExp(int maxExp) { this.maxExp = maxExp; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public String getCharacterClass() { return characterClass; }
    public void setCharacterClass(String characterClass) { this.characterClass = characterClass; }
}
