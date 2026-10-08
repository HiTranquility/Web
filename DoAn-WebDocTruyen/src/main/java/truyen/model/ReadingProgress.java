package truyen.model;

import java.io.Serializable;

/**
 * Vị trí đọc của một người trong một truyện (bảng reading_progress).
 *
 * Tự ghi khi đọc — người dùng không bấm gì. Khác hẳn Bookmark (truyện tự bấm
 * lưu): đang đọc dở một truyện KHÔNG có nghĩa là muốn cất nó vào tủ sách.
 */
public class ReadingProgress implements Serializable {

    private int userId;
    private int storyId;
    private int chapterId;

    /** Lấy qua JOIN chapters — để nút hiện "Đọc tiếp (Chương 12)". */
    private int chapterNo;

    /** Đoạn trên cùng màn hình lúc rời trang, đếm từ 1. 0 = đầu chương. */
    private int paraNo;

    public ReadingProgress() { }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getChapterNo() { return chapterNo; }
    public void setChapterNo(int chapterNo) { this.chapterNo = chapterNo; }

    public int getParaNo() { return paraNo; }
    public void setParaNo(int paraNo) { this.paraNo = paraNo; }

    /**
     * Ép số đoạn trình duyệt gửi lên về khoảng hợp lệ [0, tổng số đoạn].
     *
     * Số này đến từ JavaScript nên không tin được: chương bị tác giả sửa ngắn
     * đi, hoặc ai đó tự gõ request. Lưu một số vượt quá thì lần sau "đọc tiếp"
     * cuộn tới một đoạn không tồn tại — nhẹ thì đứng ở đầu chương, nặng thì
     * JavaScript tìm phần tử null.
     */
    public static int clampPara(int paraNo, int totalParas) {
        if (paraNo < 0 || totalParas <= 0) return 0;
        return Math.min(paraNo, totalParas);
    }
}
