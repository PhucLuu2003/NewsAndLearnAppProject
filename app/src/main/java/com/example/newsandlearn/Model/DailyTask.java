package com.example.newsandlearn.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * DailyTask Model - Represents daily learning goals/tasks
 * Helps users build consistent learning habits
 */
public class DailyTask implements Parcelable {
    
    public enum TaskType {
        READ_ARTICLE,
        LEARN_WORDS,
        COMPLETE_LESSON,
        WATCH_VIDEO,
        PRACTICE_GRAMMAR,
        SPEND_TIME,
        CUSTOM
    }
    
    public enum TaskStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
    
    private String id;
    private String title;
    private String description;
    private String icon;               // Icon emoji or name
    
    private TaskType type;
    private String typeString;         // For Firebase
    private TaskStatus status;
    private String statusString;       // For Firebase
    
    // Progress tracking
    private int targetValue;           // Target to complete (e.g., 1 article, 10 words)
    private int currentValue;          // Current progress
    private String unit;               // "articles", "words", "minutes", etc.
    
    // Rewards
    private int xpReward;
    private String badgeReward;        // Optional badge icon
    
    // Timing
    private Date assignedDate;         // Date this task was assigned
    private Date completedAt;          // When task was completed
    private Date expiresAt;            // When task expires (usually end of day)
    
    // Metadata
    private int priority;              // 1-5 (1=low, 5=high)
    private boolean isRecurring;       // Daily recurring task
    private boolean isOptional;        // Optional vs required task

    // Constructors
    public DailyTask() {
        this.type = TaskType.READ_ARTICLE;
        this.typeString = "READ_ARTICLE";
        this.status = TaskStatus.NOT_STARTED;
        this.statusString = "NOT_STARTED";
        this.currentValue = 0;
        this.assignedDate = new Date();
        this.isRecurring = true;
        this.isOptional = false;
        this.priority = 3;
    }

    public DailyTask(String id, String title, String description, TaskType type,
                    int targetValue, String unit, int xpReward) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.typeString = type.name();
        this.targetValue = targetValue;
        this.unit = unit;
        this.xpReward = xpReward;
        this.icon = getDefaultIcon(type);
    }

    // Parcelable implementation
    protected DailyTask(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        icon = in.readString();
        typeString = in.readString();
        type = typeString != null ? TaskType.valueOf(typeString) : TaskType.READ_ARTICLE;
        statusString = in.readString();
        status = statusString != null ? TaskStatus.valueOf(statusString) : TaskStatus.NOT_STARTED;
        targetValue = in.readInt();
        currentValue = in.readInt();
        unit = in.readString();
        xpReward = in.readInt();
        badgeReward = in.readString();
        long assignedDateTime = in.readLong();
        assignedDate = assignedDateTime != -1 ? new Date(assignedDateTime) : new Date();
        long completedAtTime = in.readLong();
        completedAt = completedAtTime != -1 ? new Date(completedAtTime) : null;
        long expiresAtTime = in.readLong();
        expiresAt = expiresAtTime != -1 ? new Date(expiresAtTime) : null;
        priority = in.readInt();
        isRecurring = in.readByte() != 0;
        isOptional = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(icon);
        dest.writeString(typeString);
        dest.writeString(statusString);
        dest.writeInt(targetValue);
        dest.writeInt(currentValue);
        dest.writeString(unit);
        dest.writeInt(xpReward);
        dest.writeString(badgeReward);
        dest.writeLong(assignedDate != null ? assignedDate.getTime() : -1);
        dest.writeLong(completedAt != null ? completedAt.getTime() : -1);
        dest.writeLong(expiresAt != null ? expiresAt.getTime() : -1);
        dest.writeInt(priority);
        dest.writeByte((byte) (isRecurring ? 1 : 0));
        dest.writeByte((byte) (isOptional ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DailyTask> CREATOR = new Creator<DailyTask>() {
        @Override
        public DailyTask createFromParcel(Parcel in) {
            return new DailyTask(in);
        }

        @Override
        public DailyTask[] newArray(int size) {
            return new DailyTask[size];
        }
    };

    // Helper methods
    
    /**
     * Update progress and check if task is completed
     */
    public boolean updateProgress(int value) {
        currentValue = Math.min(value, targetValue);
        
        if (currentValue > 0 && currentValue < targetValue) {
            status = TaskStatus.IN_PROGRESS;
            statusString = "IN_PROGRESS";
        } else if (currentValue >= targetValue) {
            complete();
            return true;
        }
        return false;
    }
    
    /**
     * Increment progress by 1
     */
    public boolean incrementProgress() {
        return updateProgress(currentValue + 1);
    }
    
    /**
     * Mark task as completed
     */
    public void complete() {
        status = TaskStatus.COMPLETED;
        statusString = "COMPLETED";
        currentValue = targetValue;
        completedAt = new Date();
    }
    
    /**
     * Get progress percentage
     */
    public int getProgress() {
        if (targetValue == 0) return 0;
        return Math.min(100, (currentValue * 100) / targetValue);
    }
    
    /**
     * Check if task is completed
     */
    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }
    
    /**
     * Check if task is expired
     */
    public boolean isExpired() {
        if (expiresAt == null) return false;
        return new Date().after(expiresAt);
    }
    
    /**
     * Get default icon for task type
     */
    private String getDefaultIcon(TaskType type) {
        switch (type) {
            case READ_ARTICLE: return "📰";
            case LEARN_WORDS: return "📝";
            case COMPLETE_LESSON: return "✅";
            case WATCH_VIDEO: return "🎥";
            case PRACTICE_GRAMMAR: return "📖";
            case SPEND_TIME: return "⏱️";
            default: return "🎯";
        }
    }
    
    /**
     * Get progress text (e.g., "5/10 words")
     */
    public String getProgressText() {
        return currentValue + "/" + targetValue + " " + (unit != null ? unit : "");
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

    public TaskType getType() { return type; }
    public void setType(TaskType type) { 
        this.type = type;
        this.typeString = type.name();
    }

    public String getTypeString() { return typeString; }
    public void setTypeString(String typeString) { 
        this.typeString = typeString;
        try {
            this.type = TaskType.valueOf(typeString);
        } catch (Exception e) {
            this.type = TaskType.READ_ARTICLE;
        }
    }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { 
        this.status = status;
        this.statusString = status.name();
    }

    public String getStatusString() { return statusString; }
    public void setStatusString(String statusString) { 
        this.statusString = statusString;
        try {
            this.status = TaskStatus.valueOf(statusString);
        } catch (Exception e) {
            this.status = TaskStatus.NOT_STARTED;
        }
    }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public int getCurrentValue() { return currentValue; }
    public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public String getBadgeReward() { return badgeReward; }
    public void setBadgeReward(String badgeReward) { this.badgeReward = badgeReward; }

    public Date getAssignedDate() { return assignedDate; }
    public void setAssignedDate(Date assignedDate) { this.assignedDate = assignedDate; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public Date getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Date expiresAt) { this.expiresAt = expiresAt; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public boolean isOptional() { return isOptional; }
    public void setOptional(boolean optional) { isOptional = optional; }
}
