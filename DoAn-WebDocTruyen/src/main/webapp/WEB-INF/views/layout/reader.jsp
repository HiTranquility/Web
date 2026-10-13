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
<body class="reader-body" data-ctx="${pageContext.request.contextPath}" data-theme="dark" data-size="m" data-leading="normal" data-font="serif"
      data-csrf="${csrfToken}" data-resume-para="${resumePara}"
      data-kind="${story.comic ? 'comic' : 'text'}" data-unit="${story.unitLabel}" data-cwidth="fit">

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
        Chương ${chapter.chapterNo} &middot;
        <c:choose>
            <c:when test="${story.comic}">${chapter.pageCount} trang</c:when>
            <c:otherwise>~${chapter.readMinutes} phút đọc</c:otherwise>
        </c:choose>
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
        <%--
          LƯU TRUYỆN & ĐÁNH DẤU ĐOẠN — hai việc KHÁC NHAU, hai nút khác nhau.
            📚 lưu cả truyện vào tủ sách (giống nút "Lưu truyện" ở trang chi tiết)
            🔖 đánh dấu ĐOẠN đang ở đầu màn hình để đọc lại sau (phím B)
          Còn vị trí đọc thì tự nhớ, không cần nút nào — xem cuối read.jsp.
        --%>
        <div class="tool-group" role="group" aria-label="Lưu và đánh dấu">
            <button type="button" class="tool-btn ${bookmarked ? 'is-on' : ''}" id="btn-save-story"
                    data-story-id="${story.id}" aria-pressed="${bookmarked ? 'true' : 'false'}"
                    title="${bookmarked ? 'Đã lưu vào tủ sách — bấm để bỏ lưu' : 'Lưu truyện vào tủ sách'}">📚</button>
            <button type="button" class="tool-btn" id="btn-mark-para"
                    title="Đánh dấu ${story.unitLabel} đang đọc (B)" aria-label="Đánh dấu ${story.unitLabel} đang đọc">🔖</button>
            <button type="button" class="tool-btn text-only" id="btn-highlights-drawer"
                    title="Xem các đoạn tô màu & trích dẫn (H)" aria-label="Tô màu & Trích dẫn">🖍️<span class="tool-badge-pill" id="hl-counter-badge" style="display:none">0</span></button>
            <button type="button" class="tool-btn text-only" id="btn-toggle-hl-visibility"
                    title="Ẩn / Hiện màu tô đoạn văn (Chế độ đọc sạch sẽ - Alt+H)" aria-label="Ẩn hiện màu tô">👁️</button>
        </div>

        <%--
          BỀ RỘNG TRANG — chỉ có ở truyện tranh (CSS ẩn ở truyện chữ, và ẩn
          ngược lại cỡ chữ / giãn dòng / font / giọng đọc ở truyện tranh).
          Cùng cơ chế data-set như các nút khác: JS chỉ đặt data-cwidth lên
          <body>, CSS lo phần còn lại, lựa chọn nhớ trong localStorage.
        --%>
        <div class="tool-group comic-only" role="group" aria-label="Bề rộng trang">
            <button type="button" class="tool-btn" data-set="cwidth" data-val="fit" title="Vừa khung đọc (760px)">▭</button>
            <button type="button" class="tool-btn" data-set="cwidth" data-val="wide" title="Rộng (1100px)">⬌</button>
            <button type="button" class="tool-btn" data-set="cwidth" data-val="full" title="Toàn màn hình (100%)">⛶</button>
        </div>

        <div class="tool-group text-only" role="group" aria-label="Cỡ chữ">
            <button type="button" class="tool-btn" data-set="size" data-val="s">A<small>-</small></button>
            <button type="button" class="tool-btn" data-set="size" data-val="m">A</button>
            <button type="button" class="tool-btn" data-set="size" data-val="l">A<small>+</small></button>
        </div>

        <div class="tool-group text-only" role="group" aria-label="Giãn dòng">
            <button type="button" class="tool-btn" data-set="leading" data-val="tight" title="Dòng khít">≡</button>
            <button type="button" class="tool-btn" data-set="leading" data-val="normal" title="Dòng thường">☰</button>
            <button type="button" class="tool-btn" data-set="leading" data-val="loose" title="Dòng thưa">⩸</button>
        </div>

        <div class="tool-group text-only" role="group" aria-label="Font chữ">
            <button type="button" class="tool-btn" data-set="font" data-val="serif" title="Font có chân (Serif)">Serif</button>
            <button type="button" class="tool-btn" data-set="font" data-val="sans" title="Font không chân (Sans)">Sans</button>
        </div>

        <div class="tool-group" role="group" aria-label="Nền">
            <button type="button" class="tool-btn" data-set="theme" data-val="dark"  title="Nền tối">🌙</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="light" title="Nền sáng">☀️</button>
            <button type="button" class="tool-btn" data-set="theme" data-val="sepia" title="Nền giấy">📜</button>
        </div>

        <%--
          Bật / tắt đọc liên tục, Tự cuộn trang, Giọng đọc TTS, Mưa thư giãn, In chương và Phím tắt trợ giúp.
        --%>
        <div class="tool-group">
            <button type="button" class="tool-btn" id="toggle-autoscroll"
                    title="Tự động cuộn trang (C)" aria-label="Tự động cuộn">⚡📜</button>
            <button type="button" class="tool-btn text-only" id="toggle-tts"
                    title="Đọc truyện bằng giọng nói (Text-to-Speech)" aria-label="Đọc giọng nói">🎧</button>
            <button type="button" class="tool-btn" id="toggle-ambient"
                    title="Âm thanh môi trường thư giãn (M)" aria-label="Âm thanh môi trường">🎵<span id="ambient-current-icon" style="font-size:0.75rem;">🌧️</span></button>
            <button type="button" class="tool-btn" id="toggle-sleep-timer"
                    title="Hẹn giờ tắt đọc truyện (Sleep Timer - Alt+T)" aria-label="Hẹn giờ tắt" style="position:relative;">⏱️<span id="sleep-timer-badge" class="sleep-timer-badge" style="display:none"></span></button>
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

<%-- BẢNG ĐIỀU KHIỂN ÂM THANH MÔI TRƯỜNG THƯ GIÃN (AMBIENT SOUNDSCAPES) --%>
<div class="reader-ambient-pop" id="reader-ambient-pop" hidden>
    <div class="ambient-pop-head">
        <span class="ambient-pop-title">🎵 Âm thanh thư giãn</span>
        <button type="button" class="ambient-pop-close" id="ambient-pop-close" aria-label="Đóng">&times;</button>
    </div>
    <div class="ambient-sound-types">
        <button type="button" class="ambient-type-btn is-active" data-sound="rain" title="Mưa rơi ngoài hiên">
            <span class="amb-icon">🌧️</span><span class="amb-name">Mưa rào</span>
        </button>
        <button type="button" class="ambient-type-btn" data-sound="fire" title="Lò sưởi ấm cúng / Lửa trại">
            <span class="amb-icon">🔥</span><span class="amb-name">Lò sưởi</span>
        </button>
        <button type="button" class="ambient-type-btn" data-sound="waves" title="Sóng biển đêm vỗ bờ">
            <span class="amb-icon">🌊</span><span class="amb-name">Sóng biển</span>
        </button>
        <button type="button" class="ambient-type-btn" data-sound="night" title="Đêm hè tĩnh lặng & tiếng dế">
            <span class="amb-icon">🦗</span><span class="amb-name">Đêm hè</span>
        </button>
    </div>
    <div class="ambient-ctrl-row">
        <button type="button" class="btn-ambient-play" id="btn-ambient-toggle-play">
            <span id="amb-play-icon">▶</span> <span id="amb-play-text">Bật âm thanh</span>
        </button>
        <div class="ambient-volume-wrap">
            <span class="vol-icon">🔊</span>
            <input type="range" id="ambient-volume-slider" min="0" max="100" value="35" class="ambient-slider">
            <span class="vol-pct" id="ambient-vol-val">35%</span>
        </div>
    </div>
</div>

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
                <span>Bật / tắt Âm thanh thư giãn (Ambient)</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>Alt</kbd> + <kbd>T</kbd></span>
                <span>Hẹn giờ tắt đọc truyện (Sleep Timer)</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>B</kbd></span>
                <span>Đánh dấu / bỏ đánh dấu đoạn đang đọc</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>C</kbd></span>
                <span>Bật / tắt Tự động cuộn trang</span>
            </div>
            <div class="shortcut-row comic-only">
                <span class="key-combo"><kbd>J</kbd> / <kbd>K</kbd></span>
                <span>Cuộn nhanh 1 trang ảnh (Truyện tranh)</span>
            </div>
            <div class="shortcut-row">
                <span class="key-combo"><kbd>H</kbd></span>
                <span>Bật / tắt Bảng trích dẫn & tô màu</span>
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

<%-- BẢNG HẸN GIỜ TẮT ĐỌC TRUYỆN (SLEEP TIMER DIALOG) --%>
<dialog id="sleep-timer-modal" class="reader-dialog sleep-timer-dialog">
    <div class="dialog-card sleep-timer-card">
        <div class="dialog-head">
            <div class="hl-drawer-title-box">
                <h4>⏱️ Hẹn giờ tắt đọc truyện</h4>
            </div>
            <button type="button" class="dialog-close" id="close-sleep-timer" aria-label="Đóng">&times;</button>
        </div>
        <div class="dialog-body sleep-timer-body">
            <p class="sleep-desc">Tự động tạm dừng giọng đọc, tắt âm thanh thư giãn, dừng tự cuộn và làm mờ màn hình khi hết giờ để bạn ngủ ngon.</p>
            
            <div class="sleep-status-box" id="sleep-status-box" style="display:none">
                <div class="sleep-countdown-val" id="sleep-countdown-val">00:00</div>
                <div class="sleep-status-lbl">Thời gian đọc truyện còn lại</div>
                <button type="button" class="btn btn-danger btn-sm" id="btn-cancel-sleep-timer">❌ Hủy hẹn giờ</button>
            </div>

            <div class="sleep-presets" id="sleep-presets">
                <button type="button" class="sleep-btn" data-mins="15">
                    <span class="sl-time">15</span><span class="sl-unit">phút</span>
                </button>
                <button type="button" class="sleep-btn" data-mins="30">
                    <span class="sl-time">30</span><span class="sl-unit">phút</span>
                </button>
                <button type="button" class="sleep-btn" data-mins="45">
                    <span class="sl-time">45</span><span class="sl-unit">phút</span>
                </button>
                <button type="button" class="sleep-btn" data-mins="60">
                    <span class="sl-time">60</span><span class="sl-unit">phút</span>
                </button>
                <button type="button" class="sleep-btn sleep-btn-chapter" data-mins="chapter">
                    <span class="sl-time">📖</span><span class="sl-unit">Tự tắt khi hết chương</span>
                </button>
            </div>
        </div>
    </div>
</dialog>

<%-- Rèm che màn đêm mờ dịu khi hết giờ --%>
<div class="screen-sleep-curtain" id="screen-sleep-curtain" hidden>
    <div class="sleep-curtain-msg">
        <span class="sleep-moon">🌙</span>
        <h3>Chúc bạn ngủ ngon!</h3>
        <p>Hẹn giờ đọc truyện đã kết thúc. Hãy nghỉ ngơi để nạp lại năng lượng nhé.</p>
        <button type="button" class="btn btn-ghost btn-sm" id="btn-wake-up">Tiếp tục đọc truyện</button>
    </div>
</div>

<%-- MODAL TẠO ẢNH TRÍCH DẪN NGHỆ THUẬT (QUOTE CARD GENERATOR) --%>
<dialog id="quote-card-modal" class="reader-dialog quote-card-dialog">
    <div class="dialog-card quote-card-box">
        <div class="dialog-head">
            <div class="hl-drawer-title-box">
                <h4>📸 Ảnh trích dẫn nghệ thuật</h4>
            </div>
            <button type="button" class="dialog-close" id="close-quote-card" aria-label="Đóng">&times;</button>
        </div>
        <div class="dialog-body quote-card-body">
            <div class="qc-preview-container">
                <canvas id="quote-card-canvas" width="1080" height="1350" class="qc-canvas"></canvas>
            </div>
            <div class="qc-controls">
                <div class="qc-style-label">Chọn phong cách thẩm mỹ:</div>
                <div class="qc-themes">
                    <button type="button" class="qc-theme-btn is-active" data-theme="sepia" title="Cổ Điển Ngà Vàng">📜 Cổ điển</button>
                    <button type="button" class="qc-theme-btn" data-theme="dark" title="Dạ Yến Ánh Kim">🌌 Dạ yến</button>
                    <button type="button" class="qc-theme-btn" data-theme="pastel" title="Hoàng Hôn Thơ Mộng">🌸 Thơ mộng</button>
                    <button type="button" class="qc-theme-btn" data-theme="emerald" title="Ngọc Bích Thanh Tịnh">🍃 Ngọc bích</button>
                </div>
                <div class="qc-actions">
                    <button type="button" class="btn btn-ghost btn-sm" id="btn-copy-quote-card">📋 Sao chép ảnh</button>
                    <button type="button" class="btn btn-primary btn-sm" id="btn-download-quote-card">💾 Tải ảnh PNG</button>
                </div>
            </div>
        </div>
    </div>
</dialog>

<%-- BỘ CÔNG CỤ TÔ MÀU NỔI (FLOATING HIGHLIGHT TOOLBAR) --%>
<div class="reader-hl-toolbar" id="reader-hl-toolbar" aria-hidden="true">
    <div class="hl-colors">
        <button type="button" class="hl-color-btn hl-c-yellow" data-color="yellow" title="Tô Vàng nắng">
            <span class="hl-swatch"></span>
        </button>
        <button type="button" class="hl-color-btn hl-c-green" data-color="green" title="Tô Xanh ngọc">
            <span class="hl-swatch"></span>
        </button>
        <button type="button" class="hl-color-btn hl-c-blue" data-color="blue" title="Tô Xanh lam">
            <span class="hl-swatch"></span>
        </button>
        <button type="button" class="hl-color-btn hl-c-purple" data-color="purple" title="Tô Tím mộng mơ">
            <span class="hl-swatch"></span>
        </button>
        <button type="button" class="hl-color-btn hl-c-pink" data-color="pink" title="Tô Hồng san hô">
            <span class="hl-swatch"></span>
        </button>
    </div>
    <div class="hl-divider"></div>
    <div class="hl-actions">
        <button type="button" class="hl-action-btn" id="hl-action-note" title="Viết ghi chú cá nhân cho đoạn này">
            <span>📝</span><span class="hl-lbl">Ghi chú</span>
        </button>
        <button type="button" class="hl-action-btn" id="hl-action-mark" title="Đánh dấu đoạn này vào Tủ sách">
            <span>🔖</span><span class="hl-lbl">Đánh dấu</span>
        </button>
        <button type="button" class="hl-action-btn" id="hl-action-copy" title="Sao chép trích dẫn">
            <span>📋</span><span class="hl-lbl">Trích dẫn</span>
        </button>
        <button type="button" class="hl-action-btn" id="hl-action-quote-card" title="Tạo ảnh trích dẫn nghệ thuật">
            <span>📸</span><span class="hl-lbl">Xuất ảnh</span>
        </button>
        <button type="button" class="hl-action-btn hl-remove-btn" id="hl-action-clear" title="Xóa tô màu">
            <span>🗑️</span>
        </button>
    </div>
