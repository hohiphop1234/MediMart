# MediMart Node API

The Android application calls this Express API. The API verifies each Supabase
access token and uses Supabase Postgres, Auth, and Storage; it does not connect
to MongoDB at runtime.

## Local configuration

1. Copy `.env.example` to `.env`.
2. Set `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` from Supabase Dashboard
   **Connect**.
3. Set `SUPABASE_SECRET_KEY` from the dashboard's server-side API keys.
   This key stays on the Node server only: never put it in Android, Git, or a
   chat message.
4. Run `npm install` and `npm run dev`.

## Email OTP setup

Enable **Authentication > Sign In / Providers > Email** in Supabase. In the
email template used for sign-in, include `{{ .Token }}` so users receive the
six-digit code that the Android screen requests. The default Supabase SMTP
service is intended only for testing; configure your own SMTP provider before
releasing the app.
