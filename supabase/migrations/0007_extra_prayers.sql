-- 0007_extra_prayers.sql
-- Adds Tahajjud (a voluntary night prayer) as a loggable prayer that is EXCLUDED from
-- streaks, and makes prayer_streaks compute only the five fard prayers.
--
-- Jummah is deliberately NOT added: it is the Friday replacement for Dhuhr (same time slot,
-- you never pray both), so it is stored as 'dhuhr' and only relabeled "Jummah" in the app on
-- Fridays. That keeps the Dhuhr streak flowing through Fridays automatically.
--
-- Run this in the Supabase SQL editor. (Postgres 15+ allows ADD VALUE inside a transaction
-- as long as the new value isn't USED in the same transaction — it isn't here.)

alter type public.prayer_name add value if not exists 'tahajjud';

-- Recreate the streak function to iterate an explicit fard list instead of the whole enum,
-- so Tahajjud (and any future non-fard prayer) never produces a streak and never counts.
create or replace function public.prayer_streaks(target uuid, today date)
returns table (prayer public.prayer_name, current_streak int, best_streak int)
language sql
stable
security invoker
set search_path = public
as $$
  with fard as (
    select unnest(array['fajr','dhuhr','asr','maghrib','isha']::public.prayer_name[]) as prayer
  ),
  kept as (
    select l.prayer, l.prayer_date
    from public.prayer_logs l
    join fard f on f.prayer = l.prayer            -- fard only; ignores tahajjud
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
         coalesce(max(r.len) filter (where r.ended_on >= today - 1), 0)::int,
         coalesce(max(r.len), 0)::int
  from fard e
  left join runs r on r.prayer = e.prayer
  group by e.prayer;
$$;
