-- 0006_grants.sql
-- Table/function privileges. RLS restricts ROWS; grants restrict OPERATIONS. Both apply.
-- anon (unauthenticated) is intentionally granted nothing on these tables — every policy
-- requires auth.uid().

grant usage on schema public to authenticated, anon;

grant select, insert, update          on public.profiles    to authenticated;
grant select, insert, update, delete  on public.prayer_logs to authenticated;
grant select, delete                  on public.shares      to authenticated;  -- no insert/update
grant select, insert, update          on public.invites     to authenticated;  -- no delete (revoke instead)

-- Functions
grant execute on function public.can_view(uuid)                         to authenticated;
grant execute on function public.gen_invite_code()                      to authenticated;
grant execute on function public.create_invite(int, boolean, interval)  to authenticated;
grant execute on function public.prayer_streaks(uuid, date)             to authenticated;

revoke execute on function public.redeem_invite(text) from public, anon;
grant  execute on function public.redeem_invite(text) to authenticated;
