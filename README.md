# App Backend

Production-oriented Java 17 / Spring Boot 3 REST API for JWT authentication and MongoDB-backed user management.

## Architecture

- `controller`: versioned REST endpoints and OpenAPI annotations
- `service`: authentication, password hashing, user CRUD, filtering, sorting, and offset pagination
- `repository`: Spring Data MongoDB persistence
- `document`: internal MongoDB documents; APIs return DTOs only
- `security`: HS256 token generation/validation and bearer-token filter
- `exception`: consistent safe JSON error responses
- `config`: stateless Spring Security, permissive CORS required by deployment, OpenAPI, and idempotent seed data

Passwords are BCrypt hashes. Emails have a unique MongoDB index. API responses never expose password hashes. JWT access tokens expire after 30 minutes and refresh tokens are intentionally unsupported.

> Deployment configuration requires all endpoints to be permitted by the Spring Security filter chain for development convenience. JWT generation and validation components are included, and Swagger exposes the bearer scheme, but endpoint authorization is not enforced in this configured profile.

## Prerequisites

- Java 17+
- MongoDB reachable at `mongodb://localhost:27017/gen_02a8b31a019a`

## Run

```bash
./gradlew clean build
./start.sh
```

Windows:

```bat
gradlew.bat clean build
start.bat
```

Service: `http://localhost:25026`  
Swagger UI: `http://localhost:25026/docs`  
OpenAPI JSON: `http://localhost:25026/api-docs`

Docker artifacts are intentionally not generated because Docker and Compose are disabled for this deployment target.

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `JWT_SECRET` | HS256 signing secret (use 32+ random bytes) | safe development fallback |
| `JWT_EXPIRATION` | Access-token lifetime in milliseconds | `1800000` |

The verified MongoDB URI and server port are fixed in `application.properties` for this deployment. See `.env.example` for deployment values. Never use the fallback JWT secret in production.

## API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register with validated name, email, and strong password |
| POST | `/api/v1/auth/login` | Validate credentials and return an HS256 JWT |
| POST | `/api/v1/users` | Create a user |
| GET | `/api/v1/users` | List; `offset`, `limit`, `sortBy`, `direction`, `filter` |
| GET | `/api/v1/users/{id}` | Retrieve by id |
| PUT | `/api/v1/users/{id}` | Update name and email |
| DELETE | `/api/v1/users/{id}` | Delete by id |
| GET | `/actuator/health` | Health status |

Default list pagination is offset `0`, limit `20`; maximum limit is `100`. Sort fields: `name`, `email`, `createdAt`, `updatedAt`. Filtering performs a case-insensitive literal match across name and email.

## Response and error behavior

Successful endpoints return typed JSON DTOs and standard status codes (`200`, `201`, `204`). Errors use a safe structure containing timestamp, status, error, message, request path, and field validation errors. Duplicate emails return `409`; invalid input returns `400`; invalid credentials return `401`; missing users return `404`.

## Tests

```bash
./gradlew test
./gradlew clean build
```

Tests cover JWT behavior, authentication, BCrypt hashing, validation, MongoDB persistence, CRUD, pagination, duplicate handling, and safe error responses. Final live curl results are in `api_tests/test_results.md` and `api_test_report.xlsx`.
