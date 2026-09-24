# 🏷️ ISSUE-010: PWA — Cài đặt ứng dụng & Hỗ trợ đọc truyện Offline

> **Đích:** `docs/projects/issues/ISSUE-010-pwa-doc-offline/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-010 |
| **Người làm** | Dev D — Frontend & PWA |
| **Trạng thái** | 🟡 Đang làm |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Toàn bộ hệ thống — Trải nghiệm di động & Offline |
| **Đụng vào** | `src/main/webapp/manifest.json` · `src/main/webapp/sw.js` · `src/main/webapp/WEB-INF/views/layout/parts/head.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

- Cho phép độc giả cài đặt trang web trực tiếp về màn hình chính (Add to Home Screen) trên điện thoại và máy tính như một ứng dụng độc lập (Native App).
- Tích hợp Service Worker để lưu cache các tài nguyên tĩnh (CSS, JS, Icon) và nội dung các chương truyện đã đọc, cho phép người đọc mở lại đọc tiếp ngay cả khi mất mạng internet hoặc đi tàu xe/máy bay.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Có file `manifest.json` đầy đủ tên app, màu nền, icon và display `standalone`.
- [ ] Có file `sw.js` xử lý cache tài nguyên tĩnh và chiến lược Network-First / Cache-Fallback.
- [ ] `head.jsp` khai báo manifest và đăng ký Service Worker mượt mà.
