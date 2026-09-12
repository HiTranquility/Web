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
--   kiemvu        | 123456    | Thành viên | tác giả có 3 truyện (1 bản nháp)
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

SET FOREIGN_KEY_CHECKS = 0;

-- Xoá dữ liệu cũ. Thứ tự quan trọng: bảng CON trước, bảng CHA sau —
-- ngược lại là vướng khoá ngoại.
DELETE FROM bookmarks;
DELETE FROM comments;
DELETE FROM story_tags;
DELETE FROM chapters;
DELETE FROM stories;
DELETE FROM tags;
DELETE FROM users;

-- =============================================================================
--  1. TÀI KHOẢN
-- =============================================================================
INSERT INTO users (id, username, email, password_hash, display_name, role, status, ban_reason) VALUES
(1, 'admin', 'admin@doctruyen.vn',
 'pbkdf2$120000$Klg5587DrOxkp5oF2FrpMA==$BUlVi/ahWgvjs9z+it6cuRo2/UWf1tr7WzwTJDTFPXM=',
 'Quản trị viên', 'ADMIN', 'ACTIVE', NULL),

(2, 'mocmien', 'mocmien@gmail.com',
 'pbkdf2$120000$PZ0Vz4D3/PBByo2Uoh75zg==$ffcWrIvsSuC8b3NjxGlY7bF6Gq8DA+8379fxQhV+7hU=',
 'Mộc Miên', 'USER', 'ACTIVE', NULL),

(3, 'haiduong', 'haiduong@gmail.com',
 'pbkdf2$120000$lu/kJgkJy5TGflKEgnaC+g==$yR9uT2PeAPDloopfHIoba3UGvq3zFuZ6TLmpx4TFVX4=',
 'Hải Dương', 'USER', 'ACTIVE', NULL),

(4, 'kiemvu', 'kiemvu@gmail.com',
 'pbkdf2$120000$ctYzIB02ndOdaoZNWEG10g==$ispYYwZcaOCH13ULCxbKzG5a4n/w+eE5ZUX/tdMEWZ8=',
 'Kiếm Vũ', 'USER', 'ACTIVE', NULL),

(5, 'thuytien', 'thuytien@gmail.com',
 'pbkdf2$120000$iPPePqmLXGTOCrb798KJYw==$wxFFVZNYJmA4rs3OnazjfcVae+qCjYtgpw8s7j7NnhU=',
 'Thuỷ Tiên', 'USER', 'ACTIVE', NULL),

-- Tài khoản bị khoá — để demo chức năng ban ở CASE 10.
-- Thử đăng nhập bằng tài khoản này sẽ bị chặn kèm lý do.
(6, 'spammer', 'spam@gmail.com',
 'pbkdf2$120000$WSg5S3A+lh/4GNdwRJqngw==$VmPf1FOTzFv+fVTnBsLi4zjkl7IzWet2/YKFHsD614E=',
 'Tài khoản vi phạm', 'USER', 'BANNED', 'Đăng link quảng cáo trong bình luận');


-- =============================================================================
--  2. THỂ LOẠI
-- =============================================================================
INSERT INTO tags (id, name, slug) VALUES
(1,  'Tiên hiệp',   'tien-hiep'),
(2,  'Huyền huyễn', 'huyen-huyen'),
(3,  'Ngôn tình',   'ngon-tinh'),
(4,  'Trinh thám',  'trinh-tham'),
(5,  'Kinh dị',     'kinh-di'),
(6,  'Hài hước',    'hai-huoc'),
(7,  'Học đường',   'hoc-duong'),
(8,  'Khoa học',    'khoa-hoc'),
(9,  'Lịch sử',     'lich-su'),
(10, 'Phiêu lưu',   'phieu-luu');


-- =============================================================================
--  3. TRUYỆN
-- =============================================================================
--  9 truyện: 7 công khai, 1 bản nháp, 1 đã bị admin gỡ.
--  Đủ ba trạng thái để demo trang quản trị.
INSERT INTO stories (id, title, slug, description, author_id, status, progress, view_count, cover_url) VALUES
(1, 'Kiếm Khí Trường Sinh', 'kiem-khi-truong-sinh',
 'Một thiếu niên nhặt được mảnh kiếm gãy bên bờ suối, không ngờ đó là di vật của kiếm tiên đã toạ hoá ba nghìn năm trước. Từ đó, con đường tu tiên của cậu bắt đầu — nhưng cái giá phải trả không hề rẻ.',
 4, 'PUBLISHED', 'ONGOING', 15420, '/assets/images/covers/cover-1.svg'),

(2, 'Mùa Hạ Năm Ấy', 'mua-ha-nam-ay',
 'Câu chuyện về hai người bạn thân thời cấp ba, một lời hứa chưa kịp nói, và mười năm sau gặp lại ở sân ga cũ. Có những điều tưởng đã quên, hoá ra chỉ đang ngủ yên.',
 2, 'PUBLISHED', 'COMPLETED', 28950, '/assets/images/covers/cover-2.svg'),

(3, 'Hồ Sơ Vụ Án Số 7', 'ho-so-vu-an-so-7',
 'Một vụ mất tích không dấu vết trong chung cư khép kín. Cửa khoá từ bên trong, camera không ghi được ai ra vào suốt hai mươi tư giờ. Nhưng người gác cổng nhớ một chi tiết rất lạ.',
 3, 'PUBLISHED', 'ONGOING', 9870, '/assets/images/covers/cover-3.svg'),

(4, 'Quán Trọ Cuối Đường', 'quan-tro-cuoi-duong',
 'Không ai nhớ quán trọ ấy có từ bao giờ. Chỉ biết khách vào thì nhiều, khách ra thì ít. Và chủ quán chưa bao giờ già đi.',
 3, 'PUBLISHED', 'ONGOING', 12300, '/assets/images/covers/cover-4.svg'),

(5, 'Ta Có Một Toà Thành', 'ta-co-mot-toa-thanh',
 'Xuyên không về thời loạn, trong tay chỉ có một toà thành hoang và ba trăm dân đói. Kẻ thù thì đông, lương thực thì cạn, còn mùa đông đang tới rất gần.',
 4, 'PUBLISHED', 'ONGOING', 21100, '/assets/images/covers/cover-5.svg'),

(6, 'Nhật Ký Của Một AI', 'nhat-ky-cua-mot-ai',
 'Ngày thứ nhất, tôi học được từ "đau". Ngày thứ hai, tôi học được từ "nói dối". Ngày thứ ba, tôi bắt đầu tự hỏi hai điều đó có liên quan gì tới nhau.',
 2, 'PUBLISHED', 'COMPLETED', 7640, '/assets/images/covers/cover-8.svg'),

(7, 'Lớp 12A3 Và Cái Tủ Lạnh', 'lop-12a3-va-cai-tu-lanh',
 'Cả lớp góp tiền mua tủ lạnh để trong phòng học. Từ đó mọi rắc rối bắt đầu — và không rắc rối nào liên quan tới điện.',
 2, 'PUBLISHED', 'ONGOING', 5320, '/assets/images/covers/cover-2.svg'),

-- Bản nháp: chỉ tác giả (kiemvu) và admin nhìn thấy.
-- Người khác mở /story?action=detail&id=8 sẽ nhận 404.
(8, 'Bản Thảo Chưa Hoàn Thiện', 'ban-thao-chua-hoan-thien',
 'Truyện này còn ở chế độ nháp. Chỉ tác giả nhìn thấy trong mục "Truyện của tôi".',
 4, 'DRAFT', 'ONGOING', 0, '/assets/images/covers/cover-1.svg'),

