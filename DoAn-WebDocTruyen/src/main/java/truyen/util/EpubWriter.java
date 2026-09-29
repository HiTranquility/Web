package truyen.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import truyen.model.Chapter;
import truyen.model.Story;

/**
 * Tiện ích xuất truyện ra định dạng EPUB 2.0 chuẩn quốc tế (ISSUE-021).
 *
 * <p>EPUB thực chất là một gói lưu trữ ZIP chứa các tài liệu XHTML và XML metadata.
 * Dùng thuần thư viện {@code java.util.zip} có sẵn trong JDK, không cần thêm thư viện ngoài.
 *
 * <p>QUY TẮC BẮT BUỘC CỦA CHUẨN EPUB:
 * <ul>
 *   <li>File đầu tiên trong ZIP BẮT BUỘC là {@code mimetype} với nội dung
 *       {@code application/epub+zip}.</li>
 *   <li>{@code mimetype} BẮT BUỘC lưu ở dạng STORED (không nén), tự tính size và CRC32.</li>
 *   <li>Thư mục {@code META-INF/container.xml} trỏ tới gói nội dung {@code OEBPS/content.opf}.</li>
 *   <li>Mọi tài liệu chương phải tuân thủ chuẩn XHTML 1.1 nghiêm ngặt và escape XML đúng.</li>
 * </ul>
 */
public final class EpubWriter {

    private static final String MIME_TYPE = "application/epub+zip";

    private EpubWriter() {
        // Chặn khởi tạo class tiện ích thuần static
    }

