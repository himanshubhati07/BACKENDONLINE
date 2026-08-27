COMMIT_MESSAGE: Add JUnit5/Mockito unit+integration test suite, fix actuator health security bypass, move server port to 27171

## Features Added
- Full JUnit 5 + Mockito unit test suite for all three `@Service` classes (`AuthService`, `ProductService`, `UserService`) — 22 unit test methods covering register/login success & failure paths, password hashing verification, product CRUD + search/category-filter repository routing, and user profile/admin operations.
- Full Spring Boot integration test suite (`@SpringBootTest`, `RANDOM_PORT`, real `TestRestTemplate`, real MongoDB — no mocks/H2/Testcontainers) for `AuthController`, `ProductController`, and `UserController` — 33 integration test methods covering:
  - Registration (valid, duplicate email → 409, invalid email → 400, weak password → 400, missing name → 400)
  - Login (valid → token, wrong password → 401, unknown email → 401, missing fields → 400)
  - `/users/me` (valid token → 200, no token → 401, invalid token → 401)
  - `/users` admin listing (ADMIN → 200 paginated, USER → 403, no token → 401)
  - User deletion (ADMIN → 200 then re-delete → 404 proving removal, USER → 403)
  - Full product CRUD lifecycle: create → list → get by id → update → delete → get again (404), plus RBAC checks (USER forbidden on write ops, no-token unauthorized on read), validation failures (bad price/quantity), pagination, search, category+sort filtering, empty-result search.
- All new tests obtain their JWTs through the real `/api/v1/auth/register` and `/api/v1/auth/login` endpoints (ADMIN test accounts are seeded directly via `UserRepository`/`PasswordEncoder` since public registration always assigns `USER`, then authenticated through the real login endpoint — no JWT is ever fabricated by test code).
- Test-only Maven dependency `org.apache.httpcomponents.client5:httpclient5` added so integration tests can exercise endpoints that deliberately return 401 on POST/PUT/DELETE (see Bug Fixes below).

## Bug Fixes (found via testing this Brownfield project)
1. **`/actuator/health` returned 401 instead of 200** — `SecurityConfig`'s `authorizeHttpRequests` public matcher list did not include the actuator health endpoint, so the platform's own health-check polling (and any external monitor) was blocked by JWT auth. Added `/actuator/health` and `/actuator/health/**` to the `permitAll()` matcher list. `/actuator/info` remains governed by the same list only if explicitly hit; no other actuator endpoints are exposed (`management.endpoints.web.exposure.include=health,info` unchanged).
2. **TestRestTemplate `HttpRetryException: cannot retry due to server authentication, in streaming mode`** on any POST/PUT/DELETE request that deliberately expects a 401 response (e.g. login with wrong password, create-product with no token). This is a JDK `HttpURLConnection` limitation when a request has a body and receives a 401. Fixed by adding `httpclient5` as a test dependency and swapping `TestRestTemplate`'s request factory to `HttpComponentsClientHttpRequestFactory` in a `@BeforeEach` in each integration test class.

## Files Modified
- `src/main/resources/application.properties` — `server.port` changed from hardcoded `22916` to `${SERVER_PORT:27171}` (env-overridable, required deployment port); test resource override for `jwt.secret`/mongo URI documented.
- `src/main/java/com/example/app/config/SecurityConfig.java` — added `/actuator/health` and `/actuator/health/**` to the public (`permitAll`) request matcher list so health checks work without a JWT.
- `start.sh` / `start.bat` — `SERVER_PORT` changed from `22916` to `27171`.
- `README.md` / `api_tests/test_results.md` — all references to port `22916` updated to `27171`.
- `pom.xml` — added test-scope `org.apache.httpcomponents.client5:httpclient5` dependency (integration tests only, does not affect the shipped runtime jar).

## Files Added
- `src/test/resources/application.properties` — supplies a JWT secret for the test classpath (main `application.properties` intentionally has no default secret and refuses to start without `JWT_SECRET`); points at the same MongoDB database (`gen_e8ea1cb1f5b0` on `localhost:27017`) the app ships against by default.
- `src/test/java/com/example/app/service/AuthServiceTest.java` — Mockito unit tests for `AuthService`.
- `src/test/java/com/example/app/service/ProductServiceTest.java` — Mockito unit tests for `ProductService`.
- `src/test/java/com/example/app/service/UserServiceTest.java` — Mockito unit tests for `UserService`.
- `src/test/java/com/example/app/controller/AuthControllerIntegrationTest.java` — full-stack integration tests for the auth flow.
- `src/test/java/com/example/app/controller/ProductControllerIntegrationTest.java` — full-stack integration tests for Product CRUD, RBAC, pagination, search, filtering.
- `src/test/java/com/example/app/controller/UserControllerIntegrationTest.java` — full-stack integration tests for `/users/me`, admin listing, and user deletion.

## Files Removed
No existing files/functionality were removed.

## Secrets Moved
No new hardcoded secrets were found in the source. The existing brownfield codebase already externalized every secret before this change (`jwt.secret=${JWT_SECRET:}`, `spring.data.mongodb.uri=${MONGODB_URI:...}`, `app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:...}`) — these were verified, not modified. `server.port` was converted from a hardcoded literal to `${SERVER_PORT:27171}` for deployment-port flexibility (not a secret, but externalized for consistency).

## DB URLs Resolved
No JDBC database is used by this project (MongoDB via Spring Data). An existing MongoDB server was found already running locally (`mongodb://localhost:27017`) and the application's default database `gen_e8ea1cb1f5b0` was used as-is — no new database was created, no existing collections/data were altered or deleted.

## Test Results Summary
55 PASSED, 0 FAILED, 0 SKIPPED
(22 unit tests across AuthServiceTest/ProductServiceTest/UserServiceTest + 33 integration tests across AuthControllerIntegrationTest/ProductControllerIntegrationTest/UserControllerIntegrationTest)

Baseline (iteration 0) surfaced 3 errors, all the same root cause (`HttpRetryException` described above under Bug Fixes #2) — fixed in iteration 1 by switching the test HTTP client to Apache HttpClient5. Re-run after the fix: 55/55 passed. `mvn clean install` and `mvn package` both succeed, and the packaged jar starts cleanly on port 27171 against the running MongoDB instance with `/actuator/health` returning `200 {"status":"UP"}`.
