<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  chapter/form-comic.jsp — Thêm / sửa chương TRUYỆN TRANH
================================================================================
  Nhận: chapter (kèm chapter.pages khi sửa) · story · maxPages · maxPageMb
        (thông báo lỗi do layout/editor.jsp in — không in lại ở đây)

  Truyện chữ dùng chapter/form.jsp (trình soạn văn bản). ChapterServlet chọn
  form theo story.comic — hai form, CÙNG một action create/edit.

  CÁCH FORM GỬI THỨ TỰ TRANG
    Ảnh mới:   input name="pages" (multiple) — server lấy theo đúng thứ tự file.
    pageOrder: "p12,n0,p15,n1" — p{id} = trang đã có, n{i} = ảnh mới thứ i.
    removePages: "13,14" — id trang cũ bị bỏ.
    Server ghép lại bằng ChapterPage.arrange() — hàm thuần, có test riêng,
    và GIỮ LẠI mọi trang form quên nhắc tới (mất trang tệ hơn lệch chỗ).

  KHÔNG CÓ JAVASCRIPT
    Vẫn chạy: chọn file -> lưu -> các trang mới nối vào cuối theo thứ tự chọn.
    Chỉ mất phần xem trước, kéo thả sắp xếp và bỏ trang.

  _csrf GỬI CẢ TRÊN URL, giống form đăng truyện: form quá cỡ thì Tomcat bỏ
  phần thân multipart, ô _csrf trong thân mất theo — CsrfFilter trả 403 khó
  hiểu thay vì để servlet báo "ảnh quá lớn". Token trên URL vẫn còn.
================================================================================
--%>
<c:set var="isNew" value="${empty chapter.id or chapter.id eq 0}"/>

<div class="editor-header-meta">
    <h1>${isNew ? 'Thêm chương truyện tranh' : 'Sửa chương truyện tranh'}</h1>
    <p class="muted-note">
        🎨 Truyện: <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}" style="color:var(--ember); font-weight:600;"><c:out value="${story.title}"/></a>
    </p>
</div>

