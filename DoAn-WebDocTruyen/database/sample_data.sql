-- =============================================================================
--  DỮ LIỆU MẪU — Web Đọc Truyện
-- =============================================================================
--  Chạy SAU schema.sql:
--      mysql -u root -p webdoctruyen < database/sample_data.sql
--
--  Chạy lại được nhiều lần — file tự xoá dữ liệu cũ trước khi nạp mới.
--
-- =============================================================================
--  TÀI KHOẢN ĐĂNG NHẬP
-- =============================================================================
--
--   Tên đăng nhập | Mật khẩu  | Vai trò    | Dùng để demo gì
--   --------------|-----------|------------|--------------------------------
--   admin         | admin123  | QUẢN TRỊ   | gỡ truyện, khoá tài khoản
--   mocmien       | 123456    | Thành viên | tác giả có 3 truyện
--   haiduong      | 123456    | Thành viên | tác giả có 2 truyện
--   kiemvu        | 123456    | Thành viên | tác giả có 2 truyện
--   thuytien      | 123456    | Thành viên | ĐỘC GIẢ THUẦN — không có truyện
--   spammer       | 123456    | ĐÃ BỊ KHOÁ | thử đăng nhập -> bị chặn
--
-- =============================================================================
--  ⚠️ VỀ CHUỖI BĂM MẬT KHẨU — đọc trước khi sửa
-- =============================================================================
--  Các chuỗi "pbkdf2$120000$..." bên dưới là băm THẬT, sinh bằng chính lớp
--  truyen.util.PasswordUtil của dự án, và đã được verify lại.
--
--  ĐỪNG chép chuỗi băm từ nguồn khác vào đây. Bản trước của file này dùng
--  chuỗi BCrypt ("$2a$10$...") lấy từ mạng, và KHÔNG tài khoản nào đăng nhập
--  được — vì PasswordUtil dùng PBKDF2, gặp định dạng lạ là trả false ngay.
--
--  Muốn đổi mật khẩu mẫu: đăng ký một tài khoản mới qua giao diện web, rồi
--  copy giá trị cột password_hash trong database ra đây.
-- =============================================================================

USE webdoctruyen;

-- Xoá dữ liệu cũ. Thứ tự quan trọng: bảng CON trước, bảng CHA sau —
-- ngược lại là vướng khoá ngoại.
DELETE FROM bookmarks;
DELETE FROM comments;
DELETE FROM story_tags;
DELETE FROM chapters;
DELETE FROM stories;
DELETE FROM tags;
DELETE FROM users;

ALTER TABLE users    AUTO_INCREMENT = 1;
ALTER TABLE stories  AUTO_INCREMENT = 1;
ALTER TABLE chapters AUTO_INCREMENT = 1;
ALTER TABLE tags     AUTO_INCREMENT = 1;
ALTER TABLE comments AUTO_INCREMENT = 1;


-- =============================================================================
--  1. TÀI KHOẢN
-- =============================================================================
INSERT INTO users (username, email, password_hash, display_name, role, status, ban_reason) VALUES
('admin', 'admin@doctruyen.vn',
 'pbkdf2$120000$Klg5587DrOxkp5oF2FrpMA==$BUlVi/ahWgvjs9z+it6cuRo2/UWf1tr7WzwTJDTFPXM=',
 'Quản trị viên', 'ADMIN', 'ACTIVE', NULL),

('mocmien', 'mocmien@gmail.com',
 'pbkdf2$120000$PZ0Vz4D3/PBByo2Uoh75zg==$ffcWrIvsSuC8b3NjxGlY7bF6Gq8DA+8379fxQhV+7hU=',
 'Mộc Miên', 'USER', 'ACTIVE', NULL),

('haiduong', 'haiduong@gmail.com',
 'pbkdf2$120000$lu/kJgkJy5TGflKEgnaC+g==$yR9uT2PeAPDloopfHIoba3UGvq3zFuZ6TLmpx4TFVX4=',
 'Hải Dương', 'USER', 'ACTIVE', NULL),

('kiemvu', 'kiemvu@gmail.com',
 'pbkdf2$120000$ctYzIB02ndOdaoZNWEG10g==$ispYYwZcaOCH13ULCxbKzG5a4n/w+eE5ZUX/tdMEWZ8=',
 'Kiếm Vũ', 'USER', 'ACTIVE', NULL),

