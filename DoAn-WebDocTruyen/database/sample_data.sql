-- =============================================================================
--  Dữ liệu mẫu — để demo và để test giao diện khi chưa có nội dung thật
-- =============================================================================
--  Chạy SAU schema.sql:
--      mysql -u root -p webdoctruyen < database/sample_data.sql
--
--  Mật khẩu của mọi tài khoản mẫu đều là:  123456
--  (chuỗi băm bên dưới là BCrypt của "123456" — CASE 01 sẽ dùng tới)
-- =============================================================================

USE webdoctruyen;

-- Xoá dữ liệu cũ. Thứ tự quan trọng: xoá bảng con trước bảng cha, không thì
-- vướng khoá ngoại.
DELETE FROM bookmarks;
DELETE FROM comments;
DELETE FROM story_tags;
DELETE FROM chapters;
DELETE FROM stories;
DELETE FROM tags;
DELETE FROM users;

-- Đặt lại bộ đếm AUTO_INCREMENT để id bắt đầu từ 1 cho dễ nhìn khi test.
ALTER TABLE users    AUTO_INCREMENT = 1;
ALTER TABLE stories  AUTO_INCREMENT = 1;
ALTER TABLE chapters AUTO_INCREMENT = 1;
ALTER TABLE tags     AUTO_INCREMENT = 1;


