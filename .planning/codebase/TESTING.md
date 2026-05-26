# Testing

> Last mapped: 2026-05-26

## Overview

| Metric | Value |
|--------|-------|
| Total Tests | **103** |
| Pass Rate | 100% (0 failures) |
| Coverage Tool | JaCoCo 0.8.10 |
| Coverage Report | `target/site/jacoco/index.html` |
| Test Framework | JUnit 5 + Mockito + MockMvc |

## Test Types

### 1. Unit Tests (Service Layer) — 57 tests
Testing business logic with mocked dependencies.

| Test Class | Tests | File |
|------------|:-----:|------|
| `BarberServiceTest` | 12 | `src/test/java/.../domain/barber/BarberServiceTest.java` |
| `ClientServiceTest` | 10 | `src/test/java/.../domain/client/ClientServiceTest.java` |
| `CatalogServiceTest` | 10 | `src/test/java/.../domain/catalog/CatalogServiceTest.java` |
| `AppointmentServiceTest` | 25 | `src/test/java/.../domain/appointment/AppointmentServiceTest.java` |

**Pattern:**
```java
@ExtendWith(MockitoExtension.class)
class BarberServiceTest {
    @Mock private BarberRepository barberRepository;
    @InjectMocks private BarberService barberService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("tenant-1");
        // Mock SecurityContextHolder if needed
    }
    @AfterEach
    void tearDown() { TenantContext.clear(); }
}
```

### 2. Controller Tests (MockMvc) — 19 tests
Testing HTTP routing, validation, and response format with `@WebMvcTest`.

| Test Class | Tests | File |
|------------|:-----:|------|
| `BarberControllerTest` | 6 | `src/test/java/.../domain/barber/BarberControllerTest.java` |
| `ClientControllerTest` | 3 | `src/test/java/.../domain/client/ClientControllerTest.java` |
| `CatalogControllerTest` | 5 | `src/test/java/.../domain/catalog/CatalogControllerTest.java` |
| `AppointmentControllerTest` | 5 | `src/test/java/.../domain/appointment/AppointmentControllerTest.java` |

**Pattern:**
```java
@WebMvcTest(BarberController.class)
@AutoConfigureMockMvc(addFilters = false)   // Disable security filters
class BarberControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private BarberService barberService;
    @MockBean private TokenService tokenService;  // Always mock
}
```

### 3. Integration Tests (E2E) — 12 tests
Full stack with real PostgreSQL, JWT, and security filter chain.

| Test Class | Tests | File |
|------------|:-----:|------|
| `BarberIntegrationTest` | 1 | `src/test/java/.../domain/barber/BarberIntegrationTest.java` |
| `ClientIntegrationTest` | 2 | `src/test/java/.../domain/client/ClientIntegrationTest.java` |
| `CatalogIntegrationTest` | 1 | `src/test/java/.../domain/catalog/CatalogIntegrationTest.java` |
| `AppointmentIntegrationTest` | 2 | `src/test/java/.../domain/appointment/AppointmentIntegrationTest.java` |
| `MultiTenantIntegrationTest` | 1 | `src/test/java/.../domain/MultiTenantIntegrationTest.java` |
| `AuthorizationTest` | 5 | `src/test/java/.../security/AuthorizationTest.java` |

**Base class:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {
    // Uses real PostgreSQL on localhost:5432 (Docker)
}
```

### 4. Security Tests — 12 tests

| Test Class | Tests | Type | File |
|------------|:-----:|------|------|
| `AuthorizationTest` | 5 | Integration | `src/test/java/.../security/AuthorizationTest.java` |
| `RateLimitingTest` | 3 | Unit | `src/test/java/.../security/RateLimitingTest.java` |
| `InputValidationTest` | 5 | WebMvcTest | `src/test/java/.../security/InputValidationTest.java` |
| `SecurityFilterTest` | 2 | Unit | `src/test/java/.../security/SecurityFilterTest.java` |

### 5. Smoke Test — 1 test
- `DhzSaasApiBackendApplicationTests` — Context loads check

## Mocking Strategy

### Service Tests
- `@Mock` on repositories, password encoder
- `TenantContext.setTenantId()` in `@BeforeEach`, `TenantContext.clear()` in `@AfterEach`
- `SecurityContextHolder` mocked for email extraction

### Controller Tests
- `@MockBean` on Services and `TokenService`
- `@AutoConfigureMockMvc(addFilters = false)` to skip security filter chain
- Tests focus on routing, validation, and response format

### Integration Tests
- NO mocks — real database, real security, real JWT
- Token generation via `TokenService.generateToken(email, tenantId, role)`
- Requests include `Authorization: Bearer {token}` header

## Test Data Patterns

### Passwords
All test passwords must be ≥6 characters (JSR-380): `"password123"`

### Phone Numbers
Must match regex `^\(\d{2}\)\s?9?\d{4}-?\d{4}$`: `"(11) 99999-9999"`

### Tenant Isolation in Tests
```java
// Unit tests: set manually
TenantContext.setTenantId("tenant-1");

// Integration tests: derived from registered ADMIN's barber entity
Barber admin = barberRepository.findByEmail(adminEmail).orElseThrow();
String tenantId = admin.getTenantId();
String token = tokenService.generateToken(adminEmail, tenantId, "ADMIN");
```

## Running Tests

```bash
# Full suite (103 tests)
.\mvnw.cmd clean test

# Specific class
.\mvnw.cmd test -Dtest="BarberServiceTest"

# Security tests only
.\mvnw.cmd test -Dtest="AuthorizationTest,RateLimitingTest,InputValidationTest"

# Coverage report
# Generated automatically at target/site/jacoco/index.html
```
