import { createClient } from '@supabase/supabase-js'

const url = import.meta.env.VITE_SUPABASE_URL
const anonKey = import.meta.env.VITE_SUPABASE_ANON_KEY

if (!url || !anonKey) {
  throw new Error(
    'VITE_SUPABASE_URL / VITE_SUPABASE_ANON_KEY are missing. Copy .env.example to .env.local and fill them in.',
  )
}

// Same Supabase project as the Android app: same accounts, same tables, same RLS policies.
// The client has no authorization logic of its own — every request is filtered server-side.
export const supabase = createClient(url, anonKey)
