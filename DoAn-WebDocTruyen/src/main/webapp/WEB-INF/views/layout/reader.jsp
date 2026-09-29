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
<body class="reader-body" data-ctx="${pageContext.request.contextPath}" data-theme="dark" data-size="m" data-leading="normal" data-font="serif">

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
        <span class="reader-pct-badge" id="read-pct-badge">0%</span>
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

        <div class="tool-group" role="group" aria-label="Font chữ">
            <button type="button" class="tool-btn" data-set="font" data-val="serif" title="Font có chân (Serif)">Serif</button>
            <button type="button" class="tool-btn" data-set="font" data-val="sans" title="Font không chân (Sans)">Sans</button>
        </div>

        <div class="tool-group" role="group" aria-label="Nền">
            <button type="button" class="tool-btn" data-set="theme" data-val="dark"  title="Nền tối">🌙</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="light" title="Nền sáng">☀️</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="sepia" title="Nền giấy">📜</button>
        </div>

        <%--
          Bật / tắt đọc liên tục, Giọng đọc TTS, Mưa thư giãn, In chương và Phím tắt trợ giúp.
        --%>
        <div class="tool-group">
            <button type="button" class="tool-btn" id="toggle-tts"
                    title="Đọc truyện bằng giọng nói (Text-to-Speech)" aria-label="Đọc giọng nói">🎧</button>
            <button type="button" class="tool-btn" id="toggle-ambient"
                    title="Bật / Tắt âm thanh mưa rơi thư giãn" aria-label="Âm thanh mưa">🌧️</button>
            <button type="button" class="tool-btn" id="btn-print-chapter"
                    title="In chương truyện / Xuất PDF (Ctrl+P)" aria-label="In chương">🖨️</button>
            <button type="button" class="tool-btn" id="toggle-zen"
                    title="Chế độ tập trung Zen Mode (Z)" aria-label="Chế độ tập trung">🧘</button>
            <button type="button" class="tool-btn" id="toggle-continuous"
                    aria-pressed="true" title="Đọc liên tục">∞</button>
            <button type="button" class="tool-btn" id="toggle-shortcuts"
                    title="Phím tắt đọc truyện (?)" aria-label="Phím tắt đọc truyện">⌨</button>
        </div>
    </div>
</header>

<main class="reader-wrap">
    <jsp:include page="${contentPage}" />
</main>

<%-- Thanh điều khiển Giọng đọc Text-to-Speech (TTS) --%>
<div class="reader-tts-bar" id="reader-tts-bar" aria-hidden="true">
    <%-- Hàng chính: điều khiển phát --%>
    <div class="tts-main-row">
        <button type="button" class="tts-btn" id="tts-prev-btn" title="Đoạn trước">⏮</button>
        <button type="button" class="tts-btn tts-btn-play" id="tts-play-btn" title="Phát / Tạm dừng">▶</button>
        <button type="button" class="tts-btn" id="tts-stop-btn" title="Dừng đọc">⏹</button>
        <button type="button" class="tts-btn" id="tts-next-btn" title="Đoạn tiếp">⏭</button>
        <span class="tts-info" id="tts-info">Sẵn sàng</span>
        <span class="tts-rate-badge" id="tts-rate-badge" title="Đổi tốc độ đọc">1.0x</span>
        <button type="button" class="tts-btn tts-btn-settings" id="tts-settings-btn"
                title="Cài đặt giọng đọc" aria-expanded="false">⚙</button>
        <button type="button" class="tts-btn tts-btn-close" id="tts-close-btn" title="Thu gọn">&times;</button>
    </div>
    <%-- Hàng cài đặt nâng cao (ẩn mặc định, mở khi nhấn ⚙) --%>
    <div class="tts-settings-row" id="tts-settings-row">
        <div class="tts-setting-item">
            <label for="tts-voice-select" class="tts-label">🎙 Giọng đọc</label>
            <select id="tts-voice-select" class="tts-select">
                <option value="">— Mặc định trình duyệt —</option>
            </select>
        </div>
        <div class="tts-setting-item">
            <label for="tts-pitch-range" class="tts-label">🎵 Cao độ</label>
            <div class="tts-range-wrap">
                <input type="range" id="tts-pitch-range" class="tts-range"
                       min="0.5" max="2" step="0.1" value="1">
                <span class="tts-range-val" id="tts-pitch-val">1.0</span>
            </div>
        </div>
        <div class="tts-setting-item">
            <label for="tts-volume-range" class="tts-label">🔊 Âm lượng</label>
            <div class="tts-range-wrap">
                <input type="range" id="tts-volume-range" class="tts-range"
                       min="0" max="1" step="0.1" value="1">
                <span class="tts-range-val" id="tts-volume-val">100%</span>
            </div>
        </div>
    </div>
</div>

