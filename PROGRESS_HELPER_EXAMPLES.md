# 🚀 ProgressHelper Usage Examples

## Cách sử dụng ProgressHelper trong các Fragment/Activity

### 1. **Khi hoàn thành một bài học** 📚

```java
// Trong VocabularyFragment, GrammarFragment, etc.
public void onLessonCompleted() {
    // Cập nhật progress: tăng 5% cho module này
    ProgressHelper.completeLesson("vocabulary", 5);
    
    // Hiển thị thông báo
    Toast.makeText(getContext(), "✅ Lesson completed! +5% progress", Toast.LENGTH_SHORT).show();
}
```

### 2. **Khi hoàn thành Quiz** ✍️

```java
// Trong QuizActivity
public void onQuizFinished(int score) {
    // score từ 0-100
    ProgressHelper.completeQuiz("grammar", score);
    
    if (score >= 70) {
        Toast.makeText(this, "🎉 Great job! Score: " + score, Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(this, "📖 Keep practicing! Score: " + score, Toast.LENGTH_SHORT).show();
    }
}
```

### 3. **Khi thêm từ vựng mới** 📝

```java
// Trong VocabularyFragment
public void addNewWord(String word, String definition) {
    ProgressHelper.addVocabulary(
        word,           // "beautiful"
        definition,     // "attractive, pleasing to the eye"
        "intermediate"  // beginner, intermediate, advanced
    );
    
    Toast.makeText(getContext(), "📚 Word added to your collection!", Toast.LENGTH_SHORT).show();
}
```

### 4. **Khi ôn tập từ vựng** 🔄

```java
// Khi user đánh dấu từ đã thuộc
public void markWordAsKnown(String wordId) {
    ProgressHelper.updateVocabularyStatus(wordId, "known");
    // Status: "new", "learning", "known", "mastered"
}

// Khi user đánh dấu từ đã thành thạo
public void markWordAsMastered(String wordId) {
    ProgressHelper.updateVocabularyStatus(wordId, "mastered");
    ProgressHelper.updateModuleProgress("vocabulary", 2); // Bonus 2%
}
```

### 5. **Khi đọc bài báo** 📰

```java
// Trong ArticleDetailActivity
private void updateReadingProgress(String articleId, int scrollProgress) {
    // scrollProgress từ 0-100 dựa trên scroll position
    ProgressHelper.updateReadingProgress(articleId, scrollProgress);
    
    if (scrollProgress >= 100) {
        Toast.makeText(this, "🎉 Article completed!", Toast.LENGTH_SHORT).show();
    }
}
```

### 6. **Tracking thời gian học** ⏱️

```java
// Trong mỗi Fragment/Activity
private long startTime;

@Override
public void onResume() {
    super.onResume();
    startTime = System.currentTimeMillis();
}

@Override
public void onPause() {
    super.onPause();
    long endTime = System.currentTimeMillis();
    int studyMinutes = (int) ((endTime - startTime) / 60000); // Convert to minutes
    
    if (studyMinutes > 0) {
        ProgressHelper.updateStudyTime(studyMinutes);
    }
}
```

### 7. **Cập nhật Streak** 🔥

```java
// Gọi khi user hoàn thành daily goal
public void onDailyGoalCompleted() {
    ProgressHelper.updateStreak();
    
    // Show celebration
    showStreakAnimation();
    Toast.makeText(getContext(), "🔥 Streak updated!", Toast.LENGTH_SHORT).show();
}
```

---

## 📋 Complete Example: VocabularyFragment

```java
public class VocabularyFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private List<VocabularyWord> words;
    private long startTime;
    
    @Override
    public void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Track study time
        long endTime = System.currentTimeMillis();
        int minutes = (int) ((endTime - startTime) / 60000);
        if (minutes > 0) {
            ProgressHelper.updateStudyTime(minutes);
        }
    }
    
    // When user completes a vocabulary lesson
    private void onLessonCompleted() {
        ProgressHelper.completeLesson("vocabulary", 5);
        showCompletionDialog();
    }
    
    // When user adds a new word
    private void onAddWord(String word, String definition, String level) {
        ProgressHelper.addVocabulary(word, definition, level);
        Toast.makeText(getContext(), "✅ Word added!", Toast.LENGTH_SHORT).show();
        loadWords(); // Refresh list
    }
    
    // When user marks word as known
    private void onWordStatusChanged(String wordId, String status) {
        ProgressHelper.updateVocabularyStatus(wordId, status);
        
        if (status.equals("mastered")) {
            // Give bonus progress
            ProgressHelper.updateModuleProgress("vocabulary", 2);
            showCelebration();
        }
    }
    
    // When user completes a vocabulary quiz
    private void onQuizCompleted(int score) {
        ProgressHelper.completeQuiz("vocabulary", score);
        
        if (score >= 90) {
            Toast.makeText(getContext(), "🌟 Perfect! +10% progress", Toast.LENGTH_SHORT).show();
        } else if (score >= 70) {
            Toast.makeText(getContext(), "✅ Good job! +" + (score/10) + "% progress", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "📖 Keep practicing!", Toast.LENGTH_SHORT).show();
        }
    }
}
```

---

## 📋 Complete Example: ReadingFragment

```java
public class ArticleDetailActivity extends AppCompatActivity {
    
    private ScrollView scrollView;
    private String articleId;
    private long startTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        
        articleId = getIntent().getStringExtra("article_id");
        startTime = System.currentTimeMillis();
        
        setupScrollListener();
    }
    
    private void setupScrollListener() {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollView.getScrollY();
            int height = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
            int progress = (int) ((scrollY / (float) height) * 100);
            
            // Update progress every 10%
            if (progress % 10 == 0) {
                ProgressHelper.updateReadingProgress(articleId, progress);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Track reading time
        long endTime = System.currentTimeMillis();
        int minutes = (int) ((endTime - startTime) / 60000);
        if (minutes > 0) {
            ProgressHelper.updateStudyTime(minutes);
        }
    }
}
```

---

## 🎯 Best Practices

### 1. **Gọi incrementDailyGoal() khi:**
- User hoàn thành một lesson
- User hoàn thành một quiz (score >= 70)
- User đọc xong một article
- User hoàn thành một speaking/listening exercise

### 2. **Cập nhật module progress khi:**
- Hoàn thành lesson: +5%
- Quiz score 70-80: +7%
- Quiz score 80-90: +8%
- Quiz score 90-100: +10%
- Mastered vocabulary word: +2%
- Completed article: +5%

### 3. **Track study time:**
- Trong onResume() lưu startTime
- Trong onPause() tính duration và update
- Chỉ update nếu >= 1 minute

### 4. **Update streak:**
- Khi daily goal completed (5/5)
- Check mỗi ngày lúc midnight
- Reset nếu user skip 1 ngày

---

## 🔔 Notifications (Future Enhancement)

```java
// Reminder to complete daily goal
public void scheduleReminder() {
    // Use WorkManager or AlarmManager
    // Check if daily goal not completed
    // Send notification at 8 PM
}
```

---

## 📊 Analytics (Future Enhancement)

```java
// Track user behavior
public void logEvent(String eventName, Map<String, Object> params) {
    FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
}
```

---

**Lưu ý:** 
- Tất cả methods trong ProgressHelper đều async
- Không cần callback vì UI sẽ tự refresh khi onResume()
- Nếu cần realtime update, dùng Firestore listeners
