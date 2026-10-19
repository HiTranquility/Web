# -*- coding: utf-8 -*-
import sys

file_path = r"C:\Users\Admin\Downloads\Web\DoAn-WebDocTruyen\docs\TONG-HOP-HE-THONG.md"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Update Header metadata
old_hdr = """> **Tài liệu tổng hợp kiến trúc, bản đồ trang, danh mục chức năng đầy đủ, sơ đồ tuần tự hệ thống (Sequence Diagrams) và 30 phân hệ nâng cao đã hoàn thiện.**  
> Cập nhật: 2026-10-13 · **Phiên bản:** Hoàn thiện 30 Module Công nghệ Nâng cao (ISSUE-001 → ISSUE-029 + Immersion & Audit Suite) · **Độ phủ test:** 175/175 tests PASS (100%)."""

new_hdr = """> **Tài liệu tổng hợp kiến trúc, bản đồ trang, danh mục chức năng đầy đủ, sơ đồ tuần tự hệ thống (Sequence Diagrams), ma trận phân quyền RBAC và 32 phân hệ nâng cao đã hoàn thiện.**  
> Cập nhật: 2026-10-14 · **Phiên bản:** Hoàn thiện 32 Module Công nghệ Nâng cao (ISSUE-001 → ISSUE-033: RBAC Đa Tầng, In-Page Quick Top-Up, VIP Unlock Modal, Modern Confirm Dialog) · **Độ phủ test:** 175/175 tests PASS (100%)."""

content = content.replace(old_hdr, new_hdr)

# 2. Update stats table in Section 1
old_sec1 = "| **Quy mô mã nguồn** | **86 lớp Java**, **58 file JSP (38 trang nội dung, 5 layouts, 15 partials)**, **21 bảng CSDL**, **30 file test / 175 bài Unit Test** |"
new_sec1 = "| **Quy mô mã nguồn** | **88 lớp Java**, **61 file JSP (38 trang nội dung, 5 layouts, 18 partials)**, **25 bảng CSDL**, **30 file test / 175 bài Unit Test** |"
content = content.replace(old_sec1, new_sec1)

# 3. Update table in Section 2
old_sec2 = """| **Số lượng chức năng** | 17 – 28 chức năng | **50 chức năng hoàn chỉnh** | **Đạt 178%** (vượt xa toàn bộ yêu cầu cơ bản và tuỳ chọn ban đầu) |
| **Số lượng trang web (Views)** | 30 – 31 trang | **38 trang web JSP + 8 Endpoints** | Bổ sung Admin Wallet, Admin Audit, Thống kê tác giả, FULLTEXT, Lỗi 403, Raw view, REST API, Drive Backup, Gamification, Comic |
| **Khung bố cục (Layouts)** | 4 layouts | **5 layouts** (`main`, `auth`, `reader`, `admin`, `editor`) | Bổ sung layout `editor` chuyên biệt cho sáng tác chương truyện |
| **Số lớp mã nguồn Java** | 47 – 60 lớp | **86 lớp Java** | 22 Controllers, 19 DAOs, 17 Models, 6 Filters, 13 Utilities, 12 Services/Helpers |
| **Số file view JSP** | 50 – 51 files | **58 file JSP** | 38 trang nội dung, 5 layout wrappers, 15 view partials tái sử dụng |
| **Bảng dữ liệu CSDL** | 13 bảng | **21 bảng** | Bổ sung `wallets`, `transactions`, `report_evidence`, `unlocked_chapters`, `reviews`, `review_votes`, `user_quests`, `reading_progress`, `chapter_pages`, `audit_logs` |"""

