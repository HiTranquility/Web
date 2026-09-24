package truyen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import truyen.util.GoogleConfig;
import truyen.util.GoogleTokenVerifier;
import truyen.util.GoogleTokenVerifier.GoogleUser;

/**
 * Kiểm thử xác thực token Google OIDC (Phase 2 ISSUE-001 & bug-001).
 *
 * Kiểm tra đầy đủ 4 ca cốt lõi trong tài liệu đặc tả:
 * 1. Token rỗng/null/trắng -> trả về null
 * 2. Token bịa đặt / sai định dạng -> trả về null
 * 3. Token đã hết hạn (exp trong quá khứ) -> trả về null
 * 4. Token của dự án khác (aud không khớp client ID) -> trả về null
 */
@DisplayName("GoogleTokenVerifier — Xác thực idToken Google/Firebase")
class GoogleTokenVerifierTest {

    private GoogleTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        // Cấu hình mock cho kiểm thử
        Properties testProps = new Properties();
        testProps.setProperty("google.auth.enabled", "true");
        testProps.setProperty("google.client_id", "webdoctruyen-app.apps.googleusercontent.com");
        testProps.setProperty("firebase.project_id", "webdoctruyen-app");
        GoogleConfig.setOverrideConfig(testProps);

        // Khởi tạo verifier với chế độ kiểm tra claim cục bộ
        verifier = new GoogleTokenVerifier(true);
    }

    @AfterEach
    void tearDown() {
        GoogleConfig.reset();
    }

    private String createMockJwt(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("mock_crypto_signature_bytes".getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + "." + signature;
    }

    @Test
    @DisplayName("Ca 1: Token rỗng, null hoặc khoảng trắng -> trả về null")
    void testEmptyToken() {
        assertNull(verifier.verify(null), "Token null phải trả về null");
        assertNull(verifier.verify(""), "Token rỗng phải trả về null");
        assertNull(verifier.verify("   "), "Token khoảng trắng phải trả về null");
        assertNull(verifier.verify("\t\n"), "Token xuống dòng phải trả về null");
    }

    @Test
    @DisplayName("Ca 2: Token bịa đặt, chuỗi vô nghĩa hoặc sai định dạng JWT -> trả về null")
    void testFakeToken() {
        // Không có 3 phần
        assertNull(verifier.verify("day-la-chuoi-idToken-bia-dat"), "Chuỗi thường không phải JWT phải trả về null");
        assertNull(verifier.verify("part1.part2"), "Chỉ có 2 phần phải trả về null");
        assertNull(verifier.verify("part1.part2.part3.part4"), "Có 4 phần phải trả về null");

        // Payload không phải Base64
        assertNull(verifier.verify("header.@@@invalid_base64@@@.signature"));

        // Payload không phải JSON hợp lệ
        String malformedJwt = createMockJwt("day-khong-phai-json");
        assertNull(verifier.verify(malformedJwt));

        // Thiếu sub hoặc email
        String missingSub = createMockJwt("{\"email\":\"user@gmail.com\",\"aud\":\"webdoctruyen-app.apps.googleusercontent.com\"}");
        assertNull(verifier.verify(missingSub));

        String missingEmail = createMockJwt("{\"sub\":\"123456\",\"aud\":\"webdoctruyen-app.apps.googleusercontent.com\"}");
        assertNull(verifier.verify(missingEmail));
    }

    @Test
    @DisplayName("Ca 3: Token hết hạn (exp trong quá khứ) -> trả về null")
    void testExpiredToken() {
        long pastTimeSeconds = (System.currentTimeMillis() / 1000L) - 3600; // 1 giờ trước
        String expiredPayload = "{"
                + "\"sub\":\"google_user_sub_12345\","
                + "\"email\":\"test@gmail.com\","
                + "\"iss\":\"https://accounts.google.com\","
                + "\"aud\":\"webdoctruyen-app.apps.googleusercontent.com\","
                + "\"exp\":" + pastTimeSeconds + ","
                + "\"email_verified\":true"
                + "}";

        String expiredToken = createMockJwt(expiredPayload);
        assertNull(verifier.verify(expiredToken), "Token có exp trong quá khứ bắt buộc phải bị từ chối");
    }

    @Test
    @DisplayName("Ca 4: Token thuộc dự án khác (aud không khớp Client ID) -> trả về null")
    void testOtherProjectToken() {
        long futureTimeSeconds = (System.currentTimeMillis() / 1000L) + 3600; // Còn hạn 1 giờ
        String otherProjectPayload = "{"
                + "\"sub\":\"google_user_sub_99999\","
                + "\"email\":\"attacker@gmail.com\","
                + "\"iss\":\"https://accounts.google.com\","
                + "\"aud\":\"ke-tan-cong-malicious-app.apps.googleusercontent.com\","
                + "\"exp\":" + futureTimeSeconds + ","
                + "\"email_verified\":true"
                + "}";

        String otherToken = createMockJwt(otherProjectPayload);
        assertNull(verifier.verify(otherToken), "Token của Client ID dự án khác bắt buộc phải bị từ chối (aud mismatch)");
    }

    @Test
    @DisplayName("Ca 5: Token hợp lệ đầy đủ thông tin -> trích xuất chính xác GoogleUser")
    void testValidTokenExtractsUser() {
        long futureTimeSeconds = (System.currentTimeMillis() / 1000L) + 3600;
        String validPayload = "{"
                + "\"sub\":\"google_sub_88888888\","
                + "\"email\":\"mocmien.reader@gmail.com\","
                + "\"name\":\"Mộc Miên\","
                + "\"picture\":\"https://lh3.googleusercontent.com/a/avatar.jpg\","
                + "\"iss\":\"https://accounts.google.com\","
                + "\"aud\":\"webdoctruyen-app.apps.googleusercontent.com\","
                + "\"exp\":" + futureTimeSeconds + ","
                + "\"email_verified\":true"
                + "}";

        String validToken = createMockJwt(validPayload);
        GoogleUser user = verifier.verify(validToken);

        assertNotNull(user, "Token hợp lệ phải trả về đối tượng GoogleUser");
        assertEquals("google_sub_88888888", user.getSub());
        assertEquals("mocmien.reader@gmail.com", user.getEmail());
        assertEquals("Mộc Miên", user.getName());
        assertEquals("https://lh3.googleusercontent.com/a/avatar.jpg", user.getPicture());
    }

    @Test
    @DisplayName("Ca 6: Token có email chưa xác thực (email_verified: false) -> trả về null")
    void testUnverifiedEmailToken() {
        long futureTimeSeconds = (System.currentTimeMillis() / 1000L) + 3600;
        String unverifiedPayload = "{"
                + "\"sub\":\"google_sub_unverified\","
                + "\"email\":\"fake@gmail.com\","
                + "\"iss\":\"https://accounts.google.com\","
                + "\"aud\":\"webdoctruyen-app.apps.googleusercontent.com\","
                + "\"exp\":" + futureTimeSeconds + ","
                + "\"email_verified\":false"
                + "}";

        String unverifiedToken = createMockJwt(unverifiedPayload);
        assertNull(verifier.verify(unverifiedToken), "Token có email_verified false phải bị từ chối");
    }
}
