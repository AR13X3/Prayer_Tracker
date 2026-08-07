-- 0005_rpc.sql
-- Remote-procedure functions the client calls via PostgREST /rest/v1/rpc/*.

-- ── Invite code generation ────────────────────────────────────────────────
-- 6 chars from an unambiguous alphabet: no I/O/0/1.
create or replace function public.gen_invite_code()
returns text
language sql
volatile
as $$
  select string_agg(
           substr('ABCDEFGHJKLMNPQRSTUVWXYZ23456789',
                  1 + floor(random() * 31)::int, 1),
           '')
  from generate_series(1, 6);
$$;

-- Create an invite owned by the caller, generating a unique code with retry on collision.
-- SECURITY INVOKER: the invites_insert policy (owner_id = auth.uid()) still applies.
create or replace function public.create_invite(
  p_max_uses int      default 1,
  p_mutual   boolean  default true,
  p_ttl      interval default interval '14 days'
)
returns public.invites
language plpgsql
security invoker
set search_path = public
as $$
declare
  me   uuid := (select auth.uid());
  code text;
  row  public.invites;
  attempt int := 0;
begin
  if me is null then
    raise exception 'not authenticated' using errcode = '28000';
  end if;

  loop
    attempt := attempt + 1;
    code := public.gen_invite_code();
    begin
      insert into public.invites (code, owner_id, expires_at, max_uses, mutual)
      values (code, me, now() + p_ttl, p_max_uses, p_mutual)
      returning * into row;
      return row;
    exception when unique_violation then
      if attempt >= 10 then
        raise exception 'could not allocate a unique invite code';
      end if;
      -- else loop and try another code
    end;
  end loop;
end;
$$;

-- ── Invite redemption ─────────────────────────────────────────────────────
-- The redeemer can neither read the invite row (not owner) nor insert into shares
-- (no insert policy). Redemption happens only here, atomically, as SECURITY DEFINER.
create or replace function public.redeem_invite(invite_code text)
returns table (owner_id uuid, owner_name text)
language plpgsql
security definer
set search_path = public
as $$
declare
  v  public.invites;
  me uuid := (select auth.uid());
begin
  if me is null then
    raise exception 'not authenticated' using errcode = '28000';
  end if;

  select * into v from public.invites
  where code = upper(trim(invite_code))
  for update;

  if not found
     or v.revoked
     or v.expires_at < now()
     or v.uses >= v.max_uses then
    -- One indistinguishable message: never leak whether a code exists.
    raise exception 'invalid or expired invite code' using errcode = 'P0002';
  end if;

  if v.owner_id = me then
    raise exception 'cannot redeem your own invite' using errcode = 'P0001';
  end if;

  insert into public.shares (owner_id, viewer_id) values (v.owner_id, me)
    on conflict do nothing;

  if v.mutual then
    insert into public.shares (owner_id, viewer_id) values (me, v.owner_id)
      on conflict do nothing;
  end if;

  update public.invites set uses = uses + 1 where code = v.code;

  return query select p.id, p.display_name from public.profiles p where p.id = v.owner_id;
end;
$$;

-- ── Per-prayer streaks ────────────────────────────────────────────────────
-- Classic gaps-and-islands. A day counts if the prayer was on_time or late; an ABSENT
-- log breaks the streak. SECURITY INVOKER so prayer_logs RLS filters to what the caller
-- may see — no extra authorization logic. `today` is a parameter, not current_date,
-- because the server is UTC and the streak is anchored to the user's LOCAL day.
create or replace function public.prayer_streaks(target uuid, today date)
returns table (prayer public.prayer_name, current_streak int, best_streak int)
language sql
stable
security invoker
set search_path = public
as $$
  with kept as (
    select l.prayer, l.prayer_date
    from public.prayer_logs l
    where l.user_id = target
      and l.status in ('on_time','late')
      and l.prayer_date <= today
  ),
  islands as (
    select prayer,
           prayer_date,
           prayer_date - (row_number() over (partition by prayer
                                             order by prayer_date))::int as grp
    from kept
  ),
  runs as (
    select prayer, count(*)::int as len, max(prayer_date) as ended_on
    from islands
    group by prayer, grp
  )
  select e.prayer,
         -- "current" if the run reached at least yesterday (today may not have happened)
         coalesce(max(r.len) filter (where r.ended_on >= today - 1), 0)::int,
         coalesce(max(r.len), 0)::int
  from unnest(enum_range(null::public.prayer_name)) as e(prayer)
  left join runs r on r.prayer = e.prayer
  group by e.prayer;
$$;
