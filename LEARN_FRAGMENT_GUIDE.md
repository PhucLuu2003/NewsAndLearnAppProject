# 📚 Learn Fragment - Firebase Integration Guide

## ✨ Tính năng đã hoàn thành

### 1. **Daily Goal Tracking** 🎯
- Hiển thị tiến độ mục tiêu hàng ngày (ví dụ: 2/5 completed)
- Progress bar động với animation mượt mà
- Tự động tạo mục tiêu mới mỗi ngày
- Lưu trữ và đồng bộ với Firebase

### 2. **Module Progress Tracking** 📊
- Theo dõi tiến độ cho 6 modules:
  - Vocabulary (Từ vựng)
  - Grammar (Ngữ pháp)
  - Listening (Nghe)
  - Speaking (Nói)
  - Reading (Đọc)
  - Writing (Viết)
- Lưu lịch sử truy cập module
- Tự động cập nhật khi user hoàn thành bài học

### 3. **Beautiful UI Design** 🎨
- Gradient backgrounds cho mỗi module
- Smooth animations khi load và click
- Modern card design với shadows
- Responsive layout 2 cột

### 4. **Firebase Integration** 🔥
- Real-time data synchronization
- Automatic user progress tracking
- Module access logging
- Daily goal management

---

## 🗂️ Cấu trúc Firebase Firestore

### Collection: `users/{userId}`

#### Document: `users/{userId}`
```json
{
  "username": "John Doe",
  "email": "john@example.com",
  "level": "Beginner",
  "createdAt": "2025-12-17T14:30:00Z"
}
```

#### Sub-collection: `daily_goals/{date}`
```json
{
  "completed": 2,
  "total": 5,
  "date": "2025-12-17"
}
```

**Mô tả:**
- `completed`: Số lượng tasks đã hoàn thành trong ngày
- `total`: Tổng số tasks cần hoàn thành (mặc định: 5)
- `date`: Ngày theo format yyyy-MM-dd

#### Sub-collection: `module_progress/current`
```json
{
  "vocabulary": 75,
  "grammar": 60,
  "listening": 45,
  "speaking": 30,
  "reading": 55,
  "writing": 20
}
```

**Mô tả:**
- Mỗi field lưu % tiến độ hoàn thành của module (0-100)
- Document ID luôn là "current" để dễ truy cập

#### Sub-collection: `module_access/{auto-id}`
```json
{
  "module": "Vocabulary",
  "timestamp": "2025-12-17 14:30:45"
}
```

**Mô tả:**
- Ghi lại mỗi lần user truy cập vào một module
- Dùng để phân tích hành vi học tập

---

## 🔧 Cách sử dụng

### 1. Cập nhật Daily Goal
Khi user hoàn thành một task:

```java
String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

FirebaseFirestore.getInstance()
    .collection("users").document(userId)
    .collection("daily_goals").document(today)
    .update("completed", FieldValue.increment(1))
    .addOnSuccessListener(aVoid -> {
        // Refresh UI
        loadDailyGoalFromFirebase();
    });
```

### 2. Cập nhật Module Progress
Khi user hoàn thành một lesson trong module:

```java
String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
String moduleName = "vocabulary"; // hoặc grammar, listening, etc.

FirebaseFirestore.getInstance()
    .collection("users").document(userId)
    .collection("module_progress").document("current")
    .update(moduleName, FieldValue.increment(5)) // Tăng 5%
    .addOnSuccessListener(aVoid -> {
        // Refresh UI
        loadModuleProgressFromFirebase();
    });
```

### 3. Reset Daily Goal (Chạy mỗi ngày mới)
```java
String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

Map<String, Object> newGoal = new HashMap<>();
newGoal.put("completed", 0);
newGoal.put("total", 5);
newGoal.put("date", today);

FirebaseFirestore.getInstance()
    .collection("users").document(userId)
    .collection("daily_goals").document(today)
    .set(newGoal);
```

---

## 🎯 Các chức năng cần thêm (TODO)

### 1. **Achievement System** 🏆
- Thêm badges khi đạt milestones
- Unlock special features
- Leaderboard

### 2. **Streak Tracking** 🔥
- Đếm số ngày học liên tục
- Hiển thị streak counter
- Reminder notifications

### 3. **Detailed Statistics** 📈
- Biểu đồ tiến độ theo thời gian
- So sánh với người dùng khác
- Weekly/Monthly reports

### 4. **Personalized Recommendations** 💡
- Gợi ý module dựa trên tiến độ
- Adaptive difficulty
- Smart scheduling

---

## 🎨 Customization

### Thay đổi màu gradient cho modules
Chỉnh sửa các file trong `res/drawable/`:
- `module_vocabulary_bg.xml` - Màu tím
- `module_grammar_bg.xml` - Màu xanh dương
- `module_listening_bg.xml` - Màu xanh lá
- `module_speaking_bg.xml` - Màu cam
- `module_reading_bg.xml` - Màu hồng
- `module_writing_bg.xml` - Màu đỏ

### Thay đổi số lượng daily goals
Trong `LearnFragment.java`, method `createDefaultDailyGoal()`:
```java
dailyGoal.put("total", 5); // Thay đổi số này
```

---

## 📱 Testing

### Test với Firebase Emulator (Optional)
```bash
firebase emulators:start
```

### Test trên thiết bị thật
1. Build APK: `.\gradlew.bat assembleDebug`
2. Install: `adb install -r app\build\outputs\apk\debug\app-debug.apk`
3. Kiểm tra Firebase Console để xem data

---

## 🐛 Troubleshooting

### Lỗi: "User not authenticated"
- Đảm bảo user đã đăng nhập
- Check `FirebaseAuth.getInstance().getCurrentUser()` không null

### Lỗi: "Permission denied"
- Kiểm tra Firestore Security Rules
- Đảm bảo user có quyền read/write collection của mình

### Data không cập nhật
- Check internet connection
- Verify Firebase configuration
- Check Logcat cho error messages

---

## 📝 Notes

- Fragment tự động refresh data khi `onResume()`
- Animations được tối ưu để không lag
- Tất cả Firebase operations đều có fallback với default values
- UI vẫn hoạt động ngay cả khi offline (hiển thị cached data)

---

**Created by:** AI Assistant
**Last Updated:** 2025-12-17
**Version:** 1.0
