# Supabase setup for MediMart

`migrations/20260719000000_initial_medimart_schema.sql` creates the MediMart tables, Row Level Security policies, three Storage buckets, and safe RPC functions for checkout and reward redemption.

## Apply the migration

1. Open the Supabase project SQL Editor.
2. Paste the migration file in full and run it in a new query.
3. Inspect the created tables, RLS policies, functions, and buckets.
4. Configure Phone Auth and its SMS provider before migrating the Android login flow.

The project can later be linked to the Supabase CLI and this migration applied with `supabase db push`. CLI linking/deployment needs an authenticated Supabase CLI account and project-level administrative access; the Android publishable key is intentionally insufficient.

## Client configuration

Set these ignored values in `medimart-android/local.properties`:

```properties
supabase.url=https://your-project.supabase.co
supabase.publishableKey=your-publishable-key
```

Never put a `service_role` key in Android or commit it to Git. Direct client access is protected by RLS; checkout and reward redemption use database functions that derive the user from the Supabase JWT.
