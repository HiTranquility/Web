package truyen;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import truyen.model.User;
import truyen.util.AdminAccess;
import truyen.util.RedirectUtil;
import static org.junit.jupiter.api.Assertions.*;

class RedirectAccessTest {
    private User user(String role) { User u = new User(); u.setRole(role); return u; }

    @Test void rejectsExternalAmbiguousAndControlCharacterRedirects() {
        String[] bad = { "https://example.test", "//example.test", "/\\example.test", "/%2fexample.test",
                "/%5cexample.test", "/%252fexample.test", "/a/../admin", "/%2e%2e/admin", "/x;v/admin",
                "/x\r\nInjected: yes", "/x?x=%0d%0aInjected", "/app//example.test" };
        for (String value : bad) assertNull(RedirectUtil.localPath(value, "/app"), value);
    }

    @Test void stripsOnlyCompleteContextSegment() {
        assertEquals("/chapter?id=12", RedirectUtil.localPath("/app/chapter?id=12", "/app"));
        assertEquals("/apple", RedirectUtil.localPath("/apple", "/app"));
        assertEquals("/", RedirectUtil.localPath("/app", "/app"));
    }

    @Test void normalUserCannotBeLandedInAdminAreaOrAuthActions() {
        User u = user("USER");
        for (String path : new String[] { "/admin", "/admin/user", "/auth?action=logout", "/error?code=403" }) {
            assertEquals("/", RedirectUtil.afterLogin(u, path, ""));
        }
    }

    @Test void moderatorReturnsOnlyToRoutesWithRequiredPermission() {
        User u = user("MODERATOR"); u.setPermissions(Collections.singleton("HANDLE_REPORTS"));
        assertEquals("/admin/report?id=1", RedirectUtil.afterLogin(u, "/app/admin/report?id=1", "/app"));
        assertEquals("/", RedirectUtil.afterLogin(u, "/admin/wallet", "/app"));
        assertTrue(AdminAccess.canAccess(u, "/admin/dashboard"));
        assertFalse(AdminAccess.canAccess(u, "/admin/reporting"));
    }

    @Test void adminKeepsRequestedDestinationOrDashboardFallback() {
        User u = user("ADMIN");
        assertEquals("/admin/user", RedirectUtil.afterLogin(u, "/admin/user", ""));
        assertEquals("/admin/dashboard", RedirectUtil.afterLogin(u, "//example.test", ""));
    }
}