</div>

<%-- NGĂN KÉO / DIALOG DANH SÁCH ĐIỂM NHẤN & TRÍCH DẪN --%>
<dialog id="highlights-drawer" class="reader-dialog hl-drawer">
    <div class="dialog-card hl-drawer-card">
        <div class="dialog-head">
            <div class="hl-drawer-title-box">
                <h3>🖍️ Điểm nhấn & Trích dẫn</h3>
                <span class="hl-story-tag"><c:out value="${story.title}"/></span>
            </div>
            <button type="button" class="dialog-close" id="close-highlights" aria-label="Đóng">&times;</button>
        </div>
        <div class="dialog-body hl-drawer-body">
            <%-- Thanh tìm kiếm & chuyển đổi phạm vi --%>
            <div class="hl-toolbar-controls">
                <div class="hl-search-wrap">
                    <input type="search" id="hl-search-input" class="hl-search-input" placeholder="🔍 Tìm kiếm từ khoá trong trích dẫn..." autocomplete="off">
                </div>
                <div class="hl-scope-toggle">
                    <button type="button" class="hl-scope-btn is-active" id="hl-scope-chapter" data-scope="chapter">Chương này</button>
                    <button type="button" class="hl-scope-btn" id="hl-scope-all" data-scope="all">Cả bộ truyện</button>
                </div>
            </div>

            <%-- Bộ lọc màu sắc --%>
            <div class="hl-filter-tabs" id="hl-filter-tabs">
                <button type="button" class="hl-filter-btn is-active" data-filter="all">Tất cả (<span id="hl-tab-all-count">0</span>)</button>
                <button type="button" class="hl-filter-btn" data-filter="yellow">🟡 Vàng (<span id="hl-tab-yellow-count">0</span>)</button>
                <button type="button" class="hl-filter-btn" data-filter="green">🟢 Xanh (<span id="hl-tab-green-count">0</span>)</button>
                <button type="button" class="hl-filter-btn" data-filter="blue">🔵 Lam (<span id="hl-tab-blue-count">0</span>)</button>
                <button type="button" class="hl-filter-btn" data-filter="purple">🟣 Tím (<span id="hl-tab-purple-count">0</span>)</button>
                <button type="button" class="hl-filter-btn" data-filter="pink">🔴 Hồng (<span id="hl-tab-pink-count">0</span>)</button>
            </div>

            <%-- Thanh tác vụ nhanh hàng loạt --%>
            <div class="hl-batch-bar">
                <span class="hl-summary-label" id="hl-summary-label">Đang hiển thị 0 đoạn</span>
                <div class="hl-batch-actions">
                    <button type="button" class="btn-batch-action" id="hl-btn-export-all" title="Sao chép danh sách trích dẫn đang xem ra clipboard">📋 Chép tất cả</button>
                    <button type="button" class="btn-batch-action btn-batch-danger" id="hl-btn-clear-chapter" title="Dọn dẹp các điểm nhấn trong chương này">🧹 Xóa sạch chương này</button>
                </div>
            </div>

            <div class="hl-list-items" id="hl-drawer-list">
                <!-- Nội dung danh sách trích dẫn nạp bằng JavaScript -->
            </div>
        </div>
    </div>
</dialog>

<%-- MODAL NHẬP GHI CHÚ CÁ NHÂN CHO ĐOẠN TRÍCH (READER ANNOTATION MODAL) --%>
<dialog id="hl-note-modal" class="reader-dialog hl-note-dialog">
    <div class="dialog-card hl-note-card">
        <div class="dialog-head">
            <div class="hl-drawer-title-box">
                <h4>📝 Ghi chú cá nhân</h4>
            </div>
            <button type="button" class="dialog-close" id="close-hl-note" aria-label="Đóng">&times;</button>
        </div>
        <div class="dialog-body hl-note-body">
            <blockquote class="hl-note-preview" id="hl-note-preview-text"></blockquote>
            <label for="hl-note-textarea" class="hl-note-label">Cảm nghĩ / Suy ngẫm của bạn:</label>
            <textarea id="hl-note-textarea" class="hl-note-textarea" rows="3" maxlength="500" placeholder="Viết cảm nghĩ, suy ngẫm hoặc ghi chú cá nhân của bạn vào đây..."></textarea>
            <div class="hl-note-actions">
                <button type="button" class="btn btn-ghost btn-sm" id="btn-cancel-hl-note">Huỷ</button>
                <button type="button" class="btn btn-danger btn-sm" id="btn-delete-hl-note" style="display:none">Xoá ghi chú</button>
                <button type="button" class="btn btn-primary btn-sm" id="btn-save-hl-note">💾 Lưu ghi chú</button>
            </div>
        </div>
    </div>
</dialog>


<%-- ĐIỀU HƯỚNG TRANG NỔI CHO TRUYỆN TRANH (COMIC FLOAT NAV) --%>
<c:if test="${story.comic}">
    <div class="comic-page-nav" id="comicPageNav" aria-label="Điều hướng trang truyện tranh">
        <button type="button" class="comic-nav-btn" id="comicNavPrev" title="Trang trước (K)">▲</button>
        <button type="button" class="comic-page-badge" id="comicPageBadge" title="Bấm để nhảy trang">
            <span>🖼️</span>
            <span id="comicPageNum">Trang 1</span> / <span id="comicPageTotal">${chapter.pageCount > 0 ? chapter.pageCount : 1}</span>
        </button>
        <button type="button" class="comic-nav-btn" id="comicNavNext" title="Trang sau (J)">▼</button>
    </div>

    <div class="comic-jump-pop" id="comicJumpPop" hidden>
        <div class="comic-jump-head">
            <span>Chuyển nhanh đến trang</span>
            <button type="button" class="comic-jump-close" id="comicJumpClose">&times;</button>
        </div>
        <div class="comic-jump-body">
            <input type="range" id="comicJumpSlider" min="1" max="${chapter.pageCount > 0 ? chapter.pageCount : 1}" value="1" class="comic-slider">
            <div class="comic-jump-val"><span id="comicJumpVal">1</span> / ${chapter.pageCount > 0 ? chapter.pageCount : 1}</div>
            <button type="button" class="btn btn-primary btn-sm" id="comicJumpConfirm">Tới trang này</button>
        </div>
    </div>

    <%-- MODAL PHÓNG TO ẢNH TRUYỆN TRANH (COMIC LIGHTBOX & ZOOM) --%>
    <div class="comic-lightbox" id="comic-lightbox" hidden role="dialog" aria-modal="true" aria-label="Xem ảnh phóng to">
        <div class="lightbox-backdrop" id="lightbox-backdrop"></div>
        <div class="lightbox-toolbar">
            <div class="lightbox-info">
                <span class="lightbox-badge" id="lightbox-page-indicator">Trang 1 / 1</span>
                <span class="lightbox-zoom-val" id="lightbox-zoom-val">100%</span>
            </div>
            <div class="lightbox-actions">
                <button type="button" class="lb-btn" id="lb-btn-zoom-in" title="Phóng to (+)">➕</button>
                <button type="button" class="lb-btn" id="lb-btn-zoom-out" title="Thu nhỏ (-)">➖</button>
                <button type="button" class="lb-btn" id="lb-btn-zoom-reset" title="Tỷ lệ 100% (0)">↺</button>
                <button type="button" class="lb-btn" id="lb-btn-fit" title="Vừa màn hình (F)">⛶</button>
                <button type="button" class="lb-btn lb-btn-close" id="lb-btn-close" title="Đóng (Esc)">&times;</button>
            </div>
        </div>
        <button type="button" class="lightbox-arrow lb-prev" id="lb-arrow-prev" title="Trang trước (←)">‹</button>
        <div class="lightbox-canvas-wrap" id="lightbox-canvas-wrap">
            <img src="" alt="" class="lightbox-img" id="lightbox-img" draggable="false">
        </div>
        <button type="button" class="lightbox-arrow lb-next" id="lb-arrow-next" title="Trang sau (→)">›</button>
    </div>
</c:if>

<%-- THANH ĐIỀU KHIỂN TỰ ĐỘNG CUỘN TRANG (AUTO-SCROLL) --%>
<div class="reader-autoscroll-bar" id="reader-autoscroll-bar" style="display: none;">
    <span class="as-label">⚡ Tự cuộn</span>
    <button type="button" class="as-btn" id="as-toggle-btn" title="Phát / Tạm dừng">⏸</button>
    <div class="as-speeds">
        <button type="button" class="as-spd-btn is-active" data-speed="1">1x</button>
        <button type="button" class="as-spd-btn" data-speed="2">2x</button>
        <button type="button" class="as-spd-btn" data-speed="3">3x</button>
    </div>
    <button type="button" class="as-close-btn" id="as-close-btn" title="Tắt tự cuộn">&times;</button>
</div>

<%-- CANVAS HIỆU ỨNG ĂN MỪNG MỞ KHOÁ / TẶNG XU (CELEBRATION CONFETTI & COIN SHOWER) --%>
<canvas class="celebration-canvas" id="celebration-canvas" style="display: none;"></canvas>

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

<%-- Thông báo ngắn ("Đã đánh dấu đoạn 12", "Đã đưa bạn về chỗ đang đọc").
     role=status + aria-live: trình đọc màn hình đọc lên mà không cướp focus. --%>
