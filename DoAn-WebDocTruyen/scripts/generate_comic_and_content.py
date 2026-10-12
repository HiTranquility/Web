"""
Script sinh anh bia va cac trang truyen tranh Webtoon dep mat, chuyen nghiep bang Pillow.
Luu vao C:\\Users\\Admin\\truyen-uploads\\pages\\... va assets/images/covers/...
"""
import os
import math
from PIL import Image, ImageDraw, ImageFont

BASE_UPLOAD = os.path.join(os.path.expanduser("~"), "truyen-uploads")
COVERS_DIR = os.path.abspath("src/main/webapp/assets/images/covers")

os.makedirs(COVERS_DIR, exist_ok=True)
os.makedirs(BASE_UPLOAD, exist_ok=True)

def get_font(size, bold=False):
    # Try common Windows fonts
    font_names = ["arialbd.ttf" if bold else "arial.ttf", "segoeui.ttf", "tahoma.ttf"]
    for fn in font_names:
        try:
            return ImageFont.truetype(os.path.join(os.environ.get("WINDIR", "C:\\Windows"), "Fonts", fn), size)
        except Exception:
            continue
    return ImageFont.load_default()

def draw_gradient(draw, width, height, color1, color2, vertical=True):
    for i in range(height if vertical else width):
        t = i / (height if vertical else width)
        r = int(color1[0] * (1 - t) + color2[0] * t)
        g = int(color1[1] * (1 - t) + color2[1] * t)
        b = int(color1[2] * (1 - t) + color2[2] * t)
        if vertical:
            draw.line([(0, i), (width, i)], fill=(r, g, b))
        else:
            draw.line([(i, 0), (i, height)], fill=(r, g, b))

