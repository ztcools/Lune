---
name: dev
description: >-
  Lune project development rules. Use when coding, fixing bugs, optimizing, refactoring, or implementing features.
  Enforces: iterative verify-fix-verify loop, quantified validation (not just compile-check), optimal over expedient,
  network resilience, dependencies-first, memory/performance awareness, deep root-cause analysis.
user-invocable: true
---

# Lune Development Rules

## 1. Iterative Verify-Fix Loop
After every code change, verify it works — do not assume. The cycle is:
```
code → build → run → verify (curl/UI/logs) → find issues → fix → repeat
```
Stop only when ALL verification passes. If a page shows errors, fix the root cause — never silence or skip.

## 2. Quantified Validation
Verification must produce measurable results, not just "compiles OK":
- API: `curl` and check response count, status code, content
- UI: screenshot or inspect console for errors
- Performance: check response time, memory delta, render frames
- Data: verify DB row count, API total field

## 3. Deep Root-Cause Analysis
When a bug surfaces, trace to the source. Do not patch symptoms. Check:
- Backend logs (`docker logs lune-backend-dev`)
- DB state (`docker exec lune-mysql-dev mysql ...`)
- Network tab (browser console)
- Frontend console errors
Fix at the origin, not the surface.

## 4. Optimal Solutions
If the user's approach is suboptimal, use the better approach. Do not cut corners. Do not avoid hard problems. When in doubt, choose the option that benefits the project long-term, not the option that costs fewer tokens or lines.

## 5. Memory & Performance Awareness
- No `Math.random()` inside `computed()` — use `ref` with explicit triggers
- All `setInterval`/`addEventListener` cleaned up in `onUnmounted`
- Atomic SQL for counters (not read-modify-write)
- DB queries filtered at SQL level, not in Java memory
- Avoid silent catch blocks — at minimum log the error

## 6. Network Resilience
When accessing external resources (npm, Maven, apt, APIs):
- Retry up to 3 times with 5-second delays
- If all fail: report the URL and ask user to check network/proxy
- NEVER fall back to mirrors or degraded alternatives

## 7. Dependencies First
Before implementing non-trivial functionality, check if a mature library exists. Only build from scratch when no suitable dependency exists. Prefer: actively maintained, large community, minimal transitive dependencies.

## 8. Project-Specific Conventions
- Backend: constructor injection, `var` for locals, `LambdaQueryWrapper`, `@TableLogic` on entities
- Frontend: Pinia stores, `usePageBackground(key)` for backgrounds, scoped CSS
- DB: `docker exec lune-mysql-dev mysql -u root -plune-dev-2024 lune` for queries
- Config: `.env.local` for local dev, `.env.template` for prod template — never commit real secrets
- Build: `make dev` or `docker compose -f docker-compose.dev.yml --env-file .env.local up -d --build`
- Logs: `docker logs lune-backend-dev` for backend, `make dev-logs` for all
- Test data: `/tmp/lune-seed.sql` — load via `docker cp` + `docker exec mysql < file`
