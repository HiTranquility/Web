# Records — what to write, where

| Record | When | Where | Template |
| :-- | :-- | :-- | :-- |
| GATE | before any gated action | in chat (copy it into the report) | `GATE.md` |
| REPORT | end of every work turn | `reports/YYYY-MM-DD/HHMM-<slug>.md` | `REPORT.md` |
| ADR | the user chooses between alternatives, or you pick between real alternatives mid-task | `docs/decisions/ADR-NNNN-<slug>.md` | `ADR.md` |
| SPIKE | safe probe of an unknown task | `sandbox/spikes/SPIKE-NNN-<slug>/README.md` | `SPIKE.md` |
| WORKFLOW | a repeatable procedure | drafts: `.outrigger/local/workflows/drafts/`; approved: `.outrigger/local/workflows/` | `WORKFLOW.md` |
| BUG | a defect outside the current task | `reports/bugs/BUG-NNN-<slug>.md` | `BUG.md` |
| REQ | a new request that is more than a quick fix — **before** building | `docs/requirements/REQ-NNN-<slug>.md` | `REQ.md` |
| HANDOFF | a session ends with work unfinished, or the task passes to someone else | `reports/YYYY-MM-DD/handoff-<slug>.md` | `HANDOFF.md` |
| STATUS | the user asks for a weekly / periodic summary | `reports/status/YYYY-MM-DD.md` | `STATUS.md` |
| POSTMORTEM | something was lost, leaked or broken — or nearly was | `docs/postmortems/PM-NNN-<slug>.md` | `POSTMORTEM.md` |
| RISK | once per project; reviewed at each phase change | `docs/RISK.md` | `RISK.md` |
| MEETING | the user gives meeting notes or a transcript | `docs/meetings/YYYY-MM-DD-<slug>.md` | `MEETING.md` |

Which one? idea or request → REQ · choice made → ADR · unknown territory → SPIKE · work done → REPORT · work
unfinished → HANDOFF · something went wrong → POSTMORTEM (and a BUG if code is at fault) · could go wrong → RISK.
Do not invent a new document format: if none fits, ask the user, then draft a template in `.outrigger/local/`.

- Numbers (`NNNN`, `NNN`) continue from the highest existing file of that type; never reuse one.
- Search for an existing record on the same topic before creating a new one.
- The guard writes machine logs: `.outrigger/state/logs/audit-YYYY-MM.jsonl`, `apps.jsonl`, and a daily
  summary in `reports/outrigger/YYYY-MM-DD.md`. Do not edit them.
- Every report says what was **not** done or **not** verified. "Done" without evidence is not done.
