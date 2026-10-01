import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Set high DPI and font
plt.rcParams['font.sans-serif'] = ['DejaVu Sans', 'Arial', 'Segoe UI']
plt.rcParams['axes.edgecolor'] = '#94a3b8'

output_dir = r"C:\Users\Admin\.gemini\antigravity-ide\brain\14870ee8-7f36-46e3-9412-1a13f25a116a"

def draw_sequence_diagram(title, participants, messages, filename, height=10):
    fig, ax = plt.subplots(figsize=(11, height), dpi=200)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Title box
    fig.patch.set_facecolor('#ffffff')
    ax.set_facecolor('#ffffff')
    
    plt.text(50, 97, title, ha='center', va='center', fontsize=14, fontweight='bold', color='#1e3a8a')

    # Calculate participant x positions
    n_parts = len(participants)
    spacing = 84 / (n_parts - 1) if n_parts > 1 else 50
    x_pos = {}
    for i, p in enumerate(participants):
        x = 8 + i * spacing
        x_pos[p] = x

    top_y = 90
    bottom_y = 5

    # Draw participants (top & bottom boxes)
    for p, x in x_pos.items():
        # Lifeline
        ax.plot([x, x], [bottom_y + 3, top_y - 3], color='#cbd5e1', linestyle='--', linewidth=1.2, zorder=1)

        # Top box
        box = patches.FancyBboxPatch((x - 6.5, top_y - 2.5), 13, 5,
                                     boxstyle="round,pad=0.3,rounding_size=1",
                                     fc='#e0f2fe', ec='#0284c7', lw=1.5, zorder=3)
        ax.add_patch(box)
        ax.text(x, top_y, p, ha='center', va='center', fontsize=9.5, fontweight='bold', color='#0369a1', zorder=4)

        # Bottom box
        box_b = patches.FancyBboxPatch((x - 6.5, bottom_y - 2.5), 13, 5,
                                       boxstyle="round,pad=0.3,rounding_size=1",
                                       fc='#f1f5f9', ec='#94a3b8', lw=1.2, zorder=3)
        ax.add_patch(box_b)
        ax.text(x, bottom_y, p, ha='center', va='center', fontsize=9, fontweight='bold', color='#475569', zorder=4)

    # Draw messages
    n_msgs = len(messages)
    step_y = (top_y - bottom_y - 8) / (n_msgs + 1)

    for i, msg in enumerate(messages):
        current_y = top_y - 5 - (i + 1) * step_y
        m_from = msg.get('from')
        m_to = msg.get('to')
        text = msg.get('text', '')
        m_type = msg.get('type', 'sync') # 'sync', 'return', 'self', 'note'

        if m_type == 'note':
            # Note across some participants
            note_x = (x_pos[m_from] + x_pos[m_to]) / 2
            note_w = abs(x_pos[m_to] - x_pos[m_from]) + 8
            note_box = patches.FancyBboxPatch((note_x - note_w/2, current_y - 1.8), note_w, 3.6,
                                              boxstyle="round,pad=0.2,rounding_size=0.8",
                                              fc='#fef3c7', ec='#f59e0b', lw=1.2, zorder=3)
            ax.add_patch(note_box)
            ax.text(note_x, current_y, text, ha='center', va='center', fontsize=8.5, fontweight='bold', color='#92400e', zorder=4)
        elif m_type == 'self':
            x = x_pos[m_from]
            # Small loop arrow
            arc = patches.FancyArrowPatch((x, current_y + 1), (x, current_y - 1),
                                          connectionstyle="arc3,rad=-1.2",
                                          arrowstyle='->,head_width=3,head_length=5',
                                          color='#d97706', lw=1.3, zorder=3)
            ax.add_patch(arc)
            ax.text(x + 1.5, current_y, text, ha='left', va='center', fontsize=8.2, color='#b45309', fontweight='bold', zorder=4)
        else:
            x1 = x_pos[m_from]
            x2 = x_pos[m_to]
            is_return = (m_type == 'return')
            color = '#64748b' if is_return else '#0f172a'
            style = 'dashed' if is_return else 'solid'

            arrow = patches.FancyArrowPatch((x1, current_y), (x2, current_y),
                                           arrowstyle='->,head_width=3,head_length=5',
                                           linestyle=style, color=color, lw=1.3, zorder=2)
            ax.add_patch(arrow)

            mid_x = (x1 + x2) / 2
            ax.text(mid_x, current_y + 1.2, text, ha='center', va='bottom',
                    fontsize=8.5, color=color, fontweight='bold' if not is_return else 'normal',
                    bbox=dict(boxstyle='square,pad=0.1', facecolor='#ffffff', edgecolor='none', alpha=0.9),
                    zorder=3)

    plt.tight_layout()
    out_file = os.path.join(output_dir, filename)
    plt.savefig(out_file, bbox_inches='tight', facecolor='#ffffff')
    plt.close()
    print("SUCCESS: Generated " + filename)

