#!/usr/bin/env bash
# docs/reindex.sh — quét docs/ và in ra mã kế tiếp + trạng thái doc
set -uo pipefail
cd "$(dirname "$0")" || exit 1
DOCS="."

TYPES=(
  "ISSUE:projects/issues"
  "bug:projects/bugs"
  "REQ:requirements"
)

next_id() {
  local prefix="$1" nums max
  nums=$(grep -rEoh --include='*.md' --exclude-dir=templates "${prefix}-[0-9]+" "$DOCS" 2>/dev/null \
        | sed -E "s/^${prefix}-0*//" | grep -E '^[0-9]+$' | sort -n)
  max=$(echo "$nums" | tail -1)
  [ -z "$max" ] && echo 1 || echo $((max+1))
}

strip_row() { sed -E 's/\|//g; s/^[[:space:]]+//; s/[[:space:]]+$//'; }

echo "# 📇 INDEX (máy sinh) — $(date +%Y-%m-%d)"
echo
echo "## 🔢 Mã kế tiếp cho mỗi loại"
echo
echo "| Loại | Mã tiếp theo nên dùng |"
echo "|---|---|"
for entry in "${TYPES[@]}"; do
  prefix="${entry%%:*}"
  printf "| %s | \`%s-%03d\` |\n" "$prefix" "$prefix" "$(next_id "$prefix")"
done
echo

echo "## 📚 Doc theo loại"
for entry in "${TYPES[@]}"; do
  prefix="${entry%%:*}"; dir="${entry##*:}"
  echo
  echo "### $prefix — \`docs/$dir/\`"
  if [ ! -d "$DOCS/$dir" ]; then echo "_(chưa có thư mục)_"; continue; fi
  found=0
  while IFS= read -r f; do
    found=1
    title=$(grep -m1 -E '^# ' "$f" 2>/dev/null | sed -E 's/^#[[:space:]]*//')
    status=$(grep -m1 -E '^\|[[:space:]]*\*\*(Trạng thái|Status|Mức|Kết luận)\*\*' "$f" 2>/dev/null | strip_row)
    rel="${f#./}"
    echo "- \`$rel\` — ${title:-?}"
    [ -n "${status:-}" ] && echo "    - ${status}"
  done < <(find "$DOCS/$dir" -name '*.md' -not -name 'INDEX.md' -not -name 'README.md' 2>/dev/null | sort)
  [ "$found" = 0 ] && echo "_(chưa có doc)_"
done

echo
echo "> ⚠️ Script quét tự động từ thư mục và dòng Meta Trạng thái."
