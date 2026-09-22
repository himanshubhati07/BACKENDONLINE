COMMIT_MESSAGE: Add edit (PUT) endpoint for API keys, completing edit/delete coverage across all entities

## Summary
The Task entity already had full edit (`PUT /tasks/{id}`) and delete (`DELETE /tasks/{id}`,
`DELETE /tasks`) support from an earlier session. The API key entity only had create, list,
and revoke (delete) — it was missing an edit endpoint. This session adds `PUT /api-keys/{id}`
so every entity in the project now supports both editing and deletion. Also aligned the
server port and test defaults to the port assigned for this run (22315).

## Features Added
- `PUT /api/v1/api-keys/{key_id}` — edit an existing API key's `name` and/or `is_active`
  status. Returns 404 if the key doesn't exist. Admin-key protected, same as the other
  `/api-keys` routes.
- Verified/confirmed pre-existing Delete/Edit operations for Tasks: `PUT /tasks/{task_id}`
  (edit), `DELETE /tasks/{task_id}` (delete one), `DELETE /tasks` (delete all).
- Verified/confirmed pre-existing Delete operation for API keys: `DELETE /api-keys/{key_id}`
  (revoke/deactivate).

## Files Modified
- app/schemas.py — added `ApiKeyUpdate` schema (optional `name`, `is_active`).
- app/routers/api_keys.py — added `update_api_key` handler for `PUT /api-keys/{key_id}`.
- .env_b053751c0f39d3e4 — `PORT` updated to `22315` (this run's assigned port).
- start.sh — default `PORT` fallback updated to `22315`.
- tests/run_all.sh, tests/test_api_keys.sh, tests/test_tasks.sh, tests/README.md —
  `BASE_URL` default updated to `http://localhost:22315`.
- tests/test_api_keys.sh — added a new step testing `PUT /api-keys/{id}` (rename a key,
  assert 200 and updated name in the response).

## Files Added
(none — extended existing files/tests)

## Secrets Extracted
(none new — ADMIN_API_KEY was already present in .env_b053751c0f39d3e4 from a prior run)

## DB URLs Resolved
- postgresql+asyncpg://myuser:mypassword@localhost:5432/gen_53cbec77f35a -> unchanged (already working)
- postgresql+asyncpg://myuser:mypassword@localhost:5432/gen_e06e18eb23c5 -> unchanged (already working)

## Test Results Summary
2 PASSED, 0 FAILED, 0 SKIPPED
- tests/test_api_keys.sh: PASSED (create, list, edit/PUT rename, revoke API key; 401 without admin header)
- tests/test_tasks.sh: PASSED (create, get, list, edit/PUT, delete-all; 401 without API key)
