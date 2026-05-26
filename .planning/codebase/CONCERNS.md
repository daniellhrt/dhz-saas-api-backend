# Concerns

> Last mapped: 2026-05-26

## Technical Debt

### TD-1: No @PreAuthorize Role-Based Access Control
**Severity:** Medium  
**Location:** `src/main/java/br/com/dht/apibackend/domain/appointment/AppointmentController.java:62`

```java
// TODO: Validate ADMIN role here if using Spring Security roles (@PreAuthorize("hasRole('ADMIN')"))
```

Currently, ADMIN role enforcement happens **inside service methods** (e.g., `BarberService.assertAdminRole()`) rather than declaratively via `@PreAuthorize`. This works but:
- Requires manual role checks in every service method needing authorization
- Missing from `AppointmentController` (noted as TODO)
- Does not leverage Spring Security's built-in method security

**Recommendation:** Enable `@EnableMethodSecurity` and use `@PreAuthorize("hasRole('ADMIN')")` on appropriate service methods.

---

### TD-2: Product and Sale Domains Lack Tests
**Severity:** Medium  
**Location:** `src/main/java/br/com/dht/apibackend/domain/product/`, `src/main/java/br/com/dht/apibackend/domain/sale/`

The `product` and `sale` bounded contexts (added via `V9__create_sales_and_products.sql`) have zero test coverage:
- No `ProductServiceTest`, `ProductControllerTest`, or `ProductIntegrationTest`
- No `SaleServiceTest`, `SaleControllerTest`, or `SaleIntegrationTest`

**Recommendation:** Add unit + controller + integration tests in Sprint 2, following the established patterns from barber/client/catalog/appointment.

---

### TD-3: Barber Email Uniqueness Is Global, Not Per-Tenant
**Severity:** Low  
**Location:** `src/main/java/br/com/dht/apibackend/domain/barber/BarberService.java:30,51`

`findByEmail()` is a global lookup (not tenant-scoped). This means:
- A barber email must be globally unique across ALL tenants
- This is intentional for login (email = `sub` claim in JWT) but could cause issues if two separate barbershops want to register the same email

**Recommendation:** Keep as-is (email is the global identity), but document this constraint clearly in API docs.

---

### TD-4: No Pagination Default Limits
**Severity:** Low  
**Location:** All controllers accepting `Pageable`

Controllers accept `Pageable` without enforcing default page size or maximum page size. A client could request `?size=10000` and receive all records.

**Recommendation:** Add `@PageableDefault(size = 20, sort = "id")` and configure `spring.data.web.pageable.max-page-size` in application.yml.

---

## Security Considerations

### SEC-1: JWT Secret Management
**Status:** ⚠️ Needs Attention  
The JWT secret is stored in environment variables (`.env` file). The `.env.example` contains a placeholder.

- Production must use a strong (≥256-bit) secret
- `.env` file is in `.gitignore` (not committed)

### SEC-2: Rate Limiting Only on Login
**Status:** Acceptable  
Rate limiting (`RateLimitingFilter`) only protects `/api/v1/auth/login` (5 req/min per IP). Other endpoints are unprotected from abuse.

**Recommendation:** Consider adding rate limiting to other write endpoints (POST/PUT/DELETE) in future sprints.

### SEC-3: No Token Refresh / Revocation
**Status:** Known Limitation  
JWT tokens are valid for 24h with no refresh endpoint and no server-side revocation (blacklist). If a token is compromised, it remains valid until expiry.

**Recommendation:** Future sprint: implement refresh tokens + token blacklist (Redis).

---

## Performance Considerations

### PERF-1: N+1 Query Risk in Appointment Listing
**Status:** Low Risk  
`AppointmentService.listAllAppointments()` uses `findAllByTenantId()` which loads appointment entities but may trigger lazy-loading for `client` and `serviceItem` relationships.

**Recommendation:** Add `@EntityGraph` or `JOIN FETCH` query if performance degrades.

### PERF-2: In-Memory Rate Limiting Without Redis
**Status:** Acceptable (Development)  
When Redis is unavailable, rate limiting uses `ConcurrentHashMap`. In a multi-instance deployment, each instance would have its own bucket — effectively multiplying the rate limit.

**Recommendation:** Ensure Redis is always available in production.

---

## Fragile Areas

### FRAG-1: TenantContext ThreadLocal
The `TenantContext` relies on `ThreadLocal` cleared in `SecurityFilter.finally`. If any code path bypasses the filter (e.g., async processing, scheduled tasks), tenant isolation could leak.

### FRAG-2: Flyway Version Compatibility
Flyway 9.22.3 (bundled with Spring Boot 3.2.5) warns about PostgreSQL 16.14 being newer than the officially supported version. This is benign but should be monitored when upgrading PostgreSQL.

### FRAG-3: Appointment State Machine
The state machine in `AppointmentService` is implemented with if/else chains. As more states/transitions are added, this could become hard to maintain.

**Recommendation:** Consider extracting to a State Pattern or Spring Statemachine if complexity grows.
