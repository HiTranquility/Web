-- =============================================================================
--  MỞ RỘNG KHO TRUYỆN TOÀN DIỆN: TRUYỆN TRANH (COMIC) & TRUYỆN CHỮ CHẤT LƯỢNG CAO
-- =============================================================================

USE webdoctruyen;
SET NAMES utf8mb4;

-- 1. THÊM TRUYỆN TRANH & TRUYỆN CHỮ MỚI
INSERT INTO stories (id, title, slug, description, author_id, status, progress, story_type, view_count, cover_url) VALUES
(10, 'Thợ Săn Hầm Ngục: Kẻ Thức Tỉnh Cuối Cùng', 'tho-san-ham-nguc-ke-thuc-tinh-cuoi-cung',
 'Khi các cánh cổng không gian nối liền dị giới mở ra, nhân loại bước vào kỷ nguyên của Thợ Săn. Trần Minh - thợ săn hạng E bét bảng bị coi là phế vật, trong một lần sinh tử tại Hầm Ngục Kép đã vô tình kích hoạt Hệ Thống Độc Quyền vô hạn! Hành trình từ kẻ yếu nhất bước lên ngai vàng chúa tể bóng đêm bắt đầu.',
 4, 'PUBLISHED', 'ONGOING', 'COMIC', 34200, '/assets/images/covers/cover-comic-1.png'),

(11, 'Cuộc Sống Thường Nhật Của Hoàng Tử Mèo', 'cuoc-song-thuong-nhat-cua-hoang-tu-meo',
 'Đại hoàng tử Leo của Miêu Quốc vì tai nạn ma pháp bí ẩn mà bị biến thành một chú mèo mướp béo múp míp rơi xuống ban công nhà cô nàng họa sĩ Hân Vy. Từ đó, những tình huống dở khóc dở cười giữa con mèo hoàng gia kiêu kỳ và cô chủ mê thú cưng bắt đầu diễn ra mỗi ngày!',
 2, 'PUBLISHED', 'ONGOING', 'COMIC', 21500, '/assets/images/covers/cover-comic-2.png'),

(12, 'Toàn Chức Pháp Sư: Thần Cấp Khởi Đầu', 'toan-chuc-phap-su-than-cap-khoi-dau',
 'Tỉnh lại sau một giấc ngủ dài, thế giới bỗng nhiên biến đổi hoàn toàn. Trường học không còn dạy khoa học mà giảng dạy Ma Pháp nguyên tố! Trong khi bạn học chỉ thức tỉnh được một hệ ma pháp sơ cấp, Diệp Phong lại thức tỉnh song hệ ma pháp siêu cấp Lôi - Hỏa ngay từ ngày đầu tiên!',
 3, 'PUBLISHED', 'ONGOING', 'TEXT', 18900, '/assets/images/covers/cover-magic.png'),

(13, 'Ám Dạ Trùng Sinh: Vương Phi Nghịch Thiên', 'am-da-trung-sinh-vuong-phi-nghich-thien',
 'Kiếp trước nàng là đệ nhất sát thủ kiêm bác sĩ quân y hiện đại bị phản bội bỏ mạng. Trùng sinh vào thân xác đích nữ phế sài của tướng phủ bị hãm hại đẩy xuống vực sâu, nàng thề kiếp này kẻ nợ nàng phải trả bằng máu! Y thuật nghịch thiên, độc bộ thiên hạ, một bước lên ngôi vị cửu ngũ chí tôn.',
 5, 'PUBLISHED', 'ONGOING', 'TEXT', 26400, '/assets/images/covers/cover-reborn.png')
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    story_type = VALUES(story_type),
    description = VALUES(description),
    cover_url = VALUES(cover_url);

-- 2. GẮN THỂ LOẠI (story_tags)
INSERT IGNORE INTO story_tags (story_id, tag_id) VALUES
(10, 2), (10, 10),       -- Solo Hunter: Huyền huyễn, Phiêu lưu
(11, 6), (11, 7), (11, 3),-- Hoàng tử mèo: Hài hước, Học đường, Ngôn tình
(12, 2), (12, 10), (12, 8),-- Toàn chức pháp sư: Huyền huyễn, Phiêu lưu, Khoa học
(13, 1), (13, 3), (13, 9); -- Vương phi nghịch thiên: Tiên hiệp, Ngôn tình, Lịch sử

