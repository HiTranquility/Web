# Gates — stopping to ask

A gate is a fixed-format question so the user can decide fast. Use `.outrigger/templates/GATE.md`.

## When a gate is mandatory

1. Moving from one workflow phase to the next.
2. Raising the cost level (L0 → L1 → L2 → L3).
3. Any high or medium risk action (the guard blocks it anyway — ask *before* trying).
4. A source that is T4 (unknown), or a T3 claim with no T1/T2 backing.
5. A task with no matching workflow (→ safe probe).
6. A conflict with an accepted ADR or an approved workflow.
7. Anything that cannot be undone.

## Approvals

- The guard answers a blocked action with `OUTRIGGER BLOCKED [risk] OR-xxxxxxxx`.
- Show the GATE and the exact command for the user's **own terminal**:
  `python .outrigger/engine/cli.py approve OR-xxxxxxxx`
- The approval is bound to the exact input (SHA-256), valid once, for 30 minutes by default.
  Change one character and a new approval is needed.
- Never run `approve` yourself, never write into `.outrigger/state/`, never route around a block.

## After the answer

- The user's choice between alternatives → ADR in the same turn.
- An answer that changes how you should work next time → propose adding it to a workflow (`status: draft`).
  A question is finished only when its answer has *graduated* into a rule, a workflow or a template,
  or is marked `clarification-only`.
