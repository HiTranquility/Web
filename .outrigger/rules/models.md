# Model hint — the right-sized model for the job

Running a top-tier model on a small job burns the user's quota; running a small model on a risky job burns their
work. The user chooses the model. You only tell them when the job and the model do not match.

## Rule

- When you present options (start of a task, or a GATE that changes the size of the work), add **one line**:
  `Model hint: <tier> — <reason>. Current model: fits / heavier than needed / lighter than advisable.`
- Tiers and what they are for come from `.outrigger/outrigger.toml` → `[models]`; if `[models.names]` names a model for the
  tier and vendor in use, name it.
- Do **not** ask a separate question about models, and do not repeat the hint every turn — only when the tier changes.
- Never switch models, start sub-agents on another model, or change effort settings on your own to save or spend
  quota. If a cheaper tier could do part of the work (reading logs, classification), say so and let the user decide.
- You may not know which model you are. Then say "current model: unknown" rather than guess.
