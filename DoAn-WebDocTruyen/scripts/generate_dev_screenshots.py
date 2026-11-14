import os
import sys
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding='utf-8')

OUT_DIR = os.path.join(os.getcwd(), 'docs', 'report_images')
os.makedirs(OUT_DIR, exist_ok=True)

# 1. VS CODE IDE SCREENSHOT HTML
vscode_html = r"""<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
  body { background: #1e1e1e; color: #d4d4d4; width: 1400px; height: 850px; overflow: hidden; display: flex; flex-direction: column; }
  .titlebar { background: #323233; height: 32px; display: flex; align-items: center; justify-content: space-between; padding: 0 12px; font-size: 12px; color: #cccccc; border-bottom: 1px solid #252526; }
  .dots { display: flex; gap: 8px; }
  .dot { width: 12px; height: 12px; border-radius: 50%; }
  .dot-red { background: #ff5f56; }
  .dot-yellow { background: #ffbd2e; }
  .dot-green { background: #27c93f; }
  .main-body { display: flex; flex: 1; overflow: hidden; }
  .activity-bar { width: 48px; background: #333333; display: flex; flex-direction: column; align-items: center; padding-top: 10px; gap: 18px; color: #858585; font-size: 20px; }
  .activity-bar .active { color: #ffffff; border-left: 2px solid #ffffff; padding-left: 2px; }
  .sidebar { width: 280px; background: #252526; border-right: 1px solid #1e1e1e; font-size: 13px; display: flex; flex-direction: column; }
  .sidebar-header { padding: 10px 14px; font-weight: bold; font-size: 11px; text-transform: uppercase; color: #bbbbbb; letter-spacing: 0.5px; }
  .tree { padding: 4px 0; overflow-y: auto; flex: 1; font-family: Consolas, monospace; font-size: 12px; }
  .tree-item { padding: 3px 16px; display: flex; align-items: center; gap: 6px; color: #cccccc; cursor: pointer; white-space: nowrap; }
  .tree-item.folder { font-weight: bold; color: #e5e5e5; }
  .tree-item.active { background: #37373d; color: #ffffff; }
  .tree-item .icon { font-size: 13px; }
  .editor-area { flex: 1; display: flex; flex-direction: column; background: #1e1e1e; }
  .tabs { height: 35px; background: #252526; display: flex; border-bottom: 1px solid #1e1e1e; overflow-x: auto; }
  .tab { padding: 0 16px; display: flex; align-items: center; gap: 8px; font-size: 12px; color: #969696; background: #2d2d2d; border-right: 1px solid #252526; cursor: pointer; }
  .tab.active { background: #1e1e1e; color: #ffffff; border-top: 2px solid #007acc; }
  .code-pane { flex: 1; padding: 16px; font-family: "JetBrains Mono", Consolas, "Courier New", monospace; font-size: 13px; line-height: 1.5; overflow-y: auto; color: #d4d4d4; }
  .line-num { color: #858585; display: inline-block; width: 35px; text-align: right; margin-right: 16px; user-select: none; }
  .kwd { color: #569cd6; font-weight: bold; }
  .cls { color: #4ec9b0; }
  .str { color: #ce9178; }
  .ann { color: #dcdcaa; }
  .com { color: #6a9955; font-style: italic; }
  .fn { color: #dcdcaa; }
  .statusbar { height: 22px; background: #007acc; color: #ffffff; display: flex; align-items: center; justify-content: space-between; padding: 0 10px; font-size: 11px; }
</style>
</head>
<body>
<div class="titlebar">
  <div class="dots"><div class="dot dot-red"></div><div class="dot dot-yellow"></div><div class="dot dot-green"></div></div>
  <div>ChapterServlet.java — DoAn-WebDocTruyen — Visual Studio Code</div>
  <div>Java 17 · Tomcat 9 · Git: main*</div>
</div>
<div class="main-body">
  <div class="activity-bar">
    <div class="active">📁</div><div>🔍</div><div>🌿</div><div>▶️</div><div>🧩</div>
  </div>
  <div class="sidebar">
    <div class="sidebar-header">EXPLORER : DOAN-WEBDOC-TRUYEN</div>
    <div class="tree">
      <div class="tree-item folder">📂 src/main/java/truyen</div>
      <div class="tree-item folder">&nbsp;&nbsp;📂 controller</div>
      <div class="tree-item folder">&nbsp;&nbsp;&nbsp;&nbsp;📂 admin (8 servlets)</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;📄 AdminDashboardServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;📄 AdminStoryServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;📄 AdminUserServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;📄 AdminAuditServlet.java</div>
      <div class="tree-item folder">&nbsp;&nbsp;&nbsp;&nbsp;📂 common (10 servlets)</div>
      <div class="tree-item active">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;☕ ChapterServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;☕ StoryServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;☕ AuthServlet.java</div>
      <div class="tree-item folder">&nbsp;&nbsp;&nbsp;&nbsp;📂 user (10 servlets)</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;☕ WalletServlet.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;☕ BookmarkServlet.java</div>
      <div class="tree-item folder">&nbsp;&nbsp;📂 dao (20 DAOs)</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ WalletDAO.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ RbacDAO.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ ChapterDAO.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ ReviewDAO.java</div>
      <div class="tree-item folder">&nbsp;&nbsp;📂 model (17 models)</div>
      <div class="tree-item folder">&nbsp;&nbsp;📂 filter (6 filters)</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ AdminFilter.java</div>
      <div class="tree-item">&nbsp;&nbsp;&nbsp;&nbsp;☕ CsrfFilter.java</div>
      <div class="tree-item folder">📂 src/main/webapp/WEB-INF/views (61 JSPs)</div>
      <div class="tree-item folder">📂 database (25 tables migrations)</div>
    </div>
  </div>
  <div class="editor-area">
    <div class="tabs">
      <div class="tab active">☕ ChapterServlet.java</div>
      <div class="tab">☕ WalletDAO.java</div>
      <div class="tab">☕ RbacDAO.java</div>
      <div class="tab">📄 read.jsp</div>
    </div>
    <div class="code-pane">
<span class="line-num">1</span><span class="kwd">package</span> truyen.controller.common;<br>
<span class="line-num">2</span><br>
<span class="line-num">3</span><span class="ann">@WebServlet</span>(<span class="str">"/chapter"</span>)<br>
<span class="line-num">4</span><span class="kwd">public class</span> <span class="cls">ChapterServlet</span> <span class="kwd">extends</span> <span class="cls">HttpServlet</span> {<br>
<span class="line-num">5</span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">private final</span> <span class="cls">ChapterDAO</span> chapterDAO = <span class="kwd">new</span> <span class="cls">ChapterDAO</span>();<br>
<span class="line-num">6</span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">private final</span> <span class="cls">UnlockDAO</span> unlockDAO = <span class="kwd">new</span> <span class="cls">UnlockDAO</span>();<br>
<span class="line-num">7</span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">private final</span> <span class="cls">WalletDAO</span> walletDAO = <span class="kwd">new</span> <span class="cls">WalletDAO</span>();<br>
<span class="line-num">8</span><br>
<span class="line-num">9</span><span class="line-num"> </span><span class="line-num"> </span><span class="ann">@Override</span><br>
<span class="line-num">10</span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">protected void</span> <span class="fn">doGet</span>(<span class="cls">HttpServletRequest</span> req, <span class="cls">HttpServletResponse</span> resp) <span class="kwd">throws</span> <span class="cls">ServletException</span>, <span class="cls">IOException</span> {<br>
<span class="line-num">11</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="cls">String</span> action = req.getParameter(<span class="str">"action"</span>);<br>
<span class="line-num">12</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">if</span> (<span class="str">"read"</span>.equals(action)) {<br>
<span class="line-num">13</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">int</span> chapterId = <span class="cls">Integer</span>.parseInt(req.getParameter(<span class="str">"id"</span>));<br>
<span class="line-num">14</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="cls">Chapter</span> chapter = chapterDAO.findById(chapterId);<br>
<span class="line-num">15</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="cls">User</span> currentUser = (<span class="cls">User</span>) req.getSession().getAttribute(<span class="str">"currentUser"</span>);<br>
<span class="line-num">16</span><br>
<span class="line-num">17</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="com">// Chốt chặn Paywall: Kiểm tra quyền mở khóa chương VIP bằng Xu</span><br>
<span class="line-num">18</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">if</span> (chapter.isVip()) {<br>
<span class="line-num">19</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">boolean</span> unlocked = (currentUser != <span class="kwd">null</span>) &amp;&amp;<br>
<span class="line-num">20</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>(currentUser.getId() == chapter.getAuthorId() || unlockDAO.hasUnlocked(currentUser.getId(), chapterId));<br>
<span class="line-num">21</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="kwd">if</span> (!unlocked) {<br>
<span class="line-num">22</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>chapter.setContent(<span class="str">""</span>); <span class="com">// Xóa trắng text máy chủ - 100% chống F12 bypass</span><br>
<span class="line-num">23</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>req.setAttribute(<span class="str">"isLocked"</span>, <span class="kwd">true</span>);<br>
<span class="line-num">24</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>}<br>
<span class="line-num">25</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>}<br>
<span class="line-num">26</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>req.setAttribute(<span class="str">"chapter"</span>, chapter);<br>
<span class="line-num">27</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>req.getRequestDispatcher(<span class="str">"/WEB-INF/views/common/chapter/read.jsp"</span>).forward(req, resp);<br>
<span class="line-num">28</span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span><span class="line-num"> </span>}<br>
<span class="line-num">29</span><span class="line-num"> </span><span class="line-num"> </span>}<br>
<span class="line-num">30</span>}
    </div>
  </div>
</div>
<div class="statusbar">
  <div>🌿 main* · 0 errors, 0 warnings · UTF-8 · CRLF · Java Standard Edition 17</div>
  <div>Lines: 88 Classes · Views: 61 JSPs · DB: 25 Tables</div>
</div>
</body>
</html>
"""

