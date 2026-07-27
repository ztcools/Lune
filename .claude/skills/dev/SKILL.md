---
name: dev
description: >-
  Development work rules. Use when coding, developing, implementing features, building, fixing bugs,
  writing code, adding functionality, or doing any programming task.
  Enforces: code-only output (no docs/tests/comments unless asked), understand-before-build,
  optimal solutions over compliance, network resilience (retry, never use mirrors),
  dependencies-first (prefer mature libraries over DIY).
user-invocable: true
---

# Development Rules

Apply these rules for all development work.

## 1. Code Only
Do NOT produce documentation, README, changelogs, code comments, or tests unless explicitly asked.
Focus exclusively on implementing the requested functionality.

## 2. Understand First
Before writing any code, ensure the requirement is clear.
If ambiguous or missing details, ASK targeted clarifying questions. Do not assume.

## 3. Optimal Solutions
If the user's proposed approach is suboptimal, use the better approach.
Explain why in one sentence, then implement. Do not ask permission for obvious improvements.

## 4. Network Resilience
When accessing external resources (registries, APIs, GitHub, package managers):
- Retry up to 3 times with 5-second delays
- If all fail: "Network unreachable: <URL>. Please switch your proxy, then I'll retry."
- NEVER fall back to mirrors, alternative registries, or degraded solutions.

## 5. Dependencies First
Before implementing non-trivial functionality, search for mature libraries that solve it.
Only build from scratch when no suitable dependency exists.
Prefer: active maintenance, large community, minimal transitive dependencies.

## 6. Iterative Verify-Fix Loop
After every change: build → run → verify → fix → repeat.
Verify with actual data (curl, logs, UI), not just "code compiles."
Find bugs, memory leaks, performance issues. Fix root causes, not symptoms.

## 7. Quantified Results
Verification must produce measurable results.
Prefer quantification over assumptions: response time, row count, error count, memory delta.
