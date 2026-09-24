# 🔍 [Review N] <Tên Feature/Chore/Bug> — Review Phase N

> **Đích:** `docs/projects/issues/ISSUE-NNN-slug/review-N.md` (hoặc `docs/projects/bugs/bug-NNN-slug/review-N.md`) — nằm cạnh phase nó review. · **Luật chung:** [templates/README](../../templates/README.md)
>
> **Cách dùng:** Sau khi code xong một `phase-N.md`, copy file này thành `review-N.md` trong cùng thư mục issue/bug. Đối chiếu kết quả đo baseline, kiểm thử thủ công và Acceptance Criteria. Kết quả (Action Items) sẽ là đầu vào để thực hiện `phase-(N+1).md`.

## 📌 Meta

| | |
|---|---|
| **Issue / Bug gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-N.md](phase-1.md) |
| **Ngày review** | YYYY-MM-DD |
| **Người review** | @nguoi_review |
| **Kết luận** | ✅ Pass / 🟡 Pass có điều kiện / 🔴 Cần làm lại |

---

## 🎯 Phạm vi review

Phase N tuyên bố hoàn thành những phần việc gì? So sánh đối chiếu trực tiếp với các mục tiêu và **Acceptance Criteria** đã cam kết trong `issue.md` / `phase-N.md`.

---

## 📸 Đối chiếu Baseline

> **[MUST] Chạy lại lệnh đo.** Lấy bảng Baseline trong `phase-N.md`, chạy lại và điền kết quả sau phase. Số pass test bị tụt hoặc phát sinh lỗi biên dịch = 🔴 tự động, bất kể tính năng mới chạy đẹp cỡ nào.

| Lệnh kiểm tra | Trước phase | Sau phase | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | [vd: 28 pass / 0 fail] | [vd: 28 pass / 0 fail] | ✅/🔴 |
| Biên dịch code (`scripts\run.ps1` hoặc `javac`) | [vd: sạch, 0 lỗi] | [vd: sạch, 0 lỗi] | ✅/🔴 |
| Mở trình duyệt kiểm tra luồng chính | [vd: chạy bình thường] | [vd: chạy bình thường] | ✅/🔴 |

---

## ✅ Đối chiếu Acceptance Criteria

| Tiêu chí (từ issue.md / phase-N.md) | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Tiêu chí 1 | ✅ / ❌ / ⚠️ | ... |
| Tiêu chí 2 | ✅ / ❌ / ⚠️ | ... |
| Tiêu chí 3 | ✅ / ❌ / ⚠️ | ... |

---

## 🐞 Lỗi phát hiện khi kiểm thử (nếu có)

| # | Mức độ | Mô tả lỗi | Vị trí / File | Đề xuất khắc phục |
|---|---|---|---|---|
| 1 | 🔴 Chặn / 🟠 Nặng / 🟡 Nhẹ | ... | `...` | ... |

---

## 🧩 Gap / Thiếu sót so với mục tiêu ban đầu

- [Liệt kê các chi tiết kỹ thuật hoặc edge case bị sót so với thiết kế ban đầu]

---

## 🧹 Nợ kỹ thuật (Tech Debt phát sinh)

- [Các đoạn code tạm, hardcode, comment TODO cần dọn dẹp]

---

## 📋 Action Items → Đưa vào Phase tiếp theo

> Danh sách này là đầu vào trực tiếp cho `phase-(N+1).md` hoặc commit fix dứt điểm.

- [ ] **AI-1:** ...
- [ ] **AI-2:** ...

---

## 🏁 Kết luận (Verdict)

- **Có cho merge / chuyển sang phase sau không?** Có / Không
- **Lý do & Lời nhắn:** ...
