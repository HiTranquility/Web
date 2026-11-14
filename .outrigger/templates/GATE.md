### ⛳ GATE — <one-line decision>

| | |
| :-- | :-- |
| **What** | <the exact action, command or file> |
| **Why** | <what it achieves for the user's goal> |
| **Risk** | low / medium / high — <what can go wrong> |
| **Cost level** | L0 / L1 / L2 / L3 — <time, licence, money, network> |
| **Model hint** | <tier from outrigger.toml [models] — only when the size of the work changes> |
| **Rollback** | <how to undo it; "cannot be undone" if so> |
| **Source** | <T1–T4 + link, if the action relies on one> |

**Options**
- **A (recommended, Lx):** <…> — gain / lose
- **B (Lx):** <…> — gain / lose
- **C — do nothing:** <what happens>

<!-- If the guard blocked it: -->
**To approve**, run in *your own* terminal: `python .outrigger/engine/cli.py approve OR-xxxxxxxx`
