# Prayer Tracker — Supabase (Phase 1)

The complete backend. There is **no server code** — Postgres + RLS + PostgREST *is* the
backend. Sharing is enforced by row-level security, not application logic, so the only way
data leaks is a misconfigured policy. That is why the verification step below is mandatory
before any Android work begins.

## Files

| File | What it does |
|---|---|
| `migrations/0001_enums.sql` | `prayer_name`, `prayer_status` enums |
| `migrations/0002_tables.sql` | `profiles`, `prayer_logs`, `shares`, `invites` + indexes |
| `migrations/0003_functions_triggers.sql` | signup→profile trigger, `updated_at` triggers, `can_view()` |
| `migrations/0004_rls.sql` | **the security boundary** — all RLS policies |
| `migrations/0005_rpc.sql` | `redeem_invite()`, `create_invite()`, `gen_invite_code()`, `prayer_streaks()` |
| `migrations/0006_grants.sql` | table/function privileges (anon gets nothing) |
| `tests/rls_verification.sql` | the §9 checklist as two real users, in a rolled-back transaction |

## Apply the schema

**Order matters** — run `0001` → `0006`. Two ways:

**A. Supabase SQL editor (simplest).** Open each file in order, paste, Run.

**B. Supabase CLI.**
```bash
supabase link --project-ref <your-ref>
supabase db push          # applies everything in migrations/ in filename order
```

The files are idempotent (guarded enums, `create ... if not exists`, `create or replace`,
`drop policy if exists`), so re-running is safe.

## Verify RLS — do NOT skip this

1. Create **two real accounts** by signing up (app or Auth API). **Never** use the
   `service_role` key here — it bypasses RLS and makes every check pass meaninglessly.
2. Find their UUIDs:
   ```sql
   select id, email from auth.users order by created_at;
   ```
3. In `tests/rls_verification.sql`, find/replace `__USER_A__` and `__USER_B__` with those
   two UUIDs.
4. Run the whole file. Expected tail:
   ```
   PASS check1 … PASS check9
   ALL CHECKS PASSED — RLS boundary holds.
   ```
   Any `FAIL …` aborts the run and marks a genuine hole. The script rolls itself back, so
   your data is untouched either way.

**Phase 1 is done only when that script ends on "ALL CHECKS PASSED."** Then move to the
Android skeleton (Phase 2).

## Client-facing API surface (for the Android app later)

- Tables via PostgREST: `select`/`insert`/`update` on `prayer_logs` and `profiles`,
  `select`/`delete` on `shares`, `select`/`insert`/`update` on `invites` — all
  auto-filtered by RLS to the current user's allowed rows.
- RPCs (`POST /rest/v1/rpc/<name>`):
  - `create_invite(p_max_uses, p_mutual, p_ttl)` → the new invite row (with its `code`).
  - `redeem_invite(invite_code)` → `{ owner_id, owner_name }`, or a generic error.
  - `prayer_streaks(target, today)` → per-prayer `current_streak` / `best_streak`.
    **Pass the device's LOCAL date as `today`** — the server is UTC (see plan §7.1).

## Notes carried over from the plan

- `profiles_select` exposes `latitude`/`longitude`/`city_label` to anyone you've shared
  with. If leaking precise home coordinates matters, split location into a self-only
  table before you ship. (Flagged in `0004_rls.sql`.)
- `prayer_logs.prayer_date` is a bare local `DATE`, computed on-device — never derived
  server-side from a timestamp.
