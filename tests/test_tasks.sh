#!/bin/bash
# Tests for /api/v1/tasks endpoints (X-API-Key protected), including bulk delete-all
set -u
BASE_URL=${BASE_URL:-http://localhost:22315}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ -f "$REPO_DIR/.env_b053751c0f39d3e4" ]; then
  export $(grep -E '^ADMIN_API_KEY=' "$REPO_DIR/.env_b053751c0f39d3e4" | xargs)
fi

if [ -z "${ADMIN_API_KEY:-}" ]; then
  echo "FAILED:ADMIN_API_KEY not set"
  exit 1
fi

CREATED_KEY_IDS=()

cleanup() {
  for kid in "${CREATED_KEY_IDS[@]:-}"; do
    [ -n "$kid" ] && curl -s -o /dev/null -X DELETE "$BASE_URL/api/v1/api-keys/$kid" -H "X-Admin-Key: $ADMIN_API_KEY"
  done
}
trap cleanup EXIT

# Create an API key to use for task CRUD
CREATE_KEY_RESP=$(curl -s -X POST "$BASE_URL/api/v1/api-keys/" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_API_KEY" \
  -d '{"name":"tasks-test-key"}')
API_KEY_ID=$(echo "$CREATE_KEY_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
API_KEY=$(echo "$CREATE_KEY_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('api_key',''))")
CREATED_KEY_IDS+=("$API_KEY_ID")

if [ -z "$API_KEY" ]; then
  echo "FAILED:could not create api key for tasks test: $CREATE_KEY_RESP"
  exit 1
fi

# 0. No API key -> 401
NO_KEY_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/tasks")
if [ "$NO_KEY_CODE" != "401" ]; then
  echo "FAILED:expected 401 without X-API-Key, got $NO_KEY_CODE"
  exit 1
fi

# 1. POST - create task
CREATE_RESP=$(curl -s -X POST "$BASE_URL/api/v1/tasks" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"title":"Test Task","description":"desc","status":"TODO","priority":"LOW"}')
TASK_ID=$(echo "$CREATE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
if [ -z "$TASK_ID" ]; then
  echo "FAILED:could not create task: $CREATE_RESP"
  exit 1
fi

# 2. GET single task
GET_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/tasks/$TASK_ID" -H "X-API-Key: $API_KEY")
if [ "$GET_CODE" != "200" ]; then
  echo "FAILED:get task expected 200, got $GET_CODE"
  exit 1
fi

# 3. GET list
LIST_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/tasks?limit=20&offset=0" -H "X-API-Key: $API_KEY")
if [ "$LIST_CODE" != "200" ]; then
  echo "FAILED:list tasks expected 200, got $LIST_CODE"
  exit 1
fi

# 4. PUT - update task
UPDATE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/api/v1/tasks/$TASK_ID" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"status":"IN_PROGRESS"}')
if [ "$UPDATE_CODE" != "200" ]; then
  echo "FAILED:update task expected 200, got $UPDATE_CODE"
  exit 1
fi

# 5. Create a second task, then DELETE ALL (remove all api feature)
curl -s -X POST "$BASE_URL/api/v1/tasks" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{"title":"Second Task","status":"TODO","priority":"MEDIUM"}' > /dev/null

DELETE_ALL_RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/v1/tasks" -H "X-API-Key: $API_KEY")
DELETE_ALL_CODE=$(echo "$DELETE_ALL_RESP" | tail -n1)
DELETE_ALL_BODY=$(echo "$DELETE_ALL_RESP" | sed '$d')
if [ "$DELETE_ALL_CODE" != "200" ]; then
  echo "FAILED:delete all tasks expected 200, got $DELETE_ALL_CODE"
  exit 1
fi
DELETED_COUNT=$(echo "$DELETE_ALL_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('count',-1))")
if [ "$DELETED_COUNT" -lt 2 ]; then
  echo "FAILED:expected at least 2 tasks deleted, got $DELETED_COUNT"
  exit 1
fi

# 6. Verify list is now empty
AFTER_LIST=$(curl -s "$BASE_URL/api/v1/tasks?limit=20&offset=0" -H "X-API-Key: $API_KEY")
AFTER_TOTAL=$(echo "$AFTER_LIST" | python3 -c "import sys,json; print(json.load(sys.stdin).get('total',-1))")
if [ "$AFTER_TOTAL" != "0" ]; then
  echo "FAILED:expected 0 tasks remaining after delete-all, got $AFTER_TOTAL"
  exit 1
fi

# 7. GET the deleted task -> 404
GET_404_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/v1/tasks/$TASK_ID" -H "X-API-Key: $API_KEY")
if [ "$GET_404_CODE" != "404" ]; then
  echo "FAILED:expected 404 for deleted task, got $GET_404_CODE"
  exit 1
fi

echo "PASSED"
exit 0
