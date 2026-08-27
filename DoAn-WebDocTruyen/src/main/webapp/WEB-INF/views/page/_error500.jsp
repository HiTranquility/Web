<%@ page pageEncoding="UTF-8" %>
<%-- Mảnh nội dung trang 500. Nhận errType/errMsg do error500.jsp đặt hộ,
     vì pageContext.exception không xuyên qua được <jsp:include>. --%>
<div class="empty" style="margin-top:60px">
    <div class="empty-icon">⚠️</div>
    <h3>Hệ thống gặp sự cố</h3>
    <p>Đã có lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại,
       hoặc quay về trang chủ.</p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
</div>

<%-- XOÁ KHỐI NÀY TRƯỚC KHI NỘP: nó phơi tên lớp cho người dùng xem. --%>
<c:if test="${not empty errType}">
    <div class="panel" style="margin-top:24px">
        <h4>Chi tiết kỹ thuật (chỉ dùng lúc phát triển)</h4>
        <p style="color:var(--text-dim)">Loại: <code>${errType}</code></p>
        <p style="color:var(--text-dim)">Thông điệp: <c:out value="${errMsg}"/></p>
    </div>
</c:if>
