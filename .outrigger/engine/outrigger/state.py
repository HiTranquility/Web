"""Everything Outrigger writes to disk: audit log, app log, approvals, backups, trash, archive, STOP."""
from __future__ import annotations

import hashlib
import json
import re
import shutil
import time
from datetime import datetime, timedelta
from pathlib import Path

from .config import Config

SECRET_PATTERNS = [
    re.compile(r"(sk-[A-Za-z0-9_-]{16,})"), re.compile(r"(gh[pousr]_[A-Za-z0-9]{20,})"),
    re.compile(r"(AKIA[0-9A-Z]{16})"), re.compile(r"(xox[abpr]-[A-Za-z0-9-]{10,})"),
    re.compile(r"(?i)((?:password|passwd|pwd|secret|token|api[_-]?key)\s*[=:]\s*)(\S+)"),
    re.compile(r"(?i)(bearer\s+)([A-Za-z0-9._-]{12,})"),
    re.compile(r"(?i)((?:postgres|mysql|mongodb(?:\+srv)?|redis)://)([^\s]+)"),
]


def redact(text: str) -> str:
    for pat in SECRET_PATTERNS:
        if pat.groups >= 2:
            text = pat.sub(lambda m: m.group(1) + "***REDACTED***", text)
        else:
            text = pat.sub("***REDACTED***", text)
    return text


def now() -> datetime:
    return datetime.now().astimezone()


def _append(path: Path, record: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8") as fh:
        fh.write(json.dumps(record, ensure_ascii=False) + "\n")


def log_decision(cfg: Config, record: dict) -> None:
    record = {"ts": now().isoformat(timespec="seconds"), **record}
    if "summary" in record:
        record["summary"] = redact(str(record["summary"]))[:300]
    _append(cfg.state_dir / "logs" / f"audit-{now():%Y-%m}.jsonl", record)


def log_app(cfg: Config, app: str, decision: str, approval: str | None) -> None:
    _append(cfg.state_dir / "logs" / "apps.jsonl",
            {"ts": now().isoformat(timespec="seconds"), "app": redact(app)[:200], "decision": decision,
             "approval": approval})


def read_log(cfg: Config, since: datetime | None = None) -> list[dict]:
    out = []
    for f in sorted((cfg.state_dir / "logs").glob("audit-*.jsonl")):
        for line in f.read_text(encoding="utf-8").splitlines():
            try:
                rec = json.loads(line)
            except json.JSONDecodeError:
                continue
            if since is None or datetime.fromisoformat(rec["ts"]) > since:
                out.append(rec)
    return out


# ---------- approvals ----------

def approval_id(tool: str, tool_input: dict) -> str:
    canonical = json.dumps({"tool": tool, "input": tool_input}, sort_keys=True, ensure_ascii=False)
    return "OR-" + hashlib.sha256(canonical.encode("utf-8")).hexdigest()[:8]


def _adir(cfg: Config, kind: str) -> Path:
    d = cfg.state_dir / "approvals" / kind
    d.mkdir(parents=True, exist_ok=True)
    return d


def record_pending(cfg: Config, aid: str, info: dict) -> None:
    (_adir(cfg, "pending") / f"{aid}.json").write_text(
        json.dumps({"id": aid, "created": now().isoformat(timespec="seconds"), **info}, ensure_ascii=False, indent=2),
        encoding="utf-8")


def consume_approval(cfg: Config, aid: str) -> bool:
    f = _adir(cfg, "approved") / f"{aid}.json"
    if not f.is_file():
        return False
    data = json.loads(f.read_text(encoding="utf-8"))
    used = _adir(cfg, "used") / f.name
    if datetime.fromisoformat(data["expires"]) < now():
        f.replace(used)
        return False
    data["used"] = now().isoformat(timespec="seconds")
    used.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    f.unlink()
    return True


def approve(cfg: Config, aid: str, minutes: int | None = None) -> dict:
    src = _adir(cfg, "pending") / f"{aid}.json"
    if not src.is_file():
        raise FileNotFoundError(f"no pending action {aid}")
    data = json.loads(src.read_text(encoding="utf-8"))
    data["approved"] = now().isoformat(timespec="seconds")
    data["expires"] = (now() + timedelta(minutes=minutes or cfg.limit("approval_minutes"))).isoformat(timespec="seconds")
    (_adir(cfg, "approved") / src.name).write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    src.unlink()
    return data


def deny(cfg: Config, aid: str) -> None:
    src = _adir(cfg, "pending") / f"{aid}.json"
    if src.is_file():
        src.replace(_adir(cfg, "denied") / src.name)


def pending(cfg: Config) -> list[dict]:
    return [json.loads(f.read_text(encoding="utf-8")) for f in sorted(_adir(cfg, "pending").glob("OR-*.json"))]


# ---------- backups ----------

def backup(cfg: Config, rel: str) -> str | None:
    """Copy a file to .outrigger/state/backups before it is changed. `rel` may be absolute (agent memory outside the project)."""
    src = cfg.root / rel            # an absolute `rel` replaces the root
    if not src.is_file():
        return None
    if src.stat().st_size > cfg.limit("backup_max_mb") * 1024 * 1024:
        return "skipped: larger than backup_max_mb"
    name = f"_outside/{src.parent.name}/{src.name}" if Path(rel).is_absolute() else rel
    dest = cfg.state_dir / "backups" / f"{now():%Y-%m-%d}" / f"{name}@{now():%H%M%S}"
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dest)
    return dest.relative_to(cfg.root).as_posix()


