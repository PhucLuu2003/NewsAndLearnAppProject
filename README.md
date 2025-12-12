# 📱 News And Learn App - Ứng dụng Học Tiếng Anh qua Tin Tức

## 📋 Mô tả dự án
Ứng dụng Android giúp người dùng học tiếng Anh thông qua việc đọc tin tức với các cấp độ khác nhau (A1-C2). Ứng dụng sử dụng Firebase để xác thực và lưu trữ dữ liệu.

## ✨ Tính năng chính

### 🔐 Xác thực người dùng
- ✅ Đăng nhập bằng Email/Password
- ✅ Đăng ký tài khoản mới
- ✅ Tích hợp Firebase Authentication
- 🔄 Quên mật khẩu (đang phát triển)
- 🔄 Đăng nhập bằng Google (đang phát triển)

### 🏠 Màn hình chính (Home Fragment)
- ✅ Hiển thị danh sách bài báo theo cấp độ (Dễ/Trung bình/Khó)
- ✅ Các nút chức năng: Scan, Yêu thích, Lịch sử, Tài liệu
- ✅ Tìm kiếm bài viết
- ✅ Phân loại theo chủ đề
- ✅ Giao diện Material Design đẹp mắt với dark theme

### 💜 Màn hình yêu thích (Favorite Fragment)
- ✅ Hiển thị danh sách bài viết đã lưu
- ✅ Empty state khi chưa có bài viết
- ✅ Thêm/xóa bài viết yêu thích
- ✅ RecyclerView với animation mượt mà

### 👤 Màn hình hồ sơ (Profile Fragment)
- ✅ Hiển thị thông tin người dùng
- ✅ Thống kê streak (chuỗi ngày học)
- ✅ Thống kê: Số bài đọc, từ vựng, thời gian học
- ✅ Đăng xuất
- ✅ Card design đẹp mắt với icon emoji

### 📖 Màn hình chi tiết bài viết
- ✅ Hiển thị nội dung đầy đủ
- ✅ Thêm vào yêu thích
- ✅ Chia sẻ bài viết
- ✅ Header với hình ảnh và cấp độ

### 🔍 Màn hình tìm kiếm
- ✅ Tìm kiếm real-time
- ✅ Lọc theo từ khóa
- ✅ Hiển thị kết quả với RecyclerView

### ⚙️ Cài đặt ban đầu
- ✅ Chọn trình độ (A1-C2)
- ✅ Chọn chủ đề quan tâm
- ✅ Lưu preferences vào Firebase

## 🏗️ Kiến trúc ứng dụng

### Cấu trúc thư mục
```
app/src/main/
├── java/com/example/newsandlearn/
│   ├── Activity/
│   │   ├── MainActivity.java            # Activity chính với Bottom Navigation
│   │   ├── LoginActivity.java          # Màn hình đăng nhập
│   │   ├── RegisterActivity.java       # Màn hình đăng ký
│   │   ├── LevelSelectionActivity.java # Chọn trình độ
│   │   ├── TopicSelectionActivity.java # Chọn chủ đề
│   │   ├── SearchActivity.java         # Tìm kiếm
│   │   └── ArticleDetailActivity.java  # Chi tiết bài viết
│   ├── Fragment/
│   │   ├── HomeFragment.java           # Fragment trang chủ
│   │   ├── FavoriteFragment.java       # Fragment yêu thích
│   │   ├── ProfileFragment.java        # Fragment hồ sơ
│   │   └── SettingFragment.java        # Fragment cài đặt
│   ├── Adapter/
│   │   └── ArticleAdapter.java         # Adapter cho RecyclerView
│   ├── Model/
│   │   ├── Article.java                # Model bài viết
│   │   └── User.java                   # Model người dùng
│   └── Utils/
│       └── FirebaseService.java        # Các hàm Firebase utility
└── res/
    ├── layout/                          # 17 layout files
    ├── drawable/                        # 17 drawable resources
    ├── menu/                           # Bottom navigation menu
    └── values/                         # Colors, strings, themes
```

### Công nghệ sử dụng

#### Backend & Database
- 🔥 **Firebase Authentication** - Xác thực người dùng
- 🔥 **Firebase Firestore** - NoSQL database
- 🔥 **Firebase Cloud Storage** - Lưu trữ hình ảnh (dự kiến)

