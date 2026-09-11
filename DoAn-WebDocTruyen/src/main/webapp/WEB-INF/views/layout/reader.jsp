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

<%--
  THANH TIẾN ĐỘ ĐỌC — vạch mảnh chạy ngang trên cùng.

  VÌ SAO CẦN Ở ĐÚNG TRANG NÀY
    Từ khi có đọc liên tục, trang không còn kết thúc ở cuối chương nữa: cuộn
    tới đâu là nối thêm chương tới đó. Thanh cuộn của trình duyệt vì thế nói
    dối — nó ngắn lại mỗi lần nạp thêm, nên không cho biết mình đang ở đâu.

    Vạch này đo theo phần đã cuộn của TOÀN BỘ nội dung đang có, nên vẫn đúng
    sau mỗi lần nối thêm chương.

  Để TRỐNG khi chưa có JavaScript: không có script thì vạch đứng yên ở 0% và
  gần như vô hình — không hỏng gì, chỉ là không có thêm thông tin.

  aria-hidden: đây là thứ thuần trang trí. Trình đọc màn hình đã có cách báo
  vị trí riêng, đọc thêm "thanh tiến độ 43%" chỉ gây nhiễu.
--%>
<div class="read-progress" aria-hidden="true"><i id="read-progress-fill"></i></div>

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

        <%--
          Bật / tắt đọc liên tục.

          Nút này để RIÊNG một nhóm vì nó khác loại: ba nhóm trên chỉ đổi cách
          trang TRÔNG ra sao, nút này đổi cách trang HOẠT ĐỘNG.

          aria-pressed cho trình đọc màn hình biết đây là công tắc hai trạng
          thái chứ không phải nút bấm một lần.
        --%>
        <div class="tool-group">
            <button type="button" class="tool-btn" id="toggle-continuous"
                    aria-pressed="true" title="Đọc liên tục">∞</button>
        </div>
    </div>
</header>

<main class="reader-wrap">
    <jsp:include page="${contentPage}" />
</main>

<script>
/*
 * Duong dan goc cua ung dung, do JSP dat vao.
 *
 * JavaScript khong tu biet contextPath. Viet cung "/chapter" thi chay dung
 * khi web deploy o goc nhung sai ngay khi doi sang /webdoctruyen. Day la
 * BIEN DUY NHAT truyen tu JSP sang JS trong ca du an.
 */
var CTX = '${pageContext.request.contextPath}';

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

})();
/*
 * ĐỌC LIÊN TỤC — cuộn hết chương là chương sau tự nối vào bên dưới.
 *
 * BỐN MẢNH GHÉP
 *   1. ?action=raw     trả về CHỈ một thẻ <article>, không có khung trang
 *   2. fetch()         lấy chương sau
 *   3. IntersectionObserver  báo khi sắp đọc hết
 *   4. history.replaceState  đổi thanh địa chỉ khi trôi sang chương mới
 *
 * VÌ SAO KHÔNG DÙNG SỰ KIỆN scroll
 *   scroll bắn hàng trăm lần mỗi giây khi người dùng cuộn nhanh, và mỗi lần
 *   gọi getBoundingClientRect() là một lần trình duyệt phải tính lại bố cục.
 *   IntersectionObserver để trình duyệt tự theo dõi và chỉ báo đúng lúc cần.
 *
 * TẮT JAVASCRIPT THÌ SAO
 *   Toàn bộ đoạn này không chạy, ba nút điều hướng ở cuối trang vẫn nguyên.
 *   Đọc chậm hơn một nhịp bấm, nhưng không mất gì.
 */