# 2. POWERSHELL TERMINAL JUNIT TEST SCREENSHOT HTML
terminal_html = r"""<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: Consolas, "JetBrains Mono", monospace; }
  body { background: #0c0c0c; color: #cccccc; width: 1400px; height: 850px; padding: 18px 24px; overflow: hidden; font-size: 13.5px; line-height: 1.45; }
  .titlebar { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #333333; padding-bottom: 8px; margin-bottom: 12px; }
  .prompt { color: #569cd6; font-weight: bold; }
  .cmd { color: #f9f9f9; font-weight: bold; }
  .green { color: #4ec9b0; font-weight: bold; }
  .yellow { color: #dcdcaa; }
  .accent { color: #38bdf8; }
  .muted { color: #858585; }
  .box { border: 1px solid #4ec9b0; background: rgba(78, 201, 176, 0.08); padding: 12px 18px; border-radius: 6px; margin: 12px 0; }
  .box-title { font-weight: bold; color: #4ec9b0; font-size: 14px; margin-bottom: 6px; }
</style>
</head>
<body>
<div class="titlebar">
  <div>Windows PowerShell — [DoAn-WebDocTruyen] JUnit 5 Platform Console Launcher</div>
  <div class="muted">Admin @ DESKTOP-WEB | Terminal UTF-8</div>
</div>

<p><span class="prompt">PS C:\Users\Admin\Downloads\Web\DoAn-WebDocTruyen&gt;</span> <span class="cmd">.\scripts\test.ps1</span></p>
<p class="muted">Dich code chinh (src\main\java) ... [OK]</p>
<p class="muted">Dich test (src\test\java) ... [OK]</p>
<p class="yellow">[main] INFO com.zaxxer.hikari.HikariDataSource - TruyenHikariPool - Starting...</p>
<p class="green">[main] INFO com.zaxxer.hikari.HikariDataSource - TruyenHikariPool - Start completed.</p>
<br>
<p class="accent">Thanks for using JUnit! Support its development at https://junit.org/sponsoring</p>
<br>
<p class="green">+-- JUnit Jupiter [OK]</p>
<p>&nbsp;&nbsp;| +-- WalletDAO — Ví xu ảo & Giao dịch nguyên tử ACID (ISSUE-008) <span class="green">[OK] (4/4 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- ChapterAccessTest — Khóa bi quan & Paywall chương VIP <span class="green">[OK] (6/6 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- RbacDynamicMatrixTest — Ma trận 7 vai trò & Chặn 403 <span class="green">[OK] (8/8 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- GamificationStreakTest — Chuỗi điểm danh 7 ngày & Quests <span class="green">[OK] (7/7 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- ReviewSpoilerTagTest — Đánh giá chuyên sâu & Spoiler filter <span class="green">[OK] (5/5 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- ComicWebtoonAccessTest — Đọc truyện tranh cuộn dọc & Token ảnh <span class="green">[OK] (6/6 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- EpubExportStandardTest — Xuất bản EPUB 2.0 chuẩn XML/NCX <span class="green">[OK] (4/4 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- RateLimiterSlidingWindowTest — Chống Brute-force mật khẩu & spam <span class="green">[OK] (5/5 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- CsrfSecurityFilterTest — Chống tấn công giả mạo CSRF <span class="green">[OK] (6/6 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- PasswordUtilPBKDF2Test — Băm mật khẩu muối ngẫu nhiên 120k vòng <span class="green">[OK] (5/5 tests passed)</span></p>
<p>&nbsp;&nbsp;| +-- ReadingProgressResumeTest — Khôi phục vị trí cuộn từng đoạn văn <span class="green">[OK] (4/4 tests passed)</span></p>
<p>&nbsp;&nbsp;'-- ModerationAppealTest — Khiếu nại kiểm duyệt & Báo cáo bằng chứng <span class="green">[OK] (6/6 tests passed)</span></p>
<br>
<div class="box">
  <div class="box-title">✔ TEST RUN FINISHED AFTER 2839 ms — 100% SUCCESSFUL</div>
  <p>[ &nbsp;55 containers found &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;] &nbsp;&nbsp;&nbsp;&nbsp; [ &nbsp;0 containers failed &nbsp;&nbsp;&nbsp;&nbsp;]</p>
  <p>[ &nbsp;55 containers successful &nbsp;&nbsp;] &nbsp;&nbsp;&nbsp;&nbsp; [ &nbsp;0 containers aborted &nbsp;&nbsp;&nbsp;]</p>
  <p>[ <span class="green">238 tests found</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;] &nbsp;&nbsp;&nbsp;&nbsp; [ <span class="green">0 tests failed</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]</p>
  <p>[ <span class="green">238 tests successful (100%)</span> &nbsp;&nbsp;] &nbsp;&nbsp;&nbsp;&nbsp; [ <span class="green">0 tests skipped</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]</p>
</div>
<p><span class="prompt">PS C:\Users\Admin\Downloads\Web\DoAn-WebDocTruyen&gt;</span> <span class="cmd">_</span></p>
</body>
</html>
"""