new_sec2 = """| **Số lượng chức năng** | 17 – 28 chức năng | **52 chức năng hoàn chỉnh** | **Đạt 185%** (vượt xa toàn bộ yêu cầu cơ bản và tuỳ chọn ban đầu) |
| **Số lượng trang web (Views)** | 30 – 31 trang | **38 trang web JSP + 8 Endpoints** | Bổ sung Admin Wallet, Admin Audit, Thống kê tác giả, FULLTEXT, Lỗi 403, Raw view, REST API, Drive Backup, Gamification, Comic |
| **Khung bố cục (Layouts)** | 4 layouts | **5 layouts** (`main`, `auth`, `reader`, `admin`, `editor`) | Bổ sung layout `editor` chuyên biệt cho sáng tác chương truyện |
| **Số lớp mã nguồn Java** | 47 – 60 lớp | **88 lớp Java** | 22 Controllers, 20 DAOs (thêm RbacDAO), 17 Models, 6 Filters, 13 Utilities, 12 Services/Helpers |
| **Số file view JSP** | 50 – 51 files | **61 file JSP** | 38 trang nội dung, 5 layout wrappers, 18 view partials tái sử dụng (thêm Quick Top-up, VIP Unlock, Confirm Modal) |
| **Bảng dữ liệu CSDL** | 13 bảng | **25 bảng** | Bổ sung `wallets`, `transactions`, `report_evidence`, `unlocked_chapters`, `reviews`, `review_votes`, `user_quests`, `reading_progress`, `chapter_pages`, `audit_logs`, `roles`, `permissions`, `role_permissions`, `user_roles` |"""
content = content.replace(old_sec2, new_sec2)

# 4. Insert ISSUE-032 and ISSUE-033 in Section 5
target_issue = "| **NOTIF-001** | **Hệ Thống Thông Báo Đa Kênh Tác Giả** |"
new_issues = """| **ISSUE-032** | **Hệ Thống Phân Quyền Động Đa Tầng (Dynamic RBAC) & Ranh Giới An Toàn** | Cấu trúc 7 vai trò trong CSDL (`SUPER_ADMIN`, `ADMIN`, `MODERATOR`, `EDITOR`, `VERIFIED_AUTHOR`, `VIP_MEMBER`, `USER`) + `GUEST`; 4 bảng CSDL liên kết (`roles`, `permissions`, `role_permissions`, `user_roles`); Lớp quản trị `RbacDAO.java`; Bộ lọc `AdminFilter.java` bảo vệ nghiêm ngặt: `MODERATOR` chỉ kiểm duyệt báo cáo/bình luận, bị chặn tức thì với mã lỗi **403 Forbidden** tại các mục tài chính/kiểm toán nhạy cảm (`/admin/wallet`, `/admin/audit`, `/admin/user`). |
| **ISSUE-033** | **Bộ Ba Modal Tương Tác Trực Quan & Portal Pattern (Modal Ecosystem Suite)** | Khắc phục toàn bộ thiếu sót về trải nghiệm xác nhận và nạp xu: (1) Modal Nạp Xu Nhanh Tại Chỗ (`_quick-topup-modal.jsp`) với 4 gói xu, 1-touch simulated AJAX, tự động cập nhật số dư toàn trang và bắn pháo hoa mừng; (2) Modal Xác Nhận Mở Khoá VIP (`_vip-unlock-modal.jsp`) chi tiết giá gốc, chiết khấu hạng, số dư trước/sau, tự động chuyển nút nạp xu nếu thiếu; (3) Hộp thoại Xác Nhận Thao Tác Hiện Đại Glassmorphism (`_confirm-modal.jsp`) xóa bỏ hoàn toàn `window.confirm()` trình duyệt; (4) Kỹ thuật Portal Teleportation đưa modal ra `document.body` chống lỗi clipping; Nâng cấp Service Worker cache `v2` và cache buster `components.css?v=3.9`. |
| **NOTIF-001** | **Hệ Thống Thông Báo Đa Kênh Tác Giả** |"""
content = content.replace(target_issue, new_issues)