def draw_speech_bubble(draw, x, y, w, h, text, font, bg=(255, 255, 255), border=(40, 40, 40), text_color=(20, 20, 20), tail_pos="bottom"):
    # Rounded rect
    r = 16
    draw.rounded_rectangle([x, y, x + w, y + h], radius=r, fill=bg, outline=border, width=3)
    # Tail
    if tail_pos == "bottom":
        draw.polygon([(x + w//3, y + h), (x + w//3 + 20, y + h), (x + w//3 - 10, y + h + 24)], fill=bg, outline=border)
        # Redraw seam
        draw.line([(x + w//3 + 2, y + h - 1), (x + w//3 + 18, y + h - 1)], fill=bg, width=4)
    elif tail_pos == "top":
        draw.polygon([(x + w//3, y), (x + w//3 + 20, y), (x + w//3 - 10, y - 24)], fill=bg, outline=border)
        draw.line([(x + w//3 + 2, y + 1), (x + w//3 + 18, y + 1)], fill=bg, width=4)

    # Center text lines
    lines = text.strip().split("\n")
    line_h = font.size + 6
    total_text_h = len(lines) * line_h
    start_y = y + (h - total_text_h) // 2
    for idx, line in enumerate(lines):
        bbox = draw.textbbox((0, 0), line, font=font)
        tw = bbox[2] - bbox[0]
        tx = x + (w - tw) // 2
        ty = start_y + idx * line_h
        draw.text((tx, ty), line, fill=text_color, font=font)

def create_comic_cover_1(filepath):
    """Bìa cho Solo Hunter: Kẻ Thức Tỉnh Cuối Cùng"""
    w, h = 600, 800
    img = Image.new("RGB", (w, h), (15, 18, 30))
    draw = ImageDraw.Draw(img)
    # Cool dark blue / purple gradient
    draw_gradient(draw, w, h, (10, 15, 28), (45, 25, 75))

    # Glow portal effect in center
    for rad in range(220, 60, -8):
        alpha = int(255 * (1 - rad / 220))
        glow_color = (int(30 + alpha * 0.4), int(10 + alpha * 0.2), int(70 + alpha * 0.7))
        draw.ellipse([w//2 - rad, h//2 - rad - 20, w//2 + rad, h//2 + rad - 20], outline=glow_color, width=4)

    # Hunter silhouette
    cx, cy = w // 2, h // 2 + 10
    # Head & hair
    draw.polygon([(cx, cy - 70), (cx - 20, cy - 35), (cx + 20, cy - 35)], fill=(20, 25, 40))
    # Shoulders & coat
    draw.polygon([(cx - 80, cy + 90), (cx + 80, cy + 90), (cx + 35, cy - 30), (cx - 35, cy - 30)], fill=(18, 22, 38))
    # Dagger blade glow
    draw.line([(cx + 45, cy + 20), (cx + 110, cy - 40)], fill=(0, 220, 255), width=6)
    draw.line([(cx + 45, cy + 20), (cx + 110, cy - 40)], fill=(255, 255, 255), width=2)
    # Purple eye glow
    draw.ellipse([cx - 8, cy - 45, cx - 2, cy - 41], fill=(160, 80, 255))
    draw.ellipse([cx + 2, cy - 45, cx + 8, cy - 41], fill=(160, 80, 255))

    # Badge COMIC / WEBTOON
    f_badge = get_font(18, bold=True)
    draw.rounded_rectangle([30, 30, 190, 65], radius=8, fill=(235, 75, 40))
    draw.text((45, 37), "WEBTOON 2026", fill=(255, 255, 255), font=f_badge)

    # Title box at bottom
    draw.rectangle([0, h - 220, w, h], fill=(12, 14, 24))
    draw.line([(0, h - 220), (w, h - 220)], fill=(0, 200, 255), width=3)

    f_title1 = get_font(30, bold=True)
    f_title2 = get_font(22, bold=True)
    f_sub = get_font(16)

    draw.text((30, h - 195), "THỢ SĂN HẦM NGỤC", fill=(255, 255, 255), font=f_title1)
    draw.text((30, h - 150), "KẺ THỨC TỈNH CUỐI CÙNG", fill=(0, 215, 255), font=f_title2)
    draw.text((30, h - 105), "Tác giả: Kiếm Vũ  |  Minh họa: Studio HiTranquility", fill=(170, 180, 200), font=f_sub)
    draw.text((30, h - 75), "Huyền huyễn - Phiêu lưu - Hành động đỉnh cao", fill=(130, 145, 170), font=f_sub)

    img.save(filepath, "PNG")

def create_comic_cover_2(filepath):
    """Bìa cho Hoàng Tử Mèo"""
    w, h = 600, 800
    img = Image.new("RGB", (w, h), (255, 248, 238))
    draw = ImageDraw.Draw(img)
    # Warm pastel gradient
    draw_gradient(draw, w, h, (255, 243, 230), (255, 218, 190))

    # Cute cat face silhouette in center
    cx, cy = w // 2, h // 2 - 30
    # Ears
    draw.polygon([(cx - 110, cy - 40), (cx - 150, cy - 140), (cx - 40, cy - 80)], fill=(245, 150, 75))
    draw.polygon([(cx + 110, cy - 40), (cx + 150, cy - 140), (cx + 40, cy - 80)], fill=(245, 150, 75))
    # Inner ears
    draw.polygon([(cx - 105, cy - 50), (cx - 135, cy - 120), (cx - 55, cy - 80)], fill=(255, 190, 180))
    draw.polygon([(cx + 105, cy - 50), (cx + 135, cy - 120), (cx + 55, cy - 80)], fill=(255, 190, 180))
    # Head circle
    draw.ellipse([cx - 130, cy - 80, cx + 130, cy + 120], fill=(250, 160, 85))

    # Golden crown on head
    draw.polygon([(cx - 50, cy - 85), (cx - 60, cy - 145), (cx - 20, cy - 105), (cx, cy - 155), (cx + 20, cy - 105), (cx + 60, cy - 145), (cx + 50, cy - 85)], fill=(255, 205, 40), outline=(220, 160, 20), width=2)
    # Big sparkling eyes
    draw.ellipse([cx - 70, cy - 20, cx - 20, cy + 30], fill=(40, 40, 50))
    draw.ellipse([cx + 20, cy - 20, cx + 70, cy + 30], fill=(40, 40, 50))
    draw.ellipse([cx - 55, cy - 12, cx - 35, cy + 8], fill=(255, 255, 255))
    draw.ellipse([cx + 35, cy - 12, cx + 55, cy + 8], fill=(255, 255, 255))
    # Cute nose and mouth
    draw.polygon([(cx - 8, cy + 40), (cx + 8, cy + 40), (cx, cy + 48)], fill=(235, 100, 120))
    draw.arc([cx - 25, cy + 42, cx, cy + 62], 0, 180, fill=(60, 50, 40), width=3)
    draw.arc([cx, cy + 42, cx + 25, cy + 62], 0, 180, fill=(60, 50, 40), width=3)

    # Badge COMIC
    f_badge = get_font(18, bold=True)
    draw.rounded_rectangle([30, 30, 190, 65], radius=8, fill=(255, 120, 130))
    draw.text((45, 37), "MANGA HÀI HƯỚC", fill=(255, 255, 255), font=f_badge)

    # Bottom title banner
    draw.rectangle([0, h - 210, w, h], fill=(255, 255, 255))
    draw.line([(0, h - 210), (w, h - 210)], fill=(250, 160, 85), width=4)

    f_title1 = get_font(26, bold=True)
    f_title2 = get_font(22, bold=True)
    f_sub = get_font(16)

    draw.text((30, h - 185), "CUỘC SỐNG THƯỜNG NHẬT", fill=(70, 50, 40), font=f_title1)
    draw.text((30, h - 145), "CỦA HOÀNG TỬ MÈO 🐾", fill=(240, 120, 40), font=f_title2)
    draw.text((30, h - 100), "Tác giả: Mộc Miên  |  Đồ hoạ màu sắc sinh động", fill=(120, 100, 90), font=f_sub)
    draw.text((30, h - 70), "Thể loại: Hài hước - Học đường - Đời thường", fill=(150, 130, 120), font=f_sub)

    img.save(filepath, "PNG")

def create_magic_cover(filepath):
    """Bìa truyện chữ Toàn Chức Pháp Sư"""
    w, h = 600, 800
    img = Image.new("RGB", (w, h), (10, 20, 35))
    draw = ImageDraw.Draw(img)
    draw_gradient(draw, w, h, (10, 25, 45), (35, 15, 60))

    # Magic circles
    cx, cy = w // 2, h // 2 - 30
    for r in [180, 150, 120, 80]:
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(60, 180, 255), width=2)
    # Magic stars
    for angle in range(0, 360, 45):
        rad = math.radians(angle)
        x1 = cx + int(150 * math.cos(rad))
        y1 = cy + int(150 * math.sin(rad))
        draw.line([(cx, cy), (x1, y1)], fill=(120, 100, 240), width=2)

    # Lighting bolts
    draw.line([(cx - 100, cy - 140), (cx - 40, cy - 20), (cx - 10, cy + 60)], fill=(255, 230, 100), width=3)
    draw.line([(cx + 100, cy - 140), (cx + 40, cy - 20), (cx + 10, cy + 60)], fill=(255, 90, 60), width=3)

    # Title box
    draw.rectangle([0, h - 210, w, h], fill=(8, 14, 25))
    draw.line([(0, h - 210), (w, h - 210)], fill=(60, 180, 255), width=3)

    f_title1 = get_font(28, bold=True)
    f_title2 = get_font(22, bold=True)
    f_sub = get_font(16)

    draw.text((30, h - 185), "TOÀN CHỨC PHÁP SƯ", fill=(255, 255, 255), font=f_title1)
    draw.text((30, h - 145), "THẦN CẤP KHỞI ĐẦU ⚡🔥", fill=(255, 200, 50), font=f_title2)
    draw.text((30, h - 100), "Tác giả: Hải Dương  |  Ma Pháp - Huyền Huyễn", fill=(160, 190, 220), font=f_sub)
    draw.text((30, h - 70), "Song hệ ma pháp thức tỉnh, hành trình xưng bá dị giới!", fill=(130, 155, 180), font=f_sub)

    img.save(filepath, "PNG")

def create_reborn_cover(filepath):
    """Bìa truyện chữ Ám Dạ Trùng Sinh"""
    w, h = 600, 800
    img = Image.new("RGB", (w, h), (30, 10, 18))
    draw = ImageDraw.Draw(img)
    draw_gradient(draw, w, h, (40, 12, 22), (20, 8, 14))

    # Classical flower and moon motif
    cx, cy = w // 2, h // 2 - 40
    # Crescent moon
    draw.ellipse([cx - 110, cy - 110, cx + 110, cy + 110], fill=(255, 240, 200))
    draw.ellipse([cx - 80, cy - 130, cx + 120, cy + 90], fill=(32, 11, 20))

    # Red spider lily petals
    for i in range(12):
        rad = math.radians(i * 30)
        px = cx + int(120 * math.cos(rad))
        py = cy + int(120 * math.sin(rad))
        draw.line([(cx, cy), (px, py)], fill=(220, 50, 70), width=2)

    # Title box
    draw.rectangle([0, h - 210, w, h], fill=(22, 8, 14))
    draw.line([(0, h - 210), (w, h - 210)], fill=(230, 80, 100), width=3)

    f_title1 = get_font(28, bold=True)
    f_title2 = get_font(22, bold=True)
    f_sub = get_font(16)

    draw.text((30, h - 185), "ÁM DẠ TRÙNG SINH", fill=(255, 255, 255), font=f_title1)
    draw.text((30, h - 145), "VƯƠNG PHI NGHỊCH THIÊN 🌸", fill=(255, 110, 130), font=f_title2)
    draw.text((30, h - 100), "Tác giả: Thủy Tiên  |  Trùng Sinh - Cổ Đại - Y Thuật", fill=(220, 170, 180), font=f_sub)
    draw.text((30, h - 70), "Một bước lên cửu ngũ chí tôn, ngạo thị thiên hạ!", fill=(180, 140, 150), font=f_sub)

    img.save(filepath, "PNG")

def generate_webtoon_panel(story_id, chapter_id, page_no, total_pages, title, scene_text, dialogues, mood="dark"):
    """
    Sinh một trang ảnh Webtoon khổ dọc chuẩn 800x1200 px.
    mood: "dark" (hầm ngục, chiến đấu), "light" (hài hước, tươi sáng), "magic" (huyền bí), "climax" (cao trào VIP)
    """
    w, h = 800, 1200
    img = Image.new("RGB", (w, h), (18, 20, 28))
    draw = ImageDraw.Draw(img)

    f_title = get_font(24, bold=True)
    f_sub = get_font(17, bold=True)
    f_dialogue = get_font(16)
    f_page = get_font(15, bold=True)
    f_sound = get_font(28, bold=True)

    # 1. Background tone based on mood
    if mood == "dark":
        draw_gradient(draw, w, h, (14, 18, 30), (28, 22, 45))
        border_col = (50, 70, 110)
    elif mood == "magic":
        draw_gradient(draw, w, h, (10, 25, 42), (40, 15, 65))
        border_col = (0, 190, 240)
    elif mood == "climax":
        draw_gradient(draw, w, h, (35, 12, 18), (18, 14, 28))
        border_col = (235, 65, 75)
    else: # light / comedy
        draw_gradient(draw, w, h, (255, 246, 235), (250, 230, 215))
        border_col = (245, 170, 110)

    # 2. Header banner on page 1
    if page_no == 1:
        head_bg = (10, 12, 20) if mood != "light" else (255, 255, 255)
        text_col = (255, 255, 255) if mood != "light" else (40, 40, 40)
        accent_col = (0, 200, 255) if mood != "light" else (245, 120, 40)

        draw.rectangle([0, 0, w, 110], fill=head_bg)
        draw.line([(0, 110), (w, 110)], fill=accent_col, width=3)
        draw.text((40, 25), title.upper(), fill=text_col, font=f_title)
        draw.text((40, 65), f"CHƯƠNG {chapter_id} • TRANG {page_no}/{total_pages}", fill=accent_col, font=f_sub)
    else:
        # Smaller top bar
        draw.rectangle([0, 0, w, 40], fill=(12, 15, 22) if mood != "light" else (250, 240, 230))
        draw.text((40, 10), f"Trang {page_no} / {total_pages}", fill=(160, 175, 195) if mood != "light" else (120, 110, 100), font=f_page)

    # 3. Comic Panels (Upper panel and Lower panel)
    panel1_y1 = 130 if page_no == 1 else 60
    panel1_y2 = panel1_y1 + 450
    panel2_y1 = panel1_y2 + 30
    panel2_y2 = h - 60

    # Panel 1: Main scene
    p1_bg = (20, 26, 42) if mood != "light" else (255, 255, 255)
    draw.rounded_rectangle([40, panel1_y1, w - 40, panel1_y2], radius=12, fill=p1_bg, outline=border_col, width=3)

    # Action lines or background motif inside panel 1
    if mood == "dark":
        for ly in range(panel1_y1 + 10, panel1_y2 - 10, 30):
            draw.line([(45, ly), (w - 45, ly + 15)], fill=(28, 35, 55), width=1)
        # Glowing eye / weapon
        draw.ellipse([w//2 - 60, panel1_y1 + 140, w//2 + 60, panel1_y1 + 260], fill=(30, 40, 68), outline=(0, 220, 255), width=3)
        draw.line([(w//2 - 120, panel1_y1 + 240), (w//2 + 120, panel1_y1 + 160)], fill=(0, 230, 255), width=4)
    elif mood == "magic":
        cx, cy = w // 2, (panel1_y1 + panel1_y2) // 2
        for rad in [140, 100, 60]:
            draw.ellipse([cx - rad, cy - rad, cx + rad, cy + rad], outline=(100, 200, 255), width=2)
    elif mood == "climax":
        # Red aura
        cx, cy = w // 2, (panel1_y1 + panel1_y2) // 2
        for r in range(160, 40, -20):
            draw.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(220, 40, 60), width=3)
    else: # light / cat
        # Cute cat ears in panel 1
        cx, cy = w // 2, (panel1_y1 + panel1_y2) // 2
        draw.polygon([(cx - 80, cy - 30), (cx - 120, cy - 110), (cx - 20, cy - 60)], fill=(245, 160, 80))
        draw.polygon([(cx + 80, cy - 30), (cx + 120, cy - 110), (cx + 20, cy - 60)], fill=(245, 160, 80))
        draw.ellipse([cx - 100, cy - 60, cx + 100, cy + 100], fill=(250, 180, 110))

    # Narrative caption box in panel 1
    cap_bg = (10, 12, 18) if mood != "light" else (255, 235, 215)
    cap_txt_col = (240, 240, 240) if mood != "light" else (60, 50, 40)
    draw.rounded_rectangle([70, panel1_y1 + 25, w - 70, panel1_y1 + 105], radius=8, fill=cap_bg, outline=border_col, width=2)
    scene_lines = scene_text.split("\n")
    for s_idx, s_line in enumerate(scene_lines[:2]):
        draw.text((90, panel1_y1 + 35 + s_idx * 28), s_line, fill=cap_txt_col, font=f_sub)

    # Sound effect (onomatopoeia)
    sound_col = (255, 70, 90) if mood != "light" else (240, 100, 50)
    sfx = "XOẸT!!!" if mood == "dark" else ("OÀNHH!!" if mood == "magic" else ("BÙM!!!" if mood == "climax" else "MEOWW~"))
    draw.text((w - 240, panel1_y2 - 80), sfx, fill=sound_col, font=f_sound)

    # Panel 2: Dialogue & Reaction Scene
    p2_bg = (24, 28, 44) if mood != "light" else (255, 255, 255)
    draw.rounded_rectangle([40, panel2_y1, w - 40, panel2_y2], radius=12, fill=p2_bg, outline=border_col, width=3)

    # Speech bubbles inside panel 2
    if dialogues and len(dialogues) >= 1:
        # First bubble
        draw_speech_bubble(draw, 80, panel2_y1 + 40, w - 200, 110, dialogues[0], f_dialogue,
                           bg=(255, 255, 255) if mood != "light" else (255, 250, 240),
                           border=(0, 180, 230) if mood != "light" else (230, 140, 70),
                           tail_pos="bottom")
    if dialogues and len(dialogues) >= 2:
        # Second bubble (reply)
        draw_speech_bubble(draw, 140, panel2_y1 + 220, w - 200, 120, dialogues[1], f_dialogue,
                           bg=(240, 248, 255) if mood != "light" else (245, 255, 245),
                           border=(180, 90, 240) if mood != "light" else (100, 180, 100),
                           tail_pos="top")

    # Bottom footer indicator
    draw.rectangle([0, h - 45, w, h], fill=(12, 14, 20) if mood != "light" else (240, 230, 220))
    draw.text((w // 2 - 80, h - 35), f"— Trang {page_no}/{total_pages} —", fill=(140, 160, 190) if mood != "light" else (120, 110, 100), font=f_page)

    # Ensure output dir exists
    out_dir = os.path.join(BASE_UPLOAD, "pages", str(story_id), str(chapter_id))
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, f"page_{page_no}.png")
    img.save(out_file, "PNG")
    rel_path = f"pages/{story_id}/{chapter_id}/page_{page_no}.png"
    return rel_path, w, h

print("Starting comic image generation...")

# Generate Covers
create_comic_cover_1(os.path.join(COVERS_DIR, "cover-comic-1.png"))
create_comic_cover_2(os.path.join(COVERS_DIR, "cover-comic-2.png"))
create_magic_cover(os.path.join(COVERS_DIR, "cover-magic.png"))
create_reborn_cover(os.path.join(COVERS_DIR, "cover-reborn.png"))
print("Covers generated successfully!")

# Define Comic Pages Data
# Story 10: Thợ Săn Hầm Ngục (4 chapters: 93, 94, 95, 96)
story10_chapters = {
    93: { # Chap 1
        "title": "Cửa Ải Tử Thần",
        "mood": "dark",
        "pages": [
            ("Hầm Ngục Hạng D - Hang Động Huyết Lang", "Trần Minh nắm chặt con dao găm cùn trong tay.\n'Hôm nay lại chỉ kiếm được vài viên ma tinh rác...'", ["Đội trưởng, có điều gì đó không ổn!", "Cửa vào hầm ngục... nó vừa tự đóng kín lại rồi!"]),
            ("Bẫy Kép Kích Hoạt!", "Bức tường đá rung chuyển dữ dội.\nMột cánh cổng đỏ rực xuất hiện phía sau lưng.", ["Đây không phải hầm ngục hạng D...", "Đây là HẦM NGỤC KÉP! Quái vật cấp S sắp xuất hiện!"]),
            ("Ma Thú Huyết Nhãn Thức Tỉnh", "Tiếng gầm rú xé toạc không gian hang đá.\nNhiệt độ giảm xuống âm ba mươi độ.", ["Mọi người chạy mau!", "Không kịp nữa rồi, nó chặn đường lui rồi!"]),
            ("Tuyệt Vọng Tràn Trề", "Cả đội thợ săn lần lượt ngã xuống.\nTrần Minh cố che chắn cho đồng đội yếu nhất.", ["Tại sao mình lại yếu đuối thế này?", "Nếu có cơ hội sống lại một lần nữa..."]),
            ("Khoảnh Khắc Cận Kề Cái Chết", "Móng vuốt khổng lồ giáng xuống đỉnh đầu.\nThời gian bỗng nhiên như ngừng trôi.", ["Ngươi có muốn sống không, hỡi kẻ hấp hối?", "Ai đang nói trong đầu ta vậy?!"]),
            ("Ánh Sáng Bí Ẩn", "Một thông điệp màu xanh lam bỗng lóe sáng rực rỡ.\n[Chúc mừng! Bạn đã đáp ứng đủ điều kiện bí mật.]", ["Cái gì thế này? Bảng trạng thái sao?!", "ĐÓN ĐỌC CHƯƠNG 2: THỨC TỈNH HỆ THỐNG"])
        ]
    },
    94: { # Chap 2
        "title": "Thức Tỉnh Hệ Thống Thần Cấp",
        "mood": "magic",
        "pages": [
            ("Giao Diện Hệ Thống Độc Quyền", "[Chào mừng Ký Chủ đến với Hệ Thống Thần Săn.]\nToàn bộ vết thương trên người bắt đầu hồi phục thần tốc.", ["Đây không phải ảo giác!", "Sức mạnh đang dâng trào trong từng mạch máu!"]),
            ("Nhận Thưởng Nhiệm Vụ Khởi Đầu", "[Kích hoạt gói quà tân thủ: Tốc độ +50, Sức mạnh +30]\n[Vũ khí chuyên chúc: Song Chủy Thủ Ám Dạ]", ["Cảm giác nhẹ bẫng này là sao?", "Tốc độ phản xạ của mình tăng lên gấp mười lần!"]),
            ("Phản Công Bất Ngờ", "Trần Minh bước ra từ làn khói đen u ám.\nÁnh mắt màu tím lạnh lùng nhìn thẳng vào đàn quái vật.", ["Vừa nãy các ngươi đuổi ta rất vui vẻ đúng không?", "Bây giờ đến lượt ta săn lùng các ngươi!"]),
            ("Vũ Điệu Lưỡi Dao", "Đường kiếm sắc bén xé toạc bóng tối hang sâu.\nBốn con sói đầu đàn ngã gục trong chớp mắt.", ["Nhanh quá! Thậm chí không thấy được đường vung kiếm!", "Cậu ta thật sự là thợ săn hạng E sao?!"]),
            ("Thăng Cấp Liên Tục", "[Chúc mừng Ký Chủ đạt cấp độ 5.]\n[Mở khóa kỹ năng bị động: Tàng Hình Trong Bóng Đêm]", ["Tuyệt vời! Càng hạ quái vật, mình càng mạnh hơn!", "Hầm ngục này sẽ là bàn đạp để mình vươn lên!"]),
            ("Kẻ Sống Sót Duy Nhất", "Bước chân ra khỏi hầm ngục trong sự ngỡ ngàng của hiệp hội.\nMột huyền thoại mới chính thức bắt đầu.", ["Cậu thanh niên đó là ai thế?", "HÃY ĐÓN XEM CHƯƠNG 3: QUÉT SẠCH HẦM NGỤC SÓI BĂNG"])
        ]
    },
    95: { # Chap 3
        "title": "Quét Sạch Hầm Ngục Sói Băng",
        "mood": "dark",
        "pages": [
            ("Vùng Đất Băng Giá Bất Tận", "Hầm ngục hạng C - Đỉnh Tuyết Lãnh Phong.\nTrần Minh một mình tiến vào vùng tuyết trắng.", ["Nhiệt độ âm 40 độ nhưng nhờ giáp ma pháp, mình không hề hấn gì.", "Mục tiêu hôm nay: Thu thập 50 lõi ma tinh băng."]),
            ("Bầy Sói Băng Bao Vây", "Hai mươi cặp mắt xanh biếc phát sáng trong bão tuyết.\nThủ lĩnh Sói Băng gầm lên phát động tấn công.", ["Bao vây theo chiến thuật bầy đàn sao?", "Rất tiếc, các ngươi chọn nhầm đối thủ rồi!"]),
            ("Tuyệt Kỹ: Trảm Ảnh Cuồng Phong", "Bóng đen thoắt ẩn thoắt hiện giữa màn tuyết trắng.\nTừng nhát chém chuẩn xác vào điểm yếu chí mạng.", ["Một con... năm con... mười con!", "Điểm kinh nghiệm đang tăng vọt vùn vụt!"]),
            ("Trùm Hầm Ngục Xuất Hiện: Băng Lang Vương", "Một con sói khổng lồ cao bốn mét với bộ lông gai nhọn như băng.\nKhí tức áp đảo của ma thú tiến hóa bậc cao.", ["Gầm gừ!! Khí hàn cực độ đóng băng mặt đất!", "Con quái này có chỉ số tương đương hạng B!"]),
            ("Một Đòn Đoạt Mạng", "Kích hoạt toàn bộ ma lực tích tụ vào song kiếm.\nMột nhát đâm xuyên qua tim Băng Lang Vương.", ["Kết thúc rồi!", "[Thông báo: Tiêu diệt thành công Thủ Lĩnh Hầm Ngục!]"]),
            ("Thu Hoạch Bội Thu", "Nhặt được Trang bị Hoàng Kim: Nhẫn Hàn Băng Cực Hạn.\n[Độ hiếm: Cực phẩm, Tăng 40% phòng thủ phép]", ["Lần này bán đấu giá ít nhất cũng được 200 triệu won!", "CHƯƠNG 4 (VIP): BÓNG ĐÊM THAO TÚNG"])
        ]
    },
    96: { # Chap 4 (VIP)
        "title": "Bóng Đêm Thao Túng: Khắc Tinh Của Quái Vật",
        "mood": "climax",
        "pages": [
            ("Âm Mưu Sau Lưng", "Đội trưởng hội thợ săn Mãnh Hổ dẫn người chặn đường tại lối ra.\n'Thằng nhóc hạng E nhặt được trang bị vàng, ngoan ngoãn giao ra đây!'", ["Các người muốn cướp đồ trắng trợn giữa ban ngày sao?", "Đừng trách ta không báo trước!"]),
            ("Lộ Mặt Thật Của Kẻ Phản Bội", "Năm thợ săn hạng C đồng loạt rút vũ khí vây quanh.\n'Ở trong hầm ngục này, không có camera đâu nhóc con ạ!'", ["Ha ha, chết ở đây thì hiệp hội chỉ nghĩ là do quái vật giết thôi!", "Trần Minh khẽ thở dài: Đúng là lòng người khó đoán..."]),
            ("Kích Hoạt Uy Áp Bóng Tối", "Một luồng khí đen khổng lồ bốc lên cuồn cuộn sau lưng Trần Minh.\nĐôi mắt cậu rực sáng ánh tím uy nghiêm.", ["C-Cái quái gì thế này?! Áp lực ma lực này...", "Cậu ta không phải hạng E! Cậu ta là Thợ Săn Hạng S!!"]),
            ("Kẻ Săn Mồi Đổi Vai", "Trong nháy mắt, năm tên thợ săn bị trói chặt bởi những xúc tu bóng tối.\nVũ khí của chúng gãy vụn rơi xuống đất.", ["Tha mạng! Chúng tôi xin lỗi, xin tha mạng!", "Bỏ lại toàn bộ trang bị rồi cút khỏi mắt ta!"]),
            ("Tiến Hóa Cảnh Giới Mới", "Hệ thống rung lên tiếng chuông vang dội.\n[Ký Chủ đã hoàn thành thử thách lòng dũng cảm. Đạt danh hiệu: Chúa Tể Bóng Đêm!]", ["Sức mạnh thật sự vừa mới bắt đầu thức tỉnh.", "Thế giới này, ta sẽ là người định đoạt quy tắc!"]),
            ("Hồi Kết Rực Lửa", "Hình bóng cô độc của Trần Minh sải bước dưới ánh hoàng hôn rực rỡ.\nMột kỷ nguyên mới đã mở ra cho nhân loại.", ["Cảm ơn các độc giả đã mở khóa chương VIP ủng hộ tác giả!", "MỜI ĐÓN ĐỌC CÁC CHƯƠNG TIẾP THEO..."])
        ]
    }
}

# Story 11: Cuộc Sống Thường Nhật Của Hoàng Tử Mèo (3 chapters: 97, 98, 99)
story11_chapters = {
    97: { # Chap 1
        "title": "Vị Khách Không Mời Rơi Từ Trên Trời",
        "mood": "light",
        "pages": [
            ("Lâu Đài Miêu Quốc Xa Xôi", "Đại hoàng tử Leo đang thi triển phép thuật dịch chuyển không gian.\nMột sự cố nổ tung biến chàng thành một chú mèo mướp béo múp míp!", ["Khoan đã! Tay chân hoàng tộc của ta đâu rồi?!", "Tại sao ta lại có đuôi và kêu meo meo thế này?!"]),
            ("Hạ Cánh Nơi Ban Công", "Ban công tầng 5 của cô nàng họa sĩ Hân Vy.\nMột cục bông màu vàng cam rơi bẹp dí xuống đống chậu hoa cúc.", ["Ái chà! Cái gì vừa rơi xuống vậy trời?!", "Oa, một chú mèo mướp siêu mập mạp và dễ thương!"]),
            ("Cuộc Đối Đầu Hoàng Gia", "Hân Vy bế chú mèo lên nựng nịu vào má.\nHoàng tử Leo giãy giụa kịch liệt bảo vệ tôn nghiêm.", ["To gan! Mau thả bổn vương xuống ngay kẻ thường dân kia!", "'Meo meo meo khè khè!!' (sao tiếng kêu lại thành thế này?!)"]),
            ("Tắm Rửa Là Cực Hình", "Hân Vy kéo Leo vào bồn tắm nước ấm và xà phòng thơm phức.\nHoàng tử oai phong lẫm liệt gào thét vì sợ nước.", ["Nước! Là nước đó! Trẫm không muốn tắm!", "Ngoan nào bé mèo béo, tắm xong chị cho ăn cá hồi nhé!"]),
            ("Thỏa Thuận Đình Chiến", "Ăn xong đĩa cá hồi thơm ngon, Leo cuộn tròn trên chiếc gối êm ái.\n'Tạm thời ở nhờ nhà ả này vài hôm, tìm cách giải lời nguyền sau...'", ["Meo~ (no căng bụng rồi, ngủ thôi)", "CHƯƠNG 2: HOÀNG GIA KHÔNG THÍCH PATE"])
        ]
    },
    98: { # Chap 2
        "title": "Hoàng Gia Không Thích Pate",
        "mood": "light",
        "pages": [
            ("Bữa Sáng Quý Tộc", "Sáng sớm hôm sau, Hân Vy mở một hộp pate mèo vị gà tây đặt xuống đĩa.\nLeo bước tới, khịt mũi ngửi rồi quay mông bỏ đi một cách khinh bỉ.", ["Đây là thức ăn cho gia súc à? Trẫm chỉ ăn bò Wagyu và cá tuyết!", "Ủa, chê hả mập? Hộp này 50k đó nha con!"]),
            ("Trận Chiến Giành Chiếc Ghế Sofa", "Hân Vy ngồi làm việc vẽ truyện tranh trên máy tính bảng.\nLeo nhảy phắt lên bàn phím nằm đè lên đúng nút Delete.", ["Này mập! File vẽ của tôi xóa mất một lớp rồi kìa!", "Ai bảo ngươi dám làm việc mà không thỉnh an ta trước?"]),
            ("Tập Bay Bằng Chổi Lông Gà", "Leo nhìn thấy cây chổi lông gà phe phẩy, bản năng loài mèo trỗi dậy.\nChàng hoàng tử không kìm được lao vào vồ lấy vồ để.", ["Không được vồ! Đó chỉ là cây chổi thôi mà!", "Bắt được ngươi rồi con chim quái dị kia!"]),
            ("Nỗi Nhớ Quê Nhà", "Buổi chiều tà ngắm nhìn bầu trời thành phố tấp nập xe cộ.\nLeo bùi ngùi nhớ lại phụ vương và mẫu hậu ở vương quốc.", ["Liệu mọi người có đang lo lắng tìm ta không nhỉ?", "Hân Vy khẽ vuốt ve đầu chú mèo: Đang buồn chuyện gì sao bé cưng?"]),
            ("Tình Cảm Chớm Nở", "Leo khẽ dụi đầu vào lòng bàn tay ấm áp của cô chủ nhỏ.\nCó lẽ thế giới loài người này cũng không đến nỗi tệ.", ["CHƯƠNG 3 (VIP): BÍ MẬT ĐÊM TRĂNG TRÒN"])
        ]
    },
    99: { # Chap 3 (VIP)
        "title": "Bí Mật Đêm Trăng Tròn",
        "mood": "light",
        "pages": [
            ("Ánh Trăng Huyền Ảo", "Đêm rằm tháng Tám, ánh trăng vằng vặc chiếu rọi qua khung cửa kính.\nCơ thể nhỏ bé của chú mèo Leo bỗng phát ra luồng sáng màu vàng kim rực rỡ.", ["Ma lực đang hồi phục! Thời cơ giải phong ấn đã đến!", "Hân Vy đang ngủ say trong phòng không hề hay biết."]),
            ("Biến Hình Ngoạn Mục", "Làn khói phép thuật tan biến.\nXuất hiện giữa phòng khách là một chàng hoàng tử tuấn tú khôi ngô với mái tóc vàng óng và đôi tai mèo tinh nghịch.", ["Cuối cùng cũng lấy lại được hình dạng nửa người nửa thần!", "Chỉ tiếc là ma lực chỉ duy trì được trong một canh giờ."]),
            ("Cứu Nguy Cho Cô Chủ", "Leo thấy Hân Vy ngủ quên trên bàn vẽ, gió lạnh thổi lay rèm cửa.\nChàng nhẹ nhàng bế cô về giường rồi đắp chăn cẩn thận.", ["Ngủ mà cũng bất cẩn như thế này, đúng là ngốc nghếch...", "Gương mặt lúc ngủ trông cũng đáng yêu phết đấy chứ."]),
            ("Bị Phát Hiện Bất Ngờ?!", "Hân Vy khẽ cựa mình, mở mắt mơ màng nhìn thấy bóng dáng chàng trai lạ mặt.\n'Cậu... cậu là ai vậy? Sao lại ở trong nhà tôi?!'", ["Leo giật mình lúng túng che mặt: 'Khoan đã, hãy nghe ta giải thích!'", "Bụp! Đúng lúc đó hết giờ phép thuật, chàng lại biến về thành con mèo béo!"]),
            ("Cái Kết Hài Hước", "Hân Vy dụi mắt nhìn thấy chú mèo đang nằm trên ngực mình meo meo.\n'Chắc là mình làm việc quá sức nên nằm mơ thấy hoàng tử rồi...'", ["Leo thở phào nhẹ nhõm: Suýt nữa thì lộ tẩy!", "CẢM ƠN QUÝ ĐỘC GIẢ ĐÃ MỞ KHÓA ỦNG HỘ CHƯƠNG VIP!"])
        ]
    }
}

# Generate pages for Story 10
sql_pages = []
for ch_id, ch_data in story10_chapters.items():
    pages_list = ch_data["pages"]
    tot = len(pages_list)
    for p_no, (p_title, p_scene, p_diags) in enumerate(pages_list, 1):
        rel_path, pw, ph = generate_webtoon_panel(10, ch_id, p_no, tot, p_title, p_scene, p_diags, mood=ch_data["mood"])
        sql_pages.append(f"({ch_id}, {p_no}, '{rel_path}', {pw}, {ph})")

# Generate pages for Story 11
for ch_id, ch_data in story11_chapters.items():
    pages_list = ch_data["pages"]
    tot = len(pages_list)
    for p_no, (p_title, p_scene, p_diags) in enumerate(pages_list, 1):
        rel_path, pw, ph = generate_webtoon_panel(11, ch_id, p_no, tot, p_title, p_scene, p_diags, mood=ch_data["mood"])
        sql_pages.append(f"({ch_id}, {p_no}, '{rel_path}', {pw}, {ph})")

print(f"Generated {len(sql_pages)} comic pages in total!")

# Save SQL insert statements to file
with open("database/insert_comic_pages.sql", "w", encoding="utf-8") as f:
    f.write("-- Tự động sinh dữ liệu trang truyện tranh cho chapter_pages\nUSE webdoctruyen;\n")
    f.write("INSERT INTO chapter_pages (chapter_id, page_no, image_path, width, height) VALUES\n")
    f.write(",\n".join(sql_pages) + ";\n")

print("Saved database/insert_comic_pages.sql successfully!")
