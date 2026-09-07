<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/reader.jsp — KHUNG TRANG ĐỌC CHƯƠNG              LAYOUT 4 / 5
================================================================================
  Đây là ví dụ rõ nhất cho luật "layout mới CHỈ khi KHUNG khác".

  Khác main.jsp ở ba điểm, và cả ba đều phục vụ đúng một mục tiêu — không có
  gì phân tán khi đang đọc:
    - Thanh trên tối giản: chỉ nút quay lại + tên truyện. KHÔNG có menu.
    - KHÔNG có footer nhiều cột.
    - Nội dung hẹp (~38em) và chữ to hơn — mắt không phải quét ngang quá dài.

  Nhét ba khác biệt này vào main.jsp bằng <c:if> thì file đó đầy điều kiện.
  Tách file rẻ hơn nhiều.
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <c:set var="layoutCss" value="layout-reader" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>

<%--
  data-theme và data-size đặt ngay trên <body>, giá trị mặc định ở đây.
  Đoạn script dưới cùng sẽ đọc lựa chọn đã lưu và ghi đè.
--%>
<body class="reader-body" data-theme="dark" data-size="m" data-leading="normal">

<header class="reader-bar">
    <a class="reader-back"
       href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
        &larr; <c:out value="${story.title}"/>
    </a>

    <span class="reader-meta">
        Chương ${chapter.chapterNo} &middot; ~${chapter.readMinutes} phút đọc
    </span>

    <%--
      BẢNG TUỲ CHỈNH ĐỌC.

      VÌ SAO CHỖ NÀY DÙNG JAVASCRIPT TRONG KHI CẢ DỰ ÁN GẦN NHƯ KHÔNG DÙNG
        Mọi thứ khác trong web này chạy bằng form và link — server dựng lại
        trang. Cỡ chữ thì không thể: gửi request rồi tải lại cả trang chỉ để
        chữ to thêm 2px là vừa chậm vừa mất chỗ đang đọc.

        Đây đúng là loại việc mà JavaScript làm tốt: đổi giao diện tại chỗ,
        không đụng tới dữ liệu, hỏng thì trang vẫn đọc được bình thường.

      VÌ SAO LƯU BẰNG localStorage CHỨ KHÔNG LƯU VÀO CSDL
        Cỡ chữ là tuỳ chọn của MỘT MÀN HÌNH, không phải của một con người.
        Cùng một người đọc trên điện thoại và trên máy tính muốn hai cỡ khác
        nhau. Lưu vào tài khoản là ép cả hai giống nhau, lại còn thêm một
        bảng và một lần ghi CSDL cho việc chẳng liên quan gì tới nội dung.
    --%>
    <div class="reader-tools">
        <div class="tool-group" role="group" aria-label="Cỡ chữ">
            <button type="button" class="tool-btn" data-set="size" data-val="s">A<small>-</small></button>
            <button type="button" class="tool-btn" data-set="size" data-val="m">A</button>
            <button type="button" class="tool-btn" data-set="size" data-val="l">A<small>+</small></button>
        </div>

        <div class="tool-group" role="group" aria-label="Giãn dòng">
            <button type="button" class="tool-btn" data-set="leading" data-val="tight" title="Dòng khít">≡</button>
            <button type="button" class="tool-btn" data-set="leading" data-val="normal" title="Dòng thường">☰</button>
            <button type="button" class="tool-btn" data-set="leading" data-val="loose" title="Dòng thưa">⩸</button>
        </div>

        <div class="tool-group" role="group" aria-label="Nền">
            <button type="button" class="tool-btn" data-set="theme" data-val="dark"  title="Nền tối">🌙</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="light" title="Nền sáng">☀️</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="sepia" title="Nền giấy">📜</button>
        </div>
    </div>
</header>

<main class="reader-wrap">
    <jsp:include page="${contentPage}" />
</main>

<script>
/*
 * Tuỳ chỉnh trải nghiệm đọc.
 *
 * Toàn bộ việc của đoạn này là đặt ba thuộc tính data-* trên <body>. CSS ở
 * layout-reader.css nhìn vào đó mà đổi cỡ chữ, giãn dòng và màu nền. Không có
 * dòng nào ở đây đụng tới style trực tiếp — giữ trang trí ở CSS, hành vi ở JS.
 */
(function () {
    var body = document.body;
    var KEYS = ['size', 'leading', 'theme'];

    /*
     * localStorage có thể NÉM LỖI chứ không chỉ trả về null: trình duyệt ở
     * chế độ ẩn danh hoặc chặn dữ liệu trang sẽ chặn cả việc đọc. Không bọc
     * try/catch thì cả đoạn script chết và ba nhóm nút không nút nào chạy.
     */
    function load(key) {
        try { return localStorage.getItem('reader.' + key); } catch (e) { return null; }
    }
    function save(key, val) {
        try { localStorage.setItem('reader.' + key, val); } catch (e) { /* bỏ qua */ }
    }

    function apply(key, val) {
        body.setAttribute('data-' + key, val);
        // Tô sáng nút đang chọn
        var group = document.querySelectorAll('[data-set="' + key + '"]');
        for (var i = 0; i < group.length; i++) {
            group[i].classList.toggle('is-on', group[i].getAttribute('data-val') === val);
        }
    }

    // Khôi phục lựa chọn lần trước; chưa có thì giữ mặc định đã ghi trên <body>
    KEYS.forEach(function (k) {
        apply(k, load(k) || body.getAttribute('data-' + k));
    });

    /*
     * MỘT trình xử lý cho cả chín nút, gắn ở thẻ cha.
     *
     * Gắn riêng chín lần cũng chạy, nhưng cách này gọn hơn và vẫn đúng nếu
     * sau này thêm nút thứ mười — không phải nhớ gắn thêm.
     */
    var tools = document.querySelector('.reader-tools');
    if (tools) {
        tools.addEventListener('click', function (e) {
            var btn = e.target.closest('.tool-btn');
            if (!btn) return;
            var key = btn.getAttribute('data-set');
            var val = btn.getAttribute('data-val');
            apply(key, val);
            save(key, val);
        });
    }

    /*
     * ĐÁNH DẤU ĐÃ ĐỌC.
     *
     * Vị trí đọc "chính thức" nằm ở bảng bookmarks, do máy chủ ghi khi mở
     * chương. Cái này khác: nó nhớ TỪNG chương đã đọc, ngay cả khi người đọc
     * chưa lưu truyện và chưa đăng nhập. Mục lục ở trang chi tiết đọc lại
     * danh sách này để làm mờ những chương đã qua.
     */
    var meta = document.querySelector('[data-chapter-id]');
    if (meta) {
        try {
            var storyId = meta.getAttribute('data-story-id');
            var key = 'read.' + storyId;
            var done = JSON.parse(localStorage.getItem(key) || '[]');
            var id = meta.getAttribute('data-chapter-id');
            if (done.indexOf(id) === -1) {
                done.push(id);
                localStorage.setItem(key, JSON.stringify(done));
            }
        } catch (e) { /* không lưu được thì thôi, không ảnh hưởng việc đọc */ }
    }
})();
</script>

</body>
</html>
