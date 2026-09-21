# Task Management API

A small, focused FastAPI backend for managing tasks, secured with API Key
authentication, backed by PostgreSQL, and emitting task lifecycle events to
Apache Kafka.

## Features

- CRUD endpoints for `Task` (title, description, status, priority).
- API Key authentication via `X-API-Key` header (looked up against hashed keys in Postgres).
- Admin-only endpoints (via `X-Admin-Key`) to issue/list/revoke API keys.
- Kafka events: `TASK_CREATED`, `TASK_UPDATED`, `TASK_DELETED` published to topic `task-events`.
- `/health` endpoint reporting Postgres and Kafka connectivity.
- Offset-based pagination on the task list endpoint.
- Automatic OpenAPI docs at `/docs` and `/redoc`.

## Project Structure

```
app/
  main.py            FastAPI app, CORS, health check, router wiring
  config.py          Env-driven configuration (API prefix, project metadata)
  database.py        Async SQLAlchemy engine/session, get_db dependency
  models.py          Task and ApiKey ORM models
  schemas.py         Pydantic v2 request/response schemas
  routers/
    tasks.py         Task CRUD endpoints
    api_keys.py       Admin API key management endpoints
  core/
    security.py      API key hashing, get_current_api_key, get_admin_key
  kafka/
    producer.py       aiokafka producer + publish_task_event + health check
tests/
  unit/               Pure-logic unit tests (no DB, no server)
  test_api_keys.py     Admin endpoint tests
  test_tasks.py         Task CRUD + auth + validation tests
  conftest.py          Real Postgres test-DB fixtures (NullPool + ASGI transport)
seed.py               Creates tables + inserts sample tasks and a seed API key
```

## Requirements

- Python 3.10+
- PostgreSQL 15.10
- Apache Kafka 3.7.1 (optional at runtime — failures are logged, not fatal)

## Environment Variables

Copy `.env.example` and fill in real values. This project's own runtime env
file is `.env_6fb25fff-dbb0-4770-a1c9-7ca2debd1b59` (already provided in this
environment with working defaults).

| Variable | Description |
|---|---|
| `DATABASE_URL` | Async Postgres URL, e.g. `postgresql+asyncpg://user:pass@host:5432/db` |
| `ADMIN_API_KEY` | Secret required in `X-Admin-Key` header to manage API keys |
| `API_PREFIX` | API route prefix, default `/api/v1` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers, e.g. `localhost:9092` |
| `KAFKA_TASK_EVENTS_TOPIC` | Kafka topic for task events, default `task-events` |
| `PORT` | Port the app listens on, default `25518` |

## Running Locally

```bash
chmod +x start.sh
PORT=25518 ./start.sh
```

This creates a virtualenv, installs dependencies, and starts uvicorn with
`--reload` on port 25518.

Seed sample data (also creates tables):

```bash
python3 seed.py
```

The seed script prints a one-time API key you can use to test protected
endpoints. Alternatively, use the admin endpoint to create your own key:

```bash
curl -X POST http://localhost:25518/api/v1/api-keys/ \
  -H "X-Admin-Key: <ADMIN_API_KEY from env file>" \
  -H "Content-Type: application/json" \
  -d '{"name": "my-key"}'
```

## Authentication

- Business endpoints (`/api/v1/tasks...`) require header `X-API-Key: <key>`.
- Admin endpoints (`/api/v1/api-keys/...`) require header `X-Admin-Key: <ADMIN_API_KEY>`.
- Missing/invalid keys return `401 Unauthorized`.

## API Endpoints

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/health` | Postgres + Kafka connectivity status | none |
| POST | `/api/v1/api-keys/` | Create an API key (returns raw key once) | Admin |
| GET | `/api/v1/api-keys/` | List API keys | Admin |
| DELETE | `/api/v1/api-keys/{id}` | Revoke an API key | Admin |
| POST | `/api/v1/tasks` | Create a task | API Key |
| GET | `/api/v1/tasks` | List tasks (paginated: `limit`, `offset`) | API Key |
| GET | `/api/v1/tasks/{id}` | Get a task by id | API Key |
| PUT | `/api/v1/tasks/{id}` | Update a task | API Key |
| DELETE | `/api/v1/tasks/{id}` | Delete a task | API Key |

Interactive docs: `http://localhost:25518/docs`

## Kafka Events

On create/update/delete of a Task, the API publishes a JSON message to the
`task-events` topic:

```json
{ "event_type": "TASK_CREATED", "data": { "id": "...", "title": "...", "status": "TODO", "priority": "MEDIUM" } }
```

If Kafka is unreachable, the request still succeeds (task data is the source
of truth in Postgres) but the failure is logged with `logger.error(...)`.

## Testing

Unit tests (pure logic, no DB/server):

```bash
pytest tests/unit/ -v --tb=short
```

Full test suite (spins up a real Postgres test database `<db>_test`):

```bash
pytest tests/ -v --tb=short
```

## Docker

```bash
docker-compose up --build
```

Starts Postgres 15.10, Zookeeper + Kafka 3.7.1, and the FastAPI app on port
`25518`.

## Makefile shortcuts

```bash
make install    # pip install -r requirements.txt
make run        # start the app via start.sh
make seed       # populate sample data
make test       # run pytest
make docker-up  # docker-compose up --build
make docker-down
```
