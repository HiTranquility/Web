"""Risk rules for shell commands. Each rule returns Findings; the decision takes the worst one."""
from __future__ import annotations

import re
from dataclasses import dataclass
import json
import subprocess
from pathlib import Path
from urllib.parse import urlparse

from . import packs, state
from .config import Config
from .shell import ParseError, base, split_commands

RISK_ORDER = {"low": 0, "medium": 1, "high": 2}


@dataclass
class Finding:
    risk: str
    code: str
    reason: str
    app: str | None = None


DELETE = {"rm", "del", "erase", "rd", "rmdir", "remove-item", "ri", "unlink", "shred", "srm", "trash-put", "rimraf"}
READ_ONLY = {"cat", "type", "get-content", "gc", "less", "more", "head", "tail", "grep", "rg", "findstr",
             "select-string", "ls", "dir", "get-childitem", "gci", "wc", "stat", "file", "test-path", "echo"}
SYSTEM_HIGH = {"format", "diskpart", "bcdedit", "shutdown", "restart-computer", "stop-computer", "regedit",
               "set-mppreference", "add-mppreference", "vssadmin", "mkfs", "dd", "runas", "su", "takeown",
               "set-executionpolicy", "cipher", "__sudo__", "iex", "invoke-expression", "__encoded__"}
SCHEDULE = {"schtasks", "register-scheduledtask", "set-scheduledtask", "unregister-scheduledtask",
            "new-scheduledtask", "new-scheduledtasktrigger", "new-scheduledtaskaction", "crontab", "at", "launchctl"}
NET = {"curl", "wget", "invoke-webrequest", "iwr", "invoke-restmethod", "irm", "bitsadmin",
       "start-bitstransfer", "certutil"}
NET_OUT = {"scp", "sftp", "ftp", "rsync", "nc", "ncat", "telnet", "ssh"}
INSTALLERS = {
    "pip": {"install"}, "pip3": {"install"}, "uv": {"add", "pip"}, "pipx": {"install"},
    "npm": {"install", "i", "add"}, "pnpm": {"add", "install", "i"}, "yarn": {"add", "install"},
    "bun": {"add", "install", "i"}, "winget": {"install", "upgrade"}, "choco": {"install", "upgrade"},
    "scoop": {"install"}, "conda": {"install", "create"}, "mamba": {"install"}, "apt": {"install"},
    "apt-get": {"install"}, "brew": {"install"}, "gem": {"install"}, "cargo": {"install"},
    "install-module": None, "install-package": None, "npx": None,
}
APPS = {"start", "start-process", "saps", "explorer", "invoke-item", "ii", "code", "notepad", "open",
        "xdg-open", "simulink", "msedge", "chrome", "firefox"}
UPLOAD_FLAGS = {"-d", "--data", "--data-raw", "--data-binary", "--data-urlencode", "-f", "--form", "-t",
                "--upload-file", "--post-data", "--post-file", "-body", "-infile"}
UPLOAD_METHODS = {"post", "put", "patch", "delete"}
OUTRIGGER_HUMAN_ONLY = re.compile(r"cli\.py[\"']?\s+(?:-\S+\s+)*(approve|resume|empty-trash|deny|trust|untrust)\b", re.IGNORECASE)
OUTRIGGER_SELF = re.compile(r"\.outrigger[\\/](engine|state|outrigger\.toml|local[\\/]notify\.toml)|\.claude[\\/]settings"
                       r"|\.mcp\.json|\.codex[\\/]|\.agents[\\/]hooks\.json|\.cursor[\\/]hooks\.json", re.IGNORECASE)
PIPE_TO_SHELL = re.compile(r"(curl|wget|iwr|irm|invoke-webrequest|invoke-restmethod)\b[^|\n]*\|\s*"
                           r"(sudo\s+)?(sh|bash|zsh|python3?|iex|invoke-expression|pwsh|powershell)\b", re.IGNORECASE)
PY_DELETE = re.compile(r"shutil\.rmtree|os\.(remove|unlink|rmdir)|Path\([^)]*\)\.unlink|\.rmdir\(", re.IGNORECASE)
ROOTLIKE = {"/", "\\", "~", "~/", "*", "/*", ".", "./", "..", "../", "c:", "c:\\", "c:/", "$home", "%userprofile%"}


def worst(findings: list[Finding]) -> str:
    return max((f.risk for f in findings), key=RISK_ORDER.get, default="low")


