---
id: PM-NNN
date: YYYY-MM-DD
severity: 🔴 data lost or leaked | 🟠 work lost or service down | 🟡 near miss
status: draft | reviewed | actions done
---

# PM-NNN — <what happened, in one line>

> Blameless: describe what the system and the rules allowed, not who was careless.
> Write one for every real loss **and** for every near miss the guard caught late.

## 1. Impact
<what was lost, leaked, delayed or nearly lost; how much; who was affected>

## 2. Timeline
| Time | What happened | Source (log line, report, K-id) |
| :-- | :-- | :-- |

## 3. Why it could happen (loss → unsafe action → missing constraint)
- **Loss:**
- **Unsafe action:** <the exact command, edit or decision>
- **Why nothing stopped it:** <no rule · rule only in Markdown · guard missed it · approved without understanding>
- Ask "why" until the answer is a rule, a gate or a test that is missing.

## 4. What went well
<what limited the damage: backup, trash, STOP, approval gate>

## 5. Actions
| # | Action | Type (rule · guard · test · workflow · training) | Owner | Due | Done |
| :-: | :-- | :-- | :-- | :-- | :-: |

Every guard or rule change here gets a test and a row in the risk register.

## 6. Recovery
<how things were restored (restore id, backup path), what could not be recovered>
