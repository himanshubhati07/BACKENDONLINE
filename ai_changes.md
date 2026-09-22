COMMIT_MESSAGE: Add bulk delete-all endpoint for tasks and fix env/DB configuration

## Features Added
- Added `DELETE /api/v1/tasks` endpoint that removes all tasks in a single call ("remove all" feature).
- Returns `{"detail": "All tasks deleted", "count": <n>}` with the number of tasks removed.
- Publishes a `TASK_ALL_DELETED` Kafka event (with the deleted count) to the `task-events` topic, following the same pattern as the existing single-task delete.
- Protected the same way as all other task endpoints: requires a valid `X-API-Key` header.

## Files Modified
- app/routers/tasks.py — added `delete_all_tasks` handler for `DELETE /tasks` (bulk delete), placed before the `/{task_id}` routes.
- app/config.py, app/core/security.py, app/database.py, app/kafka/producer.py, seed.py, tests/conftest.py — updated `load_dotenv()` calls to point at `.env_b053751c0f39d3e4` (the job-specific env file) instead of the previous session's env filename.
- docker-compose.yml — updated `DATABASE_URL` for the `db` service host to the resolved, reachable Postgres URL.

## Files Added
- tests/README.md — instructions for running the curl-based test suite.
- tests/test_api_keys.sh — tests create/list/revoke API keys, plus 401 without admin header.
- tests/test_tasks.sh — tests full task CRUD plus the new bulk delete-all endpoint, plus 401 without API key and 404 after deletion.
- tests/run_all.sh — runs all test_*.sh scripts and reports pass/fail summary.

## Secrets Extracted
- ADMIN_API_KEY -> generated a fresh secret and written to .env_b053751c0f39d3e4 (was a placeholder in .env.example).

## DB URLs Resolved
- postgresql+asyncpg://myuser:mypassword@db:5432/gen_53cbec77f35a -> postgresql+asyncpg://myuser:mypassword@localhost:5432/gen_e06e18eb23c5 (docker-compose.yml, unreachable host replaced)
- postgresql+asyncpg://myuser:mypassword@localhost:5432/gen_53cbec77f35a -> unchanged (already working; used as DATABASE_URL default and in .env_b053751c0f39d3e4)

## Test Results Summary
2 PASSED, 0 FAILED, 0 SKIPPED
- tests/test_api_keys.sh: PASSED (create, list, revoke API key; 401 without admin header)
- tests/test_tasks.sh: PASSED (create, get, list, update, delete-all, 404 after delete, 401 without API key)