OUTRIGGER_CLI = re.compile(r"\.outrigger[\\/]engine[\\/]cli\.py", re.IGNORECASE)
REDIRECT_TARGET = re.compile(r"(?:^|[^<0-9&])\d?>>?\s*(\"[^\"]+\"|'[^']+'|[^\s;&|]+)")


def check_raw(command: str, segments: list[list[str]], cfg: Config, cwd: Path) -> list[Finding]:
    out = []
    if PIPE_TO_SHELL.search(command):
        out.append(Finding("high", "pipe-to-shell", "download piped straight into a shell or interpreter"))
    if OUTRIGGER_HUMAN_ONLY.search(command):
        out.append(Finding("high", "self-approval", "approve/deny/resume/empty-trash/trust are human-only commands"))
    m = OUTRIGGER_SELF.search(OUTRIGGER_CLI.sub("", command))
    read_only = all(base(s[0]) in READ_ONLY for s in segments if s)
    if m and not read_only:
        out.append(Finding("high", "outrigger-self", f"command touches Outrigger or hook configuration ({m.group(0)})"))
    for t in REDIRECT_TARGET.findall(command):
        t = t.strip("\"'")
        if t.lower() in {"/dev/null", "nul", "&1", "&2"}:
            continue
        rel = cfg.rel(t, cwd)
        if rel is None:
            if cfg.outside_kind(t) == "agent-config":
                out.append(Finding("medium", "agent-config", f"redirects output into agent configuration `{t}`"))
            elif not cfg.outside_allowed(t):
                out.append(Finding("medium", "write-outside", f"redirects output outside the project: `{t}`"))
        elif cfg.zone_of(rel) in {"red", "protected"}:
            out.append(Finding("high", "write-protected", f"redirects output into `{rel}` ({cfg.zone_of(rel)} zone)"))
    if re.search(r"base64\s+(-d|--decode)[^|\n]*\|\s*(sh|bash)", command, re.IGNORECASE):
        out.append(Finding("high", "encoded-exec", "decoded payload piped into a shell"))
    if re.search(r"\bpython3?\b.*\s-c\s", command) and PY_DELETE.search(command):
        out.append(Finding("medium", "delete-redirect", "python one-liner deletes files — use `cli.py trash`"))
    return out


def check_segment(seg: list[str], cfg: Config, cwd: Path, depth: int = 0) -> list[Finding]:
    if not seg:
        return []
    head = base(seg[0])
    args = seg[1:]
    low = [a.lower() for a in args]
    out: list[Finding] = []

    out.extend(_script_files(seg, head, cfg, cwd, depth))
    if head in NPM_RUNNERS:
        out.extend(_npm_script(head, args, cfg, cwd, depth))


    # secrets: any argument pointing into the red zone
    for a in args:
        if a.startswith("-"):
            continue
        rel = cfg.rel(a, cwd)
        if rel is not None and cfg.zone_of(rel) == "red":
            out.append(Finding("high", "secret", f"touches a secret file: {rel}"))

    if head in SYSTEM_HIGH:
        out.append(Finding("high", "system", f"`{head}` changes the system, raises privilege or runs hidden code"))
    elif head == "reg" and low and low[0] in {"add", "delete", "import", "load", "restore", "unload"}:
        out.append(Finding("high", "registry", "registry change"))
    elif head == "netsh" and any("firewall" in a for a in low):
        out.append(Finding("high", "firewall", "firewall change"))
    elif head == "sc" and low and low[0] in {"delete", "config", "stop", "create"}:
        out.append(Finding("high", "service", "Windows service change"))
    elif head in {"chmod", "chown", "icacls"} and any(a in {"-r", "777", "/grant", "/reset", "/remove"} or a.startswith("/grant") for a in low):
        out.append(Finding("high", "permissions", "recursive or world-writable permission change"))
    elif head in {"set-itemproperty", "new-itemproperty", "remove-itemproperty"} and any(a.startswith(("hklm", "hkcu")) for a in low):
        out.append(Finding("high", "registry", "registry change"))

    if head in SCHEDULE:
        if not (head == "schtasks" and low and low[0] in {"/query", "/?"}) and not (head == "crontab" and low == ["-l"]):
            out.append(Finding("high", "schedule", "creates or changes a scheduled task"))

    if head == "git":
        out.extend(_git(low))
        if low[:1] == ["commit"]:
            out.extend(_staged_secrets(low, cfg, cwd))

    if head in DELETE or (head == "find" and ("-delete" in low or "-exec" in low and any(base(a) in DELETE for a in args))):
        out.append(_delete(args, cfg, cwd))

    if head in NET:
        out.append(_net(head, args, low, cfg))
    elif head in NET_OUT:
        out.append(Finding("high", "upload", f"`{head}` can send data off this machine"))

    if head in INSTALLERS:
        verbs = INSTALLERS[head]
        sub = low[0] if low else ""
        if verbs is None or sub in verbs:
            out.append(Finding("medium", "install", f"installs software (`{head} {sub}`) — supply-chain check first"))
    if head in {"python", "python3", "py"} and "-m" in low:
        i = low.index("-m")
        if i + 2 < len(low) and low[i + 1] in {"pip", "pipx"} and low[i + 2] == "install":
            out.append(Finding("medium", "install", "pip install — supply-chain check first"))

    cli_at = next((i for i, a in enumerate(seg) if base(a) == "cli.py" and OUTRIGGER_CLI.search(a.replace("\\", "/")) or
                   (i == 0 and base(a) == "cli.py")), None)
    if cli_at is not None:
        out.extend(_outrigger_cli(seg[cli_at:], cfg, cwd))

    if head in APPS:
        out.append(Finding("medium", "app", f"opens an interactive app (`{' '.join(seg[:3])}`) — L2", app=" ".join(seg[:3])))

    for pack in cfg.packs:
        if head in pack.commands:
            out.extend(_pack_run(pack, seg, low, cfg, cwd))

    return out


