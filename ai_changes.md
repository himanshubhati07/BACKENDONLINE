COMMIT_MESSAGE: Add API entry CRUD entity (register/list/get/update/delete APIs) under /api/v1/apis

## Features Added
- New `API` entity ("ApiEntry") representing a registered API definition (name, endpoint,
  HTTP method, description) with full CRUD, satisfying the "Add new API" requirement.
- `POST /api/v1/apis` — Add a new API (primary requested operation).
- `GET /api/v1/apis?page=&size=&search=&sort=` — list APIs with offset/limit pagination
  (Spring `Pageable`, default size=20) and optional case-insensitive name search, matching
  the pagination style already used by the existing `Product` module.
- `GET /api/v1/apis/{id}` — fetch a single API entry by id.
- `PUT /api/v1/apis/{id}` — update an API entry.
- `DELETE /api/v1/apis/{id}` — delete an API entry.
- All new endpoints require a valid JWT (`isAuthenticated()`), consistent with the
  project's JWT/HS256 auth (no new refresh-token flow or role/authority (RBAC) gates were
  added, per configuration targets — RBAC=false, Refresh=false).
- JWT access-token expiry updated from 30 minutes to the requested 60 minutes
  (`jwt.expiration-ms` default now `3600000`, still overridable via `JWT_EXPIRATION`).
- `/actuator/health` made publicly accessible (added to the Spring Security permit-all
  list) so infrastructure/liveness health checks work without a token — this endpoint was
  previously blocked by the default-deny `anyRequest().authenticated()` rule, which is a
  minimal fix required for automated health polling/deployment verification.
- Server port externalized to `27587` via `server.port=${SERVER_PORT:27587}` (previously
  hardcoded to `22916`); `start.sh`/`start.bat` updated to match.

## Files Modified
- `src/main/resources/application.properties` — `server.port` now env-overridable
  (default `27587`), `jwt.expiration-ms` default changed to `3600000` (60 min).
- `src/main/java/com/example/app/config/SecurityConfig.java` — added
  `/actuator/health`, `/actuator/health/**` to the public permit-all matcher list.
- `start.sh` — `SERVER_PORT` now defaults to `27587` (env-overridable).
- `start.bat` — `SERVER_PORT` now defaults to `27587` (env-overridable).
- `README.md` — documented the new `/api/v1/apis` endpoints and updated port references.

## Files Added
- `src/main/java/com/example/app/document/ApiEntry.java` — MongoDB document for the new
  API entity (collection `apis`).
- `src/main/java/com/example/app/dto/ApiEntryRequest.java` — validated create/update
  request DTO (name, endpoint, method, description).
- `src/main/java/com/example/app/dto/ApiEntryResponse.java` — response DTO.
- `src/main/java/com/example/app/repository/ApiEntryRepository.java` — Spring Data
  MongoDB repository (findAll paginated, findByNameContainingIgnoreCase).
- `src/main/java/com/example/app/service/ApiEntryService.java` — business logic for
  create/list/get/update/delete.
- `src/main/java/com/example/app/controller/ApiEntryController.java` — REST controller
  exposing `/api/v1/apis` endpoints.

## Secrets Moved
- None required — the project already externalizes all secrets (`jwt.secret` via
  `JWT_SECRET`, `spring.data.mongodb.uri` via `MONGODB_URI`, CORS origins via
  `CORS_ALLOWED_ORIGINS`). No hardcoded credentials were found in source code.

## DB URLs Resolved
- Not applicable — this project uses MongoDB (`spring.data.mongodb.uri`), not a JDBC
  datasource. No JDBC URLs exist in the project. A local MongoDB instance was already
  running and reachable on `localhost:27017`, matching the existing
  `spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/gen_e8ea1cb1f5b0}`
  configuration; no changes were required.

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` both succeeded with zero
errors. The packaged jar was started locally on port 27587 and the following were
verified end-to-end: register, login (JWT expiresIn=3600s confirming 60-min expiry),
create/list/get/update/delete on the new `/api/v1/apis` endpoints (including 401 for
missing token and 400 for invalid method and 404 for missing/deleted id), plus
regression checks on `/api/v1/users/me`, `/api/v1/products`, `/docs`, `/api-docs`, and
`/actuator/health`.
