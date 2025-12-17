# Kế hoạch Tối ưu Giao diện - Light Theme

## Mục tiêu
Chuyển đổi toàn bộ ứng dụng từ dark theme sang light theme hiện đại với:
- Màu sắc sáng, tươi mới, phù hợp nhiều lứa tuổi
- Giao diện gọn gàng, sạch sẽ
- Tối ưu khoảng cách và spacing
- Giảm kích thước các thành phần để hiển thị nhiều nội dung hơn

## Đã hoàn thành

### 1. Cập nhật Colors.xml ✅
- Chuyển background từ `#0D0D0F` → `#F8FAFC` (light gray)
- Cập nhật text colors cho light theme
- Điều chỉnh module colors sang pastel nhẹ nhàng
- Cập nhật border và overlay colors

### 2. Activity Main ✅
- Chuyển background sang light theme
- Đơn giản hóa bottom navigation
- Giảm elevation từ 16dp → 8dp

### 3. Fragment Home - Header ✅
- Giảm margin từ 16dp → 12dp
- Giảm padding từ 20dp → 16dp
- Giảm avatar size từ 64dp → 56dp
- Giảm card elevation từ 8dp → 2dp
- Loại bỏ gradient background, dùng solid color

## Cần làm tiếp

### 4. Fragment Home - Stats Card
- Chuyển sang light theme
- Giảm padding và margin
- Cập nhật text colors
- Tối ưu spacing

### 5. Fragment Home - Search Bar & Content
- Cập nhật màu sắc
- Giảm margin
- Tối ưu kích thước

### 6. Fragment Learn
- Chuyển sang light theme
- Giảm card size từ 160dp → 140dp
- Tối ưu spacing

### 7. Fragment Profile
- Sử dụng fragment_profile_new.xml
- Cập nhật màu sắc cho light theme

### 8. Các Item Layouts
- item_article_modern.xml
- item_vocabulary_card.xml
- item_grammar_card.xml
- Và các item khác

## Nguyên tắc thiết kế

### Spacing
- Margin ngoài: 12dp (thay vì 16-20dp)
- Padding trong: 12-16dp (thay vì 20dp)
- Khoảng cách giữa các phần tử: 8-12dp (thay vì 16dp)

### Card Design
- Corner radius: 16-20dp (thay vì 24dp)
- Elevation: 2-4dp (thay vì 8dp)
- Background: Solid colors thay vì gradients

### Typography
- Giảm 2sp cho mỗi text size
- Sử dụng text colors phù hợp với light theme

### Colors
- Primary: #3B82F6 (Soft Blue)
- Secondary: #8B5CF6 (Soft Purple)
- Success: #10B981 (Emerald)
- Warning: #F59E0B (Amber)
- Error: #EF4444 (Red)
- Background: #F8FAFC
- Surface: #FFFFFF
- Text Primary: #1E293B
- Text Secondary: #64748B