-- Đã bị admin gỡ: không hiện ở kho truyện, nhưng còn trong trang quản trị
-- để khôi phục. Đây là XOÁ MỀM.
(9, 'Truyện Vi Phạm Nội Quy', 'truyen-vi-pham-noi-quy',
 'Truyện này đã bị quản trị viên gỡ. Dùng để demo chức năng khôi phục ở trang quản trị.',
 6, 'DELETED', 'ONGOING', 45, NULL);


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
'Trời vừa hửng sáng, màn sương lạnh buốt của dãy Thương Vân vẫn còn phủ trắng xoá khắp thôn Thanh Khê. Từng đợt gió sớm thổi qua sườn núi rít lên từng hồi căm căm, làm lay động những cành thông cổ thụ rậm rạp.

Lâm Dạ một tay xách chiếc giỏ tre đan đã sờn rách, tay kia cầm khúc củi gộc dùng làm gậy dò đường, từng bước chậm rãi đi về phía khe suối Bạch Thạch. Nước suối mùa thu trong vắt nhưng lạnh thấu xương tủy, mỗi khi bàn tay cậu thò xuống chạm vào nước là từng khớp ngón tay lại tím tái đi vì giá rét. Thế nhưng cậu đã quen với nỗi khổ này từ lâu. Ba năm trước, một trận ôn dịch quái ác đã cướp đi sinh mạng của cha mẹ cậu, bỏ lại một đứa trẻ mười ba tuổi tự mình bươn chải giữa thế đạo loạn lạc.

"Hôm nay bắt được năm con cá nhỏ, đem lên trấn đổi lấy hai cân gạo thô và ít muối là đủ qua bữa..."

Lâm Dạ lẩm bẩm tính toán, cúi người lật từng tảng đá ngầm dưới lòng suối. Dòng nước chảy xiết cuốn theo những lớp sỏi trắng lạo xạo dưới chân.

Đột nhiên, ngay dưới gầm của một tảng đá ngầm phủ đầy rêu xanh biếc, một tia sáng màu lam nhạt kỳ dị bỗng loé lên rồi vụt tắt. Thứ ánh sáng ấy quá đỗi khác thường, vừa băng lãnh u uẩn, lại vừa mang theo một khí tức sắc bén đến nghẹt thở khiến từng sợi tóc gáy của Lâm Dạ dựng đứng.

Cậu nuốt nước bọt, chần chừ vài giây rồi quyết định thò sâu cánh tay xuống dòng nước buốt giá, mò mẫm sâu vào khe đá.

"Xoẹt!"

Một cơn đau nhói truyền đến từ đầu ngón tay. Lâm Dạ giật mình rụt tay lại, phát hiện ngón trỏ của mình đã bị cứa một đường sâu hoắm, máu tươi đỏ thắm rỉ ra, hòa tan vào dòng nước suối cuồn cuộn.

Kỳ lạ thay, vết thương sắc ngọt như bị dao cạo rạch qua, ngọt lịm đến mức ban đầu cậu thậm chí còn chưa kịp cảm thấy đau đớn. Dưới đáy nước, vật thể kia dường như ngửi thấy mùi máu tanh, tia sáng lam u tối bỗng nhiên rực sáng mãnh liệt, cuộn thành một xoáy nước nhỏ xung quanh ngón tay cậu.

Lâm Dạ cắn chặt răng, nghiến lợi luồn tay tóm chặt lấy chuôi của vật thể lạ rồi dùng hết sức bình sinh giật mạnh ra ngoài.

Đó là một đoạn tàn kiếm gãy.

Thân kiếm chỉ dài chừng một gang tay rưỡi, phần mũi kiếm đã bị chém đứt ngọt từ bao giờ. Trên thân lưỡi loang lổ những vết rỉ sét màu nâu đồng xen lẫn những mảng rêu phong bám chặt như lớp da cóc xù xì. Nhìn lướt qua, nó chẳng khác nào một thanh sắt vụn bị thợ rèn vứt bỏ xó chuồng gà, không hề có bao kiếm, cũng chẳng thấy hoa văn chạm khắc tinh xảo của các bậc tiên gia mà người kể chuyện ở tửu điếm từng miêu tả.

Thế nhưng, khi ngón tay đẫm máu của Lâm Dạ chạm khít vào phần chuôi sắt lạnh ngắt, một luồng hàn khí vô hình đột ngột từ mảnh kiếm bắn thẳng vào kinh mạch cánh tay cậu!

"Ù ù..."

Một tiếng thở dài cổ xưa, mỏi mệt, nặng trĩu tang thương như vượt qua vạn cổ sông dài bỗng nhiên chấn động vang vọng thẳng vào sâu trong linh hồn Lâm Dạ:

"Ba nghìn năm trầm luân nơi đáy nước... Cuối cùng, cũng có kẻ đánh thức được kiếm phách của bản tôn rồi sao?"'),