(function () {
    var wrap = document.getElementById('chapters');
    var nav  = document.getElementById('reader-nav');
    if (!wrap || !window.IntersectionObserver || !window.fetch) return;

    var KEY = 'reader.continuous';
    var loading = document.getElementById('loading');
    var done    = document.getElementById('chapter-done');
    var busy    = false;
    var on      = true;

    try { on = localStorage.getItem(KEY) !== 'off'; } catch (e) { }

    /* ------------------------------------------------------ đánh dấu đã đọc */
    function markRead(block) {
        try {
            var key = 'read.' + block.getAttribute('data-story-id');
            var seen = JSON.parse(localStorage.getItem(key) || '[]');
            var id = block.getAttribute('data-chapter-id');
            if (seen.indexOf(id) === -1) {
                seen.push(id);
                localStorage.setItem(key, JSON.stringify(seen));
            }
        } catch (e) { /* trình duyệt chặn lưu trữ — việc đọc không ảnh hưởng */ }
    }

    /* ---------------------------------------------- nạp chương kế tiếp */
    function loadNext() {
        if (busy || !on) return;

        var last = wrap.lastElementChild;
        var nextId = last && last.getAttribute('data-next-id');
        if (!nextId) {                       // hết truyện
            if (done) done.hidden = false;
            if (loading) loading.hidden = true;
            return;
        }

        busy = true;
        if (loading) loading.hidden = false;

        fetch(CTX + '/chapter?action=raw&id=' + encodeURIComponent(nextId), {
            credentials: 'same-origin'       // gửi kèm cookie phiên, để server
        })                                   // còn ghi được vị trí đọc
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.text();
            })
            .then(function (html) {
                /*
                 * Dựng HTML trong một thẻ rời rồi mới lấy phần tử ra.
                 * Không dùng innerHTML += trên chính wrap: cách đó vẽ lại toàn
                 * bộ các chương đã có, và mọi trạng thái cuộn bị đặt lại.
                 */
                var box = document.createElement('div');
                box.innerHTML = html;
                var block = box.querySelector('.chapter-block');
                if (!block) throw new Error('không thấy .chapter-block');

                wrap.appendChild(block);
                watch(block);
                syncNav(block);
                if (loading) loading.hidden = true;
                busy = false;
            })
            .catch(function (err) {
                /*
                 * Hỏng thì TẮT hẳn đọc liên tục và để lộ ba nút điều hướng.
                 * Thử lại vô hạn khi máy chủ đang lỗi chỉ làm mọi thứ tệ hơn;
                 * người đọc vẫn còn nút bấm để đi tiếp.
                 */
                if (loading) loading.hidden = true;
                busy = false;
                on = false;
                if (nav) nav.scrollIntoView({ block: 'nearest' });
                if (window.console) console.warn('Đọc liên tục dừng lại:', err);
            });
    }

    /* ------------------------------- cập nhật hai nút prev/next ở cuối */
    function syncNav(block) {
        var nextId = block.getAttribute('data-next-id');
        var nextBtn = document.getElementById('nav-next');
        if (!nextBtn) return;
        if (nextId) {
            nextBtn.setAttribute('href', CTX + '/chapter?action=read&id=' + nextId);
        } else {
            nextBtn.removeAttribute('href');
            nextBtn.textContent = 'Hết truyện';
        }
    }

    /* --------------------------- theo dõi một chương: cuối + tiêu đề */
    var endObserver = new IntersectionObserver(function (entries) {
        entries.forEach(function (e) { if (e.isIntersecting) loadNext(); });
    }, {
        /*
         * rootMargin 600px = nạp TRƯỚC khi người đọc chạm đáy 600 pixel.
         * Đợi họ tới đáy rồi mới bắt đầu tải thì phải ngồi nhìn vòng quay.
         */
        rootMargin: '0px 0px 600px 0px'
    });

    var titleObserver = new IntersectionObserver(function (entries) {
        entries.forEach(function (e) {
            if (!e.isIntersecting) return;
            var b = e.target;

            /*
             * replaceState chứ KHÔNG PHẢI pushState.
             *
             * pushState thêm một mục vào lịch sử cho MỖI chương trôi qua —
             * đọc 20 chương rồi bấm Back là phải bấm 20 lần mới thoát nổi
             * trang đọc. replaceState chỉ sửa mục hiện tại.
             */
            var url = b.getAttribute('data-url');
            if (url && location.pathname + location.search !== url) {
                history.replaceState(null, '', url);
                document.title = b.getAttribute('data-title') || document.title;
            }
            markRead(b);
        });
    }, {
        /* Chỉ đổi địa chỉ khi tiêu đề chương đã lên gần đỉnh màn hình,
           không đổi ngay lúc nó vừa ló ra ở đáy. */
        rootMargin: '-25% 0px -70% 0px'
    });

    function watch(block) {
        var end = block.querySelector('.chapter-end');
        if (end) endObserver.observe(end);
        titleObserver.observe(block);
    }

    /* ------------------------------------------------------- nút bật/tắt */
    var toggle = document.getElementById('toggle-continuous');
    function paint() {
        if (!toggle) return;
        toggle.classList.toggle('is-on', on);
        toggle.setAttribute('aria-pressed', on ? 'true' : 'false');
        toggle.title = on ? 'Đọc liên tục: đang bật' : 'Đọc liên tục: đang tắt';
    }
    if (toggle) {
        toggle.addEventListener('click', function () {
            on = !on;
            try { localStorage.setItem(KEY, on ? 'on' : 'off'); } catch (e) { }
            paint();
            if (on) loadNext();
        });
        paint();
    }

    /* Chương đầu do server dựng — vẫn phải theo dõi như mọi chương khác */
    var first = wrap.querySelector('.chapter-block');
    if (first) { watch(first); markRead(first); }
})();


