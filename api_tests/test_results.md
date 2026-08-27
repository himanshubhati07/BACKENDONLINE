# Live API Test Results

Final iteration: **Iteration 0** (no endpoint failures; no fix iteration required)  
Base URL: `http://localhost:25026`

| Test | Expected | Actual | Result | Body assertion |
|---|---:|---:|---|---|
| GET `/actuator/health` | 200 | 200 | PASSED | `status` was `UP` |
| GET `/docs` (follow redirect) | 200 | 200 | PASSED | Swagger UI document returned |
| GET `/api-docs` | 200 | 200 | PASSED | OpenAPI document returned |
| POST `/api/v1/auth/register` valid | 201 | 201 | PASSED | id, normalized email, timestamps, and server-assigned `USER` role returned; password absent |
| POST `/api/v1/auth/register` duplicate | 409 | 409 | PASSED | safe duplicate-email message returned |
| POST `/api/v1/auth/register` invalid fields | 400 | 400 | PASSED | field errors returned for name, email, and password |
| POST `/api/v1/auth/login` valid | 200 | 200 | PASSED | success, Bearer token, 1800-second expiry, and safe user DTO returned |
| POST `/api/v1/auth/login` invalid password | 401 | 401 | PASSED | generic invalid-credentials message returned |
| POST `/api/v1/users` valid | 201 | 201 | PASSED | generated id and submitted name/email returned; password absent |
| POST `/api/v1/users` invalid fields | 400 | 400 | PASSED | field validation errors returned |
| GET `/api/v1/users` filter/sort/pagination | 200 | 200 | PASSED | matching user, offset 0, limit 20, and total count returned |
| GET `/api/v1/users` invalid pagination | 400 | 400 | PASSED | invalid request parameter rejected |
| GET `/api/v1/users/{id}` valid | 200 | 200 | PASSED | requested id and submitted fields returned |
| GET `/api/v1/users/{id}` unknown id | 404 | 404 | PASSED | safe `User not found` response returned |
| PUT `/api/v1/users/{id}` valid | 200 | 200 | PASSED | updated name/email and unchanged id returned |
| DELETE `/api/v1/users/{id}` valid | 204 | 204 | PASSED | empty successful response |
| GET `/api/v1/users/{id}` after delete | 404 | 404 | PASSED | deletion verified |

All users created by live tests were deleted. Seed data was not modified.
