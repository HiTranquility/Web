package truyen.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Đọc KÍCH THƯỚC ảnh (rộng x cao) từ phần đầu file — PNG, JPEG, GIF, WebP.
 *
 * VÌ SAO TỰ ĐỌC MÀ KHÔNG DÙNG javax.imageio
 *   ImageIO giải mã CẢ ảnh vào bộ nhớ chỉ để hỏi kích thước — trang truyện
 *   tranh 2000x6000 là ~48 MB RAM cho một con số. Lại không đọc được WebP.
 *   Kích thước nằm sẵn trong vài chục byte đầu (JPEG thì ở khung SOF), đọc
 *   thẳng ở đó là đủ.
 *
 * VÌ SAO CẦN KÍCH THƯỚC
 *   Trang đọc in sẵn width/height lên thẻ <img> để trình duyệt GIỮ CHỖ đúng tỉ
 *   lệ trước khi ảnh tải xong. Không có thì ảnh lazy-load hiện tới đâu trang
 *   nhảy tới đó, và "đọc tiếp tới trang 20" cuộn tới chỗ sai.
 *
 * Đọc không được (file hỏng, định dạng lạ) thì trả null — trang vẫn hiện,
 * chỉ mất phần giữ chỗ. Không bao giờ ném lỗi vì một con số phụ.
 */
public final class ImageInfo {

    /** Ảnh dài hơn chừng này byte mà chưa thấy khung SOF thì bỏ cuộc. */
    private static final int JPEG_SCAN_LIMIT = 512 * 1024;

    private ImageInfo() { }

    /** @return {rộng, cao} hoặc null nếu không đọc được. */
    public static int[] dimensions(InputStream in) throws IOException {
        byte[] h = readFully(in, 30);
        if (h.length < 24) return null;

        // PNG: chữ ký 8 byte, rồi khung IHDR — rộng ở byte 16..19, cao 20..23
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return sane(be32(h, 16), be32(h, 20));
        }
        // GIF: "GIF8xa", rộng/cao little-endian 16 bit ở byte 6..9
        if (h[0] == 'G' && h[1] == 'I' && h[2] == 'F') {
            return sane(le16(h, 6), le16(h, 8));
        }
        // WebP: "RIFF" .... "WEBP" + khung VP8 / VP8L / VP8X
        if (h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return webp(h);
        }
        // JPEG: FF D8 — phải dò từng khung tới SOF
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8) {
            return jpeg(h, in);
        }
        return null;
    }

    private static int[] webp(byte[] h) {
        if (h.length < 30) return null;
        String chunk = new String(h, 12, 4, java.nio.charset.StandardCharsets.US_ASCII);
        switch (chunk) {
            case "VP8 ":   // lossy: rộng/cao 14 bit ở byte 26..29
                return sane(le16(h, 26) & 0x3FFF, le16(h, 28) & 0x3FFF);
            case "VP8L": { // lossless: 14 bit rộng-1, 14 bit cao-1 từ byte 21
                int b0 = h[21] & 0xFF, b1 = h[22] & 0xFF, b2 = h[23] & 0xFF, b3 = h[24] & 0xFF;
                int w = 1 + (((b1 & 0x3F) << 8) | b0);
                int hh = 1 + (((b3 & 0x0F) << 10) | (b2 << 2) | ((b1 & 0xC0) >> 6));
                return sane(w, hh);
            }
            case "VP8X":   // mở rộng: rộng-1 / cao-1, 24 bit little-endian ở 24..29
                return sane(1 + le24(h, 24), 1 + le24(h, 27));
            default:
                return null;
        }
    }

    /**
     * Dò các khung JPEG: FF xx [độ dài 2 byte] [dữ liệu]. Khung SOF0..SOF15
     * (trừ C4 DHT, C8, CC DAC) chứa: độ chính xác 1 byte, cao 2 byte, rộng 2 byte.
     */
    private static int[] jpeg(byte[] head, InputStream rest) throws IOException {
        java.io.InputStream in = new java.io.SequenceInputStream(
                new java.io.ByteArrayInputStream(head, 2, head.length - 2), rest);
        int read = 2;
        while (read < JPEG_SCAN_LIMIT) {
            int b = in.read();
            if (b < 0) return null;
            read++;
            if (b != 0xFF) continue;
            int marker;
            do { marker = in.read(); read++; } while (marker == 0xFF);   // đệm FF
            if (marker < 0) return null;
            if (marker == 0xD8 || marker == 0x01 || (marker >= 0xD0 && marker <= 0xD7)) continue;
            if (marker == 0xD9 || marker == 0xDA) return null;            // hết ảnh / bắt đầu dữ liệu nén

            byte[] lenB = readFully(in, 2);
            if (lenB.length < 2) return null;
            int len = be16(lenB, 0);
            read += 2;
            boolean sof = marker >= 0xC0 && marker <= 0xCF
                    && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
            if (sof) {
                byte[] s = readFully(in, 5);
                if (s.length < 5) return null;
                return sane(be16(s, 3), be16(s, 1));
            }
            long skip = len - 2;
            while (skip > 0) {
                long n = in.skip(skip);
                if (n <= 0) { if (in.read() < 0) return null; n = 1; }
                skip -= n;
                read += n;
            }
        }
        return null;
    }

    /** Chặn số vô lý (file giả mạo): 1..30000 px mỗi chiều. */
    private static int[] sane(int w, int h) {
        return (w > 0 && h > 0 && w <= 30000 && h <= 30000) ? new int[] { w, h } : null;
    }

    private static byte[] readFully(InputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        int got = 0;
        while (got < n) {
            int r = in.read(buf, got, n - got);
            if (r < 0) break;
            got += r;
        }
        return got == n ? buf : java.util.Arrays.copyOf(buf, got);
    }

    private static int be16(byte[] b, int o) { return ((b[o] & 0xFF) << 8) | (b[o + 1] & 0xFF); }
    private static int le16(byte[] b, int o) { return (b[o] & 0xFF) | ((b[o + 1] & 0xFF) << 8); }
    private static int le24(byte[] b, int o) { return le16(b, o) | ((b[o + 2] & 0xFF) << 16); }
    private static int be32(byte[] b, int o) {
        return ((b[o] & 0xFF) << 24) | ((b[o + 1] & 0xFF) << 16) | ((b[o + 2] & 0xFF) << 8) | (b[o + 3] & 0xFF);
    }
}
