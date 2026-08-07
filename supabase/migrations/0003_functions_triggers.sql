-- 0003_functions_triggers.sql
-- Helper functions and triggers that must exist before RLS policies reference them.

-- Auto-create a profile row when a new auth user signs up.
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, display_name)
  values (new.id, coalesce(new.raw_user_meta_data->>'display_name', 'User'))
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Keep updated_at honest on row updates (columns exist in the schema; nothing set them).
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

drop trigger if exists prayer_logs_set_updated_at on public.prayer_logs;
create trigger prayer_logs_set_updated_at
  before update on public.prayer_logs
  for each row execute function public.set_updated_at();

-- Visibility helper. SECURITY DEFINER so it reads `shares` UNFILTERED by that table's
-- own RLS (avoids recursive policy evaluation), and STABLE so the planner caches it.
-- (select auth.uid()) wrapping: evaluate the uid once per query, not once per row.
create or replace function public.can_view(target uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select target = (select auth.uid())
      or exists (
        select 1 from public.shares
        where owner_id = target
          and viewer_id = (select auth.uid())
      );
$$;
