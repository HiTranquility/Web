package truyen.util;

import truyen.model.User;

/** Một chính sách dùng cho bộ lọc và đích đến sau đăng nhập. */
public final class AdminAccess {
    private AdminAccess() { }

    public static boolean isStaff(User user) {
        return user != null && (user.isAdmin() || user.isModerator() || user.isEditor()
                || (user.getPermissions() != null && !user.getPermissions().isEmpty()));
    }

    public static boolean canAccess(User user, String path) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        if (!isStaff(user)) return false;
        if (matches(path, "/admin/wallet")) return user.hasPermission("MANAGE_WALLET");
        if (matches(path, "/admin/user")) return user.hasPermission("MANAGE_USERS");
        if (matches(path, "/admin/audit")) {
            return user.hasPermission("MANAGE_SYSTEM") || user.hasPermission("MANAGE_WALLET");
        }
        if (matches(path, "/admin/tag") || matches(path, "/admin/story")) {
            return user.hasPermission("MANAGE_STORIES");
        }
        if (matches(path, "/admin/comment")) return user.hasPermission("MODERATE_COMMENTS");
        if (matches(path, "/admin/report")) return user.hasPermission("HANDLE_REPORTS");
        return "/admin".equals(path) || "/admin/".equals(path) || matches(path, "/admin/dashboard");
    }

    private static boolean matches(String path, String root) {
        return root.equals(path) || path.startsWith(root + "/");
    }
}
