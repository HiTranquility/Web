<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  chapter/form.jsp — MẢNH nội dung. Thêm / sửa chương.             CASE 06
  Nhận: chapter · story · message
--%>
<div class="editor-header-meta">
    <h1>${empty chapter.id or chapter.id eq 0 ? 'Thêm chương mới' : 'Sửa chương'}</h1>
    <p class="muted-note">
        Truyện: <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}" style="color:var(--ember); font-weight:600;"><c:out value="${story.title}"/></a>
    </p>
</div>

<c:if test="${not empty message}">
    <p class="form-error"><c:out value="${message}"/></p>
</c:if>

<%-- Thông báo khôi phục bản nháp nếu có --%>
<div id="draftAlert" class="draft-restore-alert">
    <span>💡 Phát hiện bản nháp chưa lưu gần nhất (<strong id="draftTime">vừa xong</strong>). Bạn có muốn khôi phục không?</span>
    <div style="display:flex; gap:8px;">
        <button type="button" id="btnRestoreDraft" class="btn btn-sm btn-primary">Khôi phục</button>
        <button type="button" id="btnDiscardDraft" class="btn btn-sm btn-ghost">Bỏ qua</button>
    </div>
</div>

<form id="chapterForm" action="${pageContext.request.contextPath}/chapter" method="post" class="wide-form">
    <input type="hidden" name="_csrf" value="${csrfToken}">
    <input type="hidden" name="action"
           value="${empty chapter.id or chapter.id eq 0 ? 'create' : 'edit'}">
    <input type="hidden" name="id" id="chapterId" value="${chapter.id}">
    <input type="hidden" name="storyId" id="storyId" value="${story.id}">

    <div class="form-row">
        <div style="max-width:11em">
            <label for="chapterNo">Số chương *</label>
            <%-- Servlet đã điền sẵn số kế tiếp (MAX + 1) khi thêm mới --%>
            <input type="number" id="chapterNo" name="chapterNo" min="1" required
                   value="${chapter.chapterNo}">
        </div>
        <div style="flex:1" class="field-with-counter">
            <label for="title">Tiêu đề chương *</label>
            <input type="text" id="title" name="title" maxlength="200" required
                   placeholder="Ví dụ: Khởi đầu một hành trình mới..."
                   value="<c:out value='${chapter.title}'/>">
            <span id="titleCounter" class="char-counter">0 / 200 ký tự</span>
        </div>
    </div>

    <%-- CHƯƠNG VIP VÀ GIÁ XU (ISSUE-020) --%>
    <div class="vip-setting-card">
        <label style="display:flex; align-items:center; gap:8px; cursor:pointer; font-weight:600; margin-bottom:0">
            <input type="checkbox" id="isVipCheckbox" name="isVip" value="1" ${chapter.vip ? 'checked' : ''} onchange="document.getElementById('coinPriceWrapper').style.display = this.checked ? 'flex' : 'none';">
            <span>🔒 Đặt làm Chương VIP (Có tính phí xu để mở khoá)</span>
        </label>
        <div id="coinPriceWrapper" style="display:${chapter.vip ? 'flex' : 'none'}; align-items:center; gap:8px;">
            <label for="coinPrice" style="margin-bottom:0; font-size:0.9rem">Giá mở khoá (Xu):</label>
            <input type="number" id="coinPrice" name="coinPrice" min="1" max="10000" style="width:7em; padding:6px 10px;" value="${chapter.coinPrice > 0 ? chapter.coinPrice : 10}">
        </div>
    </div>

    <div style="margin-bottom: 6px; display:flex; justify-content:space-between; align-items:baseline;">
        <label for="content" style="margin-bottom:0">Nội dung chương *</label>
        <small style="color:var(--text-mut);">Phím tắt: <strong>Ctrl+B</strong> (Đậm), <strong>Ctrl+I</strong> (Nghiêng), <strong>Ctrl+S</strong> (Lưu nhanh)</small>
    </div>

    <%-- KHÔNG GIAN SOẠN THẢO VĂN HỌC & TOOLBAR --%>
    <div class="editor-workspace" id="editorWorkspace">
        <%-- THANH CÔNG CỤ TÁC GIẢ --%>
        <div class="editor-toolbar">
            <div class="toolbar-group">
                <%-- Tabs chuyển chế độ --%>
                <div class="mode-tabs">
                    <button type="button" class="mode-tab active" id="tabWrite">✏️ Soạn thảo</button>
                    <button type="button" class="mode-tab" id="tabPreview">👁️ Xem trước</button>
                </div>

                <span class="toolbar-sep"></span>

                <%-- Định dạng cơ bản --%>
                <button type="button" class="tool-btn" id="toolBold" title="In đậm (Ctrl+B)">
                    <strong>B</strong>
                </button>
                <button type="button" class="tool-btn" id="toolItalic" title="In nghiêng (Ctrl+I)">
                    <em>I</em>
                </button>
                <button type="button" class="tool-btn" id="toolStrike" title="Gạch ngang">
                    <s>S</s>
                </button>

                <span class="toolbar-sep"></span>

                <%-- Tiện ích văn học chuyên nghiệp --%>
                <button type="button" class="tool-btn" id="toolDialogue" title="Thêm gạch đầu dòng thoại chuẩn văn học (—)">
                    <span>—</span> <span class="btn-label">Thoại</span>
                </button>
                <button type="button" class="tool-btn" id="toolDivider" title="Chèn ký hiệu hoa thị phân đoạn (* * *)">
                    <span>❖</span> <span class="btn-label">Phân đoạn</span>
                </button>
                <button type="button" class="tool-btn" id="toolQuote" title="Trích dẫn / Suy nghĩ nhân vật">
                    <span>❝</span> <span class="btn-label">Trích dẫn</span>
                </button>
                <button type="button" class="tool-btn" id="toolAuthorNote" title="Khối lời nhắn của tác giả">
                    <span>📝</span> <span class="btn-label">Lời nhắn</span>
                </button>

                <span class="toolbar-sep"></span>

                <%-- Chuẩn hoá & dọn dẹp văn bản --%>
                <button type="button" class="tool-btn" id="toolClean" title="Xóa khoảng trắng thừa và chuẩn hóa dòng trống">
                    <span>🧹</span> <span class="btn-label">Làm sạch</span>
                </button>
                <button type="button" class="tool-btn" id="toolIndent" title="Thụt đầu dòng 2 khoảng trắng cho từng đoạn">
                    <span>⇥</span> <span class="btn-label">Thụt lề</span>
                </button>
            </div>

            <div class="toolbar-group" style="margin-left:auto;">
                <button type="button" class="tool-btn" id="toolZen" title="Chế độ toàn màn hình không phân tâm (Zen Mode)">
                    <span>⛶</span> <span class="btn-label">Tập trung</span>
                </button>
            </div>
        </div>

        <%-- VÙNG NHẬP LIỆU NỘI DUNG --%>
        <textarea id="content" name="content" rows="22" required
                  class="content-editor"
                  placeholder="Bắt đầu viết nội dung chương tại đây... Xuống dòng để tách đoạn văn."><c:out value="${chapter.content}"/></textarea>

        <%-- VÙNG XEM TRƯỚC TỨC THÌ (READER PREVIEW) --%>
        <div id="editorPreview" class="editor-preview"></div>

        <%-- THANH THỐNG KÊ THỜI GIAN THỰC Ở CHÂN SOẠN THẢO --%>
        <div class="editor-stats-bar">
            <div class="stats-items">
                <span class="stat-pill">Từ: <strong id="statWords">0</strong></span>
                <span class="stat-pill">Ký tự: <strong id="statChars">0</strong></span>
                <span class="stat-pill">Đoạn: <strong id="statParas">0</strong></span>
                <span class="stat-pill">Thời gian đọc: <strong id="statTime">~0 phút</strong></span>
                <span id="statBadge" class="stat-badge badge-short">Mới bắt đầu</span>
            </div>
            <div class="draft-status" id="draftStatus">
                <span>🕒 Chưa lưu bản nháp</span>
            </div>
        </div>
    </div>

    <div class="form-actions" style="margin-top:20px;">
        <button type="submit" id="btnSubmitForm" class="btn btn-primary">
            <span>💾 Lưu chương</span>
        </button>
        <a class="btn btn-ghost"
           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">Huỷ</a>

        <c:if test="${not empty chapter.id and chapter.id ne 0}">
            <%-- Chương xoá THẬT (khác truyện) nên phải cảnh báo rõ. Dùng POST + token CSRF để bảo mật. --%>
            <button type="submit" form="deleteChapterForm" class="btn btn-danger" style="margin-left:auto"
                    onclick="return confirm('Xoá hẳn chương này? Toàn bộ nội dung chương sẽ bị xoá vĩnh viễn và không thể khôi phục.')">
                Xoá chương</button>
        </c:if>
    </div>
