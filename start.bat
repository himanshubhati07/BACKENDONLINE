@echo off
if "%SERVER_PORT%"=="" set SERVER_PORT=27587

echo Building application...
call mvn package -DskipTests -q

echo Starting application on port %SERVER_PORT%...
java -jar target\app.jar --server.port=%SERVER_PORT%
