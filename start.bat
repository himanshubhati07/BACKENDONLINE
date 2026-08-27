@echo off
set SERVER_PORT=27171

if "%JWT_SECRET%"=="" (
  for /f %%i in ('powershell -Command "[Convert]::ToBase64String((1..48 | %% {Get-Random -Minimum 0 -Maximum 256}))"') do set JWT_SECRET=%%i
)

echo Building application...
call mvn package -DskipTests -q

echo Starting application on port %SERVER_PORT%...
java -jar target\app.jar --server.port=%SERVER_PORT%
