/**
 * report-form.js — hỗ trợ form báo cáo vi phạm (ISSUE-025).
 *
 * Làm đúng hai việc, cả hai đều là TIỆN NGHI, không phải bảo mật:
 *   1. Báo ngay khi người dùng chọn quá 3 ảnh hoặc ảnh quá 2 MB.
 *   2. Nhắc điền mô tả khi chọn loại "Khác".
 *
 * HÀNG RÀO THẬT NẰM Ở MÁY CHỦ — ReportServlet đếm lại số ảnh, UploadUtil kiểm
 * lại dung lượng và đọc byte đầu file để biết có đúng là ảnh không, và
 * @MultipartConfig là chốt chặn cứng của container. Kiểm ở đây chỉ để người
 * dùng biết sớm thay vì bấm Gửi rồi mới bị đá về.
 *
 * Tắt JavaScript thì form vẫn gửi được và vẫn bị máy chủ chặn đúng như vậy.
 */
(function () {
    'use strict';

    var MAX_FILES = 3;
    var MAX_BYTES = 2 * 1024 * 1024;

    function sizeLabel(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return Math.round(bytes / 1024) + ' KB';
        return (bytes / 1024 / 1024).toFixed(1) + ' MB';
    }

    function findHint(input) {
        // Ô gợi ý nằm cạnh trong cùng một .report-field; form gọn ở bình luận
        // thì không có ô nào — lúc đó dùng alert của trình duyệt là đủ.
        var field = input.closest('.report-field');
        return field ? field.querySelector('.report-file-hint') : null;
    }

    function say(input, message, isError) {
        var hint = findHint(input);
        if (hint) {
            hint.textContent = message;
            hint.classList.toggle('is-error', !!isError);
        } else if (isError && message) {
            alert(message);
        }
    }

    document.addEventListener('change', function (e) {
        var input = e.target;
        if (!input.classList || !input.classList.contains('report-file-input')) return;

        var max = parseInt(input.getAttribute('data-max'), 10) || MAX_FILES;
        var files = input.files;

        if (!files || files.length === 0) {
            say(input, '', false);
            return;
        }

        if (files.length > max) {
            say(input, 'Chỉ được tối đa ' + max + ' ảnh — bạn đang chọn ' + files.length + '.', true);
            input.value = '';   // bỏ chọn, buộc chọn lại cho đúng
            return;
        }

        var total = 0;
        for (var i = 0; i < files.length; i++) {
            if (files[i].size > MAX_BYTES) {
                say(input, 'Ảnh "' + files[i].name + '" nặng ' + sizeLabel(files[i].size)
                         + ', vượt giới hạn 2 MB.', true);
                input.value = '';
                return;
            }
            total += files[i].size;
        }

        say(input, 'Đã chọn ' + files.length + ' ảnh · ' + sizeLabel(total), false);
    });

    /* Chọn "Khác" thì mô tả thành bắt buộc — máy chủ cũng kiểm lại điều này. */
    document.addEventListener('change', function (e) {
        var sel = e.target;
        if (!sel.name || sel.name !== 'category' || sel.tagName !== 'SELECT') return;

        var form = sel.form;
        if (!form) return;
        var reason = form.querySelector('input[name="reason"]');
        if (!reason) return;

        var isOther = sel.value === 'OTHER';
        reason.required = isOther;
        reason.placeholder = isOther
            ? 'Bắt buộc: mô tả cụ thể vi phạm'
            : 'Mô tả thêm (không bắt buộc)';
    });
})();
