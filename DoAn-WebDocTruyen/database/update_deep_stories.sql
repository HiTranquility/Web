-- ============================================================================
-- update_deep_stories.sql: Cập nhật nội dung có chiều sâu cho Story 3, 4, 5, 13
-- ============================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Nạp 5 chương cho Story 13
DELETE FROM chapter_pages WHERE chapter_id IN (SELECT id FROM chapters WHERE story_id=13);
DELETE FROM chapter_unlocks WHERE chapter_id IN (SELECT id FROM chapters WHERE story_id=13);
DELETE FROM reading_progress WHERE story_id=13;
DELETE FROM chapters WHERE story_id=13;

INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price, created_at, updated_at)
VALUES (13, 1, 'Huyết Nhuộm Vực Sâu', 'Đêm nay, sấm chớp xé toạc bầu trời xám xịt trên đỉnh Hắc Phong Nhai.

Cơn đau buốt nhói như ngàn vạn mũi kim đâm vào thần kinh kéo ý thức của Diệp Lạc Tuyết trở về từ cõi chết. Nàng khẽ mở mắt, đập vào mi mắt không phải là ánh đèn phẫu thuật quen thuộc của bệnh viện dã chiến, cũng không phải làn khói thuốc súng khét lẹt nơi chiến trường Trung Đông nơi nàng từng xưng danh đệ nhất quân y kiêm sát thủ ám dạ.

Trước mắt là vách đá dựng đứng đen ngòm, rêu phong ẩm ướt và mùi máu tanh nồng nặc bốc lên từ chiếc áo lụa rách bươm đang dính chặt vào da thịt nàng.

"A... đầu ta..."

Một luồng ký ức xa lạ cuồn cuộn ùa về như thác lũ. 

Thân xác này mang tên Diệp Lạc Tuyết, đích nữ mười sáu tuổi của Trấn Quốc Tướng quân phủ thuộc Đại Khải vương triều. Mang danh đích nữ tôn quý, nàng lại sinh ra với kinh mạch bế tắc, trời sinh phế tài không thể tu luyện võ đạo, dung nhan lại bị hạ độc chu sa hủy hoại, tính tình nhút nhát yếu hèn nên trở thành trò cười cho toàn bộ thành Yên Kinh.

Chiều nay, chính thứ muội Diệp Nhược Vi cùng vị hôn phu Tứ Hoàng tử đã lừa nàng ra bờ vực này. Ánh mắt độc địa của ả muội muội cùng câu nói cay nghiệt vẫn còn văng vẳng bên tai:
"Tỷ tỷ, một phế vật xấu xí như tỷ sao xứng đáng với ngôi vị Tứ Hoàng tử phi? Chết đi, rồi nhường vị trí ấy cho ta!"

Một cú đẩy tàn nhẫn đã khiến nàng rơi xuống vực sâu muôn trượng, vỡ nát lục phủ ngũ tạng mà chết uất ức.

"Ha... ha ha..." Khóe môi Diệp Lạc Tuyết cong lên một nụ cười lạnh lẽo thấu xương. Đôi mắt phượng vốn dĩ u tối nay bừng lên ánh sáng sắc bén như lưỡi dao vừa tuốt khỏi vỏ.

"Diệp Lạc Tuyết, oán hận cùng mối thù kiếp trước của ngươi, ta nhận lấy! Từ giây phút này, bất cứ kẻ nào dám phụ ngươi, khinh ngươi, nợ ngươi một giọt máu, ta sẽ bắt bọn chúng phải hoàn trả bằng cả dòng tộc!"

Nàng cắn chặt răng, chịu đựng cơn đau thấu trời để nắn lại khớp xương vai bị trật. Với y thuật đỉnh phong từng cứu sống hàng trăm người khỏi bàn tay tử thần, nàng nhanh chóng nhận định tình trạng cơ thể: gãy hai xương sườn, trật khớp bả vai, độc chu sa ăn sâu vào kinh lạc khiến khí huyết ngưng trệ. Nhưng may mắn thay, dưới đáy vực có dòng suối ngầm chứa linh khí cùng bụi cỏ U Ngân Hoa phát sáng lập lòe trong bóng tối.

"U Ngân Hoa ba lá, rễ cực độc nhưng lá lại là thần dược đả thông kinh mạch!"

Nàng dùng chút sức tàn bò lại gần, bứt lá cỏ nhai nuốt. Dòng nhiệt lưu nóng rực lan tỏa khắp tứ chi bách hài, thiêu đốt lớp độc chu sa đóng cặn mười sáu năm qua. Kinh mạch vốn bị tắc nghẽn nay như dòng sông phá đê, linh khí đất trời điên cuồng ùa vào huyệt đạo.

Gió rít gào bên vách đá. Dưới đáy vực sâu tăm tối, một phượng hoàng niết bàn tái sinh đã chính thức mở mắt.', 0, 0, NOW(), NOW());
INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price, created_at, updated_at)
VALUES (13, 2, 'Trở Về Tướng Phủ', 'Ba ngày sau, tại Trấn Quốc Tướng quân phủ, không khí giăng đèn kết hoa rộn rã tưng bừng.

Hôm nay là đại lễ đính hôn giữa Tứ Hoàng tử Dạ Dực và Diệp Nhược Vi. Khách khứa quyền quý từ khắp kinh thành tấp nập kéo đến chúc mừng. Diệp Nhược Vi khoác lên mình bộ hỷ phục thêu phượng lộng lẫy, gương mặt trang điểm kiều diễm, nụ cười đắc thắng không giấu nổi nơi đáy mắt.

"Đích tỷ đoản mệnh rơi xuống vực sâu mất xác, ngôi vị Tứ Hoàng tử phi này chung quy cũng thuộc về ta!" Ả khẽ nâng chén rượu lưu ly, e ấp nhìn Dạ Dực bên cạnh.

Đúng lúc nghi lễ bái thiên địa sắp bắt đầu, một tiếng "RẦM" chấn động vang lên từ cổng chính!

Cánh cửa gỗ lim ngàn năm của Tướng quân phủ bị một luồng chưởng phong hất tung, vỡ vụn thành trăm mảnh vụn bay tứ tán. Bụi khói mịt mù tan đi, một bóng hình y phục trắng như tuyết chậm rãi bước vào.

Mái tóc đen dài bay trong gió, trên mặt đeo một chiếc mạng che mỏng manh, nhưng đôi mắt sắc lạnh như băng giá khiến toàn bộ đại sảnh tức khắc lặng ngắt như tờ.

"Ai to gan dám phá rối đại hôn của Tứ Điện hạ?!" Đại tổng quản quát lớn.

Nữ tử khẽ nhếch môi, thanh âm trong trẻo nhưng mang theo uy áp kinh người vọng khắp đại điện:
"Phá rối? Muội muội ruột thịt còn chưa bái lạy đích tỷ trưởng, sao đã vội kết tóc xe duyên?"

"Xoảng!" Chiếc chén lưu ly trên tay Diệp Nhược Vi rơi xuống đất vỡ tan tành. Ả lùi lại hai bước, mặt mày cắt không còn giọt máu, ngón tay run rẩy chỉ về phía trước:
"Ngươi... ma... ngươi là người hay ma?! Ngươi rõ ràng đã rơi xuống vực..."

"Rơi xuống vực?" Diệp Lạc Tuyết bước từng bước vững chãi, tiếng hài gõ xuống nền đá thanh thúy như tiếng đếm ngược của tử thần. "Muội muội nhớ rõ ràng như vậy, xem ra ngày ấy chính tay muội đẩy ta xuống, ký ức vẫn còn khắc sâu lắm nhỉ?"

Lời vừa thốt ra, cả sảnh đường xôn xao kinh hãi. Ánh mắt các bậc vương tôn quý tộc đổ dồn về phía Diệp Nhược Vi và Dạ Dực.

Dạ Dực biến sắc, sầm mặt đứng chắn trước mặt Diệp Nhược Vi: "Càn rỡ! Ngươi là yêu nghiệt phương nào dám mạo danh đích nữ Tướng phủ vu khống hoàng thất?"

"Hoàng thất?" Diệp Lạc Tuyết khẽ cười nhạt, tay áo vung lên. Một luồng linh lực vô hình quét qua, chiếc quạt ngọc trên tay Dạ Dực đứt phán làm đôi. Đồng thời, ngọc bội gia truyền khắc chữ ''Lạc'' màu tím ánh lên rạng ngời nơi thắt lưng nàng.

"Kẻ có mắt như mù nhận giặc làm phu, Tứ Hoàng tử, hôn ước rách nát giữa ta và ngươi, hôm nay Diệp Lạc Tuyết ta chính thức phế bỏ! Từ nay về sau, các người hãy chuẩn bị sẵn sàng nhận lấy từng món nợ mà các người đã gieo!"

Nàng ném tín vật đính ước xuống chân Dạ Dực, đạp nát thành bột mịn trước con mắt kinh hoàng của trăm người.', 0, 0, NOW(), NOW());
INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price, created_at, updated_at)
VALUES (13, 3, 'Diệt Độc Cứu Hoàng Đế', 'Sau màn đại náo Tướng quân phủ, danh tiếng của Diệp Lạc Tuyết lan truyền khắp kinh kỳ như một truyền kỳ. Thế nhưng, sóng gió hoàng cung chưa bao giờ ngơi nghỉ.

Nửa đêm hôm ấy, chiếu chỉ khẩn từ trong đại nội ban ra: Đương kim Hoàng đế đột ngột thổ huyết hôn mê sâu, thái y viện thúc thủ vô sách, ai có thể giải cứu giang sơn sẽ được phong thưởng vạn lượng hoàng kim cùng tước vị hiển hách.

Khi Diệp Lạc Tuyết bước chân vào Càn Khôn cung, mùi tanh hôi của tử khí cùng thuốc sắc đã nồng nặc cả tẩm điện. Hoàng hậu cùng các phi tần khóc lóc thảm thiết, các vị ngự y đầu ngành quỳ rạp dưới đất, trán ướt đẫm mồ hôi lạnh.

"Một tiểu cô nương miệng còn hôi sữa thì hiểu gì về kỳ kinh bát mạch? Mau đuổi nàng ra ngoài!" Thái y viện chưởng quản quát lớn khi thấy nàng tiến đến long sàng.

Diệp Lạc Tuyết chẳng buồn liếc mắt nhìn lão, ngón tay thon dài đặt lên cổ tay Hoàng đế bắt mạch. Chỉ mất ba nhịp thở, nàng khẽ cười khẩy:
"Thái y viện các người quả là phế vật. Bệ hạ không phải mắc bạo bệnh, mà là trúng ''Huyết Thiền Cổ Độc'' từ Tây Vực! Cổ trùng đã ăn vào tâm mạch, nếu cứ dùng sâm nhung bổ khí thì trong vòng một nén nhang nữa, tim bệ hạ sẽ vỡ nát vì ứ huyết!"

Lời nói như sét đánh giữa trời quang. Hoàng hậu run rẩy tiến lên: "Cô nương... có thật là cứu được hoàng thượng?"

"Muốn cứu người, lui ra ngoài hết cho ta!" Diệp Lạc Tuyết quát lạnh một tiếng, khí tràng bộc phát khiến không ai dám trái lệnh.

Nàng rút từ trong tay áo ra chín cây kim bạc cửu linh. Bằng thủ pháp ''Quỷ Thủ Thần Châm'' thất truyền ngàn năm, chín mũi kim hóa thành tàn ảnh, lần lượt cắm vào cửu đại tử huyệt trên ngực Hoàng đế. 

Linh lực mộc hệ thanh thuần truyền qua thân kim, phong tỏa đường đi của cổ trùng. Bàn tay nàng vung lên, một đường dao nhỏ rạch nhẹ trên đầu ngón tay trỏ của Hoàng đế.

"Xì..."

Một tia máu đen hôi thối bắn ra, kéo theo một con sâu nhỏ màu đỏ như hạt đậu đang giãy giụa rồi chết cháy thành tro bụi dưới ngọn lửa linh hỏa trên đầu ngón tay nàng.

Trên long sàng, Hoàng đế hít một hơi dài, sắc mặt từ tím tái dần chuyển sang hồng hào, đôi mắt rồng chậm rãi mở ra.

"Trẫm... đã qua khỏi rồi sao?"

Cả tẩm điện vỡ òa trong tiếng tung hô. Hoàng đế nhìn thiếu nữ áo trắng thản nhiên đứng bên giường, ánh mắt đầy vẻ chấn động và thán phục:
"Thần y cái thế! Từ hôm nay, trẫm ban cho khanh Kim Bài Miễn Tử, kiến đế bất quỳ, thấy khanh như thấy trẫm!', 0, 0, NOW(), NOW());
INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price, created_at, updated_at)
VALUES (13, 4, 'Gặp Gỡ Dạ Vương', 'Rời khỏi hoàng cung khi trời vừa hửng sáng, sương mù giăng kín các con phố dài của kinh thành Yên Kinh.

Trên đường trở về, chiếc xe ngựa của Diệp Lạc Tuyết đột nhiên dừng khựng lại giữa ngã tư vắng vẻ. Bốn con tuấn mã kéo xe đồng loạt hí vang sợ hãi, quỳ rạp xuống nền đá như gặp phải thiên địch thượng cổ.

Phía trước, một cỗ xe ngựa bằng gỗ Thiết Mộc đen tuyền, xung quanh tỏa ra hàn khí buốt giá đứng sừng sững chặn lối.

Trên xe có khắc huy hiệu Mãng Xà Chín Đầu — biểu tượng của Chiến Thần Dạ Cửu U, Nhiếp Chính Vương quyền khuynh triều dã, người từng một kiếm chém bay mười vạn quân địch nơi biên ải, nhưng ba năm trước bị hãm hại tàn phế đôi chân, tính tình hung tàn khát máu, người người nghe danh đều mất mật.

Bức rèm xe ngựa đen khẽ lay động. Một thanh âm trầm thấp, từ tính nhưng lạnh lẽo như từ chín tầng địa ngục truyền ra:
"Diệp đại tiểu thư, y thuật nghịch thiên, châm cứu giải độc Tây Vực chỉ trong một nén nhang... quả thật khiến bản vương mở rộng tầm mắt."

Diệp Lạc Tuyết vén rèm bước xuống, ánh mắt không hề có lấy nửa phần sợ hãi, bình thản nhìn vào bóng tối bên trong cỗ xe:
"Nhiếp Chính Vương nửa đêm chặn đường, không phải chỉ để khen ngợi vài câu sáo rỗng chứ?"

Dạ Cửu U khẽ cười, tiếng cười ma mị mang theo uy áp khủng khiếp làm rung chuyển lá cây ven đường:
"Bản vương trúng Cửu U Huyền Băng Độc ba năm, kinh mạch đôi chân đóng băng, sống không bằng chết. Thiên hạ đồn rằng thần tiên cũng không chữa nổi. Ngươi dám đánh cược với bản vương một ván không?"

"Đánh cược thế nào?"

"Ngươi chữa khỏi chân cho ta, ta cho ngươi thiên hạ này làm hậu thuẫn. Bất cứ kẻ nào ngươi muốn giết, bản vương sẽ san bằng cả cửu tộc kẻ đó!"

Diệp Lạc Tuyết ngẩng cao đầu, ánh mắt rực sáng:
"Ta không cần ngươi ban phát thiên hạ, thiên hạ này ta sẽ tự tay đoạt lấy. Nhưng... một đồng minh hùng mạnh như Dạ Vương gia, Diệp Lạc Tuyết ta đồng ý nhận!"

Hai ánh mắt sắc bén giao nhau trong màn sương sớm, một hiệp ước kinh thiên động địa đủ sức lật tung cả vương triều Đại Khải đã được xác lập.', 0, 0, NOW(), NOW());
INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price, created_at, updated_at)
VALUES (13, 5, 'Khởi Đầu Nghịch Thiên: Huyết Tẩy Đao Phong', 'Mùa thu năm ấy, bãi săn Hoàng gia Thượng Lâm Uyển rực rỡ cờ hoa.

Đây là cuộc săn bắn quy tụ toàn bộ vương tôn quý tộc và các cao thủ đỉnh tiêm của ngũ đại gia tộc. Nhưng dưới lớp vỏ bọc hào nhoáng của ngày hội săn bắn, một mưu đồ đen tối nhuốm máu đã được Diệp gia thứ nhánh và Tứ Hoàng tử Dạ Dực tỉ mỉ giăng sẵn.

Sâu trong rừng rậm Lạc Hồn Cốc, Diệp Lạc Tuyết một mình phi ngựa đuổi theo một con Ngân Giác Lộc. Đột nhiên, bốn phía xung quanh bốc lên làn khói độc màu tím đen, phong tỏa toàn bộ linh khí trong vòng mười dặm!

"Vút! Vút! Vút!"

Hàng trăm mũi tên tẩm kịch độc xé gió bắn tới từ bốn phương tám hướng. 

"Diệp Lạc Tuyết, hôm nay chính là ngày tàn của ngươi! Ba mươi tên tử sĩ Địa giai cùng ''Tuyệt Khí Diệt Hồn Trận'' này, thần tiên cũng đừng mong thoát!" Giọng nói cay độc của Diệp Nhược Vi vang lên từ trên vách núi cao.

Diệp Lạc Tuyết nhảy vút lên không trung, tay áo vung ra chín dải lụa bạc mang theo cương khí xé toạc làn mưa tên. Đôi mắt nàng lóe lên tia sát khí lạnh buốt:
"Chỉ bằng đống rác rưởi này mà cũng muốn lấy mạng ta?"

Nàng rút từ thắt lưng ra một thanh chủy thủ màu đen tuyền — Ám Dạ Linh Đao. Thân hình nàng hóa thành một đạo u ảnh quỷ mị, xuất hiện chớp nhoáng giữa vòng vây quân thù. Mỗi một đao vung ra là một đường máu tươi phun trào, gọn gàng, chuẩn xác, không một động tác thừa thãi của đệ nhất sát thủ kiếp trước!

Chỉ trong mười nhịp thở, mười lăm tên tử sĩ Địa giai ngã gục trong vũng máu.

Nhưng đúng lúc đó, tên đầu lĩnh sát thủ bẻ gãy một viên ngọc đen, triệu hoán ra một con Hắc Lân Ma Xà cấp sáu khổng lồ từ lòng đất chui lên, cái đuôi thép quất mạnh về phía nàng!

Trong khoảnh khắc ngàn cân treo sợi tóc, một tiếng long ngâm trầm hùng chấn động cả thiên địa vang lên:
"Dám đụng đến người của bản vương, muốn chết!"

Một luồng kiếm khí màu tím kim từ trên trời giáng xuống, chém đứt đôi con cự xà cấp sáu trong chớp mắt! 

Giữa làn máu ma thú tung tóe, một thân ảnh vĩ đại khoác trường bào màu đen viền vàng đạp gió mà tới. Đôi chân từng bị phế của Dạ Cửu U nay vững chãi như bàn thạch, đồng tử màu tím rực lửa ma thần khiến vạn vật phải quỳ rạp thần phục.

Hắn vươn tay ôm lấy vòng eo mảnh mai của Diệp Lạc Tuyết, thanh kiếm dính máu chỉ thẳng lên đỉnh vách núi nơi Dạ Dực và Diệp Nhược Vi đang run rẩy tột cùng:
"Vương phi của ta, nàng muốn bọn chúng chết kiểu gì?"

Khắp Thượng Lâm Uyển, cuồng phong nổi lên. Trò chơi vương quyền đẫm máu chính thức bắt đầu!', 1, 5, NOW(), NOW());

-- Cập nhật nội dung có chiều sâu cho Story 3
UPDATE chapters SET content='Hồi 1: Án mạng trong căn hộ 1207

Cơn mưa rào đầu mùa hạ trút nước xối xả xuống con đường ven sông Sài Gòn, cuốn theo lớp bụi bặm ngột ngạt của thành phố nhưng không thể gột rửa đi mùi tử khí đang bao trùm lấy tầng 12 của toà chung cư cao cấp The Landmark Riverside.

Đồng hồ điểm 2 giờ 15 phút sáng.

Đại uý Phan Hoàng Vũ kéo khoá chiếc áo khoác gió sũng nước, bước vội qua dải băng vàng rào chắn có dòng chữ "HIỆN TRƯỜNG VỤ ÁN - CẤM VÀO". Mùi hạnh nhân đắng thoang thoảng lẫn trong không khí điều hoà lạnh buốt khiến sống lưng anh khẽ ớn lạnh. Đây là mùi đặc trưng của chất độc xyanua.

"Nạn nhân là ai?" Vũ hỏi người đồng đội trẻ đang ghi chép biên bản.

"Báo cáo sếp, là Hoàng Khải, 42 tuổi, giám đốc điều hành của Quỹ Đầu tư Mạo hiểm VinaVenture. Người giúp việc phát hiện lúc 1 giờ 30 khi đến chuẩn bị bữa khuya theo yêu cầu."

Nạn nhân ngồi gục trên chiếc ghế bành bọc da bò Ý cao cấp, đầu ngoẹo sang một bên, mắt trợn trừng nhìn lên trần nhà như thể vừa chứng kiến một điều kinh hoàng trước khi trút hơi thở cuối cùng. Trên bàn làm việc bằng gỗ mun là một ly rượu whisky uống dở và chiếc laptop màn hình vẫn đang sáng, nhưng ổ cứng đã bị giật mạnh ra ngoài, các vi mạch bên trong nát vụn.

"Cửa ra vào thế nào?" Vũ ngồi xổm xuống kiểm tra ổ khoá từ thông minh.

"Cửa khoá trong sếp ạ. Chốt vật lý bên trong gạt ngang, then an toàn vẫn cài. Cửa sổ ban công nhìn ra sông cũng khoá chặt từ bên trong. Không có dấu vết cạy phá hay đột nhập từ bên ngoài."

Một vụ án mạng trong phòng kín hoàn hảo. Nhưng trên tấm kính ban công hướng ra màn mưa mờ mịt, Vũ nhận thấy một chi tiết bất thường: một chấm tròn nhỏ bằng hạt ngô đã bị khoét bằng dao cắt kính kim cương, vừa đủ để luồn một sợi dây cước siêu mảnh qua khe thông gió.

Vũ nheo mắt nhìn dòng nước mưa chảy siết qua ô kính, trong đầu anh vang lên một linh cảm chẳng lành. Vụ án số 7 này không đơn thuần là một vụ thanh toán tranh chấp tài chính, mà đằng sau nó là một cái bẫy tinh vi của một kẻ săn mồi trí tuệ.', updated_at=NOW() WHERE id=67;
UPDATE chapters SET content='Hồi 2: Người gác cổng và chiếc đồng hồ chết đứng

Sảnh lễ tân toà nhà lúc 3 giờ 30 sáng tĩnh lặng như tờ, chỉ có tiếng máy in hoá đơn của ban quản lý thỉnh thoảng rít lên từng hồi đơn điệu.

Ngồi đối diện với Đại uý Vũ là ông Ba Đực, 58 tuổi, nhân viên bảo vệ trực ca đêm phụ trách phòng điều khiển camera an ninh và kiểm soát thẻ từ tầng hầm. Đôi bàn tay thô ráp của ông lão khẽ run lên khi bưng tách trà nóng do điều tra viên rót.

"Bác Đực, khoảng từ 1 giờ đến 1 giờ 45 sáng nay, bác có thấy ai ra vào thang máy khu căn hộ VIP không?" Vũ hỏi, ánh mắt bình tĩnh nhưng quan sát tỉ mỉ từng biểu cảm nhỏ trên gương mặt người đối diện.

"Dạ... thưa cán bộ, ca trực đêm nay tôi ngồi trực một mình. Hệ thống camera ghi nhận từ 12 giờ đêm không có khách lạ nào lên tầng 12 cả. Cư dân tầng đó chỉ có 4 hộ, họ đều đi ngủ sớm. Tôi nhớ rõ lúc 1 giờ 10, tôi còn đi tuần tra qua hành lang tầng trệt..."

"Bác chắc chắn về mốc thời gian đó chứ?" Vũ ngắt lời, chỉ tay vào chiếc đồng hồ cơ cổ đeo trên cổ tay trái của ông lão.

Chiếc đồng hồ Seiko 5 kim loại cũ kỹ, mặt kính đã rạn một đường nhỏ. Điều đáng nói là hai kim giờ và phút đang đứng bất động ở vị trí 1 giờ 14 phút.

Ông lão giật mình, vội vàng lấy tay áo che lại, vẻ mặt bối rối:
"À... cái này... nó hết cót từ trưa hôm qua, tôi chưa kịp lên dây cót lại..."

"Đồng hồ cơ tự động chỉ cần cử động tay là chạy tiếp, thưa bác. Trừ phi bánh răng hồi của nó đã bị một lực chấn động cực mạnh làm gãy gập." Vũ đứng dậy, đặt chiếc máy tính bảng lên bàn, trên màn hình là trích xuất dữ liệu camera an ninh cầu thang bộ thoát hiểm số 2.

Vào đúng 1 giờ 14 phút sáng, ống kính camera ở khúc cua tầng 3 bất ngờ bị nhiễu sóng trong 40 giây. Trong giây thứ 38, dưới đáy khung hình le lói bóng một người mặc áo mưa trùm kín đầu, dưới chân mang đôi ủng cao su màu xám đậm chuyên dụng của thợ lặn, và trên tay người đó xách một chiếc vali nhôm chống nước.

"Bác Ba Đực, người gác cổng chân chính sẽ không bao giờ tắt hệ thống cảnh báo cháy giả lập ở tầng hầm lúc 1 giờ 12 phút để đánh lừa cảm biến an ninh. Bác muốn tự mình nói ra kẻ đã đưa chiếc chìa khoá vạn năng cho bác, hay để chúng tôi mời con trai bác ở cảng Cát Lái lên làm việc?"

Sắc mặt người bảo vệ già trong tích tắc xám ngoét như tro tàn.', updated_at=NOW() WHERE id=68;
UPDATE chapters SET content='Hồi 3: Giải phẫu tử thi và mật mã dưới móng tay

Căn phòng giải phẫu của Viện Pháp y thành phố nồng nặc mùi hoá chất sát trùng formalin. Dưới ánh đèn huỳnh quang trắng lạnh, Bác sĩ pháp y Trâm Anh tháo găng tay cao su, kéo tấm khăn phủ trắng lên ngực thi thể Hoàng Khải.

"Kết quả độc chất học thế nào rồi chị?" Vũ bước vào, tay cầm hai cốc cà phê đặc không đường.

"Không phải xyanua thông thường như phỏng đoán ban đầu đâu Vũ." Trâm Anh lắc đầu, đưa cho anh tập hồ sơ bệnh lý vừa in còn nóng hổi. "Mùi hạnh nhân trong phòng chỉ là đòn nghi binh do hung thủ đốt tinh dầu giả mạo. Chất độc thực sự giết chết Hoàng Khải là Tetrodotoxin dạng tinh khiết chiết xuất từ gan cá nóc kết hợp với độc tố ốc cối Địa Trung Hải."

"Độc tố ốc cối?" Vũ nhíu mày.

"Đúng vậy. Loại độc này làm tê liệt hoàn toàn cơ hô hấp và dẫn truyền thần kinh vận động trong vòng 90 giây, nhưng điều tàn nhẫn nhất là nạn nhân hoàn toàn tỉnh táo và cảm nhận được sự ngạt thở từng giây từng phút cho đến khi tim ngừng đập. Không hề có phản xạ co giật, trông bên ngoài giống hệt một cơn đột quỵ do nhồi máu cơ tim."

Trâm Anh bật kính hiển vi quang học phóng đại 400 lần, chỉ vào màn hình phụ:
"Nhìn vào đây đi. Dưới móng ngón tay trỏ bàn tay phải của nạn nhân, tôi tìm thấy những hạt vi nhựa dẻo và một vết xước hình bán nguyệt. Khi dùng thuốc thử huỳnh quang, nạn nhân đã dùng chút sức tàn cuối cùng để cào một dãy ký tự lên mặt dưới của chiếc nẹp bàn làm việc."

Trên tấm ảnh chụp hồng ngoại hiện lên rõ mồn một dãy ký tự đẫm máu:
`7-DELTA-9402#`

"Đây là mã định danh tài khoản ẩn danh của ngân hàng Thuỵ Sĩ hoặc toạ độ của một container hàng lậu." Vũ nghiền ngẫm từng ký tự. "Hoàng Khải biết mình sắp bị diệt khẩu nên đã để lại chìa khoá dẫn đến kho tài liệu bí mật của tập đoàn."

Đúng lúc đó, chuông điện thoại của Vũ reo vang gấp gáp. Đầu dây bên kia là giọng nói hổn hển của trinh sát Định:
"Anh Vũ! Tìm thấy chiếc xe sedan màu tro khả nghi rồi! Nó đang đỗ tại một nhà kho bỏ hoang gần bờ kè cảng Tân Thuận, có người đang đốt tài liệu phi tang!"

"Bao vây ngay lập tức, giữ nguyên hiện trường, tôi tới ngay!" Vũ khoác vội áo súng, lao thẳng ra màn đêm.', updated_at=NOW() WHERE id=77;
UPDATE chapters SET content='Hồi 4: Người thứ ba lộ diện trong màn sương cảng

Mưa càng lúc càng nặng hạt. Gió từ sông Sài Gòn thổi tạt vào bến cảng Tân Thuận rít lên từng hồi ghê rợn qua những vách tôn rỉ sét của dãy nhà kho số 9.

Bốn trinh sát hình sự trang bị áo giáp chống đạn và súng ngắn bám sát theo hiệu lệnh tay của Đại uý Vũ, áp sát mép cửa sắt kho hàng. Mùi khói khen khét của giấy tờ và nhựa đường bốc lên từ một thùng phuy sắt ở góc cuối nhà kho.

Bên cạnh đống lửa bập bùng, một bóng người dong dỏng cao trong bộ vest đen phẳng phiu đang thản nhiên ném từng tập hồ sơ vào ngọn lửa. Trên bàn gỗ bên cạnh là chiếc vali nhôm chống nước đã mở toang, bên trong chứa hàng chục cọc tiền đô la Mỹ và một hộ chiếu ngoại giao mang tên giả.

"Công an đây! Giơ tay lên, đứng yên tại chỗ!" Vũ đá tung cánh cửa phụ, họng súng K54 chĩa thẳng vào mục tiêu.

Người đàn ông không hề giật mình. Hắn chậm rãi quay đầu lại, đẩy gọng kính vàng lên sống mũi, nở một nụ cười ngạo nghễ quen thuộc.

"Luật sư Lâm... người đại diện pháp lý kiêm bạn thân nhất của Hoàng Khải?" Vũ siết chặt cò súng, ánh mắt sắc như dao cạo.

"Đại uý Vũ, anh đến nhanh hơn tôi tưởng 15 phút đấy." Lâm thong thả phủi tàn tro bám trên cổ tay áo. "Khải là một kẻ tài năng nhưng quá ngây thơ. Hắn nghĩ rằng việc tống tiền tổ chức ''Bóng Ma Số 7'' bằng tập dữ liệu trốn thuế 500 triệu đô la sẽ giúp hắn rút lui an toàn sang Canada. Hắn không hiểu rằng, một khi đã bước chân vào bàn cờ này, chỉ có hai kết cục: làm quân cờ ngoan ngoãn hoặc trở thành một cái xác chết phòng kín."

"Ông đã thuê tay thợ lặn leo theo đường ống nước thải ban công, khoét kính và dùng phi tiêu tẩm độc bắn vào gáy Hoàng Khải qua khe gió, đúng không?"

"Biết hết rồi thì sao nào?" Lâm khẽ cười khẩy, tay phải thò vào túi áo trong.

"ĐOÀNG!"

Một tiếng súng chát chúa xé toạc màn mưa! Viên đạn từ súng của Vũ bắn trúng cổ tay Lâm, hất văng khẩu súng ngắn giảm thanh giấu trong túi áo văng ra xa ba mét. Hai trinh sát hình sự lập tức ập tới khống chế, bẻ quặt tay gã luật sư phản trắc xuống nền xi măng lạnh ngắt.

Dưới đáy chiếc vali nhôm, tập tài liệu chống cháy mang mã số `7-DELTA-9402#` vẫn còn nguyên vẹn. Danh sách 12 nhân vật cộm cán đứng sau đường dây rửa tiền xuyên quốc gia chính thức lộ diện dưới ánh đèn pin tuần tra.

Bình minh dần ló rạng trên sông Sài Gòn, xua đi màn sương mù u ám. Vụ án số 7 đã khép lại, nhưng cuộc chiến đưa công lý ra ánh sáng chỉ vừa mới bắt đầu.', updated_at=NOW() WHERE id=78;

-- Cập nhật nội dung có chiều sâu cho Story 4
UPDATE chapters SET content='Hồi 1: Ngọn đèn lồng đỏ trong sương mù

Mưa dầm xứ hoa Đà Lạt vào những ngày cuối đông buốt giá đến tê dại ngón tay.

Hoàng Minh — một nhà văn tự do chuyên viết truyện trinh thám kỳ bí — lạc tay lái chiếc xe máy cà tàng giữa con dốc quanh co ngập tràn sương mù mịt mùng của đồi thông Trại Mát. Điện thoại của anh sập nguồn từ hai tiếng trước, sóng 4G hoàn toàn biến mất, xung quanh chỉ có tiếng thông reo rên rỉ như tiếng khóc của ai oán trong gió lạnh.

Khi tưởng chừng như phải chịu chết cóng giữa rừng hoang, Minh chợt nhìn thấy một ánh sáng leo lét le lói ở cuối con hẻm dốc lát đá chẻ.

Đó là một ngọn đèn lồng giấy dầu màu đỏ thẫm đung đưa dưới mái hiên gỗ mục nát. Biển hiệu bằng gỗ lim đen bóng khắc ba chữ Nôm thếp vàng đã phai màu: "HỒI HƯƠNG QUÁN".

Tiếng chuông gió bằng đồng cũ kỹ reo lên từng tiếng "keng... keng..." khô khốc khi Minh đẩy cánh cửa gỗ khép hờ bước vào.

Bên trong quán trọ ấm áp một cách kỳ lạ, thoang thoảng mùi trầm hương cổ kính trộn lẫn với mùi trà hoa cúc khô. Một chiếc lò sưởi bằng gang cháy liu riu những khúc củi thông nổ lách tách. Phía sau quầy thu ngân bằng gỗ gụ là một người phụ nữ trạc ba mươi tuổi, khoác chiếc áo bà ba gấm màu tím huế, dung nhan thanh tú nhưng nước da trắng bệch không có lấy một giọt huyết sắc.

"Khách quan từ phương xa tới lỡ đường sao?" Người phụ nữ khẽ ngẩng đầu, đôi mắt đen láy thăm thẳm như giếng nước không đáy.

"Dạ vâng... xe tôi hỏng giữa đèo, trời mưa quá. Chị cho tôi thuê một phòng trọ qua đêm nay với." Minh mừng rỡ đáp, móc ví tiền ra.

Người phụ nữ không nhìn vào tiền, chỉ nhẹ nhàng đẩy ra một cuốn sổ da bìa nâu đóng gáy bằng chỉ đỏ cùng một cây bút lông cán trúc:
"Ở quán trọ của tôi không thu tiền mặt. Khách muốn trọ lại, chỉ cần ký tên thật, năm sinh và viết vào đây một điều mà người ân hận nhất trong cuộc đời này."

Minh ngẩn người, đưa mắt nhìn cuốn sổ. Dưới ánh nến bập bùng, những dòng chữ của những người trọ trước uốn lượn như giun bò, nhưng tuyệt nhiên không một ai có ghi ngày tháng trả phòng.', updated_at=NOW() WHERE id=69;
UPDATE chapters SET content='Hồi 2: Sổ đăng ký không có ngày mai và ba điều cấm kỵ

Cầm chìa khoá bằng đồng đúc hình đầu chim nhạn trên tay, Minh toan bước lên cầu thang gỗ thì người phụ nữ áo tím khẽ cất giọng dặn dò:

"Khách quan trọ tại phòng số 4 trên căn gác mái. Nhớ kỹ ba điều này, nếu muốn ngày mai nhìn thấy mặt trời mọc:
Thứ nhất: Sau 12 giờ đêm, nghe tiếng gõ cửa tuyệt đối không được mở, dù người bên ngoài có xưng danh là cha mẹ hay người thân nhất của cậu.
Thứ hai: Nếu nghe thấy tiếng đàn đáy vọng qua vách gỗ, hãy nhắm mắt niệm tên chính mình ba lần rồi ngủ tiếp, không được tò mò hé mắt nhìn qua khe cửa sổ.
Thứ ba: Tấm gương đồng treo đầu giường đã phủ vải đỏ, tuyệt đối không được gỡ khăn phủ ra."

Minh nuốt nước bọt đánh ực một cái, gật đầu lia lịa rồi vội vã ôm ba lô bước lên căn gác mái.

Căn phòng số 4 nhỏ hẹp nhưng sạch sẽ tinh tươm. Giữa phòng kê một chiếc giường gỗ gụ trải nệm bông trắng muốt, một chiếc bàn con đặt bình trà nguội và ngọn đèn dầu hỏa đỏ leo lét. Đầu giường quả nhiên có một tấm gương tròn bọc khung đồng cũ kỹ, bên ngoài phủ kín bằng một mảnh lụa điều đỏ như máu.

Đêm càng về khuya, không khí càng lạnh buốt. Tiếng gió rít qua khe cửa sổ rít từng cơn ghê rợn.

Minh trằn trọc không sao chợp mắt được. Đúng 12 giờ đêm, chiếc đồng hồ quả lắc dưới sảnh quán trọ điểm từng hồi chuông nặng nề:
"BOONG... BOONG... BOONG..."

Vừa dứt tiếng chuông thứ mười hai, bên ngoài hành lang bỗng vang lên tiếng bước chân lẹp xẹp. Từng bước... từng bước một dừng lại ngay trước cửa phòng số 4.

"Cốc... cốc... cốc..."

Tiếng gõ cửa vang lên đều đặn ba nhịp. Rồi một giọng nói dịu dàng, ngọt ngào quen thuộc đến thắt lòng vang lên qua khe cửa gỗ:
"Minh ơi... mở cửa cho em đi anh... Ngoài này lạnh quá... Em là Lan đây..."

Minh bàng hoàng bật dậy, toàn thân run rẩy như cầy sấy. Lan là người yêu cũ của anh, nhưng cô ấy đã qua đời trong một vụ tai nạn xe hơi cách đây đúng ba năm trước!', updated_at=NOW() WHERE id=79;
UPDATE chapters SET content='Hồi 3: Căn phòng số 4 và bến đỗ Vong Xuyên

Mồ hôi lạnh túa ra ướt đẫm trán Minh. Giọng nói bên ngoài cửa càng lúc càng ai oán, tiếng cào móng tay kèn kẹt vào gỗ lim nghe rợn tóc gáy:
"Minh... sao anh nỡ bỏ rơi em... Anh đã hứa sẽ ở bên em mãi mãi mà..."

Nhớ lại lời dặn của bà chủ quán trọ, Minh cắn chặt môi đến bật máu, hai tay bịt chặt tai, thầm niệm tên mình trong cổ họng:
"Ta là Hoàng Minh... Ta là Hoàng Minh... Ta còn sống..."

Đúng lúc đó, từ vách gỗ phòng bên cạnh, một tiếng đàn đáy trầm đục bỗng ngân vang. Tiếng đàn ai oán, u sầu như tiếng thở dài của trăm ngàn vong linh phiêu dạt giữa chốn hồng trần. Theo từng nhịp đàn, tiếng gõ cửa cùng tiếng gọi tên anh dần dần lùi xa, rồi tan biến vào hư không như bọt nước.

Sự tò mò của một kẻ cầm bút trỗi dậy lấn át cả nỗi sợ hãi. Minh rón rén bước lại gần khe nẹp gỗ nhìn sang căn phòng bên cạnh.

Qua khe hở mỏng manh, dưới ánh sáng lân tinh màu xanh lục mờ ảo, Minh kinh hoàng chứng kiến một cảnh tượng không thuộc về nhân gian:
Người phụ nữ áo tím đang ngồi gảy đàn bên một dòng sông đen ngòm cuồn cuộn chảy xiết ngay giữa lòng căn nhà gỗ. Hai bên bờ sông mọc bạt ngàn những bông hoa Bỉ Ngạn đỏ rực rỡ đến ma mị. Hàng chục bóng người mờ ảo, kẻ mặc âu phục, người mặc cổ trang, đang xếp hàng cúi đầu bước lên một chiếc thuyền nan không người chèo.

"Khách quan... xem đủ chưa?"

Một giọng nói lạnh ngắt vang lên ngay sát vành tai Minh!

Minh quay phắt lại, bà chủ quán trọ đã đứng sừng sững sau lưng anh từ bao giờ, gương mặt không chút biểu cảm:
"Hồi Hương Quán này vốn là trạm dừng chân cuối cùng của những linh hồn còn vướng bận chấp niệm trước khi qua cầu Nại Hà uống canh lãng quên. Cậu mạng lớn, dương thọ chưa tận nhưng trong lòng ôm quá nhiều day dứt với người cõi âm nên mới lạc bước tới đây."

Bà khẽ phẩy tay áo gấm, một luồng gió thơm mùi hoa cúc ùa tới quấn lấy thân thể anh.

Khi Minh bừng tỉnh mở mắt ra, ánh nắng ban mai rực rỡ đã chiếu rọi khắp đồi thông Trại Mát. Anh đang nằm gục trên tay lái chiếc xe máy bên vệ đường Quốc lộ 20. Xung quanh không hề có ngôi nhà gỗ hay ngọn đèn lồng đỏ nào cả, chỉ có trong túi áo khoác của anh vương lại một cánh hoa Bỉ Ngạn đỏ tươi như máu và chiếc bật lửa khắc tên Lan.', updated_at=NOW() WHERE id=80;

-- Cập nhật nội dung có chiều sâu cho Story 5
UPDATE chapters SET content='Hồi 1: Ba trăm dân đói và viên đá lãnh chúa

Gió bão tuyết phương bắc gào thét như bầy dã thú lồng lộn qua những khe nứt của bức tường thành đá xám tro đổ nát.

Lâm Thần đứng trên chòi canh đổ nát của Tuyết Phong Quan, nhìn xuống khoảng sân đất bốc khói lạnh bên dưới. Ba trăm con người gầy guộc trơ xương, khoác trên mình những manh áo rách tả tơi chắp vá bằng da thú thô ráp, đang co ro ôm lấy nhau dưới mái lều rơm rách nát để chống chọi lại cái lạnh âm mười lăm độ.

Trong mắt họ không còn sợ hãi, mà chỉ là sự tuyệt vọng cùng cực của những kẻ đang chờ đợi cái chết gõ cửa.

"Thiếu chủ... trong kho lương chỉ còn lại nửa bao cám mốc và hai cân muối hột. Nếu ngày mai tuyết lớn phong toả đường đèo, chúng ta không trụ nổi ba ngày đâu..." Lão quản gia Mông Khang run rẩy bẩm báo, hàm răng va vào nhau lập cập.

Lâm Thần khẽ thở dài, một luồng hơi trắng phả ra tan nhanh vào không khí. 

Ai mà ngờ được, chỉ một cái chớp mắt sau tai nạn giao thông ở thế giới hiện đại, một kỹ sư công trình kiêm chuyên gia quy hoạch đô thị như anh lại xuyên không vào thân xác một vị thiếu gia quý tộc bị gia tộc ruồng bỏ, bị đày ải đến vùng đất cằn cỗi Tuyết Phong Quan này làm Lãnh chúa bù nhìn!

Nhưng Lâm Thần không tuyệt vọng. 

Bàn tay anh nắm chặt lấy một khối ngọc hình lập phương phát sáng ấm áp trong lòng bàn tay — Viên Đá Lãnh Chúa, thánh vật kích hoạt "Hệ Thống Bản Đồ Kiến Thiết Toàn Cảnh" chỉ có trong ý thức của anh.

"Ting! Phát hiện lãnh địa Tuyết Phong Quan. Diện tích: 12 km vuông. Dân số: 312 người. Độ trung thành: 42%. Điểm tài nguyên dự trữ: 500 điểm."
"Mở khoá cây công nghệ sơ cấp: Lò sưởi hơi nước ngầm, Kỹ thuật bê tông tro núi lửa, Nông nghiệp nhà kính giữ nhiệt!"

Ánh mắt Lâm Thần bừng lên ngọn lửa kiên định. Anh quay người bước xuống chòi canh, giọng nói dõng dạc vang vọng khắp quảng trường:
"Toàn bộ bá tánh Tuyết Phong Quan nghe đây! Từ hôm nay, Lâm Thần ta là Lãnh chúa của các ngươi! Chỉ cần ta còn sống một ngày, ở toà thành này tuyệt đối không một ai phải chết vì đói rét! Tất cả đàn ông trai tráng tập hợp lại, chúng ta bắt đầu xây dựng lại nhà cửa ngay bây giờ!', updated_at=NOW() WHERE id=70;
UPDATE chapters SET content='Hồi 2: Đếm lại từ đầu: Lò rèn và tường đá

Lời hứa của vị Lãnh chúa trẻ tuổi ban đầu chỉ nhận lại những ánh nhìn hoài nghi. Nhưng khi Lâm Thần dùng 100 điểm tài nguyên đổi lấy mười bao bột mì trắng tinh cùng ba tảng thịt lợn muối từ hệ thống kho dự trữ, ngọn lửa hy vọng đã bùng cháy mãnh liệt trong lòng ba trăm con người khốn khổ.

Những nồi cháo đặc nóng hổi thơm phức bốc khói nghi ngút được phát tận tay từng người già và trẻ nhỏ. Ăn xong bữa no đầu tiên sau ba tháng ròng rã, khí thế của toàn bộ dân thành sôi sục chưa từng có.

Lâm Thần lập tức phân công lao động theo mô hình công nghiệp hoá sơ khai:
Nhóm thứ nhất gồm 50 thanh niên khoẻ mạnh theo sự hướng dẫn của anh tiến vào mỏ đá vôi phía sau núi, khai thác đá và trộn với tro núi lửa có sẵn dưới lòng suối cạn để tạo ra mẻ xi măng La Mã cổ đại đầu tiên của thế giới này.

Nhóm thứ hai do bác thợ rèn câm phụ trách, bắt tay vào cải tạo lại lò rèn cũ. Dưới sự chỉ đạo kỹ thuật của Lâm Thần, hệ thống ống thổi gió kép bằng đòn bẩy guồng nước được lắp đặt, đẩy nhiệt độ lò nung lên tới 1300 độ C. Quặng sắt thô được tôi luyện thành những lưỡi cuốc, xẻng và mũi giáo thép carbon cứng cáp, sắc bén gấp ba lần vũ khí bằng đồng thau cũ kỹ.

"Keng! Keng! Keng!"

Tiếng búa đập đe sắt vang vọng suốt ngày đêm như nhịp đập trái tim của một sinh mệnh khổng lồ đang hồi sinh.

Chỉ trong vòng năm ngày ngắn ngủi, bức tường thành phía nam cao bốn mét bằng đá hộc liên kết bằng vữa xi măng đã mọc lên sừng sững, khép kín cửa ải hiểm trở của Tuyết Phong Quan. Bên trong nội thành, dãy nhà ở tập thể bằng gạch nung có hệ thống đường ống dẫn khói sưởi chạy ngầm dưới nền đất giúp nhiệt độ trong phòng luôn duy trì ở mức ấm áp 22 độ C.

Độ trung thành của dân cư tăng vọt từ 42% lên 89%. Họ nhìn Lâm Thần không còn như một vị thiếu gia lưu đày, mà như một vị thần hộ mệnh được thượng đế phái xuống cứu rỗi vùng đất chết.', updated_at=NOW() WHERE id=71;
UPDATE chapters SET content='Hồi 3: Ba ngày trước bão tuyết và đàn sói ma

Hệ thống cảnh báo thời tiết của Viên Đá Lãnh Chúa phát ra tín hiệu đỏ chớp nháy liên hồi:
"Cảnh báo: Cơn bão tuyết cấp đại hồng thuỷ cùng đợt di cư của đàn Hắc Ma Lang phương bắc sẽ đổ bộ vào Tuyết Phong Quan sau 72 giờ nữa. Số lượng dự kiến: hơn 500 con sói ma biến dị cấp hai!"

Tin tức lan ra, sự hoang mang bắt đầu nhen nhóm trong lòng cư dân. Hắc Ma Lang là ác mộng của toàn bộ vùng biên ải — loài sói săn mồi theo bầy đàn, răng nanh sắc như dao cạo, móng vuốt có thể xé toạc giáp sắt và cực kỳ hung hãn trong mùa đông giá rét.

Lâm Thần triệu tập toàn bộ ban chỉ huy vào phòng họp tác chiến. Trên bàn sa bàn cát do chính anh nặn ra, từng cao điểm và hào lũy phòng ngự hiện lên chi tiết.

"Chúng ta không có đủ kỵ binh để đối đầu trực diện ngoài đồng trống. Chúng ta sẽ đánh chiến tranh trận địa!" Lâm Thần cắm lá cờ đỏ vào con dốc độc đạo dẫn vào ải.

Kế hoạch phòng ngự ba tầng được triển khai thần tốc:
Tầng thứ nhất: Đào một con hào sâu ba mét, rộng bốn mét dưới chân dốc, bên dưới cắm chông gỗ vót nhọn bọc thép và rải đầy nhựa thông khô.
Tầng thứ hai: Ba cỗ máy bắn đá trục quay kiểu Trebuchet được chế tạo từ gỗ thông ngàn năm đặt trên đỉnh tháp canh, nạp sẵn những khối cầu đá bọc rơm tẩm dầu lửa.
Tầng thứ ba: Đội cung thủ 40 người được trang bị nỏ liên châu bắn liên thanh 5 phát do Lâm Thần cải tiến từ mẫu nỏ Gia Cát.

Đêm thứ ba, bão tuyết gầm rú dữ dội. Từ trên sườn đồi phủ tuyết trắng xóa, hàng ngàn đốm mắt xanh lục rực lửa ma quái xuất hiện dày đặc như sao sa. Tiếng sói tru xé toạc màn đêm lạnh giá, báo hiệu cuộc tử chiến sinh tồn bắt đầu!', updated_at=NOW() WHERE id=81;
UPDATE chapters SET content='Hồi 4: Người đầu tiên bước ra và bình minh đế chế

"GẦU U U U!"

Con Sói Đầu Đàn to lớn như một cỗ xe ngựa, toàn thân phủ lớp lông gai nhọn màu đen tuyền gầm lên một tiếng chấn động, phát động toàn lực xung phong! Hàng trăm bóng đen xé gió lao nhanh trên nền tuyết, hướng thẳng về phía cổng thành Tuyết Phong Quan.

"Giữ vững vị trí! Đợi hiệu lệnh của ta!" Lâm Thần đứng trên tường thành cao, tay giơ cao thanh bảo kiếm rực sáng linh quang.

Khi đàn sói tràn tới mép hào chông cự ly 50 mét:
"BẮN TÊN LỬA!"

Một cơn mưa tên rực lửa phóng xuống rãnh hào. Nhựa thông bắt lửa bùng cháy dữ dội, tạo thành một bức tường lửa cao năm mét ngăn đôi đàn thú dữ. Hàng trăm con sói ma hoảng loạn rớt xuống hào chông rên xiết thảm thiết.

"MÁY BẮN ĐÁ, KHAI HOẢ!"

"VÚT... VÚT... UỲNH!" Ba quả cầu lửa khổng lồ bay vút qua không trung, nện thẳng vào giữa đội hình bầy sói phía sau, tạo nên những vụ nổ kinh thiên động địa quét sạch toàn bộ viện binh của địch.

Con Sói Đầu Đàn gầm rống điên cuồng, đạp lên xác đồng loại nhảy vọt qua bức tường lửa, lao thẳng lên mặt thành nhắm vào cổ họng Lâm Thần. Nhưng Lâm Thần không hề nao núng. Anh kích hoạt cơ quan bẫy điện dung nạp từ lõi tinh thể năng lượng của Viên Đá Lãnh Chúa, một tia sét xanh tím giáng thẳng vào đỉnh đầu con quái thú, đánh gục nó ngay tại chỗ!

Khi bình minh hé rạng, bão tuyết tan dần, để lại chiến trường rải rác xác thú dữ và một Tuyết Phong Quan đứng vững vàng bất khả xâm phạm.

Ba ngày sau, tiếng chuông đồng của đoàn lạc đà buôn bán vang lên từ hẻm núi tuyết. Đoàn thương buôn lớn nhất của Đế quốc Ánh Sáng mạo hiểm vượt đèo tới nơi, ngỡ ngàng dụi mắt trước một toà thành kiên cố, ống khói nhà máy nghi ngút và những người dân no ấm tươi cười.

Vị trưởng đoàn thương buôn quỳ gối trước cổng thành, dâng lên tấm bản đồ giao thương vàng ròng:
"Thưa Lãnh chúa vĩ đại... ngài có muốn bán số thép tôi luyện và lương thực kỳ diệu này cho chúng tôi không?"

Lâm Thần đứng trên đỉnh tháp thành, phóng tầm mắt về phía chân trời vô tận. Đây mới chỉ là khởi đầu của đế chế mà anh sẽ dựng xây.', updated_at=NOW() WHERE id=82;

-- Cập nhật view count và thời gian mới nhất cho stories
UPDATE stories SET view_count=35400, updated_at=NOW() WHERE id=13;
UPDATE stories SET view_count=18900, updated_at=NOW() WHERE id=3;
UPDATE stories SET view_count=21500, updated_at=NOW() WHERE id=4;
UPDATE stories SET view_count=29800, updated_at=NOW() WHERE id=5;

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;