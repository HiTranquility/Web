"""Split a shell command line into simple commands (token lists).

Heuristic, not a full shell parser. Lessons kept from IPLMS guard_command.py (bug-052, bug-053):
heredoc bodies are data, redirections are not arguments. Unlike IPLMS, anything we cannot parse
raises ParseError and the caller fails closed.
"""
from __future__ import annotations

import re
import shlex

SEPARATORS = {"&&", "||", ";", "|", "&", "\n", ";;", "|&"}
WRAPPERS = {"bash", "sh", "zsh", "dash", "cmd", "powershell", "pwsh"}
WRAPPER_FLAGS = {"-c", "/c", "/k", "-command", "-c:", "-commandwithargs"}
PREFIXES = {"sudo", "env", "nohup", "time", "exec", "command", "builtin", "nice", "xargs", "call", "&", ".",
            "if", "then", "else", "elif", "do", "while", "until", "!", "{", "source"}
HEREDOC = re.compile(r"<<-?\s*(['\"]?)([A-Za-z_][A-Za-z0-9_]*)\1")
SUBST = re.compile(r"\$\(([^()]*(?:\([^()]*\)[^()]*)*)\)|`([^`]*)`")
REDIRECT = re.compile(r"^\d*(>>?|<|>&|&>)(.*)$")
ASSIGN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*=")


class ParseError(ValueError):
    pass


def base(token: str) -> str:
    """`C:\\Program Files\\MATLAB\\bin\\matlab.exe` -> `matlab`."""
    name = token.strip("\"'").replace("\\", "/").rsplit("/", 1)[-1].lower()
    for ext in (".exe", ".cmd", ".bat", ".ps1", ".com"):
        if name.endswith(ext):
            return name[: -len(ext)]
    return name


def strip_heredocs(command: str) -> str:
    lines = command.split("\n")
    out, terminator = [], None
    for line in lines:
        if terminator is not None:
            if line.strip() == terminator:
                terminator = None
            continue
        m = HEREDOC.search(line)
        if m:
            terminator = m.group(2)
            line = line[: m.start()]
        out.append(line)
    return "\n".join(out)


def _tokens(command: str, posix: bool) -> list[str]:
    lex = shlex.shlex(command, posix=posix, punctuation_chars="|&;\n")
    lex.whitespace = " \t\r"
    lex.whitespace_split = True
    lex.commenters = ""
    try:
        toks = list(lex)
    except ValueError as exc:  # unbalanced quotes
        raise ParseError(str(exc)) from exc
    if not posix:
        toks = [t[1:-1] if len(t) >= 2 and t[0] == t[-1] and t[0] in "\"'" else t for t in toks]
    return toks


def _clean(segment: list[str]) -> list[str]:
    """Drop redirections, env assignments and harmless prefixes; keep the real command first."""
    out, skip_next = [], False
    for tok in segment:
        if skip_next:
            skip_next = False
            continue
        m = REDIRECT.match(tok)
        if m:
            if not m.group(2):
                skip_next = True
            continue
        out.append(tok)
    while out and (ASSIGN.match(out[0]) or base(out[0]) in PREFIXES):
        head = base(out.pop(0))
        if head in {"sudo", "env", "nice", "xargs"}:
            # their own options (-u user, -n 10 ...) come before the command
            while out and out[0].startswith("-"):
                out.pop(0)
        if head == "sudo":
            out.insert(0, "__sudo__")
            break
    return out


def split_commands(command: str, posix: bool = True, depth: int = 0) -> list[list[str]]:
    """Return a list of simple commands. Nested `bash -c "..."`, `$(...)` and backticks are expanded."""
    if depth > 4:
        raise ParseError("nesting too deep")
    command = strip_heredocs(command)
    nested: list[list[str]] = []
    for m in SUBST.finditer(command):
        inner = m.group(1) if m.group(1) is not None else m.group(2)
        nested.extend(split_commands(inner, posix, depth + 1))
    segments: list[list[str]] = []
    current: list[str] = []
    for tok in _tokens(command, posix) + ["\n"]:
        if tok in SEPARATORS or (tok and set(tok) <= set("|&;\n")):
            if current:
                segments.append(current)
            current = []
        else:
            current.append(tok)
    result: list[list[str]] = []
    for seg in segments:
        seg = _clean(seg)
        if not seg:
            continue
        head = base(seg[0])
        if head in WRAPPERS:
            flags = [t.lower() for t in seg[1:]]
            if any(f in ("-encodedcommand", "-enc", "-e", "-ec") for f in flags):
                result.append(["__encoded__"] + seg)
                continue
            idx = next((i for i, t in enumerate(seg[1:], 1) if t.lower() in WRAPPER_FLAGS), None)
            if idx is not None and idx + 1 < len(seg):
                inner = " ".join(seg[idx + 1:])
                result.extend(split_commands(inner, posix=head not in {"cmd", "powershell", "pwsh"} and posix,
                                             depth=depth + 1))
                continue
        result.append(seg)
    return result + nested
