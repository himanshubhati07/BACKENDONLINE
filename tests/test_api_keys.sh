#!/bin/bash
# Tests for /api/v1/api-keys endpoints (admin-key protected)
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

# 1. No admin header -> 401
NO_AUTH_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/api-keys/" \
  -H "Content-Type: application/json" -d '{"name":"no-auth-key"}')
if [ "$NO_AUTH_CODE" != "401" ]; then
  echo "FAILED:expected 401 without admin header, got $NO_AUTH_CODE"
  exit 1
fi

# 2. Create API key
CREATE_RESP=$(curl -s -X POST "$BASE_URL/api/v1/api-keys/" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_API_KEY" \
  -d '{"name":"test-suite-key"}')

KEY_ID=$(echo "$CREATE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
RAW_KEY=$(echo "$CREATE_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('api_key',''))")

if [ -z "$KEY_ID" ] || [ -z "$RAW_KEY" ]; then
  echo "FAILED:could not create api key: $CREATE_RESP"
  exit 1
fi

# 3. List API keys
LIST_CODE=$(curl -s -o /tmp/api_keys_list.json -w "%{http_code}" "$BASE_URL/api/v1/api-keys/" \
  -H "X-Admin-Key: $ADMIN_API_KEY")
if [ "$LIST_CODE" != "200" ]; then
  echo "FAILED:list api keys expected 200, got $LIST_CODE"
  exit 1
fi
FOUND=$(python3 -c "import json; data=json.load(open('/tmp/api_keys_list.json')); print(any(k['id']=='$KEY_ID' for k in data))")
if [ "$FOUND" != "True" ]; then
  echo "FAILED:created key not found in list"
  exit 1
fi

# 4. Edit (PUT) API key
UPDATE_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/api/v1/api-keys/$KEY_ID" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_API_KEY" \
  -d '{"name":"renamed-test-suite-key"}')
UPDATE_CODE=$(echo "$UPDATE_RESP" | tail -n1)
UPDATE_BODY=$(echo "$UPDATE_RESP" | sed '$d')
if [ "$UPDATE_CODE" != "200" ]; then
  echo "FAILED:update api key expected 200, got $UPDATE_CODE"
  exit 1
fi
UPDATED_NAME=$(echo "$UPDATE_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('name',''))")
if [ "$UPDATED_NAME" != "renamed-test-suite-key" ]; then
  echo "FAILED:expected updated name, got $UPDATED_NAME"
  exit 1
fi

# 5. Revoke API key
REVOKE_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/api/v1/api-keys/$KEY_ID" \
  -H "X-Admin-Key: $ADMIN_API_KEY")
if [ "$REVOKE_CODE" != "200" ]; then
  echo "FAILED:revoke api key expected 200, got $REVOKE_CODE"
  exit 1
fi

echo "PASSED"
exit 0
