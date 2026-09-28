<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  parts/head.jsp — nội dung thẻ <head>, dùng chung cho MỌI layout.
--%>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${empty pageTitle ? 'ĐọcTruyện — Nền tảng đọc truyện trực tuyến' : pageTitle}"/></title>

<%-- SEO & OpenGraph Meta Tags (ISSUE-012) --%>
<c:set var="_metaDesc" value="${not empty pageDescription ? pageDescription : (not empty story.description ? story.description : 'ĐọcTruyện — Nền tảng đọc và sáng tác truyện online miễn phí với hàng ngàn tác phẩm hấp dẫn.')}" />
<meta name="description" content="<c:out value='${_metaDesc}'/>">

<meta property="og:site_name" content="ĐọcTruyện">
<meta property="og:title" content="<c:out value='${empty pageTitle ? \"ĐọcTruyện\" : pageTitle}'/>">
<meta property="og:description" content="<c:out value='${_metaDesc}'/>">
<meta property="og:type" content="${not empty story ? 'book' : 'website'}">
<c:if test="${not empty story.coverUrl}">
    <c:set var="_ogImg" value="${fn:startsWith(story.coverUrl, '/') ? pageContext.request.contextPath.concat(story.coverUrl) : story.coverUrl}"/>
    <meta property="og:image" content="<c:out value='${_ogImg}'/>">
</c:if>

<%-- PWA Web App Manifest & Service Worker (ISSUE-010) --%>
<meta name="theme-color" content="#f97316">
<link rel="manifest" href="${pageContext.request.contextPath}/manifest.json">
<link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/app-icon.svg">
<script>
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', function () {
            navigator.serviceWorker.register('${pageContext.request.contextPath}/sw.js', { scope: '${pageContext.request.contextPath}/' }).catch(function () {});
        });
    }
</script>

<%-- Chống nháy sáng/tối (FOUC): đọc theme đã lưu trước khi CSS vẽ màn hình --%>
<script>
    (function () {
        try {
            var theme = localStorage.getItem('site_theme');
            if (theme === 'light') {
                document.documentElement.setAttribute('data-site-theme', 'light');
            }
        } catch (e) {}
    })();
</script>

<%-- contextPath: đổi tên lúc deploy vẫn chạy, không cần sửa link --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css?v=2.6">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css?v=2.6">
<c:if test="${not empty layoutCss}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${layoutCss}.css?v=2.6">
</c:if>
<c:if test="${not empty pageCss}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/${pageCss}.css?v=2.6">
</c:if>

<c:if test="${recaptchaEnabled}">
    <script>
        window.RECAPTCHA_SITE_KEY = '<c:out value="${recaptchaSiteKey}"/>';
    </script>
    <script src="https://www.google.com/recaptcha/api.js?render=<c:out value='${recaptchaSiteKey}'/>" async defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/recaptcha.js" defer></script>
</c:if>

<%--
  Hỗ trợ form báo cáo vi phạm (ISSUE-025): đếm ảnh, kiểm dung lượng, nhắc mô
  tả khi chọn "Khác". Chỉ là TIỆN NGHI — hàng rào thật nằm ở ReportServlet và
  @MultipartConfig. Tắt JS thì form vẫn gửi được và vẫn bị máy chủ chặn đúng.

  defer: file nhỏ, không chặn vẽ trang, và nó gắn listener ở cấp document nên
  không cần chờ phần tử nào có sẵn.
--%>
<script src="${pageContext.request.contextPath}/assets/js/report-form.js?v=2.6" defer></script>
