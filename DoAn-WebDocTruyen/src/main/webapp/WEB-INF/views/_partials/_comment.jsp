<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
================================================================================
  _comment.jsp — MẢNH TÁI DÙNG: một bình luận và các trả lời của nó
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

  VÌ SAO KHÔNG DÙNG ĐỆ QUY ĐỂ VẼ CÂY
    Cách "tổng quát" là để mảnh này tự include chính nó cho mỗi cấp con. Nhưng
    <%@ include %> là include TĨNH — nó dán nội dung file vào lúc biên dịch,
    nên tự include chính mình là vòng lặp vô hạn, JSP không dịch nổi.

    Mà kể cả làm được thì cũng không nên: cây chỉ sâu ĐÚNG HAI CẤP (xem
    CommentDAO.insert), nên một vòng lặp lồng một vòng lặp là đủ và đọc ra
    ngay được hình dạng cuối cùng.
================================================================================
--%>
<div class="comment">
    <span class="user-avatar">${cm.initial}</span>

    <div class="comment-body">
        <div class="comment-head">
            <b><c:out value="${cm.name}"/></b>

            <c:if test="${not empty cm.createdAt}">
                <span class="comment-time">
                    ${cm.createdAt.dayOfMonth}/${cm.createdAt.monthValue}
                </span>
            </c:if>

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

        <div class="comment-tools">
            <%--
              Nút trả lời là thẻ <details>, không phải JavaScript.

              Form trả lời nằm sẵn trong HTML nhưng bị gấp lại; bấm vào là
              trình duyệt tự mở. Không cần một dòng script nào, và vẫn chạy
              đúng khi người dùng tắt JavaScript.
            --%>
            <c:if test="${not empty currentUser}">
                <details class="reply-box">
                    <summary>↩ Trả lời</summary>
                    <form action="${pageContext.request.contextPath}/comment"
                          method="post" class="reply-form">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="storyId" value="${cmStoryId}">

                        <%-- parentId: khoá nối cả chuỗi trả lời về đúng bình
                             luận gốc này. DAO còn ép lại một lần nữa. --%>
                        <input type="hidden" name="parentId" value="${cm.id}">

                        <textarea name="content" rows="2" maxlength="1000" required
                                  placeholder="Trả lời <c:out value='${cm.name}'/>…"></textarea>
                        <button type="submit" class="btn btn-primary btn-sm">Gửi</button>
                    </form>
                </details>
            </c:if>

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

        <%--
          ---- Các trả lời ----
          Thụt vào và có vạch dọc bên trái để mắt thấy ngay đây là nhánh con.
          Trả lời xếp từ CŨ tới MỚI, ngược với bình luận gốc — đọc từ trên
          xuống là đúng mạch cuộc đối thoại.
        --%>
        <c:if test="${not empty cm.replies}">
            <div class="reply-list">
                <c:forEach var="rp" items="${cm.replies}">
                    <div class="comment comment-reply">
                        <span class="user-avatar">${rp.initial}</span>
                        <div class="comment-body">
                            <div class="comment-head">
                                <b><c:out value="${rp.name}"/></b>

                                <%-- Tác giả truyện trả lời độc giả là chuyện
                                     đáng chú ý — đánh dấu để không lẫn. --%>
                                <c:if test="${not empty story and rp.userId eq story.authorId}">
                                    <span class="pill pill-warn">Tác giả</span>
                                </c:if>

                                <c:if test="${not empty rp.createdAt}">
                                    <span class="comment-time">
                                        ${rp.createdAt.dayOfMonth}/${rp.createdAt.monthValue}
                                    </span>
                                </c:if>

                                <c:if test="${not empty currentUser
                                              and (currentUser.id eq rp.userId or currentUser.admin)}">
                                    <form action="${pageContext.request.contextPath}/comment"
                                          method="post" style="display:inline">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${rp.id}">
                                        <input type="hidden" name="storyId" value="${cmStoryId}">
                                        <button type="submit" class="link-danger">gỡ</button>
                                    </form>
                                </c:if>
                            </div>

                            <p><c:out value="${rp.content}"/></p>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:if>
    </div>
</div>
