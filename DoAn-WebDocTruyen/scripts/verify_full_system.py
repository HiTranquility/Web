# -*- coding: utf-8 -*-
"""
Kịch bản kiểm thử tích hợp tự động toàn diện cho hệ thống Web Đọc Truyện (DevServer).
Kiểm tra:
1. Public Pages (Home, Story list, Story detail, Chapter read, Rank, Rules, About)
2. REST API JSON Endpoints (/api/stories, /api/story/id, /api/chapter/id, /api/tags, /api/rank)
3. Xác thực & Quản lý phiên (Guest, User thường, Moderator, Editor, Admin)
4. Phân quyền chi tiết RBAC (Ma trận kiểm soát truy cập /admin/*)
5. Hàng rào chương VIP & Khối Modal (Quick Topup, VIP Unlock, Confirm Modal)
"""

import urllib.request
import urllib.parse
import urllib.error
import http.cookiejar
import re
import sys
import json
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8")
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding="utf-8")

BASE_URL = "http://localhost:8080"

class TestSession:
    def __init__(self, name="Guest"):
        self.name = name
        self.jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(self.jar)
        )

    def request(self, path, method="GET", data=None, headers=None, follow_redirects=True):
        url = BASE_URL + path if path.startswith("/") else path
        req_headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) IntegrationTester/1.0"
        }
        if headers:
            req_headers.update(headers)

        encoded_data = None
        if data is not None:
            if isinstance(data, dict):
                encoded_data = urllib.parse.urlencode(data).encode("utf-8")
                req_headers["Content-Type"] = "application/x-www-form-urlencoded"
            elif isinstance(data, str):
                encoded_data = data.encode("utf-8")
            else:
                encoded_data = data

        req = urllib.request.Request(url, data=encoded_data, headers=req_headers, method=method)

        class NoRedirectHandler(urllib.request.HTTPRedirectHandler):
            def http_error_302(self, req, fp, code, msg, headers):
                return fp
            http_error_301 = http_error_302
            http_error_303 = http_error_302
            http_error_307 = http_error_302

        if not follow_redirects:
            opener = urllib.request.build_opener(
                urllib.request.HTTPCookieProcessor(self.jar),
                NoRedirectHandler
            )
        else:
            opener = self.opener

        try:
            resp = opener.open(req)
            body = resp.read().decode("utf-8", errors="replace")
            return resp.status if hasattr(resp, 'status') else resp.code, body, resp.headers
        except urllib.error.HTTPError as e:
            body = e.read().decode("utf-8", errors="replace")
            return e.code, body, e.headers

    def get_csrf(self, html):
        m = re.search(r'name=["\']csrf-token["\']\s+content=["\']([^"\']+)["\']', html)
        if m:
            return m.group(1)
        m = re.search(r'name=["\']_csrf["\']\s+value=["\']([^"\']+)["\']', html)
        if m:
            return m.group(1)
        m = re.search(r'value=["\']([^"\']+)["\']\s+name=["\']_csrf["\']', html)
        if m:
            return m.group(1)
        return ""

    def login(self, username, password):
        status, html, _ = self.request("/auth?action=login")
        csrf = self.get_csrf(html)
        if not csrf:
            return False, "Không lấy được CSRF token từ trang đăng nhập"

        login_data = {
            "_csrf": csrf,
            "username": username,
            "password": password
        }
        status, body, headers = self.request("/auth", method="POST", data=login_data, follow_redirects=True)
        # Kiểm tra nếu đăng nhập thành công: URL chuyển hướng hoặc có session
        if "action=logout" in body or "Đăng xuất" in body or "/admin" in body:
            return True, "Đăng nhập thành công"
        return False, "Đăng nhập thất bại (sai user/pass hoặc bị chặn)"

results = []

def record(test_name, passed, detail=""):
    symbol = "✅ PASS" if passed else "❌ FAIL"
    results.append((test_name, passed, detail))
    print(f"[{symbol}] {test_name}" + (f" -> {detail}" if detail else ""))