/* ===========================================================================
   MỤC LỤC THẢ XUỐNG — nạp danh sách chương ở LẦN MỞ ĐẦU TIÊN
   ===========================================================================
   Vì sao nạp muộn: truyện 500 chương là 500 thẻ <a>. In sẵn vào mọi trang đọc
   nghĩa là mỗi lần lật chương đều tải lại từng ấy, trong khi phần lớn người
   đọc không mở mục lục lần nào.

   Vì sao dùng sự kiện "toggle" của <details>: trình duyệt tự bắn sự kiện này
   mỗi lần đóng/mở, nên không cần tự bắt click rồi tự quản trạng thái. Phần
   đóng/mở là việc của HTML, JavaScript chỉ lo phần nạp dữ liệu.
   ======================================================================== */
(function () {
    var box = document.getElementById('toc-box');
    if (!box || !window.fetch) return;      // không có JS/fetch -> giữ link dự phòng

    var body   = document.getElementById('toc-body');
    var loaded = false;

    box.addEventListener('toggle', function () {
        /* Chỉ nạp khi MỞ, và chỉ MỘT lần. Thiếu cờ loaded thì đóng mở năm lần
           là gọi server năm lần cho cùng một danh sách. */
        if (!box.open || loaded) return;
        loaded = true;

        var url = CTX + '/chapter?action=toc'
                + '&storyId=' + encodeURIComponent(box.getAttribute('data-story-id'))
                + '&current='  + encodeURIComponent(box.getAttribute('data-current-id'));

        fetch(url, { credentials: 'same-origin' })
            .then(function (r) {
                if (!r.ok) throw new Error(r.status);
                return r.text();
            })
            .then(function (html) {
                body.innerHTML = html;

                /* Cuộn thẳng tới chương đang đọc.
                   Mở mục lục ở chương 300 mà danh sách đứng ở chương 1 thì
                   người dùng phải tự cuộn — đúng việc mà mục lục sinh ra để
                   khỏi phải làm. block:'center' đặt nó giữa khung, thấy được
                   cả chương trước và sau. */
                var here = body.querySelector('.is-current');
                if (here && here.scrollIntoView) {
                    here.scrollIntoView({ block: 'center' });
                }
            })
            .catch(function () {
                /* Hỏng thì cho lại đường đi bộ, đừng để một ô trống câm lặng. */
                loaded = false;     // cho phép thử lại ở lần mở sau
                body.innerHTML =
                    '<p class="muted" style="padding:14px">Không tải được mục lục. '
                  + '<a href="' + CTX + '/story?action=detail&id='
                  + box.getAttribute('data-story-id') + '#muc-luc">'
                  + 'Xem ở trang truyện →</a></p>';
            });
    });
})();


/* ===========================================================================
   THANH TIẾN ĐỘ ĐỌC
   ===========================================================================
   Đo phần đã cuộn trên TOÀN BỘ nội dung hiện có. Đọc liên tục nối thêm chương
   liên tục nên chiều cao trang thay đổi suốt — vì vậy phải đo lại mỗi lần vẽ,
   không được nhớ sẵn một con số lúc tải trang.

   VÌ SAO DÙNG requestAnimationFrame CHỨ KHÔNG TÍNH THẲNG TRONG scroll
     Sự kiện scroll bắn hàng trăm lần mỗi giây. Đọc scrollHeight trong đó là
     ép trình duyệt tính lại bố cục từng lần một, và trang bắt đầu khựng đúng
     lúc người ta đang cuộn.
     Cách này gom lại: cuộn bao nhiêu lần cũng chỉ vẽ MỘT lần mỗi khung hình.

   passive: true — hứa với trình duyệt là sẽ không gọi preventDefault, nhờ vậy
   nó cuộn ngay chứ không chờ đoạn mã này chạy xong.
   ======================================================================== */
(function () {
    var fill = document.getElementById('read-progress-fill');
    if (!fill) return;

    var cho = false;

    function ve() {
        cho = false;
        var doc = document.documentElement;

        /* Phần cuộn được = chiều cao toàn trang trừ chiều cao màn hình.
           Trang ngắn hơn màn hình thì số này <= 0 -> coi như đã đọc hết,
           vừa tránh chia cho 0 vừa tránh vạch nằm im ở 0% khó hiểu. */
        var cuonDuoc = doc.scrollHeight - window.innerHeight;
        var pct = cuonDuoc > 0
                ? (window.scrollY || doc.scrollTop) / cuonDuoc * 100
                : 100;

        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        fill.style.width = pct.toFixed(1) + '%';
    }

    function hen() {
        if (cho) return;
        cho = true;
        window.requestAnimationFrame(ve);
    }

    window.addEventListener('scroll', hen, { passive: true });
    window.addEventListener('resize', hen);

    /* Đọc liên tục nối thêm chương -> trang cao lên -> phần trăm cũ sai ngay.
       Theo dõi chiều cao khối chứa chương để vẽ lại đúng lúc đó. */
    var wrap = document.querySelector('.reader-wrap');
    if (wrap && window.ResizeObserver) {
        new ResizeObserver(hen).observe(wrap);
    }

    ve();
})();
</script>

</body>
</html>
