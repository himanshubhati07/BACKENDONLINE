#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT=24037
mvn package -DskipTests -q
java -jar target/app-0.1.0.jar --server.port=24037
