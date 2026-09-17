<%@ page pageEncoding="UTF-8" %>
<%--
  page/guide.jsp — MẢNH nội dung. Hướng dẫn sử dụng.              CASE 11
  Mục tiêu đề bài: "có điều dẫn sử dụng ... dành cho người sử dụng".
--%>
<div class="doc-page">
    <h1>Hướng dẫn sử dụng</h1>
    <p class="doc-lead">
        Trang này giải thích cách dùng từng chức năng. Đọc mất khoảng 3 phút.
    </p>

    <h2>1. Tìm truyện để đọc</h2>
    <ul>
        <li>Vào <a href="${pageContext.request.contextPath}/story?action=list">Kho truyện</a>
            để xem toàn bộ.</li>
        <li>Bấm vào <b>thể loại</b> ở đầu trang để lọc — ví dụ chỉ xem Tiên hiệp.
            Số trong ngoặc là số truyện của thể loại đó.</li>
        <li>Gõ vào <b>ô tìm kiếm</b> nếu đã biết tên truyện.</li>
        <li>Kết quả lọc có thể <b>chia sẻ được</b> — cứ copy đường dẫn trên
            thanh địa chỉ gửi cho bạn bè.</li>
    </ul>

    <h2>2. Đọc truyện</h2>
    <ul>
        <li>Mở trang truyện rồi bấm <b>Đọc từ đầu</b>, hoặc chọn chương bất kỳ
            trong mục lục.</li>
        <li>Trang đọc được thiết kế riêng: không có menu, chữ to, cột hẹp cho
            đỡ mỏi mắt.</li>
        <li>Dùng nút <b>← →</b> ở cuối chương để chuyển chương.</li>
    </ul>

    <h2>3. Lưu truyện và đọc tiếp</h2>
    <ul>
        <li>Bấm <b>☆ Lưu truyện</b> ở trang truyện. Cần đăng nhập trước.</li>
        <li>Xem lại ở mục <b>Truyện đã lưu</b>.</li>
        <li>Hệ thống <b>tự nhớ</b> bạn đọc tới chương mấy — không cần bấm gì cả.
            Lần sau vào sẽ có nút <b>Đọc tiếp</b> đưa thẳng tới chương đang dở.</li>
    </ul>

    <h2>4. Tải truyện về máy</h2>
    <ul>
        <li>Bấm <b>⬇ Tải .txt</b> ở trang truyện.</li>
        <li>File gồm toàn bộ chương đã đăng, ghép theo thứ tự, đọc được bằng
            Notepad hay bất kỳ ứng dụng đọc sách nào.</li>
    </ul>

    <h2 id="upload">5. Đăng truyện của bạn</h2>
    <ol>
        <li>Đăng nhập, rồi vào <b>Truyện của tôi → + Đăng truyện mới</b>.</li>
        <li>Điền tiêu đề, giới thiệu, chọn thể loại. Đường dẫn thân thiện tự sinh.</li>
        <li>Chọn <b>Bản nháp</b> nếu chưa muốn ai thấy — chỉ mình bạn xem được.
            Khi nào ưng ý thì đổi sang <b>Công khai</b>.</li>
        <li>Lưu xong, bấm <b>+ Thêm chương</b> để đăng chương đầu tiên.
            Số chương được điền sẵn, cứ để nguyên.</li>
        <li>Xuống dòng trong ô nội dung để tách đoạn — hệ thống giữ nguyên.</li>
    </ol>

    <div class="note">
        <b>Chỉ bạn sửa được truyện của mình.</b> Người khác mở đúng đường dẫn
        sửa truyện của bạn cũng bị hệ thống chặn.
    </div>

    <h2>6. Bình luận</h2>
    <ul>
        <li>Cuộn xuống cuối trang truyện, viết vào ô rồi bấm <b>Gửi</b>.</li>
        <li>Bình luận của mình thì <b>gỡ được</b>. Quản trị viên gỡ được của mọi người.</li>
    </ul>

    <h2>7. Tài khoản</h2>
    <ul>
        <li>Đăng ký cần tên đăng nhập (chữ, số, gạch dưới), email và mật khẩu
            từ 6 ký tự.</li>
        <li>Mật khẩu được <b>băm</b> trước khi lưu — kể cả quản trị viên cũng
            không đọc được mật khẩu của bạn.</li>
        <li>Tài khoản bị khoá thì không đăng nhập được, nhưng
            <b>truyện đã đăng vẫn còn</b> trên trang.</li>
    </ul>

    <p class="doc-foot">
        Trước khi đăng bài, đọc qua
        <a href="${pageContext.request.contextPath}/page?name=rules">nội quy cộng đồng</a>.
    </p>
</div>
