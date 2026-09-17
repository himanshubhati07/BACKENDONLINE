#!/usr/bin/env bash
set -e

SERVER_PORT="${SERVER_PORT:-20893}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

set -a
[ -f .env_e39b36ca-a36d-458b-b6f1-9ab10ceb6708 ] && . ./.env_e39b36ca-a36d-458b-b6f1-9ab10ceb6708
set +a

./mvnw -q package -DskipTests

java -jar target/app.jar --server.port="$SERVER_PORT"
