-- 0001_enums.sql
-- Prayer name and status enums. Idempotent: guarded so the file can be re-run.

do $$
begin
  if not exists (select 1 from pg_type where typname = 'prayer_name') then
    create type public.prayer_name as enum ('fajr','dhuhr','asr','maghrib','isha');
  end if;
end $$;

-- on_time : prayed within the prayer's own time window
-- late    : prayed the same day, after the window closed
-- qada    : made up on a later day
-- missed  : explicitly marked not prayed
do $$
begin
  if not exists (select 1 from pg_type where typname = 'prayer_status') then
    create type public.prayer_status as enum ('on_time','late','qada','missed');
  end if;
end $$;