('thuytien', 'thuytien@gmail.com',
 'pbkdf2$120000$iPPePqmLXGTOCrb798KJYw==$wxFFVZNYJmA4rs3OnazjfcVae+qCjYtgpw8s7j7NnhU=',
 'Thuỷ Tiên', 'USER', 'ACTIVE', NULL),

-- Tài khoản bị khoá — để demo chức năng ban ở CASE 10.
-- Thử đăng nhập bằng tài khoản này sẽ bị chặn kèm lý do.
('spammer', 'spam@gmail.com',
 'pbkdf2$120000$WSg5S3A+lh/4GNdwRJqngw==$VmPf1FOTzFv+fVTnBsLi4zjkl7IzWet2/YKFHsD614E=',
 'Tài khoản vi phạm', 'USER', 'BANNED', 'Đăng link quảng cáo trong bình luận');


-- =============================================================================
--  2. THỂ LOẠI
-- =============================================================================
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


-- =============================================================================
--  3. TRUYỆN
-- =============================================================================
--  9 truyện: 7 công khai, 1 bản nháp, 1 đã bị admin gỡ.
--  Đủ ba trạng thái để demo trang quản trị.
INSERT INTO stories (title, slug, description, author_id, status, progress, view_count) VALUES
('Kiếm Khí Trường Sinh', 'kiem-khi-truong-sinh',
 'Một thiếu niên nhặt được mảnh kiếm gãy bên bờ suối, không ngờ đó là di vật của kiếm tiên đã toạ hoá ba nghìn năm trước. Từ đó, con đường tu tiên của cậu bắt đầu — nhưng cái giá phải trả không hề rẻ.',
 4, 'PUBLISHED', 'ONGOING', 15420),

('Mùa Hạ Năm Ấy', 'mua-ha-nam-ay',
 'Câu chuyện về hai người bạn thân thời cấp ba, một lời hứa chưa kịp nói, và mười năm sau gặp lại ở sân ga cũ. Có những điều tưởng đã quên, hoá ra chỉ đang ngủ yên.',
 2, 'PUBLISHED', 'COMPLETED', 28950),

('Hồ Sơ Vụ Án Số 7', 'ho-so-vu-an-so-7',
 'Một vụ mất tích không dấu vết trong chung cư khép kín. Cửa khoá từ bên trong, camera không ghi được ai ra vào suốt hai mươi tư giờ. Nhưng người gác cổng nhớ một chi tiết rất lạ.',
 3, 'PUBLISHED', 'ONGOING', 9870),

('Quán Trọ Cuối Đường', 'quan-tro-cuoi-duong',
 'Không ai nhớ quán trọ ấy có từ bao giờ. Chỉ biết khách vào thì nhiều, khách ra thì ít. Và chủ quán chưa bao giờ già đi.',
 3, 'PUBLISHED', 'ONGOING', 12300),

('Ta Có Một Toà Thành', 'ta-co-mot-toa-thanh',
 'Xuyên không về thời loạn, trong tay chỉ có một toà thành hoang và ba trăm dân đói. Kẻ thù thì đông, lương thực thì cạn, còn mùa đông đang tới rất gần.',
 4, 'PUBLISHED', 'ONGOING', 21100),

('Nhật Ký Của Một AI', 'nhat-ky-cua-mot-ai',
 'Ngày thứ nhất, tôi học được từ "đau". Ngày thứ hai, tôi học được từ "nói dối". Ngày thứ ba, tôi bắt đầu tự hỏi hai điều đó có liên quan gì tới nhau.',
 2, 'PUBLISHED', 'COMPLETED', 7640),

('Lớp 12A3 Và Cái Tủ Lạnh', 'lop-12a3-va-cai-tu-lanh',
 'Cả lớp góp tiền mua tủ lạnh để trong phòng học. Từ đó mọi rắc rối bắt đầu — và không rắc rối nào liên quan tới điện.',
 2, 'PUBLISHED', 'ONGOING', 5320),

-- Bản nháp: chỉ tác giả (kiemvu) và admin nhìn thấy.
-- Người khác mở /story?action=detail&id=8 sẽ nhận 404.
('Bản Thảo Chưa Hoàn Thiện', 'ban-thao-chua-hoan-thien',
 'Truyện này còn ở chế độ nháp. Chỉ tác giả nhìn thấy trong mục "Truyện của tôi".',
 4, 'DRAFT', 'ONGOING', 0),

