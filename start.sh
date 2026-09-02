#!/usr/bin/env bash
set -euo pipefail
SERVER_PORT=27706
mvn package -DskipTests -q
java -jar target/app-0.1.0.jar --server.port=27706
