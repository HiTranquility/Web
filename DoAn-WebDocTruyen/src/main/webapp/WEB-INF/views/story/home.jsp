<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
================================================================================
  home.jsp — MẢNH NỘI DUNG của trang chủ
================================================================================
  Nhận từ HomeServlet:
      latest        List<Story>  truyện mới cập nhật
      popular       List<Story>  truyện xem nhiều
      totalStories  int
      dbError       String, khác null nếu chưa kết nối được database

  ĐÂY LÀ MẢNH, KHÔNG PHẢI TRANG HOÀN CHỈNH
    File này KHÔNG có <html>, <head>, <body> — layout/main.jsp lo phần đó.
    Nó chỉ chứa phần ruột, được chèn vào giữa khung bằng <jsp:include>.
    Mở thẳng file này bằng URL cũng không được: nó nằm trong WEB-INF.

  VÌ SAO DÙNG JSTL (thẻ <c:...>) Ở ĐÂY
    Chương 5 chỉ dùng EL thuần vì chưa học JSTL. Nhưng trang này phải LẶP qua
    danh sách truyện, mà EL thuần không lặp được — không có <c:forEach> thì
    buộc phải viết scriptlet <% for(...) %>, tức là nhét Java vào JSP, đúng cái
    MVC muốn tránh. JSTL là chương 9 của sách, dùng sớm hơn một chút là đáng.

  VÌ SAO MỌI CHỖ IN DỮ LIỆU ĐỀU LÀ <c:out> CHỨ KHÔNG PHẢI ${...}
    ${story.title} KHÔNG tự escape HTML. Người dùng đặt tên truyện là
    <script>...</script> thì đoạn đó chạy thật trên trình duyệt người khác —
    đó là lỗ hổng XSS mình đã chứng minh được ở dự án chương 5.
    <c:out> escape sẵn, nên mặc định là an toàn.
    QUY TẮC: dữ liệu do NGƯỜI DÙNG nhập -> luôn <c:out>.
