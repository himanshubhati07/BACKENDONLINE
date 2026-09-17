# Gym Management API

A Spring Boot 3.5 / Java 17 backend for managing a gym: users, members, trainers,
membership plans, subscriptions, attendance, workout plans and payments.

## Tech Stack

- Java 17, Spring Boot 3.5
- Maven 3.9.9 (wrapper included)
- PostgreSQL 15.10 (JPA / Hibernate 6.2.24)
- JWT (HS256) authentication with refresh tokens
- Apache Kafka 3.8.0 (async domain events)
- springdoc-openapi (Swagger UI)
- WebSocket (STOMP) infrastructure

## Run Locally

```bash
cp .env_e39b36ca-a36d-458b-b6f1-9ab10ceb6708 .env   # already present, holds DB_URL/DB_USER/DB_PASSWORD/JWT_SECRET
chmod +x start.sh
./start.sh
```

The app starts on **http://localhost:20893** (override with `SERVER_PORT`).

Swagger UI: http://localhost:20893/docs
OpenAPI JSON: http://localhost:20893/api-docs
Health check: http://localhost:20893/actuator/health

## Run with Docker Compose

```bash
docker compose up --build
```

This starts Postgres, Kafka and the app together.

## Environment Variables

Stored in `.env_e39b36ca-a36d-458b-b6f1-9ab10ceb6708` (never committed):

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL for PostgreSQL |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | HS256 signing secret |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers |

## Authentication

- `POST /api/v1/auth/register` — register a user (`role` optional: `ADMIN`, `TRAINER`, `MEMBER`; defaults to `MEMBER`)
- `POST /api/v1/auth/login` — login, returns access + refresh token
- `POST /api/v1/auth/refresh` — exchange refresh token for a new access token
- `POST /api/v1/auth/logout` — revoke a refresh token
- `GET /api/v1/auth/me` — current user profile
- `PUT /api/v1/auth/me` — update profile
- `PUT /api/v1/auth/me/password` — change password

Access tokens expire in 60 minutes, refresh tokens in 7 days. Admin-only endpoints
(create/update/delete for members, trainers, plans, subscriptions) require the
`ADMIN` role.

## API Endpoints (prefix `/api/v1`)

| Method | Endpoint | Description |
|---|---|---|
| POST | /auth/register | Register |
| POST | /auth/login | Login |
| POST | /auth/refresh | Refresh access token |
| POST | /auth/logout | Revoke refresh token |
| GET/PUT | /auth/me | Get/update profile |
| PUT | /auth/me/password | Change password |
| GET/POST | /members | List (search) / add member (admin) |
| GET/PUT/DELETE | /members/{id} | Get / update / delete member (admin) |
| PUT | /members/{id}/trainer/{trainerId} | Assign trainer (admin) |
| GET/POST | /trainers | List / add trainer (admin) |
| GET/PUT/DELETE | /trainers/{id} | Get / update / delete trainer (admin) |
| GET/POST | /plans | List / add plan (admin) |
| GET/PUT/DELETE | /plans/{id} | Get / update / delete plan (admin) |
| POST | /subscriptions | Assign subscription (admin) |
| PUT | /subscriptions/member/{id}/renew | Renew (admin) |
| PUT | /subscriptions/{id}/cancel | Cancel (admin) |
| GET | /subscriptions/member/{id}/current | Current subscription |
| GET | /subscriptions/member/{id}/history | Subscription history |
| POST | /attendance/member/{id}/check-in | Check-in |
| POST | /attendance/member/{id}/check-out | Check-out |
| GET | /attendance/today | Today's attendance |
| GET | /attendance/member/{id} | Attendance history |
| GET/POST | /workout-plans | List by member / create |
| GET/PUT/DELETE | /workout-plans/{id} | Get / update / delete |
| GET/POST | /payments | List/search / record payment |
| GET | /payments/{id} | Get payment |
| GET | /dashboard | Dashboard summary |
| GET | /actuator/health | Health check |

All endpoints except `/auth/**`, `/actuator/health`, `/docs/**` and `/api-docs/**`
require `Authorization: Bearer <token>`.

Pagination: all list endpoints accept `?page=&size=` (default size 20) and return
Spring's standard `Page` JSON shape.

## Kafka

Domain events (`USER_REGISTERED`, `MEMBER_REGISTERED`, `SUBSCRIPTION_ASSIGNED`,
`SUBSCRIPTION_RENEWED`, `SUBSCRIPTION_CANCELLED`, `MEMBER_CHECK_IN`,
`MEMBER_CHECK_OUT`, `PAYMENT_RECORDED`) are published to the `gym.events` topic
for downstream/external systems to consume. This service does not itself consume
this topic.

## Tests

Endpoint verification was performed with inline `curl` commands (see
`api_tests/test_results.md` and `api_test_report.xlsx`).

## Build

```bash
./mvnw clean package
```
