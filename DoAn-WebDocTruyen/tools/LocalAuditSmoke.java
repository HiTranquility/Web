import java.io.File;
import java.lang.reflect.Field;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import truyen.controller.common.AuthServlet;
import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.util.PasswordUtil;

/** Smoke test HTTP/JSP bằng Tomcat thật, tài khoản giả và classpath không có cấu hình. */
public final class LocalAuditSmoke {
    private static int checks;
    private static String base;
    private static HttpClient client;

    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        checks++;
        System.out.println("PASS " + label);
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpResponse<String> result = client.send(HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(30)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (result.headers().firstValue("content-type").orElse("").contains("text/html")) {
            Path folder = Path.of(System.getProperty("audit.root", "build/audit"), "rendered");
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("page-" + checks + ".html"), result.body());
        }
        return result;
    }

    private static HttpResponse<String> post(String path, Map<String, String> params) throws Exception {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) body.append('&');
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String token(String html) {
        Matcher m = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
        if (!m.find()) throw new AssertionError("Rendered page missing CSRF token");
        return m.group(1);
    }

    private static void login(String role, String target) throws Exception {
        String query = "/auth?action=login" + (target == null ? "" : "&redirect=" + URLEncoder.encode(target, StandardCharsets.UTF_8));
        HttpResponse<String> page = get(query);
        check(page.statusCode() == 200, "Login JSP renders");
        HttpResponse<String> result = post("/auth", Map.of("action", "login", "username", role,
                "password", "local-audit-password", "_csrf", token(page.body())));
        String expected = "ADMIN".equals(role) ? "/audit/admin/dashboard" : "/audit/";
        check(result.statusCode() == 302 && result.headers().firstValue("location").orElse("").endsWith(expected),
                "Login " + role + " redirects to allowed page");
    }

    private static void logout() throws Exception {
        HttpResponse<String> home = get("/");
        check(home.statusCode() == 200 && home.body().contains("id=\"navLogoutForm\""), "Authenticated navbar renders POST logout form");
        HttpResponse<String> result = post("/auth", Map.of("action", "logout", "_csrf", token(home.body())));
        check(result.statusCode() == 302, "Logout redirects");
        HttpResponse<String> loggedOut = get("/");
        check(!loggedOut.body().contains("id=\"navLogoutForm\""), "Logout removes account from rendered page");
        check(get("/history").statusCode() == 302, "Protected page rejects logged-out cookie");
    }

    public static void main(String[] args) throws Exception {
        // Dùng build/audit/classes, tuyệt đối không dùng build/classes có cấu hình thật.
        for (String config : new String[] { "db.properties", "google.properties", "mail.properties" }) {
            if (LocalAuditSmoke.class.getClassLoader().getResource(config) != null) {
                throw new IllegalStateException("Audit classpath must exclude runtime configuration");
            }
        }
        Tomcat tomcat = new Tomcat();
        String auditRoot = System.getProperty("audit.root", "build/audit");
        tomcat.setBaseDir(new File(auditRoot, "tomcat").getAbsolutePath());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        tomcat.getConnector().setProperty("maxPartCount", "300");
        Context context = tomcat.addWebapp("/audit", new File("src/main/webapp").getAbsolutePath());
        WebappLoader loader = new WebappLoader(LocalAuditSmoke.class.getClassLoader());
        loader.setDelegate(true);
        context.setLoader(loader);
        StandardRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                new File(auditRoot, "classes").getAbsolutePath(), "/"));
        context.setResources(resources);
        try {
            tomcat.start();
            Wrapper authWrapper = (Wrapper) context.findChild(AuthServlet.class.getName());
            AuthServlet auth = (AuthServlet) authWrapper.allocate();
            final String hash = PasswordUtil.hash("local-audit-password");
            Field dao = AuthServlet.class.getDeclaredField("userDAO"); dao.setAccessible(true);
            dao.set(auth, new UserDAO() {
                @Override public User findByUsername(String name) {
                    User u = new User(); u.setId("ADMIN".equals(name) ? 1 : 2);
                    u.setUsername(name); u.setDisplayName("Local Audit");
                    u.setRole("ADMIN".equals(name) ? "ADMIN" : "USER");
                    u.setStatus("ACTIVE"); u.setPasswordHash(hash); return u;
                }
            });
            authWrapper.deallocate(auth);
            base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/audit";
            client = HttpClient.newBuilder().cookieHandler(new CookieManager()).connectTimeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> home = get("/");
            check(home.statusCode() == 200, "Home JSP renders");
            check(home.headers().firstValue("cache-control").orElse("").contains("no-store"), "HTML is not cached");
            check(get("/api/chapter/1").statusCode() == 401, "Chapter API requires login");
            check(post("/auth", Map.of("action", "login")).statusCode() == 403, "Missing CSRF is blocked");
            for (String path : new String[] { "/story", "/story?action=search&q=test", "/rank", "/page?name=rules",
                    "/auth?action=register", "/auth?action=forgot", "/story?action=detail&id=1" }) {
                check(get(path).statusCode() == 200, "Public route " + path);
            }
            login("USER", "/audit/admin/user");
            Map<String, Integer> routes = new LinkedHashMap<>();
            for (String path : new String[] { "/history", "/bookmark", "/follow", "/notification", "/user?action=me",
                    "/user?action=edit", "/story?action=mine", "/story?action=create", "/story?action=edit&id=1",
                    "/chapter?id=101", "/wallet", "/api/stories", "/api/chapter/101" }) routes.put(path, 200);
            routes.put("/admin/dashboard", 403);
            routes.put("/story?action=detail&id=999999", 404);
            routes.put("/story?action=detail&id=-1", 302);
            for (Map.Entry<String, Integer> route : routes.entrySet()) {
                check(get(route.getKey()).statusCode() == route.getValue(), "User route " + route.getKey());
            }
            logout();
            login("ADMIN", "/audit/admin/dashboard");
            for (String path : new String[] { "/admin/dashboard", "/admin/user", "/admin/story", "/admin/tag",
                    "/admin/comment", "/admin/report", "/admin/wallet", "/admin/audit" }) {
                check(get(path).statusCode() == 200, "Admin route " + path);
            }
            logout();
            System.out.println("HTTP_SMOKE_OK checks=" + checks);
        } finally {
            tomcat.stop(); tomcat.destroy();
        }
    }
}
