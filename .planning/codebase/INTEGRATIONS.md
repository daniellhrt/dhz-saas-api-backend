# Integrations

> Last mapped: 2026-05-26

## Database — PostgreSQL 16

| Property | Value |
|----------|-------|
| Image | `postgres:16-alpine` |
| Container | `barbersaas-postgres` |
| Port | `5432` |
| ORM | Hibernate 6.4 (JPA) |
| Migration | Flyway (9 versions: `V1` → `V9`) |
| Validation | `ddl-auto: validate` (schema matches entity) |

### Schema (Flyway Migrations)
- `V1__create_table_clients.sql` — Clients table with tenant isolation
- `V2__create_table_barbers.sql` — Barbers table with tenant + password
- `V3__create_table_service_items.sql` — Catalog services with price/duration
- `V4__create_table_appointments.sql` — Appointments with FK to clients, service_items
- `V5__add_role_to_barbers.sql` — ADMIN/USER role column
- `V6__add_cancel_reason_to_appointments.sql` — Cancel reason field
- `V7__add_fields_to_clients.sql` — CPF, birth_date, notes columns
- `V8__alter_appointments_nullable.sql` — Nullable client/service for block schedules
- `V9__create_sales_and_products.sql` — Products and sales tables

## Cache / Rate Limiting — Redis 7

| Property | Value |
|----------|-------|
| Image | `redis:7-alpine` |
| Container | `barbersaas-redis` |
| Port | `6379` |
| Client | Lettuce |
| Usage | Distributed rate limiting (Bucket4j) |

### Fallback Behavior
- `RedisConfig` creates `LettuceBasedProxyManager` only when `spring.data.redis.host` is set
- If Redis unavailable → falls back to `ConcurrentHashMap` in-memory buckets
- Config: `src/main/java/br/com/dht/apibackend/config/RedisConfig.java`

## Authentication — JWT (jjwt 0.12.5)

| Property | Value |
|----------|-------|
| Algorithm | HMAC-SHA512 |
| Token TTL | 86400000ms (24h) / 3600000ms (integration tests) |
| Claims | `sub` (email), `tenantId`, `role` |

### Flow
1. `POST /api/v1/auth/login` → `AuthService.authenticate()` validates credentials
2. `TokenService.generateToken()` signs JWT with `tenantId` + `role` claims
3. `SecurityFilter` extracts JWT from `Authorization: Bearer` header
4. Token validated → sets `SecurityContext` + `TenantContext.setTenantId()`
5. `TenantContext.clear()` in `finally` block prevents tenant leakage

## API Documentation — Springdoc OpenAPI

| Property | Value |
|----------|-------|
| Swagger UI | `/swagger-ui/index.html` |
| OpenAPI Spec | `/v3/api-docs` |

## CORS

- Configurable origins: `app.cors.allowed-origins` (default: `localhost:5173`, `localhost:3000`)
- Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Credentials: allowed
- Config: `src/main/java/br/com/dht/apibackend/config/CorsConfig.java`

## Container Orchestration — Docker Compose

- `compose.yaml`: 3 services (postgres, redis, api)
- `Dockerfile`: Multi-stage (JDK 21 build → JRE 21 runtime), non-root user (`spring:spring`)
- Production: `SPRING_PROFILES_ACTIVE=prod`
