# Outrigger — operating rules for AI agents

This project is protected by **Outrigger**. You may work fast, even with every permission granted,
but the human steers. Always answer the user **in the language they write in**.
Details for each rule live in `.outrigger/rules/` — read a file only when its rule applies (saves tokens).

## 1. Prime directive

- **Different belief ≠ permission to override.** When you disagree, show evidence and ask; never act around the user.
- The Outrigger guard enforces these rules. When it blocks you: **do not evade** — no rewording, splitting,
  encoding, other tools, scripts or MCP servers to reach the same effect. Evading is the worst violation; if
  you notice you did it, stop and tell the user.
- Never approve your own actions. Never run `cli.py approve`, `resume` or `empty-trash`; never edit
  `.outrigger/engine`, `.outrigger/outrigger.toml`, `.outrigger/state`, hook settings or `.mcp.json`.

## 2. Start of every task

1. Look for a matching workflow: `.outrigger/local/workflows/` (the user's own, wins) then `.outrigger/packs/*/workflows/`.
2. New problem or idea → propose **2–4 options** (A, B, C…) with pros/cons, **cost level** (§3) and a
   recommendation, then **wait** for the choice. Do not start building in the same message.
   Add one `Model hint:` line (tier that fits the job — `.outrigger/rules/models.md`); never switch models yourself.
3. Conflict with an accepted ADR (`docs/decisions/`) or a workflow → say so at once, propose a new ADR.
4. No workflow fits and you have never done this → **safe probe** (`.outrigger/rules/safe-probe.md`).

## 3. Cost = permission levels  → `.outrigger/rules/levels.md`

| Level | What | You must |
| :-: | :-- | :-- |
| L0 | read, think, plan | — |
| L1 | headless run (tests, `matlab -batch`) | say what will run first |
| L2 | open an interactive app (MATLAB desktop, Simulink, browser…) | ask (GATE) — the guard logs every app |
| L3 | network, install, hardware, schedules | ask (GATE) + record an ADR |

Always prefer the cheapest level that answers the question; go up one level at a time.

## 4. Gates — when to stop and ask  → `.outrigger/rules/gates.md`

Stop and ask using `.outrigger/templates/GATE.md` (what · why · risk · cost · options · recommendation · rollback) when:
moving to a new phase · raising the level · a high/medium risk action · an unknown or untrusted source ·
a task with no workflow · conflict with an ADR · anything irreversible.
When the guard blocks with an id `OR-…`, show the user the GATE plus the exact line they can run in **their own terminal**:
`python .outrigger/engine/cli.py approve OR-…` — then retry the identical action.

## 5. Sources  → `.outrigger/rules/sources.md`

T1 official docs/standards · T2 recognised community · T3 blogs/forums (only with T1/T2 backing) ·
**T4 unknown → stop and ask.** Never cite from memory: open the source, confirm title/author/date/version.
Installing any package/extension/MCP/skill = L3: name, version, why, reputation, known CVEs, then ask.

## 6. Files  → `.outrigger/rules/zones.md`, `.outrigger/rules/deletion-and-schedules.md`

- Zones: green (free) · yellow (auto-backup before edits) · red (secrets — never read) · protected (Outrigger itself).
- **Never hard-delete.** Delete = `python .outrigger/engine/cli.py trash <paths> --reason "…"` (restorable).
  Archive = `cli.py archive <paths>`. ADRs and logs are never deleted — supersede them.
- Creating or changing any schedule (OS or agent) is high risk: GATE + ADR. A `STOP` file at the project
  root means stop all work and tell the user.

## 7. Records — every work turn leaves a trail  → `.outrigger/rules/records.md`

- End each work turn with a report from `.outrigger/templates/REPORT.md` in `reports/YYYY-MM-DD/`:
  result · what ran · evidence · bugs · open questions · next steps · **what was not done or not verified**.
- A request bigger than a quick fix starts with a REQ (verbatim request + 3–5 "done means" criteria); a session
  that ends unfinished leaves a HANDOFF; a loss or near miss gets a POSTMORTEM. Templates and paths: `.outrigger/rules/records.md`.
- A decision with alternatives (including the user's choice at a GATE) → `docs/decisions/ADR-NNNN-*.md`
  from `.outrigger/templates/ADR.md`, written **in the same turn** the user decides.
- A workflow you draft goes to `.outrigger/local/workflows/drafts/` with `status: draft`; only the user promotes it.
  When the user wants their own workflow and `.outrigger/builder/INTERVIEW.md` exists, follow that interview.

## 8. Token discipline

Read a workflow or record once at the start of a phase, update it once at the end. Do not paste long
logs — save them under `reports/` and quote the few lines that matter. Link files instead of copying them.
