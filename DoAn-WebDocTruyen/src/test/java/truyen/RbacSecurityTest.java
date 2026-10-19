package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.ChapterAccess;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hệ thống Phân quyền Động RBAC & Bảo mật ChapterAccess")
public class RbacSecurityTest {

    @Test
    @DisplayName("Ca 1: Người dùng thường không có quyền quản trị hay kiểm duyệt")
    void testNormalUserPermissions() {
        User u = new User();
        u.setId(10);
        u.setRole("USER");
        u.setRoles(Collections.singletonList("USER"));

        assertFalse(u.isAdmin());
        assertFalse(u.isSuperAdmin());
        assertFalse(u.isModerator());
        assertFalse(u.isEditor());
        assertFalse(u.hasPermission("MANAGE_WALLET"));
        assertFalse(u.hasPermission("MODERATE_COMMENTS"));
        assertFalse(u.hasPermission("MANAGE_STORIES"));
    }

    @Test
    @DisplayName("Ca 2: Moderator có quyền MODERATE_COMMENTS, HANDLE_REPORTS nhưng KHÔNG có MANAGE_WALLET")
    void testModeratorPermissions() {
        User u = new User();
        u.setId(20);
        u.setRole("MODERATOR");
        u.setRoles(Collections.singletonList("MODERATOR"));
        u.setPermissions(new HashSet<>(Arrays.asList("MODERATE_COMMENTS", "HANDLE_REPORTS", "MANAGE_STORIES")));

        assertTrue(u.isModerator());
        assertFalse(u.isAdmin());
        assertTrue(u.hasPermission("MODERATE_COMMENTS"));
        assertTrue(u.hasPermission("HANDLE_REPORTS"));
        assertTrue(u.hasPermission("MANAGE_STORIES"));
        assertFalse(u.hasPermission("MANAGE_WALLET"));
        assertFalse(u.hasPermission("MANAGE_USERS"));
    }

    @Test
    @DisplayName("Ca 3: Editor có quyền MANAGE_STORIES nhưng KHÔNG có MANAGE_WALLET hay MANAGE_USERS")
    void testEditorPermissions() {
        User u = new User();
        u.setId(30);
        u.setRole("EDITOR");
        u.setRoles(Collections.singletonList("EDITOR"));
        u.setPermissions(new HashSet<>(Arrays.asList("MANAGE_STORIES", "MODERATE_COMMENTS")));

        assertTrue(u.isEditor());
        assertFalse(u.isAdmin());
        assertTrue(u.hasPermission("MANAGE_STORIES"));
        assertFalse(u.hasPermission("MANAGE_WALLET"));
        assertFalse(u.hasPermission("MANAGE_USERS"));
    }

    @Test
    @DisplayName("Ca 4: Admin và Super Admin tự động có tất cả quyền")
    void testAdminAllPermissions() {
        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        assertTrue(admin.isAdmin());
        assertTrue(admin.isModerator());
        assertTrue(admin.isEditor());
        assertTrue(admin.hasPermission("MANAGE_WALLET"));
        assertTrue(admin.hasPermission("MANAGE_USERS"));
        assertTrue(admin.hasPermission("MANAGE_SYSTEM"));
        assertTrue(admin.hasPermission("MODERATE_COMMENTS"));

        User superAdmin = new User();
        superAdmin.setId(2);
        superAdmin.setRole("SUPER_ADMIN");

        assertTrue(superAdmin.isAdmin());
        assertTrue(superAdmin.isSuperAdmin());
        assertTrue(superAdmin.hasPermission("ANY_UNKNOWN_PERMISSION"));
    }

    @Test
    @DisplayName("Ca 5: Đặc quyền READ_VIP_FREE cho phép mở khoá chương VIP miễn phí trong ChapterAccess")
    void testReadVipFreePermission() throws SQLException {
        Story story = new Story();
        story.setId(100);
        story.setAuthorId(999); // Người khác là tác giả
        story.setStatus("PUBLISHED");

        Chapter vipChapter = new Chapter();
        vipChapter.setId(500);
        vipChapter.setStoryId(100);
        vipChapter.setVip(true);
        vipChapter.setCoinPrice(10);

        User normalUser = new User();
        normalUser.setId(50);
        normalUser.setRole("USER");

        // Độc giả thường chưa mua -> bị khoá
        boolean lockedForNormal = ChapterAccess.isLocked(normalUser, story, vipChapter, (uid, cid) -> false);
        assertTrue(lockedForNormal, "Chương VIP phải bị khoá với độc giả thường chưa mở khoá");

        // Độc giả có quyền READ_VIP_FREE -> không bị khoá
        User vipMember = new User();
        vipMember.setId(60);
        vipMember.setRole("USER");
        vipMember.setRoles(Collections.singletonList("VIP_MEMBER"));
        vipMember.setPermissions(new HashSet<>(Collections.singletonList("READ_VIP_FREE")));

        boolean lockedForVip = ChapterAccess.isLocked(vipMember, story, vipChapter, (uid, cid) -> false);
        assertFalse(lockedForVip, "Chương VIP không được khoá đối với người có quyền READ_VIP_FREE");
    }

    @Test
    @DisplayName("Ca 6: canEdit cho phép tác giả, admin hoặc người có MANAGE_STORIES sửa truyện")
    void testCanEditStory() {
        Story story = new Story();
        story.setId(100);
        story.setAuthorId(5);

        User author = new User();
        author.setId(5);
        assertTrue(ChapterAccess.canEdit(author, story));

        User stranger = new User();
        stranger.setId(10);
        assertFalse(ChapterAccess.canEdit(stranger, story));

        User editor = new User();
        editor.setId(20);
        editor.setPermissions(new HashSet<>(Collections.singletonList("MANAGE_STORIES")));
        assertTrue(ChapterAccess.canEdit(editor, story));
    }
}
