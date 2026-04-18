# Production-Oriented Backend Setup

This repository is now split into two runtime modes:

- `local`: developer-friendly mode with local JWT key paths and demo seeding
- `prod`: deployment mode that expects real environment values

## What Still Needs Real Config From HR / DevOps

To deploy this backend on GCP or through GitLab CI/CD, you still need real values for:

- `DB_URL`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_URL`
- `CORS_ALLOWED_ORIGIN_PATTERNS`
- `JWT_PRIVATE_KEY_PATH`
- `JWT_PUBLIC_KEY_PATH`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

Without those values, the backend can be prepared and versioned, but not deployed correctly.

## Local Docker Run

1. Copy `.env.example` to `.env`
2. Start the stack:

```powershell
docker compose up --build
```

The local stack runs with:

- `SPRING_PROFILES_ACTIVE=local`
- seeded demo users
- server CORS (`136.116.64.6`)
- local JWT key paths

## Production Environment

1. Copy `.env.prod.example` to a real production env file outside git
2. Replace placeholder values with real GCP / GitLab values
3. Put real JWT key files into `./secrets` on the server (mapped to `/run/secrets`)
4. Run the backend with `SPRING_PROFILES_ACTIVE=prod`
5. Use `docker compose --env-file .env.prod up -d --build`

## Notes

- Do not commit real `.env` files
- Do not use the local JWT keys in production
- If this repository previously exposed secrets, rotate them before deployment