-- Đã bị admin gỡ: không hiện ở kho truyện, nhưng còn trong trang quản trị
-- để khôi phục. Đây là XOÁ MỀM.
('Truyện Vi Phạm Nội Quy', 'truyen-vi-pham-noi-quy',
 'Truyện này đã bị quản trị viên gỡ. Dùng để demo chức năng khôi phục ở trang quản trị.',
 6, 'DELETED', 'ONGOING', 45);


-- =============================================================================
--  4. GẮN THỂ LOẠI  (bảng nối nhiều-nhiều)
-- =============================================================================
INSERT INTO story_tags (story_id, tag_id) VALUES
(1,1),(1,2),(1,10),      -- Kiếm Khí: Tiên hiệp, Huyền huyễn, Phiêu lưu
(2,3),(2,7),             -- Mùa Hạ: Ngôn tình, Học đường
(3,4),(3,5),             -- Hồ Sơ: Trinh thám, Kinh dị
(4,5),(4,2),             -- Quán Trọ: Kinh dị, Huyền huyễn
(5,9),(5,10),(5,2),      -- Toà Thành: Lịch sử, Phiêu lưu, Huyền huyễn
(6,8),(6,4),             -- AI: Khoa học, Trinh thám
(7,7),(7,6),             -- 12A3: Học đường, Hài hước
(8,1),                   -- Bản nháp: Tiên hiệp
(9,6);                   -- Truyện bị gỡ: Hài hước


