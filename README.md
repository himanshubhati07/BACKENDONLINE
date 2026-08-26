# Backend API — Spring Boot + MongoDB + JWT

Production-ready REST API built with Java 17, Spring Boot, Spring Data MongoDB and JWT
authentication. Implements user registration/login, role-based authorization (USER/ADMIN)
and a full Product CRUD module.

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Web / Spring Security / Spring Data MongoDB
- MongoDB
- JWT (io.jsonwebtoken / jjwt) — HS256, 30 minute expiry
- springdoc-openapi (Swagger UI)
- Maven

## Project Structure

```
src/main/java/com/example/app/
 ├── AppApplication.java
 ├── config/        SecurityConfig, CorsConfig, OpenApiConfig, DataSeeder
 ├── document/      User, Product, Role (Spring Data MongoDB @Document)
 ├── repository/    UserRepository, ProductRepository
 ├── service/       AuthService, UserService, ProductService
 ├── controller/    AuthController, UserController, ProductController
 ├── dto/           request/response DTOs + ApiResponse/PageResponse wrappers
 ├── security/      JwtUtil, JwtFilter, UserDetailsServiceImpl
 └── exception/     GlobalExceptionHandler + custom exceptions
```

## Configuration

All configuration lives in `src/main/resources/application.properties` and can be
overridden with environment variables — no secrets are hardcoded:

| Property | Env Var | Default |
|---|---|---|
| `spring.data.mongodb.uri` | `MONGODB_URI` | `mongodb://localhost:27017/gen_e8ea1cb1f5b0` |
| `server.port` | — | `22916` |
| `jwt.secret` | `JWT_SECRET` | built-in fallback (change in production) |
| `jwt.expiration-ms` | `JWT_EXPIRATION` | `1800000` (30 minutes) |

## Running Locally

```bash
chmod +x start.sh
./start.sh
```

This builds the jar with Maven and starts the server on port `22916`.

Windows:

```bat
start.bat
```

Health check: `GET http://localhost:22916/actuator/health`

Swagger UI: `http://localhost:22916/docs`
OpenAPI JSON: `http://localhost:22916/api-docs`

## Authentication Flow

1. `POST /api/v1/auth/register` — create an account (role defaults to `USER`)
2. `POST /api/v1/auth/login` — receive a JWT access token
3. Send `Authorization: Bearer <token>` on subsequent requests
4. Spring Security validates the JWT on every request via a custom `JwtFilter`
5. `@PreAuthorize` method security enforces ADMIN-only endpoints

A seeded ADMIN account is available out of the box:

```
email: admin@example.com
password: Admin@123
```

## API Endpoints

Base path: `/api/v1`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register a new user |
| POST | `/api/v1/auth/login` | Public | Login and receive JWT |
| GET | `/api/v1/users/me` | USER/ADMIN | Get current authenticated user's profile |
| GET | `/api/v1/users?page=&size=` | ADMIN | List all users (paginated) |
| DELETE | `/api/v1/users/{id}` | ADMIN | Delete a user |
| POST | `/api/v1/products` | ADMIN | Create a product |
| GET | `/api/v1/products?page=&size=&search=&category=&sort=` | USER/ADMIN | List products (paginated, searchable, filterable) |
| GET | `/api/v1/products/{id}` | USER/ADMIN | Get a product by id |
| PUT | `/api/v1/products/{id}` | ADMIN | Update a product |
| DELETE | `/api/v1/products/{id}` | ADMIN | Delete a product |
| GET | `/actuator/health` | Public | Health check |

## Response Format

Success:
```json
{ "success": true, "message": "Product fetched successfully", "data": { } }
```

Error:
```json
{ "success": false, "message": "Product not found", "errorCode": "PRODUCT_NOT_FOUND" }
```

Validation error:
```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "errors": { "email": "Invalid email format" }
}
```

## Validation Rules

- `name`: required
- `email`: required, must be a valid email, must be unique
- `password`: min 8 characters, must contain uppercase, lowercase, digit and special character
- Product `price`: required, > 0
- Product `quantity`: required, >= 0
- Product `name`: 2–120 characters

## Security Notes

- Passwords hashed with BCrypt — never stored/returned in plain text
- JWT signed with HS256, 30 minute expiry, secret configurable via `JWT_SECRET`
- Stateless sessions (no server-side session state)
- CORS permits all origins/methods/headers for development convenience
- Global exception handler prevents leaking stack traces or internal errors

## Testing

Endpoints were exercised end-to-end with curl against a running instance (see
`/api_tests/test_results.md` for the full pass/fail log covering auth, RBAC,
CRUD, validation and pagination/search/filter scenarios).
