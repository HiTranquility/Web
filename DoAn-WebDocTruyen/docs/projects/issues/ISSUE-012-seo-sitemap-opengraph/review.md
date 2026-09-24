# 🔍 [Review] SEO, OpenGraph, Sitemap XML & Robots.txt — Nghiệm thu ISSUE-012

> **Đích:** `docs/projects/issues/ISSUE-012-seo-sitemap-opengraph/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-012 |
| **Review cho** | ISSUE-012: Tối ưu SEO, OpenGraph, Sitemap XML & Robots.txt |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **File cấu hình `webapp/robots.txt`:**
  - Thiết lập quy tắc cho bot thu thập dữ liệu (Allow `/`, chặn `/admin/`, `/auth`, `/user`).
  - Trỏ đến vị trí của Sitemap XML.
- **Backend `SitemapServlet.java` (`/sitemap.xml`):**
  - Trả về tài liệu XML chuẩn `http://www.sitemaps.org/schemas/sitemap/0.9`.
  - Tự động nạp danh sách 100 tác phẩm mới nhất cùng các trang tĩnh chính (`/`, `/story?action=list`, `/rank`, `/page?name=rules`, `/page?name=guide`).
- **Giao diện `head.jsp`:**
  - Tích hợp thẻ meta `description`.
  - Tích hợp bộ thẻ OpenGraph (`og:title`, `og:description`, `og:image`, `og:type`, `og:site_name`) tối ưu hiển thị khi chia sẻ link lên Facebook, Zalo, Telegram.