<%-- Hộp thoại hướng dẫn phím tắt --%>
<dialog id="shortcuts-modal" class="reader-dialog">
    <div class="dialog-card">
        <div class="dialog-head">
            <h3>⌨ Phím tắt đọc truyện</h3>
            <button type="button" class="dialog-close" id="close-shortcuts" aria-label="Đóng">&times;</button>
        </div>
        <div class="dialog-body">
            <div class="shortcut-row">
                <span class="key-combo"><kbd>→</kbd> hoặc <kbd>D</kbd></span>
                <span>Chương kế tiếp</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>←</kbd> hoặc <kbd>A</kbd></span>
                <span>Chương trước</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>T</kbd></span>
                <span>Bật / tắt Mục lục</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>F</kbd></span>
                <span>Bật / tắt Toàn màn hình</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>Z</kbd></span>
                <span>Chế độ đọc tập trung (Zen Mode)</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>S</kbd></span>
                <span>Bật / tắt Đọc giọng nói (TTS)</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>M</kbd></span>
                <span>Bật / tắt Âm thanh mưa rơi</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>?</kbd></span>
                <span>Hiện bảng phím tắt này</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>Esc</kbd></span>
                <span>Đóng hộp thoại / mục lục</span>
            </div>
        </div>
    </div>
</dialog>

<%-- CỤM NÚT ĐIỀU HƯỚNG NỔI (FLOATING DOCK): LÊN ĐẦU TRANG ↑ & XUỐNG BÌNH LUẬN ↓ --%>
<div class="reader-fab-dock" id="reader-fab-dock">
    <%-- Nút 1: Mũi tên lên đầu trang ↑ --%>
    <button type="button" class="reader-fab-btn reader-fab-top" id="reader-fab-top"
            title="Cuộn lên đầu trang" aria-label="Cuộn lên đầu trang">
        <span class="fab-icon-wrap">
            <svg class="fab-arrow-up" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="19" x2="12" y2="5"></line>
                <polyline points="5 12 12 5 19 12"></polyline>
            </svg>
        </span>
        <span class="fab-tooltip">Lên đầu trang ↑</span>
    </button>

    <%-- Nút 2: Mũi tên xuống bình luận ↓ kèm badge chat --%>
    <button type="button" class="reader-fab-btn reader-fab-comment" id="reader-fab-comment"
            title="Chuyển xuống bình luận chương" aria-label="Chuyển xuống bình luận chương">
        <span class="fab-icon-wrap">
            <svg class="fab-arrow-down" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <polyline points="19 12 12 19 5 12"></polyline>
            </svg>
            <span class="fab-chat-badge" aria-hidden="true">💬</span>
        </span>
        <span class="fab-tooltip">Bình luận chương ↓</span>
    </button>
</div>

<%-- Thanh tiến độ đọc mỏng mượt mà ở đỉnh màn hình --%>
<div class="reader-progress-bar" id="reader-progress-bar" aria-hidden="true"></div>

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
    var KEYS = ['size', 'leading', 'theme', 'font'];

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
        var val = load(k);
        if (!val && k === 'theme') {
            try {
                var siteTheme = localStorage.getItem('site.theme') || document.documentElement.getAttribute('data-site-theme');
                if (siteTheme === 'light') val = 'light';
            } catch (e) { }
        }
        apply(k, val || body.getAttribute('data-' + k));
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
 * ĐỊNH DẠNG VĂN HỌC (MARKDOWN VĂN HỌC & TYPOGRAPHY)
 *
 * Chuyển các cú pháp viết lách của tác giả thành HTML tao nhã và an toàn:
 * - **in đậm** -> <strong>
 * - *in nghiêng* -> <em>
 * - ~~gạch ngang~~ -> <s>
 * - Lời thoại (bắt đầu bằng — hoặc - ) -> class .dialogue
 * - Hoa thị ngắt cảnh (* * *, ***, ---) -> class .divider (❖ ❖ ❖)
 * - Trích dẫn (bắt đầu bằng >) -> <blockquote>
 * - Lời nhắn tác giả ([Tác giả: ...]) -> class .author-note
 */
