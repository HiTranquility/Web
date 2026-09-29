package truyen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;

/**
 * Tiện ích chặn tấn công dò mật khẩu (Brute-force) và chống spam bình luận.
 *
 * Lưu trữ trong bộ nhớ RAM ứng dụng (in-memory sliding window), luồng an toàn (thread-safe),
 * tự động dọn dẹp các mục cũ để tối ưu tài nguyên và ngăn chặn rò rỉ bộ nhớ.
 */
public final class RateLimiter {

    /** Số lần đăng nhập sai tối đa trước khi tạm khóa. */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /** Thời gian khóa đăng nhập: 15 phút (tính bằng mili-giây). */
    public static final long LOGIN_LOCKOUT_MILLIS = 15 * 60 * 1000L;

    /** Khoảng cách tối thiểu giữa 2 lần bình luận: 20 giây (mili-giây). */
    public static final long COMMENT_COOLDOWN_MILLIS = 20 * 1000L;

    /**
     * Bản đồ lưu lịch sử thất bại đăng nhập.
     * Khóa: IP + ":" + username (hoặc chỉ IP nếu không có username).
     * Giá trị: Danh sách timestamp các lần thử sai trong cửa sổ thời gian.
     */
    private static final Map<String, List<Long>> loginFailures = new ConcurrentHashMap<>();

    /**
     * Bản đồ lưu mốc thời gian bình luận gần nhất theo userId.
     * Khóa: userId.
     * Giá trị: timestamp lần bình luận thành công cuối cùng.
     */
    private static final Map<Integer, Long> lastCommentTimestamps = new ConcurrentHashMap<>();

    private RateLimiter() {
        // Chặn khởi tạo class tiện ích thuần static
    }

    // ========================================================================
    //  TIỆN ÍCH TRÍCH XUẤT IP
    // ========================================================================