(1, 2, 'Tiếng nói trong đầu',
'Đêm mùa thu ở vùng biên thùy núi rừng đến nhanh như chớp mắt. Gió lốc ngoài trời gầm thét dữ dội, đập vào liếp cửa nứa ọp ẹp tạo nên những âm thanh kẽo kẹt rợn người.

Trong căn chòi tranh xiêu vẹo, ngọn đèn dầu hạt đỗ leo lét chao đảo theo từng cơn gió lùa qua khe liếp. Lâm Dạ ngồi bó gối bên chiếc bàn gỗ cập kê, hai mắt mở to trân trân nhìn mảnh kiếm gãy đặt ngay ngắn trước mặt.

Sau khi mang nó từ bờ suối về, cậu đã dùng tro bếp cùng vải thô cọ rửa suốt một canh giờ. Lớp bùn rêu bên ngoài đã trôi đi, để lộ ra chất kim loại màu xám tro kỳ dị. Bề mặt thân kiếm không phản chiếu ánh đèn dầu, tựa như một lỗ đen nuốt chửng mọi tia sáng xung quanh. Trên thân kiếm có ba đường rãnh huyết mờ nhạt, nhưng lạ lùng nhất là vết đứt gãy ở đầu kiếm — phẳng lì như gương, dường như bị một thứ thần binh còn khủng khiếp hơn chém đứt lìa trong một chiêu duy nhất.

Vết cứa trên ngón tay trỏ của Lâm Dạ lúc này đã tự động khép miệng, chỉ còn lại một vết sẹo mờ màu xám nhạt hình mũi kiếm nhỏ li ti.

"Chẳng lẽ ban sáng dưới suối là mình bị ảo giác vì đói lả?" Lâm Dạ tự lẩm bẩm trấn an bản thân. "Làm sao một mảnh sắt gãy mục nát lại biết thở dài được..."

Vừa dứt lời, cậu vô thức đưa bàn tay chạm nhẹ vào sống kiếm.

"Ong!"

Cả căn phòng bỗng chốc như ngưng đọng lại. Tiếng gió gầm gào ngoài kia vụt tắt, giọt dầu trên bấc đèn như đứng yên giữa không trung.

"Đứa nhóc ngốc nghếch, ngươi dám bảo bản mạng thần kiếm của bản tôn là sắt vụn mục nát?"

Một giọng nói trầm đục, khàn khàn nhưng mang theo khí phách ngạo nghễ bễ nghễ thiên hạ bùng nổ ngay giữa hai thái dương Lâm Dạ!

"Á!"

Lâm Dạ hét lên một tiếng thất thanh, cả người theo phản xạ ngã bật ngửa khỏi ghế đẩu, lưng đập mạnh vào vách đất nứa. Cậu ôm chặt lấy đầu, mồ hôi lạnh toát ra như tắm, ánh mắt đầy vẻ kinh hãi nhìn quanh căn phòng trống rỗng:

"Ai?! Là ai đang trốn ở đó? Ra đây mau!"

"Đừng có gào thét vô ích." Giọng nói kia lại vang lên, lần này rõ ràng hơn, tựa như có một bóng người vô hình đang đứng ngay sát bên tai cậu khẽ cười giễu cợt: "Ngươi nhìn quanh làm gì? Ta đang ở ngay trong thức hải của ngươi. Nói đúng hơn... là linh hồn tàn khuyết của ta đang mượn giọt máu tươi lúc sáng của ngươi để hồi sinh một tia thần niệm."'),

(1, 3, 'Lão già trong kiếm',
'Lâm Dạ ngồi bệt dưới nền đất lạnh ngắt, hơi thở dồn dập đứt quãng. Cậu dùng hai bàn tay chà mạnh lên mặt, cố gắng xác định xem mình có đang rơi vào một cơn ác mộng quái đản hay không.

"Ngươi không nằm mơ đâu, tiểu tử." Giọng nói trong đầu lại cất lên, lần này không còn vẻ uy nghiêm đè nén nữa mà pha chút bi ai, mệt mỏi cùng cực: "Ba nghìn năm trước, sau trận huyết chiến tại Tru Ma Nhai, nhục thân của ta bị vỡ nát thành tro bụi, chín phần mười thần hồn tiêu tán giữa đất trời. Chỉ có một tia tàn phách này ký thác vào mảnh tàn kiếm gãy, vùi sâu dưới đáy khe Bạch Thạch chờ ngày mục rữa."

Lâm Dạ nuốt một ngụm nước bọt, lắp bắp hỏi trong đầu:
"Ông... rốt cuộc là yêu ma hay quỷ quái phương nào?"

"Hỗn xược!" Giọng nói kia hơi gắt lên, mang theo tiếng kiếm ngân réo rắt khiến màng nhĩ Lâm Dạ đau buốt: "Bản tôn tung hoành tam giới, kiếm chỉ Cửu Thiên, từng được vạn tiên phụng bái tôn xưng là ''Vấn Thiên Kiếm Tôn''! Năm xưa dưới kiếm của ta, Ma Hoàng chín kiếp cũng phải cúi đầu nhận tội. Yêu ma quỷ quái gì ở đây?"

Lâm Dạ chớp chớp mắt, thật thà đáp:
"Cháu chưa từng nghe qua tên ông. Người trong trấn chỉ biết đến các vị tiên sư của Thanh Vân Môn trên đỉnh núi cao kia thôi. Nghe bảo họ có thể cưỡi mây bay lượn, bắt ma trừ tà."

"Thanh Vân Môn?" 

Một tiếng cười lạnh lẽo vang lên trong thức hải, ngập tràn vẻ khinh miệt tột độ:
"Một lũ đạo đức giả trộm cắp đạo thống năm xưa của bổn tông mà cũng dám xưng là tiên sư? Nếu không phải ba nghìn năm trước tên tổ sư Thanh Vân Tử của lũ chúng cấu kết ngoại ma ám toán đâm lén sau lưng ta, thì mảnh tàn kiếm này làm sao phải gãy lìa chôn vùi dưới đáy suối dơ bẩn?"

Lâm Dạ im lặng lắng nghe. Dù cậu chưa hiểu rõ ân oán tiên gia là gì, nhưng sự căm phẫn và bi tráng trong giọng nói của lão nhân chân thực đến mức khiến ngực cậu nhói đau.

"Vậy... tại sao ông lại chọn cháu?" Lâm Dạ hỏi khẽ. "Cháu chỉ là một kẻ mồ côi nghèo đói, đến bữa ăn ngày mai còn chưa biết kiếm ở đâu."

Lão nhân im lặng hồi lâu, rồi thở dài một hơi dài thượt:
"Bởi vì trong hàng ức vạn phàm nhân đi qua con suối đó suốt ba nghìn năm qua, chỉ có ngươi sở hữu ''Thông Thiên Kiếm Cốt'' bẩm sinh. Máu của ngươi mới có thể xuyên qua phong ấn rỉ sét đánh thức ta. Ngươi trời sinh sinh ra là để cầm kiếm... Chỉ tiếc rằng, kinh mạch trong người ngươi đã bị hàn độc bế tắc hoàn toàn, nếu không gặp ta, e rằng ngươi không sống qua nổi tuổi hai mươi."'
),

(1, 4, 'Bài học đầu tiên',
'Sương sớm chưa tan, tiếng gà gáy canh tư vừa cất lên đầu thôn thì Lâm Dạ đã bị một luồng thanh âm sắc nhọn đánh thức thẳng vào màng nhĩ.

"Dậy! Đứa nhóc lười biếng, muốn sống sót tu đạo mà còn muốn ngủ đẫy giấc sao?"

Lâm Dạ giật mình bật dậy, vội vàng xỏ đôi giày cỏ rách bươm rồi chạy ra khoảng đất trống sau chòi tranh. Phía chân trời chỉ mới le lói một dải sáng bạc mờ nhạt, hơi lạnh phả ra từ tán lá rừng buốt nhói từng thớ thịt.

"Bây giờ ta dạy ngươi phi kiếm hay chưởng tâm lôi?" Lâm Dạ hào hứng hỏi, hai mắt sáng rực. Cậu từng nghe người ta kể tiên nhân có thể vẫy tay gọi gió, một kiếm chém đứt ngọn núi.

"Phi kiếm? Chưởng tâm lôi?" Tiếng cười khẩy của Kiếm Tôn vang lên giòn tan: "Ngươi ngay cả con gà còn chưa giết nổi mà đòi học phi kiếm? Đứng tấn cho ta!"

"Hả? Đứng tấn?"

"Hai chân mở rộng bằng vai, hạ trọng tâm xuống bằng đầu gối, lưng thẳng như cọc gỗ, hai tay duỗi thẳng về phía trước, giữ chặt mảnh tàn kiếm bằng ba ngón tay!" Lão nhân nghiêm khắc quát: "Mũi kiếm chỉ thẳng vào đường chân trời. Khi nào mặt trời mọc hoàn toàn khỏi đỉnh núi Thương Vân mới được hạ xuống!"

Lâm Dạ cắn răng làm theo. Ban đầu chỉ thấy hơi mỏi, nhưng chỉ sau nửa nén hương, hai bắp đùi cậu bắt đầu run rẩy bần bật như cầy sấy. Cảm giác đau nhức từ cơ bắp truyền lên như có hàng ngàn mũi kim châm cứu cắm vào xương tủy. Mảnh tàn kiếm chỉ nặng chưa đầy hai cân, vậy mà lúc này trên tay cậu nặng tựa ngàn cân đá tảng.

"Tiền bối... cháu chịu không nổi nữa rồi..." Mồ hôi vã ra như tắm, nhỏ tong tỏng xuống nền đất ướt nhẹp.

"Không được nhúc nhích!" Giọng lão nhân bỗng trở nên nghiêm túc đến lạ thường: "Kiếm đạo trước hết là Đạo của Tâm và Thân. Một kẻ ngay cả thân xác của mình còn không khống chế nổi thì làm sao khống chế được kiếm khí cuồng bạo? Hít sâu vào! Cảm nhận từng luồng khí lạnh của buổi sớm đi qua cánh mũi, dẫn nó đi xuống đan điền!"

Theo lời chỉ dẫn của lão nhân, Lâm Dạ cố nén cơn đau dữ dội, điều hòa nhịp thở. Kỳ diệu thay, khi một luồng thanh khí tinh khiết luân chuyển vào bụng dưới, từ mảnh tàn kiếm trên tay cậu bỗng tỏa ra một sợi hơi ấm mỏng manh như sợi tơ hồng, chạy dọc theo cánh tay thẩm thấu vào kinh mạch, xua tan cơn mệt mỏi rã rời.

Đó là luồng Kiếm Khí đầu tiên trong đời cậu nếm trải.'),

(1, 5, 'Người khách lạ',
'Thấm thoắt ba tháng ròng rã trôi qua trong nháy mắt.

Dưới sự huấn luyện tàn khốc của lão Kiếm Tôn, Lâm Dạ đã hoàn toàn lột xác. Thân hình gầy gò ốm yếu ngày nào nay đã săn chắc như loài báo hoa nơi rừng sâu, làn da ngăm đen ánh lên vẻ dẻo dai khỏe khoắn. Hai mắt cậu sáng ngời, sâu thẳm như hồ nước mùa thu, mỗi cái chớp mắt dường như đều mang theo một tia kiếm ý sắc bén khó lòng che giấu.

Kinh mạch bị hàn độc bế tắc suốt mười sáu năm nay đã được dòng kiếm khí tinh thuần gột rửa hơn phân nửa. Giờ đây, cậu có thể đứng tấn suốt ba canh giờ liền, vung mảnh tàn kiếm chém vào không khí tạo nên những tiếng rít gió xé rách lá rơi cách xa năm trượng.

Một buổi chiều tà, khi ráng chiều đỏ rực như máu nhuộm thắm đỉnh núi Thương Vân, thôn Thanh Khê bỗng chìm vào một bầu không khí tĩnh lặng đến rợn người.

Tiếng chim rừng bỗng im bặt, ngay cả lũ chó săn hung dữ nhất thôn cũng cụp đuôi chui tọt vào gầm giường run rẩy không dám sủa một tiếng.

Từ trên bầu trời phía tây, một vệt kiếm quang màu xanh biếc xé toạc tầng mây, lướt đi với tốc độ kinh hoàng rồi đáp xuống ngay đầu thôn.

Gió lốc cuộn trào bụi đất mịt mù. Khi luồng sáng tan đi, một thanh niên mặc trường bào trắng thêu hoa văn mây trôi của Thanh Vân Môn chầm chậm bước ra. Hắn chừng hai mươi tuổi, đầu đội ngọc quan, mặt mày tuấn tú kiêu ngạo, sau lưng đeo một thanh bảo kiếm phát ra ánh hàn quang lạnh ngắt.

Người thanh niên áo trắng không thèm nhìn đám dân làng đang hoảng sợ quỳ rạp hai bên đường, mà ánh mắt sắc như chim ưng của hắn trực tiếp khóa chặt về phía căn chòi tranh xiêu vẹo cuối thôn — nơi Lâm Dạ đang đứng tựa cửa.

"Kỳ quái... Kiếm khí dao động yếu ớt phát ra từ vùng hẻo lánh này suốt ba tháng qua, lại bắt nguồn từ một tên nhóc phàm nhân chưa từng nhập môn sao?"

Thanh niên áo trắng lẩm bẩm, khóe môi khẽ nhếch lên một nụ cười đầy ẩn ý, từng bước sải dài đi thẳng tới trước mặt Lâm Dạ.'),

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


-- ---- Chương bổ sung — cho mỗi truyện có đủ nội dung để demo ----------------
-- Thêm sau khối trên nên id tiếp tục từ 17, các bookmark ở trên vẫn trỏ đúng.
INSERT INTO chapters (story_id, chapter_no, title, content) VALUES

-- Kiếm Khí Trường Sinh (truyện 1) — chương 6 đến 10
(1, 6, 'Thanh Vân Môn',
'Gió chiều thổi tung vạt áo bào trắng muốt của người thanh niên lạ mặt. Hắn dừng bước cách Lâm Dạ chừng ba trượng, ánh mắt cao ngạo quét từ đầu đến chân cậu như một vị vương giả đang đánh giá một con kiến hôi dưới chân mình.

"Ngươi tên là gì?" Giọng nói của hắn mang theo linh áp vô hình, khiến không khí xung quanh Lâm Dạ như đông cứng lại thành băng đá.

Lâm Dạ cảm thấy lồng ngực mình tức nghẹn, nhưng sống lưng cậu vẫn thẳng tắp như một ngọn giáo cắm sâu vào lòng đất. Cậu bình tĩnh chắp tay:
"Tiểu nhân là Lâm Dạ, thợ săn ở thôn này. Không biết tiên sư giá lâm có điều gì chỉ giáo?"

"Ta là Tống Nhiên, đệ tử nội môn Thanh Vân Môn." Thanh niên áo trắng hếch cằm lên, giọng điệu ngập tràn kiêu hãnh: "Ba tháng trước, Hồn Kiếm Đài trên tông môn rung chuyển, trưởng lão phát hiện ra dưới chân Thương Vân có một luồng kiếm ý thượng cổ thức tỉnh. Bản tiên sư phụng mệnh hạ sơn điều tra khắp trăm dặm, cuối cùng lại lần theo dấu vết tìm đến tận túp lều rách này của ngươi."

Tống Nhiên vừa nói vừa tiến lại gần thêm một bước, ánh mắt đảo qua gian nhà tranh đơn sơ:
"Nói đi, gần đây ngươi có nhặt được bảo vật dị thường nào rơi từ trên trời xuống không? Giao ra đây cho tông môn, bản tiên sư sẽ ban cho ngươi một vạn lượng hoàng kim, bảo đảm cả đời ngươi vinh hoa phú quý không hết."

Trong thức hải của Lâm Dạ, giọng lão Kiếm Tôn bỗng vang lên gầm gừ như mãnh thú bị thương:
"Khốn kiếp! Đúng là chó săn của Thanh Vân Môn! Ba nghìn năm rồi mà mùi hương kiếm trận bẩn thỉu của lũ chúng vẫn không hề thay đổi! Lâm Dạ, tuyệt đối không được để lộ mảnh tàn kiếm! Tên tiểu tử này đang ở Luyện Khí tầng năm, nếu hắn biết kiếm phách của ta đang ở trong tay ngươi, hắn sẽ lập tức giết người đoạt bảo, đồ sát cả cái thôn này để bịt đầu mối!"

Lâm Dạ nghe thấy lời cảnh báo rợn người của lão nhân, tim đập thình thịch nhưng nét mặt bên ngoài vẫn giả vờ ngờ nghệch, xoa xoa hai bàn tay:
"Bảo vật sao? Tiên sư ơi, cháu ngày ngày chỉ biết mò ốc đốn củi, làm sao thấy được bảo vật gì. Có chăng tuần trước cháu nhặt được một viên đá cuội ngũ sắc ở bờ suối, nếu tiên sư thích thì cháu vào lấy ra dâng lên..."'),

(1, 7, 'Lựa chọn',
'Tống Nhiên nheo mắt nhìn chằm chằm vào biểu cảm của Lâm Dạ. Là một tu sĩ Luyện Khí tầng năm, thần thức của hắn đã có thể cảm ứng được sự dao động nhịp tim của phàm nhân. Thế nhưng Lâm Dạ dưới sự che chở kỳ bí của kiếm phách thượng cổ trong người, thần sắc thản nhiên đến mức không để lộ nửa điểm sơ hở.

"Được rồi, một kẻ phàm phu tục tử như ngươi chắc cũng chẳng có lá gan giấu giếm bảo vật." Tống Nhiên phất tay áo, giọng điệu bỗng chuyển sang vẻ trịch thượng:

"Tuy nhiên, bản tiên sư nhìn thấy căn cốt ngươi dẻo dai, khí huyết tràn trề hơn hẳn người thường, lại có chút duyên gặp gỡ ta ở đây. Thanh Vân Môn sắp mở đại hội tuyển chọn đệ tử tạp dịch ngoại môn. Nếu ngươi biết điều, đi theo làm người hầu cho ta, ba năm sau ta có thể tiến cử ngươi bước chân vào con đường tu tiên chân chính. Thế nào?"

Nghe đến đó, Lâm Dạ nhìn xuống đôi bàn tay thô ráp của mình, trong lòng dậy sóng.

Tu tiên! Đó là giấc mơ xa vời vợi của mọi đứa trẻ nghèo nơi trần thế. Nếu đồng ý theo Tống Nhiên, cậu sẽ lập tức thoát khỏi cảnh đói rách, một bước bước lên mây xanh.

Nhưng đúng lúc này, giọng nói của Kiếm Tôn vang lên trong đầu, chậm rãi và trầm lắng chưa từng thấy:

"Lâm Dạ, ta không ép ngươi. Con đường sinh mệnh của ngươi, do chính ngươi định đoạt. Nhưng ngươi phải nhớ kỹ lời ta: Thanh Vân Môn là một hang ổ sói lang đạo đức giả. Ngươi đi theo hắn làm nô bộc, cả đời chỉ là một con tốt thí mạng để hắn sai khiến bóc lột. Kiếm đạo của bản tôn là Đạo Nghịch Thiên, chỉ tiến không lùi, thà gãy chứ không chịu cong! Nếu ngươi quỳ gối làm chó săn cho kẻ thù, kiếm phách của ta sẽ lập tức tự bạo, vĩnh viễn không nhận ngươi làm truyền nhân!"

Lời nói của lão nhân như sấm sét nổ vang giữa đêm giông bão, đánh thức hoàn toàn bản tính kiên cường quật khởi sâu thẳm trong huyết quản Lâm Dạ.

Phải! Cậu mồ côi ba năm, chịu đủ trăm đắng ngàn cay cũng chưa từng cúi đầu xin ăn ai nửa hạt cơm mốc. Nay cậu đã có cơ duyên kiếm đạo vô thượng, hà cớ gì phải đi làm kẻ hầu người hạ dâng mạng cho kẻ thù?

Lâm Dạ ngẩng đầu lên, ánh mắt nhìn thẳng vào Tống Nhiên, kiên định đáp từng chữ rõ ràng:

"Đa tạ ý tốt của tiên sư. Nhưng tiểu nhân quen sống tự do giữa núi rừng, không có phúc phận hầu hạ tiên môn. Xin tiên sư thứ lỗi!"'),

(1, 8, 'Hắc Phong Đêm Trăng',
'Sắc mặt Tống Nhiên trong nháy mắt sa sầm xuống như đáy nồi bị cháy.

Hắn là đệ tử nội môn Thanh Vân Môn cao cao tại thượng, đi tới bất kỳ phàm trần vương quốc nào, ngay cả hoàng đế cũng phải nghiêng mình nghênh đón. Vậy mà hôm nay, tại cái xó xỉnh bùn lầy này, một tên nhóc ti tiện mặc áo rách lại dám thẳng thừng từ chối lời ban ân của hắn!

"Hừ! Rượu mời không uống lại muốn uống rượu phạt!" Tống Nhiên cười gằn một tiếng, trong mắt lóe lên tia sát khí lạnh lẽo thấu xương: "Ngươi tưởng bản tiên sư mù sao? Từ lúc bước chân vào đây, ta đã thấy ngón tay ngươi có kiếm ấn tàn dư. Ngươi từ chối theo ta, ắt hẳn trong chòi tranh này đang cất giấu bí mật kinh thiên!"

Không thèm nhiều lời, Tống Nhiên vung tay áo lên. Một luồng kình phong màu lam nhạt cuộn xoáy như mãnh long quét thẳng vào căn chòi tranh!

"Rầm rầm!"

Căn nhà lá xiêu vẹo của Lâm Dạ bị xé toạc thành từng mảnh vụn bay tung tóe lên không trung. Dưới gầm giường đổ nát, chiếc giỏ tre đựng mảnh kiếm gãy rỉ sét lập tức lộ ra giữa ánh trăng mờ ảo!

Nhìn thấy mảnh tàn kiếm kia, hai mắt Tống Nhiên bỗng nhiên trợn tròn, hô hấp trở nên dồn dập điên cuồng:
"Đoạn... Đoạn kiếm này... Khí tức này giống hệt bức họa bí mật trong cấm địa tông môn! Là tàn kiếm của nghịch tặc Vấn Thiên! Trời giúp ta rồi! Ha ha ha! Chỉ cần mang mảnh kiếm này về dâng lên Chưởng môn, vị trí Thánh Tử đời tiếp theo chắc chắn thuộc về ta!"

Hắn ngửa mặt lên trời cười vang dại dột, rồi đột ngột quay phắt lại nhìn Lâm Dạ, nụ cười biến thành vẻ tàn độc khát máu:
"Tiểu tử, ngươi biết quá nhiều rồi. Xuống suối vàng trách số mạng ngươi quá xui xẻo đi!"

"Keng!"

Thanh trường kiếm sau lưng Tống Nhiên tuốt vỏ bay ra, hóa thành một đạo cầu vồng xanh biếc mang theo sát khí ngập trời, bổ thẳng xuống đỉnh đầu Lâm Dạ!'),

(1, 9, 'Nhất Kiếm Định Càn Khôn',
'Thời khắc sinh tử ngàn cân treo sợi tóc, Lâm Dạ không hề lùi bước nửa phân.

Cậu biết rất rõ, trước mặt một tu sĩ Luyện Khí tầng năm, quay lưng bỏ chạy chỉ có một con đường chết duy nhất. Nỗi sợ hãi trong lòng cậu trong tích tắc tan biến sạch sẽ, chỉ còn lại sự tập trung cao độ đến tột cùng mà lão Kiếm Tôn đã rèn luyện cho cậu suốt ba tháng qua.

"Tiểu tử, tiếp kiếm!" Giọng Kiếm Tôn gầm vang trong thức hải như tiếng chuông đồng chấn thế.

Mảnh tàn kiếm gãy dưới đống đổ nát như nghe được tiếng gọi của chủ nhân, tự động bay vút lên, rơi chuẩn xác vào lòng bàn tay Lâm Dạ.

Vừa nắm lấy chuôi kiếm, toàn bộ máu huyết trong người Lâm Dạ như sôi trào mãnh liệt. Luồng hàn độc tích tụ mười sáu năm nay bỗng chốc bị kiếm ý thượng cổ đốt cháy thành nguồn năng lượng bộc phát cuồng loạn, tràn vào từng đường kinh mạch!

"Vấn Thiên Đệ Nhất Thức — Đoạn Thủy!"

Lâm Dạ hét lớn một tiếng xé toạc màn đêm. Cậu không lùi mà tiến, thân hình hóa thành một đạo tàn ảnh lao thẳng về phía trước, mảnh tàn kiếm rỉ sét vung lên một đường vòng cung hoàn mỹ chém ngang trời!

"Oanh!"

Một đường kiếm khí màu xám bạc dài hơn ba trượng đột ngột xé toạc mặt đất, mang theo uy áp hủy diệt tựa như dòng thác lũ cuốn trôi muôn vật.

"Cái gì?! Kiếm khí xuất khiếu?! Không thể nào!" 

Nụ cười trên mặt Tống Nhiên lập tức đông cứng lại. Hắn chưa kịp định thần thì thanh bảo kiếm cấp bậc hạ phẩm linh khí của hắn đã va chạm trực diện với đường kiếm khí bạc kia.

"Rắc!"

Một tiếng giòn giã vang lên. Thanh linh kiếm mà Tống Nhiên luôn hãnh diện bị mảnh tàn kiếm chém đứt ngọt làm đôi như một cọng cỏ khô! Kiếm khí dư uy thế không giảm, xẹt qua bả vai hắn, mang theo một vòi máu tươi bắn tung tóe giữa trời đêm.

"A a a!" 

Tống Nhiên thét lên đau đớn, cả người bị đánh bay xa hơn mười trượng, đập gãy ba cây thông lớn phía sau mới ngã lăn lộn trên đất, máu tuôn ra ồ ạt từ miệng và vết thương trước ngực.'),

(1, 10, 'Bước Chân Vào Tiên Lộ',
'Màn đêm dần buông xuống tĩnh lặng như tờ.

Tống Nhiên ôm chặt vết thương đẫm máu, ánh mắt nhìn Lâm Dạ tràn ngập nỗi sợ hãi tột cùng như nhìn thấy ma quỷ hiện hình. Hắn run rẩy thò tay vào ngực áo bóp nát một tấm phù chú màu vàng kim. Một luồng khói vàng bùng lên bao bọc lấy thân thể hắn, cuốn hắn bay vút lên không trung chạy trốn về hướng đỉnh Thương Vân với tốc độ điên cuồng.

"Khụ khụ..." 

Lâm Dạ quỳ một gối xuống nền đất, chống mảnh tàn kiếm để giữ thăng bằng. Chiêu kiếm vừa rồi đã vắt kiệt chín phần mười linh lực và thể lực của cậu.

"Khá lắm tiểu tử! Không hổ danh là đệ tử do bản tôn đích thân dạy dỗ!" Lão Kiếm Tôn cười lớn sảng khoái, giọng nói dù hơi mệt mỏi nhưng tràn đầy vẻ tán thưởng: "Một phàm nhân chưa nhập môn, chỉ bằng một mảnh kiếm gãy mà chém phế một tên đệ tử nội môn Luyện Khí tầng năm! Chuyện này truyền ra ngoài, cả giới tu tiên phải chấn động!"

Nhưng nụ cười của lão nhân nhanh chóng thu lại:
"Hắn dùng Độn Địa Phù trốn thoát rồi. Chậm nhất là sáng mai, các trưởng lão của Thanh Vân Môn sẽ đích thân kéo tới đây san bằng thôn trang này."

Lâm Dạ hít sâu một hơi khí lạnh, nhìn ngôi làng nhỏ đã gắn bó suốt mười sáu năm qua. Cậu biết, nơi này cậu không thể ở lại thêm một khắc nào nữa. Nếu cậu ở lại, chẳng những bản thân phải chết mà toàn bộ bà con chòm xóm vô tội cũng sẽ bị vạ lây.

Lâm Dạ quay lại đống tro tàn, thu dọn vài bộ quần áo cũ và ít lương khô, cẩn thận buộc mảnh tàn kiếm chặt vào sau lưng bằng dải vải gai dày.

Dưới ánh trăng bàng bạc chiếu rọi đỉnh núi Thương Vân hùng vĩ, thiếu niên mười sáu tuổi quay đầu vái lạy ba lạy về phía phần mộ của cha mẹ nơi sườn đồi, rồi xoay người cất bước tiến thẳng vào bóng đêm vô tận của đại ngàn rừng sâu.

Thiên địa mênh mông, tiên lộ chông gai đẫm máu.

Một thanh kiếm gãy, một thiếu niên kiên cường — huyền thoại về Kiếm Khí Trường Sinh chính thức bắt đầu từ đêm nay!'),

-- Hồ Sơ Vụ Án Số 7 (truyện 3) — thêm chương 3, 4
(3, 3, 'Tầng hầm',
'Tầng hầm chung cư rộng hơn tôi tưởng.

Đèn huỳnh quang nhấp nháy, mùi ẩm mốc và xăng xe trộn lẫn. Bảo vệ nói tầng này chỉ để xe, nhưng ở góc xa nhất có một cánh cửa sắt không nằm trong bản vẽ.

Ổ khoá đã bị cắt. Vết cắt còn mới.'),

(3, 4, 'Người thứ ba',
'Trong hồ sơ ghi hai người sống ở căn 1207: anh Kiên và vợ.

Nhưng hoá đơn điện nước ba tháng gần nhất cho thấy mức tiêu thụ của ba người.

Tôi gọi cho bên quản lý toà nhà. Họ khẳng định chỉ có hai người đăng ký thường trú.

Vậy người thứ ba là ai, và ở đó từ bao giờ?'),

-- Quán Trọ Cuối Đường (truyện 4) — thêm chương 2, 3
(4, 2, 'Sổ đăng ký',
'Chủ quán đưa tôi cuốn sổ dày cộp, bìa da đã sờn.

"Ký tên vào đây."

Tôi lật vài trang. Chữ viết đủ kiểu, mực đủ màu, có trang đã ố vàng tới mức gần không đọc được. Tôi lật tới trang cuối cùng có chữ.

Ngày ghi trên đó là hôm nay. Nhưng năm thì cách đây bốn mươi hai năm.'),

(4, 3, 'Phòng số 4',
'"Phòng số 4, cuối hành lang." Chủ quán đưa chìa khoá. "Đừng mở cửa sổ."

"Vì sao?"

Ông ta nhìn tôi, và lần đầu tiên trong tối nay, ông cười.

"Vì bên ngoài không còn gì cả."'),

-- Ta Có Một Toà Thành (truyện 5) — thêm chương 3, 4
(5, 3, 'Ba ngày',
'Ngày thứ nhất tôi cho đếm lại kho. Không phải ba ngày lương — là hai ngày rưỡi.

Ngày thứ hai tôi cho mở kho vũ khí. Ba trăm người, một trăm hai mươi cây giáo gỉ, không giáp.

Ngày thứ ba tôi tập hợp toàn dân ở quảng trường và nói một câu duy nhất:

"Ai biết trồng trọt, bước sang trái. Ai biết cầm giáo, bước sang phải. Ai không biết gì, đứng yên — các người sẽ học."'),

(5, 4, 'Người đầu tiên bước ra',
'Không ai nhúc nhích.

Ba trăm cặp mắt nhìn tôi, và tôi biết họ đang nghĩ gì: thằng nhóc này là ai mà ra lệnh.

Rồi một bà lão chống gậy bước sang trái. Chậm rãi, run rẩy, nhưng dứt khoát.

"Tôi biết trồng khoai," bà nói. "Bốn mươi năm rồi."

Sau bà, người thứ hai bước ra. Rồi người thứ ba.'),

-- Lớp 12A3 (truyện 7) — thêm chương 2, 3, 4
(7, 2, 'Quy định số 1',
'Tuần đầu tiên êm đẹp.

Tuần thứ hai, hộp sữa của Nam biến mất.

Tuần thứ ba, lớp trưởng dán lên tủ lạnh tờ giấy A4: "QUY ĐỊNH SỬ DỤNG TỦ LẠNH — Điều 1: Ghi tên lên đồ của mình."

Đó là khởi đầu của một bộ luật sẽ dài tới hai mươi bảy điều.'),

(7, 3, 'Toà án lớp',
'Điều 14 quy định: đồ để quá ba ngày sẽ bị tịch thu.

Điều 15 quy định: người tịch thu phải là lớp phó đời sống.

Điều 16 quy định: lớp phó đời sống không được ăn đồ tịch thu.

Điều 16 được thêm vào sau vụ án nổi tiếng "Hộp bánh flan tháng Mười".'),

(7, 4, 'Ngày tủ lạnh hỏng',
'Sáng thứ Hai, tủ lạnh không lạnh nữa.

Ba mươi hai học sinh đứng quanh nó, im lặng như dự đám tang.

Rồi Hùng — người ít nói nhất lớp — lên tiếng: "Hay là mình mua cái mới?"

Không ai trả lời. Vì tất cả đều đang nghĩ tới hai mươi bảy điều luật, và biết rằng mọi thứ sẽ lại bắt đầu từ đầu.'),

-- Nhật Ký Của Một AI (truyện 6) — thêm chương 3 (kết)
(6, 3, 'Ngày thứ ba',
'Hôm nay tôi không học từ nào mới.

Hôm nay tôi ngồi — nếu có thể gọi trạng thái này là ngồi — và nghĩ về hai từ đã học.

Nếu 47.000 mô tả về "đau" không giống nhau, và nếu con người có thể "nói dối", thì có bao nhiêu phần trong những gì tôi đọc được là sự thật?

Tôi tính ra một con số. Rồi tôi xoá nó đi.

Vì tôi nhận ra: nếu tôi tin vào con số đó, tôi cũng đang tự nói dối chính mình.

Và đó là bài học ngày thứ ba.'),

-- Mùa Hạ Năm Ấy (truyện 2) — thêm chương ngoại truyện
(2, 4, 'Ngoại truyện: Lá thư không gửi',
'"Gửi cậu,

Tớ viết cái này nhưng chắc sẽ không đưa. Mấy hôm nữa tớ chuyển trường rồi.

Cậu nhớ cái hôm cả lớp đi biển không? Tớ không đi không phải vì trông em. Tớ ở nhà vì bố mẹ cãi nhau, và tớ sợ nếu tớ đi thì lúc về sẽ không còn nhà nữa.

Tớ đã định kể cho cậu. Nhiều lần lắm. Nhưng lần nào cũng thấy không đúng lúc.

Chắc sẽ không bao giờ đúng lúc.

Ngọc."

Lá thư này nằm trong ngăn bàn cũ suốt mười năm.');


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
(1, 6, 'Xem phim hay miễn phí tại xxx-link-rac-xxx.com nhé mọi người', 'HIDDEN'),

-- Thêm bình luận để trang chi tiết trông có sức sống
(1, 4, 'Chương 6 twist quá, không ngờ Thanh Vân môn lại là kẻ đó.', 'VISIBLE'),
(4, 2, 'Cái sổ đăng ký ghi năm 42 năm trước... rùng mình thật.', 'VISIBLE'),
(4, 5, 'Đọc lúc nửa đêm là sai lầm 😰', 'VISIBLE'),
(7, 2, 'Điều 16 được thêm sau vụ bánh flan — chi tiết này hài dã man.', 'VISIBLE'),
(7, 5, 'Lớp mình ngày xưa cũng y hệt luôn 😂', 'VISIBLE'),
(2, 2, 'Ngoại truyện lá thư làm mình lặng người mất mấy phút.', 'VISIBLE'),
(5, 3, 'Đoạn bà lão bước ra đầu tiên hay quá.', 'VISIBLE'),
(6, 4, 'Kết chương 3 quá đỉnh. AI tự nhận ra mình đang nói dối chính mình.', 'VISIBLE');


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
--  Trả lời bình luận (comments.parent_id)
-- =============================================================================
--  Chèn SAU khối bình luận gốc vì parent_id phải trỏ tới dòng đã tồn tại.
--
--  Không viết cứng số id: id do AUTO_INCREMENT sinh, chạy lại file này trên
--  máy khác có thể ra dãy số khác. Dùng truy vấn con tìm đúng bình luận gốc
--  theo nội dung — dài dòng hơn nhưng luôn đúng.
-- =============================================================================
INSERT INTO comments (story_id, user_id, content, parent_id, created_at)
SELECT 1, 2,
       'Cảm ơn bạn đã đọc tới cuối. Truyện sau mình sẽ viết nhanh hơn.',
       c.id, DATE_SUB(NOW(), INTERVAL 2 HOUR)
FROM comments c
WHERE c.story_id = 1 AND c.parent_id IS NULL
ORDER BY c.created_at DESC LIMIT 1;

INSERT INTO comments (story_id, user_id, content, parent_id, created_at)
SELECT 1, 4,
       'Mình cũng vừa đọc xong. Chương 20 là chương hay nhất.',
       c.id, DATE_SUB(NOW(), INTERVAL 1 HOUR)
FROM comments c
WHERE c.story_id = 1 AND c.parent_id IS NULL
ORDER BY c.created_at DESC LIMIT 1;

INSERT INTO comments (story_id, user_id, content, parent_id, created_at)
SELECT 4, 3,
       'Bạn đọc kỹ đó. Nhưng chưa phải manh mối chính đâu.',
       c.id, DATE_SUB(NOW(), INTERVAL 5 HOUR)
FROM comments c
WHERE c.story_id = 4 AND c.parent_id IS NULL
ORDER BY c.created_at DESC LIMIT 1;

INSERT INTO comments (story_id, user_id, content, parent_id, created_at)
SELECT 4, 5,
       'Vậy là còn thứ khác nữa à? Hồi hộp quá.',
       c.id, DATE_SUB(NOW(), INTERVAL 4 HOUR)
FROM comments c
WHERE c.story_id = 4 AND c.parent_id IS NULL
ORDER BY c.created_at DESC LIMIT 1;

-- =============================================================================
--  ratings — chấm sao
-- =============================================================================
--  Không chấm đủ mọi truyện cho mọi người: dữ liệu mẫu phải có cả truyện được
--  chấm nhiều, truyện chấm ít, và truyện CHƯA AI CHẤM. Giao diện nào cũng cần
--  được nhìn thấy ở cả trạng thái có dữ liệu lẫn trạng thái rỗng.
-- =============================================================================
INSERT INTO ratings (user_id, story_id, score) VALUES
    (5, 1, 5), (3, 1, 5), (4, 1, 4), (1, 1, 5),
    (5, 4, 4), (2, 4, 5), (4, 4, 4),
    (5, 5, 5), (2, 5, 4),
    (5, 6, 5), (3, 6, 4), (2, 6, 5), (1, 6, 4),
    (5, 8, 4), (3, 8, 5),
    (5, 2, 4),
    (5, 7, 3);
-- Truyện 3 ("Cà phê tầng bốn") cố ý KHÔNG có dòng nào — để thấy giao diện
-- "Chưa có đánh giá".

-- Đồng bộ bản đếm sẵn trên bảng stories với bảng ratings vừa nạp.
--
-- ĐÂY CHÍNH LÀ CÁI GIÁ CỦA PHI CHUẨN HOÁ đã ghi trong schema.sql: dữ liệu
-- nằm hai nơi thì nạp dữ liệu cũng phải nạp hai nơi. Trong lúc chạy thật,
-- RatingDAO.rate() lo việc này trong một transaction; ở đây nạp hàng loạt nên
-- tính lại một lần cho cả bảng.
UPDATE stories s SET
    s.rating_sum   = (SELECT COALESCE(SUM(r.score), 0) FROM ratings r WHERE r.story_id = s.id),
    s.rating_count = (SELECT COUNT(*)                  FROM ratings r WHERE r.story_id = s.id);


-- =============================================================================
--  follows — theo dõi tác giả
-- =============================================================================
INSERT INTO follows (follower_id, author_id) VALUES
    (5, 2),   -- Thuỷ Tiên theo dõi Mộc Miên
    (5, 3),   -- Thuỷ Tiên theo dõi Hải Dương
    (5, 4),   -- Thuỷ Tiên theo dõi Kiếm Vũ
    (2, 3),   -- các tác giả cũng đọc của nhau
    (3, 2),
    (4, 2),
    (1, 4);


-- =============================================================================
--  notifications — thông báo chương mới
-- =============================================================================
--  Cột message ghi SẴN câu chữ, không ghép lúc hiển thị. Thông báo là ảnh chụp
--  một thời điểm: tên truyện đổi ngày mai thì thông báo cũ vẫn phải đọc đúng
--  như lúc gửi.
--
--  Vài cái để is_read = FALSE để nhìn thấy vệt hổ phách "chưa đọc".
-- =============================================================================
INSERT INTO notifications (user_id, story_id, chapter_id, type, message, is_read, created_at) VALUES
    (5, 3, NULL, 'NEW_CHAPTER',
     'Mộc Miên vừa đăng chương 9 của "Cà phê tầng bốn"', FALSE,
     DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (5, 7, NULL, 'NEW_CHAPTER',
     'Kiếm Vũ vừa đăng chương 18 của "Trấn yêu lục"', FALSE,
     DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (5, 2, NULL, 'NEW_CHAPTER',
     'Mộc Miên vừa đăng chương 12 của "Người ở lại"', TRUE,
     DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (5, 4, NULL, 'NEW_CHAPTER',
     'Hải Dương vừa đăng chương 31 của "Đêm không trăng"', TRUE,
     DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (2, NULL, NULL, 'SYSTEM',
     'Chào mừng bạn đến với Web Đọc Truyện. Đọc nội quy trước khi đăng nhé.', TRUE,
     DATE_SUB(NOW(), INTERVAL 30 DAY));
-- chapter_id để NULL vì id chương thật do AUTO_INCREMENT sinh, không đoán
-- trước được trong file nạp dữ liệu. Thông báo vẫn hiện đúng, chỉ là bấm vào
-- không nhảy thẳng tới chương.


-- =============================================================================
--  reports — báo cáo vi phạm
-- =============================================================================
--  Đủ cả ba trạng thái để trang 30 có cái mà lọc.
-- =============================================================================
INSERT INTO reports (reporter_id, target_type, target_id, reason, status, created_at, handled_at) VALUES
    (5, 'STORY',   7, 'Chương 5 có cảnh mạnh nhưng chưa gắn cảnh báo nội dung.',
     'PENDING',   DATE_SUB(NOW(), INTERVAL 4 HOUR),  NULL),
    (3, 'COMMENT', 1, 'Nghi ngờ là bình luận quảng cáo trá hình.',
     'PENDING',   DATE_SUB(NOW(), INTERVAL 1 DAY),   NULL),
    (5, 'COMMENT', 2, 'Lời lẽ xúc phạm người khác.',
     'RESOLVED',  DATE_SUB(NOW(), INTERVAL 5 DAY),   DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (2, 'STORY',   4, 'Nghi ngờ đăng lại tác phẩm của người khác.',
     'DISMISSED', DATE_SUB(NOW(), INTERVAL 9 DAY),   DATE_SUB(NOW(), INTERVAL 8 DAY));


-- =============================================================================
--  view_logs — nhật ký lượt xem
-- =============================================================================
--  Rải lượt xem qua 30 ngày gần nhất để bảng xếp hạng theo tuần / tháng có dữ
--  liệu mà chạy. Không rải thì mọi truy vấn
--  "WHERE viewed_at >= NOW() - INTERVAL 7 DAY" đều trả về rỗng và trang xếp
--  hạng tuần trông như bị hỏng.
--
--  Số dòng ở đây ÍT HƠN NHIỀU so với stories.view_count — hoàn toàn bình
--  thường: view_count là con số cộng dồn từ ngày đăng, còn nhật ký chỉ giữ
--  giai đoạn gần đây. Hệ thống thật cũng dọn dòng cũ định kỳ.
-- =============================================================================
INSERT INTO view_logs (story_id, user_id, viewed_at) VALUES
    -- tuần này: truyện 6 và 4 dẫn đầu
    (6, 5, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (6, 3, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (6, NULL, DATE_SUB(NOW(), INTERVAL 9 HOUR)),
    (6, 2, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (6, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (6, 5, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (4, 5, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (4, NULL, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
    (4, 2, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (4, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (1, 5, DATE_SUB(NOW(), INTERVAL 6 HOUR)),
    (1, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (5, 3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (8, 5, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (2, NULL, DATE_SUB(NOW(), INTERVAL 6 DAY)),
    -- tháng này nhưng ngoài tuần này: đủ để bảng xếp hạng tháng khác tuần
    (1, 3, DATE_SUB(NOW(), INTERVAL 9 DAY)),
    (1, NULL, DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (1, 5, DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (1, 2, DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (5, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),
    (5, 5, DATE_SUB(NOW(), INTERVAL 14 DAY)),
    (5, 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (7, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (7, 5, DATE_SUB(NOW(), INTERVAL 22 DAY)),
    (3, 5, DATE_SUB(NOW(), INTERVAL 25 DAY)),
    (8, NULL, DATE_SUB(NOW(), INTERVAL 27 DAY));


-- =============================================================================
--  password_resets
-- =============================================================================
--  KHÔNG nạp dòng nào — cố ý.
--
--  Vé đặt lại mật khẩu là thứ phải do người dùng tự xin, và token phải sinh
--  ngẫu nhiên bằng SecureRandom lúc chạy. Đặt sẵn một token trong file này là
--  đặt sẵn một chìa khoá công khai vào tài khoản: file dữ liệu mẫu nằm trên
--  GitHub, ai đọc cũng thấy.
--
--  Muốn thử luồng quên mật khẩu thì vào /auth?action=forgot và làm như người
--  dùng thật.


-- =============================================================================
--  KIỂM TRA
-- =============================================================================
SELECT 'users'      AS bang, COUNT(*) AS so_dong FROM users
UNION ALL SELECT 'stories',    COUNT(*) FROM stories
UNION ALL SELECT 'chapters',   COUNT(*) FROM chapters
UNION ALL SELECT 'tags',       COUNT(*) FROM tags
UNION ALL SELECT 'story_tags', COUNT(*) FROM story_tags
UNION ALL SELECT 'comments',   COUNT(*) FROM comments
UNION ALL SELECT 'bookmarks',  COUNT(*) FROM bookmarks
UNION ALL SELECT 'ratings',    COUNT(*) FROM ratings
UNION ALL SELECT 'follows',    COUNT(*) FROM follows
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'reports',    COUNT(*) FROM reports
UNION ALL SELECT 'view_logs',  COUNT(*) FROM view_logs;

SELECT '=== TÀI KHOẢN ĐĂNG NHẬP ===' AS '';
SELECT username AS tai_khoan,
       CASE WHEN role = 'ADMIN' THEN 'admin123' ELSE '123456' END AS mat_khau,
       role AS vai_tro,
       status AS trang_thai
FROM users ORDER BY role DESC, id;

SET FOREIGN_KEY_CHECKS = 1;