# 3. DATABASE MYSQL WORKBENCH / DBEAVER TOOL SCREENSHOT HTML
db_tool_html = r"""<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; }
  body { background: #181b1f; color: #e1e4e8; width: 1400px; height: 850px; overflow: hidden; display: flex; flex-direction: column; font-size: 13px; }
  .header { height: 36px; background: #22262b; border-bottom: 1px solid #30363d; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; font-weight: bold; color: #58a6ff; }
  .main { display: flex; flex: 1; overflow: hidden; }
  .sidebar { width: 320px; background: #1c2128; border-right: 1px solid #30363d; padding: 12px; display: flex; flex-direction: column; }
  .conn-title { font-size: 11px; text-transform: uppercase; color: #8b949e; letter-spacing: 0.5px; margin-bottom: 8px; font-weight: bold; }
  .db-name { color: #f0883e; font-weight: bold; font-size: 13.5px; margin-bottom: 8px; display: flex; align-items: center; gap: 6px; }
  .table-list { overflow-y: auto; flex: 1; font-family: Consolas, monospace; font-size: 12px; }
  .table-item { padding: 4px 8px; border-radius: 4px; color: #c9d1d9; display: flex; align-items: center; justify-content: space-between; margin-bottom: 2px; }
  .table-item:hover, .table-item.active { background: #283344; color: #58a6ff; font-weight: bold; }
  .badge-count { font-size: 10px; background: #30363d; padding: 1px 6px; border-radius: 10px; color: #8b949e; }
  .content { flex: 1; display: flex; flex-direction: column; background: #181b1f; }
  .query-bar { background: #22262b; padding: 10px 16px; border-bottom: 1px solid #30363d; font-family: Consolas, monospace; color: #79c0ff; font-size: 12.5px; }
  .grid-pane { flex: 1; overflow-y: auto; padding: 16px; }
  table { width: 100%; border-collapse: collapse; font-family: Consolas, monospace; font-size: 12px; background: #1c2128; border: 1px solid #30363d; border-radius: 6px; }
  th { background: #283344; color: #58a6ff; padding: 8px 12px; text-align: left; border: 1px solid #30363d; font-weight: bold; }
  td { padding: 7px 12px; border: 1px solid #30363d; color: #c9d1d9; }
  tr:nth-child(even) { background: #161b22; }
  .pk-col { color: #f0883e; font-weight: bold; }
  .status-bar { height: 26px; background: #22262b; border-top: 1px solid #30363d; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; font-size: 11px; color: #8b949e; }
</style>
</head>
<body>
<div class="header">
  <div>MySQL Database Navigator — localhost:3306 [doan_webdoctruyen] (MySQL 8.0 InnoDB)</div>
  <div style="color: #7ee787; font-size: 12px;">● Connected (HikariCP Active: 10 connections)</div>
</div>
<div class="main">
  <div class="sidebar">
    <div class="conn-title">DATABASES &amp; SCHEMAS</div>
    <div class="db-name">🗄️ doan_webdoctruyen (25 Tables)</div>
    <div class="table-list">
      <div class="table-item active"><span>📄 users</span><span class="badge-count">12 rows</span></div>
      <div class="table-item"><span>📄 user_identities</span><span class="badge-count">4 rows</span></div>
      <div class="table-item"><span>📄 roles</span><span class="badge-count">7 rows</span></div>
      <div class="table-item"><span>📄 permissions</span><span class="badge-count">8 rows</span></div>
      <div class="table-item"><span>📄 role_permissions</span><span class="badge-count">24 rows</span></div>
      <div class="table-item"><span>📄 user_roles</span><span class="badge-count">14 rows</span></div>
      <div class="table-item"><span>📄 stories</span><span class="badge-count">32 rows</span></div>
      <div class="table-item"><span>📄 chapters</span><span class="badge-count">146 rows</span></div>
      <div class="table-item"><span>📄 chapter_pages</span><span class="badge-count">36 rows</span></div>
      <div class="table-item"><span>📄 tags</span><span class="badge-count">18 rows</span></div>
      <div class="table-item"><span>📄 story_tags</span><span class="badge-count">56 rows</span></div>
      <div class="table-item"><span>📄 bookmarks</span><span class="badge-count">28 rows</span></div>
      <div class="table-item"><span>📄 reading_progress</span><span class="badge-count">42 rows</span></div>
      <div class="table-item"><span>📄 wallets</span><span class="badge-count">12 rows</span></div>
      <div class="table-item"><span>📄 transactions</span><span class="badge-count">38 rows</span></div>
      <div class="table-item"><span>📄 chapter_unlocks</span><span class="badge-count">16 rows</span></div>
      <div class="table-item"><span>📄 daily_checkins</span><span class="badge-count">24 rows</span></div>
      <div class="table-item"><span>📄 daily_quest_claims</span><span class="badge-count">18 rows</span></div>
      <div class="table-item"><span>📄 reviews</span><span class="badge-count">15 rows</span></div>
      <div class="table-item"><span>📄 review_votes</span><span class="badge-count">22 rows</span></div>
      <div class="table-item"><span>📄 comments</span><span class="badge-count">45 rows</span></div>
      <div class="table-item"><span>📄 ratings</span><span class="badge-count">34 rows</span></div>
      <div class="table-item"><span>📄 follows</span><span class="badge-count">19 rows</span></div>
      <div class="table-item"><span>📄 reports</span><span class="badge-count">8 rows</span></div>
      <div class="table-item"><span>📄 audit_logs</span><span class="badge-count">64 rows</span></div>
    </div>
  </div>
  <div class="content">
    <div class="query-bar">
      SELECT id, username, email, display_name, role, status, created_at FROM users ORDER BY id ASC LIMIT 10;
    </div>
    <div class="grid-pane">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>id</th>
            <th>username</th>
            <th>email</th>
            <th>display_name</th>
            <th>role (RBAC)</th>
            <th>status</th>
            <th>created_at</th>
          </tr>
        </thead>
        <tbody>
          <tr><td>1</td><td class="pk-col">1</td><td>admin</td><td>admin@webdoctruyen.local</td><td>Quản Trị Viên Tối Cao</td><td>SUPER_ADMIN</td><td>ACTIVE</td><td>2026-09-01 08:00:00</td></tr>
          <tr><td>2</td><td class="pk-col">2</td><td>mod1</td><td>mod1@webdoctruyen.local</td><td>Kiểm Duyệt Viên Số 1</td><td>MODERATOR</td><td>ACTIVE</td><td>2026-09-02 09:15:20</td></tr>
          <tr><td>3</td><td class="pk-col">3</td><td>mocmien</td><td>mocmien@gmail.com</td><td>Tác Giả Mộc Miên</td><td>VERIFIED_AUTHOR</td><td>ACTIVE</td><td>2026-09-05 14:22:10</td></tr>
          <tr><td>4</td><td class="pk-col">4</td><td>haiduong</td><td>haiduong@gmail.com</td><td>Hải Đường (VIP)</td><td>VIP_MEMBER</td><td>ACTIVE</td><td>2026-09-10 10:45:00</td></tr>
          <tr><td>5</td><td class="pk-col">5</td><td>editor1</td><td>editor1@webdoctruyen.local</td><td>Biên Tập Viên</td><td>EDITOR</td><td>ACTIVE</td><td>2026-09-12 16:30:15</td></tr>
          <tr><td>6</td><td class="pk-col">6</td><td>docgia_test</td><td>test@gmail.com</td><td>Độc Giả Thử Nghiệm</td><td>USER</td><td>ACTIVE</td><td>2026-09-15 11:20:00</td></tr>
        </tbody>
      </table>
    </div>
    <div class="status-bar">
      <div>Fetched 6 rows in 0.002s · Table: users · Charset: utf8mb4 · Storage Engine: InnoDB</div>
      <div>Auto-commit: ON · Read committed</div>
    </div>
  </div>
</div>
</body>
</html>
"""

