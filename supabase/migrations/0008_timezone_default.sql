-- 0008_timezone_default.sql
-- profiles.timezone defaulted to 'Australia/Sydney', which made every new signup look
-- Sydney-local until they opened Settings. The app now treats the device clock as the
-- source of truth and writes this column back on launch when it drifts, so the server
-- default only needs to be neutral rather than a guess.
--
-- Existing rows are left alone on purpose: the server can't know a user's real zone, and
-- the client corrects its own row the next time it loads Today.

alter table public.profiles alter column timezone set default 'UTC';