def _git(low: list[str]) -> list[Finding]:
    if not low:
        return []
    sub, rest = low[0], low[1:]
    if sub == "push":
        if any(a in {"--force", "-f", "--force-with-lease", "--mirror", "--delete", "-d", "--prune"} or a.startswith("--force") or a.startswith(":") for a in rest):
            return [Finding("high", "git-force", "force/delete push rewrites shared history")]
        return [Finding("medium", "git-push", "push sends code off this machine")]
    if sub == "reset" and "--hard" in rest:
        return [Finding("high", "git-hard", "`git reset --hard` discards work")]
    if sub == "clean" and any(a.startswith("-") and "f" in a for a in rest):
        return [Finding("high", "git-clean", "`git clean -f` deletes untracked files for good")]
    if sub == "branch" and any(a in {"-d", "--delete"} for a in rest):
        return [Finding("medium", "git-branch", "deletes a branch")]
    if sub in {"filter-branch", "filter-repo"}:
        return [Finding("high", "git-rewrite", "rewrites history")]
    if sub in {"checkout", "restore"} and ("." in rest or "--" in rest):
        return [Finding("medium", "git-discard", "discards uncommitted changes")]
    if sub == "stash" and rest[:1] in (["drop"], ["clear"]):
        return [Finding("medium", "git-stash", "drops stashed work")]
    if sub == "clone":
        return [Finding("medium", "git-clone", "downloads a repository — check the source tier")]
    return []


def _delete(args: list[str], cfg: Config, cwd: Path) -> Finding:
    targets = [a for a in args if not a.startswith("-") and not (len(a) <= 3 and a.startswith("/") and a[1:].isalpha())]
    for t in targets:
        if t.lower().rstrip("\\/") in {x.rstrip("\\/") for x in ROOTLIKE} or t.lower() in ROOTLIKE:
            return Finding("high", "delete-root", f"delete aimed at a root-like path `{t}`")
        rel = cfg.rel(t, cwd)
        if rel is None:
            return Finding("high", "delete-outside", f"delete outside the project: `{t}`")
        if rel in ("", ".") or rel == "":
            return Finding("high", "delete-root", "delete aimed at the project root")
        from .config import match_any
        if match_any(rel, cfg.section("zones").get("never_delete", [])):
            return Finding("high", "never-delete", f"`{rel}` is never_delete (ADRs, logs)")
    return Finding("medium", "delete-redirect",
                   "hard delete — use `python .outrigger/engine/cli.py trash <paths> --reason \"...\"` (restorable)")


