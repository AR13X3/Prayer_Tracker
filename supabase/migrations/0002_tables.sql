-- 0002_tables.sql
-- Core tables. All in the public schema. auth.users is Supabase-managed; never modified.

create table if not exists public.profiles (
  id                 uuid primary key references auth.users(id) on delete cascade,
  display_name       text not null check (length(trim(display_name)) between 1 and 40),
  timezone           text not null default 'Australia/Sydney',
  latitude           double precision,
  longitude          double precision,
  city_label         text,                       -- human-readable, e.g. "Sydney, AU"
  calculation_method text not null default 'MuslimWorldLeague',
  madhab             text not null default 'shafi' check (madhab in ('shafi','hanafi')),
  created_at         timestamptz not null default now(),
  updated_at         timestamptz not null default now()
);

create table if not exists public.prayer_logs (
  id          bigint generated always as identity primary key,
  user_id     uuid not null references auth.users(id) on delete cascade,
  prayer_date date not null,                     -- the user's LOCAL date. Computed on-device.
  prayer      public.prayer_name not null,
  status      public.prayer_status not null,
  in_jamaah   boolean not null default false,    -- prayed in congregation
  logged_at   timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  unique (user_id, prayer_date, prayer)
);
create index if not exists prayer_logs_user_date_idx
  on public.prayer_logs (user_id, prayer_date desc);

-- viewer_id is allowed to read owner_id's logs. Directional.
-- A mutual friendship is two rows.
create table if not exists public.shares (
  id         bigint generated always as identity primary key,
  owner_id   uuid not null references auth.users(id) on delete cascade,
  viewer_id  uuid not null references auth.users(id) on delete cascade,
  created_at timestamptz not null default now(),
  unique (owner_id, viewer_id),
  check (owner_id <> viewer_id)
);
create index if not exists shares_viewer_idx on public.shares (viewer_id);

create table if not exists public.invites (
  code       text primary key,                   -- short, uppercase, e.g. 'K7QM4P'
  owner_id   uuid not null references auth.users(id) on delete cascade,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null default (now() + interval '14 days'),
  max_uses   int not null default 1 check (max_uses > 0),
  uses       int not null default 0,
  revoked    boolean not null default false,
  mutual     boolean not null default true       -- redeeming also grants owner->redeemer view
);
create index if not exists invites_owner_idx on public.invites (owner_id);
