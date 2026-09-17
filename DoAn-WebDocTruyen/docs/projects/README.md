# 📋 `projects/` — Việc đang giao và lỗi đang nợ

Thư mục này trả lời: **ai đang làm gì, và còn lỗi nào chưa sửa?**

`docs/` còn lại là *"viết thế nào cho đúng"* (→ [`standards/`](../standards/)) và
*"nó chạy ra sao"* (→ [`giai-thich.md`](../guides/giai-thich.md)). Ở đây là **việc**, có người
chịu trách nhiệm và có ngày.

> Cần từ lúc đồ án chuyển từ **một người** sang **nhiều người**. Làm một mình thì
> nhớ trong đầu là đủ; hai người trở lên mà không ghi ra thì sẽ có hai người sửa
> cùng một `Servlet`, hoặc một lỗi không ai nhận là của mình.

---

## Chọn đúng một trong hai

Chỉ có **hai loại**. Đừng đẻ thêm loại thứ ba.

| Tình huống | Loại | Đặt ở | Khuôn |
|---|---|---|---|
| Làm một việc mới, hoặc sửa/mở rộng thứ đang chạy đúng | **ISSUE** | [`issues/`](issues/) | [`templates/issue-template.md`](../templates/issue-template.md) |
| Thứ đang chạy **sai** so với điều nó đáng ra phải làm | **bug** | [`bugs/`](bugs/) | [`templates/bug-template.md`](../templates/bug-template.md) |

**Phân biệt:** *"chưa có"* là ISSUE, *"có rồi nhưng hỏng"* là bug.
Thiếu nút Xoá bình luận → ISSUE. Bấm Xoá mà nó xoá nhầm bình luận khác → bug.

Không chắc thì mở ISSUE — sai loại không chết ai, không ghi mới chết.

### Việc lớn thì chia đợt (phase)

**Phase không phải loại thứ ba** — nó là ISSUE được cắt thành nhiều đợt, mỗi đợt
một file nằm cạnh file gốc:

```text
ISSUE-004-them-he-thong-bao-cao.md          ← việc gốc
ISSUE-004-them-he-thong-bao-cao-phase-1.md  ← đợt 1: sửa schema
ISSUE-004-them-he-thong-bao-cao-phase-2.md  ← đợt 2: DAO + Servlet + JSP
```

Khuôn: [`templates/phase-template.md`](../templates/phase-template.md).

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.** Tách khi trộn vào thì
> *hỏng là không biết hỏng do đâu* — cụ thể: có sửa `database/schema.sql`, có đụng
> `filter/`, hoặc có đổi thứ đang chạy đúng. Sửa schema **luôn đứng riêng một đợt
> và làm trước**.
>
> **Đa số việc KHÔNG cần chia đợt.** Việc gọn, một mình, không đụng schema/filter
> thì viết thẳng trong ISSUE là đủ — đẻ thêm file chỉ tốn công.

---

## Đặt tên và đánh số

```text
docs/projects/issues/ISSUE-001-them-nut-bao-cao-truyen.md
docs/projects/bugs/bug-001-xoa-nham-binh-luan.md
```

- Số **chạy liên tục**, không chia theo tháng, không reset.
- Lấy số kế tiếp: mở thư mục, nhìn số lớn nhất, cộng một. Xong.
- Phần chữ: **tiếng Việt không dấu, chữ thường, gạch ngang** — giống luật đặt tên
  nhánh ở [`04-GIT_CONVENTIONS.md §1`](../standards/04-GIT_CONVENTIONS.md).

> **[Bẫy]** Hai người cùng tạo `ISSUE-004` trong một buổi là chuyện có thật.
> `git pull` trước khi tạo file mới.

---

## Vòng đời — ba trạng thái, không hơn

```text
📝 Chưa nhận  →  🚧 Đang làm  →  ✅ Xong
                              ↘  ⛔ Bỏ  (ghi lý do, đừng xoá file)
```

| | Nghĩa là |
|---|---|
| 📝 **Chưa nhận** | Đã ghi ra, chưa ai cầm. Ô **Người làm** còn trống. |
| 🚧 **Đang làm** | Có tên người trong ô **Người làm**. Chỉ **một** người. |
| ✅ **Xong** | Code đã vào `main` và **chạy thử thật rồi**, không phải "viết xong". |
| ⛔ **Bỏ** | Không làm nữa. **Ghi lý do** — người sau sẽ hỏi lại đúng câu đó. |

**[NEVER]** để trạng thái trống hoặc để nguyên cả dòng nhiều lựa chọn trong khuôn.
Doc không ai cập nhật thì tệ hơn không có doc — giống luật số 3 ở
[`INDEX.md`](../INDEX.md).

---

## Giao việc cho người mới — làm đúng bốn bước

1. **Mở ISSUE trước, gán người sau.** Việc chưa viết ra thì chưa giao được.
2. **Một ISSUE một người.** Cần hai người thì tách hai ISSUE, đừng ghi hai tên.
3. **Chỉ ra file đích.** Ô *"Đụng vào"* phải ghi tên `Servlet` / `DAO` / `.jsp` cụ thể.
   Người mới không đoán được `controller/` có gì — bảng ở
   [`cau-truc.md`](../architecture/cau-truc.md) là chỗ tra.
4. **Bắt đọc `standards/` trước khi code.** Ghi thẳng số mục vào ISSUE, ví dụ
   *"đặt tên theo `01-CODING §1`, URL theo `§5`"*. Đừng bảo *"đọc hết standards đi"* —
   không ai đọc đâu.

---

## Xong việc thì làm gì

- Sửa **Trạng thái** → ✅, điền ô **Đã kiểm thế nào**.
- Commit theo mẫu ở [`04-GIT_CONVENTIONS §2`](../standards/04-GIT_CONVENTIONS.md),
  nhắc mã trong dòng đầu: `ISSUE-003 — Thêm nút báo cáo truyện`.
- Cập nhật bảng ở [`issues/README.md`](issues/README.md) hoặc
  [`bugs/README.md`](bugs/README.md).

**[NEVER]** xoá file đã xong. Nó là bằng chứng *"vì sao code ra nông nỗi này"*,
và lúc bảo vệ đồ án thì đây là thứ kể được.

---

## Cố ý KHÔNG có những thứ này

Đồ án này vẫn giữ nguyên tinh thần nhẹ đã ghi ở [`INDEX.md`](../INDEX.md). Bộ khung
trên chỉ có **hai loại doc** (ISSUE, bug) và **ba khuôn** (issue, phase, bug). Cố ý bỏ:

| Bỏ | Vì |
|---|---|
| `review-N.md` riêng | Phần kiểm lại đã nằm sẵn trong `phase-template §5–§6`; tách ra thành file nữa là hai chỗ phải cập nhật |
| Thư mục riêng cho mỗi ISSUE | File phase để phẳng cạnh file gốc, cùng tiền tố nên vẫn nằm sát nhau khi sắp xếp |
| Thư mục theo tháng `MM-YYYY/` | Vài chục việc cả kỳ, một thư mục phẳng là nhìn hết |
| ADR · postmortem · spike · audit · usecase | Kiến trúc đã chốt ở [`so-do.md`](../architecture/so-do.md) và 4 file `standards/`; không có Production để mà có sự cố thật |
| Script sinh mã số tự động | Đếm tay nhanh hơn cài công cụ |

Thấy thiếu thật thì thêm — nhưng **thêm vì đang vướng**, không phải vì "cho đủ bộ".