<form id="comicForm" action="${pageContext.request.contextPath}/chapter?_csrf=${csrfToken}"
      method="post" enctype="multipart/form-data" class="wide-form"
      data-max-pages="${maxPages}" data-max-mb="${maxPageMb}">
    <input type="hidden" name="_csrf" value="${csrfToken}">
    <input type="hidden" name="action" value="${isNew ? 'create' : 'edit'}">
    <input type="hidden" name="id" value="${chapter.id}">
    <input type="hidden" name="storyId" value="${story.id}">
    <input type="hidden" name="pageOrder" id="pageOrder" value="">
    <input type="hidden" name="removePages" id="removePages" value="">

    <div class="form-row">
        <div style="max-width:11em">
            <label for="chapterNo">Số chương *</label>
            <input type="number" id="chapterNo" name="chapterNo" min="1" required value="${chapter.chapterNo}">
        </div>
        <div style="flex:1">
            <label for="title">Tiêu đề chương *</label>
            <input type="text" id="title" name="title" maxlength="200" required
                   placeholder="Ví dụ: Chương 1 — Gặp gỡ"
                   value="<c:out value='${chapter.title}'/>">
        </div>
    </div>

    <%-- CHƯƠNG VIP — cùng ý nghĩa với chương chữ. Ảnh chương VIP chỉ phát cho
         người đã mở khoá (UploadedFileServlet kiểm quyền). --%>
    <div class="vip-setting-card">
        <label style="display:flex; align-items:center; gap:8px; cursor:pointer; font-weight:600; margin-bottom:0">
            <input type="checkbox" id="isVipCheckbox" name="isVip" value="1" ${chapter.vip ? 'checked' : ''}
                   onchange="document.getElementById('coinPriceWrapper').style.display = this.checked ? 'flex' : 'none';">
            <span>🔒 Đặt làm Chương VIP (Có tính phí xu để mở khoá)</span>
        </label>
        <div id="coinPriceWrapper" style="display:${chapter.vip ? 'flex' : 'none'}; align-items:center; gap:8px;">
            <label for="coinPrice" style="margin-bottom:0; font-size:0.9rem">Giá mở khoá (xu):</label>
            <input type="number" id="coinPrice" name="coinPrice" min="1" max="10000" style="width:6.5em; padding:6px 10px;"
                   value="${chapter.coinPrice > 0 ? chapter.coinPrice : 10}">
            <span class="muted small" style="font-size:0.82rem; opacity:0.8;">(khuyên dùng: 5 - 20 xu)</span>
        </div>
    </div>

    <c:if test="${not isNew}">
        <div class="vip-setting-card" style="margin-top:10px">
            <label style="display:flex; align-items:center; gap:8px; cursor:pointer; font-weight:600; margin-bottom:0">
                <input type="checkbox" name="notifyFollowers" value="1">
                <span>📢 Gửi thông báo cập nhật chương tới người theo dõi bộ truyện</span>
            </label>
        </div>
    </c:if>

    <%-- ================================================= TRANG ẢNH --%>
    <div class="comic-editor">
        <div class="comic-editor-head" style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px;">
            <div>
                <label for="pageFiles" style="margin-bottom:2px">Trang ảnh *</label>
                <p class="field-hint" style="margin:0">
                    PNG, JPG, GIF hoặc WebP · tối đa ${maxPageMb} MB/trang · ${maxPages} trang/chương.
                    Kéo thả để sắp xếp. Tên file kiểu <code>01.jpg, 02.jpg…</code> sẽ tự xếp đúng thứ tự.
                </p>
            </div>
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
                <button type="button" class="btn btn-sm btn-ghost" id="btnReversePages" title="Đảo ngược toàn bộ thứ tự trang (từ dưới lên trên)">
                    🔄 Đảo ngược thứ tự
                </button>
                <button type="button" class="btn btn-sm btn-ghost" id="btnClearNewPages" style="display:none; color:var(--danger, #ef4444);" title="Xóa tất cả các trang ảnh mới vừa thêm">
                    🗑️ Xóa ảnh mới
                </button>
                <span class="comic-count" id="pageCount">${chapter.pageCount} trang</span>
            </div>
        </div>

        <label class="comic-drop" id="dropZone" for="pageFiles">
            <input type="file" id="pageFiles" name="pages" multiple
                   accept="image/png,image/jpeg,image/gif,image/webp">
            <span class="comic-drop-icon">🖼️</span>
            <span><b>Chọn ảnh</b> hoặc kéo thả ảnh vào đây</span>
            <small>Có thể chọn nhiều lần — ảnh mới nối vào cuối danh sách</small>
        </label>

        <%-- Lưới trang. Trang ĐÃ CÓ do server in sẵn (data-token="p{id}");
             ảnh MỚI do JavaScript thêm vào (data-token="n{i}"). --%>
        <ol class="comic-grid" id="pageGrid">
            <c:forEach var="pg" items="${chapter.pages}">
                <li class="comic-thumb" draggable="true" data-token="p${pg.id}" data-id="${pg.id}">
                    <img src="${pageContext.request.contextPath}${pg.url}" alt="" loading="lazy">
                    <span class="comic-thumb-no"></span>
                    <span class="comic-thumb-tools">
                        <button type="button" data-move="-1" title="Lên trước">↑</button>
                        <button type="button" data-move="1" title="Xuống sau">↓</button>
                        <button type="button" data-remove title="Bỏ trang này">✕</button>
                    </span>
                </li>
            </c:forEach>
        </ol>
        <p class="field-hint" id="sizeWarn" hidden></p>
    </div>

    <div class="form-actions" style="margin-top:20px;">
        <button type="submit" class="btn btn-primary" id="btnSaveComic">💾 Lưu chương</button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">Huỷ</a>
        <c:if test="${not isNew}">
            <button type="submit" form="deleteChapterForm" class="btn btn-danger" style="margin-left:auto"
                    data-confirm="Xoá hẳn chương này cùng toàn bộ ảnh trang? Không khôi phục được."
                    data-confirm-title="Xác nhận xoá vĩnh viễn"
                    data-confirm-ok="Đồng ý xoá">
                Xoá chương</button>
        </c:if>
    </div>
</form>

<c:if test="${not isNew}">
    <form id="deleteChapterForm" action="${pageContext.request.contextPath}/chapter" method="post" style="display:none">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${chapter.id}">
    </form>
</c:if>

<script>
/*
 * Trình sắp xếp trang. Toàn bộ trạng thái nằm ở THỨ TỰ các <li> trong
 * #pageGrid — sắp xếp là di chuyển <li>, bỏ trang là xoá <li>. Lúc gửi form
 * mới đọc ra pageOrder / removePages và dựng lại input file. Không có mảng
 * trạng thái thứ hai nào để mà lệch với thứ người dùng đang nhìn thấy.
 */
