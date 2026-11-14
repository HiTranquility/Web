# Levels — cost and permission

Every action has a **cost level** (how much it costs to run and to undo) and a **risk** (what can go wrong).
Pick the cheapest level that answers the question. Going up a level is a gate.

| Level | Typical actions | Cost | Gate |
| :-: | :-- | :-- | :-- |
| **L0** | read files, search, plan, static reasoning | tokens only | none |
| **L1** | headless runs: unit tests, `matlab -batch`, linters, builds | CPU minutes, a licence seat for seconds | announce; the guard scans pack code first |
| **L2** | interactive apps: MATLAB desktop, Simulink, browser, GUI tools | licence seat held, user's screen, state hard to replay | GATE; the guard logs every app opened |
| **L3** | network, install, hardware, schedules, anything leaving the machine | money, supply-chain risk, irreversible | GATE + ADR |

## Risk (enforced by the guard)

| Risk | Guard action | Examples |
| :-: | :-- | :-- |
| high | block, phone notification, human approval | recursive delete outside the project, `git push --force`, `git reset --hard`, registry, firewall/antivirus, download piped into a shell, uploads, reading secrets, schedules, editing Outrigger itself, self-approval |
| medium | block, human approval | open a GUI app, unknown host, package install, hard delete inside the project, risky MCP tool, code with dangerous calls before a headless run |
| low | allow, log, backup in the yellow zone | reads, edits in the project, tests |

## Proposing options with cost

When you offer options, give each one its level, for example:

> **A (L1, about 1 min):** run `runtests` headless and read the result.
> **B (L2):** open the Simulink model and inspect it together — holds a licence seat, needs your approval.
