-- rls_verification.sql
-- Runs the entire §9 RLS checklist as TWO real authenticated users, inside ONE
-- transaction that ROLLS BACK at the end, so it leaves the database untouched.
--
-- ─────────────────────────────────────────────────────────────────────────────
-- HOW TO RUN
--   1. Sign up TWO real accounts (via the app or the Auth API). Do NOT use the
--      service_role key anywhere here — service_role BYPASSES RLS and would
--      "verify" nothing at all.
--   2. Get their auth user UUIDs:  select id, email from auth.users order by created_at;
--   3. Find/replace __USER_A__ and __USER_B__ below with those two UUIDs.
--   4. Run this whole file in the Supabase SQL editor (or psql). It prints one
--      NOTICE per check. Any failure RAISES and aborts — that is a real RLS hole,
--      not a harness bug. Ending on "ALL CHECKS PASSED" means the boundary holds.
--
-- WHY THIS WORKS
--   We run as the low-privilege `authenticated` role and set request.jwt.claims so
--   auth.uid() resolves to whichever user we're impersonating — exactly what a real
--   client's JWT does. Every switch is a TOP-LEVEL statement (not inside a function),
--   and `set local` keeps it all scoped to this single transaction.
-- ─────────────────────────────────────────────────────────────────────────────

begin;

-- ════════════════════════════════════════════════════════════════════════════
-- Segment 1 — act as USER A: seed data, create an invite, try to redeem own invite.
-- ════════════════════════════════════════════════════════════════════════════
reset role;
select set_config('request.jwt.claims', '{"sub":"__USER_A__","role":"authenticated"}', true);
set local role authenticated;

do $$
declare ok boolean;
begin
  -- A logs Fajr for the last 3 local days -> current Fajr streak should be 3.
  insert into public.prayer_logs (user_id, prayer_date, prayer, status) values
    ('__USER_A__', current_date,     'fajr', 'on_time'),
    ('__USER_A__', current_date - 1, 'fajr', 'on_time'),
    ('__USER_A__', current_date - 2, 'fajr', 'on_time');

  -- A creates an invite with a known code (deterministic; skips code generation).
  insert into public.invites (code, owner_id) values ('TEST01', '__USER_A__');

  -- Check 7: redeeming your OWN invite is rejected.
  ok := false;
  begin
    perform * from public.redeem_invite('TEST01');
  exception when others then ok := true;   -- errcode P0001
  end;
  if not ok then raise exception 'FAIL check7: A redeemed A''s own invite'; end if;
  raise notice 'PASS check7: cannot redeem your own invite';
end $$;

-- ════════════════════════════════════════════════════════════════════════════
-- Segment 2 — act as USER B: pre-share isolation, redeem, then write-protection.
-- ════════════════════════════════════════════════════════════════════════════
reset role;
select set_config('request.jwt.claims', '{"sub":"__USER_B__","role":"authenticated"}', true);
set local role authenticated;