================================================================================
--%>


    <%--
      HERO chỉ hiện với KHÁCH.

      Người đã đăng nhập không cần đọc lại lời giới thiệu web mỗi lần vào —
      họ đã biết đây là gì rồi. Chỗ quý nhất của trang, ngay đầu màn hình,
      nên dành cho thứ họ thật sự quay lại để làm: đọc tiếp truyện đang dở.
    --%>
    <c:if test="${empty currentUser}">
        <section class="hero">
            <h1>Đọc, viết và <em>chia sẻ</em> những câu chuyện</h1>
            <p>Kho truyện do cộng đồng đóng góp. Tìm theo thể loại bạn thích,
               đánh dấu để đọc tiếp, hoặc tự đăng truyện của riêng mình.</p>
        </section>
    </c:if>

    <%-- ---- BANNER TIÊU ĐIỂM (Featured Story Spotlight) ------------------- --%>
    <c:if test="${not empty popular}">
        <c:set var="featured" value="${popular[0]}"/>
        <section class="featured-showcase" aria-label="Truyện tiêu điểm">
            <div class="featured-card">
                <a class="featured-cover-box"
                   href="${pageContext.request.contextPath}/story?action=detail&amp;id=${featured.id}">
                    <c:set var="cvUrl"     value="${featured.coverUrl}"/>
                    <c:set var="cvAlt"     value="${featured.title}"/>
                    <c:set var="cvInitial" value="${featured.initial}"/>
                    <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                    <span class="badge badge-hot featured-badge">🔥 Tiêu điểm hôm nay</span>
                </a>

                <div class="featured-content">
                    <div class="featured-eyebrow">
                        <span>⭐ TRUYỆN ĐƯỢC ĐỌC NHIỀU NHẤT</span>
                        <c:choose>
                            <c:when test="${featured.completed}">
                                <span class="badge badge-done">Trọn bộ</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-going">Đang ra</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <h2 class="featured-title">
                        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${featured.id}">
                            <c:out value="${featured.title}"/>
                        </a>
                    </h2>

                    <div class="featured-meta">
                        <span>✍️ <b><c:out value="${featured.authorName}"/></b></span>
                        <span>📄 ${featured.chapterCount} chương</span>
                        <span>👁️ <fmt:formatNumber pattern="#,##0" value="${featured.viewCount}"/> lượt xem</span>
                        <c:if test="${featured.rated}">
                            <span>⭐ ${featured.ratingAvg} / 5.0</span>
                        </c:if>
                    </div>

                    <p class="featured-desc">
                        <c:choose>
                            <c:when test="${not empty featured.description}">
                                <c:out value="${featured.description}"/>
                            </c:when>
                            <c:otherwise>
                                Tác phẩm đang nhận được sự quan tâm và yêu thích nồng nhiệt từ đông đảo bạn đọc. Bấm để khám phá ngay!
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <div class="featured-actions">
                        <a class="btn btn-primary"
                           href="${pageContext.request.contextPath}/story?action=detail&amp;id=${featured.id}">
                            ▶ Đọc ngay
                        </a>
                        <a class="btn btn-ghost"
                           href="${pageContext.request.contextPath}/story?action=list&amp;sort=popular">
                            Khám phá bảng xếp hạng →
                        </a>
                    </div>
                </div>
            </div>
        </section>
    </c:if>

    <%-- ---- Đọc tiếp (chỉ người đã đăng nhập, và phải có truyện dở) ------- --%>
    <c:if test="${not empty resume}">
        <div class="section-head" style="margin-top:26px">
            <h2>📖 Đọc tiếp</h2>
            <a class="more" href="${pageContext.request.contextPath}/history">
                Lịch sử đọc →</a>
        </div>

        <div class="resume-row">
            <c:forEach var="r" items="${resume}">
                <%--
                  Cả ô là một link, không chỉ mỗi cái nút.

                  Đích đến là CHƯƠNG ĐANG DỞ, không phải trang chi tiết truyện.
                  Người bấm vào ô "Đọc tiếp" muốn đọc ngay, bắt họ ghé trang
                  giới thiệu rồi mới tìm nút đọc là thừa đúng một cú bấm — mà
                  cú bấm đó lặp lại mỗi lần vào web.
                --%>
                <a class="resume-card"
                   href="${pageContext.request.contextPath}/chapter?action=read&amp;id=${r.lastChapterId}">
                    <span class="resume-cover">
                        <c:set var="cvUrl"     value="${r.coverUrl}"/>
                        <c:set var="cvAlt"     value="${r.storyTitle}"/>
                        <c:set var="cvInitial" value="${r.initial}"/>
                        <%@ include file="/WEB-INF/views/_partials/_cover.jsp" %>
                    </span>

                    <span class="resume-info">
                        <b class="resume-title"><c:out value="${r.storyTitle}"/></b>
                        <span class="resume-meta">
                            Chương ${r.lastChapterNo} / ${r.totalChapters}
                            &middot; ${r.viewedLabel}
                        </span>

                        <%--
                          Thanh tiến độ. Không dùng thư viện biểu đồ nào cho
                          một cái vạch ngang — độ dài là CSS thuần.

                          progressPercent do model tính (số nguyên, đã chặn
                          chia cho 0). Tính trong EL thì phép chia trả về
                          Double và HTML nhận "width:42.857142857142854%".
                        --%>
                        <span class="resume-bar">
                            <span class="resume-fill"
                                  style="width:${r.progressPercent}%"></span>
                        </span>
                    </span>
                </a>
            </c:forEach>
        </div>
    </c:if>

    <%-- ---- Truyện xem nhiều -------------------------------------------- --%>
    <c:if test="${not empty popular}">
        <div class="section-head">
            <h2>🔥 Đang được đọc nhiều</h2>
            <a class="more" href="${pageContext.request.contextPath}/story?action=list&amp;sort=popular">
                Xem tất cả →</a>
        </div>
        <div class="story-grid">
            <c:forEach var="story" items="${popular}">
                <%@ include file="/WEB-INF/views/story/_card.jsp" %>
            </c:forEach>
        </div>
    </c:if>

    <%-- ---- Truyện mới cập nhật ----------------------------------------- --%>
    <div class="section-head">
        <h2>Mới cập nhật</h2>
        <c:if test="${not empty totalStories}">
            <span class="more">${totalStories} truyện</span>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${not empty latest}">
            <div class="story-grid">
                <c:forEach var="story" items="${latest}">
                    <%@ include file="/WEB-INF/views/story/_card.jsp" %>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <%-- Trạng thái rỗng cũng phải được thiết kế: trang trắng trơn
                 trông như web hỏng, ô rỗng có thiết kế thì trông như chủ ý. --%>
            <div class="empty">
                <div class="empty-icon">📚</div>
                <h3>Chưa có truyện nào</h3>
                <p>Kho truyện đang trống. Hãy là người đầu tiên đăng truyện của bạn.</p>
                <a class="btn btn-primary"
                   href="${pageContext.request.contextPath}/story?action=create">Đăng truyện đầu tiên</a>
            </div>
        </c:otherwise>
    </c:choose>

