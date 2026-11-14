# Deleting, archiving, scheduling

## Delete = soft delete

```
python .outrigger/engine/cli.py trash <path> [<path> ...] --reason "why"
python .outrigger/engine/cli.py trash-list
python .outrigger/engine/cli.py restore <trash-id>
```

- Files move to `.outrigger/state/trash/<stamp>/` with a manifest (original path, SHA-256, size, reason, time).
- More than 20 files or 100 MB at once → the guard asks for approval.
- `rm`, `del`, `Remove-Item`, `rmdir`, MATLAB `delete(...)` are blocked — use `trash`.
- Hard delete (`empty-trash`) is **human-only** and asks for a typed confirmation.
- `docs/decisions/**` (ADRs) and Outrigger logs are `never_delete`. To retire an ADR, write a new one that supersedes it.

## Archive

`python .outrigger/engine/cli.py archive <path> --reason "..."` moves files to `archive/<YYYY-MM>/` with a manifest.
Use it when the user wants to tidy old work they may still need, including old agent memory notes.

## Cleaning agent memory or old notes

1. List what you propose to remove and why (GATE).
2. Archive (not trash) the chosen files.
3. Report what moved where.

## Schedules

Creating, changing or deleting a schedule — `schtasks`, `Register-ScheduledTask`, `crontab`, `at`, agent
cron or routine tools — is **high risk**: GATE + ADR, and the guard notifies the user's phone.
A schedule proposal states: what runs, at which level (unattended runs are **at most L1**), owner,
**expiry date** (mandatory), how to stop it, where its reports go.

## Kill switch

A file named `STOP` in the project root blocks every non-read tool. Create it with
`python .outrigger/engine/cli.py stop "reason"`. Only the human can remove it (`cli.py resume`).