# 5. Update Section 7 database tables
old_sec7 = """## 7. Thiết kế Cơ sở Dữ liệu (18 Bảng CSDL)

Database: `webdoctruyen` (Charset: `utf8mb4`, Collate: `utf8mb4_unicode_ci`, Storage Engine: `InnoDB`)

```text
[users] ───────────────┬──────────────────────┬──────────────────────┐
  │ 1                  │ 1                    │ 1                    │ 1
  ▼ N                  ▼ N                    ▼ N                    ▼ N
[stories]          [user_identities]      [wallets]              [user_quests]
  │ 1                  (Google OIDC)          (Ví xu ảo)             (Gamification)
  ├────────────────┬───────────────────┬──────────────┐
  ▼ N              ▼ N                 ▼ N            ▼ N
[chapters]     [story_tags]        [bookmarks]    [story_reviews]
  │ 1              (N-N tags)          │              │ 1
  ├──────────────┐                     │              ▼ N
  ▼ N            ▼ N                   │          [review_votes]
[comments]   [unlocked_chapters] ◄─────┘
                (Chương VIP mở khoá)
```

### Danh mục 18 Bảng Chi Tiết:
1. **`users`**: Tài khoản người dùng (id, username, email, password_hash, display_name, avatar_url, bio, role, status, ban_reason, created_at).
2. **`user_identities`**: Liên kết tài khoản mạng xã hội (Google Provider UID, email, tên hiển thị, avatar).
3. **`stories`**: Thông tin truyện (id, title, slug, description, cover_url, author_id, status, progress, view_count, rating_sum, rating_count).
4. **`chapters`**: Nội dung chương (`content` kiểu `MEDIUMTEXT`, `FULLTEXT (title, content)`, cờ `is_vip`, `coin_price`).
5. **`tags`**: Danh mục thể loại (id, name, slug, description).
6. **`story_tags`**: Bảng nối nhiều-nhiều giữa truyện và thể loại (`story_id, tag_id`).
7. **`bookmarks`**: Truyện đã lưu và vị trí chương đọc dở (`user_id, story_id, last_chapter_id`).
8. **`comments`**: Bình luận truyện & chương (`story_id`, `chapter_id`, `parent_id`, `likes_count`).
9. **`ratings`**: Chấm sao đánh giá (khóa chính kép `user_id, story_id`, thang điểm 1–5).
10. **`follows`**: Theo dõi tác giả (`follower_id, author_id`).
11. **`reports`**: Báo cáo vi phạm nội dung (`reporter_id, target_type, target_id, category, reason, status`). Hỗ trợ 8 loại vi phạm và liên kết sâu tới nội dung bị báo cáo (ISSUE-025).
12. **`report_evidence`**: Bảng bằng chứng hình ảnh kèm theo báo cáo vi phạm (tối đa 3 ảnh/báo cáo, chỉ admin xem được — ISSUE-025).
13. **`notifications`**: Thông báo người dùng (`user_id, story_id, chapter_id, type, message, is_read`).
14. **`view_logs`**: Nhật ký mở đọc truyện (`user_id, story_id, chapter_id, ip_address, viewed_at`).
15. **`wallets`**: Ví xu ảo người dùng (`user_id, balance, total_spent, updated_at`).
16. **`transactions`**: Nhật ký giao dịch tài chính nguyên tử (`sender_id, receiver_id, amount, type, note`).
17. **`unlocked_chapters`**: Lưu lịch sử mở khóa chương VIP bằng xu ảo (`user_id, story_id, chapter_id, coin_price, unlocked_at` - ISSUE-020).
18. **`story_reviews` & `review_votes`**: Bài đánh giá truyện chuyên sâu kèm gắn thẻ Spoiler và bảng bình chọn hữu ích (ISSUE-022)."""

