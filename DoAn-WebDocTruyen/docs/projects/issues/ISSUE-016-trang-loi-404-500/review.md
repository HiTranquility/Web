# 🔍 [Review] Trang báo lỗi 404 & 500 tùy biến — Nghiệm thu ISSUE-016

> **Đích:** `docs/projects/issues/ISSUE-016-trang-loi-404-500/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-016 |
| **Review cho** | ISSUE-016: Trang báo lỗi 404 & 500 tùy biến phong cách truyện |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Cấu hình `web.xml`:**
  - Bổ sung `<error-page>` cho 404, 500 và ngoại lệ `java.lang.Throwable` chuyển tiếp về `/error?code=...`.
- **Backend `ErrorServlet.java`:**
  - Tiếp nhận mã lỗi từ `javax.servlet.error.status_code` hoặc query param.
  - Phân luồng hiển thị 404 (Không tìm thấy trang) và 500 (Sự cố hệ thống).
  - Sử dụng layout chuẩn `layout/main.jsp`.
- **Frontend `views/error/404.jsp` & `views/error/500.jsp`:**
  - Thiết kế hiện đại, thông điệp ấm áp, nút quay về trang chủ và khám phá truyện.
- **Kiểm thử tự động:**
  - Đã bổ sung `ErrorServletTest.java` với 2 ca kiểm thử (Ca 1: 404, Ca 2: 500) đạt 100% OK.
