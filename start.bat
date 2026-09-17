@echo off
if not defined SERVER_PORT set SERVER_PORT=20893

cd /d "%~dp0"

for /f "usebackq tokens=1,* delims==" %%A in (.env_e39b36ca-a36d-458b-b6f1-9ab10ceb6708) do set %%A=%%B

call mvnw.cmd -q package -DskipTests

java -jar target\app.jar --server.port=%SERVER_PORT%
