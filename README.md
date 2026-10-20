<div align="center">

# Spring E-commerce API

Production-oriented e-commerce backend built with Java 21, Spring Boot 4.1.1, PostgreSQL, Redis, JWT, Docker, and Testcontainers.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

</div>

> Replace the CI badge with the repository-specific workflow badge after publishing the repository. Its status is not claimed here until GitHub executes the workflow.

## Overview

Spring E-commerce API is a layered REST backend covering the main e-commerce workflow:

- JWT authentication, BCrypt password hashing, and role-based authorization
- User, address, category, product, cart, order, payment, and review APIs
- Admin user, product, category, order, and dashboard APIs
- Product filtering, pagination, validation, and business rules
- PostgreSQL persistence with JPA/Hibernate and Flyway migrations
- Redis-backed DTO caching for products and categories
- PostgreSQL/Redis integration tests with Testcontainers
- Docker Compose deployment and GitHub Actions CI configuration
- OpenAPI documentation with JWT Bearer authentication

## Stack

| Area | Technology |
|---|---|
| Runtime | Java 21 |
| Web | Spring Boot 4.1.1, Spring MVC |
| Security | Spring Security, JWT, BCrypt, method security |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 17 |
| Migrations | Flyway |
| Cache | Spring Cache, Spring Data Redis, Redis 7 |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |
| API docs | springdoc OpenAPI 3.1.1, Swagger UI |
| Delivery | Maven, Docker, Docker Compose, GitHub Actions |

## Architecture

```text
Client -> JWT filter -> Controllers -> Services/transactions -> Repositories -> PostgreSQL
                                      |                         |
                                      +------ Redis DTO cache -+
```

```text
com.example.ecommerce
├── config          Security, Redis, cache, auditing, OpenAPI, request filters
├── controller      Public and admin HTTP endpoints
├── dto             Request and response contracts
├── entity          JPA entities and domain state
├── exception       API error handling and domain exceptions
├── repository      Spring Data repositories and specifications
├── security        JWT and authenticated-user support
└── service         Business services and transaction boundaries
```

PostgreSQL is the source of truth. Redis stores response DTOs rather than managed JPA entities. Product and category mutations invalidate affected cache entries, including product list/filter/page results.

## Prerequisites

- JDK 21
- Maven 3.9+ or the included Maven Wrapper
- Docker Engine/Desktop with Compose support
- Docker access for Testcontainers integration tests

## Configuration

Production values must be supplied through environment variables. Local defaults are for development only.

| Variable | Purpose | Local default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/ecommerce` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `ecommerce` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `ecommerce` |
| `REDIS_HOST` | Redis hostname | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_SECRET` | JWT signing secret; use at least 32 characters | Development placeholder |
| `SPRING_PROFILES_ACTIVE` | Optional Spring profile | unset |

Never commit production credentials, JWT secrets, `.env` files, or tokens.

## Run with Docker Compose

```bash
docker compose up --build
```

The API listens on `http://localhost:8080`. Compose provides `postgres`, `redis`, and `api` services, health checks, service-name networking, and named data volumes.

```bash
docker compose down
```

Use `docker compose down -v` only when intentionally deleting local database and Redis volumes.

## Run locally

Start PostgreSQL and Redis with Compose, then run the application directly:

```bash
docker compose up -d postgres redis
./mvnw spring-boot:run
```

## API documentation

After startup:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- OpenAPI YAML: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

Use Swagger UI’s **Authorize** button with:

```text
Bearer <access-token>
```

## Authentication example

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","password":"StrongPassword123"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"ada@example.com","password":"StrongPassword123"}'
```

Use the returned token on protected endpoints:

```bash
curl http://localhost:8080/api/v1/cart \
  -H 'Authorization: Bearer <access-token>'
```

## Endpoint groups

| Group | Base path | Access |
|---|---|---|
| Authentication | `/api/v1/auth` | Public |
| Products | `/api/v1/products` | Public reads; mutations authenticated |
| Categories | `/api/v1/categories` | Public reads; mutations authenticated |
| Cart | `/api/v1/cart` | Authenticated user |
| Orders | `/api/v1/orders` | Authenticated user |
| Payments | `/api/v1/orders/{orderId}/payment` | Authenticated user |
| Reviews | `/api/v1/products/{productId}/reviews`, `/api/v1/reviews` | Authenticated user |
| Addresses | `/api/v1/users/{userId}/addresses` | Authenticated owner |
| Admin users | `/api/v1/admin/users` | Admin role |
| Admin products | `/api/v1/admin/products` | Admin role |
| Admin categories | `/api/v1/admin/categories` | Admin role |
| Admin orders | `/api/v1/admin/orders` | Admin role |
| Dashboard | `/api/v1/admin/dashboard` | Admin role |

Product listing supports filtering by name, category, status, minimum price, and maximum price, plus pagination. Swagger contains the exact parameter and schema definitions.

## Error responses

Errors use a consistent response contract:

```json
{
  "timestamp": "2026-10-20T12:00:00+02:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/auth/register",
  "correlationId": "7d3c0e7e-7dc1-4ad0-96e7-4ed8f2d3c2ab",
  "fieldErrors": {"email": "must be a well-formed email address"}
}
```

The application handles not-found, business, validation, malformed-request, data-integrity, endpoint-not-found, and unexpected internal errors. A valid `X-Correlation-Id` is accepted; otherwise one is generated and returned.

## Health and operations

```text
GET /actuator/health
```

The current configuration exposes only `health` and `info`. PostgreSQL and Redis health contributors are provided by Actuator when those services are available.

## Testing

```bash
./mvnw test
```

Testcontainers tests require Docker. Run the Redis integration class alone with:

```bash
./mvnw -Dtest=ProductCacheIntegrationTest test
```

Compile and package without tests:

```bash
./mvnw -DskipTests package
```

Do not claim a green full suite unless the command has completed successfully in the target environment.

## Docker image

The multi-stage Dockerfile uses a Maven Java 21 build image and a Java 21 JRE runtime image. For production, add a non-root runtime user, external secret management, image scanning, and network policies.

## CI

`.github/workflows/ci.yml` runs on pushes to `main`/`master` and pull requests. It configures Java 21, Maven caching, and `./mvnw -B verify`; GitHub-hosted runners provide Docker for Testcontainers.

The workflow file exists, but its success must be confirmed by an actual GitHub Actions run.

## Verification status

The Maven package build has been verified with tests skipped. The repository also contains extensive unit, controller, admin, and Testcontainers integration tests. The latest Redis cache run has reported cache-population failures and requires additional investigation before the complete suite can be called green. Docker Compose startup and GitHub-hosted CI have not been verified from this workspace.

## License

No license has been declared yet. Add an appropriate license before public distribution.
