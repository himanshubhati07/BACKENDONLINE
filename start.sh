#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT=29353
mvn package -DskipTests -q
java -jar target/app-0.1.0.jar --server.port=29353