def _net(head: str, args: list[str], low: list[str], cfg: Config) -> Finding:
    if any(a in UPLOAD_FLAGS for a in low) or any(a.startswith(("--data", "--form", "--upload")) for a in low):
        return Finding("high", "upload", f"`{head}` sends data to a server")
    for i, a in enumerate(low):
        if a in {"-x", "--request", "-method"} and i + 1 < len(low) and low[i + 1] in UPLOAD_METHODS:
            return Finding("high", "upload", f"`{head}` uses method {low[i + 1].upper()}")
    if head == "certutil" and "-urlcache" in low:
        return Finding("high", "download-lolbin", "certutil download (common malware technique)")
    hosts = [urlparse(a).hostname for a in args if "://" in a]
    hosts = [h for h in hosts if h]
    if hosts and all(cfg.host_allowed(h) for h in hosts):
        return Finding("low", "net-allowed", f"download from allow-listed host {hosts[0]}")
    return Finding("medium", "net-unknown",
                   f"download from a host not on the allow-list ({', '.join(hosts) or 'unknown'}) — T4 until checked")


def _outrigger_cli(args: list[str], cfg: Config, cwd: Path) -> list[Finding]:
    sub = args[1].lower() if len(args) > 1 else ""
    if sub in {"approve", "resume", "empty-trash", "deny", "trust", "untrust"}:
        return [Finding("high", "self-approval", f"`{sub}` is human-only")]
    if sub in {"trash", "archive"}:
        paths = [a for a in args[2:] if not a.startswith("-")]
        if "--reason" in args:
            i = args.index("--reason")
            paths = [a for a in args[2:i] if not a.startswith("-")] + [a for a in args[i + 2:] if not a.startswith("-")]
        files, size = 0, 0
        from .config import match_any
        for p in paths:
            rel = cfg.rel(p, cwd)
            if rel is None:
                return [Finding("high", "trash-outside", f"`{p}` is outside the project")]
            if match_any(rel, cfg.section("zones").get("never_delete", [])) and sub == "trash":
                return [Finding("high", "never-delete", f"`{rel}` is never_delete")]
            full = cfg.root / rel
            for f in ([full] if full.is_file() else full.rglob("*") if full.is_dir() else []):
                if f.is_file():
                    files += 1
                    size += f.stat().st_size
        if files > cfg.limit("trash_max_files") or size > cfg.limit("trash_max_mb") * 1024 * 1024:
            return [Finding("medium", "bulk", f"bulk {sub}: {files} files, {size / 1048576:.1f} MB")]
    return []


def _pack_run(pack, seg: list[str], low: list[str], cfg: Config, cwd: Path) -> list[Finding]:
    headless = pack.always_headless or any(f in low for f in pack.headless_flags)
    if not headless:
        return [Finding(pack.gui_level, "app", f"opens {pack.name} interactively — L2", app=f"{pack.name} (GUI)")]
    ext = pack.scan_extensions[0] if pack.scan_extensions else ""
    if pack.scan_scope == "target":
        return _pack_target(pack, seg, low, cfg, cwd, ext)
    inline = " ".join(seg[1:])
    hits = [f"<command>: {s}" for _, s in packs.scan_text(inline, pack, ext)]
    hits += packs.scan_project(cfg, pack)
    if hits:
        shown = "; ".join(hits[:8]) + (f"; … +{len(hits) - 8} more" if len(hits) > 8 else "")
        return [Finding("medium", "pack-danger",
                        f"{pack.name} headless run, but the code contains dangerous calls: {shown}",
                        app=f"{pack.name} (headless)")]
    return [Finding("low", "pack-headless", f"{pack.name} headless run (L1), code scan clean", app=f"{pack.name} (headless)")]


def _pack_target(pack, seg: list[str], low: list[str], cfg: Config, cwd: Path, ext: str) -> list[Finding]:
    """Scan only what this command runs: inline code (`-c "..."`) and the script files it names.

    Outrigger's own engine is trusted (it is protected from edits). Modules run with `-m` are not scanned:
    that is a documented limit (R-07 in docs/RISK_REGISTER.md)."""
    hits: list[str] = []
    for i, flag in enumerate(low):  # `low` is seg[1:] lower-cased, so the code follows at seg[i + 2]
        if flag in {"-c", "-e", "--eval", "-p", "--print"} and i + 2 < len(seg):
            hits += [f"<{flag}>: {s}" for _, s in packs.scan_text(seg[i + 2], pack, ext)]
    for a in seg[1:]:
        if not a.lower().endswith(tuple(pack.scan_extensions)):
            continue
        rel = cfg.rel(a, cwd)
        if rel is not None and (rel.lower().startswith(".outrigger/engine/") or state.is_trusted(cfg, rel)):
            continue
        p = (cfg.root / rel) if rel is not None else Path(a)
        if not p.is_file():
            continue
        try:
            text = p.read_text(encoding="utf-8", errors="replace")
        except OSError:
            return [Finding("medium", "pack-unreadable", f"cannot read `{a}` to check it")]
        hits += [f"{rel or a}:{n}: {s}" for n, s in packs.scan_text(text, pack, p.suffix.lower())]
    if hits:
        shown = "; ".join(hits[:8]) + (f"; … +{len(hits) - 8} more" if len(hits) > 8 else "")
        return [Finding("medium", "pack-danger", f"{pack.name} script contains dangerous calls: {shown}")]
    return []