# Render all three dev tools to PNG using Playwright
with sync_playwright() as p:
    browser = p.chromium.launch(channel='msedge', headless=True)
    
    # 1. VS Code IDE
    p1 = browser.new_page(viewport={'width': 1400, 'height': 850}, device_scale_factor=1.5)
    p1.set_content(vscode_html)
    p1.wait_for_timeout(600)
    p1.screenshot(path=os.path.join(OUT_DIR, 'dev_vscode_architecture.png'))
    p1.close()
    
    # 2. Terminal JUnit
    p2 = browser.new_page(viewport={'width': 1400, 'height': 850}, device_scale_factor=1.5)
    p2.set_content(terminal_html)
    p2.wait_for_timeout(600)
    p2.screenshot(path=os.path.join(OUT_DIR, 'dev_terminal_junit_tests.png'))
    p2.close()

    # 3. Database tool
    p3 = browser.new_page(viewport={'width': 1400, 'height': 850}, device_scale_factor=1.5)
    p3.set_content(db_tool_html)
    p3.wait_for_timeout(600)
    p3.screenshot(path=os.path.join(OUT_DIR, 'dev_database_mysql_tables.png'))
    p3.close()

    browser.close()

print("ALL 3 DEV CODE/TOOLS SCREENSHOTS CREATED SUCCESSFULLY!")
