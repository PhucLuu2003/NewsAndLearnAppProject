# 🎤 SPEECH RECOGNITION TROUBLESHOOTING GUIDE

## ✅ MIC ĐANG HOẠT ĐỘNG NHƯNG KHÔNG NHẬN DIỆN

Từ logs của bạn:
```
🔊 Volume: 0.16~1.6 dB  ← Quá nhỏ!
❌ Speech Error: No match
```

### 🎯 VẤN ĐỀ

**Volume quá thấp** - Google Speech cần **> 2-3 dB** để nhận diện là giọng nói.

### 💡 GIẢI PHÁP

#### **1. NÓI TO HƠN** 🔊
- Nói **TO, RÕ RÀNG**
- Đưa điện thoại **GẦN MIC HƠN** (5-10cm)
- Nói trong **môi trường TĨNH** (không ồn)

#### **2. KIỂM TRA MIC PERMISSION**
```
Settings → Apps → NewsAndLearn → Permissions → Microphone = ALLOWED
```

#### **3. TEST MIC**
- Mở **Google Assistant** và nói "Hello"
- Mở **Voice Recorder** và thu âm
- Nếu 2 app trên hoạt động → Mic OK, chỉ cần nói to hơn

#### **4. KIỂM TRA INTERNET**
Google Speech Recognition cần **internet** để hoạt động!

---

## 📊 LOGS BẠN NÊN THẤY (KHI HOẠT ĐỘNG ĐÚNG)

### ✅ Volume đủ lớn:
```
🔊 Volume: 3.5 dB
🔊 Volume: 5.2 dB
🔊 Volume: 8.1 dB  ← Tốt!
```

### ✅ Speech detected:
```
🎤 Speech detected!
👂 Hearing: happy
⏹️ Speech ended, processing...
📝 Results received: [happy, happily, ...]
```

---

## 🎮 TEST STEPS

1. **Click "Start Game"**
2. **Đợi** "🎤 LISTENING... Speak now!" (màu xanh lá)
3. **Nói TO**: "HAPPY" (gần mic, rõ ràng)
4. **Xem logs** - Phải thấy Volume > 3 dB

---

## ⚠️ KNOWN ISSUES

### **Nếu Volume luôn < 2 dB:**
→ Mic bị lỗi hoặc permission chưa được cấp đúng

### **Nếu thấy Volume cao nhưng vẫn "No match":**
→ Phát âm không chuẩn hoặc internet chậm

### **Nếu không thấy log "🔊 Volume":**
→ Mic permission bị từ chối

---

## 🚀 NEXT STEPS

**Hãy thử:**
1. Nói **RẤT TO** và **GẦN MIC**
2. Nói **tiếng Anh chuẩn**: "HAPPY" (không phải "háp-pi")
3. Kiểm tra **WiFi/4G** đang bật

**Nếu vẫn không được**, có thể:
- Device không hỗ trợ tốt Google Speech
- Cần dùng offline speech recognition (phức tạp hơn)
- Hoặc dùng alternative approach (tap to speak)