-- 3. TẠO CHƯƠNG CHO TRUYỆN TRANH (Story 10 & 11)
-- Lưu ý: Truyện tranh có content để rỗng '', ảnh nằm trong chapter_pages
INSERT INTO chapters (id, story_id, chapter_no, title, content, is_vip, coin_price) VALUES
(93, 10, 1, 'Cửa Ải Tử Thần', '', 0, 0),
(94, 10, 2, 'Thức Tỉnh Hệ Thống Thần Cấp', '', 0, 0),
(95, 10, 3, 'Quét Sạch Hầm Ngục Sói Băng', '', 0, 0),
(96, 10, 4, 'Bóng Đêm Thao Túng: Khắc Tinh Của Quái Vật', '', 1, 5),

(97, 11, 1, 'Vị Khách Không Mời Rơi Từ Trên Trời', '', 0, 0),
(98, 11, 2, 'Hoàng Gia Không Thích Pate', '', 0, 0),
(99, 11, 3, 'Bí Mật Đêm Trăng Tròn', '', 1, 3)
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    is_vip = VALUES(is_vip),
    coin_price = VALUES(coin_price);

-- 4. TẠO CHƯƠNG CHI TIẾT CHO TRUYỆN CHỮ MỚI (Story 12: Toàn Chức Pháp Sư)
INSERT INTO chapters (id, story_id, chapter_no, title, content, is_vip, coin_price) VALUES
(100, 12, 1, 'Thế Giới Ma Pháp Đảo Lộn', 
'Tiếng chuông báo thức reng lên inh ỏi phá tan giấc ngủ say của Diệp Phong. Cậu vươn vai ngồi dậy, ngáp dài một cái rồi với tay lấy chiếc áo đồng phục trên móc treo. Nhưng ngay khoảnh khắc ánh mắt cậu chạm vào cuốn sách giáo khoa trên bàn học, toàn thân Diệp Phong bỗng đông cứng như vừa bị sét đánh trúng.

"Đại cương Nhập môn Ma pháp Nguyên tố sơ cấp... NXB Giáo Dục Ma Pháp Thần Châu?"

Diệp Phong dụi mạnh hai mắt, tưởng chừng mình đang nằm mơ giữa ban ngày. Cậu lật vội vài trang sách. Bên trong không hề có công thức lượng giác hay định luật Newton quen thuộc, mà toàn là những sơ đồ tinh đồ ma pháp phức tạp, biểu đồ hấp thụ ma thạch cùng những dòng cảnh báo rợn người về các loài ma thú hoang dã ngoài kết giới thành phố!

"Gì thế này? Đùa nhau à? Hôm nay là Cá tháng Tư sao?"

Cậu lao nhanh ra ban công nhìn xuống phố. Khung cảnh đường phố vẫn là thành phố Đông Hải quen thuộc, xe cộ tấp nập, nhưng phía trên bầu trời lại có những tuần tra viên cưỡi trên lưng Liệt Ưng dực thú bay lượn! Các cột đèn giao thông hoạt động bằng tinh thạch phát quang, và trên màn hình quảng cáo khổng lồ tại trung tâm thương mại đang phát bản tin thời sự: "Thị trưởng thành phố vừa khen thưởng Ma Pháp Sư Cao Cấp vì đã đẩy lùi đợt tấn công của Bộc Viêm Cự Viên tại tiền tuyến phía Nam!"

Diệp Phong nuốt nước bọt ực một tiếng, trán rịn đầy mồ hôi hột. Thế giới khoa học mà cậu sinh sống mười bảy năm qua... đã bị thay thế hoàn toàn bởi một nền văn minh ma pháp kỳ dị!

"Hôm nay là lễ thức tỉnh ma pháp của trường Trung Học Ma Pháp Thiên Nam!" Diệp Phong sực nhớ ra lời người mẹ vừa nhắc sáng nay. Nếu không thể thức tỉnh được ma pháp, cậu sẽ mãi mãi chỉ là một thường dân vô dụng, không có quyền bước chân vào giảng đường đại học!', 0, 0),

(101, 12, 2, 'Lễ Thức Tỉnh Chấn Động',
'Hội trường lớn của trường Trung Học Ma Pháp Thiên Nam chật ních hơn một nghìn học sinh và phụ huynh. Không khí căng thẳng đến nghẹt thở như chuẩn bị bước vào một cuộc đại chiến sinh tử. Trên bục cao, một khối Thuỷ Tinh Thức Tỉnh trong suốt phát ra ánh sáng bàng bạc u uẩn.

"Người tiếp theo: Triệu Khải!" Giọng thầy hiệu trưởng vang rền qua loa phóng thanh.

Một nam sinh dáng vẻ tự tin bước lên bục, đặt hai tay run rẩy lên quả cầu pha lê. Mọi ánh mắt dưới khán đài đều nín thở dõi theo. Sau vài giây im ắng, một luồng ánh sáng màu đỏ rực như lửa bốc lên ngùn ngụt, thiêu đốt không khí xung quanh!

"Là Hỏa hệ! Ma pháp công kích bậc nhất giai đoạn đầu!"
"Tuyệt vời quá! Triệu gia quả nhiên sinh ra kỳ tài!"

Tiếng tán dương và vỗ tay vang dội khắp khán phòng. Triệu Khải ngẩng cao đầu bước xuống, không quên liếc nhìn Diệp Phong với ánh mắt tràn đầy sự khinh bỉ: "Cậu liệu mà chuẩn bị tinh thần đi, tên đội sổ. Đừng để quả cầu thức tỉnh chỉ phát ra màu xám xịt của phế vật vô thuộc tính!"

Diệp Phong hít một hơi thật sâu, từng bước điềm tĩnh bước lên bục giảng.

Khi bàn tay cậu vừa chạm vào bề mặt mát lạnh của viên thuỷ tinh thức tỉnh, một luồng xung điện kỳ lạ bỗng từ lòng bàn tay chạy dọc thẳng vào sâu trong đan điền tinh thần hải. Trong tâm trí cậu, một khoảng không gian vô tận tối tăm đột ngột bừng sáng. Bảy ngôi sao tinh đồ màu đỏ rực rỡ như nham thạch nóng chảy bắt đầu liên kết thành hình!

"Hỏa hệ! Cậu ta cũng thức tỉnh Hỏa hệ!" Dưới khán đài vang lên những tiếng xôn xao kinh ngạc.

Thế nhưng, điều mà không ai ở hiện trường nhận ra chính là: ngay phía sau những tinh tử màu đỏ đang rực cháy, một đám mây sấm sét màu tím thẫm cực kỳ hung hãn đang cuộn trào sấm chớp cuồng nộ!

"Một... hai... ba... bảy hạt tinh tử Lôi điện!" Diệp Phong chấn động tâm can.
Song hệ ma pháp! Cậu không chỉ thức tỉnh Hỏa hệ, mà còn thức tỉnh Lôi hệ - hệ ma pháp quý hiếm và có sức sát thương khủng khiếp nhất toàn cầu, xác suất chỉ một phần mười vạn người mới có!', 0, 0),

(102, 12, 3, 'Khống Chế Tinh Tử: Bước Chân Ma Đạo',
'Trở về căn phòng trọ nhỏ hẹp, Diệp Phong lập tức khoá chặt then cửa. Cậu ngồi xếp bằng trên giường theo tư thế minh tưởng ma pháp, nhắm mắt tập trung toàn bộ tinh thần lực vào thế giới tinh thần hải bên trong cơ thể.

Trước mắt cậu, hai dải tinh vân rực rỡ đang trôi nổi song song: một bên là biển lửa đỏ thắm hừng hực nhiệt lượng, một bên là lôi điện tím biếc gầm thét dữ dội.

"Quy tắc của ma pháp sơ cấp: muốn thi triển được một ma pháp hoàn chỉnh, ma pháp sư phải dùng ý niệm thuần thục nối liền bảy hạt tinh tử thành một đường tinh quỹ!"

Diệp Phong thở nhẹ, bắt đầu điều động ý niệm tiếp cận hạt tinh tử Hỏa hệ đầu tiên. Khác với những học sinh bình thường phải mất cả tuần lễ mới làm quen được với sự hung hãn của ma lực, linh hồn hai kiếp người của Diệp Phong sở hữu sức mạnh tinh thần vượt trội đến khó tin! Hạt tinh tử đỏ rực vừa chạm vào ý thức của cậu lập tức ngoan ngoãn đứng yên tại chỗ, không hề phản kháng.

"Một hạt... hai hạt... ba hạt!"

Chỉ trong một buổi tối duy nhất, Diệp Phong đã thuần phục được bốn hạt tinh tử. Tốc độ này nếu truyền ra ngoài chắc chắn sẽ làm chấn động toàn bộ giới ma pháp sư tỉnh Đông Hải! Thông thường, một thiên tài xuất sắc cũng phải mất ít nhất ba tháng khổ luyện mới có thể kết nối được tinh quỹ đầu tiên để phóng ra ma pháp Sơ Cấp bậc 1 "Hỏa Diễm - Bốc Cháy".

"Chưa đủ, mình phải đồng thời tu luyện cả Lôi hệ." Diệp Phong chuyển ý niệm sang vùng sấm sét tím. Từng tia điện nhỏ li ti xẹt qua các đầu ngón tay cậu trong thế giới thực, tạo nên những tiếng tanh tách lách tách trong bóng đêm.', 0, 0),

(103, 12, 4, 'Thực Chiến Tại Thao Trường Rừng Rậm',
'Hai tháng sau kỳ thức tỉnh, toàn bộ học sinh lớp chọn của trường Thiên Nam được đưa tới thao trường hoang dã ngoại ô để tham gia khóa huấn luyện sinh tồn thực tế. Khu vực này được bao bọc bởi hàng rào kết giới phòng hộ, nhưng bên trong vẫn có sự xuất hiện của những con Độc Thứ Trư và Ma Lang cấp thấp chuyên đi săn mồi.

Thầy giáo tổng phụ trách giọng nghiêm nghị: "Mỗi tổ ba người, nhiệm vụ là tìm kiếm và mang về ít nhất ba huy hiệu ma thú. Đội nào hoàn thành xuất sắc sẽ được nhà trường cấp phát ba mươi viên Ma Tinh Thạch để hỗ trợ tu luyện!"

Diệp Phong được xếp chung nhóm với Trương Vũ - một cậu bạn mập mạp nhút nhát thức tỉnh Thổ hệ, và Linh Chi - cô bạn lớp phó thức tỉnh Phong hệ.

Khi cả nhóm tiến sâu vào thung lũng sương mù, mặt đất dưới chân bỗng nhiên rung chuyển nhẹ. Từ trong bụi gai rậm rạp, một con Độc Giáp Trư với cặp răng nanh nhọn hoắt dài nửa mét, toàn thân phủ lớp vảy gai đen sì bất ngờ lao vọt ra như một chiếc xe tải mất phanh!

"Cẩn thận! Nó lao về phía chúng ta kìa!" Trương Vũ sợ hãi hét toáng lên, tay chân quýnh quáng quên mất cả cách tụng niệm ma pháp Thổ Thuẫn.

"Để mình cản nó lại!" Linh Chi vội vã vung tay giải phóng một ngọn gió lốc "Phong Quỹ - Tật Hành", nhưng lớp da dày của con quái vật chỉ hơi chững lại trong tích tắc rồi càng trở nên hung dữ lồng lộn hơn.', 0, 0),

(104, 12, 5, 'Lôi Hỏa Song Quyền Bạo Liệt (VIP)',
'Trong gang tấc hiểm nguy, Diệp Phong bước lên chắn trước mặt hai người bạn. Đôi mắt cậu ánh lên những tia sáng màu tím thẫm và đỏ rực đan xen.

"Trương Vũ, dựng Thổ Tường góc bốn mươi lăm độ ép nó chạy chậm lại! Linh Chi, dùng gió thổi tản sương mù quanh người nó!" Giọng Diệp Phong điềm tĩnh nhưng chứa đựng uy áp khiến hai người bạn lập tức hành động theo bản năng.

Bị bức tường đất ép góc, con Độc Giáp Trư giận dữ gầm lên, dồn toàn lực lao thẳng vào Diệp Phong. Khoảng cách chỉ còn chưa đầy mười mét!

Năm mét!

Ba mét!

"Hỏa Diễm — Phần Liệt!"

Diệp Phong vung tay phải, bảy hạt tinh tử đỏ rực lập tức liên kết hoàn mỹ thành một đường tinh quỹ rực cháy. Một quả cầu lửa khổng lồ với nhiệt lượng khủng khiếp phóng thẳng vào đôi mắt của con ma thú, làm nổ tung lớp phòng ngự phía trước của nó!

Chưa dừng lại ở đó, ngay khi con quái vật đau đớn lảo đảo, tay trái của Diệp Phong đồng thời giơ cao. Bầu trời âm u bỗng giáng xuống một tia sét tím chói lòa:

"Lôi Ấn — Mãng Ngân!!"

"OÀNGGGGGGGGGGGGGGG!"

Tia lôi điện hình mãng xà tím cuồng bạo đánh trúng hồng tâm, giật tung toàn bộ lớp giáp gai của con quái thú thành tro bụi, hạ gục nó tại chỗ trong một chiêu duy nhất! Trương Vũ và Linh Chi đứng chôn chân tại chỗ, há hốc mồm không nói nên lời trước sức mạnh ma pháp kinh hoàng của Diệp Phong...', 1, 5)
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    content = VALUES(content),
    is_vip = VALUES(is_vip),
    coin_price = VALUES(coin_price);

-- 5. CẬP NHẬT NỘI DUNG DÀI & ĐẦY CẢM XÚC CHO "MÙA HẠ NĂM ẤY" (Story 2)
UPDATE chapters SET 
title = 'Sân ga ngày gặp lại',
content = 'Cơn mưa rào đầu hạ bất chợt đổ xuống thành phố biển, làm nhòa đi những ô kính mờ hơi nước tại phòng chờ sân ga số 3. Tiếng còi tàu hú lên từng hồi trầm đục từ phía xa xa, báo hiệu chuyến tàu đêm muộn sắp sửa rời bến.

Hạ An kéo cao cổ chiếc áo khoác măng tô màu be, đưa bàn tay khẽ lau đi lớp sương mờ bám trên cửa kính. Mười năm rồi. Kể từ cái ngày cô ôm chiếc balo con cóc bước lên chuyến tàu rời khỏi thị trấn nhỏ này để lên thủ đô nhập học, cô chưa từng một lần quay trở lại. Nơi này lưu giữ quá nhiều kỷ niệm, mà mỗi một kỷ niệm đều gắn liền với một bóng hình mà cô ngỡ rằng mình đã hoàn toàn chôn chặt vào dĩ vãng.

"Quý khách đi chuyến tàu SE4 xin vui lòng chuẩn bị vé..."

Giọng cô phát thanh viên vang lên đều đều qua loa thông báo. Hạ An xách chiếc vali nhỏ, hòa vào dòng người hối hả bước ra sân ga. Gió biển đêm mang theo vị mặn nồng nàn phả thẳng vào gương mặt cô, làm tung bay những lọn tóc đen dài chấm vai.

Ngay tại khúc quanh của hàng ghế gỗ sồi cũ kỹ ở cuối sân ga, một bóng người đàn ông cao gầy, khoác chiếc áo dạ màu xám tro đang đứng lặng lẽ châm thuốc. Đốm lửa đỏ lập lòe trong màn đêm mưa gió, soi rõ góc nghiêng gương mặt cương nghị với hàng chân mày hơi cau lại quen thuộc.

Bàn tay kéo vali của Hạ An bỗng run lên bần bật. Trái tim cô như ngừng đập trong một tích tắc.

Là Vũ.

Người thiếu niên năm mười bảy tuổi từng đạp xe chở cô qua từng con dốc phủ đầy hoa phượng đỏ, người từng cùng cô trú mưa dưới mái hiên của tiệm sách cũ và vụng về chia cho cô nửa củ khoai nướng nóng hổi... Người mà cô đã từng nghĩ rằng sẽ đi cùng mình suốt cả cuộc đời, cho đến buổi chiều định mệnh của mùa hạ năm ấy.'
WHERE id = 64;

UPDATE chapters SET 
title = 'Mười năm một lời hứa',
content = 'Ký ức của mùa hạ năm ấy ùa về như một dòng thác lũ không thể ngăn cản.

Năm đó, hai người học chung lớp 12 chuyên Văn trường huyện. Vũ là một chàng trai trầm lặng, ít nói, luôn ngồi ở góc cuối lớp bên cạnh cửa sổ, nhưng lại có tài vẽ tranh tuyệt đẹp và một trái tim ấm áp đến lạ kỳ. Còn Hạ An là cô lớp trưởng năng nổ, luôn càu nhàu mỗi khi cậu ngủ gật trong giờ học.

"Này, cậu có biết sau này thi đại học xong, cậu muốn làm gì nhất không?" Hạ An nhớ rõ buổi chiều hôm đó, hai đứa ngồi vắt vẻo trên bờ đê lộng gió, ngắm nhìn hoàng hôn đỏ rực buông xuống dòng sông quê.

Vũ quay sang nhìn cô, ánh mắt trong veo như mặt nước mùa thu: "Tôi muốn mua một căn nhà nhỏ gần biển, mở một xưởng vẽ. Và ở đó... có cậu."

Câu nói nửa đùa nửa thật của chàng trai mới lớn khiến đôi má Hạ An đỏ bừng như ráng chiều. Cô cúi đầu che giấu nụ cười bẽn lẽn, khẽ ngoắc ngón tay út với cậu: "Hứa đấy nhé. Ai nuốt lời là làm cún con!"

Thế nhưng, cuộc đời chưa bao giờ êm đềm như những trang lưu bút tuổi học trò. Biến cố gia đình ập đến ngay trước ngày thi tốt nghiệp. Cha của Vũ phá sản, vướng vào vòng lao lý. Vũ buộc phải rời bỏ thị trấn trong đêm cùng mẹ, không để lại một lời nhắn, không một lá thư từ biệt.

Ngày Hạ An nhận giấy báo trúng tuyển đại học cũng là ngày cô đứng một mình dưới cơn mưa xối xả trước cổng nhà Vũ đã khóa trái cửa từ bao giờ. Mười năm ròng rã, cô mang theo lời hứa dang dở đó mà trưởng thành, học cách tự mình bước đi qua những tháng năm thanh xuân cô độc.'
WHERE id = 65;

UPDATE chapters SET 
title = 'Khoảnh khắc quay đầu',
content = 'Dưới ánh đèn vàng vọt của sân ga, hai ánh mắt bất ngờ chạm nhau giữa không gian ồn ào náo nhiệt.

Điều thuốc trên tay Vũ rơi xuống vũng nước mưa dưới chân, phát ra một tiếng xèo nhỏ rồi tắt ngấm. Anh đứng sững lại như một pho tượng đá, đôi mắt mở to nhìn người con gái đang đứng cách mình chỉ chừng mươi bước chân. Thời gian mười năm đã làm cho cô gái tóc đuôi ngựa ngây thơ năm nào trở thành một người phụ nữ đằm thắm, thanh lịch, nhưng ánh mắt trong veo phảng phất nỗi buồn hoài niệm ấy thì vẫn vẹn nguyên như thuở ban đầu.

"Hạ An... Là em thật sao?" Giọng Vũ khàn đặc, run rẩy trong tiếng gió rít.

Hạ An mỉm cười, một nụ cười nghẹn ngào khiến khóe mắt cô cay xè, giọt nước mắt ấm nóng lăn dài trên gò má buốt lạnh: "Chào cậu, Vũ. Đã mười năm rồi nhỉ."

Vũ bước từng bước chậm rãi lại gần cô, như sợ rằng nếu bước quá nhanh thì ảo ảnh tươi đẹp này sẽ tan biến vào màn mưa đêm. Khi khoảng cách giữa hai người chỉ còn lại một cánh tay, anh khẽ đưa bàn tay thô ráp lên, ngập ngừng rồi nhẹ nhàng chạm vào những giọt nước mưa còn đọng trên mái tóc cô.

"Tôi xin lỗi... Mười năm qua, tôi chưa từng quên lời hứa mùa hạ năm ấy."

Tiếng còi tàu lại vang lên giục giã, nhưng vào khoảnh khắc ấy, cả hai đều biết rằng chuyến tàu của cuộc đời họ giờ đây mới thực sự bắt đầu chuyến hành trình trở về bến đỗ bình yên.'
WHERE id = 66;

UPDATE chapters SET 
title = 'Ngoại truyện: Bức thư viết dưới cơn mưa (VIP)',
content = 'Trong chiếc hộp gỗ nhỏ phủ lớp bụi thời gian mà Vũ luôn mang theo bên mình suốt mười năm phiêu bạt, có một bức thư giấy học trò đã ố vàng, những dòng mực tím đã nhòe đi vì những giọt nước mắt của chàng trai mười tám tuổi đêm hôm ấy:

"Gửi Hạ An,

Nếu cậu đọc được những dòng này, có lẽ tôi đã ở một nơi rất xa rồi. Tôi không dám đến gặp cậu, vì tôi sợ nhìn thấy đôi mắt ngấn lệ của cậu sẽ khiến tôi không đủ can đảm để bước đi. Tôi không thể mang một tương lai mờ mịt và gánh nặng nợ nần để làm vướng bận ước mơ giảng đường của cậu.

Hạ An à, cảm ơn cậu vì đã làm cho mùa hạ tuổi mười bảy của tôi trở nên rực rỡ nhất trong đời. Xin lỗi vì đã nuốt lời hứa về ngôi nhà bên bờ biển. Nhưng xin cậu hãy tin tôi, dù tôi có đi đến cùng trời cuối đất, trong lòng tôi sẽ mãi mãi chỉ có một mình hình bóng của cậu.

Nếu duyên phận còn cho phép, mười năm sau, chúng ta hãy gặp lại nhau ở sân ga cũ nhé..."

Hạ An gấp lại bức thư, tựa đầu vào bờ vai vững chãi của Vũ khi chuyến tàu đêm từ từ lăn bánh. Ngoài trời, cơn mưa rào mùa hạ đã tạnh hẳn, để lộ ra bầu trời đầy sao lấp lánh như báo hiệu một ngày mới rực rỡ đang tới gần.',
is_vip = 1,
coin_price = 3
WHERE id = 87;

-- 6. THÊM ĐÁNH GIÁ (REVIEWS) VÀ BÌNH LUẬN SINH ĐỘNG CHO TRUYỆN TRANH & TRUYỆN MỚI
INSERT INTO reviews (story_id, user_id, title, content, has_spoiler, helpful_count) VALUES
(10, 2, 'Siêu phẩm truyện tranh Webtoon đỉnh nhất năm!', 'Nét vẽ quá đẹp và sắc nét, phong cách hành động không khác gì Solo Leveling Hàn Quốc! Chương 4 đánh nhau mãn nhãn, rất đáng 5 xu mở khóa VIP!', 0, 18),
(10, 3, 'Trần Minh ngầu đét, đọc cuốn không dứt ra được', 'Cảnh thức tỉnh hệ thống và hạ gục Sói Băng vẽ hoành tráng ghê. Tác giả ra chương nhanh lên nhé!', 0, 12),
(11, 4, 'Hoàng tử mèo béo cưng xỉu luôn á trời', 'Đọc giải trí cười xỉu với hai nhân vật chính. Ai mê mèo mà không đọc bộ này là phí nửa đời người haha.', 0, 15),
(12, 5, 'Cốt truyện ma pháp song hệ quá lôi cuốn', 'Văn phong của tác giả viết rất mượt mà, miêu tả chi tiết các hạt tinh tử ma pháp và trận chiến với Độc Giáp Trư nghẹt thở!', 0, 9)
ON DUPLICATE KEY UPDATE 
    title = VALUES(title),
    content = VALUES(content);

-- 7. NẠP DỮ LIỆU CÁC TRANG TRANH (chapter_pages)
-- Đã sinh sẵn trong database/insert_comic_pages.sql
