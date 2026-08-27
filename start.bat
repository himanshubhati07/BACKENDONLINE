@echo off
set SERVER_PORT=25026
call gradlew.bat bootJar -q
if errorlevel 1 exit /b 1
java -jar build\libs\app-0.1.0.jar --server.port=25026
