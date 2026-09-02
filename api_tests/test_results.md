# API Test Results

| Endpoint / case | Result | HTTP | Assertion |
|---|---:|---:|---|
| GET /actuator/health | PASSED | 200 | Response contains `status: UP`. |
| POST /api/v1/api-keys | PASSED | 201 | Returned id, submitted name, and one-time `apiKey`. |
| GET /api/v1/api-keys | PASSED | 200 | Returned created key metadata and no plaintext key. |
| DELETE /api/v1/api-keys/{id} | PASSED | 204 | Test key was revoked. |
| POST /api/v1/users without key | PASSED | 401 | Missing key was rejected. |
| GET /api/v1/users with invalid key | PASSED | 401 | Invalid key was rejected. |
| POST /api/v1/users | PASSED | 201 | Response included submitted user data, generated id, and default ACTIVE status. |
| GET /api/v1/users/{id} | PASSED | 200 | Response contained created id and submitted email. |
| GET /api/v1/users filter/search | PASSED | 200 | Response content included matching user and offset/limit fields. |
| POST /api/v1/users invalid payload | PASSED | 400 | Email, phone, and blank-name validation errors returned. |
| POST /api/v1/users duplicate email | PASSED | 409 | Duplicate email was rejected. |
| PUT /api/v1/users/{id} | PASSED | 200 | Response included updated last name, phone, and INACTIVE status. |
| DELETE /api/v1/users/{id} | PASSED | 204 | Test user soft-deleted. |
| GET deleted /api/v1/users/{id} | PASSED | 404 | Deleted test user was no longer retrievable. |
| GET missing /api/v1/users/{id} | PASSED | 404 | Unknown user was rejected. |
| GET /api/v1/users with revoked key | PASSED | 401 | Revoked test key was rejected. |

All resources created by the API test run were cleaned up: the user was soft-deleted and the test API key was revoked.
