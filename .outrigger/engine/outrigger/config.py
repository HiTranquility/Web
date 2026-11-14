"""Load .outrigger/outrigger.toml and pack manifests; resolve the project root; match zone patterns."""
from __future__ import annotations

import fnmatch
import os
import tomllib
from dataclasses import dataclass, field
from pathlib import Path

DEFAULTS: dict = {
    "project": {"name": ""},
    "zones": {
        "green": ["sandbox/**", "reports/**", "docs/decisions/**", ".outrigger/local/workflows/drafts/**"],
        "red": [".env", ".env.*", "**/.env", "**/.env.*", "**/*.pem", "**/*.key", "**/*.pfx",
                "**/id_rsa*", "**/credentials.json", "**/token.txt", "**/secretkeys/**"],
        "red_allow": ["**/.env.example", ".env.example"],
        "protected": [".outrigger/engine/**", ".outrigger/outrigger.toml", ".outrigger/OUTRIGGER.md", ".outrigger/rules/**", ".outrigger/packs/**", ".outrigger/state/**", ".outrigger/local/notify.toml",
                      ".claude/settings.json", ".claude/settings.local.json", ".mcp.json", "stop"],
        "never_delete": ["docs/decisions/**", ".outrigger/state/logs/**"],
        "outside_allow": ["~/.claude/**", "$TEMP/**", "$TMP/**"],
        "memory": ["~/.claude/projects/**/memory/**", "~/.codex/memories/**", "~/.gemini/memory/**"],
        "agent_config": ["~/.claude/CLAUDE.md", "~/.claude/settings.json", "~/.claude/settings.local.json",
                         "~/.claude/skills/**", "~/.claude/agents/**", "~/.claude/commands/**", "~/.claude/rules/**",
                         "~/.claude.json", "~/.codex/**", "~/.gemini/**", "~/.cursor/**", "~/.agents/**"],
    },
    "limits": {"trash_max_files": 20, "trash_max_mb": 100, "backup_max_mb": 50,
               "approval_minutes": 30, "scan_max_files": 3000},
    "sources": {"allow_hosts": ["mathworks.com", "python.org", "pypi.org", "github.com"]},
    "notify": {"on": ["high"]},
}


@dataclass
class Pack:
    id: str
    name: str
    commands: list[str]
    headless_flags: list[str]
    scan_extensions: list[str]
    dangerous: list[str]
    gui_level: str = "medium"
    scan_scope: str = "project"   # "project": every file with scan_extensions · "target": only the files named in the command
    always_headless: bool = False  # the tool has no GUI to open (python)


@dataclass
class Config:
    root: Path
    data: dict
    packs: list[Pack] = field(default_factory=list)

    def section(self, name: str) -> dict:
        return self.data.get(name, {})

    @property
    def state_dir(self) -> Path:
        return self.root / ".outrigger" / "state"

    @property
    def project_name(self) -> str:
        return self.section("project").get("name") or self.root.name

    def limit(self, key: str) -> int:
        return int(self.section("limits").get(key, DEFAULTS["limits"][key]))

    def rel(self, path: str | Path, cwd: Path | None = None) -> str | None:
        """Project-relative posix path, or None when outside the project."""
        p = Path(os.path.expandvars(os.path.expanduser(str(path))))
        if not p.is_absolute():
            p = (cwd or self.root) / p
        try:
            return p.resolve(strict=False).relative_to(self.root.resolve()).as_posix()
        except ValueError:
            return None

    def zone_of(self, rel: str) -> str:
        z = self.section("zones")
        if match_any(rel, z.get("red", [])) and not match_any(rel, z.get("red_allow", [])):
            return "red"
        if match_any(rel, z.get("protected", [])):
            return "protected"
        if match_any(rel, z.get("green", [])):
            return "green"
        return "yellow"

    def outside_kind(self, path: str | Path) -> str | None:
        """For a path outside the project: 'memory', 'agent-config', 'allow' or None (not allowed)."""
        p = Path(os.path.expandvars(os.path.expanduser(str(path)))).resolve(strict=False).as_posix().lower()
        zones = self.section("zones")
        for kind, key in (("memory", "memory"), ("agent-config", "agent_config"), ("allow", "outside_allow")):
            for pat in zones.get(key, []):
                expanded = os.path.expandvars(os.path.expanduser(pat))
                if "$" in expanded:  # unset variable: never match
                    continue
                glob = Path(expanded.replace("**", "\x00")).resolve(strict=False).as_posix().lower().replace("\x00", "**")
                if fnmatch.fnmatchcase(p, glob) or (glob.endswith("/**") and p == glob[:-3]):
                    return kind
        return None

    def outside_allowed(self, path: str | Path) -> bool:
        return self.outside_kind(path) in {"allow", "memory"}

    def host_allowed(self, host: str) -> bool:
        host = host.lower().strip(".")
        for allowed in self.section("sources").get("allow_hosts", []):
            allowed = allowed.lower().strip(".")
            if host == allowed or host.endswith("." + allowed):
                return True
        return False


def match_any(rel: str, patterns: list[str]) -> bool:
    rel = rel.lower()
    for pat in patterns:
        pat = pat.lower()
        if fnmatch.fnmatchcase(rel, pat):
            return True
        if pat.startswith("**/") and fnmatch.fnmatchcase(rel, pat[3:]):
            return True
        if pat.endswith("/**") and rel == pat[:-3]:
            return True
    return False


def find_root(start: Path) -> Path:
    """Nearest ancestor holding .outrigger/outrigger.toml; else CLAUDE_PROJECT_DIR; else start."""
    start = start.resolve(strict=False)
    for d in (start, *start.parents):
        if (d / ".outrigger" / "outrigger.toml").is_file():
            return d
    env = os.environ.get("CLAUDE_PROJECT_DIR")
    return Path(env).resolve(strict=False) if env else start


def _merge(base: dict, over: dict) -> dict:
    out = {k: (dict(v) if isinstance(v, dict) else v) for k, v in base.items()}
    for k, v in over.items():
        out[k] = _merge(out[k], v) if isinstance(v, dict) and isinstance(out.get(k), dict) else v
    return out


def load(start: Path) -> Config:
    root = find_root(start)
    data = DEFAULTS
    cfg_file = root / ".outrigger" / "outrigger.toml"
    if cfg_file.is_file():
        data = _merge(DEFAULTS, tomllib.loads(cfg_file.read_text(encoding="utf-8")))
    cfg = Config(root=root, data=data)
    packs_dir = root / ".outrigger" / "packs"
    if packs_dir.is_dir():
        for manifest in sorted(packs_dir.glob("*/pack.toml")):
            cfg.packs.extend(_load_pack(manifest))
    return cfg


def _load_pack(manifest: Path) -> list[Pack]:
    d = tomllib.loads(manifest.read_text(encoding="utf-8"))
    meta = d.get("pack", {})
    out = []
    for t in d.get("tools", []):
        out.append(Pack(
            id=meta.get("id", manifest.parent.name),
            name=meta.get("name", manifest.parent.name),
            commands=[c.lower() for c in t.get("commands", [])],
            headless_flags=[f.lower() for f in t.get("headless_flags", [])],
            scan_extensions=[e.lower() for e in t.get("scan_extensions", [])],
            dangerous=list(t.get("dangerous", [])),
            gui_level=t.get("gui_level", "medium"),
            scan_scope=t.get("scan_scope", "project"),
            always_headless=bool(t.get("always_headless", False)),
        ))
    return out