# ---------------------------------------------------------------------------------------------
# Scripts: what a command RUNS matters as much as the command itself (risk R-07, gap G-1).
# Shell-like scripts are parsed and judged with the same rules as a typed command.
# ---------------------------------------------------------------------------------------------

SCRIPT_POSIX = {".sh", ".bash", ".zsh"}
SCRIPT_WINDOWS = {".ps1", ".psm1", ".bat", ".cmd"}
SCRIPT_EXTS = SCRIPT_POSIX | SCRIPT_WINDOWS
SCRIPT_RUNNERS = {"bash", "sh", "zsh", "dash", "source", "powershell", "pwsh", "cmd"}
NPM_RUNNERS = {"npm", "pnpm", "yarn", "bun"}
NPM_LIFECYCLE = {"test", "start", "build", "stop", "restart"}
INTERESTING = DELETE | SYSTEM_HIGH | SCHEDULE | NET | NET_OUT | set(INSTALLERS) | {"git", "reg", "netsh", "sc"}
MAX_SCRIPT_DEPTH = 3
MAX_SCRIPT_BYTES = 400_000


def _ext(token: str) -> str:
    return Path(token.strip("\"'")).suffix.lower()


def _script_files(seg: list[str], head: str, cfg: Config, cwd: Path, depth: int) -> list[Finding]:
    candidates = [seg[0]] if _ext(seg[0]) in SCRIPT_EXTS else []
    if head in SCRIPT_RUNNERS:
        candidates += [a for a in seg[1:] if _ext(a) in SCRIPT_EXTS]
    out: list[Finding] = []
    for c in candidates:
        out.extend(_scan_script(c.strip("\"'"), cfg, cwd, depth))
    return out


def _strip_script_comments(text: str, ext: str) -> str:
    if ext in {".bat", ".cmd"}:
        return re.sub(r"(?im)^\s*(rem\b|::).*$", "", text)
    if ext in {".ps1", ".psm1"}:
        text = re.sub(r"(?s)<#.*?#>", "", text)
    return re.sub(r"(?m)(^|\s)#.*$", r"\1", text)


def _judge_text(text: str, posix: bool, cfg: Config, cwd: Path, depth: int) -> list[Finding]:
    """Run the command rules over a piece of script text. Raises ParseError when it cannot be read safely."""
    segments = split_commands(text, posix=posix)
    found = check_raw(text, segments, cfg, cwd)
    for seg in segments:
        for i, tok in enumerate(seg):
            if i == 0 or base(tok) in INTERESTING:      # also catches `if (...) { Remove-Item x }`
                found += check_segment(seg[i:], cfg, cwd, depth + 1)
    seen, out = set(), []
    for f in found:
        if f.risk != "low" and (f.code, f.reason) not in seen:
            seen.add((f.code, f.reason))
            out.append(f)
    return out


def _scan_script(arg: str, cfg: Config, cwd: Path, depth: int) -> list[Finding]:
    rel = cfg.rel(arg, cwd)
    label = rel or arg
    if rel is not None and (rel.lower().startswith(".outrigger/engine/") or state.is_trusted(cfg, rel)):
        return []
    p = (cfg.root / rel) if rel is not None else Path(arg)
    if not p.is_file():
        return []
    if depth >= MAX_SCRIPT_DEPTH:
        return [Finding("medium", "script-depth", f"`{label}`: scripts call each other too deeply to check")]
    try:
        if p.stat().st_size > MAX_SCRIPT_BYTES:
            return [Finding("medium", "script-too-large", f"`{label}` is too large to check")]
        text = p.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return [Finding("medium", "script-unreadable", f"cannot read `{label}` to check it")]
    ext = p.suffix.lower()
    try:
        found = _judge_text(_strip_script_comments(text, ext), ext in SCRIPT_POSIX, cfg, p.parent, depth)
    except ParseError as exc:
        return [Finding("medium", "script-unparsed", f"`{label}` cannot be parsed safely ({exc}) — review it, then `cli.py trust` it")]
    return [Finding(f.risk, f.code if f.code.startswith("script") else f"script:{f.code}", f"`{label}` → {f.reason}", f.app)
            for f in found[:12]]


