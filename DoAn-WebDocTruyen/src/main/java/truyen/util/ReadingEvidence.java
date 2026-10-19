package truyen.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;

/** Tín hiệu đọc tối thiểu, không nhận thời gian do client tự khai. */
public final class ReadingEvidence {
    private static final String KEY = "readingStartedAt";
    public static final long MIN_READING_MILLIS = 30_000;
    private ReadingEvidence() { }

    @SuppressWarnings("unchecked")
    public static void opened(HttpSession session, int chapterId, long now) {
        if (session == null || chapterId <= 0) return;
        synchronized (session) {
            Map<Integer, Long> starts = (Map<Integer, Long>) session.getAttribute(KEY);
            if (starts == null) { starts = new ConcurrentHashMap<>(); session.setAttribute(KEY, starts); }
            // Giới hạn bộ nhớ khi người đọc mở rất nhiều chương trong cùng phiên.
            starts.entrySet().removeIf(entry -> now - entry.getValue() > 3_600_000);
            if (starts.size() >= 100 && !starts.containsKey(chapterId)) {
                Integer oldest = starts.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
                starts.remove(oldest);
            }
            starts.putIfAbsent(chapterId, now);
        }
    }

    @SuppressWarnings("unchecked")
    public static boolean qualifies(HttpSession session, int chapterId, int position, int total, long now) {
        if (session == null || total <= 0 || position < Math.ceil(total * 0.8)) return false;
        Map<Integer, Long> starts = (Map<Integer, Long>) session.getAttribute(KEY);
        Long started = starts == null ? null : starts.get(chapterId);
        return started != null && now - started >= MIN_READING_MILLIS;
    }
}
