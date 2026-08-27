# 🗺️ Sơ đồ hệ thống

Sơ đồ vẽ bằng **Mermaid** — GitHub tự render, không cần cài gì.
Muốn xem trong VS Code thì cài extension *Markdown Preview Mermaid Support*.

**Mục lục**
1. [ERD — 7 bảng](#1-erd--quan-hệ-7-bảng)
2. [Kiến trúc 4 tầng](#2-kiến-trúc-4-tầng)
3. [Luồng MVC — một request đi qua đâu](#3-luồng-mvc--một-request-đi-qua-đâu)
4. [Layout lắp trang thế nào](#4-layout-lắp-trang-thế-nào)
5. [Luồng đăng nhập + Filter](#5-luồng-đăng-nhập--filter)
6. [Vòng đời dữ liệu theo scope](#6-vòng-đời-dữ-liệu-theo-scope)
7. [Sơ đồ use case](#7-sơ-đồ-use-case)

---

## 1. ERD — quan hệ 7 bảng

```mermaid
erDiagram
    users {
        int id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        enum role "USER|ADMIN"
        enum status "ACTIVE|BANNED"
    }
    stories {
        int id PK
        varchar title
        varchar slug UK
        int author_id FK
        enum status "DRAFT|PUBLISHED|DELETED"
        enum progress "ONGOING|COMPLETED"
        int view_count
    }
    chapters {
        int id PK
        int story_id FK
        int chapter_no
        varchar title
        mediumtext content
    }
    tags {
        int id PK
        varchar name UK
        varchar slug UK
    }
    story_tags {
        int story_id PK_FK
        int tag_id PK_FK
    }
    comments {
        int id PK
        int story_id FK
        int user_id FK
        varchar content
        enum status "VISIBLE|HIDDEN"
    }
    bookmarks {
        int user_id PK_FK
        int story_id PK_FK
        int last_chapter_id FK "NULL được"
    }

    users    ||--o{ stories    : "là tác giả của"
    users    ||--o{ comments   : "viết"
    users    ||--o{ bookmarks  : "đánh dấu"
    stories  ||--o{ chapters   : "gồm nhiều"
    stories  ||--o{ comments   : "nhận"
    stories  ||--o{ bookmarks  : "được lưu bởi"
    stories  ||--o{ story_tags : ""
    tags     ||--o{ story_tags : ""
    chapters ||--o{ bookmarks  : "vị trí đọc dở"
```

**Ba điểm đáng chú ý:**

| Điểm | Giải thích |
|------|-----------|
| Không có bảng `authors` | "Tác giả" là **quan hệ** `stories.author_id`, không phải loại người. Cùng tài khoản vừa viết truyện A vừa đọc truyện B |
| `story_tags` là bảng nối | Quan hệ **nhiều–nhiều** bắt buộc phải có bảng nối. Khoá chính gồm 2 cột |
| `bookmarks.last_chapter_id` cho phép NULL | Đã lưu nhưng chưa đọc chương nào. Xoá chương thì `SET NULL`, bookmark **vẫn còn** |

---

## 2. Kiến trúc 4 tầng

Mũi tên chỉ **hướng phụ thuộc** — luôn đi xuống, không bao giờ ngược lên.

```mermaid
flowchart TD
    B["🌐 Trình duyệt"]

    subgraph W ["Tầng WEB"]
        F["filter/<br/>AuthFilter · AdminFilter"]
        C["controller/<br/>servlet — điều phối"]
        V["views/<br/>JSP — chỉ hiển thị"]
    end

    subgraph L ["Tầng NGHIỆP VỤ"]
        M["model/<br/>JavaBean thuần"]
    end

    subgraph D ["Tầng DỮ LIỆU"]
        A["dao/<br/>chỉ SQL"]
        DB[("MySQL")]
    end

    U["util/<br/>DBConnection · PasswordUtil"]

    B  -->|request| F
    F  -->|cho qua| C
    C  -->|gọi| A
    A  -->|JDBC| DB
    A  -.->|trả object| M
    C  -->|setAttribute + forward| V
    V  -.->|đọc bằng EL| M
    V  -->|HTML| B
    A  -->|dùng| U
    C  -->|dùng| U

    style C fill:#f0863a,color:#1a0f06
    style A fill:#2bb789,color:#04241a
    style V fill:#8b7cf6,color:#fff
    style M fill:#e8eaf0,color:#101219
```

**Luật đọc sơ đồ này:**

- `dao/` **không có mũi tên nào chỉ lên** `controller/` hay `views/` — DAO không biết web tồn tại
- `views/` **không có mũi tên** tới `dao/` — JSP không được truy vấn database
- `model/` là thứ duy nhất **cả hai tầng cùng chạm** — nó là ngôn ngữ chung

**Phép thử:** đổi MySQL → PostgreSQL thì chỉ hộp `dao/` đổi. Nếu phải sửa chỗ khác, tầng đã bị rò.

---

## 3. Luồng MVC — một request đi qua đâu

Ví dụ: người dùng bấm vào một truyện.

```mermaid
sequenceDiagram
    autonumber
    participant B as 🌐 Trình duyệt
    participant T as Tomcat
    participant S as StoryServlet
    participant D as StoryDAO
    participant M as MySQL
    participant L as layout/main.jsp
    participant J as story/detail.jsp

    B->>T: GET /story?action=detail&id=5
    T->>S: tạo object request<br/>gọi doGet()
    S->>S: đọc action="detail", id=5
    S->>D: findById(5)
    D->>M: SELECT ... WHERE id = ?
    M-->>D: ResultSet
    D->>D: mapRow() → object Story
    D-->>S: trả Story
    S->>S: setAttribute("story", story)<br/>setAttribute("contentPage", detail.jsp)
    S->>L: forward tới LAYOUT
    L->>L: viết html head nav
    L->>J: jsp:include contentPage
    J->>J: đọc ${story.title} → viết HTML
    J-->>L: xong phần ruột
    L->>L: viết footer, đóng html
    L-->>B: HTML hoàn chỉnh
    Note over T: VỨT object request<br/>→ mọi attribute biến mất
```

**Ba điều rút ra:**

| Bước | Ý nghĩa |
|:----:|---------|
| 5–7 | Chỗ **duy nhất** chạm database. Đổi hệ quản trị chỉ sửa ở đây |
| 9 | Chỗ **duy nhất** nối hai thế giới: DAO trả object Java → servlet cất vào request |
| Cuối | Lý do request scope chết sau **mỗi** lần tải trang |

**Chú ý bước 10:** servlet forward tới **layout**, không phải tới `detail.jsp`. Layout mới là thứ được chạy, nó gọi trang nội dung vào giữa.

---

## 4. Layout lắp trang thế nào

```mermaid
flowchart LR
    S["Servlet<br/>đặt contentPage"] --> LAY

    subgraph LAY ["layout/main.jsp — dựng khung"]
        direction TB
        H["parts/head.jsp<br/>nạp 4 tầng CSS"]
        N["parts/nav.jsp<br/>thanh menu"]
        SLOT["🔲 jsp:include contentPage"]
        FO["parts/footer.jsp"]
        H --> N --> SLOT --> FO
    end

    SLOT -.->|chèn vào| P1["story/home.jsp"]
    SLOT -.->|hoặc| P2["story/list.jsp"]
    SLOT -.->|hoặc| P3["chapter/read.jsp"]

    LAY --> OUT["HTML hoàn chỉnh"]

    style SLOT fill:#f0863a,color:#1a0f06
```

**4 layout, không phải 25:**

```mermaid
flowchart TD
    Q1{"Có thanh nav<br/>như trang chủ?"}
    Q2{"Chưa đăng nhập,<br/>ô nhập giữa màn hình?"}
    Q3{"Có sidebar<br/>quản trị?"}
    Q4{"Toàn màn hình<br/>để đọc?"}

    Q1 -->|có| MAIN["layout/main.jsp<br/>8 trang"]
    Q1 -->|không| Q2
    Q2 -->|có| AUTH["layout/auth.jsp<br/>2 trang"]
    Q2 -->|không| Q3
    Q3 -->|có| ADMIN["layout/admin.jsp<br/>2 trang"]
    Q3 -->|không| Q4
    Q4 -->|có| READER["layout/reader.jsp<br/>1 trang"]
    Q4 -->|KHÔNG| MAIN

    style MAIN fill:#f0863a,color:#1a0f06
```

Nhánh cuối quan trọng nhất: **không rơi vào đâu thì dùng `main`**, không phải "tạo layout mới cho chắc".

---

## 5. Luồng đăng nhập + Filter

```mermaid
sequenceDiagram
    autonumber
    participant B as 🌐 Trình duyệt
    participant F as AuthFilter
    participant A as AuthServlet
    participant D as UserDAO
    participant P as PasswordUtil

    rect rgb(253, 238, 241)
        Note over B,P: Chưa đăng nhập mà vào trang cần quyền
        B->>F: GET /story?action=create
        F->>F: session.getAttribute("currentUser") == null
        F-->>B: redirect /auth?action=login
        Note right of F: StoryServlet KHÔNG BAO GIỜ chạy
    end

    rect rgb(230, 244, 242)
        Note over B,P: Đăng nhập
        B->>A: POST /auth (username, password)
        A->>D: findByUsername(username)
        D-->>A: User (có password_hash)
        A->>P: verify(password, hash)
        P-->>A: true
        A->>A: kiểm status != BANNED
        A->>A: session.setAttribute("currentUser", user)
        A-->>B: redirect về trang chủ
        Note right of A: redirect chứ không forward<br/>→ F5 không gửi lại form
    end

    rect rgb(230, 244, 242)
        Note over B,P: Lần sau đã có session
        B->>F: GET /story?action=create<br/>(cookie JSESSIONID đi kèm)
        F->>F: currentUser != null → cho qua
        F->>A: chain.doFilter()
    end
```

**Filter KHÔNG thay được kiểm tra quyền sở hữu:**

```mermaid
flowchart TD
    R["Sửa truyện id=6"] --> F{"AuthFilter:<br/>đã đăng nhập?"}
    F -->|chưa| L["redirect login"]
    F -->|rồi| S{"Servlet:<br/>story.authorId<br/>== currentUser.id?"}
    S -->|không, và không phải admin| E["❌ sendError 403"]
    S -->|đúng| OK["✅ cho sửa"]

    style E fill:#e5576f,color:#fff
    style OK fill:#2bb789,color:#04241a
```

Filter chỉ biết *"đã đăng nhập chưa"*. Nó **không biết** truyện id=6 là của ai. Thiếu ô màu đỏ thì sửa `?id=5` thành `?id=6` là sửa được truyện người khác.

---

## 6. Vòng đời dữ liệu theo scope

```mermaid
flowchart TD
    subgraph APP ["application — ServletContext"]
        direction TB
        A1["uploadDir · pageSize<br/>⏱ từ lúc server bật tới lúc tắt<br/>👥 MỌI người dùng chung"]

        subgraph SES ["session — HttpSession"]
            direction TB
            S1["currentUser<br/>⏱ tới khi đăng xuất / hết 60 phút<br/>👤 riêng từng người (cookie JSESSIONID)"]

            subgraph REQ ["request — HttpServletRequest"]
                direction TB
                R1["stories · contentPage · message<br/>⏱ MỘT lần tải trang<br/>➡️ sống qua forward, chết khi redirect/F5"]

                subgraph PG ["page — PageContext"]
                    P1["biến tạm trong c:forEach<br/>⏱ trong ĐÚNG một file JSP"]
                end
            end
        end
    end

    style PG fill:#e8eaf0,color:#101219
    style REQ fill:#cde9ff,color:#101219
    style SES fill:#d7f5e9,color:#101219
    style APP fill:#ffe4cc,color:#101219
```

**Càng ra ngoài, sống càng lâu và càng nhiều người thấy.**

`${dem}` không ghi rõ scope → EL tìm từ **trong ra ngoài**: page → request → session → application, gặp trước lấy trước. Đó là lý do người đăng nhập đặt tên `currentUser` chứ không phải `user` — tránh đụng với `user` ai đó lỡ đặt ở request.

Giải thích đầy đủ: [`giai-thich.md`](giai-thich.md) khu 1.

---

## 7. Sơ đồ use case

```mermaid
flowchart LR
    K(["👤 Khách"])
    TV(["👤 Thành viên"])
    AD(["👤 Admin"])

    subgraph UC ["Web Đọc Truyện"]
        U1["Xem kho truyện"]
        U2["Lọc theo thể loại"]
        U3["Đọc chương"]
        U4["Đăng ký / Đăng nhập"]
        U5["Đăng truyện"]
        U6["Sửa truyện CỦA MÌNH"]
        U7["Bình luận"]
        U8["Đánh dấu truyện"]
        U9["Tải truyện .txt"]
        U10["Gỡ truyện bất kỳ"]
        U11["Ban tài khoản"]
    end

    K --> U1 & U2 & U3 & U4
    TV --> U5 & U6 & U7 & U8 & U9
    AD --> U10 & U11

    style AD fill:#e5576f,color:#fff
    style TV fill:#2bb789,color:#04241a
```

**Phân quyền cộng dồn:** Thành viên làm được mọi thứ của Khách. Admin làm được mọi thứ của Thành viên.

Ô **"Sửa truyện CỦA MÌNH"** là chỗ cần kiểm quyền sở hữu, không chỉ kiểm vai trò.

---

## Cách xuất sơ đồ ra ảnh để dán vào báo cáo

Mermaid không dán thẳng vào Word được. Ba cách:

| Cách | Làm sao |
|------|---------|
| **Nhanh nhất** | Mở file này trên GitHub → chuột phải vào sơ đồ → Lưu ảnh |
| **Chất lượng cao** | <https://mermaid.live> → dán code → Actions → PNG/SVG |
| **Trong VS Code** | Cài *Markdown Preview Mermaid Support* → xem trước → chụp màn hình |
