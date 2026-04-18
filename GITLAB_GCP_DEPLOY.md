# GitLab + Google Cloud Deploy

## 1. One-Time Setup On VM
1. Install Docker and Docker Compose.
2. Create app folder: `/opt/edupedu-backend`.
3. Open firewall for API port `8080`.
4. Add SSH public key for GitLab deploy access.

## 2. GitLab CI/CD Variables
Set all variables as Protected. Set secrets as Masked. Use File type for key/env files.

| Variable | Required | Type | Description |
| --- | --- | --- | --- |
| `GCP_VM_HOST` | Yes | Variable | VM public IP or hostname |
| `GCP_VM_USER` | Yes | Variable | SSH user on VM |
| `GCP_VM_SSH_KEY` | Yes | File or Variable | Private SSH key used by pipeline |
| `PROD_ENV_FILE` | Yes | File or Variable | Full content of `.env.prod` |
| `JWT_PRIVATE_KEY_FILE` | Recommended | File or Variable | JWT private key PEM |
| `JWT_PUBLIC_KEY_FILE` | Recommended | File or Variable | JWT public key PEM |
| `GCP_VM_PORT` | No | Variable | SSH port (default `22`) |
| `GCP_APP_DIR` | No | Variable | Deploy path (default `/opt/edupedu-backend`) |

## 3. Prepare Production Env File
1. Copy `.env.prod.example`.
2. Fill all real values for DB, mail, API URL and CORS.
3. Keep `SPRING_PROFILES_ACTIVE=prod`.
4. Keep `COMPOSE_ENV_FILE=.env.prod`.
5. Put this final content into `PROD_ENV_FILE` variable.

## 4. Deploy From GitLab
1. Push branch `backend`.
2. Open Pipelines.
3. Run manual job `deploy_production`.
4. Check container status from job logs (`docker compose ps`).

## 5. Important Security Notes
1. Do not commit real `.env` files.
2. Do not commit production JWT keys.
3. Rotate any old DB/MAIL/JWT secrets that were previously exposed.
