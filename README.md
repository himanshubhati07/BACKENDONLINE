# User Management Service

Spring Boot 3 / Java 21 microservice providing API-key-protected user management backed by MySQL.

## Architecture

The service uses controller, service, repository, entity, DTO, configuration, security, and centralized exception-handling layers. User deletes are soft deletes. Flyway owns the initial database schema. The stateless API emits an `X-Request-Id` correlation header and exposes health probes.

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL running at `localhost:3306` with database `gen_d1e3b211c8e2`

The supplied environment is configured in `src/main/resources/application.properties` using the verified database connection and port `24037`. Set `API_KEY` in the environment to authenticate User APIs and set `ADMIN_API_KEY` before using API-key administration endpoints.

## Run locally

```bash
mvn compile -q
mvn test
chmod +x ./start.sh
bash ./start.sh
```

Health: `curl http://localhost:24037/actuator/health`

Swagger UI: `http://localhost:24037/docs`  
OpenAPI JSON: `http://localhost:24037/api-docs`

Docker and Docker Compose are provided. Set `ADMIN_API_KEY` and run `docker compose up --build`.

## API key bootstrap

API keys are database-backed and stored only as SHA-256 hashes. Obtain the administrator key from the `admin.api-key` application property, then create a key:

```bash
ADMIN_KEY="$ADMIN_API_KEY"
curl -X POST http://localhost:24037/api/v1/api-keys \
  -H "X-Admin-Key: $ADMIN_KEY" \
  -H 'Content-Type: application/json' \
  -d '{"name":"local-client"}'
```

Use the returned `apiKey` only in `X-API-Key`; it is returned once and never stored in plaintext.

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Public health check |
| POST | `/api/v1/api-keys` | Admin: issue an API key |
| GET | `/api/v1/api-keys` | Admin: list API-key metadata |
| DELETE | `/api/v1/api-keys/{id}` | Admin: revoke an API key |
| POST | `/api/v1/users` | Create user |
| GET | `/api/v1/users/{id}` | Get user |
| GET | `/api/v1/users?offset=0&limit=20&sort=createdAt,desc&status=ACTIVE&search=john` | List/filter/search users |
| PUT | `/api/v1/users/{id}` | Update user |
| DELETE | `/api/v1/users/{id}` | Soft delete user |

```bash
curl -H "X-API-Key: your-api-key" http://localhost:24037/api/v1/users
```

Validation rejects invalid user payloads with 400; duplicate email returns 409; a missing or invalid key returns 401.
