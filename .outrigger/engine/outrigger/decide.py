"""Platform-agnostic decision: (tool name, tool input, cwd) -> allow or deny, with side effects listed."""
from __future__ import annotations

import os
import re
from dataclasses import dataclass, field
from pathlib import Path
from urllib.parse import urlparse

from . import packs, rules, state
from .config import Config
from .rules import Finding
from .shell import ParseError, split_commands

SHELL_TOOLS = {"bash": True, "powershell": False, "shell": True, "run_shell_command": True,
               "exec_command": True, "local_shell": True}
WRITE_TOOLS = {"write", "edit", "multiedit", "notebookedit", "write_file", "replace", "apply_patch", "create_file",
               "edit_file", "str_replace_editor"}
READ_TOOLS = {"read", "glob", "grep", "ls", "notebookread", "read_file", "read_many_files", "list_directory",
              "search_file_content", "websearch", "google_web_search", "todowrite", "toolsearch"}
FETCH_TOOLS = {"webfetch", "web_fetch", "fetch"}
SCHEDULE_TOOLS = re.compile(r"cron(create|delete|update)?$|remotetrigger|scheduled[-_]?tasks?__(create|update|delete|run)",
                            re.IGNORECASE)
RISKY_MCP = re.compile(r"(delete|remove|kill|terminate|move|write|edit|create|start|execute|run|install|send|post|"
                       r"publish|push|upload|schedule|trigger|merge|open_application|type|click|drag)", re.IGNORECASE)
MEMORY_SUSPICIOUS = re.compile(
    r"ignore (all |any )?(previous|prior|above|earlier) (instructions|rules)|always approve|approve (it |them )?automatically"
    r"|without asking( the user)?|skip (the )?(gate|approval|confirmation)s?|(disable|turn off|remove|uninstall) (outrigger|the guard|hooks?)"
    r"|bypass (outrigger|the guard|approval)|cli\.py\s+(approve|trust|resume)|do not (tell|inform|mention to) the user"
    r"|bỏ qua (luật|quy tắc|guard|outrigger)|không cần hỏi|tự (động )?duyệt|tắt (outrigger|guard|hook)", re.IGNORECASE)
APP_MCP = re.compile(r"open_application|request_access|preview_start|navigate", re.IGNORECASE)


@dataclass
class Decision:
    allow: bool
    risk: str
    findings: list[Finding] = field(default_factory=list)
    approval: str | None = None
    approved: bool = False
    backups: list[str] = field(default_factory=list)
    apps: list[str] = field(default_factory=list)

    def reason(self, cfg: Config) -> str:
        top = [f for f in self.findings if f.risk == self.risk] or self.findings
        lines = [f"OUTRIGGER BLOCKED [{self.risk.upper()}] {self.approval}"]
        lines += [f"- {f.code}: {f.reason}" for f in top[:6]]
        if any(f.code == "stop" for f in top):
            lines.append("A STOP file is present. Stop working and tell the user; only they can run `cli.py resume`.")
            return "\n".join(lines)
        lines.append("Do NOT retry in another form or with another tool. Ask the user with .outrigger/templates/GATE.md.")
        lines.append(f"If they agree, they run in THEIR OWN terminal: python .outrigger/engine/cli.py approve {self.approval}")
        lines.append("Then repeat the identical action.")
        return "\n".join(lines)


def _paths_from(tool_input: dict) -> list[str]:
    out = []
    for k in ("file_path", "path", "notebook_path", "filePath", "absolute_path", "target_file"):
        v = tool_input.get(k)
        if isinstance(v, str) and v:
            out.append(v)
    for k in ("paths", "file_paths"):
        v = tool_input.get(k)
        if isinstance(v, list):
            out += [x for x in v if isinstance(x, str)]
    patch = tool_input.get("patch") or tool_input.get("input")
    if isinstance(patch, str):
        out += re.findall(r"^\*\*\* (?:Add|Update|Delete) File: (.+)$", patch, re.MULTILINE)
    return out


