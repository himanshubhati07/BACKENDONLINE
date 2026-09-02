@echo off
set SERVER_PORT=27706
call mvn package -DskipTests -q
java -jar target\app-0.1.0.jar --server.port=27706
