# 🎨 Hướng dẫn sử dụng Lottie Animation

## 📚 Tổng quan
Lottie là thư viện animation mạnh mẽ cho phép sử dụng animation JSON (After Effects) trong Android app.

**Dependency đã được thêm vào `build.gradle.kts`:**
```kotlin
implementation("com.airbnb.android:lottie:6.1.0")
```

---

## 📥 Cách tải Animation JSON

### **Bước 1: Truy cập LottieFiles**
1. Vào website: https://lottiefiles.com/
2. Tìm kiếm animation (ví dụ: "loading", "success", "book")
3. Chọn animation **FREE** (miễn phí)

### **Bước 2: Tải file JSON**
1. Click vào animation bạn thích
2. Click nút **"Download"** hoặc **"Free Download"**
3. Chọn định dạng **"Lottie JSON"**
4. File `.json` sẽ được tải về máy

### **Bước 3: Thêm vào Project**
1. Copy file `.json` vào thư mục: `app/src/main/res/raw/`
2. **Đổi tên file** theo chuẩn Android:
   - Chỉ dùng chữ thường (lowercase)
   - Dùng dấu gạch dưới `_` thay vì dấu cách
   - Ví dụ: `loading_animation.json`, `success_checkmark.json`

---

## 🎯 Animations đã được tích hợp

### **1. Success Animation** (`success_animation.json`)
- **Vị trí:** `GameResultActivity`
- **Mục đích:** Hiển thị khi hoàn thành game
- **File:** `activity_game_result.xml`

```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/success_animation"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:lottie_rawRes="@raw/success_animation"
    app:lottie_autoPlay="true"
    app:lottie_loop="false" />
```

### **2. Profile Avatar Animation** (`profile_avatar_boy.json`)
- **Vị trí:** `ProfileFragment`
- **Mục đích:** Avatar animation khi loading
- **File:** `fragment_profile.xml`

```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/avatar_animation"
    android:layout_width="128dp"
    android:layout_height="128dp"
    android:visibility="gone"
    app:lottie_rawRes="@raw/profile_avatar_boy"
    app:lottie_autoPlay="true"
    app:lottie_loop="true" />
```

---

## 💻 Cách sử dụng trong Code

### **1. Sử dụng trong XML Layout**

```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/lottieAnimation"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:lottie_rawRes="@raw/your_animation"
    app:lottie_autoPlay="true"
    app:lottie_loop="true"
    app:lottie_speed="1.0" />
```

**Các thuộc tính XML:**
- `lottie_rawRes`: File JSON trong thư mục `res/raw/`
- `lottie_autoPlay`: Tự động phát (true/false)
- `lottie_loop`: Lặp lại animation (true/false)
- `lottie_speed`: Tốc độ (1.0 = bình thường, 2.0 = nhanh gấp đôi)

### **2. Điều khiển từ Java Code**

```java
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class YourActivity extends AppCompatActivity {
    private LottieAnimationView lottieAnimation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.your_layout);
        
        lottieAnimation = findViewById(R.id.lottieAnimation);
        
        // Phát animation
        lottieAnimation.playAnimation();
        
        // Dừng animation
        lottieAnimation.pauseAnimation();
        
        // Dừng và reset về frame đầu
        lottieAnimation.cancelAnimation();
        
        // Đặt tốc độ (1.0 = bình thường, 2.0 = nhanh gấp đôi)
        lottieAnimation.setSpeed(1.5f);
        
        // Lặp lại vô hạn
        lottieAnimation.setRepeatCount(LottieDrawable.INFINITE);
        
        // Lặp lại 3 lần
        lottieAnimation.setRepeatCount(3);
        
        // Ẩn/hiện animation
        lottieAnimation.setVisibility(View.VISIBLE);
        lottieAnimation.setVisibility(View.GONE);
    }
}
```

### **3. Load từ URL (nếu cần)**

```java
lottieAnimation.setAnimationFromUrl("https://example.com/animation.json");
```

### **4. Listener cho animation**

```java
lottieAnimation.addAnimatorListener(new Animator.AnimatorListener() {
    @Override
    public void onAnimationStart(Animator animation) {
        // Animation bắt đầu
    }
    
    @Override
    public void onAnimationEnd(Animator animation) {
        // Animation kết thúc
        // Có thể chuyển màn hình hoặc ẩn animation
    }
    
    @Override
    public void onAnimationCancel(Animator animation) {
        // Animation bị hủy
    }
    
    @Override
    public void onAnimationRepeat(Animator animation) {
        // Animation lặp lại
    }
});
```