# ---------- trash / archive ----------

def _sha(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def _move_with_manifest(cfg: Config, rels: list[str], dest_root: Path, reason: str, kind: str) -> dict:
    dest_root.mkdir(parents=True, exist_ok=True)
    items = []
    for rel in rels:
        src = cfg.root / rel
        if not src.exists():
            raise FileNotFoundError(rel)
        files = [src] if src.is_file() else [p for p in src.rglob("*") if p.is_file()]
        items.append({"path": rel, "type": "file" if src.is_file() else "dir",
                      "files": [{"path": p.relative_to(cfg.root).as_posix(), "size": p.stat().st_size,
                                 "sha256": _sha(p)} for p in files]})
        target = dest_root / "files" / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(src), str(target))
    manifest = {"kind": kind, "id": dest_root.name, "time": now().isoformat(timespec="seconds"),
                "reason": reason, "items": items}
    (dest_root / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    return manifest


def trash(cfg: Config, rels: list[str], reason: str) -> dict:
    stamp = f"{now():%Y%m%d-%H%M%S}-{int(time.time() * 1000) % 1000:03d}"
    return _move_with_manifest(cfg, rels, cfg.state_dir / "trash" / stamp, reason, "trash")


def archive(cfg: Config, rels: list[str], reason: str) -> dict:
    stamp = f"{now():%Y%m%d-%H%M%S}"
    return _move_with_manifest(cfg, rels, cfg.root / "archive" / f"{now():%Y-%m}" / stamp, reason, "archive")


def trash_entries(cfg: Config) -> list[dict]:
    out = []
    for m in sorted((cfg.state_dir / "trash").glob("*/manifest.json")):
        out.append(json.loads(m.read_text(encoding="utf-8")))
    return out


def restore(cfg: Config, trash_id: str) -> list[str]:
    root = cfg.state_dir / "trash" / trash_id
    manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
    restored = []
    for item in manifest["items"]:
        src = root / "files" / item["path"]
        dest = cfg.root / item["path"]
        if dest.exists():
            dest = dest.with_name(dest.name + f".restored-{now():%Y%m%d%H%M%S}")
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.move(str(src), str(dest))
        restored.append(dest.relative_to(cfg.root).as_posix())
    shutil.rmtree(root)
    return restored


def empty_trash(cfg: Config, older_than_days: int) -> tuple[int, int]:
    cutoff = now() - timedelta(days=older_than_days)
    count, size = 0, 0
    for m in trash_entries(cfg):
        if datetime.fromisoformat(m["time"]) <= cutoff:
            folder = cfg.state_dir / "trash" / m["id"]
            for item in m["items"]:
                for f in item["files"]:
                    count += 1
                    size += f["size"]
            shutil.rmtree(folder)
    return count, size


# ---------- STOP ----------

def stop_file(cfg: Config) -> Path:
    return cfg.root / "STOP"


# ---------- trusted scripts (human-only, bound to content) ----------

def _trusted_file(cfg: Config) -> Path:
    return cfg.state_dir / "trusted.json"


def trusted(cfg: Config) -> dict:
    f = _trusted_file(cfg)
    try:
        return json.loads(f.read_text(encoding="utf-8")) if f.is_file() else {}
    except (OSError, json.JSONDecodeError):
        return {}


def _save_trusted(cfg: Config, data: dict) -> None:
    _trusted_file(cfg).parent.mkdir(parents=True, exist_ok=True)
    _trusted_file(cfg).write_text(json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True), encoding="utf-8")


def trust(cfg: Config, rel: str) -> str:
    data = trusted(cfg)
    data[rel] = _sha(cfg.root / rel)
    _save_trusted(cfg, data)
    return data[rel]


def untrust(cfg: Config, rel: str) -> bool:
    data = trusted(cfg)
    found = data.pop(rel, None) is not None
    _save_trusted(cfg, data)
    return found


def is_trusted(cfg: Config, rel: str) -> bool:
    """True only while the file is byte-identical to what the human trusted."""
    want = trusted(cfg).get(rel)
    f = cfg.root / rel
    return bool(want) and f.is_file() and _sha(f) == want
