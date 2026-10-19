package truyen;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.Servlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import truyen.controller.common.AuthServlet;
import truyen.dao.UserDAO;
import truyen.filter.AuthFilter;
import truyen.filter.CsrfFilter;
import truyen.filter.EncodingFilter;
import truyen.model.User;
import truyen.util.CsrfUtil;
import truyen.util.GoogleConfig;
import truyen.util.PasswordUtil;
import static org.junit.jupiter.api.Assertions.*;

class SessionFlowTest {
    private AuthServlet servlet;
    private WebRequestFixture web;
    private final String passwordHash = PasswordUtil.hash("audit-password");

    @BeforeEach void setUp() throws Exception {
        GoogleConfig.setOverrideConfig(new Properties());
        web = new WebRequestFixture();
        servlet = new AuthServlet();
        servlet.init(web.config);
        Field dao = AuthServlet.class.getDeclaredField("userDAO");
        dao.setAccessible(true);
        dao.set(servlet, new UserDAO() {
            @Override public User findByUsername(String username) {
                User u = new User(); u.setId(123); u.setUsername(username);
                u.setRole("USER"); u.setStatus("ACTIVE"); u.setPasswordHash(passwordHash);
                return u;
            }
            @Override public int countReadChapters(int id) throws SQLException {
                throw new SQLException("Stats unavailable");
            }
            @Override public boolean exists(String username, String email) { return false; }
            @Override public void insert(User user) { user.setId(124); }
        });
    }
    @AfterEach void reset() { GoogleConfig.reset(); }

    private void dispatch() throws Exception {
        new CsrfFilter().doFilter(web.request, web.response, (req, res) -> ((Servlet) servlet).service(req, res));
    }

    private void preparePost(String action) {
        web.method = "POST"; web.params.put("action", action);
        web.params.put("_csrf", CsrfUtil.token(web.request.getSession(true)));
    }

    @Test void loginRotatesSessionAndAvoidsForbiddenSavedAdminTarget() throws Exception {
        preparePost("login");
        WebRequestFixture.Session old = web.session;
        old.values.put("redirectAfterLogin", "/app/admin/user");
        old.values.put("privateOldAttribute", "old");
        web.params.put("username", "audit_user"); web.params.put("password", "audit-password");
        dispatch();
        assertEquals("/app/", web.redirect);
        assertTrue(old.invalidated);
        assertNotSame(old, web.session);
        assertNull(web.session.values.get("privateOldAttribute"));
        User user = (User) web.session.values.get("currentUser");
        assertNotNull(user); assertNull(user.getPasswordHash());
    }

    @Test void loginReturnsToRequestedChapterUnderContext() throws Exception {
        preparePost("login"); web.session.values.put("redirectAfterLogin", "/app/chapter?id=12");
        web.params.put("username", "audit_user"); web.params.put("password", "audit-password");
        dispatch(); assertEquals("/app/chapter?id=12", web.redirect);
    }

    @Test void wrongPasswordDoesNotAuthenticateOrDestroySession() throws Exception {
        preparePost("login"); WebRequestFixture.Session old = web.session;
        web.params.put("username", "audit_wrong"); web.params.put("password", "incorrect");
        dispatch(); assertFalse(old.invalidated); assertNull(old.values.get("currentUser"));
        assertNotNull(web.attributes.get("message"));
    }

    @Test void validLogoutInvalidatesWholeSessionAndProtectedRequestRequiresLogin() throws Exception {
        preparePost("logout"); WebRequestFixture.Session old = web.session;
        old.values.put("currentUser", new User()); old.values.put("readerPageAccess", "private");
        dispatch(); assertTrue(old.invalidated); assertTrue(old.values.isEmpty());
        assertEquals("/app/", web.redirect);
        web.path = "/history"; web.method = "GET";
        new AuthFilter().doFilter(web.request, web.response, (req, res) -> fail("Protected request passed"));
        assertEquals("/app/auth?action=login", web.redirect);
    }

    @Test void getLogoutDoesNotInvalidateSession() throws Exception {
        web.request.getSession(true); web.params.put("action", "logout");
        WebRequestFixture.Session old = web.session;
        dispatch(); assertFalse(old.invalidated);
    }

    @Test void invalidCsrfCannotLogout() throws Exception {
        preparePost("logout"); WebRequestFixture.Session old = web.session;
        web.params.put("_csrf", "wrong"); dispatch();
        assertEquals(403, web.status); assertFalse(old.invalidated);
        assertEquals(true, web.attributes.get("csrfFailure"));
    }

    @Test void registrationAlsoRotatesSession() throws Exception {
        preparePost("register"); WebRequestFixture.Session old = web.session;
        web.params.put("username", "audit_new"); web.params.put("email", "audit@example.test");
        web.params.put("password", "audit-password"); web.params.put("confirm", "audit-password");
        web.params.put("agree", "on"); dispatch();
        assertTrue(old.invalidated); assertNotNull(web.session.values.get("currentUser"));
    }

    @Test void expiredAjaxReceivesJsonInsteadOfLoginHtml() throws Exception {
        web.path = "/bookmark"; web.headers.put("Accept", "application/json");
        new AuthFilter().doFilter(web.request, web.response, (req, res) -> fail("Protected request passed"));
        assertEquals(401, web.status); assertTrue(web.output.toString().contains("\"needLogin\":true"));
        assertNull(web.redirect);
    }

    @Test void csrfCannotBeBypassedWithStaticFileSuffixOnPost() throws Exception {
        web.method = "POST"; web.path = "/auth/fake.png";
        new CsrfFilter().doFilter(web.request, web.response, (req, res) -> fail("CSRF bypassed"));
        assertEquals(403, web.status);
    }

    @Test void dynamicResponsesDisableCaching() throws Exception {
        new EncodingFilter().doFilter(web.request, web.response, (req, res) -> {});
        assertEquals("no-store, private", web.responseHeaders.get("Cache-Control"));
    }
}