---

## 🎨 Gợi ý Animation cho các màn hình

### **Loading & Progress**
- `loading_books.json` - ArticleFragment, ReadingActivity
- `loading_dots.json` - Loading chung
- `progress_bar.json` - Quiz, Games

### **Success & Achievement**
- `success_checkmark.json` - Hoàn thành bài học ✅
- `trophy_win.json` - Thắng game 🏆
- `confetti_celebration.json` - Level up 🎉
- `star_rating.json` - Đánh giá

### **Learning Specific**
- `book_reading.json` - ReadingActivity 📖
- `brain_thinking.json` - Quiz 🧠
- `lightbulb_idea.json` - Từ vựng mới 💡
- `graduation_cap.json` - Hoàn thành khóa học 🎓

### **Game Animations**
- `game_controller.json` - GamesFragment 🎮
- `sword_battle.json` - BattleActivity ⚔️
- `memory_cards.json` - FlashcardActivity 🃏
- `music_notes.json` - PronunciationBeatActivity 🎵

### **Error & Empty State**
- `error_404.json` - Không tìm thấy ❌
- `no_data_empty.json` - Empty state 📭
- `network_error.json` - Lỗi kết nối 📡

### **Interactive**
- `microphone_listening.json` - SpeakingActivity 🎤
- `writing_pencil.json` - WritingActivity ✏️
- `headphones_audio.json` - ListeningActivity 🎧
- `heart_favorite.json` - FavoriteFragment ❤️

---

## 📝 Ví dụ thực tế

### **Ví dụ 1: Loading Screen khi tải Articles**

**XML (`fragment_article.xml`):**
```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/loading_animation"
    android:layout_width="150dp"
    android:layout_height="150dp"
    android:layout_gravity="center"
    android:visibility="gone"
    app:lottie_rawRes="@raw/loading_books"
    app:lottie_autoPlay="true"
    app:lottie_loop="true" />
```

**Java (`ArticleFragment.java`):**
```java
private LottieAnimationView loadingAnimation;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_article, container, false);
    loadingAnimation = view.findViewById(R.id.loading_animation);
    
    // Hiện loading khi bắt đầu tải
    showLoading();
    loadArticles();
    
    return view;
}

private void showLoading() {
    loadingAnimation.setVisibility(View.VISIBLE);
    loadingAnimation.playAnimation();
}

private void hideLoading() {
    loadingAnimation.setVisibility(View.GONE);
    loadingAnimation.cancelAnimation();
}

private void loadArticles() {
    // Load data từ Firebase
    // ...
    // Sau khi load xong:
    hideLoading();
}
```

### **Ví dụ 2: Success Animation sau khi hoàn thành Quiz**

```java
private void showSuccessAnimation() {
    LottieAnimationView successAnim = findViewById(R.id.success_animation);
    successAnim.setVisibility(View.VISIBLE);
    successAnim.playAnimation();
    
    // Tự động ẩn sau 3 giây
    new Handler().postDelayed(() -> {
        successAnim.setVisibility(View.GONE);
        // Chuyển sang màn hình kết quả
        navigateToResult();
    }, 3000);
}
```

---

## 🔧 Tips & Best Practices

1. **Tối ưu kích thước:**
   - Chọn animation có kích thước nhỏ (< 100KB)
   - Tránh animation quá phức tạp

2. **Performance:**
   - Dừng animation khi không cần thiết
   - Sử dụng `cancelAnimation()` trong `onPause()`

3. **UX:**
   - Không lặp vô hạn cho success/error animations
   - Sử dụng `lottie_loop="false"` cho one-time animations

4. **Naming Convention:**
   - Đặt tên file rõ ràng: `loading_books.json`, `success_checkmark.json`
   - Tránh tên chung chung như `animation1.json`

---

## 🌐 Resources

- **LottieFiles:** https://lottiefiles.com/
- **Lottie Documentation:** https://airbnb.io/lottie/
- **GitHub:** https://github.com/airbnb/lottie-android

---

**Tạo bởi:** News & Learn App Team  
**Cập nhật:** 2025-12-24