    /**
     * Lấy IP thực của client, hỗ trợ trường hợp chạy sau Reverse Proxy hoặc CDN (X-Forwarded-For).
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            int commaIdx = xff.indexOf(',');
            return (commaIdx != -1 ? xff.substring(0, commaIdx) : xff).trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.trim().isEmpty()) ? remoteAddr.trim() : "127.0.0.1";
    }

    private static String buildLoginKey(String ip, String username) {
        String safeIp = (ip == null || ip.trim().isEmpty()) ? "127.0.0.1" : ip.trim();
        String safeUser = (username == null) ? "" : username.trim().toLowerCase();
        return safeIp + ":" + safeUser;
    }

    // ========================================================================
    //  CHẶN DÒ MẬT KHẨU (LOGIN RATE LIMITING)
    // ========================================================================

    /**
     * Kiểm tra xem cặp IP + username này có đang bị chặn đăng nhập do thử sai quá nhiều lần hay không.
     */
    public static boolean isLoginBlocked(String ip, String username) {
        String key = buildLoginKey(ip, username);
        List<Long> timestamps = loginFailures.get(key);
        if (timestamps == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        synchronized (timestamps) {
            cleanExpired(timestamps, now, LOGIN_LOCKOUT_MILLIS);
            return timestamps.size() >= MAX_LOGIN_ATTEMPTS;
        }
    }

    /**
     * Ghi nhận một lần đăng nhập thất bại.
     */
    public static void recordLoginFailure(String ip, String username) {
        String key = buildLoginKey(ip, username);
        loginFailures.compute(key, (k, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            long now = System.currentTimeMillis();
            synchronized (timestamps) {
                cleanExpired(timestamps, now, LOGIN_LOCKOUT_MILLIS);
                timestamps.add(now);
            }
            return timestamps;
        });
    }

    /**
     * Xóa bỏ lịch sử thất bại khi người dùng đăng nhập thành công.
     */
    public static void resetLoginAttempts(String ip, String username) {
        String key = buildLoginKey(ip, username);
        loginFailures.remove(key);
    }

    /**
     * Số lần thử còn lại trước khi bị khóa tạm thời.
     */
    public static int getRemainingAttempts(String ip, String username) {
        String key = buildLoginKey(ip, username);
        List<Long> timestamps = loginFailures.get(key);
        if (timestamps == null) {
            return MAX_LOGIN_ATTEMPTS;
        }

        long now = System.currentTimeMillis();
        synchronized (timestamps) {
            cleanExpired(timestamps, now, LOGIN_LOCKOUT_MILLIS);
            int remaining = MAX_LOGIN_ATTEMPTS - timestamps.size();
            return Math.max(0, remaining);
        }
    }

    /**
     * Tính số phút còn lại phải chờ mở khóa (tối thiểu 1 phút nếu đang bị chặn).
     */
    public static long getLoginBlockedRemainingMinutes(String ip, String username) {
        String key = buildLoginKey(ip, username);
        List<Long> timestamps = loginFailures.get(key);
        if (timestamps == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        synchronized (timestamps) {
            cleanExpired(timestamps, now, LOGIN_LOCKOUT_MILLIS);
            if (timestamps.size() < MAX_LOGIN_ATTEMPTS) {
                return 0;
            }
            // Lần thất bại sớm nhất quyết định khi nào lần đó hết hạn
            long oldestTimestamp = timestamps.get(0);
            long elapsed = now - oldestTimestamp;
            long remainingMillis = LOGIN_LOCKOUT_MILLIS - elapsed;
            if (remainingMillis <= 0) {
                return 0;
            }
            return Math.max(1, (long) Math.ceil(remainingMillis / 60000.0));
        }
    }

    // ========================================================================
    //  CHỐNG SPAM BÌNH LUẬN (COMMENT COOLDOWN)
    // ========================================================================

    /**
     * Kiểm tra người dùng có đang bình luận quá nhanh (nhỏ hơn 20s kể từ lần trước) hay không.
     */
    public static boolean isCommentSpam(int userId) {
        if (userId <= 0) {
            return false;
        }
        Long lastTime = lastCommentTimestamps.get(userId);
        if (lastTime == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - lastTime;
        return elapsed < COMMENT_COOLDOWN_MILLIS;
    }

    /**
     * Ghi nhận một lần bình luận thành công để bắt đầu tính cooldown.
     */
    public static void recordComment(int userId) {
        if (userId > 0) {
            lastCommentTimestamps.put(userId, System.currentTimeMillis());
        }
    }

    /**
     * Số giây cooldown còn lại trước khi được phép bình luận tiếp.
     */
    public static long getCommentCooldownRemainingSeconds(int userId) {
        if (userId <= 0) {
            return 0;
        }
        Long lastTime = lastCommentTimestamps.get(userId);
        if (lastTime == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - lastTime;
        long remaining = COMMENT_COOLDOWN_MILLIS - elapsed;
        if (remaining <= 0) {
            return 0;
        }
        return Math.max(1, (long) Math.ceil(remaining / 1000.0));
    }

    // ========================================================================
    //  HỖ TRỢ KIỂM THỬ VÀ DỌN DẸP
    // ========================================================================

    // ========================================================================
    //  CHỐNG SPAM BÁO CÁO VI PHẠM  (ISSUE-025)
    // ========================================================================
    //  VÌ SAO ĐẾM SỐ LẦN CHỨ KHÔNG PHẢI COOLDOWN NHƯ BÌNH LUẬN
    //    Bình luận thì chặn "gõ quá nhanh" là đủ. Báo cáo thì khác: mỗi lần
    //    gửi có thể kéo theo BA ẢNH nằm lại trên ổ đĩa máy chủ. Một tài khoản
    //    viết vòng lặp gửi báo cáo là đầy đĩa — mà đúng hôm nay ổ C của máy
    //    dev đã đầy 100% một lần rồi, nên đây không phải lo xa.
    //
    //    Cửa sổ trượt 1 phút / 3 lượt: người thật báo cáo vài cái liền nhau
    //    vẫn lọt, còn vòng lặp thì chặn ngay từ lượt thứ tư.
    // ========================================================================

    /** Tối đa bao nhiêu báo cáo trong một cửa sổ. */
    public static final int MAX_REPORTS_PER_WINDOW = 3;

    /** Độ dài cửa sổ trượt cho báo cáo: 1 phút. */
    public static final long REPORT_WINDOW_MILLIS = 60 * 1000L;

    private static final Map<Integer, List<Long>> reportTimes = new ConcurrentHashMap<>();

    /** Người này có đang gửi báo cáo quá dày không. */
    public static boolean isReportSpam(int userId) {
        if (userId <= 0) {
            return false;
        }
        List<Long> times = reportTimes.get(userId);
        if (times == null) {
            return false;
        }
        synchronized (times) {
            cleanExpired(times, System.currentTimeMillis(), REPORT_WINDOW_MILLIS);
            return times.size() >= MAX_REPORTS_PER_WINDOW;
        }
    }

    /** Ghi nhận một báo cáo vừa gửi thành công. */
    public static void recordReport(int userId) {
        if (userId <= 0) {
            return;
        }
        List<Long> times =
                reportTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        synchronized (times) {
            long now = System.currentTimeMillis();
            cleanExpired(times, now, REPORT_WINDOW_MILLIS);
            times.add(now);
        }
    }

    /** Còn phải chờ bao nhiêu giây nữa mới gửi báo cáo tiếp được. */
    public static long getReportCooldownRemainingSeconds(int userId) {
        if (userId <= 0) {
            return 0;
        }
        List<Long> times = reportTimes.get(userId);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            long now = System.currentTimeMillis();
            cleanExpired(times, now, REPORT_WINDOW_MILLIS);
            if (times.size() < MAX_REPORTS_PER_WINDOW) {
                return 0;
            }
            // Lượt sớm nhất rơi ra khỏi cửa sổ lúc nào thì mở khoá lúc đó.
            long remaining = REPORT_WINDOW_MILLIS - (now - times.get(0));
            return remaining <= 0 ? 0 : Math.max(1, remaining / 1000);
        }
    }

    // ========================================================================
    //  CHỐNG SPAM QUÊN MẬT KHẨU (FORGOT PASSWORD RATE LIMITING)
    // ========================================================================
    public static final int MAX_FORGOT_ATTEMPTS = 3;
    public static final long FORGOT_WINDOW_MILLIS = 15 * 60 * 1000L;
    private static final Map<String, List<Long>> forgotAttempts = new ConcurrentHashMap<>();

    public static boolean isForgotSpam(String ip, String email) {
        String key = (ip == null ? "127.0.0.1" : ip.trim()) + ":" + (email == null ? "" : email.trim().toLowerCase());
        List<Long> times = forgotAttempts.get(key);
        if (times == null) {
            return false;
        }
        synchronized (times) {
            cleanExpired(times, System.currentTimeMillis(), FORGOT_WINDOW_MILLIS);
            return times.size() >= MAX_FORGOT_ATTEMPTS;
        }
    }

    public static void recordForgot(String ip, String email) {
        String key = (ip == null ? "127.0.0.1" : ip.trim()) + ":" + (email == null ? "" : email.trim().toLowerCase());
        List<Long> times = forgotAttempts.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (times) {
            long now = System.currentTimeMillis();
            cleanExpired(times, now, FORGOT_WINDOW_MILLIS);
            times.add(now);
        }
    }

    /**
     * Xóa sạch dữ liệu trong bộ đệm (chủ yếu phục vụ Unit Test).
     */
    public static void clear() {
        loginFailures.clear();
        lastCommentTimestamps.clear();
        reportTimes.clear();
        forgotAttempts.clear();
    }

    private static void cleanExpired(List<Long> timestamps, long now, long maxAgeMillis) {
        Iterator<Long> it = timestamps.iterator();
        while (it.hasNext()) {
            Long t = it.next();
            if (now - t > maxAgeMillis) {
                it.remove();
            }
        }
    }
}
