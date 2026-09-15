# Cart API

Spring Boot 3.1 cart backend using Java 17, PostgreSQL, JWT authentication, and Kafka cart-item events.

## Run locally

1. Ensure PostgreSQL is available with the supplied `.env_bc9da269-4013-40e1-a958-1c655b606880` values.
2. Ensure Kafka is available at `localhost:9092` for event publishing/consumption.
3. Run `chmod +x ./start.sh && ./start.sh`.
4. Open Swagger at `http://localhost:24066/docs`.

Docker/Compose are intentionally not included because infrastructure configuration disables them.

## Environment variables

- `DB_URL`: JDBC database URL.
- `DB_USER`: PostgreSQL user.
- `DB_PASSWORD`: PostgreSQL password.
- `JWT_SECRET`: HS256 signing secret.
- `SERVER_PORT`: optional start-script override, default `24066`.

## API endpoints

| Method | Endpoint | Authentication |
|---|---|---|
| POST | /api/v1/auth/register | Public |
| POST | /api/v1/auth/login | Public |
| POST | /api/v1/products | JWT |
| GET | /api/v1/products | JWT |
| GET | /api/v1/products/{id} | JWT |
| DELETE | /api/v1/products/{id} | JWT |
| POST | /api/v1/cart/items | JWT |
| GET | /api/v1/cart | JWT |
| PUT | /api/v1/cart/items/{productId} | JWT |
| DELETE | /api/v1/cart/items/{productId} | JWT |

Pagination uses `page` and `size`, with size 20 by default. Cart total is calculated from item quantity and product price. Cart operations publish `cart-item-events` Kafka events.

## Tests

Run `./mvnw compile -q` then start the service and use the documented endpoints. Health is at `/actuator/health`.