new_sec7 = """## 7. Thiết kế Cơ sở Dữ liệu & Ma Trận Phân Quyền RBAC (25 Bảng CSDL)

Database: `webdoctruyen` (Charset: `utf8mb4`, Collate: `utf8mb4_unicode_ci`, Storage Engine: `InnoDB`)

```text
[users] ───────────────┬──────────────────────┬──────────────────────┬─────────────────────┐
  │ 1                  │ 1                    │ 1                    │ 1                   │ 1
  ▼ N                  ▼ N                    ▼ N                    ▼ N                   ▼ N
[stories]          [user_identities]      [wallets]              [user_quests]         [user_roles]
  │ 1                  (Google OIDC)          (Ví xu ảo)             (Gamification)        │ N
  ├────────────────┬───────────────────┬──────────────┐                                    ▼ 1
  ▼ N              ▼ N                 ▼ N            ▼ N                                [roles]
[chapters]     [story_tags]        [bookmarks]    [story_reviews]                          │ 1
  │ 1              (N-N tags)          │              │ 1                                  ▼ N
  ├──────────────┬───────────────────┐ │              ▼ N                            [role_permissions]
  ▼ N            ▼ N                 ▼ N          [review_votes]                           │ N
[comments]   [unlocked_chapters] [chapter_pages]                                           ▼ 1
               (Chương VIP)        (Comic Webtoon)                                   [permissions]
```

### Danh mục 25 Bảng Chi Tiết:
1. **`users`**: Tài khoản người dùng (id, username, email, password_hash, display_name, avatar_url, bio, role, status, ban_reason, vip_expire_at, created_at).
2. **`user_identities`**: Liên kết tài khoản mạng xã hội (Google Provider UID, email, tên hiển thị, avatar).
3. **`roles`**: Danh mục vai trò trong hệ thống RBAC (`code`: SUPER_ADMIN, ADMIN, MODERATOR, EDITOR, VERIFIED_AUTHOR, VIP_MEMBER, USER, `name`, `description`).
4. **`permissions`**: Danh mục quyền hạn chi tiết (`code`: MANAGE_SYSTEM, MANAGE_USERS, MANAGE_STORIES, MODERATE_COMMENTS, HANDLE_REPORTS, MANAGE_WALLET, PUBLISH_STORY, READ_VIP_FREE).
5. **`role_permissions`**: Bảng nối phân quyền vai trò (khóa chính kép `role_id, permission_id`).
6. **`user_roles`**: Bảng gán vai trò người dùng đa quyền (khóa chính kép `user_id, role_id`).
7. **`stories`**: Thông tin truyện (id, title, slug, description, cover_url, author_id, status, progress, view_count, rating_sum, rating_count, story_type).
8. **`chapters`**: Nội dung chương (`content` kiểu `MEDIUMTEXT`, `FULLTEXT (title, content)`, cờ `is_vip`, `coin_price`).
9. **`chapter_pages`**: Danh sách trang tranh truyện Comic Webtoon (`chapter_id, page_number, image_url, width, height`).
10. **`tags`**: Danh mục thể loại (id, name, slug, description).
11. **`story_tags`**: Bảng nối nhiều-nhiều giữa truyện và thể loại (`story_id, tag_id`).
12. **`bookmarks`**: Truyện đã lưu và vị trí chương đọc dở (`user_id, story_id, last_chapter_id`).
13. **`reading_progress`**: Lưu vết tiến trình đọc chi tiết đến từng đoạn văn (`user_id, story_id, chapter_id, para_no, percent, updated_at`).
14. **`comments`**: Bình luận truyện & chương (`story_id`, `chapter_id`, `parent_id`, `likes_count`).
15. **`ratings`**: Chấm sao đánh giá (khóa chính kép `user_id, story_id`, thang điểm 1–5).
16. **`follows`**: Theo dõi tác giả (`follower_id, author_id`).
17. **`reports`**: Báo cáo vi phạm nội dung (`reporter_id, target_type, target_id, category, reason, status`).
18. **`report_evidence`**: Bảng bằng chứng hình ảnh kèm theo báo cáo vi phạm (tối đa 3 ảnh/báo cáo, chỉ admin xem được).
19. **`notifications`**: Thông báo người dùng (`user_id, story_id, chapter_id, type, message, is_read`).
20. **`view_logs`**: Nhật ký mở đọc truyện (`user_id, story_id, chapter_id, ip_address, viewed_at`).
21. **`wallets`**: Ví xu ảo người dùng (`user_id, balance, total_spent, updated_at`).
22. **`transactions`**: Nhật ký giao dịch tài chính nguyên tử (`sender_id, receiver_id, amount, type, note`).
23. **`unlocked_chapters`**: Lưu lịch sử mở khóa chương VIP bằng xu ảo (`user_id, story_id, chapter_id, coin_price, unlocked_at`).
24. **`story_reviews` & `review_votes`**: Bài đánh giá truyện chuyên sâu kèm gắn thẻ Spoiler và bình chọn hữu ích.
25. **`audit_logs`**: Nhật ký kiểm toán an ninh ghi lại mọi thao tác nhạy cảm của ban quản trị (`user_id, action, target_type, target_id, details, ip_address, created_at`)."""
content = content.replace(old_sec7, new_sec7)

