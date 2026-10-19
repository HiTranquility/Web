#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script sinh 15 ảnh bìa vector chuyên biệt (Dedicated SVG Covers)
với thiết kế sang trọng, typography chuẩn, biểu tượng đặc trưng
và cập nhật trực tiếp vào cơ sở dữ liệu MySQL.
"""

import os
import re

COVER_DIR = os.path.join(os.path.dirname(__file__), "..", "src", "main", "webapp", "assets", "images", "covers")
os.makedirs(COVER_DIR, exist_ok=True)

# Định nghĩa dữ liệu 15 ảnh bìa truyện chi tiết
COVERS = [
    {
        "filename": "cover-quy-bi.svg",
        "story_title": "Quỷ Bí Chi Chủ",
        "badge": "HUYỀN HUYỄN VICTORIA",
        "title_main": "QUỶ BÍ CHI CHỦ",
        "sub_title": "LORD OF THE MYSTERIES",
        "author": "MỘC MIÊN",
        "bg_stops": [("#0d0b14", "0%"), ("#1e132b", "45%"), ("#3b173e", "100%")],
        "accent_primary": "#e63946",
        "accent_secondary": "#e0a96d",
        "accent_glow": "#9d0208",
        "elements": """
          <!-- Crimson Moon -->
          <circle cx="300" cy="360" r="140" fill="none" stroke="#e63946" stroke-width="2" stroke-opacity="0.4" stroke-dasharray="6 6"/>
          <circle cx="300" cy="360" r="110" fill="#9d0208" fill-opacity="0.25"/>
          <path d="M260 270 Q360 360 260 450 Q320 360 260 270 Z" fill="#e63946" opacity="0.6"/>
          <!-- Gears & Mechanism -->
          <circle cx="160" cy="220" r="45" fill="none" stroke="#e0a96d" stroke-width="2.5" stroke-dasharray="10 8" opacity="0.5"/>
          <circle cx="160" cy="220" r="20" fill="none" stroke="#e0a96d" stroke-width="1.5" opacity="0.4"/>
          <circle cx="440" cy="480" r="55" fill="none" stroke="#e0a96d" stroke-width="3" stroke-dasharray="12 10" opacity="0.45"/>
          <!-- Mystic Eye -->
          <circle cx="300" cy="360" r="28" fill="#0d0b14" stroke="#e0a96d" stroke-width="2"/>
          <circle cx="300" cy="360" r="12" fill="#e63946"/>
          <circle cx="296" cy="356" r="4" fill="#ffffff"/>
          <!-- Fog Tendrils -->
          <path d="M-50 700 Q150 630 300 680 T650 660 L650 800 L-50 800 Z" fill="#07060a" opacity="0.95"/>
          <path d="M-20 740 Q200 690 400 730 T620 710 L620 800 L-20 800 Z" fill="#130c1c" opacity="0.9"/>
        """
    },
    {
        "filename": "cover-daiphung.svg",
        "story_title": "Đại Phụng Đả Canh Nhân",
        "badge": "TIÊN HIỆP TRINH THÁM",
        "title_main": "ĐẠI PHỤNG ĐẢ CANH NHÂN",
        "sub_title": "DẠ HÀNH ĐẢ CANH · TRẤN QUỐC AN BANG",
        "author": "MỘC MIÊN",
        "bg_stops": [("#071318", "0%"), ("#0e2a33", "50%"), ("#1a4958", "100%")],
        "accent_primary": "#2a9d8f",
        "accent_secondary": "#f4a261",
        "accent_glow": "#48cae4",
        "elements": """
          <!-- Imperial Moon -->
          <circle cx="300" cy="340" r="130" fill="#48cae4" fill-opacity="0.12" stroke="#2a9d8f" stroke-width="1.5"/>
          <circle cx="300" cy="340" r="90" fill="#f4a261" fill-opacity="0.2"/>
          <!-- Ancient Imperial Palace Roof -->
          <path d="M120 480 Q300 420 480 480 L490 520 L110 520 Z" fill="#071318" stroke="#f4a261" stroke-width="1.5"/>
          <path d="M160 440 Q300 390 440 440 L450 480 L150 480 Z" fill="#091b22" stroke="#2a9d8f" stroke-width="1"/>
          <path d="M220 390 Q300 350 380 390 L390 440 L210 440 Z" fill="#0d2833"/>
          <!-- Bronze Gong / Lantern Silhouette -->
          <circle cx="300" cy="270" r="40" fill="#071318" stroke="#f4a261" stroke-width="3"/>
          <circle cx="300" cy="270" r="28" fill="#f4a261" fill-opacity="0.3"/>
          <line x1="300" y1="180" x2="300" y2="230" stroke="#f4a261" stroke-width="2"/>
          <!-- Swirling Night Mist -->
          <path d="M-50 670 Q160 590 320 640 T650 620 L650 800 L-50 800 Z" fill="#040b0e" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-toantri.svg",
        "story_title": "Toàn Trí Độc Giả: Điểm Nhìn Thần Bí",
        "badge": "SIÊU PHẨM SINH TỒN",
        "title_main": "TOÀN TRÍ ĐỘC GIẢ",
        "sub_title": "OMNISCIENT READER'S VIEWPOINT",
        "author": "MỘC MIÊN",
        "bg_stops": [("#050b14", "0%"), ("#0a192f", "50%"), ("#172a45", "100%")],
        "accent_primary": "#00f0ff",
        "accent_secondary": "#64ffda",
        "accent_glow": "#0070f3",
        "elements": """
          <!-- Constellation Grid & Nodes -->
          <circle cx="300" cy="350" r="160" fill="none" stroke="#0070f3" stroke-width="1" stroke-opacity="0.3" stroke-dasharray="5 5"/>
          <circle cx="300" cy="350" r="110" fill="none" stroke="#00f0ff" stroke-width="1.5" stroke-opacity="0.4"/>
          <!-- Holographic Window Screen -->
          <rect x="180" y="270" width="240" height="150" rx="6" fill="#0a192f" fill-opacity="0.75" stroke="#00f0ff" stroke-width="1.8"/>
          <line x1="200" y1="310" x2="360" y2="310" stroke="#64ffda" stroke-width="2" stroke-linecap="round"/>
          <line x1="200" y1="335" x2="330" y2="335" stroke="#0070f3" stroke-width="2" stroke-linecap="round"/>
          <line x1="200" y1="360" x2="380" y2="360" stroke="#64ffda" stroke-width="2" stroke-linecap="round"/>
          <circle cx="390" cy="290" r="4" fill="#00f0ff"/>
          <!-- Digital Particles -->
          <circle cx="160" cy="200" r="3" fill="#64ffda"/>
          <circle cx="430" cy="240" r="2.5" fill="#00f0ff"/>
          <circle cx="140" cy="480" r="2" fill="#0070f3"/>
          <circle cx="460" cy="440" r="3.5" fill="#64ffda"/>
          <!-- Apocalyptic skyline horizon -->
          <path d="M-50 690 L120 580 L220 630 L380 560 L500 640 L650 590 L650 800 L-50 800 Z" fill="#03070d" opacity="0.95"/>
        """
    },
    {
        "filename": "cover-thoren.svg",
        "story_title": "Thợ Rèn Huyền Thoại Trong Thế Giới Game",
        "badge": "VÕNG DU ĐỈNH CAO",
        "title_main": "THỢ RÈN HUYỀN THOẠI",
        "sub_title": "OVERGEARED · BẬC THẦY RÈN ĐÚC",
        "author": "MỘC MIÊN",
        "bg_stops": [("#180905", "0%"), ("#2a110a", "45%"), ("#4a1c0d", "100%")],
        "accent_primary": "#ff5400",
        "accent_secondary": "#ffd000",
        "accent_glow": "#ff8500",
        "elements": """
          <!-- Molten Aura -->
          <circle cx="300" cy="370" r="150" fill="#ff5400" fill-opacity="0.15" stroke="#ff8500" stroke-width="1.5" stroke-dasharray="8 6"/>
          <!-- Legendary Anvil & Hammer -->
          <polygon points="220,480 380,480 350,530 250,530" fill="#180905" stroke="#ff8500" stroke-width="2"/>
          <polygon points="240,440 360,440 380,480 220,480" fill="#2a110a" stroke="#ffd000" stroke-width="2"/>
          <!-- Hammer Silhouette -->
          <polygon points="280,240 370,180 390,210 300,270" fill="#ffd000"/>
          <polygon points="240,290 310,240 295,220 225,270" fill="#ff5400"/>
          <line x1="280" y1="260" x2="160" y2="420" stroke="#ffd000" stroke-width="8" stroke-linecap="round"/>
          <!-- Forging Sparks -->
          <circle cx="280" cy="380" r="4" fill="#fff"/>
          <circle cx="320" cy="360" r="3" fill="#ffd000"/>
          <circle cx="340" cy="400" r="3.5" fill="#ff5400"/>
          <circle cx="260" cy="420" r="2.5" fill="#ffd000"/>
          <path d="M-50 680 L200 620 L400 650 L650 600 L650 800 L-50 800 Z" fill="#0d0402" opacity="0.95"/>
        """
    },
    {
        "filename": "cover-nguthu.svg",
        "story_title": "Ngự Thú Phiên Bản Toàn Dân",
        "badge": "KỶ NGUYÊN NGỰ THÚ",
        "title_main": "NGỰ THÚ TOÀN DÂN",
        "sub_title": "THỨC TỈNH THẦN THÚ VÔ HẠN",
        "author": "MỘC MIÊN",
        "bg_stops": [("#03160e", "0%"), ("#082d1c", "45%"), ("#0f4c30", "100%")],
        "accent_primary": "#10b981",
        "accent_secondary": "#ffe600",
        "accent_glow": "#00f5d4",
        "elements": """
          <!-- Summoning Array / Pentagram -->
          <circle cx="300" cy="350" r="150" fill="none" stroke="#10b981" stroke-width="2" stroke-dasharray="10 6" opacity="0.6"/>
          <circle cx="300" cy="350" r="120" fill="#00f5d4" fill-opacity="0.08" stroke="#ffe600" stroke-width="1.2"/>
          <!-- Dragon / Phoenix Silhouette Wings -->
          <path d="M300 370 Q210 260 120 310 Q200 360 270 410 Z" fill="#10b981" opacity="0.65"/>
          <path d="M300 370 Q390 260 480 310 Q400 360 330 410 Z" fill="#10b981" opacity="0.65"/>
          <!-- Core Gem / Soul Orb -->
          <circle cx="300" cy="370" r="22" fill="#ffe600" stroke="#ffffff" stroke-width="3"/>
          <circle cx="300" cy="370" r="10" fill="#00f5d4"/>
          <!-- Ancient Runes -->
          <circle cx="210" cy="270" r="4" fill="#00f5d4"/>
          <circle cx="390" cy="270" r="4" fill="#00f5d4"/>
          <circle cx="300" cy="210" r="5" fill="#ffe600"/>
          <!-- Forest Floor Silhouette -->
          <path d="M-50 670 Q150 610 320 650 T650 620 L650 800 L-50 800 Z" fill="#020d08" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-vanco.svg",
        "story_title": "Vạn Cổ Đệ Nhất Thần",
        "badge": "TIÊN HIỆP VÔ ĐỊCH",
        "title_main": "VẠN CỔ ĐỆ NHẤT THẦN",
        "sub_title": "THẬP ĐẠI THÁI CỔ HỖN ĐỘN CỰ THÚ",
        "author": "MỘC MIÊN",
        "bg_stops": [("#0c0414", "0%"), ("#1e0933", "45%"), ("#380e5e", "100%")],
        "accent_primary": "#d946ef",
        "accent_secondary": "#fbbf24",
        "accent_glow": "#a855f7",
        "elements": """
          <!-- Chaos Nebula Rings -->
          <circle cx="300" cy="350" r="160" fill="none" stroke="#d946ef" stroke-width="1.8" stroke-dasharray="14 8" opacity="0.45"/>
          <circle cx="300" cy="350" r="130" fill="#a855f7" fill-opacity="0.12" stroke="#fbbf24" stroke-width="1"/>
          <!-- Primordial Beast Eyes & Horns -->
          <path d="M220 330 Q240 310 260 330 Q240 340 220 330 Z" fill="#fbbf24"/>
          <circle cx="240" cy="328" r="3" fill="#fff"/>
          <path d="M340 330 Q360 310 380 330 Q360 340 340 330 Z" fill="#fbbf24"/>
          <circle cx="360" cy="328" r="3" fill="#fff"/>
          <!-- Spanning Chaos Claws / Clashing Lightning -->
          <line x1="300" y1="160" x2="300" y2="520" stroke="#fbbf24" stroke-width="1.5" stroke-opacity="0.6"/>
          <polygon points="292,160 308,160 304,300 296,300" fill="#d946ef"/>
          <!-- Jagged peaks -->
          <path d="M-50 670 L160 520 L300 640 L450 500 L650 670 L650 800 L-50 800 Z" fill="#06020a" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-trungsinh-dothi.svg",
        "story_title": "Trùng Sinh Đô Thị Tu Tiên",
        "badge": "ĐÔ THỊ TU TIÊN",
        "title_main": "TRÙNG SINH ĐÔ THỊ TU TIÊN",
        "sub_title": "ĐỘ KIẾP TRỞ VỀ · BẮC HUYỀN TIÊN TÔN",
        "author": "MỘC MIÊN",
        "bg_stops": [("#0b1120", "0%"), ("#1e293b", "50%"), ("#0f172a", "100%")],
        "accent_primary": "#38bdf8",
        "accent_secondary": "#facc15",
        "accent_glow": "#60a5fa",
        "elements": """
          <!-- Celestial Dragon Swirl Over City -->
          <circle cx="300" cy="320" r="140" fill="#38bdf8" fill-opacity="0.08" stroke="#facc15" stroke-width="1.5" stroke-dasharray="8 6"/>
          <!-- Golden Flying Sword Beam -->
          <polygon points="296,150 304,150 307,480 293,480" fill="#facc15"/>
          <circle cx="300" cy="150" r="12" fill="#fff" opacity="0.8"/>
          <!-- Modern Skyscraper Silhouettes -->
          <rect x="80" y="450" width="60" height="250" fill="#080d1a"/>
          <rect x="160" y="410" width="70" height="290" fill="#0f172a"/>
          <rect x="250" y="470" width="50" height="230" fill="#080d1a"/>
          <rect x="320" y="390" width="80" height="310" fill="#0f172a"/>
          <rect x="420" y="430" width="65" height="270" fill="#080d1a"/>
          <!-- Window Dots -->
          <circle cx="180" cy="440" r="2" fill="#38bdf8"/>
          <circle cx="200" cy="460" r="2" fill="#facc15"/>
          <circle cx="340" cy="420" r="2" fill="#38bdf8"/>
          <circle cx="360" cy="450" r="2" fill="#facc15"/>
        """
    },
    {
        "filename": "cover-langsuongmu.svg",
        "story_title": "Bí Ẩn Làng Sương Mù",
        "badge": "TRINH THÁM RÙNG RỢN",
        "title_main": "BÍ ẨN LÀNG SƯƠNG MÙ",
        "sub_title": "HỒ SƠ ĐIỀU TRA ĐỘI TRINH SÁT SỐ 4",
        "author": "MỘC MIÊN",
        "bg_stops": [("#091314", "0%"), ("#132425", "45%"), ("#1f3738", "100%")],
        "accent_primary": "#2dd4bf",
        "accent_secondary": "#e2e8f0",
        "accent_glow": "#5eead4",
        "elements": """
          <!-- Cold Moon & Fog Waves -->
          <circle cx="300" cy="300" r="90" fill="#e2e8f0" fill-opacity="0.15" stroke="#2dd4bf" stroke-width="1"/>
          <!-- Flashlight Beam -->
          <polygon points="120,560 480,240 520,380 140,620" fill="#5eead4" opacity="0.1"/>
          <!-- Spooky Pine Silhouettes -->
          <polygon points="120,360 80,480 160,480" fill="#050a0a"/>
          <polygon points="120,440 70,550 170,550" fill="#050a0a"/>
          <polygon points="460,340 420,460 500,460" fill="#070e0f"/>
          <polygon points="460,420 410,540 510,540" fill="#070e0f"/>
          <!-- Fog Ribbons -->
          <ellipse cx="250" cy="490" rx="200" ry="25" fill="#2dd4bf" opacity="0.2"/>
          <ellipse cx="380" cy="540" rx="180" ry="20" fill="#5eead4" opacity="0.25"/>
          <path d="M-50 670 Q180 610 320 660 T650 630 L650 800 L-50 800 Z" fill="#030707" opacity="0.97"/>
        """
    },
    {
        "filename": "cover-voluyen.svg",
        "story_title": "Võ Luyện Đỉnh Phong",
        "badge": "KIẾM HIỆP CỔ ĐIỂN",
        "title_main": "VÕ LUYỆN ĐỈNH PHONG",
        "sub_title": "ĐỘC BỘ VÕ ĐẠO · KHỔ TU BẤT DIỆT",
        "author": "MỘC MIÊN",
        "bg_stops": [("#141416", "0%"), ("#212126", "45%"), ("#303038", "100%")],
        "accent_primary": "#ef4444",
        "accent_secondary": "#fef08a",
        "accent_glow": "#b91c1c",
        "elements": """
          <!-- Crimson Martial Sun -->
          <circle cx="300" cy="340" r="120" fill="#ef4444" fill-opacity="0.35" stroke="#ef4444" stroke-width="2"/>
          <circle cx="300" cy="340" r="90" fill="#b91c1c" fill-opacity="0.5"/>
          <!-- Mystic Black Book (Hắc Thư) Floating -->
          <polygon points="230,300 290,260 370,300 310,340" fill="#141416" stroke="#fef08a" stroke-width="1.8"/>
          <polygon points="230,300 310,340 310,380 230,340" fill="#0c0c0e" stroke="#fef08a" stroke-width="1.2"/>
          <polygon points="370,300 310,340 310,380 370,340" fill="#1d1d21" stroke="#fef08a" stroke-width="1.2"/>
          <!-- Aura lines -->
          <line x1="300" y1="180" x2="300" y2="240" stroke="#fef08a" stroke-width="1.5" stroke-dasharray="4 4"/>
          <!-- Mountain Ranges -->
          <path d="M-50 680 L140 490 L270 630 L420 460 L650 680 L650 800 L-50 800 Z" fill="#09090b" opacity="0.97"/>
        """
    },
    {
        "filename": "cover-thantu-dongho.svg",
        "story_title": "Thám Tử Kính Lúp: Vụ Án Đồng Hồ Cát",
        "badge": "TRINH THÁM CỔ ĐIỂN",
        "title_main": "VỤ ÁN ĐỒNG HỒ CÁT",
        "sub_title": "THÁM TỬ KÍNH LÚP LUÂN ĐÔN 1888",
        "author": "MỘC MIÊN",
        "bg_stops": [("#1a120b", "0%"), ("#2b1d14", "45%"), ("#3d2b1f", "100%")],
        "accent_primary": "#d4a373",
        "accent_secondary": "#e9c46a",
        "accent_glow": "#faedcd",
        "elements": """
          <!-- Vintage Pocket Watch / Hourglass Rim -->
          <circle cx="300" cy="350" r="130" fill="#1a120b" stroke="#e9c46a" stroke-width="3"/>
          <circle cx="300" cy="350" r="115" fill="#2b1d14" stroke="#d4a373" stroke-width="1" stroke-dasharray="8 6"/>
          <!-- Hourglass Shape Inside -->
          <polygon points="240,280 360,280 300,350" fill="#d4a373" fill-opacity="0.3" stroke="#e9c46a" stroke-width="1.5"/>
          <polygon points="240,420 360,420 300,350" fill="#e9c46a" fill-opacity="0.4" stroke="#e9c46a" stroke-width="1.5"/>
          <!-- Magnifying Glass Handle -->
          <circle cx="380" cy="300" r="50" fill="none" stroke="#faedcd" stroke-width="4"/>
          <line x1="415" y1="335" x2="480" y2="400" stroke="#e9c46a" stroke-width="7" stroke-linecap="round"/>
          <path d="M-50 680 Q180 620 320 660 T650 630 L650 800 L-50 800 Z" fill="#0f0a06" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-daula.svg",
        "story_title": "Đấu La Chi Thần Cấp Khởi Đầu",
        "badge": "HUYỀN HUYỄN ĐẤU LA",
        "title_main": "ĐẤU LA: THẦN CẤP KHỞI ĐẦU",
        "sub_title": "VÕ HỒN SONG SINH CHẤN ĐỘNG THẦN GIỚI",
        "author": "MỘC MIÊN",
        "bg_stops": [("#14001c", "0%"), ("#290038", "45%"), ("#450a5a", "100%")],
        "accent_primary": "#c026d3",
        "accent_secondary": "#f43f5e",
        "accent_glow": "#a855f7",
        "elements": """
          <!-- Dark Fallen Angel Feathered Wings -->
          <path d="M300 360 Q180 230 80 290 Q160 370 250 430 Z" fill="#c026d3" opacity="0.6"/>
          <path d="M300 360 Q420 230 520 290 Q440 370 350 430 Z" fill="#c026d3" opacity="0.6"/>
          <!-- God-Slayer Spear (Thí Thần Thương) -->
          <polygon points="295,140 305,140 308,520 292,520" fill="#f43f5e"/>
          <polygon points="280,180 320,180 300,120" fill="#ffffff"/>
          <circle cx="300" cy="180" r="14" fill="#a855f7" stroke="#fff" stroke-width="2"/>
          <!-- Soul Rings -->
          <ellipse cx="300" cy="460" rx="140" ry="25" fill="none" stroke="#f43f5e" stroke-width="2" stroke-dasharray="6 4"/>
          <ellipse cx="300" cy="490" rx="170" ry="30" fill="none" stroke="#c026d3" stroke-width="2.5"/>
          <path d="M-50 680 L200 600 L400 640 L650 590 L650 800 L-50 800 Z" fill="#09000c" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-ydo.svg",
        "story_title": "Y Đạo Vô Song: Thần Y Hạ Sơn",
        "badge": "ĐÔ THỊ THẦN Y",
        "title_main": "Y ĐẠO VÔ SONG",
        "sub_title": "THẦN Y HẠ SƠN · CỨU NHÂN ĐỘ THẾ",
        "author": "MỘC MIÊN",
        "bg_stops": [("#041c14", "0%"), ("#0b3829", "45%"), ("#12543e", "100%")],
        "accent_primary": "#10b981",
        "accent_secondary": "#fbbf24",
        "accent_glow": "#34d399",
        "elements": """
          <!-- Bagua / Longevity Lotus Circle -->
          <circle cx="300" cy="350" r="140" fill="none" stroke="#10b981" stroke-width="2" stroke-dasharray="8 6" opacity="0.6"/>
          <circle cx="300" cy="350" r="100" fill="#10b981" fill-opacity="0.1" stroke="#fbbf24" stroke-width="1.5"/>
          <!-- Golden Acupuncture Needles -->
          <polygon points="298,170 302,170 301,470 299,470" fill="#fbbf24"/>
          <circle cx="300" cy="170" r="5" fill="#fff"/>
          <polygon points="230,220 234,220 270,440 267,440" fill="#fbbf24" opacity="0.75"/>
          <polygon points="368,220 372,220 332,440 329,440" fill="#fbbf24" opacity="0.75"/>
          <!-- Lotus Petals Silhouette -->
          <path d="M300 370 C270 330 270 410 300 430 C330 410 330 330 300 370 Z" fill="#34d399" opacity="0.8"/>
          <path d="M-50 670 Q160 620 320 660 T650 620 L650 800 L-50 800 Z" fill="#020f0a" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-phuthuy.svg",
        "story_title": "Phù Thủy Cuối Cùng Ở Học Viện Phép Thuật",
        "badge": "MA PHÁP HUYỀN BÍ",
        "title_main": "PHÙ THỦY CUỐI CÙNG",
        "sub_title": "HỌC VIỆN PHÉP THUẬT CỔ ĐẠI",
        "author": "MỘC MIÊN",
        "bg_stops": [("#0b0c26", "0%"), ("#161947", "45%"), ("#262a6e", "100%")],
        "accent_primary": "#818cf8",
        "accent_secondary": "#c084fc",
        "accent_glow": "#a5b4fc",
        "elements": """
          <!-- Runic Magic Circle -->
          <circle cx="300" cy="350" r="150" fill="none" stroke="#818cf8" stroke-width="1.8" stroke-dasharray="10 8" opacity="0.5"/>
          <circle cx="300" cy="350" r="115" fill="#262a6e" fill-opacity="0.3" stroke="#c084fc" stroke-width="1.2"/>
          <!-- Floating Grimoire / Ancient Spellbook -->
          <polygon points="220,340 300,320 380,340 380,410 300,390 220,410" fill="#0b0c26" stroke="#c084fc" stroke-width="2"/>
          <line x1="300" y1="320" x2="300" y2="390" stroke="#818cf8" stroke-width="2"/>
          <!-- Floating Magic Wand & Sparks -->
          <line x1="180" y1="230" x2="320" y2="310" stroke="#c084fc" stroke-width="4" stroke-linecap="round"/>
          <circle cx="180" cy="230" r="10" fill="#fff" opacity="0.9"/>
          <!-- Magic Tower Spire Silhouette -->
          <polygon points="300,160 280,260 320,260" fill="#07071a"/>
          <path d="M-50 680 L180 590 L350 630 L520 570 L650 660 L650 800 L-50 800 Z" fill="#040513" opacity="0.96"/>
        """
    },
    {
        "filename": "cover-phong404.svg",
        "story_title": "Căn Phòng Số 404: Ám Ảnh Ký Túc Xá",
        "badge": "KINH DỊ HỌC ĐƯỜNG",
        "title_main": "CĂN PHÒNG SỐ 404",
        "sub_title": "ÁM ẢNH KÝ TÚC XÁ 15 NĂM NIÊM PHONG",
        "author": "MỘC MIÊN",
        "bg_stops": [("#160303", "0%"), ("#2b0808", "45%"), ("#450e0e", "100%")],
        "accent_primary": "#dc2626",
        "accent_secondary": "#fef08a",
        "accent_glow": "#f87171",
        "elements": """
          <!-- Blood Red Aura & Corridor Perspective -->
          <polygon points="60,180 540,180 430,560 170,560" fill="#0f0202" stroke="#dc2626" stroke-width="1.5" opacity="0.8"/>
          <!-- Heavy Weathered Door -->
          <rect x="230" y="240" width="140" height="260" fill="#1c0505" stroke="#f87171" stroke-width="2"/>
          <text x="300" y="320" font-family="'Plus Jakarta Sans', system-ui, sans-serif" font-size="28" font-weight="900" fill="#fef08a" text-anchor="middle">404</text>
          <!-- Crime Scene Caution Tapes (X crossing) -->
          <line x1="200" y1="330" x2="400" y2="430" stroke="#fef08a" stroke-width="6" stroke-opacity="0.7"/>
          <line x1="200" y1="430" x2="400" y2="330" stroke="#fef08a" stroke-width="6" stroke-opacity="0.7"/>
          <!-- Floor Blood Stain -->
          <ellipse cx="300" cy="560" rx="90" ry="18" fill="#dc2626" opacity="0.6"/>
          <path d="M-50 670 Q160 610 320 650 T650 620 L650 800 L-50 800 Z" fill="#0a0101" opacity="0.97"/>
        """
    },
    {
        "filename": "cover-baochua.svg",
        "story_title": "Trở Thành Bạo Chúa Trong Tiểu Thuyết Xuyên Không",
        "badge": "HOÀNG GIA TRỌNG SINH",
        "title_main": "TRỞ THÀNH BẠO CHÚA",
        "sub_title": "HOÀNG ĐẾ TRONG TIỂU THUYẾT LÃNG MẠN",
        "author": "MỘC MIÊN",
        "bg_stops": [("#1f0310", "0%"), ("#380720", "45%"), ("#570c32", "100%")],
        "accent_primary": "#be185d",
        "accent_secondary": "#fde047",
        "accent_glow": "#f43f5e",
        "elements": """
          <!-- Imperial Gilded Crown Silhouette -->
          <polygon points="220,380 230,300 260,340 300,280 340,340 370,300 380,380" fill="#fde047" stroke="#fff" stroke-width="2"/>
          <circle cx="300" cy="275" r="8" fill="#be185d"/>
          <circle cx="230" cy="295" r="6" fill="#be185d"/>
          <circle cx="370" cy="295" r="6" fill="#be185d"/>
          <!-- Royal Thorn & Roses Motif -->
          <path d="M200 430 Q300 480 400 430" fill="none" stroke="#fde047" stroke-width="2.5"/>
          <circle cx="250" cy="445" r="10" fill="#be185d" stroke="#fde047" stroke-width="1.5"/>
          <circle cx="350" cy="445" r="10" fill="#be185d" stroke="#fde047" stroke-width="1.5"/>
          <!-- Royal Palace Chandelier Silhouette -->
          <circle cx="300" cy="180" r="40" fill="none" stroke="#fde047" stroke-width="1.5" stroke-dasharray="6 4"/>
          <line x1="300" y1="120" x2="300" y2="180" stroke="#fde047" stroke-width="2"/>
          <path d="M-50 670 Q160 610 320 650 T650 620 L650 800 L-50 800 Z" fill="#0d0106" opacity="0.96"/>
        """
    }
]

def generate_svg(item):
    bg_stops_svg = "\n".join([f'      <stop offset="{pct}" stop-color="{col}"/>' for col, pct in item["bg_stops"]])
    svg = f"""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 800" width="100%" height="100%">
  <defs>
    <linearGradient id="bg-grad" x1="0%" y1="0%" x2="100%" y2="100%">
{bg_stops_svg}
    </linearGradient>
    <linearGradient id="gold-text" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="50%" stop-color="{item['accent_secondary']}"/>
      <stop offset="100%" stop-color="{item['accent_primary']}"/>
    </linearGradient>
    <radialGradient id="center-glow" cx="50%" cy="42%" r="55%">
      <stop offset="0%" stop-color="{item['accent_glow']}" stop-opacity="0.38"/>
      <stop offset="60%" stop-color="{item['accent_primary']}" stop-opacity="0.12"/>
      <stop offset="100%" stop-color="#000" stop-opacity="0"/>
    </radialGradient>
  </defs>

  <!-- Background Base -->
  <rect width="600" height="800" fill="url(#bg-grad)"/>
  <rect width="600" height="800" fill="url(#center-glow)"/>

  <!-- Ornate Outer Border -->
  <rect x="20" y="20" width="560" height="760" rx="8" fill="none" stroke="{item['accent_secondary']}" stroke-width="1.2" stroke-opacity="0.35"/>
  <rect x="26" y="26" width="548" height="748" rx="6" fill="none" stroke="{item['accent_primary']}" stroke-width="0.8" stroke-opacity="0.25"/>

  <!-- Top Badge -->
  <rect x="180" y="46" width="240" height="30" rx="15" fill="{item['accent_primary']}" fill-opacity="0.18" stroke="{item['accent_primary']}" stroke-width="1.2"/>
  <text x="300" y="66" font-family="'Plus Jakarta Sans', system-ui, sans-serif" font-size="12" font-weight="700" fill="{item['accent_secondary']}" text-anchor="middle" letter-spacing="3">{item['badge']}</text>

  <!-- Thematic Vector Elements -->
{item['elements']}

  <!-- Title & Typography -->
  <text x="300" y="640" font-family="'Lora', 'Playfair Display', Georgia, serif" font-size="28" font-weight="700" fill="url(#gold-text)" text-anchor="middle" letter-spacing="1">{item['title_main']}</text>
  <text x="300" y="675" font-family="'Plus Jakarta Sans', system-ui, sans-serif" font-size="13" font-weight="600" fill="#9aa3b5" text-anchor="middle" letter-spacing="3">{item['sub_title']}</text>

  <!-- Accent Divider -->
  <line x1="200" y1="702" x2="400" y2="702" stroke="{item['accent_primary']}" stroke-width="2" stroke-opacity="0.6"/>

  <!-- Author Signature -->
  <text x="300" y="738" font-family="'Plus Jakarta Sans', system-ui, sans-serif" font-size="14" font-weight="600" fill="#e8eaf0" text-anchor="middle" letter-spacing="2">TÁC GIẢ: {item['author']}</text>
</svg>
"""
    return svg

def main():
    import sys
    if sys.platform.startswith('win'):
        import io
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    
    print(f"Creating 15 dedicated SVG covers in {COVER_DIR} ...")
    for item in COVERS:
        filepath = os.path.join(COVER_DIR, item["filename"])
        content = generate_svg(item)
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"  + Generated: {item['filename']} for '{item['story_title']}'")

    print("\nAll 15 dedicated SVG covers created successfully!")

if __name__ == "__main__":
    main()

