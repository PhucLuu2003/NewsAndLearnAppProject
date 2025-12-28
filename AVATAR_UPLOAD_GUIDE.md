# 🖼️ Avatar Upload - Hướng Dẫn Hoàn Chỉnh

## ✅ Vấn Đề & Giải Pháp

### ❌ Vấn Đề Cũ
- Notification "Uploading photo" hiện nhưng avatar không lưu được
- Ảnh không được upload lên Firebase Storage
- Không cập nhật được vào Firestore

### ✅ Giải Pháp Đã Làm

#### 1. **Firebase Storage Rules** (📁 `storage.rules`)
```
- Cho phép user upload ảnh vào folder: `/profile_pictures/{userId}/`
- Cho phép read public (tất cả authenticated users)
- Cho phép write chỉ chính user đó
```

#### 2. **Android Permissions** (📄 `AndroidManifest.xml`)
```xml
- READ_EXTERNAL_STORAGE  (Android 6-12)
- READ_MEDIA_IMAGES      (Android 13+)
```

#### 3. **Runtime Permission Request** (☕ `EditProfileDialog.java`)
```
- Request permission trước khi mở gallery
- Xử lý callback onRequestPermissionsResult
- Support Android 6+
```

#### 4. **Improved Upload Logic** (☕ `EditProfileDialog.java`)
```
✅ Step 1: Upload ảnh → Firebase Storage
✅ Step 2: Lấy download URL từ Storage
✅ Step 3: Lưu URL vào Firestore (collection: users)
✅ Step 4: Cập nhật Firebase Auth profile (display name + photo)
```

---

## 🔄 Quy Trình Lưu Avatar

### **Before (Cũ)**
```
User click Change Photo
   ↓
Select Image
   ↓
Upload notification appears
   ↓
❌ STOP - Không lưu được
```

### **After (Mới)**
```
User click Change Photo
   ↓
Request Permission (READ_MEDIA_IMAGES)
   ↓
Select Image from Gallery
   ↓
Preview in dialog (Glide)
   ↓
Click Save
   ↓
Upload to Firebase Storage: /profile_pictures/{userId}/avatar.jpg
   ↓
Get Download URL
   ↓
Save to Firestore:
   {
      "photoUrl": "https://...",
      "username": "...",
      "email": "...",
      "lastUpdated": 1234567890
   }
   ↓
Update Firebase Auth Profile
   ↓
✅ SUCCESS - Avatar saved!
```

---

## 📍 Nơi Lưu Trữ Avatar

### **Firebase Storage**
- **Path**: `profile_pictures/{userId}/avatar.jpg`
- **Access**: Public download (all authenticated users can see)
- **Size**: Tùy ý (khuyên 1-5MB)

### **Firestore Database**
- **Collection**: `users`
- **Document**: `{userId}`
- **Field**: `photoUrl` (URL download từ Storage)

```json
{
  "userId": "abc123def456",
  "username": "John Doe",
  "email": "john@example.com",
  "photoUrl": "https://firebasestorage.googleapis.com/v0/b/.../avatar.jpg",
  "lastUpdated": 1703769600000
}
```

---

## 🔐 Security Rules

### **Storage Rules** (`storage.rules`)
```
✅ User chỉ có thể upload vào folder của chính mình
✅ Tất cả authenticated user có thể xem avatar
✅ Chỉ user đó có thể xóa/update avatar
```

### **Firestore Rules** (`firestore.rules`)
```
✅ User chỉ có thể read/write dữ liệu của chính mình
✅ Không ai khác có thể sửa avatar của user khác
```

---

## 🧪 Testing Avatar Upload

### **Test Steps**:
1. Vào Settings → Edit Profile
2. Click "Change Photo"
3. Chọn ảnh từ gallery
4. Xem preview trong dialog (nên hiện ảnh)
5. Click "Save"
6. Chờ notification:
   - ⏳ "Uploading photo..."
   - ✅ "Photo uploaded successfully"
   - ✅ "Firestore updated"
   - ✅ "Profile updated successfully!"

### **Verify Avatar Saved**:
- **Firestore Console**: Vào `users/{userId}` xem field `photoUrl`
- **Storage Console**: Vào `profile_pictures/{userId}/` xem file `avatar.jpg`
- **App**: Restart app, avatar vẫn hiện (cached by Glide)

---

## ⚠️ Troubleshooting

### ❌ "Permission denied"
**Solution**: Vào Settings → App Permissions → Photos, bật ON

### ❌ "Image upload failed"
**Solution**: 
- Check Firebase Storage rules (firestore.rules)
- Check file size (< 10MB)
- Check internet connection

### ❌ "Failed to get download URL"
**Solution**:
- File upload failed trước
- Check file integrity
- Retry upload

### ❌ "User not authenticated"
**Solution**:
- Cần login trước
- Sau logout, không thể edit profile

### ❌ Avatar không update trên app
**Solution**:
- Clear app cache: Settings → Apps → NewsAndLearn → Clear Cache
- Restart app
- Glide caches images 30 days (by default)

---

## 📊 Data Flow Diagram

```
┌─────────────────┐
│   User Device   │
│                 │
│ EditProfileDialog
│     ↓ (select image)
└────────┬────────┘
         │
         ↓ (upload)
┌─────────────────────────┐
│ Firebase Storage        │
│ /profile_pictures/      │
│    {userId}/avatar.jpg  │
└────────┬────────────────┘
         │ (get URL)
         ↓
┌──────────────────────────────┐
│ Firestore Database           │
│ /users/{userId}              │
│  - photoUrl (string)         │
│  - username (string)         │
│  - email (string)            │
│  - lastUpdated (timestamp)   │
└────────┬─────────────────────┘
         │ (update profile)
         ↓
┌──────────────────────────────┐
│ Firebase Authentication      │
│ - displayName                │
│ - photoUrl                   │
└──────────────────────────────┘
```

---

## 🎯 Code Changes Summary

| File | Changes |
|------|---------|
| `storage.rules` | ✅ Tạo mới - Firebase Storage rules |
| `EditProfileDialog.java` | ✅ Thêm permission request + improved upload |
| `AndroidManifest.xml` | ✅ Thêm READ_EXTERNAL_STORAGE permissions |
| `firestore.rules` | ✅ Đã có sẵn rules |

---

## ✨ Features

✅ Upload avatar to Firebase Storage  
✅ Save photoUrl to Firestore  
✅ Update Firebase Auth profile  
✅ Request runtime permissions  
✅ Preview image before save  
✅ Error handling & logs  
✅ Toast notifications for each step  
✅ Support Android 6 - 14+  

---

**Status**: 🟢 Ready to Deploy

Hãy rebuild app và test chức năng avatar upload! 🚀