function escapeHtmlSafe(str) {
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function applyInlineFormat(html) {
    if (!html) return '';
    return html
        .replace(/\*\*([^*]+?)\*\*/g, '<strong>$1</strong>')
        .replace(/~~([^~]+?)~~/g, '<s>$1</s>')
        .replace(/(^|[^*])\*([^*]+?)\*(?!\*)/g, '$1<em>$2</em>');
}

function formatChapterBlock(root) {
    if (!root) return;
    var paras = root.querySelectorAll('.chapter-content p');
    for (var i = 0; i < paras.length; i++) {
        var p = paras[i];
        var txt = p.textContent.trim();

        // 1. Phân đoạn hoa thị: * * *, ***, ---, ❖ ❖ ❖
        if (/^(\*\s*\*\s*\*|—{3,}|-{3,}|❖\s*❖\s*❖)$/.test(txt)) {
            var div = document.createElement('div');
            div.className = 'divider';
            div.setAttribute('aria-hidden', 'true');
            div.textContent = '❖ ❖ ❖';
            p.parentNode.replaceChild(div, p);
            continue;
        }

        // 2. Trích dẫn cổ phong / câu danh ngôn (bắt đầu bằng > hoặc &gt;)
        if (/^(&gt;|>)\s*/.test(txt)) {
            var clean = txt.replace(/^(&gt;|>)\s*/, '');
            var bq = document.createElement('blockquote');
            bq.innerHTML = applyInlineFormat(escapeHtmlSafe(clean));
            p.parentNode.replaceChild(bq, p);
            continue;
        }

        // 3. Lời tác giả / Ghi chú
        if (/^\[(Lời tác giả|Tác giả|Ghi chú|Note):/i.test(txt)) {
            p.classList.add('author-note');
        }
        // 4. Lời thoại nhân vật
        else if (/^([—–-]|--)\s+/.test(txt)) {
            p.classList.add('dialogue');
        }

        // Inline formatting (in đậm, in nghiêng, gạch ngang)
        p.innerHTML = applyInlineFormat(p.innerHTML);
    }

    // 5. Ước tính thời gian đọc và tổng số từ của chương
    var blocks = (root.querySelectorAll && root.querySelectorAll('.chapter-block')) || [];
    if (blocks.length === 0 && root.classList && root.classList.contains('chapter-block')) {
        blocks = [root];
    }
    for (var b = 0; b < blocks.length; b++) {
        var blk = blocks[b];
        var contentEl = blk.querySelector('.chapter-content');
        var subEl = blk.querySelector('.reader-sub');
        if (contentEl && subEl && !subEl.querySelector('.read-est')) {
            var words = contentEl.textContent.trim().split(/\s+/).filter(Boolean).length;
            if (words > 0) {
                var mins = Math.max(1, Math.round(words / 220));
                var estSpan = document.createElement('span');
                estSpan.className = 'read-est';
                estSpan.style.opacity = '0.85';
                estSpan.innerHTML = ' &middot; ⏱️ ~' + mins + ' phút đọc (' + words.toLocaleString('vi-VN') + ' từ)';
                subEl.appendChild(estSpan);
            }
        }
    }
}

// Chạy format cho toàn bộ các chương đã nạp sẵn khi tải trang
try {
    formatChapterBlock(document.getElementById('chapters'));
} catch (e) {
    if (window.console) console.warn('Lỗi định dạng văn học:', e);
}

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

    function hideLoading() {
        if (loading) {
            loading.hidden = true;
            loading.style.display = 'none';
        }
    }
    function showLoading() {
        if (loading) {
            loading.hidden = false;
            loading.style.display = 'flex';
        }
    }
    hideLoading();

    try { on = localStorage.getItem(KEY) !== 'off'; } catch (e) { }

    /* ------------------------------------------------------ đánh dấu đã đọc & lịch sử */
    function markRead(block) {
        try {
            var sId = block.getAttribute('data-story-id');
            var cId = block.getAttribute('data-chapter-id');
            if (!sId || !cId) return;

            var key = 'read.' + sId;
            var seen = JSON.parse(localStorage.getItem(key) || '[]');
            if (seen.indexOf(cId) === -1) {
                seen.push(cId);
                localStorage.setItem(key, JSON.stringify(seen));
            }

            // Ghi nhận truyện vừa đọc gần đây để Trang chủ hiển thị "Tiếp tục đọc"
            var sTitle = block.getAttribute('data-story-title');
            var cTitle = block.getAttribute('data-chapter-title');
            var cNo = block.getAttribute('data-chapter-no');
            var recent = {
                storyId: sId,
                storyTitle: sTitle,
                chapterId: cId,
                chapterTitle: cTitle,
                chapterNo: cNo,
                timestamp: Date.now()
            };
            localStorage.setItem('webdoctruyen_recent_read', JSON.stringify(recent));
        } catch (e) { /* trình duyệt chặn lưu trữ — việc đọc không ảnh hưởng */ }
    }

    /* ---------------------------------------------- nạp chương kế tiếp */
    function loadNext() {
        if (busy || !on) return;

        var last = wrap.lastElementChild;
        var nextId = last && last.getAttribute('data-next-id');
        if (!nextId) {                       // hết truyện
            if (done) {
                done.hidden = false;
                done.style.display = 'block';
            }
            hideLoading();
            return;
        }

        busy = true;
        showLoading();

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

                // Định dạng văn học cho block chương mới tải
                formatChapterBlock(block);

                wrap.appendChild(block);
                watch(block);
                syncNav(block);
                hideLoading();
                busy = false;
            })
            .catch(function (err) {
                /*
                 * Hỏng thì TẮT hẳn đọc liên tục và để lộ ba nút điều hướng.
                 * Thử lại vô hạn khi máy chủ đang lỗi chỉ làm mọi thứ tệ hơn;
                 * người đọc vẫn còn nút bấm để đi tiếp.
                 */
                hideLoading();
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
            else hideLoading();
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
        var pctBadge = document.getElementById('read-pct-badge');
        if (pctBadge) pctBadge.textContent = Math.round(pct) + '%';
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

/* ===========================================================================
   TỰ ĐỘNG ẨN / HIỆN THANH READER-BAR KHI CUỘN
   ===========================================================================
   - Cuộn xuống: ẩn thanh reader-bar để tối đa hoá diện tích đọc, không vướng mắt.
   - Cuộn lên hoặc rê chuột lên đỉnh: hiện lại ngay lập tức.
   - Khi bảng mục lục (details#toc-box) đang mở: KHÔNG tự ẩn.
   ======================================================================== */
(function () {
    var bar = document.querySelector('.reader-bar');
    if (!bar) return;

    var lastY = window.scrollY || 0;
    var ticking = false;
    var minThreshold = 70;

    function onScroll() {
        var curY = window.scrollY || 0;
        var diff = curY - lastY;

        var toc = document.getElementById('toc-box');
        var isTocOpen = toc && toc.open;

        if (curY < minThreshold || diff < -12) {
            bar.classList.remove('is-hidden');
        } else if (diff > 12 && curY > minThreshold && !isTocOpen) {
            bar.classList.add('is-hidden');
        }
        lastY = curY;
        ticking = false;
    }

    window.addEventListener('scroll', function () {
        if (!ticking) {
            window.requestAnimationFrame(onScroll);
            ticking = true;
        }
    }, { passive: true });

    // Khi rê chuột lên mép trên màn hình (< 45px) thì luôn hiện thanh
    window.addEventListener('mousemove', function (e) {
        if (e.clientY < 45) {
            bar.classList.remove('is-hidden');
        }
    }, { passive: true });
})();

/* ===========================================================================
   PHÍM TẮT ĐỌC TRUYỆN (KEYBOARD SHORTCUTS)
   ===========================================================================
   - Mũi tên Phải / Phím D: Sang chương sau
   - Mũi tên Trái / Phím A: Về chương trước
   - Phím T: Bật / tắt bảng Mục lục
   - Phím F: Bật / tắt toàn màn hình (Fullscreen)
   - Phím ?: Mở hộp thoại hướng dẫn phím tắt
   - Phím Esc: Đóng bảng mục lục / đóng hộp thoại
   ======================================================================== */
(function () {
    var modal = document.getElementById('shortcuts-modal');
    var openBtn = document.getElementById('toggle-shortcuts');
    var closeBtn = document.getElementById('close-shortcuts');

    function showModal() {
        if (!modal) return;
        if (modal.showModal) modal.showModal();
        else modal.setAttribute('open', '');
    }
    function hideModal() {
        if (!modal) return;
        if (modal.close) modal.close();
        else modal.removeAttribute('open');
    }

    var zenBtn = document.getElementById('toggle-zen');
    function toggleZenMode() {
        var isZen = document.body.classList.toggle('is-zen-mode');
        if (zenBtn) zenBtn.classList.toggle('is-active', isZen);
    }
    if (zenBtn) zenBtn.addEventListener('click', toggleZenMode);

    if (openBtn) openBtn.addEventListener('click', showModal);
    if (closeBtn) closeBtn.addEventListener('click', hideModal);
    if (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal) hideModal();
        });
    }

    window.addEventListener('keydown', function (e) {
        // Không nhận phím tắt nếu người dùng đang nhập văn bản
        var tag = (e.target.tagName || '').toLowerCase();
        if (tag === 'input' || tag === 'textarea' || tag === 'select' || e.target.isContentEditable) {
            return;
        }

        var key = e.key;
        var code = e.code;

        // Phím ? mở trợ giúp
        if (key === '?' || (e.shiftKey && (code === 'Slash' || key === '/'))) {
            e.preventDefault();
            if (modal && modal.open) hideModal();
            else showModal();
            return;
        }

        // Phím Esc đóng TOC nếu đang mở hoặc thoát Zen mode
        if (key === 'Escape') {
            if (document.body.classList.contains('is-zen-mode')) {
                toggleZenMode();
                return;
            }
            var toc = document.getElementById('toc-box');
            if (toc && toc.open) {
                toc.open = false;
            }
            return;
        }

        // Phím Z -> Bật / tắt Zen Mode
        if (code === 'KeyZ') {
            e.preventDefault();
            toggleZenMode();
            return;
        }

        // Phím S -> Bật / tắt Đọc giọng nói (TTS)
        if (code === 'KeyS') {
            e.preventDefault();
            var ttsToggle = document.getElementById('toggle-tts');
            if (ttsToggle) ttsToggle.click();
            return;
        }

        // Phím M -> Bật / tắt Âm thanh mưa rơi
        if (code === 'KeyM') {
            e.preventDefault();
            var rainToggle = document.getElementById('toggle-ambient');
            if (rainToggle) rainToggle.click();
            return;
        }

        // Mũi tên Trái hoặc phím A -> Chương trước
        if (key === 'ArrowLeft' || code === 'KeyA') {
            var prev = document.getElementById('nav-prev');
            if (prev && prev.getAttribute('href')) {
                window.location.href = prev.getAttribute('href');
            }
        }
        // Mũi tên Phải hoặc phím D -> Chương sau
        else if (key === 'ArrowRight' || code === 'KeyD') {
            var next = document.getElementById('nav-next');
            if (next && next.getAttribute('href')) {
                window.location.href = next.getAttribute('href');
            }
        }
        // Phím T -> Mở/đóng Mục lục
        else if (code === 'KeyT') {
            var tocBox = document.getElementById('toc-box');
            if (tocBox) {
                e.preventDefault();
                tocBox.open = !tocBox.open;
            }
        }
        // Phím F -> Toàn màn hình
        else if (code === 'KeyF') {
            e.preventDefault();
            if (!document.fullscreenElement) {
                if (document.documentElement.requestFullscreen) {
                    document.documentElement.requestFullscreen().catch(function () {});
                }
            } else {
                if (document.exitFullscreen) {
                    document.exitFullscreen().catch(function () {});
                }
            }
        }
    });

    /* ========================================================================
     * CỤM NÚT ĐIỀU HƯỚNG NỔI (FLOATING NAVIGATION DOCK) & THANH TIẾN ĐỘ ĐỌC
     * ===================================================================== */
    var fabTop = document.getElementById('reader-fab-top');
    var fabComment = document.getElementById('reader-fab-comment');
    var progressBar = document.getElementById('reader-progress-bar');

    // Nút Lên đầu trang
    if (fabTop) {
        fabTop.addEventListener('click', function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // Nút Chuyển xuống bình luận
    if (fabComment) {
        fabComment.addEventListener('click', function () {
            var commentsSection = document.getElementById('comments') || document.querySelector('.chapter-comments-wrap');
            if (commentsSection) {
                commentsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                var textarea = commentsSection.querySelector('textarea[name="content"]');
                if (textarea) {
                    setTimeout(function () { textarea.focus(); }, 600);
                }
            }
        });
    }

    // Theo dõi cuộn trang: Hiển thị nút Lên đầu trang & Cập nhật thanh tiến độ %
    function onReaderScroll() {
        var scrollY = window.scrollY || window.pageYOffset;
        var scrollHeight = document.documentElement.scrollHeight - window.innerHeight;

        // Hiện nút Lên đầu trang khi cuộn quá 200px
        if (fabTop) {
            fabTop.classList.toggle('is-show', scrollY > 200);
        }

        // Cập nhật thanh tiến độ đọc mỏng ở đỉnh màn hình
        if (progressBar && scrollHeight > 0) {
            var pct = Math.min(100, Math.max(0, (scrollY / scrollHeight) * 100));
            progressBar.style.width = pct + '%';
        }
    }

    window.addEventListener('scroll', onReaderScroll, { passive: true });
    onReaderScroll();

    /* ========================================================================
     * TÍNH NĂNG 1: IN CHƯƠNG TRUYỆN / XUẤT PDF (PRINT-FRIENDLY)
     * ===================================================================== */
    var btnPrint = document.getElementById('btn-print-chapter');
    if (btnPrint) {
        btnPrint.addEventListener('click', function () {
            window.print();
        });
    }

    /* ========================================================================
     * TÍNH NĂNG 2: ÂM THANH MƯA RƠI THƯ GIÃN (AMBIENT RAIN SOUND)
     *
     * Sinh âm thanh mưa rơi thuần 100% bằng Web Audio API, không tốn băng thông,
     * không cần tải tệp mp3 từ mạng, chạy offline mượt mà.
     * ===================================================================== */
    var toggleAmbient = document.getElementById('toggle-ambient');
    var audioCtx = null;
    var noiseNode = null;
    var gainNode = null;
    var isAmbientPlaying = false;

    function initAmbientRain() {
        if (audioCtx) return;
        var AudioContext = window.AudioContext || window.webkitAudioContext;
        if (!AudioContext) return;
        audioCtx = new AudioContext();

        // Tạo 5 giây Pink Noise (tiếng mưa êm tai)
        var bufferSize = audioCtx.sampleRate * 5;
        var buffer = audioCtx.createBuffer(1, bufferSize, audioCtx.sampleRate);
        var output = buffer.getChannelData(0);
        var b0 = 0, b1 = 0, b2 = 0, b3 = 0, b4 = 0, b5 = 0, b6 = 0;
        for (var i = 0; i < bufferSize; i++) {
            var white = Math.random() * 2 - 1;
            b0 = 0.99886 * b0 + white * 0.0555179;
            b1 = 0.99332 * b1 + white * 0.0750759;
            b2 = 0.96900 * b2 + white * 0.1538520;
            b3 = 0.86650 * b3 + white * 0.3104856;
            b4 = 0.55000 * b4 + white * 0.5329522;
            b5 = -0.7616 * b5 - white * 0.0168980;
            output[i] = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.035;
            b6 = white * 0.115926;
        }

        // Bộ lọc Lowpass mô phỏng tiếng mưa rơi ngoài hiên
        var filter = audioCtx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(850, audioCtx.currentTime);

        gainNode = audioCtx.createGain();
        gainNode.gain.setValueAtTime(0.35, audioCtx.currentTime);

        noiseNode = audioCtx.createBufferSource();
        noiseNode.buffer = buffer;
        noiseNode.loop = true;

        noiseNode.connect(filter);
        filter.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        noiseNode.start(0);
    }

    if (toggleAmbient) {
        toggleAmbient.addEventListener('click', function () {
            if (!isAmbientPlaying) {
                try {
                    initAmbientRain();
                    if (audioCtx.state === 'suspended') {
                        audioCtx.resume();
                    }
                    gainNode.gain.setTargetAtTime(0.35, audioCtx.currentTime, 0.1);
                    isAmbientPlaying = true;
                    toggleAmbient.classList.add('is-active');
                    toggleAmbient.title = 'Đang phát tiếng mưa (Bấm để tắt)';
                } catch (e) {
                    if (window.console) console.warn('Không thể phát âm thanh ambient:', e);
                }
            } else {
                if (gainNode) {
                    gainNode.gain.setTargetAtTime(0.0001, audioCtx.currentTime, 0.1);
                }
                isAmbientPlaying = false;
                toggleAmbient.classList.remove('is-active');
                toggleAmbient.title = 'Bật / Tắt âm thanh mưa rơi thư giãn';
            }
        });
    }

    /* ========================================================================
     * TÍNH NĂNG 3: ĐỌC TRUYỆN BẰNG GIỌNG NÓI (TEXT-TO-SPEECH - TTS)
     *
     * Dùng Web Speech API — hỗ trợ chọn giọng đọc, cao độ (pitch), âm lượng,
     * tốc độ. Tự động đọc từng đoạn văn, highlight đoạn đang đọc và cuộn
     * màn hình. Lưu cài đặt vào localStorage để nhớ cho lần sau.
     * ===================================================================== */
    var toggleTts      = document.getElementById('toggle-tts');
    var ttsBar         = document.getElementById('reader-tts-bar');
    var ttsPlayBtn     = document.getElementById('tts-play-btn');
    var ttsStopBtn     = document.getElementById('tts-stop-btn');
    var ttsPrevBtn     = document.getElementById('tts-prev-btn');
    var ttsNextBtn     = document.getElementById('tts-next-btn');
    var ttsCloseBtn    = document.getElementById('tts-close-btn');
    var ttsInfo        = document.getElementById('tts-info');
    var ttsRateBadge   = document.getElementById('tts-rate-badge');
    var ttsSettingsBtn = document.getElementById('tts-settings-btn');
    var ttsSettingsRow = document.getElementById('tts-settings-row');
    var ttsVoiceSelect = document.getElementById('tts-voice-select');
    var ttsPitchRange  = document.getElementById('tts-pitch-range');
    var ttsPitchVal    = document.getElementById('tts-pitch-val');
    var ttsVolumeRange = document.getElementById('tts-volume-range');
    var ttsVolumeVal   = document.getElementById('tts-volume-val');

    var synth = window.speechSynthesis;
    var ttsParagraphs = [];
    var ttsIndex = 0;
    var ttsIsPlaying = false;
    var ttsRate = 1.0;
    var ttsRates = [0.5, 0.8, 1.0, 1.25, 1.5, 2.0];
    var ttsRateIndex = 2; // mặc định 1.0x
    var ttsPitch = 1.0;
    var ttsVolume = 1.0;
    var ttsSelectedVoice = null;
    var ttsAllVoices = [];

    /* --- Khôi phục cài đặt đã lưu --- */
    var TTS_PREFS_KEY = 'readerTtsPrefs';
    function loadTtsPrefs() {
        try {
            var raw = localStorage.getItem(TTS_PREFS_KEY);
            if (!raw) return;
            var prefs = JSON.parse(raw);
            if (prefs.rate != null) {
                ttsRate = prefs.rate;
                var idx = ttsRates.indexOf(ttsRate);
                if (idx >= 0) ttsRateIndex = idx;
                if (ttsRateBadge) ttsRateBadge.textContent = ttsRate + 'x';
            }
            if (prefs.pitch != null) {
                ttsPitch = prefs.pitch;
                if (ttsPitchRange) ttsPitchRange.value = ttsPitch;
                if (ttsPitchVal) ttsPitchVal.textContent = ttsPitch.toFixed(1);
            }
            if (prefs.volume != null) {
                ttsVolume = prefs.volume;
                if (ttsVolumeRange) ttsVolumeRange.value = ttsVolume;
                if (ttsVolumeVal) ttsVolumeVal.textContent = Math.round(ttsVolume * 100) + '%';
            }
            // voiceName sẽ được áp dụng sau khi voices đã load xong
        } catch (e) { /* bỏ qua nếu localStorage lỗi */ }
    }
    function saveTtsPrefs() {
        try {
            var prefs = {
                rate: ttsRate,
                pitch: ttsPitch,
                volume: ttsVolume,
                voiceName: ttsSelectedVoice ? ttsSelectedVoice.name : ''
            };
            localStorage.setItem(TTS_PREFS_KEY, JSON.stringify(prefs));
        } catch (e) { /* bỏ qua */ }
    }
    loadTtsPrefs();

    /* --- Nạp danh sách giọng đọc vào dropdown --- */
    function populateVoiceList() {
        if (!synth || !ttsVoiceSelect) return;
        var voices = synth.getVoices();
        if (voices.length === 0) return;
        ttsAllVoices = voices;

        // Phân nhóm theo ngôn ngữ
        var groups = {};
        var viGroup = [];
        var otherGroups = {};
        for (var i = 0; i < voices.length; i++) {
            var v = voices[i];
            var langCode = v.lang || 'unknown';
            if (langCode === 'vi-VN' || langCode.indexOf('vi') === 0) {
                viGroup.push(v);
            } else {
                if (!otherGroups[langCode]) otherGroups[langCode] = [];
                otherGroups[langCode].push(v);
            }
        }

        // Xoá options cũ (giữ option đầu tiên "Mặc định")
        while (ttsVoiceSelect.options.length > 1) {
            ttsVoiceSelect.remove(1);
        }

        // Thêm nhóm Tiếng Việt trước
        if (viGroup.length > 0) {
            var optgroupVi = document.createElement('optgroup');
            optgroupVi.label = '🇻🇳 Tiếng Việt';
            for (var j = 0; j < viGroup.length; j++) {
                var opt = document.createElement('option');
                opt.value = viGroup[j].name;
                opt.textContent = viGroup[j].name + (viGroup[j].localService ? ' (Offline)' : ' (Online)');
                optgroupVi.appendChild(opt);
            }
            ttsVoiceSelect.appendChild(optgroupVi);
        }

        // Sắp xếp nhóm ngôn ngữ khác
        var langKeys = Object.keys(otherGroups).sort();
        // Map mã ngôn ngữ phổ biến sang tên dễ đọc
        var langNames = {
            'en-US': '🇺🇸 English (US)', 'en-GB': '🇬🇧 English (UK)',
            'ja-JP': '🇯🇵 Tiếng Nhật', 'ko-KR': '🇰🇷 Tiếng Hàn',
            'zh-CN': '🇨🇳 Tiếng Trung (CN)', 'zh-TW': '🇹🇼 Tiếng Trung (TW)',
            'fr-FR': '🇫🇷 Tiếng Pháp', 'de-DE': '🇩🇪 Tiếng Đức',
            'es-ES': '🇪🇸 Tiếng Tây Ban Nha', 'th-TH': '🇹🇭 Tiếng Thái',
            'pt-BR': '🇧🇷 Tiếng Bồ Đào Nha'
        };
        for (var k = 0; k < langKeys.length; k++) {
            var lang = langKeys[k];
            var optgroup = document.createElement('optgroup');
            optgroup.label = langNames[lang] || lang;
            var items = otherGroups[lang];
            for (var m = 0; m < items.length; m++) {
                var opt2 = document.createElement('option');
                opt2.value = items[m].name;
                opt2.textContent = items[m].name + (items[m].localService ? ' (Offline)' : ' (Online)');
                optgroup.appendChild(opt2);
            }
            ttsVoiceSelect.appendChild(optgroup);
        }

        // Khôi phục giọng đã lưu
        try {
            var raw = localStorage.getItem(TTS_PREFS_KEY);
            if (raw) {
                var prefs = JSON.parse(raw);
                if (prefs.voiceName) {
                    ttsVoiceSelect.value = prefs.voiceName;
                    ttsSelectedVoice = findVoiceByName(prefs.voiceName);
                }
            }
        } catch (e) { /* bỏ qua */ }

        // Nếu chưa có giọng đã lưu, tự chọn giọng vi-VN đầu tiên
        if (!ttsSelectedVoice && viGroup.length > 0) {
            ttsSelectedVoice = viGroup[0];
            ttsVoiceSelect.value = viGroup[0].name;
        }
    }

    function findVoiceByName(name) {
        for (var i = 0; i < ttsAllVoices.length; i++) {
            if (ttsAllVoices[i].name === name) return ttsAllVoices[i];
        }
        return null;
    }

    if (synth) {
        populateVoiceList();
        if (speechSynthesis.onvoiceschanged !== undefined) {
            speechSynthesis.onvoiceschanged = populateVoiceList;
        }
    }

    /* --- Sự kiện chọn giọng --- */
    if (ttsVoiceSelect) {
        ttsVoiceSelect.addEventListener('change', function () {
            var name = ttsVoiceSelect.value;
            ttsSelectedVoice = name ? findVoiceByName(name) : null;
            saveTtsPrefs();
            // Nếu đang phát, áp dụng giọng mới ngay
            if (ttsIsPlaying) playCurrentParagraph();
        });
    }

    /* --- Sự kiện slider Pitch --- */
    if (ttsPitchRange) {
        ttsPitchRange.addEventListener('input', function () {
            ttsPitch = parseFloat(ttsPitchRange.value);
            if (ttsPitchVal) ttsPitchVal.textContent = ttsPitch.toFixed(1);
            saveTtsPrefs();
        });
        ttsPitchRange.addEventListener('change', function () {
            if (ttsIsPlaying) playCurrentParagraph();
        });
    }

    /* --- Sự kiện slider Volume --- */
    if (ttsVolumeRange) {
        ttsVolumeRange.addEventListener('input', function () {
            ttsVolume = parseFloat(ttsVolumeRange.value);
            if (ttsVolumeVal) ttsVolumeVal.textContent = Math.round(ttsVolume * 100) + '%';
            saveTtsPrefs();
        });
        ttsVolumeRange.addEventListener('change', function () {
            if (ttsIsPlaying) playCurrentParagraph();
        });
    }

    /* --- Nút mở/đóng cài đặt nâng cao --- */
    if (ttsSettingsBtn && ttsSettingsRow) {
        ttsSettingsBtn.addEventListener('click', function () {
            var isOpen = ttsSettingsRow.classList.toggle('is-open');
            ttsSettingsBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        });
    }

    function collectParagraphs() {
        var paras = document.querySelectorAll('.chapter-content p');
        ttsParagraphs = [];
        paras.forEach(function (p) {
            var txt = p.textContent.trim();
            if (txt && !p.classList.contains('divider')) {
                ttsParagraphs.push(p);
            }
        });
    }

    function clearHighlight() {
        var actives = document.querySelectorAll('.chapter-content p.tts-active');
        actives.forEach(function (el) { el.classList.remove('tts-active'); });
    }

    function updateTtsInfo() {
        if (!ttsInfo) return;
        if (ttsParagraphs.length === 0) {
            ttsInfo.textContent = 'Trống';
        } else {
            ttsInfo.textContent = 'Đoạn ' + (ttsIndex + 1) + '/' + ttsParagraphs.length;
        }
    }

    function playCurrentParagraph() {
        if (!synth || ttsParagraphs.length === 0 || ttsIndex >= ttsParagraphs.length) {
            stopTts();
            return;
        }

        synth.cancel(); // Dừng câu trước đó

        var currentP = ttsParagraphs[ttsIndex];
        clearHighlight();
        currentP.classList.add('tts-active');
        currentP.scrollIntoView({ behavior: 'smooth', block: 'center' });

        updateTtsInfo();

        var text = currentP.textContent.trim();
        var utter = new SpeechSynthesisUtterance(text);
        utter.rate   = ttsRate;
        utter.pitch  = ttsPitch;
        utter.volume = ttsVolume;

        // Áp dụng giọng đọc đã chọn
        if (ttsSelectedVoice) {
            utter.voice = ttsSelectedVoice;
            utter.lang  = ttsSelectedVoice.lang;
        } else {
            utter.lang = 'vi-VN';
        }

        utter.onend = function () {
            if (ttsIsPlaying) {
                ttsIndex++;
                if (ttsIndex < ttsParagraphs.length) {
                    playCurrentParagraph();
                } else {
                    stopTts();
                }
            }
        };

        utter.onerror = function (e) {
            if (window.console) console.warn('Lỗi đọc giọng nói:', e);
            if (ttsIsPlaying) {
                ttsIndex++;
                if (ttsIndex < ttsParagraphs.length) {
                    playCurrentParagraph();
                } else {
                    stopTts();
                }
            }
        };

        synth.speak(utter);
        ttsIsPlaying = true;
        if (ttsPlayBtn) {
            ttsPlayBtn.innerHTML = '⏸';
            ttsPlayBtn.title = 'Tạm dừng';
        }
        if (toggleTts) toggleTts.classList.add('is-active');
    }

    function pauseTts() {
        if (!synth) return;
        if (synth.speaking && !synth.paused) {
            synth.pause();
            ttsIsPlaying = false;
            ttsPlayBtn.innerHTML = '▶';
            ttsPlayBtn.title = 'Tiếp tục phát';
        } else if (synth.paused) {
            synth.resume();
            ttsIsPlaying = true;
            ttsPlayBtn.innerHTML = '⏸';
            ttsPlayBtn.title = 'Tạm dừng';
        } else {
            collectParagraphs();
            playCurrentParagraph();
        }
    }

    function stopTts() {
        if (!synth) return;
        synth.cancel();
        ttsIsPlaying = false;
        ttsIndex = 0;
        clearHighlight();
        if (ttsPlayBtn) {
            ttsPlayBtn.innerHTML = '▶';
            ttsPlayBtn.title = 'Phát';
        }
        if (ttsInfo) ttsInfo.textContent = 'Sẵn sàng';
        if (toggleTts) toggleTts.classList.remove('is-active');
    }

    if (toggleTts) {
        toggleTts.addEventListener('click', function () {
            if (!window.speechSynthesis) {
                alert('Trình duyệt của bạn chưa hỗ trợ Web Speech API.');
                return;
            }
            ttsBar.classList.toggle('is-show');
            if (ttsBar.classList.contains('is-show')) {
                collectParagraphs();
                updateTtsInfo();
            } else {
                stopTts();
            }
        });
    }

    if (ttsPlayBtn) {
        ttsPlayBtn.addEventListener('click', function () {
            if (ttsIsPlaying) {
                pauseTts();
            } else {
                if (synth.paused) {
                    synth.resume();
                    ttsIsPlaying = true;
                    ttsPlayBtn.innerHTML = '⏸';
                } else {
                    collectParagraphs();
                    playCurrentParagraph();
                }
            }
        });
    }

    if (ttsStopBtn) {
        ttsStopBtn.addEventListener('click', stopTts);
    }

    if (ttsPrevBtn) {
        ttsPrevBtn.addEventListener('click', function () {
            if (ttsIndex > 0) {
                ttsIndex--;
                if (ttsIsPlaying) playCurrentParagraph();
                else updateTtsInfo();
            }
        });
    }

    if (ttsNextBtn) {
        ttsNextBtn.addEventListener('click', function () {
            if (ttsIndex < ttsParagraphs.length - 1) {
                ttsIndex++;
                if (ttsIsPlaying) playCurrentParagraph();
                else updateTtsInfo();
            }
        });
    }

    if (ttsRateBadge) {
        ttsRateBadge.addEventListener('click', function () {
            ttsRateIndex = (ttsRateIndex + 1) % ttsRates.length;
            ttsRate = ttsRates[ttsRateIndex];
            ttsRateBadge.textContent = ttsRate + 'x';
            saveTtsPrefs();
            if (ttsIsPlaying) {
                playCurrentParagraph();
            }
        });
    }

    if (ttsCloseBtn) {
        ttsCloseBtn.addEventListener('click', function () {
            stopTts();
            ttsBar.classList.remove('is-show');
        });
    }
})();
</script>

</body>
</html>

