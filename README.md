# Product Catalog & Ordering Service

Small Spring Boot microservice to prepare for a senior Java / Spring Boot interview. Implements common enterprise features and serves as a hands-on exercise to demonstrate knowledge of REST, persistence, transactions, caching, messaging, observability, resilience, security and testing.

## Goal
Build a microservice that manages `Product` and `Order` entities and demonstrates:
- Layered architecture (controller / service / repository)
- Transactions and concurrency control
- Caching and cache invalidation
- Messaging (Spring Cloud Stream)
- Actuator health indicators and metrics (Micrometer / Prometheus)
- Resilience (Resilience4j)
- Security (JWT or BasicAuth)
- Tests (unit + integration)
- Docker-based reproducible environment

## Key Features / Requirements
- REST CRUD for `Product` and `Order` (JSON API)
- Persistence with Spring Data JPA (H2 for tests, PostgreSQL recommended for runtime)
- DTOs, validation (`javax.validation`), and global exception handling (`@ControllerAdvice`)
- Service layer with transactional order creation that decrements product stock
- Optimistic locking on product stock updates
- Caching on product reads (Caffeine or Redis) with proper invalidation after updates
- Publish `OrderCreated` events via Spring Cloud Stream (Kafka or Rabbit binder)
- Custom Actuator `HealthIndicator` checking DB / broker / cache
- Micrometer metrics and Prometheus endpoint
- Resilience patterns (retry / circuit breaker using Resilience4j)
- Security: JWT-based auth (or BasicAuth for quick runs) for write endpoints
- Unit and integration tests (REST + DB)
- `Dockerfile` and `docker-compose.yml` to run app + DB + broker + cache
- OpenAPI / Swagger documentation
- Structured logging (JSON) and correlation ID support

## API (suggested)
- `POST /api/products` — create product
- `GET /api/products` — list products (pagination, sorting)
- `GET /api/products/{id}` — get product (cached)
- `PUT /api/products/{id}` — update product (invalidates cache)
- `POST /api/orders` — create order (transactional, publishes event)
- `GET /api/orders/{id}` — get order
- Actuator: `GET /actuator/health`, `GET /actuator/prometheus` (metrics)

## Deliverables (what to include in repo)
- Source code (Maven) with layered packages
- `README.md` (this file)
- `Dockerfile` and `docker-compose.yml`
- Database migrations (Flyway) or `data.sql`
- Unit and integration tests
- Postman collection or curl examples
- OpenAPI spec (Swagger UI)

## Local development (quick)
1. Build:
   - `mvn clean package`
2. Run with embedded H2 (dev profile):
   - `SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run`  
   - dev profile should use H2, Caffeine cache, in-memory binder (or test binder) for Cloud Stream
3. Run tests:
   - `mvn test`

## Docker / Compose
- `docker-compose.yml` brings up:
  - PostgreSQL database 
  - Zookeeper for Kafka cluster management
  - Kafka message broker
  - Prometheus for metrics collection
  - Grafana for metrics visualization and dashboards
- To start the infrastructure:
  - `docker-compose up -d`
- To shut down and clean up:
  - `docker-compose down`
  - To remove volumes as well: `docker-compose down -v`

## Configuration notes
- Uses `application.yml` (profiles: `dev`, `prod`, `test`)
- Configuration includes:
  - PostgreSQL database connection with HikariCP connection pool
  - Kafka message broker integration
  - Prometheus metrics export
  - Custom health indicators for PostgreSQL and Kafka
- Application runs on port 8081 to avoid conflicts

## Testing guidance
- Unit tests for services (mock repositories)
- Integration tests with Spring Boot Test, embedded H2, and test binder for Cloud Stream
- Cache tests: assert DB is not hit on repeated GET after first read, and is hit after cache invalidation on update
- Event tests: verify a message is published to the configured binder when an order is created

## Observability & Health
- Custom health indicators for database and Kafka broker
  - Status visible in `/actuator/health` and `/actuator/health/readiness`
- Complete monitoring solution with:
  - Prometheus metrics exposed at `/actuator/prometheus`
  - Grafana dashboards for visualizing application metrics
  - Pre-configured Spring Boot dashboard showing:
    - HTTP request rates and response times
    - JVM memory usage
    - CPU usage (system and process)
    - Database connection pool metrics
    - HTTP status code distribution
- Metrics are automatically tagged with application name
- Percentile metrics configured for HTTP requests

## Security
- Protect write endpoints (`POST`/`PUT`) with JWT or BasicAuth
- Document how to acquire tokens (or provide test tokens) in README

## Evaluation checklist (for interview)
- Proper layer separation and DTO mapping
- Transaction boundaries and optimistic locking correctness
- Cache configuration and invalidation behavior
- Cloud Stream producer configuration and message format
- Custom health indicator present in `/actuator/health`
- Micrometer metrics exposed and meaningful
- Security applied correctly to endpoints
- Tests: unit + integration for main flows
- Reproducible startup with `docker-compose`
- Clear README and run instructions

## Acceptance criteria (minimal)
- `mvn clean package` builds
- Main flows work end-to-end: create product, get product, create order
- Cache reduces DB reads for repeated product GETs
- Order creation publishes a message to the broker (or test binder captures it)
- `/actuator/health` includes custom indicators

## Hints & Example snippets
- Caching (application.yml):
  ```yaml
  spring:
    cache:
      type: caffeine
  ```

- Monitoring URLs:
  - Spring Boot Application: http://localhost:8081
  - Actuator Endpoints: http://localhost:8081/actuator
  - Prometheus: http://localhost:9090
  - Grafana: http://localhost:3000 (login with admin/admin)

## Access postgress via docker-compose
# bash
docker-compose up -d
docker-compose exec postgres psql -U postgres -d productdb