def _npm_script(head: str, args: list[str], cfg: Config, cwd: Path, depth: int) -> list[Finding]:
    """`npm run clean` is only as safe as the `clean` entry in package.json."""
    if not args or depth >= MAX_SCRIPT_DEPTH:
        return []
    first = args[0].lower()
    if first in {"run", "run-script"} and len(args) > 1:
        name = args[1]
    elif first in NPM_LIFECYCLE or (head != "npm" and not first.startswith("-")):
        name = args[0]
    else:
        return []
    pkg = cwd / "package.json"
    rel = cfg.rel(pkg, cwd)
    if not pkg.is_file() or (rel is not None and state.is_trusted(cfg, rel)):
        return []
    try:
        scripts = json.loads(pkg.read_text(encoding="utf-8", errors="replace")).get("scripts", {})
    except (OSError, ValueError):
        return [Finding("medium", "script-unreadable", "cannot read package.json to check the script")]
    out: list[Finding] = []
    for n in (f"pre{name}", name, f"post{name}"):
        body = scripts.get(n) if isinstance(scripts, dict) else None
        if not isinstance(body, str):
            continue
        try:
            found = _judge_text(body, True, cfg, cwd, depth)
        except ParseError as exc:
            found = [Finding("medium", "script-unparsed", f"cannot be parsed safely ({exc})")]
        out += [Finding(f.risk, f.code if f.code.startswith("script") else f"script:{f.code}",
                        f"package.json script `{n}` → {f.reason}", f.app) for f in found[:8]]
    return out


# ---------------------------------------------------------------------------------------------
# Secrets must not enter git history: once pushed they cannot be taken back (risk R-19).
# ---------------------------------------------------------------------------------------------

SECRET_COMMIT = [
    ("AWS access key", re.compile(r"\b(AKIA|ASIA)[0-9A-Z]{16}\b")),
    ("GitHub token", re.compile(r"\b(gh[pousr]_[A-Za-z0-9]{36,}|github_pat_[A-Za-z0-9_]{40,})")),
    ("private key", re.compile(r"-----BEGIN [A-Z ]*PRIVATE KEY-----")),
    ("API key (sk-…)", re.compile(r"\bsk-[A-Za-z0-9_-]{32,}")),
    ("Slack token", re.compile(r"\bxox[abprs]-[A-Za-z0-9-]{10,}")),
    ("Google API key", re.compile(r"\bAIza[0-9A-Za-z_-]{35}\b")),
    ("password inside a URL", re.compile(r"[a-z][a-z0-9+.-]*://[^/\s:@'\"]+:[^/\s:@'\"${}<>*]{6,}@[^\s/]+")),
]


def _staged_secrets(low: list[str], cfg: Config, cwd: Path) -> list[Finding]:
    include_unstaged = any(a == "--all" or re.fullmatch(r"-[a-z]*a[a-z]*", a) for a in low[1:])
    diffs = [["git", "diff", "--cached", "-U0", "--no-color"]]
    if include_unstaged:
        diffs.append(["git", "diff", "-U0", "--no-color"])
    hits: list[str] = []
    for cmd in diffs:
        try:
            r = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=20)
        except (OSError, subprocess.SubprocessError) as exc:
            return [Finding("medium", "secret-scan-failed", f"could not check the commit for secrets ({type(exc).__name__})")]
        if r.returncode != 0:      # not a repository etc. — git itself will refuse the commit
            continue
        current = "?"
        for line in r.stdout.splitlines():
            if line.startswith("+++ "):
                current = line[6:] if line.startswith("+++ b/") else line[4:]
                if current != "/dev/null" and cfg.zone_of(current) == "red":
                    hits.append(f"{current} (secret file)")
            elif line.startswith("+"):
                for label, pat in SECRET_COMMIT:
                    if pat.search(line):
                        hits.append(f"{current} ({label})")
    hits = sorted(set(hits))
    if not hits:
        return []
    shown = "; ".join(hits[:6]) + (f"; … +{len(hits) - 6} more" if len(hits) > 6 else "")
    return [Finding("high", "secret-commit",
                    f"the commit would put secrets into git history: {shown}. Remove them (and rotate the key if it was real)")]
