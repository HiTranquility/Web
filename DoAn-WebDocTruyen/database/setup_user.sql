-- =============================================================================
--  Tạo tài khoản MySQL riêng cho ứng dụng
-- =============================================================================
--  VÌ SAO KHÔNG DÙNG root
--    root có toàn quyền trên MỌI database của máy bạn — kể cả những thứ không
--    liên quan gì tới đồ án. Một lỗi SQL injection trong web là kẻ tấn công có
--    quyền đó. Tài khoản riêng chỉ được đụng vào đúng database webdoctruyen.
--
--    Thầy chấm bài nhìn thấy chi tiết này cũng là một điểm cộng.
--
--  CÁCH CHẠY (mở terminal, tự gõ mật khẩu root khi được hỏi):
--
--      mysql -u root -p < database/setup_user.sql
--
--  ĐỔI MẬT KHẨU BÊN DƯỚI trước khi chạy, rồi điền đúng mật khẩu đó vào
--  src/main/resources/db.properties
-- =============================================================================

-- >>> ĐỔI CHUỖI NÀY THÀNH MẬT KHẨU CỦA BẠN <<<
CREATE USER IF NOT EXISTS 'truyen_app'@'localhost'
    IDENTIFIED BY 'DoiMatKhauNayDi123';

-- Chỉ cấp quyền thao tác dữ liệu trên đúng một database.
-- KHÔNG cấp DROP, CREATE USER, hay quyền trên *.* — app không cần và không nên có.
GRANT SELECT, INSERT, UPDATE, DELETE
    ON webdoctruyen.*
    TO 'truyen_app'@'localhost';

FLUSH PRIVILEGES;

SELECT 'Đã tạo tài khoản truyen_app.' AS ket_qua;
