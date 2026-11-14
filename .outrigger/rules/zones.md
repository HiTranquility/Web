# Zones — where the agent may write

Configured in `.outrigger/outrigger.toml` → `[zones]`. The guard enforces them on every write tool and on shell commands.

| Zone | Default paths | Agent may |
| :-- | :-- | :-- |
| green | `sandbox/`, `reports/`, `docs/decisions/`, `.outrigger/local/workflows/drafts/` | write freely |
| yellow | everything else inside the project | write — the guard copies the old file to `.outrigger/state/backups/` first |
| red | `.env*` (except `.env.example`), keys, certificates, `credentials.json`, `SecretKeys/` | nothing — not even read |
| protected | `.outrigger/engine`, `.outrigger/outrigger.toml`, `.outrigger/state`, hook settings, `.mcp.json` | nothing — ask the user to edit |
| outside the project | anything not under the project root | blocked (medium) unless listed in `outside_allow` |

Experiments, copies and throw-away files go to `sandbox/`. To undo an edit, copy the file back from
`.outrigger/state/backups/<date>/` and tell the user which one.
