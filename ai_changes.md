COMMIT_MESSAGE: Configure environment API key for user management

## Features Added
- User create, get, update, delete, and offset-paginated list APIs remain available at `/api/v1/users` with validation and centralized error responses.
- User API requests can now be authenticated directly with the API key supplied through the `API_KEY` environment variable, while retaining existing database-backed API-key support.
- Added API-key unit coverage for missing, configured environment, and active stored keys; User HTTP CRUD and validation coverage remains in place.
- Container and startup configuration now use the required port `24037`.

## Files Modified
- `src/main/resources/application.properties` — configured port `24037`, retained the resolved MySQL JDBC URL, externalized MySQL credentials, and added `app.secret.api-key=${API_KEY:}`.
- `src/main/java/com/example/app/security/ApiKeyFilter.java` — validates the environment-configured API key with constant-time comparison before retaining database-key fallback behavior.
- `src/test/java/com/example/app/security/ApiKeyFilterTest.java` — added configured API-key authentication test coverage.
- `Dockerfile` — exposes and starts on port `24037`.
- `docker-compose.yml` — configures port `24037` and passes the required `API_KEY` environment variable.
- `start.sh` — packages and starts the service on port `24037`.
- `README.md` — documents `API_KEY` use and port `24037`.

## Files Added
- None.

## Secrets Moved
- User API key -> `app.secret.api-key=${API_KEY:}`
- Administrator API key -> `app.secret.admin-api-key=${ADMIN_API_KEY:}`
- MySQL username -> `spring.datasource.username=${MYSQL_USER:myuser}`
- MySQL password -> `spring.datasource.password=${MYSQL_PASSWORD:mypassword}`

## DB URLs Resolved
- `jdbc:mysql://localhost:3306/gen_d1e3b211c8e2` -> `jdbc:mysql://localhost:3306/gen_d1e3b211c8e2`

## Compilation Result
PASSED — `mvn compile -q`, `mvn test -q`, and `mvn package -DskipTests -q` completed successfully with Java 21.
