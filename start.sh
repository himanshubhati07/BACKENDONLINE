#!/bin/bash
set -e

SERVER_PORT=22916

echo "Building application..."
mvn package -DskipTests -q

echo "Starting application on port ${SERVER_PORT}..."
java -jar target/app.jar --server.port=${SERVER_PORT}
