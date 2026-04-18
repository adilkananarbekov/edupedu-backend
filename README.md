# EduPedu Backend

## Active Branch
- backend

## API Endpoint
- Base API URL: http://136.116.64.6:8080
- WebSocket: ws://136.116.64.6:8080/ws-chat
- Port: 8080

## Quick Deploy For Manager (GitLab + GCP)
1. Open GitLab pipeline on branch backend.
2. Add CI/CD variables listed in GITLAB_GCP_DEPLOY.md.
3. Create PROD_ENV_FILE from .env.prod.example.
4. Run manual job deploy_production.

## Local Run
1. Copy .env.example to .env.
2. Start stack:

```bash
docker compose up --build
```

## Production Run (Manual)
```bash
docker compose --env-file .env.prod up -d --build
```
