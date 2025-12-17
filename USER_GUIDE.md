# 📚 HƯỚNG DẪN SỬ DỤNG CHI TIẾT - LEARN FRAGMENT

## 📖 MỤC LỤC
1. [Tổng quan](#tổng-quan)
2. [Cách sử dụng từng chức năng](#cách-sử-dụng-từng-chức-năng)
3. [Cách thêm dữ liệu vào Firebase](#cách-thêm-dữ-liệu-vào-firebase)
4. [Cấu trúc dữ liệu Firebase](#cấu-trúc-dữ-liệu-firebase)
5. [Troubleshooting](#troubleshooting)

---

## 🎯 TỔNG QUAN

### Màn hình Learn bao gồm:
- **Daily Goal Card** - Hiển thị tiến độ mục tiêu hàng ngày (2/5 completed)
- **6 Learning Modules:**
  1. 📚 **Vocabulary** - Học từ vựng
  2. 📖 **Grammar** - Học ngữ pháp
  3. 🎧 **Listening** - Luyện nghe
  4. 🎤 **Speaking** - Luyện nói
  5. 📰 **Reading** - Luyện đọc
  6. ✍️ **Writing** - Luyện viết

---

## 🚀 CÁCH SỬ DỤNG TỪNG CHỨC NĂNG

### 1. 📚 VOCABULARY MODULE

#### A. Xem danh sách từ vựng:
1. Mở app → Tab "Learn"
2. Click vào card **Vocabulary** (màu tím)
3. Xem danh sách từ đã học

#### B. Thêm từ vựng mới:
1. Trong Vocabulary screen, click nút **"+"** (Add Word)
2. Nhập thông tin:
   - **Word**: Từ tiếng Anh (bắt buộc)
   - **Definition**: Nghĩa (bắt buộc)
   - **Example**: Câu ví dụ (tùy chọn)
   - **Level**: Chọn Beginner/Intermediate/Advanced
3. Click **"Save"**
4. ✅ Từ được lưu vào Firebase
5. ✅ Progress tự động tăng +1%

#### C. Filter từ vựng:
- **All** - Tất cả từ
- **Learning** - Từ đang học (mastery 1-4)
- **Mastered** - Từ đã thuộc (mastery 5)
- **Favorites** - Từ yêu thích

#### D. Nghe phát âm:
- Click icon 🔊 bên cạnh từ
- Text-to-Speech sẽ đọc từ

#### E. Đánh dấu yêu thích:
- Click icon ❤️ để thêm/bỏ favorite

---

### 2. 📖 GRAMMAR MODULE

#### A. Xem danh sách bài học:
1. Click vào card **Grammar** (màu xanh dương)
2. Xem danh sách grammar lessons

#### B. Filter theo level:
- **All Levels** - Tất cả
- **A1-A2** - Beginner
- **B1-B2** - Intermediate
- **C1-C2** - Advanced

#### C. Học bài grammar:
1. Click vào bài học muốn học
2. Đọc:
   - 📖 **Explanation** - Giải thích
   - 💡 **Examples** - Ví dụ
   - 📝 **Rules** - Quy tắc (nếu có)
3. Cuộn xuống cuối
4. Click **"Mark as Complete"**
5. ✅ Bài học được đánh dấu hoàn thành
6. ✅ Progress tự động tăng +5%
7. ✅ Daily goal tăng +1

---

### 3. 📰 READING MODULE

#### A. Đọc bài báo:
1. Click vào card **Reading** (màu hồng)
2. Chọn bài báo muốn đọc
3. Click vào bài báo

#### B. Trong Article Detail:
1. **Scroll để đọc** - Progress bar tự động cập nhật
2. **Progress tracking:**
   - 0% - Mới mở
   - 25% - Đọc được 1/4
   - 50% - Đọc được 1/2
   - 75% - Đọc được 3/4
   - 100% - Hoàn thành ✅

3. **Khi đạt 100%:**
   - ✅ Article được đánh dấu completed
   - ✅ Reading progress tăng +5%
   - ✅ Daily goal tăng +1
   - ✅ Study time được ghi nhận

#### C. Thông tin bài báo:
- **Category** - Chủ đề (Technology, Business, etc.)
- **Level** - Độ khó (Beginner, Intermediate, Advanced)
- **Reading Time** - Thời gian đọc ước tính
- **Publish Date** - Ngày xuất bản

---

### 4. 🎧 LISTENING MODULE

#### Tính năng (đang phát triển):
- Nghe audio exercises
- Xem transcript
- Làm comprehension quiz
- Track listening progress

---

### 5. 🎤 SPEAKING MODULE

#### Tính năng (đang phát triển):
- Record giọng nói
- Pronunciation scoring
- Practice conversations
- Get feedback

---

### 6. ✍️ WRITING MODULE

#### Tính năng (đang phát triển):
- Viết essays
- Grammar check
- Get AI feedback
- Track writing progress

---

## 🔥 CÁCH THÊM DỮ LIỆU VÀO FIREBASE

### 📝 CHUẨN BỊ

1. **Mở Firebase Console:**
   - Truy cập: https://console.firebase.google.com
   - Chọn project của bạn
   - Vào **Firestore Database**

2. **Chọn Collection:**
   - Vocabulary → `users/{userId}/vocabulary`
   - Grammar → `grammar_lessons`
   - Articles → `articles`

---

### 1️⃣ THÊM TỪ VỰNG (2 CÁCH)

#### Cách 1: Qua App (Khuyến nghị) ✅
1. Mở app → Vocabulary
2. Click nút **"+"**
3. Nhập thông tin
4. Click **"Save"**
5. ✅ Tự động lưu vào Firebase

#### Cách 2: Thủ công trên Firebase Console
1. Vào Firestore → `users/{userId}/vocabulary`
2. Click **"Add document"**
3. Document ID: Auto-generate
4. Thêm fields:

```json
{
  "word": "beautiful",
  "definition": "attractive, pleasing to the eye",
  "example": "She has a beautiful smile.",
  "level": "intermediate",
  "status": "new",
  "mastery": 0,
  "favorite": false,
  "addedAt": "2025-12-17 14:30:00",
  "reviewCount": 0,
  "lastReviewed": null,
  "nextReview": null
}
```

5. Click **"Save"**

---

### 2️⃣ THÊM BÀI HỌC GRAMMAR

#### Trên Firebase Console:
1. Vào Firestore → `grammar_lessons`
2. Click **"Add document"**
3. Document ID: `lesson_001` (hoặc auto-generate)
4. Thêm fields:

```json
{
  "id": "lesson_001",
  "title": "Present Perfect Tense",
  "description": "Learn how to use present perfect tense correctly",
  "level": "B1",
  "explanation": "The present perfect tense is used to describe:\n1. Actions that happened at an unspecified time\n2. Actions that started in the past and continue to the present\n3. Recent actions with present results\n\nFormula: have/has + past participle",
  "examples": "1. I have visited Paris three times.\n2. She has lived here for 5 years.\n3. They have just finished their homework.\n4. Have you ever eaten sushi?\n5. He hasn't seen that movie yet.",
  "rules": "1. Use 'have' with I, you, we, they\n2. Use 'has' with he, she, it\n3. Past participle: regular verbs add -ed, irregular verbs vary\n4. Common time expressions: ever, never, just, already, yet, for, since",
  "order": 1,
  "createdAt": "2025-12-17"
}
```

5. Click **"Save"**

#### Thêm nhiều bài học:
```json
// Lesson 2
{
  "id": "lesson_002",
  "title": "Past Simple vs Present Perfect",
  "level": "B1",
  "explanation": "...",
  "examples": "...",
  "rules": "...",
  "order": 2
}

// Lesson 3
{
  "id": "lesson_003",
  "title": "Conditional Sentences Type 1",
  "level": "B2",
  "explanation": "...",
  "examples": "...",
  "rules": "...",
  "order": 3
}
```

---

### 3️⃣ THÊM BÀI BÁO (ARTICLES)

#### Trên Firebase Console:
1. Vào Firestore → `articles`
2. Click **"Add document"**
3. Document ID: `article_001` (hoặc auto-generate)
4. Thêm fields:

```json
{
  "id": "article_001",
  "title": "The Future of Artificial Intelligence",
  "content": "Artificial intelligence (AI) is rapidly transforming our world...\n\n[Full article content here - có thể dài]\n\nIn conclusion, AI will continue to shape our future in ways we can only begin to imagine.",
  "category": "Technology",
  "level": "Intermediate",
  "imageUrl": "https://example.com/ai-image.jpg",
  "readingTime": 5,
  "publishDate": "Dec 17, 2025",
  "author": "John Doe",
  "summary": "An exploration of how AI is changing the world and what the future holds.",
  "tags": ["AI", "Technology", "Future"],
  "createdAt": "2025-12-17"
}
```

5. Click **"Save"**

#### Template cho các category khác:

**Business Article:**
```json
{
  "title": "Startup Success Stories",
  "category": "Business",
  "level": "Advanced",
  "readingTime": 7,
  "content": "..."
}
```

**Science Article:**
```json
{
  "title": "Climate Change Solutions",
  "category": "Science",
  "level": "Intermediate",
  "readingTime": 6,
  "content": "..."
}
```

**Culture Article:**
```json
{
  "title": "Traditional Festivals Around the World",
  "category": "Culture",
  "level": "Beginner",
  "readingTime": 4,
  "content": "..."
}
```

---

### 4️⃣ THÊM DỮ LIỆU HÀNG LOẠT (BATCH)

#### Sử dụng Firebase Admin SDK (Node.js):

```javascript
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

// Thêm nhiều grammar lessons
const lessons = [
  {
    id: 'lesson_001',
    title: 'Present Perfect',
    level: 'B1',
    explanation: '...',
    examples: '...'
  },
  {
    id: 'lesson_002',
    title: 'Past Simple',
    level: 'A2',
    explanation: '...',
    examples: '...'
  }
  // ... thêm nhiều lessons
];

const batch = db.batch();
lessons.forEach(lesson => {
  const ref = db.collection('grammar_lessons').doc(lesson.id);
  batch.set(ref, lesson);
});

batch.commit()
  .then(() => console.log('✅ Added all lessons'))
  .catch(err => console.error('❌ Error:', err));
```

---

## 📊 CẤU TRÚC DỮ LIỆU FIREBASE CHI TIẾT

### 🗂️ Collections Structure:

```
firestore/
│
├── users/                          (User data)
│   └── {userId}/
│       ├── (document)              
│       │   ├── username: string
│       │   ├── email: string
│       │   ├── level: string
│       │   └── createdAt: timestamp
│       │
│       ├── daily_goals/            (Daily goal tracking)
│       │   └── {date}/             (yyyy-MM-dd)
│       │       ├── completed: number
│       │       ├── total: number
│       │       └── date: string
│       │
│       ├── module_progress/        (Module progress %)
│       │   └── current/
│       │       ├── vocabulary: number (0-100)
│       │       ├── grammar: number (0-100)
│       │       ├── listening: number (0-100)
│       │       ├── speaking: number (0-100)
│       │       ├── reading: number (0-100)
│       │       └── writing: number (0-100)
│       │
│       ├── vocabulary/             (User's vocabulary)
│       │   └── {wordId}/
│       │       ├── word: string
│       │       ├── definition: string
│       │       ├── example: string
│       │       ├── level: string
│       │       ├── status: string (new/learning/known/mastered)
│       │       ├── mastery: number (0-5)
│       │       ├── favorite: boolean
│       │       ├── addedAt: string
│       │       ├── reviewCount: number
│       │       ├── lastReviewed: string
│       │       └── nextReview: string
│       │
│       ├── grammar_progress/       (Grammar completion)
│       │   └── {progressId}/
│       │       ├── lessonId: string
│       │       ├── completed: boolean
│       │       ├── score: number
│       │       └── completedAt: timestamp
│       │
│       ├── reading_progress/       (Reading progress)
│       │   └── {articleId}/
│       │       ├── articleId: string
│       │       ├── progress: number (0-100)
│       │       ├── lastRead: string
│       │       └── completed: boolean
│       │
│       ├── study_time/             (Daily study time)
│       │   └── {date}/
│       │       ├── date: string
│       │       └── minutes: number
│       │
│       ├── module_access/          (Access logs)
│       │   └── {accessId}/
│       │       ├── module: string
│       │       └── timestamp: string
│       │
│       └── progress/               (Overall progress)
│           └── current/
│               ├── currentXP: number
│               ├── xpForNextLevel: number
│               ├── currentStreak: number
│               └── lastActiveDate: string
│
├── grammar_lessons/                (Public grammar lessons)
│   └── {lessonId}/
│       ├── id: string
│       ├── title: string
│       ├── description: string
│       ├── level: string (A1/A2/B1/B2/C1/C2)
│       ├── explanation: string
│       ├── examples: string
│       ├── rules: string
│       ├── order: number
│       └── createdAt: string
│
└── articles/                       (Public articles)
    └── {articleId}/
        ├── id: string
        ├── title: string
        ├── content: string (long text)
        ├── category: string
        ├── level: string
        ├── imageUrl: string
        ├── readingTime: number (minutes)
        ├── publishDate: string
        ├── author: string
        ├── summary: string
        ├── tags: array
        └── createdAt: string
```

---

## 🎯 DAILY GOAL SYSTEM

### Cách hoạt động:
1. **Mỗi ngày mới** → Tạo document mới trong `daily_goals/{today}`
2. **Default:** `completed: 0, total: 5`
3. **Khi hoàn thành task:**
   - Complete lesson → `completed++`
   - Complete quiz → `completed++`
   - Complete article → `completed++`
4. **Progress bar** tự động cập nhật

### Code để increment:
```java
ProgressHelper.incrementDailyGoal();
```

---

## 📈 MODULE PROGRESS TRACKING

### Cách tính progress:
- **Vocabulary:** +1% mỗi từ mới, +2% khi master
- **Grammar:** +5% mỗi lesson hoàn thành
- **Reading:** +5% mỗi article hoàn thành
- **Quiz:** +score/10 % (score 80 → +8%)

### Code để update:
```java
// Tăng 5%
ProgressHelper.updateModuleProgress("grammar", 5);

// Hoặc complete lesson (tự động tăng 5% + daily goal)
ProgressHelper.completeLesson("grammar", 5);
```

---

## 🔍 TROUBLESHOOTING

### ❌ Lỗi: "Lesson not found"
**Nguyên nhân:** Không có data trong `grammar_lessons`
**Giải pháp:** Thêm lessons vào Firebase theo hướng dẫn trên

### ❌ Lỗi: "Article not found"
**Nguyên nhân:** Không có data trong `articles`
**Giải pháp:** Thêm articles vào Firebase

### ❌ Progress không cập nhật
**Nguyên nhân:** User chưa đăng nhập hoặc lỗi Firebase
**Giải pháp:** 
1. Check user đã login chưa
2. Check Firebase connection
3. Xem Logcat để debug

### ❌ Daily goal không reset
**Nguyên nhân:** Chưa implement auto-reset
**Giải pháp:** App tự động tạo document mới mỗi ngày

---

## 💡 TIPS & BEST PRACTICES

### 1. Thêm dữ liệu:
- ✅ Thêm qua app (vocabulary) - Tự động format đúng
- ✅ Thêm qua Console (lessons, articles) - Có control hơn
- ✅ Dùng batch import cho nhiều data

### 2. Content quality:
- ✅ Grammar lessons: Rõ ràng, có ví dụ cụ thể
- ✅ Articles: Độ dài phù hợp với reading time
- ✅ Vocabulary: Có example sentence

### 3. Level assignment:
- **Beginner (A1-A2):** Từ/bài cơ bản
- **Intermediate (B1-B2):** Trung bình
- **Advanced (C1-C2):** Nâng cao

### 4. Testing:
- ✅ Test thêm 1-2 items trước
- ✅ Check hiển thị đúng trong app
- ✅ Verify progress tracking hoạt động

---

## 📞 SUPPORT

### Cần thêm data mẫu?
Tham khảo các template ở trên và tạo theo format tương tự.

### Cần customize?
- Sửa `ProgressHelper.java` để thay đổi logic
- Sửa layouts để thay đổi UI
- Sửa Firebase structure nếu cần

---

**Last Updated:** 2025-12-17
**Version:** 1.0
**Author:** AI Assistant
