#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT="${SERVER_PORT:-21699}"
mvn package -DskipTests -q
java -jar target/app-0.1.0.jar --server.port=$SERVER_PORT
