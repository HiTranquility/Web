import os
from playwright.sync_api import sync_playwright

OUT_DIR = os.path.join(os.getcwd(), 'docs', 'report_images')
os.makedirs(OUT_DIR, exist_ok=True)

html_content = """<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
  body { background: #0f172a; color: #f8fafc; padding: 24px; width: 1400px; }
  .header { text-align: center; margin-bottom: 24px; }
  .header h1 { font-size: 26px; color: #38bdf8; letter-spacing: -0.5px; }
  .header p { color: #94a3b8; font-size: 13px; margin-top: 4px; }
  .grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
  .module-card { background: #1e293b; border-radius: 10px; border: 1px solid #334155; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3); }
  .module-title { padding: 10px 14px; font-weight: 700; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; display: flex; align-items: center; justify-content: space-between; }
  .t-blue { background: #0369a1; color: #e0f2fe; }
  .t-purple { background: #6d28d9; color: #ede9fe; }
  .t-emerald { background: #047857; color: #d1fae5; }
  .t-amber { background: #b45309; color: #fef3c7; }
  .t-rose { background: #be123c; color: #ffe4e6; }
  .t-indigo { background: #4338ca; color: #e0e7ff; }
  .t-cyan { background: #0e7490; color: #cffafe; }
  
  .table-item { border-bottom: 1px solid #334155; padding: 8px 12px; }
  .table-item:last-child { border-bottom: none; }
  .table-name { font-weight: 600; font-size: 13px; color: #f1f5f9; display: flex; align-items: center; justify-content: space-between; }
  .table-desc { font-size: 11px; color: #94a3b8; margin: 2px 0 5px 0; }
  .col-list { font-family: "JetBrains Mono", Consolas, monospace; font-size: 10.5px; color: #cbd5e1; line-height: 1.5; background: #0f172a; padding: 6px 8px; border-radius: 6px; border: 1px solid #1e293b; }
  .pk { color: #f59e0b; font-weight: bold; }
  .fk { color: #38bdf8; }
  .badge { font-size: 10px; padding: 2px 6px; border-radius: 4px; background: rgba(255,255,255,0.15); font-weight: normal; }
</style>
</head>
<body>

<div class="header">
  <h1>SƠ ĐỒ THIẾT KẾ CƠ SỞ DỮ LIỆU TOÀN HỆ THỐNG (25 BẢNG CSDL)</h1>
  <p>Dự án: Web Đọc Truyện Đa Nền Tảng (Hi Tranquility) · Hệ quản trị CSDL: MySQL 8.0 InnoDB · Charset: utf8mb4_unicode_ci</p>
</div>

<div class="grid">
  <!-- CỘT 1: TÀI KHOẢN & PHÂN QUYỀN ĐỘNG RBAC -->
  <div class="module-card">
    <div class="module-title t-blue">
      <span>1. Tài Khoản & RBAC (6 bảng)</span>
      <span class="badge">Auth Stack</span>
    </div>
    <div class="table-item">
      <div class="table-name"><span>users</span><span class="badge">Core</span></div>
      <div class="table-desc">Tài khoản thành viên & độc giả</div>
      <div class="col-list"><span class="pk">PK</span> id | username | email | password_hash | display_name | avatar_url | bio | role | status | ban_reason | created_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>user_identities</span><span class="badge">OAuth</span></div>
      <div class="table-desc">Liên kết Google OIDC / Firebase</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> user_id | provider | provider_uid | email | display_name | created_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>roles</span><span class="badge">RBAC</span></div>
      <div class="table-desc">7 Vai trò hệ thống</div>
      <div class="col-list"><span class="pk">PK</span> id | code (UNIQUE) | name | description | created_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>permissions</span><span class="badge">RBAC</span></div>
      <div class="table-desc">8 Quyền hạn chức năng</div>
      <div class="col-list"><span class="pk">PK</span> id | code (UNIQUE) | name | description</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>role_permissions</span><span class="badge">Map</span></div>
      <div class="table-desc">Gán quyền cho từng vai trò</div>
      <div class="col-list"><span class="pk">PK/FK</span> role_id | <span class="pk/fk">PK/FK</span> permission_id</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>user_roles</span><span class="badge">Map</span></div>
      <div class="table-desc">Phân quyền động cho người dùng</div>
      <div class="col-list"><span class="pk/fk">PK/FK</span> user_id | <span class="pk/fk">PK/FK</span> role_id | assigned_at</div>
    </div>
  </div>

  <!-- CỘT 2: NỘI DUNG TÁC PHẨM & CHƯƠNG -->
  <div class="module-card">
    <div class="module-title t-purple">
      <span>2. Nội Dung & Đọc (5 bảng)</span>
      <span class="badge">Content</span>
    </div>
    <div class="table-item">
      <div class="table-name"><span>stories</span><span class="badge">Core</span></div>
      <div class="table-desc">Tác phẩm truyện chữ & tranh</div>
      <div class="col-list"><span class="pk">PK</span> id | title | slug | description | cover_url | <span class="fk">FK</span> author_id | story_type (NOVEL/COMIC) | status | progress | view_count | rating_sum | rating_count</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>chapters</span><span class="badge">FullText</span></div>
      <div class="table-desc">Nội dung chương văn bản & VIP</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> story_id | chapter_number | title | content (MEDIUMTEXT) | is_vip | coin_price | view_count</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>chapter_pages</span><span class="badge">Comic</span></div>
      <div class="table-desc">Danh sách trang ảnh Webtoon</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> chapter_id | page_number | image_url | created_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>tags & story_tags</span><span class="badge">Taxonomy</span></div>
      <div class="table-desc">Thể loại & bảng liên kết N-N</div>
      <div class="col-list">tags(<span class="pk">PK</span> id, name, slug) | story_tags(<span class="pk/fk">PK/FK</span> story_id, <span class="pk/fk">PK/FK</span> tag_id)</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>reading_progress</span><span class="badge">Resume</span></div>
      <div class="table-desc">Tiến trình cuộn và bookmark đoạn</div>
      <div class="col-list"><span class="pk/fk">PK/FK</span> user_id, story_id, chapter_id | para_no | scroll_percent | updated_at</div>
    </div>
  </div>

  <!-- CỘT 3: VÍ XU, VIP & GIAO DỊCH NGUYÊN TỬ -->
  <div class="module-card">
    <div class="module-title t-emerald">
      <span>3. Kinh Tế Ảo & VIP (4 bảng)</span>
      <span class="badge">FinTech</span>
    </div>
    <div class="table-item">
      <div class="table-name"><span>wallets</span><span class="badge">ACID</span></div>
      <div class="table-desc">Ví xu ảo độc giả & tác giả</div>
      <div class="col-list"><span class="pk">PK/FK</span> user_id | balance (INT) | total_spent | updated_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>transactions</span><span class="badge">Ledger</span></div>
      <div class="table-desc">Nhật ký biến động số dư ví</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> sender_id | <span class="fk">FK</span> receiver_id | amount | type (TOPUP/TIP/UNLOCK/QUEST) | note | created_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>chapter_unlocks</span><span class="badge">Ownership</span></div>
      <div class="table-desc">Quyền sở hữu chương VIP vĩnh viễn</div>
      <div class="col-list"><span class="pk/fk">PK/FK</span> user_id, chapter_id | <span class="fk">FK</span> story_id | coin_price | unlocked_at</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>vip_passes</span><span class="badge">Loyalty</span></div>
      <div class="table-desc">Gói thẻ hội viên VIP chiết khấu</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> user_id | tier_level | discount_percent | start_date | end_date | is_active</div>
    </div>
  </div>

  <!-- CỘT 4: GAMIFICATION, TƯƠNG TÁC & AUDIT -->
  <div class="module-card">
    <div class="module-title t-amber">
      <span>4. Gamification & Kiểm Toán (10 bảng)</span>
      <span class="badge">Interactive</span>
    </div>
    <div class="table-item">
      <div class="table-name"><span>daily_checkins</span><span class="badge">Streak</span></div>
      <div class="table-desc">Chuỗi điểm danh 7 ngày nhận xu</div>
      <div class="col-list"><span class="pk">PK</span> id | <span class="fk">FK</span> user_id | checkin_date | streak_days | reward_coins</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>daily_quest_claims</span><span class="badge">Quests</span></div>
      <div class="table-desc">Nhiệm vụ hàng ngày (Đọc/Review)</div>
      <div class="col-list"><span class="pk/fk">PK/FK</span> user_id, quest_key, claim_date | reward_coins</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>reviews & votes</span><span class="badge">Spoiler</span></div>
      <div class="table-desc">Bài cảm nhận, cờ Spoiler, bình chọn</div>
      <div class="col-list">reviews(<span class="pk">PK</span> id, title, content, has_spoiler, helpful_count) | review_votes(<span class="pk/fk">PK/FK</span> user_id, review_id)</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>comments & ratings</span><span class="badge">Social</span></div>
      <div class="table-desc">Bình luận phân cấp & Chấm 1-5 sao</div>
      <div class="col-list">comments(<span class="pk">PK</span> id, parent_id, content) | ratings(<span class="pk/fk">PK/FK</span> user_id, story_id, score)</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>reports & evidence</span><span class="badge">Safety</span></div>
      <div class="table-desc">Tố cáo vi phạm và ảnh bằng chứng</div>
      <div class="col-list">reports(<span class="pk">PK</span> id, target_type, category, reason) | report_evidence(<span class="pk">PK</span> id, report_id, image_url)</div>
    </div>
    <div class="table-item">
      <div class="table-name"><span>audit_logs & logs</span><span class="badge">Security</span></div>
      <div class="table-desc">Nhật ký kiểm toán an ninh & lượt đọc</div>
      <div class="col-list">audit_logs(<span class="pk">PK</span> id, user_id, action, ip_address, details) | view_logs(id, user_id, story_id, viewed_at)</div>
    </div>
  </div>
</div>

</body>
</html>
"""

temp_html = os.path.join(OUT_DIR, 'db_diagram.html')
with open(temp_html, 'w', encoding='utf-8') as f:
    f.write(html_content)

with sync_playwright() as p:
    browser = p.chromium.launch(channel='msedge', headless=True)
    page = browser.new_page(viewport={'width': 1400, 'height': 860}, device_scale_factor=2)
    page.goto(f"file:///{temp_html.replace(os.sep, '/')}")
    page.wait_for_timeout(800)
    page.screenshot(path=os.path.join(OUT_DIR, '15_he_thong_csdl_erd.png'))
    browser.close()

print("GENERATE ERD SUCCESS: 15_he_thong_csdl_erd.png")
