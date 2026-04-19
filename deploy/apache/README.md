# Apache Reverse Proxy

Use [edupedu-backend.conf](/C:/Users/Adilkan/Documents/AA_Own_projects/edupedu-backend/deploy/apache/edupedu-backend.conf) on the VM that serves `http://136.116.64.6`.

## What It Does

- proxies `/api/v1/*` to Spring Boot on `127.0.0.1:8080`
- proxies Swagger routes (`/swagger-ui/*`, `/v3/api-docs`)
- proxies `/ws-chat` including WebSocket upgrade requests
- adds browser CORS headers for:
  - `http://localhost:*`
  - `http://127.0.0.1:*`
  - `http://136.116.64.6`
  - `https://136.116.64.6`
- handles `OPTIONS` preflight at Apache level

## Required Apache Modules

```bash
sudo a2enmod proxy proxy_http proxy_wstunnel headers rewrite
```

## Install

```bash
sudo cp deploy/apache/edupedu-backend.conf /etc/apache2/sites-available/edupedu-backend.conf
sudo a2dissite 000-default.conf
sudo a2ensite edupedu-backend.conf
sudo apache2ctl configtest
sudo systemctl reload apache2
```

## Backend Expectations

- Spring backend stays on `127.0.0.1:8080`
- `.env.prod` should keep:
  - `APP_URL=http://136.116.64.6`
  - `SERVER_PORT=8080`
- recommended CORS env for Spring:
  - `CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:*,http://127.0.0.1:*,http://136.116.64.6,https://136.116.64.6`

## Quick Checks

```bash
curl -i http://136.116.64.6/v3/api-docs
curl -i -X OPTIONS http://136.116.64.6/api/v1/auth/login \
  -H "Origin: http://localhost:57323" \
  -H "Access-Control-Request-Method: POST"
```
