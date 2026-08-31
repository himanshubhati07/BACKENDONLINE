COMMIT_MESSAGE: Add versioned API health endpoint with ISO timestamp

## Features Added
- Added `GET /api/v1/health`, returning `status`, build metadata `version`, and the current server `timestamp`.
- Added focused controller coverage for the response status and JSON shape.

## Files Modified
- `build.gradle` — enabled Spring Boot build metadata generation.
- `src/main/resources/application.properties` — aligned the configurable default server port with port 22662.
- `start.sh` — made startup honor `SERVER_PORT` with a 22662 default.

## Files Added
- `src/main/java/com/example/app/controller/HealthController.java` — exposes the health route.
- `src/main/java/com/example/app/dto/HealthResponse.java` — defines the health response shape.
- `src/test/java/com/example/app/controller/HealthControllerTest.java` — verifies HTTP 200 and response fields.
- `ai_changes.md` — documents this change set and test outcome.

## Secrets Moved
- None.

## DB URLs Resolved
- None; the project uses MongoDB and contains no JDBC configuration.

## Test Results Summary
- Final: 9 PASSED, 0 FAILED, 0 SKIPPED (`./gradlew test -q`).
