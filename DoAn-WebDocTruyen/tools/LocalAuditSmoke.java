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
import truyen.dao.ChapterDAO;
import truyen.dao.ChapterPageDAO;
import truyen.dao.StoryDAO;
import truyen.dao.UnlockDAO;
import truyen.dao.WalletDAO;
import truyen.dao.AchievementDAO;
import truyen.controller.common.ChapterServlet;
import truyen.controller.user.UserServlet;
import truyen.model.Achievement;
import truyen.util.AchievementPolicy;
import truyen.model.Chapter;
import truyen.model.ChapterPage;
import truyen.model.Story;
import java.util.ArrayList;
import java.util.List;
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
        return post(path, params, false);
    }
    private static HttpResponse<String> post(String path, Map<String, String> params, boolean ajax) throws Exception {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (body.length() > 0) body.append('&');
            body.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", ajax ? "application/json" : "text/html")
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

    private static void inject(Object target, String field, Object value) throws Exception {
        Field member = target.getClass().getDeclaredField(field);
        member.setAccessible(true); member.set(target, value);
    }

    private static void comicUnlockSmoke(Context context) throws Exception {
        Wrapper wrapper = (Wrapper) context.findChild(ChapterServlet.class.getName());
        ChapterServlet chapterServlet = (ChapterServlet) wrapper.allocate();
        final boolean[] paid = { false };
        final int[] balance = {100};
        final int[] quotedPrice = {0};
        inject(chapterServlet, "chapterDAO", new ChapterDAO() {
            @Override public Chapter findById(int id) {
                Chapter c = new Chapter(); c.setId(9901); c.setStoryId(990);
                c.setChapterNo(1); c.setTitle("Local comic fixture"); c.setVip(true); c.setCoinPrice(10); return c;
            }
            @Override public Chapter findNeighbour(int storyId, int chapterNo, int direction) { return null; }
        });
        inject(chapterServlet, "storyDAO", new StoryDAO() {
            @Override public Story findById(int id) {
                Story s = new Story(); s.setId(990); s.setTitle("Local Comic"); s.setAuthorId(3);
                s.setStatus("PUBLISHED"); s.setStoryType("COMIC"); return s;
            }
        });
        inject(chapterServlet, "pageDAO", new ChapterPageDAO() {
            @Override public List<ChapterPage> findByChapter(int id) {
                List<ChapterPage> pages = new ArrayList<>();
                for (int i = 1; i <= 3; i++) {
                    ChapterPage p = new ChapterPage(); p.setId(i); p.setChapterId(id); p.setPageNo(i);
                    p.setImagePath("pages/990/9901/audit-page-" + i + ".png"); pages.add(p);
                }
                return pages;
            }
        });
        inject(chapterServlet, "unlockDAO", new UnlockDAO() {
            @Override public boolean hasUnlocked(int user, int chapter) { return paid[0]; }
        });
        inject(chapterServlet, "walletDAO", new WalletDAO() {
            @Override public boolean canPersistUnlocks() { return true; }
            @Override public int getBalance(int userId) { return balance[0]; }
            @Override public boolean unlockChapter(int reader, int chapter, int author, int story, int price) {
                check(price == quotedPrice[0] && price > 1, "Unlock price matches quote and ignores forged client price");
                if (!paid[0]) { balance[0] -= price; paid[0] = true; }
                return true;
            }
        });
        wrapper.deallocate(chapterServlet);
        HttpResponse<String> locked = get("/chapter?id=9901");
        Matcher quote = Pattern.compile("data-confirm-ok=\"[^\"]*?([0-9]+) xu\"").matcher(locked.body());
        if (!quote.find()) throw new AssertionError("Unlock quote missing");
        quotedPrice[0] = Integer.parseInt(quote.group(1));
        check(locked.statusCode() == 200 && locked.body().contains("vipUnlockForm-9901"), "Locked comic renders actionable POST form");
        check(locked.body().contains("data-confirm-title=\"Xác nhận mở khóa chương\"")
                && !locked.body().contains("onclick=\"openVipUnlockModal"), "Comic unlock uses working shared confirmation");
        check(!locked.body().contains("audit-page-1.png"), "Locked comic does not expose image URLs");
        check(get("/chapter?action=unlock&id=9901").statusCode() == 405, "GET cannot buy a comic chapter");
        Map<String, String> purchase = Map.of("action", "unlock", "id", "9901", "price", "1", "_csrf", token(locked.body()));
        check(post("/chapter", purchase).statusCode() == 302, "Comic unlock POST redirects to chapter");
        HttpResponse<String> open = get("/chapter?id=9901");
        check(open.statusCode() == 200 && open.body().contains("audit-page-1.png"), "Purchased comic renders pages");
        check(!open.body().contains("vipUnlockForm-9901"), "Purchased comic no longer renders lock");
        check(post("/chapter", purchase).statusCode() == 302 && balance[0] == 100 - quotedPrice[0], "Repeated unlock does not charge twice");
    }

    private static void achievementClaimSmoke(Context context) throws Exception {
        Wrapper wrapper = (Wrapper) context.findChild(UserServlet.class.getName());
        UserServlet userServlet = (UserServlet) wrapper.allocate();
        final java.util.Set<String> claimed = new java.util.HashSet<>();
        final int[] balance = {100};
        inject(userServlet, "achievementDAO", new AchievementDAO() {
            @Override public boolean canPersistRewards() { return true; }
            @Override public List<Achievement> getAchievements(int user, User current) {
                List<Achievement> badges = AchievementPolicy.catalog(Map.of("read_count", 30, "max_streak", 7, "published_count", 1), false);
                for (Achievement badge : badges) badge.setClaimed(claimed.contains(badge.getId()));
                return badges;
            }
            @Override public boolean claim(int user, String key) {
                for (Achievement badge : getAchievements(user, null)) if (badge.getId().equals(key) && badge.isClaimable()) {
                    claimed.add(key); balance[0] += badge.getRewardCoins(); return true;
                }
                return false;
            }
        });
        inject(userServlet, "walletDAO", new WalletDAO() {
            @Override public int getBalance(int user) { return balance[0]; }
        });
        inject(userServlet, "gamificationDAO", new truyen.dao.GamificationDAO() {
            @Override public truyen.model.RewardSummary getRewardSummary(int user) {
                return new truyen.model.RewardSummary(java.time.LocalDate.now(), balance[0] - 100, balance[0] - 100);
            }
        });
        wrapper.deallocate(userServlet);
        HttpResponse<String> page = get("/user?action=achievements");
        check(page.body().contains("Nhận 15 xu"), "Earned milestone renders coin claim button");
        check(page.body().contains("+11%"), "Earned badges render capped cumulative daily bonus");
        Map<String, String> claim = Map.of("action", "claim-achievement", "key", "bookworm", "amount", "999999", "_csrf", token(page.body()));
        check(post("/user", claim).statusCode() == 302 && balance[0] == 115, "Claim awards catalog amount, ignores client amount");
        check(post("/user", claim).statusCode() == 302 && balance[0] == 115, "Milestone cannot be claimed twice through HTTP");
        HttpResponse<String> duplicate = post("/user", claim, true);
        check(duplicate.statusCode() == 200 && duplicate.body().contains("\"claimed\":true") && balance[0] == 115, "AJAX duplicate reports paid without crediting twice or redirecting");
        Map<String, String> ajaxClaim = Map.of("action", "claim-achievement", "key", "first_story", "_csrf", token(page.body()));
        HttpResponse<String> awarded = post("/user", ajaxClaim, true);
        check(awarded.statusCode() == 200 && awarded.body().contains("\"balance\":135") && awarded.body().contains("\"success\":true") && awarded.body().contains("\"todayEarned\":35") && awarded.body().contains("\"totalEarned\":35"), "AJAX claim returns authoritative wallet and accumulated rewards");
        check(post("/user", Map.of("action", "claim-achievement", "key", "scholar", "_csrf", token(page.body())), true).statusCode() == 409, "AJAX locked milestone rejects claim");
        check(post("/user", Map.of("action", "claim-achievement", "key", "unknown", "_csrf", token(page.body())), true).statusCode() == 400, "AJAX unknown milestone rejects claim");
        check(post("/user", Map.of("action", "claim-achievement", "key", "first_read"), true).statusCode() == 403, "AJAX claim still requires CSRF");
        check(page.body().contains("data-search") && page.body().contains("data-page-size") && page.body().contains("data-view=\"list\""), "Achievement browser controls render through JSP");
        HttpResponse<String> after = get("/user?action=achievements");
        check(after.body().contains("✓ Đã nhận") && after.body().contains("+11%"), "Claimed milestone retains badge benefit and shows paid state");
        check(after.body().contains("data-claimed-coins") && after.body().contains("data-claimed-count"), "Achievements page renders cumulative claimed coin stats");
        HttpResponse<String> profile = get("/user?action=me");
        Matcher quote = Pattern.compile("Điểm Danh Ngay \\(\\+([0-9]+) xu\\)").matcher(profile.body());
        Matcher dayOne = Pattern.compile("class=\"streak-day-coins\"[^>]*>\\+([0-9]+) xu").matcher(profile.body());
        check(quote.find() && dayOne.find() && quote.group(1).equals(dayOne.group(1)), "Check-in button and first-day card show the same calculated reward");
        check(profile.body().contains("data-today-earned>35<") && profile.body().contains("data-total-earned>35<"), "Profile shows daily and lifetime reward totals separately from wallet");
        check(profile.body().contains("checkin-breakdown-hint") && profile.body().contains("streak-day-bonus"), "Profile renders checkin bonus breakdown and percentage");
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
            for (String path : new String[] { "/history", "/bookmark", "/follow", "/notification", "/appeal", "/user?action=me",
                    "/user?action=edit", "/user?action=achievements", "/story?action=mine", "/story?action=create", "/story?action=edit&id=1",
                    "/chapter?id=101", "/wallet", "/api/stories", "/api/chapter/101" }) routes.put(path, 200);
            routes.put("/admin/dashboard", 403);
            routes.put("/story?action=detail&id=999999", 404);
            routes.put("/story?action=detail&id=-1", 302);
            for (Map.Entry<String, Integer> route : routes.entrySet()) {
                check(get(route.getKey()).statusCode() == route.getValue(), "User route " + route.getKey());
            }
            HttpResponse<String> achievements = get("/user?action=achievements");
            check(achievements.body().contains("id=\"perks-heading\"") && achievements.body().contains("id=\"milestones-heading\""),
                    "Achievements page renders separate perks and coin milestones");
            check(get("/user?action=claim-achievement&key=bookworm").statusCode() == 405, "GET cannot claim achievement coins");
            check(post("/user", Map.of("action", "claim-achievement", "key", "bookworm", "amount", "999999", "_csrf", token(achievements.body())))
                    .statusCode() == 302, "Unavailable achievement claim returns safely");
            achievementClaimSmoke(context);
            comicUnlockSmoke(context);
            logout();
            login("ADMIN", "/audit/admin/dashboard");
            for (String path : new String[] { "/admin/dashboard", "/admin/user", "/admin/story", "/admin/tag",
                    "/admin/comment", "/admin/report", "/admin/wallet", "/admin/audit" }) {
                check(get(path).statusCode() == 200, "Admin route " + path);
            }
            logout();
            System.out.println("HTTP_SMOKE_OK checks=" + checks);
            if (Boolean.getBoolean("audit.preview")) {
                System.out.println("AUDIT_PREVIEW_URL " + base + "/auth?action=login");
                tomcat.getServer().await();
            }
        } finally {
            tomcat.stop(); tomcat.destroy();
        }
    }
}
