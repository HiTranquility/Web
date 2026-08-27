# 🗂️ Danh mục file — ch06-demo

Mở file này khi không nhớ **file nào nằm ở đâu, làm gì**.
Đường dẫn tính từ `temp/ch06-demo/`.

---

## Cây thư mục

```
ch06-demo/
│
├── demo.ps1                    ▶ CHẠY CÁI NÀY để demo
├── run.ps1                     ▶ chạy thường (demo.ps1 gọi vào đây)
├── verify-mapping.ps1          🔍 kiểm số dòng trong bảng ánh xạ
├── README.md                   mục lục ngắn, trỏ sang ../docs/
│
├── tools/
│   └── DevServer.java          Tomcat nhúng — chỉ dùng lúc dev, KHÔNG phải bài
│
└── src/main/
    ├── java/murach/
    │   ├── business/
    │   │   └── User.java           📗 slide 4-5  JavaBean
    │   ├── email/                  📗 package CỦA SÁCH
    │   │   └── EmailListServlet.java   slide 7-8  servlet điều khiển
    │   └── demo/                   ✋ package MÌNH THÊM (không có trong sách)
    │       └── ErrorTestServlet.java   mở /404 /403 /500
    │
    └── webapp/
        ├── index.jsp               📗 slide 28  trang chính
        ├── thanks.jsp              📗 slide 30  trang cảm ơn
        ├── error_404.jsp           📗 slide 35
        ├── error_500.jsp           📗 slide 35
        ├── error_403.jsp           ✋ KHÔNG có trong slide
        │
        ├── styles/main.css         CSS
        │
        ├── demo/                   ✋ khu demo — xoá cả folder vẫn chạy
        │   ├── index.jsp               menu bấm thử từng lỗi
        │   └── loi_el.jsp              dựng lại lỗi slide 34
        │
        └── WEB-INF/
            ├── web.xml             📗 slide 35  khai <error-page>
            └── includes/
                ├── header.html     📗 slide 27
                └── footer.jsp      📗 slide 27
```

**📗 = có trong sách · ✋ = mình thêm vào**

---

## Tra theo "tôi muốn xem…"

| Muốn xem | Mở file |
|----------|---------|
| JavaBean 3 điều kiện | `java/murach/business/User.java` |
| Servlet đặt attribute rồi forward | `java/murach/email/EmailListServlet.java` |
| Kiểm tra dữ liệu nhập | `EmailListServlet.java` — tìm chữ `message` |
| Trang có form, `<c:if>`, EL | `webapp/index.jsp` |
| Trang cảm ơn + nút Return | `webapp/thanks.jsp` |
| Scriptlet `<% %>` kiểu cũ | `webapp/WEB-INF/includes/footer.jsp` |
| Khai báo trang lỗi | `webapp/WEB-INF/web.xml` |
| Cách ném lỗi để test | `java/murach/demo/ErrorTestServlet.java` |
| Lỗi EL sai tên property | `webapp/demo/loi_el.jsp` |

---

## Tra theo URL — gõ cái này thì file nào chạy

| URL | File xử lý | Ra trang |
|-----|-----------|----------|
| `localhost:8080/` | `index.jsp` | Join our email list |
| `localhost:8080/emailList` | `EmailListServlet.java` | `thanks.jsp` hoặc quay lại `index.jsp` |
| `localhost:8080/404` | `ErrorTestServlet` → `web.xml` | `error_404.jsp` |
| `localhost:8080/403` | `ErrorTestServlet` → `web.xml` | `error_403.jsp` |
| `localhost:8080/500` | `ErrorTestServlet` → `web.xml` | `error_500.jsp` |
| `localhost:8080/gõ-bậy` | Tomcat → `web.xml` | `error_404.jsp` |
| `localhost:8080/demo/` | `demo/index.jsp` | menu demo |
| `localhost:8080/demo/loi_el.jsp` | `demo/loi_el.jsp` | lỗi 500 |

---

## Hai chỗ vừa đổi tên — nếu bạn nhớ tên cũ

| Tên cũ | Tên mới | Vì sao đổi |
|--------|---------|-----------|
| `webapp/temp/` | `webapp/demo/` | đường dẫn cũ là `temp/ch06-demo/src/main/webapp/temp/` — chữ **temp hai lần**, nhìn rối |
| `murach/email/ErrorTestServlet.java` | `murach/demo/ErrorTestServlet.java` | nó không liên quan gì tới email; để `murach.demo` là biết ngay "cái này mình thêm, không phải của sách" |

---

## File nào xoá được

| File / thư mục | Xoá được? |
|----------------|-----------|
| `webapp/demo/` | ✅ xoá cả folder, app vẫn chạy đủ |
| `java/murach/demo/` | ✅ nhưng mất luôn URL `/404` `/403` `/500` gọn |
| `error_403.jsp` | ✅ không có trong slide |
| `verify-mapping.ps1` | ✅ chỉ là công cụ kiểm tra |
| `tools/DevServer.java` | ❌ **không** — mất cái này là không chạy được |
| Mọi file 📗 | ❌ **không** — đó là bài |

---

## Sau khi di chuyển file thì phải làm gì

Đổi tên hay di chuyển file là **số dòng trong bảng ánh xạ lệch ngay**. Chạy:

```bash
powershell -ExecutionPolicy Bypass -File verify-mapping.ps1
```

Nó kiểm 30 vị trí và báo rõ file nào, dòng nào, cần gì, thực tế là gì.
Lệch thì sửa lại [`chapter06-mapping.md`](chapter06-mapping.md).

> Lần đổi tên vừa rồi script bắt được 3 chỗ lệch — đúng việc của nó.