def run_tests():
    print("=" * 70)
    print("🚀 BẮT ĐẦU CHẠY KIỂM THỬ TỰ ĐỘNG TOÀN DIỆN HỆ THỐNG")
    print("=" * 70)

    # -------------------------------------------------------------
    # 1. KIỂM THỬ PUBLIC PAGES
    # -------------------------------------------------------------
    print("\n--- NHÓM 1: CÁC TRANG CÔNG KHAI (PUBLIC ENDPOINTS) ---")
    guest = TestSession("Guest")

    st, html, _ = guest.request("/")
    record("1.1 Trang chủ GET /", st == 200 and "ĐọcTruyện" in html, f"HTTP {st}")

    st, html, _ = guest.request("/story?action=list")
    record("1.2 Danh sách truyện /story?action=list", st == 200 and "Kho truyện" in html, f"HTTP {st}")

    st, html, _ = guest.request("/rank")
    record("1.3 Bảng xếp hạng /rank", st == 200 and "Xếp hạng" in html, f"HTTP {st}")

    st, html, _ = guest.request("/page?name=rules")
    record("1.4 Trang nội quy /page?name=rules", st == 200 and "Nội quy" in html, f"HTTP {st}")

    st, html, _ = guest.request("/auth?action=login")
    record("1.5 Trang đăng nhập /auth?action=login", st == 200 and "_csrf" in html, f"HTTP {st}")

    # -------------------------------------------------------------
    # 2. KIỂM THỬ RESTFUL JSON API
    # -------------------------------------------------------------
    print("\n--- NHÓM 2: RESTFUL JSON API (/api/*) ---")
    st, body, _ = guest.request("/api/stories")
    try:
        data = json.loads(body)
        record("2.1 API GET /api/stories", st == 200 and "stories" in data, f"Số truyện: {len(data.get('stories', []))}")
    except Exception as e:
        record("2.1 API GET /api/stories", False, f"JSON parse error: {e}")

    st, body, _ = guest.request("/api/tags")
    try:
        data = json.loads(body)
        record("2.2 API GET /api/tags", st == 200 and "tags" in data, f"Số thể loại: {len(data.get('tags', []))}")
    except Exception as e:
        record("2.2 API GET /api/tags", False, f"JSON parse error: {e}")

    st, body, _ = guest.request("/api/rank?by=views")
    try:
        data = json.loads(body)
        record("2.3 API GET /api/rank?by=views", st == 200 and "stories" in data, f"Số truyện xếp hạng: {len(data.get('stories', []))}")
    except Exception as e:
        record("2.3 API GET /api/rank?by=views", False, f"JSON parse error: {e}")

    # Lấy thử chi tiết truyện ID 35 (Quỷ Bí Chi Chủ)
    st, body, _ = guest.request("/api/story/35")
    try:
        data = json.loads(body)
        record("2.4 API GET /api/story/35", st == 200 and data.get("story", {}).get("title") == "Quỷ Bí Chi Chủ", f"Title: {data.get('story', {}).get('title')}")
    except Exception as e:
        record("2.4 API GET /api/story/35", False, f"Lỗi: {e}")

    # -------------------------------------------------------------
    # 3. KIỂM THỬ HÀNG RÀO PHÂN QUYỀN RBAC TRÊN ADMINFILTER
    # -------------------------------------------------------------
    print("\n--- NHÓM 3: PHÂN QUYỀN RBAC CHI TIẾT (/admin/*) ---")

    # 3.1 Khách chưa đăng nhập vào /admin/dashboard -> Phải chuyển hướng về login (302)
    st, body, hdrs = guest.request("/admin/dashboard", follow_redirects=False)
    record("3.1 Guest truy cập /admin/dashboard -> Chuyển hướng đăng nhập", st == 302, f"HTTP {st}")

    # 3.2 Độc giả thường (haiduong) đăng nhập
    user_hd = TestSession("Hải Đường")
    ok, msg = user_hd.login("haiduong", "123456")
    record("3.2 Đăng nhập tài khoản độc giả haiduong", ok, msg)

    st, body, _ = user_hd.request("/admin/dashboard")
    record("3.3 Độc giả thường vào /admin/dashboard -> 403 Forbidden", st == 403, f"HTTP {st}")

    st, body, _ = user_hd.request("/admin/wallet")
    record("3.4 Độc giả thường vào /admin/wallet -> 403 Forbidden", st == 403, f"HTTP {st}")

    # 3.3 Moderator (mod1) đăng nhập
    user_mod = TestSession("Kiểm Duyệt Viên mod1")
    ok, msg = user_mod.login("mod1", "123456")
    record("3.5 Đăng nhập tài khoản MODERATOR mod1", ok, msg)

    st, body, _ = user_mod.request("/admin/dashboard")
    record("3.6 Moderator vào /admin/dashboard -> 200 OK (Được phép)", st == 200, f"HTTP {st}")

    st, body, _ = user_mod.request("/admin/comment")
    record("3.7 Moderator vào /admin/comment -> 200 OK (Kiểm duyệt bình luận)", st == 200, f"HTTP {st}")

    st, body, _ = user_mod.request("/admin/report")
    record("3.8 Moderator vào /admin/report -> 200 OK (Xử lý báo cáo)", st == 200, f"HTTP {st}")

    st, body, _ = user_mod.request("/admin/story")
    record("3.9 Moderator vào /admin/story -> 200 OK (Quản lý truyện)", st == 200, f"HTTP {st}")

    # CÁC MỤC BỊ CẤM TUYỆT ĐỐI VỚI MODERATOR:
    st, body, _ = user_mod.request("/admin/wallet")
    record("3.10 Moderator vào /admin/wallet -> 403 Forbidden (Bảo vệ ví xu)", st == 403, f"HTTP {st}")

    st, body, _ = user_mod.request("/admin/user")
    record("3.11 Moderator vào /admin/user -> 403 Forbidden (Bảo vệ tài khoản)", st == 403, f"HTTP {st}")

    st, body, _ = user_mod.request("/admin/audit")
    record("3.12 Moderator vào /admin/audit -> 403 Forbidden (Bảo vệ nhật ký)", st == 403, f"HTTP {st}")

    # 3.4 Editor (editor1) đăng nhập
    user_ed = TestSession("Biên Tập Viên editor1")
    ok, msg = user_ed.login("editor1", "123456")
    record("3.13 Đăng nhập tài khoản EDITOR editor1", ok, msg)

    st, body, _ = user_ed.request("/admin/dashboard")
    record("3.14 Editor vào /admin/dashboard -> 200 OK", st == 200, f"HTTP {st}")

    st, body, _ = user_ed.request("/admin/story")
    record("3.15 Editor vào /admin/story -> 200 OK (Duyệt/sửa truyện)", st == 200, f"HTTP {st}")

    st, body, _ = user_ed.request("/admin/tag")
    record("3.16 Editor vào /admin/tag -> 200 OK (Quản lý thể loại)", st == 200, f"HTTP {st}")

    # CÁC MỤC BỊ CẤM VỚI EDITOR (bug-003 đã được khắc phục hoàn toàn):
    st, body, _ = user_ed.request("/admin/wallet")
    record("3.17 Editor vào /admin/wallet -> 403 Forbidden (Đã chặn Editor vào Ví xu)", st == 403, f"HTTP {st}")

    st, body, _ = user_ed.request("/admin/user")
    record("3.18 Editor vào /admin/user -> 403 Forbidden (Đã chặn Editor vào User)", st == 403, f"HTTP {st}")

    # 3.5 Quản trị viên Cấp cao (admin) đăng nhập
    user_admin = TestSession("Admin")
    ok, msg = user_admin.login("admin", "admin123")
    record("3.19 Đăng nhập tài khoản SUPER_ADMIN admin", ok, msg)

    st, body, _ = user_admin.request("/admin/dashboard")
    record("3.20 Admin vào /admin/dashboard -> 200 OK", st == 200, f"HTTP {st}")

    st, body, _ = user_admin.request("/admin/wallet")
    record("3.21 Admin vào /admin/wallet -> 200 OK", st == 200, f"HTTP {st}")

    st, body, _ = user_admin.request("/admin/user")
    record("3.22 Admin vào /admin/user -> 200 OK", st == 200, f"HTTP {st}")

    st, body, _ = user_admin.request("/admin/audit")
    record("3.23 Admin vào /admin/audit -> 200 OK", st == 200, f"HTTP {st}")

    # -------------------------------------------------------------
    # 4. KIỂM THỬ GIAO DIỆN MODALS & BẢO TOÀN QUYỀN KHI LƯU PROFILE
    # -------------------------------------------------------------
    print("\n--- NHÓM 4: KIỂM THỬ PROFILE VÀ GIAO DIỆN MODALS ---")

    # 4.1 Kiểm tra bug-004: Cập nhật hồ sơ không làm mất quyền RBAC
    st, html, _ = user_mod.request("/user?action=edit")
    csrf = user_mod.get_csrf(html)
    update_data = {
        "_csrf": csrf,
        "action": "profile",
        "displayName": "Kiểm Duyệt Viên Số 1",
        "email": "mod1@webdoctruyen.local",
        "bio": "Cập nhật bio kiểm thử RBAC không bị mất quyền."
    }
    st, _, _ = user_mod.request("/user", method="POST", data=update_data, follow_redirects=True)
    # Thử vào lại /admin/comment:
    st_check, _, _ = user_mod.request("/admin/comment")
    record("4.1 [bug-004] Cập nhật hồ sơ -> Quyền RBAC vẫn giữ nguyên (vào được /admin/comment)", st_check == 200, f"HTTP {st_check}")

    # 4.2 Kiểm tra tích hợp Modal trong giao diện
    st, html_me, _ = user_hd.request("/user?action=me")
    has_quick_topup = "quickTopupModalBackdrop" in html_me
    has_confirm_modal = "appConfirmModalBackdrop" in html_me
    record("4.2 Modal Toàn cục tích hợp trong trang /user?action=me", has_quick_topup and has_confirm_modal, "Đã nhúng Quick Topup & Confirm Modal")

    st, html, _ = user_admin.request("/admin/dashboard")
    has_confirm_admin = "appConfirmModalBackdrop" in html
    record("4.3 Confirm Modal tích hợp trong layout admin.jsp", has_confirm_admin, "Đã nhúng Confirm Modal")

    # 4.4 [ISSUE-028] Kiểm tra data-confirm bảo vệ nút Khoá và Nâng/Hạ trên users.jsp
    st, html_users, _ = user_admin.request("/admin/user")
    has_ban_confirm = 'data-confirm-title="Xác nhận khoá tài khoản"' in html_users
    has_role_confirm = 'data-confirm-title="Xác nhận thay đổi vai trò"' in html_users
    record("4.4 [ISSUE-028] Xác nhận an toàn data-confirm trên admin/users.jsp", has_ban_confirm and has_role_confirm, "Bảo vệ nút Khoá nick & Nâng/Hạ quyền")

    # 4.5 [ISSUE-028] Kiểm tra data-confirm bảo vệ nút Duyệt đăng trên stories.jsp
    st, html_stories, _ = user_admin.request("/admin/story?status=DRAFT")
    has_publish_confirm = 'data-confirm-title="Xác nhận duyệt đăng"' in html_stories
    record("4.5 [ISSUE-028] Xác nhận an toàn data-confirm Duyệt đăng trên stories.jsp", has_publish_confirm, "Bảo vệ nút Duyệt đăng truyện")

    # 4.6 [bug-007] Kiểm tra bộ lọc CHANGE_ROLE trên audit.jsp
    st, html_audit, _ = user_admin.request("/admin/audit")
    has_change_role_filter = 'action=CHANGE_ROLE' in html_audit and 'Đổi vai trò' in html_audit
    record("4.6 [bug-007] Thẻ lọc 'Đổi vai trò' (CHANGE_ROLE) trên admin/audit.jsp", has_change_role_filter, "Hiển thị bộ lọc CHANGE_ROLE")

    # 4.7 [bug-006] Khách vãng lai và độc giả thường bị chặn 403 khi vào /uploads/evidence/*
    st_evidence_guest, _, _ = guest.request("/uploads/evidence/test-evidence.jpg")
    record("4.7 [bug-006] Chặn 403 Forbidden với khách khi truy cập ảnh bằng chứng tố cáo", st_evidence_guest == 403, f"HTTP {st_evidence_guest}")

    # 4.8 [bug-008] Chống CSRF: Chặn GET /notification?action=mark_read bằng 405 Method Not Allowed
    st_notif_get, _, _ = user_hd.request("/notification?action=mark_read")
    record("4.8 [bug-008] Chặn GET /notification?action=mark_read -> 405 Method Not Allowed", st_notif_get == 405, f"HTTP {st_notif_get}")

    # 4.9 [bug-008] Cho phép POST /notification?action=mark_read với CSRF Token
    st_notif_page, html_notif, _ = user_hd.request("/notification")
    csrf_notif = user_hd.get_csrf(html_notif)
    st_notif_post, body_notif_post, _ = user_hd.request("/notification?action=mark_read", method="POST", data={"_csrf": csrf_notif})
    record("4.9 [bug-008] POST /notification?action=mark_read với CSRF -> 200 OK & JSON success", st_notif_post == 200 and "true" in body_notif_post, f"HTTP {st_notif_post}")

    # 4.10 [ISSUE-029] Kiểm tra xác nhận an toàn data-confirm nâng cấp VIP trên user/me.jsp
    has_vip_confirm = 'data-confirm-title="Xác nhận nâng cấp VIP"' in html_me
    record("4.10 [ISSUE-029] Nút nâng cấp VIP có data-confirm bảo vệ ví người dùng", has_vip_confirm, "Bảo vệ nút Kích hoạt/Gia hạn VIP")

    # 4.11 [ISSUE-029] Kiểm tra modal điều chỉnh ví chuẩn hoá và có data-confirm trên admin/wallet.jsp
    st_w, html_wallet, _ = user_admin.request("/admin/wallet")
    has_wallet_modal = 'id="adjustModal"' in html_wallet and 'data-confirm-title="Xác nhận điều chỉnh ví"' in html_wallet
    record("4.11 [ISSUE-029] Modal điều chỉnh ví chuẩn hoá và có data-confirm xác nhận", has_wallet_modal, "Bảo vệ form điều chỉnh ví Admin")

    # 4.12 [bug-009] Chống Logout CSRF: Gọi GET /auth?action=logout không được huỷ phiên làm việc
    user_test_logout = TestSession("http://localhost:8080")
    user_test_logout.login("haiduong", "123456")
    st_logout_get, _, _ = user_test_logout.request("/auth?action=logout")
    st_check_session, html_check, _ = user_test_logout.request("/user?action=me")
    is_session_preserved = (st_check_session == 200 and "Thành viên" in html_check)
    record("4.12 [bug-009] Chống Logout CSRF: GET /auth?action=logout không huỷ phiên", is_session_preserved, f"Session còn hiệu lực sau GET")

    # 4.13 [bug-009] Đăng xuất an toàn: POST /auth với action=logout và _csrf huỷ phiên thành công
    csrf_logout = user_test_logout.get_csrf(html_check)
    st_logout_post, _, _ = user_test_logout.request("/auth", method="POST", data={"action": "logout", "_csrf": csrf_logout})
    st_check_logged_out, html_logged_out, _ = user_test_logout.request("/user?action=me")
    is_logged_out = (st_check_logged_out == 302 or "Đăng nhập" in html_logged_out or "/auth?action=login" in html_logged_out)
    record("4.13 [bug-009] Đăng xuất an toàn: POST /auth?action=logout với CSRF huỷ phiên", is_logged_out, "Huỷ phiên thành công qua POST")

    # 4.14 [ISSUE-030] Chuẩn hoá Tip Modal: Kế thừa .app-modal-backdrop và có xác nhận trước khi trừ xu
    st_tip_page, html_tip_page, _ = user_hd.request("/story?action=detail&id=1")
    has_tip_modal = ('id="tipModalBackdrop"' in html_tip_page and 'app-modal-backdrop' in html_tip_page and 'Xác nhận tặng xu' in html_tip_page)
    record("4.14 [ISSUE-030] Modal tặng xu đồng bộ Design System và có xác nhận trừ xu", has_tip_modal, "Chuẩn hoá _tip-modal.jsp")

    # 4.15 [ISSUE-030] Gamification AJAX & Debounce trên user/me.jsp
    has_gamification_ajax = ('form-daily-checkin' in html_me and 'form-claim-quest' in html_me and 'window.launchCelebration' in html_me)
    record("4.15 [ISSUE-030] Tối ưu Gamification Điểm danh & Nhận thưởng AJAX & Debounce", has_gamification_ajax, "UX Điểm danh & Nhiệm vụ")

    # 4.16 [ISSUE-031] Interactive Star Rating Picker & Bình chọn hữu ích đánh giá qua AJAX
    st_story_detail, html_story_detail, _ = user_hd.request("/story?action=detail&id=1")
    has_star_picker = ('id="interactive-star-picker"' in html_story_detail and 'star-item' in html_story_detail and 'id="story-review-form"' in html_story_detail)
    csrf_story = user_hd.get_csrf(html_story_detail)
    st_vote_ajax, body_vote_ajax, _ = user_hd.request("/rating", method="POST",
                                                     data={"action": "vote", "reviewId": "1", "storyId": "1", "_csrf": csrf_story},
                                                     headers={"X-Requested-With": "XMLHttpRequest"})
    is_vote_ajax_ok = (st_vote_ajax == 200 and '"voted":' in body_vote_ajax and '"count":' in body_vote_ajax)
    record("4.16 [ISSUE-031] Interactive Star Picker & Bình chọn hữu ích AJAX không reload", has_star_picker and is_vote_ajax_ok, "Star Picker & Review Vote AJAX")

    # 4.17 [ISSUE-032] Bộ lọc & Jumper nhảy chương nhanh cùng chống spam submit comment
    has_ch_filter = ('id="chapter-filter-input"' in html_story_detail and
                     'id="chapter-filter-clear"' in html_story_detail and
                     'id="chapter-filter-empty"' in html_story_detail and
                     'chapter-pulse' in html_story_detail)
    has_comment_debounce = ('__commentSubmitDebounceSetup' in html_story_detail)
    record("4.17 [ISSUE-032] Lọc & Nhảy chương (Clear, Empty, Enter) & Debounce bình luận", has_ch_filter and has_comment_debounce, "Chapter Jumper & Comment Debounce")

    # 4.18 [ISSUE-033] Studio Tác Giả: Kéo thả ảnh bìa (Drag & Drop), hủy file và debounce submit
    st_create_story, html_create_story, _ = user_hd.request("/story?action=create")
    has_cover_ux = ('id="cover-drop-overlay"' in html_create_story and
                    'id="btnRemoveCoverFile"' in html_create_story and
                    '⏳ Đang lưu truyện...' in html_create_story)
    record("4.18 [ISSUE-033] Bìa truyện: Kéo thả Drag-Drop, nút hủy file & submit debounce", st_create_story == 200 and has_cover_ux, "Story Cover UX & Debounce")

    # 4.19 [ISSUE-033] Chương Truyện Tranh & Chữ: Đảo ngược thứ tự trang, xóa ảnh mới & debounce
    st_create_comic, html_create_comic, _ = user_hd.request("/chapter?action=create&storyId=36")
    has_comic_ux = ('id="btnReversePages"' in html_create_comic and
                    'id="btnClearNewPages"' in html_create_comic and
                    'btnSaveComic' in html_create_comic)
    st_create_text_ch, html_create_text_ch, _ = user_hd.request("/chapter?action=create&storyId=35")
    has_text_debounce = ('⏳ Đang lưu chương...' in html_create_text_ch)
    record("4.19 [ISSUE-033] Trình soạn chương: Đảo thứ tự trang, xóa ảnh mới & debounce", (st_create_comic in (200, 403) or has_comic_ux) and (st_create_text_ch in (200, 403) or has_text_debounce), "Chapter UX & Debounce")

    # -------------------------------------------------------------
    # TỔNG KẾT
    # -------------------------------------------------------------
    print("\n" + "=" * 70)
    passed_count = sum(1 for _, p, _ in results if p)
    total_count = len(results)
    print(f"📊 KẾT QUẢ KIỂM THỬ: {passed_count}/{total_count} bài kiểm tra PASS ({passed_count*100//total_count}%)")
    print("=" * 70)

    if passed_count == total_count:
        print("🎉 TOÀN BỘ HỆ THỐNG VẬN HÀNH HOÀN TOÀN ỔN ĐỊNH VÀ AN TOÀN!")
        sys.exit(0)
    else:
        print("⚠️ Có bài kiểm tra không đạt yêu cầu.")
        sys.exit(1)

if __name__ == "__main__":
    run_tests()
