# API Check Tool

`tools/check_api.py` is a small smoke-test script for the deployed backend.

It checks:
- `GET /v3/api-docs`
- `OPTIONS /api/v1/auth/login` for CORS/preflight
- `POST /api/v1/auth/login`
- all OpenAPI endpoints that can be called safely without custom payload setup

It prints:
- what is reachable
- what is blocked by auth
- what is missing
- what was skipped because it needs a real request body or real IDs

## Quick run

```bash
python tools/check_api.py --base-url http://136.116.64.6 --output api-report.json
```

## With another account

```bash
python tools/check_api.py --base-url http://136.116.64.6 --email your@email.com --password yourpass
```

## Useful flags

- `--origin http://localhost:57323`
- `--sample-id 1`
- `--timeout 30`
- `--output api-report.json`

## What to expect

If login returns `403`, the script will still continue and show which endpoints are failing because auth is blocked.
