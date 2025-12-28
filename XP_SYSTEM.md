# 🎮 XP & Streak System Documentation

## 📊 Overview
Hệ thống XP và Streak được quản lý tập trung qua `XPManager` với đồng bộ Firebase realtime.

## 🎯 XP Rewards

### Hoạt động cơ bản:
- **Đọc Article**: 50 XP
- **Hoàn thành Lesson**: 100 XP
- **Học từ vựng**: 10 XP/từ
- **Hoàn thành Quiz**: 75 XP
- **Flashcard Session**: 30 XP
- **Xem Video hoàn chỉnh**: 60 XP
- **Đạt Daily Goal**: 150 XP

### Streak Bonus:
- **Mỗi ngày streak**: 25 XP × số ngày streak
- Ví dụ: Streak 7 ngày = 25 × 7 = 175 XP bonus

## 📈 Level System

### Công thức:
```
Level = (Total XP / 500) + 1
```

### Ví dụ:
- 0-499 XP → Level 1
- 500-999 XP → Level 2
- 1000-1499 XP → Level 3
- 2000-2499 XP → Level 5

### Progress to Next Level:
```
Progress % = ((Current XP - XP for current level) / 500) × 100
```

## 🔥 Streak System

### Quy tắc:
1. **Cùng ngày**: Streak không thay đổi
2. **Ngày tiếp theo**: Streak +1
3. **Bỏ lỡ >1 ngày**: Streak reset về 1

### Tính toán:
- So sánh `lastActive` với ngày hiện tại
- Chỉ tính ngày (bỏ qua giờ/phút/giây)
- Tự động cập nhật khi user hoạt động

## 💻 Usage

### 1. Thêm XP:
```java
XPManager.getInstance().addXP(
    50,                    // XP amount
    "read_article",        // Source for tracking
    new XPManager.XPCallback() {
        @Override
        public void onSuccess(XPManager.XPResult result) {
            if (result.leveledUp) {
                // Show level up animation
                showLevelUpDialog(result.level);
            }
            // Update UI
            updateXPDisplay(result.totalXP, result.level);
        }
        
        @Override
        public void onError(String error) {
            Log.e(TAG, "XP Error: " + error);
        }
    }
);
```

### 2. Cập nhật Streak:
```java
XPManager.getInstance().updateStreak(
    new XPManager.StreakCallback() {
        @Override
        public void onSuccess(XPManager.StreakUpdateResult result) {
            if (result.isNewDay) {
                if (result.streakBroken) {
                    // Show streak broken message
                    Toast.makeText(context, "Streak reset! Start again!", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    // Show streak increased
                    Toast.makeText(context, "🔥 " + result.currentStreak + " day streak!", 
                        Toast.LENGTH_SHORT).show();
                }
            }
            // Update UI
            updateStreakDisplay(result.currentStreak);
        }
        
        @Override
        public void onError(String error) {
            Log.e(TAG, "Streak Error: " + error);
        }
    }
);
```

### 3. Tính toán Level:
```java
// Get level from XP
int level = XPManager.calculateLevel(1250); // Returns 3

// Get XP needed for next level
long xpNeeded = XPManager.getXPForNextLevel(3); // Returns 1500

// Get progress percentage
int progress = XPManager.getLevelProgress(1250); // Returns 50%
```

## 🔄 Firebase Structure

### User Document (`users/{userId}`):
```json
{
  "xp": 1250,
  "level": 3,
  "currentStreak": 7,
  "longestStreak": 15,
  "lastActive": "2025-12-28T10:30:00Z"
}
```

### XP Logs (`xp_logs/{logId}`):
```json
{
  "userId": "user123",
  "xpGained": 50,
  "source": "read_article",
  "newTotal": 1250,
  "newLevel": 3,
  "timestamp": "2025-12-28T10:30:00Z"
}
```

## 🎨 UI Integration

### HomeFragment:
```java
// Load and display XP/Streak
XPManager.getInstance().updateStreak(result -> {
    streakCount.setText(String.valueOf(result.currentStreak));
});
```

### After completing activity:
```java
// Award XP
XPManager.getInstance().addXP(
    XPManager.XP_COMPLETE_LESSON,
    "lesson_complete",
    result -> {
        // Show success with XP earned
        showCompletionDialog(result.xpGained, result.leveledUp);
    }
);
```

## 📝 Migration Guide

### Old Code:
```java
// ❌ DON'T USE
progressManager.addXP(50, callback);
userProgress.addXP(50);
```

### New Code:
```java
// ✅ USE THIS
XPManager.getInstance().addXP(50, "source", callback);
```

## 🔐 Security

- Sử dụng **Firebase Transactions** để tránh race conditions
- Tất cả operations đều atomic
- Logging tự động cho analytics
- Validation ở cả client và server (Firestore Rules)

## 📊 Analytics

XP logs được lưu tự động để:
- Theo dõi nguồn XP chính
- Phát hiện abuse/cheating
- Tối ưu hóa rewards
- Hiển thị statistics

## 🎯 Best Practices

1. **Luôn dùng XPManager** - Không update XP trực tiếp
2. **Provide source** - Để tracking và analytics
3. **Handle callbacks** - Để update UI và show feedback
4. **Update streak daily** - Khi user mở app hoặc hoàn thành activity
5. **Show feedback** - Level up animations, streak notifications

## 🚀 Future Enhancements

- [ ] XP Multipliers (events, premium)
- [ ] Streak freeze items
- [ ] Weekly/Monthly XP leaderboards
- [ ] Achievement integration
- [ ] XP shop/rewards
