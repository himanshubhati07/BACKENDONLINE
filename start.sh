#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT=25026
./gradlew bootJar -q
exec java -jar build/libs/app-0.1.0.jar --server.port=25026
