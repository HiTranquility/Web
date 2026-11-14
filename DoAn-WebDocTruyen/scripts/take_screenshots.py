import os
import sys
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding='utf-8')

OUT_DIR = os.path.join(os.getcwd(), 'docs', 'report_images')
os.makedirs(OUT_DIR, exist_ok=True)

BASE_URL = 'http://localhost:8080'

def run():
    with sync_playwright() as p:
        # Launch browser
        browser = p.chromium.launch(channel='msedge', headless=True)
        context = browser.new_context(
            viewport={'width': 1366, 'height': 820},
            device_scale_factor=1.5
        )
        page = context.new_page()

        print("[1] Chụp Trang Chủ...")
        page.goto(f"{BASE_URL}/", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '01_trang_chu.png'))

        print("[2] Chụp Chi Tiết Truyện...")
        page.goto(f"{BASE_URL}/story?action=detail&id=1", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '02_chi_tiet_truyen.png'))

        print("[3] Chụp Bảng Xếp Hạng...")
        page.goto(f"{BASE_URL}/rank", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '14_bang_xep_hang.png'))

        print("[4] Đăng nhập tài khoản độc giả haiduong...")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'haiduong')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[5] Chụp Trang Cá Nhân & Gamification...")
        page.goto(f"{BASE_URL}/user?action=me", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '08_ho_so_gamification.png'))

        print("[6] Chụp Đọc Truyện Chữ (Novel Reader - Dark Mode)...")
        page.goto(f"{BASE_URL}/chapter?action=read&id=59", wait_until="networkidle")
        page.wait_for_timeout(1200)
        page.screenshot(path=os.path.join(OUT_DIR, '03_doc_truyen_chu_darkmode.png'))

        print("[7] Chụp Đọc Truyện Chữ (Sepia Mode)...")
        # Click button theme sepia if available or execute JS to switch theme
        page.evaluate("""() => {
            const btn = document.querySelector('[data-theme="sepia"]') || document.querySelector('.theme-btn-sepia');
            if (btn) btn.click();
            else document.body.setAttribute('data-theme', 'sepia');
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '04_doc_truyen_chu_sepia.png'))

        print("[8] Chụp Modal Nạp Xu Nhanh...")
        page.evaluate("""() => {
            const modal = document.getElementById('quickTopupModal') || document.querySelector('.quick-topup-modal');
            if (modal) {
                modal.classList.add('active');
                modal.style.display = 'flex';
                modal.setAttribute('aria-hidden', 'false');
            }
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '07_modal_nap_xu_nhanh.png'))

        # Close topup modal
        page.evaluate("""() => {
            const modal = document.getElementById('quickTopupModal') || document.querySelector('.quick-topup-modal');
            if (modal) {
                modal.classList.remove('active');
                modal.style.display = 'none';
            }
        }""")

        print("[9] Chụp Modal Mở Khóa VIP / Paywall...")
        page.goto(f"{BASE_URL}/chapter?action=read&id=61", wait_until="networkidle")
        page.wait_for_timeout(1000)
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
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '06_modal_mo_khoa_vip.png'))

        # Check comic chapter
        print("[10] Chụp Đọc Truyện Tranh (Comic)...")
        page.goto(f"{BASE_URL}/story?action=detail&id=27", wait_until="networkidle")
        first_chap = page.query_selector('a[href*="/chapter?action=read"]')
        if first_chap:
            first_chap.click()
            page.wait_for_load_state("networkidle")
            page.wait_for_timeout(1000)
            page.screenshot(path=os.path.join(OUT_DIR, '05_doc_truyen_tranh_comic.png'))

        print("[11] Đăng nhập Tác giả mocmien...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'mocmien')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[12] Chụp Author Studio & Tủ Truyện...")
        page.goto(f"{BASE_URL}/story?action=mine", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '09_author_studio_mine.png'))

        print("[13] Chụp Trình Soạn Thảo Chương Truyện...")
        page.goto(f"{BASE_URL}/chapter?action=create&storyId=1", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '10_author_editor_zen.png'))

        print("[14] Đăng nhập Super Admin...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'admin')
        page.fill('input[name="password"]', 'admin123')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[15] Chụp Admin Dashboard...")
        page.goto(f"{BASE_URL}/admin/dashboard", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '11_admin_dashboard.png'))

        print("[16] Chụp Admin Audit Logs...")
        page.goto(f"{BASE_URL}/admin/audit", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '12_admin_kiem_toan_audit.png'))

        print("[17] Chụp Admin User Management...")
        page.goto(f"{BASE_URL}/admin/user", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.screenshot(path=os.path.join(OUT_DIR, '13_admin_quan_ly_user.png'))

        browser.close()
        print("HOÀN TẤT CHỤP TOÀN BỘ ẢNH GIAO DIỆN!")

if __name__ == '__main__':
    run()
