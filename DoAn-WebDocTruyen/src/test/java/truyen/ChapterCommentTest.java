package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.model.Comment;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChapterCommentTest — Kiểm thử bình luận theo chương (ISSUE-004)")
class ChapterCommentTest {

    @Test
    @DisplayName("Ca 1: isChapterComment() trả về true khi có chapterId hợp lệ")
    void testIsChapterComment() {
        Comment c = new Comment();
        c.setStoryId(10);
        c.setChapterId(105);
        c.setChapterNo(5);

        assertTrue(c.isChapterComment());
        assertEquals(105, c.getChapterId());
        assertEquals(5, c.getChapterNo());
    }

    @Test
    @DisplayName("Ca 2: isChapterComment() trả về false khi chapterId là null hoặc <= 0 (bình luận cấp truyện)")
    void testIsStoryLevelComment() {
        Comment c1 = new Comment();
        c1.setStoryId(10);
        c1.setChapterId(null);
        assertFalse(c1.isChapterComment());

        Comment c2 = new Comment();
        c2.setStoryId(10);
        c2.setChapterId(0);
        assertFalse(c2.isChapterComment());
    }

    @Test
    @DisplayName("Ca 3: Trả lời bình luận (Reply) liên kết quan hệ cha-con đúng đắn")
    void testCommentReplyHierarchy() {
        Comment parent = new Comment();
        parent.setId(1);
        parent.setStoryId(10);
        parent.setChapterId(50);
        parent.setContent("Bình luận gốc chương 50");

        Comment reply = new Comment();
        reply.setId(2);
        reply.setStoryId(10);
        reply.setChapterId(50);
        reply.setParentId(1);
        reply.setContent("Đồng ý với bạn!");

        parent.getReplies().add(reply);

        assertTrue(reply.isReply());
        assertEquals(1, reply.getParentId());
        assertEquals(1, parent.getReplies().size());
        assertEquals(1, parent.getReplyCount());
    }
}
