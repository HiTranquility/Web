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

        print("[1] 40_trang_dang_ky_recaptcha.png")
        page.goto(f"{BASE_URL}/auth?action=register", wait_until="networkidle")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '40_trang_dang_ky_recaptcha.png'))

        print("[2] 41_trang_quen_mat_khau.png")
        page.goto(f"{BASE_URL}/auth?action=forgot", wait_until="networkidle")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '41_trang_quen_mat_khau.png'))

        print("[3] 38_trang_loi_403_forbidden.png")
        page.goto(f"{BASE_URL}/error?code=403", wait_until="networkidle")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '38_trang_loi_403_forbidden.png'))

        print("[4] 39_trang_loi_404_notfound.png")
        page.goto(f"{BASE_URL}/error?code=404", wait_until="networkidle")
        page.wait_for_timeout(600)
        page.screenshot(path=os.path.join(OUT_DIR, '39_trang_loi_404_notfound.png'))

        # Login haiduong
        print("[5] Login haiduong...")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'haiduong')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[6] 29_modal_tang_xu_tip.png")
        page.goto(f"{BASE_URL}/story?action=detail&id=1", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.evaluate("""() => {
            const tipBtn = document.querySelector('[data-action="tip"]') || document.querySelector('.btn-tip') || document.querySelector('button[onclick*="tip"]');
            if (tipBtn) tipBtn.click();
            else {
                const modal = document.getElementById('tipModal') || document.querySelector('.tip-modal');
                if (modal) { modal.classList.add('is-open'); modal.style.display = 'flex'; }
            }
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '29_modal_tang_xu_tip.png'))

        print("[7] 30_interactive_star_picker.png")
        # Scroll to rating section
        page.evaluate("""() => {
            const star = document.querySelector('.star-picker') || document.querySelector('.rating-stars-input') || document.querySelector('.stars-group');
            if (star) star.scrollIntoView({ behavior: 'instant', block: 'center' });
        }""")
        page.wait_for_timeout(400)
        page.screenshot(path=os.path.join(OUT_DIR, '30_interactive_star_picker.png'))

        print("[8] 31_loc_nhay_chuong_pulse.png")
        page.evaluate("""() => {
            const filterInput = document.getElementById('chapterFilter') || document.querySelector('input[name="chapterSearch"]') || document.querySelector('.chapter-filter-input');
            if (filterInput) {
                filterInput.scrollIntoView({ behavior: 'instant', block: 'center' });
                filterInput.value = 'Chương 1';
                filterInput.dispatchEvent(new Event('input'));
            }
        }""")
        page.wait_for_timeout(400)
        page.screenshot(path=os.path.join(OUT_DIR, '31_loc_nhay_chuong_pulse.png'))

        print("[9] 34_quotes_drawer_highlighter.png")
        page.goto(f"{BASE_URL}/chapter?action=read&id=59", wait_until="networkidle")
        page.wait_for_timeout(1000)
        page.evaluate("""() => {
            const qBtn = document.querySelector('[data-action="quotes"]') || document.querySelector('.btn-quotes-drawer') || document.getElementById('quotesDrawerBtn');
            if (qBtn) qBtn.click();
            else {
                const drawer = document.getElementById('quotesDrawer') || document.querySelector('.quotes-drawer');
                if (drawer) { drawer.classList.add('is-open'); drawer.style.display = 'block'; }
            }
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '34_quotes_drawer_highlighter.png'))

        print("[10] 37_hop_thoai_confirm_glass.png")
        page.evaluate("""() => {
            const modal = document.getElementById('confirmModal') || document.querySelector('.confirm-modal-backdrop') || document.querySelector('.app-confirm-modal');
            if (modal) {
                modal.classList.add('is-open');
                modal.style.display = 'flex';
                const title = modal.querySelector('.modal-title') || modal.querySelector('h3');
                if (title) title.innerText = 'Xác Nhận Thao Tác Quan Trọng';
                const msg = modal.querySelector('.modal-message') || modal.querySelector('p');
                if (msg) msg.innerText = 'Bạn có chắc chắn muốn thực hiện giao dịch này không? Thao tác không thể hoàn tác.';
            }
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '37_hop_thoai_confirm_glass.png'))

        # Login Author mocmien
        print("[11] Login mocmien...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'mocmien')
        page.fill('input[name="password"]', '123456')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[12] 32_comic_drag_drop_upload.png")
        page.goto(f"{BASE_URL}/chapter?action=create&storyId=27", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '32_comic_drag_drop_upload.png'))

        print("[13] 33_khieu_nai_appeal.png")
        page.goto(f"{BASE_URL}/appeal", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.screenshot(path=os.path.join(OUT_DIR, '33_khieu_nai_appeal.png'))

        # Login admin
        print("[14] Login admin...")
        page.goto(f"{BASE_URL}/auth?action=logout", wait_until="networkidle")
        page.goto(f"{BASE_URL}/auth?action=login", wait_until="networkidle")
        page.fill('input[name="username"]', 'admin')
        page.fill('input[name="password"]', 'admin123')
        page.click('button[type="submit"]')
        page.wait_for_timeout(1500)

        print("[15] 42_admin_khoa_user_modal.png")
        page.goto(f"{BASE_URL}/admin/user", wait_until="networkidle")
        page.wait_for_timeout(800)
        page.evaluate("""() => {
            const banBtn = document.querySelector('[data-action="ban"]') || document.querySelector('.btn-ban-user');
            if (banBtn) banBtn.click();
            else {
                const modal = document.getElementById('banModal') || document.querySelector('.ban-modal');
                if (modal) { modal.classList.add('is-open'); modal.style.display = 'flex'; }
            }
        }""")
        page.wait_for_timeout(500)
        page.screenshot(path=os.path.join(OUT_DIR, '42_admin_khoa_user_modal.png'))

        browser.close()
        print("ADDITIONAL SCREENSHOTS CAPTURED SUCCESSFULLY!")

if __name__ == '__main__':
    run()
