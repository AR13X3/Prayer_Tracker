-- 0004_rls.sql
-- Row Level Security. THIS is the security boundary of the whole app. See tests/rls_verification.sql.
-- Every policy wraps auth.uid() in a scalar subquery for once-per-query evaluation.

alter table public.profiles    enable row level security;
alter table public.prayer_logs enable row level security;
alter table public.shares      enable row level security;
alter table public.invites     enable row level security;

-- ── profiles ──────────────────────────────────────────────────────────────
-- Readable by self and by anyone you've shared with (they need your display_name).
-- NOTE: this also exposes latitude/longitude/city_label to viewers. If precise home
-- coordinates leaking to friends is a concern, split location into a self-only table.
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles
  for select using (public.can_view(id));

drop policy if exists profiles_insert on public.profiles;
create policy profiles_insert on public.profiles
  for insert with check (id = (select auth.uid()));

drop policy if exists profiles_update on public.profiles;
create policy profiles_update on public.profiles
  for update using (id = (select auth.uid()))
             with check (id = (select auth.uid()));

-- ── prayer_logs ───────────────────────────────────────────────────────────
-- Read if shared with you; write only your own.
drop policy if exists prayer_logs_select on public.prayer_logs;
create policy prayer_logs_select on public.prayer_logs
  for select using (public.can_view(user_id));

drop policy if exists prayer_logs_insert on public.prayer_logs;
create policy prayer_logs_insert on public.prayer_logs
  for insert with check (user_id = (select auth.uid()));

drop policy if exists prayer_logs_update on public.prayer_logs;
create policy prayer_logs_update on public.prayer_logs
  for update using (user_id = (select auth.uid()))
             with check (user_id = (select auth.uid()));

drop policy if exists prayer_logs_delete on public.prayer_logs;
create policy prayer_logs_delete on public.prayer_logs
  for delete using (user_id = (select auth.uid()));

-- ── shares ────────────────────────────────────────────────────────────────
-- See rows you are party to; either party may sever the link.
-- Deliberately NO insert policy: shares are created only by redeem_invite() (SECURITY DEFINER).
drop policy if exists shares_select on public.shares;
create policy shares_select on public.shares
  for select using (owner_id  = (select auth.uid())
                 or viewer_id = (select auth.uid()));

drop policy if exists shares_delete on public.shares;
create policy shares_delete on public.shares
  for delete using (owner_id  = (select auth.uid())
                 or viewer_id = (select auth.uid()));

-- ── invites ───────────────────────────────────────────────────────────────
-- Only the creator can see or manage their invites. A redeemer never SELECTs the
-- invite row — redemption goes through redeem_invite() (SECURITY DEFINER).
drop policy if exists invites_select on public.invites;
create policy invites_select on public.invites
  for select using (owner_id = (select auth.uid()));

drop policy if exists invites_insert on public.invites;
create policy invites_insert on public.invites
  for insert with check (owner_id = (select auth.uid()));

drop policy if exists invites_update on public.invites;
create policy invites_update on public.invites
  for update using (owner_id = (select auth.uid()))
             with check (owner_id = (select auth.uid()));
