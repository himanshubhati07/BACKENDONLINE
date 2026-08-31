#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT=${SERVER_PORT:-22662}
./gradlew bootJar -q
exec java -jar build/libs/app-0.1.0.jar --server.port="$SERVER_PORT"
