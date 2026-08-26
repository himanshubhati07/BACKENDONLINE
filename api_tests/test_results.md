# API Test Results

Server: `http://localhost:22916` — MongoDB: `gen_e8ea1cb1f5b0`
Legend: PASS = behaved exactly as expected (including deliberate negative tests expecting 4xx).

## Iteration 0 (Baseline)

| # | Method | Endpoint | Case | Expected | Actual | Result |
|---|---|---|---|---|---|---|
| 1 | POST | /api/v1/auth/register | valid payload | 201 | 201 | PASS |
| 2 | POST | /api/v1/auth/register | duplicate email | 409 | 409 | PASS |
| 3 | POST | /api/v1/auth/register | invalid email format | 400 | 400 | PASS |
| 4 | POST | /api/v1/auth/register | weak password | 400 | 400 | PASS |
| 5 | POST | /api/v1/auth/login | valid credentials | 200 + token | 200 + token | PASS |
| 6 | POST | /api/v1/auth/login | invalid password | 401 | 401 | PASS |
| 7 | POST | /api/v1/auth/login | admin credentials | 200 + token | 200 + token | PASS |
| 8 | GET | /api/v1/users/me | no token | 401 | **404** | **FAILED** |
| 9 | GET | /api/v1/users/me | valid user token | 200 | 200 | PASS |
| 10 | GET | /api/v1/users | USER token | 403 | 403 | PASS |
| 11 | GET | /api/v1/users | ADMIN token | 200 | 200 | PASS |
| 12 | GET | /api/v1/products | no token | 401 | **200** | **FAILED** |
| 13 | POST | /api/v1/products | USER token (should be denied) | 403 | 403 | PASS |
| 14 | POST | /api/v1/products | ADMIN, valid payload | 201 | 201 | PASS |
| 15 | POST | /api/v1/products | invalid price (<=0) | 400 | 400 | PASS |
| 16 | POST | /api/v1/products | invalid quantity (<0) | 400 | 400 | PASS |

Two defects found in baseline: unauthenticated requests to `GET /users/me` and
`GET /products` were not rejected with 401 because those endpoints had no explicit
authentication requirement (the mandated SecurityFilterChain permits all requests
at the request-matcher level; authorization was intended to be enforced purely via
`@PreAuthorize`).

## Fix Applied (Iteration 1)

- Added `@PreAuthorize("isAuthenticated()")` to `GET /api/v1/users/me`,
  `GET /api/v1/products`, and `GET /api/v1/products/{id}`.
- Updated `GlobalExceptionHandler#handleAccessDenied` to inspect the current
  `SecurityContextHolder` authentication: anonymous/unauthenticated → `401 UNAUTHORIZED`,
  authenticated but insufficient role → `403 FORBIDDEN`.

## Iteration 1 (Full Re-test — Final Results)

| # | Method | Endpoint | Case | Expected | Actual | Result |
|---|---|---|---|---|---|---|
| 1 | POST | /api/v1/auth/register | valid payload | 201 | 201 | PASS |
| 2 | POST | /api/v1/auth/register | duplicate email | 409 | 409 | PASS |
| 3 | POST | /api/v1/auth/register | invalid email format | 400 | 400 | PASS |
| 4 | POST | /api/v1/auth/register | weak password (<8 chars / missing complexity) | 400 | 400 | PASS |
| 5 | POST | /api/v1/auth/login | valid credentials → returns token/tokenType/expiresIn | 200 | 200 | PASS |
| 6 | POST | /api/v1/auth/login | invalid password | 401 | 401 | PASS |
| 7 | POST | /api/v1/auth/login | admin credentials (seeded) | 200 | 200 | PASS |
| 8 | GET | /api/v1/users/me | no token | 401 | 401 | PASS |
| 9 | GET | /api/v1/users/me | invalid/malformed token | 401 | 401 | PASS |
| 10 | GET | /api/v1/users/me | expired token (tested with 1.5s TTL config) | 401 | 401 | PASS |
| 11 | GET | /api/v1/users/me | valid USER token | 200, correct profile fields | 200 | PASS |
| 12 | GET | /api/v1/users?page=0&size=20 | USER token (insufficient role) | 403 | 403 | PASS |
| 13 | GET | /api/v1/users?page=0&size=20 | no token | 401 | 401 | PASS |
| 14 | GET | /api/v1/users?page=0&size=20 | ADMIN token | 200, paginated list | 200 | PASS |
| 15 | DELETE | /api/v1/users/{id} | USER token (insufficient role) | 403 | 403 | PASS |
| 16 | DELETE | /api/v1/users/{id} | ADMIN, valid id (freshly-created test user) | 200 | 200 | PASS |
| 17 | DELETE | /api/v1/users/{id} | ADMIN, same id again | 404 | 404 | PASS |
| 18 | POST | /api/v1/products | USER token (insufficient role) | 403 | 403 | PASS |
| 19 | POST | /api/v1/products | ADMIN, valid payload | 201, echoes submitted fields | 201 | PASS |
| 20 | POST | /api/v1/products | invalid price (-5, must be > 0) | 400 | 400 | PASS |
| 21 | POST | /api/v1/products | invalid quantity (-1, must be >= 0) | 400 | 400 | PASS |
| 22 | GET | /api/v1/products?page=0&size=20 | no token | 401 | 401 | PASS |
| 23 | GET | /api/v1/products?page=0&size=20 | USER token | 200, paginated list | 200 | PASS |
| 24 | GET | /api/v1/products/{id} | valid id | 200, correct fields | 200 | PASS |
| 25 | GET | /api/v1/products/{id} | non-existent id | 404 | 404 | PASS |
| 26 | GET | /api/v1/products?search=phone | search by name | 200, only matching product returned | 200 | PASS |
| 27 | GET | /api/v1/products?category=electronics&sort=price,asc | filter + sort | 200, filtered & sorted result | 200 | PASS |
| 28 | PUT | /api/v1/products/{id} | USER token (insufficient role) | 403 | 403 | PASS |
| 29 | PUT | /api/v1/products/{id} | ADMIN, valid payload | 200, updated fields reflected | 200 | PASS |
| 30 | DELETE | /api/v1/products/{id} | USER token (insufficient role) | 403 | 403 | PASS |
| 31 | DELETE | /api/v1/products/{id} | ADMIN, valid id | 200 | 200 | PASS |
| 32 | GET | /api/v1/products/{id} | same id after delete | 404 | 404 | PASS |
| 33 | GET | /docs | Swagger UI | 200/302 (redirect to index) | 302 | PASS |
| 34 | GET | /api-docs | OpenAPI JSON | 200 | 200 | PASS |
| 35 | GET | /actuator/health | health check | 200 | 200 | PASS |

**Result: 35/35 PASSED, 0 FAILED, 0 SKIPPED.**

All test resources created during testing (throwaway users and products) were
deleted before finishing; seed data (`admin@example.com` and the 3 seeded
products) was left untouched.
