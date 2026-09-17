<%@ page pageEncoding="UTF-8" %>
<%--
  page/rules.jsp — MẢNH nội dung. Nội quy cộng đồng.               CASE 11

  Đây là một trong hai mục tiêu đề bài nêu rõ: "có điều dẫn sử dụng và luật
  dành cho người sử dụng".

  Trang tĩnh, không có dữ liệu động — nhưng vẫn là .jsp trong WEB-INF để dùng
  chung khung với mọi trang khác. PageServlet dẫn vào bằng /page?name=rules,
  có DANH SÁCH TRẮNG chặn path traversal.
--%>
<div class="doc-page">
    <h1>Nội quy cộng đồng</h1>
    <p class="doc-lead">
        Đọc kỹ trước khi đăng truyện hoặc bình luận. Tạo tài khoản đồng nghĩa
        với việc bạn đồng ý với những điều dưới đây.
    </p>

    <h2 id="content">1. Nội dung được phép đăng</h2>
    <ul>
        <li>Truyện do <b>chính bạn sáng tác</b>, hoặc bạn có quyền đăng.</li>
        <li>Truyện dịch phải <b>ghi rõ nguồn</b> và tên tác giả gốc.</li>
        <li>Đặt thể loại đúng với nội dung để người đọc tìm được đúng thứ họ muốn.</li>
    </ul>

    <h2>2. Nội dung bị cấm</h2>
    <ul>
        <li><b>Spam</b> — đăng lặp lại, đăng nội dung vô nghĩa để đẩy tương tác.</li>
        <li><b>Link rác và link độc hại</b> — quảng cáo, cờ bạc, trang lừa đảo,
            hoặc bất kỳ đường dẫn nào dụ người khác tải phần mềm lạ.</li>
        <li>Nội dung vi phạm pháp luật Việt Nam.</li>
        <li>Nội dung xúc phạm cá nhân, tổ chức, dân tộc, tôn giáo.</li>
        <li>Sao chép truyện của người khác rồi nhận là của mình.</li>
    </ul>

    <h2>3. Ứng xử khi bình luận</h2>
    <ul>
        <li><b>Văn minh</b> — góp ý về tác phẩm, không công kích con người.</li>
        <li>Không tiết lộ nội dung quan trọng (spoil) mà không báo trước.</li>
        <li>Không tranh cãi kéo dài; có vấn đề thì báo quản trị viên.</li>
        <li>Không để lộ thông tin cá nhân của mình hay của người khác.</li>
    </ul>

    <h2>4. Xử lý vi phạm</h2>
    <table>
        <tr><th style="width:12em">Mức độ</th><th>Xử lý</th></tr>
        <tr><td>Bình luận vi phạm</td>
            <td>Gỡ bình luận. Nội dung vẫn được lưu lại làm bằng chứng.</td></tr>
        <tr><td>Truyện vi phạm</td>
            <td>Gỡ khỏi trang. Tác giả có thể khiếu nại với quản trị viên.</td></tr>
        <tr><td>Vi phạm nhiều lần</td>
            <td><b>Khoá tài khoản.</b> Truyện đã đăng vẫn giữ nguyên để độc giả
                đang theo dõi không bị mất.</td></tr>
    </table>

    <h2>5. Quyền của bạn</h2>
    <ul>
        <li>Bạn giữ <b>toàn bộ bản quyền</b> với truyện mình đăng.</li>
        <li>Bạn có thể gỡ truyện của mình bất cứ lúc nào.</li>
        <li>Bị xử lý mà thấy oan thì liên hệ quản trị viên để xem lại.</li>
    </ul>

    <div class="note">
        <b>Tóm lại ba câu:</b> đăng thứ của mình, không rải link rác,
        và nói chuyện với nhau tử tế.
    </div>

    <p class="doc-foot">
        Chưa biết dùng trang thế nào?
        <a href="${pageContext.request.contextPath}/page?name=guide">Xem hướng dẫn sử dụng</a>
    </p>
</div>
