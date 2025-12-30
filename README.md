# Evenly – App Skeleton

Clean skeleton for a mobile-first app with Nuxt 3 PWA frontend and Helidon 4 backend. Ready for development.

## Quick start

```bash
docker compose up --build
```

- Webapp: http://localhost:3000
- API: http://localhost:8080
- Health: http://localhost:8080/api/health
- OpenAPI: http://localhost:8080/openapi

## Repository structure

- `evenly-webapp` – Nuxt 3 skeleton with Tailwind CSS
- `evenly-core` – Helidon 4 MicroProfile REST API skeleton
- `docker-compose.yml` – PostgreSQL + backend + frontend

## Frontend (evenly-webapp)

- Nuxt 3 SPA with Tailwind CSS
- PWA configured (manifest, service worker)
- Basic layout and routing
- API utility helper

Run locally:
```bash
cd evenly-webapp
npm install
npm run dev
```

## Backend (evenly-core)

- Helidon 4 MicroProfile REST API
- PostgreSQL with Flyway (ready for migrations)
- CORS configured
- Basic health endpoint at `/api/health`
- OpenAPI available at `/openapi`

Run locally:
```bash
cd evenly-core
mvn package
java -jar target/evenly-core-1.0.0.jar
```

## Environment variables

- Backend: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Frontend: `NUXT_PUBLIC_API_BASE` (defaults to http://localhost:8080)
- Docker Compose provides defaults

## Next steps

1. Add database migrations in `evenly-core/src/main/resources/db/migration/`
2. Create domain models in `evenly-core/src/main/java/io/evenly/core/domain/`
3. Implement services in `evenly-core/src/main/java/io/evenly/core/services/`
4. Create API resources in `evenly-core/src/main/java/io/evenly/core/api/`
5. Build frontend pages in `evenly-webapp/pages/`
6. Create components in `evenly-webapp/components/`
7. Add Pinia stores in `evenly-webapp/stores/`