# 6. Append to Section 11: Các Nâng Cấp Nổi Bật Gần Đây
old_sec11_end = "## 12. Hướng dẫn Vận hành Nhanh"
new_sec11_additions = """12. **Hệ Thống Phân Quyền Động Đa Tầng (Dynamic RBAC Architecture):**
    - **7 Vai trò chuẩn hoá:** `SUPER_ADMIN`, `ADMIN`, `MODERATOR`, `EDITOR`, `VERIFIED_AUTHOR`, `VIP_MEMBER`, `USER` cùng vai trò Khách vãng lai `GUEST`.
    - **Cơ chế tải quyền hạn tự động:** Lớp `RbacDAO.java` nạp vai trò và quyền hạn trực tiếp vào đối tượng `User` trong phiên làm việc.
    - **Ranh giới an toàn nghiêm ngặt (`AdminFilter`):** Phân chia rõ ràng phân khu quản trị. `MODERATOR` được phép kiểm duyệt nội dung, xử lý báo cáo, bình luận nhưng **bị chặn tuyệt đối với mã lỗi 403 Forbidden** khi cố tình truy cập vào các phân hệ tài chính nhạy cảm (`/admin/wallet`, `/admin/audit`, `/admin/user`). Chỉ `SUPER_ADMIN` mới có quyền can thiệp dòng tiền xu và xem nhật ký an ninh.

13. **Bộ Ba Modal Tương Tác Trực Quan & Portal Pattern (Modal Ecosystem Suite):**
    - **Modal Nạp Xu Nhanh Tại Chỗ (`_quick-topup-modal.jsp`):** Người đọc nạp xu trực tiếp từ header hoặc ngay khi bị thiếu xu đọc VIP/tặng hoa mà không phải rời trang; 4 gói nạp linh hoạt (20k/55 xu, 50k/135 xu, 100k/300 xu, 200k/650 xu); nạp 1 chạm qua AJAX, tự động cập nhật số dư toàn bộ trang và bắn pháo hoa mừng; có banner cảnh báo tính toán số xu còn thiếu.
    - **Modal Xác Nhận Mở Khoá VIP (`_vip-unlock-modal.jsp`):** Hiển thị chi tiết bảng giá: Giá gốc, chiết khấu hạng thành viên, giá thực tế, số dư trước và sau khi mua; nút chuyển đổi thông minh sang *"🪙 Nạp Thêm Xu Ngay"* nếu không đủ tiền.
    - **Hộp Thoại Xác Nhận Thao Tác Hiện Đại Glassmorphism (`_confirm-modal.jsp`):** Xóa bỏ hoàn toàn hộp thoại `window.confirm()` mặc định thô cứng của trình duyệt; giao diện Glassmorphism với 3 preset (danger, warning, info); tự động bắt sự kiện qua thuộc tính `data-confirm` trên toàn bộ nút xoá chương, xoá truyện, gỡ tag, ban user, giải quyết báo cáo; cung cấp API `window.showConfirmModal()`.
    - **Kỹ thuật Portal Teleportation & Cache Invalidation:** Tự động đưa modal ra ngoài `document.body` khi kích hoạt để không bị ảnh hưởng bởi CSS layout Reader; nâng cấp PWA Cache `doctruyen-pwa-v2` và gắn cache-buster `components.css?v=3.9`.

---

## 12. Hướng dẫn Vận hành Nhanh"""

content = content.replace(old_sec11_end, new_sec11_additions)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("SUCCESS: TONG-HOP-HE-THONG.md updated cleanly!")
