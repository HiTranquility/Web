import os
import sys
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding='utf-8')

OUT_DIR = os.path.join(os.getcwd(), 'docs', 'report_images')
os.makedirs(OUT_DIR, exist_ok=True)

BASE_URL = 'http://localhost:8080'

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch(channel='msedge', headless=True)
        context = browser.new_context(
            viewport={'width': 1366, 'height': 820},
            device_scale_factor=1.5
        )
        page = context.new_page()

        print("[1] 01_trang_chu.png")
        page.goto(f"{BASE_URL}/", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '01_trang_chu.png'))

        print("[2] 02_danh_sach_loc_truyen.png")
        page.goto(f"{BASE_URL}/story?action=list", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '02_danh_sach_loc_truyen.png'))

        print("[3] 03_tim_kiem_fulltext.png")
        page.goto(f"{BASE_URL}/story?action=search&q=kiếm", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '03_tim_kiem_fulltext.png'))

        print("[4] 04_chi_tiet_truyen.png")
        page.goto(f"{BASE_URL}/story?action=detail&id=1", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '04_chi_tiet_truyen.png'))

        print("[5] 05_danh_gia_spoiler.png")
        page.evaluate("""() => {
            const revTab = document.querySelector('[data-tab="reviews"]') || document.querySelector('a[href="#reviews"]');
            if (revTab) revTab.click();
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '05_danh_gia_spoiler.png'))

        print("[6] 06_chi_tiet_comic.png")
        page.goto(f"{BASE_URL}/story?action=detail&id=27", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '06_chi_tiet_comic.png'))

        print("[7] 28_trang_dang_nhap_auth.png")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '28_trang_dang_nhap_auth.png'))

        # Login haiduong
        page.fill('input[name="username"]', 'haiduong')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[8] 07_doc_novel_darkmode.png")
        page.goto(f"{BASE_URL}/chapter?action=read&id=59", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '07_doc_novel_darkmode.png'))

        print("[9] 08_doc_novel_sepia.png")
        page.evaluate("""() => {
            const btn = document.querySelector('[data-theme="sepia"]') || document.querySelector('.theme-btn-sepia');
            if (btn) btn.click();
            else document.body.setAttribute('data-theme', 'sepia');
            const drawerBtn = document.querySelector('.btn-reader-settings') || document.querySelector('[data-action="settings"]');
            if (drawerBtn) drawerBtn.click();
        }""")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '08_doc_novel_sepia.png'))

        print("[10] 09_doc_novel_auto_scroll.png")
        page.evaluate("""() => {
            const autoBtn = document.querySelector('[data-action="autoscroll"]') || document.querySelector('.btn-autoscroll') || document.getElementById('autoScrollBtn');
            if (autoBtn) autoBtn.click();
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '09_doc_novel_auto_scroll.png'))

        print("[11] 10_doc_comic_webtoon.png")
        page.goto(f"{BASE_URL}/chapter?action=read&id=121", wait_until="networkidle")
        page.wait_for_timeout(1200)
        page.screenshot(path=os.path.join(OUT_DIR, '10_doc_comic_webtoon.png'))

        print("[12] 11_man_hinh_paywall_vip.png")
        page.goto(f"{BASE_URL}/chapter?action=read&id=61", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '11_man_hinh_paywall_vip.png'))

        print("[13] 12_modal_mo_khoa_vip.png")
        page.evaluate("""() => {
            const btn = document.querySelector('[data-modal="vipUnlockModal"]') || document.querySelector('.btn-unlock-vip');
            if (btn) btn.click();
            else {
                const modal = document.getElementById('vipUnlockModal') || document.querySelector('.vip-unlock-modal');
                if (modal) {
                    modal.classList.add('active');
                    modal.style.display = 'flex';
                }
            }
        }""")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '12_modal_mo_khoa_vip.png'))

        print("[14] 13_modal_nap_xu_nhanh.png")
        page.evaluate("""() => {
            const vModal = document.getElementById('vipUnlockModal');
            if (vModal) { vModal.classList.remove('active'); vModal.style.display = 'none'; }
            const topupModal = document.getElementById('quickTopupModal') || document.querySelector('.quick-topup-modal');
            if (topupModal) {
                topupModal.classList.add('active');
                topupModal.style.display = 'flex';
            }
        }""")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '13_modal_nap_xu_nhanh.png'))

        print("[15] 14_trang_ca_nhan_gamification.png")
        page.goto(f"{BASE_URL}/user?action=me", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '14_trang_ca_nhan_gamification.png'))

        print("[16] 15_tu_truyen_bookmarks.png")
        page.goto(f"{BASE_URL}/bookmark?action=list", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '15_tu_truyen_bookmarks.png'))

        print("[17] 16_lich_su_doc_history.png")
        page.goto(f"{BASE_URL}/history", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '16_lich_su_doc_history.png'))

        print("[18] 20_bang_xep_hang_top.png")
        page.goto(f"{BASE_URL}/rank", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '20_bang_xep_hang_top.png'))

        # Login Author mocmien
        print("[19] Đăng nhập Tác giả mocmien...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'mocmien')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[20] 17_author_studio_mine.png")
        page.goto(f"{BASE_URL}/story?action=mine", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '17_author_studio_mine.png'))

        print("[21] 18_author_dang_truyen_preview.png")
        page.goto(f"{BASE_URL}/story?action=create", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '18_author_dang_truyen_preview.png'))

        print("[22] 19_author_editor_zen.png")
        page.goto(f"{BASE_URL}/chapter?action=create&storyId=1", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '19_author_editor_zen.png'))

        # Login Super Admin
        print("[23] Đăng nhập Super Admin admin...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'admin')
        page.fill('input[name="password"]', 'admin123')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[24] 21_admin_dashboard.png")
        page.goto(f"{BASE_URL}/admin/dashboard", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '21_admin_dashboard.png'))

        print("[25] 22_admin_quan_ly_truyen.png")
        page.goto(f"{BASE_URL}/admin/story", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '22_admin_quan_ly_truyen.png'))

        print("[26] 23_admin_quan_ly_user.png")
        page.goto(f"{BASE_URL}/admin/user", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '23_admin_quan_ly_user.png'))

        print("[27] 24_admin_kiem_toan_audit.png")
        page.goto(f"{BASE_URL}/admin/audit", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '24_admin_kiem_toan_audit.png'))

        print("[28] 25_admin_xu_ly_bao_cao.png")
        page.goto(f"{BASE_URL}/admin/report", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '25_admin_xu_ly_bao_cao.png'))

        print("[29] 26_admin_quan_ly_the_loai.png")
        page.goto(f"{BASE_URL}/admin/tag", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '26_admin_quan_ly_the_loai.png'))

        print("[30] 27_admin_quan_ly_vi_xu.png")
        page.goto(f"{BASE_URL}/admin/wallet", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '27_admin_quan_ly_vi_xu.png'))

        browser.close()
        print("HOÀN TẤT CHỤP TẤT CẢ CÁC TRANG VÀ CHỨC NĂNG CỦA HỆ THỐNG!")

if __name__ == '__main__':
    run()
