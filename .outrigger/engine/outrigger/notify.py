"""Phone / desktop notifications. Best effort: never raises, never blocks the guard for long.

A message carries only the risk, the project name and the approval id — never code, paths or data.
Settings: .outrigger/local/notify.toml (git-ignored)
    channel = "toast" | "ntfy" | "none"
    [ntfy]
    server = "https://ntfy.sh"      # or your own server
    topic  = "outrigger-<long random>"   # treat it like a password
"""
from __future__ import annotations

import subprocess
import sys
import tomllib
import urllib.request

from .config import Config


def settings(cfg: Config) -> dict:
    f = cfg.root / ".outrigger" / "local" / "notify.toml"
    if f.is_file():
        try:
            return tomllib.loads(f.read_text(encoding="utf-8"))
        except (OSError, tomllib.TOMLDecodeError):
            return {"channel": "toast"}
    return {"channel": "toast"}


def send(cfg: Config, risk: str, title: str, body: str) -> str:
    if risk not in cfg.section("notify").get("on", ["high"]):
        return "skipped"
    s = settings(cfg)
    channel = s.get("channel", "toast")
    try:
        if channel == "ntfy":
            ntfy = s.get("ntfy", {})
            topic = ntfy.get("topic", "")
            if not topic:
                return "ntfy: no topic configured"
            req = urllib.request.Request(
                f"{ntfy.get('server', 'https://ntfy.sh').rstrip('/')}/{topic}",
                data=body.encode("utf-8"),
                headers={"Title": title.encode("ascii", "replace").decode(), "Priority": "high" if risk == "high" else "default",
                         "Tags": "warning"},
                method="POST")
            urllib.request.urlopen(req, timeout=4).close()
            return "ntfy: sent"
        if channel == "toast" and sys.platform == "win32":
            _toast(title, body)
            return "toast: sent"
        return f"{channel}: nothing sent"
    except Exception as exc:  # noqa: BLE001 — notification must never break the guard
        return f"{channel}: failed ({type(exc).__name__})"


def _toast(title: str, body: str) -> None:
    def esc(s: str) -> str:
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "''")
    script = (
        "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;"
        "[Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] > $null;"
        f"$x = New-Object Windows.Data.Xml.Dom.XmlDocument; $x.LoadXml('<toast><visual><binding template=\"ToastGeneric\">"
        f"<text>{esc(title)}</text><text>{esc(body)}</text></binding></visual></toast>');"
        "$t = [Windows.UI.Notifications.ToastNotification]::new($x);"
        # An unregistered app id shows nothing on Windows 10/11, so borrow PowerShell's registered one.
        "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier("
        "'{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\\WindowsPowerShell\\v1.0\\powershell.exe').Show($t)"
    )
    subprocess.Popen(["powershell", "-NoProfile", "-NonInteractive", "-Command", script],
                     stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                     creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0))
