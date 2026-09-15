#!/usr/bin/env bash
set -e
SERVER_PORT="${SERVER_PORT:-24066}"
set -a
[ -f .env_bc9da269-4013-40e1-a958-1c655b606880 ] && . ./.env_bc9da269-4013-40e1-a958-1c655b606880
set +a
./mvnw package -DskipTests -q
java -jar target/app-0.1.0.jar --server.port=$SERVER_PORT
