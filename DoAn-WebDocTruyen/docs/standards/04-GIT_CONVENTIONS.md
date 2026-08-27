# 🔀 Quy ước Git

Repo: <https://github.com/HiTranquility/Web> — nhánh `main`.

> ⚠️ **Repo đang PUBLIC.** Ai cũng xem được, kể cả bạn cùng lớp. Muốn đổi:
> Settings → General → Danger Zone → Change visibility.

---

## 1. Nhánh — làm một mình thì đơn giản thôi

Đồ án một người, một kỳ → **commit thẳng vào `main`** là hợp lý. Đừng bày ra
`develop` / `release` / `hotfix` cho có, nó chỉ thêm việc.

Chỉ tách nhánh khi làm thứ **có thể hỏng và cần bỏ đi**:

```bash
git switch -c thu-nghiem-upload-anh
# làm... hỏng thì:  git switch main && git branch -D thu-nghiem-upload-anh
# được thì:         git switch main && git merge thu-nghiem-upload-anh
```

Đặt tên nhánh: chữ thường, gạch ngang, tiếng Việt không dấu.

---

## 2. Mẫu commit message

```
<Việc đã làm, viết ở thể hoàn thành>

- gạch đầu dòng chi tiết
- lý do nếu quyết định không hiển nhiên

Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
```

**Dòng đầu:** dưới 72 ký tự, nói **đã làm gì**, không phải đã sửa file nào.

| ✅ Tốt | ❌ Tệ |
|--------|-------|
| `Thêm đăng nhập và AuthFilter chặn khách` | `update` |
| `Sửa lỗi CSS vỡ ở URL nhiều cấp` | `fix bug` |
| `Chia views theo layout wrapper` | `sửa file jsp` |
| `Thêm bảng bookmarks và BookmarkDAO` | `commit lần 5` |

Nhìn `git log --oneline` mà không hiểu mình đã làm gì tuần trước thì message
đang sai.

### Nên commit theo CASE

Lộ trình có 11 CASE → mỗi CASE ít nhất một commit. Dễ tìm lại, và dễ kể lúc
bảo vệ đồ án.

```
CASE 01 — Đăng nhập, đăng ký, AuthFilter
```

---

## 3. Không bao giờ commit những thứ này

`.gitignore` ở gốc repo đã chặn sẵn, nhưng phải hiểu **vì sao**:

| Không commit | Lý do |
|--------------|-------|
| `src/main/resources/db.properties` | **chứa mật khẩu MySQL** |
| `build/` `target/` | kết quả build — ai clone về chạy `run.ps1` là có |
| `.libs/` `*.jar` | thư viện tải về, ~11 MB, `run.ps1` tự tải |
| `*.class` | file biên dịch |
| `**/WEB-INF/EmailList.txt` | dữ liệu do app sinh lúc chạy |
| `.idea/` `*.iml` | cấu hình riêng máy bạn |

**Chỉ commit `db.properties.example`** — bản mẫu không có mật khẩu thật.

### Kiểm trước khi push

```bash
git diff --cached --name-only | grep -iE "db\.properties|\.jar$|\.class$|/build/|/target/"
```

Không ra gì là sạch. Ra gì thì **dừng lại**, `git reset` rồi sửa `.gitignore`.

> Lỡ commit mật khẩu rồi thì xoá file ở commit sau **là chưa đủ** — nó vẫn nằm
> trong lịch sử, ai clone về cũng đọc được. Phải **đổi mật khẩu MySQL**.

---

## 4. Quy trình thường ngày

```bash
git status                    # xem đang sửa gì
git add -A
git status --porcelain        # ĐỌC LẠI danh sách trước khi commit
git commit                    # viết message theo mẫu §2
git push origin main
```

Bước `git status --porcelain` sau `add` là thói quen đáng có: nó bắt được file
lạ lọt vào trước khi nó thành lịch sử vĩnh viễn.

### Push bị từ chối

Nghĩa là remote có commit mà máy bạn chưa có (bạn push từ máy khác):

```bash
git pull --rebase origin main
git push origin main
```

---

## 5. Thẻ đánh dấu mốc nộp bài

Trước khi nộp, đánh dấu đúng phiên bản đã nộp:

```bash
git tag -a nop-bai-cuoi-ky -m "Bản nộp đồ án cuối kỳ"
git push origin nop-bai-cuoi-ky
```

Sau đó có sửa tiếp cũng không ảnh hưởng — thầy cô vẫn xem được đúng bản lúc nộp.
