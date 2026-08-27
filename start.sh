#!/bin/bash
set -e

SERVER_PORT=27171

# JwtUtil refuses to start without a JWT_SECRET (no insecure default is baked into
# the app on purpose). If the caller/environment hasn't supplied one, generate a
# random one for this run so the app is deployable out-of-the-box without ever
# hardcoding a secret in source control.
if [ -z "${JWT_SECRET}" ]; then
  export JWT_SECRET=$(openssl rand -hex 32 2>/dev/null || head -c 48 /dev/urandom | base64)
fi

echo "Building application..."
mvn package -DskipTests -q

echo "Starting application on port ${SERVER_PORT}..."
java -jar target/app.jar --server.port=${SERVER_PORT}
