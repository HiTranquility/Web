<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _comment.jsp — MẢNH TÁI DÙNG: một bình luận
================================================================================
  TẦNG: views/

  DÙNG Ở: story/detail.jsp · admin/comments.jsp

  BIẾN CẦN CÓ:
      cm          Comment  bình luận cần hiện (thường từ <c:forEach var="cm">)
      cmStoryId   int      id truyện, để nút gỡ biết quay về đâu

  QUYỀN GỠ BÌNH LUẬN — kiểm ở HAI nơi, không phải một:
    Ở đây (JSP) chỉ để ẨN/HIỆN nút cho gọn mắt.
    CommentServlet mới là chỗ kiểm thật. Ai đó tự gửi request POST không qua
    giao diện thì servlet vẫn chặn được.
    Kiểm ở JSP mà không kiểm ở servlet = không kiểm gì cả.
================================================================================
--%>
<div class="comment">
    <span class="user-avatar">${cm.initial}</span>

    <div class="comment-body">
        <div class="comment-head">
            <b><c:out value="${cm.name}"/></b>

            <%-- Chủ bình luận hoặc admin mới thấy nút gỡ --%>
            <c:if test="${not empty currentUser
                          and (currentUser.id eq cm.userId or currentUser.admin)}">
                <form action="${pageContext.request.contextPath}/comment" method="post"
                      style="display:inline">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${cm.id}">
                    <input type="hidden" name="storyId" value="${cmStoryId}">
                    <button type="submit" class="link-danger">gỡ</button>
                </form>
            </c:if>
        </div>

        <%-- <c:out> BẮT BUỘC — bình luận là chữ người dùng nhập --%>
        <p><c:out value="${cm.content}"/></p>

        <%--
          Báo cáo bình luận vi phạm.

          Không hiện với chính chủ bình luận (tự báo cáo mình thì vô nghĩa)
          và không hiện với admin (admin đã có nút Ẩn ở khu quản trị, mạnh
          hơn hẳn việc gửi báo cáo cho chính mình xử lý).
        --%>
        <c:if test="${not empty currentUser
                      and currentUser.id ne cm.userId
                      and not currentUser.admin}">
            <details class="report-box report-inline">
                <summary>⚠ báo cáo</summary>
                <form method="post" action="${pageContext.request.contextPath}/report">
                    <input type="hidden" name="targetType" value="COMMENT">
                    <input type="hidden" name="targetId" value="${cm.id}">
                    <input type="hidden" name="storyId" value="${cmStoryId}">
                    <input type="text" name="reason" maxlength="500"
                           class="inline-input" placeholder="Lý do…">
                    <button type="submit" class="btn btn-danger btn-sm">Gửi</button>
                </form>
            </details>
        </c:if>
    </div>
</div>
