@echo off
if not defined SERVER_PORT set SERVER_PORT=24066
for /f "usebackq tokens=1,* delims==" %%A in (.env_bc9da269-4013-40e1-a958-1c655b606880) do set %%A=%%B
call mvnw.cmd package -DskipTests -q
java -jar target\app-0.1.0.jar --server.port=%SERVER_PORT%