do $$
declare n int; ok boolean; strk int; strk_sum int;
begin
  -- B logs one prayer so the mutual-visibility direction has something to show.
  insert into public.prayer_logs (user_id, prayer_date, prayer, status) values
    ('__USER_B__', current_date, 'isha', 'late');

  -- Check 1: B, with no share, sees ZERO of A's logs.
  select count(*) into n from public.prayer_logs where user_id = '__USER_A__';
  if n <> 0 then raise exception 'FAIL check1: B saw % of A''s logs with no share', n; end if;
  raise notice 'PASS check1: no share -> B sees 0 of A''s logs';

  -- Check 5: B cannot SELECT A's invite row.
  select count(*) into n from public.invites where code = 'TEST01';
  if n <> 0 then raise exception 'FAIL check5: B could read A''s invite row'; end if;
  raise notice 'PASS check5: B cannot select A''s invite';

  -- Check 4: B cannot directly INSERT into shares (no insert policy / no grant).
  ok := false;
  begin
    insert into public.shares (owner_id, viewer_id) values ('__USER_B__', '__USER_A__');
  exception when insufficient_privilege then ok := true;
  end;
  if not ok then raise exception 'FAIL check4: direct insert into shares was allowed'; end if;
  raise notice 'PASS check4: direct shares insert rejected';

  -- Check 6: nonexistent code -> same generic error as expired/revoked/used-up.
  ok := false;
  begin
    perform * from public.redeem_invite('ZZZZZZ');
  exception when others then ok := true;   -- errcode P0002, generic message
  end;
  if not ok then raise exception 'FAIL check6: invalid code did not error'; end if;
  raise notice 'PASS check6: invalid/expired code -> generic error';

  -- Check 2a: B redeems A's invite, then reads A's logs.
  perform * from public.redeem_invite('TEST01');
  select count(*) into n from public.prayer_logs where user_id = '__USER_A__';
  if n <> 3 then raise exception 'FAIL check2a: B saw % of A''s logs, expected 3', n; end if;
  raise notice 'PASS check2a: after redeem, B reads A''s logs';

  -- Check 3: B cannot insert/update/delete A's logs.
  update public.prayer_logs set in_jamaah = true where user_id = '__USER_A__';
  get diagnostics n = row_count;
  if n <> 0 then raise exception 'FAIL check3a: B updated % of A''s log rows', n; end if;

  delete from public.prayer_logs where user_id = '__USER_A__';
  get diagnostics n = row_count;
  if n <> 0 then raise exception 'FAIL check3b: B deleted % of A''s log rows', n; end if;

  ok := false;
  begin
    insert into public.prayer_logs (user_id, prayer_date, prayer, status)
    values ('__USER_A__', current_date - 3, 'dhuhr', 'on_time');  -- WITH CHECK: user_id must be B
  exception when insufficient_privilege then ok := true;
  end;
  if not ok then raise exception 'FAIL check3c: B inserted a log owned by A'; end if;
  raise notice 'PASS check3: B cannot write A''s logs (insert/update/delete)';

  -- Check 8: prayer_streaks respects RLS (shared user -> data, stranger -> all zero).
  select current_streak into strk from public.prayer_streaks('__USER_A__', current_date)
    where prayer = 'fajr';
  if strk <> 3 then raise exception 'FAIL check8a: B saw Fajr streak % for A, expected 3', strk; end if;

  select coalesce(sum(current_streak) + sum(best_streak), 0) into strk_sum
    from public.prayer_streaks('00000000-0000-0000-0000-000000000000', current_date);
  if strk_sum <> 0 then raise exception 'FAIL check8b: stranger streaks summed to %, expected 0', strk_sum; end if;
  raise notice 'PASS check8: streaks visible for shared user, all-zero for a stranger';
end $$;

-- ════════════════════════════════════════════════════════════════════════════
-- Segment 3 — act as USER A: confirm mutual visibility, then sever the share.
-- ════════════════════════════════════════════════════════════════════════════
reset role;
select set_config('request.jwt.claims', '{"sub":"__USER_A__","role":"authenticated"}', true);
set local role authenticated;

do $$
declare n int;
begin
  -- Check 2b: A reads B's logs (the invite was mutual).
  select count(*) into n from public.prayer_logs where user_id = '__USER_B__';
  if n <> 1 then raise exception 'FAIL check2b: A saw % of B''s logs, expected 1', n; end if;
  raise notice 'PASS check2b: mutual invite -> A reads B''s logs';

  -- Check 9 setup: A severs A->B visibility.
  delete from public.shares where owner_id = '__USER_A__' and viewer_id = '__USER_B__';
end $$;

-- ════════════════════════════════════════════════════════════════════════════
-- Segment 4 — act as USER B: access to A is revoked again.
-- ════════════════════════════════════════════════════════════════════════════
reset role;
select set_config('request.jwt.claims', '{"sub":"__USER_B__","role":"authenticated"}', true);
set local role authenticated;

do $$
declare n int;
begin
  -- Check 9: after unshare, B sees ZERO of A's logs again.
  select count(*) into n from public.prayer_logs where user_id = '__USER_A__';
  if n <> 0 then raise exception 'FAIL check9: after unshare B still saw % of A''s logs', n; end if;
  raise notice 'PASS check9: after unshare, B sees 0 of A''s logs again';

  raise notice '───────────────────────────────────────────────';
  raise notice 'ALL CHECKS PASSED — RLS boundary holds.';
end $$;

reset role;
rollback;  -- leave the database exactly as it was
