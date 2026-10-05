# 🗺️ SƠ ĐỒ KIẾN TRÚC & TUẦN TỰ HỆ THỐNG (SYSTEM DIAGRAMS)

> **Tài liệu tổng hợp toàn bộ sơ đồ phân tích thiết kế hệ thống bằng định dạng chuẩn Mermaid: ERD 18 bảng CSDL, Kiến trúc MVC Model 2, Vòng đời xử lý Request, 6 Sơ đồ Tuần tự (Sequence Diagrams) chuyên sâu và Sơ đồ Use Case phân quyền.**  
> Cập nhật: 2026-10-05 · **Phiên bản:** Hoàn thiện 25 Module Công nghệ Nâng cao (ISSUE-001 → ISSUE-026).

---

## 📑 MỤC LỤC
1. [ERD — Sơ đồ Thực thể Liên kết 18 Bảng CSDL](#1-erd--sơ-đồ-thực-thể-liên-kết-18-bảng-csdl)
2. [Kiến trúc Phân tầng MVC Model 2](#2-kiến-trúc-phân-tầng-mvc-model-2)
3. [Vòng đời Xử lý Yêu cầu (Request Lifecycle)](#3-vòng-đời-xử-lý-yêu-cầu-request-lifecycle)
4. [Hệ thống 6 Sơ đồ Tuần tự Chuyên sâu (Sequence Diagrams)](#4-hệ-thống-6-sơ-đồ-tuần-tự-chuyên-sâu-sequence-diagrams)
   - [4.1. Luồng Mở Khóa Chương VIP bằng Xu Ảo](#41-luồng-mở-khóa-chương-vip-bằng-xu-ảo)
   - [4.2. Luồng Đánh Giá Chuyên Sâu & Cảnh Báo Spoiler](#42-luồng-đánh-giá-chuyên-sâu--cảnh-báo-spoiler)
   - [4.3. Luồng Trò Chơi Hóa & Nhiệm Vụ Hằng Ngày](#43-luồng-trò-chơi-hóa--nhiệm-vụ-hằng-ngày)
   - [4.4. Luồng Bảng Xếp Hạng Truyện & Tác Giả](#44-luồng-bảng-xếp-hạng-truyện--tác-giả)
   - [4.5. Luồng Bắt Buộc Đăng Nhập khi Đọc Chương (Auth Guard)](#45-luồng-bắt-buộc-đăng-nhập-khi-đọc-chương-auth-guard)
   - [4.6. Luồng Thông Báo Tác Giả Xuất Bản & Cập Nhật Chương](#46-luồng-thông-báo-tác-giả-xuất-bản--cập-nhật-chương)
5. [Cơ chế Lắp ghép Giao diện (5 Layouts Architecture)](#5-cơ-chế-lắp-ghép-giao-diện-5-layouts-architecture)
6. [Vòng đời Dữ liệu theo Scope (Page, Request, Session, Application)](#6-vòng-đời-dữ-liệu-theo-scope-page-request-session-application)
7. [Sơ đồ Use Case Phân quyền Người dùng](#7-sơ-đồ-use-case-phân-quyền-người-dùng)

---

## 1. ERD — Sơ đồ Thực thể Liên kết 18 Bảng CSDL

```mermaid
erDiagram
    users {
        int id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar display_name
        varchar avatar_url
        enum role "USER|ADMIN"
        enum status "ACTIVE|BANNED"
        datetime created_at
    }
    user_identities {
        int id PK
        int user_id FK
        varchar provider "GOOGLE"
        varchar provider_uid UK
        varchar email
    }
    stories {
        int id PK
        varchar title
        varchar slug UK
        int author_id FK
        text description
        varchar cover_url
        enum status "DRAFT|PUBLISHED|DELETED"
        enum progress "ONGOING|COMPLETED"
        int view_count
        int rating_sum
        int rating_count
    }
    chapters {
        int id PK
        int story_id FK
        int chapter_no
        varchar title
        mediumtext content
        bool is_vip
        int coin_price
        datetime created_at
    }
    tags {
        int id PK
        varchar name UK
        varchar slug UK
        varchar description
    }
    story_tags {
        int story_id PK_FK
        int tag_id PK_FK
    }
    comments {
        int id PK
        int story_id FK
        int chapter_id FK "NULL được"
        int user_id FK
        int parent_id FK "NULL = cha"
        varchar content
        int likes_count
        enum status "VISIBLE|HIDDEN"
        datetime created_at
    }
    bookmarks {
        int user_id PK_FK
        int story_id PK_FK
        int last_chapter_id FK "NULL được"
        datetime updated_at
    }
    ratings {
        int user_id PK_FK
        int story_id PK_FK
        tinyint score "1..5"
        datetime updated_at
    }
    follows {
        int follower_id PK_FK
        int author_id PK_FK
        datetime created_at
    }
    notifications {
        int id PK
        int user_id FK "người nhận"
        int story_id FK "NULL được"
        int chapter_id FK "NULL được"
        varchar type "NEW_CHAPTER|UPDATE_CHAPTER|SYSTEM"
        varchar message
        bool is_read
        datetime created_at
    }
    reports {
        int id PK
        int reporter_id FK
        varchar target_type "STORY|COMMENT"
        int target_id "ID đối tượng"
        varchar category "8 loại vi phạm"
        varchar reason
        varchar status "PENDING|RESOLVED|DISMISSED"
        datetime created_at
    }
    report_evidence {
        int id PK
        int report_id FK
        varchar file_path
        datetime created_at
    }
    view_logs {
        bigint id PK
        int story_id FK
        int chapter_id FK "NULL được"
        int user_id FK "NULL = khách"
        varchar ip_address
        datetime viewed_at
    }
    wallets {
        int id PK
        int user_id FK_UK
        int balance "Số dư xu"
        int total_spent
        datetime updated_at
    }
    transactions {
        int id PK
        int sender_id FK
        int receiver_id FK
        int amount
        varchar type "TIP|UNLOCK|CHECKIN"
        varchar note
        datetime created_at
    }
    unlocked_chapters {
        int id PK
        int user_id FK
        int story_id FK
        int chapter_id FK
        int coin_price
        datetime unlocked_at
    }
    story_reviews {
        int id PK
        int story_id FK
        int user_id FK
        tinyint score "1..5"
        varchar title
        text content
        bool has_spoiler
        int helpful_count
        datetime created_at
    }
    review_votes {
        int review_id PK_FK
        int user_id PK_FK
        datetime voted_at
    }
    user_quests {
        int id PK
        int user_id FK
        varchar quest_key "DAILY_CHECKIN|READ|COMMENT"
        date quest_date
        bool completed
        datetime completed_at
    }

    users             ||--o{ stories            : "sáng tác"
    users             ||--o{ user_identities    : "liên kết OIDC"
    users             ||--o{ comments           : "bình luận"
    users             ||--o{ bookmarks          : "lưu truyện"
    users             ||--o{ ratings            : "chấm sao"
    users             ||--o{ follows            : "theo dõi"
    users             ||--o{ notifications      : "nhận thông báo"
    users             ||--o{ reports            : "báo cáo"
    users             ||--|| wallets            : "sở hữu ví xu"
    users             ||--o{ unlocked_chapters  : "mở khóa VIP"
    users             ||--o{ story_reviews      : "viết review"
    users             ||--o{ user_quests        : "làm nhiệm vụ"
    stories           ||--o{ chapters           : "có các chương"
    stories           ||--o{ story_tags         : "thuộc thể loại"
    tags              ||--o{ story_tags         : "gán cho truyện"
    stories           ||--o{ comments           : "chứa thảo luận"
    stories           ||--o{ story_reviews      : "nhận đánh giá"
    reports           ||--o{ report_evidence    : "đính kèm ảnh"
    story_reviews     ||--o{ review_votes       : "nhận bình chọn"
    chapters          ||--o{ unlocked_chapters  : "được mở khóa"
```

---

## 2. Kiến trúc Phân tầng MVC Model 2

Mũi tên chỉ hướng phụ thuộc một chiều từ trên xuống dưới — ngăn chặn tối đa việc rò rỉ logic giữa các tầng:

```mermaid
flowchart TD
    Client["🌐 Trình duyệt Độc giả / Tác giả"]

    subgraph Presentation ["1. TẦNG TRÌNH DIỄN (Web & Views)"]
        Filter["truyen.filter<br/>Auth · Admin · Csrf · Recaptcha · Encoding"]
        Ctrl["truyen.controller<br/>Servlet tiếp nhận request & điều hướng"]
        Views["WEB-INF/views/<br/>JSP Layouts & Content Partials (JSTL/EL)"]
        Assets["assets/<br/>CSS 4 tầng (Base, Components, Layouts) & JS"]
    end

    subgraph Business ["2. TẦNG THỰC THỂ & TIỆN ÍCH (Models & Utils)"]
        Models["truyen.model<br/>JavaBeans POJO chứa dữ liệu"]
        Utils["truyen.util<br/>PasswordUtil · RateLimiter · EpubWriter · DriveClient"]
    end

    subgraph DataAccess ["3. TẦNG TRUY CẬP DỮ LIỆU (DAOs)"]
        DAOs["truyen.dao<br/>PreparedStatements SQL nguyên tử & Connection Pool"]
    end

    subgraph Persistence ["4. TẦNG LƯU TRỮ CSDL"]
        MySQL[("MySQL 8.0 InnoDB<br/>utf8mb4_unicode_ci")]
    end

    Client -->|HTTP Request| Filter
    Filter -->|Cho phép qua| Ctrl
    Ctrl -->|Gọi truy vấn| DAOs
    DAOs -->|JDBC HikariCP| MySQL
    MySQL -->>|ResultSet| DAOs
    DAOs -.->|Trả thực thể| Models
    Ctrl -->|request.setAttribute| Views
    Views -.->|Đọc bằng EL| Models
    Views -->|Nạp style & script| Assets
    Views -->>|Render HTML| Client

    Ctrl -.->|Sử dụng| Utils
    DAOs -.->|Sử dụng| Utils

    style Ctrl fill:#f0863a,color:#1a0f06
    style DAOs fill:#2bb789,color:#04241a
    style Views fill:#8b7cf6,color:#fff
    style Models fill:#e8eaf0,color:#101219
```

---

## 3. Vòng đời Xử lý Yêu cầu (Request Lifecycle)

```mermaid
sequenceDiagram
    autonumber
    participant B as 🌐 Trình duyệt
    participant T as Máy chủ Tomcat
    participant E as EncodingFilter
    participant C as CsrfFilter
    participant A as AuthFilter
    participant S as Controller Servlet
    participant D as DAO Component
    participant M as CSDL MySQL
    participant V as View JSP (Layout + Mảnh)

    B->>T: Gửi HTTP Request (VD: POST /wallet?action=unlock)
    T->>E: doFilter() ép bảng mã UTF-8
    E->>C: doFilter() xác thực CSRF Token
    C->>A: doFilter() kiểm tra phiên đăng nhập
    alt Chưa đăng nhập
        A-->>B: Redirect về /auth?action=login
    else Đã đăng nhập
        A->>S: Chuyển request đến Servlet tương ứng
        S->>S: Đọc và validate tham số (id, csrf, amount...)
        S->>D: Gọi hàm nghiệp vụ (executeUnlock)
        D->>M: Bắt đầu Transaction (UPDATE wallets, INSERT unlocked_chapters)
        M-->>D: Commit thành công
        D-->>S: Trả kết quả xử lý
        S->>S: Gán request.setAttribute(...)
        S->>V: forward(request, response)
        V->>V: Render HTML qua JSTL <c:out>
        V-->>B: Trả về trang HTML hoàn chỉnh cho trình duyệt
    end
```

---

## 4. Hệ thống 6 Sơ đồ Tuần tự Chuyên sâu (Sequence Diagrams)

### 4.1. Luồng Mở Khóa Chương VIP bằng Xu Ảo
> 🖼️ *File sơ đồ hình ảnh đính kèm:* [`diagram_seq_vip.png`](diagrams/diagram_seq_vip.png)

```mermaid
sequenceDiagram
    autonumber
    actor Reader as Độc giả (User)
    participant Browser as Trình duyệt (UI)
    participant ChapterSrv as ChapterServlet
    participant UnlockDAO as UnlockDAO
    participant WalletDAO as WalletDAO
    participant DB as CSDL MySQL

    Reader->>Browser: Bấm vào đọc chương truyện
    Browser->>ChapterSrv: GET /chapter?action=read&id=61
    ChapterSrv->>ChapterSrv: Kiểm tra chapter.isVip()
    alt Chương VIP (isVip == true)
        ChapterSrv->>UnlockDAO: hasUnlocked(userId, chapterId)
        UnlockDAO->>DB: SELECT COUNT(*) FROM unlocked_chapters WHERE user_id=? AND chapter_id=?
        DB-->>UnlockDAO: false (Chưa mở khoá)
        ChapterSrv-->>Browser: Trả về giao diện Paywall (ẩn nội dung, báo giá xu)
        Reader->>Browser: Bấm "Mở khoá bằng 10 xu"
        Browser->>ChapterSrv: POST /wallet?action=unlock (chapterId, csrfToken)
        ChapterSrv->>WalletDAO: executeUnlock(userId, storyId, chapterId, coinPrice)
        WalletDAO->>DB: Bắt đầu Transaction (con.setAutoCommit(false))
        WalletDAO->>DB: UPDATE wallets SET balance = balance - coinPrice WHERE user_id=?
        WalletDAO->>DB: INSERT INTO unlocked_chapters (user_id, story_id, chapter_id, coin_price)
        WalletDAO->>DB: con.commit()
        ChapterSrv-->>Browser: Trả về JSON { success: true }
        Browser->>Browser: Tải lại trang (Reload)
        Browser->>ChapterSrv: GET /chapter?action=read&id=61
        ChapterSrv->>UnlockDAO: hasUnlocked(userId, chapterId) -> TRUE
        ChapterSrv-->>Browser: Giải mã và hiển thị đầy đủ nội dung chương truyện
    else Chương Thường (Miễn phí)
        ChapterSrv-->>Browser: Hiển thị nội dung đọc ngay lập tức
    end
```

---

### 4.2. Luồng Đánh Giá Chuyên Sâu & Cảnh Báo Spoiler
> 🖼️ *File sơ đồ hình ảnh đính kèm:* [`diagram_seq_review.png`](diagrams/diagram_seq_review.png)

```mermaid
sequenceDiagram
    autonumber
    actor Reader as Độc giả
    participant UI as Giao diện Web
    participant StorySrv as StoryServlet / ReviewDAO
    participant DB as CSDL MySQL

    Reader->>UI: Viết đánh giá (Điểm sao, Tiêu đề, Nội dung, Tích chọn Spoiler)
    Reader->>UI: Bấm "Gửi đánh giá"
    UI->>StorySrv: POST /story?action=review (score, title, content, hasSpoiler, csrfToken)
    StorySrv->>StorySrv: Kiểm tra tính hợp lệ & Chống spam
    StorySrv->>DB: INSERT INTO story_reviews (story_id, user_id, score, title, content, has_spoiler)
    StorySrv->>DB: Cập nhật rating_sum, rating_count trong stories
    StorySrv-->>UI: Redirect /story?action=detail&id=...#reviews
    UI-->>Reader: Hiển thị bài review với khung che "Cảnh báo tiết lộ nội dung (Spoiler)"
    alt Độc giả khác bấm "Xem nội dung"
        Reader->>UI: Bấm nút mở khóa Spoiler
        UI->>UI: Tháo bỏ lớp làm mờ và hiển thị toàn bộ nội dung
    end
    alt Độc giả bấm "Hữu ích" (Helpful Vote)
        Reader->>UI: Bấm nút "👍 Hữu ích"
        UI->>StorySrv: POST /story?action=voteReview (reviewId)
        StorySrv->>DB: INSERT INTO review_votes (review_id, user_id)
        StorySrv->>DB: UPDATE story_reviews SET helpful_count = helpful_count + 1
        StorySrv-->>UI: Trả về JSON { newCount: 13, voted: true }
        UI-->>Reader: Cập nhật số đếm hữu ích tức thì không tải lại trang
    end
```

---

### 4.3. Luồng Trò Chơi Hóa & Nhiệm Vụ Hằng Ngày
> 🖼️ *File sơ đồ hình ảnh đính kèm:* [`diagram_seq_gamification.png`](diagrams/diagram_seq_gamification.png)

```mermaid
sequenceDiagram
    autonumber
    actor User as Thành viên
    participant UI as Giao diện Hồ sơ / Me
    participant GameDAO as GamificationDAO
    participant WalletDAO as WalletDAO
    participant DB as CSDL MySQL

    User->>UI: Truy cập Trang cá nhân (/user?action=me)
    UI->>GameDAO: getDailyQuests(userId)
    GameDAO->>DB: Truy vấn trạng thái hoàn thành nhiệm vụ trong ngày hôm nay
    GameDAO-->>UI: Danh sách 3 nhiệm vụ: Điểm danh, Đọc chương, Bình luận
    alt Bấm Điểm danh
        User->>UI: Bấm nút "Điểm danh nhận 10 xu"
        UI->>GameDAO: POST /user?action=checkin
        GameDAO->>DB: Kiểm tra log điểm danh hôm nay
        alt Chưa điểm danh hôm nay
            GameDAO->>WalletDAO: addCoins(userId, 10, "Thưởng điểm danh hằng ngày")
            WalletDAO->>DB: UPDATE wallets SET balance = balance + 10
            GameDAO->>DB: Ghi nhận hoàn thành nhiệm vụ DAILY_CHECKIN
            GameDAO-->>UI: Báo thành công, cộng 10 xu vào ví
            UI-->>User: Hiển thị Toast chúc mừng và tích xanh nhiệm vụ
        else Đã điểm danh rồi
            GameDAO-->>UI: Báo lỗi "Hôm nay bạn đã điểm danh rồi!"
        end
    end
```

---

### 4.4. Luồng Bảng Xếp Hạng Truyện & Tác Giả
> 🖼️ *File sơ đồ hình ảnh đính kèm:* [`diagram_seq_rank.png`](diagrams/diagram_seq_rank.png)

```mermaid
sequenceDiagram
    autonumber
    actor User as Độc giả / Giảng viên
    participant UI as Giao diện Web
    participant RankSrv as RankServlet
    participant StoryDAO as StoryDAO / UserDAO
    participant DB as CSDL MySQL

    User->>UI: Bấm vào mục "Bảng Xếp Hạng" (/rank)
    alt Xem Xếp hạng Truyện
        UI->>RankSrv: GET /rank?by=stories&period=all
        RankSrv->>StoryDAO: findTopStories(limit, period)
        StoryDAO->>DB: SELECT * FROM stories WHERE status='PUBLISHED' ORDER BY view_count DESC LIMIT 20
        DB-->>StoryDAO: Danh sách Top 20 truyện hot nhất
        RankSrv-->>UI: Render giao diện top 1, 2, 3 kèm huy chương Vàng/Bạc/Đồng
    else Xem Xếp hạng Tác giả
        User->>UI: Bấm tab "Tác giả xuất sắc" (/rank?by=authors)
        UI->>RankSrv: GET /rank?by=authors
        RankSrv->>StoryDAO: findTopAuthors(limit)
        StoryDAO->>DB: SELECT u.*, SUM(s.view_count) as total_views, COUNT(f.follower_id) as followers...
        DB-->>StoryDAO: Danh sách Top tác giả theo tổng lượt đọc
        RankSrv-->>UI: Hiển thị bảng vàng tác giả kèm số lượt đọc và người theo dõi
    end
```

---

### 4.5. Luồng Bắt Buộc Đăng Nhập khi Đọc Chương (Auth Guard)

```mermaid
sequenceDiagram
    autonumber
    actor Guest as Khách (Chưa đăng nhập)
    participant Browser as Trình duyệt
    participant AuthFilter as AuthFilter (URL Filter)
    participant LoginUI as AuthServlet (Trang Đăng nhập)
    participant ChapterUI as ChapterServlet (Trang Đọc)

    Guest->>Browser: Bấm "Đọc từ đầu" hoặc chọn 1 chương
    Browser->>AuthFilter: GET /chapter?action=read&id=101
    AuthFilter->>AuthFilter: Kiểm tra sessionScope.currentUser
    alt currentUser == null (Khách chưa đăng nhập)
        AuthFilter->>AuthFilter: Lưu URL đích vào session (redirectAfterLogin = "/chapter?action=read&id=101")
        AuthFilter->>Browser: Redirect /auth?action=login kèm flash "Vui lòng đăng nhập để đọc truyện!"
        Browser->>LoginUI: Hiển thị form Đăng nhập (hoặc nút Google OIDC)
        Guest->>LoginUI: Nhập thông tin & Đăng nhập thành công
        LoginUI->>Browser: Chuyển hướng người dùng về đúng URL đích đã lưu (/chapter?action=read&id=101)
        Browser->>ChapterUI: Tải thẳng vào chương truyện mà không bắt người dùng tìm lại từ đầu
    else currentUser != null (Đã đăng nhập)
        AuthFilter->>ChapterUI: Cho phép request đi qua (chain.doFilter)
        ChapterUI-->>Browser: Hiển thị giao diện đọc chương bình thường
    end
```

---

### 4.6. Luồng Thông Báo Tác Giả Xuất Bản & Cập Nhật Chương

```mermaid
sequenceDiagram
    autonumber
    actor Author as Tác giả
    participant Editor as ChapterServlet (Soạn thảo)
    participant ChapterDAO as ChapterDAO
    participant NotifDAO as NotificationDAO
    participant DB as CSDL MySQL
    actor Follower as Độc giả theo dõi

    alt Đăng chương mới (Create Chapter)
        Author->>Editor: Bấm "Lưu chương" (isCreate = true)
        Editor->>ChapterDAO: insert(chapter)
        Editor->>NotifDAO: notifyFollowers(story, chapter, isUpdate = false)
        NotifDAO->>DB: Lấy danh sách ID người theo dõi tác giả (follows) + người lưu truyện (bookmarks)
        NotifDAO->>DB: Loại bỏ ID của chính tác giả (tránh tự thông báo cho mình)
        NotifDAO->>DB: INSERT BATCH vào bảng notifications (type='NEW_CHAPTER', icon='📖')
        NotifDAO-->>Follower: Hiển thị chấm đỏ thông báo "Tác giả vừa đăng chương mới..."
    else Sửa chương (Edit Chapter)
        Author->>Editor: Bấm "Lưu chương" (isCreate = false, notifyFollowers checkbox = true)
        Editor->>ChapterDAO: update(chapter)
        alt Tác giả có tích chọn "Gửi thông báo cập nhật"
            Editor->>NotifDAO: notifyFollowers(story, chapter, isUpdate = true)
            NotifDAO->>DB: INSERT BATCH vào notifications (type='UPDATE_CHAPTER', icon='📝')
            NotifDAO-->>Follower: Hiển thị thông báo "Tác giả vừa cập nhật nội dung chương..."
        else Tác giả không tích chọn (Sửa lỗi nhỏ)
            Editor->>Editor: Không gửi thông báo để tránh gây phiền hà cho độc giả
        end
    end
```

---

## 5. Cơ chế Lắp ghép Giao diện (5 Layouts Architecture)

```mermaid
flowchart TD
    S["Servlet Controller<br/>gán contentPage"] --> LAY

    subgraph LAY ["layout/main.jsp (Wrapper)"]
        direction TB
        H["parts/head.jsp<br/>nạp 4 tầng CSS"]
        N["parts/nav.jsp<br/>menu kính mờ"]
        SLOT["🔲 jsp:include contentPage"]
        FO["parts/footer.jsp"]
        H --> N --> SLOT --> FO
    end

    SLOT -.->|Ghép vào| P1["common/home.jsp"]
    SLOT -.->|hoặc| P2["common/story/list.jsp"]
    SLOT -.->|hoặc| P3["user/me.jsp"]

    LAY --> OUT["Trang HTML hoàn chỉnh gửi về Client"]

    style SLOT fill:#f0863a,color:#1a0f06
```

**Bảng quyết định sử dụng 5 Layouts:**
- `main.jsp`: Khung mặc định có Nav + Footer (Trang chủ, Kho truyện, Bảng xếp hạng, Hồ sơ).
- `auth.jsp`: Khung thẻ căn giữa màn hình, không Nav (Đăng nhập, Đăng ký, Quên mật khẩu).
- `reader.jsp`: Khung đọc truyện chuyên biệt toàn màn hình, thanh công cụ font/cỡ chữ, chống mỏi mắt.
- `editor.jsp`: Khung sáng tác chương truyện văn học, đếm từ trực tiếp, phím tắt văn học.
- `admin.jsp`: Khung quản trị có Sidebar bên trái và bảng số liệu.

---

## 6. Vòng đời Dữ liệu theo Scope (Page, Request, Session, Application)

```mermaid
flowchart TD
    subgraph APP ["1. application Scope (ServletContext)"]
        direction TB
        A1["uploadDir · allTags · pageSize<br/>⏱ Sống suốt thời gian chạy server<br/>👥 Toàn bộ người dùng truy cập chung"]

        subgraph SES ["2. session Scope (HttpSession)"]
            direction TB
            S1["currentUser · csrfToken · redirectAfterLogin<br/>⏱ Sống đến khi đăng xuất hoặc hết hạn 60 phút<br/>👤 Dành riêng cho từng người dùng (Cookie JSESSIONID)"]

            subgraph REQ ["3. request Scope (HttpServletRequest)"]
                direction TB
                R1["story · chapters · reviews · flashMessage<br/>⏱ Sống trong đúng 1 lượt tải trang<br/>➡️ Tồn tại qua forward, biến mất khi redirect"]

                subgraph PG ["4. page Scope (PageContext)"]
                    P1["Biến lặp trong c:forEach (var='c')<br/>⏱ Sống trong đúng 1 trang JSP hiện tại"]
                end
            end
        end
    end

    style PG fill:#e8eaf0,color:#101219
    style REQ fill:#cde9ff,color:#101219
    style SES fill:#d7f5e9,color:#101219
    style APP fill:#ffe4cc,color:#101219
```

---

## 7. Sơ đồ Use Case Phân quyền Người dùng

```mermaid
flowchart LR
    K(["👤 Khách"])
    TV(["👤 Thành viên"])
    TG(["✍️ Tác giả"])
    AD(["🛡️ Admin"])

    subgraph UC ["Hệ Thống Web Đọc Truyện"]
        U1["Khám phá kho truyện & Bảng xếp hạng"]
        U2["Tìm kiếm FULLTEXT nội dung chương"]
        U3["Đăng ký & Đăng nhập (Google OIDC)"]
        U4["Đọc chương truyện & Lưu vị trí dở"]
        U5["Đánh dấu truyện & Nhận thông báo"]
        U6["Đánh giá chuyên sâu & Bật/Tắt Spoiler"]
        U7["Điểm danh hằng ngày & Nhận xu ảo"]
        U8["Mở khóa chương VIP bằng xu"]
        U9["Đăng truyện & Cấu hình chương VIP"]
        U10["Soạn thảo chương & Soát lỗi chính tả"]
        U11["Sao lưu truyện lên Google Drive"]
        U12["Xem biểu đồ thống kê lượt đọc 14 ngày"]
        U13["Quản lý người dùng, khóa vi phạm"]
        U14["Duyệt báo cáo & Xem bằng chứng ảnh"]
        U15["Kiểm duyệt & Ẩn bình luận rác"]
    end

    K --> U1 & U2 & U3
    TV --> U4 & U5 & U6 & U7 & U8
    TG --> U9 & U10 & U11 & U12
    AD --> U13 & U14 & U15

    style AD fill:#e5576f,color:#fff
    style TG fill:#f0863a,color:#1a0f06
    style TV fill:#2bb789,color:#04241a
```

---

## 8. Cách Xuất Bản Sơ Đồ để Thuyết Minh Đồ Án
1. **Dùng các file ảnh vẽ sẵn:** Xem tại thư mục `docs/diagrams/` (`diagram_seq_vip.png`, `diagram_seq_review.png`, `diagram_seq_gamification.png`, `diagram_seq_rank.png`).
2. **Xem trực tiếp trên GitHub / Markdown Preview:** GitHub tự động biên dịch toàn bộ các khối mã Mermaid trên thành biểu đồ tương tác sắc nét.
3. **Chỉnh sửa / Xuất độ phân giải cao:** Truy cập [Mermaid Live Editor](https://mermaid.live), sao chép mã biểu đồ và xuất dưới định dạng SVG/PNG sắc nét phục vụ in báo cáo đồ án.
