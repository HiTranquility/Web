<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
================================================================================
  chapter/raw.jsp — TRẢ VỀ TRẦN, không qua layout nào             ?action=raw
================================================================================
  TẦNG: views/

  Đây là đường dẫn mà JavaScript của trang đọc gọi bằng fetch() để lấy chương
  kế tiếp rồi nối vào cuối trang đang đọc.

  VÌ SAO PHẢI CÓ FILE RIÊNG THAY VÌ DÙNG LẠI ?action=read
    ?action=read đi qua layout/reader.jsp nên trả về NGUYÊN một trang HTML:
    <!DOCTYPE>, <head>, thanh trên, cả đoạn script. Nối nguyên cục đó vào
    giữa trang đang đọc là HTML hỏng — hai thẻ <html> lồng nhau, hai lần
    <body>, và đoạn script chạy lại lần thứ hai.

  contentType đặt ngay ở đây, KHÔNG có <!DOCTYPE>, KHÔNG có <html>.
  Cả file chỉ nhả ra đúng một thẻ <article>.
--%><%@ include file="/WEB-INF/views/chapter/_block.jsp" %>