#### Frontend & UI
- 📱 **Android SDK** - Minimum SDK 24
- 🎨 **Material Design 3** - UI components
- 🔄 **RecyclerView** - Hiển thị danh sách
- 📐 **ConstraintLayout** - Responsive layouts
- 🎯 **Bottom Navigation** - Điều hướng chính

#### Thư viện bổ sung
- androidx.appcompat
- androidx.core
- androidx.fragment
- com.google.android.material
- androidx.constraintlayout
- androidx.cardview

## 🎨 Giao diện

### Màu sắc chính
- **Background Primary**: `#111113` (Dark)
- **Background Secondary**: `#2C2C2E` (Dark Gray)
- **Accent Blue**: `#4A80F0` (Primary Button)
- **Card Background**: `#3A3A3C` (Card)
- **Active Tab**: `#A8D8FF` (Light Blue)
- **Text Primary**: `#FFFFFF` (White)
- **Text Secondary**: `#9A9A9A` (Gray)

### Font & Typography
- **Heading**: 24sp, Bold
- **Body**: 16sp, Regular
- **Caption**: 12sp, Regular
- **Button**: 16sp, Bold

## 🚀 Cài đặt & Chạy dự án

### Yêu cầu
- Android Studio Hedgehog | 2023.1.1 trở lên
- JDK 11 trở lên
- Android SDK 24+ (Android 7.0+)
- Firebase project đã được cấu hình

### Bước 1: Clone repository
```bash
git clone https://github.com/PhucLuu2003/NewsAndLearnAppProject.git
cd NewsAndLearnAppProject
```

### Bước 2: Cấu hình Firebase
1. Tạo project Firebase tại [Firebase Console](https://console.firebase.google.com/)
2. Thêm ứng dụng Android với package name: `com.example.newsandlearn`
3. Tải file `google-services.json`
4. Copy vào thư mục `app/`
5. Enable Authentication (Email/Password) và Firestore trong Firebase Console

### Bước 3: Build & Run
```bash
# Sync Gradle
./gradlew build

# Chạy trên emulator hoặc thiết bị thật
./gradlew installDebug
```

## 📱 Luồng sử dụng

1. **Lần đầu sử dụng**:
   - Mở app → Màn hình Login
   - Đăng ký tài khoản mới
   - Chọn trình độ (A1-C2)
   - Chọn chủ đề quan tâm
   - Vào trang chủ

2. **Đọc bài viết**:
   - Browse bài viết ở tab Home
   - Chọn cấp độ (Dễ/Trung bình/Khó)
   - Click vào bài viết để xem chi tiết
   - Thêm vào yêu thích bằng icon trái tim

3. **Quản lý học tập**:
   - Xem streak và thống kê ở tab Profile
   - Xem lại bài viết yêu thích ở tab Favorite
   - Theo dõi tiến độ học tập

## 📊 Cơ sở dữ liệu Firebase

### Collection: users
```javascript
{
  uid: string,
  email: string,
  username: string,
  level: string,        // A1, A2, B1, B2, C1, C2
  topics: array,        // Mảng chủ đề quan tâm
  streak: number,       // Chuỗi ngày học
  totalDays: number,    // Tổng số ngày học
  createdAt: timestamp
}
```

### Collection: articles
```javascript
{
  id: string,
  title: string,
  content: string,
  imageUrl: string,
  category: string,
  level: string,        // easy, medium, hard
  source: string,
  publishedDate: timestamp,
  views: number,
  readingTime: number
}
```

## 🔧 Tính năng sắp phát triển

- [ ] Google Sign-In
- [ ] Forgot Password
- [ ] Push Notifications
- [ ] Offline mode
- [ ] Từ điển tích hợp
- [ ] Phát âm từ vựng
- [ ] Quiz & Test
- [ ] Leaderboard
- [ ] Social sharing
- [ ] Dark/Light theme toggle

## 👨‍💻 Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng:
1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## 📄 License

Dự án này được phát triển cho mục đích học tập.

## 📞 Liên hệ

- GitHub: [@PhucLuu2003](https://github.com/PhucLuu2003)
- Email: [Your Email]

---

**Made with ❤️ by Phuc Luu**
Final project of group of 4 people learning create application with android studio
