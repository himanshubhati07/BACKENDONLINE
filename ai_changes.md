COMMIT_MESSAGE: Configure the application to use port 21699 by default

## Features Added
- Verified that the user DELETE API is absent from `UserController` and that `UserService` has no delete operation.
- Kept the separate API-key revocation endpoint unchanged because it is not a user delete API.
- Configured the application and local startup script to default to port 21699.

## Files Modified
- `src/main/resources/application.properties` — changed the default `server.port` value to 21699.
- `start.sh` — changed the default `SERVER_PORT` value to 21699.
- `ai_changes.md` — recorded the verified API state and build result.

## Files Added
- None.

## Secrets Moved
- None — API key settings were already externalized as `app.secret.api-key` and `app.secret.admin-api-key`.

## DB URLs Resolved
- `jdbc:mysql://localhost:3306/gen_7c11ddcd7a3b` -> `jdbc:mysql://localhost:3306/gen_7c11ddcd7a3b` (already working).
- The inactive Docker Compose URL was not used by the application properties configuration.

## Compilation Result
- PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.
