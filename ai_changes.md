COMMIT_MESSAGE: Remove user delete API endpoint and service method

## Features Added
- Removed the "delete user" capability entirely, per user request ("the user requests the removal of the delete API"):
  - Deleted the `DELETE /api/v1/users/{id}` endpoint from `UserController`.
  - Deleted the now-unused `UserService.delete(Long id)` soft-delete method.
  - Existing soft-delete infrastructure (the `deleted` column on `User`, and the
    `findByDeletedFalse*` repository queries used by list/get) was left intact
    since it is unrelated read-path filtering logic, not the delete API itself.
  - The unrelated `DELETE /api/v1/api-keys/{id}` (key revocation) admin endpoint
    was intentionally left untouched — it is a distinct "revoke" operation, not
    the user-facing delete API the request refers to.
- Updated the integration test and README to drop references to the removed
  delete endpoint.

## Files Modified
- `src/main/java/com/example/app/controller/UserController.java` — removed `delete()` handler and unused `DeleteMapping` import.
- `src/main/java/com/example/app/service/UserService.java` — removed `delete(Long id)` method.
- `src/test/java/com/example/app/controller/UserControllerIntegrationTest.java` — removed delete-related mock stub, assertion, and unused imports.
- `README.md` — removed the `DELETE /api/v1/users/{id}` row from the endpoint table; updated port/DB references to match resolved values.
- `src/main/resources/application.properties` — updated `server.port` to `${SERVER_PORT:23006}` and resolved the MySQL JDBC URL (see below).

## Files Added
- None.

## Secrets Moved
- None — `app.secret.api-key` and `app.secret.admin-api-key` were already externalized via environment variables in application.properties.

## DB URLs Resolved
- `jdbc:mysql://localhost:3306/gen_d1e3b211c8e2` -> `jdbc:mysql://localhost:3306/gen_7c11ddcd7a3b`

## Compilation Result
PASSED — `mvn compile -q`, `mvn test -q`, and `mvn package -DskipTests -q` all completed successfully with Java 21.
