@echo off
set SERVER_PORT=22916

echo Building application...
call mvn package -DskipTests -q

echo Starting application on port %SERVER_PORT%...
java -jar target\app.jar --server.port=%SERVER_PORT%
