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

## Curator MVP APIs
- `POST /api/v1/admin/curators/assign` - assign a teacher as curator for a student group
- `DELETE /api/v1/admin/curators/teacher/{teacherId}` - unassign curator from any group
- `GET /api/v1/admin/curators/teacher/{teacherId}` - get curator assignment by teacher
- `GET /api/v1/admin/curators/student-group/{studentGroupId}` - get curator assignment by group
- `GET /api/v1/admin/curators` - list all curator assignments
- `GET /api/v1/teacher/curators/dashboard/me` - get dashboard for currently authenticated teacher curator
- `GET /api/v1/teacher/curators/students/me` - get full list of current curator's students with risk flag
- `GET /api/v1/student/curators/me` - get current student's curator assignment

Curator assignment response includes contact fields: `teacher_email`, `teacher_phone`.

Example assign payload:
```json
{
	"teacherId": 1,
	"studentGroupId": 2
}
```