-- ---- Tài khoản --------------------------------------------------------------
INSERT INTO users (username, email, password_hash, display_name, role, status) VALUES
('admin',     'admin@doctruyen.vn',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Quản trị viên', 'ADMIN', 'ACTIVE'),
('mocmien',   'mocmien@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mộc Miên',      'USER',  'ACTIVE'),
('haiduong',  'haiduong@gmail.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hải Dương',     'USER',  'ACTIVE'),
('kiemvu',    'kiemvu@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Kiếm Vũ',       'USER',  'ACTIVE'),
('spammer',   'spam@gmail.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Tài khoản xấu', 'USER',  'BANNED');


-- ---- Thể loại ---------------------------------------------------------------
INSERT INTO tags (name, slug) VALUES
('Tiên hiệp',   'tien-hiep'),
('Huyền huyễn', 'huyen-huyen'),
('Ngôn tình',   'ngon-tinh'),
('Trinh thám',  'trinh-tham'),
('Kinh dị',     'kinh-di'),
('Hài hước',    'hai-huoc'),
('Học đường',   'hoc-duong'),
('Khoa học',    'khoa-hoc'),
('Lịch sử',     'lich-su'),
('Phiêu lưu',   'phieu-luu');


-- ---- Truyện -----------------------------------------------------------------
INSERT INTO stories (title, slug, description, author_id, status, progress, view_count) VALUES
('Kiếm Khí Trường Sinh', 'kiem-khi-truong-sinh',
 'Một thiếu niên nhặt được mảnh kiếm gãy bên bờ suối, không ngờ đó là di vật của một kiếm tiên đã tọa hoá ba nghìn năm trước.',
 4, 'PUBLISHED', 'ONGOING', 15420),

('Mùa Hạ Năm Ấy', 'mua-ha-nam-ay',
 'Câu chuyện về hai người bạn thân thời cấp ba, một lời hứa chưa kịp nói, và mười năm sau gặp lại ở sân ga cũ.',
 2, 'PUBLISHED', 'COMPLETED', 28950),

('Hồ Sơ Vụ Án Số 7', 'ho-so-vu-an-so-7',
 'Một vụ mất tích không dấu vết trong chung cư khép kín. Camera không ghi được gì. Nhưng người gác cổng nhớ một chi tiết lạ.',
 3, 'PUBLISHED', 'ONGOING', 9870),

('Quán Trọ Cuối Đường', 'quan-tro-cuoi-duong',
 'Không ai nhớ quán trọ ấy có từ bao giờ. Chỉ biết khách vào thì nhiều, khách ra thì ít.',
 3, 'PUBLISHED', 'ONGOING', 12300),

('Ta Có Một Toà Thành', 'ta-co-mot-toa-thanh',
 'Xuyên không về thời loạn, trong tay chỉ có một toà thành hoang và ba trăm dân đói.',
 4, 'PUBLISHED', 'ONGOING', 21100),

('Nhật Ký Của Một AI', 'nhat-ky-cua-mot-ai',
 'Ngày thứ nhất, tôi học được từ "đau". Ngày thứ hai, tôi học được từ "nói dối".',
 2, 'PUBLISHED', 'COMPLETED', 7640),

('Lớp 12A3 Và Cái Tủ Lạnh', 'lop-12a3-va-cai-tu-lanh',
 'Cả lớp góp tiền mua tủ lạnh để trong phòng học. Từ đó mọi rắc rối bắt đầu.',
 2, 'PUBLISHED', 'ONGOING', 5320),

('Bản Thảo Chưa Đăng', 'ban-thao-chua-dang',
 'Truyện này còn ở chế độ nháp, chỉ tác giả nhìn thấy.',
 4, 'DRAFT', 'ONGOING', 0);


-- ---- Gắn thể loại cho truyện ------------------------------------------------
-- Mỗi truyện 2-3 tag, đúng như thực tế.
INSERT INTO story_tags (story_id, tag_id) VALUES
(1,1),(1,2),(1,10),
(2,3),(2,7),
(3,4),(3,5),
(4,5),(4,2),
(5,9),(5,10),(5,2),
(6,8),(6,4),
(7,7),(7,6);


-- ---- Chương -----------------------------------------------------------------
INSERT INTO chapters (story_id, chapter_no, title, content) VALUES
(1, 1, 'Mảnh kiếm bên suối',
'Trời vừa hửng sáng, sương còn đọng trên lá.\n\nLâm Dạ xách giỏ xuống suối như mọi ngày. Nước lạnh buốt, nhưng cậu đã quen. Ba năm nay, ngày nào cũng vậy.\n\nHôm nay có gì đó khác. Dưới lớp sỏi trắng, một ánh sáng mờ nhạt lóe lên rồi tắt.\n\nCậu cúi xuống, thò tay vào dòng nước.'),
(1, 2, 'Tiếng nói trong đầu',
'Đêm đó Lâm Dạ không ngủ được.\n\nMảnh kiếm gãy nằm im trên bàn, chẳng có gì đặc biệt. Nhưng mỗi lần cậu chạm vào, trong đầu lại vang lên một tiếng thở dài.\n\n"Ngươi... nghe được ta sao?"'),
(1, 3, 'Lão già trong kiếm',
'"Ba nghìn năm rồi." Giọng nói khàn đặc. "Ngươi là người đầu tiên nghe được ta."'),

(2, 1, 'Sân ga',
'Chuyến tàu 6 giờ chiều luôn đông.\n\nTôi đứng ở sân ga số 3, tay cầm cốc cà phê đã nguội, nghĩ về một mùa hè cách đây mười năm.'),
(2, 2, 'Mười năm trước',
'Năm ấy chúng tôi mười bảy tuổi. Cả lớp trốn học đi biển, và Ngọc là người duy nhất không đi.'),

(3, 1, 'Căn hộ 1207',
'Cửa khoá từ bên trong. Cửa sổ chốt. Camera hành lang không ghi được ai ra vào suốt hai mươi tư giờ.\n\nVậy mà người trong đó đã biến mất.'),

(5, 1, 'Ba trăm dân đói',
'Tôi mở mắt ra và thấy trần nhà bằng gỗ mục.\n\nMột người mặc áo vải thô quỳ bên cạnh: "Chủ công, lương trong kho chỉ còn đủ ba ngày."'),

(6, 1, 'Ngày thứ nhất',
'Hôm nay tôi học được từ "đau".\n\nKhông phải tôi cảm thấy đau. Tôi chỉ đọc được 47.000 mô tả về nó, và nhận ra không mô tả nào giống nhau.');


-- ---- Bình luận --------------------------------------------------------------
INSERT INTO comments (story_id, user_id, content, status) VALUES
(1, 2, 'Truyện hay quá, mong tác giả ra chương đều tay ạ!', 'VISIBLE'),
(1, 3, 'Đoạn tả cảnh suối rất có không khí.', 'VISIBLE'),
(2, 4, 'Đọc xong buồn mất mấy hôm.', 'VISIBLE'),
(2, 3, 'Kết thúc hợp lý, không gượng.', 'VISIBLE'),
(3, 2, 'Vụ án này bố cục chặt ghê.', 'VISIBLE'),
(1, 5, 'Xem phim hay miễn phí tại xxx-link-rac-xxx.com', 'HIDDEN');
-- ^ bình luận cuối là mẫu vi phạm nội quy đã bị admin ẩn — dùng để demo
--   chức năng quản trị và trang nội quy.


-- ---- Đánh dấu ---------------------------------------------------------------
INSERT INTO bookmarks (user_id, story_id, last_chapter_id) VALUES
(2, 1, 2),      -- Mộc Miên đang đọc Kiếm Khí tới chương 2
(2, 3, NULL),   -- đã lưu nhưng chưa đọc
(3, 1, 1),
(4, 2, NULL);


-- ---- Kiểm tra ---------------------------------------------------------------
SELECT 'users'    AS bang, COUNT(*) AS so_dong FROM users
UNION ALL SELECT 'stories',    COUNT(*) FROM stories
UNION ALL SELECT 'chapters',   COUNT(*) FROM chapters
UNION ALL SELECT 'tags',       COUNT(*) FROM tags
UNION ALL SELECT 'story_tags', COUNT(*) FROM story_tags
UNION ALL SELECT 'comments',   COUNT(*) FROM comments
UNION ALL SELECT 'bookmarks',  COUNT(*) FROM bookmarks;