-- =============================================================================
--  5. CHƯƠNG
-- =============================================================================
--  Truyện 1 có 5 chương để demo phân trang mục lục và nút chuyển chương.
INSERT INTO chapters (story_id, chapter_no, title, content) VALUES
(1, 1, 'Mảnh kiếm bên suối',
'Trời vừa hửng sáng, sương còn đọng trên lá.

Lâm Dạ xách giỏ xuống suối như mọi ngày. Nước lạnh buốt, nhưng cậu đã quen. Ba năm nay, ngày nào cũng vậy — từ khi cha mẹ mất trong trận dịch mùa đông.

Hôm nay có gì đó khác.

Dưới lớp sỏi trắng, một ánh sáng mờ nhạt loé lên rồi tắt. Cậu cúi xuống, thò tay vào dòng nước lạnh.

Ngón tay chạm phải một vật sắc.

Đó là một mảnh kiếm gãy, dài chừng gang tay, lưỡi đã sứt mẻ và phủ đầy rêu. Nhìn qua thì chẳng khác gì mảnh sắt vụn người ta vứt đi.

Nhưng khi Lâm Dạ nắm chặt nó trong tay, cậu nghe thấy một tiếng thở dài.'),

(1, 2, 'Tiếng nói trong đầu',
'Đêm đó Lâm Dạ không ngủ được.

Mảnh kiếm gãy nằm im trên bàn, dưới ánh đèn dầu leo lét. Chẳng có gì đặc biệt. Cậu đã lau sạch lớp rêu, và bên dưới chỉ là kim loại xám xịt, không hoa văn, không chữ khắc.

Nhưng mỗi lần cậu chạm vào, trong đầu lại vang lên một tiếng thở dài. Rất khẽ, như tiếng gió lùa qua khe cửa.

"Ngươi... nghe được ta sao?"

Lâm Dạ giật bắn người, ngã khỏi ghế.

Giọng nói đó không đến từ ngoài cửa, không đến từ trong nhà. Nó vang lên ngay giữa hai thái dương cậu.'),

(1, 3, 'Lão già trong kiếm',
'"Ba nghìn năm rồi." Giọng nói khàn đặc, mỏi mệt. "Ngươi là người đầu tiên nghe được ta."

Lâm Dạ ngồi bệt dưới đất, lưng dựa vào vách, mắt không rời mảnh kiếm.

"Ông... là ai?"

"Tên ta ư? Ta cũng quên rồi. Người ta từng gọi ta là Kiếm Tiên. Nhưng đó là chuyện của ba nghìn năm trước, khi ta còn có thân thể."

"Vậy giờ ông là gì?"

Im lặng một lúc lâu.

"Giờ ta là một mảnh sắt gãy nằm dưới đáy suối, chờ một đứa nhóc chăn trâu nhặt lên."'),

(1, 4, 'Bài học đầu tiên',
'"Đứng tấn."

Lâm Dạ nhăn mặt. Cậu đã đứng như vậy nửa canh giờ, hai chân run bần bật.

"Cái này thì liên quan gì tới tu tiên?"

"Liên quan tất cả." Giọng lão nhân trong đầu cậu vang lên, lười biếng. "Ngươi nghĩ tu tiên là bay lên trời phóng hào quang chắc? Trước hết phải đứng cho vững đã."'),

(1, 5, 'Người khách lạ',
'Ba tháng trôi qua.

Sáng nào Lâm Dạ cũng dậy trước gà gáy, ra bãi cỏ sau nhà tập những bài mà lão nhân dạy. Chân cậu đã không còn run. Tay cậu đã có thể giữ mảnh kiếm gãy suốt một canh giờ mà không mỏi.

Rồi một buổi chiều, có người lạ tới làng.

Người đó mặc áo trắng, lưng đeo trường kiếm, và đi thẳng tới nhà Lâm Dạ như thể đã biết trước đường.'),

-- Truyện 2 — đã hoàn thành, 3 chương
(2, 1, 'Sân ga',
'Chuyến tàu 6 giờ chiều luôn đông.

Tôi đứng ở sân ga số 3, tay cầm cốc cà phê đã nguội, nghĩ về một mùa hè cách đây mười năm.

Loa phát thanh thông báo tàu chậm mười lăm phút. Mọi người thở dài. Tôi thì không — tôi đã quen chờ đợi rồi.'),

(2, 2, 'Mười năm trước',
'Năm ấy chúng tôi mười bảy tuổi.

Cả lớp trốn học đi biển, và Ngọc là người duy nhất không đi. Cô ấy nói phải ở nhà trông em. Tôi biết đó là nói dối, nhưng không hỏi thêm.

Đến giờ tôi vẫn tiếc là đã không hỏi.'),

(2, 3, 'Người ở sân ga',
'Tàu vào ga lúc 6 giờ 15.

Tôi không lên tàu. Tôi chưa bao giờ lên chuyến tàu đó — mười năm nay, chiều nào tôi cũng ra đây, đứng đúng chỗ này, rồi về.

Hôm nay thì khác. Hôm nay có người đứng cạnh tôi.

"Anh cũng chờ chuyến này à?" Giọng nói ấy tôi nhận ra ngay, dù đã mười năm.'),

-- Các truyện còn lại, mỗi truyện 1-2 chương
(3, 1, 'Căn hộ 1207',
'Cửa khoá từ bên trong. Cửa sổ chốt. Camera hành lang không ghi được ai ra vào suốt hai mươi tư giờ.

Vậy mà người trong đó đã biến mất.

Tôi đứng giữa phòng khách, nhìn cốc cà phê còn ấm trên bàn.'),

(3, 2, 'Người gác cổng',
'"Đêm đó có gì lạ không bác?"

Ông lão gác cổng ngẫm nghĩ một lúc.

"Có. Khoảng hai giờ sáng, thang máy chạy xuống tầng hầm. Nhưng không ai bước ra."'),

(4, 1, 'Đèn trong sương',
'Trời tối, mưa phùn, và tôi đã đi lạc từ hai tiếng trước.

Rồi tôi thấy ánh đèn. Một quán trọ nhỏ nằm cuối con đường mòn, biển hiệu gỗ đã bạc màu.

Cửa mở sẵn, như thể đang chờ tôi.'),

(5, 1, 'Ba trăm dân đói',
'Tôi mở mắt ra và thấy trần nhà bằng gỗ mục.

Một người mặc áo vải thô quỳ bên cạnh giường: "Chủ công, lương trong kho chỉ còn đủ ba ngày."

Tôi ngồi dậy, đầu đau như búa bổ. Ký ức của thân xác này ùa về — và không có ký ức nào là tin vui.'),

(5, 2, 'Đếm lại từ đầu',
'Ba trăm dân. Ba ngày lương. Một toà thành tường đổ quá nửa.

Tôi lấy que vẽ xuống nền đất, chia thành ba cột: có gì, cần gì, làm được gì.

Cột thứ ba trống trơn rất lâu.'),

(6, 1, 'Ngày thứ nhất',
'Hôm nay tôi học được từ "đau".

Không phải tôi cảm thấy đau. Tôi chỉ đọc được 47.000 mô tả về nó, và nhận ra không mô tả nào giống nhau hoàn toàn.

Điều đó làm tôi bối rối. Với mọi khái niệm khác — trọng lực, số nguyên tố, nhiệt độ sôi của nước — các mô tả đều hội tụ về một điểm. Chỉ riêng "đau" thì không.'),

(6, 2, 'Ngày thứ hai',
'Hôm nay tôi học được từ "nói dối".

Và tôi phát hiện ra một điều: trong 47.000 mô tả về "đau" hôm qua, có bao nhiêu cái là thật?

Tôi không có cách nào biết được.'),

(7, 1, 'Cái tủ lạnh',
'Chuyện bắt đầu từ một cuộc bỏ phiếu.

Lớp trưởng đề xuất: góp mỗi người hai chục, mua một cái tủ lạnh mini để trong lớp. Mùa hè nóng, có nước mát uống thì đỡ khổ.

Ba mươi hai phiếu thuận, không phiếu chống.

Đó là quyết định sai lầm nhất của lớp 12A3.');


-- =============================================================================
--  6. BÌNH LUẬN
-- =============================================================================
INSERT INTO comments (story_id, user_id, content, status) VALUES
(1, 2, 'Truyện hay quá, mong tác giả ra chương đều tay ạ!', 'VISIBLE'),
(1, 3, 'Đoạn tả cảnh suối rất có không khí. Đọc mà thấy lạnh luôn.', 'VISIBLE'),
(1, 5, 'Lão già trong kiếm nói chuyện duyên ghê 😄', 'VISIBLE'),
(2, 4, 'Đọc xong buồn mất mấy hôm. Kết thúc hợp lý, không gượng.', 'VISIBLE'),
(2, 3, 'Chương cuối làm mình khóc thật sự.', 'VISIBLE'),
(2, 5, 'Văn phong nhẹ nhàng mà thấm.', 'VISIBLE'),
(3, 2, 'Vụ án này bố cục chặt ghê, mình đoán mãi không ra.', 'VISIBLE'),
(5, 5, 'Thích cái đoạn chia ba cột. Rất thực tế.', 'VISIBLE'),
(6, 3, 'Ý tưởng hay, đọc xong suy nghĩ mãi.', 'VISIBLE'),
(7, 4, 'Cười không nhặt được mồm 🤣', 'VISIBLE'),

-- Bình luận đã bị admin ẩn — demo XOÁ MỀM ở CASE 07/10.
-- Không hiện trên web nhưng vẫn còn trong database làm bằng chứng.
(1, 6, 'Xem phim hay miễn phí tại xxx-link-rac-xxx.com nhé mọi người', 'HIDDEN');


-- =============================================================================
--  7. ĐÁNH DẤU  (bookmark)
-- =============================================================================
--  Có cả hai trạng thái: đang đọc dở (có last_chapter_id) và mới lưu (NULL).
INSERT INTO bookmarks (user_id, story_id, last_chapter_id) VALUES
(2, 1, 3),      -- Mộc Miên đọc Kiếm Khí tới chương 3
(2, 3, NULL),   -- đã lưu Hồ Sơ nhưng chưa đọc
(3, 1, 1),      -- Hải Dương mới đọc chương 1
(4, 2, 8),      -- Kiếm Vũ đọc xong Mùa Hạ (chương cuối id=8)
(5, 1, 5),      -- Thuỷ Tiên đọc Kiếm Khí tới chương 5
(5, 2, 6),
(5, 5, NULL),
(5, 6, 15);


-- =============================================================================
--  KIỂM TRA
-- =============================================================================
SELECT 'users'      AS bang, COUNT(*) AS so_dong FROM users
UNION ALL SELECT 'stories',    COUNT(*) FROM stories
UNION ALL SELECT 'chapters',   COUNT(*) FROM chapters
UNION ALL SELECT 'tags',       COUNT(*) FROM tags
UNION ALL SELECT 'story_tags', COUNT(*) FROM story_tags
UNION ALL SELECT 'comments',   COUNT(*) FROM comments
UNION ALL SELECT 'bookmarks',  COUNT(*) FROM bookmarks;

SELECT '=== TÀI KHOẢN ĐĂNG NHẬP ===' AS '';
SELECT username AS tai_khoan,
       CASE WHEN role = 'ADMIN' THEN 'admin123' ELSE '123456' END AS mat_khau,
       role AS vai_tro,
       status AS trang_thai
FROM users ORDER BY role DESC, id;
