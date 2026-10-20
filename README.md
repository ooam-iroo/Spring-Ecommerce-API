# Spring E-commerce API

Production-oriented Spring Boot REST API for users, products, categories, carts, orders, payments, reviews, and administration.

## Stack

Java 21, Spring Boot 4.1.1, Spring Security/JWT, PostgreSQL, JPA/Hibernate, Redis Cache, Flyway, Testcontainers, Docker Compose, and GitHub Actions.

## Run locally

Required environment variables are `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, and `JWT_SECRET` (at least 32 characters). Defaults are suitable only for local development.

Start infrastructure with:

```bash
docker compose up --build
```

Run tests with Docker available:

```bash
./mvnw test
```

The API listens on `http://localhost:8080`. Authentication is available under `/api/v1/auth`; send the returned JWT as `Authorization: Bearer <token>`. Health is exposed at `/actuator/health`.

## Architecture

Controllers handle HTTP concerns, services own business rules and transaction boundaries, repositories handle persistence, and DTOs define API contracts. PostgreSQL is the source of truth; Redis caches product and category read DTOs and is invalidated on relevant mutations.

## Verification status

See [REDIS_CACHE_COMPLETION_REPORT.md](REDIS_CACHE_COMPLETION_REPORT.md) for the latest verified test results and known blockers. CI configuration is present, but no GitHub-hosted workflow execution has been verified from this environment.
