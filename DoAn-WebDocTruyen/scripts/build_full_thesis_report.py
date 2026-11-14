import os
import sys
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

sys.stdout.reconfigure(encoding='utf-8')

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMG_DIR = os.path.join(ROOT_DIR, 'docs', 'report_images')
OUTPUT_DOCX = os.path.join(ROOT_DIR, 'BaoCao_TongHop_DoAn_WebDocTruyen.docx')

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=70, bottom=70, left=110, right=110):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'''
        <w:tcMar {nsdecls("w")}>
            <w:top w:w="{top}" w:type="dxa"/>
            <w:bottom w:w="{bottom}" w:type="dxa"/>
            <w:left w:w="{left}" w:type="dxa"/>
            <w:right w:w="{right}" w:type="dxa"/>
        </w:tcMar>
    ''')
    tcPr.append(tcMar)

def build_thesis_report():
    doc = Document()

    # Page Margins (0.75 in)
    for section in doc.sections:
        section.top_margin = Inches(0.75)
        section.bottom_margin = Inches(0.75)
        section.left_margin = Inches(0.8)
        section.right_margin = Inches(0.8)

    # Colors
    C_NAVY = RGBColor(30, 58, 138)       # #1E3A8A - Title & H1
    C_GOLD = RGBColor(180, 83, 9)        # #B45309 - H2 / Accent
    C_TEXT = RGBColor(31, 41, 55)        # #1F2937 - Body
    C_MUTED = RGBColor(100, 116, 139)    # #64748B - Caption / Meta
    C_LINK = RGBColor(2, 132, 199)       # #0284C7 - Link
    C_SUCCESS = RGBColor(16, 149, 93)    # Green

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(16)
        p.paragraph_format.space_after = Pt(5)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = "Arial"
        run.font.size = Pt(14)
        run.font.bold = True
        run.font.color.rgb = C_NAVY
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(11)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = "Arial"
        run.font.size = Pt(11)
        run.font.bold = True
        run.font.color.rgb = C_GOLD
        return p

    def add_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = "Arial"
        run.font.size = Pt(10)
        run.font.bold = True
        run.font.color.rgb = C_TEXT
        return p

    def add_body(text, bold_prefix=None, italic=False):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(3.5)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            rb = p.add_run(bold_prefix)
            rb.font.name = "Arial"
            rb.font.size = Pt(9.5)
            rb.font.bold = True
            rb.font.color.rgb = C_TEXT
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(9.5)
        r.font.italic = italic
        r.font.color.rgb = C_TEXT
        return p

    def add_bullet(text, bold_prefix=None):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(2.5)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            rb = p.add_run(bold_prefix)
            rb.font.name = "Arial"
            rb.font.size = Pt(9.5)
            rb.font.bold = True
            rb.font.color.rgb = C_TEXT
        r = p.add_run(text)
        r.font.name = "Arial"
        r.font.size = Pt(9.5)
        r.font.color.rgb = C_TEXT
        return p

    def add_image_box(filename, caption, width_in=6.2):
        # Look in IMG_DIR or docs/diagrams
        full_path = os.path.join(IMG_DIR, filename)
        if not os.path.exists(full_path):
            full_path = os.path.join(ROOT_DIR, 'docs', 'diagrams', filename)
        if os.path.exists(full_path):
            p_img = doc.add_paragraph()
            p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_img.paragraph_format.space_before = Pt(6)
            p_img.paragraph_format.space_after = Pt(2)
            p_img.paragraph_format.keep_with_next = True
            run = p_img.add_run()
            run.add_picture(full_path, width=Inches(width_in))
            
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_cap.paragraph_format.space_after = Pt(8)
            r_cap = p_cap.add_run(f"Hình: {caption}")
            r_cap.font.name = "Arial"
            r_cap.font.size = Pt(8.8)
            r_cap.font.italic = True
            r_cap.font.color.rgb = C_MUTED
        else:
            add_body(f"[Ảnh minh họa: {filename} - Không tìm thấy]", italic=True)

    # =============================================================
    # TRANG TIÊU ĐỀ & THÔNG TIN ĐỒ ÁN
    # =============================================================
    p_header = doc.add_paragraph()
    p_header.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_header.paragraph_format.space_before = Pt(4)
    p_header.paragraph_format.space_after = Pt(2)
    run_inst = p_header.add_run("BÁO CÁO TỔNG HỢP TOÀN BỘ ĐỒ ÁN CHUYÊN NGÀNH CNTT")
    run_inst.font.name = "Arial"
    run_inst.font.size = Pt(11)
    run_inst.font.bold = True
    run_inst.font.color.rgb = C_MUTED

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(4)
    p_title.paragraph_format.space_after = Pt(4)
    run_title = p_title.add_run("HỆ THỐNG WEBSITE ĐỌC TRUYỆN TRỰC TUYẾN ĐA NỀN TẢNG\n(WEBTOON & NOVEL PLATFORM — HI TRANQUILITY)")
    run_title.font.name = "Arial"
    run_title.font.size = Pt(16.5)
    run_title.font.bold = True
    run_title.font.color.rgb = C_NAVY

    # Metadata Table
    meta_table = doc.add_table(rows=7, cols=2)
    meta_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    meta_items = [
        ("Sinh viên thực hiện:", "Phát"),
        ("Mã Nguồn Dự Án (GitHub Link):", "https://github.com/HiTranquility/Web (Nhánh main)"),
        ("Kiến Trúc & Công Nghệ Lõi:", "Java Servlet 3.1, JSP 2.3 (MVC Model 2), MySQL 8.0 InnoDB, HikariCP 4.0.3"),
        ("Frontend & Trải Nghiệm:", "Vanilla CSS 4 Tầng Design Tokens, Vanilla JS Hướng sự kiện, PWA Service Worker v4"),
        ("Phân Quyền & Kinh Tế Ảo:", "Dynamic RBAC 7 Vai Trò, Hệ Thống Ví Xu ACID Khóa Bi Quan & Mở Khóa VIP"),
        ("Quy Mô Mã Nguồn Thực Tế:", "88 Lớp Java, 61 File JSP (38 Trang Chức Năng, 5 Layouts), 25 Bảng CSDL"),
        ("Kiểm Thử Tự Động (Unit Tests):", "238/238 Tests PASS (100% Pass Rate trên 55 Test Containers)"),
    ]
    for idx, (label, val) in enumerate(meta_items):
        row = meta_table.rows[idx]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(2.3)
        c1.width = Inches(4.5)
        c0.text = label
        c0.paragraphs[0].runs[0].font.name = "Arial"
        c0.paragraphs[0].runs[0].font.bold = True
        c0.paragraphs[0].runs[0].font.size = Pt(9)
        c1.text = val
        c1.paragraphs[0].runs[0].font.name = "Arial"
        c1.paragraphs[0].runs[0].font.size = Pt(9)
        if "github.com" in val:
            c1.paragraphs[0].runs[0].font.bold = True
            c1.paragraphs[0].runs[0].font.color.rgb = C_LINK
        set_cell_background(c0, "F1F5F9")
        set_cell_background(c1, "F8FAFC" if idx % 2 == 0 else "FFFFFF")
        set_cell_margins(c0, 45, 45, 75, 75)
        set_cell_margins(c1, 45, 45, 75, 75)

    doc.add_paragraph().paragraph_format.space_after = Pt(6)

    # =============================================================
    # CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI & QUY MÔ DỰ ÁN
    # =============================================================
    add_h1("CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI & QUY MÔ DỰ ÁN")
    add_body("Hệ thống Web Đọc Truyện Đa Nền Tảng (Hi Tranquility) là đồ án ứng dụng web quy mô lớn giải quyết bài toán đọc, sáng tác và phân phối nội dung trực tuyến gồm hai thể loại chủ đạo: Truyện chữ dài tập (Web Novel) và Truyện tranh cuộn dọc chất lượng cao (Webtoon / Comic). Hệ thống được xây dựng trên nền tảng Java Enterprise (Java EE) thuần túy với kiến trúc MVC Model 2 chuẩn mực, tuân thủ nguyên tắc không phụ thuộc framework cồng kềnh, tối ưu hiệu năng tối đa trên máy chủ và thiết bị di động.")

    p_box = doc.add_paragraph()
    p_box.paragraph_format.space_before = Pt(4)
    p_box.paragraph_format.space_after = Pt(6)
    r_lbl = p_box.add_run("🔗 ĐƯỜNG DẪN MÃ NGUỒN CHÍNH THỨC (QUAN TRỌNG ĐỂ GIẢNG VIÊN CHẤM ĐỒ ÁN):\n")
    r_lbl.font.name = "Arial"
    r_lbl.font.bold = True
    r_lbl.font.size = Pt(10)
    r_lbl.font.color.rgb = C_NAVY
    r_url = p_box.add_run("👉 https://github.com/HiTranquility/Web\n")
    r_url.font.name = "Arial"
    r_url.font.bold = True
    r_url.font.size = Pt(11.5)
    r_url.font.color.rgb = C_LINK
    r_note = p_box.add_run("(Kho mã nguồn nhánh main: Chứa toàn bộ 88 lớp Java Controllers/DAOs/Models/Filters, 61 file Views/Partials, Database Migrations, Bộ kiểm thử 238 Unit Tests, và kịch bản chạy thử tự động)")
    r_note.font.name = "Arial"
    r_note.font.size = Pt(9)
    r_note.font.italic = True
    r_note.font.color.rgb = C_MUTED

    add_h2("1.1. Đối Chiếu Mục Tiêu Đề Xuất Ban Đầu vs Hệ Thống Thực Tế")
    add_body("Qua quá trình nghiên cứu và mở rộng qua 33 Module Công nghệ (ISSUE-001 -> ISSUE-033) cùng 9 ca sửa lỗi bảo mật (bug-001 -> bug-009), quy mô hệ thống đã vượt trội hoàn toàn so với đăng ký đề tài ban đầu:")

    stat_table = doc.add_table(rows=7, cols=4)
    stat_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    s_hdr = stat_table.rows[0]
    s_hdr.cells[0].text = "Chỉ Số Định Lượng"
    s_hdr.cells[1].text = "Đề Xuất Ban Đầu"
    s_hdr.cells[2].text = "Thực Tế Hiện Tại"
    s_hdr.cells[3].text = "Tăng Trưởng & Đánh Giá"
    for c in s_hdr.cells:
        c.paragraphs[0].runs[0].font.name = "Arial"
        c.paragraphs[0].runs[0].font.bold = True
        c.paragraphs[0].runs[0].font.size = Pt(8.5)
        set_cell_background(c, "E2E8F0")
        set_cell_margins(c, 50, 50, 70, 70)

    stat_rows = [
        ("Số lượng Chức năng", "17 - 28 tính năng", "52 tính năng hoàn chỉnh", "Đạt 185% (Vượt xa đề cương)"),
        ("Module Công nghệ Nâng cao", "0 module", "33 Module (ISSUE-001 -> 033)", "Bổ sung trọn vẹn FinTech, Gamification, PWA"),
        ("Số Lớp Mã Nguồn Java", "47 lớp", "88 Lớp Java chuẩn mực", "+87% (22 Controllers, 20 DAOs, 17 Models, 6 Filters)"),
        ("Giao Diện Hiển Thị (Views)", "30 trang JSP", "61 Files JSP (38 trang, 5 layouts, 18 partials)", "+103% (Bổ sung Editor layout, Admin ví, Audit...)"),
        ("Cơ Sở Dữ Liệu", "13 bảng", "25 Bảng CSDL quan hệ", "+92% (Bổ sung 4 bảng RBAC, ví xu, review, quest)"),
        ("Kiểm Thử Tự Động (Unit Tests)", "28 bài test", "238 Bài Unit Tests (100% PASS)", "+750% (Bao phủ DAO, Filter, Token, Security)")
    ]
    for idx, (m, o, n, g) in enumerate(stat_rows):
        row = stat_table.rows[idx + 1]
        c0, c1, c2, c3 = row.cells[0], row.cells[1], row.cells[2], row.cells[3]
        c0.width, c1.width, c2.width, c3.width = Inches(1.8), Inches(1.3), Inches(1.8), Inches(1.9)
        c0.text, c1.text, c2.text, c3.text = m, o, n, g
        c0.paragraphs[0].runs[0].font.name = "Arial"
        c0.paragraphs[0].runs[0].font.bold = True
        c0.paragraphs[0].runs[0].font.size = Pt(8.5)
        c1.paragraphs[0].runs[0].font.name = "Arial"
        c1.paragraphs[0].runs[0].font.size = Pt(8.5)
        c2.paragraphs[0].runs[0].font.name = "Arial"
        c2.paragraphs[0].runs[0].font.bold = True
        c2.paragraphs[0].runs[0].font.size = Pt(8.5)
        c2.paragraphs[0].runs[0].font.color.rgb = C_NAVY
        c3.paragraphs[0].runs[0].font.name = "Arial"
        c3.paragraphs[0].runs[0].font.size = Pt(8.2)
        bg = "F8FAFC" if idx % 2 == 0 else "FFFFFF"
        set_cell_background(c0, bg)
        set_cell_background(c1, bg)
        set_cell_background(c2, bg)
        set_cell_background(c3, bg)
        set_cell_margins(c0, 45, 45, 65, 65)
        set_cell_margins(c1, 45, 45, 65, 65)
        set_cell_margins(c2, 45, 45, 65, 65)
        set_cell_margins(c3, 45, 45, 65, 65)

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # =============================================================
    # CHƯƠNG 2: KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ ÁP DỤNG
    # =============================================================
    add_h1("CHƯƠNG 2: KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ ÁP DỤNG")
    add_body("Hệ thống áp dụng mô hình kiến trúc chuẩn MVC Model 2 trên nền tảng Java Enterprise. Mỗi thành phần đảm nhiệm một vai trò độc lập, tương tác thông qua các giao diện hợp đồng chặt chẽ:")
    
    add_bullet("Model Layer: Gồm 17 Entities (POJO) biểu diễn cấu trúc dữ liệu và 20 Data Access Objects (DAO). Toàn bộ truy vấn SQL sử dụng PreparedStatement để vô hiệu hóa 100% các nguy cơ tấn công SQL Injection. Tầng dữ liệu giao tiếp với MySQL thông qua HikariCP Connection Pool hiệu năng cao.", "• ")
    add_bullet("View Layer: 61 tệp JSP chia làm 5 khung bố cục chuyên biệt (main.jsp, auth.jsp, reader.jsp, admin.jsp, editor.jsp). Toàn bộ dữ liệu hiển thị được lọc qua thẻ thư viện JSTL <c:out> để chống tấn công XSS (Cross-Site Scripting). Tái sử dụng tối đa qua 18 view partials.", "• ")
    add_bullet("Controller Layer: 22 Java Servlets tiếp nhận HTTP Request, điều phối nghiệp vụ, xác thực CSRF Token, kiểm soát phiên đăng nhập và định tuyến giao diện.", "• ")
    add_bullet("Security & Filter Layer: 6 Bộ lọc Servlet (Filters) gồm AuthFilter, AdminFilter (RBAC kiểm tra 7 vai trò và chặn 403), CsrfFilter, RateLimiterFilter (chống brute-force bằng thuật toán Sliding Window), SecurityHeadersFilter (thiết lập CSP, HSTS, X-Frame-Options, X-Content-Type-Options).", "• ")

    add_h2("2.1. Ma Trận Phân Quyền Động Đa Tầng (Dynamic RBAC — 7 Vai Trò)")
    add_body("Hệ thống không cố định vai trò mà sử dụng bảng CSDL quan hệ (roles, permissions, role_permissions, user_roles) để cấp quyền động:")
    
    rbac_table = doc.add_table(rows=8, cols=3)
    rbac_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    r_hdr = rbac_table.rows[0]
    r_hdr.cells[0].text = "Mã Vai Trò (Role Code)"
    r_hdr.cells[1].text = "Tên Vai Trò & Đối Tượng"
    r_hdr.cells[2].text = "Quyền Hạn & Ranh Giới An Toàn"
    for c in r_hdr.cells:
        c.paragraphs[0].runs[0].font.name = "Arial"
        c.paragraphs[0].runs[0].font.bold = True
        c.paragraphs[0].runs[0].font.size = Pt(8.5)
        set_cell_background(c, "E2E8F0")
        set_cell_margins(c, 50, 50, 70, 70)

    rbac_rows = [
        ("GUEST", "Khách vãng lai", "Xem danh mục, tìm kiếm, đọc chương thường miễn phí. Bị chặn bởi Auth Guard khi đọc chương yêu cầu đăng nhập."),
        ("USER", "Độc giả tiêu chuẩn", "Đọc truyện, lưu tủ sách, điểm danh nhận xu 7 ngày, làm nhiệm vụ ngày, nạp xu ví, mở khóa chương VIP, tặng hoa tác giả."),
        ("VIP_MEMBER", "Hội viên VIP", "Toàn quyền của User + hưởng đặc quyền chiết khấu từ 5% đến 25% khi mở chương VIP; nhân hệ số điểm danh x1.2 - x2.0; huy hiệu VIP lấp lánh."),
        ("VERIFIED_AUTHOR", "Tác giả xác minh", "Toàn quyền User + truy cập Author Studio (/story?action=mine), đăng truyện, xuất bản chương chữ/comic, định giá xu VIP, sao lưu Google Drive."),
        ("EDITOR", "Biên tập viên", "Quyền hỗ trợ biên tập nội dung truyện, duyệt bản thảo tác giả. Bị chặn 403 tuyệt đối tại khu vực tài chính và quản lý người dùng."),
        ("MODERATOR", "Kiểm duyệt viên", "Truy cập /admin/dashboard, xử lý khiếu nại tố cáo (/admin/report), ẩn bình luận vi phạm. BỊ CHẶN 403 NGHIÊM NGẶT tại /admin/wallet, /admin/audit, /admin/user."),
        ("SUPER_ADMIN / ADMIN", "Quản trị viên tối cao", "Toàn quyền quản trị: Quản lý người dùng, duyệt/xóa mềm truyện, thể loại, điều chỉnh ví xu (/admin/wallet), xem toàn bộ Nhật ký kiểm toán an ninh (/admin/audit).")
    ]
    for idx, (code, title, scope) in enumerate(rbac_rows):
        row = rbac_table.rows[idx + 1]
        c0, c1, c2 = row.cells[0], row.cells[1], row.cells[2]
        c0.width, c1.width, c2.width = Inches(1.8), Inches(1.8), Inches(3.2)
        c0.text, c1.text, c2.text = code, title, scope
        c0.paragraphs[0].runs[0].font.name = "Arial"
        c0.paragraphs[0].runs[0].font.bold = True
        c0.paragraphs[0].runs[0].font.size = Pt(8.5)
        c1.paragraphs[0].runs[0].font.name = "Arial"
        c1.paragraphs[0].runs[0].font.size = Pt(8.5)
        c2.paragraphs[0].runs[0].font.name = "Arial"
        c2.paragraphs[0].runs[0].font.size = Pt(8.2)
        bg = "F8FAFC" if idx % 2 == 0 else "FFFFFF"
        set_cell_background(c0, bg)
        set_cell_background(c1, bg)
        set_cell_background(c2, bg)
        set_cell_margins(c0, 45, 45, 65, 65)
        set_cell_margins(c1, 45, 45, 65, 65)
        set_cell_margins(c2, 45, 45, 65, 65)

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # Add Developer Environment Screenshots
    add_h2("2.2. Minh Chứng Môi Trường Lập Trình & Mã Nguồn Lõi")
    add_image_box("dev_vscode_architecture.png", "Cấu trúc gói mã nguồn MVC thuần Java trên IDE Visual Studio Code và mã nguồn ChapterServlet xử lý Paywall", 6.2)
    add_image_box("dev_terminal_junit_tests.png", "Cửa sổ Terminal chạy kiểm thử tự động JUnit 5 Platform Console: 238/238 Tests PASS (100% Pass Rate trên 55 Containers)", 6.2)

    # =============================================================
    # CHƯƠNG 3: THIẾT KẾ CƠ SỞ DỮ LIỆU & DATA DICTIONARY (25 BẢNG)
    # =============================================================
    add_h1("CHƯƠNG 3: THIẾT KẾ CƠ SỞ DỮ LIỆU & DATA DICTIONARY (25 BẢNG)")
    add_body("Cơ sở dữ liệu được xây dựng trên hệ quản trị MySQL 8.0 với Storage Engine InnoDB đảm bảo tính toàn vẹn khóa ngoại (Foreign Keys) và hỗ trợ giao dịch ACID. Toàn bộ bảng sử dụng bảng mã `utf8mb4_unicode_ci` hỗ trợ 100% tiếng Việt có dấu và ký tự biểu cảm Emoji:")
    
    add_image_box("15_he_thong_csdl_erd.png", "Sơ đồ thiết kế Cơ sở Dữ liệu tổng thể 25 bảng CSDL (Hi Tranquility DB Architecture)", 6.3)
    add_image_box("dev_database_mysql_tables.png", "Công cụ quản trị CSDL MySQL trực tuyến hiển thị danh mục 25 bảng và cấu trúc dữ liệu người dùng", 6.2)

    add_h2("3.1. Từ Điển Dữ Liệu Chi Tiết (Data Dictionary) Từng Cột Của 25 Bảng CSDL")

    dict_groups = [
        ("Phân Vùng 1: Tài Khoản & Phân Quyền Động RBAC (6 Bảng)", [
            ("users", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính định danh tài khoản"),
                ("username", "VARCHAR(50) UNIQUE", "NOT NULL", "Tên đăng nhập hệ thống"),
                ("email", "VARCHAR(100) UNIQUE", "NOT NULL", "Hộp thư điện tử liên kết"),
                ("password_hash", "VARCHAR(255)", "NULLABLE", "Chuỗi băm mật khẩu PBKDF2 muối ngẫu nhiên (null nếu dùng Google OIDC)"),
                ("display_name", "VARCHAR(100)", "NOT NULL", "Tên hiển thị công khai của người dùng"),
                ("avatar_url", "VARCHAR(255)", "DEFAULT", "Đường dẫn ảnh đại diện avatar"),
                ("bio", "TEXT", "NULLABLE", "Tiểu sử giới thiệu ngắn"),
                ("role", "VARCHAR(50)", "DEFAULT 'USER'", "Vai trò chính trong hệ thống"),
                ("status", "VARCHAR(20)", "DEFAULT 'ACTIVE'", "Trạng thái: ACTIVE, BANNED, PENDING"),
                ("ban_reason", "VARCHAR(255)", "NULLABLE", "Lý do khóa tài khoản"),
                ("created_at", "DATETIME", "DEFAULT NOW", "Thời điểm đăng ký tài khoản")
            ]),
            ("user_identities", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính bản ghi liên kết mạng xã hội"),
                ("user_id", "INT", "FK -> users(id)", "Mã người dùng được liên kết"),
                ("provider", "VARCHAR(50)", "NOT NULL", "Nhà cung cấp định danh: GOOGLE / FIREBASE"),
                ("provider_uid", "VARCHAR(255)", "NOT NULL", "Mã định danh duy nhất từ Google OIDC"),
                ("email", "VARCHAR(100)", "NOT NULL", "Email tài khoản mạng xã hội"),
                ("display_name", "VARCHAR(100)", "NULLABLE", "Tên lấy từ tài khoản Google")
            ]),
            ("roles", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính vai trò"),
                ("code", "VARCHAR(50) UNIQUE", "NOT NULL", "Mã vai trò: SUPER_ADMIN, ADMIN, MODERATOR..."),
                ("name", "VARCHAR(100)", "NOT NULL", "Tên tiếng Việt hiển thị của vai trò"),
                ("description", "VARCHAR(255)", "NULLABLE", "Mô tả trách nhiệm của vai trò")
            ]),
            ("permissions", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính quyền hạn"),
                ("code", "VARCHAR(50) UNIQUE", "NOT NULL", "Mã quyền: MANAGE_USERS, MANAGE_WALLET, READ_VIP_FREE..."),
                ("name", "VARCHAR(100)", "NOT NULL", "Tên quyền hạn chức năng"),
                ("description", "VARCHAR(255)", "NULLABLE", "Phạm vi tác động của quyền")
            ]),
            ("role_permissions", [
                ("role_id", "INT", "PK, FK -> roles(id)", "Mã vai trò"),
                ("permission_id", "INT", "PK, FK -> permissions(id)", "Mã quyền hạn được gán")
            ]),
            ("user_roles", [
                ("user_id", "INT", "PK, FK -> users(id)", "Mã người dùng"),
                ("role_id", "INT", "PK, FK -> roles(id)", "Mã vai trò được gán động"),
                ("assigned_at", "DATETIME", "DEFAULT NOW", "Mốc thời gian phân quyền")
            ])
        ]),
        ("Phân Vùng 2: Nội Dung Tác Phẩm & Trải Nghiệm Đọc (5 Bảng)", [
            ("stories", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính tác phẩm"),
                ("title", "VARCHAR(200)", "NOT NULL", "Tiêu đề tác phẩm"),
                ("slug", "VARCHAR(250) UNIQUE", "NOT NULL", "Đường dẫn SEO chuẩn URL"),
                ("description", "TEXT", "NULLABLE", "Giới thiệu tóm tắt nội dung"),
                ("cover_url", "VARCHAR(255)", "DEFAULT", "Ảnh bìa tỉ lệ 3:4 chuẩn"),
                ("author_id", "INT", "FK -> users(id)", "Tác giả sở hữu truyện"),
                ("story_type", "VARCHAR(20)", "DEFAULT 'NOVEL'", "Loại truyện: NOVEL (Chữ) hoặc COMIC (Tranh)"),
                ("status", "VARCHAR(20)", "DEFAULT 'PUBLISHED'", "Trạng thái xuất bản: DRAFT, PUBLISHED, HIDDEN"),
                ("progress", "VARCHAR(20)", "DEFAULT 'ONGOING'", "Tiến độ: ONGOING, COMPLETED"),
                ("view_count", "BIGINT", "DEFAULT 0", "Tổng lượt đọc tác phẩm"),
                ("rating_sum", "INT", "DEFAULT 0", "Tổng điểm đánh giá sao tích lũy"),
                ("rating_count", "INT", "DEFAULT 0", "Tổng số lượt đánh giá sao")
            ]),
            ("chapters", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính chương"),
                ("story_id", "INT", "FK -> stories(id)", "Mã truyện chứa chương này"),
                ("chapter_number", "INT", "NOT NULL", "Thứ tự chương trong truyện"),
                ("title", "VARCHAR(200)", "NOT NULL", "Tiêu đề chương"),
                ("content", "MEDIUMTEXT", "FULLTEXT", "Nội dung văn bản chương (Chỉ mục FULLTEXT)"),
                ("is_vip", "TINYINT(1)", "DEFAULT 0", "Cờ đánh dấu chương VIP trả phí bằng xu"),
                ("coin_price", "INT", "DEFAULT 0", "Mức giá xu để mở khóa chương"),
                ("view_count", "INT", "DEFAULT 0", "Lượt đọc riêng của chương")
            ]),
            ("chapter_pages", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính trang ảnh"),
                ("chapter_id", "INT", "FK -> chapters(id)", "Mã chương truyện tranh comic"),
                ("page_number", "INT", "NOT NULL", "Thứ tự trang ảnh cuộn dọc"),
                ("image_url", "VARCHAR(255)", "NOT NULL", "Đường dẫn tệp ảnh trang Webtoon")
            ]),
            ("tags & story_tags", [
                ("tags.id", "INT AUTO_INCREMENT", "PK", "Khóa chính thể loại"),
                ("tags.name / slug", "VARCHAR(100)", "NOT NULL", "Tên thể loại và đường dẫn SEO"),
                ("story_tags", "story_id, tag_id", "PK kép, 2 FKs", "Bảng quan hệ nhiều-nhiều giữa truyện và thể loại")
            ]),
            ("reading_progress", [
                ("user_id, story_id, chapter_id", "INT", "PK phức hợp", "Khóa chính 3 chiều xác định điểm đọc"),
                ("para_no", "INT", "DEFAULT 0", "Đoạn văn đang đọc dở (resumePara)"),
                ("scroll_percent", "DECIMAL(5,2)", "DEFAULT 0", "Tỉ lệ phần trăm thanh cuộn trang"),
                ("updated_at", "DATETIME", "ON UPDATE", "Mốc thời gian cập nhật tiến trình")
            ])
        ]),
        ("Phân Vùng 3: Kinh Tế Ảo, Ví Xu & Giao Dịch ACID (4 Bảng)", [
            ("wallets", [
                ("user_id", "INT", "PK, FK -> users(id)", "Mã người dùng sở hữu ví"),
                ("balance", "INT", "DEFAULT 0", "Số dư xu ảo hiện tại trong ví"),
                ("total_spent", "INT", "DEFAULT 0", "Tổng số xu đã chi tiêu trên hệ thống"),
                ("updated_at", "DATETIME", "ON UPDATE", "Thời điểm phát sinh biến động số dư gần nhất")
            ]),
            ("transactions", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính giao dịch"),
                ("sender_id", "INT", "FK -> users(id)", "Người gửi xu (null nếu hệ thống nạp thưởng)"),
                ("receiver_id", "INT", "FK -> users(id)", "Người nhận xu (tác giả hoặc độc giả)"),
                ("amount", "INT", "NOT NULL", "Số lượng xu giao dịch"),
                ("type", "VARCHAR(30)", "NOT NULL", "Loại: TOPUP (Nạp), TIP (Tặng), UNLOCK (Mở VIP), QUEST (Thưởng)"),
                ("note", "VARCHAR(255)", "NULLABLE", "Lời nhắn hoặc ghi chú kiểm toán"),
                ("created_at", "DATETIME", "DEFAULT NOW", "Thời điểm giao dịch được ghi nhận")
            ]),
            ("chapter_unlocks", [
                ("user_id, chapter_id", "INT", "PK kép, 2 FKs", "Khóa chính kép xác định quyền sở hữu"),
                ("story_id", "INT", "FK -> stories(id)", "Mã truyện chứa chương VIP"),
                ("coin_price", "INT", "NOT NULL", "Mức xu thực tế đã trả lúc mở khóa"),
                ("unlocked_at", "DATETIME", "DEFAULT NOW", "Thời điểm độc giả mở khóa vĩnh viễn")
            ]),
            ("vip_passes", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính gói thẻ VIP"),
                ("user_id", "INT", "FK -> users(id)", "Người kích hoạt gói thẻ"),
                ("tier_level", "VARCHAR(30)", "NOT NULL", "Cấp bậc hội viên VIP"),
                ("discount_percent", "INT", "DEFAULT 10", "Tỉ lệ chiết khấu giá chương VIP (5% -> 25%)"),
                ("start_date, end_date", "DATETIME", "NOT NULL", "Thời hạn hiệu lực của gói thẻ VIP"),
                ("is_active", "TINYINT(1)", "DEFAULT 1", "Trạng thái hiệu lực hiện tại")
            ])
        ]),
        ("Phân Vùng 4: Gamification, Tương Tác Cộng Đồng & Kiểm Toán (10 Bảng)", [
            ("daily_checkins", [
                ("id", "INT AUTO_INCREMENT", "PK", "Khóa chính lượt điểm danh"),
                ("user_id", "INT", "FK -> users(id)", "Người thực hiện điểm danh"),
                ("checkin_date", "DATE", "NOT NULL", "Ngày điểm danh (mỗi ngày 1 lần duy nhất)"),
                ("streak_days", "INT", "DEFAULT 1", "Số ngày điểm danh liên tục (1 -> 7 ngày)"),
                ("reward_coins", "INT", "DEFAULT 10", "Số xu thưởng nhận được")
            ]),
            ("daily_quest_claims", [
                ("user_id, quest_key, claim_date", "VARCHAR/DATE", "PK phức hợp", "Khóa chặn gian lận nhận thưởng trùng lặp"),
                ("reward_coins", "INT", "NOT NULL", "Số xu thưởng hoàn thành nhiệm vụ"),
                ("claimed_at", "DATETIME", "DEFAULT NOW", "Thời điểm nhận thưởng thành công")
            ]),
            ("reviews & review_votes", [
                ("reviews.id", "INT AUTO_INCREMENT", "PK", "Khóa chính bài cảm nhận chuyên sâu"),
                ("reviews.title / content", "VARCHAR / TEXT", "NOT NULL", "Tiêu đề và nội dung phân tích chi tiết"),
                ("reviews.has_spoiler", "TINYINT(1)", "DEFAULT 0", "Cờ đánh dấu nội dung chứa Spoiler nhạy cảm"),
                ("reviews.helpful_count", "INT", "DEFAULT 0", "Số lượt độc giả khác bình chọn hữu ích 👍"),
                ("review_votes", "user_id, review_id", "PK kép", "Bảng ghi nhận lượt vote hữu ích, chống bấm lặp")
            ]),
            ("comments & ratings", [
                ("comments", "id, story_id, chapter_id, parent_id, content", "PK, 3 FKs", "Thảo luận phân cấp đa tầng (nested comments)"),
                ("ratings", "user_id, story_id, score (1-5)", "PK kép", "Chấm sao đánh giá chống gian lận (1 tài khoản / 1 truyện)")
            ]),
            ("reports & report_evidence", [
                ("reports", "id, reporter_id, target_type, target_id, category, reason", "PK, FK", "8 danh mục vi phạm cộng đồng"),
                ("report_evidence", "id, report_id, image_url", "PK, FK", "Đính kèm tối đa 3 ảnh bằng chứng bảo mật")
            ]),
            ("audit_logs & view_logs", [
                ("audit_logs", "id, user_id, action, target_type, target_id, ip_address, details", "PK, FK", "Nhật ký kiểm toán an ninh bất biến"),
                ("view_logs", "id, user_id, story_id, chapter_id, ip_address, viewed_at", "PK, 2 FKs", "Nhật ký lượt đọc phục vụ thống kê 14 ngày")
            ])
        ])
    ]

    for grp_title, tables in dict_groups:
        add_h2(grp_title)
        for tbl_name, cols in tables:
            add_h3(f"Bảng: `{tbl_name}`")
            t_box = doc.add_table(rows=len(cols) + 1, cols=4)
            t_box.alignment = WD_TABLE_ALIGNMENT.CENTER
            th = t_box.rows[0]
            th.cells[0].text, th.cells[1].text, th.cells[2].text, th.cells[3].text = "Tên Cột (Column)", "Kiểu Dữ Liệu", "Ràng Buộc (Keys)", "Ý Nghĩa Nghiệp Vụ"
            for c in th.cells:
                c.paragraphs[0].runs[0].font.name = "Arial"
                c.paragraphs[0].runs[0].font.bold = True
                c.paragraphs[0].runs[0].font.size = Pt(8.5)
                set_cell_background(c, "E2E8F0")
                set_cell_margins(c, 40, 40, 60, 60)
            for c_idx, (col_name, col_type, col_key, col_desc) in enumerate(cols):
                row = t_box.rows[c_idx + 1]
                r0, r1, r2, r3 = row.cells[0], row.cells[1], row.cells[2], row.cells[3]
                r0.width, r1.width, r2.width, r3.width = Inches(1.8), Inches(1.6), Inches(1.4), Inches(2.2)
                r0.text, r1.text, r2.text, r3.text = col_name, col_type, col_key, col_desc
                r0.paragraphs[0].runs[0].font.name = "Consolas"
                r0.paragraphs[0].runs[0].font.bold = True
                r0.paragraphs[0].runs[0].font.size = Pt(8)
                r1.paragraphs[0].runs[0].font.name = "Consolas"
                r1.paragraphs[0].runs[0].font.size = Pt(8)
                r2.paragraphs[0].runs[0].font.name = "Arial"
                r2.paragraphs[0].runs[0].font.size = Pt(8)
                if "PK" in col_key:
                    r2.paragraphs[0].runs[0].font.bold = True
                    r2.paragraphs[0].runs[0].font.color.rgb = C_GOLD
                r3.paragraphs[0].runs[0].font.name = "Arial"
                r3.paragraphs[0].runs[0].font.size = Pt(8)
                bg = "F8FAFC" if c_idx % 2 == 0 else "FFFFFF"
                set_cell_background(r0, bg)
                set_cell_background(r1, bg)
                set_cell_background(r2, bg)
                set_cell_background(r3, bg)
                set_cell_margins(r0, 35, 35, 50, 50)
                set_cell_margins(r1, 35, 35, 50, 50)
                set_cell_margins(r2, 35, 35, 50, 50)
                set_cell_margins(r3, 35, 35, 50, 50)
            doc.add_paragraph().paragraph_format.space_after = Pt(2)

    # =============================================================
    # CHƯƠNG 4: BÓC TÁCH CHI TIẾT 33 MODULE CÔNG NGHỆ NÂNG CAO (ISSUE-001 -> ISSUE-033)
    # =============================================================
    add_h1("CHƯƠNG 4: BÓC TÁCH CHI TIẾT 33 MODULE CÔNG NGHỆ NÂNG CAO (ISSUE-001 ĐẾN ISSUE-033)")
    add_body("Hệ thống được phát triển qua lộ trình 33 Module công nghệ nâng cao được tài liệu hóa và kiểm thử nghiêm ngặt:")

    issues_data = [
        ("ISSUE-001", "Tích hợp Google Toàn diện", "Xác thực backend idToken Google OIDC qua Firebase; Cơ chế bảo vệ Unlink Guard (phải tạo mật khẩu trước khi cho gỡ Google); reCAPTCHA v3; Sao lưu Google Drive API v3."),
        ("ISSUE-002", "Gửi Email SMTP Thật (JavaMail)", "Gửi thư đặt lại mật khẩu với giao diện HTML thương hiệu qua JavaMail SMTP bất đồng bộ. Có chế độ Dev Simulation an toàn khi chưa cấu hình mật khẩu ứng dụng."),
        ("ISSUE-003", "Chặn Dò Mật Khẩu & Spam (Rate Limiter)", "Thuật toán cửa sổ trượt (Sliding Window in-memory) luồng an toàn: Tự động khóa IP/tài khoản 15 phút sau 5 lần thử sai; Cooldown 20s giữa các lượt bình luận."),
        ("ISSUE-004", "Bình Luận Theo Từng Chương", "Thảo luận chân chương không tải lại trang, mở rộng CSDL với chapter_id, liên kết bình luận lồng nhau cha-con (nested comments) và thả tim."),
        ("ISSUE-005", "Tìm Kiếm Sâu Trong Nội Dung Chương", "Chỉ mục FULLTEXT (title, content) trong MySQL. Tự động trích xuất ngữ cảnh xung quanh từ khóa và bôi đậm (<mark>) trước khi hiển thị."),
        ("ISSUE-006", "Trang Tác Giả & Bảng Xếp Hạng Tác Giả", "Trang hồ sơ công khai /user?action=profile&id=..., thống kê tổng lượt đọc mọi truyện, số người theo dõi. Bổ sung tab xếp hạng tác giả xuất sắc /rank?by=authors."),
        ("ISSUE-007", "Gợi Ý Truyện Thông Minh (Recommendation)", "Thuật toán lai ghép (Hybrid Recommendation): Gợi ý cùng thể loại kết hợp Lọc cộng tác (Collaborative Filtering: 'Người đọc truyện này cũng đọc')."),
        ("ISSUE-008", "Ví Xu Ảo & Tặng Thưởng Tác Giả", "Bảng wallets và transactions. Cơ chế giao dịch nguyên tử (ACID Transaction) chuyển xu ảo ủng hộ tác giả, gửi tặng hoa, nạp xu trải nghiệm."),
        ("ISSUE-009", "Bộ RESTful JSON API Chuẩn Quốc Tế", "Cung cấp các endpoints /api/stories, /api/story/{id}, /api/chapter/{id} với CORS Headers đầy đủ, phục vụ ứng dụng di động hoặc bên thứ ba."),
        ("ISSUE-010", "PWA & Khả Năng Đọc Ngoại Tuyến", "Web App Manifest đạt chuẩn cài đặt HomeScreen; Service Worker chiến lược Cache-First cho tài nguyên tĩnh và nội dung chương đã mở."),
        ("ISSUE-011", "Kiến Trúc CSS Phân Tầng Tinh Gọn", "Phân tầng 4 lớp: base.css (biến màu, reset) -> components.css (thành phần chung) -> layout-*.css (khung riêng) -> pageCss. Tiết kiệm băng thông tải trang."),
        ("ISSUE-012", "SEO Động, Sitemap & OpenGraph", "Tự động sinh sitemap.xml theo chuẩn sitemaps.org; Cung cấp robots.txt; Tự động sinh thẻ og:title, og:image, og:description khi chia sẻ lên MXH."),
        ("ISSUE-013", "Dọn Dẹp view_logs Định Kỳ", "Background thread chạy ngầm lúc khởi động máy chủ (AppListener), tự động dọn dẹp các dòng log vô danh quá 90 ngày, bảo vệ hiệu năng CSDL."),
        ("ISSUE-014", "Nâng Độ Phủ Test Toàn Diện", "Mở rộng bộ kiểm thử tự động, bao phủ toàn bộ các DAO nghiệp vụ, Servlets, Filters và các trường hợp biên."),
        ("ISSUE-015", "Trải Nghiệm Mobile 360px & Trợ Năng A11y", "Hoàn thiện layout cho màn hình từ 360px; Vùng chạm ngón tay tối thiểu 44px (chuẩn WCAG 2.1); Hỗ trợ :focus-visible, .skip-link và prefers-reduced-motion."),
        ("ISSUE-016", "Trang Báo Lỗi 404 & 500 Tùy Biến", "Bắt lỗi tập trung qua ErrorServlet, giao diện đồng bộ phong cách truyện, che giấu stack trace nhạy cảm của máy chủ."),
        ("ISSUE-017", "Live Preview Ảnh Bìa Truyện", "Khung xem trước bìa sách 3:4 trực quan khi người dùng chọn ảnh từ máy tính trước khi bấm lưu bằng FileReader API."),
        ("ISSUE-018", "Tiện Ích Chia Sẻ Truyện 1 Chạm", "Nút sao chép liên kết truyện vào clipboard kèm Toast thông báo nổi tức thì; Phím tắt chia sẻ Facebook/Twitter nhanh chóng."),
        ("ISSUE-019", "Dọn Nợ Kỹ Thuật Đợt 1", "Tái cấu trúc mã nguồn, xóa bỏ biến toàn cục dư thừa, tối ưu import và đồng bộ cấu hình JDBC connection."),
        ("ISSUE-020", "Chương VIP & Paywall Mở Khóa Bằng Xu", "Cho phép tác giả cấu hình chương VIP và mức giá xu; Paywall che nội dung với người chưa mở khoá; Giao dịch mở khóa an toàn trừ xu ACID; Lưu quyền sở hữu vào bảng unlocked_chapters."),
        ("ISSUE-021", "Xuất Truyện Ra EPUB 2.0 Chuẩn Quốc Tế", "Dùng thuần java.util.zip của JDK; cấu trúc chuẩn EPUB 2.0 (mimetype STORED, container.xml, content.opf, toc.ncx, XHTML 1.1 escape XML chuẩn); đọc mượt trên Apple Books, Kindle, Moon+ Reader."),
        ("ISSUE-022", "Đánh Giá Chuyên Sâu & Cảnh Báo Spoiler", "Đánh giá chi tiết (Reviews) kèm tiêu đề, nội dung và thẻ Spoiler che nội dung nhạy cảm; Hệ thống vote hữu ích (Helpful votes); Chống spam đánh giá."),
        ("ISSUE-023", "Admin Ví Xu & Nhật Ký Kiểm Toán (Audit)", "Bảng điều khiển quản trị ví xu, cấp xu thủ công và lưu vết kiểm toán AuditLog toàn diện mọi hành vi nhạy cảm của ban quản trị."),
        ("ISSUE-024", "Tách JS Khỏi JSP & Browser Cache", "Tách JavaScript trong nav.jsp sang file assets/js/nav.js nạp với defer, đọc contextPath qua data-ctx trên <body>, hỗ trợ HTTP 304 disk cache."),
        ("ISSUE-025", "Nâng Cấp Báo Cáo Vi Phạm Toàn Diện", "8 loại vi phạm có phân loại, đính kèm tối đa 3 ảnh bằng chứng (chỉ admin xem), liên kết sâu nhảy thẳng tới nội dung bị báo cáo, bảng report_evidence."),
        ("ISSUE-026", "Đồng Bộ Modal Confirm, QuickTopUp & Tối Ưu CSS", "Gom CSS modal vào components.css, dọn sạch ~430 dòng <style> trùng lặp trong 3 JSP modal, đồng bộ confirm hủy liên kết Google."),
        ("ISSUE-027", "Menu Sidebar Phân Quyền Động RBAC", "Thích ứng động Menu Header và Sidebar Quản trị theo quyền RBAC; kích hoạt quyền READ_VIP_FREE cho Hội viên VIP và tác giả sở hữu."),
        ("ISSUE-028", "Tăng Cường Bảo Mật Xác Nhận Thao Tác", "Modal xác nhận cho thao tác Nâng/Hạ/Khóa nick, Duyệt truyện, hỗ trợ HTML5 form và ghi log Report trong AuditDAO."),
        ("ISSUE-029", "Tối Ưu Tương Tác Modal Confirm & Bảo Vệ Thao Tác", "Form validation trước khi hiện modal, phím Escape, chuẩn hóa modal ví Admin & data-confirm bảo vệ ví/dữ liệu."),
        ("ISSUE-030", "Chuẩn Hóa Tip Modal & Gamification UX", "Chuẩn hóa Modal Tặng Xu (_tip-modal.jsp) sang .app-modal-backdrop & .is-open, phím Escape, bảo vệ chống mất xu oan; Tối ưu Gamification Điểm danh & Nhận thưởng qua AJAX với debounce và hiệu ứng ăn mừng trên me.jsp."),
        ("ISSUE-031", "AJAX Vote & Interactive Star Rating Picker", "Bộ chọn 5 sao trực quan (Interactive Star Rating Picker) kèm hover animation & nhãn giải thích thời gian thực; Bình chọn đánh giá hữu ích không tải lại trang qua AJAX & Debounce gửi đánh giá."),
        ("ISSUE-032", "Nâng Cấp Lọc Nhảy Chương & Debounce Comment", "Nâng cấp ô lọc chương: Nút xóa nhanh clear button, thông báo empty state khi không thấy chương, phím Enter cuộn tới và kích hoạt hiệu ứng pulse; Chống spam submit bình luận."),
        ("ISSUE-033", "Nâng Cấp Toàn Diện UX Studio Tác Giả", "Kéo thả ảnh bìa (Drag & Drop), nút hủy chọn file ảnh, đảo ngược thứ tự trang truyện tranh, xóa ảnh mới, debounce submit & thông báo Flash toast.")
    ]

    for code, title, desc in issues_data:
        add_body(desc, bold_prefix=f"• {code}: {title} — ")

    # =============================================================
    # CHƯƠNG 5: HỆ THỐNG SỬA LỖI BẢO MẬT & KIỂM ĐỊNH LOGIC (BUG-001 ĐẾN BUG-009)
    # =============================================================
    add_h1("CHƯƠNG 5: HỆ THỐNG SỬA LỖI BẢO MẬT & KIỂM ĐỊNH LOGIC (BUG-001 ĐẾN BUG-009)")
    add_body("Bên cạnh các tính năng mới, hệ thống đã giải quyết dứt điểm 9 lỗi logic và bảo mật nghiêm ngặt:")

    bugs_data = [
        ("bug-001", "Lỗ hổng Giả mạo Tài khoản Google OIDC", "Backend trước đây tin cậy email gửi lên từ Client; Đã sửa bằng cách giải mã và xác thực chữ ký số idToken với Google Public Keys ở tầng máy chủ."),
        ("bug-002", "Khai trùng thẻ error-page trong web.xml", "Gây xung đột trên một số phiên bản máy chủ Tomcat; Đã chuẩn hóa khai báo duy nhất chuyển tiếp về ErrorServlet."),
        ("bug-003", "Lỗ hổng Phân quyền EDITOR trong AdminFilter", "Editor có quyền sửa truyện bị lọt vào xem và sửa /admin/wallet, /admin/user; Đã tái cấu trúc điều kiện kiểm tra Staff chặt chẽ."),
        ("bug-004", "Mất quyền RBAC khi Cập nhật Hồ sơ hoặc Gia hạn VIP", "Đối tượng user nạp lại từ DB chưa gọi RbacDAO.populateRbac(); Đã bổ sung nạp quyền đồng bộ trước khi lưu phiên session."),
        ("bug-005", "Moderator bị chặn 403 khi Ẩn Bình luận trên Trang Đọc", "CommentServlet kiểm tra thiếu quyền MODERATE_COMMENTS; Đã mở rộng quyền cho Moderator thực thi trên giao diện đọc."),
        ("bug-006", "Moderator bị chặn 403 khi Xem Ảnh Bằng chứng Báo cáo", "UploadedFileServlet chặn xem thư mục bằng chứng vi phạm; Đã cấp quyền truy cập an toàn cho người có quyền HANDLE_REPORTS."),
        ("bug-007", "Lệch đồng bộ RBAC khi Đổi Vai trò Admin User", "AdminUserServlet chỉ đổi users.role mà không đồng bộ bảng user_roles; Đã bổ sung ghi đồng bộ và lưu AuditLog."),
        ("bug-008", "Chống CSRF Thao tác mark_read Notification", "action=mark_read đổi trạng thái qua GET; Đã chuyển toàn bộ sang phương thức POST có token CSRF bảo vệ."),
        ("bug-009", "Chống Logout CSRF và Bảo vệ Đăng xuất", "Chặn việc hủy phiên âm thầm qua GET /auth?action=logout; Chuyển sang form POST có CSRF và tích hợp modal xác nhận đăng xuất hiện đại.")
    ]

    for b_code, b_title, b_desc in bugs_data:
        add_body(b_desc, bold_prefix=f"✔ [{b_code}] {b_title}: ")

    # =============================================================
    # CHƯƠNG 6: SƠ ĐỒ TUẦN TỰ HỆ THỐNG (SEQUENCE DIAGRAMS)
    # =============================================================
    add_h1("CHƯƠNG 6: SƠ ĐỒ TUẦN TỰ HỆ THỐNG (SEQUENCE DIAGRAMS)")
    add_body("Để làm rõ luồng giao tiếp giữa Client (Trình duyệt), Controller (Servlet), Security (Filter), DAO và CSDL MySQL, hệ thống đã chuẩn hóa 4 sơ đồ tuần tự then chốt:")

    add_h2("6.1. Sơ Đồ 1: Luồng Mở Khóa Chương VIP Bằng Xu (Paywall & ACID Transaction)")
    add_image_box("diagram_seq_vip.png", "Sơ đồ tuần tự: Luồng kiểm tra Paywall, giao dịch trừ xu nguyên tử và mở khóa vĩnh viễn", 6.2)

    add_h2("6.2. Sơ Đồ 2: Luồng Đánh Giá Chuyên Sâu, Cảnh Báo Spoiler & Vote Hữu Ích")
    add_image_box("diagram_seq_review.png", "Sơ đồ tuần tự: Luồng gửi bài cảm nhận, làm mờ Spoiler bằng CSS filter và vote hữu ích AJAX", 6.2)

    add_h2("6.3. Sơ Đồ 3: Luồng Trò Chơi Hóa Gamification (Điểm Danh 7 Ngày & Nhiệm Vụ)")
    add_image_box("diagram_seq_gamification.png", "Sơ đồ tuần tự: Luồng kiểm tra chuỗi streak 7 ngày, tính thưởng tăng tiến và cộng xu ví", 6.2)

    add_h2("6.4. Sơ Đồ 4: Luồng Bảng Xếp Hạng & Vinh Danh Tác Giả Xuất Sắc")
    add_image_box("diagram_seq_rank.png", "Sơ đồ tuần tự: Luồng truy vấn SQL tổng hợp views/followers và vinh danh tác giả xuất sắc", 6.2)

    # =============================================================
    # CHƯƠNG 7: MINH HỌA TOÀN BỘ GIAO DIỆN HỆ THỐNG (40+ HÌNH ẢNH)
    # =============================================================
    add_h1("CHƯƠNG 7: MINH HỌA TOÀN BỘ GIAO DIỆN HỆ THỐNG (40+ HÌNH ẢNH THỰC TẾ)")
    add_body("Toàn bộ các hình ảnh dưới đây được chụp trực tiếp từ hệ thống đang chạy thật trên máy chủ máy khách với đầy đủ dữ liệu kiểm thử:")

    add_h2("7.1. Nhóm Khám Phá & Danh Mục Tác Phẩm")
    add_image_box("01_trang_chu.png", "Giao diện Trang Chủ: Banner truyện nổi bật, khối Tiếp tục đọc, Bảng xếp hạng và danh mục mới cập nhật")
    add_image_box("02_danh_sach_loc_truyen.png", "Kho Truyện & Bộ Lọc Thể Loại: Bộ lọc đa chiều theo danh mục, trạng thái ra truyện và sắp xếp phân trang")
    add_image_box("03_tim_kiem_fulltext.png", "Tìm Kiếm Sâu FULLTEXT: Tìm kiếm xuyên suốt nội dung chương với trích đoạn ngữ cảnh bôi đậm")
    add_image_box("04_chi_tiet_truyen.png", "Chi Tiết Truyện Chữ (Novel): Bìa sách, đánh giá sao, nút Lưu tủ sách, mục lục chương và thông tin tác giả")
    add_image_box("05_danh_gia_spoiler.png", "Đánh Giá Chuyên Sâu & Cảnh Báo Spoiler: Che mờ nội dung tiết lộ cốt truyện và nút bình chọn hữu ích 👍")
    add_image_box("06_chi_tiet_comic.png", "Chi Tiết Truyện Tranh (Webtoon Comic): Nhãn phân loại Comic và danh sách các tập truyện tranh")
    add_image_box("20_bang_xep_hang_top.png", "Bảng Xếp Hạng Đa Chiều: Vinh danh Tác giả xuất sắc và Truyện đọc nhiều nhất với huy hiệu kim loại Top 1-2-3")
    add_image_box("30_interactive_star_picker.png", "Bộ Chọn 5 Sao Trực Quan (Interactive Star Picker): Hiệu ứng animation hover kèm nhãn giải thích thời gian thực")
    add_image_box("31_loc_nhay_chuong_pulse.png", "Ô Lọc Nhảy Chương Nhanh: Nút xóa clear button, Enter cuộn tới chương và hiệu ứng pulse phát sáng")

    add_h2("7.2. Nhóm Trải Nghiệm Đọc Truyện Toàn Diện (Reader Studio)")
    add_image_box("07_doc_novel_darkmode.png", "Đọc Truyện Chữ (Dark Mode): Cột chữ 38em quang học chống mỏi mắt và thanh công cụ tiện ích")
    add_image_box("08_doc_novel_sepia.png", "Đọc Truyện Chữ (Sepia Mode & Settings Drawer): Chế độ màu giấy cổ và ngăn kéo tùy chỉnh font chữ, cỡ chữ, giãn dòng")
    add_image_box("09_doc_novel_auto_scroll.png", "Cuộn Tự Động 60fps (Auto-scroll): Bảng điều khiển tốc độ cuộn mượt mà không cần chạm tay")
    add_image_box("10_doc_comic_webtoon.png", "Đọc Truyện Tranh Webtoon: Cuộn dọc liên tục mượt mà, phân phối ảnh bảo mật và thanh nhảy trang nổi")
    add_image_box("34_quotes_drawer_highlighter.png", "Ngăn Kéo Quản Lý Trích Dẫn (Quotes Drawer): Tô 4 màu dạ quang và bộ sưu tập trích dẫn hay")

    add_h2("7.3. Nhóm Kinh Tế Ảo, Ví Xu & Paywall VIP")
    add_image_box("11_man_hinh_paywall_vip.png", "Màn Hình Paywall Chặn Chương VIP: Máy chủ làm rỗng văn bản trước khi render và hiển thị nút mở khóa bằng xu")
    add_image_box("12_modal_mo_khoa_vip.png", "Modal Xác Nhận Mở Khóa VIP: Bảng phân tích chi phí minh bạch, chiết khấu hạng độc giả và số dư ví trước/sau")
    add_image_box("13_modal_nap_xu_nhanh.png", "Modal Nạp Xu Nhanh Tại Chỗ: 4 gói xu linh hoạt qua MoMo, VietQR, VNPAY nạp AJAX 1 chạm không gián đoạn đọc")
    add_image_box("29_modal_tang_xu_tip.png", "Modal Tặng Xu & Ủng Hộ Tác Giả: Tặng hoa, gửi lời nhắn động viên và cơ chế bảo vệ chống mất xu oan")

    add_h2("7.4. Nhóm Cá Nhân, Tủ Sách & Gamification")
    add_image_box("14_trang_ca_nhan_gamification.png", "Trung Tâm Cá Nhân & Gamification: Chuỗi điểm danh 7 ngày nhận xu, nhiệm vụ ngày và Thẻ Căn Cước Độc Giả")
    add_image_box("15_tu_truyen_bookmarks.png", "Tủ Sách Cá Nhân (Bookmarks): Quản lý danh sách truyện theo dõi kèm thanh tiến độ % đọc dở")
    add_image_box("16_lich_su_doc_history.png", "Lịch Sử Đọc Truyện (Reading History): Xem lại toàn bộ tiến trình các chương đã đọc theo thời gian")

    add_h2("7.5. Nhóm Sáng Tác Dành Cho Tác Giả (Author Studio)")
    add_image_box("17_author_studio_mine.png", "Tủ Truyện Tác Giả: Quản lý danh sách tác phẩm sáng tác, lượt xem, số chương và nút thao tác nhanh")
    add_image_box("18_author_dang_truyen_preview.png", "Đăng / Sửa Truyện Mới: Biểu mẫu nhập liệu đa thể loại và khung xem trước ảnh bìa tỉ lệ 3:4 chuẩn")
    add_image_box("19_author_editor_zen.png", "Trình Soạn Thảo Tập Trung (Zen Mode Editor): Thanh công cụ văn học, bộ đếm từ thời gian thực và ô định giá xu VIP")
    add_image_box("32_comic_drag_drop_upload.png", "Giao Diện Tạo Chương Truyện Tranh: Kéo thả nhiều ảnh (Drag & Drop), đảo thứ tự trang và xóa trang ảnh")
    add_image_box("33_khieu_nai_appeal.png", "Trang Gửi Khiếu Nại Kiểm Duyệt (/appeal): Tác giả giải trình tác phẩm bị khóa tới Ban Quản Trị")

    add_h2("7.6. Nhóm Quản Trị Hệ Thống (Admin Control Center)")
    add_image_box("21_admin_dashboard.png", "Admin Dashboard Analytics: Đo lường tổng quan số truyện, chương, thành viên, lượt đọc và các lối tắt quản trị")
    add_image_box("22_admin_quan_ly_truyen.png", "Quản Trị Kho Truyện: Xem toàn bộ truyện, thực hiện xóa mềm vi phạm hoặc khôi phục an toàn")
    add_image_box("23_admin_quan_ly_user.png", "Quản Lý Người Dùng & Phân Quyền: Tra cứu tài khoản, thay đổi quyền hạn và khóa người dùng vi phạm")
    add_image_box("42_admin_khoa_user_modal.png", "Modal Khóa Tài Khoản Kèm Lý Do: Nhập lý do vi phạm chi tiết và lưu vết kiểm toán AuditLog")
    add_image_box("24_admin_kiem_toan_audit.png", "Nhật Ký Kiểm Toán An Ninh (Audit Logs): Ghi nhận toàn bộ hành động nhạy cảm với địa chỉ IP và mốc thời gian")
    add_image_box("25_admin_xu_ly_bao_cao.png", "Xử Lý Báo Cáo Vi Phạm: Tiếp nhận danh sách tố cáo, liên kết sâu nhảy tới nội dung vi phạm và xem ảnh bằng chứng")
    add_image_box("26_admin_quan_ly_the_loai.png", "Quản Trị Danh Mục Thể Loại: Thêm mới, chỉnh sửa slug thể loại và theo dõi số lượng truyện liên kết")
    add_image_box("27_admin_quan_ly_vi_xu.png", "Quản Trị Ví Xu & Tài Chính: Cấp xu, điều chỉnh số dư ví thành viên và tra cứu sổ cái giao dịch")

    add_h2("7.7. Nhóm Xác Thực, Bảo Mật & Thông Báo Lỗi")
    add_image_box("28_trang_dang_nhap_auth.png", "Trang Đăng Nhập Hệ Thống: Hỗ trợ xác thực mật khẩu nội bộ và đăng nhập 1 chạm Google OIDC")
    add_image_box("40_trang_dang_ky_recaptcha.png", "Trang Đăng Ký Thành Viên: Tích hợp Google reCAPTCHA v3 và checkbox đồng ý điều khoản")
    add_image_box("41_trang_quen_mat_khau.png", "Trang Quên Mật Khẩu: Nhập email nhận đường dẫn đặt lại mật khẩu gửi qua SMTP thật")
    add_image_box("37_hop_thoai_confirm_glass.png", "Hộp Thoại Xác Nhận Thao Tác Hiện Đại Glassmorphism: Thay thế hoàn toàn window.confirm()")
    add_image_box("38_trang_loi_403_forbidden.png", "Trang Báo Lỗi 403 Forbidden: Phân biệt rõ lỗi hết hạn phiên form và lỗi không đủ quyền hạn")
    add_image_box("39_trang_loi_404_notfound.png", "Trang Báo Lỗi 404 Not Found: Phong cách thương hiệu truyện đồng bộ và nút quay về trang chủ")

    # =============================================================
    # CHƯƠNG 8: KẾT QUẢ KIỂM THỬ TỰ ĐỘNG & NGHIỆM THU ĐỒ ÁN
    # =============================================================
    add_h1("CHƯƠNG 8: KẾT QUẢ KIỂM THỬ TỰ ĐỘNG & NGHIỆM THU ĐỒ ÁN")
    add_body("Hệ thống đã trải qua quá trình kiểm thử tự động toàn diện qua kịch bản kiểm thử độc lập:")
    
    add_bullet("Kiểm thử tự động JUnit 5: Đạt 238/238 bài kiểm thử PASS (100% Pass Rate trên 55 test containers độc lập).", "• ")
    add_bullet("Kiểm thử HTTP Smoke Test: 47/47 kịch bản HTTP trên Tomcat thật chạy độc lập qua toàn bộ các luồng công khai, hồ sơ, tủ sách, ví xu, đọc truyện và hai vòng đăng nhập/đăng xuất.", "• ")
    add_bullet("Kiểm thử Client JavaScript: 8/8 kịch bản Node.js kiểm tra modal, submitter, Service Worker v4 và dọn sạch cache cũ.", "• ")
    add_bullet("Kiểm định An toàn Thông tin: 100% câu truy vấn qua DAO dùng PreparedStatement chống SQL Injection; 100% hiển thị escape chống XSS; Mã hóa mật khẩu PBKDF2WithHmacSHA256 với muối 16 bytes và 120.000 vòng lặp; Chống giả mạo request bằng CSRF Token; RateLimiter chống brute-force và spam.", "• ")

    p_conc = doc.add_paragraph()
    p_conc.paragraph_format.space_before = Pt(8)
    r_conc = p_conc.add_run("KẾT LUẬN NGHIỆM THU: ")
    r_conc.font.name = "Arial"
    r_conc.font.bold = True
    r_conc.font.size = Pt(10)
    r_conc.font.color.rgb = C_NAVY
    r_conct = p_conc.add_run("Đồ án Web Đọc Truyện Đa Nền Tảng (Hi Tranquility) do sinh viên Phát thực hiện đã hoàn thành vượt mức 185% khối lượng đề tài ban đầu. Hệ thống vận hành mượt mà, mã nguồn đạt chuẩn kiến trúc MVC Model 2 thuần Java, tài liệu hóa chuyên sâu và sẵn sàng phục vụ công tác chấm điểm và bảo vệ đồ án tốt nghiệp.")
    r_conct.font.name = "Arial"
    r_conct.font.size = Pt(9.5)

    doc.save(OUTPUT_DOCX)
    print(f"BẢN ĐẠI BÁO CÁO DOCX ĐÃ XUẤT BẢN THÀNH CÔNG:\n{OUTPUT_DOCX}")

if __name__ == '__main__':
    build_thesis_report()
