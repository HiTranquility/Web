package truyen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import truyen.model.Chapter;
import truyen.model.Story;
import truyen.util.EpubWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EpubWriter — Xuất truyện ra định dạng EPUB 2.0 chuẩn (ISSUE-021)")
class EpubWriterTest {

    private Map<String, byte[]> readZipEntries(byte[] zipData, List<String> entryOrder) throws IOException {
        Map<String, byte[]> map = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryOrder.add(entry.getName());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = zis.read(buf)) > 0) {
                    baos.write(buf, 0, len);
                }
                map.put(entry.getName(), baos.toByteArray());
                zis.closeEntry();
            }
        }
        return map;
    }

    @Test
    @DisplayName("Ca 1: Entry ĐẦU TIÊN bắt buộc là mimetype với nội dung application/epub+zip")
    void testMimetypeFirstEntry() throws IOException {
        Story story = new Story();
        story.setId(1);
        story.setTitle("Huyền Thiên Tiên Kỷ");
        story.setAuthorName("Tiêu Tiêu Phong");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EpubWriter.write(out, story, new ArrayList<>());

        byte[] zipBytes = out.toByteArray();
        assertTrue(zipBytes.length > 50, "File EPUB sinh ra phải có nội dung");

        List<String> entryOrder = new ArrayList<>();
        Map<String, byte[]> entries = readZipEntries(zipBytes, entryOrder);

        assertFalse(entryOrder.isEmpty(), "File ZIP không được rỗng");
        assertEquals("mimetype", entryOrder.get(0), "Quy tắc EPUB: mimetype BẮT BUỘC là entry đầu tiên");

        String mimeContent = new String(entries.get("mimetype"), StandardCharsets.US_ASCII);
        assertEquals("application/epub+zip", mimeContent);
    }

    @Test
    @DisplayName("Ca 2: Cấu trúc file EPUB có đầy đủ container.xml, content.opf, toc.ncx, style.css")
    void testRequiredEpubStructure() throws IOException {
        Story story = new Story();
        story.setId(10);
        story.setTitle("Bá Vương Chi Kiếm");
        story.setAuthorName("Hắc Dạ Thần");
        story.setDescription("Truyện tu tiên kỳ ảo đỉnh cao.");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EpubWriter.write(out, story, new ArrayList<>());

        List<String> order = new ArrayList<>();
        Map<String, byte[]> entries = readZipEntries(out.toByteArray(), order);

        assertTrue(entries.containsKey("META-INF/container.xml"), "Thiếu META-INF/container.xml");
        assertTrue(entries.containsKey("OEBPS/content.opf"), "Thiếu OEBPS/content.opf");
        assertTrue(entries.containsKey("OEBPS/toc.ncx"), "Thiếu OEBPS/toc.ncx");
        assertTrue(entries.containsKey("OEBPS/style.css"), "Thiếu OEBPS/style.css");
        assertTrue(entries.containsKey("OEBPS/title.xhtml"), "Thiếu OEBPS/title.xhtml");

        String container = new String(entries.get("META-INF/container.xml"), StandardCharsets.UTF_8);
        assertTrue(container.contains("full-path=\"OEBPS/content.opf\""));
    }

    @Test
    @DisplayName("Ca 3: Escape XML chuẩn — các ký tự &, <, >, \", ' không làm vỡ cú pháp")
    void testXmlEscapeSpecialCharacters() {
        assertEquals("Tom &amp; Jerry", EpubWriter.escapeXml("Tom & Jerry"));
        assertEquals("&lt;Lời tác giả&gt;", EpubWriter.escapeXml("<Lời tác giả>"));
        assertEquals("&quot;Tuyệt phẩm&quot;", EpubWriter.escapeXml("\"Tuyệt phẩm\""));
        assertEquals("It&apos;s fine", EpubWriter.escapeXml("It's fine"));
        assertEquals("", EpubWriter.escapeXml(null));
    }

    @Test
    @DisplayName("Ca 4: Truyện 0 chương -> không sập, có trang thông báo tử tế")
    void testZeroChaptersHandledGracefully() throws IOException {
        Story story = new Story();
        story.setId(99);
        story.setTitle("Truyện Mới Chưa Viết");
        story.setAuthorName("Tác giả tập sự");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EpubWriter.write(out, story, new ArrayList<>());

        List<String> order = new ArrayList<>();
        Map<String, byte[]> entries = readZipEntries(out.toByteArray(), order);

        assertTrue(entries.containsKey("OEBPS/notice.xhtml"), "Phải có trang thông báo khi chưa có chương");
        String notice = new String(entries.get("OEBPS/notice.xhtml"), StandardCharsets.UTF_8);
        assertTrue(notice.contains("Truyện hiện chưa có chương nào được xuất bản"));
    }

    @Test
    @DisplayName("Ca 5: Truyện có nhiều chương -> sinh đủ các file chapter xhtml và mục lục ncx")
    void testMultipleChaptersGeneration() throws IOException {
        Story story = new Story();
        story.setId(25);
        story.setTitle("Vạn Cổ Đệ Nhất Thần");
        story.setAuthorName("Phong Thanh Dương");
        story.setDescription("Cuộc hành trình từ đống tro tàn vươn lên đỉnh cao vũ trụ.");

        List<Chapter> chapters = new ArrayList<>();

        Chapter c1 = new Chapter();
        c1.setChapterNo(1);
        c1.setTitle("Huyết Kiếp");
        c1.setContent("Mưa rơi như trút nước trên đỉnh Thiên Thần Sơn.\nThiếu niên mở mắt ra, trong lòng rực lửa báo thù.");
        chapters.add(c1);

        Chapter c2 = new Chapter();
        c2.setChapterNo(2);
        c2.setTitle("Thức Tỉnh Võ Hồn");
        c2.setContent("Một luồng sáng vàng rực bùng nổ từ đan điền.\nVõ hồn thượng cổ giáng thế, kinh động bát phương!");
        chapters.add(c2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EpubWriter.write(out, story, chapters);

        List<String> order = new ArrayList<>();
        Map<String, byte[]> entries = readZipEntries(out.toByteArray(), order);

        assertTrue(entries.containsKey("OEBPS/chapter_001.xhtml"), "Thiếu chương 1");
        assertTrue(entries.containsKey("OEBPS/chapter_002.xhtml"), "Thiếu chương 2");

        String ch1 = new String(entries.get("OEBPS/chapter_001.xhtml"), StandardCharsets.UTF_8);
        assertTrue(ch1.contains("Huyết Kiếp"));
        assertTrue(ch1.contains("<p>Mưa rơi như trút nước trên đỉnh Thiên Thần Sơn.</p>"));

        String toc = new String(entries.get("OEBPS/toc.ncx"), StandardCharsets.UTF_8);
        assertTrue(toc.contains("Huyết Kiếp"));
        assertTrue(toc.contains("Thức Tỉnh Võ Hồn"));
        assertTrue(toc.contains("chapter_001.xhtml"));
        assertTrue(toc.contains("chapter_002.xhtml"));

        String opf = new String(entries.get("OEBPS/content.opf"), StandardCharsets.UTF_8);
        assertTrue(opf.contains("Vạn Cổ Đệ Nhất Thần"));
        assertTrue(opf.contains("Phong Thanh Dương"));
        assertTrue(opf.contains("id=\"ch-001\""));
        assertTrue(opf.contains("id=\"ch-002\""));
    }
}