def main():
    # 1. VIP Paywall Diagram
    vip_parts = ["User", "ChapterServlet", "UnlockDAO", "WalletDAO", "MySQL", "View"]
    vip_msgs = [
        {"from": "User", "to": "ChapterServlet", "text": "1: GET /chapter?action=read&id=61", "type": "sync"},
        {"from": "ChapterServlet", "to": "UnlockDAO", "text": "2: hasUnlocked(userId, 61)", "type": "sync"},
        {"from": "UnlockDAO", "to": "MySQL", "text": "3: SELECT COUNT(*) FROM chapter_unlocks", "type": "sync"},
        {"from": "MySQL", "to": "UnlockDAO", "text": "4: 0 (Chưa mở khóa)", "type": "return"},
        {"from": "UnlockDAO", "to": "ChapterServlet", "text": "5: return false", "type": "return"},
        {"from": "ChapterServlet", "to": "ChapterServlet", "text": "6: chapter.setContent(\"\") [Bảo mật]", "type": "self"},
        {"from": "ChapterServlet", "to": "View", "text": "7: Forward read.jsp (isLocked=true)", "type": "sync"},
        {"from": "View", "to": "User", "text": "8: Render Paywall (_locked.jsp): Báo giá 20 xu & Nút Mở khóa", "type": "return"},
        {"from": "User", "to": "ChapterServlet", "text": "9: POST /chapter?action=unlock&id=61", "type": "sync"},
        {"from": "ChapterServlet", "to": "WalletDAO", "text": "10: unlockChapter(userId, authorId, 20)", "type": "sync"},
        {"from": "WalletDAO", "to": "MySQL", "text": "ACID Transaction: Atomic lock & balance transfer", "type": "note"},
        {"from": "WalletDAO", "to": "MySQL", "text": "11: SELECT balance FOR UPDATE -> Trừ xu độc giả, cộng xu tác giả", "type": "sync"},
        {"from": "WalletDAO", "to": "MySQL", "text": "12: INSERT chapter_unlocks & transactions -> Commit", "type": "sync"},
        {"from": "MySQL", "to": "WalletDAO", "text": "13: Transaction OK", "type": "return"},
        {"from": "WalletDAO", "to": "ChapterServlet", "text": "14: return true", "type": "return"},
        {"from": "ChapterServlet", "to": "User", "text": "15: Redirect /chapter?action=read&id=61", "type": "return"},
        {"from": "ChapterServlet", "to": "View", "text": "16: Render đầy đủ nội dung chương VIP", "type": "sync"},
        {"from": "View", "to": "User", "text": "17: Độc giả đọc trọn vẹn chương truyện", "type": "return"}
    ]
    draw_sequence_diagram("Sơ đồ tuần tự 1: Mở khóa chương VIP bằng Xu (ISSUE-020)",
                          vip_parts, vip_msgs, "diagram_seq_vip.png", height=11)

    # 2. Review & Spoiler Diagram
    rev_parts = ["Reader", "RatingServlet", "StoryServlet", "ReviewDAO", "MySQL", "View"]
    rev_msgs = [
        {"from": "Reader", "to": "RatingServlet", "text": "1: POST /rating?action=review (title, content, hasSpoiler=1)", "type": "sync"},
        {"from": "RatingServlet", "to": "ReviewDAO", "text": "2: upsert(review)", "type": "sync"},
        {"from": "ReviewDAO", "to": "MySQL", "text": "3: INSERT INTO reviews ... ON DUPLICATE KEY UPDATE", "type": "sync"},
        {"from": "MySQL", "to": "ReviewDAO", "text": "4: OK", "type": "return"},
        {"from": "RatingServlet", "to": "Reader", "text": "5: Redirect /story?action=detail&id=1#reviews", "type": "return"},
        {"from": "Reader", "to": "StoryServlet", "text": "6: GET /story?action=detail&id=1#reviews", "type": "sync"},
        {"from": "StoryServlet", "to": "ReviewDAO", "text": "7: findByStory(storyId, currentUserId)", "type": "sync"},
        {"from": "ReviewDAO", "to": "MySQL", "text": "8: SELECT r.*, u.username ... ORDER BY helpful_count DESC", "type": "sync"},
        {"from": "MySQL", "to": "ReviewDAO", "text": "9: List<Review>", "type": "return"},
        {"from": "StoryServlet", "to": "View", "text": "10: Forward detail.jsp (Tab Đánh giá)", "type": "sync"},
        {"from": "View", "to": "Reader", "text": "11: Review Card hiển thị (Nội dung Spoiler bị che mờ 6px)", "type": "return"},
        {"from": "Reader", "to": "View", "text": "12: Bấm nút 'Hiển thị nội dung' -> JS gỡ blur ngay tại client", "type": "sync"},
        {"from": "Reader", "to": "RatingServlet", "text": "13: POST /rating?action=vote&reviewId=...", "type": "sync"},
        {"from": "RatingServlet", "to": "ReviewDAO", "text": "14: toggleVote(userId, reviewId)", "type": "sync"},
        {"from": "ReviewDAO", "to": "MySQL", "text": "15: INSERT review_votes & UPDATE helpful_count +/- 1", "type": "sync"},
        {"from": "RatingServlet", "to": "Reader", "text": "16: Redirect & Cập nhật số đếm hữu ích tức thì", "type": "return"}
    ]
    draw_sequence_diagram("Sơ đồ tuần tự 2: Đánh giá chi tiết, Spoiler Tag & Vote hữu ích (ISSUE-022)",
                          rev_parts, rev_msgs, "diagram_seq_review.png", height=10.5)

    # 3. Gamification Diagram
    game_parts = ["User", "UserServlet", "GamificationDAO", "MySQL", "View"]
    game_msgs = [
        {"from": "User", "to": "UserServlet", "text": "1: GET /user?action=me", "type": "sync"},
        {"from": "UserServlet", "to": "GamificationDAO", "text": "2: getTodayCheckin(userId) & getDailyQuests(userId)", "type": "sync"},
        {"from": "GamificationDAO", "to": "MySQL", "text": "3: Quét daily_checkins, view_logs, comments", "type": "sync"},
        {"from": "MySQL", "to": "GamificationDAO", "text": "4: Dữ liệu chuỗi streak & tiến độ nhiệm vụ", "type": "return"},
        {"from": "UserServlet", "to": "View", "text": "5: Render me.jsp (Trạm nhận xu, chuỗi 7 ngày, nút nhận quà)", "type": "sync"},
        {"from": "View", "to": "User", "text": "6: Hiển thị giao diện Gamification Dashboard", "type": "return"},
        {"from": "User", "to": "UserServlet", "text": "7: POST /user?action=checkin (Bấm 'Điểm danh ngay')", "type": "sync"},
        {"from": "UserServlet", "to": "GamificationDAO", "text": "8: doCheckin(userId)", "type": "sync"},
        {"from": "GamificationDAO", "to": "MySQL", "text": "Transaction: Atomic Check-in & Coin Credit", "type": "note"},
        {"from": "GamificationDAO", "to": "MySQL", "text": "9: INSERT daily_checkins (streak_days, reward_coins)", "type": "sync"},
        {"from": "GamificationDAO", "to": "MySQL", "text": "10: UPDATE wallets SET balance = balance + coins", "type": "sync"},
        {"from": "GamificationDAO", "to": "MySQL", "text": "11: INSERT transactions VALUES (...) -> Commit", "type": "sync"},
        {"from": "MySQL", "to": "GamificationDAO", "text": "12: OK", "type": "return"},
        {"from": "UserServlet", "to": "User", "text": "13: Redirect /user?action=me (Cộng xu và tick xanh Ngày)", "type": "return"}
    ]
    draw_sequence_diagram("Sơ đồ tuần tự 3: Gamification điểm danh 7 ngày & Nhiệm vụ hàng ngày (ISSUE-026)",
                          game_parts, game_msgs, "diagram_seq_gamification.png", height=10)

    # 4. Ranking Diagram
    rank_parts = ["Reader", "RankServlet", "UserDAO", "MySQL", "View"]
    rank_msgs = [
        {"from": "Reader", "to": "RankServlet", "text": "1: GET /rank?type=authors (Bấm tab 'Tác giả nổi bật')", "type": "sync"},
        {"from": "RankServlet", "to": "UserDAO", "text": "2: findTopAuthors(limit = 20)", "type": "sync"},
        {"from": "UserDAO", "to": "MySQL", "text": "3: SELECT u.*, SUM(s.view_count), COUNT(f.follower_id) GROUP BY u.id", "type": "sync"},
        {"from": "MySQL", "to": "UserDAO", "text": "4: Danh sách 20 tác giả hàng đầu", "type": "return"},
        {"from": "UserDAO", "to": "RankServlet", "text": "5: List<User> topAuthors", "type": "return"},
        {"from": "RankServlet", "to": "View", "text": "6: Forward rank/index.jsp (topAuthors, currentType='authors')", "type": "sync"},
        {"from": "View", "to": "Reader", "text": "7: Hiển thị Bảng vinh danh Tác giả với huy hiệu Top 1-2-3", "type": "return"}
    ]
    draw_sequence_diagram("Sơ đồ tuần tự 4: Bảng xếp hạng Tác giả nổi bật & Vinh danh (ISSUE-006)",
                          rank_parts, rank_msgs, "diagram_seq_rank.png", height=8.5)

if __name__ == "__main__":
    main()
