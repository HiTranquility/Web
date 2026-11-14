#!/usr/bin/env python3
"""Outrigger hook entry point.  Usage: hook.py <platform> <event>

platform: claude   (codex / antigravity / cursor adapters: release 2)
event:    pre-tool | session-start | stop

FAIL-CLOSED: if anything goes wrong while judging a tool call, the call is denied (exit 2).
"""
from __future__ import annotations

import json
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

if sys.version_info < (3, 11):  # tomllib
    print("OUTRIGGER: Python 3.11+ required — blocking to stay safe.", file=sys.stderr)
    sys.exit(2)

from outrigger import config, notify, state  # noqa: E402
from outrigger.decide import decide  # noqa: E402


def claude_pre_tool(payload: dict) -> int:
    cwd = Path(payload.get("cwd") or ".")
    cfg = config.load(cwd)
    tool = payload.get("tool_name", "")
    tool_input = payload.get("tool_input") or {}
    d = decide(cfg, tool, tool_input, cwd)
    summary = json.dumps(tool_input, ensure_ascii=False)[:300]

    for rel in d.backups:
        state.backup(cfg, rel)
    for app in d.apps:
        state.log_app(cfg, app, "allowed" if d.allow else "blocked", d.approval)

    if d.allow:
        if d.findings or d.approved:
            state.log_decision(cfg, {"tool": tool, "risk": d.risk, "decision": "approved" if d.approved else "allowed",
                                     "id": d.approval, "codes": [f.code for f in d.findings], "summary": summary})
        return 0

    state.record_pending(cfg, d.approval, {"tool": tool, "risk": d.risk, "codes": [f.code for f in d.findings],
                                           "summary": state.redact(summary), "reasons": [f.reason for f in d.findings]})
    sent = notify.send(cfg, d.risk, f"Outrigger: {d.risk.upper()} risk blocked",
                       f"{cfg.project_name}: {d.approval} waits for you") if d.risk == "high" else "skipped"
    state.log_decision(cfg, {"tool": tool, "risk": d.risk, "decision": "blocked", "id": d.approval,
                             "codes": [f.code for f in d.findings], "summary": summary, "notify": sent})
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PreToolUse", "permissionDecision": "deny",
                                             "permissionDecisionReason": d.reason(cfg)}}, ensure_ascii=False))
    return 0


def session_start(payload: dict) -> int:
    cfg = config.load(Path(payload.get("cwd") or "."))
    waiting = state.pending(cfg)
    lines = [f"Outrigger guard active (project: {cfg.project_name}). Rules: AGENTS.md; details in .outrigger/rules/."]
    if state.stop_file(cfg).exists():
        lines.append("STOP file present: do not change anything; tell the user.")
    if waiting:
        lines.append(f"{len(waiting)} blocked action(s) still wait for the user: " + ", ".join(w['id'] for w in waiting[:5]))
    packs = sorted({p.name for p in cfg.packs})
    if packs:
        lines.append("Packs installed: " + ", ".join(packs) + " (workflows in .outrigger/packs/*/workflows/).")
    print("\n".join(lines))
    return 0


def stop(payload: dict) -> int:
    """Append this turn's guard events to reports/outrigger/YYYY-MM-DD.md (machine log for humans)."""
    cfg = config.load(Path(payload.get("cwd") or "."))
    marker = cfg.state_dir / "last_stop"
    since = None
    if marker.is_file():
        try:
            since = datetime.fromisoformat(marker.read_text(encoding="utf-8").strip())
        except ValueError:
            since = None
    events = [e for e in state.read_log(cfg, since)]
    marker.parent.mkdir(parents=True, exist_ok=True)
    marker.write_text(state.now().isoformat(timespec="seconds"), encoding="utf-8")
    if not events:
        return 0
    day = state.now().strftime("%Y-%m-%d")
    out = cfg.root / "reports" / "outrigger" / f"{day}.md"
    out.parent.mkdir(parents=True, exist_ok=True)
    new = not out.exists()
    with out.open("a", encoding="utf-8") as fh:
        if new:
            fh.write(f"# Outrigger guard log — {day}\n\nWritten by the Stop hook. Do not edit.\n")
        fh.write(f"\n## Turn ending {state.now():%H:%M:%S}\n\n| Time | Risk | Decision | Id | Tool | What |\n|---|---|---|---|---|---|\n")
        for e in events:
            what = str(e.get("summary", "")).replace("|", "\\|").replace("\n", " ")[:120]
            fh.write(f"| {e['ts'][11:19]} | {e.get('risk')} | {e.get('decision')} | {e.get('id') or ''} | "
                     f"{e.get('tool')} | {what} |\n")
    return 0


def main() -> int:
    platform = sys.argv[1] if len(sys.argv) > 1 else "claude"
    event = sys.argv[2] if len(sys.argv) > 2 else "pre-tool"
    raw = sys.stdin.buffer.read().decode("utf-8", errors="replace")
    payload = json.loads(raw) if raw.strip() else {}
    if platform != "claude":
        print(f"OUTRIGGER: platform '{platform}' is not supported in this release — blocking.", file=sys.stderr)
        return 2
    if event == "pre-tool":
        return claude_pre_tool(payload)
    if event == "session-start":
        return session_start(payload)
    if event == "stop":
        return stop(payload)
    print(f"OUTRIGGER: unknown event '{event}'", file=sys.stderr)
    return 2


if __name__ == "__main__":
    try:
        sys.exit(main())
    except SystemExit:
        raise
    except BaseException as exc:  # fail closed
        event = sys.argv[2] if len(sys.argv) > 2 else "pre-tool"
        if event == "pre-tool":
            print(f"OUTRIGGER internal error ({type(exc).__name__}: {exc}). Blocking to stay safe. "
                  "Tell the user; they can run `python .outrigger/engine/cli.py selftest`.", file=sys.stderr)
            sys.exit(2)
        sys.exit(0)  # session-start / stop must never break the session
