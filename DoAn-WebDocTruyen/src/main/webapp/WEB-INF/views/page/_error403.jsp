<%@ page pageEncoding="UTF-8" %>
<%--
  _error403.jsp — MẢNH nội dung trang 403.
  TẦNG: views/

  KHI NÀO NGƯỜI DÙNG THẤY TRANG NÀY:
    - Thành viên thường mở URL /admin/*            (AdminFilter chặn)
    - Sửa truyện người khác bằng cách đổi ?id=     (StoryServlet chặn)
    - Gỡ bình luận không phải của mình             (CommentServlet chặn)

  PHÂN BIỆT 401 / 403 / 404:
    401 = chưa đăng nhập     -> hệ thống đá về trang đăng nhập
    403 = ĐÃ đăng nhập nhưng KHÔNG ĐỦ QUYỀN
    404 = không có gì ở đây

  Riêng truyện ở chế độ nháp cố ý trả 404 chứ không phải 403 — vì 403 vô tình
  xác nhận "truyện này CÓ tồn tại", còn 404 thì không tiết lộ gì cả.
--%>
<div class="empty" style="margin-top:60px">
    <div class="empty-icon">🔒</div>
    <h3>Bạn không có quyền vào trang này</h3>
    <p>Trang này dành cho quản trị viên, hoặc cho chủ sở hữu của nội dung.
       Nếu bạn nghĩ đây là nhầm lẫn, hãy liên hệ quản trị viên.</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
</div>