(function () {
    var form   = document.getElementById('comicForm');
    var grid   = document.getElementById('pageGrid');
    var input  = document.getElementById('pageFiles');
    var drop   = document.getElementById('dropZone');
    var countEl = document.getElementById('pageCount');
    var warn   = document.getElementById('sizeWarn');
    var btnReverse = document.getElementById('btnReversePages');
    var btnClearNew = document.getElementById('btnClearNewPages');
    if (!form || !grid || !input) return;

    var toastFn = window.showToast || function (msg) {
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

    var MAX_PAGES = parseInt(form.getAttribute('data-max-pages'), 10) || 200;
    var MAX_BYTES = (parseInt(form.getAttribute('data-max-mb'), 10) || 5) * 1024 * 1024;
    var MAX_TOTAL = 150 * 1024 * 1024;          // khớp maxRequestSize (160 MB) có chừa phần dư

    var fresh = [];                              // File[] — ảnh mới, chỉ số = n{i}
    var removed = [];                            // id trang cũ bị bỏ
    var canRebuild = typeof DataTransfer !== 'undefined';

    function updateClearNewButton() {
        if (!btnClearNew) return;
        var hasNew = grid.querySelector('.comic-thumb.is-new') !== null;
        btnClearNew.style.display = hasNew ? 'inline-block' : 'none';
    }

    function renumber() {
        var items = grid.children;
        for (var i = 0; i < items.length; i++) {
            items[i].querySelector('.comic-thumb-no').textContent = i + 1;
        }
        countEl.textContent = items.length + ' trang';
        countEl.classList.toggle('is-over', items.length > MAX_PAGES);
        updateClearNewButton();
    }

    if (btnReverse) {
        btnReverse.addEventListener('click', function () {
            var items = Array.prototype.slice.call(grid.children);
            if (items.length <= 1) return;
            items.reverse().forEach(function (el) {
                grid.appendChild(el);
            });
            renumber();
            toastFn('🔄 Đã đảo ngược thứ tự các trang ảnh!');
        });
    }

    if (btnClearNew) {
        btnClearNew.addEventListener('click', function () {
            var newItems = grid.querySelectorAll('.comic-thumb.is-new');
            if (newItems.length === 0) return;
            newItems.forEach(function (el) { el.remove(); });
            fresh = [];
            renumber();
            checkSizes();
            toastFn('🗑️ Đã xóa toàn bộ trang ảnh mới vừa thêm.');
        });
    }

    /* Sắp theo tên file kiểu "tự nhiên": 2.jpg trước 10.jpg (so số, không so chữ). */
    function naturalCompare(a, b) {
        return a.name.localeCompare(b.name, undefined, { numeric: true, sensitivity: 'base' });
    }

    function addFiles(list) {
        var files = Array.prototype.slice.call(list).filter(function (f) {
            return /^image\/(png|jpeg|gif|webp)$/.test(f.type);
        }).sort(naturalCompare);
        files.forEach(function (f) {
            var idx = fresh.length;
            fresh.push(f);
            var li = document.createElement('li');
            li.className = 'comic-thumb is-new';
            li.draggable = true;
            li.setAttribute('data-token', 'n' + idx);
            var img = document.createElement('img');
            img.alt = '';
            img.src = URL.createObjectURL(f);
            li.appendChild(img);
            li.insertAdjacentHTML('beforeend',
                '<span class="comic-thumb-no"></span><span class="comic-thumb-badge">Mới</span>'
              + '<span class="comic-thumb-tools"><button type="button" data-move="-1" title="Lên trước">↑</button>'
              + '<button type="button" data-move="1" title="Xuống sau">↓</button>'
              + '<button type="button" data-remove title="Bỏ trang này">✕</button></span>');
            if (f.size > MAX_BYTES) li.classList.add('is-too-big');
            grid.appendChild(li);
        });
        renumber();
        checkSizes();
    }

    function checkSizes() {
        var total = 0, big = 0;
        grid.querySelectorAll('.comic-thumb.is-new').forEach(function (li) {
            var f = fresh[parseInt(li.getAttribute('data-token').slice(1), 10)];
            if (!f) return;
            total += f.size;
            if (f.size > MAX_BYTES) big++;
        });
        var msg = '';
        if (big) msg = '⚠️ Có ' + big + ' ảnh nặng quá ' + (MAX_BYTES / 1048576) + ' MB (viền đỏ) — nén lại hoặc bỏ ra.';
        else if (total > MAX_TOTAL) msg = '⚠️ Tổng ảnh mới ' + Math.round(total / 1048576)
                + ' MB — quá nặng cho một lần lưu. Lưu bớt, rồi sửa chương để thêm tiếp.';
        warn.textContent = msg;
        warn.hidden = !msg;
        return !msg;
    }

    /* Khi đã tự quản danh sách file thì input chỉ là nút bấm: mỗi lần chọn
       xong lấy file ra rồi xoá trắng, để chọn lần hai không đè lần một. */
    input.addEventListener('change', function () {
        if (!canRebuild) return;                 // trình duyệt cũ: để input tự gửi như form thường
        addFiles(input.files);
        input.value = '';
    });

    if (canRebuild && drop) {
        ['dragenter', 'dragover'].forEach(function (ev) {
            drop.addEventListener(ev, function (e) {
                if (e.dataTransfer && e.dataTransfer.types.indexOf('Files') !== -1) {
                    e.preventDefault(); drop.classList.add('is-over');
                }
            });
        });
        ['dragleave', 'drop'].forEach(function (ev) {
            drop.addEventListener(ev, function () { drop.classList.remove('is-over'); });
        });
        drop.addEventListener('drop', function (e) {
            if (!e.dataTransfer || !e.dataTransfer.files.length) return;
            e.preventDefault();
            addFiles(e.dataTransfer.files);
        });
    }

    /* ↑ ↓ ✕ trên từng ô */
    grid.addEventListener('click', function (e) {
        var btn = e.target.closest('button');
        if (!btn) return;
        var li = btn.closest('.comic-thumb');
        if (btn.hasAttribute('data-remove')) {
            if (li.hasAttribute('data-id')) removed.push(li.getAttribute('data-id'));
            li.remove();
        } else {
            var dir = parseInt(btn.getAttribute('data-move'), 10);
            var sib = dir < 0 ? li.previousElementSibling : li.nextElementSibling;
            if (sib) grid.insertBefore(li, dir < 0 ? sib : sib.nextElementSibling);
        }
        renumber();
        checkSizes();
    });

    /* Kéo thả để sắp xếp trong lưới */
    var dragging = null;
    grid.addEventListener('dragstart', function (e) {
        dragging = e.target.closest('.comic-thumb');
        if (dragging) { dragging.classList.add('is-dragging'); e.dataTransfer.effectAllowed = 'move'; }
    });
    grid.addEventListener('dragend', function () {
        if (dragging) dragging.classList.remove('is-dragging');
        dragging = null;
        renumber();
    });
    grid.addEventListener('dragover', function (e) {
        if (!dragging) return;
        e.preventDefault();
        var over = e.target.closest('.comic-thumb');
        if (!over || over === dragging) return;
        var r = over.getBoundingClientRect();
        var after = (e.clientX - r.left) > r.width / 2;
        grid.insertBefore(dragging, after ? over.nextElementSibling : over);
    });

    form.addEventListener('submit', function (e) {
        if (!checkSizes()) { e.preventDefault(); warn.scrollIntoView({ block: 'center' }); return; }
        var items = grid.children;
        if (items.length === 0 && !input.files.length) {
            e.preventDefault();
            toastFn('⚠️ Chương truyện tranh cần ít nhất một trang ảnh.');
            return;
        }
        if (items.length > MAX_PAGES) {
            e.preventDefault();
            toastFn('⚠️ Một chương tối đa ' + MAX_PAGES + ' trang.');
            return;
        }

        /* Dựng lại input file theo thứ tự ảnh mới CÒN trong lưới, và đánh lại
           chỉ số n0, n1… cho khớp đúng thứ tự gửi lên. */
        var order = [];
        if (canRebuild) {
            var dt = new DataTransfer(), k = 0;
            for (var i = 0; i < items.length; i++) {
                var tok = items[i].getAttribute('data-token');
                if (tok.charAt(0) === 'n') {
                    dt.items.add(fresh[parseInt(tok.slice(1), 10)]);
                    order.push('n' + (k++));
                } else {
                    order.push(tok);
                }
            }
            input.files = dt.files;
        } else {
            for (var j = 0; j < items.length; j++) order.push(items[j].getAttribute('data-token'));
        }
        document.getElementById('pageOrder').value = order.join(',');
        document.getElementById('removePages').value = removed.join(',');

        var save = document.getElementById('btnSaveComic');
        if (save) {
            save.disabled = true;
            save.dataset.originalText = save.innerHTML;
            save.innerHTML = '⏳ Đang tải ảnh lên…';
            setTimeout(function () {
                save.disabled = false;
                if (save.dataset.originalText) save.innerHTML = save.dataset.originalText;
            }, 15000);
        }
    });

    renumber();
})();
</script>
