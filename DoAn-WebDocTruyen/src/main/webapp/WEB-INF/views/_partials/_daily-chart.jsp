<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
================================================================================
  _daily-chart.jsp — MẢNH TÁI DÙNG: biểu đồ cột theo ngày
================================================================================
  TẦNG: views/

  DÙNG Ở: trang 25 (bảng điều khiển — 3 biểu đồ) · 16 (thống kê của tác giả)

  BIẾN CẦN CÓ:
      dcData   int[]   số liệu, phần tử 0 là ngày XA NHẤT
      dcTitle  String  nhãn trên biểu đồ
      dcMax    int     giá trị lớn nhất (servlet tính sẵn — xem ghi chú dưới)
      dcUnit   String  đơn vị, tuỳ chọn

  VÌ SAO KHÔNG DÙNG THƯ VIỆN BIỂU ĐỒ
    Chart.js kéo theo ~200KB JavaScript cho một hàng cột. Cả dự án này chạy
    bằng form và link, thêm một thư viện chỉ để vẽ 14 hình chữ nhật là đổi
    quá nhiều lấy quá ít. Cột dựng bằng CSS thì cũng chạy khi tắt JavaScript.

  VÌ SAO dcMax DO SERVLET TÍNH CHỨ KHÔNG TÍNH Ở ĐÂY
    EL không có hàm max() cho mảng. Tính trong JSP phải viết một vòng lặp
    <c:forEach> chỉ để tìm số lớn nhất, rồi vòng thứ hai mới vẽ — đọc rối và
    lặp hai lần trên cùng dữ liệu. Servlet có sẵn mảng thì tính một dòng.

  CHIỀU CAO TÍNH THEO PHẦN TRĂM CỦA dcMax
    Cột cao nhất luôn chạm trần, nên biểu đồ luôn "đầy" dù số liệu to hay nhỏ.
    Đổi lại: KHÔNG so sánh được hai biểu đồ cạnh nhau bằng mắt — mỗi cái một
    thang. Vì vậy trục luôn ghi rõ giá trị lớn nhất.
================================================================================
--%>
<div class="chart">
    <div class="chart-head">
        <span class="chart-title"><c:out value="${dcTitle}"/></span>
        <span class="chart-max">
            cao nhất ${dcMax}<c:if test="${not empty dcUnit}"> ${dcUnit}</c:if>
        </span>
    </div>

    <div class="chart-bars">
        <c:forEach var="v" items="${dcData}" varStatus="st">
            <%--
              title= cho biết con số khi rê chuột. Không dùng tooltip tự vẽ:
              thuộc tính title là thứ trình đọc màn hình cũng đọc được.
            --%>
            <span class="chart-col" title="${v}<c:if test='${not empty dcUnit}'> ${dcUnit}</c:if>">
                <%-- min-height 2px: ngày bằng 0 vẫn phải thấy có cột, không
                     thì nhìn như dữ liệu bị thiếu chứ không phải bằng không --%>
                <i style="height:${dcMax gt 0 ? (v * 100 / dcMax) : 0}%"
                   class="${v eq dcMax and dcMax gt 0 ? 'peak' : ''}"></i>
            </span>
        </c:forEach>
    </div>

    <div class="chart-foot">
        <%-- fn:length CHU KHONG PHAI ${dcData.length}.
             EL coi dau cham tren mang la mot CHI SO, nen ${dcData.length} bi
             hieu thanh "phan tu thu 'length'" va nem NumberFormatException:
                 For input string: "length"
             Loi chi lo ra luc chay, JSP dich van qua. --%>
        <span>${fn:length(dcData)} ngày trước</span>
        <span>hôm nay</span>
    </div>
</div>
