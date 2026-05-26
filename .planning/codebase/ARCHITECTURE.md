# Architecture

> Last mapped: 2026-05-26

## Pattern

**Multi-tenant SaaS REST API** using a layered DDD-lite architecture with row-level tenant isolation.

### Architectural Style
- **Layered Architecture** — Controller → Service → Repository
- **DDD-Lite Bounded Contexts** — Each domain (barber, client, catalog, appointment, product, sale) is a self-contained package with its own Controller, Service, Repository, DTO, and Entity
- **Multi-Tenant** — Row-level isolation via `tenant_id` column; every query filters by `TenantContext.getTenantId()`

## Layers

```
┌─────────────────────────────────────────────┐
│              Security Filter Chain           │
│  RateLimitingFilter → SecurityFilter         │
│  (JWT extraction, TenantContext injection)   │
├─────────────────────────────────────────────┤
│              REST Controllers                │
│  AuthController, BarberController,           │
│  ClientController, CatalogController,        │
│  AppointmentController, ProductController,   │
│  SaleController                              │
├─────────────────────────────────────────────┤
│              Services (Business Logic)       │
│  AuthService, BarberService, ClientService,  │
│  CatalogService, AppointmentService,         │
│  ProductService, SaleService                 │
├─────────────────────────────────────────────┤
│              Repositories (Data Access)      │
│  Spring Data JPA with tenant-scoped queries  │
│  findByIdAndTenantId / findAllByTenantId     │
├─────────────────────────────────────────────┤
│              Database (PostgreSQL 16)         │
│  Flyway migrations V1-V9                     │
└─────────────────────────────────────────────┘
```

## Data Flow

### Request Lifecycle
1. HTTP request arrives at embedded Tomcat
2. `RateLimitingFilter` checks if `/api/v1/auth/login` → rate limit by IP (Bucket4j)
3. `SecurityFilter` extracts JWT `Bearer` token from `Authorization` header
4. Token validated → `TenantContext.setTenantId(tenantId)` via `ThreadLocal`
5. Spring Security sets `Authentication` with email + `ROLE_ADMIN` or `ROLE_USER`
6. Controller receives request, delegates to Service
7. Service uses `TenantContext.getTenantId()` for all data-scoped operations
8. Repository queries always include `tenant_id` filter
9. `finally` block in `SecurityFilter` calls `TenantContext.clear()` — prevents tenant leakage across pooled threads

### Tenant Isolation Contract
- **Set in:** `SecurityFilter.doFilterInternal()` (from JWT `tenantId` claim)
- **Used by:** Every Service via `TenantContext.getTenantId()`
- **Cleared in:** `SecurityFilter.doFilterInternal()` `finally` block
- **Repository pattern:** `findByIdAndTenantId()`, `findAllByTenantId()`

## Key Abstractions

### TenantContext (`config/TenantContext.java`)
`ThreadLocal<String>` storing the current request's `tenant_id`. Critical for row-level isolation.

### SecurityFilter (`security/SecurityFilter.java`)
`OncePerRequestFilter` that extracts JWT, validates, sets `SecurityContext` + `TenantContext`, and cleans up in `finally`.

### GlobalExceptionHandler (`exception/GlobalExceptionHandler.java`)
Central exception mapping:
- `InvalidCredentialsException` → 401
- `IllegalArgumentException` / `IllegalStateException` → 400
- `SecurityException` → 403
- `MethodArgumentNotValidException` → 400 (validation)
- `Exception` → 500 (generic fallback, no stack trace leak)

All errors return `StandardError` JSON: `{ timestamp, status, error, message, path }`

### Appointment State Machine
```
PENDING → CONFIRMED → IN_PROGRESS → COMPLETED
PENDING → CANCELED
CONFIRMED → CANCELED
```
- Transitions enforced in `AppointmentService`
- `COMPLETED` can be reverted to `IN_PROGRESS`
- Overlap check (`hasOverlappingAppointment`) ignores `CANCELED` appointments

## Entry Points

| Entry Point | Path | Auth |
|-------------|------|------|
| Register (public) | `POST /api/v1/auth/register` | None |
| Login (public) | `POST /api/v1/auth/login` | None |
| Swagger UI | `/swagger-ui/index.html` | None |
| All other endpoints | `/api/v1/*` | JWT Bearer required |
