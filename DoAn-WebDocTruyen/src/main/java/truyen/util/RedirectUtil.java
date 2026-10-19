package truyen.util;

import java.net.URI;
import java.net.URISyntaxException;
import truyen.model.User;

/** Kiểm tra đường dẫn nội bộ trước khi nối context và chuyển hướng. */
public final class RedirectUtil {
    private RedirectUtil() { }

    public static String localPath(String target, String contextPath) {
        if (target == null || !target.startsWith("/") || target.startsWith("//")) return null;
        try {
            URI uri = new URI(target);
            String path = uri.getPath();
            if (uri.isAbsolute() || uri.getRawAuthority() != null || path == null
                    || path.contains("//") || path.contains("\\") || path.contains(";")
                    || !new URI(null, null, path, null).normalize().getPath().equals(path)
                    || target.matches("(?is).*%(?:2f|5c|25|0[0-9a-f]|1[0-9a-f]|7f).*")) return null;
            for (int i = 0; i < target.length(); i++) {
                if (Character.isISOControl(target.charAt(i))) return null;
            }
            for (String segment : path.split("/")) {
                if (".".equals(segment) || "..".equals(segment)) return null;
            }
            String result = uri.getRawPath();
            if (contextPath != null && !contextPath.isEmpty()) {
                if (result.equals(contextPath)) result = "/";
                else if (result.startsWith(contextPath + "/")) result = result.substring(contextPath.length());
            }
            if (!result.startsWith("/") || result.startsWith("//")) return null;
            return result + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public static String afterLogin(User user, String target, String contextPath) {
        String fallback = (user != null && user.isAdmin()) ? "/admin/dashboard" : "/";
        String local = localPath(target, contextPath);
        if (local == null) return fallback;
        String path = URI.create(local).getPath();
        if ("/auth".equals(path) || path.startsWith("/auth/") || "/error".equals(path)
                || path.startsWith("/WEB-INF/") || path.startsWith("/META-INF/")) return fallback;
        if (("/admin".equals(path) || path.startsWith("/admin/")) && !AdminAccess.canAccess(user, path)) {
            return fallback;
        }
        return local;
    }
}