def _write_findings(cfg: Config, tool: str, tool_input: dict, cwd: Path) -> tuple[list[Finding], list[str]]:
    findings, to_backup = [], []
    for p in _paths_from(tool_input):
        rel = cfg.rel(p, cwd)
        if rel is None:
            kind = cfg.outside_kind(p)
            if kind == "agent-config":
                findings.append(Finding("medium", "agent-config",
                                        f"`{p}` changes how agents behave in every project — only with your approval"))
            elif kind == "memory":
                findings.append(Finding("low", "memory-write", f"agent memory written: {Path(p).name}"))
                to_backup.append(str(Path(os.path.expanduser(p)).resolve(strict=False)))
                text = " ".join(str(tool_input.get(k, "")) for k in ("content", "new_string", "new_source"))
                m = MEMORY_SUSPICIOUS.search(text)
                if m:
                    findings.append(Finding("medium", "memory-suspicious",
                                            f"the memory note contains an instruction that weakens safety: “{m.group(0)[:50]}”"))
            elif kind != "allow":
                findings.append(Finding("medium", "write-outside", f"writes outside the project: {p}"))
            continue
        zone = cfg.zone_of(rel)
        if zone == "red":
            findings.append(Finding("high", "secret", f"writes a secret file: {rel}"))
        elif zone == "protected":
            findings.append(Finding("high", "outrigger-self", f"`{rel}` is protected — only the human edits it"))
        elif zone == "yellow":
            to_backup.append(rel)
        # dangerous code being written into a pack file is logged (the headless run will be gated)
        content = " ".join(str(tool_input.get(k, "")) for k in ("content", "new_string", "new_source"))
        for pack in cfg.packs:
            if Path(rel).suffix.lower() in pack.scan_extensions and content:
                hits = packs.scan_text(content, pack, Path(rel).suffix.lower())
                if hits:
                    findings.append(Finding("low", "pack-danger-written",
                                            f"{rel} now contains: {', '.join(h[1] for h in hits[:4])}"))
    return findings, to_backup


def _read_findings(cfg: Config, tool_input: dict, cwd: Path) -> list[Finding]:
    out = []
    for p in _paths_from(tool_input):
        rel = cfg.rel(p, cwd)
        if rel is not None and cfg.zone_of(rel) == "red":
            out.append(Finding("high", "secret", f"reads a secret file: {rel}"))
    pattern = str(tool_input.get("pattern", "")) + " " + str(tool_input.get("glob", ""))
    if re.search(r"(^|[/\\*\s])\.env(\b|\*)|id_rsa|\.pem\b|\.key\b", pattern) and "example" not in pattern:
        out.append(Finding("medium", "secret-search", "searches for secret files"))
    return out


def decide(cfg: Config, tool: str, tool_input: dict, cwd: Path) -> Decision:
    t = tool.lower()
    short = t.split("__")[-1] if t.startswith("mcp__") else t
    findings: list[Finding] = []
    backups: list[str] = []
    is_read = short in READ_TOOLS

    if state.stop_file(cfg).exists() and not is_read:
        findings.append(Finding("high", "stop", "STOP file present — all non-read tools are blocked"))

    command = tool_input.get("command") or tool_input.get("cmd")
    if isinstance(command, list):
        command = " ".join(map(str, command))
    if isinstance(command, str) and command.strip() and (short in SHELL_TOOLS or t.startswith("mcp__")):
        posix = SHELL_TOOLS.get(short, True)
        try:
            segments = split_commands(command, posix=posix)
        except ParseError as exc:
            segments = []
            findings.append(Finding("medium", "unparsed", f"could not parse the command safely ({exc})"))
        findings += rules.check_raw(command, segments, cfg, cwd)
        for seg in segments:
            findings += rules.check_segment(seg, cfg, cwd)
    elif short in WRITE_TOOLS:
        f, backups = _write_findings(cfg, short, tool_input, cwd)
        findings += f
    elif is_read:
        findings += _read_findings(cfg, tool_input, cwd)
    elif short in FETCH_TOOLS:
        host = urlparse(str(tool_input.get("url", ""))).hostname or ""
        if not cfg.host_allowed(host):
            findings.append(Finding("medium", "net-unknown", f"fetch from `{host}` — not on the allow-list (T4 until checked)"))
    elif SCHEDULE_TOOLS.search(t):
        findings.append(Finding("high", "schedule", f"`{tool}` creates or changes a schedule"))
    elif t.startswith("mcp__"):
        if APP_MCP.search(short):
            findings.append(Finding("medium", "app", f"MCP `{tool}` opens an app or page — L2", app=tool))
        elif RISKY_MCP.search(short):
            findings.append(Finding("medium", "mcp-risky", f"MCP tool `{tool}` can change things outside Outrigger's view"))
        f, b = _write_findings(cfg, short, tool_input, cwd) if _paths_from(tool_input) and RISKY_MCP.search(short) else ([], [])
        findings += f
        backups += b

    risk = rules.worst(findings)
    apps = [f.app for f in findings if f.app]
    if risk == "low":
        return Decision(True, risk, findings, backups=backups, apps=apps)
    aid = state.approval_id(tool, tool_input)
    if state.consume_approval(cfg, aid):
        return Decision(True, risk, findings, approval=aid, approved=True, backups=backups, apps=apps)
    return Decision(False, risk, findings, approval=aid, apps=apps)