<div class="reader-toast" id="reader-toast" role="status" aria-live="polite" hidden></div>

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
    var KEYS = ['size', 'leading', 'theme', 'font', 'cwidth'];   // cwidth: bề rộng trang truyện tranh

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
        /* Thẻ thay thế PHẢI mang theo data-para — số thứ tự đoạn mà đánh dấu
           đoạn và "đọc tiếp đúng đoạn" dựa vào. Mất nó là đếm lệch từ đó trở đi. */
        var paraNo = p.getAttribute('data-para');

        // 1. Phân đoạn hoa thị: * * *, ***, ---, ❖ ❖ ❖
        if (/^(\*\s*\*\s*\*|—{3,}|-{3,}|❖\s*❖\s*❖)$/.test(txt)) {
            var div = document.createElement('div');
            div.className = 'divider';
            div.setAttribute('aria-hidden', 'true');
            if (paraNo) div.setAttribute('data-para', paraNo);
            div.textContent = '❖ ❖ ❖';
            p.parentNode.replaceChild(div, p);
            continue;
        }

        // 2. Trích dẫn cổ phong / câu danh ngôn (bắt đầu bằng > hoặc &gt;)
        if (/^(&gt;|>)\s*/.test(txt)) {
            var clean = txt.replace(/^(&gt;|>)\s*/, '');
            var bq = document.createElement('blockquote');
            if (paraNo) bq.setAttribute('data-para', paraNo);
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

    /* ------------------------------------------------- đánh dấu chương đã đọc */
    /*
     * Chỉ ghi danh sách chương đã đọc (để trang chi tiết làm mờ các hàng).
     *
     * TỪNG ghi thêm "truyện vừa đọc" vào localStorage cho một khung "Tiếp tục
     * đọc" ở trang chủ — đã bỏ: trùng với dải "Đọc tiếp" lấy từ CSDL, và
     * localStorage không gắn với tài khoản nên máy dùng chung thì người đăng
     * nhập sau thấy truyện người trước đang đọc.
     */
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
        } catch (e) { /* trình duyệt chặn lưu trữ — việc đọc không ảnh hưởng */ }
    }

    /* ------------------------------------------ hết truyện + khối gợi ý */
    /*
     * Gợi ý nạp MỘT lần, bằng cùng mảnh _recommend.jsp với trang chi tiết.
     * Chạy cả khi TẮT đọc liên tục: đọc tới cuối chương cuối là hết truyện,
     * bất kể chương đó tới bằng cách nào.
     */
    var recsLoaded = false;
    function showDone() {
        if (done) {
            done.hidden = false;
            done.style.display = 'block';
        }
        hideLoading();

        var box = document.getElementById('reader-recs');
        if (!box || recsLoaded) return;
        recsLoaded = true;
        fetch(CTX + '/story?action=recommend&id=' + encodeURIComponent(box.getAttribute('data-story-id')),
              { credentials: 'same-origin' })
            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.text(); })
            .then(function (html) { box.innerHTML = html; })
            .catch(function () { recsLoaded = false; });   // lần sau tới cuối thì thử lại
    }

    /* ---------------------------------------------- nạp chương kế tiếp */
    function loadNext() {
        var last = wrap.lastElementChild;
        var nextId = last && last.getAttribute('data-next-id');
        if (!nextId) {                       // hết truyện
            showDone();
            return;
        }

        if (busy || !on) return;

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
   NHỚ ĐÚNG ĐOẠN ĐANG ĐỌC · ĐÁNH DẤU ĐOẠN · LƯU TRUYỆN
   ===========================================================================
   Mỗi đoạn văn mang data-para (số thứ tự, đếm từ 1 — xem _block.jsp).

   VỊ TRÍ ĐỌC
     "Đoạn đang đọc" = đoạn đầu tiên còn thấy được ngay dưới thanh công cụ.
     Gửi lên /bookmark?action=progress khi người đọc DỪNG CUỘN 1.5 giây, và
     khi rời trang / chuyển tab. Không gửi theo từng sự kiện scroll — cuộn
     một chương là hàng nghìn sự kiện, mà chỉ chỗ dừng lại mới đáng nhớ.

     navigator.sendBeacon: trình duyệt bảo đảm gửi xong kể cả khi trang đang
     đóng — fetch() thường bị huỷ giữa chừng đúng lúc cần nhất (bấm Back).

   ĐỌC TIẾP
     Mở trang có #doan-12 (từ danh sách đánh dấu) hoặc server báo
     data-resume-para (mở lại đúng chương đang dở) thì cuộn tới đoạn đó.

   TẤT CẢ ĐỀU LÀ NÂNG CẤP: tắt JavaScript thì vẫn đọc được, server vẫn nhớ
   CHƯƠNG (ChapterServlet ghi lúc mở trang), chỉ mất phần "đúng đoạn".
   ======================================================================== */
(function () {
    var wrap = document.getElementById('chapters');
    if (!wrap || !document.querySelector) return;

    var CSRF = document.body.getAttribute('data-csrf') || '';
    var UNIT = document.body.getAttribute('data-unit') || 'đoạn';   // "trang" với truyện tranh
    var COMIC = document.body.getAttribute('data-kind') === 'comic';
    var bar = document.querySelector('.reader-bar');
    var toastEl = document.getElementById('reader-toast');

    /* Mép dưới của thanh công cụ dính trên cùng. ĐO mỗi lần chứ không đặt
       cứng: màn hẹp thì thanh gãy thành 2–3 hàng, cuộn xuống thì thanh tự
       ẩn (mép dưới <= 0). Đặt cứng 72px là "đoạn đang đọc" có khi lại là
       đoạn đang nằm khuất sau thanh công cụ. */
    function topGap() {
        var b = bar ? bar.getBoundingClientRect().bottom : 0;
        return Math.max(0, b);
    }
    var toastTimer = null;

    function toast(msg, actionLabel, onAction) {
        if (!toastEl) return;
        toastEl.textContent = msg;
        if (actionLabel) {
            var b = document.createElement('button');
            b.type = 'button';
            b.className = 'reader-toast-btn';
            b.textContent = actionLabel;
            b.addEventListener('click', function () { hideToast(); onAction(); });
            toastEl.appendChild(b);
        }
        toastEl.hidden = false;
        toastEl.classList.add('is-show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(hideToast, actionLabel ? 6000 : 2600);
    }
    function hideToast() {
        if (!toastEl) return;
        toastEl.classList.remove('is-show');
        toastEl.hidden = true;
    }

    function form(data) {
        var body = new URLSearchParams();
        for (var k in data) body.append(k, data[k]);
        body.append('_csrf', CSRF);
        return body;
    }
    function postJson(data) {
        return fetch(CTX + '/bookmark', {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: form(data)
        }).then(function (r) { return r.json(); });
    }

    function parasOf(block) {
        return block.querySelectorAll('.chapter-content [data-para]');
    }

    /* Chương đang chiếm màn hình: chương CUỐI CÙNG có đỉnh đã lên quá 1/3
       màn hình. Đọc liên tục có thể có nhiều chương trên cùng một trang. */
    function currentBlock() {
        var blocks = wrap.querySelectorAll('.chapter-block');
        var line = window.innerHeight / 3;
        var cur = blocks[0] || null;
        for (var i = 0; i < blocks.length; i++) {
            if (blocks[i].getBoundingClientRect().top <= line) cur = blocks[i];
            else break;
        }
        return cur;
    }

    /* Đoạn đầu tiên còn thấy được — tìm nhị phân: chương dài vài trăm đoạn,
       gọi getBoundingClientRect cho từng đoạn là ép trình duyệt tính bố cục
       vài trăm lần. Nhị phân chỉ cần chừng 8–9 lần. */
    function currentPara(block) {
        var ps = parasOf(block);
        /* Truyện chữ: +24px ≈ một dòng — đoạn chỉ còn ló nửa dòng cuối dưới
           thanh công cụ thì coi như đã đọc qua, đoạn kế mới là "đang đọc".
           Truyện tranh: một trang cao cả màn hình, nên "đang xem" là trang
           đang nằm ở 1/3 màn hình — không phải mẩu đuôi 50px của trang trước
           còn sót trên cùng. */
        var gap = topGap();
        var line = COMIC ? gap + (window.innerHeight - gap) * 0.35 : gap + 24;
        var lo = 0, hi = ps.length - 1, found = -1;
        while (lo <= hi) {
            var mid = (lo + hi) >> 1;
            if (ps[mid].getBoundingClientRect().bottom > line) { found = mid; hi = mid - 1; }
            else lo = mid + 1;
        }
        return found >= 0 ? ps[found] : null;
    }

    function position() {
        var block = currentBlock();
        if (!block) return null;
        var el = currentPara(block);
        return {
            block: block,
            chapterId: block.getAttribute('data-chapter-id'),
            el: el,
            paraNo: el ? parseInt(el.getAttribute('data-para'), 10) : 0
        };
    }

    function scrollToPara(block, n) {
        var el = block.querySelector('.chapter-content [data-para="' + n + '"]');
        if (!el) return false;
        /* Lấy chiều cao thanh lúc ĐANG HIỆN (offsetHeight không đổi khi thanh
           trượt lên ẩn) — sau cú cuộn này thanh vẫn có thể hiện lại. */
        var gap = bar ? bar.offsetHeight : 0;
        var y = el.getBoundingClientRect().top + window.scrollY - gap - 8;
        /* behavior 'instant' là CỐ Ý: base.css đặt scroll-behavior: smooth cho
           cả trang, nên scrollTo thường sẽ TRƯỢT qua cả chương tới đoạn 200 —
           chậm, chóng mặt, và bắn hàng trăm sự kiện scroll trên đường đi. */
        window.scrollTo({ top: Math.max(0, y), behavior: 'instant' });
        el.classList.add('is-flash');
        setTimeout(function () { el.classList.remove('is-flash'); }, 1800);
        return true;
    }

    /* ------------------------------------------------ gửi vị trí đọc */
    var first = wrap.querySelector('.chapter-block');
    var lastSent = first ? first.getAttribute('data-chapter-id') + ':'
            + (document.body.getAttribute('data-resume-para') || '0') : '';

    function report() {
        var pos = position();
        if (!pos || !pos.chapterId) return;
        var key = pos.chapterId + ':' + pos.paraNo;
        if (key === lastSent) return;          // chưa đi đâu thì khỏi gửi
        lastSent = key;

        var body = form({ action: 'progress', chapterId: pos.chapterId, paraNo: pos.paraNo });
        if (navigator.sendBeacon) {
            navigator.sendBeacon(CTX + '/bookmark', body);
        } else {
            fetch(CTX + '/bookmark', { method: 'POST', body: body,
                                       credentials: 'same-origin', keepalive: true });
        }
    }

    var idle = null;
    window.addEventListener('scroll', function () {
        clearTimeout(idle);
        idle = setTimeout(report, 1500);
    }, { passive: true });
    document.addEventListener('visibilitychange', function () {
        if (document.visibilityState === 'hidden') report();
    });
    window.addEventListener('pagehide', report);

    /* ------------------------------------------------ đọc tiếp đúng đoạn */
    if (first) {
        var m = location.hash.match(/^#doan-(\d+)$/);
        var target = m ? parseInt(m[1], 10)
                       : parseInt(document.body.getAttribute('data-resume-para') || '0', 10);
        if (m) {
            if (target >= 1) scrollToPara(first, target);
        } else if (target > 1 && scrollToPara(first, target)) {
            toast('Đã đưa bạn về ' + UNIT + ' ' + target + ', chỗ đang đọc dở.', 'Về đầu chương', function () {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        }
    }

    /* ------------------------------------------------ đánh dấu đoạn */
    function applyMarks(block) {
        var list = (block.getAttribute('data-marked') || '').split(',');
        list.forEach(function (n) {
            if (!n) return;
            var el = block.querySelector('.chapter-content [data-para="' + n + '"]');
            if (el) el.classList.add('is-marked');
        });
    }
    [].forEach.call(wrap.querySelectorAll('.chapter-block'), applyMarks);

    /* Chương nối thêm khi đọc liên tục cũng phải được tô — theo dõi #chapters
       thay vì sửa vào đoạn mã đọc liên tục: hai việc, hai chỗ. */
    if (window.MutationObserver) {
        new MutationObserver(function (records) {
            records.forEach(function (r) {
                [].forEach.call(r.addedNodes, function (n) {
                    if (n.nodeType === 1 && n.classList.contains('chapter-block')) applyMarks(n);
                });
            });
        }).observe(wrap, { childList: true });
    }

    var markBtn = document.getElementById('btn-mark-para');
    var marking = false;
    function toggleMark() {
        if (marking) return;
        var pos = position();
        if (!pos || !pos.el) {
            toast('Chương này chưa có ' + UNIT + ' nào để đánh dấu.');
            return;
        }
        var wasMarked = pos.el.classList.contains('is-marked');
        marking = true;
        postJson({ action: wasMarked ? 'unmark' : 'mark',
                   chapterId: pos.chapterId, paraNo: pos.paraNo })
            .then(function (res) {
                if (res.success) pos.el.classList.toggle('is-marked', !!res.marked);
                toast(res.message || (res.success ? 'Đã lưu.' : 'Chưa lưu được.'));
            })
            .catch(function () { toast('Mất kết nối, chưa đánh dấu được.'); })
            .then(function () { marking = false; });
    }
    if (markBtn) markBtn.addEventListener('click', toggleMark);

    /* ------------------------------------------------ lưu truyện */
    var saveBtn = document.getElementById('btn-save-story');
    if (saveBtn) {
        saveBtn.addEventListener('click', function () {
            var saved = saveBtn.getAttribute('aria-pressed') === 'true';
            saveBtn.disabled = true;
            postJson({ action: saved ? 'remove' : 'add', storyId: saveBtn.getAttribute('data-story-id') })
                .then(function (res) {
                    if (!res.success) { toast(res.message || 'Chưa lưu được.'); return; }
                    var now = !!res.bookmarked;
                    saveBtn.classList.toggle('is-on', now);
                    saveBtn.setAttribute('aria-pressed', now ? 'true' : 'false');
                    saveBtn.title = now ? 'Đã lưu vào tủ sách — bấm để bỏ lưu' : 'Lưu truyện vào tủ sách';
                    toast(res.message);
                })
                .catch(function () { toast('Mất kết nối, thử lại sau nhé.'); })
                .then(function () { saveBtn.disabled = false; });
        });
    }

    /* ------------------------------------------------ điều hướng trang truyện tranh (Comic Nav) */
    if (COMIC) {
        var pageNumEl = document.getElementById('comicPageNum');
        var pageTotalEl = document.getElementById('comicPageTotal');
        var navPrev = document.getElementById('comicNavPrev');
        var navNext = document.getElementById('comicNavNext');
        var badgeBtn = document.getElementById('comicPageBadge');
        var jumpPop = document.getElementById('comicJumpPop');
        var jumpClose = document.getElementById('comicJumpClose');
        var jumpSlider = document.getElementById('comicJumpSlider');
        var jumpVal = document.getElementById('comicJumpVal');
        var jumpConfirm = document.getElementById('comicJumpConfirm');

        function updateComicNav() {
            var pos = position();
            if (!pos || !pos.block) return;
            var curPage = pos.paraNo || 1;
            var pages = pos.block.querySelectorAll('.comic-page');
            var tot = pages.length;
            if (pageNumEl) pageNumEl.textContent = 'Trang ' + curPage;
            if (pageTotalEl && tot > 0) pageTotalEl.textContent = tot;
            if (jumpSlider && tot > 0) {
                jumpSlider.max = tot;
                if (!jumpPop || jumpPop.hidden) {
                    jumpSlider.value = curPage;
                    if (jumpVal) jumpVal.textContent = curPage;
                }
            }
        }

        window.addEventListener('scroll', updateComicNav, { passive: true });
        updateComicNav();

        if (badgeBtn && jumpPop) {
            badgeBtn.addEventListener('click', function () {
                jumpPop.hidden = !jumpPop.hidden;
            });
        }
        if (jumpClose && jumpPop) {
            jumpClose.addEventListener('click', function () {
                jumpPop.hidden = true;
            });
        }
        if (jumpSlider && jumpVal) {
            jumpSlider.addEventListener('input', function () {
                jumpVal.textContent = jumpSlider.value;
            });
        }
        if (jumpConfirm) {
            jumpConfirm.addEventListener('click', function () {
                var targetPage = parseInt(jumpSlider.value, 10);
                var curB = currentBlock();
                if (curB && targetPage > 0) {
                    scrollToPara(curB, targetPage);
                }
                if (jumpPop) jumpPop.hidden = true;
            });
        }

        function scrollComicStep(direction) {
            var pos = position();
            if (!pos || !pos.block) return;
            var curPage = pos.paraNo || 1;
            var pages = pos.block.querySelectorAll('.comic-page');
            var target = curPage + direction;
            if (target >= 1 && target <= pages.length) {
                scrollToPara(pos.block, target);
            }
        }

        if (navPrev) navPrev.addEventListener('click', function () { scrollComicStep(-1); });
        if (navNext) navNext.addEventListener('click', function () { scrollComicStep(1); });

        window.scrollComicStep = scrollComicStep;

        /* -------------------------------------------- LIGHTBOX PHÓNG TO ẢNH TRUYỆN TRANH */
        (function setupComicLightbox() {
            var lightbox = document.getElementById('comic-lightbox');
            if (!lightbox) return;

            var lbImg = document.getElementById('lightbox-img');
            var lbBackdrop = document.getElementById('lightbox-backdrop');
            var lbClose = document.getElementById('lb-btn-close');
            var lbZoomIn = document.getElementById('lb-btn-zoom-in');
            var lbZoomOut = document.getElementById('lb-btn-zoom-out');
            var lbZoomReset = document.getElementById('lb-btn-zoom-reset');
            var lbFit = document.getElementById('lb-btn-fit');
            var lbPrev = document.getElementById('lb-arrow-prev');
            var lbNext = document.getElementById('lb-arrow-next');
            var lbIndicator = document.getElementById('lightbox-page-indicator');
            var lbZoomVal = document.getElementById('lightbox-zoom-val');
            var lbCanvasWrap = document.getElementById('lightbox-canvas-wrap');

            var currentZoom = 1.0;
            var panX = 0, panY = 0;
            var isDragging = false;
            var startX = 0, startY = 0;
            var currentImgIdx = 0;
            var allImgs = [];

            function updateTransform() {
                if (!lbImg) return;
                lbImg.style.transform = 'translate(' + panX + 'px, ' + panY + 'px) scale(' + currentZoom + ')';
                if (lbZoomVal) lbZoomVal.textContent = Math.round(currentZoom * 100) + '%';
            }

            function refreshComicImages() {
                allImgs = Array.prototype.slice.call(document.querySelectorAll('.comic-page img'));
            }

            function showImageAtIndex(idx) {
                if (!allImgs.length) refreshComicImages();
                if (idx < 0) idx = 0;
                if (idx >= allImgs.length) idx = allImgs.length - 1;
                currentImgIdx = idx;
                var targetImg = allImgs[idx];
                if (!targetImg) return;

                lbImg.src = targetImg.src;
                lbImg.alt = targetImg.alt || ('Trang ' + (idx + 1));
                currentZoom = 1.0;
                panX = 0;
                panY = 0;
                updateTransform();

                if (lbIndicator) {
                    lbIndicator.textContent = 'Trang ' + (idx + 1) + ' / ' + allImgs.length;
                }
            }

            function openLightbox(imgEl) {
                refreshComicImages();
                currentImgIdx = allImgs.indexOf(imgEl);
                if (currentImgIdx === -1) currentImgIdx = 0;
                showImageAtIndex(currentImgIdx);
                lightbox.hidden = false;
                document.body.style.overflow = 'hidden';
            }

            function closeLightbox() {
                lightbox.hidden = true;
                document.body.style.overflow = '';
                currentZoom = 1.0;
                panX = 0;
                panY = 0;
                updateTransform();
            }

            function setZoom(newZoom) {
                currentZoom = Math.min(3.5, Math.max(0.5, newZoom));
                if (currentZoom === 1.0) {
                    panX = 0;
                    panY = 0;
                }
                updateTransform();
            }

            // Click vào bất kỳ trang truyện tranh nào trên trang (bao gồm cả khi cuộn nối chương)
            document.addEventListener('click', function (e) {
                var comicImg = e.target.closest('.comic-page img');
                if (comicImg) {
                    e.preventDefault();
                    openLightbox(comicImg);
                }
            });

            if (lbClose) lbClose.addEventListener('click', closeLightbox);
            if (lbBackdrop) lbBackdrop.addEventListener('click', closeLightbox);

            if (lbZoomIn) lbZoomIn.addEventListener('click', function () { setZoom(currentZoom + 0.25); });
            if (lbZoomOut) lbZoomOut.addEventListener('click', function () { setZoom(currentZoom - 0.25); });
            if (lbZoomReset) lbZoomReset.addEventListener('click', function () { setZoom(1.0); });
            if (lbFit) lbFit.addEventListener('click', function () { setZoom(1.0); });

            if (lbPrev) lbPrev.addEventListener('click', function (e) {
                e.stopPropagation();
                if (currentImgIdx > 0) showImageAtIndex(currentImgIdx - 1);
            });
            if (lbNext) lbNext.addEventListener('click', function (e) {
                e.stopPropagation();
                if (currentImgIdx < allImgs.length - 1) showImageAtIndex(currentImgIdx + 1);
            });

            // Double click: zoom 2x / reset 1x
            if (lbImg) {
                lbImg.addEventListener('dblclick', function (e) {
                    e.preventDefault();
                    e.stopPropagation();
                    setZoom(currentZoom > 1.2 ? 1.0 : 2.0);
                });
            }

            // Cuộn con lăn chuột để zoom mượt mà
            if (lbCanvasWrap) {
                lbCanvasWrap.addEventListener('wheel', function (e) {
                    if (lightbox.hidden) return;
                    e.preventDefault();
                    var delta = e.deltaY < 0 ? 0.15 : -0.15;
                    setZoom(currentZoom + delta);
                }, { passive: false });

                // Kéo rê / Pan ảnh khi đang phóng to
                lbCanvasWrap.addEventListener('mousedown', function (e) {
                    if (lightbox.hidden || e.target.closest('.lightbox-toolbar, .lightbox-arrow')) return;
                    isDragging = true;
                    startX = e.clientX - panX;
                    startY = e.clientY - panY;
                    lbCanvasWrap.classList.add('is-dragging');
                });

                window.addEventListener('mousemove', function (e) {
                    if (!isDragging) return;
                    panX = e.clientX - startX;
                    panY = e.clientY - startY;
                    updateTransform();
                });

                window.addEventListener('mouseup', function () {
                    if (isDragging) {
                        isDragging = false;
                        lbCanvasWrap.classList.remove('is-dragging');
                    }
                });
            }

            // Phím tắt bàn phím
            document.addEventListener('keydown', function (e) {
                if (lightbox.hidden) return;
                if (e.key === 'Escape') {
                    closeLightbox();
                } else if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') {
                    if (currentImgIdx > 0) showImageAtIndex(currentImgIdx - 1);
                } else if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') {
                    if (currentImgIdx < allImgs.length - 1) showImageAtIndex(currentImgIdx + 1);
                } else if (e.key === '+' || e.key === '=') {
                    setZoom(currentZoom + 0.25);
                } else if (e.key === '-' || e.key === '_') {
                    setZoom(currentZoom - 0.25);
                } else if (e.key === '0') {
                    setZoom(1.0);
                }
            });
        })();
    }
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
   - Phím B: Đánh dấu / bỏ đánh dấu đoạn đang đọc
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
            if (document.body.getAttribute('data-kind') === 'comic') return;   // truyện tranh không có chữ để đọc
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

        // Phím B -> Đánh dấu / bỏ đánh dấu đoạn đang đọc
        if (code === 'KeyB' && !e.ctrlKey && !e.metaKey) {
            e.preventDefault();
            var markToggle = document.getElementById('btn-mark-para');
            if (markToggle) markToggle.click();
            return;
        }

        // Phím C -> Bật / tắt Tự động cuộn trang
        if (code === 'KeyC' && !e.ctrlKey && !e.metaKey) {
            e.preventDefault();
            var asToggle = document.getElementById('toggle-autoscroll');
            if (asToggle) asToggle.click();
            return;
        }

        // Phím J / K cuộn trang trong truyện tranh
        if (window.scrollComicStep && (code === 'KeyJ' || code === 'KeyK')) {
            e.preventDefault();
            window.scrollComicStep(code === 'KeyJ' ? 1 : -1);
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
     * TÍNH NĂNG 2: HỆ THỐNG ÂM THANH MÔI TRƯỜNG ĐA DẠNG (AMBIENT SOUNDSCAPES)
     *
     * 4 loại âm thanh môi trường thư giãn tổng hợp 100% bằng Web Audio API:
     * 1. rain: Mưa rào ngoài hiên (Pink noise + Lowpass filter)
     * 2. fire: Lò sưởi ấm cúng (Pink noise + crackle pops ngẫu nhiên)
     * 3. waves: Sóng biển vỗ bờ (Pink noise + LFO điều chế tần số cắt 0.1Hz)
     * 4. night: Đêm hè tĩnh lặng (Low ambient + sine crickets nhẹ nhàng)
     * ===================================================================== */
    var toggleAmbientBtn = document.getElementById('toggle-ambient');
    var ambientPop = document.getElementById('reader-ambient-pop');
    var ambientPopClose = document.getElementById('ambient-pop-close');
    var ambientTypeBtns = document.querySelectorAll('.ambient-type-btn');
    var btnAmbientPlay = document.getElementById('btn-ambient-toggle-play');
    var ambPlayIcon = document.getElementById('amb-play-icon');
    var ambPlayText = document.getElementById('amb-play-text');
    var ambCurrentIcon = document.getElementById('ambient-current-icon');
    var ambientVolSlider = document.getElementById('ambient-volume-slider');
    var ambientVolVal = document.getElementById('ambient-vol-val');

    var audioCtx = null;
    var masterGain = null;
    var currentSoundType = localStorage.getItem('reader.ambient_sound') || 'rain';
    var ambientVolume = parseFloat(localStorage.getItem('reader.ambient_volume') || '0.35');
    var isAmbientPlaying = false;
    var activeSoundNodes = [];

    var AMBIENT_ICONS = {
        rain: '🌧️',
        fire: '🔥',
        waves: '🌊',
        night: '🦗'
    };

    function getAudioContext() {
        if (!audioCtx) {
            var AudioContext = window.AudioContext || window.webkitAudioContext;
            if (AudioContext) {
                audioCtx = new AudioContext();
                masterGain = audioCtx.createGain();
                masterGain.gain.setValueAtTime(ambientVolume, audioCtx.currentTime);
                masterGain.connect(audioCtx.destination);
            }
        }
        return audioCtx;
    }

    function createPinkNoiseBuffer(ctx, seconds) {
        var bufferSize = ctx.sampleRate * (seconds || 4);
        var buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
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
        return buffer;
    }

    function stopActiveAmbientNodes() {
        activeSoundNodes.forEach(function (n) {
            try {
                if (n.stop) n.stop();
                if (n.disconnect) n.disconnect();
            } catch (e) {}
        });
        activeSoundNodes = [];
    }

    function playSoundType(type) {
        var ctx = getAudioContext();
        if (!ctx) return;
        if (ctx.state === 'suspended') ctx.resume();

        stopActiveAmbientNodes();

        if (type === 'rain') {
            var pBuffer = createPinkNoiseBuffer(ctx, 4);
            var src = ctx.createBufferSource();
            src.buffer = pBuffer;
            src.loop = true;
            var filter = ctx.createBiquadFilter();
            filter.type = 'lowpass';
            filter.frequency.setValueAtTime(820, ctx.currentTime);
            src.connect(filter);
            filter.connect(masterGain);
            src.start(0);
            activeSoundNodes.push(src, filter);
        } else if (type === 'fire') {
            var pBuffer2 = createPinkNoiseBuffer(ctx, 3);
            var srcRumble = ctx.createBufferSource();
            srcRumble.buffer = pBuffer2;
            srcRumble.loop = true;
            var fLow = ctx.createBiquadFilter();
            fLow.type = 'lowpass';
            fLow.frequency.setValueAtTime(450, ctx.currentTime);
            srcRumble.connect(fLow);
            fLow.connect(masterGain);
            srcRumble.start(0);
            activeSoundNodes.push(srcRumble, fLow);

            var crackleTimer = setInterval(function () {
                if (!isAmbientPlaying || currentSoundType !== 'fire') {
                    clearInterval(crackleTimer);
                    return;
                }
                if (Math.random() > 0.45) {
                    try {
                        var cOsc = ctx.createOscillator();
                        var cGain = ctx.createGain();
                        cOsc.type = 'triangle';
                        cOsc.frequency.setValueAtTime(120 + Math.random() * 800, ctx.currentTime);
                        cGain.gain.setValueAtTime(0.08 * (Math.random() * 0.8 + 0.2), ctx.currentTime);
                        cGain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.04);
                        cOsc.connect(cGain);
                        cGain.connect(masterGain);
                        cOsc.start();
                        cOsc.stop(ctx.currentTime + 0.05);
                    } catch (e) {}
                }
            }, 80);
        } else if (type === 'waves') {
            var pBuffer3 = createPinkNoiseBuffer(ctx, 5);
            var srcWaves = ctx.createBufferSource();
            srcWaves.buffer = pBuffer3;
            srcWaves.loop = true;
            var fWaves = ctx.createBiquadFilter();
            fWaves.type = 'lowpass';
            fWaves.frequency.setValueAtTime(350, ctx.currentTime);

            var lfo = ctx.createOscillator();
            lfo.frequency.setValueAtTime(0.1, ctx.currentTime);
            var lfoGain = ctx.createGain();
            lfoGain.gain.setValueAtTime(420, ctx.currentTime);
            lfo.connect(lfoGain);
            lfoGain.connect(fWaves.frequency);

            srcWaves.connect(fWaves);
            fWaves.connect(masterGain);
            srcWaves.start(0);
            lfo.start(0);
            activeSoundNodes.push(srcWaves, fWaves, lfo, lfoGain);
        } else if (type === 'night') {
            var pBuffer4 = createPinkNoiseBuffer(ctx, 4);
            var srcBreeze = ctx.createBufferSource();
            srcBreeze.buffer = pBuffer4;
            srcBreeze.loop = true;
            var fBreeze = ctx.createBiquadFilter();
            fBreeze.type = 'lowpass';
            fBreeze.frequency.setValueAtTime(280, ctx.currentTime);
            srcBreeze.connect(fBreeze);
            fBreeze.connect(masterGain);
            srcBreeze.start(0);
            activeSoundNodes.push(srcBreeze, fBreeze);

            var cricketOsc = ctx.createOscillator();
            cricketOsc.type = 'sine';
            cricketOsc.frequency.setValueAtTime(4600, ctx.currentTime);
            var cricketLfo = ctx.createOscillator();
            cricketLfo.frequency.setValueAtTime(12, ctx.currentTime);
            var cLfoGain = ctx.createGain();
            cLfoGain.gain.setValueAtTime(0.015, ctx.currentTime);
            var cGainNode = ctx.createGain();
            cGainNode.gain.setValueAtTime(0.015, ctx.currentTime);

            cricketLfo.connect(cLfoGain);
            cricketOsc.connect(cGainNode);
            cGainNode.connect(masterGain);
            cricketOsc.start(0);
            cricketLfo.start(0);
            activeSoundNodes.push(cricketOsc, cricketLfo, cLfoGain, cGainNode);
        }
    }

    function setAmbientPlayingState(playing) {
        isAmbientPlaying = playing;
        if (playing) {
            playSoundType(currentSoundType);
            if (masterGain && audioCtx) {
                masterGain.gain.setTargetAtTime(ambientVolume, audioCtx.currentTime, 0.1);
            }
            if (btnAmbientPlay) {
                btnAmbientPlay.classList.add('is-playing');
                ambPlayIcon.textContent = '⏸';
                ambPlayText.textContent = 'Tạm dừng âm thanh';
            }
            if (toggleAmbientBtn) toggleAmbientBtn.classList.add('is-on');
        } else {
            if (masterGain && audioCtx) {
                masterGain.gain.setTargetAtTime(0.0001, audioCtx.currentTime, 0.1);
            }
            setTimeout(stopActiveAmbientNodes, 200);
            if (btnAmbientPlay) {
                btnAmbientPlay.classList.remove('is-playing');
                ambPlayIcon.textContent = '▶';
                ambPlayText.textContent = 'Bật âm thanh';
            }
            if (toggleAmbientBtn) toggleAmbientBtn.classList.remove('is-on');
        }
    }

    if (toggleAmbientBtn && ambientPop) {
        toggleAmbientBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            var isHidden = ambientPop.hidden;
            ambientPop.hidden = !isHidden;
            toggleAmbientBtn.setAttribute('aria-expanded', isHidden ? 'true' : 'false');
        });
    }
    if (ambientPopClose) {
        ambientPopClose.addEventListener('click', function () {
            ambientPop.hidden = true;
            if (toggleAmbientBtn) toggleAmbientBtn.setAttribute('aria-expanded', 'false');
        });
    }

    ambientTypeBtns.forEach(function (btn) {
        var sType = btn.getAttribute('data-sound');
        if (sType === currentSoundType) btn.classList.add('is-active');
        btn.addEventListener('click', function () {
            ambientTypeBtns.forEach(function (b) { b.classList.remove('is-active'); });
            btn.classList.add('is-active');
            currentSoundType = sType;
            localStorage.setItem('reader.ambient_sound', currentSoundType);
            if (ambCurrentIcon) ambCurrentIcon.textContent = AMBIENT_ICONS[currentSoundType] || '🎵';
            if (isAmbientPlaying) {
                playSoundType(currentSoundType);
            }
        });
    });

    if (btnAmbientPlay) {
        btnAmbientPlay.addEventListener('click', function () {
            setAmbientPlayingState(!isAmbientPlaying);
        });
    }

    if (ambientVolSlider) {
        ambientVolSlider.value = Math.round(ambientVolume * 100);
        if (ambientVolVal) ambientVolVal.textContent = ambientVolSlider.value + '%';
        ambientVolSlider.addEventListener('input', function () {
            var val = parseInt(this.value, 10) / 100;
            ambientVolume = val;
            localStorage.setItem('reader.ambient_volume', val.toString());
            if (ambientVolVal) ambientVolVal.textContent = this.value + '%';
            if (masterGain && audioCtx) {
                masterGain.gain.setValueAtTime(val, audioCtx.currentTime);
            }
        });
    }

    /* ========================================================================
     * TÍNH NĂNG: BỘ HẸN GIỜ TẮT ĐỌC TRUYỆN (SLEEP TIMER & SESSION CLOCK)
     * ===================================================================== */
    var btnToggleSleepTimer = document.getElementById('toggle-sleep-timer');
    var sleepTimerModal = document.getElementById('sleep-timer-modal');
    var closeSleepTimer = document.getElementById('close-sleep-timer');
    var sleepTimerBadge = document.getElementById('sleep-timer-badge');
    var sleepStatusBox = document.getElementById('sleep-status-box');
    var sleepCountdownVal = document.getElementById('sleep-countdown-val');
    var btnCancelSleep = document.getElementById('btn-cancel-sleep-timer');
    var sleepPresets = document.querySelectorAll('.sleep-btn');
    var sleepCurtain = document.getElementById('screen-sleep-curtain');
    var btnWakeUp = document.getElementById('btn-wake-up');

    var sleepTimerInterval = null;
    var sleepTargetTimestamp = 0;
    var isSleepOnChapterEnd = false;

    function openSleepTimerModal() {
        if (sleepTimerModal) {
            if (typeof sleepTimerModal.showModal === 'function') sleepTimerModal.showModal();
            else sleepTimerModal.setAttribute('open', '');
        }
    }

    function closeSleepTimerModal() {
        if (sleepTimerModal) {
            if (typeof sleepTimerModal.close === 'function') sleepTimerModal.close();
            else sleepTimerModal.removeAttribute('open');
        }
    }

    if (btnToggleSleepTimer) {
        btnToggleSleepTimer.addEventListener('click', function () {
            openSleepTimerModal();
        });
    }
    if (closeSleepTimer) {
        closeSleepTimer.addEventListener('click', closeSleepTimerModal);
    }

    function triggerSleepExpire() {
        cancelSleepTimer();
        closeSleepTimerModal();

        // Tắt Ambient
        setAmbientPlayingState(false);

        // Tắt TTS
        if (window.speechSynthesis) {
            try { window.speechSynthesis.cancel(); } catch (e) {}
        }
        if (typeof isTtsPlaying !== 'undefined' && isTtsPlaying) {
            if (typeof stopTts === 'function') stopTts();
        }

        // Tắt Auto-scroll
        if (typeof isAutoScrolling !== 'undefined' && isAutoScrolling) {
            if (typeof stopAutoScroll === 'function') stopAutoScroll();
        }

        // Hiện rèm che màn đêm chúc ngủ ngon
        if (sleepCurtain) {
            sleepCurtain.hidden = false;
        }
    }

    function cancelSleepTimer() {
        if (sleepTimerInterval) {
            clearInterval(sleepTimerInterval);
            sleepTimerInterval = null;
        }
        sleepTargetTimestamp = 0;
        isSleepOnChapterEnd = false;
        if (sleepTimerBadge) sleepTimerBadge.style.display = 'none';
        if (sleepStatusBox) sleepStatusBox.style.display = 'none';
        if (btnToggleSleepTimer) btnToggleSleepTimer.classList.remove('is-on');
    }

    function startSleepTimer(minutes) {
        cancelSleepTimer();

        if (minutes === 'chapter') {
            isSleepOnChapterEnd = true;
            if (sleepTimerBadge) {
                sleepTimerBadge.textContent = '📖';
                sleepTimerBadge.style.display = 'inline-block';
            }
            if (sleepStatusBox) {
                sleepStatusBox.style.display = 'block';
                sleepCountdownVal.textContent = 'Hết chương';
            }
            if (btnToggleSleepTimer) btnToggleSleepTimer.classList.add('is-on');
            closeSleepTimerModal();
            if (typeof showToast === 'function') showToast('Đã hẹn giờ: Tự động tắt khi đọc xong chương này.');
            return;
        }

        var totalSeconds = parseInt(minutes, 10) * 60;
        sleepTargetTimestamp = Date.now() + (totalSeconds * 1000);
        if (btnToggleSleepTimer) btnToggleSleepTimer.classList.add('is-on');

        function updateCountdown() {
            var remainingSec = Math.max(0, Math.round((sleepTargetTimestamp - Date.now()) / 1000));
            if (remainingSec <= 0) {
                triggerSleepExpire();
                return;
            }
            var mins = Math.floor(remainingSec / 60);
            var secs = remainingSec % 60;
            var timeStr = (mins < 10 ? '0' : '') + mins + ':' + (secs < 10 ? '0' : '') + secs;

            if (sleepCountdownVal) sleepCountdownVal.textContent = timeStr;
            if (sleepTimerBadge) {
                sleepTimerBadge.textContent = (mins + 1) + 'm';
                sleepTimerBadge.style.display = 'inline-block';
            }
        }

        if (sleepStatusBox) sleepStatusBox.style.display = 'block';
        updateCountdown();
        sleepTimerInterval = setInterval(updateCountdown, 1000);
        closeSleepTimerModal();
        if (typeof showToast === 'function') showToast('Đã hẹn giờ tắt đọc truyện sau ' + minutes + ' phút.');
    }

    sleepPresets.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var mins = this.getAttribute('data-mins');
            startSleepTimer(mins);
        });
    });

    if (btnCancelSleep) {
        btnCancelSleep.addEventListener('click', function () {
            cancelSleepTimer();
            if (typeof showToast === 'function') showToast('Đã hủy hẹn giờ tắt đọc truyện.');
        });
    }

    if (btnWakeUp && sleepCurtain) {
        btnWakeUp.addEventListener('click', function () {
            sleepCurtain.hidden = true;
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

<script>
/* ===========================================================================
   TỰ ĐỘNG CUỘN TRANG (SMOOTH AUTO-SCROLL)
   ===========================================================================
   - Tự động cuộn trang mượt mà bằng requestAnimationFrame
   - Tương thích cả truyện tranh Webtoon và truyện chữ
   - 3 mức tốc độ (1x ~ 60px/s, 2x ~ 130px/s, 3x ~ 240px/s)
   - Tự tạm dừng khi người dùng tự lăn chuột hoặc chạm vuốt màn hình
   - Dừng mượt mà khi chạm đáy trang
   - Phím tắt C để bật / tắt tức thì
   ======================================================================== */
(function () {
    var toggleBtn = document.getElementById('toggle-autoscroll');
    var bar = document.getElementById('reader-autoscroll-bar');
    var playPauseBtn = document.getElementById('as-toggle-btn');
    var closeBtn = document.getElementById('as-close-btn');
    var speedBtns = document.querySelectorAll('.as-spd-btn');

    var isRunning = false;
    var isPaused = false;
    var userInteracting = false;
    var userResumeTimer = null;
    var rafId = null;

    var speedMultipliers = { '1': 1.0, '2': 2.2, '3': 4.0 };
    var currentSpeed = localStorage.getItem('reader_as_speed') || '1';

    function setSpeed(spd) {
        currentSpeed = spd;
        localStorage.setItem('reader_as_speed', spd);
        speedBtns.forEach(function (b) {
            b.classList.toggle('is-active', b.getAttribute('data-speed') === spd);
        });
    }
    setSpeed(currentSpeed);

    speedBtns.forEach(function (b) {
        b.addEventListener('click', function () {
            setSpeed(b.getAttribute('data-speed'));
        });
    });

    function scrollStep() {
        if (!isRunning) return;
        if (!isPaused && !userInteracting) {
            var px = speedMultipliers[currentSpeed] || 1.0;
            window.scrollBy({ top: px, behavior: 'instant' });

            // Kiểm tra xem đã chạm đáy trang chưa
            var scrollPos = window.innerHeight + window.scrollY;
            var maxScroll = document.documentElement.scrollHeight || document.body.scrollHeight;
            if (scrollPos >= maxScroll - 8) {
                stopAutoScroll();
                return;
            }
        }
        rafId = requestAnimationFrame(scrollStep);
    }

    function startAutoScroll() {
        isRunning = true;
        isPaused = false;
        if (bar) bar.style.display = 'flex';
        if (toggleBtn) toggleBtn.classList.add('is-on');
        if (playPauseBtn) playPauseBtn.textContent = '⏸';
        cancelAnimationFrame(rafId);
        rafId = requestAnimationFrame(scrollStep);
    }

    function stopAutoScroll() {
        isRunning = false;
        isPaused = false;
        cancelAnimationFrame(rafId);
        if (bar) bar.style.display = 'none';
        if (toggleBtn) toggleBtn.classList.remove('is-on');
    }

    function togglePlayPause() {
        if (!isRunning) return;
        isPaused = !isPaused;
        if (playPauseBtn) playPauseBtn.textContent = isPaused ? '▶' : '⏸';
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            if (isRunning) stopAutoScroll();
            else startAutoScroll();
        });
    }

    if (playPauseBtn) playPauseBtn.addEventListener('click', togglePlayPause);
    if (closeBtn) closeBtn.addEventListener('click', stopAutoScroll);

    // Tự tạm dừng khi người dùng lăn chuột hoặc touch, sau 1.2s tự động cuộn tiếp
    function onUserScrollIntervention() {
        if (!isRunning || isPaused) return;
        userInteracting = true;
        clearTimeout(userResumeTimer);
        userResumeTimer = setTimeout(function () {
            userInteracting = false;
        }, 1200);
    }
    window.addEventListener('wheel', onUserScrollIntervention, { passive: true });
    window.addEventListener('touchmove', onUserScrollIntervention, { passive: true });

    window.toggleAutoScroll = function () {
        if (isRunning) stopAutoScroll();
        else startAutoScroll();
    };
})();

/* ===========================================================================
   HIỆU ỨNG ĂN MỪNG MỞ KHOÁ VIP & TẶNG XU (CELEBRATION ENGINE)
   ===========================================================================
   - Confetti đa sắc màu & Cơn mưa đồng xu vàng (Golden Coins Shower)
   - Âm thanh khánh tiết chúc mừng & tiếng xu kêu lách cách (Web Audio Synth)
   - Kích hoạt khi mở khoá chương thành công hoặc khi tặng xu cho tác giả
   ======================================================================== */
(function () {
    var canvas = document.getElementById('celebration-canvas');
    if (!canvas) return;
    var ctx = canvas.getContext('2d');
    var animId = null;
    var particles = [];

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resize);
    resize();

    // Sinh âm thanh chúc mừng bằng Web Audio API
    function playCelebrationSound(type) {
        try {
            var AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) return;
            var ctxAudio = new AudioContext();

            if (type === 'unlock') {
                // Hợp âm chiến thắng rạng rỡ C5 - E5 - G5 - C6
                var notes = [523.25, 659.25, 783.99, 1046.50];
                notes.forEach(function (freq, idx) {
                    var osc = ctxAudio.createOscillator();
                    var gain = ctxAudio.createGain();
                    osc.type = 'triangle';
                    osc.frequency.setValueAtTime(freq, ctxAudio.currentTime + idx * 0.08);

                    gain.gain.setValueAtTime(0, ctxAudio.currentTime + idx * 0.08);
                    gain.gain.linearRampToValueAtTime(0.25, ctxAudio.currentTime + idx * 0.08 + 0.04);
                    gain.gain.exponentialRampToValueAtTime(0.001, ctxAudio.currentTime + idx * 0.08 + 0.8);

                    osc.connect(gain);
                    gain.connect(ctxAudio.destination);
                    osc.start(ctxAudio.currentTime + idx * 0.08);
                    osc.stop(ctxAudio.currentTime + idx * 0.08 + 0.85);
                });
            } else {
                // Tiếng đồng xu lách cách leng keng
                [1800, 2400, 2900, 3400].forEach(function (freq, idx) {
                    var osc = ctxAudio.createOscillator();
                    var gain = ctxAudio.createGain();
                    osc.type = 'sine';
                    osc.frequency.setValueAtTime(freq, ctxAudio.currentTime + idx * 0.09);

                    gain.gain.setValueAtTime(0, ctxAudio.currentTime + idx * 0.09);
                    gain.gain.linearRampToValueAtTime(0.2, ctxAudio.currentTime + idx * 0.09 + 0.02);
                    gain.gain.exponentialRampToValueAtTime(0.001, ctxAudio.currentTime + idx * 0.09 + 0.45);

                    osc.connect(gain);
                    gain.connect(ctxAudio.destination);
                    osc.start(ctxAudio.currentTime + idx * 0.09);
                    osc.stop(ctxAudio.currentTime + idx * 0.09 + 0.5);
                });
            }
        } catch (e) {
            // Audio policy ignore
        }
    }

    var COLORS = ['#f59e0b', '#fbbf24', '#ef4444', '#10b981', '#3b82f6', '#8b5cf6', '#ec4899', '#ffffff'];

    function createParticle(type) {
        var isCoin = (type === 'coins') || (type === 'unlock' && Math.random() < 0.4);
        return {
            isCoin: isCoin,
            x: Math.random() * canvas.width,
            y: -20 - Math.random() * 50,
            vx: (Math.random() - 0.5) * 4,
            vy: Math.random() * 3 + 2.5,
            size: isCoin ? (Math.random() * 8 + 14) : (Math.random() * 8 + 6),
            color: COLORS[Math.floor(Math.random() * COLORS.length)],
            rotation: Math.random() * 360,
            rotSpeed: (Math.random() - 0.5) * 8,
            wobble: Math.random() * Math.PI * 2,
            wobbleSpeed: Math.random() * 0.1 + 0.05,
            opacity: 1
        };
    }

    function loop() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        var alive = false;

        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            p.y += p.vy;
            p.x += p.vx + Math.sin(p.wobble) * 1.5;
            p.wobble += p.wobbleSpeed;
            p.rotation += p.rotSpeed;

            if (p.y > canvas.height - 40) {
                p.opacity -= 0.03;
            }

            if (p.opacity > 0 && p.y < canvas.height + 40) {
                alive = true;
                ctx.save();
                ctx.globalAlpha = Math.max(0, p.opacity);
                ctx.translate(p.x, p.y);
                ctx.rotate((p.rotation * Math.PI) / 180);

                if (p.isCoin) {
                    var scaleX = Math.cos(p.wobble);
                    ctx.scale(scaleX, 1);
                    ctx.beginPath();
                    ctx.arc(0, 0, p.size / 2, 0, Math.PI * 2);
                    ctx.fillStyle = '#f59e0b';
                    ctx.fill();
                    ctx.lineWidth = 2;
                    ctx.strokeStyle = '#fef08a';
                    ctx.stroke();

                    ctx.beginPath();
                    ctx.arc(0, 0, p.size / 3.5, 0, Math.PI * 2);
                    ctx.strokeStyle = '#d97706';
                    ctx.lineWidth = 1.2;
                    ctx.stroke();
                } else {
                    ctx.fillStyle = p.color;
                    ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
                }
                ctx.restore();
            }
        }

        if (alive) {
            animId = requestAnimationFrame(loop);
        } else {
            canvas.style.display = 'none';
            particles = [];
            cancelAnimationFrame(animId);
        }
    }

    window.launchCelebration = function (type) {
        type = type || 'unlock';
        resize();
        canvas.style.display = 'block';
        particles = [];
        var count = type === 'coins' ? 70 : 110;
        for (var i = 0; i < count; i++) {
            particles.push(createParticle(type));
        }
        playCelebrationSound(type);
        cancelAnimationFrame(animId);
        animId = requestAnimationFrame(loop);
    };

    <c:if test="${not empty sessionScope.flash and sessionScope.flash.contains('khoá')}">
    window.addEventListener('DOMContentLoaded', function () {
        setTimeout(function () {
            window.launchCelebration('unlock');
        }, 350);
    });
    </c:if>
})();
</script>

