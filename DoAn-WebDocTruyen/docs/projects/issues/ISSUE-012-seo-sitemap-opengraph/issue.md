# 🏷️ ISSUE-012: Tối ưu SEO, OpenGraph, Sitemap XML & Robots.txt

> **Đích:** `docs/projects/issues/ISSUE-012-seo-sitemap-opengraph/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-012 |
| **Người làm** | Dev D — SEO & Web |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Toàn bộ hệ thống — Định danh & SEO |
| **Đụng vào** | `src/main/webapp/WEB-INF/views/layout/parts/head.jsp` · `src/main/java/truyen/controller/common/SitemapServlet.java` · `src/main/webapp/robots.txt` |

---

## 1. Làm cái gì, và vì sao (Goal)

Hiện tại:
- `head.jsp` chỉ có thẻ `<title>`, thiếu hoàn toàn thẻ mô tả `<meta name="description">` và các thẻ OpenGraph (`og:title`, `og:description`, `og:image`, `og:url`, `og:type`).
- Khi độc giả copy link truyện dán lên Facebook, Zalo, Telegram, mạng xã hội không lấy được ảnh bìa và tóm tắt truyện, chỉ hiện ra ô trắng không tiêu đề.
- Không có file `robots.txt` hướng dẫn bot tìm kiếm và thiếu `sitemap.xml` động để Google / Bing lập chỉ mục các truyện mới.

**Sau khi hoàn thành:**
- `head.jsp` tự động sinh thẻ SEO và OpenGraph chuẩn chỉnh theo từng trang. Khi xem chi tiết truyện, `og:image` tự động lấy ảnh bìa truyện, `og:description` lấy tóm tắt truyện.
- Tạo servlet `/sitemap.xml` sinh file XML sitemap động theo chuẩn sitemaps.org.
- Bổ sung `robots.txt` cho phép bot crawl các trang công khai và chặn các trang riêng tư (admin, user settings).

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] `head.jsp` có đầy đủ `meta description`, `og:title`, `og:description`, `og:image`, `og:url`.
- [ ] Truy cập `/sitemap.xml` trả về XML sitemap hợp lệ với content-type `application/xml`.
- [ ] Truy cập `/robots.txt` trả về quy định bot và trỏ đến sitemap.
