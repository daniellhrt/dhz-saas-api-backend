# Stack

> Last mapped: 2026-05-26

## Language & Runtime

| Property | Value |
|----------|-------|
| Language | Java 21 (LTS) |
| Runtime | Eclipse Temurin JDK 21 |
| Build | Maven (Maven Wrapper `mvnw`) |
| Framework | Spring Boot 3.2.5 |
| Packaging | JAR (embedded Tomcat) |

## Core Dependencies

### Spring Boot Starters
- `spring-boot-starter-web` — REST API + embedded Tomcat
- `spring-boot-starter-data-jpa` — Hibernate 6.4 ORM + Spring Data JPA
- `spring-boot-starter-validation` — JSR-380 Bean Validation (Jakarta)
- `spring-boot-starter-security` — Spring Security 6.x
- `spring-boot-devtools` — Hot reload (dev only, optional)
- `spring-boot-docker-compose` — Automatic Docker Compose lifecycle (dev only, optional)

### Database
- **PostgreSQL 16** — Primary database (runtime)
- **H2 Database** — Fallback for unit test profile (runtime)
- **Flyway Core** — Schema migrations (`src/main/resources/db/migration/V1..V9`)
- **Hibernate Dialect** — `org.hibernate.dialect.PostgreSQLDialect`

### Security & Auth
- **jjwt 0.12.5** (io.jsonwebtoken) — JWT generation, signing (HMAC-SHA512), validation
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- **BCrypt** — Password hashing via `BCryptPasswordEncoder`
- **Bucket4j 7.6.0** — Rate limiting (5 req/min per IP on login)
  - `bucket4j-core` — Core token bucket algorithm
  - `bucket4j-redis` — Distributed rate limiting via Redis
- **Lettuce Core** — Redis client for Bucket4j distributed buckets

### API Documentation
- **Springdoc OpenAPI 2.5.0** — Swagger UI at `/swagger-ui/index.html`

### Code Quality
- **Lombok** — `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`, `@Builder`
- **JaCoCo 0.8.10** — Code coverage reports (`target/site/jacoco/index.html`)

### Test Dependencies
- `spring-boot-starter-test` — JUnit 5, Mockito, MockMvc, AssertJ
- `spring-security-test` — Security testing utilities
- **Testcontainers 1.20.1** — `testcontainers`, `junit-jupiter`, `postgresql`
- `spring-boot-testcontainers` — `@ServiceConnection` support

## Configuration

### Profiles
| Profile | Purpose | Data Source |
|---------|---------|-------------|
| `dev` (default) | Local development | Docker Compose auto-start PostgreSQL + Redis |
| `integration` | Integration tests | Direct PostgreSQL on `localhost:5432` |
| `prod` | Production | Env vars for DB/Redis/JWT |

### Key Config Files
- `src/main/resources/application.yml` — Main config with dev/prod profiles
- `src/test/resources/application-integration.yml` — Integration test profile
- `.env` / `.env.example` — Environment variables (DB creds, JWT secret)
- `compose.yaml` — Docker Compose (PostgreSQL 16 + Redis 7 + API)
- `Dockerfile` — Multi-stage build (Temurin 21 JDK → JRE, non-root user)

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | fallback string | HMAC key (min 256-bit) |
| `POSTGRES_USER` | from `.env` | Database user |
| `POSTGRES_PASSWORD` | from `.env` | Database password |
| `POSTGRES_DB` | from `.env` | Database name |
| `REDIS_HOST` | `localhost` | Redis host for distributed rate limiting |