<%-- ========================================================================
     JAVASCRIPT: BỘ CÔNG CỤ TÔ MÀU ĐOẠN VĂN & ĐÁNH DẤU TRÍCH DẪN
     ======================================================================== --%>
<script>
(function () {

    'use strict';

    var STORY_ID = '${story.id}';
    var STORY_TITLE = '<c:out value="${story.title}"/>';
    var CURRENT_CHAPTER_ID = '${chapter.id}';
    var CURRENT_CHAPTER_NO = '${chapter.chapterNo}';
    var CTX = (document.body && document.body.dataset && document.body.dataset.ctx) || '';
    var STORAGE_KEY = 'truyen_hl_' + STORY_ID;
    var VISIBILITY_KEY = 'truyen_hl_hidden';

    var toolbar = document.getElementById('reader-hl-toolbar');
    var drawer = document.getElementById('highlights-drawer');
    var drawerBtn = document.getElementById('btn-highlights-drawer');
    var drawerCloseBtn = document.getElementById('close-highlights');
    var toggleHlBtn = document.getElementById('btn-toggle-hl-visibility');
    var counterBadge = document.getElementById('hl-counter-badge');
    var drawerList = document.getElementById('hl-drawer-list');
    var filterTabs = document.getElementById('hl-filter-tabs');
    var searchInput = document.getElementById('hl-search-input');
    var scopeChapterBtn = document.getElementById('hl-scope-chapter');
    var scopeAllBtn = document.getElementById('hl-scope-all');
    var summaryLabel = document.getElementById('hl-summary-label');
    var exportAllBtn = document.getElementById('hl-btn-export-all');
    var clearChapterBtn = document.getElementById('hl-btn-clear-chapter');

    var currentSelectionContext = null;
    var activeFilter = 'all';
    var activeScope = 'chapter'; // 'chapter' hoặc 'all'
    var searchKeyword = '';

    // ---- 1. Chế độ đọc sạch sẽ (Ẩn/Hiện màu tô để không bị rối mắt khi có quá nhiều) ----
    function initVisibilityMode() {
        var isHidden = localStorage.getItem(VISIBILITY_KEY) === '1';
        if (isHidden) {
            document.body.classList.add('hl-hidden-mode');
            if (toggleHlBtn) {
                toggleHlBtn.classList.add('is-hl-off');
                toggleHlBtn.textContent = '👁️‍🗨️';
                toggleHlBtn.title = 'Đang ẩn màu tô (Bấm để hiện lại)';
            }
        }
    }
    initVisibilityMode();

    if (toggleHlBtn) {
        toggleHlBtn.addEventListener('click', toggleHighlightVisibility);
    }

    function toggleHighlightVisibility() {
        var isCurrentlyHidden = document.body.classList.toggle('hl-hidden-mode');
        localStorage.setItem(VISIBILITY_KEY, isCurrentlyHidden ? '1' : '0');
        if (toggleHlBtn) {
            toggleHlBtn.classList.toggle('is-hl-off', isCurrentlyHidden);
            toggleHlBtn.textContent = isCurrentlyHidden ? '👁️‍🗨️' : '👁️';
            toggleHlBtn.title = isCurrentlyHidden ? 'Đang ẩn màu tô (Bấm để hiện lại)' : 'Ẩn màu tô đoạn văn (Alt+H)';
        }
        showToast(isCurrentlyHidden ? 'Đã ẩn màu tô (Chế độ đọc thuần khiết)' : 'Đã hiển thị lại các đoạn tô màu');
    }

    // ---- 2. Quản lý Dữ liệu LocalStorage ----
    function getHighlights() {
        try {
            var raw = localStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : [];
        } catch (e) {
            return [];
        }
    }

    function saveHighlights(list) {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
        } catch (e) {}
        updateCounter();
    }

    function updateCounter() {
        var list = getHighlights();
        // Đếm số lượng của chương hiện tại cho badge trên thanh đọc
        var currentChapHighlights = list.filter(function(h) {
            return String(h.chapterId) === String(CURRENT_CHAPTER_ID) || String(h.chapterNo) === String(CURRENT_CHAPTER_NO);
        });
        var count = currentChapHighlights.length;
        if (counterBadge) {
            counterBadge.textContent = count;
            counterBadge.style.display = count > 0 ? 'inline-block' : 'none';
        }

        // Cập nhật số đếm trên các tab màu
        var tabAllCount = document.getElementById('hl-tab-all-count');
        var tabYellowCount = document.getElementById('hl-tab-yellow-count');
        var tabGreenCount = document.getElementById('hl-tab-green-count');
        var tabBlueCount = document.getElementById('hl-tab-blue-count');
        var tabPurpleCount = document.getElementById('hl-tab-purple-count');
        var tabPinkCount = document.getElementById('hl-tab-pink-count');

        var scopedList = activeScope === 'chapter' ? currentChapHighlights : list;

        if (tabAllCount) tabAllCount.textContent = scopedList.length;
        if (tabYellowCount) tabYellowCount.textContent = scopedList.filter(function(h){return h.color==='yellow';}).length;
        if (tabGreenCount) tabGreenCount.textContent = scopedList.filter(function(h){return h.color==='green';}).length;
        if (tabBlueCount) tabBlueCount.textContent = scopedList.filter(function(h){return h.color==='blue';}).length;
        if (tabPurpleCount) tabPurpleCount.textContent = scopedList.filter(function(h){return h.color==='purple';}).length;
        if (tabPinkCount) tabPinkCount.textContent = scopedList.filter(function(h){return h.color==='pink';}).length;
    }

    // ---- 3. Hiển thị Toast ----
    function showToast(msg) {
        if (typeof window.toast === 'function') {
            window.toast(msg);
        } else {
            var t = document.createElement('div');
            t.className = 'reader-toast';
            t.textContent = msg;
            t.style.cssText = 'position:fixed;bottom:24px;left:50%;transform:translateX(-50%);background:rgba(15,23,42,0.95);color:#fff;padding:8px 18px;border-radius:99px;font-size:0.85rem;z-index:9999;box-shadow:0 4px 14px rgba(0,0,0,0.3);';
            document.body.appendChild(t);
            setTimeout(function() { t.remove(); }, 2400);
        }
    }

    // ---- 4. Vị trí và Hiện / Ẩn Floating Toolbar ----
    function showToolbarAt(rect) {
        if (!toolbar) return;
        var left = rect.left + (rect.width / 2);
        var isTopNear = rect.top < 70;
        var top = isTopNear ? (rect.bottom + 12) : (rect.top - 10);

        toolbar.style.left = Math.max(120, Math.min(window.innerWidth - 120, left)) + 'px';
        toolbar.style.top = top + 'px';
        toolbar.classList.toggle('hl-below', isTopNear);
        toolbar.classList.add('is-visible');
        toolbar.setAttribute('aria-hidden', 'false');
    }

    function hideToolbar() {
        if (!toolbar) return;
        toolbar.classList.remove('is-visible');
        toolbar.setAttribute('aria-hidden', 'true');
        currentSelectionContext = null;
    }

    // ---- 5. Xử lý Bôi đen văn bản & Click vào đoạn đã tô ----
    document.addEventListener('mouseup', handleTextSelection);
    document.addEventListener('touchend', function() {
        setTimeout(handleTextSelection, 80);
    });

    function handleTextSelection(e) {
        if (toolbar && toolbar.contains(e.target)) return;

        var sel = window.getSelection();
        if (!sel || sel.isCollapsed || !sel.rangeCount) {
            // Không có text chọn, kiểm tra nếu click trúng một thẻ .hl-mark có sẵn
            if (e.target && e.target.classList && e.target.classList.contains('hl-mark')) {
                var markEl = e.target;
                var rect = markEl.getBoundingClientRect();
                var hlId = markEl.getAttribute('data-hl-id');
                currentSelectionContext = {
                    isExistingMark: true,
                    markElement: markEl,
                    hlId: hlId,
                    text: markEl.textContent
                };
                showToolbarAt(rect);
                return;
            }
            hideToolbar();
            return;
        }

        var text = sel.toString().trim();
        if (text.length < 2) {
            hideToolbar();
            return;
        }

        var range = sel.getRangeAt(0);
        var container = range.commonAncestorContainer;
        if (container.nodeType === 3) container = container.parentNode;

        var contentWrap = container.closest ? container.closest('.chapter-content') : null;
        if (!contentWrap) {
            hideToolbar();
            return;
        }

        var pEl = container.closest ? container.closest('[data-para]') : null;
        var block = container.closest ? container.closest('.chapter-block') : null;

        var paraNo = pEl ? parseInt(pEl.getAttribute('data-para'), 10) : 0;
        var chapterId = block ? block.getAttribute('data-chapter-id') : CURRENT_CHAPTER_ID;
        var chapterNo = block ? block.getAttribute('data-chapter-no') : CURRENT_CHAPTER_NO;
        var chapterTitle = block ? block.getAttribute('data-chapter-title') : '<c:out value="${chapter.title}"/>';

        currentSelectionContext = {
            range: range.cloneRange(),
            text: text,
            paraNo: paraNo,
            pEl: pEl,
            chapterId: chapterId,
            chapterNo: chapterNo,
            chapterTitle: chapterTitle
        };

        var rect = range.getBoundingClientRect();
        if ((!rect || (rect.width === 0 && rect.height === 0)) && pEl) {
            rect = pEl.getBoundingClientRect();
        }
        showToolbarAt(rect);
    }

    document.addEventListener('mousedown', function (e) {
        if (toolbar && !toolbar.contains(e.target)) {
            var sel = window.getSelection();
            if (!sel || sel.isCollapsed) {
                hideToolbar();
            }
        }
    });

    // ---- 6. Hành động: Chọn màu Highlight ----
    var colorBtns = toolbar ? toolbar.querySelectorAll('.hl-color-btn') : [];
    colorBtns.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;

            var color = this.getAttribute('data-color');
            var ctx = currentSelectionContext;

            if (ctx.isExistingMark && ctx.markElement) {
                var oldColor = ctx.markElement.className.match(/hl-(yellow|green|blue|purple|pink)/);
                if (oldColor) ctx.markElement.classList.remove(oldColor[0]);
                ctx.markElement.classList.add('hl-' + color);

                var list = getHighlights();
                var item = list.find(function(h) { return h.id === ctx.hlId; });
                if (item) {
                    item.color = color;
                    saveHighlights(list);
                }
                showToast('Đã đổi màu sang ' + getColorName(color));
                hideToolbar();
                return;
            }

            var hlId = 'hl_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5);
            var mark = document.createElement('mark');
            mark.className = 'hl-mark hl-' + color;
            mark.setAttribute('data-hl-id', hlId);

            try {
                ctx.range.surroundContents(mark);
            } catch (err) {
                var frag = ctx.range.extractContents();
                mark.appendChild(frag);
                ctx.range.insertNode(mark);
            }

            var newItem = {
                id: hlId,
                storyId: STORY_ID,
                chapterId: ctx.chapterId,
                chapterNo: ctx.chapterNo,
                chapterTitle: ctx.chapterTitle,
                paraNo: ctx.paraNo,
                color: color,
                text: ctx.text,
                createdAt: new Date().toISOString()
            };

            var list = getHighlights();
            list.unshift(newItem);
            saveHighlights(list);

            showToast('Đã tô màu đoạn trích (' + getColorName(color) + ')');

            if (window.getSelection) window.getSelection().removeAllRanges();
            hideToolbar();
        });
    });

    function getColorName(c) {
        switch(c) {
            case 'yellow': return 'Vàng nắng';
            case 'green':  return 'Xanh ngọc';
            case 'blue':   return 'Xanh lam';
            case 'purple': return 'Tím mộng mơ';
            case 'pink':   return 'Hồng san hô';
            default: return c;
        }
    }

    // ---- 7. Hành động: Đánh dấu đoạn vào Tủ sách 🔖 ----
    var markActionBtn = document.getElementById('hl-action-mark');
    if (markActionBtn) {
        markActionBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;
            var ctx = currentSelectionContext;

            if (!ctx.isExistingMark) {
                var btnYellow = toolbar.querySelector('.hl-c-yellow');
                if (btnYellow) btnYellow.click();
            }

            if (ctx.chapterId && ctx.paraNo > 0) {
                var body = new URLSearchParams();
                body.append('action', 'mark');
                body.append('chapterId', ctx.chapterId);
                body.append('paraNo', ctx.paraNo);
                body.append('snippet', (ctx.text || '').substring(0, 180));

                fetch(CTX + '/bookmark', {
                    method: 'POST',
                    body: body,
                    credentials: 'same-origin',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                })
                .then(function(r) { return r.json(); })
                .then(function(res) {
                    if (res.success) {
                        if (ctx.pEl) ctx.pEl.classList.add('is-marked');
                        showToast('Đã lưu đoạn văn vào Tủ sách!');
                    } else {
                        showToast(res.message || 'Chưa lưu được vào Tủ sách.');
                    }
                })
                .catch(function() {
                    showToast('Đã lưu vào bộ nhớ cục bộ trên máy!');
                });
            } else {
                showToast('Đã lưu đánh dấu đoạn!');
            }
            hideToolbar();
        });
    }

    // ---- 8. Hành động: Sao chép trích dẫn 📋 ----
    var copyActionBtn = document.getElementById('hl-action-copy');
    if (copyActionBtn) {
        copyActionBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;
            var ctx = currentSelectionContext;

            var quoteText = '“' + ctx.text + '”\n\n— Trích Chương ' + (ctx.chapterNo || '') + ': '
                + (ctx.chapterTitle || '') + ' (' + STORY_TITLE + ')';

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(quoteText).then(function() {
                    showToast('Đã sao chép trích dẫn vào bộ nhớ tạm!');
                }).catch(function() {
                    fallbackCopy(quoteText);
                });
            } else {
                fallbackCopy(quoteText);
            }
            hideToolbar();
        });
    }

    function fallbackCopy(text) {
        var ta = document.createElement('textarea');
        ta.value = text;
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        try {
            document.execCommand('copy');
            showToast('Đã sao chép trích dẫn!');
        } catch (e) {
            showToast('Không thể sao chép.');
        }
        ta.remove();
    }

    /* ========================================================================
     * TÍNH NĂNG: TRÌNH TẠO ẢNH TRÍCH DẪN NGHỆ THUẬT (QUOTE CARD GENERATOR)
     * ===================================================================== */
    var quoteCardModal = document.getElementById('quote-card-modal');
    var closeQuoteCardBtn = document.getElementById('close-quote-card');
    var quoteCardCanvas = document.getElementById('quote-card-canvas');
    var btnCopyQuoteCard = document.getElementById('btn-copy-quote-card');
    var btnDownloadQuoteCard = document.getElementById('btn-download-quote-card');
    var quoteThemeBtns = document.querySelectorAll('.qc-theme-btn');
    var hlActionQuoteCardBtn = document.getElementById('hl-action-quote-card');

    var currentQuoteData = null;
    var currentCardTheme = 'sepia';

    var CARD_THEMES = {
        sepia: {
            bgGradient: ['#fbf2e3', '#f2deb9'],
            frameBorder: 'rgba(180, 83, 9, 0.35)',
            innerBorder: 'rgba(180, 83, 9, 0.15)',
            accent: '#b45309',
            quoteMark: 'rgba(180, 83, 9, 0.18)',
            text: '#451a03',
            meta: '#78350f',
            footer: 'rgba(120, 53, 15, 0.7)'
        },
        dark: {
            bgGradient: ['#0f172a', '#1e1b4b'],
            frameBorder: 'rgba(245, 158, 11, 0.45)',
            innerBorder: 'rgba(245, 158, 11, 0.18)',
            accent: '#fbbf24',
            quoteMark: 'rgba(251, 191, 36, 0.22)',
            text: '#f8fafc',
            meta: '#cbd5e1',
            footer: 'rgba(203, 213, 225, 0.7)'
        },
        pastel: {
            bgGradient: ['#fff1f2', '#fdf4ff'],
            frameBorder: 'rgba(244, 63, 94, 0.35)',
            innerBorder: 'rgba(244, 63, 94, 0.15)',
            accent: '#e11d48',
            quoteMark: 'rgba(244, 63, 94, 0.18)',
            text: '#4c0519',
            meta: '#881337',
            footer: 'rgba(136, 19, 55, 0.7)'
        },
        emerald: {
            bgGradient: ['#f0fdf4', '#ecfdf5'],
            frameBorder: 'rgba(16, 185, 129, 0.35)',
            innerBorder: 'rgba(16, 185, 129, 0.15)',
            accent: '#059669',
            quoteMark: 'rgba(16, 185, 129, 0.18)',
            text: '#064e3b',
            meta: '#065f46',
            footer: 'rgba(6, 95, 70, 0.7)'
        }
    };

    function openQuoteCardModal(data) {
        if (!data || !data.text) return;
        currentQuoteData = data;
        if (quoteCardModal) {
            if (typeof quoteCardModal.showModal === 'function') quoteCardModal.showModal();
            else quoteCardModal.setAttribute('open', '');
        }
        renderQuoteCardCanvas();
    }

    function closeQuoteCardModal() {
        if (quoteCardModal) {
            if (typeof quoteCardModal.close === 'function') quoteCardModal.close();
            else quoteCardModal.removeAttribute('open');
        }
    }

    if (closeQuoteCardBtn) closeQuoteCardBtn.addEventListener('click', closeQuoteCardModal);
    if (quoteCardModal) {
        quoteCardModal.addEventListener('click', function(e) {
            if (e.target === quoteCardModal) closeQuoteCardModal();
        });
    }

    quoteThemeBtns.forEach(function(btn) {
        btn.addEventListener('click', function() {
            quoteThemeBtns.forEach(function(b) { b.classList.remove('is-active'); });
            this.classList.add('is-active');
            currentCardTheme = this.getAttribute('data-theme') || 'sepia';
            renderQuoteCardCanvas();
        });
    });

    function wrapCanvasText(ctx, text, maxWidth) {
        var words = text.split(/\s+/);
        var lines = [];
        var currentLine = '';
        for (var i = 0; i < words.length; i++) {
            var word = words[i];
            var testLine = currentLine ? (currentLine + ' ' + word) : word;
            var width = ctx.measureText(testLine).width;
            if (width > maxWidth && currentLine) {
                lines.push(currentLine);
                currentLine = word;
            } else {
                currentLine = testLine;
            }
        }
        if (currentLine) lines.push(currentLine);
        return lines;
    }

    function renderQuoteCardCanvas() {
        if (!quoteCardCanvas || !currentQuoteData) return;
        var ctx = quoteCardCanvas.getContext('2d');
        var w = 1080;
        var h = 1350;
        quoteCardCanvas.width = w;
        quoteCardCanvas.height = h;

        var theme = CARD_THEMES[currentCardTheme] || CARD_THEMES.sepia;

        // 1. Nền Gradient mượt mà
        var grad = ctx.createLinearGradient(0, 0, w, h);
        grad.addColorStop(0, theme.bgGradient[0]);
        grad.addColorStop(1, theme.bgGradient[1]);
        ctx.fillStyle = grad;
        ctx.fillRect(0, 0, w, h);

        // 2. Khung viền nghệ thuật đôi (Double Decorative Frame)
        var margin = 60;
        ctx.strokeStyle = theme.frameBorder;
        ctx.lineWidth = 4;
        ctx.strokeRect(margin, margin, w - margin * 2, h - margin * 2);

        var innerMargin = 74;
        ctx.strokeStyle = theme.innerBorder;
        ctx.lineWidth = 1.5;
        ctx.strokeRect(innerMargin, innerMargin, w - innerMargin * 2, h - innerMargin * 2);

        // Họa tiết góc (Corner Diamonds)
        function drawCornerDiamond(cx, cy) {
            ctx.fillStyle = theme.accent;
            ctx.beginPath();
            ctx.moveTo(cx, cy - 8);
            ctx.lineTo(cx + 8, cy);
            ctx.lineTo(cx, cy + 8);
            ctx.lineTo(cx - 8, cy);
            ctx.closePath();
            ctx.fill();
        }
        drawCornerDiamond(innerMargin, innerMargin);
        drawCornerDiamond(w - innerMargin, innerMargin);
        drawCornerDiamond(innerMargin, h - innerMargin);
        drawCornerDiamond(w - innerMargin, h - innerMargin);

        // 3. Dấu mở ngoặc kép nghệ thuật to lớn mờ ảo
        ctx.font = 'bold 220px Georgia, "Times New Roman", serif';
        ctx.fillStyle = theme.quoteMark;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        ctx.fillText('“', w / 2, 130);

        // 4. Nội dung trích dẫn
        var rawText = (currentQuoteData.text || '').trim();
        var maxTextWidth = w - 240;

        // Tự động điều chỉnh kích thước chữ theo độ dài đoạn trích
        var fontSize = 48;
        var lineHeight = 76;
        if (rawText.length > 280) {
            fontSize = 38;
            lineHeight = 60;
        } else if (rawText.length > 180) {
            fontSize = 42;
            lineHeight = 68;
        } else if (rawText.length < 80) {
            fontSize = 54;
            lineHeight = 84;
        }

        ctx.font = 'italic ' + fontSize + 'px Georgia, "Merriweather", "Times New Roman", serif';
        ctx.fillStyle = theme.text;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        var lines = wrapCanvasText(ctx, rawText, maxTextWidth);

        // Căn giữa theo trục Y trong vùng giữa
        var textBlockHeight = lines.length * lineHeight;
        var startY = 320 + Math.max(0, (560 - textBlockHeight) / 2);

        for (var i = 0; i < lines.length; i++) {
            ctx.fillText(lines[i], w / 2, startY + (i * lineHeight));
        }

        // 5. Đường kẻ phân cách hoa văn giữa (Ornamental Divider)
        var divY = Math.min(1030, startY + textBlockHeight + 60);
        if (divY < 920) divY = 930;

        ctx.strokeStyle = theme.frameBorder;
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(w / 2 - 140, divY);
        ctx.lineTo(w / 2 + 140, divY);
        ctx.stroke();

        drawCornerDiamond(w / 2, divY);

        // 6. Thông tin Truyện & Chương
        var storyTitle = STORY_TITLE || 'Đọc Truyện';
        var chapterInfo = 'Chương ' + (currentQuoteData.chapterNo || '') + (currentQuoteData.chapterTitle ? (': ' + currentQuoteData.chapterTitle) : '');

        ctx.font = '700 34px "Plus Jakarta Sans", system-ui, -apple-system, sans-serif';
        ctx.fillStyle = theme.accent;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(storyTitle, w / 2, divY + 70);

        ctx.font = 'italic 26px Georgia, serif';
        ctx.fillStyle = theme.meta;
        ctx.fillText(chapterInfo, w / 2, divY + 120);

        // 7. Footer Watermark thương hiệu
        ctx.font = '600 20px "Plus Jakarta Sans", system-ui, sans-serif';
        ctx.fillStyle = theme.footer;
        ctx.letterSpacing = '1px';
        ctx.fillText('📖 ĐọcTruyện • Tinh hoa văn học', w / 2, h - 100);
    }

    // Tải ảnh PNG về máy
    if (btnDownloadQuoteCard) {
        btnDownloadQuoteCard.addEventListener('click', function() {
            if (!quoteCardCanvas) return;
            try {
                var link = document.createElement('a');
                var timestamp = Date.now();
                link.download = 'trich-dan-chuong-' + (currentQuoteData ? currentQuoteData.chapterNo : 'quote') + '-' + timestamp + '.png';
                link.href = quoteCardCanvas.toDataURL('image/png');
                document.body.appendChild(link);
                link.click();
                link.remove();
                if (typeof showToast === 'function') showToast('Đã tải ảnh trích dẫn nghệ thuật thành công! 📸');
            } catch (err) {
                if (typeof showToast === 'function') showToast('Không thể tải ảnh: ' + err.message);
            }
        });
    }

    // Sao chép ảnh vào clipboard
    if (btnCopyQuoteCard) {
        btnCopyQuoteCard.addEventListener('click', function() {
            if (!quoteCardCanvas) return;
            if (quoteCardCanvas.toBlob && window.ClipboardItem && navigator.clipboard && navigator.clipboard.write) {
                quoteCardCanvas.toBlob(function(blob) {
                    if (!blob) return;
                    navigator.clipboard.write([
                        new ClipboardItem({ 'image/png': blob })
                    ]).then(function() {
                        if (typeof showToast === 'function') showToast('Đã sao chép ảnh trích dẫn vào bộ nhớ tạm! 📋');
                    }).catch(function() {
                        btnDownloadQuoteCard.click();
                    });
                }, 'image/png');
            } else {
                btnDownloadQuoteCard.click();
            }
        });
    }

    // Gắn sự kiện nút Xuất ảnh trên Floating Toolbar
    if (hlActionQuoteCardBtn) {
        hlActionQuoteCardBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;
            var ctx = currentSelectionContext;
            hideToolbar();
            openQuoteCardModal({
                text: ctx.text,
                chapterNo: ctx.chapterNo,
                chapterTitle: ctx.chapterTitle || ('Chương ' + ctx.chapterNo)
            });
        });
    }

    // ---- 8b. Hành động: Thêm / Sửa Ghi Chú Cá Nhân 📝 ----
    var noteModal = document.getElementById('hl-note-modal');
    var notePreview = document.getElementById('hl-note-preview-text');
    var noteTextarea = document.getElementById('hl-note-textarea');
    var btnSaveNote = document.getElementById('btn-save-hl-note');
    var btnCancelNote = document.getElementById('btn-cancel-hl-note');
    var btnDeleteNote = document.getElementById('btn-delete-hl-note');
    var btnCloseNote = document.getElementById('close-hl-note');
    var activeEditingHlId = null;

    function openNoteModalForHighlight(hlId, quoteText, existingNote) {
        activeEditingHlId = hlId;
        if (notePreview) notePreview.textContent = '“' + (quoteText || '') + '”';
        if (noteTextarea) noteTextarea.value = existingNote || '';
        if (btnDeleteNote) btnDeleteNote.style.display = existingNote ? 'inline-block' : 'none';
        if (noteModal) {
            if (typeof noteModal.showModal === 'function') {
                noteModal.showModal();
            } else {
                noteModal.setAttribute('open', '');
            }
        }
    }

    function closeNoteModal() {
        if (noteModal) {
            if (typeof noteModal.close === 'function') {
                noteModal.close();
            } else {
                noteModal.removeAttribute('open');
            }
        }
        activeEditingHlId = null;
    }

    if (btnCloseNote) btnCloseNote.addEventListener('click', closeNoteModal);
    if (btnCancelNote) btnCancelNote.addEventListener('click', closeNoteModal);

    if (btnSaveNote) {
        btnSaveNote.addEventListener('click', function() {
            if (!activeEditingHlId) return;
            var noteVal = noteTextarea ? noteTextarea.value.trim() : '';
            var list = getHighlights();
            var item = list.find(function(h) { return h.id === activeEditingHlId; });
            if (item) {
                item.note = noteVal;
                saveHighlights(list);

                var markEl = document.querySelector('[data-hl-id="' + activeEditingHlId + '"]');
                if (markEl) {
                    if (noteVal) {
                        markEl.classList.add('has-note');
                        markEl.setAttribute('title', '📝 Ghi chú: ' + noteVal);
                    } else {
                        markEl.classList.remove('has-note');
                        markEl.removeAttribute('title');
                    }
                }
                showToast(noteVal ? 'Đã lưu ghi chú cá nhân!' : 'Đã xóa ghi chú.');
                renderDrawerList();
            }
            closeNoteModal();
        });
    }

    if (btnDeleteNote) {
        btnDeleteNote.addEventListener('click', function() {
            if (!activeEditingHlId) return;
            var list = getHighlights();
            var item = list.find(function(h) { return h.id === activeEditingHlId; });
            if (item) {
                item.note = '';
                saveHighlights(list);
                var markEl = document.querySelector('[data-hl-id="' + activeEditingHlId + '"]');
                if (markEl) {
                    markEl.classList.remove('has-note');
                    markEl.removeAttribute('title');
                }
                showToast('Đã xóa ghi chú.');
                renderDrawerList();
            }
            closeNoteModal();
        });
    }

    var noteActionBtn = document.getElementById('hl-action-note');
    if (noteActionBtn) {
        noteActionBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;
            var ctx = currentSelectionContext;

            var targetHlId = ctx.hlId;
            var targetText = ctx.text;
            var existingNote = '';

            if (ctx.isExistingMark) {
                var item = getHighlights().find(function(h) { return h.id === targetHlId; });
                if (item) existingNote = item.note || '';
            } else {
                // Nếu chưa được tô màu, tự động tô màu vàng nắng trước rồi mở ghi chú
                var defaultColor = 'yellow';
                var hlId = 'hl_' + Date.now() + '_' + Math.random().toString(36).substr(2, 5);
                var mark = document.createElement('mark');
                mark.className = 'hl-mark hl-' + defaultColor;
                mark.setAttribute('data-hl-id', hlId);

                try {
                    ctx.range.surroundContents(mark);
                } catch (err) {
                    var frag = ctx.range.extractContents();
                    mark.appendChild(frag);
                    ctx.range.insertNode(mark);
                }

                var newItem = {
                    id: hlId,
                    storyId: STORY_ID,
                    chapterId: ctx.chapterId,
                    chapterNo: ctx.chapterNo,
                    chapterTitle: ctx.chapterTitle,
                    paraNo: ctx.paraNo,
                    color: defaultColor,
                    text: ctx.text,
                    note: '',
                    createdAt: new Date().toISOString()
                };

                var list = getHighlights();
                list.unshift(newItem);
                saveHighlights(list);

                targetHlId = hlId;
                if (window.getSelection) window.getSelection().removeAllRanges();
            }

            hideToolbar();
            openNoteModalForHighlight(targetHlId, targetText, existingNote);
        });
    }

    // ---- 9. Hành động: Xóa Tô màu 🗑️ ----
    var clearActionBtn = document.getElementById('hl-action-clear');
    if (clearActionBtn) {
        clearActionBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!currentSelectionContext) return;
            var ctx = currentSelectionContext;

            if (ctx.isExistingMark && ctx.markElement) {
                removeMarkElement(ctx.markElement, ctx.hlId);
                showToast('Đã xóa tô màu đoạn này.');
            } else {
                var parentMark = ctx.range && ctx.range.commonAncestorContainer.nodeType === 1
                    ? ctx.range.commonAncestorContainer.closest('.hl-mark')
                    : (ctx.range ? ctx.range.commonAncestorContainer.parentNode.closest('.hl-mark') : null);
                if (parentMark) {
                    removeMarkElement(parentMark, parentMark.getAttribute('data-hl-id'));
                    showToast('Đã xóa tô màu đoạn này.');
                }
            }
            hideToolbar();
        });
    }

    function removeMarkElement(markEl, hlId) {
        if (!markEl) return;
        var parent = markEl.parentNode;
        while (markEl.firstChild) {
            parent.insertBefore(markEl.firstChild, markEl);
        }
        markEl.remove();

        if (hlId) {
            var list = getHighlights().filter(function(h) { return h.id !== hlId; });
            saveHighlights(list);
        }
    }

    // ---- 10. Phục hồi Highlights khi tải trang hoặc nối chương mới ----
    function applySavedHighlights(container) {
        var list = getHighlights();
        if (!list || !list.length) return;

        var blocks = (container || document).querySelectorAll('.chapter-block');
        blocks.forEach(function (block) {
            var chId = block.getAttribute('data-chapter-id');
            var chNo = block.getAttribute('data-chapter-no');

            var itemsForChapter = list.filter(function(h) {
                return (h.chapterId && String(h.chapterId) === String(chId))
                    || (h.chapterNo && String(h.chapterNo) === String(chNo));
            });

            if (!itemsForChapter.length) return;

            itemsForChapter.forEach(function (item) {
                var pEl = block.querySelector('.chapter-content [data-para="' + item.paraNo + '"]');
                if (!pEl) return;

                if (pEl.querySelector('[data-hl-id="' + item.id + '"]')) return;

                var text = item.text;
                if (!text || pEl.textContent.indexOf(text) === -1) return;

                highlightTextInElement(pEl, text, item.color, item.id, item.note);
            });
        });
    }

    function highlightTextInElement(element, searchText, color, id, note) {
        var innerHTML = element.innerHTML;
        var idx = innerHTML.indexOf(searchText);
        if (idx !== -1 && innerHTML.indexOf('data-hl-id="' + id + '"') === -1) {
            var noteClass = note ? ' has-note' : '';
            var titleAttr = note ? ' title="📝 Ghi chú: ' + escapeHtml(note) + '"' : '';
            var replacement = '<mark class="hl-mark hl-' + color + noteClass + '" data-hl-id="' + id + '"' + titleAttr + '>' + searchText + '</mark>';
            element.innerHTML = innerHTML.replace(searchText, replacement);
        }
    }

    applySavedHighlights(document);
    updateCounter();

    var wrap = document.getElementById('chapters');
    if (wrap && window.MutationObserver) {
        new MutationObserver(function (records) {
            records.forEach(function (r) {
                [].forEach.call(r.addedNodes, function (n) {
                    if (n.nodeType === 1 && n.classList.contains('chapter-block')) {
                        applySavedHighlights(n);
                    }
                });
            });
        }).observe(wrap, { childList: true });
    }

    // ---- 11. Ngăn kéo / Modal Quản lý Trích dẫn & Điểm nhấn ----
    function getFilteredList() {
        var list = getHighlights();

        // 1. Lọc theo phạm vi (Chương này vs Cả bộ truyện)
        if (activeScope === 'chapter') {
            list = list.filter(function(h) {
                return String(h.chapterId) === String(CURRENT_CHAPTER_ID) || String(h.chapterNo) === String(CURRENT_CHAPTER_NO);
            });
        }

        // 2. Lọc theo màu
        if (activeFilter !== 'all') {
            list = list.filter(function(h) { return h.color === activeFilter; });
        }

        // 3. Lọc theo từ khóa tìm kiếm (tìm cả trong câu trích dẫn và ghi chú)
        if (searchKeyword) {
            var kw = searchKeyword.toLowerCase();
            list = list.filter(function(h) {
                return (h.text && h.text.toLowerCase().indexOf(kw) !== -1)
                    || (h.note && h.note.toLowerCase().indexOf(kw) !== -1)
                    || (h.chapterTitle && h.chapterTitle.toLowerCase().indexOf(kw) !== -1);
            });
        }

        return list;
    }

    function renderDrawerList() {
        if (!drawerList) return;
        var list = getFilteredList();

        if (summaryLabel) {
            var scopeName = activeScope === 'chapter' ? ('Chương ' + CURRENT_CHAPTER_NO) : 'Toàn bộ truyện';
            summaryLabel.textContent = 'Đang hiển thị ' + list.length + ' đoạn (' + scopeName + ')';
        }

        updateCounter();

        if (!list.length) {
            var emptyHint = searchKeyword ? 'Không tìm thấy trích dẫn phù hợp từ khoá "' + searchKeyword + '".'
                          : (activeScope === 'chapter' ? 'Chương này chưa có đoạn nào được tô màu.' : 'Chưa có đoạn trích dẫn nào được lưu.');
            drawerList.innerHTML = '<div class="hl-empty-note">'
                + '<span>🖍️</span>'
                + '<p>' + emptyHint + '</p>'
                + '<small class="muted">' + (activeScope === 'chapter' ? 'Bạn có thể chuyển sang xem "Cả bộ truyện" hoặc bôi đen văn bản để tô màu ngay.' : 'Hãy bôi đen bất kỳ đoạn văn nào trong truyện để tô màu!') + '</small>'
                + '</div>';
            return;
        }

        var html = '';
        list.forEach(function (item) {
            var d = new Date(item.createdAt || Date.now());
            var dateStr = d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            var isLong = item.text && item.text.length > 180;

            var noteHtml = '';
            if (item.note) {
                noteHtml = '<div class="hl-item-note-box">'
                         + '<span class="hl-note-tag">📝 Ghi chú:</span> '
                         + '<span class="hl-note-text">' + escapeHtml(item.note) + '</span>'
                         + '<button type="button" class="hl-btn-edit-note" data-hl-id="' + item.id + '" title="Chỉnh sửa ghi chú">✏️ Sửa</button>'
                         + '</div>';
            } else {
                noteHtml = '<div class="hl-item-note-empty">'
                         + '<button type="button" class="hl-btn-add-note" data-hl-id="' + item.id + '">+ Thêm ghi chú</button>'
                         + '</div>';
            }

            html += '<div class="hl-item-card c-' + item.color + '" data-hl-id="' + item.id + '" data-chapter-id="' + item.chapterId + '" data-para="' + item.paraNo + '">'
                + '<div class="hl-item-meta">'
                + '<span class="hl-item-chapter">Chương ' + (item.chapterNo || '') + ' &middot; Đoạn ' + item.paraNo + '</span>'
                + '<span>' + dateStr + '</span>'
                + '</div>'
                + '<p class="hl-item-text" id="text-' + item.id + '">“' + escapeHtml(item.text) + '”</p>'
                + (isLong ? '<button type="button" class="hl-btn-toggle-expand" data-target="text-' + item.id + '">Xem thêm &darr;</button>' : '')
                + noteHtml
                + '<div class="hl-item-actions">'
                + '<button type="button" class="hl-item-btn btn-jump">🚀 Đi tới đoạn</button>'
                + '<button type="button" class="hl-item-btn btn-quote-card">📸 Thẻ ảnh</button>'
                + '<button type="button" class="hl-item-btn btn-copy">📋 Sao chép</button>'
                + '<button type="button" class="hl-item-btn btn-del">🗑️ Xoá</button>'
                + '</div>'
                + '</div>';
        });

        drawerList.innerHTML = html;

        // Bắt sự kiện trên từng item
        drawerList.querySelectorAll('.hl-item-card').forEach(function (card) {
            var hlId = card.getAttribute('data-hl-id');
            var paraNo = card.getAttribute('data-para');
            var chId = card.getAttribute('data-chapter-id');

            // Bấm vào card -> Nhảy tới đoạn
            card.addEventListener('click', function (e) {
                if (e.target.closest('.btn-copy') || e.target.closest('.btn-del') || e.target.closest('.btn-quote-card')
                    || e.target.closest('.hl-btn-toggle-expand') || e.target.closest('.hl-btn-edit-note') || e.target.closest('.hl-btn-add-note')) return;
                closeDrawer();
                jumpToHighlight(chId, paraNo, hlId);
            });

            // Nút Xem thêm / Thu gọn
            var btnExpand = card.querySelector('.hl-btn-toggle-expand');
            if (btnExpand) {
                btnExpand.addEventListener('click', function(e) {
                    e.stopPropagation();
                    var targetId = this.getAttribute('data-target');
                    var textEl = document.getElementById(targetId);
                    if (textEl) {
                        var expanded = textEl.classList.toggle('is-expanded');
                        this.innerHTML = expanded ? 'Thu gọn &uarr;' : 'Xem thêm &darr;';
                    }
                });
            }

            // Nút Thêm / Sửa Ghi Chú
            var btnEditNote = card.querySelector('.hl-btn-edit-note');
            if (btnEditNote) {
                btnEditNote.addEventListener('click', function(e) {
                    e.stopPropagation();
                    var item = getHighlights().find(function(h) { return h.id === hlId; });
                    if (item) {
                        closeDrawer();
                        openNoteModalForHighlight(item.id, item.text, item.note || '');
                    }
                });
            }
            var btnAddNote = card.querySelector('.hl-btn-add-note');
            if (btnAddNote) {
                btnAddNote.addEventListener('click', function(e) {
                    e.stopPropagation();
                    var item = getHighlights().find(function(h) { return h.id === hlId; });
                    if (item) {
                        closeDrawer();
                        openNoteModalForHighlight(item.id, item.text, '');
                    }
                });
            }

            // Nút Thẻ ảnh trích dẫn nghệ thuật
            var btnQuoteCard = card.querySelector('.btn-quote-card');
            if (btnQuoteCard) {
                btnQuoteCard.addEventListener('click', function (e) {
                    e.stopPropagation();
                    var item = getHighlights().find(function(h) { return h.id === hlId; });
                    if (!item) return;
                    closeDrawer();
                    openQuoteCardModal({
                        text: item.text,
                        chapterNo: item.chapterNo,
                        chapterTitle: item.chapterTitle || ('Chương ' + item.chapterNo)
                    });
                });
            }

            // Nút Sao chép
            var btnCopy = card.querySelector('.btn-copy');
            if (btnCopy) {
                btnCopy.addEventListener('click', function (e) {
                    e.stopPropagation();
                    var item = getHighlights().find(function(h) { return h.id === hlId; });
                    if (!item) return;
                    var q = '“' + item.text + '”' + (item.note ? ('\n[Ghi chú: ' + item.note + ']') : '')
                          + '\n\n— Trích Chương ' + item.chapterNo + ' (' + STORY_TITLE + ')';
                    if (navigator.clipboard) navigator.clipboard.writeText(q);
                    showToast('Đã sao chép trích dẫn!');
                });
            }

            // Nút Xóa
            var btnDel = card.querySelector('.btn-del');
            if (btnDel) {
                btnDel.addEventListener('click', function (e) {
                    e.stopPropagation();
                    var list = getHighlights().filter(function(h) { return h.id !== hlId; });
                    saveHighlights(list);

                    var markEl = document.querySelector('[data-hl-id="' + hlId + '"]');
                    if (markEl) removeMarkElement(markEl, null);

                    renderDrawerList();
                    showToast('Đã xóa đoạn trích dẫn.');
                });
            }
        });
    }

    function jumpToHighlight(chapterId, paraNo, hlId) {
        var markEl = hlId ? document.querySelector('[data-hl-id="' + hlId + '"]') : null;
        if (markEl) {
            markEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
            markEl.classList.add('is-flash');
            setTimeout(function() { markEl.classList.remove('is-flash'); }, 1800);
            return;
        }

        var block = document.querySelector('.chapter-block[data-chapter-id="' + chapterId + '"]');
        if (block && typeof window.scrollToPara === 'function') {
            window.scrollToPara(block, parseInt(paraNo, 10));
        } else {
            var p = document.querySelector('[data-para="' + paraNo + '"]');
            if (p) {
                p.scrollIntoView({ behavior: 'smooth', block: 'center' });
                p.classList.add('is-flash');
                setTimeout(function() { p.classList.remove('is-flash'); }, 1800);
            }
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    // ---- 12. Gắn sự kiện các bộ lọc, tìm kiếm và thao tác hàng loạt ----
    if (filterTabs) {
        filterTabs.querySelectorAll('.hl-filter-btn').forEach(function (tab) {
            tab.addEventListener('click', function () {
                filterTabs.querySelectorAll('.hl-filter-btn').forEach(function(b) { b.classList.remove('is-active'); });
                this.classList.add('is-active');
                activeFilter = this.getAttribute('data-filter');
                renderDrawerList();
            });
        });
    }

    if (searchInput) {
        searchInput.addEventListener('input', function() {
            searchKeyword = this.value.trim();
            renderDrawerList();
        });
    }

    if (scopeChapterBtn && scopeAllBtn) {
        scopeChapterBtn.addEventListener('click', function() {
            scopeChapterBtn.classList.add('is-active');
            scopeAllBtn.classList.remove('is-active');
            activeScope = 'chapter';
            renderDrawerList();
        });
        scopeAllBtn.addEventListener('click', function() {
            scopeAllBtn.classList.add('is-active');
            scopeChapterBtn.classList.remove('is-active');
            activeScope = 'all';
            renderDrawerList();
        });
    }

    // Sao chép toàn bộ danh sách trích dẫn đang xem
    if (exportAllBtn) {
        exportAllBtn.addEventListener('click', function() {
            var list = getFilteredList();
            if (!list.length) {
                showToast('Không có đoạn nào để sao chép.');
                return;
            }
            var text = '=== DANH SÁCH TRÍCH DẪN: ' + STORY_TITLE + ' ===\n\n';
            list.forEach(function(item, idx) {
                text += (idx + 1) + '. Chương ' + (item.chapterNo || '') + ' (Đoạn ' + item.paraNo + '):\n';
                text += '“' + item.text + '”\n\n';
            });
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text).then(function() {
                    showToast('Đã sao chép tất cả ' + list.length + ' đoạn trích dẫn!');
                });
            } else {
                fallbackCopy(text);
            }
        });
    }

    // Dọn dẹp / Xóa tất cả điểm nhấn của chương này
    if (clearChapterBtn) {
        clearChapterBtn.addEventListener('click', function() {
            var list = getHighlights();
            var chapCount = list.filter(function(h) {
                return String(h.chapterId) === String(CURRENT_CHAPTER_ID) || String(h.chapterNo) === String(CURRENT_CHAPTER_NO);
            }).length;

            if (chapCount === 0) {
                showToast('Chương này hiện không có điểm nhấn nào để xoá.');
                return;
            }

            if (!confirm('Bạn có chắc chắn muốn xoá tất cả ' + chapCount + ' đoạn tô màu trong Chương ' + CURRENT_CHAPTER_NO + ' không?')) {
                return;
            }

            // Gỡ toàn bộ thẻ mark trong DOM
            var block = document.querySelector('.chapter-block[data-chapter-id="' + CURRENT_CHAPTER_ID + '"]') || document;
            block.querySelectorAll('.hl-mark').forEach(function(el) {
                var parent = el.parentNode;
                while (el.firstChild) {
                    parent.insertBefore(el.firstChild, el);
                }
                el.remove();
            });

            // Lọc bỏ khỏi storage
            var remaining = list.filter(function(h) {
                return String(h.chapterId) !== String(CURRENT_CHAPTER_ID) && String(h.chapterNo) !== String(CURRENT_CHAPTER_NO);
            });
            saveHighlights(remaining);

            renderDrawerList();
            showToast('Đã dọn dẹp sạch sẽ toàn bộ ' + chapCount + ' điểm nhấn của chương này!');
        });
    }

    function openDrawer() {
        if (!drawer) return;
        renderDrawerList();
        try {
            if (drawer.showModal) drawer.showModal();
            else drawer.setAttribute('open', '');
        } catch (e) {
            drawer.setAttribute('open', '');
        }
    }

    function closeDrawer() {
        if (!drawer) return;
        if (typeof drawer.close === 'function') {
            drawer.close();
        } else {
            drawer.removeAttribute('open');
        }
    }

    if (drawerBtn) drawerBtn.addEventListener('click', openDrawer);
    if (drawerCloseBtn) drawerCloseBtn.addEventListener('click', closeDrawer);

    if (drawer) {
        drawer.addEventListener('click', function (e) {
            if (e.target === drawer) closeDrawer();
        });
    }

    // Phím tắt H: Bật/tắt Drawer; Alt+H: Bật/tắt Chế độ đọc sạch sẽ (Ẩn/Hiện màu tô)
    document.addEventListener('keydown', function (e) {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
        if (e.altKey && (e.key === 'h' || e.key === 'H')) {
            e.preventDefault();
            toggleHighlightVisibility();
            return;
        }
        if (e.key === 'h' || e.key === 'H') {
            e.preventDefault();
            if (drawer && drawer.open) {
                closeDrawer();
            } else {
                openDrawer();
            }
        }
    });

})();
</script>

</body>
</html>


