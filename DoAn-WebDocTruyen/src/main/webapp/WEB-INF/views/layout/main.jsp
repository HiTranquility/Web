<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  layout/main.jsp — KHUNG TRANG CHÍNH
================================================================================
  Đây là "layout": nó dựng khung HTML đầy đủ, rồi CHÈN nội dung trang vào giữa.

  CÁCH DÙNG — servlet gọi như sau:
      request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");
      forward("/WEB-INF/views/layout/main.jsp");

  Trang nội dung (home.jsp, list.jsp...) chỉ là MẢNH: không có <html>, không có
  <head>, không có <body>. Nó chỉ chứa phần ruột.

  VÌ SAO LÀM KIỂU NÀY THAY VÌ header.jsp + footer.jsp NHƯ TRƯỚC
    Cách cũ: mỗi trang tự include header rồi include footer.
      -> thẻ <body> mở ở file này, đóng ở file kia. Quên một </div> là vỡ layout
         mà không có lỗi nào báo, IDE cũng không kiểm tra giúp được.
      -> mỗi trang phải nhớ include ĐÚNG CẶP. Lỡ dùng header của main với footer
         của auth là hỏng.
      -> thêm layout thứ 3, thứ 4 thì số cặp phải nhớ tăng theo.

    Cách này: thẻ mở và thẻ đóng nằm CÙNG MỘT FILE. Trang nội dung không cần
    biết gì về khung. Thêm layout mới = thêm một file như file này.

  BIẾN TRANG NỘI DUNG CÓ THỂ ĐẶT (đặt TRƯỚC khi forward, trong servlet):
      contentPage  BẮT BUỘC — đường dẫn tới mảnh nội dung
      pageTitle    tiêu đề tab trình duyệt
      activeNav    mục menu đang sáng: home | browse | rules
      pageCss      tên file CSS riêng của trang, không kèm .css
================================================================================
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%-- head.jsp nạp base.css + components.css + layout-main.css --%>
    <c:set var="layoutCss" value="layout-main" scope="request"/>
    <%@ include file="parts/head.jsp" %>
</head>
<body>

<%@ include file="parts/nav.jsp" %>

<main class="shell">

    <%--
      Thong bao chung — hien MOT LAN o day thay vi lap <c:if> trong tung manh.
      Servlet nao dat "message" thi trang do tu co thong bao, khong phai sua gi.
      Them trang moi cung duoc huong san.
    --%>
    <%--
      THONG BAO SAU KHI CHUYEN TRANG ("flash message").

      Vi sao phai la SESSION chu khong phai request:
        Luu ho so xong, servlet redirect ve /user?action=me. Redirect la mot
        request MOI hoan toan — moi thu dat trong request cu deu mat sach.
        Session song qua nhieu request nen tin nhan di theo duoc.

      Vi sao phai XOA NGAY sau khi hien:
        Khong xoa thi "Da luu ho so" bam theo nguoi dung sang moi trang, tan
        toi luc dang xuat. Doc xong la bo — day dung la ly do cai mau nay ten
        "flash": loe mot cai roi tat.

      c:remove chinh la buoc "doc xong thi bo" do.
    --%>
    <c:if test="${not empty flash}">
        <div class="panel panel-ok"><c:out value="${flash}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>

    <c:if test="${not empty message}">
        <div class="panel panel-warn" style="margin-bottom:22px">
            <c:out value="${message}"/>
        </div>
    </c:if>

    <%--
      Chỗ nội dung được chèn vào.

      Dùng <jsp:include> (include lúc CHẠY) chứ không phải <%@ include %>
      (lúc biên dịch), vì đường dẫn nằm trong biến ${contentPage} — chỉ biết
      được lúc chạy. Include lúc biên dịch cần đường dẫn cố định, viết cứng.
    --%>
    <jsp:include page="${contentPage}" />
</main>

<%@ include file="parts/footer.jsp" %>