    /**
     * Ghi toàn bộ truyện và danh sách chương ra OutputStream dưới dạng file EPUB.
     */
    public static void write(OutputStream out, Story story, List<Chapter> chapters) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("OutputStream không được null");
        }

        String bookId = "urn:uuid:" + (story != null && story.getId() > 0
                ? UUID.nameUUIDFromBytes(("story-" + story.getId()).getBytes(StandardCharsets.UTF_8))
                : UUID.randomUUID());
        String title = story != null && story.getTitle() != null && !story.getTitle().trim().isEmpty()
                ? story.getTitle().trim() : "Truyện chưa có tiêu đề";
        String author = story != null && story.getAuthorName() != null && !story.getAuthorName().trim().isEmpty()
                ? story.getAuthorName().trim() : "Tác giả ẩn danh";
        String description = story != null && story.getDescription() != null
                ? story.getDescription().trim() : "";

        ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8);

        // 1. mimetype (Entry đầu tiên, STORED, không nén)
        writeMimetype(zip);

        // 2. META-INF/container.xml
        writeContainerXml(zip);

        // 3. OEBPS/style.css
        writeStyleCss(zip);

        // 4. OEBPS/title.xhtml (Trang bìa giới thiệu)
        writeTitlePage(zip, title, author, description);

        // 5. OEBPS/chuong-xxx.xhtml (Các chương truyện)
        if (chapters == null || chapters.isEmpty()) {
            writeEmptyNoticePage(zip);
        } else {
            for (int i = 0; i < chapters.size(); i++) {
                writeChapterPage(zip, chapters.get(i), i + 1);
            }
        }

        // 6. OEBPS/toc.ncx (Mục lục bấm được)
        writeTocNcx(zip, bookId, title, author, chapters);

        // 7. OEBPS/content.opf (Khai báo gói metadata và thứ tự đọc)
        writeContentOpf(zip, bookId, title, author, description, chapters);

        zip.finish();
        zip.flush();
    }

    /**
     * Ghi file mimetype: bắt buộc là entry đầu tiên và STORED (không nén).
     */
    private static void writeMimetype(ZipOutputStream zip) throws IOException {
        byte[] bytes = MIME_TYPE.getBytes(StandardCharsets.US_ASCII);

        ZipEntry entry = new ZipEntry("mimetype");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(bytes.length);
        entry.setCompressedSize(bytes.length);

        CRC32 crc = new CRC32();
        crc.update(bytes);
        entry.setCrc(crc.getValue());

        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }

    /**
     * Ghi META-INF/container.xml trỏ tới content.opf.
     */
    private static void writeContainerXml(ZipOutputStream zip) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                   + "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n"
                   + "  <rootfiles>\n"
                   + "    <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n"
                   + "  </rootfiles>\n"
                   + "</container>";
        addDeflatedEntry(zip, "META-INF/container.xml", xml);
    }

    /**
     * Ghi OEBPS/style.css định dạng chữ thanh lịch cho thiết bị đọc.
     */
    private static void writeStyleCss(ZipOutputStream zip) throws IOException {
        String css = "body {\n"
                   + "  font-family: serif, 'Times New Roman', 'Noto Serif';\n"
                   + "  line-height: 1.65;\n"
                   + "  margin: 5%;\n"
                   + "  color: #1a1a1a;\n"
                   + "}\n"
                   + "h1.book-title {\n"
                   + "  text-align: center;\n"
                   + "  font-size: 2em;\n"
                   + "  margin-top: 1.5em;\n"
                   + "  margin-bottom: 0.3em;\n"
                   + "  line-height: 1.2;\n"
                   + "}\n"
                   + ".book-author {\n"
                   + "  text-align: center;\n"
                   + "  font-style: italic;\n"
                   + "  font-size: 1.1em;\n"
                   + "  margin-bottom: 2em;\n"
                   + "  color: #555;\n"
                   + "}\n"
                   + ".book-desc {\n"
                   + "  margin: 2em auto;\n"
                   + "  max-width: 85%;\n"
                   + "  padding: 1.2em;\n"
                   + "  background: #f8f8f8;\n"
                   + "  border-left: 4px solid #f97316;\n"
                   + "  font-style: italic;\n"
                   + "  text-align: justify;\n"
                   + "}\n"
                   + "h2.chapter-title {\n"
                   + "  text-align: center;\n"
                   + "  font-size: 1.4em;\n"
                   + "  margin-top: 1.5em;\n"
                   + "  margin-bottom: 1.2em;\n"
                   + "  border-bottom: 1px solid #eee;\n"
                   + "  padding-bottom: 0.4em;\n"
                   + "}\n"
                   + "p {\n"
                   + "  text-indent: 1.5em;\n"
                   + "  margin-top: 0;\n"
                   + "  margin-bottom: 0.6em;\n"
                   + "  text-align: justify;\n"
                   + "}\n";
        addDeflatedEntry(zip, "OEBPS/style.css", css);
    }

    /**
     * Ghi trang bìa / giới thiệu OEBPS/title.xhtml.
     */
    private static void writeTitlePage(ZipOutputStream zip, String title, String author, String description) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"vi\">\n");
        sb.append("<head>\n");
        sb.append("  <title>").append(escapeXml(title)).append("</title>\n");
        sb.append("  <link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"/>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("  <h1 class=\"book-title\">").append(escapeXml(title)).append("</h1>\n");
        sb.append("  <div class=\"book-author\">Tác giả: ").append(escapeXml(author)).append("</div>\n");

        if (!description.isEmpty()) {
            sb.append("  <div class=\"book-desc\">\n");
            for (String p : description.split("\\R")) {
                String t = p.trim();
                if (!t.isEmpty()) {
                    sb.append("    <p>").append(escapeXml(t)).append("</p>\n");
                }
            }
            sb.append("  </div>\n");
        }

        sb.append("  <p style=\"text-align:center; font-size:0.9em; color:#888; margin-top:3em;\">Xuất bản điện tử từ ĐọcTruyện</p>\n");
        sb.append("</body>\n");
        sb.append("</html>");

        addDeflatedEntry(zip, "OEBPS/title.xhtml", sb.toString());
    }

    /**
     * Ghi trang thông báo khi truyện chưa có chương nào.
     */
    private static void writeEmptyNoticePage(ZipOutputStream zip) throws IOException {
        String xhtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                     + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
                     + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"vi\">\n"
                     + "<head>\n"
                     + "  <title>Thông báo</title>\n"
                     + "  <link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"/>\n"
                     + "</head>\n"
                     + "<body>\n"
                     + "  <h2 class=\"chapter-title\">Thông báo</h2>\n"
                     + "  <p style=\"text-align:center; font-style:italic;\">Truyện hiện chưa có chương nào được xuất bản.</p>\n"
                     + "</body>\n"
                     + "</html>";
        addDeflatedEntry(zip, "OEBPS/notice.xhtml", xhtml);
    }

    /**
     * Ghi một chương truyện ra OEBPS/chapter_xxx.xhtml.
     */
    private static void writeChapterPage(ZipOutputStream zip, Chapter ch, int index) throws IOException {
        String chTitle = ch.getTitle() != null && !ch.getTitle().trim().isEmpty()
                ? ch.getTitle().trim()
                : "Chương " + ch.getChapterNo();
        String filename = String.format("OEBPS/chapter_%03d.xhtml", index);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"vi\">\n");
        sb.append("<head>\n");
        sb.append("  <title>").append(escapeXml(chTitle)).append("</title>\n");
        sb.append("  <link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\"/>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("  <h2 class=\"chapter-title\">").append(escapeXml(chTitle)).append("</h2>\n");

        List<String> paragraphs = ch.getParagraphs();
        if (paragraphs.isEmpty()) {
            sb.append("  <p><i>(Nội dung chương trống)</i></p>\n");
        } else {
            for (String p : paragraphs) {
                sb.append("  <p>").append(escapeXml(p)).append("</p>\n");
            }
        }

        sb.append("</body>\n");
        sb.append("</html>");

        addDeflatedEntry(zip, filename, sb.toString());
    }

    /**
     * Ghi OEBPS/toc.ncx — chuẩn điều hướng mục lục cho máy đọc sách (Kindle, Apple Books).
     */
    private static void writeTocNcx(ZipOutputStream zip, String bookId, String title, String author, List<Chapter> chapters) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n");
        sb.append("  <head>\n");
        sb.append("    <meta name=\"dtb:uid\" content=\"").append(escapeXml(bookId)).append("\"/>\n");
        sb.append("    <meta name=\"dtb:depth\" content=\"1\"/>\n");
        sb.append("    <meta name=\"dtb:totalPageCount\" content=\"0\"/>\n");
        sb.append("    <meta name=\"dtb:maxPageNumber\" content=\"0\"/>\n");
        sb.append("  </head>\n");
        sb.append("  <docTitle><text>").append(escapeXml(title)).append("</text></docTitle>\n");
        sb.append("  <docAuthor><text>").append(escapeXml(author)).append("</text></docAuthor>\n");
        sb.append("  <navMap>\n");

        int playOrder = 1;
        sb.append("    <navPoint id=\"navPoint-").append(playOrder).append("\" playOrder=\"").append(playOrder).append("\">\n");
        sb.append("      <navLabel><text>Trang bìa</text></navLabel>\n");
        sb.append("      <content src=\"title.xhtml\"/>\n");
        sb.append("    </navPoint>\n");

        if (chapters == null || chapters.isEmpty()) {
            playOrder++;
            sb.append("    <navPoint id=\"navPoint-").append(playOrder).append("\" playOrder=\"").append(playOrder).append("\">\n");
            sb.append("      <navLabel><text>Thông báo</text></navLabel>\n");
            sb.append("      <content src=\"notice.xhtml\"/>\n");
            sb.append("    </navPoint>\n");
        } else {
            for (int i = 0; i < chapters.size(); i++) {
                playOrder++;
                Chapter ch = chapters.get(i);
                String chTitle = ch.getTitle() != null && !ch.getTitle().trim().isEmpty()
                        ? ch.getTitle().trim()
                        : "Chương " + ch.getChapterNo();
                String src = String.format("chapter_%03d.xhtml", i + 1);

                sb.append("    <navPoint id=\"navPoint-").append(playOrder).append("\" playOrder=\"").append(playOrder).append("\">\n");
                sb.append("      <navLabel><text>").append(escapeXml(chTitle)).append("</text></navLabel>\n");
                sb.append("      <content src=\"").append(src).append("\"/>\n");
                sb.append("    </navPoint>\n");
            }
        }

        sb.append("  </navMap>\n");
        sb.append("</ncx>");

        addDeflatedEntry(zip, "OEBPS/toc.ncx", sb.toString());
    }

    /**
     * Ghi OEBPS/content.opf — đóng gói thông tin metadata, danh sách tài nguyên và trình tự đọc (spine).
     */
    private static void writeContentOpf(ZipOutputStream zip, String bookId, String title, String author,
                                        String description, List<Chapter> chapters) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n");
        sb.append("  <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n");
        sb.append("    <dc:title>").append(escapeXml(title)).append("</dc:title>\n");
        sb.append("    <dc:creator opf:role=\"aut\">").append(escapeXml(author)).append("</dc:creator>\n");
        sb.append("    <dc:language>vi</dc:language>\n");
        sb.append("    <dc:identifier id=\"BookID\">").append(escapeXml(bookId)).append("</dc:identifier>\n");
        sb.append("    <dc:date>").append(LocalDate.now()).append("</dc:date>\n");
        if (!description.isEmpty()) {
            sb.append("    <dc:description>").append(escapeXml(description)).append("</dc:description>\n");
        }
        sb.append("  </metadata>\n");

        sb.append("  <manifest>\n");
        sb.append("    <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n");
        sb.append("    <item id=\"style\" href=\"style.css\" media-type=\"text/css\"/>\n");
        sb.append("    <item id=\"title-page\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>\n");

        if (chapters == null || chapters.isEmpty()) {
            sb.append("    <item id=\"notice\" href=\"notice.xhtml\" media-type=\"application/xhtml+xml\"/>\n");
        } else {
            for (int i = 0; i < chapters.size(); i++) {
                String id = String.format("ch-%03d", i + 1);
                String href = String.format("chapter_%03d.xhtml", i + 1);
                sb.append("    <item id=\"").append(id).append("\" href=\"").append(href).append("\" media-type=\"application/xhtml+xml\"/>\n");
            }
        }
        sb.append("  </manifest>\n");

        sb.append("  <spine toc=\"ncx\">\n");
        sb.append("    <itemref idref=\"title-page\"/>\n");
        if (chapters == null || chapters.isEmpty()) {
            sb.append("    <itemref idref=\"notice\"/>\n");
        } else {
            for (int i = 0; i < chapters.size(); i++) {
                String id = String.format("ch-%03d", i + 1);
                sb.append("    <itemref idref=\"").append(id).append("\"/>\n");
            }
        }
        sb.append("  </spine>\n");
        sb.append("</package>");

        addDeflatedEntry(zip, "OEBPS/content.opf", sb.toString());
    }

    /**
     * Helper thêm một entry text nén DEFLATED vào ZIP.
     */
    private static void addDeflatedEntry(ZipOutputStream zip, String path, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry(path);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }

    /**
     * Escape XML nghiêm ngặt cho XHTML (chặn lỗi cú pháp trong tài liệu e-book).
     */
    public static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
