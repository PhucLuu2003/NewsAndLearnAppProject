# 🔊 Hướng Dẫn Debug Chức Năng Text-to-Speech (TTS)

## 📋 Các Thay Đổi Đã Thực Hiện

### 1. **Sửa Click Listener trong VocabularyAdapter**
- Thay đổi từ click vào `ImageView` sang click vào `MaterialCardView` container
- Thêm fallback cho layout không có container
- Thêm logging để track clicks

### 2. **Cải Thiện Khởi Tạo TTS trong VocabularyFragment**
- Thêm callback khi TTS sẵn sàng
- Hiển thị Toast "Voice ready! 🔊" khi TTS đã khởi tạo xong
- Thêm logging chi tiết

### 3. **Nâng Cấp Hàm speakWord()**
- Thêm nhiều logging để debug
- Hiển thị Toast khi đang chuẩn bị voice
- Hiển thị Toast khi đang phát âm

## 🧪 Cách Kiểm Tra

### Bước 1: Cài Đặt App
```bash
# File APK nằm ở:
app\build\outputs\apk\debug\app-debug.apk

# Cài đặt bằng Android Studio hoặc copy file vào điện thoại
```

### Bước 2: Mở Logcat
Trong Android Studio:
1. Chọn **View** → **Tool Windows** → **Logcat**
2. Filter theo tag: `VocabularyFragment` hoặc `TTSManager` hoặc `VocabularyAdapter`

### Bước 3: Test Chức Năng
1. Mở app và đăng nhập
2. Vào tab **Vocabulary**
3. Nhấn vào **nút loa** (🔊) trên bất kỳ vocabulary card nào
4. Quan sát:
   - **Toast messages** xuất hiện trên màn hình
   - **Logs** trong Logcat
   - **Âm thanh** từ loa điện thoại

## 📊 Logs Cần Chú Ý

### Khi Fragment Được Tạo:
```
VocabularyFragment: Initializing TTS...
TTSManager: 🎤 initialize() called. isInitialized=false, isInitializing=false
TTSManager: 🚀 Creating TextToSpeech with engine: com.google.android.tts
TTSManager: 📨 TTS init callback: status=0 (SUCCESS=0)
TTSManager: ✅ TTS initialized successfully! isInitialized=true
VocabularyFragment: TTS initialized successfully!
```

### Khi Nhấn Nút Loa:
```
VocabularyAdapter: Speaker button clicked for word: Ambitious
VocabularyFragment: speakWord() called with word: Ambitious
VocabularyFragment: TTSManager initialized: true
VocabularyFragment: TTS is ready, speaking: Ambitious
TTSManager: 🔊 speakWord() called: word=Ambitious, isInitialized=true
TTSManager: 🗣️ speak() called: text=Ambitious, isInitialized=true
TTSManager: ▶️ Calling tts.speak() with text: Ambitious
```

## 🔍 Các Vấn Đề Có Thể Gặp

### 1. **Không Thấy Log "Speaker button clicked"**
➡️ **Nguyên nhân:** Click listener không hoạt động
➡️ **Giải pháp:** Kiểm tra xem `speaker_button_container` có tồn tại trong layout không

### 2. **Thấy Log "TTS not initialized"**
➡️ **Nguyên nhân:** TTS chưa sẵn sàng
➡️ **Giải pháp:** Đợi Toast "Voice ready! 🔊" xuất hiện trước khi nhấn nút

### 3. **TTS Initialized Nhưng Không Có Âm Thanh**
➡️ **Nguyên nhân:** 
   - Volume điện thoại bị tắt
   - Google TTS engine chưa được cài đặt
   - Thiếu quyền RECORD_AUDIO (mặc dù không bắt buộc cho TTS)

➡️ **Giải pháp:**
   - Kiểm tra volume điện thoại
   - Cài đặt Google Text-to-Speech từ Play Store
   - Vào Settings → Apps → Permissions và kiểm tra quyền

### 4. **Error: "LANG_MISSING_DATA" hoặc "LANG_NOT_SUPPORTED"**
➡️ **Nguyên nhân:** Thiếu dữ liệu ngôn ngữ tiếng Anh
➡️ **Giải pháp:**
   - Mở **Settings** → **Language & Input** → **Text-to-Speech**
   - Chọn **Google Text-to-Speech Engine**
   - Tải xuống dữ liệu tiếng Anh (English US)

## 🎯 Toast Messages Bạn Sẽ Thấy

1. **"Voice ready! 🔊"** - TTS đã sẵn sàng
2. **"Preparing voice... 🎤"** - TTS đang khởi tạo
3. **"🔊 [word]"** - Đang phát âm từ

## 🛠️ Debug Commands

### Xem Logs Realtime:
```bash
# Filter theo tag
adb logcat -s VocabularyFragment TTSManager VocabularyAdapter

# Hoặc filter theo keyword
adb logcat | grep -i "speak\|tts\|speaker"
```

### Kiểm Tra TTS Engine:
```bash
# Liệt kê các TTS engines có sẵn
adb shell settings get secure tts_default_synth

# Kiểm tra Google TTS
adb shell pm list packages | grep tts
```

## ✅ Checklist Trước Khi Test

- [ ] App đã được build thành công
- [ ] App đã được cài đặt lên thiết bị
- [ ] Volume điện thoại > 0
- [ ] Google Text-to-Speech đã được cài đặt
- [ ] Dữ liệu tiếng Anh đã được tải xuống
- [ ] Logcat đang mở và filter đúng tag
- [ ] Đã vào tab Vocabulary và có vocabulary items

## 📞 Nếu Vẫn Không Hoạt Động

Hãy gửi cho tôi:
1. **Screenshot** của Logcat khi nhấn nút loa
2. **Screenshot** của Toast messages (nếu có)
3. **Thông tin thiết bị:** Android version, brand, model

---

**Lưu ý:** Tất cả các thay đổi đã được commit và build thành công. File APK mới nhất nằm ở `app\build\outputs\apk\debug\app-debug.apk`