<%--
  CHỐNG BẤM GỬI HAI LẦN — cho MỌI form trong layout này.

  VÌ SAO CẦN
    Máy chậm hoặc mạng chậm thì bấm "Lưu truyện" xong trang đứng im vài giây.
    Phản xạ tự nhiên là bấm lại. Hai request cùng bay lên, và với form "Đăng
    truyện mới" là ra HAI truyện giống hệt nhau.

    Vài chỗ đã an toàn sẵn nhờ khoá ở CSDL (chấm sao có khoá chính kép, lưu
    truyện dùng INSERT ... ON DUPLICATE KEY). Nhưng thêm truyện, thêm chương,
    gửi bình luận thì không có gì chặn.

  ĐẶT Ở LAYOUT, không đặt ở từng trang: có hơn ba mươi form trong dự án, bỏ
  sót một cái là lỗi quay lại. Ở đây thì mọi trang dùng layout main đều được
  bảo vệ, kể cả trang thêm sau này.

  Nghe sự kiện "submit" ở document chứ không gắn vào từng form: form nào được
  JavaScript tạo ra sau cũng dính luật này, và chỉ tốn một trình xử lý.
--%>
<script>
(function () {
    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!form || form.tagName !== 'FORM') return;

        /*
         * Bỏ qua form TÌM KIẾM (method GET).
         *
         * Tìm kiếm chỉ đọc, gửi hai lần cũng không hỏng gì, mà khoá nút lại
         * gây khó chịu: gõ từ khoá khác rồi Enter lại là nút đã xám.
         */
        if ((form.method || '').toLowerCase() !== 'post') return;

        var nut = form.querySelector('button[type="submit"], button:not([type])');
        if (!nut || nut.disabled) return;

        /*
         * KHÔNG disable ngay lập tức.
         *
         * Nút bị disable thì trình duyệt KHÔNG gửi kèm giá trị của nó — mà
         * vài form trong dự án phân biệt hành động bằng name/value của nút.
         * Hoãn sang vòng lặp sự kiện kế tiếp: lúc đó dữ liệu đã được thu thập
         * xong, khoá nút không ảnh hưởng gì nữa.
         */
        setTimeout(function () {
            nut.disabled = true;
            nut.dataset.chuCu = nut.textContent;
            nut.textContent = 'Đang gửi…';
        }, 0);

        /*
         * Mở khoá lại nếu người dùng quay lại trang bằng nút Back.
         *
         * Trình duyệt khôi phục trang từ bộ nhớ đệm y nguyên trạng thái lúc
         * rời đi — tức là nút vẫn đang xám và ghi "Đang gửi…". Không có đoạn
         * này thì người dùng quay lại sửa một ô rồi không gửi lại được nữa.
         */
        window.addEventListener('pageshow', function (ev) {
            if (ev.persisted && nut.disabled) {
                nut.disabled = false;
                if (nut.dataset.chuCu) nut.textContent = nut.dataset.chuCu;
            }
        });
    });

    /* Global Toast Notification */
    window.showToast = function (msg) {
        var old = document.getElementById('global-toast');
        if (old) old.remove();
        var toast = document.createElement('div');
        toast.id = 'global-toast';
        toast.className = 'toast-bubble';
        toast.textContent = msg;
        document.body.appendChild(toast);
        setTimeout(function () { toast.classList.add('is-show'); }, 20);
        setTimeout(function () {
            toast.classList.remove('is-show');
            setTimeout(function () { toast.remove(); }, 280);
        }, 2800);
    };

    /* Nút Cuộn lên đầu trang (Back to Top) */
    var bttBtn = document.getElementById('site-back-to-top');
    if (!bttBtn) {
        bttBtn = document.createElement('button');
        bttBtn.type = 'button';
        bttBtn.className = 'back-to-top';
        bttBtn.id = 'site-back-to-top';
        bttBtn.title = 'Lên đầu trang';
        bttBtn.setAttribute('aria-label', 'Cuộn lên đầu trang');
        bttBtn.innerHTML = '<span>↑</span>';
        document.body.appendChild(bttBtn);
    }
    window.addEventListener('scroll', function () {
        bttBtn.classList.toggle('is-show', window.scrollY > 350);
    }, { passive: true });
    bttBtn.addEventListener('click', function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
})();
</script>

</body>
</html>
