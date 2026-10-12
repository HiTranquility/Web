# -*- coding: utf-8 -*-
import urllib.request, urllib.parse, http.cookiejar

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))

# 1. Lấy trang login để lấy cookie session và CSRF token
login_page = opener.open('http://localhost:8080/auth?action=login').read().decode('utf-8', errors='ignore')
import re
m_csrf = re.search(r'name="_csrf"\s+value="([^"]+)"', login_page)
login_csrf = m_csrf.group(1) if m_csrf else ''

# Đăng nhập bằng mocmien
login_data = urllib.parse.urlencode({'action': 'login', 'username': 'mocmien', 'password': '123456', '_csrf': login_csrf}).encode()
req = urllib.request.Request('http://localhost:8080/auth?action=login', data=login_data)
res = opener.open(req)
print("1. Login status:", res.status)

# 2. Đọc Chương 1 Story 13 (Ám Dạ Trùng Sinh)
res1 = opener.open('http://localhost:8080/chapter?action=read&id=105')
html1 = res1.read().decode('utf-8', errors='ignore')
print("2. Chapter 105 (Story 13 Ch 1): Status", res1.status, "HTML bytes:", len(html1))
print("   - Content check 'Hac Phong Nhai':", 'Hắc Phong Nhai' in html1)
print("   - Has toggle-autoscroll:", 'toggle-autoscroll' in html1)
print("   - Has reader-autoscroll-bar:", 'reader-autoscroll-bar' in html1)
print("   - Has celebration-canvas:", 'celebration-canvas' in html1)

# 3. Đọc Chương 5 Story 13 (VIP)
res5 = opener.open('http://localhost:8080/chapter?action=read&id=109')
html5 = res5.read().decode('utf-8', errors='ignore')
print("3. Chapter 109 (Story 13 Ch 5 VIP): Status", res5.status)
print("   - Has VIP Lock container:", 'vip-lock-container' in html5)
print("   - Has 5 xu price:", '5 xu' in html5)

# 4. Đọc Chương 1 Story 10 (Comic Webtoon)
res_c = opener.open('http://localhost:8080/chapter?action=read&id=93')
html_c = res_c.read().decode('utf-8', errors='ignore')
print("4. Chapter 93 (Comic Webtoon): Status", res_c.status)
print("   - Has comic pages:", 'comic-page' in html_c)
print("   - Has comicPageNav:", 'comicPageNav' in html_c)
print("   - Has toggle-autoscroll:", 'toggle-autoscroll' in html_c)

# 5. Thử mở khoá Chương 5 Story 13 cho mocmien
m = re.search(r'name="_csrf"\s+value="([^"]+)"', html5)
csrf = m.group(1) if m else ''
print("5. Csrf token found:", bool(csrf))
unlock_data = urllib.parse.urlencode({'action': 'unlock', 'id': '109', '_csrf': csrf}).encode()
res_unlock = opener.open(urllib.request.Request('http://localhost:8080/chapter', data=unlock_data))
html_unlocked = res_unlock.read().decode('utf-8', errors='ignore')
print("6. After unlock: Status", res_unlock.status)
print("   - Flash success message:", 'thành công' in html_unlocked)
print("   - VIP content unlocked check:", 'Thượng Lâm Uyển' in html_unlocked)
