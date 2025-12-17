package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Achievement Model - Represents unlockable achievements/badges
 * Used for gamification and user motivation
 */
public class Achievement implements Parcelable {
    
    public enum AchievementCategory {
        READING,
        VOCABULARY,
        GRAMMAR,
        STREAK,
        GENERAL,
        SPECIAL
    }
    
    public enum AchievementTier {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND
    }
    
    private String id;
    private String title;
    private String description;
    private String icon;               // Icon name or emoji
    private String iconUrl;            // URL to icon image
    
    private AchievementCategory category;
    private String categoryString;     // For Firebase
    private AchievementTier tier;
    private String tierString;         // For Firebase
    
    // Unlock conditions
    private String conditionType;      // "articles_read", "words_learned", "streak", etc.
    private int conditionTarget;       // Target value to unlock
    private int conditionCurrent;      // Current progress (user-specific)
    
    // Rewards
    private int xpReward;
    private String badgeColor;         // Color for the badge
    
    // Status (user-specific)
    private boolean isUnlocked;
    private Date unlockedAt;
    private boolean isNew;             // Newly unlocked, not yet viewed
    
    // Metadata
    private int rarity;                // 1-5 (how rare/difficult)
    private Date createdAt;

    // Constructors
    public Achievement() {
        this.category = AchievementCategory.GENERAL;
        this.categoryString = "GENERAL";
        this.tier = AchievementTier.BRONZE;
        this.tierString = "BRONZE";
        this.isUnlocked = false;
        this.isNew = false;
        this.conditionCurrent = 0;
        this.createdAt = new Date();
    }

    public Achievement(String id, String title, String description, String icon,
                      AchievementCategory category, AchievementTier tier,
                      String conditionType, int conditionTarget, int xpReward) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.categoryString = category.name();
        this.tier = tier;
        this.tierString = tier.name();
        this.conditionType = conditionType;
        this.conditionTarget = conditionTarget;
        this.xpReward = xpReward;
        this.badgeColor = getTierColor(tier);
    }

    // Parcelable implementation
    protected Achievement(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        icon = in.readString();
        iconUrl = in.readString();
        categoryString = in.readString();
        category = categoryString != null ? AchievementCategory.valueOf(categoryString) : AchievementCategory.GENERAL;
        tierString = in.readString();
        tier = tierString != null ? AchievementTier.valueOf(tierString) : AchievementTier.BRONZE;
        conditionType = in.readString();
        conditionTarget = in.readInt();
        conditionCurrent = in.readInt();
        xpReward = in.readInt();
        badgeColor = in.readString();
        isUnlocked = in.readByte() != 0;
        long unlockedAtTime = in.readLong();
        unlockedAt = unlockedAtTime != -1 ? new Date(unlockedAtTime) : null;
        isNew = in.readByte() != 0;
        rarity = in.readInt();
        long createdAtTime = in.readLong();
        createdAt = createdAtTime != -1 ? new Date(createdAtTime) : new Date();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(icon);
        dest.writeString(iconUrl);
        dest.writeString(categoryString);
        dest.writeString(tierString);
        dest.writeString(conditionType);
        dest.writeInt(conditionTarget);
        dest.writeInt(conditionCurrent);
        dest.writeInt(xpReward);
        dest.writeString(badgeColor);
        dest.writeByte((byte) (isUnlocked ? 1 : 0));
        dest.writeLong(unlockedAt != null ? unlockedAt.getTime() : -1);
        dest.writeByte((byte) (isNew ? 1 : 0));
        dest.writeInt(rarity);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Achievement> CREATOR = new Creator<Achievement>() {
        @Override
        public Achievement createFromParcel(Parcel in) {
            return new Achievement(in);
        }

        @Override
        public Achievement[] newArray(int size) {
            return new Achievement[size];
        }
    };

    // Helper methods
    
    /**
     * Check if achievement should be unlocked based on current progress
     */
    public boolean checkUnlock(int currentValue) {
        if (!isUnlocked && currentValue >= conditionTarget) {
            unlock();
            return true;
        }
        conditionCurrent = Math.min(currentValue, conditionTarget);
        return false;
    }
    
    /**
     * Unlock this achievement
     */
    public void unlock() {
        isUnlocked = true;
        unlockedAt = new Date();
        isNew = true;
        conditionCurrent = conditionTarget;
    }
    
    /**
     * Get progress percentage
     */
    public int getProgress() {
        if (conditionTarget == 0) return 0;
        return Math.min(100, (conditionCurrent * 100) / conditionTarget);
    }
    
    /**
     * Get tier color
     */
    private String getTierColor(AchievementTier tier) {
        switch (tier) {
            case BRONZE: return "#CD7F32";
            case SILVER: return "#C0C0C0";
            case GOLD: return "#FFD700";
            case PLATINUM: return "#E5E4E2";
            case DIAMOND: return "#B9F2FF";
            default: return "#CD7F32";
        }
    }
    
    /**
     * Get display icon (emoji or default)
     */
    public String getDisplayIcon() {
        if (icon != null && !icon.isEmpty()) {
            return icon;
        }
        // Default icons based on category
        switch (category) {
            case READING: return "📚";
            case VOCABULARY: return "🧠";
            case GRAMMAR: return "📖";
            case STREAK: return "🔥";
            case SPECIAL: return "⭐";
            default: return "🏆";
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public AchievementCategory getCategory() { return category; }
    public void setCategory(AchievementCategory category) { 
        this.category = category;
        this.categoryString = category.name();
    }

    public String getCategoryString() { return categoryString; }
    public void setCategoryString(String categoryString) { 
        this.categoryString = categoryString;
        try {
            this.category = AchievementCategory.valueOf(categoryString);
        } catch (Exception e) {
            this.category = AchievementCategory.GENERAL;
        }
    }

    public AchievementTier getTier() { return tier; }
    public void setTier(AchievementTier tier) { 
        this.tier = tier;
        this.tierString = tier.name();
        this.badgeColor = getTierColor(tier);
    }

    public String getTierString() { return tierString; }
    public void setTierString(String tierString) { 
        this.tierString = tierString;
        try {
            this.tier = AchievementTier.valueOf(tierString);
        } catch (Exception e) {
            this.tier = AchievementTier.BRONZE;
        }
    }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public int getConditionTarget() { return conditionTarget; }
    public void setConditionTarget(int conditionTarget) { this.conditionTarget = conditionTarget; }

    public int getConditionCurrent() { return conditionCurrent; }
    public void setConditionCurrent(int conditionCurrent) { this.conditionCurrent = conditionCurrent; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }

    public Date getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Date unlockedAt) { this.unlockedAt = unlockedAt; }

    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }

    public int getRarity() { return rarity; }
    public void setRarity(int rarity) { this.rarity = rarity; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Additional helper methods for adapter compatibility
    public String getName() { return title; }
    public String getType() { return tierString != null ? tierString.toLowerCase() : "bronze"; }
    public int getCurrentProgress() { return conditionCurrent; }
    public int getRequiredProgress() { return conditionTarget; }
    public boolean isNewlyUnlocked() { return isNew; }
}
