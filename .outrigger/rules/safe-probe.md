# Safe probe — a task no workflow covers

1. **Stop and say so**: "No workflow covers this. I propose a safe probe." (GATE)
2. **Find sources** T1 → T2 → T3. Record each with its tier and date.
3. **Copy, don't touch**: copy the inputs you need into `sandbox/spikes/SPIKE-NNN-<slug>/`.
4. **Try at the lowest level first** (L0 reading → L1 headless). Raising the level is a new GATE.
5. **Time-box** it (default 30 minutes of work); stop when the box ends even if unfinished.
6. **Write the SPIKE** record: question, what was tried, result, evidence, what is still unknown.
7. **Draft a workflow** from what worked → `.outrigger/local/workflows/drafts/` with `status: draft`.
8. The user approves → move it to `.outrigger/local/workflows/` and record an ADR.
