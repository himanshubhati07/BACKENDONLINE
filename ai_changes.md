COMMIT_MESSAGE: Secure user management configuration and add test coverage

## Features Added
- Preserved the existing MySQL-backed User create, get, update, delete, and offset-paginated list APIs at `/api/v1/users`.
- Added unit coverage for API-key rejection and acceptance and MockMvc HTTP coverage for User CRUD and validation responses.
- Added a 400 response mapping for invalid user pagination input.
- Added Dockerfile and Docker Compose deployment support on port 29353.

## Files Modified
- `src/main/resources/application.properties` — configured port 29353, retained the resolved MySQL JDBC URL, and reads the administrator API key from `ADMIN_API_KEY`.
- `src/main/java/com/example/app/controller/ApiKeyController.java` — injects the environment-backed API key property.
- `src/main/java/com/example/app/config/SecurityConfig.java` — removed CORS enablement to match target configuration.
- `src/main/java/com/example/app/exception/GlobalExceptionHandler.java` — maps illegal request parameters to HTTP 400.
- `start.sh` — packages and starts the service on port 29353.
- `README.md` — documents the environment API key, port, and Docker Compose startup.
- `.gitignore` — ignores build artifacts and local code-index artifacts.

## Files Added
- `Dockerfile` — container image definition for the Spring Boot jar.
- `docker-compose.yml` — local MySQL and application deployment configuration.
- `src/test/java/com/example/app/security/ApiKeyFilterTest.java` — API-key filter unit tests.
- `src/test/java/com/example/app/controller/UserControllerIntegrationTest.java` — HTTP-level User CRUD and validation tests.

## Secrets Moved
- administrator API key -> `app.secret.admin-api-key=${ADMIN_API_KEY:}`

## DB URLs Resolved
- `jdbc:mysql://localhost:3306/gen_d1e3b211c8e2` -> `jdbc:mysql://localhost:3306/gen_d1e3b211c8e2`

## Compilation Result
PASSED — `mvn compile -q`, `mvn test -q`, and `mvn package -DskipTests -q` completed successfully with Java 21.