</form>

<c:if test="${not empty chapter.id and chapter.id ne 0}">
    <form id="deleteChapterForm" action="${pageContext.request.contextPath}/chapter" method="post" style="display:none">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${chapter.id}">
    </form>
</c:if>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var titleInput = document.getElementById('title');
    var titleCounter = document.getElementById('titleCounter');
    var chapterNoInput = document.getElementById('chapterNo');
    var contentArea = document.getElementById('content');
    var previewPane = document.getElementById('editorPreview');
    var tabWrite = document.getElementById('tabWrite');
    var tabPreview = document.getElementById('tabPreview');
    var toolZen = document.getElementById('toolZen');
    var form = document.getElementById('chapterForm');
    var draftStatus = document.getElementById('draftStatus');
    var draftAlert = document.getElementById('draftAlert');
    var draftTime = document.getElementById('draftTime');
    var btnRestoreDraft = document.getElementById('btnRestoreDraft');
    var btnDiscardDraft = document.getElementById('btnDiscardDraft');

    var storyId = document.getElementById('storyId') ? document.getElementById('storyId').value : '0';
    var chapterId = document.getElementById('chapterId') ? document.getElementById('chapterId').value : '0';
    var draftStorageKey = 'truyen_chapter_draft_' + storyId + '_' + chapterId;

    // 1. CẬP NHẬT BỘ ĐẾM KÝ TỰ TIÊU ĐỀ
    function updateTitleCounter() {
        if (!titleInput || !titleCounter) return;
        var len = titleInput.value.length;
        titleCounter.textContent = len + ' / 200 ký tự';
        if (len >= 200) {
            titleCounter.className = 'char-counter limit-full';
        } else if (len >= 180) {
            titleCounter.className = 'char-counter limit-near';
        } else {
            titleCounter.className = 'char-counter';
        }
    }
    if (titleInput) {
        titleInput.addEventListener('input', updateTitleCounter);
        updateTitleCounter();
    }

    // 2. TÍNH TOÁN THỐNG KÊ THỜI GIAN THỰC (Words, Chars, Paras, ReadTime)
    var statWords = document.getElementById('statWords');
    var statChars = document.getElementById('statChars');
    var statParas = document.getElementById('statParas');
    var statTime = document.getElementById('statTime');
    var statBadge = document.getElementById('statBadge');

    function updateStats() {
        if (!contentArea) return;
        var text = contentArea.value;
        var charCount = text.length;

        // Đếm từ
        var trimmed = text.trim();
        var wordCount = 0;
        if (trimmed.length > 0) {
            var words = trimmed.match(/\S+/g);
            wordCount = words ? words.length : 0;
        }

        // Đếm đoạn văn (dòng không rỗng)
        var lines = text.split(/\r?\n/);
        var paraCount = 0;
        for (var i = 0; i < lines.length; i++) {
            if (lines[i].trim().length > 0) {
                paraCount++;
            }
        }

        // Thời gian đọc ước tính: trung bình 200 từ/phút
        var minutes = Math.max(1, Math.round(wordCount / 200));
        if (wordCount === 0) minutes = 0;

        if (statWords) statWords.textContent = wordCount.toLocaleString('vi-VN');
        if (statChars) statChars.textContent = charCount.toLocaleString('vi-VN');
        if (statParas) statParas.textContent = paraCount.toLocaleString('vi-VN');
        if (statTime) statTime.textContent = '~' + minutes + ' phút đọc';

        if (statBadge) {
            if (wordCount === 0) {
                statBadge.className = 'stat-badge badge-short';
                statBadge.textContent = 'Mới bắt đầu';
            } else if (wordCount < 600) {
                statBadge.className = 'stat-badge badge-short';
                statBadge.textContent = 'Chương ngắn (< 600 từ)';
            } else if (wordCount <= 2500) {
                statBadge.className = 'stat-badge badge-medium';
                statBadge.textContent = 'Độ dài lý tưởng (600 - 2.500 từ)';
            } else {
                statBadge.className = 'stat-badge badge-long';
                statBadge.textContent = 'Chương dài (> 2.500 từ)';
            }
        }
    }
    if (contentArea) {
        contentArea.addEventListener('input', updateStats);
        updateStats();
    }

    // 3. XỬ LÝ CHÈN KÝ TỰ / FORMAT TẠI VỊ TRÍ CON TRỎ (CURSOR INSERTION)
    function wrapOrInsert(before, after, defaultText) {
        contentArea.focus();
        var start = contentArea.selectionStart;
        var end = contentArea.selectionEnd;
        var selected = contentArea.value.substring(start, end);
        var replacement = '';

        if (selected.length > 0) {
            replacement = before + selected + after;
        } else {
            replacement = before + defaultText + after;
        }

        contentArea.setRangeText(replacement, start, end, 'end');
        updateStats();
        triggerAutoSave();
    }

    // Nút Bold (Ctrl+B)
    var btnBold = document.getElementById('toolBold');
    if (btnBold) {
        btnBold.addEventListener('click', function () { wrapOrInsert('**', '**', 'văn bản in đậm'); });
    }

    // Nút Italic (Ctrl+I)
    var btnItalic = document.getElementById('toolItalic');
    if (btnItalic) {
        btnItalic.addEventListener('click', function () { wrapOrInsert('*', '*', 'văn bản in nghiêng'); });
    }

    // Nút Strikethrough
    var btnStrike = document.getElementById('toolStrike');
    if (btnStrike) {
        btnStrike.addEventListener('click', function () { wrapOrInsert('~~', '~~', 'văn bản gạch ngang'); });
    }

    // Nút Lời thoại (Thêm "— ")
    var btnDialogue = document.getElementById('toolDialogue');
    if (btnDialogue) {
        btnDialogue.addEventListener('click', function () {
            contentArea.focus();
            var start = contentArea.selectionStart;
            var end = contentArea.selectionEnd;
            var selected = contentArea.value.substring(start, end);

            if (selected.length > 0) {
                var lines = selected.split(/\r?\n/);
                var transformed = lines.map(function(line) {
                    var trimmed = line.trimStart();
                    if (trimmed.startsWith('—') || trimmed.startsWith('-')) {
                        return line;
                    }
                    return '— ' + trimmed;
                }).join('\n');
                contentArea.setRangeText(transformed, start, end, 'end');
            } else {
                wrapOrInsert('— ', '', 'Lời thoại nhân vật...');
            }
            updateStats();
            triggerAutoSave();
        });
    }

    // Nút Hoa thị phân đoạn (* * *)
    var btnDivider = document.getElementById('toolDivider');
    if (btnDivider) {
        btnDivider.addEventListener('click', function () {
            wrapOrInsert('\n\n* * *\n\n', '', '');
        });
    }

    // Nút Trích dẫn (>)
    var btnQuote = document.getElementById('toolQuote');
    if (btnQuote) {
        btnQuote.addEventListener('click', function () {
            wrapOrInsert('\n> "', '"\n', 'Nội dung trích dẫn hoặc độc thoại nội tâm...');
        });
    }

    // Nút Lời nhắn tác giả
    var btnAuthorNote = document.getElementById('toolAuthorNote');
    if (btnAuthorNote) {
        btnAuthorNote.addEventListener('click', function () {
            wrapOrInsert('\n\n[Lời tác giả: ', ']\n\n', 'Cảm ơn bạn đọc đã theo dõi chương này!');
        });
    }

    // Nút Làm sạch văn bản (Xóa khoảng trắng thừa)
    var btnClean = document.getElementById('toolClean');
    if (btnClean) {
        btnClean.addEventListener('click', function () {
            var text = contentArea.value;
            if (!text.trim()) return;
            var lines = text.split(/\r?\n/);
            var cleanedLines = [];
            var lastWasEmpty = false;

            for (var i = 0; i < lines.length; i++) {
                var clean = lines[i].trim();
                if (clean.length === 0) {
                    if (!lastWasEmpty) {
                        cleanedLines.push('');
                        lastWasEmpty = true;
                    }
                } else {
                    cleanedLines.push(clean);
                    lastWasEmpty = false;
                }
            }
            contentArea.value = cleanedLines.join('\n');
            updateStats();
            triggerAutoSave();
            alert('✓ Đã dọn dẹp khoảng trắng thừa và định dạng chuẩn các dòng văn bản!');
        });
    }

    // Nút Thụt lề đầu dòng (Indent)
    var btnIndent = document.getElementById('toolIndent');
    if (btnIndent) {
        btnIndent.addEventListener('click', function () {
            var text = contentArea.value;
            if (!text.trim()) return;
            var lines = text.split(/\r?\n/);
            var indented = lines.map(function(line) {
                var trimmed = line.trimStart();
                if (trimmed.length > 0 && !trimmed.startsWith('—') && !trimmed.startsWith('* * *') && !trimmed.startsWith('>')) {
                    return '    ' + trimmed;
                }
                return line;
            }).join('\n');
            contentArea.value = indented;
            updateStats();
            triggerAutoSave();
        });
    }

    // 4. CHUYỂN ĐỔI CHẾ ĐỘ SOẠN THẢO VÀ XEM TRƯỚC (PREVIEW)
    function renderPreview() {
        if (!previewPane) return;
        var titleText = titleInput ? titleInput.value.trim() : '';
        var chapterNo = chapterNoInput ? chapterNoInput.value : '';
        var rawText = contentArea.value;

        var html = '<div class="preview-meta">'
                 + '<h2>' + (titleText ? escapeHtml(titleText) : 'Chưa có tiêu đề') + '</h2>'
                 + '<p>Chương ' + (chapterNo || '?') + ' &middot; Chế độ xem trước của người đọc</p>'
                 + '</div>';

        var lines = rawText.split(/\r?\n/);
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i].trim();
            if (!line) continue;

            if (line === '* * *' || line === '❖ ❖ ❖' || line === '---') {
                html += '<div class="divider">❖ ❖ ❖</div>';
            } else if (line.startsWith('>')) {
                html += '<blockquote>' + escapeHtml(line.replace(/^>\s*/, '')) + '</blockquote>';
            } else if (line.startsWith('—') || line.startsWith('- ')) {
                html += '<p class="dialogue">' + escapeHtml(line) + '</p>';
            } else {
                html += '<p>' + escapeHtml(line) + '</p>';
            }
        }

        previewPane.innerHTML = html;
    }

    function escapeHtml(str) {
        return str.replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;')
                  .replace(/'/g, '&#039;');
    }

    if (tabWrite && tabPreview && contentArea && previewPane) {
        tabWrite.addEventListener('click', function () {
            tabWrite.classList.add('active');
            tabPreview.classList.remove('active');
            contentArea.style.display = 'block';
            previewPane.classList.remove('active');
            contentArea.focus();
        });

        tabPreview.addEventListener('click', function () {
            tabPreview.classList.add('active');
            tabWrite.classList.remove('active');
            renderPreview();
            contentArea.style.display = 'none';
            previewPane.classList.add('active');
        });
    }

    // 5. CHẾ ĐỘ TOÀN MÀN HÌNH TẬP TRUNG (ZEN MODE)
    if (toolZen) {
        toolZen.addEventListener('click', function () {
            document.body.classList.toggle('zen-active');
            var isZen = document.body.classList.contains('zen-active');
            toolZen.classList.toggle('active', isZen);
            if (isZen) {
                toolZen.querySelector('.btn-label').textContent = 'Thoát Zen';
            } else {
                toolZen.querySelector('.btn-label').textContent = 'Tập trung';
            }
        });
    }

    // Phím Esc thoát Zen Mode
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && document.body.classList.contains('zen-active')) {
            document.body.classList.remove('zen-active');
            if (toolZen) {
                toolZen.classList.remove('active');
                toolZen.querySelector('.btn-label').textContent = 'Tập trung';
            }
        }
    });

    // 6. PHÍM TẮT: Ctrl+B, Ctrl+I, Ctrl+S
    document.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && !e.shiftKey && !e.altKey) {
            if (e.key === 'b' || e.key === 'B') {
                e.preventDefault();
                wrapOrInsert('**', '**', 'văn bản in đậm');
            } else if (e.key === 'i' || e.key === 'I') {
                e.preventDefault();
                wrapOrInsert('*', '*', 'văn bản in nghiêng');
            } else if (e.key === 's' || e.key === 'S') {
                e.preventDefault();
                // Lưu form ngay
                if (validateForm()) {
                    clearDraft();
                    form.submit();
                }
            }
        }
    });

    // 7. CƠ CHẾ TỰ ĐỘNG LƯU NHÁP (LOCALSTORAGE AUTO-SAVE)
    var autoSaveTimer = null;
    function triggerAutoSave() {
        if (!contentArea) return;
        clearTimeout(autoSaveTimer);
        autoSaveTimer = setTimeout(function () {
            var draftData = {
                title: titleInput ? titleInput.value : '',
                content: contentArea.value,
                time: new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
            };
            try {
                localStorage.setItem(draftStorageKey, JSON.stringify(draftData));
                if (draftStatus) {
                    draftStatus.className = 'draft-status saved';
                    draftStatus.innerHTML = '<span>✓ Đã lưu nháp tự động lúc ' + draftData.time + '</span>';
                }
            } catch (err) {
                console.warn('Không thể lưu nháp vào localStorage', err);
            }
        }, 1500);
    }

    if (contentArea) {
        contentArea.addEventListener('input', triggerAutoSave);
    }
    if (titleInput) {
        titleInput.addEventListener('input', triggerAutoSave);
    }

    // Kiểm tra bản nháp cũ khi mở trang
    try {
        var savedRaw = localStorage.getItem(draftStorageKey);
        if (savedRaw) {
            var saved = JSON.parse(savedRaw);
            var currentContent = contentArea ? contentArea.value : '';
            // Nếu bản nháp khác với nội dung hiện tại và có chữ
            if (saved.content && saved.content.trim() !== currentContent.trim()) {
                if (draftAlert && draftTime) {
                    draftTime.textContent = saved.time || 'gần đây';
                    draftAlert.classList.add('visible');
                }
            }
        }
    } catch (e) {}

    if (btnRestoreDraft) {
        btnRestoreDraft.addEventListener('click', function () {
            try {
                var savedRaw = localStorage.getItem(draftStorageKey);
                if (savedRaw) {
                    var saved = JSON.parse(savedRaw);
                    if (saved.content) contentArea.value = saved.content;
                    if (saved.title && titleInput && !titleInput.value) titleInput.value = saved.title;
                    updateStats();
                    updateTitleCounter();
                    if (draftAlert) draftAlert.classList.remove('visible');
                    alert('✓ Đã khôi phục thành công bản nháp lúc ' + (saved.time || ''));
                }
            } catch (e) {}
        });
    }

    if (btnDiscardDraft) {
        btnDiscardDraft.addEventListener('click', function () {
            clearDraft();
            if (draftAlert) draftAlert.classList.remove('visible');
        });
    }

    function clearDraft() {
        try {
            localStorage.removeItem(draftStorageKey);
        } catch (e) {}
    }

    // 8. CLIENT-SIDE FORM VALIDATION
    function validateForm() {
        if (!titleInput || !contentArea) return true;
        var t = titleInput.value.trim();
        var c = contentArea.value.trim();
        var no = chapterNoInput ? parseInt(chapterNoInput.value, 10) : 1;

        if (!no || no <= 0) {
            alert('⚠️ Số thứ tự chương phải là số nguyên dương lớn hơn 0.');
            if (chapterNoInput) chapterNoInput.focus();
            return false;
        }

        if (t.length === 0) {
            alert('⚠️ Vui lòng nhập tiêu đề chương (không được để trống hoặc chỉ chứa khoảng trắng).');
            titleInput.focus();
            return false;
        }

        if (t.length > 200) {
            alert('⚠️ Tiêu đề chương không được vượt quá 200 ký tự.');
            titleInput.focus();
            return false;
        }

        if (c.length === 0) {
            alert('⚠️ Vui lòng nhập nội dung chương (không được để trống).');
            if (tabWrite && !tabWrite.classList.contains('active')) {
                tabWrite.click();
            }
            contentArea.focus();
            return false;
        }

        return true;
    }

    if (form) {
        form.addEventListener('submit', function (e) {
            if (!validateForm()) {
                e.preventDefault();
            } else {
                clearDraft();
            }
        });
    }
});
</script>

